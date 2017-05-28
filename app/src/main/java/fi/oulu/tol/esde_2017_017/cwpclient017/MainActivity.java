package fi.oulu.tol.esde_2017_017.cwpclient017;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWPControl;
import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWPMessaging;

public class MainActivity extends AppCompatActivity implements CWPProvider {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private boolean cwpBound;
    private CWPService cwpService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FIXME?
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Intent serviceIntent = new Intent(this, CWPService.class);
        ComponentName startServiceRespoonse = startService(serviceIntent);
        //if (startServiceRespoonse.) { //FIXME check if service started

        //}


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, CWPService.class);
        bindService(intent, cwpConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cwpBound) {
            cwpService.stopUsing();
            unbindService(cwpConnection);
            cwpBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cwpService = null;  // garbage collector will handle the rest.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    public CWPMessaging getMessaging() {
        return (cwpService != null) ? cwpService.getMessaging() : null;
    }

    public CWPControl getControl() {
        return (cwpService != null) ? cwpService.getControl() : null;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private TappingFragment tapper = null;
        private ControlFragment control = null;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (tapper == null) {
                    tapper = new TappingFragment();
                }
                return tapper;
            }
            if (position == 1) {
                if (control == null) {
                    control = new ControlFragment();
                }
                return control;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return "Tapping";
            if (position == 1)
                return "Connection Settings";
            return null;
        }
    }

    private ServiceConnection cwpConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CWPService.CWPBinder binder = (CWPService.CWPBinder) service;
            cwpService = binder.getService();
            cwpService.startUsing();
            cwpBound = true;

            Fragment tFragment = mSectionsPagerAdapter.getItem(0);
            if (tFragment != null) {
                ((TappingFragment)tFragment).setMessaging(cwpService.getMessaging());
            }
            Fragment cFragment = mSectionsPagerAdapter.getItem(1);
            if (cFragment != null) {
                ((ControlFragment)cFragment).setControl(cwpService.getControl());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            cwpBound = false;
        }
    };
}
