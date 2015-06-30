package se_tpb_facadeReplacer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.daisy.util.fileset.AudioFile;
import org.daisy.util.xml.SmilClock;
import org.daisy.util.xml.catalog.CatalogEntityResolver;
import org.daisy.util.xml.catalog.CatalogExceptionNotRecoverable;
import org.daisy.util.xml.pool.PoolException;
import org.daisy.util.xml.pool.StAXEventFactoryPool;
import org.daisy.util.xml.pool.StAXInputFactoryPool;
import org.daisy.util.xml.pool.StAXOutputFactoryPool;
import org.daisy.util.xml.stax.StaxEntityResolver;

/**
 * Add a PDTB notice clip to the first SMIL file. 
 * @author Linus Ericson
 */
class SmilPatcher {
	
	private XMLEventFactory mXef;
	private XMLEventWriter mWriter;
	
	// FIXME audio filename path will be incorrect if the smil files are located in a subdirectory
	
	/**
	 * Add a notice par
	 */
	public void addNoticePar(File inputFile, File outputFile, AudioFile audioFile, String nccId, String smilId) throws PoolException, CatalogExceptionNotRecoverable, XMLStreamException, FileNotFoundException {
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
				
		XMLEventReader reader = xif.createXMLEventReader(new FileInputStream(inputFile));		
		OutputStream outputStream = new FileOutputStream(outputFile);
		
		boolean firstParSeen = false;
		String firstNccReference = null;
		
		
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			
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
			} else if (event.isEndElement() && !firstParSeen) {
				EndElement ee = event.asEndElement();
				if ("par".equals(ee.getName().getLocalPart())) {
					// This is the first </par>. Now is the time to inject the PTDB notice.
					firstParSeen = true;
					injectNoticePar(audioFile, nccId, smilId);
				}
			} else if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				if (!firstParSeen) {
					if ("text".equals(se.getName().getLocalPart())) {
						for (Iterator it = se.getAttributes(); it.hasNext(); ) {
							Attribute att = (Attribute)it.next();
							if ("src".equals(att.getName().getLocalPart())) {
								// Remember the first reference to the ncc.html
								firstNccReference = att.getValue();
							}							
						}							
					}
				} else {
					if ("text".equals(se.getName().getLocalPart())) {
						Collection<Attribute> coll = new ArrayList<Attribute>();
						for (Iterator it = se.getAttributes(); it.hasNext(); ) {							
							Attribute att = (Attribute)it.next();
							if ("src".equals(att.getName().getLocalPart())) {
								if (att.getValue().equals(firstNccReference)) {
									// OK, so we had a reference to the first ncc item. But since
									// this isn't the first par, the PDTB notice has been injected.
									// The reference should point to the PDTB item in the ncc instead.
									att = mXef.createAttribute("src", "ncc.html#" + nccId);
								}								
							}
							coll.add(att);
						}
						event = mXef.createStartElement(se.getName(), coll.iterator(), se.getNamespaces());
					}
				}
			} else if (event.isCharacters()) {
				// Convert to DOS line endings to make all players happy
				Characters ch = (Characters)event;
				event = mXef.createCharacters(ch.getData().replace("\r\n", "\n").replace("\n", "\r\n"));
			}
			mWriter.add(event);
		}
		
		
		reader.close();						
		StAXInputFactoryPool.getInstance().release(xif, mXifProperties);
		StAXEventFactoryPool.getInstance().release(mXef);
		
		mWriter.flush();
		mWriter.close();
		
		StAXOutputFactoryPool.getInstance().release(xof, mXofProperties);
	}
	
	/**
	 * Inject the PDTB notice par.
	 * @param audioFile
	 * @param nccId
	 * @param smilId
	 * @throws XMLStreamException
	 */
	private void injectNoticePar(AudioFile audioFile, String nccId, String smilId) throws XMLStreamException {
		mWriter.add(endElement("par"));
		mWriter.add(space("\r\n\t"));
		
		mWriter.add(startElement("par"));
		mWriter.add(attribute("endsync", "last"));
		mWriter.add(attribute("id", smilId));
		mWriter.add(space("\r\n\t\t"));
		
		mWriter.add(startElement("text"));
		mWriter.add(attribute("id", smilId + "text"));
		mWriter.add(attribute("src", "ncc.html#" + nccId));
		mWriter.add(endElement("text"));
		mWriter.add(space("\r\n\t\t"));
		
		mWriter.add(startElement("audio"));
		mWriter.add(attribute("id", smilId + "audio"));
		mWriter.add(attribute("src", audioFile.getName()));
		mWriter.add(attribute("clip-begin", getClipBegin(audioFile)));
		mWriter.add(attribute("clip-end", getClipEnd(audioFile)));
		mWriter.add(endElement("audio"));
		mWriter.add(space("\r\n\t"));
	}
	
	private String getClipBegin(AudioFile audioFile) {
		SmilClock start = new SmilClock(0);
		return "npt=" + start.toString(SmilClock.TIMECOUNT_SEC);
	}
	
	/**
	 * Get the length of the PDTB notice.
	 * @param audioFile
	 * @return
	 */
	private String getClipEnd(AudioFile audioFile) {
		/*long duration = 0;
		if (audioFile instanceof WavFile) {
			duration = ((WavFile)audioFile).getDuration();
		} else if (audioFile instanceof Mp3File) {
			duration = ((Mp3File)audioFile).getCalculatedDurationMillis();
		} else {
			// FIXME throw			
		}
		
		SmilClock end = new SmilClock(duration);
		*/
		return "npt=" + audioFile.getLength().toString(SmilClock.TIMECOUNT_SEC);
	}

	/* Just some helper function below... */
	
	private StartElement startElement(String name) {
		return mXef.createStartElement("", null, name);
	}
	
	private EndElement endElement(String name) {
		return mXef.createEndElement("", null, name);
	}
	
	private Attribute attribute(String name, String value) {
		return mXef.createAttribute(name, value);
	}
	
	private Characters space(String value) {
		return mXef.createSpace(value);
	}
	
}
