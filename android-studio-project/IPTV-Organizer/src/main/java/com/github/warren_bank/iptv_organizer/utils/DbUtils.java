package com.github.warren_bank.iptv_organizer.utils;

import com.github.warren_bank.iptv_organizer.utils.SettingsUtils;
import com.github.warren_bank.iptv_organizer.utils.StreamUtils;

import com.github.warren_bank.iptv_organizer.database.DbGateway;
import com.github.warren_bank.iptv_organizer.database.Update;

import se.kmdev.tvepg.epg.domain.EPGChannel;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbUtils {
  private static DbGateway db = null;

  public static void initDb(Context context) {
    if (DbUtils.db == null) {
      DbUtils.db = DbGateway.getInstance(context.getApplicationContext());

      Update update = new Update(context);
      update.updateDatabase(null, 0, null, true);
    }
  }

  public static DbGateway getDb() {
    return DbUtils.db;
  }

  // ---------------------------------------------------------------------------
  // backup and restore:
  // ---------------------------------------------------------------------------

  private static String getDbPath() {
    return DbUtils.db.getSQLiteStore().getSQLiteDatabase().getPath();
  }

  private static File getDbFile() {
    return new File(getDbPath());
  }

  public static void doDbBackup(Context context, OutputStream outputStream) {
    File dbFile = getDbFile();

    DbUtils.db.getSQLiteStore().close();
    StreamUtils.pipeFromFile(dbFile, outputStream);
    DbUtils.db.getSQLiteStore().open(context);
  }

  public static void doDbRestore(Context context, InputStream inputStream) {
    File dbFile = getDbFile();

    DbUtils.db.getSQLiteStore().close();
    StreamUtils.pipeToFile(inputStream, dbFile);
    DbUtils.db.getSQLiteStore().open(context);
  }

  // ---------------------------------------------------------------------------
  // serialize/deserialize M3U channel mappings:
  // ---------------------------------------------------------------------------

  public static String encodeMap(Map<String, String> map) {
    if ((map == null) || map.isEmpty()) return null;

    try {
      ArrayList<String> lines = new ArrayList<String>();

      for (Map.Entry<String, String> entry : map.entrySet()) {
        String key = (String) entry.getKey();
        String val = (String) entry.getValue();

        lines.add(key + "  =>  " + val);
      }

      return TextUtils.join("\n", lines);
    }
    catch(Exception e) {
      return null;
    }
  }

  public static Map<String, String> decodeMap(String encodedMap) {
    if (TextUtils.isEmpty(encodedMap)) return null;

    try {
      Map<String, String> map = new HashMap<String, String>();
      String[] lines = TextUtils.split(encodedMap.trim(), "\\s*[\r\n]+\\s*");
      String value_sep = "\\s+=>\\s+";

      for (String line : lines) {
        if (TextUtils.isEmpty(line)) continue;

        String[] values = TextUtils.split(line, value_sep);

        if (values.length == 2) {
          map.put(values[0], values[1]);
        }
      }

      return map;
    }
    catch(Exception e) {
      return null;
    }
  }

  // ---------------------------------------------------------------------------
  // serialize/deserialize M3U channel url static values, and filter whitelists:
  // ---------------------------------------------------------------------------

  public static String encodeList(List<String> list) {
    if ((list == null) || list.isEmpty()) return null;

    try {
      return TextUtils.join("\n", list);
    }
    catch(Exception e) {
      return null;
    }
  }

  public static List<String> decodeList(String encodedList) {
    if (TextUtils.isEmpty(encodedList)) return null;

    try {
      String[] lines = TextUtils.split(encodedList.trim(), "\\s*[\r\n]+\\s*");
      return Arrays.asList(lines);
    }
    catch(Exception e) {
      return null;
    }
  }

  // ---------------------------------------------------------------------------
  // resolve M3U channel url from template + static values
  // ---------------------------------------------------------------------------

  public static String resolveM3uMediaUrl(String template) {
    if (TextUtils.isEmpty(template)) return null;

    List<String> values = DbUtils.getDb().getM3uChannelUrlStaticValues();

    return ((values == null) || values.isEmpty())
      ? template
      : String.format(template, values.toArray(new String[0]));
  }

  public static String getM3uMediaUrlForEpgChannel(EPGChannel epgChannel) {
    String media_url = DbUtils.getDb().getM3uMediaUrlForEpgChannel(epgChannel);
    return DbUtils.resolveM3uMediaUrl(media_url);
  }

  public static String getDefaultM3uUrlPreference(Context context) {
    String url = SettingsUtils.getDefaultM3uUrlPreference(context);

    if (!TextUtils.isEmpty(url) && SettingsUtils.getApplyDefaultUrlTemplates(context))
      url = DbUtils.resolveM3uMediaUrl(url);

    return url;
  }

  public static String getDefaultXmltvEpgUrlPreference(Context context) {
    String url = SettingsUtils.getDefaultXmltvEpgUrlPreference(context);

    if (!TextUtils.isEmpty(url) && SettingsUtils.getApplyDefaultUrlTemplates(context))
      url = DbUtils.resolveM3uMediaUrl(url);

    return url;
  }

  // ---------------------------------------------------------------------------
  // extract M3U channel template from url + static values
  // ---------------------------------------------------------------------------

  public static String extractM3uMediaTemplate(String media_url) {
    if (TextUtils.isEmpty(media_url)) return null;

    List<String> values = DbUtils.getDb().getM3uChannelUrlStaticValues();

    return extractM3uMediaTemplate(media_url, values);
  }

  public static String extractM3uMediaTemplate(String media_url, List<String> values) {
    if (TextUtils.isEmpty(media_url)) return null;

    if ((values == null) || values.isEmpty()) return media_url;

    for (int i=0; i < values.size(); i++) {
      String target = values.get(i);
      String replacement = "%" + (i+1) + "$s";

      if (!TextUtils.isEmpty(target))
        media_url = media_url.replace(target, replacement);
    }

    return media_url;
  }

}
