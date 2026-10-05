<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dao.NodeDAO, java.util.*" %>
<html>
<head><title>Network</title><link rel="stylesheet" href="css/style.css"></head>
<body>
<nav class="navbar"><h2>💧 Water Network</h2>
  <div><a href="index.html">Home</a> <a href="route.jsp">Find Route</a> <a href="leaks.jsp">Leaks</a></div>
</nav>

<h2>Add Tank / Area</h2>
<form action="addNode" method="post">
  ID: <input name="id" required>
  Name: <input name="name" required>
  Type: <select name="type"><option value="TANK">Tank</option><option value="AREA">Area</option></select>
  Val1 (capacity/demand): <input name="val1" type="number" step="any" required>
  Val2 (current/available): <input name="val2" type="number" step="any" required>
  <button type="submit">Add</button>
</form>

<h2>Add Pipeline</h2>
<form action="addPipeline" method="post">
  From ID: <input name="fromId" required>
  To ID: <input name="toId" required>
  Weight: <input name="weight" type="number" step="any" required>
  <button type="submit">Add</button>
</form>

<h2>All Nodes</h2>
<table>
<tr><th>ID</th><th>Name</th><th>Type</th><th>Val1</th><th>Val2</th></tr>
<%
  List<Map<String,Object>> nodes = new NodeDAO().getAllNodes();
  for (Map<String,Object> n : nodes) {
%>
<tr><td><%=n.get("id")%></td><td><%=n.get("name")%></td><td><%=n.get("type")%></td><td><%=n.get("val1")%></td><td><%=n.get("val2")%></td></tr>
<% } %>
</table>
</body>
</html>