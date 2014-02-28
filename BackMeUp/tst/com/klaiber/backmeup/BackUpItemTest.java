package com.klaiber.backmeup;

import static org.junit.Assert.*;

import org.junit.Test;

public class BackUpItemTest {

	@Test
	public void testGenerateHashForFile() {
		assertEquals("Hash not correct","5RPUBH26UT8GC80WGWWCKC080G0KOWG0",BackupItem.generateHashForFile("testdata/test1_1/Download-icon.png"));
	}

}
