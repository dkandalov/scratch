package scratch.ide;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;
import scratch.Scratch;
import scratch.ScratchConfig;

import java.util.ArrayList;
import java.util.List;

import static scratch.ScratchConfig.AppendType.APPEND;


@SuppressWarnings("UnusedDeclaration")
@State(name = "ScratchConfig", storages = {@Storage(id = "main", file = "$APP_CONFIG$/scratch_config.xml")})
public class ScratchConfigPersistence implements PersistentStateComponent<ScratchConfigPersistence> {
	private boolean isNeedMigration = true;
	private boolean isListenToClipboard = false;
	private List<String> fullScratchNamesOrdered = new ArrayList<String>();
	private ScratchConfig.AppendType clipboardAppendType = APPEND;
	private ScratchConfig.AppendType newScratchAppendType = APPEND;
	public String scratchesFolderPath = null;

	public static ScratchConfigPersistence getInstance() {
		return ServiceManager.getService(ScratchConfigPersistence.class);
	}

	public ScratchConfig asConfig() {
		return ScratchConfig.DEFAULT_CONFIG
				.needsMigration(isNeedMigration)
				.listenToClipboard(isListenToClipboard)
				.with(ContainerUtil.map(fullScratchNamesOrdered, new Function<String, Scratch>() {
					@Override public Scratch fun(String it) {
						return Scratch.createFrom(it);
					}
				}))
				.withClipboard(clipboardAppendType)
				.withNewScratch(newScratchAppendType);
	}

	public void updateFrom(ScratchConfig config) {
		isNeedMigration = config.needMigration;
		isListenToClipboard = config.listenToClipboard;
		fullScratchNamesOrdered = new ArrayList<String>(ContainerUtil.map(config.scratches, new Function<Scratch, String>() {
					@Override public String fun(Scratch it) {
						return it.fullNameWithMnemonics;
					}
		}));
	}

	public List<String> getFullScratchNamesOrdered() {
		return fullScratchNamesOrdered;
	}

	public void setFullScratchNamesOrdered(List<String> fullScratchNamesOrdered) {
		this.fullScratchNamesOrdered = fullScratchNamesOrdered;
	}

	public boolean isListenToClipboard() {
		return isListenToClipboard;
	}

	public void setListenToClipboard(boolean listenToClipboard) {
		isListenToClipboard = listenToClipboard;
	}

	public boolean isNeedMigration() {
		return isNeedMigration;
	}

	public void setNeedMigration(boolean needMigration) {
		isNeedMigration = needMigration;
	}

	public ScratchConfig.AppendType getClipboardAppendType() {
		return clipboardAppendType;
	}

	public void setClipboardAppendType(ScratchConfig.AppendType clipboardAppendType) {
		this.clipboardAppendType = clipboardAppendType;
	}

	@Nullable @Override public ScratchConfigPersistence getState() {
		return this;
	}

	@Override public void loadState(ScratchConfigPersistence state) {
		XmlSerializerUtil.copyBean(state, this);
	}

	public String getScratchesFolderPath() {
		return scratchesFolderPath;
	}

	public void setScratchesFolderPath(String scratchesFolderPath) {
		this.scratchesFolderPath = scratchesFolderPath;
	}

	public ScratchConfig.AppendType getNewScratchAppendType() {
		return newScratchAppendType;
	}

	public void setNewScratchAppendType(ScratchConfig.AppendType newScratchAppendType) {
		this.newScratchAppendType = newScratchAppendType;
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ScratchConfigPersistence that = (ScratchConfigPersistence) o;

		if (isListenToClipboard != that.isListenToClipboard) return false;
		if (isNeedMigration != that.isNeedMigration) return false;
		if (clipboardAppendType != that.clipboardAppendType) return false;
		if (fullScratchNamesOrdered != null ? !fullScratchNamesOrdered.equals(that.fullScratchNamesOrdered) : that.fullScratchNamesOrdered != null)
			return false;
		if (newScratchAppendType != that.newScratchAppendType) return false;
		if (scratchesFolderPath != null ? !scratchesFolderPath.equals(that.scratchesFolderPath) : that.scratchesFolderPath != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (isNeedMigration ? 1 : 0);
		result = 31 * result + (isListenToClipboard ? 1 : 0);
		result = 31 * result + (fullScratchNamesOrdered != null ? fullScratchNamesOrdered.hashCode() : 0);
		result = 31 * result + (clipboardAppendType != null ? clipboardAppendType.hashCode() : 0);
		result = 31 * result + (newScratchAppendType != null ? newScratchAppendType.hashCode() : 0);
		result = 31 * result + (scratchesFolderPath != null ? scratchesFolderPath.hashCode() : 0);
		return result;
	}
}
