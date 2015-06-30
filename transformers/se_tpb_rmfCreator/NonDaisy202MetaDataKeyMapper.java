package se_tpb_rmfCreator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Maps nds meta data keys to Dublin Core and more.
 * @author piki
 *
 */
public class NonDaisy202MetaDataKeyMapper {

	private static Map<String,String> ndsToDc;
	
	static {
		ndsToDc = new LinkedHashMap<String, String>();
		ndsToDc.put("IDENTIFIER", "dc:identifier");
		ndsToDc.put("TITLE", "dc:title");
		ndsToDc.put("CREATORS_ALL", "dc:creator");
		ndsToDc.put("PUBLISHER", "dc:publisher");
		ndsToDc.put("FORMAT", "dc:format");
		ndsToDc.put("DATE", "dc:date");
		ndsToDc.put("IMAGES", "illustrationer");
	}

	/**
	 * Maps an NDS meta data key (as in se.tpb.nds.archive.bookstock.integration.BookPropertyName)
	 * to a Dublin Core key.
	 * @param ndsKey
	 * @return
	 */
	public static String mapNdsMetadataKeyToDublinCore(String ndsKey){
				String dcKey = null;
		dcKey = ndsToDc.get(ndsKey);
		return dcKey;
	}
	
	public static Set<String> getNdsKeys(){
		return ndsToDc.keySet();
	}
}
