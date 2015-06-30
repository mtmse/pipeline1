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

package org.daisy.pipeline.core.event;

import javax.xml.stream.Location;

public class CoreMessageEvent extends MessageEvent {

	public CoreMessageEvent(Object source, String message, Type type, Cause cause) {
		this(source, message, type, cause, null);
	}

	public CoreMessageEvent(Object source, String message, Type type) {
		this(source, message, type, MessageEvent.Cause.SYSTEM, null);
	}

	private static final long serialVersionUID = -6421079219387999182L;
	
	public CoreMessageEvent(Object source, String message, Type type, Cause cause, Location location) {
		super(source, message, type, cause, location);
	}

}
