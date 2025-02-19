package se_tpb_speechgen2.external.win.sapi5  ;

import com4j.*;

public enum DISPID_SpeechVoiceStatus implements ComEnum {
    DISPID_SVSCurrentStreamNumber(1),
    DISPID_SVSLastStreamNumberQueued(2),
    DISPID_SVSLastResult(3),
    DISPID_SVSRunningState(4),
    DISPID_SVSInputWordPosition(5),
    DISPID_SVSInputWordLength(6),
    DISPID_SVSInputSentencePosition(7),
    DISPID_SVSInputSentenceLength(8),
    DISPID_SVSLastBookmark(9),
    DISPID_SVSLastBookmarkId(10),
    DISPID_SVSPhonemeId(11),
    DISPID_SVSVisemeId(12),
    ;

    private final int value;
    DISPID_SpeechVoiceStatus(int value) { this.value=value; }
    public int comEnumValue() { return value; }
}
