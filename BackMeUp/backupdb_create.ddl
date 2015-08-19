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

Create index itm_hash_idx on items(hash);
Create index itm_bug_idx on items(backupgroup_id);

CREATE TABLE  backupitems (
  id integer NOT NULL AUTO_INCREMENT primary key,
  run_id integer DEFAULT NULL,
  item_id integer DEFAULT NULL,
  path varchar(255) DEFAULT NULL,
  hash varchar(255) DEFAULT NULL,
  size bigint DEFAULT NULL,
  lastmodified bigint DEFAULT NULL
);

Create index unmodifiedItems on backupitems(path, size, lastmodified);
Create index bu_item_id_idx on backupitems(item_id);
Create index bu_hash_idx on backupitems(hash);

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
