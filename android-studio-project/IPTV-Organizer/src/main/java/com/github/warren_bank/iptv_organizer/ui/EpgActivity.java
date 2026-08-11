package com.github.warren_bank.iptv_organizer.ui;

import com.github.warren_bank.iptv_organizer.R;
import com.github.warren_bank.iptv_organizer.common.Constants;
import com.github.warren_bank.iptv_organizer.data.model.EPGDataImpl;
import com.github.warren_bank.iptv_organizer.ui.ChannelsActivity;
import com.github.warren_bank.iptv_organizer.ui.ExitActivity;
import com.github.warren_bank.iptv_organizer.ui.MainSearchChannelsActivity;
import com.github.warren_bank.iptv_organizer.ui.SettingsActivity;
import com.github.warren_bank.iptv_organizer.ui.dialog.DataProgressDialog;
import com.github.warren_bank.iptv_organizer.ui.dialog.SavedSearchKeywordsListDialog;
import com.github.warren_bank.iptv_organizer.utils.DateUtils;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;
import com.github.warren_bank.iptv_organizer.utils.ImportUtils;
import com.github.warren_bank.iptv_organizer.utils.SettingsUtils;

import se.kmdev.tvepg.epg.EPG;
import se.kmdev.tvepg.epg.EPGClickListener;
import se.kmdev.tvepg.epg.domain.EPGChannel;
import se.kmdev.tvepg.epg.domain.EPGEvent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class EpgActivity extends AppCompatActivity {

  public static void open(Context context) {
    Intent intent = new Intent(context, EpgActivity.class);
    context.startActivity(intent);
  }

  private EPG         epgView;
  private EPGDataImpl epgData;
  private SearchView  searchView;

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
    setContentView(R.layout.activity_epg);

    epgData = new EPGDataImpl(null);

    initToolbar();
    initEpgView();

    final String urlText = getNewIntentDataUri(getIntent());

    if (urlText != null) {
      importNewIntentDataUri(urlText);
    }
    else {
      readChannelsFromDb();
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
    final DataProgressDialog listener = new DataProgressDialog(EpgActivity.this);
    dataProgressDialog = listener;

    // Do network on a background thread
    new Thread(() -> openUrlAsStream(urlText, listener)).start();
  }

  private void readChannelsFromDb() {
    final DataProgressDialog listener = new DataProgressDialog(EpgActivity.this, R.string.loading, getString(R.string.activity_epg));
    dataProgressDialog = listener;

    // Read channels from DB on a background thread
    new Thread(() -> initEpgData(listener)).start();
  }

  @Override
  protected void onResume() {
    super.onResume();

    initSavedSearchDialog();

    if (dataProgressDialog != null) dataProgressDialog.resume(EpgActivity.this);
  }

  @Override
  protected void onPause() {
    if (dataProgressDialog != null) dataProgressDialog.pause();

    destroySavedSearchDialog();

    super.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (epgView != null)
      epgView.clearEPGImageCache();
  }

  // ---------------------------------------------------------------------------------------------
  // ActionBar:
  // ---------------------------------------------------------------------------------------------

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_epg, menu);
    boolean isVisible;

    isVisible = (Build.VERSION.SDK_INT >= 19);
    menu.findItem(R.id.epg_menuitem_import_xmltv_file).setVisible(isVisible);

    isVisible = epgData.hasData();
    menu.findItem(R.id.epg_menuitem_search).setVisible(isVisible);

    searchView = (SearchView) menu.findItem(R.id.epg_menuitem_search).getActionView();
    initSavedSearchDialog(true);
    initSearch();

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    switch(menuItem.getItemId()) {

      case R.id.epg_menuitem_main: {
        MainSearchChannelsActivity.open(EpgActivity.this);
        return true;
      }

      case R.id.epg_menuitem_channels: {
        ChannelsActivity.open(EpgActivity.this);
        return true;
      }

      case R.id.epg_menuitem_import_xmltv_url: {
        showUrlDialog();
        return true;
      }

      case R.id.epg_menuitem_import_xmltv_file: {
        showFileChooser();
        return true;
      }

      case R.id.epg_menuitem_search: {
        return true;
      }

      case R.id.epg_menuitem_settings: {
        SettingsActivity.open(EpgActivity.this);
        return true;
      }

      case R.id.epg_menuitem_exit: {
        ExitActivity.open(EpgActivity.this);
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
      if (searchView.hasFocus())
        searchView.clearFocus();
      else
        searchView.setIconified(true);
      return;
    }
    super.onBackPressed();
  }

  // ---------------------------------------------------------------------------------------------
  // epg_menuitem_import_xmltv_url:
  // ---------------------------------------------------------------------------------------------

  private void showUrlDialog() {
    final EditText input = new EditText(EpgActivity.this);
    input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
    input.setHint(R.string.pref_default_xmltv_url_hint);
    input.setText(
      DbUtils.getDefaultXmltvEpgUrlPreference(EpgActivity.this),
      TextView.BufferType.NORMAL
    );

    new AlertDialog.Builder(EpgActivity.this)
        .setTitle(R.string.epg_dialog_import_xmltv_url_title)
        .setView(input)
        .setPositiveButton(R.string.epg_dialog_import_xmltv_url_button_positive, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            final String urlText = input.getText().toString().trim();
            if (urlText.isEmpty()) return;

            dialog.dismiss();

            final DataProgressDialog listener = new DataProgressDialog(EpgActivity.this);
            dataProgressDialog = listener;

            // Do network on a background thread
            new Thread(() -> openUrlAsStream(urlText, listener)).start();
          }
        })
        .setNegativeButton(R.string.epg_dialog_import_xmltv_url_button_negative, null)
        .show();
  }

  private void openUrlAsStream(String urlText, DataProgressDialog listener) {
    List<HttpURLConnection> connList  = new ArrayList<HttpURLConnection>();
    List<InputStream> inputStreamList = new ArrayList<InputStream>();

    boolean parseListInXmltvUrl = SettingsUtils.getParseListInXmltvEpgUrl(EpgActivity.this);
    boolean importAllListValues = parseListInXmltvUrl && SettingsUtils.getImportAllListValuesInXmltvEpgUrl(EpgActivity.this);

    try {
      if (urlText == null)
        throw new Exception("Invalid EPG URL");

      urlText = urlText.trim();

      String[] allUrls = parseListInXmltvUrl
        ? urlText.split(Constants.SEARCH_KEYWORD_ARRAY_SPLIT_REGEX)
        : new String[]{urlText};

      for (String nextUrl : allUrls) {
        if (nextUrl.isEmpty() || !"http".equals(nextUrl.substring(0, 4).toLowerCase())) continue;

        HttpURLConnection conn = null;
        InputStream inputStream = null;

        try {
          URL url = new URL(nextUrl);
          conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("GET");
          conn.setFollowRedirects(true);
          conn.setConnectTimeout(15000);
          conn.setReadTimeout(15000);

          // note: 3xx redirects won't surface; they are automatically followed by HttpURLConnection.
          int code = conn.getResponseCode();
          if (code < 200 || code >= 300)
            throw new Exception("HTTP " + code);

          boolean isGzip = urlText.substring(urlText.length() - 3).toLowerCase().equals(".gz");
          if (!isGzip) {
            // check HTTP response header: content-type
            String contentType = conn.getContentType();
            if (contentType != null) {
              contentType = contentType.toLowerCase();
              isGzip = (
                contentType.equals("application/gzip")   || contentType.startsWith("application/gzip;") ||
                contentType.equals("application/x-gzip") || contentType.startsWith("application/x-gzip;")
              );
            }
          }

          inputStream = conn.getInputStream();

          if (isGzip)
            inputStream = (InputStream) new GZIPInputStream(inputStream);

          connList.add(conn);
          inputStreamList.add(inputStream);

          if (importAllListValues)
            continue;
          else
            break;
        }
        catch(Exception e) {
          try {
            if (inputStream != null) inputStream.close();
          } catch (Exception ignored) {}
          if (conn != null) conn.disconnect();
          continue;
        }
      }

      if (inputStreamList.isEmpty())
        throw new Exception("Unable to connect to any EPG URL");

      importXmlTvFromStream(inputStreamList, listener);
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    } finally {
      for (InputStream inputStream : inputStreamList) {
        try {
          if (inputStream != null) inputStream.close();
        } catch (Exception ignored) {}
      }
      for (HttpURLConnection conn : connList) {
        if (conn != null) conn.disconnect();
      }
      listener.dismiss(true);
      dataProgressDialog = null;
    }
  }

  // ---------------------------------------------------------------------------------------------
  // epg_menuitem_import_xmltv_file:
  // ---------------------------------------------------------------------------------------------

  private static int FILE_CHOOSER_REQUEST_CODE = 1;

  private void showFileChooser() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("*/*");

    // https://android.googlesource.com/platform/external/mime-support/+/9817b71a54a2ee8b691c1dfa937c0f9b16b3473c/mime.types
    // https://android.googlesource.com/platform/frameworks/base/+/4fa4de177280/mime/java-res/android.mime.types
    String[] mimeTypes = {"application/xml", "application/xmltv", "text/xml", "text/xmltv", "application/gzip", "application/x-gzip", "application/octet-stream"};
    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

    startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if ((requestCode == FILE_CHOOSER_REQUEST_CODE) && (resultCode == RESULT_OK)) {
      final Uri uri = data.getData();
      if (uri == null) return;

      final DataProgressDialog listener = new DataProgressDialog(EpgActivity.this);
      dataProgressDialog = listener;

      // Read file on a background thread
      new Thread(() -> openFileAsStream(uri, listener)).start();
    }
  }

  private void openFileAsStream(Uri uri, DataProgressDialog listener) {
    InputStream inputStream = null;

    try {
      String uriText = uri.getPath();
      boolean isGzip = uriText.substring(uriText.length() - 3).toLowerCase().equals(".gz");

      inputStream = getContentResolver().openInputStream(uri);

      if (isGzip)
        inputStream = (InputStream) new GZIPInputStream(inputStream);

      importXmlTvFromStream(inputStream, listener);
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

  private void importXmlTvFromStream(InputStream inputStream, DataProgressDialog listener) throws Exception {
    final EPGDataImpl newEpgData = new EPGDataImpl(
      ImportUtils.importXmlTv(inputStream, listener)
    );

    refreshEpgOnUiThread(newEpgData);
  }

  private void importXmlTvFromStream(List<InputStream> inputStreamList, DataProgressDialog listener) throws Exception {
    final EPGDataImpl newEpgData = new EPGDataImpl(
      ImportUtils.importXmlTv(inputStreamList, listener)
    );

    refreshEpgOnUiThread(newEpgData);
  }

  private void refreshEpgOnUiThread(final EPGDataImpl newEpgData) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        refreshEpg(newEpgData);
      }
    });
  }

  private void initEpgView() {
    epgView = (EPG) findViewById(R.id.epg);

    epgView.setEPGClickListener(new EPGClickListener() {
      @Override
      public void onChannelClicked(int channelPosition, EPGChannel epgChannel) {
        if (epgChannel == null) return;

        String channelUrl = DbUtils.getM3uMediaUrlForEpgChannel(epgChannel);
        if (channelUrl == null) return;

        try {
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(channelUrl));
          startActivity(intent);
        }
        catch(Exception e) {
          Toast.makeText(EpgActivity.this, R.string.toast_error_no_app_found, Toast.LENGTH_SHORT).show();
        }
      }

      @Override
      public void onEventClicked(int channelPosition, int programPosition, EPGEvent epgEvent) {
        if (epgEvent == null) return;

        EPGChannel epgChannel = epgData.getChannel(channelPosition);

        String   channelName    = (epgChannel != null) ? epgChannel.getName() : null;
        String   programTitle   = epgEvent.getTitle();
        String   programDescr   = epgEvent.getDescription();
        long     programTsStart = epgEvent.hasStart() ? epgEvent.getStart() : -1L;
        long     programTsEnd   = epgEvent.hasEnd()   ? epgEvent.getEnd()   : -1L;
        String[] programTsRange = DateUtils.formatTimestampRange(programTsStart, programTsEnd, null);

        if (TextUtils.isEmpty(channelName))  channelName  = null;
        if (TextUtils.isEmpty(programTitle)) programTitle = null;
        if (TextUtils.isEmpty(programDescr)) programDescr = null;

        StringBuffer programTsBuf = new StringBuffer();
        if (programTsRange[0] != null) {
          programTsBuf.append(programTsRange[0]);

          if (programTsRange[1] != null) {
            programTsBuf.append(" -");

            programTsBuf.append(
              programTsRange[2].startsWith("yyyy")
                ? "\n"
                : " "
            );
            programTsBuf.append(programTsRange[1]);
          }
        }
        String programTs = (programTsBuf.length() == 0)
          ? null
          : programTsBuf.toString();

        ArrayList<String> messageList = new ArrayList<String>();
        if (programTitle != null)
          messageList.add(programTitle);
        if (programTs != null)
          messageList.add(programTs);
        if (programDescr != null)
          messageList.add(programDescr);

        if (messageList.isEmpty()) return;

        String message = TextUtils.join("\n\n", messageList);

        new AlertDialog.Builder(EpgActivity.this)
          .setTitle(channelName)
          .setMessage(message)
          .setPositiveButton(R.string.ok, null)
          .show();
      }

      @Override
      public void onResetButtonClicked() {
        epgView.recalculateAndRedraw(true);
      }
    });
  }

  private void initEpgData(DataProgressDialog listener) {
    try {
      final EPGDataImpl newEpgData = DbUtils.getDb().getEpgData(listener);

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          refreshEpg(newEpgData);
        }
      });
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    } finally {
      listener.dismiss(true);
      dataProgressDialog = null;
    }
  }

  private void refreshEpg(EPGDataImpl newEpgData) {
    boolean animate = (epgData != null);

    // fallback: this should never happen; EPG data is always read in a background thread and passed as input.
    epgData = (newEpgData == null)
      ? DbUtils.getDb().getEpgData()
      : newEpgData;

    epgView.setEPGData(epgData);
    epgView.recalculateAndRedraw(animate);

    if (searchView != null)
      invalidateOptionsMenu();
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
      String title = getString(R.string.activity_epg);
      getSupportActionBar().setTitle(title);
    }
    catch(Exception ignored) {}
  }

  private void initSavedSearchDialog() {
    initSavedSearchDialog(false);
  }

  private void initSavedSearchDialog(boolean force) {
    if (force)
      destroySavedSearchDialog();

    if ((savedSearchDialog == null) && (searchView != null))
      savedSearchDialog = new SavedSearchKeywordsListDialog(EpgActivity.this, searchView, R.drawable.bookmark);
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
        epgData.filterChannels(constraint, Constants.SEARCH_KEYWORD_ARRAY_SPLIT_REGEX, Constants.SEARCH_KEYWORD_MIN_LENGTH, Constants.SEARCH_KEYWORD_CASE_SENSITIVE);
        epgView.recalculateAndRedraw(false);
        if (savedSearchDialog != null) savedSearchDialog.update();
        return false;
      }

      @Override
      public boolean onQueryTextChange(String constraint) {
        epgData.filterChannels(constraint, Constants.SEARCH_KEYWORD_ARRAY_SPLIT_REGEX, Constants.SEARCH_KEYWORD_MIN_LENGTH, Constants.SEARCH_KEYWORD_CASE_SENSITIVE);
        epgView.recalculateAndRedraw(false);
        if (savedSearchDialog != null) savedSearchDialog.update();
        return false;
      }
    });
  }
}
