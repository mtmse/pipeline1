/**
 * 
 */
package se_tpb_speechgen2.tts.adapters.sentRepl;

import Utils.StringUtils;
import integration.content.ChangedSentence;
import integration.jobKernel.JobDatabaseException;
import integration.jobKernel.JobKernelException;
import integration.jobKernel.JobKernelInputException;
import integration.jobKernel.db.AdminDBHandler;
import integration.jobKernel.db.BookDBHandler;

/**
 * @author Janine Wicke
 * 
 * Queries a database (filled by the speechGenCopyBookData transformer) for sentences edited with Uttalsverktyg.
 * If a sentence is found (original text equals the current sentence) the replacement is sent to the TTS.   
 *
 */
public class SentenceReplacer {
   BookDBHandler speechgenDBHandler;
   
   AdminDBHandler dbprovider;
   
   public SentenceReplacer(String url, String driver, String databaseName) throws SentenceReplacerException {
	   speechgenDBHandler = new BookDBHandler(url, driver, databaseName);
	   dbprovider = new AdminDBHandler(url, driver);
	   
	   try {
		if (!dbprovider.checkIfDatabaseExists(databaseName)) throw new SentenceReplacerException("database does not exist " +databaseName);
	} catch (JobDatabaseException e) {
		throw new SentenceReplacerException(e.getMessage(), e);
	}
   }
   
   /**
    * look original sentence up in db. return replacement if found, otherwise return null. 
    * @param orgSent
    * @throws SentenceReplacerException if input is empty
    */
   public String replaceSentence(String orgSent) throws SentenceReplacerException {
	   if (StringUtils.isStringEmpty(orgSent)) throw new SentenceReplacerException("given input is empty!");
	   
	   ChangedSentence org = new ChangedSentence(orgSent, null);
	   ChangedSentence replacement = null;
	try {
		replacement = speechgenDBHandler.getChangedSentence(org);
		
	} catch (JobDatabaseException e) {
		System.err.println("JobDatabaseException " + e.getMessage());
		throw new SentenceReplacerException(e.getMessage(), e);
	} catch (JobKernelException e) {
		System.err.println("JobKernelException " + e.getMessage());
		throw new SentenceReplacerException(e.getMessage(), e);
	} catch (JobKernelInputException e) {
		System.err.println("JobKernelInputException " + e.getMessage());
		throw new SentenceReplacerException(e.getMessage(), e);
	}
	   
	   String ret = null;
	   if (replacement != null && !StringUtils.isStringEmpty(replacement.getReplacedText()))
		   ret = replacement.getReplacedText();
	   return ret;
   }
}

