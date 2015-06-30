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
package int_daisy_mixedContentNormalizer.dom;

import int_daisy_mixedContentNormalizer.AbstractNormalizer;
import int_daisy_mixedContentNormalizer.SiblingState;
import int_daisy_mixedContentNormalizer.dom.DOMConfig.TextNodeType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.daisy.pipeline.core.transformer.TransformerDelegateListener;
import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.xml.pool.StAXEventFactoryPool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

/**
 * A mixed content normalizer using DOM.
 * @author Markus Gylling
 */
public class DOMNormalizer extends AbstractNormalizer {
	private DOMConfig mConfig = null;		
	private int mInputDocElementCount = 0;
	private int mCurrentElementCount = 0;
	private Map<String, Element> mWrapperElementCache = null;
	
	
	public DOMNormalizer(TransformerDelegateListener transformer, DOMConfig dnc){
		super(transformer);
		mConfig = dnc;
		mWrapperElementCache = new HashMap<String, Element>(); 
	}
		
	public Result normalize(Source input) throws TransformerRunException {
		mModCount = 0;		
		try{
			
			DOMSource ds = (DOMSource) input;
			Document doc = (Document)ds.getNode();
						
			/*
			 * Set up a TreeWalker and iterate over the DOM instance.
			 */
			DocumentTraversal dt = (DocumentTraversal)doc;
            TreeWalker walker = dt.createTreeWalker(doc.getDocumentElement(),
            		NodeFilter.SHOW_ELEMENT, new NodeFilterImpl(), true); 
            
            Element e = null;     
            
		    while ((e=(Element)walker.nextNode())!=null) {					    			    	

		    	if(mTransformer.delegateCheckAbort()) throw new TransformerRunException ("user abort");	
		    	
		    	NodeList children = e.getChildNodes();		
		    	SiblingState state = getSiblingState(children);
		    	if(state==SiblingState.UNBALANCED) normalizeChildren(e);
		    	
		    	mCurrentElementCount++;
		    }
			    
		    /*
		     * Done.
		     */
		    return new DOMResult(doc);
		    					
		}catch (Exception e) {			
			throw new TransformerRunException(e.getMessage(),e);
		}	
	}
		
	private SiblingState getSiblingState(NodeList siblings) {
		boolean hasNonWhiteSpaceText = false;
		boolean hasIgnorableElems = false;
		boolean hasNonIgnorableElems = false;
		
		for (int i = 0; i < siblings.getLength(); i++) {
			
			Node child = siblings.item(i);
			if(child.getNodeType() == Node.TEXT_NODE) {				
				TextNodeType tnt = mConfig.getTextNodeType(child.getNodeValue(), 
						child.getParentNode().getNamespaceURI());
				if(tnt == TextNodeType.TEXT) hasNonWhiteSpaceText = true;				
			}
			else
			if(child.getNodeType() == Node.ELEMENT_NODE) {
				Element elm = (Element)child;
				boolean isEmpty = elm.getFirstChild()==null;
				if (!mConfig.isIgnorable(elm)) {
					if(!isEmpty){ 
						hasNonIgnorableElems = true;
					}else{
						//empty, is ignorable even if not in config
					}
				}else{
					//mg20081027
					//is marked as ignorable, check its descendants
					if(mConfig.isIgnorableElementsAndWhitespaceOnly(elm.getChildNodes(), true)
							|| isEmpty 
								||mConfig.isTextOnly(elm.getChildNodes())) {
						hasIgnorableElems = true;	
					}else{
						hasNonIgnorableElems = true;
					}
				}
			}	
			else {
				//System.err.println("unexpected nodetype in getSiblingDescription");
			}
		}	
		
		if(hasNonWhiteSpaceText && !hasNonIgnorableElems) return SiblingState.BALANCED;
		if(hasIgnorableElems && !hasNonIgnorableElems) return SiblingState.BALANCED;					
		if(hasNonIgnorableElems && !hasIgnorableElems && !hasNonWhiteSpaceText) return SiblingState.BALANCED;
		return SiblingState.UNBALANCED;		
	}
		
	private final static String KEY_VISITED_IGNORABLE = "KEY_VISITED_IGNORABLE";
	
	/**
	 * Normalize a sibling list by finding consecutive series of sync-ignorable nodes, and wrapping them.
	 * @param parent an Element in State.UNBALANCED.
	 */
	private void normalizeChildren(Element parent) {
		//find as many consecutive ignorables (text or config ignores) as possible; 0-n per try
		List<Node> ignorables = new LinkedList<Node>();		
		while(true) {
			ignorables.clear();
			getConsecutiveIgnorableSiblings(parent,ignorables);			
			if(ignorables.isEmpty()) break;
			trimAndWrap(ignorables);
		}			
	}
	
	private void getConsecutiveIgnorableSiblings(Element parent,List<Node> ignorables) {
		Node currentNode = null;		
		NodeList children = parent.getChildNodes();
		
		for (int i = 0; i < children.getLength(); i++) {			
			currentNode = children.item(i);
			if(currentNode.getUserData(KEY_VISITED_IGNORABLE)==Boolean.TRUE) continue;
			if (currentNode.getNodeType() == Node.TEXT_NODE 
					|| (currentNode.getNodeType() == Node.ELEMENT_NODE 
							&& mConfig.isIgnorable((Element)currentNode) 
							&& mConfig.isIgnorableElementsAndTextOnly(((Element)currentNode).getChildNodes(), true))) {
				//ignorable element, whitespace node or text node
				ignorables.add(currentNode);		
			}else{
				//first match of a non-ignorable node
				//if we have something collected, abort
				if(!ignorables.isEmpty()) return;
			}
		}	
	}
	
	private boolean trimAndWrap(List<Node> ignorables) {
		//remove leading and trailing whitespace					
		ignorables = trim(ignorables);		
		//if we after trim have exactly one non-ws textnode 
		//or if length (regardless of type) > 1, then wrap		
		if(ignorables.size()>1 || (ignorables.size()==1 && ignorables.get(0).getNodeType() == Node.TEXT_NODE )) { 						
			Element wrapper = wrap(ignorables);								  
			if(mConfig.isScrubbingWrappers(wrapper.getNamespaceURI())) {
				scrub(wrapper); 
			}
			return true;
		}
		//ignorable but not wrapped, mark that
		for(Node n : ignorables) {
			n.setUserData(KEY_VISITED_IGNORABLE, Boolean.TRUE, null);
		}
		return false;
	}
//	
	
	private List<Node> evaluate(List<Node> ignorables) {
		//remove leading and trailing whitespace					
		ignorables = trim(ignorables);		
		//if we after trim have exactly one non-ws textnode 
		//or if length (regardless of type) > 1, then wrap		
		if(ignorables.size()>1 || (ignorables.size()==1 && ignorables.get(0).getNodeType() == Node.TEXT_NODE )) { 						
			Element wrapper = wrap(ignorables);								  
			if(mConfig.isScrubbingWrappers(wrapper.getNamespaceURI())) {
				scrub(wrapper); 
			}
		}	
		ignorables.clear();
		return ignorables;
	}
	
	/**
	 * If first and/or last child of wrapper are textnodes, 
	 * and these begin with whitespace chars,
	 * lift those outside the wrapper
	 */
	
	private void scrub(Element wrapper) {
		Node child = wrapper.getFirstChild();				
		if(child.getNodeType() == Node.TEXT_NODE) {
			scrub(child,Direction.BEFORE);
		}
		
		child = wrapper.getLastChild();
		if(child.getNodeType() == Node.TEXT_NODE) {
			scrub(child,Direction.AFTER);
		}
		
	}

	private void scrub(Node textNode, Direction dir) {
		String value = textNode.getNodeValue();
		StringBuilder movedChars = new StringBuilder();
				
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if(mConfig.isWhitespace(c,textNode.getParentNode().getNamespaceURI())) {
				movedChars.append(c);								
			}
			else{
				break;
			}
		}
		
		if(movedChars.length()>0) {
			//change the inner value
			textNode.setNodeValue(value.substring(movedChars.length()));
			//add a new text node outside wrapper, given direction
			Node newTextNode = textNode.getOwnerDocument().createTextNode(movedChars.toString());
			Element wrapper = (Element)textNode.getParentNode();
			@SuppressWarnings("unused")
			Node inserted = null;
			if(dir==Direction.BEFORE) {
				inserted = wrapper.getParentNode().insertBefore(newTextNode, wrapper);				
			}else{
				Node wrappersNextSibling = wrapper.getNextSibling();
				if(wrappersNextSibling!=null) {
					wrapper.getParentNode().insertBefore(newTextNode, wrappersNextSibling);
				}else{
					inserted = wrapper.getParentNode().appendChild(newTextNode);
				}
			}
			//System.err.println("Moved " + dir.toString() +": [" + inserted.getNodeValue() + "]");
		}			
		
	}
	
	private enum Direction {
		BEFORE,
		AFTER;
	}
	
	/**
	 * Remove leading and trailing whitespace nodes. Remove leading and trailing empty elements.
	 */
	private List<Node> trim(List<Node> nodes) {						
		while(!nodes.isEmpty()) {
			Node n = nodes.get(0);			
			if(n.getNodeType()==Node.TEXT_NODE){				
				TextNodeType tnt = mConfig.getTextNodeType(n.getNodeValue(), n.getParentNode().getNamespaceURI());
				if(!(tnt==TextNodeType.TEXT)){
					n.setUserData(KEY_VISITED_IGNORABLE, Boolean.TRUE, null);
					nodes.remove(0);	
				}else{
					break;
				}
			} else if(n.getNodeType()==Node.ELEMENT_NODE){
				Element e = (Element) n;
				if(e.getFirstChild()==null) {
					n.setUserData(KEY_VISITED_IGNORABLE, Boolean.TRUE, null);
					nodes.remove(0);
				}else{
					break;
				}
			}else{
				break;
			}
		}
		
		while(!nodes.isEmpty()) {
			Node n = nodes.get(nodes.size()-1);
			
			if(n.getNodeType()==Node.TEXT_NODE){
				TextNodeType tnt = mConfig.getTextNodeType(n.getNodeValue(), n.getParentNode().getNamespaceURI());
				if(!(tnt==TextNodeType.TEXT)){
					n.setUserData(KEY_VISITED_IGNORABLE, Boolean.TRUE, null);
					nodes.remove(nodes.size()-1);	
				}else{
					break;
				}
			} else if(n.getNodeType()==Node.ELEMENT_NODE){
				Element e = (Element) n;
				if(e.getFirstChild()==null) {
					n.setUserData(KEY_VISITED_IGNORABLE, Boolean.TRUE, null);
					nodes.remove(nodes.size()-1);
				}else{
					break;
				}	
			}else{
				break;
			}
		}
		return nodes;
	}

	
	/**
	 * Move inparam nodes into a wrapper element. 
	 * The resulting wrapper will be placed at the position of the first node in the inparam list.
	 */
	private Element wrap (List<Node> nodelist) {	
		//System.out.println("wrapping " + nodelist.size() + " nodes");
		Element parent = (Element)nodelist.get(0).getParentNode();
		Element newElem = createWrapper(parent);
		newElem.setUserData("isWrapper","true",null);
		parent.insertBefore(newElem, nodelist.get(0));
		for(Node n : nodelist) {
			Node move = n.getParentNode().removeChild(n);
			newElem.appendChild(move);
			move.setUserData("isWrapped", "true", null);
		}		
		mModCount++;
		return newElem;				
	}
	
	
	private Element createWrapper(Element source) {
		String currentNS = source.getNamespaceURI();
		Element newElem = mWrapperElementCache.get(currentNS);
		if(newElem == null) {				
			StartElement wrapperSource = mConfig.getWrapperElement(currentNS);		
			if(wrapperSource==null){
				XMLEventFactory xef = StAXEventFactoryPool.getInstance().acquire();				
				wrapperSource = xef.createStartElement(source.getPrefix(), source.getNamespaceURI(), source.getLocalName());
				StAXEventFactoryPool.getInstance().release(xef);
			}			
			newElem = source.getOwnerDocument().createElementNS(
				wrapperSource.getName().getNamespaceURI(), 
				wrapperSource.getName().getLocalPart());			
			for (Iterator<?> iter = wrapperSource.getAttributes(); iter.hasNext();) {
				Attribute a = (Attribute) iter.next();
				newElem.setAttribute(a.getName().getLocalPart(), a.getValue());
			}
			mWrapperElementCache.put(currentNS, newElem);
		}
		return (Element)newElem.cloneNode(true);
	}

	class NodeFilterImpl implements NodeFilter{
		public short acceptNode(Node n) {
			Element e = (Element)n;			
			if(e.getUserData("isWrapper")!=null){
				//System.err.println("walker skipping: wrapper encountered " + e.getNodeName());
				return NodeFilter.FILTER_SKIP;
			}

			if(e.getFirstChild()==null){
				//System.err.println("walker skipping: empty element " + e.getNodeName());
				return NodeFilter.FILTER_SKIP;
			}
		    				    
			return NodeFilter.FILTER_ACCEPT;
		}		
	}
	
	/**
	 * Retrieve the number of elements in input document after normalization has been completed.
	 * <p>If this method is called prior to calling {@link #normalize(Source)}, it will return
	 * the initial number of elements in input document.
	 */
	public int getFinalElementCount() {
		return mInputDocElementCount + mModCount;
	}

	public void setInputDocElementCount(int count) {
		mInputDocElementCount = count;
	}

}