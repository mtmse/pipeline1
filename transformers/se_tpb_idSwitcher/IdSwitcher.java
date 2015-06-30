package se_tpb_idSwitcher;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.event.MessageEvent;
import org.daisy.pipeline.core.transformer.Transformer;
import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.file.Directory;
import org.daisy.util.file.FileUtils;
import org.daisy.util.file.FilenameOrFileURI;
import org.daisy.util.fileset.Fileset;
import org.daisy.util.fileset.FilesetErrorHandler;
import org.daisy.util.fileset.FilesetFile;
import org.daisy.util.fileset.FilesetType;
import org.daisy.util.fileset.exception.FilesetFatalException;
import org.daisy.util.fileset.exception.FilesetFileException;
import org.daisy.util.fileset.impl.FilesetImpl;
import org.daisy.util.fileset.manipulation.FilesetFileManipulator;
import org.daisy.util.fileset.manipulation.FilesetManipulationException;
import org.daisy.util.fileset.manipulation.FilesetManipulator;
import org.daisy.util.fileset.manipulation.FilesetManipulatorListener;
import org.daisy.util.xml.pool.StAXInputFactoryPool;

/**
 * Switches the ID (dtb:uid, dc:identifier etc) in a fileset.
 * <p>
 * To add support for another fileset type, create a filter
 * (extending <code>FilterBase</code>) for all file types
 * in the fileset and add them to the <code>IdSwitchManipulator</code>.
 * Then add support for the fileset type in the execute method in this class.
 * </p> 
 * @author Linus Ericson
 */
public class IdSwitcher extends Transformer implements FilesetErrorHandler, FilesetManipulatorListener {
		
	private Map<String,Object> properties;
	private StAXInputFactoryPool pool;
	private String identifier;
	
	public IdSwitcher(InputListener inListener, Boolean isInteractive) {
		super(inListener, isInteractive);
		pool = StAXInputFactoryPool.getInstance();
		properties = pool.getDefaultPropertyMap(false);
	}

	@Override
	protected boolean execute(Map parameters) throws TransformerRunException {
		String input = (String)parameters.remove("input");
		String output = (String)parameters.remove("output");
		identifier = (String)parameters.remove("identifier");
		String regex = (String)parameters.remove("regex");
		
		// Make sure identifier matches pattern
		if (!identifier.matches(regex)) {
			throw new TransformerRunException(i18n("REGEX_MISMATCH"));
		}
		
		try {
			File inputFile = FilenameOrFileURI.toFile(input);
			Directory outputDir = new Directory(FilenameOrFileURI.toFile(output));
			
			// Make sure output directory exists
			FileUtils.createDirectory(outputDir);
			
			// Build fileset
			Fileset fileset = new FilesetImpl(inputFile.toURI(), this, false, false);			
			if (fileset.hadErrors()) {
				throw new TransformerRunException(i18n("FILESET_ERRORS"));
			}
			
			// Make sure we have a supported fileset type
			if (fileset.getFilesetType() == FilesetType.DTBOOK_DOCUMENT) {
				sendMessage(i18n("FILESET_FOUND", fileset.getFilesetType().toNiceNameString()), MessageEvent.Type.INFO_FINER);
			} else if (fileset.getFilesetType() == FilesetType.DAISY_202) {
				sendMessage(i18n("FILESET_FOUND", fileset.getFilesetType().toNiceNameString()), MessageEvent.Type.INFO_FINER);
			}
			// Add support for more fileset types here...
			else {
				throw new TransformerRunException(i18n("NOT_SUPPORTED", fileset.getFilesetType().toNiceNameString()));
			}
			
			FilesetManipulator manipulator = new FilesetManipulator();
			manipulator.setListener(this);
			manipulator.setInputFileset(fileset);
			manipulator.setOutputFolder(outputDir);
			manipulator.iterate();
			
			// All OK
			return true;
		} catch (IOException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (FilesetFatalException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (FilesetManipulationException e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
		
	}
		
	public void error(FilesetFileException ffe) throws FilesetFileException {
		sendMessage(ffe);
	}

	public FilesetFileManipulator nextFile(FilesetFile file) throws FilesetManipulationException {
		return new IdSwitchManipulator(identifier, pool, properties);
	}

}
