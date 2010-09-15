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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * @author Dmitry Kandalov
 */
@State(
		name = "ScratchData",
		storages = {@Storage(id = "main", file = "$APP_CONFIG$/scratch.xml")}
)
public class ScratchData implements PersistentStateComponent<ScratchData> {
	private static final int SIZE = 5;
	private String[] text = new String[SIZE];

	public static ScratchData getInstance() {
		return ServiceManager.getService(ScratchData.class);
	}

	public ScratchData() {
		for (int i = 0; i < text.length; i++) {
			text[i] = "";
		}
	}

	public String[] getScratchText() {
		return text;
	}

	public void setScratchText(String[] text) {
		this.text = text;
	}

	public void setScratchText(int scratchIndex, String text) {
		this.text[scratchIndex] = text;
	}

	@Override
	public ScratchData getState() {
		return this;
	}

	@Override
	public void loadState(ScratchData scratchData) {
		XmlSerializerUtil.copyBean(scratchData, this);
	}
} 