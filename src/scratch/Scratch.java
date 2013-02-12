package scratch;

/**
 * User: dima
 * Date: 10/02/2013
 */
public class Scratch {
	public final String fullNameWithMnemonics;
	public final String name;
	public final String extension;

	public static Scratch createFrom(String fullNameWithMnemonics) {
		return new Scratch(
				fullNameWithMnemonics,
				extractNameFrom(fullNameWithMnemonics),
				extractExtensionFrom(fullNameWithMnemonics)
		);
	}

	private Scratch(String fullNameWithMnemonics, String name, String extension) {
		this.fullNameWithMnemonics = fullNameWithMnemonics;
		this.name = name;
		this.extension = extension;
	}

	public String asFileName() {
		return name + "." + extension;
	}

	private static String extractExtensionFrom(String fileName) {
		int index = fileName.lastIndexOf(".");
		return index == -1 ? "" : fileName.substring(index + 1).replace("&", "");
	}

	private static String extractNameFrom(String fileName) {
		int index = fileName.lastIndexOf(".");
		if (index == -1) index = fileName.length();
		return fileName.substring(0, index).replace("&", "");
	}

	@Override public String toString() {
		return "{fullNameWithMnemonics='" + fullNameWithMnemonics + "'";
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Scratch scratch = (Scratch) o;

		if (extension != null ? !extension.equals(scratch.extension) : scratch.extension != null) return false;
		if (fullNameWithMnemonics != null ? !fullNameWithMnemonics.equals(scratch.fullNameWithMnemonics) : scratch.fullNameWithMnemonics != null)
			return false;
		if (name != null ? !name.equals(scratch.name) : scratch.name != null) return false;

		return true;
	}

	@Override public int hashCode() {
		int result = fullNameWithMnemonics != null ? fullNameWithMnemonics.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (extension != null ? extension.hashCode() : 0);
		return result;
	}
}
