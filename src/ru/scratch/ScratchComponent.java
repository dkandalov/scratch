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

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Dmitry Kandalov
 */
public class ScratchComponent implements ApplicationComponent {
	private static final int DEFAULT_SCRATCH = 0;
	private ScratchVirtualFile[] files;

	@Override
	public void initComponent() {
		String[] text = ScratchData.getInstance().getScratchText();
		files = new ScratchVirtualFile[text.length];
		for (int i = DEFAULT_SCRATCH; i < text.length; i++) {
			files[i] = new ScratchVirtualFile(text[i], i);
		}
	}

	@Override
	public void disposeComponent() {
		files = null;
	}

	@NotNull
	@Override
	public String getComponentName() {
		return ScratchComponent.class.getSimpleName();
	}

	public VirtualFile getDefaultScratch() {
		return files[DEFAULT_SCRATCH];
	}

	public ScratchVirtualFile[] getScratchFiles() {
		return files;
	}
}
