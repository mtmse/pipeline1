package se_tpb_speechgen2.external.win.sapi5  ;

import com4j.*;

public enum DISPID_SpeechCustomStream implements ComEnum {
    DISPID_SCSBaseStream(100),
    ;

    private final int value;
    DISPID_SpeechCustomStream(int value) { this.value=value; }
    public int comEnumValue() { return value; }
}
