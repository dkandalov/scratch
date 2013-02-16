package scratch.filesystem;

import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.Function;
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

/**
 * User: dima
 * Date: 10/02/2013
 */
public class FileSystem {
	private static final Logger LOG = Logger.getInstance(FileSystem.class);
	private static final String SCRATCH_FOLDER = "";

	private final VirtualFileManager fileManager = VirtualFileManager.getInstance();
	private final Condition<VirtualFile> canBeScratch = new Condition<VirtualFile>() {
		@Override public boolean value(VirtualFile it) {
			return it != null && it.exists() && !it.isDirectory() && !isHidden(it.getName());
		}
	};
	private final String scratchesFolderPath;


	public FileSystem(String scratchesFolderPath) {
		if (scratchesFolderPath == null || scratchesFolderPath.isEmpty()) {
			this.scratchesFolderPath = PathManager.getPluginsPath() + "/scratches/";
		} else {
			this.scratchesFolderPath = scratchesFolderPath + "/"; // add trailing "/" in case it's not specified in config
		}
	}

	public List<String> listScratchFiles() {
		VirtualFile virtualFile = virtualFileBy(SCRATCH_FOLDER);
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
					LOG.warn(e);
					return false;
				}
			}
		});
	}

	public boolean createEmptyFile(String fileName) {
		return createFile(fileName, "");
	}

	public boolean createFile(final String fileName, final String text) {
		return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
			@Override public Boolean compute() {
				try {
					ensureExists(new File(scratchesFolderPath));

					VirtualFile scratchesFolder = virtualFileBy(SCRATCH_FOLDER);
					if (scratchesFolder == null) return false;

					VirtualFile scratchFile = scratchesFolder.createChildData(FileSystem.this, fileName);
					scratchFile.setBinaryContent(text.getBytes(Charset.forName("UTF8")));

					return true;
				} catch (IOException e) {
					LOG.warn(e);
					return false;
				}
			}
		});
	}

	public boolean removeFile(final String fileName) {
		return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
			@Override public Boolean compute() {
				VirtualFile virtualFile = virtualFileBy(fileName);
				if (virtualFile == null) return false;

				try {
					virtualFile.delete(FileSystem.this);
					return true;
				} catch (IOException e) {
					LOG.warn(e);
					return false;
				}
			}
		});
	}

	@Nullable public VirtualFile virtualFileBy(String fileName) {
		return fileManager.refreshAndFindFileByUrl("file://" + scratchesFolderPath + fileName);
	}

	public boolean isScratch(final VirtualFile virtualFile) {
		VirtualFile scratchFolder = virtualFileBy(SCRATCH_FOLDER);
		return scratchFolder != null && ContainerUtil.exists(scratchFolder.getChildren(), new Condition<VirtualFile>() {
			@Override public boolean value(VirtualFile it) {
				return it.equals(virtualFile);
			}
		});
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
