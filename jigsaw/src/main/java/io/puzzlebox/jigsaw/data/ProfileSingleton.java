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

public class ProfileSingleton {

	private static final String TAG = ProfileSingleton.class.getSimpleName();

	private static final ArrayList<HashMap<String, String>> inputs = new ArrayList<>();
	private static final ArrayList<HashMap<String, String>> outputs = new ArrayList<>();
	public ArrayList<HashMap<String, String>> profiles = new ArrayList<>();
	private static final ArrayList<String[]> profiles_inputs = new ArrayList<>();
	private static final ArrayList<String[]> profiles_outputs = new ArrayList<>();

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
			data = new HashMap<>();
			data.put("id", devicesInput[i]);
			data.put("icon", devicesInputIcon[i]);
			data.put("company", devicesInputCompany[i]);
			data.put("product", devicesInputProduct[i]);
			data.put("active", "false");
			inputs.add(data);
		}

		String[] devicesOutput = context.getResources().getStringArray(R.array.devices_output_array);
		String[] devicesOutputIcon = context.getResources().getStringArray(R.array.devices_output_icon_array);
		String[] devicesOutputCompany = context.getResources().getStringArray(R.array.devices_output_company_array);
		String[] devicesOutputProduct = context.getResources().getStringArray(R.array.devices_output_product_array);

		for (int i = 0; i < devicesOutput.length ; i++) {
			data = new HashMap<>();
			data.put("id", devicesOutput[i]);
			data.put("icon", devicesOutputIcon[i]);
			data.put("company", devicesOutputCompany[i]);
			data.put("product", devicesOutputProduct[i]);
			data.put("active", "false");
			outputs.add(data);
		}

		String[] devicesProfile = context.getResources().getStringArray(R.array.devices_profile_array);
		String[] devicesProfileInputs; // = context.getResources().getStringArray(R.array.devices_profile_company_array);
		String[] devicesProfileOutputs; // = context.getResources().getStringArray(R.array.devices_profile_product_array);

		for (String aDevicesProfile : devicesProfile) {
			try {
				data = new HashMap<>();
				data.put("id", aDevicesProfile);

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

				profiles_inputs.add(devicesProfileInputs);
				profiles_outputs.add(devicesProfileOutputs);

				profiles.add(data);
			} catch (Exception e) {
				Log.e(TAG, "Error adding Profile tile: " + e);
			}
		}
	}

	// Reference: http://stackoverflow.com/a/17622392
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
	}

	public static Drawable getDeviceDrawable(String name) {
		for (HashMap<String, String> map : inputs) {
			Log.d(TAG, "id: " + map.get("id"));
			if (map.get("id").equals(name)) {
				Log.d(TAG, "icon: " + map.get("icon"));

				return Resources.getSystem().getDrawable(
						Resources.getSystem().getIdentifier(map.get("icon"), "drawable", "io.puzzlebox.orbit")
				);
			}
		}
		return null;
	}

	public String getDeviceIconPath(String name) {
		for (HashMap<String, String> map : inputs) {
			if (map.get("id").equals(name)) {
				return(map.get("icon"));
			}
		}

		for (HashMap<String, String> map : outputs) {
			if (map.get("id").equals(name)) {
				return(map.get("icon"));
			}
		}
		return null;
	}

	public void updateStatus(String id, String name, String value) {
		for (HashMap<String, String> map : inputs) {
			if (map.get("id").equals(id)) {
				map.put(name, value);
			}
		}

		for (HashMap<String, String> map : outputs) {
			if (map.get("id").equals(id)) {
				map.put(name, value);
			}
		}

		for (HashMap<String, String> map : profiles) {
			if (map.get("id").equals(id)) {
				map.put(name, value);
			}
		}
	}

	public boolean isActive(String category, int index) {
		boolean result = false;
		try {
			switch (category) {
				case "inputs":
					if (inputs.get(index).get("active").equals("true")) {
						result = true;
					}
					break;
				case "outputs":
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
		} catch (Exception e) {
			Log.e(TAG, "Error checking if tile is active:" + e);
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
						inputsActive[j] = (isActive("inputs", k));
						Log.d(TAG, "inputsActive[" + j + "]: " + inputsActive[j]);
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
						outputsActive[j] = (isActive("outputs", k));
						Log.d(TAG, "outputsActive[" + j + "]: " + outputsActive[j]);
					}
					++k;
				}
				++j;
			}

			Boolean allAvailable = true;

			try {
				for (Boolean check : inputsActive)
					if (!check)
						allAvailable = false;
				for (Boolean check : outputsActive)
					if (!check)
						allAvailable = false;
			} catch (Exception e) {
				// TODO 2018-03-14
				allAvailable = false;
				Log.e(TAG, "Exception parsing inputsActive/outputsActive: " + e.toString());
			}

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
