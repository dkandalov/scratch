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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class Util {
	public static final Icon NO_ICON = null;
	private static final Key<Project> PROJECT_KEY = Key.create("Project");

	public static UserDataHolder holdingOnTo(Project project) {
		UserDataHolder userDataHolder = new UserDataHolderBase();
		userDataHolder.putUserData(PROJECT_KEY, project);
		return userDataHolder;
	}

	@SuppressWarnings("ConstantConditions")
	@NotNull public static Project takeProjectFrom(UserDataHolder userDataHolder) {
		return userDataHolder.getUserData(PROJECT_KEY);
	}
}
