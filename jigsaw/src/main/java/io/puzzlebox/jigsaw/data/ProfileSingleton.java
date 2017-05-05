package io.puzzlebox.jigsaw.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
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
	public ArrayList<HashMap<String, String>> profiles = new ArrayList<>();
	public static ArrayList<String[]> profiles_inputs = new ArrayList<>();
	public static ArrayList<String[]> profiles_outputs = new ArrayList<>();

	public int tilesAnimationId = R.anim.tiles_slow;

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

//		Log.d(TAG, "inputs: " + inputs);


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

//		Log.d(TAG, "outputs: " + outputs);


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
			data.put("status", "disabled");

			devicesProfileInputs = context.getResources().getStringArray(
					  getId(data.get("id") + "_input", R.array.class));
			devicesProfileOutputs = context.getResources().getStringArray(
					  getId(data.get("id") + "_output", R.array.class));

//			Log.d(TAG, "devicesProfileInputs: " + devicesProfileInputs);
//			Log.d(TAG, "devicesProfileOutputs: " + devicesProfileOutputs);

//			for (String s : devicesProfileInputs) {
//				Log.e(TAG, "s: " + s);
//			}

			profiles_inputs.add(devicesProfileInputs);
			profiles_outputs.add(devicesProfileOutputs);


			profiles.add(data);
		}

//		Log.d(TAG, "profiles: " + profiles);

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
//				Log.e(TAG, "icon: " + map.get("icon"));
				return(map.get("icon"));
			}
		}

		for (HashMap<String, String> map : outputs) {
//			Log.e(TAG, "id: " + map.get("id"));
			if (map.get("id").equals(name)) {
//				Log.e(TAG, "icon: " + map.get("icon"));
				return(map.get("icon"));
			}
		}

		return null;

	}


	public void updateStatus(String id, String name, String value) {

//		Log.e(TAG, "mTileReceiver.onReceive() id: " + id);
//		Log.e(TAG, "mTileReceiver.onReceive() name: " + name);
//		Log.e(TAG, "mTileReceiver.onReceive() value: " + value);

//		index = 0;
		for (HashMap<String, String> map : inputs) {
			if (map.get("id").equals(id)) {
//				inputs.get(index).put(name, value);
				map.put(name, value);
			}
//			++index;
		}

//		int index = 0;
		for (HashMap<String, String> map : outputs) {
			if (map.get("id").equals(id)) {
//				outputs.get(index).put(name, value);
//				Log.e(TAG, "outputs.get(index).get(name): " + outputs.get(index).get(name));
//				outputs.get(index).put(name, value);
//				Log.e(TAG, "outputs.get(index).get(name): " + outputs.get(index).get(name));
				map.put(name, value);
			}
//			++index;
		}

		for (HashMap<String, String> map : profiles) {
			if (map.get("id").equals(id)) {
//				profiles.get(index).put(name, value);
				map.put(name, value);
			}
		}

	}


	public boolean isActive(String category, int index) {

		boolean result = false;

		switch(category) {
			case "inputs":
//				Log.e(TAG, "inputs.get(" + index + ").get(\"active\"): " + inputs.get(index).get("active"));
				if (inputs.get(index).get("active").equals("true")) {
					result = true;
				}
				break;
			case "outputs":
//				Log.e(TAG, "outputs.get(" + index + ").get(\"active\"): " + outputs.get(index).get("active"));
				if (outputs.get(index).get("active").equals("true")) {
					result = true;
				}
				break;
			case "profiles":
				if (profiles.get(index).get("active").equals("true")) {
					result = true;
				}
				break;
		}

		return result;

	}


	public Drawable getProfileTileColor(Context context, int index) {

		if (ProfileSingleton.getInstance().isActive("profiles", index)) {
			profiles.get(index).put("status", "activated");
			return new ColorDrawable(  context.getResources().getColor(R.color.tileActivated));

		} else {

			Boolean[] inputsActive = new Boolean[profiles_inputs.get(index).length];
			Boolean[] outputsActive = new Boolean[profiles_outputs.get(index).length];

			int j = 0;
			int k;
			for (String name : profiles_inputs.get(index)) {
				k = 0;
				for (HashMap<String, String> map : inputs) {
					if (map.get("id").equals(name)) {
//						Log.e(TAG, "(map.get(\"id\").equals(" + name + "))");
						inputsActive[j] = (isActive("inputs", k));
//						Log.e(TAG, "inputsActive[" + j + "]: " + inputsActive[j]);
					}
					++k;
				}
				++j;
			}

			j = 0;
			for (String name : profiles_outputs.get(index)) {
				k = 0;
				for (HashMap<String, String> map : outputs) {
					if (map.get("id").equals(name)) {
//						Log.e(TAG, "(map.get(\"id\").equals(" + name + "))");
						outputsActive[j] = (isActive("outputs", k));
//						Log.e(TAG, "outputsActive[" + j + "]: " + outputsActive[j]);
					}
					++k;
				}
				++j;
			}

			Boolean allAvailable = true;
			for (Boolean check : inputsActive)
				if (!check)
					allAvailable = false;
			for (Boolean check : outputsActive)
				if (!check)
					allAvailable = false;

			if (allAvailable) {
				profiles.get(index).put("status", "available");
				return new ColorDrawable(context.getResources().getColor(R.color.white));
			} else {
				profiles.get(index).put("status", "disabled");
				return new ColorDrawable(context.getResources().getColor(R.color.tileDisabled));
			}
		}

	}


	public String getStatus(String id) {
		for (HashMap<String, String> map : inputs) {
			if (map.get("id").equals(id)) {
				return map.get("status");
			}
		}

		for (HashMap<String, String> map : outputs) {
			if (map.get("id").equals(id)) {
				return map.get("status");
			}
		}

		for (HashMap<String, String> map : profiles) {
			if (map.get("id").equals(id)) {
				return map.get("status");
			}
		}
		return null;
	}


	public String getValue(String id, String name) {
		for (HashMap<String, String> map : inputs) {
			if (map.get("id").equals(id)) {
				return map.get(name);
			}
		}

		for (HashMap<String, String> map : outputs) {
			if (map.get("id").equals(id)) {
				return map.get(name);
			}
		}

		for (HashMap<String, String> map : profiles) {
			if (map.get("id").equals(id)) {
				return map.get(name);
			}
		}
		return null;
	}

}
