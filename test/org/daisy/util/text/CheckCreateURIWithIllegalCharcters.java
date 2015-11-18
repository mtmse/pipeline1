package org.daisy.util.text;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class CheckCreateURIWithIllegalCharcters {

	private String URILegalCharacters;
	private String URIIllegalCharacters;

	public CheckCreateURIWithIllegalCharcters(String URILegalCharacters, String URIIllegalCharacters) {
		this.URILegalCharacters = URILegalCharacters;
		this.URIIllegalCharacters = URIIllegalCharacters;
	}

	@Test
	public void testCreateURI() throws Exception {
		URI expected = new URI("");
		URI actual = new URI("");
		;
		try {
			expected = new URI(URILegalCharacters);

			actual = URIUtils.createURI(URIIllegalCharacters);

		} catch (URISyntaxException e) {
			fail("Syntax exception with expected URI: " + expected + "  " + "Actual: " + actual + "\n" + e.getMessage()
					+ "\n" + e.getStackTrace());
		}
		assertThat(actual, is(expected));
	}

	@Parameterized.Parameters // (name = "{index}: URI [{0}]={0}")
	public static Collection<String[]> pairOfURILegalandIllegal() {

		List<String[]> pairOfURILegalandIllegal = new LinkedList<String[]>();

		// Various illegal characters
		pairOfURILegalandIllegal.add(new String[] { "A%20C", "A C" });
		pairOfURILegalandIllegal.add(new String[] { "A%22C", "A\"C" });
		pairOfURILegalandIllegal.add(new String[] { "A%60C", "A`C" });
		pairOfURILegalandIllegal.add(new String[] { "A%3CC", "A<C" });
		pairOfURILegalandIllegal.add(new String[] { "A%3EC", "A>C" });
		pairOfURILegalandIllegal.add(new String[] { "A%5EC", "A^C" });
		pairOfURILegalandIllegal.add(new String[] { "A%5CC", "A\\C" });
		pairOfURILegalandIllegal.add(new String[] { "A%7BC", "A{C" });
		pairOfURILegalandIllegal.add(new String[] { "A%7CC", "A|C" });
		pairOfURILegalandIllegal.add(new String[] { "A%7DC", "A}C" });
		pairOfURILegalandIllegal.add(new String[] { "A%5BC", "A[C" });
		pairOfURILegalandIllegal.add(new String[] { "A%5DC", "A]C" });
		pairOfURILegalandIllegal.add(new String[] { "A日本C", "A日本C" });
		pairOfURILegalandIllegal.add(new String[] { "A日本C", "A%E6%97%A5%E6%9C%ACC" });
		// In user info
		pairOfURILegalandIllegal
				.add(new String[] { "http://us%20er@host:80/path?query#frag", "http://us er@host:80/path?query#frag" });
		pairOfURILegalandIllegal.add(
				new String[] { "http://us%20er@host:80/path?query#frag", "http://us%20er@host:80/path?query#frag" });
		// In host
		pairOfURILegalandIllegal
				.add(new String[] { "http://user@ho%20st:80/path?query#frag", "http://user@ho st:80/path?query#frag" });
		pairOfURILegalandIllegal.add(
				new String[] { "http://user@ho%20st:80/path?query#frag", "http://user@ho%20st:80/path?query#frag" });
		// In path
		pairOfURILegalandIllegal
				.add(new String[] { "http://user@host:80/pa%20th?query#frag", "http://user@host:80/pa th?query#frag" });
		pairOfURILegalandIllegal.add(
				new String[] { "http://user@host:80/pa%20th?query#frag", "http://user@host:80/pa%20th?query#frag" });
		// In query
		pairOfURILegalandIllegal
				.add(new String[] { "http://user@host:80/path?que%20ry#frag", "http://user@host:80/path?que ry#frag" });
		pairOfURILegalandIllegal.add(
				new String[] { "http://user@host:80/path?que%20ry#frag", "http://user@host:80/path?que%20ry#frag" });
		// In fragement
		pairOfURILegalandIllegal
				.add(new String[] { "http://user@host:80/path?query#fr%20ag", "http://user@host:80/path?query#fr ag" });
		pairOfURILegalandIllegal.add(
				new String[] { "http://user@host:80/path?query#fr%20ag", "http://user@host:80/path?query#fr%20ag" });
		// Decode slash in path
		pairOfURILegalandIllegal.add(new String[] { "file:///c:/dir/file.tmp", "file:///c:/dir%2Ffile.tmp" });
		// in first segment of a relative
		pairOfURILegalandIllegal.add(new String[] { "rel/path", "rel%2Fpath" });
		// in first char of a relative
		pairOfURILegalandIllegal.add(new String[] { "/relpath", "%2Frelpath" });
		pairOfURILegalandIllegal.add(new String[] { "file:///c:/dir/file.tmp", "file:///c:/dir%2ffile.tmp" });
		 // in first segment of a relative
		pairOfURILegalandIllegal.add(new String[] { "rel/path", "rel%2fpath" });
		 // in first char of a relative
		pairOfURILegalandIllegal.add(new String[] { "/relpath", "%2frelpath" });
		// Keep colon encoded in relative path, decode otherwise
		// in an authorized place
		pairOfURILegalandIllegal.add(new String[] { "file:///c:/dir/file.tmp", "file:///c%3A/dir/file.tmp" });
		// in an authorized place (hidden by percent encoded slash)
		pairOfURILegalandIllegal.add(new String[] { "/:", "%2f%3a" });
		// in first segment of a relative
		pairOfURILegalandIllegal.add(new String[] { "rel%3Apath", "rel%3Apath" });
		// many times in first segment of a relative
		pairOfURILegalandIllegal.add(new String[] { "rel%3Apath%3Apath", "rel%3Apath%3Apath" });
		// encoded percent char
		pairOfURILegalandIllegal.add(new String[] { "%253A", "%253A" });

		return pairOfURILegalandIllegal;
	}
}
