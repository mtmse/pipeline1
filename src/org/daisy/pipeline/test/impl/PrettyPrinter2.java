package org.daisy.pipeline.test.impl;

import java.util.List;

import org.daisy.pipeline.test.PipelineTest;
import org.daisy.util.file.Directory;

public class PrettyPrinter2 extends PipelineTest {

	public PrettyPrinter2(Directory dataInputDir, Directory dataOutputDir) {
		super(dataInputDir, dataOutputDir);
	}
	
	@Override
	public List<String> getParameters() {		
		mParameters.add("--input=" + mDataInputDir + "/dtb/d202/dontworrybehappy/ncc.html");
		mParameters.add("--output=" + mDataOutputDir + "/PrettyPrinter2/");
		return mParameters;
	}

	@Override
	public String getResultDescription() {		
		return "pretty printer";
	}

	@Override
	public boolean supportsScript(String scriptName) {
		if("PrettyPrinter.taskScript".equals(scriptName)) {
			return true;
		}		
		return false;
	}

	@Override
	public void confirm() {
		// TODO Auto-generated method stub
		
	}

}
