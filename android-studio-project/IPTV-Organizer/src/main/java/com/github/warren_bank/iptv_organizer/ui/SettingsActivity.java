package com.github.warren_bank.iptv_organizer.ui;

import com.github.warren_bank.iptv_organizer.ui.ChannelsActivity;
import com.github.warren_bank.iptv_organizer.ui.settings.custom_preference.DbEditTextPreference;
import com.github.warren_bank.iptv_organizer.ui.settings.custom_preference.DbPreferenceDataStore;
import com.github.warren_bank.iptv_organizer.ui.settings.MySharedPreferenceChangeListener;
import com.github.warren_bank.iptv_organizer.ui.settings.SettingsFragment;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;
import com.github.warren_bank.iptv_organizer.utils.SettingsUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.TextUtils;

public class SettingsActivity extends PreferenceActivity implements MySharedPreferenceChangeListener.UpdateListener {

  public static void open(Context context) {
    Intent intent = new Intent(context, SettingsActivity.class);
    context.startActivity(intent);
  }

  public static final int DB_FILE_EXPORT_REQUEST_CODE = 1;
  public static final int DB_FILE_IMPORT_REQUEST_CODE = 2;

  private static final String EXTRA_SETTINGS_AUTO_SAVE                    = "SETTINGS_AUTO_SAVE";                     // Boolean
  private static final String EXTRA_SETTINGS_AUTO_CLOSE                   = "SETTINGS_AUTO_CLOSE";                    // Boolean
  private static final String EXTRA_SETTINGS_APPLY_STATIC_STRINGS         = "SETTINGS_APPLY_STATIC_STRINGS";          // Boolean
  private static final String EXTRA_SEARCH_INPUT_DEBOUNCE_INTERVAL        = "SEARCH_INPUT_DEBOUNCE_INTERVAL";         // int
  private static final String EXTRA_SEARCH_RESULTS_MAX_COUNT              = "SEARCH_RESULTS_MAX_COUNT";               // int
  private static final String EXTRA_SEARCH_RESULTS_REMOVE_DUPLICATE_NAMES = "SEARCH_RESULTS_REMOVE_DUPLICATE_NAMES";  // Boolean
  private static final String EXTRA_M3U_DEFAULT_PLAYLIST_URL              = "M3U_DEFAULT_PLAYLIST_URL";               // String
  private static final String EXTRA_M3U_APPEND_PLAYLISTS                  = "M3U_APPEND_PLAYLISTS";                   // Boolean
  private static final String EXTRA_M3U_MAP_CHANNEL_NAME_TO_ID            = "M3U_MAP_CHANNEL_NAME_TO_ID";             // String[]
  private static final String EXTRA_M3U_MAP_CHANNEL_ID_TO_ID              = "M3U_MAP_CHANNEL_ID_TO_ID";               // String[]
  private static final String EXTRA_M3U_CHANNEL_NAME_WHITELIST            = "M3U_CHANNEL_NAME_WHITELIST";             // String[]
  private static final String EXTRA_M3U_CHANNEL_ID_WHITELIST              = "M3U_CHANNEL_ID_WHITELIST";               // String[]
  private static final String EXTRA_M3U_CHANNEL_NAME_BLACKLIST            = "M3U_CHANNEL_NAME_BLACKLIST";             // String[]
  private static final String EXTRA_M3U_CHANNEL_ID_BLACKLIST              = "M3U_CHANNEL_ID_BLACKLIST";               // String[]
  private static final String EXTRA_M3U_MEDIA_URL_STATIC_STRINGS          = "M3U_MEDIA_URL_STATIC_STRINGS";           // String[]
  private static final String EXTRA_EPG_DEFAULT_XMLTV_URL                 = "EPG_DEFAULT_XMLTV_URL";                  // String
  private static final String EXTRA_EPG_AUTO_UPDATE_DEFAULT_XMLTV_URL     = "EPG_AUTO_UPDATE_DEFAULT_XMLTV_URL";      // Boolean
  private static final String EXTRA_EPG_PARSE_LIST_IN_XMLTV_URL           = "EPG_PARSE_LIST_IN_XMLTV_URL";            // Boolean
  private static final String EXTRA_EPG_PREFERRED_LANGUAGE                = "EPG_PREFERRED_LANGUAGE";                 // String
  private static final String EXTRA_EPG_CHANNEL_M3U_WHITELIST             = "EPG_CHANNEL_M3U_WHITELIST";              // Boolean
  private static final String EXTRA_EPG_CHANNEL_NAME_WHITELIST            = "EPG_CHANNEL_NAME_WHITELIST";             // String[]
  private static final String EXTRA_EPG_CHANNEL_ID_WHITELIST              = "EPG_CHANNEL_ID_WHITELIST";               // String[]
  private static final String EXTRA_EPG_CHANNEL_NAME_BLACKLIST            = "EPG_CHANNEL_NAME_BLACKLIST";             // String[]
  private static final String EXTRA_EPG_CHANNEL_ID_BLACKLIST              = "EPG_CHANNEL_ID_BLACKLIST";               // String[]
  private static final String EXTRA_SAVED_SEARCH_KEYWORDS_LIST            = "SAVED_SEARCH_KEYWORDS_LIST";             // String[]

  private static Activity             self   = null;
  private static DbEditTextPreference dbPref = null;

  public static Activity setDbEditTextPreference(DbEditTextPreference pref) {
    if (self == null) return null;

    dbPref = pref;
    return self;
  }

  private MySharedPreferenceChangeListener listener;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    self = this;

    listener = new MySharedPreferenceChangeListener(this, this);

    onNewIntent(getIntent());
  }

  @Override
  protected void onStart() {
    super.onStart();
    listener.register();
  }

  @Override
  protected void onStop() {
    super.onStop();
    listener.unregister();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode != RESULT_OK) return;
    if (data == null) return;

    Uri uri = data.getData();
    if (uri == null) return;

    if (requestCode == DB_FILE_EXPORT_REQUEST_CODE) {
      try {
        DbUtils.doDbBackup(
          SettingsActivity.this,
          getContentResolver().openOutputStream(uri, "w")
        );
      }
      catch(Exception ignored) {}
      return;
    }

    if (requestCode == DB_FILE_IMPORT_REQUEST_CODE) {
      try {
        DbUtils.doDbRestore(
          SettingsActivity.this,
          getContentResolver().openInputStream(uri)
        );

        // open "M3U Channels" and force data refresh
        ChannelsActivity.open(SettingsActivity.this, true);

        finish();
      }
      catch(Exception ignored) {}
      return;
    }

    if (dbPref == null) return;
    dbPref.onResult(requestCode, uri);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if (intent == null) return;

    listener.register();

    boolean didUpdate = false;

    try {
      if (intent.hasExtra(EXTRA_SETTINGS_AUTO_SAVE)) {
        boolean value = intent.getBooleanExtra(EXTRA_SETTINGS_AUTO_SAVE, true);
        SettingsUtils.setDbEditTextPreferenceAutoSave(SettingsActivity.this, value);
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_SETTINGS_AUTO_CLOSE)) {
        boolean value = intent.getBooleanExtra(EXTRA_SETTINGS_AUTO_CLOSE, true);
        SettingsUtils.setDbEditTextPreferenceAutoClose(SettingsActivity.this, value);
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_SETTINGS_APPLY_STATIC_STRINGS)) {
        boolean value = intent.getBooleanExtra(EXTRA_SETTINGS_APPLY_STATIC_STRINGS, true);
        SettingsUtils.setApplyDefaultUrlTemplates(SettingsActivity.this, value);
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_SEARCH_INPUT_DEBOUNCE_INTERVAL)) {
        int value = intent.getIntExtra(EXTRA_SEARCH_INPUT_DEBOUNCE_INTERVAL, -1);
        if (value >= 0) {
          SettingsUtils.setSearchInputDebounceIntervalMs(SettingsActivity.this, value);
          didUpdate = true;
        }
      }

      if (intent.hasExtra(EXTRA_SEARCH_RESULTS_MAX_COUNT)) {
        int value = intent.getIntExtra(EXTRA_SEARCH_RESULTS_MAX_COUNT, -1);
        if (value >= 0) {
          SettingsUtils.setMaxCountOfSearchResults(SettingsActivity.this, value);
          didUpdate = true;
        }
      }

      if (intent.hasExtra(EXTRA_SEARCH_RESULTS_REMOVE_DUPLICATE_NAMES)) {
        boolean value = intent.getBooleanExtra(EXTRA_SEARCH_RESULTS_REMOVE_DUPLICATE_NAMES, true);
        SettingsUtils.setRemoveDuplicateNamesFromSearchResults(SettingsActivity.this, value);
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_M3U_DEFAULT_PLAYLIST_URL)) {
        String value = intent.getStringExtra(EXTRA_M3U_DEFAULT_PLAYLIST_URL);
        SettingsUtils.setDefaultM3uUrlPreference(SettingsActivity.this, value);
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_M3U_APPEND_PLAYLISTS)) {
        boolean value = intent.getBooleanExtra(EXTRA_M3U_APPEND_PLAYLISTS, false);
        SettingsUtils.setAppendM3uPlaylists(SettingsActivity.this, value);
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_M3U_MAP_CHANNEL_NAME_TO_ID)) {
        String[] value = intent.getStringArrayExtra(EXTRA_M3U_MAP_CHANNEL_NAME_TO_ID);
        DbPreferenceDataStore.putString(
          DbPreferenceDataStore.KEY_M3U_CHANNELS_MAPPING_NAME_TO_ID,
          joinStringArray(value)
        );
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_M3U_MAP_CHANNEL_ID_TO_ID)) {
        String[] value = intent.getStringArrayExtra(EXTRA_M3U_MAP_CHANNEL_ID_TO_ID);
        DbPreferenceDataStore.putString(
          DbPreferenceDataStore.KEY_M3U_CHANNELS_MAPPING_ID_TO_ID,
          joinStringArray(value)
        );
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_M3U_CHANNEL_NAME_WHITELIST)) {
        String[] value = intent.getStringArrayExtra(EXTRA_M3U_CHANNEL_NAME_WHITELIST);
        DbPreferenceDataStore.putString(
          DbPreferenceDataStore.KEY_M3U_CHANNELS_FILTER_WHITELIST_NAMES,
          joinStringArray(value)
        );
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_M3U_CHANNEL_ID_WHITELIST)) {
        String[] value = intent.getStringArrayExtra(EXTRA_M3U_CHANNEL_ID_WHITELIST);
        DbPreferenceDataStore.putString(
          DbPreferenceDataStore.KEY_M3U_CHANNELS_FILTER_WHITELIST_IDS,
          joinStringArray(value)
        );
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_M3U_CHANNEL_NAME_BLACKLIST)) {
        String[] value = intent.getStringArrayExtra(EXTRA_M3U_CHANNEL_NAME_BLACKLIST);
        DbPreferenceDataStore.putString(
          DbPreferenceDataStore.KEY_M3U_CHANNELS_FILTER_BLACKLIST_NAMES,
          joinStringArray(value)
        );
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_M3U_CHANNEL_ID_BLACKLIST)) {
        String[] value = intent.getStringArrayExtra(EXTRA_M3U_CHANNEL_ID_BLACKLIST);
        DbPreferenceDataStore.putString(
          DbPreferenceDataStore.KEY_M3U_CHANNELS_FILTER_BLACKLIST_IDS,
          joinStringArray(value)
        );
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_M3U_MEDIA_URL_STATIC_STRINGS)) {
        String[] value = intent.getStringArrayExtra(EXTRA_M3U_MEDIA_URL_STATIC_STRINGS);
        DbPreferenceDataStore.putString(
          DbPreferenceDataStore.KEY_M3U_CHANNELS_MEDIA_URL_STATIC_VALUES,
          joinStringArray(value)
        );
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_EPG_DEFAULT_XMLTV_URL)) {
        String value = intent.getStringExtra(EXTRA_EPG_DEFAULT_XMLTV_URL);
        SettingsUtils.setDefaultXmltvEpgUrlPreference(SettingsActivity.this, value);
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_EPG_AUTO_UPDATE_DEFAULT_XMLTV_URL)) {
        boolean value = intent.getBooleanExtra(EXTRA_EPG_AUTO_UPDATE_DEFAULT_XMLTV_URL, true);
        SettingsUtils.setAutoUpdateDefaultXmltvEpgUrl(SettingsActivity.this, value);
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_EPG_PARSE_LIST_IN_XMLTV_URL)) {
        boolean value = intent.getBooleanExtra(EXTRA_EPG_PARSE_LIST_IN_XMLTV_URL, true);
        SettingsUtils.setParseListInXmltvEpgUrl(SettingsActivity.this, value);
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_EPG_PREFERRED_LANGUAGE)) {
        String value = intent.getStringExtra(EXTRA_EPG_PREFERRED_LANGUAGE);
        SettingsUtils.setPreferredXmltvLanguage(SettingsActivity.this, value);
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_EPG_CHANNEL_M3U_WHITELIST)) {
        boolean value = intent.getBooleanExtra(EXTRA_EPG_CHANNEL_M3U_WHITELIST, true);
        SettingsUtils.setFilterM3uChannels(SettingsActivity.this, value);
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_EPG_CHANNEL_NAME_WHITELIST)) {
        String[] value = intent.getStringArrayExtra(EXTRA_EPG_CHANNEL_NAME_WHITELIST);
        DbPreferenceDataStore.putString(
          DbPreferenceDataStore.KEY_EPG_CHANNELS_FILTER_WHITELIST_NAMES,
          joinStringArray(value)
        );
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_EPG_CHANNEL_ID_WHITELIST)) {
        String[] value = intent.getStringArrayExtra(EXTRA_EPG_CHANNEL_ID_WHITELIST);
        DbPreferenceDataStore.putString(
          DbPreferenceDataStore.KEY_EPG_CHANNELS_FILTER_WHITELIST_IDS,
          joinStringArray(value)
        );
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_EPG_CHANNEL_NAME_BLACKLIST)) {
        String[] value = intent.getStringArrayExtra(EXTRA_EPG_CHANNEL_NAME_BLACKLIST);
        DbPreferenceDataStore.putString(
          DbPreferenceDataStore.KEY_EPG_CHANNELS_FILTER_BLACKLIST_NAMES,
          joinStringArray(value)
        );
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_EPG_CHANNEL_ID_BLACKLIST)) {
        String[] value = intent.getStringArrayExtra(EXTRA_EPG_CHANNEL_ID_BLACKLIST);
        DbPreferenceDataStore.putString(
          DbPreferenceDataStore.KEY_EPG_CHANNELS_FILTER_BLACKLIST_IDS,
          joinStringArray(value)
        );
        didUpdate = true;
      }

      if (intent.hasExtra(EXTRA_SAVED_SEARCH_KEYWORDS_LIST)) {
        String[] value = intent.getStringArrayExtra(EXTRA_SAVED_SEARCH_KEYWORDS_LIST);
        DbPreferenceDataStore.putString(
          DbPreferenceDataStore.KEY_SAVED_SEARCH_KEYWORDS_LIST,
          joinStringArray(value)
        );
//      didUpdate = true;
      }
    }
    catch(Exception e) {}

    if (!didUpdate && !hasFragment())
      didUpdate = true;

    if (didUpdate)
      reloadFragment();
  }

  @Override
  public void onSharedPreferenceChanged() {
    reloadFragment();
  }

  private void reloadFragment() {
    getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
  }

  private SettingsFragment getFragment() {
    return (SettingsFragment) getFragmentManager().findFragmentById(android.R.id.content);
  }

  private boolean hasFragment() {
    SettingsFragment fragment = getFragment();
    return (fragment != null);
  }

  private static String joinStringArray(String[] value) {
    String result = (value != null)
      ? TextUtils.join("\n", value)
      : null;

    if ("null".equals(result)) result = null;

    return result;
  }
}
