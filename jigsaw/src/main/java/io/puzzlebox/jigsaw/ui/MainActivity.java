/*
 * Puzzlebox Jigsaw
 * Copyright (2015-2020) Puzzlebox Productions, LLC
 * License: GNU Affero General Public License Version 3
 */

package io.puzzlebox.jigsaw.ui;

import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import io.puzzlebox.jigsaw.R;
import io.puzzlebox.jigsaw.data.ConfigurationSingleton;
import io.puzzlebox.jigsaw.data.SessionSingleton;

public class MainActivity extends AppCompatActivity implements
		  WelcomeFragment.OnFragmentInteractionListener,
		  SessionFragment.OnFragmentInteractionListener,
		  EEGFragment.OnFragmentInteractionListener,
		  TilesFragment.OnFragmentInteractionListener,
		  SupportFragment.OnFragmentInteractionListener,
		  DialogInputNeuroSkyMindWaveFragment.OnFragmentInteractionListener,
		  DialogInputJoystickFragment.OnFragmentInteractionListener,
		  DialogOutputPuzzleboxGimmickFragment.OnFragmentInteractionListener,
		  DialogOutputAudioIRFragment.OnFragmentInteractionListener,
		  DialogOutputSessionFragment.OnFragmentInteractionListener {

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


	public void onFragmentInteraction(Uri uri) {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try {
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size); // TODO API 13
			ConfigurationSingleton.getInstance().displayHeight = size.y;
			ConfigurationSingleton.getInstance().displayWidth = size.x;

			Log.d(TAG, "ConfigurationSingleton.getInstance().displayHeight: " + ConfigurationSingleton.getInstance().displayHeight);
			Log.d(TAG, "ConfigurationSingleton.getInstance().displayWidth: " + ConfigurationSingleton.getInstance().displayWidth);

			// Calculate ActionBar height
			TypedValue tv = new TypedValue();
			if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
				ConfigurationSingleton.getInstance().actionBarHeight =
						  TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception calculating screen dimensions for navigation WebView: " + e);
		}

		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = findViewById(R.id.drawer_layout);
		mDrawerList = findViewById(R.id.navigation_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				  GravityCompat.START);

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

	protected void onCreateCustom() {
		// For use with custom applications
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		return mDrawerToggle.onOptionsItemSelected(item);
	}

	protected List<DrawerItem> getDrawerDataList() {
		List<DrawerItem> dataList = new ArrayList<>();

		dataList.add(new DrawerItem(getString(io.puzzlebox.jigsaw.R.string.title_fragment_welcome), io.puzzlebox.jigsaw.R.mipmap.ic_puzzlebox));
		dataList.add(new DrawerItem(getString(io.puzzlebox.jigsaw.R.string.title_fragment_session), io.puzzlebox.jigsaw.R.mipmap.ic_session_color));
		dataList.add(new DrawerItem(getString(io.puzzlebox.jigsaw.R.string.title_fragment_eeg), io.puzzlebox.jigsaw.R.mipmap.ic_eeg_color));
		dataList.add(new DrawerItem(getString(io.puzzlebox.jigsaw.R.string.title_fragment_support), io.puzzlebox.jigsaw.R.mipmap.ic_support));

		return dataList;
	}

	public void SelectItem(int position) {

		android.support.v4.app.Fragment fragment = null;
		Bundle args = new Bundle();
		String backStackName = "";
		switch (position) {
			case 0:
				backStackName = getResources().getString(R.string.title_fragment_welcome);
				try{
					fragment = getSupportFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (fragment == null)
					fragment = new WelcomeFragment();
				break;
			case 1:
				backStackName = getResources().getString(R.string.title_fragment_session);
				try{
					fragment = getSupportFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (fragment == null)
					fragment = new SessionFragment();
				break;
			case 2:
				backStackName = getResources().getString(R.string.title_fragment_eeg);
				try{
					fragment = getSupportFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (fragment == null)
					fragment = new EEGFragment();
				break;
			case 3:
				backStackName = getResources().getString(R.string.title_fragment_support);
				try{
                    fragment = getSupportFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (fragment == null)
					fragment = new SupportFragment();
				break;

			default:
				break;
		}

		if (fragment != null)
			fragment.setArguments(args);
		android.support.v4.app.FragmentManager frgManager = getSupportFragmentManager();
		frgManager.beginTransaction().replace(R.id.container, fragment)
				  .addToBackStack(backStackName)
				  .commit();

		mDrawerList.setItemChecked(position, true);
		setTitle(dataList.get(position).getItemName());
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		if (getSupportActionBar() != null)
			getSupportActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private class DrawerItemClickListener implements
			  ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
		                        long id) {
			SelectItem(position);
		}
	}

	@Override
	public void onPause() {
		Log.v(TAG, "onPause()");
		super.onPause();
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy()");
		super.onDestroy();
		SessionSingleton.getInstance().removeTemporarySessionFile();
	}
}
