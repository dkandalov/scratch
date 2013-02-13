package scratch.filesystem;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.Function;
import org.jetbrains.annotations.Nullable;
import scratch.Scratch;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.intellij.openapi.util.io.FileUtil.toSystemIndependentName;
import static com.intellij.util.containers.ContainerUtil.findAll;
import static com.intellij.util.containers.ContainerUtil.map;
import static scratch.MrScratchManager.Answer;

/**
 * User: dima
 * Date: 10/02/2013
 */
public class FileSystem {
	private static final Logger LOG = Logger.getInstance(FileSystem.class);
	private static final String ROOT_PATH = toSystemIndependentName(PathManager.getPluginsPath() + "/scratches/");

	private final VirtualFileManager fileManager = VirtualFileManager.getInstance();
	private final Condition<VirtualFile> canBeScratch = new Condition<VirtualFile>() {
		@Override public boolean value(VirtualFile it) {
			return it != null && it.exists() && !it.isDirectory() && !isHidden(it.getName());
		}
	};

	public List<String> listScratchFiles() {
		VirtualFile virtualFile = fileManager.refreshAndFindFileByUrl("file://" + ROOT_PATH);
		if (virtualFile == null || !virtualFile.exists()) {
			return Collections.emptyList();
		}

		return map(findAll(virtualFile.getChildren(), canBeScratch), new Function<VirtualFile, String>() {
			@Override public String fun(VirtualFile it) {
				return it.getName();
			}
		});
	}

	public boolean scratchFileExists(String fileName) {
		VirtualFile virtualFile = fileManager.refreshAndFindFileByUrl("file://" + ROOT_PATH + fileName);
		return canBeScratch.value(virtualFile);
	}

	public Answer isValidScratchName(String fileName) {
		boolean hasPathChars = fileName.contains("/") || fileName.contains("\\");
		boolean hasWildcards = fileName.contains("*") || fileName.contains("?");
		if (hasPathChars || hasWildcards || isHidden(fileName)) {
			return Answer.no("Not a valid file name");
		} else if (new File(ROOT_PATH + fileName).exists()) {
			return Answer.no("There is existing file with this name");
		} else {
			return Answer.yes();
		}
	}

	public boolean renameFile(String oldFileName, final String newFileName) {
		final VirtualFile virtualFile = fileManager.refreshAndFindFileByUrl("file://" + ROOT_PATH + oldFileName);
		if (virtualFile == null) return false;

		return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
			@Override public Boolean compute() {
				try {
					virtualFile.rename(this, newFileName);
					return true;
				} catch (IOException e) {
					LOG.warn(e);
					return false;
				}
			}
		});
	}

	public boolean createFile(String fileName, String text) {
		// TODO create ROOT_PATH folder if it doesn't exist
		// TODO implement
		return false;
	}

	@Nullable public VirtualFile findVirtualFileFor(Scratch scratch) {
		return fileManager.findFileByUrl("file://" + ROOT_PATH + scratch.asFileName());
	}

	private static boolean isHidden(String fileName) {
		return fileName.startsWith(".");
	}
}
