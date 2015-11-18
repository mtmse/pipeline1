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
public class CheckCreateURIWithIllegalCharcters {

    private String urlSample;

    public CheckCreateURIWithIllegalCharcters(String urlSample) {
        this.urlSample = urlSample;
    }

    @Test
    public void testSyntax() throws Exception {
        URI expected = new URI(urlSample);

        URI actual = URIUtils.createURI(urlSample);

        assertThat(actual, is(expected));
    }

    @Parameterized.Parameters //(name = "{index}: URI [{0}]={0}")
    public static Collection<String[]> validURIs() {
        List<String[]> validURIs = new LinkedList<String[]>();
        validURIs.add(new String[]{"http://example.org/aaa/bbb#ccc"});

        validURIs.add(new String[]{"http://example.org/aaa/bbb#ccc"});
        validURIs.add(new String[]{"mailto:local@domain.org"});
        validURIs.add(new String[]{"mailto:local@domain.org#frag"});
        validURIs.add(new String[]{"HTTP://EXAMPLE.ORG/AAA/BBB#CCC"});
        validURIs.add(new String[]{"//example.org/aaa/bbb#ccc"});
        validURIs.add(new String[]{"/aaa/bbb#ccc"});
        validURIs.add(new String[]{"bbb#ccc"});
        validURIs.add(new String[]{"#ccc"});
        validURIs.add(new String[]{"#"});
        validURIs.add(new String[]{"/"});
        validURIs.add(new String[]{"http://example.org/aaa/bbb#ccc"});
        validURIs.add(new String[]{"http://example.org/aaa/bbb#ccc"});
        validURIs.add(new String[]{"/"});
        validURIs.add(new String[]{"aaa/bbb"});
        validURIs.add(new String[]{"http://example.org:80/aaa/bbb#ccc"});
        validURIs.add(new String[]{"http://example.org:/aaa/bbb#ccc"});
        validURIs.add(new String[]{"http://example.org./aaa/bbb#ccc"});
        validURIs.add(new String[]{"http://example.123./aaa/bbb#ccc"});
        validURIs.add(new String[]{"http://example.org"});
        validURIs.add(new String[]{"http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]:80/index.html"});
        validURIs.add(new String[]{"http://[1080:0:0:0:8:800:200C:417A]/index.html"});
        validURIs.add(new String[]{"http://[3ffe:2a00:100:7031::1]"});
        validURIs.add(new String[]{"http://[1080::8:800:200C:417A]/foo"});
        validURIs.add(new String[]{"http://[::192.9.5.5]/ipng"});
        validURIs.add(new String[]{"http://[::FFFF:129.144.52.38]:80/index.html"});
        validURIs.add(new String[]{"http://[2010:836B:4179::836B:4179]"});
        validURIs.add(new String[]{"//[2010:836B:4179::836B:4179]"});
        validURIs.add(new String[]{"./aaa"});
        validURIs.add(new String[]{"../aaa"});
        validURIs.add(new String[]{"g:h"});
        validURIs.add(new String[]{"g"});
        validURIs.add(new String[]{"./g"});
        validURIs.add(new String[]{"g/"});
        validURIs.add(new String[]{"/g"});
        validURIs.add(new String[]{"//g"});
        validURIs.add(new String[]{"?y"});
        validURIs.add(new String[]{"g?y"});
        validURIs.add(new String[]{"#s"});
        validURIs.add(new String[]{"g#s"});
        validURIs.add(new String[]{"g?y#s"});
        validURIs.add(new String[]{";x"});
        validURIs.add(new String[]{"g;x"});
        validURIs.add(new String[]{"g;x?y#s"});
        validURIs.add(new String[]{"."});
        validURIs.add(new String[]{"./"});
        validURIs.add(new String[]{".."});
        validURIs.add(new String[]{"../"});
        validURIs.add(new String[]{"../g"});
        validURIs.add(new String[]{"../.."});
        validURIs.add(new String[]{"../../"});
        validURIs.add(new String[]{"../../g"});
        validURIs.add(new String[]{"../../../g"});
        validURIs.add(new String[]{"../../../../g"});
        validURIs.add(new String[]{"/./g"});
        validURIs.add(new String[]{"/../g"});
        validURIs.add(new String[]{"g."});
        validURIs.add(new String[]{".g"});
        validURIs.add(new String[]{"g.."});
        validURIs.add(new String[]{"..g"});
        validURIs.add(new String[]{"./../g"});
        validURIs.add(new String[]{"./g/."});
        validURIs.add(new String[]{"g/./h"});
        validURIs.add(new String[]{"g/../h"});
        validURIs.add(new String[]{"g;x=1/./y"});
        validURIs.add(new String[]{"g;x=1/../y"});
        validURIs.add(new String[]{"g?y/./x"});
        validURIs.add(new String[]{"g?y/../x"});
        validURIs.add(new String[]{"g#s/./x"});
        validURIs.add(new String[]{"g#s/../x"});
        validURIs.add(new String[]{""});
        validURIs.add(new String[]{"A'C"});
        validURIs.add(new String[]{"A$C"});
        validURIs.add(new String[]{"A@C"});
        validURIs.add(new String[]{"http://example/Andr&#567"});
        validURIs.add(new String[]{"file:///C:/DEV/Haskell/lib/HXmlToolbox-3.01/examples/"});
        validURIs.add(new String[]{"http://46229EFFE16A9BD60B9F1BE88B2DB047ADDED785/demo.mp3"});
        validURIs.add(new String[]{"http://example++/"});

        return validURIs;
    }
}
