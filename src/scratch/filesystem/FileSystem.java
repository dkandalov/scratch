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

package scratch.filesystem;

import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;
import scratch.Answer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.findAll;
import static com.intellij.util.containers.ContainerUtil.map;


public class FileSystem {
	private static final Logger log = Logger.getInstance(FileSystem.class);

	/**
	 * Use UTF-8 to be compatible with old version of plugin.
	 */
	private static final Charset charset = Charset.forName("UTF8");
	private static final String scratchFolder = "";

	private final VirtualFileManager fileManager = VirtualFileManager.getInstance();
	private final Condition<VirtualFile> canBeScratch = it -> it != null && it.exists() && !it.isDirectory() && !isHidden(it.getName());
	private final String scratchesFolderPath;


	public FileSystem(String scratchesFolderPath) {
		if (scratchesFolderPath == null || scratchesFolderPath.isEmpty()) {
			this.scratchesFolderPath = PathManager.getPluginsPath() + "/scratches/";
		} else {
			this.scratchesFolderPath = scratchesFolderPath + "/"; // add trailing "/" in case it's not specified in config
		}
	}

	public List<String> listScratchFiles() {
		VirtualFile virtualFile = virtualFileBy(scratchFolder);
		if (virtualFile == null || !virtualFile.exists()) {
			return Collections.emptyList();
		}
		return map(findAll(virtualFile.getChildren(), canBeScratch), VirtualFile::getName);
	}

	public boolean scratchFileExists(String fileName) {
		VirtualFile virtualFile = virtualFileBy(fileName);
		return canBeScratch.value(virtualFile);
	}

	public Answer isValidScratchName(String fileName) {
		boolean hasPathChars = fileName.contains("/") || fileName.contains("\\");
		boolean hasWildcards = fileName.contains("*") || fileName.contains("?");
		if (hasPathChars || hasWildcards || isHidden(fileName) || !VirtualFile.isValidName(fileName)) {
			return Answer.no("Not a valid file name");
		} else if (new File(scratchesFolderPath + fileName).exists()) {
			return Answer.no("There is existing file with this name");
		} else {
			return Answer.yes();
		}
	}

	public boolean renameFile(String oldFileName, final String newFileName) {
		final VirtualFile virtualFile = virtualFileBy(oldFileName);
		if (virtualFile == null) return false;

		return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
			@Override public Boolean compute() {
				try {
					virtualFile.rename(this, newFileName);
					return true;
				} catch (IOException e) {
					log.warn(e);
					return false;
				}
			}
		});
	}

	public boolean createEmptyFile(String fileName) {
		return createFile(fileName, "");
	}

	public boolean createFile(final String fileName, final String text) {
		return ApplicationManager.getApplication().runWriteAction((Computable<Boolean>) () -> {
			try {
				ensureExists(new File(scratchesFolderPath));

				VirtualFile scratchesFolder = virtualFileBy(scratchFolder);
				if (scratchesFolder == null) return false;

				VirtualFile scratchFile = scratchesFolder.createChildData(FileSystem.this, fileName);
				scratchFile.setBinaryContent(text.getBytes(charset));

				return true;
			} catch (IOException e) {
				log.warn(e);
				return false;
			}
		});
	}

	public boolean removeFile(final String fileName) {
		return ApplicationManager.getApplication().runWriteAction((Computable<Boolean>) () -> {
			VirtualFile virtualFile = virtualFileBy(fileName);
			if (virtualFile == null) return false;

			try {
				virtualFile.delete(FileSystem.this);
				return true;
			} catch (IOException e) {
				log.warn(e);
				return false;
			}
		});
	}

	@Nullable public VirtualFile virtualFileBy(String fileName) {
		return fileManager.refreshAndFindFileByUrl("file://" + scratchesFolderPath + fileName);
	}

	public boolean isScratch(final VirtualFile virtualFile) {
		VirtualFile scratchFolder = virtualFileBy(FileSystem.scratchFolder);
		return scratchFolder != null && ContainerUtil.exists(scratchFolder.getChildren(), it -> it.equals(virtualFile));
	}

	private static boolean isHidden(String fileName) {
		return fileName.startsWith(".");
	}

	private static void ensureExists(File dir) throws IOException {
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException(CommonBundle.message("exception.directory.can.not.create", dir.getPath()));
		}
	}
}
