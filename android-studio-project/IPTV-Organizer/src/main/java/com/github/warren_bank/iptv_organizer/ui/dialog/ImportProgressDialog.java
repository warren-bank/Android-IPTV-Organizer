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
  private CharSequence message;

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
      update(data);
    }
    catch(Exception ignored) {}
  }

  public void update(CharSequence message) {
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
    message  = null;
  }
}
