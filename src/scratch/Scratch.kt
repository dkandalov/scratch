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

package scratch


data class Scratch(
    val fullNameWithMnemonics: String,
    val name: String = Scratch.extractNameFrom(fullNameWithMnemonics),
    val extension: String = Scratch.extractExtensionFrom(fullNameWithMnemonics)
) {
    val fileName: String get() = name + "." + extension

    override fun toString() = "{fullNameWithMnemonics='$fullNameWithMnemonics'}"

    companion object {
        private fun extractExtensionFrom(fileName: String): String {
            val index = fileName.lastIndexOf(".")
            return if (index == -1) "" else fileName.substring(index + 1).replace("&", "")
        }

        private fun extractNameFrom(fileName: String): String {
            var index = fileName.lastIndexOf(".")
            if (index == -1) index = fileName.length
            return fileName.substring(0, index).replace("&", "")
        }
    }
}
