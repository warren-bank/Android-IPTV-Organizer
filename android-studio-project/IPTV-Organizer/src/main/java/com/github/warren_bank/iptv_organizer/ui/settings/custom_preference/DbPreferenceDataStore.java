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
  public static final String KEY_EPG_CHANNELS_FILTER_WHITELIST_NAMES  = "epg_channels_filter_whitelist_names";
  public static final String KEY_EPG_CHANNELS_FILTER_WHITELIST_IDS    = "epg_channels_filter_whitelist_ids";
  public static final String KEY_EPG_CHANNELS_FILTER_BLACKLIST_NAMES  = "epg_channels_filter_blacklist_names";
  public static final String KEY_EPG_CHANNELS_FILTER_BLACKLIST_IDS    = "epg_channels_filter_blacklist_ids";
  public static final String KEY_SAVED_SEARCH_KEYWORDS_LIST           = "saved_search_keywords_list";

  public static boolean putString(String key, String value) {
    try {
      switch(key) {
          case KEY_M3U_CHANNELS_MAPPING_NAME_TO_ID: {
              Map<String, String> data = DbUtils.decodeMap(value);
              return DbUtils.getDb().saveM3uChannelNameMappings(data);
          }

          case KEY_M3U_CHANNELS_MAPPING_ID_TO_ID: {
              Map<String, String> data = DbUtils.decodeMap(value);
              return DbUtils.getDb().saveM3uChannelIdMappings(data);
          }

          case KEY_M3U_CHANNELS_MEDIA_URL_STATIC_VALUES: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveM3uChannelUrlStaticValues(data);
          }

          case KEY_M3U_CHANNELS_FILTER_WHITELIST_NAMES: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveM3uChannelNameFilterWhitelist(data);
          }

          case KEY_M3U_CHANNELS_FILTER_WHITELIST_IDS: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveM3uChannelIdFilterWhitelist(data);
          }

          case KEY_M3U_CHANNELS_FILTER_BLACKLIST_NAMES: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveM3uChannelNameFilterBlacklist(data);
          }

          case KEY_M3U_CHANNELS_FILTER_BLACKLIST_IDS: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveM3uChannelIdFilterBlacklist(data);
          }

          case KEY_EPG_CHANNELS_FILTER_WHITELIST_NAMES: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveEpgChannelNameFilterWhitelist(data);
          }

          case KEY_EPG_CHANNELS_FILTER_WHITELIST_IDS: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveEpgChannelIdFilterWhitelist(data);
          }

          case KEY_EPG_CHANNELS_FILTER_BLACKLIST_NAMES: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveEpgChannelNameFilterBlacklist(data);
          }

          case KEY_EPG_CHANNELS_FILTER_BLACKLIST_IDS: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().saveEpgChannelIdFilterBlacklist(data);
          }

          case KEY_SAVED_SEARCH_KEYWORDS_LIST: {
              List<String> data = DbUtils.decodeList(value);
              return DbUtils.getDb().setSavedSearchKeywordsList(data);
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
              Map<String, String> data = DbUtils.getDb().getM3uChannelNameMappings();
              return DbUtils.encodeMap(data);
          }

          case KEY_M3U_CHANNELS_MAPPING_ID_TO_ID: {
              Map<String, String> data = DbUtils.getDb().getM3uChannelIdMappings();
              return DbUtils.encodeMap(data);
          }

          case KEY_M3U_CHANNELS_MEDIA_URL_STATIC_VALUES: {
              List<String> data = DbUtils.getDb().getM3uChannelUrlStaticValues();
              return DbUtils.encodeList(data);
          }

          case KEY_M3U_CHANNELS_FILTER_WHITELIST_NAMES: {
              List<String> data = DbUtils.getDb().getM3uChannelNameFilterWhitelist();
              return DbUtils.encodeList(data);
          }

          case KEY_M3U_CHANNELS_FILTER_WHITELIST_IDS: {
              List<String> data = DbUtils.getDb().getM3uChannelIdFilterWhitelist();
              return DbUtils.encodeList(data);
          }

          case KEY_M3U_CHANNELS_FILTER_BLACKLIST_NAMES: {
              List<String> data = DbUtils.getDb().getM3uChannelNameFilterBlacklist();
              return DbUtils.encodeList(data);
          }

          case KEY_M3U_CHANNELS_FILTER_BLACKLIST_IDS: {
              List<String> data = DbUtils.getDb().getM3uChannelIdFilterBlacklist();
              return DbUtils.encodeList(data);
          }

          case KEY_EPG_CHANNELS_FILTER_WHITELIST_NAMES: {
              List<String> data = DbUtils.getDb().getEpgChannelNameFilterWhitelist();
              return DbUtils.encodeList(data);
          }

          case KEY_EPG_CHANNELS_FILTER_WHITELIST_IDS: {
              List<String> data = DbUtils.getDb().getEpgChannelIdFilterWhitelist();
              return DbUtils.encodeList(data);
          }

          case KEY_EPG_CHANNELS_FILTER_BLACKLIST_NAMES: {
              List<String> data = DbUtils.getDb().getEpgChannelNameFilterBlacklist();
              return DbUtils.encodeList(data);
          }

          case KEY_EPG_CHANNELS_FILTER_BLACKLIST_IDS: {
              List<String> data = DbUtils.getDb().getEpgChannelIdFilterBlacklist();
              return DbUtils.encodeList(data);
          }

          case KEY_SAVED_SEARCH_KEYWORDS_LIST: {
              List<String> data = DbUtils.getDb().getSavedSearchKeywordsList();
              return DbUtils.encodeList(data);
          }
      }
    }
    catch(Exception e) {}
    return defValue;
  }
}
