package se_tpb_dtbinfo;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.event.MessageEvent;
import org.daisy.pipeline.core.transformer.Transformer;
import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.file.Directory;
import org.daisy.util.file.FileUtils;
import org.daisy.util.fileset.Fileset;
import org.daisy.util.fileset.FilesetErrorHandler;
import org.daisy.util.fileset.FilesetFile;
import org.daisy.util.fileset.ImageFile;
import org.daisy.util.fileset.exception.FilesetFatalException;
import org.daisy.util.fileset.exception.FilesetFileErrorException;
import org.daisy.util.fileset.exception.FilesetFileException;
import org.daisy.util.fileset.exception.FilesetFileFatalErrorException;
import org.daisy.util.fileset.exception.FilesetFileWarningException;
import org.daisy.util.fileset.impl.FilesetImpl;
import org.daisy.util.xml.catalog.CatalogEntityResolver;
import org.daisy.util.xml.catalog.CatalogExceptionNotRecoverable;
import org.daisy.util.xml.xslt.Stylesheet;
import org.daisy.util.xml.xslt.XSLTException;

/**
 * 
 * @author Linus Ericson
 */
public class DtbInfo extends Transformer implements FilesetErrorHandler {

    private static String sFactory = "net.sf.saxon.TransformerFactoryImpl";
	//private static String sFactory = "com.icl.saxon.TransformerFactoryImpl";
	
	public DtbInfo(InputListener inListener, Boolean isInteractive) {
		super(inListener, isInteractive);
	}

	protected boolean execute(Map<String, String> parameters) throws TransformerRunException {
		String manifest = (String)parameters.remove("manifest");
		String outDir = (String)parameters.remove("outDir");
		String special = (String)parameters.remove("special");
		// Parameter bookNumberIfDualSpeed is sent directly to the XSLT
		
		try {	
			File outFile = new File(outDir);
			File inFile = new File(manifest);
			// EFile eInFile = new EFile(inFile);
			String outFileName;
			Directory folder;
			File stylesheet;
			File procurementPatch2011 = new File(this.getTransformerDirectory(), "move-cover-text.xsl");
			if ("true".equals(special)) {
				stylesheet = new File(this.getTransformerDirectory(), "dtbinfo-special.xsl");
			} else if ("punkt".equals(special)) {
				stylesheet = new File(this.getTransformerDirectory(), "punktinfo.xsl");
			} else {
				stylesheet = new File(this.getTransformerDirectory(), "dtbinfo.xsl");
			}
			
			if (outFile.toString().endsWith(".xml")) {
				folder = new Directory(outFile.getParentFile());
				outFileName = outFile.getName();
			} else {
				folder = new Directory(outFile);
				outFileName = inFile.getName();
			}
			FileUtils.createDirectory(folder);
			
			if (inFile.getParentFile().equals(folder)) {
				throw new TransformerRunException("Output directory may not be same as input directory");
			}
			Fileset fileset = this.buildFileSet(new File(manifest));
			DateFormat df = new SimpleDateFormat("yyyy");
			parameters.put("year", df.format(new Date()));
			File tmp = File.createTempFile("dtbinfo", ".tmp");
			// Copy to String/Object map for use with Stylesheet.apply
			Map<String, Object> params = new HashMap<String, Object>(parameters);
			Stylesheet.apply(manifest, procurementPatch2011.toString(), tmp.toString(), sFactory, params, CatalogEntityResolver.getInstance());
			Stylesheet.apply(tmp.toString(), stylesheet.toString(), new File(folder, outFileName).toString(), sFactory, params, CatalogEntityResolver.getInstance());
			if (!tmp.delete()) {
				tmp.deleteOnExit();
			}
			for (Iterator<FilesetFile> it = fileset.getLocalMembers().iterator(); it.hasNext(); ) {
				FilesetFile fsf = it.next();
				if (fsf instanceof ImageFile) {
					folder.addFile(fsf.getFile());
				}
			}			
        } catch (XSLTException e) {
            throw new TransformerRunException(e.getMessage(), e);
		} catch (CatalogExceptionNotRecoverable e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (FilesetFatalException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (IOException e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
		
		return true;
	}
	
	private Fileset buildFileSet(File manifest) throws FilesetFatalException {
        return new FilesetImpl(manifest.toURI(), this, false, true);
    }
	
	public void error(FilesetFileException ffe) throws FilesetFileException {		
		if(ffe instanceof FilesetFileFatalErrorException) {
			this.sendMessage("Serious error in "	+ ffe.getOrigin().getName() + ": " 
					+ ffe.getCause().getMessage() + " [" + ffe.getCause().getClass().getSimpleName() + "]", MessageEvent.Type.WARNING, MessageEvent.Cause.INPUT);			
		}else if (ffe instanceof FilesetFileErrorException) {
			this.sendMessage("Error in " + ffe.getOrigin().getName() + ": " 
					+ ffe.getCause().getMessage() + " [" + ffe.getCause().getClass().getSimpleName() + "]", MessageEvent.Type.WARNING, MessageEvent.Cause.INPUT);
		}else if (ffe instanceof FilesetFileWarningException) {
			this.sendMessage("Warning in " + ffe.getOrigin().getName() + ": " 
					+ ffe.getCause().getMessage() + " [" + ffe.getCause().getClass().getSimpleName() + "]", MessageEvent.Type.WARNING, MessageEvent.Cause.INPUT);
		}else{
			this.sendMessage("Exception with unknown severity in " + ffe.getOrigin().getName() + ": "
					+ ffe.getCause().getMessage() + " [" + ffe.getCause().getClass().getSimpleName() + "]", MessageEvent.Type.WARNING, MessageEvent.Cause.INPUT);
		}		
	}

}
