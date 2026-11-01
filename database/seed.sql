TRUNCATE leaks, pipelines, nodes RESTART IDENTITY CASCADE;
INSERT INTO nodes(id,name,type,capacity,current_level,demand,available_water) VALUES
('T-01','North Reservoir','TANK',1200,980,0,0),('T-02','Hilltop Tank','TANK',700,510,0,0),
('A-01','Riverside','AREA',0,0,160,145),('A-02','Old Town','AREA',0,0,230,155),
('A-03','Market District','AREA',0,0,300,205),('A-04','East Gardens','AREA',0,0,180,180),('A-05','South Ward','AREA',0,0,250,170);
INSERT INTO pipelines(source_id,destination_id,distance,capacity,status) VALUES
('T-01','A-01',3.2,220,'ACTIVE'),('T-01','A-02',5.4,260,'ACTIVE'),('A-01','A-02',2.1,100,'ACTIVE'),
('A-02','A-03',3.6,180,'ACTIVE'),('T-02','A-03',4.8,200,'ACTIVE'),('T-02','A-04',2.7,160,'ACTIVE'),
('A-04','A-03',3.9,90,'FAILED'),('A-03','A-05',2.5,140,'ACTIVE');
INSERT INTO leaks(pipeline_id,location,description,severity,estimated_loss,reporter,status,created_at) VALUES
(1,'Riverside main','Visible leak near the junction valve',5,38,'A. Kumar','IN PROGRESS',NOW()-INTERVAL '2 hours'),
(4,'Market District','Pressure drop reported by residents',4,22,'Market resident','REPORTED',NOW()-INTERVAL '45 minutes'),
(NULL,'East Gardens, block 2','Small surface seepage',2,5,'R. Shah','UNDER REVIEW',NOW()-INTERVAL '1 day');
