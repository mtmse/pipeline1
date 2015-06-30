package se_tpb_idSwitcher;

import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

/**
 * Filter that works for all XML file types in a Daisy 2.02 fileset.
 * @author Linus Ericson
 *
 */
public class FilterD202 extends FilterBase {

	/**
	 * @param xer
	 * @param outStream
	 * @param identifier
	 * @throws XMLStreamException
	 */
	public FilterD202(XMLEventReader xer, OutputStream outStream, String identifier) throws XMLStreamException {
		super(xer, outStream, identifier);
	}

	/* (non-Javadoc)
	 * @see org.daisy.util.xml.stax.StaxFilter#startElement(javax.xml.stream.events.StartElement)
	 */
	@Override
	protected StartElement startElement(StartElement event) {
		if ("meta".equals(event.getName().getLocalPart())) {
			Attribute att = event.getAttributeByName(new QName("name"));
			if (att != null && "dc:identifier".equals(att.getValue())) {
				ArrayList<Attribute> arr = new ArrayList<Attribute>();
				arr.add(getEventFactory().createAttribute("name", "dc:identifier"));
				arr.add(getEventFactory().createAttribute("content", getIdentifier()));
				return getEventFactory().createStartElement(event.getName(), arr.iterator(), event.getNamespaces());
			}
		}
		return super.startElement(event);
	}
}
