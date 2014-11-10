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
    public static final String PREF_SEARCH_QUERY = "searchQuery"; //key for SharedPreference value saved in onNewIntent override in PhotoGaleryActivity
    public static final String PREF_LAST_RESULT_ID = "lastResultId"; //Since your service will be polling for new result, it needs to know last fetched result

    private static final String ENDPOINT = "https://api.flickr.com/services/rest/";  //flickr rest service
    private static final String API_KEY = "d7301a53e45b6ad92997e36d14fb6850"; //your flickr api key
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent"; //flickr method name
    private static final String METHOD_SEARCH = "flickr.photos.search"; //flickr search method name
    private static final String PARAM_EXTRAS = "extras"; //
    private static final String EXTRA_SMALL_URL = "url_s";  //this tells Flickr to include URL for the small version of the picture if available
    private static final String PARAM_TEXT = "text"; //text query parameter passed to flicker.photos.search
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
    //public ArrayList<GaleryItem> fetchItems(){
    public ArrayList<GaleryItem> downloadGalleryItems(String url){
        ArrayList<GaleryItem> items = new ArrayList<GaleryItem>();

        try{
            /*
             Moved to new fetchItems() below
            String url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method", METHOD_GET_RECENT)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                    .build().toString();
            */
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

    public ArrayList<GaleryItem> fetchItems(){
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_GET_RECENT)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .build().toString();
        return downloadGalleryItems(url);
    }

    public ArrayList<GaleryItem> search(String query){
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_SEARCH)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .appendQueryParameter(PARAM_TEXT, query)
                .build().toString();
        return downloadGalleryItems(url);
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
