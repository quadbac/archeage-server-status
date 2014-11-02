/**
 * Created by Steve on 02/11/2014.
 */
package com.quadbac.archeageserverstatus.model;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.quadbac.archeageserverstatus.OnStatusReadListener;

public class StatusReader {

    public final static int EU_SERVERS = 1;
    public final static int NA_SERVERS = 2;
    public final static int SERVICES = 3;

    private static String SERVER_STATUS_URI = "http://api.youenvy.us/?request=status&output=JSON";
    private OnStatusReadListener listener;
    private int region;
    private ArrayList<String> notifyList;

    public StatusReader(OnStatusReadListener listener, int region, ArrayList<String> notifyList) {
        this.listener = listener;
        this.region = region;
        this.notifyList = notifyList;
    }

    public void readStatus() {
        new GetJSONTask().execute(SERVER_STATUS_URI);
    }

    private class GetJSONTask extends AsyncTask<String, Void, String> {
        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected String doInBackground(String... urls) {
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urls[0]);
            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } else {
                    Log.e(urls[0], "Failed to retrieve JSON");
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return builder.toString();
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(String result) {
            parseJSON(result);
        }
    }

    private void parseJSON(String result) {
         ArrayList<ServerStatus> serverList = new ArrayList<ServerStatus>();
        try {
            JSONObject serverJSON = new JSONObject(result);
            JSONObject envy = serverJSON.getJSONObject("envy");
            JSONObject servers = envy.getJSONObject("servers");
            JSONArray array;
            switch (region) {
                case EU_SERVERS:
                    array = servers.getJSONArray("europe");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject o = array.getJSONObject(i);
                        serverList.add(new ServerStatus(o.getString("serverName"), o.getString("serverStatus"), o.getString("latency"), notifyList.contains(o.getString("serverName"))));
                    }
                    break;
                case NA_SERVERS:
                    array = servers.getJSONArray("northAmerica");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject o = array.getJSONObject(i);
                        serverList.add(new ServerStatus(o.getString("serverName"), o.getString("serverStatus"), o.getString("latency"), notifyList.contains(o.getString("serverName"))));
                    }
                    break;
                case SERVICES:
                    array = envy.getJSONArray("services");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject o = array.getJSONObject(i);
                        serverList.add(new ServerStatus(o.getString("serviceName"), o.getString("serviceStatus"), "", notifyList.contains(o.getString("serviceName"))));
                    }
                    break;
            }

            listener.onStatusRead(serverList);

        } catch (Exception e) {
            e.printStackTrace();
            listener.onStatusRead(serverList);
        }
    }
}

