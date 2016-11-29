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

import org.junit.Test;

import static com.intellij.util.containers.ContainerUtil.list;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static scratch.ScratchConfig.DEFAULT_CONFIG;
import static scratch.ScratchConfig.DOWN;
import static scratch.ScratchConfig.UP;

public class ScratchConfigTest {

	private final Scratch scratch1 = Scratch.create("scratch1.txt");
	private final Scratch scratch2 = Scratch.create("scratch2.txt");
	private final Scratch scratch3 = Scratch.create("scratch3.txt");
	private final ScratchConfig config = DEFAULT_CONFIG.with(list(scratch1, scratch2, scratch3));

	@Test public void movingTopScratchToBottom() {
		assertThat(config.move(scratch1, UP), equalTo(DEFAULT_CONFIG.with(list(scratch2, scratch3, scratch1))));
	}

	@Test public void movingBottomScratchToTop() {
		assertThat(config.move(scratch3, DOWN), equalTo(DEFAULT_CONFIG.with(list(scratch3, scratch1, scratch2))));
	}

	@Test public void movingScratchUp() {
		assertThat(config.move(scratch2, UP), equalTo(DEFAULT_CONFIG.with(list(scratch2, scratch1, scratch3))));
	}
}
