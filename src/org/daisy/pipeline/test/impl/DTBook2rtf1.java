package org.daisy.pipeline.test.impl;

import java.util.List;

import org.daisy.pipeline.test.PipelineTest;
import org.daisy.util.file.Directory;

public class DTBook2rtf1 extends PipelineTest {

	public DTBook2rtf1(Directory dataInputDir, Directory dataOutputDir) {
		super(dataInputDir, dataOutputDir);
	}
	
	@Override
	public List<String> getParameters() {
		//mParameters.add("--input=" + mDataInputDir + "/dtbook/hauy_valid.xml");
		mParameters.add("--input=" + mDataInputDir + "/dtbook/hauy-2005-1.xml");
		//mParameters.add("--input=" + mDataInputDir + "/dtbook/the_waste_land.xml");
		mParameters.add("--output=" + mDataOutputDir + "/DTBook2RTF1/fromDtbook.rtf");
		mParameters.add("--inclTOC=false");
		return mParameters;
	}

	@Override
	public String getResultDescription() {		
		return "";
	}

	@Override
	public boolean supportsScript(String scriptName) {
		if("DtbookToRtf.taskScript".equals(scriptName)) {
			return true;
		}		
		return false;
	}

	@Override
	public void confirm() {
		// TODO Auto-generated method stub
		
	}

}
