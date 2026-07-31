package com.github.warren_bank.iptv_organizer.data.filter;

import com.github.warren_bank.iptv_organizer.App;
import com.github.warren_bank.iptv_organizer.data.model.ChannelListItem;
import com.github.warren_bank.iptv_organizer.database.DbGateway;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;
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
  private List<String> nameWhitelistSubstrings;
  private List<String> nameWhitelist;
  private List<String> idWhitelist;
  private List<String> nameBlacklistSubstrings;
  private List<String> nameBlacklist;
  private List<String> idBlacklist;

  public XmlTvFilter() throws Exception {
    DbGateway db = DbUtils.getDb();
    if (db == null) throw new Exception("DbGateway is null");

    this.filterM3uChannels = SettingsUtils.getFilterM3uChannels(App.context);
    this.m3uChannels = filterM3uChannels
      ? db.getM3u()
      : null;
    this.nameWhitelistSubstrings = db.getEpgChannelNameFilterWhitelistSubset(true);
    this.nameWhitelist           = db.getEpgChannelNameFilterWhitelistSubset(false);
    this.idWhitelist             = db.getEpgChannelIdFilterWhitelist();
    this.nameBlacklistSubstrings = db.getEpgChannelNameFilterBlacklistSubset(true);
    this.nameBlacklist           = db.getEpgChannelNameFilterBlacklistSubset(false);
    this.idBlacklist             = db.getEpgChannelIdFilterBlacklist();
  }

  public void filterXmlTv(Map<EPGChannel, List<EPGEvent>> data) {
    // =======================
    // apply filter whitelists
    // =======================

    if ((filterM3uChannels && !m3uChannels.isEmpty()) || !nameWhitelistSubstrings.isEmpty() || !nameWhitelist.isEmpty() || !idWhitelist.isEmpty()) {
      // One or more whitelists are configured. Channel must match one to pass filter.

      List<EPGChannel> pendingDeletion = new ArrayList<EPGChannel>();

      channelNameWLFilterLoop:
      for (EPGChannel channel : data.keySet()) {
        if (filterM3uChannels && !m3uChannels.isEmpty()) {
          if (passM3uChannelsFilter(channel, m3uChannels))
            continue;
        }
        if (!nameWhitelistSubstrings.isEmpty()) {
          for (String substr : nameWhitelistSubstrings) {
            if (!TextUtils.isEmpty(channel.getName()) && channel.getName().contains(substr))
              continue channelNameWLFilterLoop;
          }
        }
        if (!nameWhitelist.isEmpty()) {
          if (!TextUtils.isEmpty(channel.getName()) && nameWhitelist.contains(channel.getName()))
            continue;
        }
        if (!idWhitelist.isEmpty()) {
          if (!TextUtils.isEmpty(channel.getChannelID()) && idWhitelist.contains(channel.getChannelID()))
            continue;
        }

        // no match
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

    if (!nameBlacklistSubstrings.isEmpty() || !nameBlacklist.isEmpty() || !idBlacklist.isEmpty()) {
      // One or more blacklists are configured. Channel must match none to pass filter.

      List<EPGChannel> pendingDeletion = new ArrayList<EPGChannel>();

      channelNameBLFilterLoop:
      for (EPGChannel channel : data.keySet()) {
        if (!nameBlacklistSubstrings.isEmpty()) {
          for (String substr : nameBlacklistSubstrings) {
            if (!TextUtils.isEmpty(channel.getName()) && channel.getName().contains(substr)) {
              pendingDeletion.add(channel);
              continue channelNameBLFilterLoop;
            }
          }
        }
        if (!nameBlacklist.isEmpty()) {
          if (!TextUtils.isEmpty(channel.getName()) && nameBlacklist.contains(channel.getName())) {
            pendingDeletion.add(channel);
            continue;
          }
        }
        if (!idBlacklist.isEmpty()) {
          if (!TextUtils.isEmpty(channel.getChannelID()) && idBlacklist.contains(channel.getChannelID())) {
            pendingDeletion.add(channel);
            continue;
          }
        }
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
