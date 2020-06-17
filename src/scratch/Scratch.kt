package scratch


data class Scratch(val fullNameWithMnemonics: String) {
    val name: String = fullNameWithMnemonics.extractName()
    val extension: String = fullNameWithMnemonics.extractExtension()
    val fileName: String get() = "$name.$extension"

    override fun toString() = "{fullNameWithMnemonics='$fullNameWithMnemonics'}"

    companion object {
        private fun String.extractExtension(): String {
            val index = lastIndexOf(".")
            return if (index == -1) "" else substring(index + 1).replace("&", "")
        }

        private fun String.extractName(): String {
            var index = lastIndexOf(".")
            if (index == -1) index = length
            return substring(0, index).replace("&", "")
        }
    }
}
