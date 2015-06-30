package se_tpb_rmfCreator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.daisy.util.i18n.UCharReplacer;

public class CharsetNormalizer2{

	private Charset charset;
	private CharsetEncoder enc;
	private UCharReplacer charReplacer;

	
	public CharsetNormalizer2()	throws IOException {
		charReplacer = new UCharReplacer();
		charReplacer.addSubstitutionTable(this.getClass().getResource("character-translation-table.xml"));
		charset = Charset.forName("iso8859-1");
		enc = charset.newEncoder();
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
