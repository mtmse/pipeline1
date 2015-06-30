package se_tpb_mkisofs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.event.MessageEvent;
import org.daisy.pipeline.core.transformer.Transformer;
import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.execution.Command;
import org.daisy.util.execution.ExecutionException;
import org.daisy.util.file.Directory;
import org.daisy.util.file.FileUtils;
import org.daisy.util.file.FilenameOrFileURI;
import org.daisy.util.file.NullOutputStream;
import org.daisy.util.fileset.FilesetFile;
import org.daisy.util.fileset.exception.FilesetFatalException;
import org.daisy.util.fileset.impl.FilesetFileFactory;
import org.daisy.util.fileset.util.ManifestFinder;
import org.daisy.util.text.LineFilter;

/**
 * Creates one or several ISO images files using mkisofs. 
 * @author Linus Ericson
 */
public class CreateISO extends Transformer implements LineFilter, FileFilter {

	private final static Pattern progressPattern = Pattern.compile("^\\s*(\\d+\\.\\d+)%\\sdone.*$");
	
	private String mkisofsPath = null;
	
	public CreateISO(InputListener inListener, Boolean isInteractive) {
		super(inListener, isInteractive);
	}

	@Override
	protected boolean execute(Map parameters) throws TransformerRunException {
		String input = (String)parameters.remove("input");
		String output = (String)parameters.remove("output");
		String subdirs = (String)parameters.remove("subdirs");
		String manifestDirs = (String)parameters.remove("manifestDirs");
		String volId = (String)parameters.remove("volId");
		String producer = (String)parameters.remove("preparer");
		String skipThis = (String)parameters.remove("skipThis");
		
		mkisofsPath = System.getProperty("pipeline.mkisofs.path");
        File test = new File(mkisofsPath);
		if(!test.exists() || !test.canRead()) {
			String message = i18n("MKISOFS_INSTALL_PROBLEM", mkisofsPath);
			this.sendMessage(message, MessageEvent.Type.ERROR,MessageEvent.Cause.SYSTEM);
			throw new TransformerRunException(message);
		}
		
		if (Boolean.parseBoolean(skipThis)) {
			sendMessage("Skipping ISO creation", MessageEvent.Type.INFO);
			return true;
		} else {
			sendMessage("Creating ISO using input " + input, MessageEvent.Type.INFO);
			sendMessage("Creating ISO using output " + output, MessageEvent.Type.INFO);
		}
		
		boolean useSubdirs = Boolean.parseBoolean(subdirs);
		boolean useManifestDirs = Boolean.parseBoolean(manifestDirs);
		if ("".equals(volId)) {
			volId = null;
		}
		
		boolean success = false;
		
		try {
			if (useManifestDirs) {
				File basedir = FilenameOrFileURI.toFile(input);
				File outDir = FilenameOrFileURI.toFile(output);
				
				if (!basedir.isDirectory()) {
					// FIXME better exception
					throw new IOException("input must be directory when using subdirs");
				}
				
				Directory inputBaseDir = new Directory(basedir);
        		Collection inputFiles = ManifestFinder.getManifests(true, inputBaseDir);
        		List<File> dirs = new ArrayList<File>();        		
        		for (Iterator i = inputFiles.iterator(); i.hasNext();) {
    				FilesetFile manifest = FilesetFileFactory.newInstance().newFilesetFile((File)i.next());    				
    				File dir = manifest.getFile().getParentFile();
    				this.maybeAddDir(dirs, dir);
    			}
        		for (File dir : dirs) {
        			// FIXME if multiple subdirs have the same name and contain a manifest, this could be incorrect
					File iso = new File(outDir, dir.getName() + ".iso");
					
					// Create ISO
					int result = ceateIso(dir, iso, volId, producer);
					if (result != 0) {
						throw new TransformerRunException("mkisofs failed");
					}
				}
				success = true;
			} else if (useSubdirs) {
				File basedir = FilenameOrFileURI.toFile(input);
				File outDir = FilenameOrFileURI.toFile(output);
				
				if (!basedir.isDirectory()) {
					// FIXME better exception
					throw new IOException("input must be directory when using subdirs");
				}
				
				// Make sure output dir exists
				FileUtils.createDirectory(outDir);
				
				File[] dirs = basedir.listFiles(this);				
				for (File dir : dirs) {
					File iso = new File(outDir, dir.getName() + ".iso");
					
					// Create ISO
					int result = ceateIso(dir, iso, volId, producer);
					if (result != 0) {
						throw new TransformerRunException("mkisofs failed");
					}
				}
				success = true;
			} else {
				File dir = FilenameOrFileURI.toFile(input);
				File iso = FilenameOrFileURI.toFile(output);
				
				// Make sure output dir exists
				FileUtils.createDirectory(iso.getParentFile());
				
				// Create ISO
				int result = ceateIso(dir, iso, volId, producer);
				if (result != 0) {
					throw new TransformerRunException("mkisofs failed");
				}
				success = true;
			}
		} catch (ExecutionException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (IOException e) {
			throw new TransformerRunException(e.getMessage(), e);
		} catch (TransformerRunException e) {
			throw e;
		} catch (FilesetFatalException e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
		
		return success;
	}
	
	private void maybeAddDir(List<File> dirs, File dir) {
		ListIterator<File> it = dirs.listIterator();
		while (it.hasNext()) {
			File next = it.next();
			if (isAncestor(dir, next)) {
				// FIXME
				it.remove();
			} else if (isAncestor(next, dir)) {
				return;
			}
		}
		dirs.add(dir);
	}
	
	private boolean isAncestor(File ancestor, File child) {
		return child.getAbsolutePath().startsWith(ancestor.getAbsolutePath());
	}
	
	private int ceateIso(File dir, File outputFile, String volId, String producer) throws ExecutionException {
		sendMessage("Running mkisofs dir=" + dir + ", outFile=" + outputFile, MessageEvent.Type.INFO);
		
		try {
			// Make sure output directory exists
			FileUtils.createDirectory(outputFile.getParentFile());
		} catch (IOException e) {
			throw new ExecutionException(e.getMessage(), e);
		}
		
		if (volId == null) {
			volId = dir.getName();
		}
		List<String> args = new ArrayList<String>();
		args.add(mkisofsPath);
		args.add("-J");
		args.add("-r");
		args.add("-f");
		args.add("-gui");
		args.add("-hide-rr-moved");
		args.add("-iso-level");	args.add("2");
		args.add("-no-bak");		
		args.add("-o");	args.add(outputFile.getAbsolutePath());
		args.add("-pad");
		args.add("-p");	args.add(producer);
		args.add("-V");	args.add(volId);
		args.add(dir.getAbsolutePath());
		
		OutputStream stdout = new NullOutputStream();
		OutputStream stderr = new NullOutputStream();		
		return Command.execute(args.toArray(new String[args.size()]), null, stdout, stderr, 0, 1000, this);		
	}
	
	public synchronized String filterLine(String line) {
		Matcher matcher = progressPattern.matcher(line);
		if (matcher.matches()) {
			double d = Double.valueOf(matcher.group(1));
			d = d / 100;
			progress(d);
			System.err.println("iso progress: " + d);
		} else {
			//System.err.println("Filter: " + line);
		}
		return line;
	}

	public boolean accept(File pathname) {
		return pathname.isDirectory();
	}
	
}
