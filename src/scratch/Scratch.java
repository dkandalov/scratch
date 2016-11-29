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


public class Scratch {
	public final String fullNameWithMnemonics;
	public final String name;
	public final String extension;

	public static Scratch create(String fullNameWithMnemonics) {
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
		return "{fullNameWithMnemonics='" + fullNameWithMnemonics + "'}";
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
