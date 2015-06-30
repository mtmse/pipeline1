package org.daisy.pipeline.test.impl;

import java.util.List;

import org.daisy.pipeline.test.PipelineTest;
import org.daisy.util.file.Directory;

public class FilesetGenerator1 extends PipelineTest {

	public FilesetGenerator1(Directory dataInputDir, Directory dataOutputDir) {
		super(dataInputDir, dataOutputDir);
	}
	
	@Override
	public List<String> getParameters() {		
		mParameters.add("--input=" + mDataInputDir + "/xhtml/daisy_202.html");		
		mParameters.add("--outputPath=" + mDataOutputDir + "/FilesetGenerator1/");
		
		mParameters.add("--identifier=C123456789");
		mParameters.add("--outputEncoding=iso-8859-1");
		return mParameters;
	}

	@Override
	public String getResultDescription() {		
		return "";
	}

	@Override
	public boolean supportsScript(String scriptName) {
		if("Fileset-XhtmlToDaisy202TextOnly.taskScript".equals(scriptName)) {
			return true;
		}		
		return false;
	}

	@Override
	public void confirm() {
		// TODO Auto-generated method stub		
	}

}
