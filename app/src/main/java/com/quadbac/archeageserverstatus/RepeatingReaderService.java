package com.quadbac.archeageserverstatus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.quadbac.archeageserverstatus.model.ServerStatus;

import java.util.ArrayList;

/**
 * Created by Steve on 09/11/2014.
 */
public class RepeatingReaderService extends Service {

    private Updater updater;
    public static boolean isRunning = false;
    private ArrayList<ServerStatus> serverList = new ArrayList<ServerStatus>();
    public static final String BROADCAST_READER_RUNNING = "com.quadbac.archeageserverstatus.BROADCAST_READER_RUNNING";
    public static final String BROADCAST_READER_STOPPED = "com.quadbac.archeageserverstatus.BROADCAST_READER_STOPPED";
    public static final int NOTIFICATION_ID = 42;


    @Override
    public void onCreate() {
        super.onCreate();
        updater = new Updater();
        // Register to receive broadcasts from the FeedReaderService
        IntentFilter feedReaderFilter = new IntentFilter(FeedReaderService.BROADCAST_RESULTS_ACTION);
        StatusReadReceiver statusReadReceiver = new StatusReadReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                statusReadReceiver,
                feedReaderFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_service_notification)
                            .setContentTitle("Archeage Server Status")
                            .setContentText("Monitoring server status...");
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
            Notification notification = mBuilder.build();
            startForeground(NOTIFICATION_ID, notification);
            updater.start();
            // Broadcast service started
            Intent startedIntent = new Intent(BROADCAST_READER_RUNNING);
            LocalBroadcastManager.getInstance(this).sendBroadcast(startedIntent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRunning) {
            updater.interrupt();
            isRunning = false;
        }
        updater = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

  private class Updater extends Thread {
      private static final long DELAY = 60000;

      Updater() {
          super("Updater");
      }

      @Override
      public void run() {
          isRunning = true;
          while (isRunning) {
              try {
                  Thread.sleep(DELAY);
                  Intent readStatusIntent = new Intent(RepeatingReaderService.this.getApplicationContext(), FeedReaderService.class);
                  readStatusIntent.putParcelableArrayListExtra(FeedReaderService.CURRENT_SERVER_LIST, serverList);
                  startService(readStatusIntent);
              } catch (InterruptedException e) {
                  isRunning = false;
                  // Broadcast service stopped
                  Intent startedIntent = new Intent(BROADCAST_READER_STOPPED);
                  LocalBroadcastManager.getInstance(RepeatingReaderService.this).sendBroadcast(startedIntent);
              }
          }
      }
  }

    // Broadcast receiver for receiving status updates from the FeedReaderService
    private class StatusReadReceiver extends BroadcastReceiver
    {
        // Prevents instantiation
        private StatusReadReceiver() {
        }
        // Called when the StatusReadReceiver gets an Intent it's registered to receive
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(FeedReaderService.BROADCAST_RESULTS_ACTION)) {
                serverList = intent.getParcelableArrayListExtra(FeedReaderService.NEW_SERVER_LIST);
            }
        }
    }

}
