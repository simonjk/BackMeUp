package com.klaiber.backmeup;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

public class BackUpItemTest {

	@Test
	public void testGenerateHashForFile() {
		assertEquals("Hash not correct","5rpubh26ut5nshs3psmm9m1h257q2a7nl32ani1fs50rfao0as",BackupItem.generateHashForFile("testdata/test1_1/Download-icon.png"));
	}

	
	
}
