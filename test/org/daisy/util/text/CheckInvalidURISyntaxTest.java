package org.daisy.util.text;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class CheckInvalidURISyntaxTest {

    private String urlSample;

    public CheckInvalidURISyntaxTest(String urlSample) {
        this.urlSample = urlSample;
    }

    @Test
    public void testSyntax() throws Exception {
        URI expected = new URI(urlSample);

        URI actual = URIUtils.createURI(urlSample);

        assertThat(actual, is(expected));
    }

    @Parameterized.Parameters //(name = "{index}: URI [{0}]={0}")
    public static Collection<String[]> samples() {
        List<String[]> samples = new LinkedList<String[]>();
        samples.add(new String[]{"http://example.org/aaa/bbb#ccc"});

        samples.add(new String[]{"http://example.org/aaa/bbb#ccc"});
        samples.add(new String[]{"mailto:local@domain.org"});
        samples.add(new String[]{"mailto:local@domain.org#frag"});
        samples.add(new String[]{"HTTP://EXAMPLE.ORG/AAA/BBB#CCC"});
        samples.add(new String[]{"//example.org/aaa/bbb#ccc"});
        samples.add(new String[]{"/aaa/bbb#ccc"});
        samples.add(new String[]{"bbb#ccc"});
        samples.add(new String[]{"#ccc"});
        samples.add(new String[]{"#"});
        samples.add(new String[]{"/"});
        samples.add(new String[]{"http://example.org/aaa/bbb#ccc"});
        samples.add(new String[]{"http://example.org/aaa/bbb#ccc"});
        samples.add(new String[]{"/"});
        samples.add(new String[]{"aaa/bbb"});
        samples.add(new String[]{"http://example.org:80/aaa/bbb#ccc"});
        samples.add(new String[]{"http://example.org:/aaa/bbb#ccc"});
        samples.add(new String[]{"http://example.org./aaa/bbb#ccc"});
        samples.add(new String[]{"http://example.123./aaa/bbb#ccc"});
        samples.add(new String[]{"http://example.org"});
        samples.add(new String[]{"http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]:80/index.html"});
        samples.add(new String[]{"http://[1080:0:0:0:8:800:200C:417A]/index.html"});
        samples.add(new String[]{"http://[3ffe:2a00:100:7031::1]"});
        samples.add(new String[]{"http://[1080::8:800:200C:417A]/foo"});
        samples.add(new String[]{"http://[::192.9.5.5]/ipng"});
        samples.add(new String[]{"http://[::FFFF:129.144.52.38]:80/index.html"});
        samples.add(new String[]{"http://[2010:836B:4179::836B:4179]"});
        samples.add(new String[]{"//[2010:836B:4179::836B:4179]"});
        samples.add(new String[]{"./aaa"});
        samples.add(new String[]{"../aaa"});
        samples.add(new String[]{"g:h"});
        samples.add(new String[]{"g"});
        samples.add(new String[]{"./g"});
        samples.add(new String[]{"g/"});
        samples.add(new String[]{"/g"});
        samples.add(new String[]{"//g"});
        samples.add(new String[]{"?y"});
        samples.add(new String[]{"g?y"});
        samples.add(new String[]{"#s"});
        samples.add(new String[]{"g#s"});
        samples.add(new String[]{"g?y#s"});
        samples.add(new String[]{";x"});
        samples.add(new String[]{"g;x"});
        samples.add(new String[]{"g;x?y#s"});
        samples.add(new String[]{"."});
        samples.add(new String[]{"./"});
        samples.add(new String[]{".."});
        samples.add(new String[]{"../"});
        samples.add(new String[]{"../g"});
        samples.add(new String[]{"../.."});
        samples.add(new String[]{"../../"});
        samples.add(new String[]{"../../g"});
        samples.add(new String[]{"../../../g"});
        samples.add(new String[]{"../../../../g"});
        samples.add(new String[]{"/./g"});
        samples.add(new String[]{"/../g"});
        samples.add(new String[]{"g."});
        samples.add(new String[]{".g"});
        samples.add(new String[]{"g.."});
        samples.add(new String[]{"..g"});
        samples.add(new String[]{"./../g"});
        samples.add(new String[]{"./g/."});
        samples.add(new String[]{"g/./h"});
        samples.add(new String[]{"g/../h"});
        samples.add(new String[]{"g;x=1/./y"});
        samples.add(new String[]{"g;x=1/../y"});
        samples.add(new String[]{"g?y/./x"});
        samples.add(new String[]{"g?y/../x"});
        samples.add(new String[]{"g#s/./x"});
        samples.add(new String[]{"g#s/../x"});
        samples.add(new String[]{""});
        samples.add(new String[]{"A'C"});
        samples.add(new String[]{"A$C"});
        samples.add(new String[]{"A@C"});
        samples.add(new String[]{"http://example/Andr&#567"});
        samples.add(new String[]{"file:///C:/DEV/Haskell/lib/HXmlToolbox-3.01/examples/"});
        samples.add(new String[]{"http://46229EFFE16A9BD60B9F1BE88B2DB047ADDED785/demo.mp3"});
        samples.add(new String[]{"http://example++/"});

        return samples;
    }
}
