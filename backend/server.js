require('dotenv').config();
const express = require('express');
const path = require('path');
const { Pool } = require('pg');
const session = require('express-session');
const PgSession = require('connect-pg-simple')(session);
const bcrypt = require('bcryptjs');
const { WaterGraph, MinHeap, LeakQueue, LeakPriorityQueue, LeakLookup } = require('./structures');

const app = express();
const pool = new Pool({ connectionString: process.env.DATABASE_URL });
app.use(express.json());
app.use(session({ store: new PgSession({ pool, createTableIfMissing: true }), secret: process.env.SESSION_SECRET || 'local-aquagrid-session-secret-change-this', resave: false, saveUninitialized: false, cookie: { httpOnly: true, sameSite: 'lax', secure: process.env.NODE_ENV === 'production', maxAge: 1000 * 60 * 60 * 12 } }));
const wrap = fn => (req, res, next) => Promise.resolve(fn(req, res, next)).catch(next);
const rows = async (sql, values = []) => (await pool.query(sql, values)).rows;
const frontend = path.join(__dirname, '..', 'frontend');
const requirePageAuth = (req, res, next) => req.session.user ? next() : res.redirect('/login.html');
const requireAuth = (req, res, next) => req.session.user ? next() : res.status(401).json({ error: 'Please sign in to continue.' });
const requireAdmin = (req, res, next) => req.session.user?.role === 'ADMIN' ? next() : res.status(403).json({ error: 'Admin access is required for this action.' });
app.get('/', (req, res) => res.redirect(req.session.user ? '/index.html' : '/login.html'));
app.get('/login', (req, res) => res.redirect(req.session.user ? '/index.html' : '/login.html'));
app.get('/index.html', requirePageAuth, (req, res) => res.sendFile(path.join(frontend, 'index.html')));
app.use(express.static(frontend));

app.get('/api/auth/me', (req, res) => res.json({ user: req.session.user || null }));
app.post('/api/auth/register', wrap(async (req, res) => {
  const displayName = String(req.body.displayName || '').trim();
  const email = String(req.body.email || '').trim().toLowerCase();
  const password = String(req.body.password || '');
  if (displayName.length < 2 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) || password.length < 10) return res.status(400).json({ error: 'Enter your name, a valid email, and a password with at least 10 characters.' });
  const passwordHash = await bcrypt.hash(password, 12);
  const result = await rows("INSERT INTO users(display_name,email,password_hash,role) VALUES($1,$2,$3,'USER') RETURNING id,display_name,email,role", [displayName,email,passwordHash]);
  req.session.user = { id: result[0].id, name: result[0].display_name, email: result[0].email, role: result[0].role };
  req.session.save(error => error ? res.status(500).json({ error: 'Could not start your session.' }) : res.status(201).json({ user: req.session.user }));
}));
app.post('/api/auth/login', wrap(async (req, res) => {
  const email = String(req.body.email || '').trim().toLowerCase();
  const password = String(req.body.password || '');
  const user = (await rows('SELECT * FROM users WHERE email=$1', [email]))[0];
  if (!user || !(await bcrypt.compare(password, user.password_hash))) return res.status(401).json({ error: 'Email or password is incorrect.' });
  req.session.regenerate(error => {
    if (error) return res.status(500).json({ error: 'Could not start your session.' });
    req.session.user = { id: user.id, name: user.display_name, email: user.email, role: user.role };
    req.session.save(saveError => saveError ? res.status(500).json({ error: 'Could not start your session.' }) : res.json({ user: req.session.user }));
  });
}));
app.post('/api/auth/logout', requireAuth, (req, res) => req.session.destroy(error => {
  if (error) return res.status(500).json({ error: 'Could not sign out.' });
  res.clearCookie('connect.sid', { httpOnly: true, sameSite: 'lax', secure: process.env.NODE_ENV === 'production' }).json({ ok: true });
}));
app.use('/api', requireAuth);

async function loadGraph() {
  const [nodes, edges] = await Promise.all([rows('SELECT * FROM nodes ORDER BY id'), rows('SELECT * FROM pipelines ORDER BY id')]);
  const graph = new WaterGraph();
  nodes.forEach(n => graph.addNode(n.id));
  edges.forEach(e => graph.addEdge(e));
  return { graph, nodes, edges };
}
app.get('/api/dashboard', wrap(async (req, res) => {
  const { nodes, edges } = await loadGraph();
  const leaks = await rows("SELECT * FROM leaks WHERE status NOT IN ('RESOLVED','REJECTED') ORDER BY created_at");
  const areas = nodes.filter(n => n.type === 'AREA');
  const critical = areas.filter(a => Number(a.demand) - Number(a.available_water) >= 30);
  const graph = new WaterGraph(); nodes.forEach(n => graph.addNode(n.id)); edges.forEach(e => graph.addEdge(e));
  const tanks = nodes.filter(n => n.type === 'TANK');
  const disconnected = areas.filter(a => !tanks.some(t => graph.bfs(t.id).includes(a.id)));
  res.json({ tanks: tanks.length, areas: areas.length, activePipelines: edges.filter(e => e.status === 'ACTIVE').length, failedPipelines: edges.filter(e => e.status === 'FAILED').length,
    shortageAreas: areas.filter(a => Number(a.demand) > Number(a.available_water)).length, criticalShortages: critical.length,
    pendingLeaks: leaks.length, criticalLeaks: leaks.filter(l => l.severity >= 4).length, disconnectedAreas: disconnected.length });
}));
app.get('/api/network', wrap(async (req, res) => { const { graph, nodes, edges } = await loadGraph(); res.json({ nodes, edges, adjacencyList: graph.toAdjacencyList() }); }));
app.post('/api/nodes', requireAdmin, wrap(async (req, res) => {
  const { id, name, type, capacity = 0, current_level = 0, demand = 0, available_water = 0 } = req.body;
  if (!id || !name || !['TANK','AREA'].includes(type)) return res.status(400).json({ error: 'Provide id, name, and type TANK or AREA.' });
  const result = await rows('INSERT INTO nodes(id,name,type,capacity,current_level,demand,available_water) VALUES($1,$2,$3,$4,$5,$6,$7) RETURNING *', [id.trim(),name.trim(),type,capacity,current_level,demand,available_water]); res.status(201).json(result[0]);
}));
app.delete('/api/nodes/:id', requireAdmin, wrap(async (req, res) => { const result = await pool.query('DELETE FROM nodes WHERE id=$1', [req.params.id]); res.status(result.rowCount ? 204 : 404).end(); }));
app.post('/api/pipelines', requireAdmin, wrap(async (req, res) => {
  const { source_id, destination_id, distance, capacity } = req.body;
  if (!source_id || !destination_id || !Number.isFinite(+distance) || +distance < 0 || !Number.isFinite(+capacity)) return res.status(400).json({ error: 'Provide endpoints, non-negative distance, and capacity.' });
  const result = await rows('INSERT INTO pipelines(source_id,destination_id,distance,capacity) VALUES($1,$2,$3,$4) RETURNING *', [source_id,destination_id,distance,capacity]); res.status(201).json(result[0]);
}));
app.patch('/api/pipelines/:id/status', requireAdmin, wrap(async (req,res) => { const status = req.body.status; if (!['ACTIVE','FAILED'].includes(status)) return res.status(400).json({error:'Status must be ACTIVE or FAILED.'}); const result = await rows('UPDATE pipelines SET status=$1 WHERE id=$2 RETURNING *',[status,req.params.id]); res.status(result.length?200:404).json(result[0] || {error:'Pipeline not found.'}); }));
app.delete('/api/pipelines/:id', requireAdmin, wrap(async (req,res) => { const result=await pool.query('DELETE FROM pipelines WHERE id=$1',[req.params.id]); res.status(result.rowCount?204:404).end(); }));
app.get('/api/areas', wrap(async (req,res) => { const areas=await rows("SELECT *, GREATEST(demand-available_water,0) AS shortage FROM nodes WHERE type='AREA' ORDER BY id"); res.json(areas.map(a=>({...a, classification:a.shortage >= 30?'Critical':a.shortage>0?'Shortage':'Sufficient'}))); }));
app.get('/api/analysis', wrap(async (req,res) => { const {graph,nodes,edges}=await loadGraph(); const tanks=nodes.filter(n=>n.type==='TANK'),areas=nodes.filter(n=>n.type==='AREA'); const served=[],unserved=[]; for(const area of areas){const routes=tanks.map(t=>({tank:t.id,...graph.dijkstra(t.id,area.id)})).filter(route=>route.path.length).sort((a,b)=>a.cost-b.cost);if(routes.length)served.push({id:area.id,name:area.name,path:routes[0].path,cost:routes[0].cost});else{const connections=edges.filter(e=>e.status==='ACTIVE'&&(e.source_id===area.id||e.destination_id===area.id)).map(e=>e.source_id===area.id?e.destination_id:e.source_id);unserved.push({id:area.id,name:area.name,reason:connections.length?`Connected by active pipeline to ${connections.join(', ')}, but no active route from a tank is available.`:'No active pipeline is connected to this area.'});}} res.json({components:graph.components(),served,unserved}); }));
app.get('/api/route', wrap(async (req,res) => { const {source,target}=req.query; if(!source||!target) return res.status(400).json({error:'Provide source and target.'}); const {graph}=await loadGraph(); const result=graph.dijkstra(source,target); res.json({...result, source,target}); }));
app.post('/api/leaks', wrap(async (req,res) => { const {pipeline_id=null,location,description,severity,estimated_loss=0}=req.body; if(!location||!description||!Number.isInteger(+severity)||severity<1||severity>5) return res.status(400).json({error:'Provide location, description, and severity from 1 to 5.'}); const user=req.session.user; const result=await rows('INSERT INTO leaks(pipeline_id,location,description,severity,estimated_loss,reporter,reporter_id) VALUES($1,$2,$3,$4,$5,$6,$7) RETURNING *',[pipeline_id,location,description,severity,estimated_loss,user.name,user.id]); res.status(201).json(result[0]); }));
app.get('/api/leaks', wrap(async (req,res) => { const all=await rows(`SELECT * FROM leaks ${req.query.mine==='1'?'WHERE reporter_id=$1':''} ORDER BY created_at`,req.query.mine==='1'?[req.session.user.id]:[]); const queue=new LeakQueue(), heap=new LeakPriorityQueue(), lookup=new LeakLookup(); all.forEach(l=>{queue.enqueue(l);heap.enqueue(l);lookup.set(l.id,l)}); res.json({reports:all,queue:queue.toArray(),priorityQueue:heap.toArray(),lookupSize:lookup.size}); }));
app.get('/api/leaks/lookup/:id', wrap(async (req,res) => { const all=await rows('SELECT * FROM leaks ORDER BY created_at'); const lookup=new LeakLookup(); all.forEach(l=>lookup.set(l.id,l)); const report=lookup.get(req.params.id); res.status(report?200:404).json(report||{error:'Leak report not found.'}); }));
app.get('/api/leaks/:id', wrap(async (req,res) => { const report=(await rows('SELECT * FROM leaks WHERE id=$1',[req.params.id]))[0]; res.status(report?200:404).json(report||{error:'Leak report not found.'}); }));
app.patch('/api/leaks/:id', requireAdmin, wrap(async (req,res) => { const {status,resolution_note=''}=req.body; const allowed=['REPORTED','UNDER REVIEW','IN PROGRESS','RESOLVED','REJECTED']; if(!allowed.includes(status)) return res.status(400).json({error:'Invalid leak status.'}); if(status==='RESOLVED'&&!resolution_note.trim()) return res.status(400).json({error:'Resolution note is required when resolving a report.'}); const result=await rows("UPDATE leaks SET status=$1,resolution_note=$2,resolved_at=CASE WHEN $1='RESOLVED' THEN NOW() ELSE NULL END,updated_at=NOW() WHERE id=$3 RETURNING *",[status,resolution_note,req.params.id]); res.status(result.length?200:404).json(result[0]||{error:'Leak report not found.'}); }));
app.use((err,req,res,next)=>{ console.error(err); res.status(err.code==='23505'?409:err.code==='23503'?400:500).json({error:err.code==='23505'?'That ID already exists.':err.code==='23503'?'A referenced node or pipeline does not exist.':'Request failed. Check the server log.'}); });
app.listen(process.env.PORT||3000,()=>console.log(`AquaGrid listening on http://localhost:${process.env.PORT||3000}`));
