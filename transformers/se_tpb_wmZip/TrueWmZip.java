package se_tpb_wmZip;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

import javax.xml.parsers.ParserConfigurationException;

import org.daisy.util.file.Directory;
import org.daisy.util.file.FileUtils;
import org.daisy.util.file.RecursiveFileListIterator;
import org.xml.sax.SAXException;

import se.tpb.id3tag.Id3Tag;
import se.tpb.id3tag.Id3TagEncoder;
import se.tpb.playlist.Playlist;
import se.tpb.playlist.PlaylistItem;
import se.tpb.watermark.WatermarkedInputStream;

import com.ostermiller.util.ConcatInputStream;

import de.schlichtherle.io.ArchiveDetector;
import de.schlichtherle.io.DefaultArchiveDetector;
import de.schlichtherle.io.File;
import de.schlichtherle.io.archive.zip.Zip32Driver;

/**
 * Convenience methods for creating zip files using truezip.
 * 
 * https://truezip.dev.java.net/
 * 
 * @author Linus Ericson
 *
 */
public class TrueWmZip {

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
	public static List<? extends java.io.File> createZipFiles(java.io.File inputDir, java.io.File outputDir, String filenamePrefix, long maxSize, boolean id3, String watermark, boolean addPlaylists) throws IOException {

		// Use best speed for compression
		Zip32Driver zd = new Zip32Driver(Deflater.BEST_SPEED); 
		File.setDefaultArchiveDetector(new DefaultArchiveDetector(ArchiveDetector.ALL, "zip", zd));
		
		int count = 1;
		long currentSize = 0;
		List<File> resultList = new ArrayList<File>();
		
		Directory baseDir = new Directory(inputDir);
		Map<String,Playlist> playlistMap = createPlaylists(baseDir);		
		//System.err.println(playlistMap.size() + " playlists found");
		
		RecursiveFileListIterator iter = new RecursiveFileListIterator(inputDir, new FileFilter() {
			public boolean accept(java.io.File pathname) {
				return !pathname.getAbsolutePath().contains("~");
			}
		});
		
		File zipFile = new File(outputDir, filenamePrefix + "_" + count + ".zip");
		if (zipFile.exists() && !zipFile.deleteAll()) {
			throw new IOException("Cannot delete existing zip file " + zipFile);
		}
		
		int i = 0;
		
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
			
			// Get current playlist
			URI relative = baseDir.toURI().relativize(file.toURI());
			String relativePath = relative.toString();
			String subdir = "";
			if (relativePath.lastIndexOf("/") != -1) {
				subdir = relativePath.substring(0, relativePath.lastIndexOf("/"));
			}
			Playlist playlist = playlistMap.get(subdir);
			//System.err.println("playlist: " + playlist);
			
			// Add file to current zip file
			URI dirUri = file.getParentFile().toURI();
			URI relativeDirUri = inputDir.toURI().relativize(dirUri);
			// FIXME this is a very ugly way to solve the %20 in subdir problem
			File relativeDir = new File(zipFile, relativeDirUri.toString().replaceAll("%20", " "));
			File relativeFile = new File(relativeDir, file.getName());
			if (file.getName().toLowerCase().endsWith(".mp3")) {
				++i;
				Id3Tag id3tag = null;
				if (id3) {
					id3tag = getId3Tag(playlist, file);
				}
				if (i % 3 == 0 && watermark != null && !"".equals(watermark)) {
					// Add watermark and (possibly, may be null) id3 tag
					InputStream is = new WatermarkedInputStream(file, watermark, id3tag);
					if (!relativeFile.copyFrom(is)) {
						throw new IOException("Cannot add watermarked stream " + file + " to zip archive " + zipFile);
					}
				} else if (id3) {
					// Add id3 tag only
					byte[] tag = Id3TagEncoder.encodeId3v2Tag(id3tag);
					InputStream is = new ConcatInputStream(new ByteArrayInputStream(tag), new FileInputStream(file));
					if (!relativeFile.copyFrom(is)) {
						throw new IOException("Cannot add id3-tagged stream " + file + " to zip archive " + zipFile);
					}
				} else {
					// No watermarking and no id3. Just copy the file
					if (!relativeFile.copyFrom(new File(file))) {
						throw new IOException("Cannot add file " + file + " to zip archive " + zipFile);
					}
				}
			} else {
				if (file.getName().toLowerCase().equals("ncc.html") && playlist != null) {
					// We add the playlist when we find the ncc
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					Writer writer = null;
					try {
						writer = new BufferedWriter(new OutputStreamWriter(baos, "ISO-8859-1"));
						for (String filename : playlist.getItems().keySet()) {
							writer.write(filename + "\r\n");
						}
					} catch (UnsupportedEncodingException e) {
						// Not supposed to happen...
					} finally {
						if (writer != null) {
							writer.close();
						}
					}					
					File playlistFile = new File(relativeDir, "playlist.m3u");
					if (!playlistFile.copyFrom(new ByteArrayInputStream(baos.toByteArray()))) {
						throw new IOException("Cannot add playlist file (using ncc " + file + ") to zip archive " + zipFile);
					}
					// Increase disk usage
					currentSize += baos.size();
					
				}
				// Not an MP3, just copy
				if (!relativeFile.copyFrom(new File(file))) {
					throw new IOException("Cannot add file " + file + " to zip archive " + zipFile);
				}
			}
			
			// Increase current size
			currentSize += file.length();
		}
		
		// Close last zip file
		File.umount(zipFile);
		
		// If only one zip file was produced, rename it
		if (count == 1) {
			java.io.File newFile = new java.io.File(zipFile.getParentFile(), filenamePrefix + ".zip");
			// Make sure java.io.File is used to avoid the slow de.schlichtherle.io.File
			// renameTo() function (which seems to do copy+delete instead of move)
			FileUtils.moveFile(new java.io.File(zipFile.getAbsolutePath()), newFile);
			zipFile = new File(newFile);
		}
		
		// Add last zip file to result list
		resultList.add(zipFile);
		
		return resultList;
	}
	
	/**
	 * Gets the ID3 tag (if possible)
	 * @param playlist
	 * @param file
	 * @return
	 */
	private static Id3Tag getId3Tag(Playlist playlist, java.io.File file) {
		if (playlist != null) {
			PlaylistItem pli = playlist.getItems().get(file.getName());
			if (pli != null) {
				// PlaylistItem found. Create tag.
				String album = playlist.getTitle();			
				String artist = playlist.getAuthors().size() > 0 ? playlist.getAuthors().get(0) : null;
				String title = pli.getHeading();
				int trackNumber = pli.getTrackNumber();
				
				Id3Tag tag = new Id3Tag(title, artist, album, null, null, trackNumber);
				return tag;
			}
		}
		// Creating default ID3 tag
		return new Id3Tag(file.getName(), null, null, null, null, -1);
	}
	
	/**
	 * Generates a playlist map.
	 * The key in the map is the subdirectory (relative to the base dir for the entire book) of the ncc file,
	 * and the value is the corresponding playlist. For single book volumes, there is only one entry in the map
	 * and the key is typically the empty string. For multi book volumes there will be one entry for each sub book.
	 * @param baseDir
	 * @return
	 * @throws IOException
	 */
	private static Map<String,Playlist> createPlaylists(Directory baseDir) throws IOException {
		Map<String,Playlist> result = new HashMap<String,Playlist>();
		
		// Find all ncc.html files recursively
		RecursiveFileListIterator iter = new RecursiveFileListIterator(baseDir, new FileFilter() {
			public boolean accept(java.io.File file) {
				return !file.getAbsolutePath().contains("~") && (file.isDirectory() || "ncc.html".equals(file.getName()));
			}
		});
		
		while (iter.hasNext()) {
			java.io.File nccFile = iter.next();
			URI relative = baseDir.toURI().relativize(nccFile.toURI());
			String relativePath = relative.toString();
						
			// Get subdir (relative to input base dir)
			String subdir = "";
			if (relativePath.lastIndexOf("/") != -1) {
				subdir = relativePath.substring(0, relativePath.lastIndexOf("/"));
			}
			
			if (nccFile.canRead()) {
				try {
					// Generate playlist info
					Playlist playlist = Playlist.generate(nccFile);					
					result.put(subdir, playlist);					
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
		
}

