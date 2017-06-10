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

import scratch.ScratchConfig.AppendType.APPEND
import scratch.ScratchConfig.AppendType.PREPEND
import scratch.ScratchConfig.DefaultScratchMeaning.TOPMOST


data class ScratchConfig(
    val scratches: List<Scratch>,
    val lastOpenedScratch: Scratch?,
    val listenToClipboard: Boolean,
    val needMigration: Boolean,
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
            APPEND -> newScratches.add(scratch)
            PREPEND -> newScratches.add(0, scratch)
            else -> throw IllegalStateException()
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

    fun needsMigration(value: Boolean) = copy(needMigration = value)

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
            emptyList<Scratch>(), null, false, true, APPEND, APPEND, TOPMOST
        )
        val up = -1
        val down = 1
    }
}
