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

package scratch.ide;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerAdapter;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scratch.Answer;
import scratch.MrScratchManager;
import scratch.Scratch;
import scratch.ScratchConfig;
import scratch.filesystem.FileSystem;
import scratch.ide.popup.ScratchListPopup;
import scratch.ide.popup.ScratchListPopupStep;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static com.intellij.openapi.fileEditor.FileEditorManagerListener.FILE_EDITOR_MANAGER;
import static java.awt.datatransfer.DataFlavor.stringFlavor;
import static scratch.ScratchConfig.AppendType.APPEND;
import static scratch.ScratchConfig.AppendType.PREPEND;
import static scratch.ide.ScratchComponent.mrScratchManager;
import static scratch.ide.Util.*;


public class Ide {
	private final FileSystem fileSystem;
	private final ScratchLog log;

	private int scratchListSelectedIndex;


	public Ide(FileSystem fileSystem, ScratchLog log) {
		this.fileSystem = fileSystem;
		this.log = log;
	}

	public void persistConfig(ScratchConfig config) {
		ScratchConfigPersistence.getInstance().updateFrom(config);
	}

	public void displayScratchesListPopup(List<Scratch> scratches, final UserDataHolder userDataHolder) {
		ScratchListPopupStep popupStep = new ScratchListPopupStep(scratches, takeProjectFrom(userDataHolder));
		popupStep.setDefaultOptionIndex(scratchListSelectedIndex);
		ScratchListPopup popup = new ScratchListPopup(popupStep) {

			@Override protected void onNewScratch() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override public void run() {
						mrScratchManager().userWantsToEnterNewScratchName(userDataHolder);
					}
				});
			}

			@Override protected void onRenameScratch(final Scratch scratch) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override public void run() {
						mrScratchManager().userWantsToEditScratchName(scratch);
					}
				});
			}

			@Override protected void onScratchDelete(Scratch scratch) {
				mrScratchManager().userAttemptedToDeleteScratch(scratch);
			}

            @Override protected void onScratchDeleteWithoutPrompt(Scratch scratch) {
                mrScratchManager().userWantsToDeleteScratch(scratch);
            }

			@Override protected void onScratchMoved(Scratch scratch, int shift) {
				mrScratchManager().userMovedScratch(scratch, shift);
			}

			@Override public void dispose() {
				scratchListSelectedIndex = getSelectedIndex();
				super.dispose();
			}
		};
		popup.showCenteredInCurrentWindow(takeProjectFrom(userDataHolder));
	}

	public void openScratch(Scratch scratch, UserDataHolder userDataHolder) {
		Project project = takeProjectFrom(userDataHolder);

		VirtualFile file = fileSystem.virtualFileBy(scratch.asFileName());
		if (file != null) {
			new OpenFileDescriptor(project, file).navigate(true);
		} else {
			log.failedToFindVirtualFileFor(scratch);
		}
	}

	public void openNewScratchDialog(String suggestedScratchName, UserDataHolder userDataHolder) {
		String message = "Scratch name (you can use '&' for mnemonics):";
		String scratchName = Messages.showInputDialog(message, "New Scratch", NO_ICON, suggestedScratchName, new InputValidatorEx() {
			@Override public boolean checkInput(String scratchName) {
				return ScratchComponent.mrScratchManager().checkIfUserCanCreateScratchWithName(scratchName).isYes;
			}

			@Nullable @Override public String getErrorText(String scratchName) {
				Answer answer = ScratchComponent.mrScratchManager().checkIfUserCanCreateScratchWithName(scratchName);
				return answer.explanation;
			}

			@Override public boolean canClose(String inputString) {
				return true;
			}
		});
		if (scratchName == null) return;

		mrScratchManager().userWantsToAddNewScratch(scratchName, userDataHolder);
	}

	public void addTextTo(Scratch scratch, final String clipboardText, final ScratchConfig.AppendType appendType) {
		VirtualFile virtualFile = fileSystem.virtualFileBy(scratch.asFileName());
		if (virtualFile == null) {
			log.failedToFindVirtualFileFor(scratch);
			return;
		}
		final Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
		if (document == null) return;
		if (hasFocusInEditor(document)) return;

		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override public void run() {
				String text = document.getText();
				String newText;
				if (appendType == APPEND) {
					if (text.endsWith("\n")) {
						newText = text + clipboardText;
					} else {
						newText = text + "\n" + clipboardText;
					}
				} else if (appendType == PREPEND) {
					newText = clipboardText + "\n" + text;
				} else {
					throw new IllegalStateException();
				}

				document.setText(newText);
				FileDocumentManager.getInstance().saveDocument(document);
			}
		});
	}

	public void showRenameDialogFor(Scratch scratch) {
		String initialValue = scratch.fullNameWithMnemonics;
		String message = "Scratch name (you can use '&' for mnemonics):";
		String newScratchName = Messages.showInputDialog(message, "Scratch Rename", NO_ICON, initialValue, new ScratchListPopup.ScratchNameValidator(scratch));

		if (newScratchName != null) {
			mrScratchManager().userWantsToRename(scratch, newScratchName);
		}
	}

	public void showDeleteDialogFor(Scratch scratch) {
		String message = "Do you want to delete '" + scratch.asFileName() + "'?\n(This operation cannot be undone)";
		int userAnswer = Messages.showOkCancelDialog(message, "Delete Scratch", "&Delete", "&Cancel", UIUtil.getQuestionIcon());
		if (userAnswer != Messages.OK) return;

		mrScratchManager().userWantsToDeleteScratch(scratch);
	}


	/**
	 *  Note that it's possible to turn on clipboard listener and forget about it
	 *  with it appending clipboard content to default scratch forever.
	 *
	 *  Assume that notification on plugin start is good enough to remind user about clipboard listener.
	 */
	public static class ClipboardListener {
		private static final Logger LOG = Logger.getInstance(ClipboardListener.class);

		private final MrScratchManager mrScratchManager;

		public ClipboardListener(MrScratchManager mrScratchManager) {
			this.mrScratchManager = mrScratchManager;
		}

		public void startListening() {
			CopyPasteManager.getInstance().addContentChangedListener(new CopyPasteManager.ContentChangedListener() {
				@Override
				public void contentChanged(@Nullable Transferable oldTransferable, Transferable newTransferable) {
					if (!mrScratchManager.shouldListenToClipboard()) return;

					try {
						String oldClipboard = null;
						if (oldTransferable != null) {
							Object transferData = oldTransferable.getTransferData(stringFlavor);
							oldClipboard = (transferData == null ? null : transferData.toString());
						}
						String clipboard = null;
						if (newTransferable != null) {
							Object transferData = newTransferable.getTransferData(stringFlavor);
							clipboard = (transferData == null ? null : transferData.toString());
						}
						if (clipboard == null || StringUtils.equals(oldClipboard, clipboard)) return;

						mrScratchManager.clipboardListenerWantsToAddTextToScratch(clipboard);

					} catch (UnsupportedFlavorException e) {
						LOG.info(e);
					} catch (IOException e) {
						LOG.info(e);
					}
				}
			});
		}
	}

	public static class OpenEditorTracker {
		private final MrScratchManager mrScratchManager;
		private final FileSystem fileSystem;
		// use WeakHashMap "just in case" to avoid keeping project references
		private final Map<Project, MessageBusConnection> connectionsByProject = new WeakHashMap<Project, MessageBusConnection>();

		public OpenEditorTracker(MrScratchManager mrScratchManager, FileSystem fileSystem) {
			this.mrScratchManager = mrScratchManager;
			this.fileSystem = fileSystem;
		}

		public OpenEditorTracker startTracking() {
			ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerAdapter() {
				@Override public void projectOpened(final Project project) {
                    MessageBusConnection connection = project.getMessageBus().connect();
                    connection.subscribe(FILE_EDITOR_MANAGER, new FileEditorManagerAdapter() {
                        @Override
                        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                            VirtualFile virtualFile = event.getNewFile();
                            if (virtualFile == null) return;

                            if (fileSystem.isScratch(virtualFile)) {
                                mrScratchManager.userOpenedScratch(virtualFile.getName());
                            }
                        }
                    });
                    connectionsByProject.put(project, connection);
                }

				@Override public void projectClosed(Project project) {
					MessageBusConnection connection = connectionsByProject.remove(project);
					if (connection != null) connection.disconnect();
				}
			});
			return this;
		}
    }
}
