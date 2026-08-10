package com.github.warren_bank.iptv_organizer.data.model;

import com.github.warren_bank.filterablerecyclerview.FilterableListItem;

import java.util.Comparator;

public class ChannelListItem implements FilterableListItem {
  public final int position;
  public final String name;
  public       String media_url;
  public       String tvg_id;
  public final String tvg_name;

  public ChannelListItem(int position, String name, String media_url, String tvg_id, String tvg_name) {
    if (name == null) name = "[undefined]";

    this.position  = position;
    this.name      = name;
    this.media_url = media_url;
    this.tvg_id    = tvg_id;
    this.tvg_name  = tvg_name;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public String getFilterableValue() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;

    if ((obj == null) || !(obj instanceof ChannelListItem))
      return false;

    ChannelListItem that = (ChannelListItem) obj;

    if (!this.media_url.equals(that.media_url))
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return this.media_url.hashCode();
  }

  public boolean areNamesEqual() {
    return this.name.equals(this.tvg_name);
  }

  // Comparator private classes

  private static class SequentialOrderComparator implements Comparator<FilterableListItem> {
    @Override
    public int compare(FilterableListItem x, FilterableListItem y) {
      if ((x == null) || (y == null)) throw new NullPointerException();

      ChannelListItem a = (ChannelListItem) x;
      ChannelListItem b = (ChannelListItem) y;

      if (a.position < b.position) return -1;
      if (a.position > b.position) return 1;
      return 0;
    }
  }

  private static class AlphabeticOrderComparator implements Comparator<FilterableListItem> {
    @Override
    public int compare(FilterableListItem x, FilterableListItem y) {
      if ((x == null) || (y == null)) throw new NullPointerException();

      ChannelListItem a = (ChannelListItem) x;
      ChannelListItem b = (ChannelListItem) y;

      return a.name.compareTo(b.name);
    }
  }

  // Comparator static instances

  public static final SequentialOrderComparator sequentialOrderComparator = new SequentialOrderComparator();
  public static final AlphabeticOrderComparator alphabeticOrderComparator = new AlphabeticOrderComparator();
}
