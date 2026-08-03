package com.github.warren_bank.iptv_organizer.data.filter;

import com.github.warren_bank.iptv_organizer.data.model.ChannelListItem;
import com.github.warren_bank.iptv_organizer.database.DbGateway;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;
import com.github.warren_bank.iptv_organizer.utils.FilterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class M3uFilter {
  private Map<String, String> nameMapping_FF;
  private Map<String, String> nameMapping_TF;
  private Map<String, String> nameMapping_FT;
  private Map<String, String> nameMapping_TT;
  private Map<String, String> idMapping;

  private List<String> nameWhitelist_FF;
  private List<String> nameWhitelist_TF;
  private List<String> nameWhitelist_FT;
  private List<String> nameWhitelist_TT;
  private List<String> idWhitelist;

  private List<String> nameBlacklist_FF;
  private List<String> nameBlacklist_TF;
  private List<String> nameBlacklist_FT;
  private List<String> nameBlacklist_TT;
  private List<String> idBlacklist;

  private List<String> urlValues;

  private boolean hasMapping;
  private boolean hasWhitelist;
  private boolean hasBlacklist;

  public M3uFilter() throws Exception {
    DbGateway db = DbUtils.getDb();
    if (db == null) throw new Exception("DbGateway is null");

    this.nameMapping_FF   = db.getM3uChannelNameMappingsSubset(false, false);
    this.nameMapping_TF   = db.getM3uChannelNameMappingsSubset(true,  false);
    this.nameMapping_FT   = db.getM3uChannelNameMappingsSubset(false, true);
    this.nameMapping_TT   = db.getM3uChannelNameMappingsSubset(true,  true);
    this.idMapping        = db.getM3uChannelIdMappings();

    this.nameWhitelist_FF = db.getM3uChannelNameFilterWhitelistSubset(false, false);
    this.nameWhitelist_TF = db.getM3uChannelNameFilterWhitelistSubset(true,  false);
    this.nameWhitelist_FT = db.getM3uChannelNameFilterWhitelistSubset(false, true);
    this.nameWhitelist_TT = db.getM3uChannelNameFilterWhitelistSubset(true,  true);
    this.idWhitelist      = db.getM3uChannelIdFilterWhitelist();

    this.nameBlacklist_FF = db.getM3uChannelNameFilterBlacklistSubset(false, false);
    this.nameBlacklist_TF = db.getM3uChannelNameFilterBlacklistSubset(true,  false);
    this.nameBlacklist_FT = db.getM3uChannelNameFilterBlacklistSubset(false, true);
    this.nameBlacklist_TT = db.getM3uChannelNameFilterBlacklistSubset(true,  true);
    this.idBlacklist      = db.getM3uChannelIdFilterBlacklist();

    this.urlValues        = db.getM3uChannelUrlStaticValues();

    this.hasMapping       = (!nameMapping_FF.isEmpty()   || !nameMapping_TF.isEmpty()   || !nameMapping_FT.isEmpty()   || !nameMapping_TT.isEmpty()   || !idMapping.isEmpty());
    this.hasWhitelist     = (!nameWhitelist_FF.isEmpty() || !nameWhitelist_TF.isEmpty() || !nameWhitelist_FT.isEmpty() || !nameWhitelist_TT.isEmpty() || !idWhitelist.isEmpty());
    this.hasBlacklist     = (!nameBlacklist_FF.isEmpty() || !nameBlacklist_TF.isEmpty() || !nameBlacklist_FT.isEmpty() || !nameBlacklist_TT.isEmpty() || !idBlacklist.isEmpty());
  }

  public void filterM3u(List<ChannelListItem> channels) {
    // ==============
    // apply mappings
    // ==============

    if (hasMapping) {
      String value;
      for (ChannelListItem channel : channels) {
        value = null;

        if (value == null)
          value = FilterUtils.mapContains(nameMapping_FF, channel.name,     false, false);
        if (value == null)
          value = FilterUtils.mapContains(nameMapping_FF, channel.tvg_name, false, false);
        if (value == null)
          value = FilterUtils.mapContains(nameMapping_TF, channel.name,     true,  false);
        if (value == null)
          value = FilterUtils.mapContains(nameMapping_TF, channel.tvg_name, true,  false);
        if (value == null)
          value = FilterUtils.mapContains(nameMapping_FT, channel.name,     false, true);
        if (value == null)
          value = FilterUtils.mapContains(nameMapping_FT, channel.tvg_name, false, true);
        if (value == null)
          value = FilterUtils.mapContains(nameMapping_TT, channel.name,     true,  true);
        if (value == null)
          value = FilterUtils.mapContains(nameMapping_TT, channel.tvg_name, true,  true);
        if (value == null)
          value = FilterUtils.mapContains(idMapping,      channel.tvg_id,   false, false);
        if (value != null)
          channel.tvg_id = value;
      }
    }

    // =======================
    // apply filter whitelists
    // =======================

    if (hasWhitelist) {
      // One or more whitelists are configured. Channel must match one to pass filter.

      ChannelListItem channel;
      boolean found;
      for (int i = (channels.size() - 1); i >= 0; i--) {
        channel = channels.get(i);
        found = false;

        if (!found)
          found = FilterUtils.listContains(nameWhitelist_FF, channel.name,     false, false);
        if (!found)
          found = FilterUtils.listContains(nameWhitelist_FF, channel.tvg_name, false, false);
        if (!found)
          found = FilterUtils.listContains(nameWhitelist_TF, channel.name,     true,  false);
        if (!found)
          found = FilterUtils.listContains(nameWhitelist_TF, channel.tvg_name, true,  false);
        if (!found)
          found = FilterUtils.listContains(nameWhitelist_FT, channel.name,     false, true);
        if (!found)
          found = FilterUtils.listContains(nameWhitelist_FT, channel.tvg_name, false, true);
        if (!found)
          found = FilterUtils.listContains(nameWhitelist_TT, channel.name,     true,  true);
        if (!found)
          found = FilterUtils.listContains(nameWhitelist_TT, channel.tvg_name, true,  true);
        if (!found)
          found = FilterUtils.listContains(idWhitelist,      channel.tvg_id,   false, false);
        if (!found)
          channels.remove(i);
      }
    }

    // =======================
    // apply filter blacklists
    // =======================

    if (hasBlacklist) {
      // One or more blacklists are configured. Channel must match none to pass filter.

      ChannelListItem channel;
      boolean found;
      for (int i = (channels.size() - 1); i >= 0; i--) {
        channel = channels.get(i);
        found = false;

        if (!found)
          found = FilterUtils.listContains(nameBlacklist_FF, channel.name,     false, false);
        if (!found)
          found = FilterUtils.listContains(nameBlacklist_FF, channel.tvg_name, false, false);
        if (!found)
          found = FilterUtils.listContains(nameBlacklist_TF, channel.name,     true,  false);
        if (!found)
          found = FilterUtils.listContains(nameBlacklist_TF, channel.tvg_name, true,  false);
        if (!found)
          found = FilterUtils.listContains(nameBlacklist_FT, channel.name,     false, true);
        if (!found)
          found = FilterUtils.listContains(nameBlacklist_FT, channel.tvg_name, false, true);
        if (!found)
          found = FilterUtils.listContains(nameBlacklist_TT, channel.name,     true,  true);
        if (!found)
          found = FilterUtils.listContains(nameBlacklist_TT, channel.tvg_name, true,  true);
        if (!found)
          found = FilterUtils.listContains(idBlacklist,      channel.tvg_id,   false, false);
        if (found)
          channels.remove(i);
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
