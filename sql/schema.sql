-- Airport Air Traffic Control & Gate Allocation System (MySQL 8.0.16+)
DROP DATABASE IF EXISTS atc_db;
CREATE DATABASE atc_db;
USE atc_db;

CREATE TABLE users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(30) NOT NULL UNIQUE,
  password_hash CHAR(64) NOT NULL,
  role ENUM('ADMIN','ATC','GATE_MANAGER') NOT NULL
);
CREATE TABLE airlines (
  airline_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(60) NOT NULL,
  code VARCHAR(3) NOT NULL UNIQUE
);
CREATE TABLE aircraft (
  aircraft_id INT AUTO_INCREMENT PRIMARY KEY,
  registration_no VARCHAR(15) NOT NULL UNIQUE,
  model VARCHAR(30) NOT NULL,
  size_category ENUM('S','M','L') NOT NULL,
  airline_id INT NOT NULL,
  FOREIGN KEY (airline_id) REFERENCES airlines(airline_id)
);
CREATE TABLE flights (
  flight_id INT AUTO_INCREMENT PRIMARY KEY,
  flight_no VARCHAR(10) NOT NULL UNIQUE,
  aircraft_id INT NOT NULL,
  origin VARCHAR(40) NOT NULL,
  destination VARCHAR(40) NOT NULL,
  sched_arrival DATETIME NOT NULL,
  sched_departure DATETIME NOT NULL,
  status ENUM('SCHEDULED','DELAYED','LANDED','DEPARTED','CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
  FOREIGN KEY (aircraft_id) REFERENCES aircraft(aircraft_id),
  CONSTRAINT chk_times CHECK (sched_departure > sched_arrival)
);
CREATE TABLE runways (
  runway_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(10) NOT NULL UNIQUE,
  length_m INT NOT NULL CHECK (length_m > 0),
  status ENUM('ACTIVE','CLOSED','MAINTENANCE') NOT NULL DEFAULT 'ACTIVE'
);
CREATE TABLE gates (
  gate_id INT AUTO_INCREMENT PRIMARY KEY,
  terminal VARCHAR(5) NOT NULL,
  gate_no VARCHAR(5) NOT NULL,
  size_capacity ENUM('S','M','L') NOT NULL,
  status ENUM('FREE','OCCUPIED','MAINTENANCE') NOT NULL DEFAULT 'FREE',
  UNIQUE (terminal, gate_no)
);
CREATE TABLE gate_allocations (
  alloc_id INT AUTO_INCREMENT PRIMARY KEY,
  flight_id INT NOT NULL UNIQUE,
  gate_id INT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  FOREIGN KEY (flight_id) REFERENCES flights(flight_id) ON DELETE CASCADE,
  FOREIGN KEY (gate_id) REFERENCES gates(gate_id),
  CONSTRAINT chk_alloc CHECK (end_time > start_time)
);
CREATE TABLE runway_assignments (
  assign_id INT AUTO_INCREMENT PRIMARY KEY,
  flight_id INT NOT NULL,
  runway_id INT NOT NULL,
  operation ENUM('LAND','TAKEOFF') NOT NULL,
  slot_time DATETIME NOT NULL,
  FOREIGN KEY (flight_id) REFERENCES flights(flight_id) ON DELETE CASCADE,
  FOREIGN KEY (runway_id) REFERENCES runways(runway_id),
  UNIQUE (flight_id, operation)
);

-- ---------- Sample data ----------
INSERT INTO users(username,password_hash,role) VALUES
 ('admin',SHA2('admin123',256),'ADMIN'),
 ('atc1',SHA2('atc123',256),'ATC'),
 ('gate1',SHA2('gate123',256),'GATE_MANAGER');

INSERT INTO airlines(airline_id,name,code) VALUES
 (1,'Air India','AI'),(2,'IndiGo','6E'),(3,'Emirates','EK'),(4,'Singapore Airlines','SQ'),(5,'SpiceJet','SG');

INSERT INTO aircraft(aircraft_id,registration_no,model,size_category,airline_id) VALUES
 (1,'VT-ALK','A320','M',1),(2,'VT-IZA','A321','M',2),(3,'VT-IJB','ATR72','S',2),(4,'A6-EOA','B777-300ER','L',3),
 (5,'9V-SMA','A350-900','L',4),(6,'VT-SGA','B737-800','M',5),(7,'VT-ANB','B787-8','L',1),(8,'VT-SPC','Q400','S',5);

INSERT INTO flights(flight_id,flight_no,aircraft_id,origin,destination,sched_arrival,sched_departure,status) VALUES
 (1,'AI101',1,'Delhi','Mumbai','2026-10-01 06:00','2026-10-01 07:30','SCHEDULED'),
 (2,'6E202',2,'Hyderabad','Kolkata','2026-10-01 06:30','2026-10-01 08:00','SCHEDULED'),
 (3,'6E305',3,'Coimbatore','Madurai','2026-10-01 07:00','2026-10-01 08:15','SCHEDULED'),
 (4,'EK543',4,'Dubai','Dubai','2026-10-01 08:00','2026-10-01 10:30','SCHEDULED'),
 (5,'SQ528',5,'Singapore','Singapore','2026-10-01 09:00','2026-10-01 11:00','DELAYED'),
 (6,'SG812',6,'Bengaluru','Delhi','2026-10-01 09:30','2026-10-01 11:00','SCHEDULED'),
 (7,'AI987',7,'London','London','2026-10-01 10:00','2026-10-01 13:00','SCHEDULED'),
 (8,'SG455',8,'Tirupati','Bengaluru','2026-10-01 10:30','2026-10-01 11:30','SCHEDULED');

INSERT INTO runways(runway_id,name,length_m,status) VALUES
 (1,'07/25',3658,'ACTIVE'),(2,'12/30',2940,'ACTIVE'),(3,'03/21',1800,'CLOSED');

INSERT INTO gates(gate_id,terminal,gate_no,size_capacity,status) VALUES
 (1,'T1','A1','S','FREE'),(2,'T1','A2','S','FREE'),(3,'T1','A3','M','OCCUPIED'),(4,'T1','A4','M','OCCUPIED'),
 (5,'T2','B1','M','FREE'),(6,'T2','B2','L','OCCUPIED'),(7,'T2','B3','L','FREE'),(8,'T2','B4','L','FREE'),
 (9,'T3','C1','L','FREE'),(10,'T3','C2','M','MAINTENANCE');

INSERT INTO gate_allocations(flight_id,gate_id,start_time,end_time) VALUES
 (1,3,'2026-10-01 06:00','2026-10-01 07:30'),
 (2,4,'2026-10-01 06:30','2026-10-01 08:00'),
 (4,6,'2026-10-01 08:00','2026-10-01 10:30');

INSERT INTO runway_assignments(flight_id,runway_id,operation,slot_time) VALUES
 (1,1,'LAND','2026-10-01 06:00'),(1,1,'TAKEOFF','2026-10-01 07:30'),(2,2,'LAND','2026-10-01 06:30');

-- Useful demo query: free gates between 10:00 and 12:00
-- SELECT * FROM gates g WHERE g.status<>'MAINTENANCE' AND NOT EXISTS (
--   SELECT 1 FROM gate_allocations a WHERE a.gate_id=g.gate_id
--   AND a.start_time < '2026-10-01 12:00' AND a.end_time > '2026-10-01 10:00');
