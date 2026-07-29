package com.github.warren_bank.iptv_organizer.utils;

import com.github.warren_bank.iptv_organizer.data.model.ChannelListItem;
import com.github.warren_bank.iptv_organizer.data.parser.M3uParser;
import com.github.warren_bank.iptv_organizer.data.parser.ParserProgressListener;
import com.github.warren_bank.iptv_organizer.data.parser.XmlTvParser;
import com.github.warren_bank.iptv_organizer.database.DbGateway;
import com.github.warren_bank.iptv_organizer.utils.DbUtils;

import se.kmdev.tvepg.epg.domain.EPGChannel;
import se.kmdev.tvepg.epg.domain.EPGEvent;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ImportUtils {

  // --------------------------------------------------------------------------- import M3U

  public static List<ChannelListItem> importM3u(InputStream inputStream, boolean appendList, int firstPosition, ParserProgressListener listener) throws Exception {
    DbGateway db = DbUtils.getDb();
    if (db == null) throw new Exception("DbGateway is null");

    List<ChannelListItem> channels = M3uParser.parseM3u(inputStream, firstPosition, listener);

    // ==========
    // save to DB
    // ==========
    if (!db.saveM3u(channels, appendList)) throw new Exception("Failed to save imported M3U to database.");

    return channels;
  }

  // --------------------------------------------------------------------------- import XMLTV

  public static Map<EPGChannel, List<EPGEvent>> importXmlTv(InputStream inputStream, ParserProgressListener listener) throws Exception {
    DbGateway db = DbUtils.getDb();
    if (db == null) throw new Exception("DbGateway is null");

    Map<EPGChannel, List<EPGEvent>> data = XmlTvParser.parseXmlTv(inputStream, listener);

    // ==========
    // save to DB
    // ==========
    if (!db.saveEpg(data)) throw new Exception("Failed to save imported EPG to database.");

    return data;
  }
}
