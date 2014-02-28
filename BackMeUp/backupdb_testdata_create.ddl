CREATE TABLE drives (
  id integer  AUTO_INCREMENT NOT NULL PRIMARY KEY ,
  name varchar(255) DEFAULT NULL,
  drivefull boolean DEFAULT NULL,
  extern boolean DEFAULT NULL
 );

CREATE TABLE  drives_groups (
  drive_id integer NOT NULL ,
  group_id integer NOT NULL ,
  PRIMARY KEY (drive_id,group_id)
);

CREATE TABLE  items (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  backupgroup_id integer DEFAULT NULL,
  hash varchar(255) DEFAULT NULL,
  drive1_id integer DEFAULT NULL,
  drive2_id integer DEFAULT NULL
);

CREATE TABLE  backupitems (
  id integer NOT NULL AUTO_INCREMENT primary key,
  run_id integer DEFAULT NULL,
  item_id integer DEFAULT NULL,
  path varchar(255) DEFAULT NULL,
  hash varchar(255) DEFAULT NULL,
  size bigint DEFAULT NULL,
  lastmodified bigint DEFAULT NULL
);

CREATE TABLE  runs (
  id integer NOT NULL AUTO_INCREMENT Primary Key,
  backupgroup_id integer DEFAULT NULL,
  time_started datetime DEFAULT NULL,
  time_finished datetime DEFAULT NULL,
  sucessful boolean DEFAULT NULL,  
);

create table directory (
id integer Auto_Increment Primary key,
path varchar(2000),
backupgroup_id integer,
recursive boolean
);

create table backupgroup (
id integer Auto_Increment Primary key,
name varchar(255)
);

INSERT INTO BACKUPGROUP VALUES (1,'test1');
INSERT INTO directory VALUES (null, 'testdata/test1_1/', 1, true);
INSERT INTO directory VALUES (null, 'testdata/test1_2/', 1, true);
INSERT INTO directory VALUES (null, 'testdata/test1_3/', 1, false);
INSERT INTO drives VALUES (1, 'testtarget/d1_1/', false, false);
INSERT INTO drives VALUES (2, 'testtarget/d1_2/', false, true);
INSERT INTO drives_groups VALUES (1,1);
INSERT INTO drives_groups VALUES (2,1);
