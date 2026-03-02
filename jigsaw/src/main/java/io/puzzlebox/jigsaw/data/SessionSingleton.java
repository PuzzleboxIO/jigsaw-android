package io.puzzlebox.jigsaw.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.util.Log;

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

	public int getRequestExternalStorage() {
		return REQUEST_EXTERNAL_STORAGE;
	}

	/**
	 * Update the current timestamp to the current date and time.
	 */
	public void updateTimestamp() {
		// Make a new Date object. It will be initialized to the current time.
		currentTimestamp = new Date();
	}

	public String getCurrentDate(){
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			return dateFormat.format(currentTimestamp);
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
			return null;
		}
	}

	public String getCurrentTimestamp(){
		try {
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
			return timeFormat.format(currentTimestamp);
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
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
			Log.e(TAG, "Exception", e);
		}

		try {
			date2 = df.parse( getCurrentTimestamp() );
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
		}

		try {
			if (date1 != null && date2 != null)
				diff = date2.getTime() - date1.getTime();
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
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
				String val = data.get( data.size() - (count - i ) ).get(key);
				result[i] = (val != null) ? Integer.parseInt(val) : 0;
			} catch (ArrayIndexOutOfBoundsException e) {
				result[i] = 0;
			} catch (Exception e) {
				Log.e(TAG, "Exception", e);
				result[i] = 0;
			}
		}
		return result;
	}

	public String getExportDataCSV() {

		StringBuilder output = new StringBuilder();
		String current;

		List<String[]> dataCSV = getExportData();

		if (dataCSV == null)
			return output.toString();

		for (String[] line: dataCSV) {

			current = Arrays.toString(line);
			current = current.replaceAll("\\[", "");
			current = current.replaceAll("]", "");
			output.append(current).append("\n");
		}
		return output.toString();
	}

	// Tracks the last exported file so it can be cleaned up without needing a Context.
	private File lastExportedFile = null;

	public void removeTemporarySessionFile() {
		if (lastExportedFile != null && lastExportedFile.exists()) {
			if (lastExportedFile.delete()) lastExportedFile = null;
		}
	}

	public Intent getExportSessionIntent(Context context) {

		removeTemporarySessionFile();

		Intent mShareIntent = new Intent();

		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("application/csv"); // Produces the most correct options in the share menu
		try {
			sessionFilename = sessionName + "_" + SessionSingleton.getInstance().getTimestampPS4() + ".csv";

			// Use app-private external storage — no permission required on any API level.
			File exportDir = context.getExternalFilesDir(null);
			File tempFile = new File(exportDir, sessionFilename);

			GenerateCsv.generateCsvFile(
					tempFile, SessionSingleton.getInstance().getExportDataCSV());

			lastExportedFile = tempFile;

			// FileProvider replaces Uri.fromFile() which throws FileUriExposedException on API 24+.
			Uri U = FileProvider.getUriForFile(context,
					context.getPackageName() + ".fileprovider", tempFile);
			i.putExtra(Intent.EXTRA_STREAM, U);
			i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

			i.putExtra(Intent.EXTRA_SUBJECT, sessionFilename);
			i.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.share_message));

			mShareIntent = Intent.createChooser(i, "Share Session");
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
		}
		return mShareIntent;
	}

	public static class GenerateCsv {
		public static void generateCsvFile(File sFileName, String fileContent) {
			try (FileWriter writer = new FileWriter(sFileName)) {
				writer.append(fileContent);
				writer.flush();
			} catch (IOException e) {
				Log.e(TAG, "Exception", e);
			}
		}
	}

}