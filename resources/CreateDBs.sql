CREATE DATABASE HowlServer;
USE HowlServer;

CREATE TABLE unauth
(
	time_of_attempt TIMESTAMP DEFAULT NOW(),
	remote_address VARCHAR(25) NOT NULL,
	info TEXT
);

CREATE TABLE connection_log
(
	time_of_connection TIMESTAMP DEFAULT NOW(),
	remote_address VARCHAR(25) NOT NULL,
	type VARCHAR(10) NOT NULL
);

CREATE TABLE location
(
	lat_long VARCHAR(30) NOT NULL,
	time_of_log TIMESTAMP DEFAULT NOW()
);
