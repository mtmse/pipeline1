/**
 * 
 */
package se_tpb_sendBook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.transformer.Transformer;
import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.execution.Command;
import org.daisy.util.execution.ExecutionException;
import org.daisy.util.file.FilenameOrFileURI;

/**
 * @author Janine Wicke
 *
 */
public class SendBook extends Transformer {

	/**
	 * @param inListener
	 * @param isInteractive
	 */
	public SendBook(InputListener inListener, Boolean isInteractive) {
		super(inListener, isInteractive);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.core.transformer.Transformer#execute(java.util.Map)
	 */
	@Override
	protected boolean execute(Map<String, String> parameters) throws TransformerRunException {
		
		File inputFile = FilenameOrFileURI.toFile( parameters.remove("input"));
		
		
		String input2Ssh = parameters.remove("command");
		
		int timeout = Integer.parseInt(parameters.remove("timeout"));
		
		
		try {
			//define input stream
			FileInputStream fIn = new FileInputStream(inputFile);
			
			
			
			
			int returnValue = Command.execute(input2Ssh.split(" "), null, fIn, null, null, timeout, 1000, null);
			
			if (returnValue != 0){
				throw new TransformerRunException("Ssh returned != 0");
			}
			fIn.close();
			
		} catch (FileNotFoundException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (ExecutionException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (IOException e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
			
		
		// TODO Auto-generated method stub
		return true;
	}

}
