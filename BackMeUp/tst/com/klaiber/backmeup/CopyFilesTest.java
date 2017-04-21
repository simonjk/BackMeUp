package com.klaiber.backmeup;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.BeforeClass;
//import org.junit.FixMethodOrder;
//import org.junit.runners.MethodSorters;
import org.junit.Test;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CopyFilesTest {

	private static DBConnector con;
	private static int run1;
	
	@BeforeClass
	public static void setup() {
		con = new H2EmbeddedConnector();
		if (con.open("jdbc:h2:mem:test;INIT=RUNSCRIPT FROM 'backupdb_testdata_create.ddl'")) {
			run1 = con.createRun(1);
			
			int group = 1;
			
			DirectoryCrawler dc = new DirectoryCrawler(con, run1,group);
			
			for (String d : con.getDirectories(group)){
				//System.out.print(d);
				try {
					dc.crawl(Paths.get(d), true);
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
	
			con.checkForUnmodifiedFiles(run1);
			Set<BackupItem> unhashed = con.getUnhashedItems(run1);
			for(BackupItem bi : unhashed){
				bi.generateHash();
			}
			con.matchHashes(run1);
			
		} else {
			fail("Db could not be Initated");
		}
	}
	
	@Test
	public void test01NumOfUnsavedItems() {
		assertEquals("wrong number of unsaved Items internal", 11 , con.getUnsavedItems(run1, false).size());
		assertEquals("wrong number of unsaved Items external", 11 , con.getUnsavedItems(run1, true).size());
	}
	
	@Test
	public void test02SetItemSaved() {		
		assertEquals("wrong inital number of unsaved Items internal", 11 , con.getUnsavedItems(run1, false).size());
		assertEquals("wrong inital number of unsaved Items external", 11 , con.getUnsavedItems(run1, true).size());
		assertTrue("Set Item saved internal failed",con.setItemSaved(con.getUnsavedItems(run1, false).iterator().next(), 1, false));
		assertEquals("wrong number of unsaved Items internal after finishing internal", 10, con.getUnsavedItems(run1, false).size());
		assertEquals("wrong number of unsaved Items external after finishing internal", 11 , con.getUnsavedItems(run1, true).size());
		assertTrue("Set Item saved external failed",con.setItemSaved(con.getUnsavedItems(run1, true).iterator().next(), 2, true));
		assertEquals("wrong number of unsaved Items internal after finishing external", 10,  con.getUnsavedItems(run1, false).size() );
		assertEquals("wrong number of unsaved Items external after finishing external", 10, con.getUnsavedItems(run1, true).size() );
		
	}
	

}
