package com.klaiber.backmeup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

public class H2EmbeddedConnector implements DBConnector {

	private Connection con;
	private boolean active = false;
	private Logger log = LogHandler.getLogger();
	
	@Override
	public boolean open(String ConnectString) {
		try{
			Class.forName("org.h2.Driver");
			con = DriverManager.getConnection(ConnectString, "", "");			
		} catch (Exception e) {
			return false;
		}
		active = true;
		return true;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> getDirectories(int group) {
		Set<String> result = new HashSet<String>();
		try {
			PreparedStatement stmt = con.prepareStatement("Select PATH from DIRECTORY where BACKUPGROUP_ID = ?");
			stmt.setInt(1, group);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				result.add(rs.getString(1));
			}
			rs.close();
			stmt.close();
		} catch ( Exception e) {
			return null;
		}				
		return result;
	}

	@Override
	public Map<Integer, String> getGroups() {
		Map<Integer, String> result = new TreeMap<Integer,String>();
		try {
			PreparedStatement stmt = con.prepareStatement("Select ID, NAME from BACKUPGROUP ORDER BY ID ASC");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				result.put(rs.getInt(1), rs.getString(2));
			}
			rs.close();
			stmt.close();
		} catch ( Exception e) {
			return null;
		}				
		return result;
	}

	@Override
	public int createRun(int group) {
		int result = -1;
		try {
			PreparedStatement stmt = con.prepareStatement("INSERT INTO RUNS (BACKUPGROUP_ID, TIME_STARTED) VALUES (?, ?)");
			stmt.setInt(1, group);
			stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			stmt.execute();
			ResultSet generatedKeys = stmt.getGeneratedKeys();
			if (generatedKeys.next()) {
			    result = generatedKeys.getInt(1);
			}
			generatedKeys.close();
			stmt.close();
		} catch ( Exception e) {
			return -1;
		}
		return result;
	}

	@Override
	public Run getRun(int run) {		
		Run result = null;
		try {
			PreparedStatement stmt = con.prepareStatement("Select * from runs where id = ?");
			stmt.setInt(1, run);
			ResultSet rs = stmt.executeQuery();			
			if (rs.next()) {
			    int id = rs.getInt("ID");
				int backupgroup_id = rs.getInt("BACKUPGROUP_ID");
			    Date started = rs.getTimestamp("time_started");
			    if (rs.wasNull()) started = null;
			    Date finished = rs.getTimestamp("time_finished");
			    if (rs.wasNull()) finished = null;
			    Boolean success = rs.getBoolean("sucessful");
			    result = new Run(id,backupgroup_id,started,finished,success);
			}
			rs.close();
			stmt.close();

			
		} catch (Exception ex) {
			return null;
		}
		
		return result;
	}


	@Override
	public Map<Integer, Run> getAllRuns(int group) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, Run> getUnfinishedRuns(int group) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, Run> getFinishedRuns(int group) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, Run> getSuccesfulRuns(int group) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, Run> getFailedRuns(int group) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean finishRun(int run, boolean success) {
		
		try {
			PreparedStatement stmt = con.prepareStatement("UPDATE RUNS SET TIME_FINISHED = ?, SUCESSFUL = ? WHERE ID = ?");
			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			stmt.setBoolean(2, success);
			stmt.setInt(3, run);
			if (stmt.executeUpdate() < 1) return false;			
			stmt.close();
		} catch ( Exception e) {
			return false;
		}
		return true;
		
	}

	@Override
	public synchronized boolean addBackupItem(int run, String path, long size, long modified) {
		try {
			//log.info("Adding Item ["+path+"] to run ["+run+"] Size:["+size+"] TimeStamp:["+modified+"]" );
			PreparedStatement stmt = con.prepareStatement("INSERT INTO BACKUPITEMS (RUN_ID, PATH, SIZE, LASTMODIFIED) VALUES (?, ?, ?, ?)");
			stmt.setInt(1, run);
			stmt.setString(2,path);
			stmt.setLong(3, size);
			stmt.setLong(4, modified);
			stmt.execute();
			stmt.close();
		} catch ( Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public long checkForUnmodifiedFiles(int run) {
		long result = 0;
		try {
			
			//Find run to compare to
			int comparerun = -1;
			PreparedStatement stmt = con.prepareStatement("SELECT ID FROM RUNS WHERE BACKUPGROUP_ID in (SELECT BACKUPGROUP_ID FROM RUNS WHERE ID =?) AND TIME_FINISHED IS NOT NULL and SUCESSFUL = true order by TIME_FINISHED DESC LIMIT 1");
			stmt.setInt(1, run);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
			    comparerun = rs.getInt(1);
			}
			rs.close();
			stmt.close();
			
			if (comparerun < 1) return -2;
			
			stmt = con.prepareStatement("Select c.item_id as item_id, c.hash as hash, n.id as id from " + 
			"(Select * from backupitems where run_id =?) as c " +
			"inner join (Select * from backupitems where run_id =?) as n " +
			"on c.path = n.path and c.SIZE = n.SIZE and c.lastmodified = n.lastmodified ");
			stmt.setInt(1, comparerun);
			stmt.setInt(2, run);
			rs = stmt.executeQuery();
			
			PreparedStatement stmt2 = con.prepareStatement("UPDATE backupitems SET ITEM_ID = ?, HASH = ? WHERE id = ?");
			
			while (rs.next()){
				
				stmt2.setInt(1, rs.getInt(1));
				stmt2.setString(2, rs.getString(2));
				stmt2.setInt(3, rs.getInt(3));
				result = result + stmt2.executeUpdate();
				stmt2.clearParameters();
			}
			
			stmt2.close();
			rs.close();
			stmt.close();
			
		} catch ( Exception e) {
			return -1;
		}
		return result;

	}

	@Override
	public long matchHashes(int run) {
		Run r = this.getRun(run);
		
		long result = 0;
		result = matchOnHash(r, result);
		if (result < 0) return result;
		
		try {
			PreparedStatement stmt = con.prepareStatement("Insert into items(backupgroup_id, hash) " +
					"SELECT ? as backupgroup_id, hash from backupitems " +
					"where run_id = ? and ITEM_ID is null " );
			stmt.setInt(1, r.getBackupgroup());
			stmt.setInt(2, r.getId());
			
			result = result + stmt.executeUpdate();
			
			stmt.close();
			
			
			
		} catch ( Exception e) {
			e.printStackTrace();
			return -1;
		}	
		
		result = matchOnHash(r, result);
		return result; 
		


	}

	private long matchOnHash(Run run, long result) {
		try {
					
			//search for matching hashes and add Item_id if found
			PreparedStatement stmt = con.prepareStatement("Select b.id as bid, i.id as iid from " +
					"items i " +
					"inner join backupitems b " +
					"on i.hash = b.hash " +
					"where i.backupgroup_id = ? and b.ITEM_ID is null ");
			stmt.setInt(1, run.getBackupgroup());
			ResultSet rs = stmt.executeQuery();
			
			PreparedStatement stmt2 = con.prepareStatement("UPDATE backupitems SET ITEM_ID = ? WHERE id = ?");
			
			while (rs.next()){
				
				stmt2.setInt(1, rs.getInt(2));
				stmt2.setInt(2, rs.getInt(1));
				result = result + stmt2.executeUpdate();
				stmt2.clearParameters();
			}
			
			stmt2.close();
			rs.close();
			stmt.close();
			
			
			
		} catch ( Exception e) {
			e.printStackTrace();
			return -1;
		}
		return result;
	}

	@Override
	public Set<BackupItem> getUnsavedItems(int run, boolean external) {
		int count = 0;
		Set<BackupItem> result = new LinkedHashSet<BackupItem>();
		try {
			Run r = getRun(run);
			PreparedStatement stmt = con.prepareStatement("Select min(b.id), max(b.run_id), b.item_ID, min(b.path), "+
														  "i.hash, min(b.size), min(b.lastmodified), i.drive1_id, "+
														  "i.drive2_id from Backupitems b inner join items i "+
														  "on (b.item_id = i.id) where backupgroup_id = "
														  + "? and Drive"+(external?2:1)+"_ID is null "+
														  "group by i.hash having max(run_id)=? order by size desc");
			stmt.setInt(1, r.getBackupgroup());
			stmt.setInt(2, run);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				count++;
			    int id = rs.getInt(1);
				int run_id = rs.getInt(2);
			    Integer item_id = rs.getInt(3);
			    if (rs.wasNull()) item_id = null;
			    String path = rs.getString(4);
			    String hash = rs.getString(5);
			    long size = rs.getLong(6);
			    long lastModified = rs.getLong(7);
			    Integer drive1 = rs.getInt(8);
			    if (rs.wasNull()) drive1 = null;
			    Integer drive2 = rs.getInt(9);
			    if (rs.wasNull()) drive2 = null;
			    BackupItem bi = new BackupItem(this,id,run_id,item_id,path,hash,size,lastModified,drive1,drive2);
			    result.add(bi);
			}
			rs.close();
			stmt.close();
			log.info("Num of Files to Backup: ["+count+"]");

			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		return result;
	}

	@Override
	public boolean setItemSaved(BackupItem item, String driveName, boolean external) {
		try {
			PreparedStatement stmt = con.prepareStatement("Select id from drives where name = ?");
			stmt.setString(1, driveName);
			ResultSet rs = stmt.executeQuery();			
			if (rs.next()) {
				return setItemSaved(item, rs.getInt("id"), external);
			} else {
				return false;
			}
		} catch (Exception ex) {
			return false;
		} 
		
		
	}
	
	@Override
	public boolean setItemSaved(BackupItem item, int drive, boolean external) {
		try {
			PreparedStatement stmt = con.prepareStatement("UPDATE items SET Drive"+(external?2:1)+"_ID = ? WHERE id = ?");
			stmt.setInt(1, drive);
			stmt.setInt(2,item.getItem_id());
			int result = stmt.executeUpdate();
			if (result != 1) return false;
		} catch (Exception ex) {
			return false;
		}
		
		return true;
	}
	

	@Override
	public Set<BackupItem> getUnhashedItems(int run) {
		Set<BackupItem> result = new HashSet<BackupItem>();
		try {
			PreparedStatement stmt = con.prepareStatement("Select * from backupitems where run_id = ? and item_id is null");
			stmt.setInt(1, run);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
			    int id = rs.getInt("ID");
				int run_id = rs.getInt("RUN_ID");
			    Integer item_id = rs.getInt("ITEM_ID");
			    if (rs.wasNull()) item_id = null;
			    String path = rs.getString("PATH");
			    String hash = rs.getString("HASH");
			    long size = rs.getLong("SIZE");
			    long lastModified = rs.getLong("LASTMODIFIED");
			    BackupItem bi = new BackupItem(this,id,run_id,item_id,path,hash,size,lastModified,null,null);
			    result.add(bi);
			}
			rs.close();
			stmt.close();

			
		} catch (Exception ex) {
			return null;
		}
		
		return result;
	}

	@Override
	public boolean setBackupItemHash(BackupItem backupItem) {
		try {
			PreparedStatement stmt = con.prepareStatement("UPDATE backupitems SET HASH = ? WHERE id = ?");
			stmt.setString(1, backupItem.getHash());
			stmt.setInt(2, backupItem.getId());
			int result = stmt.executeUpdate();
			if (result != 1) return false;
		} catch (Exception ex) {
			return false;
		}
		
		return true;
	}

	@Override
	public Set<String> getUsableDrives(int group, boolean external) {
		Set<String> result = new LinkedHashSet<String>();
		try {			
			PreparedStatement stmt = con.prepareStatement("Select Name from DRIVES d inner join DRIVES_GROUPS dg on (d.id = dg.drive_id) "+
														"where dg.group_id = ? and d.drivefull = false and d.extern = ? order by id asc");
			
			stmt.setInt(1, group);
			stmt.setBoolean(2, external);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
			    String driveName = rs.getString(1);
			    result.add(driveName);				
			}
			rs.close();
			stmt.close();

			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		return result;
	}

	@Override
	public int setDriveFull(String driveName) {
		try {
			PreparedStatement stmt = con.prepareStatement("UPDATE drives SET drivefull = true WHERE name = ?");
			stmt.setString(1, driveName);
			int result = stmt.executeUpdate();
			if (result != 1) return -1;
		} catch (Exception ex) {
			return -1;
		}
		
		return 0;
	}

	@Override
	public String getXmlRepresentation(int run) {
		StringBuffer result = new StringBuffer();
		Run r = getRun(run);
		result.append("<backmeup>\n\t<run>\n");
		result.append("\t\t<backupgroup>"+r.getBackupgroup()+"</backupgroup>\n");
		result.append("\t\t<started>"+r.getStarted().getTime()+"</started>\n");
		result.append("\t\t<finished>"+r.getFinished().getTime()+"</finished>\n");
		if (r.isSuccess()) result.append("\t\t<successful/>\n");
		
		try {
		
			PreparedStatement stmt = con.prepareStatement("Select b.path, b.hash, b.size, b.lastmodified, d1.name, d2.name "
															+ "from backupitems b "
															+ "inner join items i "
															+ "on (b.item_id = i.id) "
															+ "inner join drives d1 "
															+ "on (i.drive1_id = d1.id) "
															+ "inner join drives d2 "
															+ "on (i.drive2_id = d2.id) "
															+ "where b.run_id = ? "
															+ "order by path");
			stmt.setInt(1, run);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				result.append("\t\t<item>\n");
				result.append("\t\t\t<path>"+rs.getString(1)+"</path>\n");
				result.append("\t\t\t<hash>"+rs.getString(2)+"</hash>\n");
				result.append("\t\t\t<size>"+rs.getLong(3)+"</size>\n");
				result.append("\t\t\t<lastmodified>"+rs.getLong(4)+"</lastmodified>\n");
				result.append("\t\t\t<drive1>"+rs.getString(5)+"</drive1>\n");
				result.append("\t\t\t<drive2>"+rs.getString(6)+"</drive2>\n");
				result.append("\t\t</item>\n");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		result.append("\t</run>\n</backmeup>");
		return result.toString();
	}

}
