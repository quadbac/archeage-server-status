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

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.quadbac.archeageserverstatus.OnStatusReadListener;

public class StatusReader {
    private static String SERVER_STATUS_URI = "http://api.youenvy.us/?request=status&output=JSON";
    private ArrayList<String> notifyList;
    private ArrayList<OnStatusReadListener> listeners = new ArrayList<OnStatusReadListener>();

    public StatusReader(ArrayList<String> notifyList)
    {
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
                    return "FAIL";
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                return "FAIL";
            } catch (IOException e) {
                e.printStackTrace();
                return "FAIL";
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
        if (!result.equals("FAIL")) {
            try {
                JSONObject serverJSON = new JSONObject(result);
                JSONObject envy = serverJSON.getJSONObject("envy");
                JSONObject servers = envy.getJSONObject("servers");
                JSONArray array;
                array = servers.getJSONArray("europe");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject o = array.getJSONObject(i);
                    serverList.add(new ServerStatus(o.getString("serverName"), o.getString("serverStatus"), o.getString("latency"), notifyList.contains(o.getString("serverName")), ServerStatus.EU_SERVERS));
                }
                array = servers.getJSONArray("northAmerica");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject o = array.getJSONObject(i);
                    serverList.add(new ServerStatus(o.getString("serverName"), o.getString("serverStatus"), o.getString("latency"), notifyList.contains(o.getString("serverName")), ServerStatus.NA_SERVERS));
                }
                array = envy.getJSONArray("services");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject o = array.getJSONObject(i);
                    serverList.add(new ServerStatus(o.getString("serviceName"), o.getString("serviceStatus"), "", notifyList.contains(o.getString("serviceName")), ServerStatus.SERVICES));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        notifyStatusRead(serverList);
    }

    public void addListener (OnStatusReadListener listener) {
        listeners.add(listener);
    }

    public void removeListener (OnStatusReadListener listener) {
        listeners.remove(listener);
    }

    public void notifyStatusRead (ArrayList<ServerStatus> serverList) {
        for (OnStatusReadListener listener : listeners) {
            listener.onStatusRead(serverList);
        }
    }
}

