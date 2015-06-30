package se_tpb_idSwitcher;

import java.io.OutputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import org.daisy.util.xml.stax.StaxFilter;

/**
 * Base class for all id switch filters.
 * @author Linus Ericson
 *
 */
public class FilterBase extends StaxFilter {

	private String identifier;
	
	public FilterBase(XMLEventReader xer, OutputStream outStream, String identifier)	throws XMLStreamException {
		super(xer, outStream);
		this.identifier = identifier;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	
}
