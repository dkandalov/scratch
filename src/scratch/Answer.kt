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


data class Answer( val isYes: Boolean, val explanation: String?) {
    val isNo = !isYes

    override fun toString() = if (isYes) "Yes" else "No($explanation)"

    companion object {
        fun no(explanation: String) = Answer(false, explanation)
        fun yes() = Answer(true, null)
    }
}
