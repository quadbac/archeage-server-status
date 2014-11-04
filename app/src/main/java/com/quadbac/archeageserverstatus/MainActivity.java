package com.quadbac.archeageserverstatus;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.quadbac.archeageserverstatus.model.ServerListAdapter;
import com.quadbac.archeageserverstatus.model.ServerStatus;
import com.quadbac.archeageserverstatus.model.StatusReader;


public class MainActivity extends Activity implements OnStatusReadListener, ActionBar.TabListener {

    private StatusReader statusReader;
    private Handler statusHandler;
    private SharedPreferences mPrefs;
    private final static int READ_STATUS = 1;
    private final static int SERVICE_PAGE = 0;
    private final static int EU_SERVER_PAGE = 1;
    private final static int NA_SERVER_PAGE = 2;
    private ArrayList<ServerStatus> serverList = new ArrayList<ServerStatus>();
    private ArrayList<String> notifyList = new ArrayList<String>();
    private int notificationId = 0;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve list of notification servers from shared preferences
        mPrefs = getSharedPreferences("notificationPrefs", Context.MODE_PRIVATE);
        Set<String> notificationSet = mPrefs.getStringSet("notificationSet", new HashSet<String>());
        notifyList = new ArrayList<String>(notificationSet);

        // Create the Status reader
        statusReader = new StatusReader(notifyList);
        statusReader.addListener(this);

        // Create the Handler which will request a new status read once per minute

        statusHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                // Do task here
                if (msg.what == READ_STATUS) {
                    statusReader.readStatus();
                    if (!statusHandler.hasMessages(READ_STATUS)) statusHandler.sendEmptyMessageDelayed(READ_STATUS, 60000);
                }
            }
        };
        statusHandler.sendEmptyMessageDelayed(READ_STATUS, 0);

        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save list of notification servers to shared preferences
        mPrefs = getSharedPreferences("notificationPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        Set<String> notificationSet = new HashSet<String>(notifyList);
        editor.putStringSet("notificationSet", notificationSet);
        editor.commit();
    }

    @Override
    public void onStatusRead(ArrayList<ServerStatus> newServerList) {
        // Check for possible notifications needed
        for (ServerStatus newStatus : newServerList) {
            if (notifyList.contains(newStatus.getName()) && (serverList!=null)) {
                for (ServerStatus oldStatus : serverList) {
                    if ((oldStatus.getName().equals(newStatus.getName())) && !(oldStatus.getStatus().equals(newStatus.getStatus()))) {
                        // Status has changed and server is on the notify list, fire off a notification
                        sendNotification(newStatus);
                    }
                }
            }
        }
        this.serverList = newServerList;
    }

    private void sendNotification(ServerStatus newStatus) {
        Date date = new Date();
        String dateTime = DateFormat.getTimeInstance().format(date)+" on "+DateFormat.getDateInstance().format(date);
        String title = newStatus.getName()+" status";
        String text = ((newStatus.getRegion()==ServerStatus.SERVICES)?"Service":"Server")+" "+newStatus.getStatus().toLowerCase()+" at "+dateTime;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(text);
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(notificationId++, mBuilder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ServerStatusFragment newFragment = null;
            switch (position) {
                case SERVICE_PAGE:
                    newFragment = ServerStatusFragment.newInstance(notifyList, ServerStatus.SERVICES);
                    statusHandler.sendEmptyMessageDelayed(READ_STATUS, 0);
                    break;
                case EU_SERVER_PAGE:
                    newFragment = ServerStatusFragment.newInstance(notifyList, ServerStatus.EU_SERVERS);
                    statusHandler.sendEmptyMessageDelayed(READ_STATUS, 0);
                    break;
                case NA_SERVER_PAGE:
                    newFragment = ServerStatusFragment.newInstance(notifyList, ServerStatus.NA_SERVERS);
                    statusHandler.sendEmptyMessageDelayed(READ_STATUS, 0);
                    break;
            }
            statusReader.addListener(newFragment);
            return newFragment;
            // getItem is called to instantiate the fragment for the given page.
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case SERVICE_PAGE:
                    return getString(R.string.title_section1);
                case EU_SERVER_PAGE:
                    return getString(R.string.title_section2);
                case NA_SERVER_PAGE:
                    return getString(R.string.title_section3);
            }
            return null;
        }

    }
}
