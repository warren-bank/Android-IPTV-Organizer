package com.github.warren_bank.iptv_organizer.database;

import com.github.warren_bank.iptv_organizer.common.Constants;
import com.github.warren_bank.iptv_organizer.data.DataProgressListener;
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

  public boolean saveM3u(List<ChannelListItem> channels, boolean appendList, DataProgressListener listener) {
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

          if (listener != null) listener.onData(channel.name);

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

  public boolean saveEpgData(EPGDataImpl epgData, DataProgressListener listener) {
    Map<EPGChannel, List<EPGEvent>> data = (epgData != null)
      ? epgData.getData()
      : null;

    return saveEpg(data, listener);
  }

  public boolean saveEpg(Map<EPGChannel, List<EPGEvent>> data, DataProgressListener listener) {
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

          if (listener != null) listener.onData(channel.getName());

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
    return getM3u(null);
  }

  public List<ChannelListItem> getM3u(DataProgressListener listener) {
    List<ChannelListItem> channels = new ArrayList<ChannelListItem>();
    String query = "SELECT * FROM m3u_channels ORDER BY position ASC";

    Cursor c = null;
    try {
      c = db.query(query);

      readM3uResults(channels, c, listener);
    }
    catch (Exception e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return channels;
  }

  public List<ChannelListItem> searchM3u(String[] keywords, int LIMIT) {
    return searchM3u(keywords, LIMIT, 2, null);
  }

  public List<ChannelListItem> searchM3u(String[] keywords, int LIMIT, int minKeywordLength, DataProgressListener listener) {
    List<ChannelListItem> channels = new ArrayList<ChannelListItem>();

    ArrayList<String> conditions = new ArrayList<String>();
    for (String keyword : keywords) {
      if (keyword.length() >= minKeywordLength) {
        conditions.add("name"     + " LIKE " + sqlEscapeString("%" + keyword + "%")); // note: LIKE is always case-insensitive
        conditions.add("tvg_name" + " LIKE " + sqlEscapeString("%" + keyword + "%"));
      }
    }

    if (conditions.isEmpty())
      return channels;

    String WHERE = " WHERE " + TextUtils.join(" OR ", conditions);
    String query = "SELECT * FROM m3u_channels" + WHERE + " ORDER BY name ASC LIMIT " + LIMIT;

    Cursor c = null;
    try {
      c = db.query(query);

      readM3uResults(channels, c, listener);
    }
    catch (Exception e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return channels;
  }

  private void readM3uResults(List<ChannelListItem> channels, Cursor c, DataProgressListener listener) throws Exception {
    if (channels == null) throw new Exception("readM3uResults: 1st parameter must be a non-null List");

    int position;
    String name, media_url, tvg_id, tvg_name;

    if ((c != null) && c.moveToFirst() && c.isFirst()) {
      do {
        position  = getColumnInteger(c, "position", -1);
        name      = getColumnString (c, "name");
        media_url = getColumnString (c, "media_url");
        tvg_id    = getColumnString (c, "tvg_id");
        tvg_name  = getColumnString (c, "tvg_name");

        if (listener != null) listener.onData(name);

        ChannelListItem channel = new ChannelListItem(position, name, media_url, tvg_id, tvg_name);
        channels.add(channel);
      } while (c.moveToNext());
    }
  }

  public EPGDataImpl getEpgData() {
    return getEpgData(null);
  }

  public EPGDataImpl getEpgData(DataProgressListener listener) {
    Map<EPGChannel, List<EPGEvent>> data = getEpg(listener);
    return new EPGDataImpl(data);
  }

  public Map<EPGChannel, List<EPGEvent>> getEpg() {
    return getEpg(null);
  }

  public Map<EPGChannel, List<EPGEvent>> getEpg(DataProgressListener listener) {
    Map<EPGChannel, List<EPGEvent>> data = new HashMap<EPGChannel, List<EPGEvent>>();

    List<EPGChannel> channels = getEpgChannels(listener);
    if ((channels != null) && !channels.isEmpty()) {
      for (EPGChannel channel : channels) {
        List<EPGEvent> programs = getEpgEvents(channel, listener);
        if ((programs != null) && !programs.isEmpty()) {
          data.put(channel, programs);
        }
      }
    }

    return data;
  }

  private List<EPGChannel> getEpgChannels(DataProgressListener listener) {
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

          if (listener != null) listener.onData(name);

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

  private List<EPGEvent> getEpgEvents(EPGChannel channel, DataProgressListener listener) {
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

          if (listener != null) listener.onData(title);

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
  // write M3U channel url static values to DB:
  // ---------------------------------------------------------------------------

  public boolean saveM3uChannelUrlStaticValues(List<String> values) {
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

  public List<String> getM3uChannelUrlStaticValues() {
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
  // internal: encode/decode fields that contain prefix flags
  // ---------------------------------------------------------------------------

  private static class FieldFlags {
    public boolean case_insensitive;
    public boolean match_substring;
    public String encoded;
    public String decoded;

    public static int toInt(boolean value) {
      return value ? 1 : 0;
    }

    public FieldFlags(String encoded) {
      if (encoded != null) encoded = encoded.trim();
      int length = (encoded == null) ? 0 : encoded.length();
      if (length == 0) encoded = null;

      this.case_insensitive = false;
      this.match_substring  = false;
      this.encoded          = encoded;
      this.decoded          = encoded;

      int index = 0;
      while (length > index) {
        char c = encoded.charAt(index);

        if ((index == 0) && (c == '#')) {
          index += 1;
          continue;
        }

        if (c == '~') {
          this.case_insensitive = true;
          index += 1;
          continue;
        }

        if (c == '*') {
          this.match_substring = true;
          index += 1;
          continue;
        }

        break;
      }

      if (index > 0) {
        this.decoded = encoded.substring(index).trim();
      }

      if (this.case_insensitive) {
        this.encoded = this.encoded.toLowerCase();
        this.decoded = this.decoded.toLowerCase();
      }
    }

    public FieldFlags(int case_insensitive, int match_substring, String decoded) {
      this.case_insensitive = (case_insensitive == 1);
      this.match_substring  = (match_substring  == 1);

      if (decoded != null) {
        decoded = decoded.trim();
        if (this.case_insensitive) decoded = decoded.toLowerCase();
      }

      this.decoded = decoded;
      this.encoded = decoded;

      if ((decoded != null) && (this.case_insensitive || this.match_substring)) {
        StringBuffer sb = new StringBuffer();
        sb.append('#');
        if (this.case_insensitive) sb.append('~');
        if (this.match_substring)  sb.append('*');
        sb.append(decoded);

        this.encoded = sb.toString();
      }
    }
  }

  // ---------------------------------------------------------------------------
  // internal: write generic channel mappings to DB:
  // ---------------------------------------------------------------------------

  private boolean saveChannelMappings(Map<String, String> data, String tableName, String field1Name, String field2Name, boolean hasFieldFlags) {
    SQLiteDatabase dbase = db.getSQLiteDatabase();
    String query;
    ContentValues cvals;
    boolean result = true;
    try {
      dbase.beginTransaction();

      query = "DELETE FROM " + tableName;
      dbase.execSQL(query);
      query = null;

      if ((data != null) && !data.isEmpty()) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
          String field1Value = (String) entry.getKey();
          String field2Value = (String) entry.getValue();

          cvals = new ContentValues();

          if (hasFieldFlags) {
            FieldFlags flags = new FieldFlags(field1Value);

            cvals.put(field1Name, flags.decoded);
            cvals.put(field2Name, field2Value);

            cvals.put("case_insensitive", FieldFlags.toInt(flags.case_insensitive));
            cvals.put("match_substring",  FieldFlags.toInt(flags.match_substring));
          }
          else {
            cvals.put(field1Name, field1Value);
            cvals.put(field2Name, field2Value);
          }

          insertOrThrowUnlessConstraintViolated(dbase, tableName, null, cvals);
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
  // internal: read generic channel mappings from DB:
  // ---------------------------------------------------------------------------

  private Map<String, String> getChannelMappings(String tableName, String field1Name, String field2Name, boolean hasFieldFlags) {
    Map<String, String> data = new HashMap<String, String>();
    String query = "SELECT * FROM " + tableName + " ORDER BY " + field1Name + " ASC";
    String field1Value, field2Value;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          field1Value = getColumnString(c, field1Name);
          field2Value = getColumnString(c, field2Name);

          if (hasFieldFlags) {
            int case_insensitive = getColumnInteger(c, "case_insensitive");
            int match_substring  = getColumnInteger(c, "match_substring");

            FieldFlags flags = new FieldFlags(case_insensitive, match_substring, field1Value);
            field1Value = flags.encoded;
          }

          data.put(field1Value, field2Value);
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
  // internal: read subset of decoded generic channel mappings from DB:
  // notes:
  //   * can only be used if tableName hasFieldFlags
  //   * map keys are decoded, and do not include a prefix with encoded flags
  // ---------------------------------------------------------------------------

  private Map<String, String> getChannelMappingsSubset(String tableName, String field1Name, String field2Name, Boolean case_insensitive, Boolean match_substring) {
    Map<String, String> data = new HashMap<String, String>();

    ArrayList<String> conditions = new ArrayList<String>();
    if (case_insensitive != null) {
      conditions.add("case_insensitive = " + FieldFlags.toInt(case_insensitive));
    }
    if (match_substring != null) {
      conditions.add("match_substring = " + FieldFlags.toInt(match_substring));
    }
    String WHERE = !conditions.isEmpty()
      ? (" WHERE " + TextUtils.join(" AND ", conditions))
      : "";

    String query = "SELECT " + field1Name + ", " + field2Name + " FROM " + tableName + WHERE + " ORDER BY " + field1Name + " ASC";
    String field1Value, field2Value;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          field1Value = getColumnString(c, field1Name);
          field2Value = getColumnString(c, field2Name);

          data.put(field1Value, field2Value);
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
  // internal: write generic channel filter lists to DB:
  // ---------------------------------------------------------------------------

  private boolean saveChannelFilterList(List<String> fieldValues, String tableName, String fieldName, boolean hasFieldFlags) {
    SQLiteDatabase dbase = db.getSQLiteDatabase();
    String query;
    ContentValues cvals;
    boolean result = true;
    try {
      dbase.beginTransaction();

      query = "DELETE FROM " + tableName;
      dbase.execSQL(query);
      query = null;

      if ((fieldValues != null) && !fieldValues.isEmpty()) {
        for (String fieldValue : fieldValues) {
          if (TextUtils.isEmpty(fieldValue)) continue;

          cvals = new ContentValues();

          if (hasFieldFlags) {
            FieldFlags flags = new FieldFlags(fieldValue);

            cvals.put(fieldName, flags.decoded);

            cvals.put("case_insensitive", FieldFlags.toInt(flags.case_insensitive));
            cvals.put("match_substring",  FieldFlags.toInt(flags.match_substring));
          }
          else {
            cvals.put(fieldName, fieldValue);
          }

          insertOrThrowUnlessConstraintViolated(dbase, tableName, null, cvals);
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

  private boolean saveChannelNameFilterList(List<String> fieldValues, String tableName) {
    String fieldName      = "name";
    boolean hasFieldFlags = true;

    return saveChannelFilterList(fieldValues, tableName, fieldName, hasFieldFlags);
  }

  private boolean saveChannelIdFilterList(List<String> fieldValues, String tableName) {
    String fieldName      = "tvg_id";
    boolean hasFieldFlags = false;

    return saveChannelFilterList(fieldValues, tableName, fieldName, hasFieldFlags);
  }

  // ---------------------------------------------------------------------------
  // internal: read generic channel filter lists from DB:
  // ---------------------------------------------------------------------------

  private List<String> getChannelFilterList(String tableName, String fieldName, boolean hasFieldFlags) {
    List<String> fieldValues = new ArrayList<String>();
    String query = "SELECT * FROM " + tableName + " ORDER BY " + fieldName + " ASC";
    String fieldValue;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          fieldValue = getColumnString(c, fieldName);

          if (hasFieldFlags) {
            int case_insensitive = getColumnInteger(c, "case_insensitive");
            int match_substring  = getColumnInteger(c, "match_substring");

            FieldFlags flags = new FieldFlags(case_insensitive, match_substring, fieldValue);
            fieldValue = flags.encoded;
          }

          fieldValues.add(fieldValue);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return fieldValues;
  }

  private List<String> getChannelNameFilterList(String tableName) {
    String fieldName      = "name";
    boolean hasFieldFlags = true;

    return getChannelFilterList(tableName, fieldName, hasFieldFlags);
  }

  private List<String> getChannelIdFilterList(String tableName) {
    String fieldName      = "tvg_id";
    boolean hasFieldFlags = false;

    return getChannelFilterList(tableName, fieldName, hasFieldFlags);
  }

  // ---------------------------------------------------------------------------
  // internal: read subset of decoded generic channel filter lists from DB:
  // notes:
  //   * can only be used if tableName hasFieldFlags
  //   * list items are decoded, and do not include a prefix with encoded flags
  // ---------------------------------------------------------------------------

  private List<String> getChannelFilterListSubset(String tableName, String fieldName, Boolean case_insensitive, Boolean match_substring) {
    List<String> fieldValues = new ArrayList<String>();

    ArrayList<String> conditions = new ArrayList<String>();
    if (case_insensitive != null) {
      conditions.add("case_insensitive = " + FieldFlags.toInt(case_insensitive));
    }
    if (match_substring != null) {
      conditions.add("match_substring = " + FieldFlags.toInt(match_substring));
    }
    String WHERE = !conditions.isEmpty()
      ? (" WHERE " + TextUtils.join(" AND ", conditions))
      : "";

    String query = "SELECT " + fieldName + " FROM " + tableName + WHERE + " ORDER BY " + fieldName + " ASC";
    String fieldValue;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          fieldValue = getColumnString(c, fieldName);

          fieldValues.add(fieldValue);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return fieldValues;
  }

  private List<String> getChannelNameFilterListSubset(String tableName, Boolean case_insensitive, Boolean match_substring) {
    String fieldName = "name";

    return getChannelFilterListSubset(tableName, fieldName, case_insensitive, match_substring);
  }

  // ---------------------------------------------------------------------------
  // write M3U channel mappings to DB:
  // ---------------------------------------------------------------------------

  public boolean saveM3uChannelNameMappings(Map<String, String> data) {
    String tableName      = "m3u_channels_mapping_name_to_id";
    String field1Name     = "name";
    String field2Name     = "new_tvg_id";
    boolean hasFieldFlags = true;

    return saveChannelMappings(data, tableName, field1Name, field2Name, hasFieldFlags);
  }

  public boolean saveM3uChannelIdMappings(Map<String, String> data) {
    String tableName      = "m3u_channels_mapping_id_to_id";
    String field1Name     = "old_tvg_id";
    String field2Name     = "new_tvg_id";
    boolean hasFieldFlags = false;

    return saveChannelMappings(data, tableName, field1Name, field2Name, hasFieldFlags);
  }

  // ---------------------------------------------------------------------------
  // read M3U channel mappings from DB:
  // ---------------------------------------------------------------------------

  public Map<String, String> getM3uChannelNameMappings() {
    String tableName      = "m3u_channels_mapping_name_to_id";
    String field1Name     = "name";
    String field2Name     = "new_tvg_id";
    boolean hasFieldFlags = true;

    return getChannelMappings(tableName, field1Name, field2Name, hasFieldFlags);
  }

  public Map<String, String> getM3uChannelNameMappingsSubset(Boolean case_insensitive, Boolean match_substring) {
    String tableName      = "m3u_channels_mapping_name_to_id";
    String field1Name     = "name";
    String field2Name     = "new_tvg_id";

    return getChannelMappingsSubset(tableName, field1Name, field2Name, case_insensitive, match_substring);
  }

  public Map<String, String> getM3uChannelIdMappings() {
    String tableName      = "m3u_channels_mapping_id_to_id";
    String field1Name     = "old_tvg_id";
    String field2Name     = "new_tvg_id";
    boolean hasFieldFlags = false;

    return getChannelMappings(tableName, field1Name, field2Name, hasFieldFlags);
  }

  // ---------------------------------------------------------------------------
  // write M3U channel filter whitelists to DB:
  // ---------------------------------------------------------------------------

  public boolean saveM3uChannelNameFilterWhitelist(List<String> names) {
    String tableName = "m3u_channels_filter_whitelist_names";
    return saveChannelNameFilterList(names, tableName);
  }

  public boolean saveM3uChannelIdFilterWhitelist(List<String> ids) {
    String tableName = "m3u_channels_filter_whitelist_ids";
    return saveChannelIdFilterList(ids, tableName);
  }

  // ---------------------------------------------------------------------------
  // read M3U channel filter whitelists from DB:
  // ---------------------------------------------------------------------------

  public List<String> getM3uChannelNameFilterWhitelist() {
    String tableName = "m3u_channels_filter_whitelist_names";
    return getChannelNameFilterList(tableName);
  }

  public List<String> getM3uChannelNameFilterWhitelistSubset(Boolean case_insensitive, Boolean match_substring) {
    String tableName = "m3u_channels_filter_whitelist_names";
    return getChannelNameFilterListSubset(tableName, case_insensitive, match_substring);
  }

  public List<String> getM3uChannelIdFilterWhitelist() {
    String tableName = "m3u_channels_filter_whitelist_ids";
    return getChannelIdFilterList(tableName);
  }

  // ---------------------------------------------------------------------------
  // write M3U channel filter blacklists to DB:
  // ---------------------------------------------------------------------------

  public boolean saveM3uChannelNameFilterBlacklist(List<String> names) {
    String tableName = "m3u_channels_filter_blacklist_names";
    return saveChannelNameFilterList(names, tableName);
  }

  public boolean saveM3uChannelIdFilterBlacklist(List<String> ids) {
    String tableName = "m3u_channels_filter_blacklist_ids";
    return saveChannelIdFilterList(ids, tableName);
  }

  // ---------------------------------------------------------------------------
  // read M3U channel filter blacklists from DB:
  // ---------------------------------------------------------------------------

  public List<String> getM3uChannelNameFilterBlacklist() {
    String tableName = "m3u_channels_filter_blacklist_names";
    return getChannelNameFilterList(tableName);
  }

  public List<String> getM3uChannelNameFilterBlacklistSubset(Boolean case_insensitive, Boolean match_substring) {
    String tableName = "m3u_channels_filter_blacklist_names";
    return getChannelNameFilterListSubset(tableName, case_insensitive, match_substring);
  }

  public List<String> getM3uChannelIdFilterBlacklist() {
    String tableName = "m3u_channels_filter_blacklist_ids";
    return getChannelIdFilterList(tableName);
  }

  // ---------------------------------------------------------------------------
  // write EPG channel filter whitelists to DB:
  // ---------------------------------------------------------------------------

  public boolean saveEpgChannelNameFilterWhitelist(List<String> names) {
    String tableName = "epg_channels_filter_whitelist_names";
    return saveChannelNameFilterList(names, tableName);
  }

  public boolean saveEpgChannelIdFilterWhitelist(List<String> ids) {
    String tableName = "epg_channels_filter_whitelist_ids";
    return saveChannelIdFilterList(ids, tableName);
  }

  // ---------------------------------------------------------------------------
  // read EPG channel filter whitelists from DB:
  // ---------------------------------------------------------------------------

  public List<String> getEpgChannelNameFilterWhitelist() {
    String tableName = "epg_channels_filter_whitelist_names";
    return getChannelNameFilterList(tableName);
  }

  public List<String> getEpgChannelNameFilterWhitelistSubset(Boolean case_insensitive, Boolean match_substring) {
    String tableName = "epg_channels_filter_whitelist_names";
    return getChannelNameFilterListSubset(tableName, case_insensitive, match_substring);
  }

  public List<String> getEpgChannelIdFilterWhitelist() {
    String tableName = "epg_channels_filter_whitelist_ids";
    return getChannelIdFilterList(tableName);
  }

  // ---------------------------------------------------------------------------
  // write EPG channel filter blacklists to DB:
  // ---------------------------------------------------------------------------

  public boolean saveEpgChannelNameFilterBlacklist(List<String> names) {
    String tableName = "epg_channels_filter_blacklist_names";
    return saveChannelNameFilterList(names, tableName);
  }

  public boolean saveEpgChannelIdFilterBlacklist(List<String> ids) {
    String tableName = "epg_channels_filter_blacklist_ids";
    return saveChannelIdFilterList(ids, tableName);
  }

  // ---------------------------------------------------------------------------
  // read EPG channel filter blacklists from DB:
  // ---------------------------------------------------------------------------

  public List<String> getEpgChannelNameFilterBlacklist() {
    String tableName = "epg_channels_filter_blacklist_names";
    return getChannelNameFilterList(tableName);
  }

  public List<String> getEpgChannelNameFilterBlacklistSubset(Boolean case_insensitive, Boolean match_substring) {
    String tableName = "epg_channels_filter_blacklist_names";
    return getChannelNameFilterListSubset(tableName, case_insensitive, match_substring);
  }

  public List<String> getEpgChannelIdFilterBlacklist() {
    String tableName = "epg_channels_filter_blacklist_ids";
    return getChannelIdFilterList(tableName);
  }

  // ---------------------------------------------------------------------------
  // saved search keywords:
  // ---------------------------------------------------------------------------

  public boolean setSavedSearchKeywordsList(List<String> keywordsList) {
    String tableName      = "saved_search_keywords_list";
    String fieldName      = "search_keywords";
    boolean hasFieldFlags = false;

    return saveChannelFilterList(keywordsList, tableName, fieldName, hasFieldFlags);
  }

  public List<String> getSavedSearchKeywordsList() {
    String tableName      = "saved_search_keywords_list";
    String fieldName      = "search_keywords";
    boolean hasFieldFlags = false;

    return getChannelFilterList(tableName, fieldName, hasFieldFlags);
  }

  public boolean addSavedSearchKeywordsListItem(String keywords) {
    String query = "INSERT INTO saved_search_keywords_list (search_keywords) VALUES (" + sqlEscapeString(keywords) + ")";
    return db.execQuery(query);
  }

  public boolean removeSavedSearchKeywordsListItem(String keywords) {
    String query = "DELETE FROM saved_search_keywords_list WHERE search_keywords = " + sqlEscapeString(keywords);
    return db.execQuery(query);
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
