package com.github.warren_bank.iptv_organizer.data.filter;

import com.github.warren_bank.iptv_organizer.App;
import com.github.warren_bank.iptv_organizer.data.model.ChannelListItem;
import com.github.warren_bank.iptv_organizer.database.DbGateway;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;
import com.github.warren_bank.iptv_organizer.utils.FilterUtils;
import com.github.warren_bank.iptv_organizer.utils.SettingsUtils;

import se.kmdev.tvepg.epg.domain.EPGChannel;
import se.kmdev.tvepg.epg.domain.EPGEvent;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlTvFilter {
  private boolean filterM3uChannels;
  private List<ChannelListItem> m3uChannels;

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

  private boolean hasWhitelist;
  private boolean hasBlacklist;

  public XmlTvFilter() throws Exception {
    DbGateway db = DbUtils.getDb();
    if (db == null) throw new Exception("DbGateway is null");

    this.filterM3uChannels = SettingsUtils.getFilterM3uChannels(App.context);
    this.m3uChannels = filterM3uChannels
      ? db.getM3u()
      : null;

    this.nameWhitelist_FF = db.getEpgChannelNameFilterWhitelistSubset(false, false);
    this.nameWhitelist_TF = db.getEpgChannelNameFilterWhitelistSubset(true,  false);
    this.nameWhitelist_FT = db.getEpgChannelNameFilterWhitelistSubset(false, true);
    this.nameWhitelist_TT = db.getEpgChannelNameFilterWhitelistSubset(true,  true);
    this.idWhitelist      = db.getEpgChannelIdFilterWhitelist();

    this.nameBlacklist_FF = db.getEpgChannelNameFilterBlacklistSubset(false, false);
    this.nameBlacklist_TF = db.getEpgChannelNameFilterBlacklistSubset(true,  false);
    this.nameBlacklist_FT = db.getEpgChannelNameFilterBlacklistSubset(false, true);
    this.nameBlacklist_TT = db.getEpgChannelNameFilterBlacklistSubset(true,  true);
    this.idBlacklist      = db.getEpgChannelIdFilterBlacklist();

    this.hasWhitelist     = (!nameWhitelist_FF.isEmpty() || !nameWhitelist_TF.isEmpty() || !nameWhitelist_FT.isEmpty() || !nameWhitelist_TT.isEmpty() || !idWhitelist.isEmpty());
    this.hasBlacklist     = (!nameBlacklist_FF.isEmpty() || !nameBlacklist_TF.isEmpty() || !nameBlacklist_FT.isEmpty() || !nameBlacklist_TT.isEmpty() || !idBlacklist.isEmpty());
  }

  public void filterXmlTv(Map<EPGChannel, List<EPGEvent>> data) {
    // =======================
    // apply filter whitelists
    // =======================

    if ((filterM3uChannels && !m3uChannels.isEmpty()) || hasWhitelist) {
      // One or more whitelists are configured. Channel must match one to pass filter.

      List<EPGChannel> pendingDeletion = new ArrayList<EPGChannel>();

      boolean found;
      for (EPGChannel channel : data.keySet()) {
        found = false;

        if (!found && filterM3uChannels && !m3uChannels.isEmpty())
          found = passM3uChannelsFilter(channel, m3uChannels);
        if (!found)
          found = FilterUtils.listContains(nameWhitelist_FF, channel.getName(),      false, false);
        if (!found)
          found = FilterUtils.listContains(nameWhitelist_TF, channel.getName(),      true,  false);
        if (!found)
          found = FilterUtils.listContains(nameWhitelist_FT, channel.getName(),      false, true);
        if (!found)
          found = FilterUtils.listContains(nameWhitelist_TT, channel.getName(),      true,  true);
        if (!found)
          found = FilterUtils.listContains(idWhitelist,      channel.getChannelID(), false, false);
        if (!found)
          pendingDeletion.add(channel);
      }

      if (!pendingDeletion.isEmpty()) {
        for (EPGChannel channel : pendingDeletion) {
          data.remove(channel);
        }
        pendingDeletion.clear();
      }
    }

    // =======================
    // apply filter blacklists
    // =======================

    if (hasBlacklist) {
      // One or more blacklists are configured. Channel must match none to pass filter.

      List<EPGChannel> pendingDeletion = new ArrayList<EPGChannel>();

      boolean found;
      for (EPGChannel channel : data.keySet()) {
        found = false;

        if (!found)
          found = FilterUtils.listContains(nameBlacklist_FF, channel.getName(),      false, false);
        if (!found)
          found = FilterUtils.listContains(nameBlacklist_TF, channel.getName(),      true,  false);
        if (!found)
          found = FilterUtils.listContains(nameBlacklist_FT, channel.getName(),      false, true);
        if (!found)
          found = FilterUtils.listContains(nameBlacklist_TT, channel.getName(),      true,  true);
        if (!found)
          found = FilterUtils.listContains(idBlacklist,      channel.getChannelID(), false, false);
        if (found)
          pendingDeletion.add(channel);
      }

      if (!pendingDeletion.isEmpty()) {
        for (EPGChannel channel : pendingDeletion) {
          data.remove(channel);
        }
        pendingDeletion.clear();
      }
    }
  }

  private static boolean passM3uChannelsFilter(EPGChannel epgChannel, List<ChannelListItem> m3uChannels) {
    for (ChannelListItem m3uChannel: m3uChannels) {
      if (!TextUtils.isEmpty(m3uChannel.name)     && m3uChannel.name.equals(    epgChannel.getName()))      return true;
      if (!TextUtils.isEmpty(m3uChannel.tvg_name) && m3uChannel.tvg_name.equals(epgChannel.getName()))      return true;
      if (!TextUtils.isEmpty(m3uChannel.tvg_id)   && m3uChannel.tvg_id.equals(  epgChannel.getChannelID())) return true;
    }
    return false;
  }

  public boolean passesXmlTvFilter(EPGChannel channel) {
    if (channel == null) return false;
    Map<EPGChannel, List<EPGEvent>> data = new HashMap<EPGChannel, List<EPGEvent>>();
    data.put(channel, null);
    filterXmlTv(data);
    return !data.isEmpty();
  }

  public boolean passesXmlTvValidator(EPGChannel channel) {
    // required fields: channelID, name
    return ((channel != null) && !TextUtils.isEmpty(channel.getChannelID()) && !TextUtils.isEmpty(channel.getName()));
  }

  public boolean passesXmlTvValidator(EPGEvent program) {
    // required fields: start, title
    return ((program != null) && (program.getStart() >= 0) && !TextUtils.isEmpty(program.getTitle()));
  }
}
