/*
 * DMFC - The DAISY Multi Format Converter
 * Copyright (C) 2007  Daisy Consortium
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package se_tpb_rmfCreator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.event.MessageEvent;
import org.daisy.pipeline.core.transformer.Transformer;
import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.file.Directory;
import org.daisy.util.file.FilenameOrFileURI;
import org.daisy.util.file.TempFile;
import org.daisy.util.fileset.D202NccFile;
import org.daisy.util.fileset.D202TextualContentFile;
import org.daisy.util.fileset.Fileset;
import org.daisy.util.fileset.FilesetErrorHandler;
import org.daisy.util.fileset.FilesetFile;
import org.daisy.util.fileset.exception.FilesetFatalException;
import org.daisy.util.fileset.exception.FilesetFileErrorException;
import org.daisy.util.fileset.exception.FilesetFileException;
import org.daisy.util.fileset.exception.FilesetFileFatalErrorException;
import org.daisy.util.fileset.exception.FilesetFileWarningException;
import org.daisy.util.fileset.impl.FilesetImpl;
import org.daisy.util.fileset.util.ManifestFinder;
import org.daisy.util.xml.catalog.CatalogEntityResolver;
import org.daisy.util.xml.catalog.CatalogExceptionNotRecoverable;
import org.daisy.util.xml.pool.StAXInputFactoryPool;
import org.daisy.util.xml.stax.StaxEntityResolver;
import org.daisy.util.xml.stax.StaxFilter;
import org.daisy.util.xml.xslt.Stylesheet;
import org.daisy.util.xml.xslt.XSLTException;

import se_tpb_rmfCreator.db.BookDAO;
import se_tpb_rmfCreator.db.DatabaseException;

/**
 * Creates an RMF file from a Daisy 2.02 book
 * @author Linus Ericson
 */
public class RMFCreator extends Transformer implements FilesetErrorHandler {

	private static Pattern nccPattern = Pattern.compile("ncc\\.htm.");
	private static Pattern textViewPattern = Pattern.compile("(D\\d{3,4})c?");
	
	public RMFCreator(InputListener inListener, Boolean isInteractive) {
		super(inListener, isInteractive);
	}

	protected boolean execute(Map parameters) throws TransformerRunException {
		String xmlFileName = (String)parameters.remove("input");
        String xsltFileName = (String)parameters.remove("xslt");
        String outFileDir = (String)parameters.remove("out");
        String factory = (String)parameters.remove("factory");
        
		try {			
			File manifestFile = FilenameOrFileURI.toFile(xmlFileName);
	        if (manifestFile.isDirectory()) {
	        	Collection<File> fileCollection = ManifestFinder.getManifests(true, new Directory(manifestFile));
	        	boolean nccFound = false;
	        	boolean textViewFound = false;
	        	//search for ncc.htm*
	        	for (Iterator<File> it = fileCollection.iterator(); it.hasNext(); ) {
	        		File manifest = (File)it.next();
	        		Matcher nccMatcher = nccPattern.matcher(manifest.getName());
	        		if (nccMatcher.matches() && !manifest.getAbsolutePath().contains("~")) {
	        			nccFound = true;
	        			manifestFile = manifest;
	        		}
	        	}
	        	if(!nccFound){
	        		//check if manifest is a book dir
	        		Matcher dirMatcher = textViewPattern.matcher(manifestFile.getName());
	        		if(dirMatcher.matches()){
	        			textViewFound = true;
	        		}else{
	        			//search for a textView dir
			        	File[] dirs = manifestFile.listFiles();
			        	List<File> dirList = Arrays.asList(dirs);
			        	for(File dir:dirList){
			        		if(dir.isDirectory()){
			        			Matcher textViewMatcher = textViewPattern.matcher(dir.getName());
			        			if(textViewMatcher.matches()){
				        			textViewFound = true;
				        			manifestFile = dir;
				        		}
			        		}
			        	}
	        		}
	        	}
	        	
	        	if (!nccFound && !textViewFound) {
	        		throw new TransformerRunException("Could not find either an ncc.html file or a text view dir!");
	        	}else if(textViewFound){
	        		String tpbNr = manifestFile.getName();
	        		Matcher dirMatcher = textViewPattern.matcher(manifestFile.getName());
	        		if(dirMatcher.matches()){
	        			tpbNr = dirMatcher.group(1);
	        		}
	        		File outFile = new File(outFileDir, tpbNr + ".rmf");
	        		if(!outFile.createNewFile()){
	        			throw new TransformerRunException("Could not create file: " +outFile.getAbsolutePath());
	        		}
	        		this.sendMessage("Creating rmf file: "+outFile.getAbsolutePath(), MessageEvent.Type.INFO_FINER);
	        		this.createRmfBasedOnDatabaseData(outFile);
	        		//exit here
	        		return true;
	        	}
	        }			
			
	        this.sendMessage("Using manifest file: " + manifestFile, MessageEvent.Type.INFO_FINER);
	        this.sendMessage("Using output folder: " + outFileDir, MessageEvent.Type.INFO_FINER);	        
	        
			int numImages = 0;
			NccResult nccResult = null;			
	        Fileset fileset = this.buildFileSet(manifestFile);   
	        if (fileset.hadErrors()) {
	        	throw new TransformerRunException("Fileset had errors");
	        }
	        for (Iterator it = fileset.getLocalMembers().iterator(); it.hasNext(); ) {
                FilesetFile fsf = (FilesetFile)it.next();
                if (fsf instanceof D202TextualContentFile) {
                	numImages += this.countImages(fsf.getFile());
                } else if (fsf instanceof D202NccFile) {
                	nccResult = this.parseNcc(fsf.getFile());
                }
            }
	        for (int i = 1; i <= nccResult.numVolumes; ++i) {
	        	String suffix = "";
	        	if (nccResult.numVolumes > 1) {
	        		suffix = "_" + String.valueOf(i);
	        	}
	        	File outFile = new File(outFileDir, nccResult.identifier + suffix + ".rmf");
	        	
	        	// Make sure outpur dir exists
	        	outFile.getParentFile().mkdirs();
	        	this.sendMessage("Creating RMF file: " + outFile, MessageEvent.Type.INFO_FINER);	
		        parameters.put("num_images", String.valueOf(numImages));
		        parameters.put("current_volume", String.valueOf(i));
		        parameters.put("num_volumes", String.valueOf(nccResult.numVolumes));
		        
		        TempFile temp = new TempFile();
		        this.normalizeChars(manifestFile, temp.getFile());
			    Stylesheet.apply(temp.getFile().getAbsolutePath(), xsltFileName, outFile.getAbsolutePath(), factory, parameters, CatalogEntityResolver.getInstance());
			    temp.delete();
	        }	        
        } catch (XSLTException e) {
            throw new TransformerRunException(e.getMessage(), e);
		} catch (CatalogExceptionNotRecoverable e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (FilesetFatalException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (FileNotFoundException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (XMLStreamException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (IOException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (DatabaseException e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
        return true;
	}
	
	private void createRmfBasedOnDatabaseData(File outFile) throws DatabaseException, IOException {

		//get data from db
		String fName = outFile.getName();
		String tpbNr = fName.substring(0,fName.indexOf("."));
		Map<String,String> metaData = BookDAO.retrieveBookProperties(tpbNr);

		//write to file
		//FIXME do it by xsl
		OutputStreamWriter out = null;
		try {
			OutputStream fout= new FileOutputStream(outFile,true);
			OutputStream bout= new BufferedOutputStream(fout);
			out = new OutputStreamWriter(bout, "8859_1");
			out.write("[MergeMetaData]\r\n");
			CharsetNormalizer2 charNormalizer = new CharsetNormalizer2();
			for(Iterator<String> it = NonDaisy202MetaDataKeyMapper.getNdsKeys().iterator(); it.hasNext();){
				String ndsKey = it.next();
				String value = metaData.get(ndsKey);
				String dcKey = NonDaisy202MetaDataKeyMapper.mapNdsMetadataKeyToDublinCore(ndsKey);
				value=(value!=null) ? charNormalizer.translate(value) : "";
				out.write(dcKey+" = "+value);
				out.write("\r\n");
				out.flush();
			}
		} catch (IOException e) {
			throw e;
		}finally{
			if(out!=null){
				out.close();
			}
		}
	}

	private void normalizeChars(File input, File output) throws XMLStreamException, IOException {
		StAXInputFactoryPool pool = StAXInputFactoryPool.getInstance();
		Map<String,Object> props = pool.getDefaultPropertyMap(false);
		XMLInputFactory xif = pool.acquire(props);
		try {
			XMLEventReader reader = xif.createXMLEventReader(new FileInputStream(input));
			StaxFilter filter = new CharsetNormalizer(reader, new FileOutputStream(output));
			filter.filter();
			filter.close();
		} finally {
			pool.release(xif, props);
		}
	}
	
	private int countImages(File file) throws CatalogExceptionNotRecoverable, FileNotFoundException, XMLStreamException {
		int imgCount = 0;
		XMLInputFactory mFactory = XMLInputFactory.newInstance();
        mFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        mFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        //mFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
        mFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
		mFactory.setXMLResolver(new StaxEntityResolver(CatalogEntityResolver.getInstance()));
		mFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
		XMLEventReader reader = mFactory.createXMLEventReader(new FileInputStream(file));
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				if ("img".equals(se.getName().getLocalPart())) {
					imgCount++;
				}				
			}
		}
		reader.close();
		return imgCount;
	}
	
	private NccResult parseNcc(File file) throws CatalogExceptionNotRecoverable, FileNotFoundException, XMLStreamException {
		String filename = null;
		int numVolumes = 1;
		XMLInputFactory mFactory = XMLInputFactory.newInstance();
        mFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        mFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        //mFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
        mFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
		mFactory.setXMLResolver(new StaxEntityResolver(CatalogEntityResolver.getInstance()));
		mFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
		XMLEventReader reader = mFactory.createXMLEventReader(new FileInputStream(file));
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				if ("meta".equals(se.getName().getLocalPart())) {
					String name = null;
					String content = null;
					for (Iterator it = se.getAttributes(); it.hasNext(); ) {
						Attribute attr = (Attribute)it.next();
						if ("name".equals(attr.getName().getLocalPart())) {
							name = attr.getValue();
						} else if ("content".equals(attr.getName().getLocalPart())) {
							content = attr.getValue();
						}						
					}
					if ("dc:identifier".equals(name)) {
						filename = content;
					} else if ("ncc:setInfo".equals(name)) {
						numVolumes = Integer.parseInt(content.substring(content.lastIndexOf(" ") + 1));
					}
				}
			}
		}
		reader.close();
		return new NccResult(filename, numVolumes);
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
	
	private class NccResult {
		public NccResult(String id, int vols) {
			identifier = id;
			numVolumes = vols;
		}
		public String identifier;
		public int numVolumes;
	}
	
	
}
