package ru.scratch;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static ru.scratch.XmlUtil.escape;
import static ru.scratch.XmlUtil.unescape;

/**
 * User: dima
 * Date: 29/08/2012
 */
public class XmlUtilTest {
	@Test
	public void escapeAndUnEscapeText() throws Exception {
		assertThat(escape("123qwe"), equalTo("123qwe"));
		assertThat(escape("<a>fake tag</a>"), equalTo("&lt;a&gt;fake tag&lt;/a&gt;"));
		assertThat(unescape("&lt;a&gt;fake tag&lt;/a&gt;"), equalTo("<a>fake tag</a>"));

		assertThat(escape("фыва"), equalTo("&#1092;&#1099;&#1074;&#1072;"));
		assertThat(unescape("&#1092;&#1099;&#1074;&#1072;"), equalTo("фыва"));
	}
}
