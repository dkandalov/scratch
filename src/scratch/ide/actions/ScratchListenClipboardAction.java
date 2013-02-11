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
package scratch.ide.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.IconLoader;
import scratch.ide.ScratchComponent;
import scratch.ide.ScratchConfigPersistence;

import javax.swing.*;

public class ScratchListenClipboardAction extends ToggleAction implements DumbAware {
	private static final Icon IS_ON_ICON = AllIcons.Actions.Menu_paste;
	private static final Icon IS_OFF_ICON = IconLoader.getDisabledIcon(IS_ON_ICON);

	@Override public void setSelected(AnActionEvent event, boolean enabled) {
		ScratchComponent.instance().userWantsToListenToClipboard(enabled);
		event.getPresentation().setIcon(enabled ? IS_ON_ICON : IS_OFF_ICON);
	}

	@Override public boolean isSelected(AnActionEvent event) {
		return ScratchConfigPersistence.getInstance().isListenToClipboard();
	}

	@Override public void update(AnActionEvent event) {
		super.update(event);
		event.getPresentation().setIcon(isSelected(event) ? IS_ON_ICON : IS_OFF_ICON);
	}
}
