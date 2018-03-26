package com.example.xpl.map;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by xpl on 17-11-27.
 */

public class NetworkUtils {

    final static String WEATHER_URL = "http://www.sojson.com/open/api/weather/json.shtml?city=";

    //获得URL
    public static URL buildUrl(String city){
        String weather_url = WEATHER_URL+city;
        Uri builtUri = Uri.parse(weather_url).buildUpon().build();
        URL url = null;
        try{
            url = new URL(builtUri.toString());
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        return url;
    }


    public static String getResponseFromHttpUrl(URL url) throws IOException{
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
        try{
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if(hasInput){
                return scanner.next();
            }else{
                return null;
            }
        }finally {
            urlConnection.disconnect();
        }
    }
}