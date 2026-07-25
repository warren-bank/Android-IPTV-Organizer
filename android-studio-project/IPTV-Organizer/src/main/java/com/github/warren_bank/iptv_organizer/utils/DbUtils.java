package com.github.warren_bank.iptv_organizer.utils;

import com.github.warren_bank.iptv_organizer.database.DbGateway;
import com.github.warren_bank.iptv_organizer.database.Update;

import se.kmdev.tvepg.epg.domain.EPGChannel;

import android.content.Context;
import android.text.TextUtils;

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
  // misc business logic:
  // ---------------------------------------------------------------------------

  public static String getM3uMediaUrlForEpgChannel(EPGChannel epgChannel) {
    String media_url = DbUtils.getDb().getM3uMediaUrlForEpgChannel(epgChannel);
    return DbUtils.resolveM3uMediaUrl(media_url);
  }

  public static String resolveM3uMediaUrl(String template) {
    if (TextUtils.isEmpty(template)) return null;

    List<String> values = DbUtils.getDb().getChannelUrlStaticValues();

    return ((values == null) || values.isEmpty())
      ? template
      : String.format(template, values.toArray(new String[0]));
  }

}
