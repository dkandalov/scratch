package scratch;

/**
* User: dima
* Date: 10/02/2013
*/
public class ScratchInfo {
	public final String nameWithMnemonics;
	public final String name;
	public final String extension;

	public static ScratchInfo createFrom(String fullNameWithMnemonics) {
		return new ScratchInfo(
				extractNameFrom(fullNameWithMnemonics),
				extractExtensionFrom(fullNameWithMnemonics)
		);
	}

	public ScratchInfo(String nameWithMnemonics, String extension) {
		this.nameWithMnemonics = nameWithMnemonics;
		this.name = nameWithMnemonics.replace("&", "");
		this.extension = extension;
	}

	public String asFileName() {
		return name + "." + extension;
	}

	public String fullNameWithMnemonics() {
		return nameWithMnemonics + "." + extension;
	}

	private static String extractExtensionFrom(String fileName) {
		int index = fileName.lastIndexOf(".");
		return index == -1 ? "" : fileName.substring(index + 1);
	}

	private static String extractNameFrom(String fileName) {
		int index = fileName.lastIndexOf(".");
		if (index == -1) index = fileName.length();
		return fileName.substring(0, index);
	}

	@Override public String toString() {
		return "{nameWithMnemonics='" + nameWithMnemonics + "'" + ", extension='" + extension + "'}";
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ScratchInfo that = (ScratchInfo) o;

		if (extension != null ? !extension.equals(that.extension) : that.extension != null) return false;
		if (nameWithMnemonics != null ? !nameWithMnemonics.equals(that.nameWithMnemonics) : that.nameWithMnemonics != null) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = nameWithMnemonics != null ? nameWithMnemonics.hashCode() : 0;
		result = 31 * result + (extension != null ? extension.hashCode() : 0);
		return result;
	}
}
