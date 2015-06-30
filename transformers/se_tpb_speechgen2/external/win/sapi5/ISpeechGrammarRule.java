package se_tpb_speechgen2.external.win.sapi5  ;

import com4j.*;

/**
 * ISpeechGrammarRule Interface
 */
@IID("{AFE719CF-5DD1-44F2-999C-7A399F1CFCCC}")
public interface ISpeechGrammarRule extends Com4jObject {
    /**
     * RuleAttributes
     */
    @VTID(7)
    se_tpb_speechgen2.external.win.sapi5.SpeechRuleAttributes attributes();

    /**
     * InitialState
     */
    @VTID(8)
    se_tpb_speechgen2.external.win.sapi5.ISpeechGrammarRuleState initialState();

    /**
     * Name
     */
    @VTID(9)
    java.lang.String name();

    /**
     * Id
     */
    @VTID(10)
    int id();

    /**
     * Clear
     */
    @VTID(11)
    void clear();

    /**
     * AddResource
     */
    @VTID(12)
    void addResource(
        java.lang.String resourceName,
        java.lang.String resourceValue);

    /**
     * AddState
     */
    @VTID(13)
    se_tpb_speechgen2.external.win.sapi5.ISpeechGrammarRuleState addState();

}
