package se_tpb_speechgen2.tts.adapters;

import se_tpb_speechgen2.tts.TTSException;
import se_tpb_speechgen2.tts.util.TTSUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SpeechServerTTS extends AbstractTTSAdapter {
    public SpeechServerTTS(TTSUtils ttsUtils, Map<String, String> params) {
        super(ttsUtils, params);

    }

    @Override
    public void read(String line, File destination) throws IOException, TTSException {



        throw new NotImplementedException();
    }
}
