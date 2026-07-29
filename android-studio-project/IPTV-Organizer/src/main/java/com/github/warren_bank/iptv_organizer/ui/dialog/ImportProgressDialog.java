package com.github.warren_bank.iptv_organizer.ui.dialog;

import com.github.warren_bank.iptv_organizer.R;
import com.github.warren_bank.iptv_organizer.data.parser.ParserProgressListener;

import android.app.Activity;
import android.app.ProgressDialog;

public class ImportProgressDialog implements ParserProgressListener {
  private static final long UI_UPDATE_INTERVAL_MS = 200l;
  private long lastUpdateTime;

  private Activity activity;
  private ProgressDialog dialog;
  private CharSequence title;
  private CharSequence message;

  private Runnable changeTitle = new Runnable() {
    public void run() {
      if (dialog == null) return;

      dialog.setTitle(title);
    }
  };

  private Runnable changeMessage = new Runnable() {
    public void run() {
      if (dialog == null) return;

      dialog.setMessage(message);
    }
  };

  public ImportProgressDialog(Activity activity) {
    this.lastUpdateTime = 0l;

    this.activity = activity;
    this.dialog   = ProgressDialog.show(activity, activity.getString(R.string.import_file), null, true, false);
  }

  @Override
  public void onData(String data) {
    try {
      updateMessage(data);
    }
    catch(Exception ignored) {}
  }

  public void updateTitle(int resId) {
    if (activity == null) return;

    updateTitle(
      activity.getString(resId)
    );
  }

  public void updateTitle(CharSequence title) {
    if (activity == null) return;

    this.title = title;

    activity.runOnUiThread(changeTitle);
  }

  public void updateMessage(CharSequence message) {
    if (activity == null) return;

    this.message = message;

    long currentTime = System.currentTimeMillis();
    if (currentTime - lastUpdateTime >= UI_UPDATE_INTERVAL_MS) {
      lastUpdateTime = currentTime;
      activity.runOnUiThread(changeMessage);
      Thread.yield();
    }
  }

  public void dismiss() {
    dialog.dismiss();
    activity = null;
    dialog   = null;
    title    = null;
    message  = null;
  }
}
