package com.github.warren_bank.iptv_organizer.ui;

import com.github.warren_bank.iptv_organizer.R;
import com.github.warren_bank.iptv_organizer.common.Constants;
import com.github.warren_bank.iptv_organizer.data.model.ChannelListItem;
import com.github.warren_bank.iptv_organizer.ui.EpgActivity;
import com.github.warren_bank.iptv_organizer.ui.SettingsActivity;
import com.github.warren_bank.iptv_organizer.ui.dialog.DataProgressDialog;
import com.github.warren_bank.iptv_organizer.ui.dialog.SavedSearchKeywordsListDialog;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;
import com.github.warren_bank.iptv_organizer.utils.ImportUtils;
import com.github.warren_bank.iptv_organizer.utils.SettingsUtils;

import com.github.warren_bank.filterablerecyclerview.Filter;
import com.github.warren_bank.filterablerecyclerview.FilterableListItem;
import com.github.warren_bank.filterablerecyclerview.FilterableListItemOnClickListener;
import com.github.warren_bank.filterablerecyclerview.FilterableViewHolder;
import com.github.warren_bank.filterablerecyclerview.FilterableAdapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChannelsActivity extends AppCompatActivity implements FilterableListItemOnClickListener {

  public static void open(Context context) {
    Intent intent = new Intent(context, ChannelsActivity.class);
    context.startActivity(intent);
  }

  // ---------------------------------------------------------------------------------------------
  // Data Structures:
  // ---------------------------------------------------------------------------------------------

  private static enum SORT_OPTION { SEQUENTIAL, ALPHABETIC }

  private SORT_OPTION sort_order;

  // ---------------------------------------------------------------------------------------------
  // RecyclerView:
  // ---------------------------------------------------------------------------------------------

  private List<FilterableListItem>  unfilteredList;
  private FilterableAdapter         recyclerFilterableAdapter;
  private RecyclerView              recyclerView;

  private Filter                    searchFilter;
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

  private DataProgressDialog            dataProgressDialog;
  private SavedSearchKeywordsListDialog savedSearchDialog;

  // ---------------------------------------------------------------------------------------------
  // Lifecycle Events:
  // ---------------------------------------------------------------------------------------------

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_channels);

    unfilteredList = new ArrayList<FilterableListItem>();

    initToolbar();
    initRecyclerView();
    initSort();

    final String urlText = getNewIntentDataUri(getIntent());

    if (urlText != null) {
      importNewIntentDataUri(urlText);
    }
    else {
      final DataProgressDialog listener = new DataProgressDialog(ChannelsActivity.this, R.string.loading, getString(R.string.activity_channels));
      dataProgressDialog = listener;

      // Read channels from DB on a background thread
      new Thread(() -> initList(listener)).start();
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    final String urlText = getNewIntentDataUri(getIntent());

    if (urlText != null) {
      importNewIntentDataUri(urlText);
    }
  }

  private String getNewIntentDataUri(Intent intent) {
    if (intent == null) return null;

    Uri data = intent.getData();
    if (data == null) return null;

    String urlText = data.toString().trim();
    if (urlText.isEmpty()) return null;

    return urlText;
  }

  private void importNewIntentDataUri(final String urlText) {
    final DataProgressDialog listener = new DataProgressDialog(ChannelsActivity.this);
    dataProgressDialog = listener;

    // Do network on a background thread
    new Thread(() -> openUrlAsStream(urlText, listener)).start();
  }

  @Override
  protected void onResume() {
    super.onResume();

    initSavedSearchDialog();

    if (dataProgressDialog != null) dataProgressDialog.resume(ChannelsActivity.this);
  }

  @Override
  protected void onPause() {
    if (dataProgressDialog != null) dataProgressDialog.pause();

    if (savedSearchDialog != null) {
      savedSearchDialog.release();
      savedSearchDialog = null;
    }

    super.onPause();
  }

  // ---------------------------------------------------------------------------------------------
  // ActionBar:
  // ---------------------------------------------------------------------------------------------

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_channels, menu);
    boolean isVisible;

    isVisible = (Build.VERSION.SDK_INT >= 19);
    menu.findItem(R.id.channels_menuitem_import_m3u_file).setVisible(isVisible);

    isVisible = !unfilteredList.isEmpty();
    menu.findItem(R.id.channels_menuitem_search).setVisible(isVisible);
    menu.findItem(R.id.channels_menuitem_sort_sequential).setVisible(isVisible);
    menu.findItem(R.id.channels_menuitem_sort_alphabetic).setVisible(isVisible);

    searchView = (SearchView) menu.findItem(R.id.channels_menuitem_search).getActionView();
    initSavedSearchDialog();
    initSearch();

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    switch(menuItem.getItemId()) {

      case R.id.channels_menuitem_epg: {
        EpgActivity.open(ChannelsActivity.this);
        return true;
      }

      case R.id.channels_menuitem_import_m3u_url: {
        showUrlDialog();
        return true;
      }

      case R.id.channels_menuitem_import_m3u_file: {
        showFileChooser();
        return true;
      }

      case R.id.channels_menuitem_search: {
        return true;
      }

      case R.id.channels_menuitem_sort_sequential: {
        sort_order = SORT_OPTION.SEQUENTIAL;  // the sequential order in which channels naturally occur in the M3U file
        sortRecyclerView();
        return true;
      }

      case R.id.channels_menuitem_sort_alphabetic: {
        sort_order = SORT_OPTION.ALPHABETIC;
        sortRecyclerView();
        return true;
      }

      case R.id.channels_menuitem_settings: {
        SettingsActivity.open(ChannelsActivity.this);
        return true;
      }

      case R.id.channels_menuitem_exit: {
        ExitActivity.open(ChannelsActivity.this);
        return true;
      }

      default: {
        return super.onOptionsItemSelected(menuItem);
      }
    }
  }

  @Override
  public void onBackPressed() {
    if (dataProgressDialog != null) {
      return;
    }
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
  // channels_menuitem_import_m3u_url:
  // ---------------------------------------------------------------------------------------------

  private void showUrlDialog() {
    final EditText input = new EditText(ChannelsActivity.this);
    input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
    input.setHint(R.string.pref_default_m3u_url_hint);
    input.setText(
      DbUtils.getDefaultM3uUrlPreference(ChannelsActivity.this),
      TextView.BufferType.NORMAL
    );

    new AlertDialog.Builder(ChannelsActivity.this)
        .setTitle(R.string.channels_dialog_import_m3u_url_title)
        .setView(input)
        .setPositiveButton(R.string.channels_dialog_import_m3u_url_button_positive, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            final String urlText = input.getText().toString().trim();
            if (urlText.isEmpty()) return;

            final DataProgressDialog listener = new DataProgressDialog(ChannelsActivity.this);
            dataProgressDialog = listener;

            // Do network on a background thread
            new Thread(() -> openUrlAsStream(urlText, listener)).start();
          }
        })
        .setNegativeButton(R.string.channels_dialog_import_m3u_url_button_negative, null)
        .show();
  }

  private void openUrlAsStream(String urlText, DataProgressDialog listener) {
    HttpURLConnection conn = null;
    InputStream inputStream = null;

    try {
      URL url = new URL(urlText);
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(15000);
      conn.setReadTimeout(15000);

      int code = conn.getResponseCode();
      if (code < 200 || code >= 300) {
        throw new Exception("HTTP " + code);
      }

      inputStream = conn.getInputStream();
      importM3uFromStream(inputStream, listener);
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    } finally {
      try {
        if (inputStream != null) inputStream.close();
      } catch (Exception ignored) {}
      if (conn != null) conn.disconnect();
      listener.dismiss(true);
      dataProgressDialog = null;
    }
  }

  // ---------------------------------------------------------------------------------------------
  // channels_menuitem_import_m3u_file:
  // ---------------------------------------------------------------------------------------------

  private static int FILE_CHOOSER_REQUEST_CODE = 1;

  private void showFileChooser() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("*/*");

    // https://android.googlesource.com/platform/external/mime-support/+/9817b71a54a2ee8b691c1dfa937c0f9b16b3473c/mime.types
    // https://android.googlesource.com/platform/frameworks/base/+/4fa4de177280/mime/java-res/android.mime.types
    String[] mimeTypes = {"application/vnd.apple.mpegurl", "application/mpegurl", "audio/mpegurl", "video/mpegurl", "application/x-mpegurl", "audio/x-mpegurl", "video/x-mpegurl", "application/x-mpegURL", "audio/x-mpegURL", "video/x-mpegURL"};
    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

    startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if ((requestCode == FILE_CHOOSER_REQUEST_CODE) && (resultCode == RESULT_OK)) {
      final Uri uri = data.getData();
      if (uri == null) return;

      final DataProgressDialog listener = new DataProgressDialog(ChannelsActivity.this);
      dataProgressDialog = listener;

      // Read file on a background thread
      new Thread(() -> openFileAsStream(uri, listener)).start();
    }
  }

  private void openFileAsStream(Uri uri, DataProgressDialog listener) {
    InputStream inputStream = null;

    try {
      inputStream = getContentResolver().openInputStream(uri);
      importM3uFromStream(inputStream, listener);
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    } finally {
      try {
        if (inputStream != null) inputStream.close();
      } catch (Exception ignored) {}
      listener.dismiss(true);
      dataProgressDialog = null;
    }
  }

  // ---------------------------------------------------------------------------------------------
  // internal:
  // ---------------------------------------------------------------------------------------------

  private void importM3uFromStream(InputStream inputStream, DataProgressDialog listener) throws Exception {
    final boolean appendList = SettingsUtils.getAppendM3uPlaylists(ChannelsActivity.this);

    int firstPosition = appendList
      ? (unfilteredList.size() + 1)
      : 1;

    final List<ChannelListItem> newList = ImportUtils.importM3u(inputStream, appendList, firstPosition, listener);

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        refreshList(newList, appendList);
      }
    });
  }

  private void initList(DataProgressDialog listener) {
    try {
      final List<ChannelListItem> newList = DbUtils.getDb().getM3u(listener);

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          refreshList(newList, false);
        }
      });
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    } finally {
      listener.dismiss(true);
      dataProgressDialog = null;
    }
  }

  private void refreshList(List<ChannelListItem> newList, boolean appendList) {
    if (newList == null) {
      // fallback: this should never happen; channel list is always read in a background thread and passed as input.
      newList = DbUtils.getDb().getM3u();
    }

    if (unfilteredList == null) {
      // fallback: this should never happen; an empty list is initialized at creation and passed to the RecyclerView adapter.
      unfilteredList = castList(newList);
    }
    else {
      if (!appendList)
        unfilteredList.clear();

      unfilteredList.addAll(castList(newList));
    }

    if (recyclerFilterableAdapter != null)
      recyclerFilterableAdapter.refresh();

    if (searchView != null)
      invalidateOptionsMenu();
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
      String title = getString(R.string.activity_channels);
      getSupportActionBar().setTitle(title);
    }
    catch(Exception ignored) {}
  }

  private void initRecyclerView() {
    recyclerFilterableAdapter  = new FilterableAdapter(
      R.layout.one_line_list_item,
      unfilteredList,
      ChannelsActivity.this,
      ChannelsFilterableViewHolder.class,
      ChannelsActivity.class,
      ChannelsActivity.this,
      Constants.SEARCH_KEYWORD_ARRAY_SPLIT_REGEX,
      Constants.SEARCH_KEYWORD_MIN_LENGTH,
      Constants.SEARCH_KEYWORD_CASE_SENSITIVE
    );

    recyclerView = findViewById(R.id.rv_channels);
    recyclerView.setLayoutManager(new LinearLayoutManager(ChannelsActivity.this));
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(recyclerFilterableAdapter);

    // add divider between list items
    recyclerView.addItemDecoration(
      new DividerItemDecoration(ChannelsActivity.this, DividerItemDecoration.VERTICAL)
    );

    searchFilter = recyclerFilterableAdapter.getFilter();
  }

  private void initSort() {
    // order immediately after data is extracted from file
    sort_order = SORT_OPTION.SEQUENTIAL;
  }

  private void initSavedSearchDialog() {
    if ((savedSearchDialog == null) && (searchView != null))
      savedSearchDialog = new SavedSearchKeywordsListDialog(ChannelsActivity.this, searchView, R.drawable.bookmark);
  }

  private void initSearch() {
    searchView.setMaxWidth(Integer.MAX_VALUE);

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String constraint) {
        searchFilter.query(constraint);
        if (savedSearchDialog != null) savedSearchDialog.update();
        return false;
      }

      @Override
      public boolean onQueryTextChange(String constraint) {
        searchFilter.query(constraint);
        if (savedSearchDialog != null) savedSearchDialog.update();
        return false;
      }
    });
  }

  private void sortRecyclerView() {
    final Comparator comparator;

    switch(sort_order) {
      case SEQUENTIAL:
        comparator = ChannelListItem.sequentialOrderComparator;
        break;
      case ALPHABETIC:
        comparator = ChannelListItem.alphabeticOrderComparator;
        break;
      default:
        comparator = null;
        break;
    }

    if (comparator == null) return;

    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(final Void ... params) {
        Collections.sort(unfilteredList, comparator);
        return null;
      }
 
      @Override
      protected void onPostExecute(final Void result) {
        recyclerFilterableAdapter.refresh();
      }
    }.execute();
  }

  private void viewChannel(ChannelListItem channel) {
    String channelUrl = DbUtils.resolveM3uMediaUrl(channel.media_url);
    if (channelUrl == null) return;

    try {
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(channelUrl));
      startActivity(intent);
    }
    catch(Exception e) {
      Toast.makeText(ChannelsActivity.this, R.string.toast_error_no_app_found, Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onFilterableListItemClick(FilterableListItem item) {
    ChannelListItem channel = (ChannelListItem) item;

    viewChannel(channel);
  }
}
