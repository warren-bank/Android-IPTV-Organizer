package com.github.warren_bank.iptv_organizer.ui;

import com.github.warren_bank.iptv_organizer.R;
import com.github.warren_bank.iptv_organizer.common.Constants;
import com.github.warren_bank.iptv_organizer.data.model.EPGDataImpl;
import com.github.warren_bank.iptv_organizer.ui.ChannelsActivity;
import com.github.warren_bank.iptv_organizer.ui.SettingsActivity;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;
import com.github.warren_bank.iptv_organizer.utils.ImportUtils;

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

public class EpgActivity extends AppCompatActivity {

  public static void open(Context context) {
    Intent intent = new Intent(context, EpgActivity.class);
    context.startActivity(intent);
  }

  private EPG         epgView;
  private EPGDataImpl epgData;
  private SearchView  searchView;

  // ---------------------------------------------------------------------------------------------
  // Lifecycle Events:
  // ---------------------------------------------------------------------------------------------

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_epg);

    initEpg();
    initToolbar();

    onNewIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    try {
      Uri data = intent.getData();
      if (data == null) return;

      String urlText = data.toString().trim();
      if (urlText.isEmpty()) return;

      // Do network on a background thread
      new Thread(() -> openUrlAsStream(urlText)).start();
    } catch (Exception ignored) {}
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
    initSearch();

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    switch(menuItem.getItemId()) {

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
    if (!searchView.isIconified()) {
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
            String urlText = input.getText().toString().trim();
            if (urlText.isEmpty()) return;

            // Do network on a background thread
            new Thread(() -> openUrlAsStream(urlText)).start();
          }
        })
        .setNegativeButton(R.string.epg_dialog_import_xmltv_url_button_negative, null)
        .show();
  }

  private void openUrlAsStream(String urlText) {
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
      importXmlTvFromStream(inputStream);
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    } finally {
      try {
        if (inputStream != null) inputStream.close();
      } catch (Exception ignored) {}
      if (conn != null) conn.disconnect();
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
    String[] mimeTypes = {"application/xml", "application/xmltv", "text/xml", "text/xmltv"};
    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

    startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if ((requestCode == FILE_CHOOSER_REQUEST_CODE) && (resultCode == RESULT_OK)) {
      Uri uri = data.getData();
      if (uri == null) return;

      openFileAsStream(uri);
    }
  }

  private void openFileAsStream(Uri uri) {
    InputStream inputStream = null;

    try {
      inputStream = getContentResolver().openInputStream(uri);
      importXmlTvFromStream(inputStream);
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    } finally {
      try {
        if (inputStream != null) inputStream.close();
      } catch (Exception ignored) {}
    }
  }

  // ---------------------------------------------------------------------------------------------
  // internal:
  // ---------------------------------------------------------------------------------------------

  private void importXmlTvFromStream(InputStream inputStream) throws Exception {
    final EPGDataImpl newEpgData = new EPGDataImpl(
      ImportUtils.importXmlTv(inputStream)
    );

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        refreshEpg(newEpgData);
      }
    });
  }

  private void initEpg() {
    epgView = (EPG) findViewById(R.id.epg);

    epgView.setEPGClickListener(new EPGClickListener() {
      @Override
      public void onChannelClicked(int channelPosition, EPGChannel epgChannel) {
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
      }

      @Override
      public void onResetButtonClicked() {
        epgView.recalculateAndRedraw(true);
      }
    });

    refreshEpg(null);
  }

  private void refreshEpg(EPGDataImpl newEpgData) {
    boolean animate = (epgData != null);

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

  private void initSearch() {
    searchView.setMaxWidth(Integer.MAX_VALUE);

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String constraint) {
        epgData.filterChannels(constraint, false);
        epgView.recalculateAndRedraw(false);
        return false;
      }

      @Override
      public boolean onQueryTextChange(String constraint) {
        epgData.filterChannels(constraint, false);
        epgView.recalculateAndRedraw(false);
        return false;
      }
    });
  }
}
