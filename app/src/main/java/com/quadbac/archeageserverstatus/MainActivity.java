package com.quadbac.archeageserverstatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.quadbac.archeageserverstatus.model.ServerStatus;


public class MainActivity extends Activity implements ActionBar.TabListener, Switch.OnCheckedChangeListener {

    private SharedPreferences mPrefs;
    private StatusReadReceiver statusReadReceiver;
    private final static int SERVICE_PAGE = 0;
    private final static int EU_SERVER_PAGE = 1;
    private final static int NA_SERVER_PAGE = 2;
    private ArrayList<OnStatusReadListener> listeners = new ArrayList<OnStatusReadListener>();
    private ArrayList<ServerStatus> serverList = new ArrayList<ServerStatus>();
    private ArrayList<String> notifyList = new ArrayList<String>();

    private ProgressDialog statusReadDialog;
    private Switch monitorSwitch;

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

        // Create the Handler which will request a new status read once per minute
//        statusHandler = new StatusHandler(this, STATUS_DELAY);

        setContentView(R.layout.activity_main);

        monitorSwitch = (Switch) findViewById(R.id.monitorSwitch);
        monitorSwitch.setOnCheckedChangeListener(this);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ad

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
        startRepeatingReaderService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check whether the RepeatingReaderService is running & set monitorSwitch accordingly
        monitorSwitch.setChecked(RepeatingReaderService.isRunning);

        // Register to receive broadcasts from the FeedReaderService
        IntentFilter feedReaderFilter = new IntentFilter(FeedReaderService.BROADCAST_READING_ACTION);
        feedReaderFilter.addAction(FeedReaderService.BROADCAST_RESULTS_ACTION);
        feedReaderFilter.addAction(RepeatingReaderService.BROADCAST_READER_RUNNING);
        feedReaderFilter.addAction(RepeatingReaderService.BROADCAST_READER_STOPPED);
        statusReadReceiver = new StatusReadReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                statusReadReceiver,
                feedReaderFilter);
        // Fire off a single server status read to refresh the server lists
        startFeedReaderService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNotifyList(notifyList);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(statusReadReceiver);
    }

    public void onClickRetryButton(View v){
        startFeedReaderService();
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
//       if (id == R.id.action_settings) {
//            return true;
//        }
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

    public void startFeedReaderService(){
        Intent readStatusIntent = new Intent(this, FeedReaderService.class);
        readStatusIntent.putParcelableArrayListExtra(FeedReaderService.CURRENT_SERVER_LIST, serverList);
        startService(readStatusIntent);
    }

    public void startRepeatingReaderService(){
        Intent repeatingReadIntent = new Intent(this, RepeatingReaderService.class);
        repeatingReadIntent.putParcelableArrayListExtra(FeedReaderService.CURRENT_SERVER_LIST, serverList);
        startService(repeatingReadIntent);
    }

    public void stopRepeatingReaderService(){
        Intent repeatingReadIntent = new Intent(this, RepeatingReaderService.class);
        stopService(repeatingReadIntent);
    }

    public void showStatusReadDialog(){
        statusReadDialog = ProgressDialog.show(this, "Please Wait...", "Downloading Server Status ...", true, true);

    }

    public void addListener (OnStatusReadListener listener) {
        listeners.add(listener);
    }

    public void removeListener (OnStatusReadListener listener) {
        listeners.remove(listener);
    }

    public void removeAllListeners () {
        listeners.removeAll(listeners);
    }

    public void saveNotifyList(ArrayList<String> notifyList) {
        // Save list of notification servers to shared preferences
        mPrefs = getSharedPreferences("notificationPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        Set<String> notificationSet = new HashSet<String>(notifyList);
        editor.putStringSet("notificationSet", notificationSet);
        editor.commit();

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isEnabled) {
        if (isEnabled) {
            startRepeatingReaderService();
        } else {
            stopRepeatingReaderService();
        }
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
                    newFragment = ServerStatusFragment.newInstance(serverList, notifyList, ServerStatus.SERVICES);
                    MainActivity.this.addListener(newFragment);
                    break;
                case EU_SERVER_PAGE:
                    newFragment = ServerStatusFragment.newInstance(serverList, notifyList, ServerStatus.EU_SERVERS);
                    MainActivity.this.addListener(newFragment);
                    break;
                case NA_SERVER_PAGE:
                    newFragment = ServerStatusFragment.newInstance(serverList, notifyList, ServerStatus.NA_SERVERS);
                    MainActivity.this.addListener(newFragment);
                    break;
            }
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

    // Broadcast receiver for receiving status updates from the FeedReaderService
    private class StatusReadReceiver extends BroadcastReceiver
    {
        // Prevents instantiation
        private StatusReadReceiver() {
        }
        // Called when the StatusReadReceiver gets an Intent it's registered to receive
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(FeedReaderService.BROADCAST_READING_ACTION)) {
                    showStatusReadDialog();
            }

            if (intent.getAction().equals(FeedReaderService.BROADCAST_RESULTS_ACTION)) {
                if (statusReadDialog != null) statusReadDialog.dismiss();
                serverList = intent.getParcelableArrayListExtra(FeedReaderService.NEW_SERVER_LIST);
                for (OnStatusReadListener listener : listeners) {
                    listener.onStatusRead(serverList);
                }
            }

            if (intent.getAction().equals(RepeatingReaderService.BROADCAST_READER_RUNNING)) {
                monitorSwitch.setChecked(true);
            }

            if (intent.getAction().equals(RepeatingReaderService.BROADCAST_READER_STOPPED)) {
                monitorSwitch.setChecked(false);
            }
        }
    }
}
