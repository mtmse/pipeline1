package se_tpb_speechgen2.tts.adapters;

import org.junit.Ignore;
import org.junit.Test;
import se_tpb_speechgen2.tts.TTSException;
import se_tpb_speechgen2.tts.util.TTSUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SpeechServerTTSTest {
    @Test
    public void create_speech_server_tts() {
        assertThat(true, is(true));
    }

    @Test
    @Ignore
    public void synthesize() throws IOException, TTSException {
        Map params = new HashMap();
        TTSUtils ttsUtils = new TTSUtils(params);

        SpeechServerTTS tts = new SpeechServerTTS(ttsUtils, params);

        String sentence = "Den bruna hunden springer omkring och hoppar.";
        File soundFile = new File("result.wav");

        tts.read(sentence, soundFile);
    }
}
