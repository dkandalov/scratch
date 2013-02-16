/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scratch.ide;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.Arrays;

@State(name = "ScratchData", storages = {@Storage(id = "main", file = "$APP_CONFIG$/scratch.xml")})
public class ScratchOldData implements PersistentStateComponent<ScratchOldData> {
	private static final int SIZE = 5;

	private final String scratchTexts[];

	public static ScratchOldData getInstance() {
		return ServiceManager.getService(ScratchOldData.class);
	}

	public ScratchOldData() {
		scratchTexts = new String[SIZE];
		Arrays.fill(scratchTexts, "");
	}

	// used by intellij serializer through reflection
	@SuppressWarnings({"UnusedDeclaration"})
	public String[] getScratchText() {
		return scratchTexts;
	}

	// used by intellij serializer through reflection
	@SuppressWarnings({"UnusedDeclaration"})
	public void setScratchText(String[] text) {
		System.arraycopy(text, 0, scratchTexts, 0, text.length);
	}

	public String[] getScratchTextInternal() {
		String result[] = new String[SIZE];
		for (int i = 0; i < result.length; i++) {
			result[i] = XmlUtil.unescape(scratchTexts[i]);
		}
		return result;
	}

	@Override
	public ScratchOldData getState() {
		return this;
	}

	@Override
	public void loadState(ScratchOldData scratchOldData) {
		XmlSerializerUtil.copyBean(scratchOldData, this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass()) {
			return false;
		} else {
			ScratchOldData that = (ScratchOldData) o;
			return Arrays.equals(scratchTexts, that.scratchTexts);
		}
	}

	@Override
	public int hashCode() {
		return scratchTexts == null ? 0 : Arrays.hashCode(scratchTexts);
	}

	public static class XmlUtil {
		private static final int ESCAPED_CODE_MAX_SIZE = 10;

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
	}
}
