package io.puzzlebox.jigsaw.data;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

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

	private static Date currentTimestamp = new Date();
	private static Date sessionTimestamp = new Date();

	private static String sessionName = "Untitled"; // default for when Session fragment is never loaded

	private static String sessionFilename;

	private static ArrayList<HashMap<String, String>> data = new ArrayList<>();

	private static final ArrayList<Integer> rawEEG = new ArrayList<>();

	private static int frequencyRawEEG = 512; // default 512 Hz

	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static final String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	private static final SessionSingleton ourInstance = new SessionSingleton();

	public static SessionSingleton getInstance() {
		return ourInstance;
	}

	private SessionSingleton() {
	}

	public void appendData(HashMap<String, String> packet){
		data.add(packet);
	}

	public void appendRawEEG(int raw) {
		rawEEG.add(raw);
	}

	public Integer[] getCurrentRawEEG() {

		int historySize = frequencyRawEEG;

		Integer[] currentEEG = new Integer[historySize];
		for (int i = 0; i < historySize; i++) {
			try {
				currentEEG[i] = rawEEG.get(rawEEG.size() - (historySize - i));
			} catch (ArrayIndexOutOfBoundsException e) {
				currentEEG[i] = 0;
			}
		}
		return currentEEG;
	}

	public ArrayList<HashMap<String, String>> getData(){
		return data;
	}

	public int getRequestExternalStorage() {
		return REQUEST_EXTERNAL_STORAGE;
	}

	/**
	 *
	 * @return yyyy-MM-dd HH:mm:ss formate date as string
	 */
	public Date updateTimestamp() {
		// Make a new Date object. It will be initialized to the current time.
		currentTimestamp = new Date();
		return currentTimestamp;
	}

	public String getCurrentDate(){
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			return dateFormat.format(currentTimestamp);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getCurrentTimestamp(){
		try {
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
			return timeFormat.format(currentTimestamp);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getTimestampPS4() {
		Date mDate = new Date();
		SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
		return mDateFormat.format(mDate);
	}

	public void setSessionName(String profileName) {
		sessionName = profileName;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setFrequencyRawEEG(int frequency) {
		frequencyRawEEG = frequency;
	}

	public Integer getFrequencyRawEEG() {
		return frequencyRawEEG;
	}

	public String getSessionTimestamp() {

		java.util.Date date1 = new java.util.Date();
		java.util.Date date2 = new java.util.Date();
		long diff = 0;

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

		// TODO There's clearly a bug with the math above, with the (apparently) correct values returning as negative
		// NOTE Actual session data doesn't seem affected when exported
		if (hours < 0)
			hours = hours * -1;
		if (minutes < 0)
			minutes = minutes * -1;
		if (seconds < 0)
			seconds = seconds * -1;

		return (hours<10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
	}

	public void resetSession() {
		removeTemporarySessionFile();
		sessionTimestamp = new Date();
		data = new ArrayList<>();
	}

	public List<String[]> getExportData() {
		List<String[]> dataCSV = new ArrayList<>();

		dataCSV.add(new String[] {
				"Date",
				"Time",
				"Attention",
				"Meditation",
				"Signal Level",
		});

		for (int i = 0; i < data.size(); i++) {
			dataCSV.add(new String[] {
					data.get(i).get("Date"),
					data.get(i).get("Time"),
					data.get(i).get("Attention"),
					data.get(i).get("Meditation"),
					data.get(i).get("Signal Level"),
			});
		}
		return dataCSV;
	}

	public Number[] getSessionRangeValues(String key, Integer count) {

		Number[] result = new Number[count];

		for (int i = 0; i < count; i++) {
			try {
				result[i] = Integer.valueOf(
						data.get( data.size() - (count - i ) ).get(key));
			} catch (ArrayIndexOutOfBoundsException e) {
				result[i] = 0;
			} catch (Exception e) {
				e.printStackTrace();
				result[i] = 0;
			}
		}
		return result;
	}

	public String getExportDataCSV() {

		String output = "";
		String current;

		List<String[]> dataCSV = getExportData();

		if (dataCSV == null)
			return output;

		for (String[] line: dataCSV) {

			current = Arrays.toString(line);
			current = current.replaceAll("\\[", "");
			current = current.replaceAll("]", "");
			output = output + current + "\n";
		}
		return output;
	}

	public void exportDataToCSV(String filepath, String filename) {

		removeTemporarySessionFile();

		if (filepath == null)
			filepath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

		if (filename == null) {
			filename = sessionName + "_" + getTimestampPS4();
		}

		Log.i(TAG, "exportDataToCSV: " + filepath + "/" + filename + ".csv");

		try {
			String filenameCSV = filepath  + "/" + filename + ".csv";
			CSVWriter writer = new CSVWriter(new FileWriter(filenameCSV), ',');

			List<String[]> dataCSV = getExportData();

			writer.writeAll(dataCSV);
			writer.close();

			sessionFilename = filename;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean removeTemporarySessionFile() {

		if (sessionFilename != null) {

			File mFile = new File(Environment.getExternalStorageDirectory().toString()
					+ File.separator + sessionFilename);

			return mFile.exists() && mFile.delete();
		} else
			return false;
	}

	public Intent getExportSessionIntent(Context context) {

		removeTemporarySessionFile();

		Intent mShareIntent = new Intent();

		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("application/csv"); // Produces the most correct options in the share menu
		try {
			sessionFilename = sessionName + "_" + SessionSingleton.getInstance().getTimestampPS4() + ".csv";

			String tempFilePath = Environment.getExternalStorageDirectory().toString()
					+ File.separator + sessionFilename;

			File tempFile = new File(tempFilePath);

			GenerateCsv.generateCsvFile(
					tempFile, SessionSingleton.getInstance().getExportDataCSV());

			Uri U = Uri.fromFile(tempFile);
			i.putExtra(Intent.EXTRA_STREAM, U);

			i.putExtra(Intent.EXTRA_SUBJECT, sessionFilename);
			i.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.share_message));

			mShareIntent = Intent.createChooser(i, "Share Session");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mShareIntent;
	}

	public static class GenerateCsv {
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
					Log.e(TAG, "IOException: " + e);
				}
			}
			return writer;
		}
	}

	/**
	 * Checks if the app has permission to write to device storage
	 *
	 * If the app does not has permission then the user will be prompted to grant permissions
	 * Android Jelly Bean (4.1) [API 16]
	 *
	 * @param activity
	 */
	public static void verifyStoragePermissions(final Activity activity) {
		// Check if we have write permission
		int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {

			final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(activity.getResources().getString(R.string.dialog_title_request_permission));
			builder.setMessage(activity.getResources().getString(R.string.dialog_message_request_permission));
			builder.setPositiveButton(android.R.string.ok,null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				@TargetApi(Build.VERSION_CODES.M)
				public void onDismiss(DialogInterface dialog) {
					ActivityCompat.requestPermissions(
							activity,
							PERMISSIONS_STORAGE,
							REQUEST_EXTERNAL_STORAGE
					);
				}
			});
			builder.show();
		} else {
			Intent i = SessionSingleton.getInstance().getExportSessionIntent(activity.getApplicationContext());

			if (i != null) {
				activity.startActivity(i);
			} else {
				Toast.makeText(activity.getApplicationContext(), "Error exporting session data for sharing", Toast.LENGTH_SHORT).show();
			}
		}
	}
}