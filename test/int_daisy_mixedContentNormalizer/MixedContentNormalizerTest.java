package int_daisy_mixedContentNormalizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.daisy.pipeline.exception.TransformerDisabledException;
import org.daisy.pipeline.exception.TransformerRunException;
import org.junit.Test;
import org.hamcrest.Matcher.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class MixedContentNormalizerTest {

	private static final int BUFFER_SIZE = 1024;

	@Test
	public void testNormalizer_01() throws TransformerRunException, TransformerDisabledException, IOException {
		
		Map<String,String> parameters = new HashMap<String, String>();
		 
		String testNormalizeOutputName = "test/int_daisy_mixedContentNormalizer/test_normalize_output.xml";
		String testNormalizeInput = "test/int_daisy_mixedContentNormalizer/test_normalize_input.xml";
        String testGoldenMaster = "test/int_daisy_mixedContentNormalizer/golden_master_test_normalize.xml";
		parameters.put("input", testNormalizeInput);
		parameters.put("output", testNormalizeOutputName);
		parameters.put("addSyncPoints", "true");
		parameters.put("implementation", "dom");
		
		MixedContentNormalizer n = new MixedContentNormalizer(null, false);
		n.execute(parameters);
		
		File goldenMaster = new File(testGoldenMaster);
		File testOutput = new File(testNormalizeOutputName);
		assertThat(FileUtils.contentEquals(goldenMaster, testOutput), is(Boolean.TRUE));
	}
}
