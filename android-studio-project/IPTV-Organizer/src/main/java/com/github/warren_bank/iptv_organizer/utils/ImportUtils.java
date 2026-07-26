package com.github.warren_bank.iptv_organizer.utils;

import com.github.warren_bank.iptv_organizer.App;
import com.github.warren_bank.iptv_organizer.data.model.ChannelListItem;
import com.github.warren_bank.iptv_organizer.data.parser.M3uParser;
import com.github.warren_bank.iptv_organizer.data.parser.XmlTvParser;
import com.github.warren_bank.iptv_organizer.database.DbGateway;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;
import com.github.warren_bank.iptv_organizer.utils.SettingsUtils;

import se.kmdev.tvepg.epg.domain.EPGChannel;
import se.kmdev.tvepg.epg.domain.EPGEvent;

import android.text.TextUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportUtils {

  // --------------------------------------------------------------------------- import M3U

  public static List<ChannelListItem> importM3u(InputStream inputStream) throws Exception {
    DbGateway db = DbUtils.getDb();
    if (db == null) throw new Exception("DbGateway is null");

    List<ChannelListItem> channels = M3uParser.parseM3u(inputStream);

    // ==============
    // apply mappings
    // ==============
    Map<String, String> nameMapping = db.getChannelNameMappings();
    Map<String, String> idMapping   = db.getChannelIdMappings();

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
    List<String> nameWhitelistSubstrings = db.getChannelNameFilterWhitelistSubset(true);
    List<String> nameWhitelist           = db.getChannelNameFilterWhitelistSubset(false);
    List<String> idWhitelist             = db.getChannelIdFilterWhitelist();

    if (!nameWhitelistSubstrings.isEmpty() || !nameWhitelist.isEmpty() || !idWhitelist.isEmpty()) {
      // One or more whitelists are configured. Channel must match one to pass filter.

      channelNameFilterLoop:
      for (int i = (channels.size() - 1); i >= 0; i--) {
        ChannelListItem channel = channels.get(i);

        if (!nameWhitelistSubstrings.isEmpty()) {
          for (String substr : nameWhitelistSubstrings) {
            if (!TextUtils.isEmpty(channel.name)     && channel.name.contains(substr))
              continue channelNameFilterLoop;
            if (!TextUtils.isEmpty(channel.tvg_name) && channel.tvg_name.contains(substr))
              continue channelNameFilterLoop;
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

    // ===============================
    // replace media URL static values
    // ===============================
    List<String> urlValues = db.getChannelUrlStaticValues();

    if (!urlValues.isEmpty()) {
      for (ChannelListItem channel : channels) {
        if (TextUtils.isEmpty(channel.media_url)) continue;

        for (int i=0; i < urlValues.size(); i++) {
          String target = urlValues.get(i);
          String replacement = "%" + (i+1) + "$s";

          if (!TextUtils.isEmpty(target))
            channel.media_url = channel.media_url.replace(target, replacement);
        }
      }
    }

    // ==========
    // save to DB
    // ==========
    if (!db.saveM3u(channels)) throw new Exception("Failed to save imported M3U to database.");

    return channels;
  }

  // --------------------------------------------------------------------------- import XMLTV

  public static Map<EPGChannel, List<EPGEvent>> importXmlTv(InputStream inputStream) throws Exception {
    DbGateway db = DbUtils.getDb();
    if (db == null) throw new Exception("DbGateway is null");

    Map<EPGChannel, List<EPGEvent>> data = XmlTvParser.parseXmlTv(inputStream);

    // ===================
    // apply import filter
    // ===================
    if (SettingsUtils.getApplyXmltvImportFilter(App.context)) {
      List<EPGChannel> pendingDeletion = new ArrayList<EPGChannel>();

      List<ChannelListItem> m3uChannels = db.getM3u();
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

    // ==========
    // save to DB
    // ==========
    if (!db.saveEpg(data)) throw new Exception("Failed to save imported EPG to database.");

    return data;
  }

  private static boolean passXmltvImportFilter(EPGChannel epgChannel, List<ChannelListItem> m3uChannels) {
    for (ChannelListItem m3uChannel: m3uChannels) {
      if (!TextUtils.isEmpty(m3uChannel.name)     && m3uChannel.name.equals(    epgChannel.getName()))      return true;
      if (!TextUtils.isEmpty(m3uChannel.tvg_name) && m3uChannel.tvg_name.equals(epgChannel.getName()))      return true;
      if (!TextUtils.isEmpty(m3uChannel.tvg_id)   && m3uChannel.tvg_id.equals(  epgChannel.getChannelID())) return true;
    }
    return false;
  }
}
