package se_tpb_rmfCreator;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;

import org.daisy.util.i18n.UCharReplacer;
import org.daisy.util.xml.stax.StaxFilter;

public class CharsetNormalizer extends StaxFilter {

	private Charset charset;
	private CharsetEncoder enc;
	private UCharReplacer charReplacer;
	
	public CharsetNormalizer(XMLEventReader xer, OutputStream outStream)
			throws XMLStreamException, IOException {
		super(xer, outStream);
		charReplacer = new UCharReplacer();
		charReplacer.addSubstitutionTable(this.getClass().getResource("character-translation-table.xml"));
		//charReplacer.setFallbackState(charReplacer.FALLBACK_TRANSLITERATE_ANY_TO_LATIN, true);
		//charReplacer.setFallbackState(charReplacer.FALLBACK_TRANSLITERATE_REMOVE_NONSPACING_MARKS, true);
		//charReplacer.setExclusionRepertoire(Charset.forName("iso8859-1"));		
		charset = Charset.forName("iso8859-1");
		enc = charset.newEncoder();
	}

	/* (non-Javadoc)
	 * @see org.daisy.util.xml.stax.StaxFilter#characters(javax.xml.stream.events.Characters)
	 */
	@Override
	protected Characters characters(Characters event) {		
		return getEventFactory().createCharacters(translate(event.getData()));
	}

	/* (non-Javadoc)
	 * @see org.daisy.util.xml.stax.StaxFilter#startElement(javax.xml.stream.events.StartElement)
	 */
	@Override
	protected StartElement startElement(StartElement event) {
		if ("meta".equals(event.getName().getLocalPart())) {
			Attribute name = event.getAttributeByName(new QName("name"));
			Attribute content = event.getAttributeByName(new QName("content"));
			if (name != null && content != null) {
				List<Attribute> attrs = new ArrayList<Attribute>();
				attrs.add(name);
				attrs.add(getEventFactory().createAttribute("content", translate(content.getValue())));
				StartElement se = getEventFactory().createStartElement(event.getName(), attrs.iterator(), event.getNamespaces());
				return se;
			}
		}
		return event;
	}
	
	public String translate(String str) {
		String rep = charReplacer.replace(str).toString();		
		CharacterIterator iter = new StringCharacterIterator(rep);		
		StringBuilder sb = new StringBuilder();
		for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
	         if (enc.canEncode(c)) {
	        	 sb.append(c);
	         }
	    }
		rep = sb.toString();
		
		return rep;
	}

}
