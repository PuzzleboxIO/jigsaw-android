package io.puzzlebox.jigsaw.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import io.puzzlebox.jigsaw.R;


public class MainTabSliderActivity extends AppCompatActivity implements
        WelcomeFragment.OnFragmentInteractionListener,
        SessionFragment.OnFragmentInteractionListener,
        EEGFragment.OnFragmentInteractionListener
{
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    // ################################################################

    public void onFragmentInteraction(Uri uri) {
    }

    // ################################################################

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_slider);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

//        @Override
//        public Fragment getItem(int position) {
//            // getItem is called to instantiate the fragment for the given page.
//            // Return a PlaceholderFragment (defined as a static inner class below).
//            return PlaceholderFragment.newInstance(position + 1);
//        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).


//            android.app.Fragment fragment = null;
            android.support.v4.app.Fragment fragment = null;
            Bundle args = new Bundle();
            String backStackName = "";
            switch (position) {
                case 0:
                    backStackName = "welcome";
                    try{
//                        fragment = getFragmentManager().findFragmentByTag(backStackName);
                        fragment = getSupportFragmentManager().findFragmentByTag(backStackName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fragment == null)
                        fragment = new WelcomeFragment();
                    break;
                case 1:
                    backStackName = "session";
                    try{
//                        fragment = getFragmentManager().findFragmentByTag(backStackName);
                        fragment = getSupportFragmentManager().findFragmentByTag(backStackName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fragment == null)
                        fragment = new SessionFragment();
                    break;
                case 2:
                    backStackName = "eeg";
                    try{
//                        fragment = getFragmentManager().findFragmentByTag(backStackName);
                        fragment = getSupportFragmentManager().findFragmentByTag(backStackName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fragment == null)
                        fragment = new EEGFragment();

                    break;
                default:
                    break;
            }
            return fragment;
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
                case 0:
                    return getString(R.string.title_fragment_welcome).toUpperCase(l);
                case 1:
                    return getString(R.string.title_fragment_session).toUpperCase(l);
                case 2:
                    return getString(R.string.title_fragment_eeg).toUpperCase(l);
                case 3:
                    return getString(R.string.title_fragment_bloom).toUpperCase(l);
            }
            return null;
        }
    }

}
