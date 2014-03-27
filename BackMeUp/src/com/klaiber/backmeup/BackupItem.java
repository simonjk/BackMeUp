package com.klaiber.backmeup;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class BackupItem {
	/* Obsolete
	//public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	public static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	//public static final String ALPHABET = "0123456789ABCDEF";
	public static final int BASE = ALPHABET.length();
	*/
	
	private DBConnector con;
	private int id;
	private int run;
	private Integer item_id;
	private String path;
	private String hash;
	private long size;
	private long lastModified;
	private Integer drive1;
	private Integer drive2;
	
	/* Obsolete and probably buggy
	public static String fromBase10(BigInteger i) {
		StringBuilder sb = new StringBuilder("");
		while (i.compareTo(BigInteger.ZERO) == 1) {
		i = fromBase10(i, sb);
		}
		return sb.reverse().toString();
		}
		 
	private static BigInteger fromBase10(BigInteger i, final StringBuilder sb) {
		int rem = (i.mod(BigInteger.valueOf(BASE))).intValue();
		sb.append(ALPHABET.charAt(rem));
		return i.divide(BigInteger.valueOf(BASE));
		}
	*/
	
	public BackupItem(DBConnector dbConnector, int id, int run, Integer item_id, String path,
			String hash, long size, long lastModified, Integer drive1,
			Integer drive2) {
		this.con = dbConnector;
		this.id = id;
		this.run = run;
		this.item_id = item_id;
		this.path = path;
		this.hash = hash;
		this.size = size;
		this.lastModified = lastModified;
		this.drive1 = drive1;
		this.drive2 = drive2;
	}

	public Integer getItem_id() {
		return item_id;
	}
/*
	public void setItem_id(Integer item_id) {
		this.item_id = item_id;
	}
*/
	public String getHash() {
		return hash;
	}

	public void generateHash() {
	    String hash = null;
		
		hash = generateHashForFile(hash); 
		
		
		this.hash = hash;
		con.setBackupItemHash(this);
	}

	public static String generateHashForFile(String filepath) {
		String hash = null;
		try{
	    		MessageDigest md = MessageDigest.getInstance("SHA-256");
	    		FileInputStream fis = new FileInputStream(filepath);
		 
		        byte[] dataBytes = new byte[1024];
		 
		        int nread = 0; 
		        while ((nread = fis.read(dataBytes)) != -1) {
		          md.update(dataBytes, 0, nread);
		        };
		        byte[] mdbytes = md.digest();
		 
		        //convert the byte to hex format method 
		        StringBuffer sb = new StringBuffer();
		        for (int i = 0; i < mdbytes.length; i++) {
		          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		        }
		        fis.close();
		        hash = new BigInteger(sb.toString(), 16).toString(36);		                
		        
	} catch (Exception ex) {
	 //log	
	}
		return hash;
	}

	public Integer getDrive1() {
		return drive1;
	}

	public void setDrive1(Integer drive1) {
		this.drive1 = drive1;
	}

	public Integer getDrive2() {
		return drive2;
	}

	public void setDrive2(Integer drive2) {
		this.drive2 = drive2;
	}

	public int getId() {
		return id;
	}

	public int getRun() {
		return run;
	}

	public String getPath() {
		return path;
	}

	public long getSize() {
		return size;
	}

	public long getLastModified() {
		return lastModified;
	}
	
	public void calculateHash(){
		
	}
	
}
