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
  private boolean applyFilter;
  private List<ChannelListItem> m3uChannels;

  public XmlTvFilter() throws Exception {
    this.applyFilter = SettingsUtils.getApplyXmltvImportFilter(App.context);

    if (applyFilter) {
      DbGateway db = DbUtils.getDb();
      if (db == null) throw new Exception("DbGateway is null");

      this.m3uChannels = db.getM3u();
    }
  }

  public void filterXmlTv(Map<EPGChannel, List<EPGEvent>> data) {
    // ===================
    // apply import filter
    // ===================

    if (applyFilter) {
      List<EPGChannel> pendingDeletion = new ArrayList<EPGChannel>();

      if (!m3uChannels.isEmpty()) {
        for (EPGChannel epgChannel : data.keySet()) {
          if (!passXmltvImportFilter(epgChannel, m3uChannels)) {
            pendingDeletion.add(epgChannel);
          }
        }
      }

      if (!pendingDeletion.isEmpty()) {
        for (EPGChannel epgChannel : pendingDeletion) {
          data.remove(epgChannel);
        }
        pendingDeletion.clear();
      }
    }
  }

  private static boolean passXmltvImportFilter(EPGChannel epgChannel, List<ChannelListItem> m3uChannels) {
    for (ChannelListItem m3uChannel: m3uChannels) {
      if (!TextUtils.isEmpty(m3uChannel.name)     && m3uChannel.name.equals(    epgChannel.getName()))      return true;
      if (!TextUtils.isEmpty(m3uChannel.tvg_name) && m3uChannel.tvg_name.equals(epgChannel.getName()))      return true;
      if (!TextUtils.isEmpty(m3uChannel.tvg_id)   && m3uChannel.tvg_id.equals(  epgChannel.getChannelID())) return true;
    }
    return false;
  }

  public boolean passesXmlTvFilter(EPGChannel epgChannel) {
    if (epgChannel == null) return false;
    Map<EPGChannel, List<EPGEvent>> data = new HashMap<EPGChannel, List<EPGEvent>>();
    data.put(epgChannel, null);
    filterXmlTv(data);
    return !data.isEmpty();
  }
}
