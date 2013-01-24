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

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.Nullable;

public class Util {

	@Nullable
	public static VirtualFile getVirtualFile(String absolutePath) {
		if (!absolutePath.startsWith("file://")) {
			absolutePath = "file://" + absolutePath;
		}
		return VirtualFileManager.getInstance().refreshAndFindFileByUrl(FileUtil.toSystemIndependentName(absolutePath));
	}
}
