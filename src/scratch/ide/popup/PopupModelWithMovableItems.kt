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

package scratch.ide.popup

import com.intellij.openapi.ui.popup.ListPopupStep
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.speedSearch.ElementFilter
import com.intellij.ui.speedSearch.SpeedSearch
import java.util.*
import javax.swing.AbstractListModel

internal class PopupModelWithMovableItems<T>(
    private val myFilter: ElementFilter<T>,
    private val mySpeedSearch: SpeedSearch,
    private val myStep: ListPopupStep<T>
): AbstractListModel<T>() {

    private val myOriginalList = myStep.values.toMutableList()
    private val myFilteredList = ArrayList<T>()

    private var myFullMatchIndex = -1
    private var myStartsWithIndex = -1

    init {
        rebuildLists()
    }

    fun moveItem(item: T, shift: Int): Int {
        val oldIndex = myOriginalList.indexOf(item)
        var newIndex = oldIndex + shift
        if (newIndex < 0) newIndex += myOriginalList.size
        if (newIndex >= myOriginalList.size) newIndex -= myOriginalList.size

        myOriginalList.removeAt(oldIndex)
        myOriginalList.add(newIndex, item)

        rebuildLists()
        fireContentsChanged(this, 0, myFilteredList.size)

        return newIndex
    }

    fun deleteItem(item: T) {
        val i = myOriginalList.indexOf(item)
        if (i >= 0) {
            myOriginalList.removeAt(i)
            rebuildLists()
            fireContentsChanged(this, 0, myFilteredList.size)
        }
    }

    operator fun get(i: Int): Any? {
        if (i >= 0 && i < myFilteredList.size) {
            return myFilteredList[i]
        }

        return null
    }

    private fun rebuildLists() {
        myFilteredList.clear()
        myFullMatchIndex = -1
        myStartsWithIndex = -1

        myOriginalList
            .filter { myFilter.shouldBeShowing(it) }
            .forEach { addToFiltered(it) }
    }

    private fun addToFiltered(each: T) {
        myFilteredList.add(each)
        val filterString = toUpperCase(mySpeedSearch.filter)
        val candidateString = toUpperCase(myStep.getTextFor(each))
        val index = myFilteredList.size - 1

        if (myFullMatchIndex == -1 && filterString == candidateString) {
            myFullMatchIndex = index
        }

        if (myStartsWithIndex == -1 && candidateString.startsWith(filterString)) {
            myStartsWithIndex = index
        }
    }

    override fun getSize(): Int {
        return myFilteredList.size
    }

    override fun getElementAt(index: Int): T? {
        if (index >= myFilteredList.size) {
            return null
        }
        return myFilteredList[index]
    }

    fun isSeparatorAboveOf(aValue: T): Boolean {
        return getSeparatorAbove(aValue) != null
    }

    fun getCaptionAboveOf(value: T): String {
        val separator = getSeparatorAbove(value)
        if (separator != null) {
            return separator.text
        }
        return ""
    }

    private fun getSeparatorAbove(value: T): ListSeparator? {
        return myStep.getSeparatorAbove(value)
    }

    fun refilter() {
        rebuildLists()
        if (myFilteredList.isEmpty() && !myOriginalList.isEmpty()) {
            mySpeedSearch.noHits()
        } else {
            fireContentsChanged(this, 0, myFilteredList.size)
        }
    }

    fun isVisible(`object`: T): Boolean {
        return myFilteredList.contains(`object`)
    }

    val closestMatchIndex: Int
        get() = if (myFullMatchIndex != -1) myFullMatchIndex else myStartsWithIndex

    private fun toUpperCase(s: String): String {
        return StringUtil.capitalize(s)
    }
}