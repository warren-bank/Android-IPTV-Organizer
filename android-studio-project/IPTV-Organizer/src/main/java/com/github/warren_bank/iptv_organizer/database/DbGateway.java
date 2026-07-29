package com.github.warren_bank.iptv_organizer.database;

import com.github.warren_bank.iptv_organizer.common.Constants;
import com.github.warren_bank.iptv_organizer.data.model.ChannelListItem;
import com.github.warren_bank.iptv_organizer.data.model.EPGDataImpl;

import se.kmdev.tvepg.epg.domain.EPGChannel;
import se.kmdev.tvepg.epg.domain.EPGEvent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbGateway {
  private static DbGateway instance = null;

  private Context     context;
  private SQLiteStore db;

  public static DbGateway getInstance(Context context) {
    if (instance == null) {
      instance = new DbGateway(context.getApplicationContext());
    }
    return instance;
  }

  private DbGateway(Context context) {
    this.context = context;
    this.db      = SQLiteStore.getInstance(context);
  }

  public SQLiteStore getSQLiteStore() {
    return this.db;
  }

  // ---------------------------------------------------------------------------
  // helpers:
  // ---------------------------------------------------------------------------

  private String sqlEscapeString(String value) {
    boolean notNull = false;
    return sqlEscapeString(value, notNull);
  }

  private String sqlEscapeString(String value, boolean notNull) throws NullPointerException {
    if (value == null) {
      if (notNull)
        throw new NullPointerException("illegal call to: DatabaseUtils.sqlEscapeString(null)");
      else
        return "NULL";
    }
    return DatabaseUtils.sqlEscapeString(value);
  }

  private String normalizeEmptyString(String value) {
    String defaultValue = "";
    return normalizeEmptyString(value, defaultValue);
  }

  private String normalizeEmptyString(String value, String defaultValue) {
    List<String> blacklist = null;
    return normalizeEmptyString(value, defaultValue, blacklist);
  }

  private String normalizeEmptyString(String value, String defaultValue, List<String> blacklist) {
    return ((value == null) || value.isEmpty() || value.toLowerCase().equals("null"))
      ? defaultValue
      : ((blacklist != null) && blacklist.contains(value))
        ? defaultValue
        : value;
  }

  private String getColumnString(Cursor c, String columnName) {
    String defaultValue = "";
    return getColumnString(c, columnName, defaultValue);
  }

  private String getColumnString(Cursor c, String columnName, String defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    return normalizeEmptyString(
      c.getString(columnIndex),
      defaultValue
    );
  }

  private int getColumnInteger(Cursor c, String columnName) {
    int defaultValue = -1;
    return getColumnInteger(c, columnName, defaultValue);
  }

  private int getColumnInteger(Cursor c, String columnName, int defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    int value = c.getInt(columnIndex);
    return value;
  }

  private long getColumnLong(Cursor c, String columnName) {
    long defaultValue = -1l;
    return getColumnLong(c, columnName, defaultValue);
  }

  private long getColumnLong(Cursor c, String columnName, long defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    long value = c.getLong(columnIndex);
    return value;
  }

  private float getColumnFloat(Cursor c, String columnName) {
    float defaultValue = -1.0f;
    return getColumnFloat(c, columnName, defaultValue);
  }

  private float getColumnFloat(Cursor c, String columnName, float defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    float value = c.getFloat(columnIndex);
    return value;
  }

  private boolean getColumnBoolean(Cursor c, String columnName) {
    boolean defaultValue = false;
    return getColumnBoolean(c, columnName, defaultValue);
  }

  private boolean getColumnBoolean(Cursor c, String columnName, boolean defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    int value = getColumnInteger(c, columnName, 0);
    return (value == 1);
  }

  private long insertOrThrowUnlessConstraintViolated(SQLiteDatabase dbase, String table, String nullColumnHack, ContentValues values) {
    try {
      return dbase.insertOrThrow(table, nullColumnHack, values);
    }
    catch(SQLException e) {
      if (e instanceof SQLiteConstraintException)
        return -1;
      else
        throw e;
    }
  }

  // ---------------------------------------------------------------------------
  // write models to DB:
  // ---------------------------------------------------------------------------

  public boolean saveM3u(List<ChannelListItem> channels, boolean appendList) {
    SQLiteDatabase dbase = db.getSQLiteDatabase();
    String query;
    ContentValues cvals;
    boolean result = true;
    try {
      dbase.beginTransaction();

      if (!appendList) {
        query = "DELETE FROM m3u_channels";
        dbase.execSQL(query);
        query = null;
      }

      if ((channels != null) && !channels.isEmpty()) {
        for (ChannelListItem channel : channels) {
          if (channel == null) continue;

          cvals = new ContentValues();
          cvals.put("position",  channel.position);
          cvals.put("name",      channel.name);
          cvals.put("media_url", channel.media_url);
          cvals.put("tvg_id",    channel.tvg_id);
          cvals.put("tvg_name",  channel.tvg_name);

          insertOrThrowUnlessConstraintViolated(dbase, "m3u_channels", null, cvals);
          cvals = null;
        }
      }

      dbase.setTransactionSuccessful();
    }
    catch (SQLException e) {
      result = false;
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    finally {
      dbase.endTransaction();
    }
    return result;
  }

  public boolean saveEpgData(EPGDataImpl epgData) {
    Map<EPGChannel, List<EPGEvent>> data = (epgData != null)
      ? epgData.getData()
      : null;

    return saveEpg(data);
  }

  public boolean saveEpg(Map<EPGChannel, List<EPGEvent>> data) {
    SQLiteDatabase dbase = db.getSQLiteDatabase();
    String query;
    ContentValues cvals;
    boolean result = true;
    try {
      dbase.beginTransaction();

      query = "DELETE FROM xmltv_channels";
      dbase.execSQL(query);
      query = null;

      query = "DELETE FROM xmltv_programs";
      dbase.execSQL(query);
      query = null;

      if ((data != null) && !data.isEmpty()) {
        for (Map.Entry<EPGChannel, List<EPGEvent>> entry : data.entrySet()) {
          EPGChannel     channel  = (EPGChannel)     entry.getKey();
          List<EPGEvent> programs = (List<EPGEvent>) entry.getValue();

          cvals = new ContentValues();
          cvals.put("id",       channel.getChannelID());
          cvals.put("name",     channel.getName());
          cvals.put("icon_url", channel.getImageURL());

          insertOrThrowUnlessConstraintViolated(dbase, "xmltv_channels", null, cvals);
          cvals = null;

          for (EPGEvent program : programs) {
            if (program == null) continue;

            cvals = new ContentValues();
            cvals.put("channel_id",         channel.getChannelID());
            cvals.put("start_timestamp_ms", program.getStart());
            cvals.put("stop_timestamp_ms",  program.getEnd());
            cvals.put("title",              program.getTitle());
            cvals.put("description",        program.getDescription());

            insertOrThrowUnlessConstraintViolated(dbase, "xmltv_programs", null, cvals);
            cvals = null;
          }
        }
      }

      dbase.setTransactionSuccessful();
    }
    catch (SQLException e) {
      result = false;
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    finally {
      dbase.endTransaction();
    }
    return result;
  }

  // ---------------------------------------------------------------------------
  // read models from DB:
  // ---------------------------------------------------------------------------

  public List<ChannelListItem> getM3u() {
    List<ChannelListItem> channels = new ArrayList<ChannelListItem>();
    String query = "SELECT * FROM m3u_channels ORDER BY position ASC";
    int position;
    String name, media_url, tvg_id, tvg_name;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          position  = getColumnInteger(c, "position", -1);
          name      = getColumnString (c, "name");
          media_url = getColumnString (c, "media_url");
          tvg_id    = getColumnString (c, "tvg_id");
          tvg_name  = getColumnString (c, "tvg_name");

          ChannelListItem channel = new ChannelListItem(position, name, media_url, tvg_id, tvg_name);
          channels.add(channel);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return channels;
  }

  public EPGDataImpl getEpgData() {
    Map<EPGChannel, List<EPGEvent>> data = getEpg();
    return new EPGDataImpl(data);
  }

  public Map<EPGChannel, List<EPGEvent>> getEpg() {
    Map<EPGChannel, List<EPGEvent>> data = new HashMap<EPGChannel, List<EPGEvent>>();

    List<EPGChannel> channels = getEpgChannels();
    if ((channels != null) && !channels.isEmpty()) {
      for (EPGChannel channel : channels) {
        List<EPGEvent> programs = getEpgEvents(channel);
        if ((programs != null) && !programs.isEmpty()) {
          data.put(channel, programs);
        }
      }
    }

    return data;
  }

  private List<EPGChannel> getEpgChannels() {
    List<EPGChannel> channels = new ArrayList<EPGChannel>();
    String query = "SELECT * FROM xmltv_channels ORDER BY name ASC";
    String id, name, icon_url;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          id       = getColumnString(c, "id");
          name     = getColumnString(c, "name");
          icon_url = getColumnString(c, "icon_url");

          EPGChannel channel = new EPGChannel(id, name, icon_url);
          channels.add(channel);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return channels;
  }

  private List<EPGEvent> getEpgEvents(EPGChannel channel) {
    List<EPGEvent> programs = new ArrayList<EPGEvent>();
    String query = "SELECT * FROM xmltv_programs WHERE channel_id = " + sqlEscapeString(channel.getChannelID()) + " ORDER BY start_timestamp_ms ASC";
    long start_timestamp_ms, stop_timestamp_ms;
    String title, description;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          start_timestamp_ms = getColumnLong  (c, "start_timestamp_ms", -1l);
          stop_timestamp_ms  = getColumnLong  (c, "stop_timestamp_ms", -1l);
          title              = getColumnString(c, "title");
          description        = getColumnString(c, "description");

          EPGEvent program = new EPGEvent(start_timestamp_ms, stop_timestamp_ms, title, description);
          programs.add(program);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return programs;
  }

  // ---------------------------------------------------------------------------
  // write M3U channel mappings to DB:
  // ---------------------------------------------------------------------------

  public boolean saveChannelNameMappings(Map<String, String> data) {
    SQLiteDatabase dbase = db.getSQLiteDatabase();
    String query;
    ContentValues cvals;
    boolean result = true;
    try {
      dbase.beginTransaction();

      query = "DELETE FROM m3u_channels_mapping_name_to_id";
      dbase.execSQL(query);
      query = null;

      if ((data != null) && !data.isEmpty()) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
          String name       = (String) entry.getKey();
          String new_tvg_id = (String) entry.getValue();

          cvals = new ContentValues();
          cvals.put("name",       name);
          cvals.put("new_tvg_id", new_tvg_id);

          insertOrThrowUnlessConstraintViolated(dbase, "m3u_channels_mapping_name_to_id", null, cvals);
          cvals = null;
        }
      }

      dbase.setTransactionSuccessful();
    }
    catch (SQLException e) {
      result = false;
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    finally {
      dbase.endTransaction();
    }
    return result;
  }

  public boolean saveChannelIdMappings(Map<String, String> data) {
    SQLiteDatabase dbase = db.getSQLiteDatabase();
    String query;
    ContentValues cvals;
    boolean result = true;
    try {
      dbase.beginTransaction();

      query = "DELETE FROM m3u_channels_mapping_id_to_id";
      dbase.execSQL(query);
      query = null;

      if ((data != null) && !data.isEmpty()) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
          String old_tvg_id = (String) entry.getKey();
          String new_tvg_id = (String) entry.getValue();

          cvals = new ContentValues();
          cvals.put("old_tvg_id", old_tvg_id);
          cvals.put("new_tvg_id", new_tvg_id);

          insertOrThrowUnlessConstraintViolated(dbase, "m3u_channels_mapping_id_to_id", null, cvals);
          cvals = null;
        }
      }

      dbase.setTransactionSuccessful();
    }
    catch (SQLException e) {
      result = false;
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    finally {
      dbase.endTransaction();
    }
    return result;
  }

  // ---------------------------------------------------------------------------
  // read M3U channel mappings from DB:
  // ---------------------------------------------------------------------------

  public Map<String, String> getChannelNameMappings() {
    Map<String, String> data = new HashMap<String, String>();
    String query = "SELECT * FROM m3u_channels_mapping_name_to_id ORDER BY name ASC";
    String name, new_tvg_id;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          name       = getColumnString(c, "name");
          new_tvg_id = getColumnString(c, "new_tvg_id");

          data.put(name, new_tvg_id);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return data;
  }

  public Map<String, String> getChannelIdMappings() {
    Map<String, String> data = new HashMap<String, String>();
    String query = "SELECT * FROM m3u_channels_mapping_id_to_id ORDER BY old_tvg_id ASC";
    String old_tvg_id, new_tvg_id;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          old_tvg_id = getColumnString(c, "old_tvg_id");
          new_tvg_id = getColumnString(c, "new_tvg_id");

          data.put(old_tvg_id, new_tvg_id);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return data;
  }

  // ---------------------------------------------------------------------------
  // write M3U channel url static values to DB:
  // ---------------------------------------------------------------------------

  public boolean saveChannelUrlStaticValues(List<String> values) {
    SQLiteDatabase dbase = db.getSQLiteDatabase();
    String query;
    ContentValues cvals;
    boolean result = true;
    try {
      dbase.beginTransaction();

      query = "DELETE FROM m3u_channels_media_url_static_values";
      dbase.execSQL(query);
      query = null;

      if ((values != null) && !values.isEmpty()) {
        int position = 1;

        for (String value : values) {
          if (TextUtils.isEmpty(value)) continue;

          cvals = new ContentValues();
          cvals.put("position", position);
          cvals.put("value",    value);

          insertOrThrowUnlessConstraintViolated(dbase, "m3u_channels_media_url_static_values", null, cvals);
          cvals = null;
          position += 1;
        }
      }

      dbase.setTransactionSuccessful();
    }
    catch (SQLException e) {
      result = false;
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    finally {
      dbase.endTransaction();
    }
    return result;
  }

  // ---------------------------------------------------------------------------
  // read M3U channel url static values from DB:
  // ---------------------------------------------------------------------------

  public List<String> getChannelUrlStaticValues() {
    List<String> values = new ArrayList<String>();
    String query = "SELECT * FROM m3u_channels_media_url_static_values ORDER BY position ASC";
    String value;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          value = getColumnString(c, "value");

          values.add(value);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return values;
  }

  // ---------------------------------------------------------------------------
  // write M3U channel filter whitelists to DB:
  // ---------------------------------------------------------------------------

  public boolean saveChannelNameFilterWhitelist(List<String> names) {
    SQLiteDatabase dbase = db.getSQLiteDatabase();
    String query;
    ContentValues cvals;
    boolean result = true;
    try {
      dbase.beginTransaction();

      query = "DELETE FROM m3u_channels_filter_whitelist_names";
      dbase.execSQL(query);
      query = null;

      if ((names != null) && !names.isEmpty()) {
        for (String name : names) {
          if (TextUtils.isEmpty(name)) continue;

          cvals = new ContentValues();
          cvals.put("name", name);

          insertOrThrowUnlessConstraintViolated(dbase, "m3u_channels_filter_whitelist_names", null, cvals);
          cvals = null;
        }
      }

      dbase.setTransactionSuccessful();
    }
    catch (SQLException e) {
      result = false;
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    finally {
      dbase.endTransaction();
    }
    return result;
  }

  public boolean saveChannelIdFilterWhitelist(List<String> ids) {
    SQLiteDatabase dbase = db.getSQLiteDatabase();
    String query;
    ContentValues cvals;
    boolean result = true;
    try {
      dbase.beginTransaction();

      query = "DELETE FROM m3u_channels_filter_whitelist_ids";
      dbase.execSQL(query);
      query = null;

      if ((ids != null) && !ids.isEmpty()) {
        for (String tvg_id : ids) {
          if (TextUtils.isEmpty(tvg_id)) continue;

          cvals = new ContentValues();
          cvals.put("tvg_id", tvg_id);

          insertOrThrowUnlessConstraintViolated(dbase, "m3u_channels_filter_whitelist_ids", null, cvals);
          cvals = null;
        }
      }

      dbase.setTransactionSuccessful();
    }
    catch (SQLException e) {
      result = false;
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    finally {
      dbase.endTransaction();
    }
    return result;
  }

  // ---------------------------------------------------------------------------
  // read M3U channel filter whitelists from DB:
  // ---------------------------------------------------------------------------

  public List<String> getChannelNameFilterWhitelist() {
    List<String> names = new ArrayList<String>();
    String query = "SELECT * FROM m3u_channels_filter_whitelist_names ORDER BY name ASC";
    String name;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          name = getColumnString(c, "name");

          names.add(name);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return names;
  }

  public List<String> getChannelNameFilterWhitelistSubset(boolean getSubstringPatterns) {
    List<String> names = new ArrayList<String>();
    String query = "SELECT name FROM m3u_channels_filter_whitelist_names WHERE name" + (getSubstringPatterns ? "" : " NOT") + " LIKE " + sqlEscapeString(Constants.M3U_CHANNELS_FILTER_WHITELIST_NAMES_BY_SUBSTRING_TOKEN + "%") + " ORDER BY name ASC";
    String name;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          name = getColumnString(c, "name");

          // remove leading token sequence from substring patterns
          if (getSubstringPatterns)
            name = name.substring(Constants.M3U_CHANNELS_FILTER_WHITELIST_NAMES_BY_SUBSTRING_TOKEN.length());

          names.add(name);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return names;
  }

  public List<String> getChannelIdFilterWhitelist() {
    List<String> ids = new ArrayList<String>();
    String query = "SELECT * FROM m3u_channels_filter_whitelist_ids ORDER BY tvg_id ASC";
    String tvg_id;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          tvg_id = getColumnString(c, "tvg_id");

          ids.add(tvg_id);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return ids;
  }

  // ---------------------------------------------------------------------------
  // write M3U channel filter blacklists to DB:
  // ---------------------------------------------------------------------------

  public boolean saveChannelNameFilterBlacklist(List<String> names) {
    SQLiteDatabase dbase = db.getSQLiteDatabase();
    String query;
    ContentValues cvals;
    boolean result = true;
    try {
      dbase.beginTransaction();

      query = "DELETE FROM m3u_channels_filter_blacklist_names";
      dbase.execSQL(query);
      query = null;

      if ((names != null) && !names.isEmpty()) {
        for (String name : names) {
          if (TextUtils.isEmpty(name)) continue;

          cvals = new ContentValues();
          cvals.put("name", name);

          insertOrThrowUnlessConstraintViolated(dbase, "m3u_channels_filter_blacklist_names", null, cvals);
          cvals = null;
        }
      }

      dbase.setTransactionSuccessful();
    }
    catch (SQLException e) {
      result = false;
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    finally {
      dbase.endTransaction();
    }
    return result;
  }

  public boolean saveChannelIdFilterBlacklist(List<String> ids) {
    SQLiteDatabase dbase = db.getSQLiteDatabase();
    String query;
    ContentValues cvals;
    boolean result = true;
    try {
      dbase.beginTransaction();

      query = "DELETE FROM m3u_channels_filter_blacklist_ids";
      dbase.execSQL(query);
      query = null;

      if ((ids != null) && !ids.isEmpty()) {
        for (String tvg_id : ids) {
          if (TextUtils.isEmpty(tvg_id)) continue;

          cvals = new ContentValues();
          cvals.put("tvg_id", tvg_id);

          insertOrThrowUnlessConstraintViolated(dbase, "m3u_channels_filter_blacklist_ids", null, cvals);
          cvals = null;
        }
      }

      dbase.setTransactionSuccessful();
    }
    catch (SQLException e) {
      result = false;
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    finally {
      dbase.endTransaction();
    }
    return result;
  }

  // ---------------------------------------------------------------------------
  // read M3U channel filter blacklists from DB:
  // ---------------------------------------------------------------------------

  public List<String> getChannelNameFilterBlacklist() {
    List<String> names = new ArrayList<String>();
    String query = "SELECT * FROM m3u_channels_filter_blacklist_names ORDER BY name ASC";
    String name;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          name = getColumnString(c, "name");

          names.add(name);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return names;
  }

  public List<String> getChannelNameFilterBlacklistSubset(boolean getSubstringPatterns) {
    List<String> names = new ArrayList<String>();
    String query = "SELECT name FROM m3u_channels_filter_blacklist_names WHERE name" + (getSubstringPatterns ? "" : " NOT") + " LIKE " + sqlEscapeString(Constants.M3U_CHANNELS_FILTER_BLACKLIST_NAMES_BY_SUBSTRING_TOKEN + "%") + " ORDER BY name ASC";
    String name;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          name = getColumnString(c, "name");

          // remove leading token sequence from substring patterns
          if (getSubstringPatterns)
            name = name.substring(Constants.M3U_CHANNELS_FILTER_BLACKLIST_NAMES_BY_SUBSTRING_TOKEN.length());

          names.add(name);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return names;
  }

  public List<String> getChannelIdFilterBlacklist() {
    List<String> ids = new ArrayList<String>();
    String query = "SELECT * FROM m3u_channels_filter_blacklist_ids ORDER BY tvg_id ASC";
    String tvg_id;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          tvg_id = getColumnString(c, "tvg_id");

          ids.add(tvg_id);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return ids;
  }

  // ---------------------------------------------------------------------------
  // misc business logic:
  // ---------------------------------------------------------------------------

  public String getM3uMediaUrlForEpgChannel(EPGChannel epgChannel) {
    if (epgChannel == null) return null;

    String query = "SELECT media_url FROM m3u_channels WHERE media_url IS NOT NULL AND (name = " + sqlEscapeString(epgChannel.getName()) + " OR tvg_name = " + sqlEscapeString(epgChannel.getName()) + " OR tvg_id = " + sqlEscapeString(epgChannel.getChannelID()) + ") ORDER BY position ASC LIMIT 1";
    String media_url = null;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        media_url = getColumnString(c, "media_url", media_url);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return media_url;
  }

}
