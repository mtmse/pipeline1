package org.daisy.pipeline.test.impl;

import java.util.List;

import org.daisy.pipeline.test.PipelineTest;
import org.daisy.util.file.Directory;

public class OpsCreator3 extends PipelineTest {

	public OpsCreator3(Directory dataInputDir, Directory dataOutputDir) {
		super(dataInputDir, dataOutputDir);
	}
	
	@Override
	public List<String> getParameters() {
		mParameters.add("--input=" + mDataInputDir + "/xhtml/daisy_202.html");
		mParameters.add("--output=" + mDataOutputDir + "/OpsCreator3/daisy_202.epub");				
		return mParameters;
	}

	@Override
	public String getResultDescription() {		
		return "Should create an XHTML OCF, warn about XHTML 1.0 content being used, upgrade to 1.1, " +
				"and then report a number of errors'";
	}

	@Override
	public boolean supportsScript(String scriptName) {
		if("OPSCreator.taskScript".equals(scriptName)) {
			return true;
		}		
		return false;
	}

	@Override
	public void confirm() {
		// TODO Auto-generated method stub
		
	}

}
