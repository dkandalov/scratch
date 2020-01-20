package scratch.ide

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ide.CopyPasteManager.ContentChangedListener
import com.intellij.openapi.ide.CopyPasteManager.getInstance
import scratch.MrScratchManager
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
    fun startListening() {
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
        copyPasteManager.addContentChangedListener(listener, application)
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