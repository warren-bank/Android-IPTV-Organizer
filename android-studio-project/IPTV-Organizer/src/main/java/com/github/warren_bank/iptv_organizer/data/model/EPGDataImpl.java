package com.github.warren_bank.iptv_organizer.data.model;

import com.google.common.collect.Lists;

import se.kmdev.tvepg.epg.EPGData;
import se.kmdev.tvepg.epg.domain.EPGChannel;
import se.kmdev.tvepg.epg.domain.EPGEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class EPGDataImpl implements EPGData {
  private Map<EPGChannel, List<EPGEvent>> data;

  private List<EPGChannel> allChannels;
  private List<EPGChannel> filteredChannels;

  public EPGDataImpl(Map<EPGChannel, List<EPGEvent>> data) {
    this.data = data;

    if (data != null) {
      allChannels = Lists.newArrayList(data.keySet());
      Collections.sort(allChannels, alphabeticOrderComparator);
    }
    else {
      allChannels = Lists.newArrayList();
    }

    filterChannels(null, null, 0, false);
  }

  public Map<EPGChannel, List<EPGEvent>> getData() {
    return data;
  }

  public void filterChannels(String constraint, String keywordArraySplitRegex, int minKeywordLength, boolean caseSensitive) {
    if ((constraint == null) || constraint.isEmpty()) {
      filteredChannels = Lists.newArrayList(allChannels);
    }
    else {
      filteredChannels = Lists.newArrayList();

      if (!caseSensitive) {
        constraint = constraint.toLowerCase();

        if (keywordArraySplitRegex != null) keywordArraySplitRegex = keywordArraySplitRegex.toLowerCase();
      }

      String[] keywords = (keywordArraySplitRegex != null)
        ? constraint.split(keywordArraySplitRegex)
        : new String[]{constraint};

      for (EPGChannel channel : allChannels) {
        String name = channel.getName();

        if (!caseSensitive) name = name.toLowerCase();

        for (String keyword : keywords) {
          if ((keyword.length() >= minKeywordLength) && name.contains(keyword)) {
            filteredChannels.add(channel);
            break;
          }
        }
      }
    }
  }

  @Override
  public EPGChannel getChannel(int channelPosition) {
    try {
      return filteredChannels.get(channelPosition);
    }
    catch(Exception e) {
      return null;
    }
  }

  @Override
  public List<EPGEvent> getEvents(int channelPosition) {
    EPGChannel channel = getChannel(channelPosition);
    return ((channel != null) && (data != null))
      ? data.get(channel)
      : Lists.newArrayList();
  }

  @Override
  public EPGEvent getEvent(int channelPosition, int programPosition) {
    try {
      return getEvents(channelPosition).get(programPosition);
    }
    catch(Exception e) {
      return null;
    }
  }

  @Override
  public int getChannelCount() {
    return filteredChannels.size();
  }

  @Override
  public boolean hasData() {
    return !filteredChannels.isEmpty();
  }

  // Comparator static class

  private static class AlphabeticOrderComparator implements Comparator<EPGChannel> {
    @Override
    public int compare(EPGChannel a, EPGChannel b) {
      if ((a == null) || (b == null)) throw new NullPointerException();

      return a.getName().compareTo(b.getName());
    }
  }

  // Comparator static instances

  private static final AlphabeticOrderComparator alphabeticOrderComparator = new AlphabeticOrderComparator();
}
