package com.klaiber.backmeup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

public abstract class AbstractDBConnector implements DBConnector{

	private Connection con;
	private boolean active = false;
	private Logger log = LogHandler.getLogger();
	
	public final int DRIVERNAME = 1;
	public final int GETDIRSSELECT = 2;
	public final int GETGROUPSSELECT = 3;
	public final int CREATERUNINSERT = 4;
	public final int GETRUNSELECT = 5;
	public final int FINISHRUNUPDATE = 6;
	public final int ADDBUIINSERT = 7;
	public final int CHECKUNMODCREATE = 8;
	public final int CHECKUNMODUPDATE = 9;
	public final int GETFILEFILTERSELECT = 10;
	public final int GETDIRFILTERSELECT = 11;
	public final int MATCHHASHESINSERT = 12;
	public final int MATCHONHASHUPDATE = 13;
	public final int GETUNSAVEDSELECT = 14;
	public final int GETSAVEDSELECT = 15;
	public final int SETSAVEDSELECT = 16;
	public final int SETSAVEDUPDATE = 17;
	public final int GETUNHASHEDSELECT = 18;
	public final int GETDRIVENAMESELECT = 19;
	public final int SETBUIHASHUPDATE = 20;
	public final int GETUSABLEDRIVESSELECT = 21;
	public final int SETDRIVEFULLUPDATE = 22;
	public final int GETXMLSELECT = 23;
	
	public abstract String getConfigValue(int key);
		
	
	public AbstractDBConnector() {
		super();
	}

	@Override
	public boolean open(String ConnectString) {
		try{
			Class.forName(getConfigValue(DRIVERNAME));
			con = DriverManager.getConnection(ConnectString, "usr", "pwd");			
		} catch (Exception e) {
			e.printStackTrace();
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
			PreparedStatement stmt = con.prepareStatement(getConfigValue(GETDIRSSELECT));
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
			PreparedStatement stmt = con.prepareStatement(getConfigValue(GETGROUPSSELECT));
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
			PreparedStatement stmt = con.prepareStatement(getConfigValue(CREATERUNINSERT), Statement.RETURN_GENERATED_KEYS);
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
			e.printStackTrace();
			return -1;
		}		
		return result;
	}

	@Override
	public Run getRun(int run) {		
		Run result = null;
		try {
			PreparedStatement stmt = con.prepareStatement(getConfigValue(GETRUNSELECT));
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
	
			
		} catch (Exception e) {
			e.printStackTrace();
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
			PreparedStatement stmt = con.prepareStatement(getConfigValue(FINISHRUNUPDATE));
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
			PreparedStatement stmt = con.prepareStatement(getConfigValue(ADDBUIINSERT));
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
			log.info("Checking for Unmodified Files");
	
			PreparedStatement stmt = con.prepareStatement(getConfigValue(CHECKUNMODCREATE).replaceAll("###RUN###", ""+run));
			stmt.setInt(1, run);
			stmt.setInt(2, run);
			stmt.execute();
			
			stmt = con.prepareStatement(getConfigValue(CHECKUNMODUPDATE).replaceAll("###RUN###", ""+run));
			stmt.setInt(1, run);
			result = result + stmt.executeUpdate();						
			stmt.close();
			
		
			
			
		} catch ( Exception e) {
			e.printStackTrace();
			return -1;
		}
		return result;
	
	}

	@Override
	public String[] getFileFilters(int group) {
		Set<String> result = new HashSet<String>();
		try {
			PreparedStatement stmt = con.prepareStatement(getConfigValue(GETFILEFILTERSELECT));
			stmt.setInt(1, group);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				result.add(rs.getString(1));
				System.out.println(rs.getString(1));
			}
			rs.close();
			stmt.close();
		} catch ( Exception e) {
			return new String[0];
		}				
		
		return result.toArray(new String[result.size()]);
	}

	@Override
	public String[] getDirFilters(int group) {
		Set<String> result = new HashSet<String>();
		try {
			PreparedStatement stmt = con.prepareStatement(getConfigValue(GETDIRFILTERSELECT));
			stmt.setInt(1, group);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				result.add(rs.getString(1));
				System.out.println(rs.getString(1));
			}
			rs.close();
			stmt.close();
		} catch ( Exception e) {
			return new String[0];
		}				
		
		return result.toArray(new String[result.size()]);
	}

	@Override
	public long matchHashes(int run) {
		Run r = this.getRun(run);
		
		long result = 0;
		result = matchOnHash(r, result);
		if (result < 0) return result;
		
		try {
			log.info("Starting Insert of new Items");
			PreparedStatement stmt = con.prepareStatement(getConfigValue(MATCHHASHESINSERT));
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

	public long matchOnHash(Run run, long result) {
		try {
					
			//search for matching hashes and add Item_id if found
			log.info("Starting Match on Hash");
			PreparedStatement stmt = con.prepareStatement(getConfigValue(MATCHONHASHUPDATE));
			stmt.setInt(1, run.getBackupgroup());
			result = result + stmt.executeUpdate();
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
			PreparedStatement stmt = con.prepareStatement(getConfigValue(GETUNSAVEDSELECT).replaceAll("###DRIVENUM###", ""+(external?2:1)));
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
			    log.info("Added File to Backup: ["+path+"]");
			}
			rs.close();
			stmt.close();
			log.info("Num of Files to Backup: ["+count+"]");
	
			
		} catch (Exception ex) {
			ex.printStackTrace();
			log.severe("Error getting unsaved Items: ["+ex.toString()+"]");
			return null;
		}
		
		return result;
	}

	@Override
	public Set<BackupItem> getSavedItems(int run, boolean external) {
		int count = 0;
		Set<BackupItem> result = new LinkedHashSet<BackupItem>();
		try {
			Run r = getRun(run);
			PreparedStatement stmt = con.prepareStatement(getConfigValue(GETSAVEDSELECT).replaceAll("###DRIVENUM###", ""+(external?2:1)));
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
			    log.info("Added File to Restore: ["+path+"]");
			}
			rs.close();
			stmt.close();
			log.info("Num of Files to Restore: ["+count+"]");
	
			
		} catch (Exception ex) {
			ex.printStackTrace();
			log.severe("Error getting saved Items: ["+ex.toString()+"]");
			return null;
		}
		
		return result;
	}

	@Override
	public boolean setItemSaved(BackupItem item, String driveName, boolean external) {
		try {
			PreparedStatement stmt = con.prepareStatement(getConfigValue(SETSAVEDSELECT));
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
			PreparedStatement stmt = con.prepareStatement(getConfigValue(SETSAVEDUPDATE).replaceAll("###DRIVENUM###", ""+(external?2:1)));
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
			PreparedStatement stmt = con.prepareStatement(getConfigValue(GETUNHASHEDSELECT));
			stmt.setInt(1, run);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
			    int id = rs.getInt("ID");
				int run_id = rs.getInt("RUN_ID");
			    Integer item_id = rs.getInt("ITEM_ID");
			    if (rs.wasNull()) item_id = null;
			    String path = rs.getString("PATH");
			    String hash = rs.getString("HASH");
			    long size = rs.getLong("FILESIZE");
			    long lastModified = rs.getLong("LASTMODIFIED");
			    BackupItem bi = new BackupItem(this,id,run_id,item_id,path,hash,size,lastModified,null,null);
			    result.add(bi);
			}
			rs.close();
			stmt.close();
	
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return result;
	}

	@Override
	public String getDriveName(int drive) {
		String result = "";
		try {
			PreparedStatement stmt = con.prepareStatement(getConfigValue(GETDRIVENAMESELECT));
			stmt.setInt(1, drive);
			ResultSet rs = stmt.executeQuery();			
			if (rs.next()) {
			    result = rs.getString("NAME");				
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
			PreparedStatement stmt = con.prepareStatement(getConfigValue(SETBUIHASHUPDATE));
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
			PreparedStatement stmt = con.prepareStatement(getConfigValue(GETUSABLEDRIVESSELECT));
			
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
			PreparedStatement stmt = con.prepareStatement(getConfigValue(SETDRIVEFULLUPDATE));
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
		
			PreparedStatement stmt = con.prepareStatement(getConfigValue(GETXMLSELECT));
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