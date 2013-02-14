package scratch;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.map;
import static scratch.ScratchConfig.AppendType.APPEND;

/**
 * User: dima
 * Date: 10/02/2013
 */
public class ScratchConfig {
	public static final ScratchConfig DEFAULT_CONFIG = new ScratchConfig(Collections.<Scratch>emptyList(), false, true, APPEND);

	public enum AppendType { APPEND, PREPEND }

	public final List<Scratch> scratches;
	public final boolean needMigration;
	public final boolean listenToClipboard;
	public final AppendType clipboardAppendType;

	private ScratchConfig(List<Scratch> scratches, boolean listenToClipboard, boolean needMigration, AppendType clipboardAppendType) {
		this.scratches = Collections.unmodifiableList(scratches);
		this.listenToClipboard = listenToClipboard;
		this.needMigration = needMigration;
		this.clipboardAppendType = clipboardAppendType;
	}

	public ScratchConfig with(List<Scratch> newScratches) {
		return new ScratchConfig(newScratches, listenToClipboard, needMigration, clipboardAppendType);
	}

	public ScratchConfig append(Scratch scratch) {
		ArrayList<Scratch> newScratches = new ArrayList<Scratch>(scratches);
		newScratches.add(scratch);
		return this.with(newScratches);
	}

	public ScratchConfig replace(final Scratch scratch, final Scratch newScratch) {
		return new ScratchConfig(map(scratches, new Function<Scratch, Scratch>() {
			@Override public Scratch fun(Scratch it) {
				return it.equals(scratch) ? newScratch : it;
			}
		}), listenToClipboard, needMigration, clipboardAppendType);
	}

	public ScratchConfig move(final Scratch scratch, int shift) {
		final Scratch prevScratch = scratches.get(scratches.indexOf(scratch) + shift);
		return this.with(map(scratches, new Function<Scratch, Scratch>() {
			@Override public Scratch fun(Scratch it) {
				if (it.equals(prevScratch)) return scratch;
				else if (it.equals(scratch)) return prevScratch;
				else return it;
			}
		}));
	}

	public ScratchConfig needsMigration(boolean value) {
		return new ScratchConfig(scratches, listenToClipboard, value, clipboardAppendType);
	}

	public ScratchConfig listenToClipboard(boolean value) {
		return new ScratchConfig(scratches, value, needMigration, clipboardAppendType);
	}

	public ScratchConfig with(AppendType appendType) {
		return new ScratchConfig(scratches, listenToClipboard, needMigration, appendType);
	}

	@Override public String toString() {
		return "ScratchConfig{" +
				"listenToClipboard=" + listenToClipboard + ", " +
				"needMigration=" + needMigration + ", " +
				"scratches=\n" + StringUtil.join(scratches, ",\n") +
				'}';
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ScratchConfig that = (ScratchConfig) o;

		if (listenToClipboard != that.listenToClipboard) return false;
		if (needMigration != that.needMigration) return false;
		if (scratches != null ? !scratches.equals(that.scratches) : that.scratches != null)
			return false;

		return true;
	}

	@Override public int hashCode() {
		int result = scratches != null ? scratches.hashCode() : 0;
		result = 31 * result + (needMigration ? 1 : 0);
		result = 31 * result + (listenToClipboard ? 1 : 0);
		return result;
	}
}
