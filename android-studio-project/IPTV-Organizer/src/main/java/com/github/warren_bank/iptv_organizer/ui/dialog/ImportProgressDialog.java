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
  private int titleResId;
  private CharSequence title;
  private CharSequence message;

  private Runnable changeTitle = new Runnable() {
    public void run() {
      if (!canUpdateUi() || (title == null)) return;

      dialog.setTitle(title);
    }
  };

  private Runnable changeMessage = new Runnable() {
    public void run() {
      if (!canUpdateUi() || (message == null)) return;

      dialog.setMessage(message);
    }
  };

  public ImportProgressDialog(Activity activity) {
    this(activity, R.string.import_file, null, null);
  }

  public ImportProgressDialog(Activity activity, int titleResId, CharSequence message) {
    this(activity, titleResId, null, message);
  }

  public ImportProgressDialog(Activity activity, CharSequence title, CharSequence message) {
    this(activity, -1, title, message);
  }

  private ImportProgressDialog(Activity activity, int titleResId, CharSequence title, CharSequence message) {
    this.titleResId     = titleResId;
    this.title          = title;
    this.message        = message;
    this.lastUpdateTime = 0l;

    WakeLockMgr.acquire(activity);

    pause();
    resume(activity);
  }

  // called from main UI Thread
  public void resume(Activity activity) {
    if (!isPaused) return;

    if ((activity != null) && (titleResId != -1)) {
      title      = activity.getString(titleResId);
      titleResId = -1;
    }

    this.activity = activity;
    this.dialog = (activity != null)
      ? ProgressDialog.show(activity, title, message, true, false)
      : null;
    this.isPaused = false;
  }

  // called from main UI Thread
  public void pause() {
    if (isPaused) return;

    release(false);
  }

  // called from background worker Thread
  @Override
  public void onData(String data) {
    try {
      updateMessage(data);
    }
    catch(Exception ignored) {}
  }

  // called from background worker Thread
  public void updateTitle(int resId) {
    this.titleResId = resId;
    this.title      = null;

    if (activity == null) return;

    updateTitle(
      activity.getString(resId)
    );
  }

  // called from background worker Thread
  public void updateTitle(CharSequence title) {
    this.titleResId = -1;
    this.title      = title;

    if (!canUpdateUi()) return;

    activity.runOnUiThread(changeTitle);
  }

  // called from background worker Thread
  public void updateMessage(CharSequence message) {
    this.message = message;

    if (!canUpdateUi()) return;

    long currentTime = System.currentTimeMillis();
    if (currentTime - lastUpdateTime >= UI_UPDATE_INTERVAL_MS) {
      lastUpdateTime = currentTime;
      activity.runOnUiThread(changeMessage);
//    Thread.yield();
    }
  }

  private boolean canUpdateUi() {
    return canUpdateUi(true);
  }

  private boolean canUpdateUi(boolean requireWindowToken) {
    return (!isPaused && (activity != null) && !activity.isFinishing() && !activity.isDestroyed() && (!requireWindowToken || (activity.getWindow().getDecorView().getWindowToken() != null)) && (dialog != null) && dialog.isShowing());
  }

  // conditionally called from background worker Thread
  public void dismiss(boolean isBackground) {
    if (isBackground && canUpdateUi(false)) {
      activity.runOnUiThread(new Runnable() {
        public void run() {
          dismiss();
        }
      });
    }
    else {
      dismiss();
    }
  }

  // called from main UI Thread
  public void dismiss() {
    release(true);
  }

  // called from main UI Thread (pause, dismiss)
  private void release(boolean finalize) {
    if (canUpdateUi(false))
      dialog.dismiss();

    if (!isPaused) {
      this.isPaused = true;
      this.activity = null;
      this.dialog   = null;
    }

    if (finalize) {
      this.title   = null;
      this.message = null;

      WakeLockMgr.release();
    }
  }
}
