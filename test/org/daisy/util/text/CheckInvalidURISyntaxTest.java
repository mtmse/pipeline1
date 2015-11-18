package org.daisy.util.text;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class CheckInvalidURISyntaxTest {

    private String invalidURISample;

    public CheckInvalidURISyntaxTest(String urlSample) {
        this.invalidURISample = urlSample;
    }

    @Test
    public void testSyntax() throws Exception {
        try {
        	URIUtils.createURI(invalidURISample);
        	fail(new StringBuilder().append("Should raise syntax error for \"").append(invalidURISample).append('\"').toString());
        } catch (URISyntaxException e) {
            		assertTrue(true);
        }
    }

    @Parameterized.Parameters // Needs newer JUnit version, >= 4.11 (name = "{index}: URI [{0}]={0}")
    public static Collection<String[]> invalidURIs() {
        List<String[]> invalidURIs = new LinkedList<String[]>();
        invalidURIs.add(new String[]{"http://ui@:12"});
        invalidURIs.add(new String[]{"%"});
        invalidURIs.add(new String[]{"A%Z"});
        invalidURIs.add(new String[]{"%ZZ"});
        invalidURIs.add(new String[]{"%AZ"});
        invalidURIs.add(new String[]{"[2010:836B:4179::836B:4179]"});
        invalidURIs.add(new String[]{"http://foo.org:80Path/More"});
        invalidURIs.add(new String[]{"ht tp://foo.org:80/More"});
        //invalidUris.add("::");
        invalidURIs.add(new String[]{"http://[xyz]/"});

        return invalidURIs;
    }
}
