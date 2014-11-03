package com.learning.dino.photogalery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dbulj on 02/11/2014.
 * ThumbnailDownloader is how we schedkule work on the background thread from the main thread using
 * ThumbnailDownloader's handler.
 * Token is a generic argument for some object used to id each download.
 */
public class ThumbnailDownloader<Token> extends HandlerThread {

    private static final String TAG = "ThubnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0; //Message 'what' used to describe the message

    Handler mHandler; //Message 'target' handler that will process the message
    Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>()); //Message 'obj' object sent with message

    //Tie ThumbnailDownloader via mResponseHandler to the main thread's looper.
    Handler mResponseHandler; //handler tied to the main thread's looper handler passed in c-tor.

    //Listener interface to communicate the responses with and do UI work with the returniing Bitmaps.
    Listener<Token> mListener;
    public interface Listener<Token>{
        //void onThumbnailDownloaded(Token token, String url, Bitmap thumbnail); //CHALLANGE, Ch27
        void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }
    public void setListener(Listener<Token> listener){
        mListener = listener;
    }

    //public ThumbnailDownloader(){
    public ThumbnailDownloader(Handler responseHandler){
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared(){
        //onLooperPrepared is called before the Looper checks the queue for the first time, so
        //we create here our Handler
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                //so check for message type, retrieve the Token and pass it to handleRequest()
                if (msg.what == MESSAGE_DOWNLOAD){
                    @SuppressWarnings("unchecked")
                    Token token = (Token)msg.obj;
                    Log.i(TAG, "Got a request for url: " + requestMap.get(token));
                    handleRequest(token);
                }
            }
        };
    }

    public void queueThumbnail(Token token, String url){
        Log.i(TAG, "Got an Url: " + url);
        requestMap.put(token, url); //add passed in Token URL pair to the map
        //then obtain a message, give it the token as its object, and send it off to be put on the
        //message queue
        mHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
    }

    private void handleRequest(final Token token){
        //here is where downloading happens
        try{
            final String url = requestMap.get(token);
            if (url == null){
                return;
            }

            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);

            //use BitmapFactory to construct a bitmap with the array of bytes returned
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");

            //Handler.post() is a convenience method for posting Message.  Because mResponseHandler
            //is associated with the main (UI) thread's Looper, this UI update will run on the main
            //thread.
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    //Since GridView recycles its views, we check that each Token gets teh correct
                    //image even if another request has been made in the meantime.
                    if (requestMap.get(token) != url){
                        return;
                    }
                    requestMap.remove(token);
                    //mListener.onThumbnailDownloaded(token, url, bitmap); //CHALLANGE, Ch27
                    mListener.onThumbnailDownloaded(token, bitmap);
                }
            });
        }catch(IOException ioe){
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    //If user rotates the device, ThumbnailDownloader may be handing on the invalid ImageViews.  So,
    //this method will clean all the requests our of message queue.
    public void clearQueue(){
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }
}

