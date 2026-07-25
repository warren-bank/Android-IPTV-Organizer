package com.github.warren_bank.iptv_organizer;

import com.github.warren_bank.iptv_organizer.utils.DbUtils;

import android.app.Application;

public class App extends Application {
  public static Application context = null;

  @Override
  public void onCreate() {
    super.onCreate();

    App.context = this;
    DbUtils.initDb(this);
  }
}
