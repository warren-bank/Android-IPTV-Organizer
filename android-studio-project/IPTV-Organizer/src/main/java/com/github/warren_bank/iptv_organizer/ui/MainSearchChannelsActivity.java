package com.github.warren_bank.iptv_organizer.ui;

import com.github.warren_bank.iptv_organizer.R;
import com.github.warren_bank.iptv_organizer.common.Constants;
import com.github.warren_bank.iptv_organizer.data.model.ChannelListItem;
import com.github.warren_bank.iptv_organizer.ui.ChannelsActivity;
import com.github.warren_bank.iptv_organizer.ui.EpgActivity;
import com.github.warren_bank.iptv_organizer.ui.ExitActivity;
import com.github.warren_bank.iptv_organizer.ui.SettingsActivity;
import com.github.warren_bank.iptv_organizer.ui.dialog.SavedSearchKeywordsListDialog;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;
import com.github.warren_bank.iptv_organizer.utils.SettingsUtils;

import com.github.warren_bank.filterablerecyclerview.FilterableListItem;
import com.github.warren_bank.filterablerecyclerview.FilterableListItemOnClickListener;
import com.github.warren_bank.filterablerecyclerview.FilterableViewHolder;
import com.github.warren_bank.filterablerecyclerview.FilterableAdapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainSearchChannelsActivity extends AppCompatActivity implements FilterableListItemOnClickListener {

  public static void open(Context context) {
    Intent intent = new Intent(context, MainSearchChannelsActivity.class);
    context.startActivity(intent);
  }

  // ---------------------------------------------------------------------------------------------
  // RecyclerView:
  // ---------------------------------------------------------------------------------------------

  private List<FilterableListItem>  channelList;
  private FilterableAdapter         recyclerFilterableAdapter;
  private RecyclerView              recyclerView;

  private SearchView                searchView;

  public class ChannelsFilterableViewHolder extends FilterableViewHolder {
    private TextView text1;

    public ChannelsFilterableViewHolder(
      View view,
      List<FilterableListItem> filteredList,
      FilterableListItemOnClickListener listener
    ) {
      super(view, filteredList, listener);
    }

    @Override
    public void onCreate(View view) {
      text1 = view.findViewById(android.R.id.text1);
    }

    @Override
    public void onUpdate(FilterableListItem filterableListItem) {
      ChannelListItem channel = (ChannelListItem) filterableListItem;

      text1.setText(channel.name);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Dialogs:
  // ---------------------------------------------------------------------------------------------

  private SavedSearchKeywordsListDialog savedSearchDialog;

  // ---------------------------------------------------------------------------------------------
  // Background Search Thread Management:
  // ---------------------------------------------------------------------------------------------

  private static int     SEARCH_INPUT_DEBOUNCE_INTERVAL_MS     = 2000;
  private static int     SEARCH_RESULTS_MAX_COUNT              = 100;
  private static boolean SEARCH_RESULTS_REMOVE_DUPLICATE_NAMES = true;

  private static class SearchRunnable implements Runnable {
    private MainSearchChannelsActivity activity;
    private String constraint;

    SearchRunnable(MainSearchChannelsActivity activity, String constraint) {
      this.activity   = activity;
      this.constraint = constraint;
    }

    @Override
    public void run() {
      // Read channels from DB on a background thread
      new Thread(() -> activity.performSearch(constraint)).start();
    }
  }

  private final Handler searchHandler = new Handler(Looper.getMainLooper());

  private void startSearch(String constraint) {
    // cancel pending post
    searchHandler.removeCallbacksAndMessages(null);

    // reschedule new pending post
    searchHandler.postDelayed(
      new SearchRunnable(MainSearchChannelsActivity.this, constraint),
      SEARCH_INPUT_DEBOUNCE_INTERVAL_MS
    );
  }

  // ---------------------------------------------------------------------------------------------
  // Lifecycle Events:
  // ---------------------------------------------------------------------------------------------

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_channels);

    refreshList(null);
    initToolbar();
    initRecyclerView();
    readSettings();
  }

  @Override
  protected void onResume() {
    super.onResume();

    initSavedSearchDialog();
    readSettings();
  }

  @Override
  protected void onPause() {
    destroySavedSearchDialog();

    super.onPause();
  }

  // ---------------------------------------------------------------------------------------------
  // ActionBar:
  // ---------------------------------------------------------------------------------------------

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);

    searchView = (SearchView) menu.findItem(R.id.main_menuitem_search).getActionView();
    initSavedSearchDialog(true);
    initSearch();

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    switch(menuItem.getItemId()) {

      case R.id.main_menuitem_search: {
        return true;
      }

      case R.id.main_menuitem_channels: {
        ChannelsActivity.open(MainSearchChannelsActivity.this);
        return true;
      }

      case R.id.main_menuitem_epg: {
        EpgActivity.open(MainSearchChannelsActivity.this);
        return true;
      }

      case R.id.main_menuitem_settings: {
        SettingsActivity.open(MainSearchChannelsActivity.this);
        return true;
      }

      case R.id.main_menuitem_exit: {
        ExitActivity.open(MainSearchChannelsActivity.this);
        return true;
      }

      default: {
        return super.onOptionsItemSelected(menuItem);
      }
    }
  }

  @Override
  public void onBackPressed() {
    if ((savedSearchDialog != null) && savedSearchDialog.isShowing()) {
      savedSearchDialog.hide();
      return;
    }
    if (!searchView.isIconified()) {
      searchView.setIconified(true);
      return;
    }
    super.onBackPressed();
  }

  // ---------------------------------------------------------------------------------------------
  // internal:
  // ---------------------------------------------------------------------------------------------

  protected void performSearch(String constraint) {
    try {
      final List<ChannelListItem> newList;

      if (TextUtils.isEmpty(constraint)) {
        newList = null;
      }
      else {
        String[] keywords = constraint.split(Constants.SEARCH_KEYWORD_ARRAY_SPLIT_REGEX);

        newList = DbUtils.getDb().searchM3u(keywords, SEARCH_RESULTS_MAX_COUNT, SEARCH_RESULTS_REMOVE_DUPLICATE_NAMES);
      }

      if ((newList == null) && channelList.isEmpty())
        return;

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          refreshList(newList);
        }
      });
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
  }

  private void refreshList(List<ChannelListItem> newList) {
    if (newList == null)
      newList = new ArrayList<ChannelListItem>();

    if (channelList == null) {
      channelList = castList(newList);
    }
    else {
      channelList.clear();
      channelList.addAll(castList(newList));
    }

    if (recyclerFilterableAdapter != null)
      recyclerFilterableAdapter.refresh();
  }

  private List<FilterableListItem> castList(List<ChannelListItem> list) {
    return (List<FilterableListItem>)(List<?>) list;
  }

  private void initToolbar() {
    Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);

    setSupportActionBar(toolbar);
    setToolbarTitle();

    toolbar.setNavigationIcon(R.drawable.arrow_back);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onBackPressed();
      }
    });
  }

  private void setToolbarTitle() {
    try {
      String title = getString(R.string.activity_main);
      getSupportActionBar().setTitle(title);
    }
    catch(Exception ignored) {}
  }

  private void initRecyclerView() {
    recyclerFilterableAdapter  = new FilterableAdapter(
      R.layout.one_line_list_item,
      channelList,
      MainSearchChannelsActivity.this,
      ChannelsFilterableViewHolder.class,
      MainSearchChannelsActivity.class,
      MainSearchChannelsActivity.this,
      Constants.SEARCH_KEYWORD_ARRAY_SPLIT_REGEX,
      Constants.SEARCH_KEYWORD_MIN_LENGTH,
      Constants.SEARCH_KEYWORD_CASE_SENSITIVE
    );

    recyclerView = findViewById(R.id.rv_channels);
    recyclerView.setLayoutManager(new LinearLayoutManager(MainSearchChannelsActivity.this));
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(recyclerFilterableAdapter);

    // add divider between list items
    recyclerView.addItemDecoration(
      new DividerItemDecoration(MainSearchChannelsActivity.this, DividerItemDecoration.VERTICAL)
    );
  }

  private void initSavedSearchDialog() {
    initSavedSearchDialog(false);
  }

  private void initSavedSearchDialog(boolean force) {
    if (force)
      destroySavedSearchDialog();

    if ((savedSearchDialog == null) && (searchView != null))
      savedSearchDialog = new SavedSearchKeywordsListDialog(MainSearchChannelsActivity.this, searchView, R.drawable.bookmark);
  }

  private void destroySavedSearchDialog() {
    if (savedSearchDialog != null) {
      savedSearchDialog.release();
      savedSearchDialog = null;
    }
  }

  private void initSearch() {
    searchView.setMaxWidth(Integer.MAX_VALUE);

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String constraint) {
        startSearch(constraint);
        if (savedSearchDialog != null) savedSearchDialog.update();
        return false;
      }

      @Override
      public boolean onQueryTextChange(String constraint) {
        startSearch(constraint);
        if (savedSearchDialog != null) savedSearchDialog.update();
        return false;
      }
    });
  }

  private void readSettings() {
    MainSearchChannelsActivity.SEARCH_INPUT_DEBOUNCE_INTERVAL_MS     = SettingsUtils.getSearchInputDebounceIntervalMs(        MainSearchChannelsActivity.this);
    MainSearchChannelsActivity.SEARCH_RESULTS_MAX_COUNT              = SettingsUtils.getMaxCountOfSearchResults(              MainSearchChannelsActivity.this);
    MainSearchChannelsActivity.SEARCH_RESULTS_REMOVE_DUPLICATE_NAMES = SettingsUtils.getRemoveDuplicateNamesFromSearchResults(MainSearchChannelsActivity.this);
  }

  private void viewChannel(ChannelListItem channel) {
    String channelUrl = DbUtils.resolveM3uMediaUrl(channel.media_url);
    if (channelUrl == null) return;

    try {
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(channelUrl));
      startActivity(intent);
    }
    catch(Exception e) {
      Toast.makeText(MainSearchChannelsActivity.this, R.string.toast_error_no_app_found, Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onFilterableListItemClick(FilterableListItem item) {
    ChannelListItem channel = (ChannelListItem) item;

    viewChannel(channel);
  }
}
