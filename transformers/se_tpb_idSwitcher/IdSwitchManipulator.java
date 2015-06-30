package se_tpb_idSwitcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.daisy.util.file.FileUtils;
import org.daisy.util.fileset.D202MasterSmilFile;
import org.daisy.util.fileset.D202NccFile;
import org.daisy.util.fileset.D202SmilFile;
import org.daisy.util.fileset.D202TextualContentFile;
import org.daisy.util.fileset.FilesetFile;
import org.daisy.util.fileset.XmlFile;
import org.daisy.util.fileset.Z3986DtbookFile;
import org.daisy.util.fileset.manipulation.FilesetFileManipulator;
import org.daisy.util.fileset.manipulation.FilesetManipulationException;
import org.daisy.util.xml.pool.PoolException;
import org.daisy.util.xml.pool.StAXInputFactoryPool;

/**
 * A <code>FilesetFileManipulator</code> for changing the identifier in a file.
 * @author Linus Ericson
 *
 */
public class IdSwitchManipulator implements FilesetFileManipulator {

	private Map<String,Object> properties;
	private StAXInputFactoryPool pool;
	private String identifier;
	
	
	public IdSwitchManipulator(String identifier, StAXInputFactoryPool pool, Map<String,Object> properties) {
		this.identifier = identifier;
		this.pool = pool;
		this.properties = properties;
	}
	
	public File manipulate(FilesetFile filesetFile, File output, boolean allowDestinationOverwrite)	throws FilesetManipulationException {
		
		try {
			if (filesetFile instanceof XmlFile) {
				XMLInputFactory xif = null;
				XMLEventReader reader = null;
				try {
					FilterBase filter = null;
					xif = pool.acquire(properties);
					reader = xif.createXMLEventReader(new FileInputStream(filesetFile.getFile()));
					if (filesetFile instanceof Z3986DtbookFile) {
						filter = new FilterDTBook(reader, new FileOutputStream(output), identifier);
					}
					if (filesetFile instanceof D202NccFile ||
						filesetFile instanceof D202MasterSmilFile ||
						filesetFile instanceof D202SmilFile ||
						filesetFile instanceof D202TextualContentFile) {
						filter = new FilterD202(reader, new FileOutputStream(output), identifier);							
						}
					// Add support for more filter types here...
					
					if (filter != null) {
						// Perform filtering
						filter.filter();
						filter.close();
					} else {
						// Unsupported XML file type
						FileUtils.copy(filesetFile.getFile(), output);
					}
				} finally {
					pool.release(xif, properties);
					if (reader != null) {
						reader.close();
					}
				}
			} else {
				// Not XML
				FileUtils.copy(filesetFile.getFile(), output);
			}
			
			return output;
		} catch (PoolException e) {
			throw new FilesetManipulationException(e.getMessage(), e);
		} catch (FileNotFoundException e) {
			throw new FilesetManipulationException(e.getMessage(), e);
		} catch (XMLStreamException e) {
			throw new FilesetManipulationException(e.getMessage(), e);
		} catch (IOException e) {
			throw new FilesetManipulationException(e.getMessage(), e);
		}		
	}

}
