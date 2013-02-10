package scratch.filesystem;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.Nullable;
import scratch.ScratchInfo;

import java.util.List;

import static com.intellij.openapi.util.io.FileUtil.toSystemIndependentName;

/**
* User: dima
* Date: 10/02/2013
*/
public class FileSystem {
	private static final String ROOT_PATH = toSystemIndependentName(PathManager.getPluginsPath() + "/scratch/");

	public boolean createFile(String fileName, String text) {
		// TODO implement
		return false;
	}

	public List<String> listOfScratchFiles() {
		// TODO implement
		return null;
	}

	public boolean fileExists(String fileName) {
		// TODO implement
		return false;
	}

	public boolean renameFile(String oldFileName, String newFileName) {
		// TODO implement
		return false;
	}

	@Nullable public VirtualFile findVirtualFileFor(ScratchInfo scratchInfo) {
		VirtualFileManager fileManager = VirtualFileManager.getInstance();
		return fileManager.refreshAndFindFileByUrl("file://" + ROOT_PATH + scratchInfo.asFileName());
	}
}
