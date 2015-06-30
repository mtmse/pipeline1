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
package org.daisy.util.dtb.resource;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Getter for a set of static default Zed Resource files.
 * @author Markus Gylling
 */
public class ResourceFile {

	public static enum Type {
		TEXT_ONLY;
	}
	
	/**
	 * Get a set of URLs that represent a resource file and 
	 * its possible auxilliary files. One of the URLs returned
	 * is that of the resource file.
	 */
	public static Set<URL> get(Type type) {
		Set<String> filenames = new HashSet<String>();
		
		if(type == Type.TEXT_ONLY){
			filenames.add("text.res"); 
		}
		
		Set<URL> urls = new HashSet<URL>();
		
		for(String name : filenames) {
			urls.add(ResourceFile.class.getResource(name));
		}
		
		return urls;
	}
}
