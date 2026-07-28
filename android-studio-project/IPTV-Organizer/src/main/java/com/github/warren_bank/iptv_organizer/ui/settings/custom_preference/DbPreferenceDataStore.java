package com.github.warren_bank.iptv_organizer.ui.settings.custom_preference;

import com.github.warren_bank.iptv_organizer.utils.DbUtils;

import java.util.List;
import java.util.Map;

public class DbPreferenceDataStore {

  public static final String KEY_M3U_CHANNELS_MAPPING_NAME_TO_ID      = "m3u_channels_mapping_name_to_id";
  public static final String KEY_M3U_CHANNELS_MAPPING_ID_TO_ID        = "m3u_channels_mapping_id_to_id";
  public static final String KEY_M3U_CHANNELS_MEDIA_URL_STATIC_VALUES = "m3u_channels_media_url_static_values";
  public static final String KEY_M3U_CHANNELS_FILTER_WHITELIST_NAMES  = "m3u_channels_filter_whitelist_names";
  public static final String KEY_M3U_CHANNELS_FILTER_WHITELIST_IDS    = "m3u_channels_filter_whitelist_ids";
  public static final String KEY_M3U_CHANNELS_FILTER_BLACKLIST_NAMES  = "m3u_channels_filter_blacklist_names";
  public static final String KEY_M3U_CHANNELS_FILTER_BLACKLIST_IDS    = "m3u_channels_filter_blacklist_ids";

  public static boolean putString(String key, String value) {
    try {
      switch(key) {
          case KEY_M3U_CHANNELS_MAPPING_NAME_TO_ID: {
              Map<String, String> data = DbUtils.decodeMap(value);
              return DbUtils.getDb().saveChannelNameMappings(data);
          }

          case KEY_M3U_CHANNELS_MAPPING_ID_TO_ID: {
              Map<String, String> data = DbUtils.decodeMap(value);
              return DbUtils.getDb().saveChannelIdMappings(data);
          }

          case KEY_M3U_CHANNELS_MEDIA_URL_STATIC_VALUES: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveChannelUrlStaticValues(data);
          }

          case KEY_M3U_CHANNELS_FILTER_WHITELIST_NAMES: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveChannelNameFilterWhitelist(data);
          }

          case KEY_M3U_CHANNELS_FILTER_WHITELIST_IDS: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveChannelIdFilterWhitelist(data);
          }

          case KEY_M3U_CHANNELS_FILTER_BLACKLIST_NAMES: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveChannelNameFilterBlacklist(data);
          }

          case KEY_M3U_CHANNELS_FILTER_BLACKLIST_IDS: {
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
          case KEY_M3U_CHANNELS_MAPPING_NAME_TO_ID: {
              Map<String, String> data = DbUtils.getDb().getChannelNameMappings();
              return DbUtils.encodeMap(data);
          }

          case KEY_M3U_CHANNELS_MAPPING_ID_TO_ID: {
              Map<String, String> data = DbUtils.getDb().getChannelIdMappings();
              return DbUtils.encodeMap(data);
          }

          case KEY_M3U_CHANNELS_MEDIA_URL_STATIC_VALUES: {
              List<String> data = DbUtils.getDb().getChannelUrlStaticValues();
              return DbUtils.encodeList(data);
          }

          case KEY_M3U_CHANNELS_FILTER_WHITELIST_NAMES: {
              List<String> data = DbUtils.getDb().getChannelNameFilterWhitelist();
              return DbUtils.encodeList(data);
          }

          case KEY_M3U_CHANNELS_FILTER_WHITELIST_IDS: {
              List<String> data = DbUtils.getDb().getChannelIdFilterWhitelist();
              return DbUtils.encodeList(data);
          }

          case KEY_M3U_CHANNELS_FILTER_BLACKLIST_NAMES: {
              List<String> data = DbUtils.getDb().getChannelNameFilterBlacklist();
              return DbUtils.encodeList(data);
          }

          case KEY_M3U_CHANNELS_FILTER_BLACKLIST_IDS: {
              List<String> data = DbUtils.getDb().getChannelIdFilterBlacklist();
              return DbUtils.encodeList(data);
          }
      }
    }
    catch(Exception e) {}
    return defValue;
  }
}
