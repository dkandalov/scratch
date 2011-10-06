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
package ru.scratch;

import java.util.Arrays;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * @author Dmitry Kandalov
 */
@SuppressWarnings({"UnusedDeclaration"})
@State(
		name = "ScratchData",
		storages = {@Storage(id = "main", file = "$APP_CONFIG$/scratch.xml")}
)
public class ScratchData implements PersistentStateComponent<ScratchData> {
	private static final int SIZE = 5;
	private String scratchTexts[];

	public static ScratchData getInstance() {
		return ServiceManager.getService(ScratchData.class);
	}

	public ScratchData() {
		scratchTexts = new String[SIZE];
		for (int i = 0; i < scratchTexts.length; i++)
			scratchTexts[i] = "";

	}

	public String[] getScratchText() {
		String result[] = new String[SIZE];
		for (int i = 0; i < result.length; i++) {
			result[i] = XmlUtil.escape(scratchTexts[i]);
		}
		return result;
	}

	public void setScratchText(String[] text) {
		for (int i = 0; i < text.length; i++) {
			scratchTexts[i] = XmlUtil.unescape(text[i]);
		}
	}

	String[] getScratchTextInternal() {
		return scratchTexts;
	}

	public void setScratchText(int scratchIndex, String text) {
		scratchTexts[scratchIndex] = text;
	}

	@Override
	public ScratchData getState() {
		return this;
	}

	@Override
	public void loadState(ScratchData scratchData) {
		XmlSerializerUtil.copyBean(scratchData, this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass()) {
			return false;
		} else {
			ScratchData that = (ScratchData) o;
			return Arrays.equals(scratchTexts, that.scratchTexts);
		}
	}

	@Override
	public int hashCode() {
		return scratchTexts == null ? 0 : Arrays.hashCode(scratchTexts);
	}
}