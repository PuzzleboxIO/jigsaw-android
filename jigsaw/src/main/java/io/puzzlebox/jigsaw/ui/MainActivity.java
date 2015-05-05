/**
 * Puzzlebox Jigsaw
 * Copyright 2015 Puzzlebox Productions, LLC
 * License: GNU Affero General Public License Version 3
 */

package io.puzzlebox.jigsaw.ui;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import io.puzzlebox.jigsaw.R;
import io.puzzlebox.jigsaw.data.SessionSingleton;

public class MainActivity extends AppCompatActivity implements
		  WelcomeFragment.OnFragmentInteractionListener,
		  SessionFragment.OnFragmentInteractionListener,
		  EEGFragment.OnFragmentInteractionListener
//		  BloomFragment.OnFragmentInteractionListener
{

	private final static String TAG = MainActivity.class.getSimpleName();

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mTitle;
	private CharSequence mDrawerTitle;
	NavigationDrawerAdapter adapter;

	List<DrawerItem> dataList;


	// ################################################################

	public void onFragmentInteraction(Uri uri) {
//		Log.d(TAG, "onFragmentInteraction()");
	}


	// ################################################################

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		dataList = new ArrayList<>();
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.navigation_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				  GravityCompat.START);


		// Add Drawer Item to dataList
//		dataList.add(new DrawerItem(getString(R.string.title_fragment_welcome), R.mipmap.ic_puzzlebox));
//		dataList.add(new DrawerItem(getString(R.string.title_fragment_session), R.mipmap.ic_session));
//		dataList.add(new DrawerItem(getString(R.string.title_fragment_eeg), R.mipmap.ic_eeg));
////		dataList.add(new DrawerItem(getString(R.string.title_fragment_bloom), R.mipmap.ic_bloom));

		dataList = getDrawerDataList();

		adapter = new NavigationDrawerAdapter(this, R.layout.navigation_drawer_item,
				  dataList);

		mDrawerList.setAdapter(adapter);


		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
			getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
		}

		Toolbar mToolbar = new Toolbar(this);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				  mToolbar, R.string.drawer_open,
				  R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
				// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
				// onPrepareOptionsMenu()
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			SelectItem(0);
		}

		SessionSingleton.getInstance().resetSession();


		onCreateCustom();

	}


	// ################################################################

	protected void onCreateCustom() {
		// For use with custom applications
	}

	// ################################################################

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		super.onOptionsItemSelected(item);

		return mDrawerToggle.onOptionsItemSelected(item);

	}


	// ################################################################

	protected List<DrawerItem> getDrawerDataList() {
		List<DrawerItem> dataList = new ArrayList<>();

		dataList.add(new DrawerItem(getString(io.puzzlebox.jigsaw.R.string.title_fragment_welcome), io.puzzlebox.jigsaw.R.mipmap.ic_puzzlebox));
		dataList.add(new DrawerItem(getString(io.puzzlebox.jigsaw.R.string.title_fragment_session), io.puzzlebox.jigsaw.R.mipmap.ic_session));
		dataList.add(new DrawerItem(getString(io.puzzlebox.jigsaw.R.string.title_fragment_eeg), io.puzzlebox.jigsaw.R.mipmap.ic_eeg));
//		dataList.add(new DrawerItem(getString(io.puzzlebox.jigsaw.R.string.title_fragment_bloom), io.puzzlebox.jigsaw.R.mipmap.ic_bloom));

		return dataList;
	}


	// ################################################################

	public void SelectItem(int position) {

		android.app.Fragment fragment = null;
		Bundle args = new Bundle();
		String backStackName = "";
		switch (position) {
			case 0:
				backStackName = "welcome";
				try{
					fragment = getFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (fragment == null)
					fragment = new WelcomeFragment();
				break;
			case 1:
				backStackName = "session";
				try{
					fragment = getFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (fragment == null)
					fragment = new SessionFragment();
				break;
			case 2:
				backStackName = "eeg";
				try{
					fragment = getFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (fragment == null)
					fragment = new EEGFragment();

				break;
//			case 3:
//				backStackName = "bloom";
//				try{
//					fragment = getFragmentManager().findFragmentByTag(backStackName);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				if (fragment == null)
//					fragment = new BloomFragment();
//
//				break;
			default:
				break;
		}

		if (fragment != null)
			fragment.setArguments(args);
		android.app.FragmentManager frgManager = getFragmentManager();
		frgManager.beginTransaction().replace(R.id.container, fragment)
				  .addToBackStack(backStackName)
				  .commit();

		mDrawerList.setItemChecked(position, true);
		setTitle(dataList.get(position).getItemName());
		mDrawerLayout.closeDrawer(mDrawerList);

	}


	// ################################################################

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		if (getSupportActionBar() != null)
			getSupportActionBar().setTitle(mTitle);
	}


	// ################################################################

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();

	}


	// ################################################################

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}


	// ################################################################

	private class DrawerItemClickListener implements
			  ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
		                        long id) {
			SelectItem(position);

		}
	}


	// ################################################################

	@Override
	public void onPause() {

		Log.v(TAG, "onPause()");

		super.onPause();


	} // onPause


	// ################################################################

	@Override
	public void onDestroy() {

		Log.v(TAG, "onDestroy()");

		super.onDestroy();

		SessionSingleton.getInstance().removeTemporarySessionFile();


	} // onDestroy

}
