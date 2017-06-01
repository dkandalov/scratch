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

package scratch.ide

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import org.apache.commons.lang.StringEscapeUtils

@State(name = "ScratchData", storages = arrayOf(Storage(id = "main", file = "\$APP_CONFIG$/scratch.xml")))
class ScratchOldData: PersistentStateComponent<ScratchOldData> {

    private val scratchTexts = Array<String>(SIZE, { "" })

    @Suppress("unused") // used by intellij serializer through reflection
    var scratchText: Array<String>
        get() = scratchTexts
        set(text) = System.arraycopy(text, 0, scratchTexts, 0, text.size)

    @Suppress("unused") // used by intellij serializer through reflection
    val scratchTextInternal: Array<String>
        get() {
            val result = Array<String>(SIZE, { "" })
            for (i in result.indices) {
                result[i] = XmlUtil.unescape(scratchTexts[i])
            }
            return result
        }

    override fun getState(): ScratchOldData? {
        return this
    }

    override fun loadState(scratchOldData: ScratchOldData) {
        XmlSerializerUtil.copyBean(scratchOldData, this)
    }


    object XmlUtil {
        private val ESCAPED_CODE_MAX_SIZE = 10

        fun unescape(text: String): String {
            val unescapedControlCodes = unescapeControlCodes(text)
            return StringEscapeUtils.unescapeXml(unescapedControlCodes)
        }

        private fun unescapeControlCodes(text: String): String {
            val builder = StringBuilder()
            var i = 0
            while (i < text.length) {
                val c = text[i]
                if (c == '&' && i + 1 < text.length && text[i + 1] == '#') {
                    val semiColonIndex = semiColonIndex(text, i)
                    if (semiColonIndex != -1) {
                        val value = Integer.valueOf(text.substring(i + 2, semiColonIndex))!!
                        builder.append(value.toChar())
                        i = semiColonIndex
                    }
                } else {
                    builder.append(c)
                }
                i++
            }
            return builder.toString()
        }

        private fun semiColonIndex(text: String, fromPos: Int): Int {
            var semiColonIndex = -1
            var j = 1
            do {
                if (j > ESCAPED_CODE_MAX_SIZE)
                    break
                if (text[fromPos + j] == ';') {
                    semiColonIndex = fromPos + j
                    break
                }
                j++
            } while (true)
            return semiColonIndex
        }
    }

    companion object {
        private val SIZE = 5

        @JvmStatic val instance: ScratchOldData
            get() = ServiceManager.getService(ScratchOldData::class.java)
    }
}
