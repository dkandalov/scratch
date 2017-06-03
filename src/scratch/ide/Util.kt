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

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.IdeFocusManager
import javax.swing.Icon

object Util {
    val NO_ICON: Icon? = null
    private val PROJECT_KEY = Key.create<Project>("Project")

    fun holdingOnTo(project: Project?): UserDataHolder {
        val userDataHolder = UserDataHolderBase()
        userDataHolder.putUserData(PROJECT_KEY, project)
        return userDataHolder
    }

    fun takeProjectFrom(userDataHolder: UserDataHolder): Project? {
        return userDataHolder.getUserData(PROJECT_KEY)
    }

    val selectedEditor: Editor?
        get() {
            val frame = IdeFocusManager.findInstance().lastFocusedFrame ?: return null
            val project = frame.project ?: return null

            val instance = FileEditorManager.getInstance(project)
            return instance.selectedTextEditor
        }

    fun currentFileIn(project: Project?): VirtualFile? {
        if (project == null) return null
        return (FileEditorManagerEx.getInstance(project) as FileEditorManagerEx).currentFile
    }

    fun hasFocusInEditor(document: Document): Boolean {
        val selectedTextEditor = selectedEditor
        return selectedTextEditor != null && selectedTextEditor.document == document
    }
}
