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
package org.daisy.util.fileset.validation.message;

import java.net.URI;

/**
 * Message issued when a Validator encounters an invalid state in the input fileset
 * that is flagged to have a severity level of <em>error</em>. 
 * @author Markus Gylling
 */
public class ValidatorErrorMessage extends ValidatorMessage {

	public ValidatorErrorMessage(URI file, String message, int line, int column) {
		super(file, message, line, column);
	}
	
	public ValidatorErrorMessage(URI file, String message) {
		super(file, message);
	}

}
