package se_tpb_facadeReplacer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import javazoom.jl.decoder.BitstreamException;

import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.event.MessageEvent;
import org.daisy.pipeline.core.transformer.Transformer;
import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.file.Directory;
import org.daisy.util.file.FilenameOrFileURI;
import org.daisy.util.file.TempFile;
import org.daisy.util.fileset.AudioFile;
import org.daisy.util.fileset.D202NccFile;
import org.daisy.util.fileset.D202SmilFile;
import org.daisy.util.fileset.Fileset;
import org.daisy.util.fileset.FilesetErrorHandler;
import org.daisy.util.fileset.FilesetFile;
import org.daisy.util.fileset.exception.FilesetFatalException;
import org.daisy.util.fileset.exception.FilesetFileException;
import org.daisy.util.fileset.exception.FilesetFileFatalErrorException;
import org.daisy.util.fileset.impl.FilesetFileFactory;
import org.daisy.util.fileset.impl.FilesetImpl;
import org.daisy.util.fileset.util.ManifestFinder;
import org.daisy.util.xml.SmilClock;
import org.daisy.util.xml.catalog.CatalogExceptionNotRecoverable;
import org.daisy.util.xml.pool.PoolException;

/**
 * Replace the facade of a book protected using the pdtbv1 specification.
 * @author Linus Ericson
 */
public class FacadeReplacer extends Transformer implements FilesetErrorHandler {
	
	private static double OLD_FILESET_DONE = 0.1;
	private static double NEW_FILESET_DONE = 0.2;
	private static double COPY_DONE = 0.7;
	private static double SMIL_DONE = 0.9;
	private static double NCC_DONE = 1;
	
	private String mNoticeAudio;
	private String mNoticeText;
	private FilesetFileFactory mFilesetFileFactory;
	
	private Map<String, Boolean> mXofProperties = new HashMap<String,Boolean>();
	private Map<String, Boolean> mXifProperties = new HashMap<String,Boolean>();
	
	private int count = 0;
	private int total = 1;

	public FacadeReplacer(InputListener inListener, Boolean isInteractive) {
		super(inListener, isInteractive);
		
		mFilesetFileFactory = FilesetFileFactory.newInstance();
		
		mXifProperties.put(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
		mXifProperties.put(XMLInputFactory.IS_VALIDATING, Boolean.TRUE);
		
		mXofProperties.put(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.FALSE);
	}

	protected boolean execute(Map parameters) throws TransformerRunException {
		String encrypted = (String)parameters.remove("encrypted");
		String facade = (String)parameters.remove("facade");
		String output = (String)parameters.remove("output");
		mNoticeText = (String)parameters.remove("noticeText");
		mNoticeAudio = (String)parameters.remove("noticeAudio");	
		
		try {
			File inputBase = FilenameOrFileURI.toFile(encrypted);
			File facadeBase = FilenameOrFileURI.toFile(facade);
			File outputBase = FilenameOrFileURI.toFile(output);
			
			// Input can be either a directory containing one or more books
			// or a direct reference to a ncc.html manifest file
			if (inputBase.isDirectory()) {
				
				Collection inputFiles = ManifestFinder.getManifests(true, new Directory(inputBase));
				Collection<FilesetFile> manifests = new ArrayList<FilesetFile>();
				// Find manifests
				for (Iterator i = inputFiles.iterator(); i.hasNext();) {
					FilesetFile manifest = FilesetFileFactory.newInstance().newFilesetFile((File)i.next());    				
					//if this is a file we should work on given acceptedTypes inparam
					if (manifest instanceof D202NccFile) {					
						manifests.add(manifest);
					}
				}
				
				// Loop through manifests
				total = manifests.size();
				for (Iterator<FilesetFile> it = manifests.iterator(); it.hasNext(); ) {
					FilesetFile manifest = it.next();
					
					File facadeDir = null;
					File outputDir = null;
					
					// Check for new facade
					if(!inputBase.getCanonicalPath().equals(manifest.getParentFolder().getCanonicalPath())){
						URI relative = inputBase.toURI().relativize(manifest.getFile().toURI());
						File hypo = new File(facadeBase, relative.getPath());						
						facadeDir = hypo.getParentFile();
						if (!facadeDir.isDirectory()) {
							throw new TransformerRunException("Not a directory");
						}
					}else{
						facadeDir = facadeBase;
						if (!facadeDir.isDirectory()) {
							throw new TransformerRunException("Not a directory");
						}
					}
					
					// Calculate output dir
					if(!inputBase.getCanonicalPath().equals(manifest.getParentFolder().getCanonicalPath())){
						URI relative = inputBase.toURI().relativize(manifest.getFile().toURI());
						File hypo = new File(outputBase, relative.getPath());						
						outputDir = hypo.getParentFile();						
					}else{
						outputDir = outputBase;
					}
					
					this.sendMessage(i18n("INPUT_FOLDER", manifest.getParentFolder()), MessageEvent.Type.INFO_FINER);
					this.sendMessage(i18n("FACADE_FOLDER", facadeDir), MessageEvent.Type.INFO_FINER);
					this.sendMessage(i18n("OUTPUT_FOLDER", outputDir), MessageEvent.Type.INFO_FINER);
					
					/* Replace the facade book */
					this.replaceFacade(manifest.getParentFolder(), new Directory(facadeDir), new Directory(outputDir));
					count++;
				}
				
			} else {
				// Folder for current facade
				Directory encryptedFolder = new Directory(FilenameOrFileURI.toFile(encrypted));			
				
				// Folder for new facade
				Directory facadeFolder = new Directory(FilenameOrFileURI.toFile(facade));			
				
				// Output folder
				Directory outputFolder = new Directory(FilenameOrFileURI.toFile(output));			
				
				// Replace facade
				this.replaceFacade(encryptedFolder, facadeFolder, outputFolder);
			}
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new TransformerRunException(e.getMessage(), e);
		} catch (FilesetFatalException e) {
			e.printStackTrace();
			throw new TransformerRunException(e.getMessage(), e);
		} catch (CatalogExceptionNotRecoverable e) {
			e.printStackTrace();
			throw new TransformerRunException(e.getMessage(), e);
		} catch (PoolException e) {
			e.printStackTrace();
			throw new TransformerRunException(e.getMessage(), e);
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new TransformerRunException(e.getMessage(), e);			
		} catch (BitstreamException e) {
			e.printStackTrace();
			throw new TransformerRunException(e.getMessage(), e);
		}
		
		return true;
	}
	
	/**
	 * Replaces the facade of a single book.
	 * @param encryptedFolder the folder containing the encrypted book
	 * @param facadeFolder the folder containing the new facade book
	 * @param outputFolder the output folder
	 * @throws CatalogExceptionNotRecoverable
	 * @throws PoolException
	 * @throws XMLStreamException
	 * @throws FilesetFatalException
	 * @throws TransformerRunException
	 * @throws IOException
	 * @throws BitstreamException
	 */
	private void replaceFacade(Directory encryptedFolder, Directory facadeFolder, Directory outputFolder) throws CatalogExceptionNotRecoverable, PoolException, XMLStreamException, FilesetFatalException, TransformerRunException, IOException, BitstreamException {
		
		// Old facade fileset
		Fileset encryptedFileset = new FilesetImpl(new File(encryptedFolder, "ncc.html").toURI(), this, false, false);
		this.progress((double)count/total + OLD_FILESET_DONE/total);
		this.checkAbort();
		
		// New facade fileset
		Fileset facadeFileset = new FilesetImpl(new File(facadeFolder, "ncc.html").toURI(), this, false, false);
		this.progress((double)count/total + NEW_FILESET_DONE/total);
		this.checkAbort();
		
		//*************************************************************
		
		D202NccFile nccFile = null;
		try {
			nccFile = (D202NccFile)facadeFileset.getManifestMember();
		} catch (ClassCastException e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
		
		// Read metadata from old facade
		Map<String,String> oldFacadeMeta = Metadata.getNccMetadata(encryptedFileset.getManifestMember());
		
		// Make sure PDTB metadata exists
		String pdtbVersion = oldFacadeMeta.get("prod:pdtb-version");
		String pdtbBookKey = oldFacadeMeta.get("prod:pdtb-bookKey");
		String pdtbNccFile = oldFacadeMeta.get("prod:pdtb-nccFile");
		if (pdtbVersion == null || pdtbBookKey == null || pdtbNccFile == null) {
			throw new TransformerRunException(encryptedFileset.getManifestMember().getFile() + " is not a PDTB facade manifest (metadata missing)");
		}
		
		// Create output folder
		outputFolder.mkdirs();
		
		// *************************************************************
		
		// Copy everything (including subdirs) from folder with encrypted book
		// to the output folder.
		if (!encryptedFolder.copyChildrenTo(outputFolder, true)) {
			throw new TransformerRunException("Couldn't copy all files from encrypted book directory.");
		}
		this.progress((double)count/total + COPY_DONE/total);
		this.checkAbort();
		
		// Remove pdtbtitle.smil/mp3/html?
		
		// *************************************************************
		
		String nccId = "ncc-pdtb-notice";
		String smilId = "pdtbnotice";
		String smilPrefix = "ncc-";
		
		// Copy notice audio to output
		File noticeAudioFile = new File(this.getTransformerDirectory(), mNoticeAudio);
		outputFolder.addFile(noticeAudioFile);
		AudioFile audioFile = (AudioFile)mFilesetFileFactory.newFilesetFile(noticeAudioFile);
		audioFile.parse();		
				
		// Copy all SMIL files from the new facade 
		//  - Add a filename prefix
		//  - Update ncc:totalElapsedTime
		//  - Insert notice clip into first smil and update ncc:timeInThisSmil
		SmilClock totalTime = this.copySmil(facadeFileset, outputFolder, smilPrefix, audioFile, nccId, smilId);
		this.progress((double)count/total + SMIL_DONE/total);
		this.checkAbort();
		
		// Copy audio, but skip this step if the destination file exists
		
		// Copy ncc.html. Add PDTB metadata and add reference to SMIL notice clip.
		// Use the prefixed SMIL names. Update ncc metadata (ncc:totalTime, ncc:tocItems)
		this.createNcc(outputFolder, nccFile, nccId, smilId, smilPrefix, pdtbVersion, pdtbBookKey, pdtbNccFile, totalTime);
		this.progress((double)count/total + NCC_DONE/total);
		this.checkAbort();
	}
	
	/**
	 * Create the ncc.html by patching the the (soon to be) facade book ncc.html.	 * 
	 * @param outputFolder the output folder
	 * @param nccFile the ncc.html file
	 * @param nccId the ID of the notice text in the ncc.
	 * @param smilId the ID of the notice text in the first smil
	 * @param smilPrefix the smil filename prefix for the facade book
	 * @param pdtbVersion the PDTB version
	 * @param pdtbBookKey the PDTB book key
	 * @param pdtbNccFile the PDTB encrypted ncc file
	 * @param totalTime the total time of the book after the notice clip was inserted
	 * @throws CatalogExceptionNotRecoverable
	 * @throws PoolException
	 * @throws XMLStreamException
	 * @throws IOException 
	 */
	private void createNcc(Directory outputFolder, D202NccFile nccFile, String nccId, String smilId, String smilPrefix, String pdtbVersion, String pdtbBookKey, String pdtbNccFile, SmilClock totalTime) throws CatalogExceptionNotRecoverable, PoolException, XMLStreamException, IOException {
		NccPatcher nccPatcher = new NccPatcher();		
		nccPatcher.patch(nccFile, new File(outputFolder, nccFile.getName()), nccId, smilId, smilPrefix, mNoticeText, pdtbVersion, pdtbBookKey, pdtbNccFile, totalTime);
	}
	
	/**
	 * Patch the smil files of the facade book.
	 * @param fileset the fileset of the facade book
	 * @param outputFolder the output folder
	 * @param prefix the smil file prefix for the facade book
	 * @param audioFile the audio file to be inserted as the notice clip
	 * @param nccId the ID of the notice clip in the ncc.html
	 * @param smilId the ID of the notice clip in the smil
	 * @return the total time of the book
	 * @throws IOException
	 * @throws CatalogExceptionNotRecoverable
	 * @throws XMLStreamException
	 * @throws PoolException
	 */
	private SmilClock copySmil(Fileset fileset, Directory outputFolder, String prefix, AudioFile audioFile, String nccId, String smilId) throws IOException, CatalogExceptionNotRecoverable, XMLStreamException, PoolException {
		D202NccFile nccFile = (D202NccFile)(fileset.getManifestMember());
		Iterator it = nccFile.getSpineItems().iterator();
		SmilFileClockFixer smilFileClockFixer = new SmilFileClockFixer();
		long totalElapsedTime = 0;
		
		// Patch first smil
		D202SmilFile smilFile = (D202SmilFile)it.next();
		TempFile tempFile = new TempFile();
		SmilPatcher smilPatcher = new SmilPatcher();
		smilPatcher.addNoticePar(smilFile.getFile(), tempFile.getFile(), audioFile, nccId, smilId);
		File outFile = new File(outputFolder, prefix + smilFile.getName());
		totalElapsedTime = smilFileClockFixer.fix(tempFile.getFile(), outFile, totalElapsedTime);
		
		// Do the rest
		while (it.hasNext()) {
    		smilFile = (D202SmilFile)it.next();
    		outFile = new File(outputFolder, prefix + smilFile.getName());
    		totalElapsedTime += smilFileClockFixer.fix(smilFile.getFile(), outFile, totalElapsedTime);
    	}
		
		return new SmilClock(totalElapsedTime);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.daisy.util.fileset.interfaces.FilesetErrorHandler#error(org.daisy.util.fileset.exception.FilesetFileException)
	 */
	public void error(FilesetFileException ffe) throws FilesetFileException {
		if(ffe instanceof FilesetFileFatalErrorException) {
			throw ffe;
		} 
		this.sendMessage(ffe);	
	}	
	
}
