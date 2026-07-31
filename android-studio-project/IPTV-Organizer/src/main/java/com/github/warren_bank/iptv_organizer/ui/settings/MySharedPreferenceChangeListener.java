package com.github.warren_bank.iptv_organizer.ui.settings;

import com.github.warren_bank.iptv_organizer.R;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;
import com.github.warren_bank.iptv_organizer.utils.SettingsUtils;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

  public interface UpdateListener {
    public void onSharedPreferenceChanged();
  }

  private Context context;
  private UpdateListener listener;
  private boolean isRegistered;

  private final String pref_apply_default_url_templates_key;
  private final String pref_default_m3u_url_key;
  private final String pref_default_xmltv_url_key;

  public MySharedPreferenceChangeListener(Context context, UpdateListener listener) {
    this.context      = context;
    this.listener     = listener;
    this.isRegistered = false;

    this.pref_apply_default_url_templates_key = context.getString(R.string.pref_apply_default_url_templates_key);
    this.pref_default_m3u_url_key             = context.getString(R.string.pref_default_m3u_url_key);
    this.pref_default_xmltv_url_key           = context.getString(R.string.pref_default_xmltv_url_key);
  }

  public void register() {
    if (!isRegistered) {
      isRegistered = true;
      SettingsUtils.getPrefs(context).registerOnSharedPreferenceChangeListener(MySharedPreferenceChangeListener.this);
    }
  }

  public void unregister() {
    if (isRegistered) {
      isRegistered = false;
      SettingsUtils.getPrefs(context).unregisterOnSharedPreferenceChangeListener(MySharedPreferenceChangeListener.this);
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key == null) return;

    if (key.equals(pref_apply_default_url_templates_key)) {
      boolean didChange = false;

      didChange |= updateM3uUrl();
      didChange |= updateEpgUrl();

      if (didChange && (listener != null)) listener.onSharedPreferenceChanged();
      return;
    }

    if (key.equals(pref_default_m3u_url_key)) {
      boolean didChange = updateM3uUrl();

      if (didChange && (listener != null)) listener.onSharedPreferenceChanged();
      return;
    }

    if (key.equals(pref_default_xmltv_url_key)) {
      boolean didChange = updateEpgUrl();

      if (didChange && (listener != null)) listener.onSharedPreferenceChanged();
      return;
    }
  }

  private boolean updateM3uUrl() {
    String oldUrl = SettingsUtils.getDefaultM3uUrlPreference(context);
    if (oldUrl.isEmpty()) return false;

    boolean useTemplate = SettingsUtils.getApplyDefaultUrlTemplates(context);
    String newUrl = useTemplate
      ? DbUtils.extractM3uMediaTemplate(oldUrl)
      : DbUtils.resolveM3uMediaUrl(oldUrl);

    boolean didChange = !oldUrl.equals(newUrl);

    if (didChange)
      SettingsUtils.setDefaultM3uUrlPreference(context, newUrl);

    return didChange;
  }

  private boolean updateEpgUrl() {
    String oldUrl = SettingsUtils.getDefaultXmltvEpgUrlPreference(context);
    if (oldUrl.isEmpty()) return false;

    boolean useTemplate = SettingsUtils.getApplyDefaultUrlTemplates(context);
    String newUrl = useTemplate
      ? DbUtils.extractM3uMediaTemplate(oldUrl)
      : DbUtils.resolveM3uMediaUrl(oldUrl);

    boolean didChange = !oldUrl.equals(newUrl);

    if (didChange)
      SettingsUtils.setDefaultXmltvEpgUrlPreference(context, newUrl);

    return didChange;
  }
}
