package org.daisy.pipeline.test.impl;

import java.util.List;

import org.daisy.pipeline.test.PipelineTest;
import org.daisy.util.file.Directory;

public class UnicodeNormalizer1 extends PipelineTest {

	public UnicodeNormalizer1(Directory dataInputDir, Directory dataOutputDir) {
		super(dataInputDir, dataOutputDir);
	}
	
	@Override
	public List<String> getParameters() {		
		mParameters.add("--input=" + mDataInputDir + "/xhtml/multi-language-unicode.html");
		mParameters.add("--output=" + mDataOutputDir + "/UnicodeNormalizer1/");
		return mParameters;
	}

	@Override
	public String getResultDescription() {		
		return "perform normalization";
	}

	@Override
	public boolean supportsScript(String scriptName) {
		if("UnicodeNormalizer.taskScript".equals(scriptName)) {
			return true;
		}		
		return false;
	}

	@Override
	public void confirm() {
		// TODO Auto-generated method stub
		
	}

}
