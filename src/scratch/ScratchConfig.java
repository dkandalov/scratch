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

import com.intellij.util.Function;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.map;
import static java.util.Collections.unmodifiableList;
import static scratch.ScratchConfig.AppendType.APPEND;
import static scratch.ScratchConfig.AppendType.PREPEND;
import static scratch.ScratchConfig.DefaultScratchMeaning.TOPMOST;


public class ScratchConfig {
	public static final ScratchConfig DEFAULT_CONFIG = new ScratchConfig(
			Collections.<Scratch>emptyList(), null, false, true, APPEND, APPEND, TOPMOST);
	public static final int UP = -1;
	public static final int DOWN = 1;

	public enum AppendType { APPEND, PREPEND }
	public enum DefaultScratchMeaning { TOPMOST, LAST_OPENED }

	public final List<Scratch> scratches;
	@Nullable public final Scratch lastOpenedScratch;
	public final boolean needMigration;
	public final boolean listenToClipboard;
	public final AppendType clipboardAppendType;
	private final AppendType newScratchAppendType;
	public final DefaultScratchMeaning defaultScratchMeaning;

	private ScratchConfig(List<Scratch> scratches, @Nullable Scratch lastOpenedScratch, boolean listenToClipboard, boolean needMigration,
	                      AppendType clipboardAppendType, AppendType newScratchAppendType, DefaultScratchMeaning defaultScratchMeaning) {
		this.lastOpenedScratch = lastOpenedScratch;
		this.scratches = unmodifiableList(scratches);
		this.listenToClipboard = listenToClipboard;
		this.needMigration = needMigration;
		this.clipboardAppendType = clipboardAppendType;
		this.newScratchAppendType = newScratchAppendType;
		this.defaultScratchMeaning = defaultScratchMeaning;
	}

	public ScratchConfig with(List<Scratch> newScratches) {
		return new ScratchConfig(
				newScratches, lastOpenedScratch, listenToClipboard, needMigration,
				clipboardAppendType, newScratchAppendType, defaultScratchMeaning
		);
	}

	public ScratchConfig add(Scratch scratch) {
		ArrayList<Scratch> newScratches = new ArrayList<Scratch>(scratches);
		if (newScratchAppendType == APPEND) {
			newScratches.add(scratch);
		} else if (newScratchAppendType == PREPEND) {
			newScratches.add(0, scratch);
		} else {
			throw new IllegalStateException();
		}
		return this.with(newScratches);
	}

	public ScratchConfig without(Scratch scratch) {
		ArrayList<Scratch> newScratches = new ArrayList<Scratch>(scratches);
		newScratches.remove(scratch);
		return this.with(newScratches);
	}

	public ScratchConfig replace(final Scratch scratch, final Scratch newScratch) {
		List<Scratch> scratchList = map(scratches, new Function<Scratch, Scratch>() {
			@Override public Scratch fun(Scratch it) {
				return it.equals(scratch) ? newScratch : it;
			}
		});
		Scratch lastOpened = (scratch.equals(lastOpenedScratch) ? newScratch : lastOpenedScratch);
		return new ScratchConfig(scratchList, lastOpened, listenToClipboard, needMigration, clipboardAppendType, newScratchAppendType, defaultScratchMeaning);
	}

	public ScratchConfig move(final Scratch scratch, int shift) {
		int oldIndex = scratches.indexOf(scratch);
		int newIndex = oldIndex + shift;
		if (newIndex < 0) newIndex += scratches.size();
		if (newIndex >= scratches.size()) newIndex -= scratches.size();

		List<Scratch> newScratches = new ArrayList<Scratch>(scratches);
		newScratches.remove(oldIndex);
		newScratches.add(newIndex, scratch);
		return this.with(newScratches);
	}

	public ScratchConfig listenToClipboard(boolean value) {
		return new ScratchConfig(
				scratches, lastOpenedScratch, value, needMigration,
				clipboardAppendType, newScratchAppendType, defaultScratchMeaning);
	}

	public ScratchConfig needsMigration(boolean value) {
		return new ScratchConfig(
				scratches, lastOpenedScratch, listenToClipboard, value,
				clipboardAppendType, newScratchAppendType, defaultScratchMeaning);
	}

	public ScratchConfig withClipboard(AppendType value) {
		if (value == null) return this;
		return new ScratchConfig(
				scratches, lastOpenedScratch, listenToClipboard, needMigration,
				value, newScratchAppendType, defaultScratchMeaning);
	}

	public ScratchConfig withNewScratch(AppendType value) {
		if (value == null) return this;
		return new ScratchConfig(
				scratches, lastOpenedScratch, listenToClipboard, needMigration,
				clipboardAppendType, value, defaultScratchMeaning);
	}

	public ScratchConfig withDefaultScratchMeaning(DefaultScratchMeaning value) {
		if (value == null) return this;
		return new ScratchConfig(
				scratches, lastOpenedScratch, listenToClipboard, needMigration,
				clipboardAppendType, newScratchAppendType, value);
	}

	public ScratchConfig withLastOpenedScratch(Scratch value) {
		return new ScratchConfig(
				scratches, value, listenToClipboard, needMigration,
				clipboardAppendType, newScratchAppendType, defaultScratchMeaning);
	}

	@Override public String toString() {
		return "ScratchConfig{" +
				"clipboardAppendType=" + clipboardAppendType +
				", scratches=" + scratches +
				", lastOpenedScratch=" + lastOpenedScratch +
				", needMigration=" + needMigration +
				", listenToClipboard=" + listenToClipboard +
				", newScratchAppendType=" + newScratchAppendType +
				", defaultScratchMeaning=" + defaultScratchMeaning +
				'}';
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ScratchConfig that = (ScratchConfig) o;

		if (listenToClipboard != that.listenToClipboard) return false;
		if (needMigration != that.needMigration) return false;
		if (clipboardAppendType != that.clipboardAppendType) return false;
		if (defaultScratchMeaning != that.defaultScratchMeaning) return false;
		if (lastOpenedScratch != null ? !lastOpenedScratch.equals(that.lastOpenedScratch) : that.lastOpenedScratch != null)
			return false;
		if (newScratchAppendType != that.newScratchAppendType) return false;
		if (scratches != null ? !scratches.equals(that.scratches) : that.scratches != null) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = scratches != null ? scratches.hashCode() : 0;
		result = 31 * result + (lastOpenedScratch != null ? lastOpenedScratch.hashCode() : 0);
		result = 31 * result + (needMigration ? 1 : 0);
		result = 31 * result + (listenToClipboard ? 1 : 0);
		result = 31 * result + (clipboardAppendType != null ? clipboardAppendType.hashCode() : 0);
		result = 31 * result + (newScratchAppendType != null ? newScratchAppendType.hashCode() : 0);
		result = 31 * result + (defaultScratchMeaning != null ? defaultScratchMeaning.hashCode() : 0);
		return result;
	}
}
