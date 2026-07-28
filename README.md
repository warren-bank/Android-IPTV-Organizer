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
  2. Append M3U Playlists
     - when `true`: Imported data is added to the existing list of M3U channels
     - when `false`: Imported data replaces the existing list of M3U channels
     - default: `false`
  3. Map from channel name to channel ID
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
  4. Map from channel ID to channel ID
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
  5. Filter by channel name whitelist
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
  6. Filter by channel ID whitelist
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
  7. Filter by channel name blacklist
     - specify one channel name (ie: `target_value`) per line
     - format of input and usage is identical to the "channel name whitelist"
       * matching M3U channels are discarded during import
  8. Filter by channel ID blacklist
     - specify one channel ID (ie: `target_value`) per line
     - format of input and usage is identical to the "channel ID whitelist"
       * matching M3U channels are discarded during import
  9. Media URL static string values
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

__Update Settings__

1. explicit Intent with one or more of the following Extras
   * (required) package = `com.github.warren_bank.iptv_organizer`
   * (required) class   = `com.github.warren_bank.iptv_organizer.ui.SettingsActivity`
   * Extras:
     1. name = `SETTINGS_AUTO_SAVE`
        * type = `Boolean`
        * setting = _Settings &gt; Auto save data after file import_
     1. name = `SETTINGS_AUTO_CLOSE`
        * type = `Boolean`
        * setting = _Settings &gt; Auto close dialog after file import_
     1. name = `SETTINGS_APPLY_STATIC_STRINGS`
        * type = `Boolean`
        * setting = _Settings &gt; Apply static string values to user-defined default import URL templates_
     1. name = `M3U_DEFAULT_PLAYLIST_URL`
        * type = `String`
        * setting = _M3U Channels &gt; Default M3U Playlist URL_
     1. name = `M3U_APPEND_PLAYLISTS`
        * type = `Boolean`
        * setting = _M3U Channels &gt; Append M3U Playlists_
     1. name = `M3U_MAP_CHANNEL_NAME_TO_ID`
        * type = `String[]`
        * setting = _M3U Channels &gt; Map from channel name to channel ID_
        * specify one mapping per `String` in the format:
          ```text
          "${target_value}\s+=>\s+${new_tvg_id}"
          ```
     1. name = `M3U_MAP_CHANNEL_ID_TO_ID`
        * type = `String[]`
        * setting = _M3U Channels &gt; Map from channel ID to channel ID_
        * specify one mapping per `String` in the format:
          ```text
          "${target_value}\s+=>\s+${new_tvg_id}"
          ```
     1. name = `M3U_CHANNEL_NAME_WHITELIST`
        * type = `String[]`
        * setting = _M3U Channels &gt; Filter by channel name whitelist_
        * specify one channel name per `String`
     1. name = `M3U_CHANNEL_ID_WHITELIST`
        * type = `String[]`
        * setting = _M3U Channels &gt; Filter by channel ID whitelist_
        * specify one channel ID per `String`
     1. name = `M3U_CHANNEL_NAME_BLACKLIST`
        * type = `String[]`
        * setting = _M3U Channels &gt; Filter by channel name blacklist_
        * specify one channel name per `String`
     1. name = `M3U_CHANNEL_ID_BLACKLIST`
        * type = `String[]`
        * setting = _M3U Channels &gt; Filter by channel ID blacklist_
        * specify one channel ID per `String`
     1. name = `M3U_MEDIA_URL_STATIC_STRINGS`
        * type = `String[]`
        * setting = _M3U Channels &gt; Media URL static string values_
        * specify one static string per `String`
     1. name = `EPG_DEFAULT_XMLTV_URL`
        * type = `String`
        * setting = _EPG Channels &gt; Default XMLTV EPG URL_
     1. name = `EPG_APPLY_IMPORT_FILTER`
        * type = `Boolean`
        * setting = _EPG Channels &gt; Apply Import Filter_
2. example: how to update _IPTV-Organizer_ settings on a remote device that is also running [_ExoAirPlayer_](https://github.com/warren-bank/Android-ExoPlayer-AirPlay-Receiver)
   ```bash
   # network address for running instance of 'ExoPlayer AirPlay Receiver'
   airplay_ip='192.168.1.100:8192'
   
   post_body='
     package: com.github.warren_bank.iptv_organizer
     class: com.github.warren_bank.iptv_organizer.ui.SettingsActivity
     extra-SETTINGS_AUTO_SAVE: (bool) true
     extra-SETTINGS_AUTO_CLOSE: (bool) true
     extra-SETTINGS_APPLY_STATIC_STRINGS: (bool) true
     extra-M3U_DEFAULT_PLAYLIST_URL: %4$s/playlist/%1$s/%2$s/m3u_plus?output=hls
     extra-M3U_APPEND_PLAYLISTS: (bool) false
     extra-M3U_MAP_CHANNEL_NAME_TO_ID: USA C-SPAN            => cspan.us
     extra-M3U_MAP_CHANNEL_NAME_TO_ID: USA C-SPAN 2          => cspan2.us
     extra-M3U_MAP_CHANNEL_NAME_TO_ID: USA C-SPAN 3          => cspan3.us
     extra-M3U_MAP_CHANNEL_NAME_TO_ID: USA CNN INTERNATIONAL => cnninternational.us
     extra-M3U_MAP_CHANNEL_ID_TO_ID: cspan                   => cspan.us
     extra-M3U_MAP_CHANNEL_ID_TO_ID: cspan2                  => cspan2.us
     extra-M3U_MAP_CHANNEL_ID_TO_ID: cspan3                  => cspan3.us
     extra-M3U_MAP_CHANNEL_ID_TO_ID: cnninternational        => cnninternational.us
     extra-M3U_CHANNEL_NAME_WHITELIST: USA C-SPAN
     extra-M3U_CHANNEL_NAME_WHITELIST: USA C-SPAN 2
     extra-M3U_CHANNEL_NAME_WHITELIST: USA C-SPAN 3
     extra-M3U_CHANNEL_NAME_WHITELIST: USA CNN INTERNATIONAL
     extra-M3U_CHANNEL_NAME_WHITELIST: +*Supernatural S
     extra-M3U_CHANNEL_NAME_WHITELIST: +*CNN
     extra-M3U_CHANNEL_NAME_WHITELIST: +*ESPN
     extra-M3U_CHANNEL_ID_WHITELIST: cspan.us
     extra-M3U_CHANNEL_ID_WHITELIST: cspan2.us
     extra-M3U_CHANNEL_ID_WHITELIST: cspan3.us
     extra-M3U_CHANNEL_ID_WHITELIST: cnninternational.us
     extra-M3U_CHANNEL_NAME_BLACKLIST: (String[]) null
     extra-M3U_CHANNEL_ID_BLACKLIST: (String[]) null
     extra-M3U_MEDIA_URL_STATIC_STRINGS: USERNAME
     extra-M3U_MEDIA_URL_STATIC_STRINGS: PASSWORD
     extra-M3U_MEDIA_URL_STATIC_STRINGS: http://kytv.xyz:80
     extra-M3U_MEDIA_URL_STATIC_STRINGS: http://kytv.xyz:25461
     extra-EPG_DEFAULT_XMLTV_URL: %3$s/xmltv.php?username=%1$s&password=%2$s
     extra-EPG_APPLY_IMPORT_FILTER: (bool) true
   '
   
   curl --silent -X POST \
     -H "Content-Type: text/parameters" \
     --data-binary "$post_body" \
     "http://${airplay_ip}/start-activity"
   ```

#### Legal:

* copyright: [Warren Bank](https://github.com/warren-bank)
* license: [GPL-2.0](https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt)
