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

package scratch.ide.popup;

import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.ui.popup.ListSeparator;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.speedSearch.ElementFilter;
import com.intellij.ui.speedSearch.SpeedSearch;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
class PopupModelWithMovableItems extends AbstractListModel {

	private final List<Object> myOriginalList;
	private final List<Object> myFilteredList = new ArrayList<>();

	private final ElementFilter myFilter;
	private final ListPopupStep myStep;

	private int myFullMatchIndex = -1;
	private int myStartsWithIndex = -1;
	private final SpeedSearch mySpeedSearch;

	public PopupModelWithMovableItems(ElementFilter filter, SpeedSearch speedSearch, ListPopupStep step) {
		myFilter = filter;
		myStep = step;
		mySpeedSearch = speedSearch;
		myOriginalList = new ArrayList<>(step.getValues());
		rebuildLists();
	}

	public int moveItem(Object item, int shift) {
		int oldIndex = myOriginalList.indexOf(item);
		int newIndex = oldIndex + shift;
		if (newIndex < 0) newIndex += myOriginalList.size();
		if (newIndex >= myOriginalList.size()) newIndex -= myOriginalList.size();

		myOriginalList.remove(oldIndex);
		myOriginalList.add(newIndex, item);

		rebuildLists();
		fireContentsChanged(this, 0, myFilteredList.size());

		return newIndex;
	}

	public void deleteItem(final Object item) {
		final int i = myOriginalList.indexOf(item);
		if (i >= 0) {
			myOriginalList.remove(i);
			rebuildLists();
			fireContentsChanged(this, 0, myFilteredList.size());
		}
	}

	@Nullable
	public Object get(final int i) {
		if (i >= 0 && i < myFilteredList.size()) {
			return myFilteredList.get(i);
		}

		return null;
	}

	private void rebuildLists() {
		myFilteredList.clear();
		myFullMatchIndex = -1;
		myStartsWithIndex = -1;

		for (Object each : myOriginalList) {
			if (myFilter.shouldBeShowing(each)) {
				addToFiltered(each);
			}
		}
	}

	private void addToFiltered(Object each) {
		myFilteredList.add(each);
		String filterString = toUpperCase(mySpeedSearch.getFilter());
		String candidateString = toUpperCase(myStep.getTextFor(each));
		int index = myFilteredList.size() - 1;

		if (myFullMatchIndex == -1 && filterString.equals(candidateString)) {
			myFullMatchIndex = index;
		}

		if (myStartsWithIndex == -1 && candidateString.startsWith(filterString)) {
			myStartsWithIndex = index;
		}
	}

	@Override public int getSize() {
		return myFilteredList.size();
	}

	@Override public Object getElementAt(int index) {
		if (index >= myFilteredList.size()) {
			return null;
		}
		return myFilteredList.get(index);
	}

	public boolean isSeparatorAboveOf(Object aValue) {
		return getSeparatorAbove(aValue) != null;
	}

	public String getCaptionAboveOf(Object value) {
		ListSeparator separator = getSeparatorAbove(value);
		if (separator != null) {
			return separator.getText();
		}
		return "";
	}

	private ListSeparator getSeparatorAbove(Object value) {
		return myStep.getSeparatorAbove(value);
	}

	public void refilter() {
		rebuildLists();
		if (myFilteredList.isEmpty() && !myOriginalList.isEmpty()) {
			mySpeedSearch.noHits();
		}
		else {
			fireContentsChanged(this, 0, myFilteredList.size());
		}
	}

	public boolean isVisible(Object object) {
		return myFilteredList.contains(object);
	}

	public int getClosestMatchIndex() {
		return myFullMatchIndex != -1 ? myFullMatchIndex : myStartsWithIndex;
	}

	private static String toUpperCase(String s) {
		return StringUtil.capitalize(s);
	}
}