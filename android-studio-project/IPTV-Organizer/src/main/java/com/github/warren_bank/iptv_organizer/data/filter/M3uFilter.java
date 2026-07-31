package com.github.warren_bank.iptv_organizer.data.filter;

import com.github.warren_bank.iptv_organizer.data.model.ChannelListItem;
import com.github.warren_bank.iptv_organizer.database.DbGateway;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class M3uFilter {
  private Map<String, String> nameMapping;
  private Map<String, String> idMapping;
  private List<String> nameWhitelistSubstrings;
  private List<String> nameWhitelist;
  private List<String> idWhitelist;
  private List<String> nameBlacklistSubstrings;
  private List<String> nameBlacklist;
  private List<String> idBlacklist;
  private List<String> urlValues;

  public M3uFilter() throws Exception {
    DbGateway db = DbUtils.getDb();
    if (db == null) throw new Exception("DbGateway is null");

    this.nameMapping             = db.getM3uChannelNameMappings();
    this.idMapping               = db.getM3uChannelIdMappings();
    this.nameWhitelistSubstrings = db.getM3uChannelNameFilterWhitelistSubset(true);
    this.nameWhitelist           = db.getM3uChannelNameFilterWhitelistSubset(false);
    this.idWhitelist             = db.getM3uChannelIdFilterWhitelist();
    this.nameBlacklistSubstrings = db.getM3uChannelNameFilterBlacklistSubset(true);
    this.nameBlacklist           = db.getM3uChannelNameFilterBlacklistSubset(false);
    this.idBlacklist             = db.getM3uChannelIdFilterBlacklist();
    this.urlValues               = db.getM3uChannelUrlStaticValues();
  }

  public void filterM3u(List<ChannelListItem> channels) {
    // ==============
    // apply mappings
    // ==============

    if (!nameMapping.isEmpty() || !idMapping.isEmpty()) {
      for (ChannelListItem channel : channels) {
        if (!nameMapping.isEmpty()) {
          if (!TextUtils.isEmpty(channel.name) && nameMapping.containsKey(channel.name)) {
            channel.tvg_id = (String) nameMapping.get(channel.name);
            continue;
          }
          if (!TextUtils.isEmpty(channel.tvg_name) && nameMapping.containsKey(channel.tvg_name)) {
            channel.tvg_id = (String) nameMapping.get(channel.tvg_name);
            continue;
          }
        }
        if (!idMapping.isEmpty()) {
          if (!TextUtils.isEmpty(channel.tvg_id) && idMapping.containsKey(channel.tvg_id)) {
            channel.tvg_id = (String) idMapping.get(channel.tvg_id);
            continue;
          }
        }
      }
    }

    // =======================
    // apply filter whitelists
    // =======================

    if (!nameWhitelistSubstrings.isEmpty() || !nameWhitelist.isEmpty() || !idWhitelist.isEmpty()) {
      // One or more whitelists are configured. Channel must match one to pass filter.

      channelNameWLFilterLoop:
      for (int i = (channels.size() - 1); i >= 0; i--) {
        ChannelListItem channel = channels.get(i);

        if (!nameWhitelistSubstrings.isEmpty()) {
          for (String substr : nameWhitelistSubstrings) {
            if (!TextUtils.isEmpty(channel.name)     && channel.name.contains(substr))
              continue channelNameWLFilterLoop;
            if (!TextUtils.isEmpty(channel.tvg_name) && channel.tvg_name.contains(substr))
              continue channelNameWLFilterLoop;
          }
        }
        if (!nameWhitelist.isEmpty()) {
          if (!TextUtils.isEmpty(channel.name)     && nameWhitelist.contains(channel.name))
            continue;
          if (!TextUtils.isEmpty(channel.tvg_name) && nameWhitelist.contains(channel.tvg_name))
            continue;
        }
        if (!idWhitelist.isEmpty()) {
          if (!TextUtils.isEmpty(channel.tvg_id)   && idWhitelist.contains(channel.tvg_id))
            continue;
        }

        // no match
        channels.remove(i);
      }
    }

    // =======================
    // apply filter blacklists
    // =======================

    if (!nameBlacklistSubstrings.isEmpty() || !nameBlacklist.isEmpty() || !idBlacklist.isEmpty()) {
      // One or more blacklists are configured. Channel must match none to pass filter.

      channelNameBLFilterLoop:
      for (int i = (channels.size() - 1); i >= 0; i--) {
        ChannelListItem channel = channels.get(i);

        if (!nameBlacklistSubstrings.isEmpty()) {
          for (String substr : nameBlacklistSubstrings) {
            if (!TextUtils.isEmpty(channel.name)     && channel.name.contains(substr)) {
              channels.remove(i);
              continue channelNameBLFilterLoop;
            }
            if (!TextUtils.isEmpty(channel.tvg_name) && channel.tvg_name.contains(substr)) {
              channels.remove(i);
              continue channelNameBLFilterLoop;
            }
          }
        }
        if (!nameBlacklist.isEmpty()) {
          if (!TextUtils.isEmpty(channel.name)     && nameBlacklist.contains(channel.name)) {
            channels.remove(i);
            continue;
          }
          if (!TextUtils.isEmpty(channel.tvg_name) && nameBlacklist.contains(channel.tvg_name)) {
            channels.remove(i);
            continue;
          }
        }
        if (!idBlacklist.isEmpty()) {
          if (!TextUtils.isEmpty(channel.tvg_id)   && idBlacklist.contains(channel.tvg_id)) {
            channels.remove(i);
            continue;
          }
        }
      }
    }

    // ===============================
    // replace media URL static values
    // ===============================

    if (!urlValues.isEmpty()) {
      for (ChannelListItem channel : channels) {
        channel.media_url = DbUtils.extractM3uMediaTemplate(channel.media_url, urlValues);
      }
    }
  }

  public boolean passesM3uFilter(ChannelListItem channel) {
    if (channel == null) return false;
    List<ChannelListItem> channels = new ArrayList<ChannelListItem>();
    channels.add(channel);
    filterM3u(channels);
    return !channels.isEmpty();
  }
}
