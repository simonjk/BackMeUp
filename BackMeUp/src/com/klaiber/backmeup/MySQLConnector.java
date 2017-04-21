package com.klaiber.backmeup;

public class MySQLConnector extends AbstractDBConnector implements DBConnector {

	@Override
	public String getConfigValue(int key) {
		switch (key) {
			case DRIVERNAME:
				return "com.mysql.jdbc.Driver";
			case GETDIRSSELECT:
				return "Select PATH from DIRECTORY where BACKUPGROUP_ID = ?";
			case GETGROUPSSELECT:
				return "Select ID, NAME from BACKUPGROUP ORDER BY ID ASC";
			case CREATERUNINSERT:
				return "INSERT INTO RUNS (BACKUPGROUP_ID, TIME_STARTED) VALUES (?, ?)";
			case GETRUNSELECT:
				return "Select * from RUNS where id = ?";
			case FINISHRUNUPDATE:
				return "UPDATE RUNS SET TIME_FINISHED = ?, SUCESSFUL = ? WHERE ID = ?";
			case ADDBUIINSERT:
				return "INSERT INTO BACKUPITEMS (RUN_ID, PATH, FILESIZE, LASTMODIFIED) VALUES (?, ?, ?, ?)";
			case CHECKUNMODCREATE:
				return "create table NEWESTBU_###RUN### as "+
						"SELECT max(b.id) as id FROM BACKUPITEMS b "+
						"inner join RUNS r "+
						"on r.id = b.run_id "+
						"where r.backupgroup_id in (SELECT BACKUPGROUP_ID FROM RUNS WHERE ID = ?) "+					
						"and r.id < ? "+
						"and not b.hash is null "+
						"group by path";
			case CHECKUNMODUPDATE:
				return "Update BACKUPITEMS t "+
						"inner join " +
						"BACKUPITEMS as n " +
						"on  t.id = n.id " +
						"inner join BACKUPITEMS as c " +
						"on c.path = n.path and c.FILESIZE = n.FILESIZE and c.lastmodified = n.lastmodified " +
						"inner join NEWESTBU_###RUN### x " +
						"on c.id = x.id " +
						"SET t.item_id = c.item_id, t.hash=c.hash " +
						"where n.run_id = ?";
			case GETFILEFILTERSELECT:
				return "Select expression from FILTERS where (BACKUPGROUP_ID = ? OR BACKUPGROUP_ID is null) and file = 1";
			case GETDIRFILTERSELECT:
				return "Select expression from FILTERS where (BACKUPGROUP_ID = ? OR BACKUPGROUP_ID is null) and dir = 1";
			case MATCHHASHESINSERT:
				return "Insert into ITEMS(backupgroup_id, hash) " +
						"SELECT ? as backupgroup_id, hash from BACKUPITEMS " +
						"where run_id = ? and (ITEM_ID is null or ITEM_ID = 0) and not HASH is null ";
			case MATCHONHASHUPDATE:
				return "UPDATE BACKUPITEMS t " + 
						"inner join BACKUPITEMS b " + 
						"on t.id = b.id " + 
						"inner join ITEMS i " + 
						"on i.hash = b.hash " + 
						"SET b.ITEM_ID = i.id " + 
						"where i.backupgroup_id = ? and (b.ITEM_ID is null or b.ITEM_ID = 0)";
			case GETUNSAVEDSELECT:
				return "Select min(b.id), max(b.run_id), b.item_ID, min(b.path), "+
						  "i.hash, min(b.filesize) as size, min(b.lastmodified), i.drive1_id, "+
						  "i.drive2_id from BACKUPITEMS b inner join ITEMS i "+
						  "on (b.item_id = i.id) where backupgroup_id = "
						  + "? and Drive###DRIVENUM###_ID is null "+
						  "group by i.hash having max(run_id)=? order by filesize desc";
			case GETSAVEDSELECT:
				return "Select b.id, b.run_id, b.item_ID, b.path, "+
						  "i.hash, b.filesize as size, b.lastmodified, i.drive1_id, "+
						  "i.drive2_id from BACKUPITEMS b inner join ITEMS i "+
						  "on (b.item_id = i.id) where backupgroup_id = "
						  + "? and b.run_id=? and not Drive###DRIVENUM###_ID is null "+
						  "order by Drive###DRIVENUM###_ID asc, filesize desc";
			case SETSAVEDSELECT:
				return "Select id from DRIVES where name = ?";
			case SETSAVEDUPDATE:
				return "UPDATE ITEMS SET Drive###DRIVENUM###_ID = ? WHERE id = ?";
			case GETUNHASHEDSELECT:
				return "Select * from BACKUPITEMS where run_id = ? and item_id is null";
			case GETDRIVENAMESELECT:
				return "Select name from DRIVES where id = ?";
			case SETBUIHASHUPDATE:
				return "UPDATE BACKUPITEMS SET HASH = ? WHERE id = ?";
			case GETUSABLEDRIVESSELECT:
				return "Select Name from DRIVES d inner join DRIVES_GROUPS dg on (d.id = dg.drive_id) "+
						"where dg.group_id = ? and d.drivefull = false and d.extern = ? order by id asc";
			case SETDRIVEFULLUPDATE:
				return "UPDATE DRIVES SET drivefull = true WHERE name = ?";
			case GETXMLSELECT:
				return "Select b.path, b.hash, b.filesize, b.lastmodified, d1.name, d2.name "
						+ "from BACKUPITEMS b "
						+ "inner join ITEMS i "
						+ "on (b.item_id = i.id) "
						+ "inner join DRIVES d1 "
						+ "on (i.drive1_id = d1.id) "
						+ "inner join DRIVES d2 "
						+ "on (i.drive2_id = d2.id) "
						+ "where b.run_id = ? "
						+ "order by path";
			default:
				return "";
		}
	}

	
	
}
