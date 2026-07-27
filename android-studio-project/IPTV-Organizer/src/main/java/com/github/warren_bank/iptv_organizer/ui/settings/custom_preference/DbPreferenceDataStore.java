package com.github.warren_bank.iptv_organizer.ui.settings.custom_preference;

import com.github.warren_bank.iptv_organizer.utils.DbUtils;

import java.util.List;
import java.util.Map;

public class DbPreferenceDataStore {

  public static boolean putString(String key, String value) {
    try {
      switch(key) {
          case "m3u_channels_mapping_name_to_id": {
              Map<String, String> data = DbUtils.decodeMap(value);
              return DbUtils.getDb().saveChannelNameMappings(data);
          }

          case "m3u_channels_mapping_id_to_id": {
              Map<String, String> data = DbUtils.decodeMap(value);
              return DbUtils.getDb().saveChannelIdMappings(data);
          }

          case "m3u_channels_media_url_static_values": {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveChannelUrlStaticValues(data);
          }

          case "m3u_channels_filter_whitelist_names": {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveChannelNameFilterWhitelist(data);
          }

          case "m3u_channels_filter_whitelist_ids": {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveChannelIdFilterWhitelist(data);
          }

          case "m3u_channels_filter_blacklist_names": {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveChannelNameFilterBlacklist(data);
          }

          case "m3u_channels_filter_blacklist_ids": {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveChannelIdFilterBlacklist(data);
          }
      }
    }
    catch(Exception e) {}
    return false;
  }

  public static String getString(String key, String defValue) {
    try {
      switch(key) {
          case "m3u_channels_mapping_name_to_id": {
              Map<String, String> data = DbUtils.getDb().getChannelNameMappings();
              return DbUtils.encodeMap(data);
          }

          case "m3u_channels_mapping_id_to_id": {
              Map<String, String> data = DbUtils.getDb().getChannelIdMappings();
              return DbUtils.encodeMap(data);
          }

          case "m3u_channels_media_url_static_values": {
              List<String> data = DbUtils.getDb().getChannelUrlStaticValues();
              return DbUtils.encodeList(data);
          }

          case "m3u_channels_filter_whitelist_names": {
              List<String> data = DbUtils.getDb().getChannelNameFilterWhitelist();
              return DbUtils.encodeList(data);
          }

          case "m3u_channels_filter_whitelist_ids": {
              List<String> data = DbUtils.getDb().getChannelIdFilterWhitelist();
              return DbUtils.encodeList(data);
          }

          case "m3u_channels_filter_blacklist_names": {
              List<String> data = DbUtils.getDb().getChannelNameFilterBlacklist();
              return DbUtils.encodeList(data);
          }

          case "m3u_channels_filter_blacklist_ids": {
              List<String> data = DbUtils.getDb().getChannelIdFilterBlacklist();
              return DbUtils.encodeList(data);
          }
      }
    }
    catch(Exception e) {}
    return defValue;
  }
}
