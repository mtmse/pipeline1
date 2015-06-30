package se_tpb_wmZip;

import java.io.File;
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
public class CreateWmZip extends Transformer {
	
	private static long MAX_SIZE = 1024 * 1024 * 1024 * 2L - 1024*1024; // 2 GB - 1MB (for playlist)
	//private static long MAX_SIZE = 1024 * 1024 * 20;
	
	/**
	 * Constructor
	 * @param inListener
	 * @param isInteractive
	 */
	public CreateWmZip(InputListener inListener, Boolean isInteractive) {
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
		String watermark = parameters.remove("watermark");
		String addID3 = parameters.remove("addID3");
		String addPlaylist = parameters.remove("addPlaylist");
		boolean id3 = "true".equals(addID3);
		boolean playlists = "true".equals(addPlaylist);
		
		File inputDir = FilenameOrFileURI.toFile(input);
		File outputDir = FilenameOrFileURI.toFile(output);
		
		boolean success = true;

		try {		
			TrueWmZip.createZipFiles(inputDir, outputDir, prefix, MAX_SIZE, id3, watermark, playlists);						
		} catch (ArchiveException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (IOException e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
		
		return success;
	}
}

