package se_tpb_facadeReplacer;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.daisy.util.fileset.ManifestFile;
import org.daisy.util.xml.catalog.CatalogEntityResolver;
import org.daisy.util.xml.catalog.CatalogExceptionNotRecoverable;
import org.daisy.util.xml.pool.PoolException;
import org.daisy.util.xml.pool.StAXInputFactoryPool;
import org.daisy.util.xml.stax.StaxEntityResolver;

/**
 * Extracts the metadata from a ncc.html file.
 * @author Linus Ericson 
 */
class Metadata {
	
	/**
	 * Extracts the metadata of a book into a map.
	 * @param manifest the ncc.html manifest file
	 * @return a map containing the metadata
	 * @throws PoolException
	 * @throws CatalogExceptionNotRecoverable
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	public static Map<String,String> getNccMetadata(ManifestFile manifest) throws PoolException, CatalogExceptionNotRecoverable, FileNotFoundException, XMLStreamException {
		Map<String,String> metadata = new HashMap<String,String>();
		Map<String, Object> mXifProperties = new HashMap<String,Object>();
		mXifProperties.put(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_VALIDATING, Boolean.TRUE);
		
		XMLInputFactory xif = StAXInputFactoryPool.getInstance().acquire(mXifProperties);						
		xif.setXMLResolver(new StaxEntityResolver(CatalogEntityResolver.getInstance()));
		
		XMLEventReader reader = xif.createXMLEventReader(manifest.asInputStream());
		
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				if ("meta".equals(se.getName().getLocalPart())) {
					Attribute name = se.getAttributeByName(new QName("name"));
					Attribute content = se.getAttributeByName(new QName("content"));
					if (name != null && content != null) {
						metadata.put(name.getValue(), content.getValue());
					}
				}
			}
		}
		
		reader.close();						
		StAXInputFactoryPool.getInstance().release(xif, mXifProperties);
		
		return metadata;
	}

}
