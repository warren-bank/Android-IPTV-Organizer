package com.github.warren_bank.iptv_organizer.data.parser;

import com.github.warren_bank.iptv_organizer.App;
import com.github.warren_bank.iptv_organizer.data.DataProgressListener;
import com.github.warren_bank.iptv_organizer.data.filter.XmlTvFilter;
import com.github.warren_bank.iptv_organizer.utils.SettingsUtils;

import se.kmdev.tvepg.epg.domain.EPGChannel;
import se.kmdev.tvepg.epg.domain.EPGEvent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class XmlTvParser {
  // Standard XMLTV date format: 20260716171500 +0000
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US);

  private static long parseDateFormat(String value) {
    try {
      if (value == null) throw new Exception("null");

      return XmlTvParser.dateFormat.parse(value).getTime();
    }
    catch(Exception e) {
      return -1L;
    }
  }

  public static Map<EPGChannel, List<EPGEvent>> parseXmlTv(InputStream inputStream, DataProgressListener listener) throws Exception {
    String preferredLang = SettingsUtils.getPreferredXmltvLanguage(App.context);
    if ((preferredLang != null) && preferredLang.isEmpty()) preferredLang = null;

    Map<String, EPGChannel> channelMap = new LinkedHashMap<>();
    Map<EPGChannel, List<EPGEvent>> parsedData = new LinkedHashMap<>();
    XmlTvFilter xmlTvFilter = new XmlTvFilter();

    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    XmlPullParser parser = factory.newPullParser();
    parser.setInput(inputStream, null);

    int eventType = parser.getEventType();

    // temporary channel state
    EPGChannel currentChannel = null;
    boolean currentChannelNameIsPreferredLang = false;

    // temporary programme state
    String currentChannelId = null;
    String currentTitle = null;
    String currentDescription = null;
    boolean currentTitleIsPreferredLang = false;
    boolean currentDescriptionIsPreferredLang = false;
    long startTime = -1L;
    long endTime = -1L;

    while (eventType != XmlPullParser.END_DOCUMENT) {
      String tagName = parser.getName();

      switch (eventType) {
        case XmlPullParser.START_TAG:
          if ("channel".equals(tagName)) {
            String id = parser.getAttributeValue(null, "id");
            if ((id != null) && (!channelMap.containsKey(id))) {
              currentChannel = new EPGChannel(id, null, null);
              channelMap.put(id, currentChannel);
            }
          } else if ("display-name".equals(tagName) && currentChannel != null) {
            if ((preferredLang != null) && !currentChannelNameIsPreferredLang && preferredLang.equals(parser.getAttributeValue(null, "lang"))) {
              currentChannel.setName(null);
              currentChannelNameIsPreferredLang = true;
            }
            if (currentChannel.getName() == null)
              currentChannel.setName(parser.nextText());
          } else if ("icon".equals(tagName) && currentChannel != null) {
            currentChannel.setImageURL(parser.getAttributeValue(null, "src"));
          } else if ("programme".equals(tagName)) {
            currentChannelId = parser.getAttributeValue(null, "channel");
            startTime = XmlTvParser.parseDateFormat(parser.getAttributeValue(null, "start"));
            endTime   = XmlTvParser.parseDateFormat(parser.getAttributeValue(null, "stop"));
          } else if ("title".equals(tagName) && currentChannelId != null) {
            if ((preferredLang != null) && !currentTitleIsPreferredLang && preferredLang.equals(parser.getAttributeValue(null, "lang"))) {
              currentTitle = null;
              currentTitleIsPreferredLang = true;
            }
            if (currentTitle == null)
              currentTitle = parser.nextText();
          } else if ("desc".equals(tagName) && currentChannelId != null) {
            if ((preferredLang != null) && !currentDescriptionIsPreferredLang && preferredLang.equals(parser.getAttributeValue(null, "lang"))) {
              currentDescription = null;
              currentDescriptionIsPreferredLang = true;
            }
            if (currentDescription == null)
              currentDescription = parser.nextText();
          }
          break;

        case XmlPullParser.END_TAG:
          if ("programme".equals(tagName) && currentChannelId != null) {
            if ((listener != null) && (currentTitle != null)) listener.onData(currentTitle);

            EPGChannel channel = channelMap.get(currentChannelId);
            if (channel != null) {
              EPGEvent event = new EPGEvent(startTime, endTime, currentTitle, currentDescription);

              if (xmlTvFilter.passesXmlTvValidator(event)) {
                if (!parsedData.containsKey(channel)) {
                  parsedData.put(channel, new ArrayList<>());
                }
                parsedData.get(channel).add(event);
              }
            }
            // Reset temporary programme state
            currentChannelId = null;
            currentTitle = null;
            currentDescription = null;
            currentTitleIsPreferredLang = false;
            currentDescriptionIsPreferredLang = false;
          } else if ("channel".equals(tagName) && currentChannel != null) {
            if ((listener != null) && (currentChannel.getName() != null)) listener.onData(currentChannel.getName());

            if (!xmlTvFilter.passesXmlTvValidator(currentChannel) || !xmlTvFilter.passesXmlTvFilter(currentChannel)) {
              String id = currentChannel.getChannelID();
              channelMap.remove(id);
            }
            // Reset temporary channel state
            currentChannel = null;
            currentChannelNameIsPreferredLang = false;
          }
          break;
      }
      eventType = parser.next();
    }
    return parsedData;
  }
}
