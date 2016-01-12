package ljl.com.homuraproject;

/**
 * Created by hzfd on 2016/1/6.
 */

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class LyricSearch {
    private URL url;
    public static final String DEFAULT_LOCAL = "GB2312";
    StringBuffer sb = new StringBuffer();
    private InputStreamReader in = null;
    private BufferedReader br = null;
    private int idnumber = 0;

    public LyricSearch(String musicName, String singerName) {
        musicName = musicName.replace('?', '+');
        singerName = singerName.replace('?', '+');
        musicName = musicName.replace(' ', '+');
        singerName = singerName.replace(' ', '+');
        musicName = "Yellow";
        singerName = "Coldplay";
        int eventType = 0;
        try {
            musicName = URLEncoder.encode(musicName, "UTF-8");
            singerName = URLEncoder.encode(singerName, "UTF-8");
            musicName = musicName.replaceAll("%2B", "+");
            singerName = singerName.replaceAll("%2B", "+");
            String strUrl = "http://box.zhangmen.baidu.com/x?op=12&count=1&title="
                    + musicName + "$$" + singerName + "$$$$";
            Log.d("CHENGJR1", "strUrl =" + strUrl);
            url = new URL(strUrl);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5 * 1000);
            conn.setRequestMethod("GET");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        InputStream inStream = null;
        try {
            inStream = conn.getInputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            eventType = parser.getEventType();
        } catch (XmlPullParserException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    // 获取解析器当前指向的元素的名称
                    String name = parser.getName();
                    Log.d("CHENGJR1", "name = " + name);
                    if ("lrcid".equals(name)) {
                        try {
                            idnumber = Integer.parseInt(parser.nextText());
                        } catch (NumberFormatException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (XmlPullParserException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Log.d("CHENGJR1", "idnumber = " + idnumber);
                    }
                    break;
            }
            try {
                eventType = parser.next();
            } catch (XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    public ArrayList fetchLyric() {
        String strid = "";
        if (idnumber == 0) {
            return null;
        }
        String geciURL = "http://box.zhangmen.baidu.com/bdlrc/" + idnumber
                / 100 + "/" + idnumber + ".lrc";
        Log.d("CHENGJR1", "geciURL = " + geciURL);
        ArrayList gcContent = new ArrayList();
        String s = new String();
        try {
            url = new URL(geciURL);
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(url.openStream(),
                    "GB2312"));
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if (br == null) {
            Log.d("CHENGJR1", "br is  null");
        } else {
            try {
                while ((s = br.readLine()) != null) {
                    // Sentence sentence = new Sentence(s);
                    gcContent.add(s);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return gcContent;
    }
}
