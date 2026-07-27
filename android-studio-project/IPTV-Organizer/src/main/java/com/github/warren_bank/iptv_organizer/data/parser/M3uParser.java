package com.github.warren_bank.iptv_organizer.data.parser;

import com.github.warren_bank.iptv_organizer.data.model.ChannelListItem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class M3uParser {

  public static List<ChannelListItem> parseM3u(InputStream inputStream, int firstPosition) throws Exception {
    ArrayList<ChannelListItem> channels = new ArrayList<ChannelListItem>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    int position = firstPosition;
    String currentName = null;
    String currentTvgId = null;
    String currentTvgName = null;
    String line;

    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.startsWith("#EXTINF:")) {
        // Extract title after the comma in #EXTINF:-1,Channel Title
        int commaIndex = line.indexOf(",");
        if (commaIndex != -1) {
          currentName    = line.substring(commaIndex + 1);
          currentTvgId   = extractAttribute(line, "tvg-id=\"",   "\"");
          currentTvgName = extractAttribute(line, "tvg-name=\"", "\"");
        }
      } else if (!line.isEmpty() && !line.startsWith("#")) {
        // media URL
        if (currentName != null) {
          channels.add(new ChannelListItem(position, currentName, line, currentTvgId, currentTvgName));
          position += 1;
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

  private static String extractAttribute(String haystack, String prefix, String suffix) {
    int prefixIndex = haystack.indexOf(prefix);
    if (prefixIndex == -1) return null;
    prefixIndex += prefix.length();

    int suffixIndex = haystack.indexOf(suffix, prefixIndex);
    if (suffixIndex == -1) return null;

    return haystack.substring(prefixIndex, suffixIndex);
  }
}
