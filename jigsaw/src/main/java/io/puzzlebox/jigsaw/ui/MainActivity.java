/*
 * Puzzlebox Jigsaw
 * Copyright (2015-2020) Puzzlebox Productions, LLC
 * License: GNU Affero General Public License Version 3
 */

package io.puzzlebox.jigsaw.ui;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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

	protected static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
	protected static final int PERMISSION_REQUEST_BLUETOOTH = 2;

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mTitle;
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

		mTitle = getTitle();
		mDrawerLayout = findViewById(R.id.drawer_layout);
		mDrawerList = findViewById(R.id.navigation_drawer);

		// Android 15+ (API 35+) forces edge-to-edge: AppCompat's SubDecor places both the
		// fragment container and the navigation drawer at y=0 (behind the status bar and
		// ActionBar). Use post() to measure actual on-screen positions after layout and
		// apply exactly the padding needed to push each view below the ActionBar bottom.
		if (Build.VERSION.SDK_INT >= 35) {
			final View container = findViewById(R.id.container);
			final ListView drawerList = mDrawerList;
			if (container != null) {
				container.post(() -> {
					View actionBarCont = getWindow().getDecorView()
							.findViewById(androidx.appcompat.R.id.action_bar_container);
					if (actionBarCont == null) return;
					int[] abLoc = new int[2];
					actionBarCont.getLocationOnScreen(abLoc);
					int abBottom = abLoc[1] + actionBarCont.getHeight();

					int[] cLoc = new int[2];
					container.getLocationOnScreen(cLoc);
					int topPad = abBottom - cLoc[1];
					if (topPad > 0) container.setPadding(0, topPad, 0, 0);

					if (drawerList != null) {
						int[] dLoc = new int[2];
						drawerList.getLocationOnScreen(dLoc);
						int drawPad = abBottom - dLoc[1];
						if (drawPad > 0) drawerList.setPadding(0, drawPad, 0, 0);
					}
				});
			}
		}

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
				if (getSupportActionBar() != null) getSupportActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
				// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				invalidateOptionsMenu(); // creates call to
				// onPrepareOptionsMenu()
			}
		};

		mDrawerLayout.addDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			SelectItem(0);
		}

		SessionSingleton.getInstance().resetSession();

		onCreateCustom();
	}

	protected void onCreateCustom() {
		// Request Bluetooth and location permissions required for BLE scanning.
		// Subclasses that override this method without calling super() are responsible
		// for their own permission requests (e.g. orbit-android's MainActivity).
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			List<String> needed = new ArrayList<>();
			if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
				needed.add(android.Manifest.permission.BLUETOOTH_SCAN);
			if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
				needed.add(android.Manifest.permission.BLUETOOTH_CONNECT);
			if (!needed.isEmpty())
				requestPermissions(needed.toArray(new String[0]), PERMISSION_REQUEST_BLUETOOTH);
		} else {
			if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.permission_bluetooth_dialog_title));
				builder.setMessage(getString(R.string.permission_bluetooth_dialog_message));
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(dialog -> requestPermissions(
						new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
						PERMISSION_REQUEST_COARSE_LOCATION));
				builder.show();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PERMISSION_REQUEST_BLUETOOTH) {
			for (int result : grantResults) {
				if (result != PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(this, getString(R.string.permission_bluetooth_required), Toast.LENGTH_LONG).show();
					return;
				}
			}
		} else if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
			if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this, getString(R.string.permission_location_required), Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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

		androidx.fragment.app.Fragment fragment = null;
		Bundle args = new Bundle();
		String backStackName = "";
		switch (position) {
			case 0:
				backStackName = getResources().getString(R.string.title_fragment_welcome);
				try{
					fragment = getSupportFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					Log.e(TAG, "Exception", e);
				}
				if (fragment == null)
					fragment = new WelcomeFragment();
				break;
			case 1:
				backStackName = getResources().getString(R.string.title_fragment_session);
				try{
					fragment = getSupportFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					Log.e(TAG, "Exception", e);
				}
				if (fragment == null)
					fragment = new SessionFragment();
				break;
			case 2:
				backStackName = getResources().getString(R.string.title_fragment_eeg);
				try{
					fragment = getSupportFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					Log.e(TAG, "Exception", e);
				}
				if (fragment == null)
					fragment = new EEGFragment();
				break;
			case 3:
				backStackName = getResources().getString(R.string.title_fragment_support);
				try{
                    fragment = getSupportFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					Log.e(TAG, "Exception", e);
				}
				if (fragment == null)
					fragment = new SupportFragment();
				break;

			default:
				break;
		}

		if (fragment == null) return;
		fragment.setArguments(args);
		androidx.fragment.app.FragmentManager frgManager = getSupportFragmentManager();
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
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
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
