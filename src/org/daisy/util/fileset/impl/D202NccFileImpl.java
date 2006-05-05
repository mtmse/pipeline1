package org.daisy.util.fileset.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.daisy.util.fileset.exception.FilesetFileErrorException;
import org.daisy.util.fileset.interfaces.Fileset;
import org.daisy.util.fileset.interfaces.FilesetFile;
import org.daisy.util.fileset.interfaces.xml.d202.D202NccFile;
import org.daisy.util.fileset.util.URIStringParser;
import org.daisy.util.xml.SmilClock;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Represents the Navigation Control Center (NCC) file in a Daisy 2.02 fileset
 * @author Markus Gylling
 */
final class D202NccFileImpl extends Xhtml10FileImpl implements D202NccFile {
	private SmilClock myStatedDuration = null;
	private String myDcIdentifier = null;
	private String myDcTitle = null;	
	private boolean hasNccSetInfo = false;
	private boolean hasRelAttrsInBody = false;
	private LinkedHashMap spineMap= new LinkedHashMap();
	private boolean finalSpineMapIsBuilt = false;

		
    D202NccFileImpl(URI uri) throws ParserConfigurationException, SAXException, IOException {
        super(uri, D202NccFile.mimeStringConstant);          
    }
        
	public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {		
		for (int i = 0; i < attrs.getLength(); i++) {					
			attrName = attrs.getQName(i);
			attrValue = attrs.getValue(i).intern(); //for some reason							
			if (attrName=="id") {
				QName q = new QName(namespaceURI,sName);
				this.putIdAndQName(attrValue,q);
			}else if (regex.matches(regex.XHTML_ATTRS_WITH_URIS,attrName)) {
				putUriValue(attrValue);
			}
			
			if (this.parsingBody && attrName=="rel") {
				this.hasRelAttrsInBody = true;
			}
			
			try {
				if (sName=="meta") {
					if (attrValue=="ncc:totalTime") {
						myStatedDuration = new SmilClock(attrs.getValue("content")); 
					}else if (attrValue=="dc:identifier") {
						myDcIdentifier = attrs.getValue("content");
					}else if (attrValue=="dc:title") {
						myDcTitle = attrs.getValue("content");
					}else if (attrValue=="ncc:setInfo"||attrValue=="ncc:setinfo") {
						String setinfovalue = attrs.getValue("content");
						if (!setinfovalue.equals("1 of 1")){
							this.hasNccSetInfo = true;
						}
					}	
				}	
			} catch (Exception e) {				
				myExceptions.add(new FilesetFileErrorException(this,e));				
			}
			
		} //for (int i
	}	
	
	public InputSource resolveEntity(String publicId, String systemId) throws IOException {
		//override the XmlFileImpl method in order to substitute DTDs
		//from xhtml to the subset one
		if (publicId.startsWith("-//W3C//DTD XHTML")) {
			publicId = "-//DAISY//DTD ncc v2.02//EN";
		}	
		return super.resolveEntity(publicId,systemId);
	}
	
	public SmilClock getStatedDuration() {				
		return myStatedDuration;
	}
	
	public String getDcIdentifier() {
		return myDcIdentifier;
	}
	
	public String getDcTitle() {
		return myDcTitle;
	}

	public boolean hasMultiVolumeIndicators() {
		if(this.hasRelAttrsInBody && this.hasNccSetInfo) {
			return true;
		}
		return false;
	}

	public Collection getSpineItems() throws IllegalStateException {
		if (finalSpineMapIsBuilt) {
			return spineMap.values();	
		}
		throw new IllegalStateException("spinemap is not built");
	}

	/*package*/ void buildSpineMap (Fileset fileset) {
		//this can be done only when a complete fileset is built
		Iterator it = this.getUriStrings().iterator();
		while(it.hasNext()) {
			String str = (String) it.next();
			URI key = null;
			if(str.indexOf(".smil")>=0) {		
				str = URIStringParser.stripFragment(str);
				key = this.getFile().toURI().resolve(str);
				FilesetFile ff = fileset.getLocalMember(key);
				if (ff!=null){
					spineMap.put(key,fileset.getLocalMember(key));
				}
			}									
		}				
		finalSpineMapIsBuilt = true;
	}
	
	private static final long serialVersionUID = 1009746859210460470L;
}