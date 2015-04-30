package io.puzzlebox.jigsaw;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.puzzlebox.jigsaw.data.CreateSessionFileInGoogleDrive;
import io.puzzlebox.jigsaw.data.SessionSingleton;
import io.puzzlebox.jigsaw.ui.DrawerItem;
import io.puzzlebox.jigsaw.ui.NavigationDrawerAdapter;

public class MainActivity extends ActionBarActivity implements
		  WelcomeFragment.OnFragmentInteractionListener,
		  SessionFragment.OnFragmentInteractionListener,
		  EEGFragment.OnFragmentInteractionListener
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

	static Menu menu;


	// ################################################################

	public void onFragmentInteraction(Uri uri) {
		Log.e(TAG, "onFragmentInteraction()");
	}


	// ################################################################

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dataList = new ArrayList<>();
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.navigation_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				  GravityCompat.START);


		// Add Drawer Item to dataList
		dataList.add(new DrawerItem(getString(R.string.title_fragment_welcome), R.drawable.ic_welcome));
		dataList.add(new DrawerItem(getString(R.string.title_fragment_session), R.drawable.ic_welcome));
		dataList.add(new DrawerItem(getString(R.string.title_fragment_eeg), R.drawable.ic_welcome));

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


	// ################################################################


//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}


//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.action_bar_share_menu, menu);
//
//		// Locate MenuItem
//		MenuItem item = menu.findItem(R.id.menu_item_share);
//
//		// Fetch and store ShareActionProvider
//		ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
//
//
//
////		mShareButtonClickListener
//
//
//
////		Intent googleDriveIntent = new Intent(this, CreateSessionFileInGoogleDrive.class);
////		mShareActionProvider.setShareIntent(googleDriveIntent);
//
//
//
////		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//////		sharingIntent.setType("text/comma_separated_values/csv");
//////		sharingIntent.setType("text/comma_separated_values");
////		sharingIntent.setType("text/csv");
//////		sharingIntent.setType(getmimefromextension("csv"));
////
////
//////		sharingIntent.setData( SessionSingleton.getInstance().getExportDataCSV() );
//////		sharingIntent.putExtra(Intent., SessionSingleton.getInstance().getExportDataCSV());
////		sharingIntent.putExtra(Intent.EXTRA_STREAM, SessionSingleton.getInstance().getExportDataCSV());
//////		startActivity(Intent.createChooser(sharingIntent, getResources().getText(R.string.send_to)));
////		mShareActionProvider.setShareIntent(sharingIntent);
//
//
//
//
//
//
//
////		String to = textTo.getText().toString();
////		String subject = textSubject.getText().toString();
////		String message = textMessage.getText().toString();
//
//		Intent i = new Intent(Intent.ACTION_SEND);
////		i.setType("plain/text");
////		i.setType("plain/csv");
////		i.setType("text/comma-separated-values");
//		i.setType("application/csv");
//		File data = null;
//		try {
////			Date dateVal = new Date();
////			String filename = dateVal.toString();
//
//
//			String filename = SessionSingleton.getInstance().getTimestampPS4();
//
//////			data = File.createTempFile("Report", ".csv");
////			data = File.createTempFile(filename, ".csv");
//
//
////			String FILE = Environment.getExternalStorageDirectory() + File.separator
////					  + "Foldername";
//			String FILE = Environment.getExternalStorageDirectory().toString();
//
//
////			Log.e(TAG, "data: " + data.toString());
//
//
//////			FileWriter out = (FileWriter) GenerateCsv.generateCsvFile(
//////					  data, "Name,Data1");
////
////			FileWriter out = (FileWriter) GenerateCsv.generateCsvFile(
////					  data, SessionSingleton.getInstance().getExportDataCSV());
//
//
////			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(data));
//
//
//
//			String temp_path = FILE + File.separator + filename + ".csv";
//
//
//			File F = new File(temp_path);
//
//			FileWriter out = (FileWriter) GenerateCsv.generateCsvFile(
//					  F, SessionSingleton.getInstance().getExportDataCSV());
//
//
//
//			Uri U = Uri.fromFile(F);
////			Uri U = Uri.fromFile(temp_path);
//			i.putExtra(Intent.EXTRA_STREAM, U);
//
//
//
//
//
//////			i.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
////			i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
////			i.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_message));
//////			startActivity(Intent.createChooser(i, "E-mail"));
//
//
////			startActivity(Intent.createChooser(i, "Share Session"));
//
//			mShareActionProvider.setShareIntent(Intent.createChooser(i, "Share Session"));
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
////
////
////
//////
//////		String filepath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
//////		String filename = SessionSingleton.getInstance().getTimestampPS4();
////////		File outputDir = getCacheDir(); // context being the Activity pointer
//////
//////
//////		try {
//////////			File outputFile = File.createTempFile("jigsaw-session", "csv", outputDir);
////////			File outputFile = File.createTempFile(filename, "csv", outputDir);
//////
////////			SessionSingleton.getInstance().exportDataToCSV(outputFile.toString());
////////			SessionSingleton.getInstance().exportDataToCSV(outputFile.toString(), filename);
//////			SessionSingleton.getInstance().exportDataToCSV(filepath, filename);
//////
////////			Uri uriFile = FileProvider.getUriForFile(this, "io.puzzlebox", outputFile);
////////			Uri uriFile = FileProvider.getUriForFile(this, "io.puzzlebox.jigsaw", new File(filepath + "/" + filename + ".csv"));
//////			Uri uriFile = FileProvider.getUriForFile(this, "io.puzzlebox.jigsaw", new File(filepath + "/" + "jigsaw-session" + ".csv"));
//////			Uri uriFile = new Uri().fromParts()
//////
//////			Intent intent = new Intent(Intent.ACTION_SEND);
//////
//////			intent.setDataAndType(uriFile, "plain/csv");
//////
//////			intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//////			intent.createChooser(intent, "Choose");
////////			startActivity(intent);
//////
//////			mShareActionProvider.setShareIntent(intent);
//////
//////		} catch (Exception e) {
//////			e.printStackTrace();
//////		}
//////
//////
////
////
////
////
////
////
//////		String filepath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
//////		String filename = SessionSingleton.getInstance().getTimestampPS4();
////////		File outputDir = getCacheDir(); // context being the Activity pointer
//////
//////
//////		try {
//////////			File outputFile = File.createTempFile("jigsaw-session", "csv", outputDir);
////////			File outputFile = File.createTempFile(filename, "csv", outputDir);
//////
////////			SessionSingleton.getInstance().exportDataToCSV(outputFile.toString());
////////			SessionSingleton.getInstance().exportDataToCSV(outputFile.toString(), filename);
//////			SessionSingleton.getInstance().exportDataToCSV(filepath, filename);
//////
////////			Uri uriFile = FileProvider.getUriForFile(this, "io.puzzlebox", outputFile);
//////			Uri uriFile = FileProvider.getUriForFile(this, "io.puzzlebox.jigsaw", new File(filepath + "/" + filename));
//////
//////			Intent intent = new Intent(Intent.ACTION_SEND);
//////
//////			intent.setDataAndType(uriFile, "plain/csv");
//////
//////			intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//////			intent.createChooser(intent, "Choose");
//////			startActivity(intent);
//////
////////			mShareActionProvider.setShareIntent(intent);
//////
//////		} catch (Exception e) {
//////			e.printStackTrace();
//////		}
////
////
//////		if (outputFile != null) {
////////		Uri uriFile = FileProvider.getUriForFile(this, "com.example.ria.masterdetailnoten", backupFile);
//////			Uri uriFile = FileProvider.getUriForFile(this, "io.puzzlebox", outputFile);
//////			Intent intent = new Intent(Intent.ACTION_SEND);
////////		intent.setDataAndType(uriFile, "application/vnd.ms-excel");
////////		intent.setDataAndType(uriFile, "text/csv");
//////			intent.setDataAndType(uriFile, "text/csv");
////////		sharingIntent.setType("text/csv");
//////			intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//////			intent.createChooser(intent, "Choose");
//////			startActivity(intent);
//////		}
////
////
//////		PackageManager packManager = getPackageManager();
//////		List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(googleDriveIntent, PackageManager.MATCH_DEFAULT_ONLY);
//////
//////		boolean resolved = false;
//////		for (ResolveInfo resolveInfo : resolvedInfoList) {
//////			if (resolveInfo.activityInfo.packageName.startsWith("com.google.drive")) {
//////				googleDriveIntent.setClassName(
//////						  resolveInfo.activityInfo.packageName,
//////						  resolveInfo.activityInfo.name);
//////				resolved = true;
//////				break;
//////			} else {
//////				Log.e(TAG, resolveInfo.activityInfo.packageName);
//////			}
//////		}
//////		if (resolved) {
////////			startActivity(googleDriveIntent);
//////			mShareActionProvider.setShareIntent(googleDriveIntent);
//////		} else {
//////			Log.d(TAG, "Google Drive app not found");
////////			Toast.makeText(this, "Google Drive app not found, please install to store data", Toast.LENGTH_LONG).show();
//////		}
////
////
//////		googleDriveIntent.setT
//////		mShareActionProvider.setShareIntent(googleDriveIntent);
////
////
//
//
//
//		MenuItem.OnMenuItemClickListener mShareButtonClickListener = new MenuItem.OnMenuItemClickListener() {
//
//			@Override
//			public boolean onMenuItemClick(MenuItem item) {
//
//				Log.e(TAG, "public boolean onMenuItemClick(MenuItem item): " + item.toString());
//
//				exportSession(item);
//
//				return false;
//			}
//		};
//
//
//		return true;
//
//	}


//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//
////		final String searchTags = "#" + mParamSearchTags.replaceAll(", ", " #") + " ";
//
//		super.onCreateOptionsMenu(menu, inflater);
//
//		this.menu = menu;
//
//		// inflate menu
//		Log.e(TAG, "inflater.inflate(R.menu.action_bar_share_menu, menu);");
//		inflater.inflate(R.menu.action_bar_share_menu, menu);
//
//		// Locate MenuItem
//		MenuItem item = menu.findItem(R.id.menu_item_share);
//
//		// Fetch and store ShareActionProvider
//		ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
//
//
//
//
////		Intent googleDriveIntent = new Intent(getActivity().getApplicationContext(), CreateSessionFileInGoogleDrive.class);
////		mShareActionProvider.setShareIntent(googleDriveIntent);
//
//
//
//
////		Intent tweetIntent = new Intent(Intent.ACTION_SEND);
////		tweetIntent.putExtra(Intent.EXTRA_TEXT, searchTags);
////		tweetIntent.setType("text/plain");
////
////		PackageManager packManager = getActivity().getApplicationContext().getPackageManager();
////		List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);
////
////		boolean resolved = false;
////		for (ResolveInfo resolveInfo : resolvedInfoList) {
////			if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
////				tweetIntent.setClassName(
////						  resolveInfo.activityInfo.packageName,
////						  resolveInfo.activityInfo.name);
////				resolved = true;
////				break;
////			}
////		}
////		if (resolved) {
//////			startActivity(tweetIntent);
////		} else {
////			Toast.makeText(getActivity().getBaseContext(), "Twitter app not found, please install to add to the conversation", Toast.LENGTH_LONG).show();
////		}
////
////		mShareActionProvider.setShareIntent(tweetIntent);
//
//
//
//
////		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
////		sharingIntent.setType("text/plain");
////		String shareBody = "Here is the share content body";
////		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
////		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
////		startActivity(Intent.createChooser(sharingIntent, "Share via"));
//
//
//
//
//		Intent shareIntent = new Intent(Intent.ACTION_SEND);
//
//		PackageManager packManager = getActivity().getBaseContext().getPackageManager();
//		List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
//
//		boolean resolved = false;
//		for (ResolveInfo resolveInfo : resolvedInfoList) {
//			resolved = true;
//		}
//		if (resolved) {
//			startActivity(shareIntent);
//		}
//
//		mShareActionProvider.setShareIntent(shareIntent);
//
//
//	}


//		@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//
//		// Find the MenuItem that we know has the ShareActionProvider
//		MenuItem item = menu.findItem(R.id.menu_item_share);
//
//		// Get its ShareActionProvider
//		mShareActionProvider = (ShareActionProvider) item.getActionProvider();
//
//		// Connect the dots: give the ShareActionProvider its Share Intent
//		if (mShareActionProvider != null) {
//			mShareActionProvider.setShareIntent(mShareIntent);
//		}
//
//		// Return true so Android will know we want to display the menu
//		return true;
//	}



//	MenuItem.OnMenuItemClickListener mShareButtonClickListener = new MenuItem.OnMenuItemClickListener() {
//
//		@Override
//		public boolean onMenuItemClick(MenuItem item) {
//
//			Log.e(TAG, "public boolean onMenuItemClick(MenuItem item): " + item.toString());
//
//			exportSession(item);
//
//			return false;
//		}
//	};


	// ################################################################

//	public void exportSession(MenuItem item) {
//
//		Log.e(TAG, "exportSession(MenuItem item): " + item.toString());
//
//		// Fetch and store ShareActionProvider
//		ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
//
//
//		Intent i = new Intent(Intent.ACTION_SEND);
////		i.setType("plain/text");
////		i.setType("plain/csv");
////		i.setType("text/comma-separated-values");
//		i.setType("application/csv");
//		File data = null;
//		try {
////			Date dateVal = new Date();
////			String filename = dateVal.toString();
//
//
//			String filename = SessionSingleton.getInstance().getTimestampPS4();
//
//////			data = File.createTempFile("Report", ".csv");
////			data = File.createTempFile(filename, ".csv");
//
//
////			String FILE = Environment.getExternalStorageDirectory() + File.separator
////					  + "Foldername";
//			String FILE = Environment.getExternalStorageDirectory().toString();
//
//
//			String temp_path = FILE + File.separator + filename + ".csv";
//
//
//			File F = new File(temp_path);
//
//			FileWriter out = (FileWriter) GenerateCsv.generateCsvFile(
//					  F, SessionSingleton.getInstance().getExportDataCSV());
//
//
//			Uri U = Uri.fromFile(F);
////			Uri U = Uri.fromFile(temp_path);
//			i.putExtra(Intent.EXTRA_STREAM, U);
//
//
//			mShareActionProvider.setShareIntent(Intent.createChooser(i, "Share Session"));
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//
//	}




	// ################################################################

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

//		Toast.makeText(getBaseContext(),
//				  getResources().getText(R.string.app_name) +
//						    " Version: " +
//						    BuildConfig.VERSION_NAME,
//				  Toast.LENGTH_SHORT).show();

		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
//		if (mDrawerToggle.onOptionsItemSelected(item)) {
//			return true;
//		}
//
//		return false;

		Log.e(TAG, "onOptionsItemSelected(): " + item.toString());

		return mDrawerToggle.onOptionsItemSelected(item);

//		return super.onOptionsItemSelected(item);

	}


	// ################################################################

	public void SelectItem(int position) {

		android.app.Fragment fragment = null;
		Bundle args = new Bundle();
		String backStackName = "";
		switch (position) {
			case 0:
//				fragment = new WelcomeFragment();
				backStackName = "eeg";
				try{
					fragment = getFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					e.printStackTrace();
//					fragment = new EEGFragment();
				}
				Log.e(TAG, "fragment = new EEGFragment()");
				if (fragment == null)
					fragment = new EEGFragment();
				break;
			case 1:
//				fragment = new SessionFragment();
				backStackName = "session";
				try{
					fragment = getFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					e.printStackTrace();
//					fragment = new SessionFragment();
				}
				Log.e(TAG, "fragment = new SessionFragment()");
				if (fragment == null)
					fragment = new SessionFragment();
				break;
			case 2:
//				fragment = new EEGFragment();
				backStackName = "eeg";
				try{
					fragment = getFragmentManager().findFragmentByTag(backStackName);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Log.e(TAG, "fragment = new EEGFragment()");
				if (fragment == null)
					fragment = new EEGFragment();

				break;
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

//	public static class GenerateCsv {
////		public static FileWriter generateCsvFile(File sFileName,String fileContent) {
//			public static FileWriter generateCsvFile(File sFileName,String fileContent) {
//			FileWriter writer = null;
//
//			try {
//				writer = new FileWriter(sFileName);
//				writer.append(fileContent);
//				writer.flush();
//
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}finally
//			{
//				try {
//					writer.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			return writer;
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

}
