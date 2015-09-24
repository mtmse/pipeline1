package se_tpb_speechgen2.tts.adapters;

import org.apache.commons.io.FileUtils;
import se.mtm.speech.synthesis.SpeechClient;
import se.mtm.speech.synthesis.SynthesizedSound;
import se_tpb_speechgen2.tts.TTSException;
import se_tpb_speechgen2.tts.util.TTSUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SpeechServerTTS extends AbstractTTSAdapter {
    private final int maxRetries;
    private final SpeechClient client;

    public SpeechServerTTS(TTSUtils ttsUtils, Map<String, String> params) {
        super(ttsUtils, params);

        String host = System.getProperty("speech.server.host", "localhost");

        String stringPort = System.getProperty("speech.server.port", "9090");
        int port = Integer.parseInt(stringPort);

        String stringRetries = System.getProperty("speech.server.max.retries", "3");
        maxRetries = Integer.parseInt(stringRetries);

        client = new SpeechClient(host, port);
    }

    @Override
    public void read(String line, File destination) throws IOException, TTSException {
        SynthesizedSound sound = client.synthesise(line);

        int retries = 0;
        while (sound.isNotAccepted() && retries < maxRetries) {
            pause();
            sound = client.synthesise(line);
        }
        if (sound.isNotAccepted()) {
            throw new TTSException("line not accepted");
        }

        retries = 0;
        while (sound.isTimeout() && retries < maxRetries) {
            pause();
            sound = client.synthesise(line);
        }
        if (sound.isTimeout()) {
            throw new TTSException("timeout");
        }

        writeWavFile(sound, destination);
    }

    private void writeWavFile(SynthesizedSound sound, File soundFile) throws IOException {
        byte[] content = sound.getSound();
        FileUtils.writeByteArrayToFile(soundFile, content);
    }

    private void pause() throws TTSException {
        int sleepTimeBeforeRetry = 3000;
        try {
            Thread.sleep(sleepTimeBeforeRetry);
        } catch (InterruptedException e) {
            throw new TTSException(e.getMessage(), e);
        }
    }
}
