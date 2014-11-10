package com.quadbac.archeageserverstatus;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.quadbac.archeageserverstatus.OnStatusReadListener;
import com.quadbac.archeageserverstatus.R;
import com.quadbac.archeageserverstatus.model.ServerStatus;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Steve on 07/11/2014.
 */
public class FeedReaderService extends IntentService {

    private static int notificationId = 0;
    private static final String SERVER_STATUS_URI = "http://api.youenvy.us/?request=status&output=JSON";
    public static final String CURRENT_SERVER_LIST = "CURRENT_SERVER_LIST";
    public static final String NEW_SERVER_LIST = "NEW_SERVER_LIST";
    public static final String BROADCAST_READING_ACTION = "com.quadbac.archeageserverstatus.BROADCAST_READING_ACTION";
    public static final String BROADCAST_RESULTS_ACTION = "com.quadbac.archeageserverstatus.BROADCAST_RESULTS_ACTION";
    private SharedPreferences mPrefs;

    public FeedReaderService() {
        super("FeedReaderService");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
         // Gets data from the incoming Intent
        ArrayList<ServerStatus> currentServerList = workIntent.getParcelableArrayListExtra(CURRENT_SERVER_LIST);
//        ArrayList<String> notifyList = workIntent.getStringArrayListExtra(NOTIFY_LIST);

        // Retrieve list of notification servers from shared preferences
        mPrefs = getSharedPreferences("notificationPrefs", Context.MODE_PRIVATE);
        Set<String> notificationSet = mPrefs.getStringSet("notificationSet", new HashSet<String>());
        ArrayList<String> notifyList = new ArrayList<String>(notificationSet);

        // Broadcast start of server data read
        Intent startedIntent = new Intent(BROADCAST_READING_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(startedIntent);
        // Do work here, based on the contents of dataString
        ArrayList<ServerStatus> newServerList = parseServerJSON(readServerStatus(), notifyList);

        // Check for possible notifications needed
        for (ServerStatus newStatus : newServerList) {
            if (notifyList.contains(newStatus.getName()) && (currentServerList != null)) {
                for (ServerStatus oldStatus : currentServerList) {
                    if ((oldStatus.getName().equals(newStatus.getName())) && !(oldStatus.getStatus().equals(newStatus.getStatus()))) {
                        // Status has changed and server is on the notify list, fire off a notification
                        sendNotification(newStatus);
                    }
                }
            }
        }

        // Broadcast the new server list
        // Broadcast start of server data read
        Intent resultsIntent = new Intent(BROADCAST_RESULTS_ACTION).putParcelableArrayListExtra(NEW_SERVER_LIST, newServerList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultsIntent);

    }

    private String readServerStatus() {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(SERVER_STATUS_URI);
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
                Log.e(SERVER_STATUS_URI, "Failed to retrieve JSON");
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

    private ArrayList<ServerStatus> parseServerJSON(String result, ArrayList<String> notifyList) {
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
        return serverList;
    }

    private void sendNotification(ServerStatus newStatus) {
        Date date = new Date();
        String dateTime = DateFormat.getTimeInstance().format(date) + " on " + DateFormat.getDateInstance().format(date);
        String title = newStatus.getName() + " status";
        String text = ((newStatus.getRegion() == ServerStatus.SERVICES) ? "Service" : "Server") + " " + newStatus.getStatus().toLowerCase() + " at " + dateTime;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(text);
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, com.quadbac.archeageserverstatus.MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(com.quadbac.archeageserverstatus.MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(notificationId++, mBuilder.build());
    }

}