package com.github.warren_bank.iptv_organizer.data.parser;

import com.github.warren_bank.iptv_organizer.common.Constants;
import com.github.warren_bank.iptv_organizer.data.filter.M3uFilter;
import com.github.warren_bank.iptv_organizer.data.model.ChannelListItem;
import com.github.warren_bank.iptv_organizer.data.parser.ParserProgressListener;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class M3uParser {

  public static List<ChannelListItem> parseM3u(InputStream inputStream, int firstPosition, ParserProgressListener listener) throws Exception {
    ArrayList<ChannelListItem> channels = new ArrayList<ChannelListItem>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    M3uFilter m3uFilter = new M3uFilter();
    int position = firstPosition;
    String currentName = null;
    String currentTvgId = null;
    String currentTvgName = null;
    String line;

    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.startsWith("#EXTINF:")) {
        try {
          SimpleM3UParser.M3U_Entry curEntry = SimpleM3UParser.parseExtInf(line);
          if (curEntry.name != null) {
            currentName    = curEntry.name;
            currentTvgId   = curEntry.tvgId;
            currentTvgName = curEntry.tvgName;
          }
        }
        catch(Exception e) {
          Log.e(Constants.LOG_TAG, "Failed to parse M3U line: " + line);
        }
      } else if (!line.isEmpty() && !line.startsWith("#")) {
        // media URL
        if (currentName != null) {
          if (listener != null) listener.onData(currentName);

          ChannelListItem channel = new ChannelListItem(position, currentName, line, currentTvgId, currentTvgName);

          if (m3uFilter.passesM3uFilter(channel)) {
            channels.add(channel);
            position += 1;
          }

          // Reset
          currentName = null;
          currentTvgId = null;
          currentTvgName = null;
        }
      }
    }
    reader.close();
    return channels;
  }

  // ---------------------------------------------------------------------------
  // based on:
  //   https://github.com/gsantner/opoc/raw/5e8a6445ae7a079ce9c0dd4f4e6f9a9b6d3fd5a4/java/java/net/gsantner/opoc/format/playlist/SimpleM3UParser.java
  // copyright:
  //   2019-2022 Gregor Santner <https://gsantner.net/>
  // license:
  //   Unlicense <https://unlicense.org/>
  // ---------------------------------------------------------------------------
  private static class SimpleM3UParser {
    private final static String EXTINF_TAG = "#EXTINF:";
    private final static String EXTINF_TVG_NAME = "tvg-name=\"";
    private final static String EXTINF_TVG_ID = "tvg-id=\"";
    private final static String EXTINF_TVG_LOGO = "tvg-logo=\"";
    private final static String EXTINF_TVG_EPGURL = "tvg-epgurl=\"";
    private final static String EXTINF_GROUP_TITLE = "group-title=\"";
    private final static String EXTINF_RADIO = "radio=\"";
    private final static String EXTINF_TAGS = "tags=\"";

    public static class M3U_Entry {
      public String tvgName, name;
      public String tvgLogo;
      public String tvgEpgUrl;
      public String tvgId;
      public String groupTitle;
      public String url;
      public String[] tags = new String[0];
      public int seconds = -1;
      public boolean isRadio = false;
    }

    public static M3U_Entry parseExtInf(String line) {
      M3U_Entry curEntry = new M3U_Entry();
      StringBuilder buf = new StringBuilder(20);
      if (line.length() < EXTINF_TAG.length() + 1) {
        return curEntry;
      }

      // Strip tag
      line = line.substring(EXTINF_TAG.length());

      // Read seconds (may end with comma or whitespace)
      while (line.length() > 0) {
        char c = line.charAt(0);
        if (Character.isDigit(c) || c == '-' || c == '+') {
          buf.append(c);
          line = line.substring(1);
        } else {
          break;
        }
      }
      if (buf.length() == 0 || line.isEmpty()) {
        return curEntry;
      }
      curEntry.seconds = Integer.parseInt(buf.toString());

      // tvg tags
      String old = null;
      while (!line.isEmpty() && !line.startsWith(",") && !line.equals(old)) {
        old = line = line.trim();
        if (line.startsWith(EXTINF_TVG_NAME) && line.length() > EXTINF_TVG_NAME.length()) {
          line = line.substring(EXTINF_TVG_NAME.length());
          int i = line.indexOf("\"");
          curEntry.tvgName = line.substring(0, i).replace("'", "");
          line = line.substring(i + 1);
        } else if (line.startsWith(EXTINF_TVG_LOGO) && line.length() > EXTINF_TVG_LOGO.length()) {
          line = line.substring(EXTINF_TVG_LOGO.length());
          int i = line.indexOf("\"");
          curEntry.tvgLogo = line.substring(0, i);
          line = line.substring(i + 1);
        } else if (line.startsWith(EXTINF_TVG_EPGURL) && line.length() > EXTINF_TVG_EPGURL.length()) {
          line = line.substring(EXTINF_TVG_EPGURL.length());
          int i = line.indexOf("\"");
          curEntry.tvgEpgUrl = line.substring(0, i);
          line = line.substring(i + 1);
        } else if (line.startsWith(EXTINF_RADIO) && line.length() > EXTINF_RADIO.length()) {
          line = line.substring(EXTINF_RADIO.length());
          int i = line.indexOf("\"");
          curEntry.isRadio = Boolean.parseBoolean(line.substring(0, i));
          line = line.substring(i + 1);
        } else if (line.startsWith(EXTINF_GROUP_TITLE) && line.length() > EXTINF_GROUP_TITLE.length()) {
          line = line.substring(EXTINF_GROUP_TITLE.length());
          int i = line.indexOf("\"");
          curEntry.groupTitle = line.substring(0, i);
          line = line.substring(i + 1);
        } else if (line.startsWith(EXTINF_TVG_ID) && line.length() > EXTINF_TVG_ID.length()) {
          line = line.substring(EXTINF_TVG_ID.length());
          int i = line.indexOf("\"");
          curEntry.tvgId = line.substring(0, i);
          line = line.substring(i + 1);
        } else if (line.startsWith(EXTINF_TAGS) && line.length() > EXTINF_TAGS.length()) {
          line = line.substring(EXTINF_TAGS.length());
          int i = line.indexOf("\"");
          curEntry.tags = line.substring(0, i).split(",");
          line = line.substring(i + 1);
        } else {
          line = line.substring(line.indexOf("\"") + 1);
          line = line.substring(line.indexOf("\"") + 1);
        }
      }

      // Name
      line = line.trim();
      if (line.length() > 1 && line.startsWith(",")) {
        line = line.substring(1);
        line = line.trim();
        if (!line.isEmpty()) {
          curEntry.name = line.replace("'", "");
        }
      }
      return curEntry;
    }
  }
}
