package se_tpb_speechgen2.external.win.sapi5  ;

import com4j.*;

public enum DISPID_SpeechPhraseElement implements ComEnum {
    DISPID_SPEAudioTimeOffset(1),
    DISPID_SPEAudioSizeTime(2),
    DISPID_SPEAudioStreamOffset(3),
    DISPID_SPEAudioSizeBytes(4),
    DISPID_SPERetainedStreamOffset(5),
    DISPID_SPERetainedSizeBytes(6),
    DISPID_SPEDisplayText(7),
    DISPID_SPELexicalForm(8),
    DISPID_SPEPronunciation(9),
    DISPID_SPEDisplayAttributes(10),
    DISPID_SPERequiredConfidence(11),
    DISPID_SPEActualConfidence(12),
    DISPID_SPEEngineConfidence(13),
    ;

    private final int value;
    DISPID_SpeechPhraseElement(int value) { this.value=value; }
    public int comEnumValue() { return value; }
}
