package io.puzzlebox.jigsaw;

import android.app.Activity;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity
		  implements WelcomeFragment.OnFragmentInteractionListener
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

	public void onFragmentInteraction(Uri uri) {
		Log.e(TAG, "onFragmentInteraction()");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		dataList = new ArrayList<DrawerItem>();
		dataList = new ArrayList<>();
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.navigation_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				  GravityCompat.START);



		// Add Drawer Item to dataList
		dataList.add(new DrawerItem(getString(R.string.title_fragment_welcome), R.drawable.ic_welcome));
//		dataList.add(new DrawerItem(getString(R.string.title_fragment_remote_control), R.drawable.ic_remote_control));

		adapter = new NavigationDrawerAdapter(this, R.layout.navigation_drawer_item,
				  dataList);

		mDrawerList.setAdapter(adapter);


		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				  R.drawable.ic_drawer, R.string.drawer_open,
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



	}

//	@Override
//	public void onNavigationDrawerItemSelected(int position) {
//		// update the main content by replacing fragments
//		FragmentManager fragmentManager = getSupportFragmentManager();
//		fragmentManager.beginTransaction()
//				  .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
//				  .commit();
//	}

//	public void onSectionAttached(int number) {
//		switch (number) {
//			case 1:
//				mTitle = getString(R.string.title_section1);
//				break;
//			case 2:
//				mTitle = getString(R.string.title_section2);
//				break;
//			case 3:
//				mTitle = getString(R.string.title_section3);
//				break;
//		}
//	}

//	public void restoreActionBar() {
//		ActionBar actionBar = getSupportActionBar();
//		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//		actionBar.setDisplayShowTitleEnabled(true);
//		actionBar.setTitle(mTitle);
//	}


//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		if (!mNavigationDrawerFragment.isDrawerOpen()) {
//			// Only show items in the action bar relevant to this screen
//			// if the drawer is not showing. Otherwise, let the drawer
//			// decide what to show in the action bar.
//			getMenuInflater().inflate(R.menu.main, menu);
//			restoreActionBar();
//			return true;
//		}
//		return super.onCreateOptionsMenu(menu);
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//
//		//noinspection SimplifiableIfStatement
//		if (id == R.id.action_settings) {
//			return true;
//		}
//
//		return super.onOptionsItemSelected(item);

		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return false;

	}


	public void SelectItem(int position) {

		android.app.Fragment fragment = null;
		Bundle args = new Bundle();
		switch (position) {
			case 0:
				fragment = new WelcomeFragment();
//				fragment = new RemoteControlFragment();
				break;
//			case 1:
//				fragment = new RemoteControlFragment();
//				break;
			default:
				break;
		}

		fragment.setArguments(args);
		android.app.FragmentManager frgManager = getFragmentManager();
		frgManager.beginTransaction().replace(R.id.container, fragment)
				  .commit();

		mDrawerList.setItemChecked(position, true);
		setTitle(dataList.get(position).getItemName());
		mDrawerLayout.closeDrawer(mDrawerList);

	}


	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
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


//	/**
//	 * A placeholder fragment containing a simple view.
//	 */
//	public static class PlaceholderFragment extends Fragment {
//		/**
//		 * The fragment argument representing the section number for this
//		 * fragment.
//		 */
//		private static final String ARG_SECTION_NUMBER = "section_number";
//
//		/**
//		 * Returns a new instance of this fragment for the given section
//		 * number.
//		 */
//		public static PlaceholderFragment newInstance(int sectionNumber) {
//			PlaceholderFragment fragment = new PlaceholderFragment();
//			Bundle args = new Bundle();
//			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//			fragment.setArguments(args);
//			return fragment;
//		}
//
//		public PlaceholderFragment() {
//		}
//
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
//		                         Bundle savedInstanceState) {
//			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//			return rootView;
//		}
//
//		@Override
//		public void onAttach(Activity activity) {
//			super.onAttach(activity);
//			((MainActivity) activity).onSectionAttached(
//					  getArguments().getInt(ARG_SECTION_NUMBER));
//		}
//	}

}
