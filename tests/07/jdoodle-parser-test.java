// https://www.jdoodle.com/online-java-compiler

public class MyClass {
  public static void main(String args[]) {
    testLine("""
#EXTINF:-1 tvg-id="starz.us" tvg-name="USA StarZ East" tvg-logo="https://starz.imgix.net/BuyStarz/international/sz-logo-2x.png?auto=compress,format" group-title="USA Movies Channels",USA StarZ East
""");
    testLine("""
#EXTINF:-1 tvg-id="" tvg-name="24/7 Beverly Hills, 90210" tvg-logo="" group-title="24/7 Classic Show",24/7 Beverly Hills, 90210
""");
    testLine("""
#EXTINF:-1 tvg-id="" tvg-name="24/7 Gomer Pyle, U.S.M.C." tvg-logo="https://www.gstatic.com/tv/thumb/tvbanners/394788/p394788_b_v8_ab.jpg" group-title="24/7 Classic Show",24/7 Gomer Pyle, U.S.M.C.
""");
    testLine("""
#EXTINF:-1 tvg-id="" tvg-name="24/7 Petticoat Junction" tvg-logo="https://m.media-amazon.com/images/M/MV5BMTI2MDE2MDA0M15BMl5BanBnXkFtZTcwOTM1MzU1MQ@@._V1_UY1200_CR285,0,630,1200_AL_.jpg" group-title="24/7 Classic Show",24/7 Petticoat Junction
""");
    testLine("""
#EXTINF:-1 tvg-id="" tvg-name="CA | Poway, Etc. | Telemundo KUAN" tvg-logo="http://172.110.220.61:80/images/97db854115c384be584aba81866fdd46.png" group-title="USA Local Channels ( Full List )",CA | Poway, Etc. | Telemundo KUAN
""");
    testLine("""
#EXTINF:-1 tvg-id="" tvg-name="24/7 Superstar Family Movie" tvg-logo="https://purewows3.imgix.net/images/articles/2019_04/The-Goonies-family-movie.jpg?auto=format,compress&cs=strip" group-title="24/7 Movie Categories",24/7 Superstar Family Movie
""");
    testLine("""
#EXTINF:-1 tvg-id="" tvg-name="24/7 Dr. Quinn, Medicine Woman" tvg-logo="http://172.110.220.61:80/images/d86514bd294b183f961c43d5c8dccaca.jpg" group-title="24/7 Western",24/7 Dr. Quinn, Medicine Woman
""");
    testLine("""
#EXTINF:-1 tvg-id="" tvg-name="24/7 Sabrina, the Teenage Witch" tvg-logo="http://23.227.147.172:80/images/a431bfe14340c308e4000c2485dd03c7.jpg" group-title="24/7 Kids & Family",24/7 Sabrina, the Teenage Witch
""");
    testLine("""
#EXTINF:-1 tvg-id="" tvg-name="USA Red Bull TV" tvg-logo="https://img.redbull.com/images/e_trim:10:transparent/c_limit,w_466,h_256/bo_5px_solid_rgb:00000000/q_auto,f_png/redbullcom/2016/10/01/1331821221085_2/red-bull-tv" group-title="USA Sports",USA Red Bull TV
""");
    testLine("""
#EXTINF:-1 tvg-id="lonestar.us" tvg-name="USA Lone Star" tvg-logo="https://static.wixstatic.com/media/2d47fc_b61ff2842d68432d827385d49b8be182~mv2.png/v1/crop/x_0,y_31,w_720,h_657/fill/w_276,h_252,al_c,q_85,usm_0.66_1.00_0.01,enc_auto/LoneStar_light%20transparent%20square%20(1)_pn.png" group-title="USA Movies Channels",USA Lone Star
""");
    testLine("""
#EXTINF:-1 tvg-id="" tvg-name="USA Recipe TV" tvg-logo="data:image/png;base64,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" group-title="USA Entertainment",USA Recipe TV
""");
    testLine("""
#EXTINF:-1 tvg-id="" tvg-name="24/7 Walker, Texas Ranger" tvg-logo="" group-title="24/7 Action & Adventure",24/7 Walker, Texas Ranger
""");
    testLine("""
#EXTINF:-1 tvg-id="" tvg-name="USA News4 WJXT Jacksonville" tvg-logo="https://images.crunchbase.com/image/upload/c_lpad,f_auto,q_auto:eco,dpr_1/v1509760902/yzlhonp4wjzkwwhabvq2.png" group-title="USA Local - MISC",USA News4 WJXT Jacksonville
""");
    testLine("""
#EXTINF:-1 tvg-id="" tvg-name="24/7 Below Deck Mediterranean" tvg-logo="data:image/jpeg;base64,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" group-title="24/7 Reality",24/7 Below Deck Mediterranean
""");
    testLine("""
#EXTINF:-1 tvg-id="fox24kpej.us" tvg-name="USA FOX 24 KPEJ Midland" tvg-logo="data:image/jpeg;base64,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" group-title="USA Local - FOX",USA FOX 24 KPEJ Midland
""");
    testLine("""
#EXTINF:-1 tvg-id="" tvg-name="DAZN CA 02: Kings World Cup Clubs - Day 3 (In English, Portuguese & Spanish) @ 28 Jul 12:00 PM ET" tvg-logo="https://image.discovery.indazn.com/eu/v3/eu/none/2oax0it2kctv3a683g7ialfu3_image-header_pRow_1747824741000/fill/center/top/none/85/800/600/png/image" group-title="Sports DAZN",DAZN CA 02: Kings World Cup Clubs - Day 3 (In English, Portuguese & Spanish) @ 28 Jul 12:00 PM ET
""");
  }

  private static void testLine(String line) {
    SimpleM3UParser.M3U_Entry curEntry = SimpleM3UParser.parseExtInf(line);
    System.out.println(curEntry.name);
  }

  // ---------------------------------------------------------------------------
  // based on:
  //   https://github.com/gsantner/opoc/raw/5e8a6445ae7a079ce9c0dd4f4e6f9a9b6d3fd5a4/java/java/net/gsantner/opoc/format/playlist/SimpleM3UParser.java
  // copyright:
  //   2019-2022 Gregor Santner <https://gsantner.net/>
  // license:
  //   Unlicense <https://unlicense.org/>
  // ---------------------------------------------------------------------------
  private static class SimpleM3UParser {
    private final static String EXTINF_TAG = "#EXTINF:";
    private final static String EXTINF_TVG_NAME = "tvg-name=\"";
    private final static String EXTINF_TVG_ID = "tvg-id=\"";
    private final static String EXTINF_TVG_LOGO = "tvg-logo=\"";
    private final static String EXTINF_TVG_EPGURL = "tvg-epgurl=\"";
    private final static String EXTINF_GROUP_TITLE = "group-title=\"";
    private final static String EXTINF_RADIO = "radio=\"";
    private final static String EXTINF_TAGS = "tags=\"";

    public static class M3U_Entry {
      public String tvgName, name;
      public String tvgLogo;
      public String tvgEpgUrl;
      public String tvgId;
      public String groupTitle;
      public String url;
      public String[] tags = new String[0];
      public int seconds = -1;
      public boolean isRadio = false;
    }

    public static M3U_Entry parseExtInf(String line) {
      M3U_Entry curEntry = new M3U_Entry();
      StringBuilder buf = new StringBuilder(20);
      if (line.length() < EXTINF_TAG.length() + 1) {
        return curEntry;
      }

      // Strip tag
      line = line.substring(EXTINF_TAG.length());

      // Read seconds (may end with comma or whitespace)
      while (line.length() > 0) {
        char c = line.charAt(0);
        if (Character.isDigit(c) || c == '-' || c == '+') {
          buf.append(c);
          line = line.substring(1);
        } else {
          break;
        }
      }
      if (buf.length() == 0 || line.isEmpty()) {
        return curEntry;
      }
      curEntry.seconds = Integer.parseInt(buf.toString());

      // tvg tags
      String old = null;
      while (!line.isEmpty() && !line.startsWith(",") && !line.equals(old)) {
        old = line = line.trim();
        if (line.startsWith(EXTINF_TVG_NAME) && line.length() > EXTINF_TVG_NAME.length()) {
          line = line.substring(EXTINF_TVG_NAME.length());
          int i = line.indexOf("\"");
          curEntry.tvgName = line.substring(0, i).replace("'", "");
          line = line.substring(i + 1);
        } else if (line.startsWith(EXTINF_TVG_LOGO) && line.length() > EXTINF_TVG_LOGO.length()) {
          line = line.substring(EXTINF_TVG_LOGO.length());
          int i = line.indexOf("\"");
          curEntry.tvgLogo = line.substring(0, i);
          line = line.substring(i + 1);
        } else if (line.startsWith(EXTINF_TVG_EPGURL) && line.length() > EXTINF_TVG_EPGURL.length()) {
          line = line.substring(EXTINF_TVG_EPGURL.length());
          int i = line.indexOf("\"");
          curEntry.tvgEpgUrl = line.substring(0, i);
          line = line.substring(i + 1);
        } else if (line.startsWith(EXTINF_RADIO) && line.length() > EXTINF_RADIO.length()) {
          line = line.substring(EXTINF_RADIO.length());
          int i = line.indexOf("\"");
          curEntry.isRadio = Boolean.parseBoolean(line.substring(0, i));
          line = line.substring(i + 1);
        } else if (line.startsWith(EXTINF_GROUP_TITLE) && line.length() > EXTINF_GROUP_TITLE.length()) {
          line = line.substring(EXTINF_GROUP_TITLE.length());
          int i = line.indexOf("\"");
          curEntry.groupTitle = line.substring(0, i);
          line = line.substring(i + 1);
        } else if (line.startsWith(EXTINF_TVG_ID) && line.length() > EXTINF_TVG_ID.length()) {
          line = line.substring(EXTINF_TVG_ID.length());
          int i = line.indexOf("\"");
          curEntry.tvgId = line.substring(0, i);
          line = line.substring(i + 1);
        } else if (line.startsWith(EXTINF_TAGS) && line.length() > EXTINF_TAGS.length()) {
          line = line.substring(EXTINF_TAGS.length());
          int i = line.indexOf("\"");
          curEntry.tags = line.substring(0, i).split(",");
          line = line.substring(i + 1);
        } else {
          line = line.substring(line.indexOf("\"") + 1);
          line = line.substring(line.indexOf("\"") + 1);
        }
      }

      // Name
      line = line.trim();
      if (line.length() > 1 && line.startsWith(",")) {
        line = line.substring(1);
        line = line.trim();
        if (!line.isEmpty()) {
          curEntry.name = line.replace("'", "");
        }
      }
      return curEntry;
    }
  }
}

/*
=======
output:
=======
USA StarZ East
24/7 Beverly Hills, 90210
24/7 Gomer Pyle, U.S.M.C.
24/7 Petticoat Junction
CA | Poway, Etc. | Telemundo KUAN
24/7 Superstar Family Movie
24/7 Dr. Quinn, Medicine Woman
24/7 Sabrina, the Teenage Witch
USA Red Bull TV
USA Lone Star
USA Recipe TV
24/7 Walker, Texas Ranger
USA News4 WJXT Jacksonville
24/7 Below Deck Mediterranean
USA FOX 24 KPEJ Midland
DAZN CA 02: Kings World Cup Clubs - Day 3 (In English, Portuguese & Spanish) @ 28 Jul 12:00 PM ET
=======
*/
