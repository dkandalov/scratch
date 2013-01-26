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

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.openapi.util.io.FileUtil.*;

/**
 * @author Dmitry Kandalov
 */
public class ScratchComponent implements ApplicationComponent {
	private static final Logger LOG = Logger.getInstance(ScratchComponent.class);

	@Override
	public void initComponent() {
		ScratchData instance = ScratchData.getInstance();
		if (instance.isFirstRun()) {
			String text[] = instance.getScratchTextInternal();
			int i = 0;
			for (String s : text) {
				try {
					if (!StringUtils.isBlank(s)) {
						createFile("scratch" + ++i + ".txt", s);
					}
				} catch (IOException e) {
					LOG.error(e);
				}
			}
			instance.setFirstRun(false);
		}
	}

	public static void openInEditor(Project project, File scratchFile) {
		VirtualFile virtualFile = Util.getVirtualFile(scratchFile.getAbsolutePath());
		if (virtualFile != null) {
			new OpenFileDescriptor(project, virtualFile).navigate(true);
			ScratchData.getInstance().setLastOpenedFileName(scratchFile.getName());
		}
	}

	public static void createFile(String fileName, String content) throws IOException {
		FileUtil.writeToFile(new File(pluginsRootPath(), fileName), content);
		ScratchData.getInstance().addCreatedFile(fileName);
	}

	public static File createFile(String fileName) {
		File file = new File(pluginsRootPath(), fileName);
		FileUtil.createIfDoesntExist(file);
		ScratchData.getInstance().addCreatedFile(fileName);
		return file;
	}

	@Override
	public void disposeComponent() {
	}

	@NotNull
	@Override
	public String getComponentName() {
		return ScratchComponent.class.getSimpleName();
	}

	@Nullable
	public static VirtualFile getDefaultScratch() {
		File file = nameToFileMap().get(ScratchData.getInstance().getLastOpenedFileName());
		if (file == null) {
			List<File> entries = filesAsList();
			if (entries.isEmpty()) {
				return null;
			}
			file = entries.get(0);
		}
		return Util.getVirtualFile(file);
	}

	public static String pluginsRootPath() {
		return toSystemIndependentName(PathManager.getPluginsPath() + "/scratch");
	}

	public static Map<String, File> nameToFileMap() {
		File[] files = getFiles();
		if (files == null)
			return Collections.emptyMap();

		Map<String, File> result = new HashMap<String, File>();
		for (File file : files) {
			result.put(file.getName(), file);
		}
		return result;
	}

	/**
	 * ordered by creation + files added outside IJ ordered by name,
	 * when the file no longer exists, it is removed from ScretchData
	 */
	public static List<File> filesAsList() {
		List<String> createdFileNames = ScratchData.getInstance().getCreatedFileNames();
		List<File> result = new ArrayList<File>(createdFileNames.size());
		Map<String, File> map = nameToFileMap();

		addFilesOrderedByCreation(createdFileNames, result, map);
		//created outside IntelliJ
		addNewFiles(result, map);
		return result;
	}

	private static void addFilesOrderedByCreation(List<String> createdFileNames, List<File> result, Map<String, File> map) {
		for (String createdFileName : new ArrayList<String>(createdFileNames)) {
			File file = map.get(createdFileName);
			if (file != null) {
				result.add(file);
			} else {
				ScratchData.getInstance().removeCreatedFileName(createdFileName);
			}
			map.remove(createdFileName);
		}
	}

	private static void addNewFiles(List<File> result, Map<String, File> map) {
		if (!map.isEmpty()) {
			List<String> newFileNames = new ArrayList<String>();
			newFileNames.addAll(map.keySet());
			Collections.sort(newFileNames);
			for (String newFileName : newFileNames) {
				result.add(map.get(newFileName));
				ScratchData.getInstance().addCreatedFile(newFileName);
			}
		}
	}

	protected static File[] getFiles() {
		return new File(pluginsRootPath()).listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return !file.isDirectory();
			}
		});
	}
}
