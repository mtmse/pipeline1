package org.daisy.util.wincom.sapi5  ;

import com4j.*;

/**
 * ISpPhrase Interface
 */
@IID("{1A5C0354-B621-4B5A-8791-D306ED379E53}")
public interface ISpPhrase extends Com4jObject {
        @VTID(6)
        void discard(
            int dwValueTypes);

    }