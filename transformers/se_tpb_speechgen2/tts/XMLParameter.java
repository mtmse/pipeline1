/*
 * Daisy Pipeline (C) 2005-2008 Daisy Consortium
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
package se_tpb_speechgen2.tts;

import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Variable substitution in xml file, both attributes 
 * and text nodes.
 * Replaces variables like ${var_name} to something else, e g
 * /home/myname/myDMFCdir/files.
 * The Map supplied when creating XMLParameter must hold 
 * (in this case) the key-value pair (var_name, /home/myname/myDMFCdir/files).
 * 
 * @author Martin Blomberg
 *
 */
public class XMLParameter {
	private Map<String,String> parameterMapping = null;
	private static boolean DEBUG = false;
	
	public XMLParameter(Map<String,String> map) {
		this.parameterMapping = map;
	}
	
	public Document eval(Document doc) {
		// TODO: just make a copy of the document instead,
		// process the copy and return it instead of the 
		// document provided as parameter.
		DEBUG(doc);
		depthFirstTraversal(doc.getDocumentElement());
		DEBUG(doc);
		
		return doc;
	}
	
	private void depthFirstTraversal(Node node) {
		switch (node.getNodeType()) {
		
		case Node.ELEMENT_NODE:			
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				depthFirstTraversal(list.item(i));
			}
			traverseAttrs(node.getAttributes());
			break;
		
		case Node.TEXT_NODE:
			node.setNodeValue(eval(node.getNodeValue()));
			break;
		
		default:
			// no action
		}
	}
	
	private void traverseAttrs(NamedNodeMap nnm) {
		int len = nnm.getLength();
		for (int i = 0; i < len; i++) {
			Attr at = (Attr) nnm.item(i);
			at.setValue(eval(at.getValue()));
		}
	}
	
	
	private String eval(String strValue) {
		for (Iterator<String> it = parameterMapping.keySet().iterator(); it.hasNext(); ) {
			String key = it.next();
			String value = parameterMapping.get(key);
			value = value.replaceAll("\\\\", "\\\\\\\\");
			strValue = strValue.replaceAll("\\$\\{" + key + "\\}", value);
		}
		
		return strValue;
	}
	
	private static void DEBUG(Document d) {
		if (DEBUG) {
			DEBUG("DEBUG(Document): ");
			try {
				TransformerFactory xformFactory = TransformerFactory.newInstance();  
				javax.xml.transform.Transformer idTransform = xformFactory.newTransformer();
				idTransform.setOutputProperty(OutputKeys.INDENT, "yes");
				Source input = new DOMSource(d);
				Result output = new StreamResult(System.err);
				idTransform.transform(input, output);
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void DEBUG(String msg) {
		if (DEBUG) {
			System.err.println("XMLParameter: " + msg);
		}
	}
}
