/**
 * 
 */
package se_tpb_speechGenCopyBookData;

import integration.content.ChangedSentence;
import integration.jobKernel.JobDatabaseException;
import integration.jobKernel.JobKernelException;
import integration.jobKernel.JobKernelInputException;
import integration.jobKernel.db.AdminDBHandler;
import integration.jobKernel.db.BookDBHandler;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.transaction.TransactionRequiredException;

import org.apache.log4j.Logger;
import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.transformer.Transformer;
import org.daisy.pipeline.exception.TransformerRunException;

/**
 * @author jawi
 * This transformer is needed to integrate gbook (Uttalsverktyget) with pipeline. 
 * Sentences that have been edited with the Uttalsverktyg for a specific book (jobid) will be 
 * copied to a database (PARAM_SPEECHGEN_DB_NAME), where the speech-gen transfomer looks for replacements.
 * 
 * The general database is first emptied. Then all sentences for the given book are copied.
 * 
 * This transfomer needs the jar file gbook.jar
 */
public class SpeechGenCopyBookData extends Transformer {
	
	public static String PARAM_JOB_DB_NAME = "jobDbName";
	public static String PARAM_SPEECHGEN_DB_NAME = "speechgenDbName";
	public static String PARAM_DB_DRIVER = "dbDriver";
	public static String PARAM_DB_URL = "dbUrl";

	Logger logger = Logger.getLogger(SpeechGenCopyBookData.class);

	BookDBHandler bookJobDBHandler;
	BookDBHandler speechGenDBHandler;
	
	AdminDBHandler dbProvider;
	
	public SpeechGenCopyBookData(InputListener inListener, Boolean isInteractive) {
		super(inListener, isInteractive);
		
		bookJobDBHandler = null;
		speechGenDBHandler = null;
		dbProvider = null;
	}
	
	
	
	public void init(String url, String driver, String bookJobName, String speechGenDBName) throws TransformerRunException  {
		dbProvider = new AdminDBHandler(url, driver);
		
		try {
			if(!dbProvider.checkIfDatabaseExists(bookJobName)) throw new TransformerRunException("database for given gbook-job does not exist " + bookJobName);
		
			if(!dbProvider.checkIfDatabaseExists(speechGenDBName)) throw new TransformerRunException("database for speechgen sentence replacer does not exist " + speechGenDBName);
		
			bookJobDBHandler = new BookDBHandler(url, driver, bookJobName);
			speechGenDBHandler = new BookDBHandler(url, driver, speechGenDBName);
		} catch (JobDatabaseException e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
		
	}
	
	private void emptySpeechGenDB() throws TransformerRunException {
		
		try {
			Vector<Integer> idStore = new Vector<Integer>();
			List<ChangedSentence> list = speechGenDBHandler.getAllChangedSentence();
			
			for (ChangedSentence c:list)
				idStore.add(c.getId());
			
			speechGenDBHandler.deleteFromChangedSentence(idStore);
			logger.info(idStore.size()+" sentences deleted. table is empty now!");
		} catch (JobDatabaseException e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
		
	}
	
	private void copySentences() throws TransformerRunException {
		try {
			List<ChangedSentence> list = bookJobDBHandler.getAllChangedSentence();
			for(ChangedSentence c :list) {
				speechGenDBHandler.insertChangedSentence(c);
			}
			logger.info(list.size() + " sentences copied from database " + bookJobDBHandler.getName() + " to " + speechGenDBHandler.getName());
		} catch (JobDatabaseException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (JobKernelException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (JobKernelInputException e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
	}
	
	

	@Override
	protected boolean execute(Map<String, String> parameters)
			throws TransformerRunException {
		String jobdbname = parameters.get(PARAM_JOB_DB_NAME);
		String speechgenDbName = parameters.get(PARAM_SPEECHGEN_DB_NAME);
		String dbUrl = parameters.get(PARAM_DB_URL);
		String dbDriver = parameters.get(PARAM_DB_DRIVER);
		
		this.init(dbUrl, dbDriver, jobdbname, speechgenDbName);
		
		this.emptySpeechGenDB();
		try {
			System.out.println("speechgendb contains sentences: " + this.speechGenDBHandler.getAllChangedSentence().size());
		} catch (JobDatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.copySentences();
		
		return true;
	}

}
