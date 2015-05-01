package io.puzzlebox.jigsaw.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.puzzlebox.jigsaw.R;

public class SessionSingleton {

	private static final String TAG = SessionSingleton.class.getSimpleName();

//	private static ArrayList<HashMap<String, String>> timestamp;

	private static Date currentTimestamp = new Date();
	private static Date sessionTimestamp = new Date();

	private static String sessionFilename;

	private static ArrayList<HashMap<String, String>> data = new ArrayList<>();

//	private ArrayList<String> fragmentTags = new ArrayList<>();

	private static SessionSingleton ourInstance = new SessionSingleton();

	public static SessionSingleton getInstance() {
		return ourInstance;
	}

	private SessionSingleton() {
	}

//	public void setData(ArrayList<HashMap<String, String>> data){
//		this.data = data;
//	}

	public void appendData(HashMap<String, String> packet){
		data.add(packet);
	}

	public void appendRawEEG(Number[] rawEEG) {
//		data.add(packet);
	}

	public ArrayList<HashMap<String, String>> getData(){
		return data;
	}

//	public ArrayList<String> getFragmentTags() {
//		return fragmentTags;
//	}
//
//	public void appendFragmentTag(String tag) {
//		fragmentTags.add(tag);
//	}
//
//	public void resetFragmentTags() {
//		fragmentTags = new ArrayList<>();
//	}


	// ################################################################

	public void setSessionFilename(String filename) {
		sessionFilename = filename;
	}

	public String getSessionFilename() {
		return sessionFilename;
	}


	// ################################################################

	/**
	 *
	 * @return yyyy-MM-dd HH:mm:ss formate date as string
	 */

//	public static Date updateTimestamp() {
	public Date updateTimestamp() {
		// Make a new Date object. It will be initialized to the current time.
		currentTimestamp = new Date();
		return currentTimestamp;
	}


	// ################################################################

	public String getCurrentDate(){
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			return dateFormat.format(currentTimestamp);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	// ################################################################

	public String getCurrentTimestamp(){
		try {
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
			return timeFormat.format(currentTimestamp);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	// ################################################################

	public String getTimestampPS4() {
		Date mDate = new Date();
		SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
		return mDateFormat.format(mDate);
	}


	// ################################################################

	public String getSessionTimestamp() {

//		Date mDate = new Date();

		java.util.Date date1 = new java.util.Date();
		java.util.Date date2 = new java.util.Date();
		long diff = 0;

//		java.text.DateFormat df = new java.text.SimpleDateFormat("hh:mm:ss");
		java.text.DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);


		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
		String sessionTimestampString = timeFormat.format(sessionTimestamp);

		try {
			date1 = df.parse( sessionTimestampString );
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			date2 = df.parse( getCurrentTimestamp() );
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			diff = date2.getTime() - date1.getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}

		int timeInSeconds = (int) (diff / 1000);
		int hours, minutes, seconds;
		hours = timeInSeconds / 3600;
		timeInSeconds = timeInSeconds - (hours * 3600);
		minutes = timeInSeconds / 60;
		timeInSeconds = timeInSeconds - (minutes * 60);
		seconds = timeInSeconds;

//		return (hours<10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds) + " h";

		return (hours<10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);

	}


	// ################################################################

	public void resetSession() {
		sessionTimestamp = new Date();
		data = new ArrayList<>();
	}


	// ################################################################

	public List<String[]> getExportData() {
		List<String[]> dataCSV = new ArrayList<>();

		dataCSV.add(new String[] {
				  "Date",
				  "Time",
				  "Attention",
				  "Meditation",
				  "Signal Level",
//				  "Power",
		});

		for (int i = data.size() - 1; i >= 0; i--) {
			dataCSV.add(new String[] {
					  data.get(i).get("Date"),
					  data.get(i).get("Time"),
					  data.get(i).get("Attention"),
					  data.get(i).get("Meditation"),
					  data.get(i).get("Signal Level"),
//					  data.get(i).get("Power"),
			});
		}

		return dataCSV;
	}


	// ################################################################

	public String getExportDataCSV() {

		String output = "";

		List<String[]> dataCSV = getExportData();

		if (dataCSV == null)
			return output;

////		for (int i = dataCSV.size() - 1; i >= 0; i--) {
//		for (int i = 0; i <= dataCSV.size() - 1; i++) {
//			output = output + dataCSV.get(i) + "\n";
//		}

		for (String[] line: dataCSV) {
			output = output + Arrays.toString(line) + "\n";
//			Log.e(TAG, Arrays.toString(line));
		}

		return output;
	}


	// ################################################################

	public void exportDataToCSV(String filepath, String filename) {

//		String csv = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

		if (filepath == null)
			filepath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

		if (filename == null)
			filename = getTimestampPS4();

		Log.i(TAG, "exportDataToCSV: " + filepath + "/" + filename + ".csv");

		try {
			String filenameCSV = filepath  + "/" + filename + ".csv";
			CSVWriter writer = new CSVWriter(new FileWriter(filenameCSV), ',');

			List<String[]> dataCSV = getExportData();

			writer.writeAll(dataCSV);
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	// ################################################################

	public Intent getExportSessionIntent(Context context, MenuItem item) {

		Log.d(TAG, "exportSession(MenuItem item): " + item.toString());

		// Fetch and store ShareActionProvider
//		ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);


		Intent mShareIntent = new Intent();

		Intent i = new Intent(Intent.ACTION_SEND);
//		i.setType("plain/text");
//		i.setType("plain/csv");
//		i.setType("text/comma-separated-values");
		i.setType("application/csv"); // Produces the most correct options in the share menu
		try {

			String filename = SessionSingleton.getInstance().getTimestampPS4() + ".csv";

			String tempFilePath = Environment.getExternalStorageDirectory().toString()
					  + File.separator + filename;

			File tempFile = new File(tempFilePath);

//			FileWriter out = GenerateCsv.generateCsvFile(
//					  tempFile, SessionSingleton.getInstance().getExportDataCSV());
			GenerateCsv.generateCsvFile(
					  tempFile, SessionSingleton.getInstance().getExportDataCSV());

			Uri U = Uri.fromFile(tempFile);
			i.putExtra(Intent.EXTRA_STREAM, U);

//			i.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.share_subject));
			i.putExtra(Intent.EXTRA_SUBJECT, filename);
			i.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.share_message));

			mShareIntent = Intent.createChooser(i, "Share Session");

//			context.startActivity(mShareIntent);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return mShareIntent;

	}


	// ################################################################

	public static class GenerateCsv {
		//		public static FileWriter generateCsvFile(File sFileName,String fileContent) {
		public static FileWriter generateCsvFile(File sFileName,String fileContent) {
			FileWriter writer = null;

			try {
				writer = new FileWriter(sFileName);
				writer.append(fileContent);
				writer.flush();

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (writer != null) {
						writer.close();
					}
				} catch (IOException e) {
					Log.d(TAG, "IOException: " + e);
				}
			}
			return writer;
		}
	}


	// ################################################################

//	public void exportDataGoogleDrive() {
//
//		try {
////			String filenameCSV = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getTimestampPS4() + ".csv";
////			CSVWriter writer = new CSVWriter(new FileWriter(filenameCSV), ',');
//
//			List<String[]> dataCSV = getExportData();
//
////			Intent intent = new Intent(get(), CreateFileInAppFolderActivity.class);
//
////			writer.writeAll(dataCSV);
////			writer.close();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}


}