package com.klaiber.backmeup;

import static org.junit.Assert.*;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileCopyWorkerTest {
	
	@Before
	public void setup() throws Exception {
		File tgt = new File("testtarget/fcwtest/");
		if (!tgt.exists()){
			tgt.mkdirs();
		}
		FileUtils.copyFileToDirectory(new File("testdata/drivetokens/TST001/_bmudrive"), tgt);
	}

	@After
	public void tearDown() throws Exception {
		File tgt = new File("testtarget/fcwtest/");
		if (tgt.exists()){
			FileUtils.deleteDirectory(tgt);
		}
	}
	
	@Test
	public void testSuccessfulCopy() throws Exception {
		File src = new File("testdata/test1_1/Download-icon.png");
		MockCopyController mcc = new MockCopyController();
		FileCopyWorker fcw = new FileCopyWorker(src ,new File("testtarget/fcwtest/"),"TST001","5rpubh26ut5nshs3psmm9m1h257q2a7nl32ani1fs50rfao0as",12345L,15728640L,mcc);
		Thread t = new Thread(fcw);
		t.start();
		TimeUnit.SECONDS.sleep(3);
		assertTrue("Not returned",mcc.isReturned());
		assertEquals("Stauscode not 0",mcc.getFinishedStatusCode(),0);
	}
	
	@Test
	public void testWrongHashCopy() throws Exception {
		File src = new File("testdata/test1_1/Download-icon.png");
		MockCopyController mcc = new MockCopyController();
		FileCopyWorker fcw = new FileCopyWorker(src,new File("testtarget/fcwtest/"),"TST001","5sdfaubh26ut5nshs3psmm9m1h257q2a7nl32ani1fs50rfao0as",12345L,15728640L,mcc);
		Thread t = new Thread(fcw);
		t.start();
		TimeUnit.SECONDS.sleep(3);
		assertTrue("Not returned",mcc.isReturned());
		assertEquals("Stauscode not 1", 1 ,mcc.getFinishedStatusCode());
	}
	
	@Test
	public void testMissingSrcFileCopy() throws Exception {
		MockCopyController mcc = new MockCopyController();
		FileCopyWorker fcw = new FileCopyWorker(new File("testdata/test1_1/notexistant.png"),new File("testtarget/fcwtest/"),"TST001","5sdfaubh26ut5nshs3psmm9m1h257q2a7nl32ani1fs50rfao0as",12345L,15728640L,mcc);
		Thread t = new Thread(fcw);
		t.start();
		TimeUnit.SECONDS.sleep(3);
		assertTrue("Not returned",mcc.isReturned());
		assertEquals("Stauscode not 2", 2, mcc.getFinishedStatusCode());
	}

	@Test
	public void testFileLargeCopy() throws Exception {
		MockCopyController mcc = new MockCopyController();
		File src = new File("testdata/test1_1/Download-icon.png");
		File tgt = new File("testtarget/fcwtest/");
		long remainingSpace = tgt.getFreeSpace();
		long fileSize = src.length();
		long reserve = remainingSpace - (fileSize-1000);
		long max = fileSize - 1500;				
		FileCopyWorker fcw = new FileCopyWorker(src,tgt,"TST001","5rpubh26ut5nshs3psmm9m1h257q2a7nl32ani1fs50rfao0as",reserve,max,mcc);
		Thread t = new Thread(fcw);
		t.start();
		TimeUnit.SECONDS.sleep(3);
		assertTrue("Not returned",mcc.isReturned());
		assertEquals("Stauscode not 3",3 ,mcc.getFinishedStatusCode());
	}
	
	@Test
	public void testDriveFullCopy() throws Exception {
		File src = new File("testdata/test1_1/Download-icon.png");
		MockCopyController mcc = new MockCopyController();
		FileCopyWorker fcw = new FileCopyWorker(src, new File("testtarget/fcwtest/"),"TST001","5rpubh26ut5nshs3psmm9m1h257q2a7nl32ani1fs50rfao0as",1099511627776L,15728640L,mcc);
		Thread t = new Thread(fcw);
		t.start();
		TimeUnit.SECONDS.sleep(3);
		assertTrue("Not returned",mcc.isReturned());
		assertEquals("Stauscode not 4", 4 ,mcc.getFinishedStatusCode());
	}
	
	@Test
	public void testTgtFileExistsCopy() throws Exception {
		File src = new File("testdata/test1_1/Download-icon.png");
		MockCopyController mcc = new MockCopyController();
		FileCopyWorker fcw = new FileCopyWorker(src,new File("testtarget/fcwtest/"),"TST001","5rpubh26ut5nshs3psmm9m1h257q2a7nl32ani1fs50rfao0as",12345L,15728640L,mcc);
		Thread t = new Thread(fcw);
		t.start();
		TimeUnit.SECONDS.sleep(3);
		assertTrue("Not returned 1",mcc.isReturned());
		assertEquals("Stauscode not 0", 0 ,mcc.getFinishedStatusCode());
		mcc = new MockCopyController();
		fcw = new FileCopyWorker(new File("testdata/test1_1/Download-icon.png"),new File("testtarget/fcwtest/"),"TST001","5rpubh26ut5nshs3psmm9m1h257q2a7nl32ani1fs50rfao0as",12345L,15728640L,mcc);		
		t = new Thread(fcw);
		t.start();
		TimeUnit.SECONDS.sleep(3);
		assertTrue("Not returned 2",mcc.isReturned());
		assertEquals("Stauscode not 5", 5 ,mcc.getFinishedStatusCode());
	}
	
	@Test
	public void testNoTargetDriveCopy() throws Exception {
		File src = new File("testdata/test1_1/Download-icon.png");
		File tgt = new File("testtarget/fcwtestnoitexist/");
		MockCopyController mcc = new MockCopyController();
		FileCopyWorker fcw = new FileCopyWorker(src,tgt,"TST001","5rpubh26ut5nshs3psmm9m1h257q2a7nl32ani1fs50rfao0as",12345L,15728640L,mcc);
		Thread t = new Thread(fcw);
		t.start();
		TimeUnit.SECONDS.sleep(3);
		assertTrue("No Action Required",mcc.isActionRequested());
		assertEquals("Stauscode not 1", 1,mcc.getActionStatusCode());
		fcw.stopProcess();
	}
	
	@Test
	public void testTargetDriveNotInizalizedCopy() throws Exception {
		File src = new File("testdata/test1_1/Download-icon.png");
		File tgt =  new File("testtarget/fcwtest/");
		File bmu = new File("testtarget/fcwtest/_bmudrive");
		if (bmu.exists() && bmu.isFile()) bmu.delete();
		MockCopyController mcc = new MockCopyController();
		FileCopyWorker fcw = new FileCopyWorker(src,tgt,"TST001","5rpubh26ut5nshs3psmm9m1h257q2a7nl32ani1fs50rfao0as",12345L,15728640L,mcc);
		Thread t = new Thread(fcw);
		t.start();
		TimeUnit.SECONDS.sleep(3);
		assertTrue("No Action Required",mcc.isActionRequested());
		assertEquals("Stauscode not 2", 2, mcc.getActionStatusCode());
		fcw.stopProcess();
	}
	
	@Test
	public void testWrongTargetDriveCopy() throws Exception {
		File src = new File("testdata/test1_1/Download-icon.png");
		File tgt =  new File("testtarget/fcwtest/");
		MockCopyController mcc = new MockCopyController();
		FileCopyWorker fcw = new FileCopyWorker(src,tgt,"TST002","5rpubh26ut5nshs3psmm9m1h257q2a7nl32ani1fs50rfao0as",12345L,15728640L,mcc);
		Thread t = new Thread(fcw);
		t.start();
		TimeUnit.SECONDS.sleep(3);
		assertTrue("No Action Required",mcc.isActionRequested());
		assertEquals("Stauscode not 3",3,mcc.getActionStatusCode());
		fcw.stopProcess();
	}
}
