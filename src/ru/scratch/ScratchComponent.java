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

import static com.intellij.openapi.util.io.FileUtilRt.toSystemIndependentName;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Kandalov
 */
public class ScratchComponent implements ApplicationComponent {
	private static final Logger LOG = Logger.getInstance(ScratchComponent.class);

	@Override
	public void initComponent() {
		ScratchData instance = ScratchData.getInstance();
		if (nameToPathList().isEmpty()) {
			String text[] = instance.getScratchTextInternal();
			int i = 0;
			for (String s : text) {
				try {
					if (!StringUtils.isBlank(s)) {
						FileUtil.writeToFile(new File(pluginsRootPath() + "/" + "scratch" + ++i + ".txt"), s);
					}
				} catch (IOException e) {
					LOG.error(e);
				}
			}
		}
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
		String path = nameToPathMap().get(ScratchData.getInstance().getDefaultFileName());
		if (path == null) {
			List<Map.Entry<String, String>> entries = nameToPathList();
			if (entries.isEmpty()) {
				return null;
			}
			path = entries.get(0).getValue();
		}
		return Util.getVirtualFile(path);
	}

	public static String pluginsRootPath() {
		return toSystemIndependentName(PathManager.getPluginsPath() + "/scratch");
	}

	public static Map<String, String> nameToPathMap() {
		File[] files = new File(pluginsRootPath()).listFiles(new FileFilter() {
			@SuppressWarnings("SimplifiableIfStatement")
			@Override
			public boolean accept(File file) {
				return !file.isDirectory();
			}
		});
		if (files == null)
			return new HashMap<String, String>();

		HashMap<String, String> result = new HashMap<String, String>();
		for (File file : files) {
			result.put(file.getName(), file.getAbsolutePath());
		}
		return result;
	}

	public static List<Map.Entry<String, String>> nameToPathList() {
		List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>();
		list.addAll(ScratchComponent.nameToPathMap().entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
			@Override
			public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		return list;
	}
}
