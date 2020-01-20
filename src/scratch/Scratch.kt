package scratch


data class Scratch(
    val fullNameWithMnemonics: String,
    val name: String = extractNameFrom(fullNameWithMnemonics),
    val extension: String = extractExtensionFrom(fullNameWithMnemonics)
) {
    val fileName: String get() = "$name.$extension"

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
