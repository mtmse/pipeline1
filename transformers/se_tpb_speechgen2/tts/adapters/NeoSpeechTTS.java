package se_tpb_speechgen2.tts.adapters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.sound.sampled.UnsupportedAudioFileException;

import se_tpb_speechgen2.audio.AudioFiles;
import se_tpb_speechgen2.tts.TTSException;
import se_tpb_speechgen2.tts.util.TTSUtils;
import voiceware.libttsapi;


/**
 * @author Martin Blomberg
 *
 */
public class NeoSpeechTTS extends AbstractTTSAdapter {

	private libttsapi mTTS;
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private static int slaveNr = 0;
	public static boolean DEBUG = true;
	private String threadName;
	
	public NeoSpeechTTS(TTSUtils ttsUtils, Map<String, String> params) {
		super(ttsUtils, params);
		mTTS = new libttsapi();
		threadName = "NeoSpeechTTS-" + NeoSpeechTTS.getNextId();
	}
	
	/* (non-Javadoc)
	 * @see se_tpb_speechgen2.tts.adapters.AbstractTTSAdapter#canSpeak(java.lang.String)
	 */
	@Override
	protected boolean canSpeak(String line) {
		return TTSUtils.containsLettersOrDigits(line);
	}

	/* (non-Javadoc)
	 * @see se_tpb_speechgen2.tts.adapters.AbstractTTSAdapter#read(java.lang.String, java.io.File)
	 */
	@Override
	public void read(String line, File destination) throws TTSException {			
		int nReturn = 0;
		
		line = "<speak version=\"1.0\" xml:lang=\"en\">" + line + "</speak>";
	
		// FIXME externalize these three parameters:
		String host = "127.0.0.1";
		int port = 7000;
		int voice = libttsapi.TTS_PAUL_DB;
		
		/*
		 * Make request
		 * read reply
		 * make call to store as file
		 * make call to determine audio duration
		 */
		String errorMessageTail = "Attempted to speak: " + line;
		try {
			DEBUG("Writes the line >>" + line + "<<");
			nReturn = mTTS.ttsRequestBufferEx(
				host, // voice text server hostname
				port, // voice text server port
				line, // the text to process
				voice, // speaker id, different voices, ie. 
				libttsapi.FORMAT_WAV,  	// audio format
				libttsapi.TEXT_SSML, 	// input text format
				/* the following four int values are set to default,
				 * but will be ignored since the text format is SSML. */
				100,	// volume		
				100,	// speed
				100,	// pitch
				0,		// dict num
				libttsapi.TRUE, // first frame wanted
				libttsapi.TRUE); // one frame delivery
			DEBUG("Finished writing text.");
		} catch (IOException e) {
			// VoiceText/NeoSpeech: TTS_RESULT_ERROR
			nReturn = -9;
			e.printStackTrace();
		}
		
		if (nReturn == libttsapi.TTS_RESULT_SUCCESS) {
			try {
				DEBUG("begin writing audio data");
				writeToFile(destination, mTTS.szVoiceData, mTTS.nVoiceLength);
				DEBUG("finished writing audio data");
			} catch (IOException e) {
				e.printStackTrace();
				String msg = e.getMessage() + ", " + errorMessageTail;
				throw new TTSException(msg, e);
			}
		} else {
			String msg = "Interaction with NeoSpeech TTS failed, error code: " 
				+ nReturn + ".\n" + errorMessageTail;
			throw new TTSException(msg);
		}
		
		long audioFileDuration = 0;
		try {
			audioFileDuration = AudioFiles.getAudioFileDuration(destination);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			String msg = e.getMessage() + "\n" + errorMessageTail;
			throw new TTSException(msg, e);
		} catch (IOException e) {
			e.printStackTrace();
			String msg = e.getMessage() + ", " + errorMessageTail;
			throw new TTSException(msg, e);
		}
		
		if (audioFileDuration == 0) {
			String msg = "Error processing file " + destination + 
			", bailing out!\n" + errorMessageTail;
			throw new TTSException(msg);
		}

		DEBUG("audio duration : " + audioFileDuration);
		//return audioFileDuration;
	}
	
	/**
	 * Writes the content of src [0, len] to the file denoted by destination.
	 * @param destination the file in which to store the contents of src.
	 * @param src the array holding audio data.
	 * @param len the number of bytes in src to write (starting from 0).
	 * @throws IOException
	 */
	private static void writeToFile(File destination, byte[] src, int len) throws IOException {
		FileOutputStream out;
		out = new FileOutputStream(destination);
		out.write(src, 0, len);
		out.close();
	}
	
	/* (non-Javadoc)
	 * @see se_tpb_speechgen2.tts.adapters.AbstractTTSAdapter#close()
	 */
	@Override 
	public void close() {
		System.err.println("Closing NeoSpeech, done!");
	}
	
	private static synchronized int getNextId() {
		return slaveNr++;
	}
	
	private void DEBUG(String msg) {
		if (DEBUG) {
			String base = "DEBUG [" + formatter.format(new Date()) + ":" + threadName + "] " + getClass().getName() + ": ";
			System.err.println(base + msg);
		}
	}
}

