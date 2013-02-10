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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import scratch.ide.ScratchComponent;

import static scratch.ide.ScratchComponent.ACTION_EVENT_KEY;
import static scratch.ide.actions.OpenScratchAction.projectFor;

/**
 * @author Dmitry Kandalov
 */
public class OpenScratchListAction extends DumbAwareAction {
	@Override
	public void actionPerformed(AnActionEvent event) {
		UserDataHolder userDataHolder = new UserDataHolderBase();
		userDataHolder.putUserData(ACTION_EVENT_KEY, event);
		ScratchComponent.instance().userWantsToSeeScratchesList(userDataHolder);

//		ListPopupStep popupStep = new BaseListPopupStep<String>("strings", asList("aaa", "bbb", "ccc"));
//		ScratchListPopup popup = new ScratchListPopup(popupStep);
//		popup.showCenteredInCurrentWindow(project);
	}

	@Override
	public void update(AnActionEvent e) {
		e.getPresentation().setEnabled(projectFor(e) != null);
	}
}
