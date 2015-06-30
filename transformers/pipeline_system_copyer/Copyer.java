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
package pipeline_system_copyer;

import java.io.File;
import java.util.Map;

import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.event.MessageEvent;
import org.daisy.pipeline.core.transformer.Transformer;
import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.file.Directory;
import org.daisy.util.file.FileUtils;

/**
 * Transformer that copies resources on the filesystem.
 * @author Markus Gylling
 */
public class Copyer extends Transformer {
	
	
	public Copyer(InputListener inListener, Boolean isInteractive) {
		super(inListener, isInteractive);
	}

	@SuppressWarnings({"unchecked", "unused"})
	@Override
	protected boolean execute(Map parameters) throws TransformerRunException {		
		try{
			
			String inputParam = (String)parameters.remove("input");
			String destinationParam = (String)parameters.remove("destination");
			String inputExcludeRegex = (String)parameters.remove("inputExcludeRegex");
			String inputDeepParam = (String)parameters.remove("inputDeep");
			String overwriteParam = (String)parameters.remove("overwrite");
			
			boolean deep = inputDeepParam.equals("true");
			boolean overwrite = overwriteParam.equals("true");
			if(inputExcludeRegex!=null && inputExcludeRegex.length()==0) inputExcludeRegex=null; 
			
			File input = new File(inputParam);
			if(!input.exists()) throw new TransformerRunException(input + " does not exist");
			
			this.sendMessage(i18n("COPYING", input.getAbsolutePath()), MessageEvent.Type.INFO_FINER, MessageEvent.Cause.SYSTEM);
			
			if(input.isFile()) {
				File dest = new File(destinationParam);
				if(overwrite ||(!dest.exists())){
					FileUtils.copyFile(input, dest );
				}else{
					this.sendMessage(i18n("COPY_CANCELED", dest.getAbsolutePath()), MessageEvent.Type.INFO_FINER, MessageEvent.Cause.SYSTEM);					
				}
			}else if(input.isDirectory()){	
				Directory inputFolder = new Directory(input);
				File output = new File(destinationParam);
				FileUtils.createDirectory(output);							
				Directory destination = new Directory(output);				
				boolean result = inputFolder.copyChildrenTo(destination, overwrite, deep, inputExcludeRegex );
				if(!result) {
					this.sendMessage(i18n("COPY_CANCELED_UNKNOWN"), MessageEvent.Type.INFO_FINER, MessageEvent.Cause.SYSTEM);
				}
			}else{
				throw new TransformerRunException("input is not recognized"); 
			}

		}catch (Exception e) {
			this.sendMessage(i18n("COPIER_MAIN_EXCEPTION", e.getMessage()), MessageEvent.Type.WARNING, MessageEvent.Cause.SYSTEM);			
		}		
		return true;
	}

}
