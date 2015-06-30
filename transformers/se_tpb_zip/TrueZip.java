package se_tpb_zip;

import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import de.schlichtherle.io.File;

/**
 * Convenience methods for creating zip files using truezip.
 * 
 * https://truezip.dev.java.net/
 * 
 * @author Linus Ericson
 *
 */
public class TrueZip {

	/**
	 * Creates one or more zip files.
	 * <p>
	 * This method creates one or more zip files from the contents of <code>inputDir</code> to
	 * directory <code>outputDir</code>. The size of the files within each zip file as at most
	 * <code>maxSize</code> bytes and the files have the filenames are <code>filenamePrefix</code>
	 * concatenated with an underscore and a sequential number. The filename suffix is <em>.zip</em>. 
	 * </p>
	 * @param inputDir the input director
	 * @param outputDir the output directory
	 * @param filenamePrefix the filename prefix
	 * @param maxSize the maximum size of the files within a zip file
	 * @return a list of zip files
	 * @throws IOException
	 */
	public static List<? extends java.io.File> createZipFiles(java.io.File inputDir, java.io.File outputDir, String filenamePrefix, long maxSize) throws IOException {
				
		int count = 1;
		long currentSize = 0;
		List<File> resultList = new ArrayList<File>();
		
		RecursiveFileListIterator iter = new RecursiveFileListIterator(inputDir, new FileFilter() {
			public boolean accept(java.io.File pathname) {
				return !pathname.getAbsolutePath().contains("~");
			}
		});
		
		File zipFile = new File(outputDir, filenamePrefix + "_" + count + ".zip");
		if (zipFile.exists() && !zipFile.deleteAll()) {
			throw new IOException("Cannot delete existing zip file " + zipFile);
		}
		
		outputDir.mkdirs();
		while (iter.hasNext()) {
			java.io.File file = iter.next();
			
			if (currentSize + file.length() >= maxSize) {				
				++count;
				
				// Close zip file
				File.umount(zipFile);
				
				// Add zip file to result list
				resultList.add(zipFile);
				
				// Start new zip file
				zipFile = new File(outputDir, filenamePrefix + "_" + count + ".zip");
				if (zipFile.exists() && !zipFile.deleteAll()) {
					throw new IOException("Cannot delete existing zip file " + zipFile);
				}
				
				// Reset current size
				currentSize = 0;
			}
			// Add file to current zip file
			URI dirUri = file.getParentFile().toURI();
			URI relativeDirUri = inputDir.toURI().relativize(dirUri);
			File relativeDir = new File(zipFile, relativeDirUri.toString());
			File relativeFile = new File(relativeDir, file.getName());
			if (!relativeFile.copyFrom(new File(file))) {
				throw new IOException("Cannot add file " + file + " to zip archive " + zipFile);
			}
			
			// Increase current size
			currentSize += file.length();
		}
		
		// Close last zip file
		File.umount(zipFile);
		
		// Add last zip file to result list
		resultList.add(zipFile);
		
		return resultList;
	}
	
	/**
	 * Creates a single zip file.
	 * <p>
	 * This method creates the zip archive <code>outputFile</code> from the contents of
	 * directory <code>inputDir</code>.
	 * </p>
	 * @param inputDir the input directory
	 * @param outputFile the output file
	 * @return the zip file
	 * @throws IOException
	 */
	public static java.io.File createZipFile(java.io.File inputDir, java.io.File outputFile) throws IOException {
		boolean result = new File(inputDir).copyAllTo(new File(outputFile));
		if (!result) {
			throw new IOException("Cannot zip to" + outputFile);
		}
		return outputFile;
	}
}

