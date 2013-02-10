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
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import scratch.ide.ScratchComponent;

import static scratch.ide.Util.holdingTo;

/**
 * @author Dmitry Kandalov
 */
public class OpenDefaultScratchAction extends DumbAwareAction {
	@Override
	public void actionPerformed(AnActionEvent event) {
		ScratchComponent.instance().userWantsToOpenDefaultScratch(holdingTo(event));
//		Project project = projectFor(event);
//
//		VirtualFile defaultScratch = ScratchComponent.getDefaultScratch();
//		if (defaultScratch != null) {
//			OpenFileDescriptor fileDescriptor = new OpenFileDescriptor(project, defaultScratch);
//			fileDescriptor.navigate(true);
//		} else {
//			new AddScratchAction().actionPerformed(event);
//		}
	}

	@Override
	public void update(AnActionEvent e) {
		e.getPresentation().setEnabled(projectFor(e) != null);
	}

	public static Project projectFor(AnActionEvent event) {
		return event.getData(PlatformDataKeys.PROJECT);
	}
}
