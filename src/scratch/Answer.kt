package scratch


data class Answer(val isYes: Boolean, val explanation: String = "") {
    val isNo = !isYes

    override fun toString() = if (isYes) "Yes" else "No($explanation)"

    companion object {
        fun no(explanation: String) = Answer(false, explanation)
        fun yes() = Answer(true)
    }
}
