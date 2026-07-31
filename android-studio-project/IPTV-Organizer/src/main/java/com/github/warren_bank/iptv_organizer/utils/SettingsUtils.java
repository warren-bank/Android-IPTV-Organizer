package com.github.warren_bank.iptv_organizer.utils;

import com.github.warren_bank.iptv_organizer.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsUtils {

  private static SharedPreferences getPrefs(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  private static SharedPreferences.Editor getPrefsEditor(Context context) {
    SharedPreferences prefs = getPrefs(context);

    return getPrefsEditor(prefs);
  }

  private static SharedPreferences.Editor getPrefsEditor(SharedPreferences prefs) {
    return prefs.edit();
  }

  // --------------------

  public static boolean getDbEditTextPreferenceAutoSave(Context context) {
    return getDbEditTextPreferenceAutoSave(context, getPrefs(context));
  }

  private static boolean getDbEditTextPreferenceAutoSave(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_dbedittextpreference_autosave_key);
    String pref_default = context.getString(R.string.pref_dbedittextpreference_autosave_default);
    boolean val_default = "true".equals(pref_default);

    return prefs.getBoolean(pref_key, val_default);
  }

  public static boolean setDbEditTextPreferenceAutoSave(Context context, boolean value) {
    SharedPreferences.Editor editor = getPrefsEditor(context);
    setDbEditTextPreferenceAutoSave(context, value, editor);
    return editor.commit();
  }

  private static void setDbEditTextPreferenceAutoSave(Context context, boolean value, SharedPreferences.Editor editor) {
    String pref_key = context.getString(R.string.pref_dbedittextpreference_autosave_key);

    editor.putBoolean(pref_key, value);
  }

  // --------------------

  public static boolean getDbEditTextPreferenceAutoClose(Context context) {
    return getDbEditTextPreferenceAutoClose(context, getPrefs(context));
  }

  private static boolean getDbEditTextPreferenceAutoClose(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_dbedittextpreference_autoclose_key);
    String pref_default = context.getString(R.string.pref_dbedittextpreference_autoclose_default);
    boolean val_default = "true".equals(pref_default);

    return prefs.getBoolean(pref_key, val_default);
  }

  public static boolean setDbEditTextPreferenceAutoClose(Context context, boolean value) {
    SharedPreferences.Editor editor = getPrefsEditor(context);
    setDbEditTextPreferenceAutoClose(context, value, editor);
    return editor.commit();
  }

  private static void setDbEditTextPreferenceAutoClose(Context context, boolean value, SharedPreferences.Editor editor) {
    String pref_key = context.getString(R.string.pref_dbedittextpreference_autoclose_key);

    editor.putBoolean(pref_key, value);
  }

  // --------------------

  public static boolean getApplyDefaultUrlTemplates(Context context) {
    return getApplyDefaultUrlTemplates(context, getPrefs(context));
  }

  private static boolean getApplyDefaultUrlTemplates(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_apply_default_url_templates_key);
    String pref_default = context.getString(R.string.pref_apply_default_url_templates_default);
    boolean val_default = "true".equals(pref_default);

    return prefs.getBoolean(pref_key, val_default);
  }

  public static boolean setApplyDefaultUrlTemplates(Context context, boolean value) {
    SharedPreferences.Editor editor = getPrefsEditor(context);
    setApplyDefaultUrlTemplates(context, value, editor);
    return editor.commit();
  }

  private static void setApplyDefaultUrlTemplates(Context context, boolean value, SharedPreferences.Editor editor) {
    String pref_key = context.getString(R.string.pref_apply_default_url_templates_key);

    editor.putBoolean(pref_key, value);
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

  public static boolean setDefaultM3uUrlPreference(Context context, String value) {
    SharedPreferences.Editor editor = getPrefsEditor(context);
    setDefaultM3uUrlPreference(context, value, editor);
    return editor.commit();
  }

  private static void setDefaultM3uUrlPreference(Context context, String value, SharedPreferences.Editor editor) {
    String pref_key = context.getString(R.string.pref_default_m3u_url_key);

    editor.putString(pref_key, value);
  }

  // --------------------

  public static boolean getAppendM3uPlaylists(Context context) {
    return getAppendM3uPlaylists(context, getPrefs(context));
  }

  private static boolean getAppendM3uPlaylists(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_append_m3u_playlists_key);
    String pref_default = context.getString(R.string.pref_append_m3u_playlists_default);
    boolean val_default = "true".equals(pref_default);

    return prefs.getBoolean(pref_key, val_default);
  }

  public static boolean setAppendM3uPlaylists(Context context, boolean value) {
    SharedPreferences.Editor editor = getPrefsEditor(context);
    setAppendM3uPlaylists(context, value, editor);
    return editor.commit();
  }

  private static void setAppendM3uPlaylists(Context context, boolean value, SharedPreferences.Editor editor) {
    String pref_key = context.getString(R.string.pref_append_m3u_playlists_key);

    editor.putBoolean(pref_key, value);
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

  public static boolean setDefaultXmltvEpgUrlPreference(Context context, String value) {
    SharedPreferences.Editor editor = getPrefsEditor(context);
    setDefaultXmltvEpgUrlPreference(context, value, editor);
    return editor.commit();
  }

  private static void setDefaultXmltvEpgUrlPreference(Context context, String value, SharedPreferences.Editor editor) {
    String pref_key = context.getString(R.string.pref_default_xmltv_url_key);

    editor.putString(pref_key, value);
  }

  // --------------------

  public static String getPreferredXmltvLanguage(Context context) {
    return getPreferredXmltvLanguage(context, getPrefs(context));
  }

  private static String getPreferredXmltvLanguage(Context context, SharedPreferences prefs) {
    String pref_key    = context.getString(R.string.pref_preferred_xmltv_language_key);
    String val_default = context.getString(R.string.pref_preferred_xmltv_language_default);

    return prefs.getString(pref_key, val_default);
  }

  public static boolean setPreferredXmltvLanguage(Context context, String value) {
    SharedPreferences.Editor editor = getPrefsEditor(context);
    setPreferredXmltvLanguage(context, value, editor);
    return editor.commit();
  }

  private static void setPreferredXmltvLanguage(Context context, String value, SharedPreferences.Editor editor) {
    String pref_key = context.getString(R.string.pref_preferred_xmltv_language_key);

    editor.putString(pref_key, value);
  }

  // --------------------

  public static boolean getFilterM3uChannels(Context context) {
    return getFilterM3uChannels(context, getPrefs(context));
  }

  private static boolean getFilterM3uChannels(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_epg_channels_filter_whitelist_m3u_key);
    String pref_default = context.getString(R.string.pref_epg_channels_filter_whitelist_m3u_default);
    boolean val_default = "true".equals(pref_default);

    return prefs.getBoolean(pref_key, val_default);
  }

  public static boolean setFilterM3uChannels(Context context, boolean value) {
    SharedPreferences.Editor editor = getPrefsEditor(context);
    setFilterM3uChannels(context, value, editor);
    return editor.commit();
  }

  private static void setFilterM3uChannels(Context context, boolean value, SharedPreferences.Editor editor) {
    String pref_key = context.getString(R.string.pref_epg_channels_filter_whitelist_m3u_key);

    editor.putBoolean(pref_key, value);
  }

}
