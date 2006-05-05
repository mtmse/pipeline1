package org.daisy.util.fileset.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;

import org.daisy.util.fileset.FilesetType;
import org.daisy.util.fileset.exception.FilesetFatalException;
import org.daisy.util.fileset.interfaces.FilesetFile;


/**
 * <p>Use to retrieve single instances from the FilesetFile hierarchy, ie a FilesetFile without a Fileset owner</p>
 * <p>Usage example:</p>
 * <pre><code>
 *  FilesetFileFactory fac = FilesetFileFactory.newInstance();
 *	File in = new File("D:/myFile.html");
 *	try {
 *		FilesetFile ret = fac.newFilesetFile(in,null);
 *		if(ret instanceof Xhtml10File){
 *			Xhtml10File xht = (Xhtml10File) ret;
 *			xht.parse();
 *			//...
 *		}else if (ret instanceof HtmlFile) {				
 *			//...
 *		}
 *	} catch (FilesetFatalException e) {
 *		throw e;
 *	}
 *		
 *	//alternatively, using the other newFilesetFile method
 *  //which forces the use of a certain interface
 *	try {
 *		FilesetFile ret2 = fac.newFilesetFile("Xhtml10File", in.toURI());
 *		Xhtml10File xht = (Xhtml10File) ret2;
 *		//...
 *	} catch (FilesetFatalException e) {
 *		throw e;
 *	}
 * 
 * </code></pre>
 * @author Markus Gylling
 */
public final class FilesetFileFactory {
	
	public static FilesetFileFactory newInstance() {
		return new FilesetFileFactory();
	}

	/**
	 * <p>Uses the heuristic type detection algorithm within Fileset
	 * to instantiate and return a subclass of FilesetFile that
	 * best matches the incoming file object.</p> 
	 * <p>The actual type of FilesetFile subclass returned is determined 
	 * by the caller via casting or instanceof tests.</p>
	 * @param file the File object for which a FilesetFile subclass should be returned 
	 * @return a FilesetFile subclass. If detection fails, a FilesetFile implementing the
	 * interface AnonymousFile will be returned.
	 * @throws FilesetFatalException 
	 */
	public FilesetFile newFilesetFile(File file) throws FilesetFatalException {
		return newFilesetFile(file, FilesetType.UNKNOWN);		
	}

	
	/**
	 * <p>Uses the heuristic type detection algorithm within Fileset
	 * to instantiate and return a subclass of FilesetFile that
	 * best matches the incoming file object.</p> 
	 * <p>The actual type of FilesetFile subclass returned is determined 
	 * by the user via casting or instanceof tests.</p>
	 * @param file the File object for which a FilesetFile subclass should be returned
	 * @param filesetType the FilesetType that the expected returned file is a typical member of. 
	 * @return a FilesetFile subclass. If detection fails, a FilesetFile implementing the
	 * interface AnonymousFile will be returned.
	 * @throws FilesetFatalException 
	 */
	public FilesetFile newFilesetFile(File file, FilesetType filesetType) throws FilesetFatalException {
		try {
			if(file==null) throw new NullPointerException("file is null");
			if(filesetType==null) throw new NullPointerException("filesetType is null");				
			if(!file.exists()) throw new FileNotFoundException();
			if(!file.canRead()) throw new IOException();										
			return FilesetImpl.getType(null,file.toURI(),file.toString(),filesetType);
		} catch (Exception e) {
			throw new FilesetFatalException(e);
		} 		
	}
	
	/**
	 * Instantiates and returns a FilesetFile that implements the interface given in inparam.
	 * @param interfaceName the local/simple name of the interface that the return object should implement. 
	 * The interface simplename must be one of those in org.daisy.util.fileset.interfaces.
	 * @param resource URI of the resource to return a FilesetFile for. Only local URIs are explicitly supported at the moment.
	 * @return An implementation of the interface given in inparam.
	 * @throws FilesetFatalException 
	 */
	public FilesetFile newFilesetFile(String interfaceName, URI resource) throws FilesetFatalException {
		try {
			File f = new File(resource);
			if(!f.exists()) throw new FileNotFoundException();
			if(!f.canRead()) throw new IOException();
			//assumes that the naming convention is predictable... this could be improved
			//by iterating over impls and checking getInterfaces.
			String implName = "org.daisy.util.fileset.impl." + interfaceName + "Impl";		
			Class implClass = Class.forName(implName);		
			Constructor constr = implClass.getDeclaredConstructor(new Class[] {URI.class});
			return (FilesetFile) constr.newInstance(new Object[] {resource});
		} catch (Exception e) {
			throw new FilesetFatalException(e);
		} 		
	}		
}