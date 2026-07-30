package com.github.warren_bank.iptv_organizer.ui.dialog;

import com.github.warren_bank.iptv_organizer.R;
import com.github.warren_bank.iptv_organizer.data.parser.ParserProgressListener;
import com.github.warren_bank.iptv_organizer.utils.WakeLockMgr;

import android.app.Activity;
import android.app.ProgressDialog;

public class ImportProgressDialog implements ParserProgressListener {
  private static final long UI_UPDATE_INTERVAL_MS = 200l;
  private long lastUpdateTime;
  private boolean isPaused;

  private Activity activity;
  private ProgressDialog dialog;
  private CharSequence title;
  private CharSequence message;

  private Runnable changeTitle = new Runnable() {
    public void run() {
      if ((dialog == null) || (title == null)) return;

      dialog.setTitle(title);
    }
  };

  private Runnable changeMessage = new Runnable() {
    public void run() {
      if ((dialog == null) || (message == null)) return;

      dialog.setMessage(message);
    }
  };

  public ImportProgressDialog(Activity activity) {
    this.lastUpdateTime = 0l;
    this.isPaused = false;

    this.activity = activity;
    this.dialog = (activity != null)
      ? ProgressDialog.show(activity, activity.getString(R.string.import_file), null, true, false)
      : null;

    WakeLockMgr.acquire(activity);
  }

  public void pause() {
    this.isPaused = true;

    this.title = null;
    this.message = null;
  }

  public void resume() {
    this.isPaused = false;

    if (title   != null) updateTitle(title);
    if (message != null) updateMessage(message);
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
    this.title = title;

    if ((activity == null) || isPaused) return;

    activity.runOnUiThread(changeTitle);
  }

  public void updateMessage(CharSequence message) {
    this.message = message;

    if ((activity == null) || isPaused) return;

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

    WakeLockMgr.release();
  }
}
