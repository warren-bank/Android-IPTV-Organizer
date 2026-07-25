package com.github.warren_bank.iptv_organizer.ui.settings.custom_preference;

import com.github.warren_bank.iptv_organizer.utils.DbUtils;

import android.preference.PreferenceDataStore;

import java.util.List;
import java.util.Map;

public class DbPreferenceDataStore implements PreferenceDataStore {
  @Override
  public void putString(String key, String value) {
    try {
      switch(key) {
          case "m3u_channels_mapping_name_to_id": {
              Map<String, String> data = DbUtils.decodeMap(value);
              DbUtils.getDb().saveChannelNameMappings(data);
          }
          break;

          case "m3u_channels_mapping_id_to_id": {
              Map<String, String> data = DbUtils.decodeMap(value);
              DbUtils.getDb().saveChannelIdMappings(data);
          }
          break;

          case "m3u_channels_media_url_static_values": {
              List<String> data = DbUtils.decodeList(value);
              DbUtils.getDb().saveChannelUrlStaticValues(data);
          }
          break;

          case "m3u_channels_filter_whitelist_names": {
              List<String> data = DbUtils.decodeList(value);
              DbUtils.getDb().saveChannelNameFilterWhitelist(data);
          }
          break;

          case "m3u_channels_filter_whitelist_ids": {
              List<String> data = DbUtils.decodeList(value);
              DbUtils.getDb().saveChannelIdFilterWhitelist(data);
          }
          break;
      }
    }
    catch(Exception e) {}
  }

  @Override
  public String getString(String key, String defValue) {
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
      }
    }
    catch(Exception e) {}
    return defValue;
  }
}
