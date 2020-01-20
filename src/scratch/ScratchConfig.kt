package scratch

import scratch.ScratchConfig.AppendType.APPEND
import scratch.ScratchConfig.AppendType.PREPEND
import scratch.ScratchConfig.DefaultScratchMeaning.TOPMOST


data class ScratchConfig(
    val scratches: List<Scratch>,
    val lastOpenedScratch: Scratch?,
    val listenToClipboard: Boolean,
    val clipboardAppendType: AppendType,
    private val newScratchAppendType: AppendType,
    val defaultScratchMeaning: DefaultScratchMeaning
) {
    enum class AppendType {
        APPEND, PREPEND
    }

    enum class DefaultScratchMeaning {
        TOPMOST, LAST_OPENED
    }

    fun add(scratch: Scratch): ScratchConfig {
        val newScratches = scratches.toMutableList()
        when (newScratchAppendType) {
            APPEND  -> newScratches.add(scratch)
            PREPEND -> newScratches.add(0, scratch)
        }
        return this.with(newScratches)
    }

    fun with(newScratches: List<Scratch>) = copy(scratches = newScratches)

    fun without(scratch: Scratch) = copy(scratches = scratches.filter { it != scratch })

    fun replace(scratch: Scratch, newScratch: Scratch) = copy(
        scratches = scratches.map { if (it == scratch) newScratch else it },
        lastOpenedScratch = if (scratch == lastOpenedScratch) newScratch else lastOpenedScratch
    )

    fun move(scratch: Scratch, shift: Int): ScratchConfig {
        val oldIndex = scratches.indexOf(scratch)
        var newIndex = oldIndex + shift
        if (newIndex < 0) newIndex += scratches.size
        if (newIndex >= scratches.size) newIndex -= scratches.size

        val newScratches = scratches.toMutableList()
        newScratches.removeAt(oldIndex)
        newScratches.add(newIndex, scratch)
        return this.with(newScratches)
    }

    fun listenToClipboard(value: Boolean) = copy(listenToClipboard = value)

    fun withClipboard(value: AppendType?): ScratchConfig {
        if (value == null) return this
        return copy(clipboardAppendType = value)
    }

    fun withNewScratch(value: AppendType?): ScratchConfig {
        if (value == null) return this
        return copy(newScratchAppendType = value)
    }

    fun withDefaultScratchMeaning(value: DefaultScratchMeaning?): ScratchConfig {
        return if (value == null) this
        else copy(defaultScratchMeaning = value)
    }

    fun withLastOpenedScratch(value: Scratch?) = copy(lastOpenedScratch = value)

    companion object {
        val defaultConfig = ScratchConfig(
            scratches = emptyList(),
            lastOpenedScratch = null,
            listenToClipboard = false,
            clipboardAppendType = APPEND,
            newScratchAppendType = APPEND,
            defaultScratchMeaning = TOPMOST
        )
        const val up = -1
        const val down = 1
    }
}
