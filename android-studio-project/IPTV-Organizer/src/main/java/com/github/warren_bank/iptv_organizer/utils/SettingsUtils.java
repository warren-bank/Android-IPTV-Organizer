package com.github.warren_bank.iptv_organizer.utils;

import com.github.warren_bank.iptv_organizer.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsUtils {

  public static SharedPreferences getPrefs(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  // --------------------

  public static String getDefaultM3uUrlPreference(Context context) {
    return getDefaultM3uUrlPreference(context, getPrefs(context));
  }

  private static String getDefaultM3uUrlPreference(Context context, SharedPreferences prefs) {
    String pref_key    = context.getString(R.string.pref_default_m3u_url_key);
    String val_default = context.getString(R.string.pref_default_m3u_url_default);

    return prefs.getString(pref_key, val_default);
  }

  // --------------------

  public static String getDefaultXmltvEpgUrlPreference(Context context) {
    return getDefaultXmltvEpgUrlPreference(context, getPrefs(context));
  }

  private static String getDefaultXmltvEpgUrlPreference(Context context, SharedPreferences prefs) {
    String pref_key    = context.getString(R.string.pref_default_xmltv_url_key);
    String val_default = context.getString(R.string.pref_default_xmltv_url_default);

    return prefs.getString(pref_key, val_default);
  }

  // --------------------

  public static boolean getApplyXmltvImportFilter(Context context) {
    return getApplyXmltvImportFilter(context, getPrefs(context));
  }

  private static boolean getApplyXmltvImportFilter(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_apply_import_filter_xmltv_channels_key);
    String pref_default = context.getString(R.string.pref_apply_import_filter_xmltv_channels_default);
    boolean val_default = "true".equals(pref_default);

    return prefs.getBoolean(pref_key, val_default);
  }

}
