package se_tpb_speechgen2;

import org.daisy.pipeline.core.InputListener;
import org.daisy.pipeline.core.event.RequestEvent;
import org.daisy.pipeline.core.event.UserReplyEvent;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SpeechGen2Test {
    @Test
    public void create_SpeechGen2_instance() throws Exception {
        InputListener inputListener = getInputListener();

        SpeechGen2 speechGen2 = new SpeechGen2(inputListener, false);

        assertNotNull(speechGen2);
    }

    private InputListener getInputListener() {
        return new InputListener() {
            public UserReplyEvent getUserReply(RequestEvent event) {
                throw new RuntimeException("Not implemented");
            }
        };
    }
}
