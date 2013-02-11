package scratch;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;

import java.util.Collections;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.map;
import static scratch.ScratchConfig.AppendType.APPEND;

/**
 * User: dima
 * Date: 10/02/2013
 */
public class ScratchConfig {
	public static final ScratchConfig DEFAULT_CONFIG = new ScratchConfig(Collections.<ScratchInfo>emptyList(), false, true, APPEND);

	public enum AppendType { APPEND, PREPEND }

	public final List<ScratchInfo> scratchInfos;
	public final boolean needMigration;
	public final boolean listenToClipboard;
	public final AppendType clipboardAppendType;

	private ScratchConfig(List<ScratchInfo> scratchInfos, boolean listenToClipboard, boolean needMigration, AppendType clipboardAppendType) {
		this.scratchInfos = scratchInfos;
		this.listenToClipboard = listenToClipboard;
		this.needMigration = needMigration;
		this.clipboardAppendType = clipboardAppendType;
	}

	public ScratchConfig with(List<ScratchInfo> newScratchInfos) {
		return new ScratchConfig(newScratchInfos, listenToClipboard, needMigration, clipboardAppendType);
	}

	public ScratchConfig replace(final ScratchInfo scratchInfo, final ScratchInfo newScratchInfo) {
		return new ScratchConfig(map(scratchInfos, new Function<ScratchInfo, ScratchInfo>() {
			@Override public ScratchInfo fun(ScratchInfo it) {
				return it.equals(scratchInfo) ? newScratchInfo : it;
			}
		}), listenToClipboard, needMigration, clipboardAppendType);
	}

	public ScratchConfig move(final ScratchInfo scratchInfo, int shift) {
		final ScratchInfo prevScratchInfo = scratchInfos.get(scratchInfos.indexOf(scratchInfo) + shift);
		return this.with(map(scratchInfos, new Function<ScratchInfo, ScratchInfo>() {
			@Override public ScratchInfo fun(ScratchInfo it) {
				if (it.equals(prevScratchInfo)) return scratchInfo;
				else if (it.equals(scratchInfo)) return prevScratchInfo;
				else return it;
			}
		}));
	}

	public ScratchConfig needsMigration(boolean value) {
		return new ScratchConfig(scratchInfos, listenToClipboard, value, clipboardAppendType);
	}

	public ScratchConfig listenToClipboard(boolean value) {
		return new ScratchConfig(scratchInfos, value, needMigration, clipboardAppendType);
	}

	public ScratchConfig with(AppendType appendType) {
		return new ScratchConfig(scratchInfos, listenToClipboard, needMigration, appendType);
	}

	@Override public String toString() {
		return "ScratchConfig{" +
				"listenToClipboard=" + listenToClipboard + ", " +
				"needMigration=" + needMigration + ", " +
				"scratchInfos=\n" + StringUtil.join(scratchInfos, ",\n") +
				'}';
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ScratchConfig that = (ScratchConfig) o;

		if (listenToClipboard != that.listenToClipboard) return false;
		if (needMigration != that.needMigration) return false;
		if (scratchInfos != null ? !scratchInfos.equals(that.scratchInfos) : that.scratchInfos != null)
			return false;

		return true;
	}

	@Override public int hashCode() {
		int result = scratchInfos != null ? scratchInfos.hashCode() : 0;
		result = 31 * result + (needMigration ? 1 : 0);
		result = 31 * result + (listenToClipboard ? 1 : 0);
		return result;
	}
}
