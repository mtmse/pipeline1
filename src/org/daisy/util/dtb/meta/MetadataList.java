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
package org.daisy.util.dtb.meta;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import org.daisy.util.dtb.meta.MetadataItem;
import org.daisy.util.xml.pool.StAXEventFactoryPool;
import org.daisy.util.xml.pool.StAXInputFactoryPool;
import org.daisy.util.xml.pool.StAXOutputFactoryPool;

/**
 * Holds an ordered list of abstract XML metadata items.
 * @author Markus Gylling
 */
public class MetadataList extends LinkedList<MetadataItem> {
			
	/**
	 * Constructor.
	 */
	public MetadataList() {
		super();	
	}
	
	/**
	 * Add a metadata item to this list.
	 */
	public boolean add(QName name, String value) {		
		return this.add(new MetadataItem(name,value));
		
	}

	/**
	 * Add a metadata item to this list.
	 */
	public boolean add(String localName, String value) {				
		return this.add(new MetadataItem(new QName(localName),value));
	}
	
	/**
	 * Add a metadata item to this list.
	 */
	public boolean add(String nsURI, String localName, String value) {				
		return this.add(new MetadataItem(new QName(nsURI, localName),value));
	}
	
	/**
	 * Get the first metadata item in the list that matches on inparam QName.
	 * If not match, return null.
	 */
	public MetadataItem get(QName name) {
		for(MetadataItem item : this) {
			if(item.getQName().equals(name)) {
				return item;
			}
		}
		return null;
	}
		
	public List<XMLEvent> asXMLEvents() {
		List<XMLEvent> list = new LinkedList<XMLEvent>();
		for(MetadataItem item : this) {
			list.addAll(item.asXMLEvents());
		}		
		return list;
	}
			
	public void asXMLEvents(XMLEventConsumer consumer) throws XMLStreamException {								
		for (MetadataItem m : this) {
			m.asXMLEvents(consumer);			
		}				
	}
	
	@Override 
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(MetadataItem item : this) {
			sb.append(item.getQName().toString()).append("\n");
			sb.append(item.getValue()).append("\n");
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * Serialize this list as an XML document.
	 */
	public void serialize(File destination) throws XMLStreamException, IOException {
		Map<String,Object> xofProperties = StAXOutputFactoryPool.getInstance().getDefaultPropertyMap();
		XMLOutputFactory xof = null;
		XMLEventFactory xef = null;
		try{
			xof = StAXOutputFactoryPool.getInstance().acquire(xofProperties);
			xef = StAXEventFactoryPool.getInstance().acquire();
			QName root = new QName("MetadataList");
			XMLEventWriter xew = xof.createXMLEventWriter(new FileWriter(destination));
			xew.add(xef.createStartDocument("utf-8"));
			xew.add(xef.createStartElement(root,null,null));			
			this.asXMLEvents(xew);
			xew.add(xef.createEndElement(root,null));			
			xew.add(xef.createEndDocument());
		}finally{
			StAXOutputFactoryPool.getInstance().release(xof, xofProperties);
			StAXEventFactoryPool.getInstance().release(xef);
		}
	}

	/**
	 * Create an instance of this class from an XML serialization.
	 */

	public static MetadataList deserialize(URL source) throws XMLStreamException, IOException {
		Map<String,Object> xifProperties = StAXInputFactoryPool.getInstance().getDefaultPropertyMap(Boolean.FALSE);
		XMLInputFactory xif = null;
		XMLEventFactory xef = null;
		MetadataList ret = new MetadataList();
		try{
			xif = StAXInputFactoryPool.getInstance().acquire(xifProperties);
			xef = StAXEventFactoryPool.getInstance().acquire();
			XMLEventReader xer = xif.createXMLEventReader(source.openStream());
			
			boolean isRootElement = true;
			boolean isMetaElementOpen = false;
			QName rootElem = null;
			MetadataItem m = null;
			
			while(xer.hasNext()) {
				XMLEvent event = xer.nextEvent();
				if(event.isStartElement()) {
					StartElement se = event.asStartElement();
					if(!isRootElement) {
						isMetaElementOpen = true;
						m = new MetadataItem(se.getName());
						for (Iterator<?> iter = se.getAttributes(); iter.hasNext();) {
							m.addAttribute((Attribute) iter.next());							
						}
					}else {
						rootElem = se.getName();
					}
					isRootElement = false;
				}else if(event.isCharacters()) {
					if(m!=null && isMetaElementOpen) m.setValue(event.asCharacters().getData());					
				}else if(event.isEndElement()) {
					isMetaElementOpen = false;
					if(!event.asEndElement().getName().equals(rootElem)) {
						if(m!=null)ret.add(m);	
					}					
				}
			}					
		}finally{
			StAXInputFactoryPool.getInstance().release(xif, xifProperties);
			StAXEventFactoryPool.getInstance().release(xef);
		}
		return ret;
	}
	
	private static final long serialVersionUID = 5154760082579948477L;
}
