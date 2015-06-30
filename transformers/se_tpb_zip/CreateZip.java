package se_tpb_zip;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;

import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.transformer.Transformer;
import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.file.FilenameOrFileURI;

import de.schlichtherle.io.ArchiveException;


/**
 * Creates one or several Zip images files using truezip. 
 * @author Linus Ericson
 */
public class CreateZip extends Transformer {
	
	private static long MAX_SIZE = 1024 * 1024 * 1024 * 2L;
	//private static long MAX_SIZE = 1024 * 1024 * 20;
	
	/**
	 * Constructor
	 * @param inListener
	 * @param isInteractive
	 */
	public CreateZip(InputListener inListener, Boolean isInteractive) {
		super(inListener, isInteractive);
	}

	/*
	 * (non-Javadoc)
	 * @see org.daisy.pipeline.core.transformer.Transformer#execute(java.util.Map)
	 */
	@Override
	protected boolean execute(Map<String,String> parameters) throws TransformerRunException {
		String input = parameters.remove("input");
		String output = parameters.remove("output");
		String prefix = parameters.remove("prefix");
		
		File inputDir = FilenameOrFileURI.toFile(input);
		File outputDir = FilenameOrFileURI.toFile(output);
		
		// Check the input
		RecursiveFileListIterator iter = new RecursiveFileListIterator(inputDir, new FileFilter() {
			public boolean accept(File file) {					
				return !file.getAbsolutePath().contains("~");
			}
		});
		long totalSize = 0;
		while (iter.hasNext()) {
			File file = iter.next();
			totalSize += file.length();
		}
		
		boolean success = true;

		try {		
			if (totalSize < MAX_SIZE) {
				TrueZip.createZipFile(inputDir, new File(outputDir, prefix + ".zip"));
			} else {
				TrueZip.createZipFiles(inputDir, outputDir, prefix, MAX_SIZE);
			}			
		} catch (ArchiveException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (IOException e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
		
		return success;
	}
}

