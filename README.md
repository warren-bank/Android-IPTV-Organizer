#### [IPTV Organizer](https://github.com/warren-bank/Android-IPTV-Organizer)

Android app that organizes IPTV channel and EPG information.

#### Features:

* M3U (IPTV channels) and XMLTV (EPG) files can be imported from either a network URL or the local filesystem
* during import&hellip;
  - M3U (IPTV channels) data can&hellip;
    * assign a new channel ID value based on the channel name
    * assign a new channel ID value based on the channel ID
    * determine whether to save (or discard) channels by whitelist and blacklist filters:
      1. channel name
      2. channel ID
         - after all remapping assignments have occurred
  - XMLTV (EPG) data can&hellip;
    * determine whether to save (or discard) channels by the existence of a matching M3U (IPTV channel)
      - where matching is based on the equality of either of the fields:
        1. channel name
        2. channel ID
* during display&hellip;
  - M3U (IPTV channels) data can be&hellip;
    * searched by keyword
    * sorted by either:
      - sequential order (as channels occur in the M3U file)
      - alphabetic order (by channel name)
    * clicked to start an Intent, which can open the channel's audio/video stream in an external media player
  - XMLTV (EPG) data can be&hellip;
    * searched by keyword
    * clicked to start an Intent, which can open the channel's audio/video stream in an external media player

#### Settings:

* Settings
  1. Auto save data after file import
     - when `true`: Imported data is immediately saved to DB
     - when `false`: Imported data is copied to text field in dialog, but not yet saved to DB
     - default: `true`
  2. Auto close dialog after file import
     - when `true`: Close dialog after data is imported and automatically saved to DB
     - when `false`: Do not close dialog after data is imported and automatically saved to DB
     - default: `true`
  3. Apply static string values to user-defined default import URL templates
     - when `true`: Default import URL values are templates
     - when `false`: Default import URL values are not templates
     - default: `true`
* M3U Channels
  1. Default M3U Playlist URL
     - specify the initial value in the dialog: "Import M3U Playlist URL"
     - example:
       * is not template:
         ```text
         http://kytv.xyz:25461/playlist/USERNAME/PASSWORD/m3u_plus?output=hls
         ```
       * is template:
         ```text
         %4$s/playlist/%1$s/%2$s/m3u_plus?output=hls
         ```
  2. Map from channel name to channel ID
     - specify one mapping per line in the format:
       ```text
       "${target_value}\s+=>\s+${new_tvg_id}"
       ```
     - where `target_value` is equal to either:
       * channel `name`
       * channel `tvg_name`
     - where `new_tvg_id` should match an EPG channel ID
     - example:
       ```text
       USA C-SPAN            => cspan.us
       USA C-SPAN 2          => cspan2.us
       USA C-SPAN 3          => cspan3.us
       USA CNN INTERNATIONAL => cnninternational.us
       ```
  3. Map from channel ID to channel ID
     - specify one mapping per line in the format:
       ```text
       "${target_value}\s+=>\s+${new_tvg_id}"
       ```
     - where `target_value` is equal to:
       * channel `tvg_id`
     - where `new_tvg_id` should match an EPG channel ID
     - example:
       ```text
       cspan            => cspan.us
       cspan2           => cspan2.us
       cspan3           => cspan3.us
       cnninternational => cnninternational.us
       ```
  4. Filter by channel name whitelist
     - specify one channel name (ie: `target_value`) per line
     - where `target_value` is equal to either:
       * channel `name`
       * channel `tvg_name`
     - example:
       ```text
       USA C-SPAN
       USA C-SPAN 2
       USA C-SPAN 3
       USA CNN INTERNATIONAL
       ```
     - when `target_value` begins with the sequence: `+*`
       * rather than being tested for equality to the value of this string,<br>channel fields are tested for the presence of this substring (excluding the leading 2-char sequence)
       * examples:
         1. VOD series
            - `target_value` = `+*Supernatural S`
            - M3U channel names that pass the filter:
              ```text
              Supernatural S01E01
              Supernatural S01E02
              Supernatural S01E03
              ```
         2. alternate live streams
            - `target_value` = `+*CNN`
            - M3U channel names that pass the filter:
              ```text
              US: CNN
              CA: CNN
              CNN International
              ```
         3. related live streams
            - `target_value` = `+*ESPN`
            - M3U channel names that pass the filter:
              ```text
              ESPN
              ESPN+
              ESPN2
              ESPN3
              ESPNU
              ```
  5. Filter by channel ID whitelist
     - specify one channel ID (ie: `target_value`) per line
     - where `target_value` is equal to:
       * channel `tvg_id`
     - example:
       ```text
       cspan.us
       cspan2.us
       cspan3.us
       cnninternational.us
       ```
  6. Filter by channel name blacklist
     - specify one channel name (ie: `target_value`) per line
     - format of input and usage is identical to the "channel name whitelist"
       * matching M3U channels are discarded during import
  7. Filter by channel ID blacklist
     - specify one channel ID (ie: `target_value`) per line
     - format of input and usage is identical to the "channel ID whitelist"
       * matching M3U channels are discarded during import
  8. Media URL static string values
     - specify one static string (ie: `target_value`) per line
     - where all substring occurances of `target_value` in channel `media_url` values are replaced by template variables during import of M3U (IPTV channels) data
     - when static string values are updated, channel `media_url` values will immediately reflect the changes&hellip; the media URLs are rehydrated by substituting the static strings for template variables
       * be careful to not remove or reorder static strings, as doing so will require the reimport of M3U (IPTV channels) data to correct the channel `media_url` template strings
     - example:
       ```text
       USERNAME
       PASSWORD
       http://kytv.xyz:80
       http://kytv.xyz:25461
       ```
* EPG Channels
  1. Default XMLTV EPG URL
     - specify the initial value in the dialog: "Import XMLTV EPG URL"
     - example:
       * is not template:
         ```text
         http://kytv.xyz:80/xmltv.php?username=USERNAME&password=PASSWORD
         ```
       * is template:
         ```text
         %3$s/xmltv.php?username=%1$s&password=%2$s
         ```
  2. Apply Import Filter
     - when `true`: Import channels found in M3U
     - when `false`: Import all channels
     - default: `true`

#### Intent filters:

Enables the automatic updating of IPTV channel and EPG information from externally bookmarked network URLs.

Supported Intents:

__Import M3U (IPTV channels)__

1. action = `android.intent.action.VIEW`
   * (optional) package = `com.github.warren_bank.iptv_organizer`
   * (optional) class   = `com.github.warren_bank.iptv_organizer.ui.ChannelsActivity`
   * data = `<any URL>`
   * type = any of:
     ```json
     [
       "application/vnd.apple.mpegurl",
       "application/mpegurl",
       "audio/mpegurl",
       "video/mpegurl",
       "application/x-mpegurl",
       "audio/x-mpegurl",
       "video/x-mpegurl",
       "application/x-mpegURL",
       "audio/x-mpegURL",
       "video/x-mpegURL"
     ]
     ```
2. action = `android.intent.action.VIEW`
   * (optional) package = `com.github.warren_bank.iptv_organizer`
   * (optional) class   = `com.github.warren_bank.iptv_organizer.ui.ChannelsActivity`
   * data = `<URL that ends with a .m3u or .M3U file extension>`

__Import XMLTV (EPG)__

1. action = `android.intent.action.VIEW`
   * (optional) package = `com.github.warren_bank.iptv_organizer`
   * (optional) class   = `com.github.warren_bank.iptv_organizer.ui.EpgActivity`
   * data = `<any URL>`
   * type = any of:
     ```json
     [
       "application/xml",
       "application/xmltv",
       "text/xml",
       "text/xmltv"
     ]
     ```
2. action = `android.intent.action.VIEW`
   * (optional) package = `com.github.warren_bank.iptv_organizer`
   * (optional) class   = `com.github.warren_bank.iptv_organizer.ui.EpgActivity`
   * data = `<URL that ends with a .xml, .xmltv, .XML, or .XMLTV file extension>`

#### Legal:

* copyright: [Warren Bank](https://github.com/warren-bank)
* license: [GPL-2.0](https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt)
