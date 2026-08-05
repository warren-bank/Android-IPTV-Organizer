package com.github.warren_bank.iptv_organizer.ui.dialog;

import com.github.warren_bank.iptv_organizer.R;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;

import java.util.Collections;
import java.util.List;

public class SavedSearchKeywordsListDialog {

  public interface Listener {
    public void onSavedSearchKeywordsListItemClick(String keywords);
  }

  private SearchView searchView;
  private Listener listener;
  private List<String> keywordsList;
  private ArrayAdapter<String> adapter;
  private AlertDialog dialog;
  private Button addButton;  // positive: add item
  private Button delButton;  // negative: remove item
  private boolean isShowing; // note: need to manage this internally because dialog.isShowing() will always return true after dialog.show() has been called once (until the dialog has been either dismissed or cancelled)

  public SavedSearchKeywordsListDialog(Context context, SearchView searchView) {
    this(context, searchView, -1);
  }

  public SavedSearchKeywordsListDialog(Context context, SearchView searchView, int submitButtonResId) {
    this(context, searchView, submitButtonResId, null);
  }

  public SavedSearchKeywordsListDialog(Context context, SearchView searchView, int submitButtonResId, Listener listener) {
    this.searchView   = searchView;
    this.listener     = listener;
    this.keywordsList = DbUtils.getDb().getSavedSearchKeywordsList();

    this.adapter = new ArrayAdapter<>(
      context,
      android.R.layout.select_dialog_item,
      keywordsList
    );

    this.dialog = new AlertDialog.Builder(context)
      .setTitle(R.string.dialog_saved_search_keywords_list_title)
      .setPositiveButton(R.string.add, null)
      .setNegativeButton(R.string.remove, null)
      .setNeutralButton(R.string.close, null)
      .setAdapter(adapter, null)
      .setCancelable(false)
      .show();

    // notes:
    //   builder.create() does NOT inflate the View hierarchy, so Button elements are null
    //   builder.show()   does.. so we get our Button references and quickly hide the dialog, which does NOT destroy this View hierarchy.. only changes its visibility

    this.addButton    = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
    this.delButton    = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
    Button hideButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
    ListView listView = dialog.getListView();

    dialog.hide();
    this.isShowing = false;

    // override default behavior for all buttons

    addButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addItem();
      }
    });

    delButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        removeItem();
      }
    });

    hideButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hide();
      }
    });

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
      }
    });

    initSearchView(submitButtonResId);
  }

  private void initSearchView(int submitButtonResId) {
    searchView.setSubmitButtonEnabled(true);

    View submitButton = searchView.findViewById(androidx.appcompat.R.id.search_go_btn);

    if (submitButton != null) {
      if ((submitButtonResId > 0) && (submitButton instanceof ImageView)) {
        ImageView iv = (ImageView) submitButton;

//      iv.setAdjustViewBounds(true);
//      iv.setMaxHeight(24);
//      iv.setMaxWidth(24);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        iv.setLayoutParams(new LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        iv.setImageResource(submitButtonResId);
      }

      submitButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          searchView.clearFocus();
          show();
        }
      });
    }
  }

  private String getCurrentKeywords() {
    return searchView.getQuery().toString();
  }

  private boolean isCurrentKeywordsEmpty() {
    String currentKeywords = getCurrentKeywords();
    return ((currentKeywords == null) || currentKeywords.isEmpty());
  }

  private int binarySearch() {
    return Collections.binarySearch(keywordsList, getCurrentKeywords());
  }

  private boolean isSaved() {
    int index = binarySearch();
    return isSaved(index);
  }

  private boolean isSaved(int index) {
    return (index >= 0);
  }

  private void addItem() {
    String currentKeywords = getCurrentKeywords();
    int index = binarySearch();
    if (isSaved(index)) {
      update();
      return;
    }

    // save to DB
    DbUtils.getDb().addSavedSearchKeywordsListItem(currentKeywords);

    // update UI (maintain sort order)
    index = -(index + 1);
    keywordsList.add(index, currentKeywords);
    adapter.notifyDataSetChanged();
    update();
  }

  private void removeItem() {
    String currentKeywords = getCurrentKeywords();
    int index = binarySearch();
    if (!isSaved(index)) {
      update();
      return;
    }

    // save to DB
    DbUtils.getDb().removeSavedSearchKeywordsListItem(currentKeywords);

    // update UI
    keywordsList.remove(index);
    adapter.notifyDataSetChanged();

    if (keywordsList.isEmpty())
      hide();
    else
      update();
  }

  private void selectItem(int position) {
    String selectKeywords = keywordsList.get(position);
    searchView.setQuery(selectKeywords, true);
    hide();
    if (listener != null) listener.onSavedSearchKeywordsListItemClick(selectKeywords);
  }

  public boolean isShowing() {
    return isShowing;
  }

  public void show() {
    boolean ignoreIfEmpty = true;
    show(ignoreIfEmpty);
  }

  public void show(boolean ignoreIfEmpty) {
    if (!isShowing) {
      if (ignoreIfEmpty && keywordsList.isEmpty() && isCurrentKeywordsEmpty()) return;

      isShowing = true;
      dialog.show();
    }

    update();
  }

  public void hide() {
    if (isShowing) {
      isShowing = false;
      dialog.hide();
    }
  }

  public void update() {
    if (!isShowing) return;

    if (isCurrentKeywordsEmpty()) {
      addButton.setVisibility(View.GONE);
      delButton.setVisibility(View.GONE);
    }
    else if (isSaved()) {
      addButton.setVisibility(View.GONE);
      delButton.setVisibility(View.VISIBLE);
    }
    else {
      addButton.setVisibility(View.VISIBLE);
      delButton.setVisibility(View.GONE);
    }
  }

  public void release() {
    if (dialog.isShowing())
      dialog.dismiss();

    this.searchView   = null;
    this.keywordsList = null;
    this.adapter      = null;
    this.dialog       = null;
    this.addButton    = null;
    this.delButton    = null;
    this.isShowing    = false;
  }
}
