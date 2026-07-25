package com.github.warren_bank.iptv_organizer.database;

import com.github.warren_bank.iptv_organizer.common.Constants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.util.List;

public class SQLiteStore extends SQLiteOpenHelper {
  private static SQLiteStore instance = null;

  private SQLiteDatabase db;

  public static SQLiteStore getInstance(Context context) {
    if (instance == null)
      instance = new SQLiteStore(context.getApplicationContext());

    return instance;
  }

  private SQLiteStore(Context context) {
    super(context, Constants.DB_FILENAME, null, 1);

    db = getWritableDatabase();

    if (Build.VERSION.SDK_INT >= 16) {
      db.disableWriteAheadLogging();
    }
  }

  public SQLiteDatabase getSQLiteDatabase() {
    return db;
  }

  public long insert(String table, String nullColumnHack, ContentValues values) {
    boolean skipVersionCheck = false;
    return insert(table, nullColumnHack, values, skipVersionCheck);
  }

  public long insert(String table, String nullColumnHack, ContentValues values, boolean skipVersionCheck) {
    // only run queries against the current DB schema
    if (!skipVersionCheck && Update.needsUpdate(this)) return -1l;

    return db.insert(table, nullColumnHack, values);
  }

  public Cursor query(String query) {
    boolean skipVersionCheck = false;
    return query(query, skipVersionCheck);
  }

  public Cursor query(String query, boolean skipVersionCheck) {
    // only run queries against the current DB schema
    if (!skipVersionCheck && Update.needsUpdate(this)) return null;

    Cursor c = null;

    try {
      c = db.rawQuery(query, null);
    }
    catch (SQLiteException e) {
      return null;
    }
    return c;
  }

  public boolean execQuery(String query) {
    boolean skipVersionCheck = false;
    return execQuery(query, skipVersionCheck);
  }

  public boolean execQuery(String query, boolean skipVersionCheck) {
    // only run queries against the current DB schema
    if (!skipVersionCheck && Update.needsUpdate(this)) return false;

    try {
      db.execSQL(query);
    }
    catch (SQLiteException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public boolean execTransaction(List<String> queries) {
    boolean skipVersionCheck = false;
    return execTransaction(queries, skipVersionCheck);
  }

  public boolean execTransaction(List<String> queries, boolean skipVersionCheck) {
    // only run queries against the current DB schema
    if (!skipVersionCheck && Update.needsUpdate(this)) return false;

    // validate input
    if ((queries == null) || queries.isEmpty()) return false;

    boolean result = true;
    try {
      db.beginTransaction();
      for (String query : queries) {
        db.execSQL(query);
      }
      db.setTransactionSuccessful();
    }
    catch (SQLiteException e) {
      e.printStackTrace();
      result = false;
    }
    finally {
      db.endTransaction();
    }
    return result;
  }

  @Override
  public synchronized void close() {
    if (db != null) db.close();
    super.close();
  }

  @Override
  public void onCreate(SQLiteDatabase dbase) {
    if (dbase == null)
      dbase = db;
    if (dbase == null)
      return;

    try {
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS application ("
        + "    version INTEGER NOT NULL PRIMARY KEY"
        + ");"
      );
      dbase.execSQL(
          "INSERT INTO application (version) VALUES (" + Update.VERSION_CURRENT + ");"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS m3u_channels ("
        + "    position             INTEGER NOT NULL PRIMARY KEY,"
        + "    name                 VARCHAR NOT NULL,"
        + "    media_url            VARCHAR NOT NULL,"
        + "    tvg_id               VARCHAR,"
        + "    tvg_name             VARCHAR,"

        + "    UNIQUE (media_url)"
        + ");"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS xmltv_channels ("
        + "    id                   VARCHAR NOT NULL PRIMARY KEY,"
        + "    name                 VARCHAR NOT NULL,"
        + "    icon_url             VARCHAR"
        + ");"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS xmltv_programs ("
        + "    channel_id           VARCHAR NOT NULL,"
        + "    start_timestamp_ms   INTEGER NOT NULL,"
        + "    stop_timestamp_ms    INTEGER,"
        + "    title                VARCHAR NOT NULL,"
        + "    description          VARCHAR,"

        + "    UNIQUE (channel_id, start_timestamp_ms),"
        + "    FOREIGN KEY (channel_id) REFERENCES xmltv_channels (id)"
        + ");"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS m3u_channels_mapping_name_to_id ("
        + "    name                 VARCHAR NOT NULL PRIMARY KEY,"
        + "    new_tvg_id           VARCHAR NOT NULL"
        + ");"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS m3u_channels_mapping_id_to_id ("
        + "    old_tvg_id           VARCHAR NOT NULL PRIMARY KEY,"
        + "    new_tvg_id           VARCHAR NOT NULL"
        + ");"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS m3u_channels_media_url_static_values ("
        + "    position             INTEGER NOT NULL PRIMARY KEY,"
        + "    value                VARCHAR NOT NULL"
        + ");"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS m3u_channels_filter_whitelist_names ("
        + "    name                 VARCHAR NOT NULL PRIMARY KEY"
        + ");"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS m3u_channels_filter_whitelist_ids ("
        + "    tvg_id               VARCHAR NOT NULL PRIMARY KEY"
        + ");"
      );
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    Update.resetVersionCache();
  }

  @Override
  public void onUpgrade(SQLiteDatabase dbase, int oldVersion, int newVersion) {
    Update.resetVersionCache();
  }
}
