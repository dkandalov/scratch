package scratch;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * @author DKandalov
 */
public class XmlUtil {
	private static final int ESCAPED_CODE_MAX_SIZE = 10;

	public static String escape(String text) {
		String escapedXml = StringEscapeUtils.escapeXml(text);
		return escapeControlCodes(escapedXml);
	}

	public static String unescape(String text) {
		String unescapedControlCodes = unescapeControlCodes(text);
		return StringEscapeUtils.unescapeXml(unescapedControlCodes);
	}

	private static String unescapeControlCodes(String text) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '&' && i + 1 < text.length() && text.charAt(i + 1) == '#') {
				int semiColonIndex = semiColonIndex(text, i);
				if (semiColonIndex != -1) {
					int value = Integer.valueOf(text.substring(i + 2, semiColonIndex));
					builder.append((char) value);
					i = semiColonIndex;
				}
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
	}

	private static int semiColonIndex(String text, int fromPos) {
		int semiColonIndex = -1;
		int j = 1;
		do {
			if (j > ESCAPED_CODE_MAX_SIZE)
				break;
			if (text.charAt(fromPos + j) == ';') {
				semiColonIndex = fromPos + j;
				break;
			}
			j++;
		} while (true);
		return semiColonIndex;
	}

	private static String escapeControlCodes(String text) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (isNotAllowedControlCode(c))
				builder.append("&#").append(c).append(';');
			else
				builder.append(c);
		}
		return builder.toString();
	}

	private static boolean isNotAllowedControlCode(char c) {
		return c < ' ' && c != '\t' && c != '\n' && c != '\r';
	}
}
