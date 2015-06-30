package org.daisy.pipeline.test.impl;

import java.util.List;

import org.daisy.pipeline.test.PipelineTest;
import org.daisy.util.file.Directory;

public class RenamerTaggerValidator1 extends PipelineTest {

	public RenamerTaggerValidator1(Directory dataInputDir, Directory dataOutputDir) {
		super(dataInputDir, dataOutputDir);
	}
	
	@Override
	public List<String> getParameters() {			
		mParameters.add("--userInput=" + mDataInputDir + "/dtb/d202/dontworrybehappy/ncc.html");
		mParameters.add("--userOutput=" + mDataOutputDir + "/RenamerTaggerValidator1/");
		return mParameters;
	}

	@Override
	public String getResultDescription() {		
		return "";
	}

	@Override
	public boolean supportsScript(String scriptName) {
		if("RenamerTaggerValidator.taskScript".equals(scriptName)) {
			return true;
		}		
		return false;
	}

	@Override
	public void confirm() {
		// TODO Auto-generated method stub		
	}

}
