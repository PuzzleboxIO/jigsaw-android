package io.puzzlebox.jigsaw.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import io.puzzlebox.jigsaw.R;

/**
 * Created by sc on 4/25/17.
 */

public class ProfileSingleton {

	private static final String TAG = ProfileSingleton.class.getSimpleName();

	public static ArrayList<HashMap<String, String>> inputs = new ArrayList<>();
	public static ArrayList<HashMap<String, String>> outputs = new ArrayList<>();
	//	public static ArrayList<HashMap<String, String>> profiles = new ArrayList<>();
	public ArrayList<HashMap<String, String>> profiles = new ArrayList<>();
	public static ArrayList<String[]> profiles_inputs = new ArrayList<>();
	public static ArrayList<String[]> profiles_outputs = new ArrayList<>();

	private static final ProfileSingleton ourInstance = new ProfileSingleton();

	public static ProfileSingleton getInstance() {
		return ourInstance;
	}

	private ProfileSingleton() {
	}


	public void parseXML(Context context) {

		HashMap<String, String> data;

		String[] devicesInput = context.getResources().getStringArray(R.array.devices_input_array);
		String[] devicesInputIcon = context.getResources().getStringArray(R.array.devices_input_icon_array);
		String[] devicesInputCompany = context.getResources().getStringArray(R.array.devices_input_company_array);
		String[] devicesInputProduct = context.getResources().getStringArray(R.array.devices_input_product_array);

		for (int i = 0; i < devicesInput.length ; i++) {
			data = new HashMap<String, String>();
			data.put("id", devicesInput[i]);
			data.put("icon", devicesInputIcon[i]);
			data.put("company", devicesInputCompany[i]);
			data.put("product", devicesInputProduct[i]);
			data.put("active", "false");
			inputs.add(data);
		}

		Log.d(TAG, "inputs: " + inputs);


		String[] devicesOutput = context.getResources().getStringArray(R.array.devices_output_array);
		String[] devicesOutputIcon = context.getResources().getStringArray(R.array.devices_output_icon_array);
		String[] devicesOutputCompany = context.getResources().getStringArray(R.array.devices_output_company_array);
		String[] devicesOutputProduct = context.getResources().getStringArray(R.array.devices_output_product_array);

		for (int i = 0; i < devicesOutput.length ; i++) {
			data = new HashMap<String, String>();
			data.put("id", devicesOutput[i]);
			data.put("icon", devicesOutputIcon[i]);
			data.put("company", devicesOutputCompany[i]);
			data.put("product", devicesOutputProduct[i]);
			data.put("active", "false");
			outputs.add(data);
		}

		Log.d(TAG, "outputs: " + outputs);


		String[] devicesProfile = context.getResources().getStringArray(R.array.devices_profile_array);
//		String[] devicesProfileIcon = context.getResources().getStringArray(R.array.devices_profile_icon_array);
//		String[] devicesProfileCompany = context.getResources().getStringArray(R.array.devices_profile_company_array);
//		String[] devicesProfileProduct = context.getResources().getStringArray(R.array.devices_profile_product_array);
		String[] devicesProfileInputs; // = context.getResources().getStringArray(R.array.devices_profile_company_array);
		String[] devicesProfileOutputs; // = context.getResources().getStringArray(R.array.devices_profile_product_array);

		for (int i = 0; i < devicesProfile.length ; i++) {
			data = new HashMap<String, String>();
			data.put("id", devicesProfile[i]);
//			data.put("icon", devicesProfileIcon[i]);
//			data.put("company", devicesProfileCompany[i]);
//			data.put("product", devicesProfileProduct[i]);

//			Log.e(TAG, "data.get(\"id\"): " + data.get("id"));

			data.put("title", context.getResources().getString(
					  getId(data.get("id") + "_title", R.string.class)));
			data.put("icon", context.getResources().getString(
					  getId(data.get("id") + "_icon", R.string.class)));
			data.put("company", context.getResources().getString(
					  getId(data.get("id") + "_company", R.string.class)));
			data.put("product", context.getResources().getString(
					  getId(data.get("id") + "_product", R.string.class)));

			data.put("active", "false");

			devicesProfileInputs = context.getResources().getStringArray(
					  getId(data.get("id") + "_input", R.array.class));
			devicesProfileOutputs = context.getResources().getStringArray(
					  getId(data.get("id") + "_output", R.array.class));

			Log.d(TAG, "devicesProfileInputs: " + devicesProfileInputs);
			Log.d(TAG, "devicesProfileOutputs: " + devicesProfileOutputs);

//			for (String s : devicesProfileInputs) {
//				Log.e(TAG, "s: " + s);
//			}

			profiles_inputs.add(devicesProfileInputs);
			profiles_outputs.add(devicesProfileOutputs);


			profiles.add(data);
		}

		Log.d(TAG, "profiles: " + profiles);

	}


	// Reference: http://stackoverflow.com/a/17622392
//	public static int getId(String resourceName, Class<?> c) {
	public int getId(String resourceName, Class<?> c) {
		try {
			Field idField = c.getDeclaredField(resourceName);
			return idField.getInt(idField);
		} catch (Exception e) {
			throw new RuntimeException("No resource ID found for: "
					  + resourceName + " / " + c, e);
		}
	}


	public static Drawable getAndroidDrawable(String pDrawableName){
		int resourceId= Resources.getSystem().getIdentifier(pDrawableName, "drawable", "android");
		if(resourceId==0){
			return null;
		} else {
			return Resources.getSystem().getDrawable(resourceId);
		}

//		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){ return mContext.getDrawable(resourceId); } else { return mContext.getResources().getDrawable(resourceId); }

	}


	public static Drawable getDeviceDrawable(String name) {

		for (HashMap<String, String> map : inputs) {
			Log.e(TAG, "id: " + map.get("id"));
			if (map.get("id").equals(name)) {
//				return Resources.getSystem().getDrawable(map.get("icon"));
				Log.e(TAG, "icon: " + map.get("icon"));

				return Resources.getSystem().getDrawable(
						  Resources.getSystem().getIdentifier(map.get("icon"), "drawable", "io.puzzlebox.orbit")
				);

			}
		}

		return null;
	}


//	public static String getDeviceIconPath(String name) {
	public String getDeviceIconPath(String name) {

		for (HashMap<String, String> map : inputs) {
//			Log.e(TAG, "id: " + map.get("id"));
			if (map.get("id").equals(name)) {
				Log.e(TAG, "icon: " + map.get("icon"));
				return(map.get("icon"));
			}
		}

		for (HashMap<String, String> map : outputs) {
//			Log.e(TAG, "id: " + map.get("id"));
			if (map.get("id").equals(name)) {
				Log.e(TAG, "icon: " + map.get("icon"));
				return(map.get("icon"));
			}
		}

		return null;

	}


}
