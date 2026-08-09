package com.github.warren_bank.iptv_organizer.utils;

import java.util.List;
import java.util.Map;

public class FilterUtils {

  // preconditions:
  //   * if case_insensitive, then every value in haystack is already lowercase
  public static boolean listContains(List<String> haystack, String needle, boolean case_insensitive, boolean match_substring) {
    if ((haystack == null) || haystack.isEmpty() || (needle == null) || needle.isEmpty()) return false;

    if (case_insensitive) needle = needle.toLowerCase();

    // exact match: find needle in haystack
    if (!match_substring)
      return haystack.contains(needle);

    // fuzzy match: find any item in haystack that is a substring of needle
    for (String substr : haystack) {
      if (needle.contains(substr)) return true;
    }

    return false;
  }

  // preconditions:
  //   * if case_insensitive, then every key in haystack is already lowercase
  public static String mapContains(Map<String, String> haystack, String needle, boolean case_insensitive, boolean match_substring) {
    if ((haystack == null) || haystack.isEmpty() || (needle == null) || needle.isEmpty()) return null;

    if (case_insensitive) needle = needle.toLowerCase();

    // exact match: find needle in haystack (or null)
    if (!match_substring)
      return (String) haystack.get(needle);

    // fuzzy match: find any key in haystack that is a substring of needle
    for (Map.Entry<String, String> entry : haystack.entrySet()) {
      String substr = (String) entry.getKey();
      if (needle.contains(substr)) return (String) entry.getValue();
    }

    return null;
  }

}
