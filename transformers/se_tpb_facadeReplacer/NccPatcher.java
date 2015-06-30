package se_tpb_facadeReplacer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.daisy.util.fileset.D202NccFile;
import org.daisy.util.fileset.util.FilesetConstants;
import org.daisy.util.xml.SmilClock;
import org.daisy.util.xml.catalog.CatalogEntityResolver;
import org.daisy.util.xml.catalog.CatalogExceptionNotRecoverable;
import org.daisy.util.xml.pool.PoolException;
import org.daisy.util.xml.pool.StAXEventFactoryPool;
import org.daisy.util.xml.pool.StAXInputFactoryPool;
import org.daisy.util.xml.pool.StAXOutputFactoryPool;
import org.daisy.util.xml.stax.StaxEntityResolver;

/**
 * Patch the ncc.html file
 * @author Linus Ericson
 */
class NccPatcher {
	
	private XMLEventFactory mXef;
	private XMLEventWriter mWriter;
	private SmilClock mNccTotalTime;
	
	// FIXME audio filename path will be incorrect if the smil files are located in a subdirectory
	
	public void patch(D202NccFile inputFile, File outputFile, String nccId, String smilId, String smilPrefix, String noticeText, String pdtbVersion, String pdtbBookKey, String pdtbNccFile, SmilClock nccTotalTime) throws PoolException, CatalogExceptionNotRecoverable, XMLStreamException, IOException {
		
		mNccTotalTime = nccTotalTime;
		
		// Setup factories
		Map<String, Object> mXifProperties = new HashMap<String,Object>();
		mXifProperties.put(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
		
		Map<String, Object> mXofProperties = new HashMap<String,Object>();
		mXofProperties.put(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.FALSE);
		
		XMLInputFactory xif = StAXInputFactoryPool.getInstance().acquire(mXifProperties);						
		xif.setXMLResolver(new StaxEntityResolver(CatalogEntityResolver.getInstance()));
		
		mXef = StAXEventFactoryPool.getInstance().acquire();
		XMLOutputFactory xof = StAXOutputFactoryPool.getInstance().acquire(mXofProperties);
				
		XMLEventReader reader = xif.createXMLEventReader(new FileInputStream(inputFile.getFile()));		
		OutputStream outputStream = new FileOutputStream(outputFile);
		
		boolean firstH1Seen = false;
		String latestHref = null;
		boolean skipNext = false;
		
		
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			
			if (skipNext && !event.isStartElement()) {
				continue;
			}
			skipNext = false;
			
			if (event.isStartDocument()) {
				// Create writer
				StartDocument sd = (StartDocument)event;
            	if (sd.encodingSet()) {
            	    mWriter = xof.createXMLEventWriter(outputStream, sd.getCharacterEncodingScheme());
            	    mWriter.add(event);
            	} else {
            	    mWriter = xof.createXMLEventWriter(outputStream, "utf-8");
            	    event = mXef.createStartDocument("utf-8", "1.0");
            	    mWriter.add(event);
            	}
            	event = mXef.createSpace("\r\n");
			} else if (event.isEndElement() && !firstH1Seen) {
				EndElement ee = event.asEndElement();
				// When we see the first </h1> we know it's the right time to insert
				// the reference to the PDTB notice clip
				if ("h1".equals(ee.getName().getLocalPart())) {
					firstH1Seen = true;
					this.injectNoticeRef(nccId, smilId, latestHref, noticeText, smilPrefix);
				} else if ("head".equals(ee.getName().getLocalPart())) {
					// Insert the PDTB metadata at the end of the head section
					this.injectPdtbMetadata(pdtbVersion, pdtbBookKey, pdtbNccFile);
				}
			} else if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				if ("a".equals(se.getName().getLocalPart())) {
					// Add the facade smil file prefix to all links
					Collection<Attribute> coll = new ArrayList<Attribute>();
					for (Iterator it = se.getAttributes(); it.hasNext(); ) {
						Attribute att = (Attribute)it.next();
						if ("href".equals(att.getName().getLocalPart())) {
							latestHref = att.getValue();
							att = mXef.createAttribute("href", smilPrefix + att.getValue());
						}
						coll.add(att);
					}
					event = mXef.createStartElement(se.getName(), coll.iterator(), se.getNamespaces());					
				} else if ("meta".equals(se.getName().getLocalPart())) {
					Attribute name = se.getAttributeByName(new QName("name"));
					Attribute content = se.getAttributeByName(new QName("content"));
					Attribute scheme = se.getAttributeByName(new QName("scheme"));
					if (name != null && content != null) {
						// Update metadata item
						event = this.handleMeta(name.getValue(), content.getValue(), scheme!=null?scheme.getValue():null, se.getNamespaces());
						if (event == null) {
							skipNext = true;							
						}
					}
				}
			} else if (event.getEventType() == XMLStreamConstants.DTD) {
				mWriter.add(event);
				event = mXef.createSpace("\r\n");
			} else if (event.isCharacters()) {
				// Convert to DOS line endings to make all players happy
				Characters ch = (Characters)event;
				event = mXef.createCharacters(ch.getData().replace("\r\n", "\n").replace("\n", "\r\n"));
			}
			if (event != null) {
				mWriter.add(event);
			}
		}
		
		
		reader.close();						
		StAXInputFactoryPool.getInstance().release(xif, mXifProperties);
		StAXEventFactoryPool.getInstance().release(mXef);
		
		mWriter.flush();
		mWriter.close();
		outputStream.close();
		
		StAXOutputFactoryPool.getInstance().release(xof, mXofProperties);
	}
	
	/**
	 * Update a metadata element. Make sure the attributes are written in
	 * the following order to make all players happy: name, content, scheme.
	 * @param name
	 * @param content
	 * @param scheme
	 * @param namespaces
	 * @return
	 */
	private StartElement handleMeta(String name, String content, String scheme, Iterator namespaces) {
		Collection<Attribute> attrs = new ArrayList<Attribute>();
		attrs.add(mXef.createAttribute("name", name));
		// Add one tocItem
		if ("ncc:tocItems".equals(name)) {
			content = String.valueOf(Integer.parseInt(content)+1);
		}
		// Update ncc:totalTime
		if ("ncc:totalTime".equals(name)) {
			if (mNccTotalTime != null) {
				content = mNccTotalTime.toString();
				if (content.contains(".")) {
					content = content.substring(0, content.indexOf("."));
				}
			}
		}
		if ("ncc:file".equals(name) || "ncc:kByteSize".equals(name)) {
			// Don't bother to update these, we remove them since they are optional
			return null;
		}
		attrs.add(mXef.createAttribute("content", content));
		if (scheme != null) {
			attrs.add(mXef.createAttribute("scheme", scheme));
		}
		return mXef.createStartElement(new QName(FilesetConstants.NAMESPACEURI_XHTML10, "meta", ""), attrs.iterator(), namespaces);
	}
	
	/**
	 * Inject the PDTB metadata
	 * @param pdtbVersion
	 * @param pdtbBookKey
	 * @param pdtbNccFile
	 * @throws XMLStreamException
	 */
	private void injectPdtbMetadata(String pdtbVersion, String pdtbBookKey, String pdtbNccFile) throws XMLStreamException {
		mWriter.add(space("\t"));
		mWriter.add(startElement("meta"));		
		mWriter.add(attribute("name", "prod:pdtb-version"));
		mWriter.add(attribute("content", pdtbVersion));
		mWriter.add(endElement("meta"));
		mWriter.add(space("\r\n\t\t"));
		
		mWriter.add(startElement("meta"));		
		mWriter.add(attribute("name", "prod:pdtb-bookKey"));
		mWriter.add(attribute("content", pdtbBookKey));
		mWriter.add(endElement("meta"));
		mWriter.add(space("\r\n\t\t"));
		
		mWriter.add(startElement("meta"));		
		mWriter.add(attribute("name", "prod:pdtb-nccFile"));
		mWriter.add(attribute("content", pdtbNccFile));
		mWriter.add(endElement("meta"));
		mWriter.add(space("\r\n\t"));
	}
	
	/**
	 * Inject the reference to the notice smil clip
	 * @param nccId
	 * @param smilId
	 * @param latestHref
	 * @param noticeText
	 * @param smilPrefix
	 * @throws XMLStreamException
	 */
	private void injectNoticeRef(String nccId, String smilId, String latestHref, String noticeText, String smilPrefix) throws XMLStreamException {
		mWriter.add(endElement("h1"));
		mWriter.add(space("\r\n\t\t"));
		
		mWriter.add(startElement("h1"));
		mWriter.add(attribute("id", nccId));
		mWriter.add(attribute("class", "notice"));
		
		mWriter.add(startElement("a"));
		mWriter.add(attribute("href", getNoticeHref(latestHref, smilPrefix, smilId)));		
		mWriter.add(characters(noticeText));
		mWriter.add(endElement("a"));
		
	}
	
	private String getNoticeHref(String latestHref, String smilPrefix, String smilId) {
		String smilName = latestHref.substring(0, latestHref.indexOf("#"));
		return smilPrefix + smilName + "#" + smilId;
	}
		
	/* Just some helper function below... */
	
	private StartElement startElement(String name) {
		return mXef.createStartElement("", FilesetConstants.NAMESPACEURI_XHTML10, name);
	}
	
	private EndElement endElement(String name) {
		return mXef.createEndElement("", FilesetConstants.NAMESPACEURI_XHTML10, name);
	}
	
	private Attribute attribute(String name, String value) {
		return mXef.createAttribute(name, value);
	}
	
	private Characters space(String value) {
		return mXef.createSpace(value);
	}
	
	private Characters characters(String value) {
		return mXef.createCharacters(value);
	}
	
}
