package com.github.warren_bank.iptv_organizer.ui.settings;

import com.github.warren_bank.iptv_organizer.R;
import com.github.warren_bank.iptv_organizer.ui.SettingsActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragment {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    // Backup Database
    findPreference(getString(R.string.pref_advanced_backup_db_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "IPTV-Organizer.sqlite");

        getActivity().startActivityForResult(intent, SettingsActivity.DB_FILE_EXPORT_REQUEST_CODE);
        return true;
      }
    });

    // Restore Database
    findPreference(getString(R.string.pref_advanced_restore_db_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        getActivity().startActivityForResult(intent, SettingsActivity.DB_FILE_IMPORT_REQUEST_CODE);
        return true;
      }
    });
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);

    // fix for Android 15+ edge-to-edge layout enforcement
    if ((view != null) && (Build.VERSION.SDK_INT >= 14))
      view.setFitsSystemWindows(true);

    return view;
  }
}
