package se_tpb_watermark;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.transformer.Transformer;
import org.daisy.pipeline.exception.TransformerRunException;
import org.daisy.util.file.Directory;
import org.daisy.util.file.FileUtils;
import org.daisy.util.file.FilenameOrFileURI;
import org.daisy.util.file.RecursiveFileListIterator;
import org.xml.sax.SAXException;

import se.tpb.id3tag.Id3Tag;
import se.tpb.id3tag.Id3TagEncoder;
import se.tpb.playlist.Playlist;
import se.tpb.playlist.PlaylistItem;
import se.tpb.watermark.WatermarkedInputStream;

import com.ostermiller.util.ConcatInputStream;

/**
 * Transformer that adds a watermark to MP3 files
 * @author Linus Ericson
 *
 */
public class Watermark extends Transformer {

	public Watermark(InputListener inListener, Boolean isInteractive) {
		super(inListener, isInteractive);
	}

	@Override
	protected boolean execute(Map<String, String> parameters) throws TransformerRunException {
		String input = parameters.remove("input");
		String output = parameters.remove("output");
		String watermark = parameters.remove("watermark");
		String addID3 = parameters.remove("addID3");
		String addPlaylist = parameters.remove("addPlaylist");
		boolean id3 = "true".equals(addID3);
		boolean playlists = "true".equals(addPlaylist);
		
		try {
			Directory baseDir = new Directory(FilenameOrFileURI.toFile(input));
			Directory outputDir = new Directory(FilenameOrFileURI.toFile(output));
			FileUtils.createDirectory(outputDir);
			
			Map<String,Playlist> playlistMap = createPlaylists(baseDir, outputDir, playlists);
			
			RecursiveFileListIterator iter = new RecursiveFileListIterator(baseDir, new FileFilter() {
				public boolean accept(File file) {					
					return !file.getAbsolutePath().contains("~");
				}
			});
			long timeBefore = System.currentTimeMillis();
			int i = 0;
			long timeWatermark = 0;
			long timeId3 = 0;
			long timeCopy = 0;
			while (iter.hasNext()) {
				File inFile = iter.next();
				URI relative = baseDir.toURI().relativize(inFile.toURI());
				File outFile = new File(outputDir.toURI().resolve(relative));
				
				String relativePath = relative.toString();
				String subdir = "";
				if (relativePath.lastIndexOf("/") != -1) {
					subdir = relativePath.substring(0, relativePath.lastIndexOf("/"));
				}
				Playlist playlist = playlistMap.get(subdir);
				
				FileUtils.createDirectory(outFile.getParentFile());
				if (inFile.getName().toLowerCase().endsWith(".mp3")) {
					++i;
					Id3Tag id3tag = null;
					if (id3) {
						id3tag = getId3Tag(playlist, inFile);
					}
					if (i % 3 == 0 && watermark != null && !"".equals(watermark)) {
						// Add watermark and (possibly, may be null) id3 tag
						InputStream is = new WatermarkedInputStream(inFile, watermark, id3tag);
						long then = System.currentTimeMillis();
						FileUtils.writeInputStreamToFile(is, outFile);
						long now = System.currentTimeMillis();
						timeWatermark += now - then;
					} else if (id3) {
						// Add id3 tag only
						byte[] tag = Id3TagEncoder.encodeId3v2Tag(id3tag);
						InputStream is = new ConcatInputStream(new ByteArrayInputStream(tag), new FileInputStream(inFile));
						long then = System.currentTimeMillis();
						FileUtils.writeInputStreamToFile(is, outFile);
						long now = System.currentTimeMillis();
						timeId3 += now - then;
					} else {
						// No watermarking and no id3. Just copy the file
						long then = System.currentTimeMillis();
						FileUtils.copyFile(inFile, outFile);
						long now = System.currentTimeMillis();
						timeCopy += now - then;
					}
				} else {
					// Not an mp3 file. Just copy it
					long then = System.currentTimeMillis();
					FileUtils.copyFile(inFile, outFile);
					long now = System.currentTimeMillis();
					timeCopy += now - then;
				}
				this.checkAbort();				
			}
			long diff = System.currentTimeMillis() - timeBefore;
			System.err.println("Watermarking/copying took " + ((double)diff/1000) + " seconds");
			System.err.println("  Watermark: " + timeWatermark);
			System.err.println("  ID3:       " + timeId3);
			System.err.println("  Copy:      " + timeCopy);
		} catch (IOException e) {
			throw new TransformerRunException(e.getMessage(), e);
		}
		
		return true;
	}
	
	/**
	 * Generates a playlist map.
	 * The key in the map is the subdirectory (relative to the base dir for the entire book) of the ncc file,
	 * and the value is the corresponding playlist. For single book volumes, there is only one entry in the map
	 * and the key is typically the empty string. For multi book volumes there will be one entry for each sub book.
	 * @param baseDir
	 * @param baseOutputDir
	 * @param addPlaylists
	 * @return
	 * @throws IOException
	 */
	private Map<String,Playlist> createPlaylists(Directory baseDir, Directory baseOutputDir, boolean addPlaylists) throws IOException {
		Map<String,Playlist> result = new HashMap<String,Playlist>();
		
		// Find all ncc.html files recursively
		RecursiveFileListIterator iter = new RecursiveFileListIterator(baseDir, new FileFilter() {
			public boolean accept(File file) {
				return !file.getAbsolutePath().contains("~") && (file.isDirectory() || "ncc.html".equals(file.getName()));
			}
		});
		
		while (iter.hasNext()) {
			File nccFile = iter.next();
			URI relative = baseDir.toURI().relativize(nccFile.toURI());
			String relativePath = relative.toString();
			
			// Get output directory
			File outputDir = new File(baseOutputDir.toURI().resolve(relative)).getParentFile();
			
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
					
					// Write playlist to file
					if (addPlaylists) {
						createPlaylist(playlist, new File(outputDir, "playlist.m3u"));
					}
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
	
	/**
	 * Gets the ID3 tag (if possible)
	 * @param playlist
	 * @param file
	 * @return
	 */
	private Id3Tag getId3Tag(Playlist playlist, File file) {
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
	 * Creates a playlist
	 * @param playlist
	 * @param file
	 * @throws IOException
	 */
	private void createPlaylist(Playlist playlist, File file) throws IOException {
		FileUtils.createDirectory(file.getParentFile());
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "ISO-8859-1"));
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
	}
	
}

