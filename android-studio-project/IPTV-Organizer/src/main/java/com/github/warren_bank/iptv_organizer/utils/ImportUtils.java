package com.github.warren_bank.iptv_organizer.utils;

import com.github.warren_bank.iptv_organizer.R;
import com.github.warren_bank.iptv_organizer.data.model.ChannelListItem;
import com.github.warren_bank.iptv_organizer.data.parser.M3uParser;
import com.github.warren_bank.iptv_organizer.data.parser.XmlTvParser;
import com.github.warren_bank.iptv_organizer.database.DbGateway;
import com.github.warren_bank.iptv_organizer.ui.dialog.DataProgressDialog;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;

import se.kmdev.tvepg.epg.domain.EPGChannel;
import se.kmdev.tvepg.epg.domain.EPGEvent;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ImportUtils {

  // --------------------------------------------------------------------------- import M3U

  public static List<ChannelListItem> importM3u(InputStream inputStream, boolean appendList, int firstPosition, DataProgressDialog listener) throws Exception {
    DbGateway db = DbUtils.getDb();
    if (db == null) throw new Exception("DbGateway is null");

    List<ChannelListItem> channels = M3uParser.parseM3u(inputStream, firstPosition, listener);

    // ==========
    // save to DB
    // ==========

    if (listener != null) listener.updateTitle(R.string.save);

    if (!db.saveM3u(channels, appendList, listener)) throw new Exception("Failed to save imported M3U to database.");

    return channels;
  }

  // --------------------------------------------------------------------------- import XMLTV

  public static Map<EPGChannel, List<EPGEvent>> importXmlTv(InputStream inputStream, DataProgressDialog listener) throws Exception {
    List<InputStream> inputStreamList = Collections.singletonList(inputStream);
    return importXmlTv(inputStreamList, listener);
  }

  public static Map<EPGChannel, List<EPGEvent>> importXmlTv(List<InputStream> inputStreamList, DataProgressDialog listener) throws Exception {
    DbGateway db = DbUtils.getDb();
    if (db == null) throw new Exception("DbGateway is null");

    Map<EPGChannel, List<EPGEvent>> allData = null;

    for (InputStream inputStream : inputStreamList) {
      Map<EPGChannel, List<EPGEvent>> newData = XmlTvParser.parseXmlTv(inputStream, listener);

      if ((newData == null) || newData.isEmpty()) continue;

      if (allData == null) {
        allData = newData;
      }
      else {
        // only add non-duplicate channels
        for (Map.Entry<EPGChannel, List<EPGEvent>> entry : newData.entrySet()) {
          EPGChannel channel = (EPGChannel) entry.getKey();

          if (!allData.containsKey(channel)) {
            allData.put(channel, (List<EPGEvent>) entry.getValue());
          }
        }
      }
    }

    if (allData == null)
      throw new Exception("Failed to import any EPG data from input streams.");

    // ==========
    // save to DB
    // ==========

    if (listener != null) listener.updateTitle(R.string.save);

    if (!db.saveEpg(allData, listener)) throw new Exception("Failed to save imported EPG to database.");

    return allData;
  }
}
