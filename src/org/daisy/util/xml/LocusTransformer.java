/*
 * org.daisy.util (C) 2005-2008 Daisy Consortium
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
package org.daisy.util.xml;

import javax.xml.stream.Location;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.daisy.util.fileset.exception.FilesetFileException;
import org.daisy.util.fileset.validation.message.ValidatorMessage;
import org.w3c.css.sac.CSSParseException;
import org.w3c.dom.DOMLocator;
import org.xml.sax.SAXParseException;

/**
 *  Translate between different API expressions of an XML event locus.
 *  <p>See also {@link org.daisy.util.exception.ExceptionTransformer}</p>
 *  @author Markus Gylling
 */

public class LocusTransformer  {
	
	public static Location newLocation(ValidatorMessage message) {		
		LocationImpl loc =  new LocusTransformer().new LocationImpl();	
		try{			
			loc.setColumnNumber(message.getColumn());
			loc.setLineNumber(message.getLine());
			if(message.getFile()!=null)
				loc.setSystemId(message.getFile().toString());
		}catch (Exception e) {}
		return loc;
	}
	
	public static Location newLocation(SAXParseException spe) {		
		LocationImpl loc =  new LocusTransformer().new LocationImpl();	
		try{
			loc.setColumnNumber(spe.getColumnNumber());
			loc.setLineNumber(spe.getLineNumber());
			loc.setSystemId(spe.getSystemId());
		}catch (Exception e) {}
		return loc;
	}
	
	public static Location newLocation(CSSParseException cpe) {		
		LocationImpl loc =  new LocusTransformer().new LocationImpl();	
		try{
			loc.setColumnNumber(cpe.getColumnNumber());
			loc.setLineNumber(cpe.getLineNumber());
			loc.setSystemId(cpe.getURI());
		}catch (Exception e) {}
		
		return loc;
	}
	
	public static Location newLocation(FilesetFileException ffe) {
		LocationImpl loc =  new LocusTransformer().new LocationImpl();	
		try{
		    Throwable cause = ffe.getRootCause();
		    if (cause==null) cause = ffe.getCause();
		    
		    Location innerLocation = null;
			if(cause instanceof CSSParseException) {
			  innerLocation = LocusTransformer.newLocation((CSSParseException)cause);
			}else if(cause instanceof SAXParseException) {
			  innerLocation = LocusTransformer.newLocation((SAXParseException)cause);
			}
			
			loc.setSystemId(ffe.getOrigin().getFile().toURI().toString());
			if(innerLocation!=null){
				loc.setLineNumber(innerLocation.getLineNumber());
				loc.setColumnNumber(innerLocation.getColumnNumber());
			}
		}catch (Exception e) {}
		
		return loc;
	}
	
	public static Location newLocation(TransformerException te) {
		LocationImpl loc =  new LocusTransformer().new LocationImpl();
		SourceLocator sl = te.getLocator();
		try{
			loc.setColumnNumber(sl.getColumnNumber());
			loc.setLineNumber(sl.getLineNumber());
			loc.setSystemId(sl.getSystemId());
			loc.setPublicId(sl.getPublicId());
		}catch (Exception e) {}
		return loc;
	}
	
	/**
	 * Try to create a Location from inparam Exception, return null if inparam is not
	 * successfully transformed.
	 */
	public static Location newLocation(Exception e) {				
		if(e instanceof SAXParseException) {
			return LocusTransformer.newLocation((SAXParseException)e);
		}else if(e instanceof CSSParseException) {
			return LocusTransformer.newLocation((CSSParseException)e);
		}else if(e instanceof FilesetFileException) {
			return LocusTransformer.newLocation((FilesetFileException)e);
		}else if(e instanceof TransformerException) {
			return LocusTransformer.newLocation((TransformerException)e);
		}
		return null;
	}
	
	public static Location newLocation(DOMLocator location) {		
		LocationImpl loc =  new LocusTransformer().new LocationImpl();		
		try{
			loc.setColumnNumber(location.getColumnNumber());
			loc.setLineNumber(location.getLineNumber());
			loc.setSystemId(location.getUri());
		}catch (Exception e) {}
		return loc;
	}
	
	private LocusTransformer() {
		
	}
	
	
	private class LocationImpl implements Location {		
	    private int mCharacterOffset = -1;
	    private int mColumnNumber = -1;
	    private int mLineNumber = -1;
	    private String mPublicId = null;
	    private String mSystemId = null;
	    
		private LocationImpl () {
			
		}
		
		public int getCharacterOffset() {		
			return mCharacterOffset;
		}

		public int getColumnNumber() {
			return mColumnNumber;
		}

		public int getLineNumber() {
			return mLineNumber;
		}

		public String getPublicId() {
			return mPublicId;
		}

		public String getSystemId() {
			return mSystemId;
		}

		@SuppressWarnings("unused")
		private void setCharacterOffset(int characterOffset) {
			mCharacterOffset = characterOffset;
		}

		private void setColumnNumber(int columnNumber) {
			mColumnNumber = columnNumber;
		}

		private void setLineNumber(int lineNumber) {
			mLineNumber = lineNumber;
		}

		private void setPublicId(String publicId) {
			mPublicId = publicId;
		}

		private void setSystemId(String systemId) {
			mSystemId = systemId;
		}

	}







}
