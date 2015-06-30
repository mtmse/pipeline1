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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import org.daisy.util.xml.pool.StAXEventFactoryPool;

/**
 * Represents a primitive single metadata item, serving as a superclass for more specialized metadata types. 
 * <p>Each metadata item has a QName, a value and zero or several attributes.</p>
 * @author Markus Gylling
 */
public class MetadataItem {
	private static StAXEventFactoryPool mEventFactoryPool = null; 
	private QName mQName = null;	
	private String mValue = null;
	private List<Attribute> mAttributes = null;
		
	/**
	 * Constructor.
	 * @param qName the qualified XML name of this metadata item
	 */
	public MetadataItem(QName qName) {
		this(qName,null);
	}
	
	/**
	 * Constructor.
	 * @param name the unqualified XML name of this metadata item
	 */
	public MetadataItem(String name) {
		this(new QName(name),null);
	}
	
	/**
	 * Constructor.
	 * @param qName the qualified XML name of this metadata item
	 * @param value the main value of this metadata item.
	 */
	public MetadataItem(QName qName, String value) {
		if(qName==null) throw new NullPointerException();
		if(mEventFactoryPool==null) mEventFactoryPool=StAXEventFactoryPool.getInstance();
		mQName = qName;
		mValue = value;
		mAttributes = new LinkedList<Attribute>();  
	}
	
	/**
	 * Set the main value of this metadata item.
	 * <p>If a value is not set, the metadata item will be rendered as an empty element, else the value will appear as a text child of the constructor QName.</p>
	 */
	public void setValue(String value) {
		mValue = value;
	}
	
	public void addAttribute(Attribute a) {
		mAttributes.add(a);
	}
		
	public void addAttribute(QName name, String value)  {
		XMLEventFactory xef = mEventFactoryPool.acquire();
		if(mAttributes==null) mAttributes = new LinkedList<Attribute>();
		try{
			mAttributes.add(xef.createAttribute(name, value));			
		}finally{
			mEventFactoryPool.release(xef);
		}
	}
	
	public void addAttribute(String name, String value)  {
		this.addAttribute(new QName(name), value);
	}

	public void addAttribute(String nsURI, String name, String value)  {
		this.addAttribute(new QName(nsURI, name), value);
	}
	
	/**
	 * Retrieve this metadata item as a list of XMLEvent
	 */
	public List<XMLEvent> asXMLEvents() {
		List<XMLEvent> list = new LinkedList<XMLEvent>();
		XMLEventFactory xef = mEventFactoryPool.acquire();
		try{
			list.add(xef.createStartElement(mQName, null, null));
			for(Attribute a : mAttributes) {
				list.add(a);
			}
			if(mValue!=null) {
				list.add(xef.createCharacters(mValue));
			}			
			list.add(xef.createEndElement(mQName, null));
		}finally{
			mEventFactoryPool.release(xef);
		}		
		return list;
	}
		
	public void asXMLEvents(XMLEventConsumer consumer) throws XMLStreamException {
		List<XMLEvent> list = this.asXMLEvents();
		for (XMLEvent event : list) {
			consumer.add(event);
		}
	}
	
	public String getValue() {
		return mValue;
	}
	
	public QName getQName() {
		return mQName;
	}
	
	public ListIterator<Attribute> getAttributes() {
		return mAttributes.listIterator();
	}
}
