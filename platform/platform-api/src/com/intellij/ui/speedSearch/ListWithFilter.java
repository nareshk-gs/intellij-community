/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
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

/*
 * @author max
 */
package com.intellij.ui.speedSearch;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.LightColors;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.UIBundle;
import com.intellij.util.Function;
import com.intellij.util.ui.ComponentWithEmptyText;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ListWithFilter<T> extends JPanel implements DataProvider {
  private final JList<T> myList;
  private final SearchTextField mySearchField = new SearchTextField(false);
  private final NameFilteringListModel<T> myModel;
  private final JScrollPane myScrollPane;
  private final MySpeedSearch mySpeedSearch;

  @Override
  public Object getData(@NonNls String dataId) {
    if (SpeedSearchSupply.SPEED_SEARCH_CURRENT_QUERY.is(dataId)) {
      return mySearchField.getText();
    }
    return null;
  }

  public static <T> JComponent wrap(@NotNull JList<T> list, @NotNull JScrollPane scrollPane, @Nullable Function<T, String> namer) {
    return wrap(list, scrollPane, namer, false);
  }

  public static <T> JComponent wrap(@NotNull JList<T> list, @NotNull JScrollPane scrollPane, @Nullable Function<T, String> namer, 
                                    boolean highlightAllOccurrences) {
    return new ListWithFilter<>(list, scrollPane, namer, highlightAllOccurrences);
  }

  private ListWithFilter(@NotNull JList<T> list,
                         @NotNull JScrollPane scrollPane,
                         @Nullable Function<T, String> namer,
                         boolean highlightAllOccurrences) {
    super(new BorderLayout());

    if (list instanceof ComponentWithEmptyText) {
      ((ComponentWithEmptyText)list).getEmptyText().setText(UIBundle.message("message.noMatchesFound"));
    }

    myList = list;
    myScrollPane = scrollPane;

    mySearchField.getTextEditor().setFocusable(false);
    mySearchField.setVisible(false);

    add(mySearchField, BorderLayout.NORTH);
    add(myScrollPane, BorderLayout.CENTER);

    mySpeedSearch = new MySpeedSearch(highlightAllOccurrences);
    mySpeedSearch.setEnabled(namer != null);

    myList.addKeyListener(new KeyAdapter() {
      public void keyPressed(final KeyEvent e) {
        mySpeedSearch.process(e);
      }
    });
    final int selectedIndex = myList.getSelectedIndex();
    final int modelSize = myList.getModel().getSize();
    myModel = new NameFilteringListModel<>(myList, namer, s -> mySpeedSearch.shouldBeShowing(s), mySpeedSearch);
    if (myModel.getSize() == modelSize) {
      myList.setSelectedIndex(selectedIndex);
    }

    setBackground(list.getBackground());
    //setFocusable(true);
  }

  @Override
  protected void processFocusEvent(FocusEvent e) {
    super.processFocusEvent(e);
    if (e.getID() == FocusEvent.FOCUS_GAINED) {
      IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> {
        IdeFocusManager.getGlobalInstance().requestFocus(myList, true);
      });
    }
  }

  public boolean resetFilter() {
    boolean hadPattern = mySpeedSearch.isHoldingFilter();
    if (mySearchField.isVisible()) {
      mySpeedSearch.reset();
    }
    return hadPattern;
  }

  public SpeedSearch getSpeedSearch() {
    return mySpeedSearch;
  }

  private class MySpeedSearch extends SpeedSearch {
    boolean searchFieldShown;
    boolean myInUpdate;

    private MySpeedSearch(boolean highlightAllOccurrences) {
      super(highlightAllOccurrences);
      // native mac "clear button" is not captured by SearchTextField.onFieldCleared
      mySearchField.addDocumentListener(new DocumentAdapter() {
        @Override
        protected void textChanged(DocumentEvent e) {
          if (myInUpdate) return;
          if (mySearchField.getText().isEmpty()) {
            mySpeedSearch.reset();
          }
        }
      });
      installSupplyTo(myList);
    }

    public void update() {
      myInUpdate = true;
      mySearchField.getTextEditor().setBackground(UIUtil.getTextFieldBackground());
      onSpeedSearchPatternChanged();
      mySearchField.setText(getFilter());
      if (isHoldingFilter() && !searchFieldShown) {
        mySearchField.setVisible(true);
        searchFieldShown = true;
      }
      else if (!isHoldingFilter() && searchFieldShown) {
        mySearchField.setVisible(false);
        searchFieldShown = false;
      }

      myInUpdate = false;
      revalidate();
    }

    @Override
    public void noHits() {
      mySearchField.getTextEditor().setBackground(LightColors.RED);
    }

    private void revalidate() {
      JBPopup popup = PopupUtil.getPopupContainerFor(mySearchField);
      if (popup != null) {
        popup.pack(false, true);
      }
      ListWithFilter.this.revalidate();
    }
  }

  protected void onSpeedSearchPatternChanged() {
    T prevSelection = myList.getSelectedValue(); // save to restore the selection on filter drop
    myModel.refilter();
    if (myModel.getSize() > 0) {
      int fullMatchIndex = mySpeedSearch.isHoldingFilter() ? myModel.getClosestMatchIndex() : myModel.getElementIndex(prevSelection);
      if (fullMatchIndex != -1) {
        myList.setSelectedIndex(fullMatchIndex);
      }

      if (myModel.getSize() <= myList.getSelectedIndex() || !myModel.contains(myList.getSelectedValue())) {
        myList.setSelectedIndex(0);
      }
    }
    else {
      mySpeedSearch.noHits();
      revalidate();
    }
  }

  public JList getList() {
    return myList;
  }

  public JScrollPane getScrollPane() {
    return myScrollPane;
  }

  @Override
  public void requestFocus() {
    IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> {
      IdeFocusManager.getGlobalInstance().requestFocus(myList, true);
    });
  }
}
