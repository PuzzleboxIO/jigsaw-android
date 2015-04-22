package io.puzzlebox.jigsaw;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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