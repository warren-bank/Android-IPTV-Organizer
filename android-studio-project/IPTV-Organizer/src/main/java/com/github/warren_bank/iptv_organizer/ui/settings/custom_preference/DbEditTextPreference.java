package com.github.warren_bank.iptv_organizer.ui.settings.custom_preference;

import com.github.warren_bank.iptv_organizer.R;
import com.github.warren_bank.iptv_organizer.ui.SettingsActivity;
import com.github.warren_bank.iptv_organizer.utils.StreamUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;

public class DbEditTextPreference extends EditTextPreference {
  public DbEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    applyCustomizations();
  }
  public DbEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    applyCustomizations();
  }
  public DbEditTextPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    applyCustomizations();
  }
  public DbEditTextPreference(Context context) {
    super(context);
    applyCustomizations();
  }

  private static final int FILE_IMPORT_REQUEST_CODE = 1;
  private static final int FILE_EXPORT_REQUEST_CODE = 2;

  private CharSequence mNeutralButtonText;

  private void applyCustomizations() {
    if (Build.VERSION.SDK_INT >= 26) {
      setPreferenceDataStore(new DbPreferenceDataStore());
    }
    else {
      // TODO
      //   in v1.x: minSDK = 26 (Android 8.0 Oreo)
      //   in v2.x: minSDK = 19 (Android 4.4 KitKat, and equal to the "android-tv-epg" library)
      //            a different methodology will be used.. TBD
    }

    getEditText().setInputType(
      InputType.TYPE_CLASS_TEXT |
      InputType.TYPE_TEXT_VARIATION_NORMAL |
      InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE |
      InputType.TYPE_TEXT_FLAG_MULTI_LINE |
      InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
    );

    setNegativeButtonText(R.string.dbedittextpreference_buttontext_negative); // Import File
    setNeutralButtonText(R.string.dbedittextpreference_buttontext_neutral);   // Export File
    setPositiveButtonText(R.string.dbedittextpreference_buttontext_positive); // Save
  }

  public void setNeutralButtonText(CharSequence neutralButtonText) {
    mNeutralButtonText = neutralButtonText;
  }
  public void setNeutralButtonText(int neutralButtonTextResId) {
    setNeutralButtonText(getContext().getString(neutralButtonTextResId));
  }
  public CharSequence getNeutralButtonText() {
    return mNeutralButtonText;
  }

  @Override
  protected void showDialog(Bundle state) {
    super.showDialog(state);

    AlertDialog dialog = (AlertDialog) getDialog();

    Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
    Button neutralButton  = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);

    negativeButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        // Import File

        // this is going to be a little hacky..
        // 1. DbEditTextPreference needs a reference to an Activity
        //      activity.startActivityForResult(intent, reqCode)
        // 2. the Activity needs a reference to the instance of DbEditTextPreference that started the Intent
        //      activity.onActivityResult(reqCode, resCode, intent)
        //    to foward the result (reqCode and intent),
        //    which is needed to open an I/O stream

        Activity activity = SettingsActivity.setDbEditTextPreference(DbEditTextPreference.this);
        if (activity == null) return;

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        activity.startActivityForResult(intent, FILE_IMPORT_REQUEST_CODE);
      }
    });

    neutralButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        // Export File

        Activity activity = SettingsActivity.setDbEditTextPreference(DbEditTextPreference.this);
        if (activity == null) return;

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "IPTV-Organizer.txt");

        activity.startActivityForResult(intent, FILE_EXPORT_REQUEST_CODE);
      }
    });
  }

  @Override
  protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
    super.onPrepareDialogBuilder(builder);

    // Do NOT assign listeners to "negative" or "neutral" here!
    // The default behavior is to dismiss the dialog when clicked, and call onClick().
    builder.setNegativeButton(getNegativeButtonText(), null);
    builder.setNeutralButton( getNeutralButtonText(),  null);

    // note: This is allowed for "positive" (Save)..
    // Its default behavior is:
    // 1. DialogPreference.onClick() sets private: mWhichButtonClicked = which;
    // 2. DialogPreference.onDismiss calls: onDialogClosed(mWhichButtonClicked == DialogInterface.BUTTON_POSITIVE);
    // 3. EditTextPreference.onDialogClosed() calls: setText(text);
    // 4. EditTextPreference.setText() calls: persistString(text);
    // 5. Preference.persistString() calls: dataStore.putString(mKey, value);
    // 6.   => Save the value to DB
  }

  public void onResult(int requestCode, Uri uri) {
    switch(requestCode) {
      case FILE_IMPORT_REQUEST_CODE: {
        SettingsActivity.setDbEditTextPreference(null);

        try {
          InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
          String text = StreamUtils.read(inputStream);
          if (text == null) return;

          // update EditText field
          getEditText().setText(text, TextView.BufferType.NORMAL);

          // save to DB
          setText(text);
        }
        catch (Exception e) {}
      }
      break;

      case FILE_EXPORT_REQUEST_CODE: {
        SettingsActivity.setDbEditTextPreference(null);

        try {
          OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri, "w");
          String text = getEditText().getText().toString();

          StreamUtils.write(outputStream, text);
        }
        catch (Exception e) {}
      }
      break;
    }
  }

}
