package com.github.warren_bank.iptv_organizer.ui;

import com.github.warren_bank.iptv_organizer.ui.settings.custom_preference.DbEditTextPreference;
import com.github.warren_bank.iptv_organizer.ui.settings.SettingsFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

  public static void open(Context context) {
    Intent intent = new Intent(context, SettingsActivity.class);
    context.startActivity(intent);
  }

  private static Activity             self   = null;
  private static DbEditTextPreference dbPref = null;

  public static Activity setDbEditTextPreference(DbEditTextPreference pref) {
    if (self == null) return null;

    dbPref = pref;
    return self;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    self = this;

    getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (dbPref == null) return;
    if (resultCode != RESULT_OK) return;
    if (data == null) return;

    Uri uri = data.getData();
    if (uri == null) return;

    dbPref.onResult(requestCode, uri);
  }
}
