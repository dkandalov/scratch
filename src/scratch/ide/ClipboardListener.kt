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

package scratch.ide

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ide.CopyPasteManager.ContentChangedListener
import com.intellij.openapi.ide.CopyPasteManager.getInstance
import scratch.MrScratchManager
import scratch.ide.Util.execute
import scratch.ide.Util.whenDisposed
import java.awt.datatransfer.DataFlavor.stringFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

/**
 * Note that it's possible to turn on clipboard listener and forget about it.
 * So it will keep appending clipboard content to default scratch forever.
 * To avoid the problem remind user on IDE startup that clipboard listener is on.
 */
class ClipboardListener(
    private val mrScratchManager: MrScratchManager,
    private val copyPasteManager: CopyPasteManager = getInstance(),
    private val commandProcessor: CommandProcessor = CommandProcessor.getInstance(),
    private val application: Application = ApplicationManager.getApplication()
) {
    fun startListening(parentDisposable: Disposable) {
        val listener = ContentChangedListener { oldTransferable: Transferable?, newTransferable: Transferable? ->
            if (mrScratchManager.shouldListenToClipboard()) {
                try {
                    pasteIntoDefaultScratch(newTransferable, oldTransferable)
                } catch (e: UnsupportedFlavorException) {
                    log.info(e)
                } catch (e: IOException) {
                    log.info(e)
                }
            }
        }
        copyPasteManager.addContentChangedListener(listener)
        parentDisposable.whenDisposed {
            copyPasteManager.removeContentChangedListener(listener)
        }
    }

    private fun pasteIntoDefaultScratch(newTransferable: Transferable?, oldTransferable: Transferable?) {
        val oldClipboard = oldTransferable?.getTransferData(stringFlavor)?.toString()
        val clipboard = newTransferable?.getTransferData(stringFlavor)?.toString()

        if (clipboard != null && oldClipboard != clipboard) {
            // Invoke action later so that modification of document is not tracked by IDE as undoable "Copy" action
            // See https://github.com/dkandalov/scratch/issues/30
            application.invokeLater {
                commandProcessor.execute {
                    application.runWriteAction {
                        mrScratchManager.clipboardListenerWantsToPasteTextToScratch(clipboard)
                    }
                }
            }
        }
    }

    companion object {
        private val log = Logger.getInstance(ClipboardListener::class.java)
    }
}