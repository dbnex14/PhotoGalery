package com.learning.dino.photogalery;

import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by dbulj on 28/10/2014.
 * Class handling networking.
 */
public class FlickrFetchr {

    public static final String TAG = "FlickrFetchr";

    private static final String ENDPOINT = "https://api.flickr.com/services/rest/";  //flickr rest service
    private static final String API_KEY = "d7301a53e45b6ad92997e36d14fb6850"; //your flickr api key
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent"; //flickr method name
    private static final String PARAM_EXTRAS = "extras"; //
    private static final String EXTRA_SMALL_URL = "url_s";  //this tells Flickr to include URL for the small version of the picture if available

    private static final String XML_PHOTO = "photo"; //constant specifying name of photo XML element

    byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = conn.getInputStream();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK){
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0){
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally{
            conn.disconnect();
        }
    }

    public String getUrl(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    //public void fetchItems(){
    public ArrayList<GaleryItem> fetchItems(){
        ArrayList<GaleryItem> items = new ArrayList<GaleryItem>();

        try{
            String url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method", METHOD_GET_RECENT)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                    .build().toString();
            String xmlString = getUrl(url);
            Log.i(TAG, "Received xml: " + xmlString);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));

            parseItems(items, parser);
        }catch (IOException ioe){
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (XmlPullParserException xppe){
            Log.e(TAG, "Failed to parse items", xppe);
        }

        return items;
    }

    void parseItems(ArrayList<GaleryItem> items, XmlPullParser parser)
        throws XmlPullParserException, IOException {

        int eventType = parser.next();

        while(eventType != XmlPullParser.END_DOCUMENT){
            if (eventType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())){
                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null, "title");
                String smallUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);

                GaleryItem item = new GaleryItem();
                item.setId(id);
                item.setCaption(caption);
                item.setUrl(smallUrl);
                items.add(item);
            }
            eventType = parser.next();
        }
    }
}