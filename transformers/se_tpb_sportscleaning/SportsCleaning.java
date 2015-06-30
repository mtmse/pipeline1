/**
 * 
 */
package se_tpb_sportscleaning;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.transformer.Transformer;
import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.xml.catalog.CatalogEntityResolver;
import org.daisy.util.xml.catalog.CatalogExceptionNotRecoverable;
import org.daisy.util.xml.stax.StaxEntityResolver;

/**
 * @author jawi
 * inside the tagg named <level1 class="sports"></level1> all - in between numbers are surrounded with a acronym tag 
 */
public class SportsCleaning extends Transformer {
	
	private final String cleaningLevel = "level1";
	private final String startCleaningElementName="sports";

	/**
	 * sportsCleaningRegex to clean sport part in news paper from - between numbers
	 */
	public static final String sportsCleaningRegex = "([0-9]+)(\\s*[-–—]+\\s*)([0-9]+)";
	
	
	
	 XMLEventFactory eventFactory ;

	public SportsCleaning(InputListener inListener, Boolean isInteractive) {
		super(inListener, isInteractive);
		eventFactory = XMLEventFactory.newInstance();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.core.transformer.Transformer#execute(java.util.Map)
	 */
	@Override
	protected boolean execute(Map<String, String> parameters)
			throws TransformerRunException {
		 String input = parameters.remove("input");
	      String output = parameters.remove("output");
		
	      XMLEventReader reader = null;
	      XMLEventWriter writer = null;
	      
	      XMLInputFactory factory = XMLInputFactory.newInstance();
	        try {
	            factory.setXMLResolver(new StaxEntityResolver(CatalogEntityResolver.getInstance()));
	            reader = factory.createXMLEventReader(new FileInputStream(input));
	    	 
	            XMLOutputFactory   outputFactory = XMLOutputFactory.newInstance();
	            
	            outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.FALSE);
	            
	    	  writer = outputFactory.createXMLEventWriter(new FileOutputStream(output), "utf-8");
	          

	    	  boolean hasSeenStartElement = false, hasSeenEndElement = false; 
			  while (reader.hasNext()) {
			      XMLEvent event = (XMLEvent) reader.next();
			     
			     
			      
		    	  if (event.getEventType() == event.START_ELEMENT && isElementStartElementForCleaning (event, startCleaningElementName)) {
		    		  hasSeenStartElement = true;
		    		 
		    	  }
		    	  if( event.getEventType() == event.END_ELEMENT && event.asEndElement().getName().toString().contains(cleaningLevel) && hasSeenStartElement) {
		    		  hasSeenEndElement = true;
		    	  }
			      
			      
			      if (hasSeenStartElement && !hasSeenEndElement) {
			    	  event = performCleaning(event, writer );
			    	 
			      }
			      
			      if(hasSeenStartElement && hasSeenEndElement) {
			    	  hasSeenEndElement = false;
			    	  hasSeenStartElement = false;
			      }
			      
			     if(event!=null) writer.add(event);
			  }
			  writer.flush();
			  
		} catch (FileNotFoundException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (XMLStreamException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (FactoryConfigurationError e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (CatalogExceptionNotRecoverable e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
		finally {
			try {
				if (reader != null) reader.close();
				if (writer != null) writer.close();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	      
		
		return true;
	}

	/**
	 * if the given event is characters and matches the regex: split the event and surround - between numbers with accronym tagg.  
	 * @param event
	 * @param eventWriter
	 * @return
	 * @throws XMLStreamException
	 */
	private XMLEvent performCleaning(XMLEvent event, XMLEventWriter eventWriter) throws XMLStreamException {
		XMLEvent mEvent = event;
		if (event.isCharacters()) {
			String inputStr = event.asCharacters().getData();
			
			Pattern pattern = Pattern.compile(SportsCleaning.sportsCleaningRegex);
			Matcher matcher = pattern.matcher(inputStr);
			Vector<StringSplit> matcherPositions = new Vector<StringSplit>();
			int notMatchStart = 0;
			int countMatches =0;
			boolean matchFound = matcher.find();
			
			while(matchFound) {
				//handle regex match
				
				for (int i=1; i<=matcher.groupCount();i++) {
					
				
					if (i%3==2) {
						String matchStr = matcher.group(i);
						int startMatch =  inputStr.indexOf(matcher.group(0));
						if (notMatchStart > startMatch) startMatch = notMatchStart;
						int startPos = inputStr.indexOf(matchStr, startMatch);
						if(startPos != notMatchStart && notMatchStart <= inputStr.length()) {
							
							matcherPositions.add(new StringSplit(notMatchStart,startPos, false));
						}
						int endPos = startPos + matchStr.length();
						
						matcherPositions.add(new StringSplit(startPos, endPos, true));
						notMatchStart = endPos;
					}
				}
				
				matchFound = matcher.find();
				countMatches++;
			}
			if(matcher.hitEnd() && notMatchStart < inputStr.length()) {
				matcherPositions.add(new StringSplit(notMatchStart, inputStr.length(), false));
			}
			
			//build xml-event for string splits
			for (StringSplit st:matcherPositions) {
				
				String splitted=inputStr.substring((int)st.getX(), (int)st.getY());
				
				if(st.isRegexMatch()) { //build special acronym-xml-event
					eventWriter.add(eventFactory.createStartElement("", null, "abbr"));
					//eventWriter.add(eventFactory.createAttribute("pronounce", "yes"));
					eventWriter.add(eventFactory.createAttribute("title", "  "));
					eventWriter.add(eventFactory.createCharacters("-"));
					eventWriter.add(eventFactory.createEndElement("", null, "abbr"));
				}else{ //just a normal characters
					eventWriter.add(eventFactory.createCharacters(splitted));
				}
			}
			
			

			
			mEvent= null;
		}
		return mEvent;
	}
	
	
	
	
	
	/**
	 * 
	 * @return true if element does match given element name for cleaning
	 */
	private boolean isElementStartElementForCleaning(XMLEvent event, String cleaningStartElementName) {
		String attribut = "";
		if (event.isStartElement()|| event.isEndElement())  {
			Iterator<Attribute> it = event.asStartElement().getAttributes();
			while(it.hasNext()) {
				Attribute a = it.next();
				if (a.getValue().equals(cleaningStartElementName)) {
					return true;
				}
				
			}
		
			
		}
	
		
		return false;
		
	}
}

