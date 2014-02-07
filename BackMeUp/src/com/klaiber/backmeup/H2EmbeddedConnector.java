package com.klaiber.backmeup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

public class H2EmbeddedConnector implements DBConnector {

	private Connection con;
	private boolean active = false;
	private Logger log = Logger.getLogger("main");
	
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
			PreparedStatement stmt = con.prepareStatement("UPDATE RUN SET TIME_FINISHED = ?, SUCESSFUL = ? WHERE ID = ?");
			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			stmt.setBoolean(2, success);
			stmt.setInt(3, run);
			if (stmt.executeUpdate() < 1) return false;;			
			stmt.close();
		} catch ( Exception e) {
			return false;
		}
		return true;
		
	}

	@Override
	public synchronized boolean addBackupItem(int run, String path, long size, long modified) {
		try {
			log.info("Adding Item ["+path+"] to run ["+run+"] Size:["+size+"] TimeStamp:["+modified+"]" );
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setItemSaved(BackupItem item, int drive, boolean external) {
		// TODO Auto-generated method stub
		return false;
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

}
