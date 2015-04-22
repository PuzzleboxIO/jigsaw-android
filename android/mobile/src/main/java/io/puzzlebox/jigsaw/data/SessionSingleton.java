package io.puzzlebox.jigsaw.data;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

//import com.opencsv.CSVWriter;
//import com.opencsv.*;
//import org.apache.commons.lang3.*;

/**
 * Created by sc on 4/21/15.
 */
public class SessionSingleton {

	private static ArrayList<HashMap<String, String>> timestamp;

	private static Date currentTimestamp;

//	private static ArrayList<String> = new ArrayList();

//	private static String currentDate;
//	private static String currentTime;



	private static ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

	private static String query = "?q=session-xml";

	private ArrayList<String> fragmentTags = new ArrayList<String>();


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

	public ArrayList<HashMap<String, String>> getData(){
		return data;
	}

	public String getQuery() {
		return query;
	}


	public ArrayList<String> getFragmentTags() {
		return fragmentTags;
	}

	public void appendFragmentTag(String tag) {
		fragmentTags.add(tag);
	}

	public void resetFragmentTags() {
		fragmentTags = new ArrayList<String>();
	}

	/**
	 *
	 * @return yyyy-MM-dd HH:mm:ss formate date as string
	 */

//	public static Date updateTimestamp() {
	public Date updateTimestamp() {
		// Make a new Date object. It will be initialized to the current time.

		currentTimestamp = new Date();

		return new Date();
	}

	public String getCurrentDate(){
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String currentDate = dateFormat.format(currentTimestamp);
			return currentDate;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getCurrentTimestamp(){
		try {
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
			return timeFormat.format(currentTimestamp);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getTimestampPS4() {
		Date mDate = new Date();
		SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String mTimestamp = mDateFormat.format(mDate);
		return mTimestamp;
	}

	public void exportDataToCSV() {
		String csv = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

		try {

			String filenameCSV = "/sdcard/" + getTimestampPS4() + ".csv";

//			CSVWriter writer = new CSVWriter(new FileWriter(csv));
			CSVWriter writer = new CSVWriter(new FileWriter(filenameCSV), ',');


			List<String[]> dataCSV = new ArrayList<String[]>();

			dataCSV.add(new String[] {
					  "Date",
					  "Time",
					  "Attention",
					  "Meditation",
					  "Signal Level",
					  "Power",
			});

//			for (int i = WhatsOnSingleton.getInstance().getData().size() - 1; i >= 0; i--) {
			for (int i = data.size() - 1; i >= 0; i--) {

				dataCSV.add(new String[] {
//						  String.valueOf(data.get(i).get("Date")),
//						  String.valueOf(data.get(i).get("Time")),
//						  String.valueOf(data.get(i).get("Attention")),
//						  String.valueOf(data.get(i).get("Meditation")),
//						  String.valueOf(data.get(i).get("Signal Level")),
//						  String.valueOf(data.get(i).get("Power")),
						  data.get(i).get("Date"),
						  data.get(i).get("Time"),
						  data.get(i).get("Attention"),
						  data.get(i).get("Meditation"),
						  data.get(i).get("Signal Level"),
						  data.get(i).get("Power"),
				});

			}


			writer.writeAll(dataCSV);

			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

////	public static HashMap<String, String> getCurrentTimestamp(){
//		public static String[] getCurrentTimestamp(){
//		try {
//			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//			String currentDate = dateFormat.format(currentTimestamp);
//
//			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
//			String currentTime = timeFormat.format(currentTimestamp);
//
////			HashMap timestamp = new HashMap();
//
//			String[] timestamp = new String[];
//
//
////			List
//			return ([currentDate, currentTime]);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}


//	public void getTimeStamp(String hashtag, List<SessionSingleton.objectTweet> tweets) {
//		this.tweets = tweets;
//		this.tweetsByHashtag.put(hashtag, tweets);
//	}


//	public static String getCurrentTimeStamp(){
//		try {
////			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
////			String currentTimeStamp = dateFormat.format(new Date()); // Find todays date
//			String currentTimeStamp = dateFormat.format(currentTimestamp); // Find todays date
//			return currentTimeStamp;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}

}