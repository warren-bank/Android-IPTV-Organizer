package com.github.warren_bank.iptv_organizer.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

  private static int offsetUTC = TimeZone.getDefault().getRawOffset();

  private static boolean isSameDayUTC(long timestamp1, long timestamp2) {
    long millisInDay = 24 * 60 * 60 * 1000L;
    return ((timestamp1 / millisInDay) == (timestamp2 / millisInDay));
  }

  private static boolean isSameDay(long timestamp1, long timestamp2) {
    return isSameDayUTC(timestamp1 + offsetUTC, timestamp2 + offsetUTC);
  }

  public static String[] formatTimestampRange(long timestamp1, long timestamp2, String defaultValue) {
    boolean OK1 = (timestamp1 >= 0);
    boolean OK2 = (timestamp2 >= 0);

    // short-circuit
    if (!OK1 && !OK2) return new String[]{defaultValue, defaultValue, null};

    boolean isToday = OK1        && isSameDay(timestamp1, System.currentTimeMillis());
    boolean sameDay = OK1 && OK2 && isSameDay(timestamp1, timestamp2);

    String format = (isToday && (sameDay || !OK2))
      ? "hh:mm aa"
      : "yyyy-MM-dd hh:mm aa";

    SimpleDateFormat sdf = new SimpleDateFormat(format);

    String format1 = OK1
      ? sdf.format(new Date(timestamp1))
      : defaultValue;

    String format2 = OK2
      ? sdf.format(new Date(timestamp2))
      : defaultValue;

    return new String[]{format1, format2, format};
  }

}
