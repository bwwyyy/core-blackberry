package com.ht.tests.unit;

import java.io.IOException;

import com.ht.rcs.blackberry.fs.AutoFlashFile;
import com.ht.rcs.blackberry.fs.Path;
import com.ht.rcs.blackberry.utils.Utils;
import com.ht.tests.AssertException;
import com.ht.tests.TestUnit;
import com.ht.tests.Tests;

public class UT_File extends TestUnit {

	public UT_File(String name, Tests tests) {
		super(name, tests);
	}
	
	private void FileCreateTest() throws AssertException {
		AutoFlashFile file = new AutoFlashFile(Path.SDPath + "testCreate.txt", false);
		boolean ret = file.Create();
		AssertThat(ret == true, "Cannot create");
		
		ret = file.Exists();
		AssertThat(ret == true, "don't exists");
		
		file.Delete();
		ret = file.Exists();
		AssertThat(ret == false, "still exists");
	}
	
	private void FileCreateHiddenTest() throws AssertException {
		AutoFlashFile file = new AutoFlashFile(Path.SDPath + "testHidden.txt", true);
		boolean ret = file.Create();
		AssertThat(ret == true, "Cannot create");
		
		ret = file.Exists();
		AssertThat(ret == true, "don't exists");
		
		file.Delete();
		ret = file.Exists();
		AssertThat(ret == false, "still exists");
	}
	
	private void PathSDPresentTest() {
		
		boolean ret=Path.SDPresent();
		
		
	}
	
	private void FileReadWriteTest() throws AssertException {
		AutoFlashFile file = new AutoFlashFile(Path.SDPath + "testRW.txt", false);
		boolean ret = file.Create();
		AssertThat(ret == true, "Cannot create");
		
		byte[] read= file.Read();
		AssertThat(read.length == 0, "read more than 0");
		
		file.Write(42);
		read= file.Read();
		AssertThat(read.length == 4, "read something wrong");

		int value = Utils.byteArrayToInt(read, 0);
		AssertThat(value == 42, "read something different");
			
		file.Delete();
		ret = file.Exists();
		AssertThat(ret == false, "still exists");
				
	}
	
	private void FileAppendTest() throws AssertException {
		AutoFlashFile file = new AutoFlashFile(Path.SDPath + "testAppend.txt", false);
		boolean ret = file.Create();
		AssertThat(ret == true, "Cannot create");
		
		byte[] read= file.Read();
		AssertThat(read.length == 0, "read more than 0");
		
		file.Write(42);
		read= file.Read();
		AssertThat(read.length == 4, "read something wrong 1");
		
		file.Append(100);
		read= file.Read();
		AssertThat(read.length == 8, "read something wrong 2");
		
		int value = Utils.byteArrayToInt(read, 0);
		AssertThat(value == 42, "read something different 1 ");
		value = Utils.byteArrayToInt(read, 4);
		AssertThat(value == 100, "read something different 2");
				
		file.Delete();
		ret = file.Exists();
		AssertThat(ret == false, "still exists");
				
	}
	
	public boolean run() throws AssertException {
		FileCreateTest();
		FileCreateHiddenTest();
		FileReadWriteTest();	
		FileAppendTest();
		PathSDPresentTest();
		
		return true;
	}

	






}