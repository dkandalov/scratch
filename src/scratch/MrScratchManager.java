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

package scratch;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import scratch.filesystem.FileSystem;
import scratch.ide.Ide;
import scratch.ide.ScratchLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.*;
import static scratch.ScratchConfig.DefaultScratchMeaning;


public class MrScratchManager {
	private final Ide ide;
	private final ScratchLog log;
	private final FileSystem fileSystem;
	private ScratchConfig config;

	public MrScratchManager(Ide ide, FileSystem fileSystem, ScratchConfig config, ScratchLog log) {
		this.ide = ide;
		this.fileSystem = fileSystem;
		this.config = config;
		this.log = log;
	}

	public void migrate(List<String> scratchTexts) {
		if (!fileSystem.listScratchFiles().isEmpty()) {
			log.willNotMigrateBecauseTargetFolderIsNotEmpty();
			return;
		}
		boolean allEmpty = !ContainerUtil.exists(scratchTexts, new Condition<String>() {
			@Override public boolean value(String s) {
				return !s.isEmpty();
			}
		});
		if (allEmpty) {
			List<Scratch> scratches = Arrays.asList(
					Scratch.createFrom("&scratch.txt"),
					Scratch.createFrom("scratch&2.txt"),
					Scratch.createFrom("scratch&3.xml"),
					Scratch.createFrom("scratch&4.xml")
			);
			for (Scratch scratch : scratches) {
				fileSystem.createEmptyFile(scratch.asFileName());
			}
			updateConfig(config.with(scratches).needsMigration(false));
			return;
		}

		List<Integer> indexes = new ArrayList<Integer>();
		List<Scratch> scratches = new ArrayList<Scratch>();

		for (int i = 1; i <= scratchTexts.size(); i++) {
			String scratchName = (i == 1 ? "&scratch" : "scratch&" + i);
			Scratch scratch = Scratch.createFrom(scratchName + ".txt");

			boolean wasCreated = fileSystem.createFile(scratch.asFileName(), scratchTexts.get(i - 1));
			if (wasCreated) {
				scratches.add(scratch);
			} else {
				indexes.add(i);
			}
		}

		if (indexes.isEmpty()) {
			log.migratedScratchesToFiles();
		} else {
			log.failedToMigrateScratchesToFiles(indexes);
		}
		updateConfig(config.with(scratches).needsMigration(false));
	}


	public void userWantsToSeeScratchesList(UserDataHolder userDataHolder) {
		syncScratchesWithFileSystem();
		ide.displayScratchesListPopup(config.scratches, userDataHolder);
	}

	public void userWantsToOpenScratch(Scratch scratch, UserDataHolder userDataHolder) {
		if (fileSystem.scratchFileExists(scratch.asFileName())) {
			ide.openScratch(scratch, userDataHolder);
		} else {
			log.failedToOpen(scratch);
		}
	}

	public void userWantsToOpenDefaultScratch(UserDataHolder userDataHolder) {
		syncScratchesWithFileSystem();

		if (config.scratches.isEmpty()) {
			userWantsToEnterNewScratchName(userDataHolder);
			return;
		}

		ide.openScratch(getDefaultScratch(), userDataHolder);
	}

	public void userWantsToChangeMeaningOfDefaultScratch(DefaultScratchMeaning value) {
		updateConfig(config.withDefaultScratchMeaning(value));
	}

	public DefaultScratchMeaning defaultScratchMeaning() {
		return config.defaultScratchMeaning;
	}

	public void userOpenedScratch(String scratchFileName) {
		Scratch scratch = findByFileName(scratchFileName);
		if (scratch != null) {
			updateConfig(config.withLastOpenedScratch(scratch));
		}
	}


	public void userWantsToEditScratchName(String scratchFileName) {
		Scratch scratch = findByFileName(scratchFileName);
		if (scratch != null) {
			userWantsToEditScratchName(scratch);
		}
	}

	public void userWantsToEditScratchName(Scratch scratch) {
		ide.showRenameDialogFor(scratch);
	}

	public Answer checkIfUserCanRename(final Scratch scratch, String fullNameWithMnemonics) {
		if (fullNameWithMnemonics.isEmpty()) return Answer.no("Name cannot be empty");

		final Scratch renamedScratch = Scratch.createFrom(fullNameWithMnemonics);
		if (scratch.asFileName().equals(renamedScratch.asFileName())) return Answer.yes();

		boolean haveScratchWithSameName = exists(config.scratches, new Condition<Scratch>() {
			@Override public boolean value(Scratch it) {
				return !it.equals(scratch)
						&& it.name.equals(renamedScratch.name)
						&& it.extension.equals(renamedScratch.extension);
			}
		});
		if (haveScratchWithSameName) return Answer.no("There is already a scratch with this name");

		return fileSystem.isValidScratchName(renamedScratch.asFileName());
	}

	public void userWantsToRename(Scratch scratch, String fullNameWithMnemonics) {
		if (scratch.fullNameWithMnemonics.equals(fullNameWithMnemonics)) return;

		Scratch renamedScratch = Scratch.createFrom(fullNameWithMnemonics);
		boolean wasRenamed = fileSystem.renameFile(scratch.asFileName(), renamedScratch.asFileName());
		if (wasRenamed) {
			updateConfig(config.replace(scratch, renamedScratch));
		} else {
			log.failedToRename(scratch);
		}
	}


	public void userMovedScratch(final Scratch scratch, int shift) {
		updateConfig(config.move(scratch, shift));
	}


	public void userWantsToListenToClipboard(boolean value) {
		updateConfig(config.listenToClipboard(value));
		log.listeningToClipboard(value);
	}

	public void clipboardListenerWantsToAddTextToScratch(String clipboardText) {
		if (config.scratches.isEmpty()) {
			log.failedToOpenDefaultScratch();
		} else {
			Scratch scratch = getDefaultScratch();
			if (fileSystem.scratchFileExists(scratch.asFileName())) {
				ide.addTextTo(scratch, clipboardText, config.clipboardAppendType);
			} else {
				log.failedToOpenDefaultScratch();
			}
		}
	}

	public boolean shouldListenToClipboard() {
		return config.listenToClipboard;
	}

	public void userWantsToEnterNewScratchName(UserDataHolder userDataHolder) {
		String defaultName = "scratch";
		String defaultExtension = "txt";
		if (isUniqueScratchName(defaultName)) {
			ide.openNewScratchDialog(defaultName + "." + defaultExtension, userDataHolder);
			return;
		}
		for (int i = 1; i < 100; i++) {
			if (isUniqueScratchName(defaultName + i)) {
				ide.openNewScratchDialog(defaultName + i + "." + defaultExtension, userDataHolder);
				return;
			}
		}
	}

	public Answer checkIfUserCanCreateScratchWithName(String fullNameWithMnemonics) {
		if (fullNameWithMnemonics.isEmpty()) return Answer.no("Name cannot be empty");

		final Scratch scratch = Scratch.createFrom(fullNameWithMnemonics);

		if (!isUniqueScratchName(scratch.name, scratch.extension))
			return Answer.no("There is already a scratch with this name");

		return fileSystem.isValidScratchName(scratch.asFileName());
	}

	public void userWantsToAddNewScratch(String fullNameWithMnemonics, UserDataHolder userDataHolder) {
		Scratch scratch = Scratch.createFrom(fullNameWithMnemonics);
		boolean wasCreated = fileSystem.createEmptyFile(scratch.asFileName());
		if (wasCreated) {
			updateConfig(config.add(scratch));
			ide.openScratch(scratch, userDataHolder);
		} else {
			log.failedToCreate(scratch);
		}
	}


	public void userAttemptedToDeleteScratch(String scratchFileName) {
		Scratch scratch = findByFileName(scratchFileName);
		if (scratch != null)
			userAttemptedToDeleteScratch(scratch);
	}

	public void userAttemptedToDeleteScratch(Scratch scratch) {
		ide.showDeleteDialogFor(scratch);
	}

	public void userWantsToDeleteScratch(Scratch scratch) {
		boolean wasRemoved = fileSystem.removeFile(scratch.asFileName());
		if (wasRemoved) {
			updateConfig(config.without(scratch));
		} else {
			log.failedToDelete(scratch);
		}
	}


	private void syncScratchesWithFileSystem() {
		final List<String> fileNames = fileSystem.listScratchFiles();

		final List<Scratch> oldScratches = findAll(config.scratches, new Condition<Scratch>() {
			@Override public boolean value(Scratch it) {
				return fileNames.contains(it.asFileName());
			}
		});
		Condition<String> whichAreNewFiles = new Condition<String>() {
			@Override public boolean value(final String fileName) {
				return !exists(oldScratches, new Condition<Scratch>() {
					@Override public boolean value(Scratch scratch) {
						return fileName.equals(scratch.asFileName());
					}
				});
			}
		};
		List<String> newFileNames = filter(fileNames, whichAreNewFiles);
		List<Scratch> newScratches = map(newFileNames, new Function<String, Scratch>() {
			@Override public Scratch fun(String it) {
				return Scratch.createFrom(it);
			}
		});

		List<Scratch> scratches = concat(oldScratches, newScratches);
		if (!newScratches.isEmpty() || oldScratches.size() != config.scratches.size()) {
			ScratchConfig newConfig = config.with(scratches);
			if (!scratches.contains(config.lastOpenedScratch)) {
				newConfig = newConfig.withLastOpenedScratch(null);
			}
			updateConfig(newConfig);
		}
	}

	private Scratch getDefaultScratch() {
		switch (config.defaultScratchMeaning) {
			case TOPMOST:
				return config.scratches.get(0);
			case LAST_OPENED:
				return config.lastOpenedScratch != null ? config.lastOpenedScratch : config.scratches.get(0);
			default:
				throw new IllegalStateException();
		}
	}

	private boolean isUniqueScratchName(final String name) {
		return !exists(config.scratches, new Condition<Scratch>() {
			@Override public boolean value(Scratch it) {
				return it.name.equals(name);
			}
		});
	}

	private boolean isUniqueScratchName(final String name, final String extension) {
		return !exists(config.scratches, new Condition<Scratch>() {
			@Override public boolean value(Scratch it) {
				return it.name.equals(name) && it.extension.equals(extension);
			}
		});
	}

	private void updateConfig(ScratchConfig newConfig) {
		if (config.equals(newConfig)) return;
		config = newConfig;
		ide.persistConfig(config);
	}

	private Scratch findByFileName(final String scratchFileName) {
		return ContainerUtil.find(config.scratches, new Condition<Scratch>() {
			@Override public boolean value(Scratch it) {
				return it.asFileName().equals(scratchFileName);
			}
		});
	}
}
