/*
 * Daisy Pipeline (C) 2005-2008 Daisy Consortium
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.pipeline.util;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import org.daisy.util.file.EFile;
import org.daisy.util.file.Directory;
import org.daisy.util.fileset.FilesetErrorHandler;
import org.daisy.util.fileset.exception.FilesetFatalException;
import org.daisy.util.fileset.exception.FilesetFileException;
import org.daisy.util.fileset.impl.FilesetImpl;
import org.daisy.util.text.URIUtils;
import org.daisy.util.xml.peek.PeekResult;
import org.daisy.util.xml.peek.Peeker;
import org.daisy.util.xml.peek.PeekerPool;
import org.daisy.util.xml.pool.StAXInputFactoryPool;
import org.xml.sax.SAXParseException;

/**
 * Util class (with main) to check the documentation package within a Pipeline 
 * distribution and report inexistence, invalidity, broken links, etc.
 * <p>This class is typically run at build stage.<p>  
 * <p>This class assumes that documentation resources are not jarred.</p>
 * @author Markus Gylling
 */
public class DocChecker implements FilesetErrorHandler {
					
	private Map<String, Object> mXifProperties = null;
	
	/**
	 * @param pipelineRootDir a string representing the root directory of the pipeline.
	 */	
	public DocChecker(String pipelineRootDir) throws Exception {
		
		mXifProperties = new HashMap<String, Object>();
		mXifProperties.put(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
		
		Directory  rootDir = new Directory(pipelineRootDir);
		assert(rootDir.exists());
		
		Directory scriptsDir = new Directory(rootDir,"scripts");
		Directory transformersDir = new Directory(rootDir,"transformers");
		Directory docDir = new Directory(rootDir,"doc");
		Directory endUserDocDir = new Directory(docDir,"enduser");
		Directory devDocDir = new Directory(docDir,"developer");
				
		Map<File,String> scriptAndTransformerFiles = new HashMap<File,String>();
		
		scriptAndTransformerFiles.putAll(getDocuments(transformersDir,"transformer","tdf"));
		scriptAndTransformerFiles.putAll(getDocuments(scriptsDir,"taskScript","taskScript"));
				
		//get all existing transformer and script documentation files
		Collection<URI> documentationFiles = parseScriptAndTransformerFiles(scriptAndTransformerFiles);
		
		//add end user docs
		Collection<File> c = endUserDocDir.getFiles(true, ".+\\.[Hh][Tt][Mm][Ll]?");
		for (File f : c) {
			documentationFiles.add(f.toURI());
		}
						
		//add developer docs
		c = devDocDir.getFiles(true, ".+\\.[Hh][Tt][Mm][Ll]?");
		for (File f : c) {
			documentationFiles.add(f.toURI());
		}
		
		//since the documentation files are XHTML, we run a Fileset instance on each 
		//and validate that way.
		System.err.println("[DocChecker info] Found " + documentationFiles.size() + " existing documentation files");
		for (URI uri : documentationFiles) {
			try{
				new FilesetImpl(uri,this,true,false);
			}catch (FilesetFatalException ffe) {
				System.err.println("[DocChecker Warning] FilesetFatalError exception: " 
						+ ffe.getMessage()
						+ ffe.getRootCauseMessagesAsString());
			}
		}
		System.err.println("DocChecker done.");
	}

	private Set<URI> parseScriptAndTransformerFiles(Map<File, String> scriptAndTransformerFiles) throws Exception {
		//incoming is <transformer> and <taskScript> documents
		//both have an optional element <documentation uri="../relative/document.html">
		//warn if this element is not present
		//if element is present, resolve the URI and check if the destination exists
		//if it exists, add to return set
		//if it doesnt exist, warn
		Set<URI> existingDocumentationFiles = new HashSet<URI>();
		
		for (Iterator<?> iter = scriptAndTransformerFiles.keySet().iterator(); iter.hasNext();) {
			File file = (File) iter.next();
			if(file.getName().toLowerCase().contains("multiformat")) {
				System.err.println("stop");
			}
			String docURI = parseForDocURI(file);
			if(docURI==null){
				//dont report _dev errors				
				if(!file.getParentFile().getName().equals("_dev"))
					System.err.println("[DocChecker Warning] File " + file.getParentFile().getName()+"/"+file.getName() + " has no inline documentation URI");
			}else{
				URI resolvedURI = URIUtils.resolve(file.toURI(), docURI);
				File test = new File(resolvedURI);
				if(test.exists()) {
					existingDocumentationFiles.add(resolvedURI);
				}else{
					if(!file.getParentFile().getName().equals("_dev"))
					System.err.println("[DocChecker Warning] Documentation URI in " + file.getParentFile().getName()+"/"+file.getName() + " does not resolve. URI is: " + resolvedURI );
				}
			}
			
		}
		 								
		return existingDocumentationFiles;		
	}

	private String parseForDocURI(File file) throws Exception {
		XMLInputFactory xif = StAXInputFactoryPool.getInstance().acquire(mXifProperties);
		FileInputStream fis = new FileInputStream(file);
		StreamSource ss = new StreamSource(fis);
		ss.setSystemId(file);
		
		
		XMLEventReader reader = xif.createXMLEventReader(ss);
		
		String ret = null;		
		while (reader.hasNext() && ret == null) {
			XMLEvent event = reader.nextEvent();			
			if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				if(se.getName().getLocalPart().equals("documentation")) {
					Attribute attr = se.getAttributeByName(new QName("uri"));
					if(attr!=null) ret = attr.getValue();											
				}
			}
		}	
		fis.close();
		reader.close();
		StAXInputFactoryPool.getInstance().release(xif, mXifProperties);
		return ret;
	}

	private Map<File,String> getDocuments(Directory rootDir, String rootElemLocalName, String extension) throws Exception {
		 		
		Collection<File> all = rootDir.getFiles(true);
		Map<File,String> ret = new HashMap<File,String>();
				
		Peeker peeker = PeekerPool.getInstance().acquire();
		for (File file : all) {
			EFile ef = new EFile(file);
			if(ef.getExtension()!=null && ef.getExtension().equals(extension)) {
				try {
					PeekResult result = peeker.peek(file);	
					if(result.getRootElementLocalName().equals(rootElemLocalName)){
						ret.put(file, rootElemLocalName);
					}
				}catch (Exception e) {
					System.err.println("[DocChecker Warning] Peeker exception in " + file.getName());
				}
			}
		}
		PeekerPool.getInstance().release(peeker);		
		System.err.println("[DocChecker info] Found " + ret.size() + " files of type " + rootElemLocalName);
		return ret;
	}

	@SuppressWarnings("unused")
	public void error(FilesetFileException ffe) throws FilesetFileException {
		String line = "";
		if(ffe.getCause().getMessage().contains("no matching file type found for")) return;
		
		if(ffe.getCause() instanceof SAXParseException) {
			SAXParseException spe = (SAXParseException) ffe.getCause();
			line = Integer.toString(spe.getLineNumber());
		}
		System.err.println("[DocChecker Warning] FilesetFileException in: " 
				+ ffe.getOrigin().getFile().getParentFile().getName() + "/" 
				+ ffe.getOrigin().getName() + "[" + line +  "]: "
				+ ffe.getCause().getMessage()
				);		
	}
	
	public static void main(String[] args) throws Exception {
		new DocChecker(args[0]);
	}

}
