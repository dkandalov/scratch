package scratch.ide

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.FileEditorManagerListener.FILE_EDITOR_MANAGER
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import scratch.MrScratchManager

class OpenEditorTracker(
    private val mrScratchManager: MrScratchManager,
    private val fileSystem: FileSystem,
    private val application: Application = ApplicationManager.getApplication()
) {
    fun startTracking() {
        val fileEditorListener = object: FileEditorManagerListener {
            override fun selectionChanged(event: FileEditorManagerEvent) {
                val virtualFile = event.newFile ?: return
                if (fileSystem.isScratch(virtualFile)) {
                    mrScratchManager.userOpenedScratch(virtualFile.name)
                }
            }
        }
        application.messageBus.connect().subscribe(ProjectManager.TOPIC, object: ProjectManagerListener {
            override fun projectOpened(project: Project) {
                project.messageBus.connect(project).subscribe(FILE_EDITOR_MANAGER, fileEditorListener)
            }
        })
    }
}