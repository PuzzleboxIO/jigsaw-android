package io.puzzlebox.jigsaw.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

import io.puzzlebox.jigsaw.R;
import io.puzzlebox.jigsaw.data.ConfigurationSingleton;
import io.puzzlebox.jigsaw.data.DeviceEmotivInsightSingleton;
import io.puzzlebox.jigsaw.service.EmotivInsightService;

public class DialogInputEmotivInsightFragment extends DialogFragment {

	private final static String TAG = DialogInputEmotivInsightFragment.class.getSimpleName();

	public final static String profileID = "emotiv_insight";

	// UI
	View v;
	Button buttonConnectEEG;
	Button buttonDeviceEnable;

	ImageView imageViewEmotivInsight;


	private int currentAF3 = 0;
	private int currentAF4 = 0;
	private int currentT7 = 0;
	private int currentT8 = 0;
	private int currentPz = 0;
	private int currentCMS = 0;

	private static Intent intentEmotivInsight;

	private EmotivInsightFragmentListener mListener;

	public DialogInputEmotivInsightFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.dialog_input_emotiv_insight, container, false);

		Window dialogWindow = requireDialog().getWindow();
		if (dialogWindow != null) dialogWindow.setTitle( getString(R.string.title_dialog_fragment_emotiv_insight));

		buttonConnectEEG = v.findViewById(R.id.buttonConnectEEG);
		buttonConnectEEG.setOnClickListener(v -> connectHeadset());


		Button buttonDeviceCancel = v.findViewById(R.id.buttonDeviceCancel);
		buttonDeviceCancel.setOnClickListener(v -> {
			disconnectHeadset();
			broadcastTileStatus("false");
			dismiss();
		});

		buttonDeviceEnable = v.findViewById(R.id.buttonDeviceEnable);
		buttonDeviceEnable.setOnClickListener(v -> {
			broadcastTileStatus("true");
			dismiss();
		});

		imageViewEmotivInsight = v.findViewById(R.id.imageViewEmotivInsight);


		drawEEGStatus(0, 0, 0, 0, 0, 0);

		intentEmotivInsight = new Intent(requireActivity(), EmotivInsightService.class);

		// Enable to attempt to automatically connect to nearby Emotiv Insight
		// headsets, without the user having to manually press the "Connect" button
		// connectHeadset();

		return v;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof EmotivInsightFragmentListener) {
			mListener = (EmotivInsightFragmentListener) context;
		} else {
			throw new RuntimeException(context
					+ " must implement EmotivInsightFragmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public interface OnFragmentInteractionListener {
		void onFragmentInteraction(Uri uri);
	}

	@Override
	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(
				requireActivity().getApplicationContext()).unregisterReceiver(
				mSignalQualityReceiver);
		LocalBroadcastManager.getInstance(
				requireActivity()).unregisterReceiver(
				mStatusReceiver);
	}

	@Override
	public void onResume() {
		// Store access variables for window and blank point
		Window window = requireDialog().getWindow();

		if (window == null) {
			super.onResume();
			return;
		}

		window.setGravity(Gravity.CENTER);

		// Call super onResume after sizing
		super.onResume();

		LocalBroadcastManager.getInstance(requireActivity().getApplicationContext()).registerReceiver(
				mSignalQualityReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.signal_quality"));

		LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
				mStatusReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.status"));
	}

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String name = intent.getStringExtra("name");
			String value = intent.getStringExtra("value");

			if (name == null) return;
			if (! "populateSelectEEG".equals(name)) {
				Log.d(TAG, "[" + name + "]: " + value);
			}

			switch(name) {
				case "populateSelectEEG":
					populateSelectEEG();
					break;

				case "status":
					if (getActivity() != null && value != null) {
						switch (value) {
							case "connected":
								buttonConnectEEG.setText(getString(R.string.buttonStatusEmotivInsightDisconnect));

								buttonDeviceEnable.setEnabled(true);
								buttonDeviceEnable.setVisibility(View.VISIBLE);

								break;

							case "disconnected":
								buttonConnectEEG.setText(getString(R.string.buttonStatusEmotivInsightConnect));

								buttonDeviceEnable.setEnabled(false);
								buttonDeviceEnable.setVisibility(View.INVISIBLE);

								break;
						}
					}
					break;
				case "select":
					if (value != null) {
						int mId = Integer.parseInt(value);
						if (mId == -1) {
							onSelectEEGRefresh();
						} else {
							Log.d(TAG, "onSelectEEGItem(" + mId + ")");
							onSelectEEGItem(mId);
						}
					}
					break;
			}
		}

	};

	public void connectHeadset() {
		/*
		 * Called when the "Connect" button is pressed
		 */
		if (!DeviceEmotivInsightSingleton.getInstance().lock) {
			requireActivity().startService(intentEmotivInsight);
		} else {
			disconnectHeadset();
		}
	}

	public void disconnectHeadset() {
		/*
		 * Called when "Disconnect" button is pressed
		 */
		if (DeviceEmotivInsightSingleton.getInstance().lock) {
			DeviceEmotivInsightSingleton.getInstance().connectEmotivInsight(-1);
		}

		requireActivity().stopService(intentEmotivInsight);

		buttonConnectEEG.setText(getString(R.string.buttonStatusEmotivInsightConnect));
	}

	public void onSelectEEGRefresh() {
	}

	public void onSelectEEGItem(Integer deviceNumber) {

		Log.d(TAG, "Selecting Insight: " + deviceNumber);

		if (deviceNumber != -1) {
			DeviceEmotivInsightSingleton.getInstance().connectEmotivInsight(deviceNumber);
		}

		// Dismiss dialog
		DialogInputEmotivInsightSelectEEGFragment mSelectEEG = (DialogInputEmotivInsightSelectEEGFragment) requireActivity().getSupportFragmentManager().findFragmentByTag("mSelectEEG");

		if (mSelectEEG != null)
			mSelectEEG.dismiss();

		DeviceEmotivInsightSingleton.getInstance().selectEEGDialogVisible = false;
	}

	public void populateSelectEEG() {

		if (!DeviceEmotivInsightSingleton.getInstance().lock) {

			int number = DeviceEmotivInsightSingleton.getInstance().getInsightDeviceCount();

			DialogInputEmotivInsightSelectEEGFragment mSelectEEG = (DialogInputEmotivInsightSelectEEGFragment) requireActivity().getSupportFragmentManager().findFragmentByTag("mSelectEEG");
			if (mSelectEEG != null) {
				return;
			}

			DeviceEmotivInsightSingleton.getInstance().detectedDevices = new ArrayList<>();

			for (int deviceCount = 0; deviceCount < number; deviceCount++) {
				String deviceName = DeviceEmotivInsightSingleton.getInstance().getInsightDeviceName(deviceCount);
				Log.d(TAG, "Insight detected: " + deviceCount + ": \"" + deviceName + "\" ");

				if (!DeviceEmotivInsightSingleton.getInstance().detectedDevices.contains(deviceName)) {
					DeviceEmotivInsightSingleton.getInstance().detectedDevices.add(deviceCount, deviceName);
				}

				showSelectEEG();
			}
		}
		else {
			Log.e(TAG, "populateSelectEEG called on locked device");
		}
	}

	public void showSelectEEG() {

		if (!DeviceEmotivInsightSingleton.getInstance().selectEEGDialogVisible) {

			DeviceEmotivInsightSingleton.getInstance().selectEEGDialogVisible = true;

			FragmentManager fm = requireActivity().getSupportFragmentManager();
			DialogInputEmotivInsightSelectEEGFragment mSelectEEG = new DialogInputEmotivInsightSelectEEGFragment();
			try {
				mSelectEEG.show(fm, "mSelectEEG");
			} catch (Exception e) {
				Log.e(TAG, "mSelectEEG.show(fm, \"mSelectEEG\"): " + e);
			}
		}
	}

	private void drawEEGStatus(int AF3, int AF4, int T7, int T8, int Pz, int CMS) {

		Resources r;

		int scale_x = ConfigurationSingleton.getInstance().displayWidth / 2;
		int scale_y = ConfigurationSingleton.getInstance().displayHeight / 3;

		try {
			r = getResources();
		} catch (Exception e) {
			Log.e(TAG, "drawEEGStatus: Failure to getResources: " + e);
			return;
		}

		Drawable[] layers = new Drawable[7];
		int drawableID;

		layers[0] = new BitmapDrawable(r, decodeSampledBitmapFromResource(r, R.drawable.device_eeg_sensor_head, scale_x, scale_y));


		Log.e(TAG, "layers[0].getDrawable().getIntrinsic*(): " + layers[0].getIntrinsicWidth() + ", " + layers[0].getIntrinsicHeight());

		switch (AF3) {
			case 1 -> drawableID = R.drawable.device_eeg_sensor_af3_red;
			case 2 -> drawableID = R.drawable.device_eeg_sensor_af3_orange;
			case 4 -> drawableID = R.drawable.device_eeg_sensor_af3_green;
			default -> drawableID = R.drawable.device_eeg_sensor_af3_white;
		}

		layers[1] = new BitmapDrawable(r, decodeSampledBitmapFromResource(r, drawableID, scale_x, scale_y));

//		imageViewAF3.setImageResource(drawableID);  // TODO

		switch (AF4) {
			case 1 -> drawableID = R.drawable.device_eeg_sensor_af4_red;
			case 2 -> drawableID = R.drawable.device_eeg_sensor_af4_orange;
			case 4 -> drawableID = R.drawable.device_eeg_sensor_af4_green;
			default -> drawableID = R.drawable.device_eeg_sensor_af4_white;
		}

		layers[2] = new BitmapDrawable(r, decodeSampledBitmapFromResource(r, drawableID, scale_x, scale_y));

//		imageViewAF4.setImageResource(drawableID); // TODO

		switch (T7) {
			case 1 -> drawableID = R.drawable.device_eeg_sensor_t7_red;
			case 2 -> drawableID = R.drawable.device_eeg_sensor_t7_orange;
			case 4 -> drawableID = R.drawable.device_eeg_sensor_t7_green;
			default -> drawableID = R.drawable.device_eeg_sensor_t7_white;
		}

		layers[3] = new BitmapDrawable(r, decodeSampledBitmapFromResource(r, drawableID, scale_x, scale_y));

//		imageViewT7.setImageResource(drawableID); // TODO

		switch (T8) {
			case 1 -> drawableID = R.drawable.device_eeg_sensor_t8_red;
			case 2 -> drawableID = R.drawable.device_eeg_sensor_t8_orange;
			case 4 -> drawableID = R.drawable.device_eeg_sensor_t8_green;
			default -> drawableID = R.drawable.device_eeg_sensor_t8_white;
		}

		layers[4] = new BitmapDrawable(r, decodeSampledBitmapFromResource(r, drawableID, scale_x, scale_y));

//		imageViewT8.setImageResource(drawableID); // TODO

		switch (Pz) {
			case 1 -> drawableID = R.drawable.device_eeg_sensor_pz_red;
			case 2 -> drawableID = R.drawable.device_eeg_sensor_pz_orange;
			case 4 -> drawableID = R.drawable.device_eeg_sensor_pz_green;
			default -> drawableID = R.drawable.device_eeg_sensor_pz_white;
		}

		layers[5] = new BitmapDrawable(r, decodeSampledBitmapFromResource(r, drawableID, scale_x, scale_y));

//		imageViewPz.setImageResource(drawableID); // TODO

		switch (CMS) {
			case 1 -> drawableID = R.drawable.device_eeg_sensor_tp7_cms_red;
			case 2 -> drawableID = R.drawable.device_eeg_sensor_tp7_cms_orange;
			case 4 -> drawableID = R.drawable.device_eeg_sensor_tp7_cms_green;
			default -> drawableID = R.drawable.device_eeg_sensor_tp7_cms_white;
		}

		layers[6] = new BitmapDrawable(r, decodeSampledBitmapFromResource(r, drawableID, scale_x, scale_y));

//		imageViewCMS.setImageResource(drawableID); // TODO

		LayerDrawable layerDrawable = new LayerDrawable(layers);

		imageViewEmotivInsight.setImageDrawable(layerDrawable);
	}

	private final BroadcastReceiver mSignalQualityReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String af3Str = intent.getStringExtra("AF3");
			String af4Str = intent.getStringExtra("AF4");
			String t7Str = intent.getStringExtra("T7");
			String t8Str = intent.getStringExtra("T8");
			String pzStr = intent.getStringExtra("Pz");
			String cmsStr = intent.getStringExtra("CMS");
			if (af3Str == null || af4Str == null || t7Str == null || t8Str == null || pzStr == null || cmsStr == null) return;
			int AF3 = Integer.parseInt(af3Str);
			int AF4 = Integer.parseInt(af4Str);
			int T7 = Integer.parseInt(t7Str);
			int T8 = Integer.parseInt(t8Str);
			int Pz = Integer.parseInt(pzStr);
			int CMS = Integer.parseInt(cmsStr);

			// If any value has change update all ImageViews
			if ((AF3 != currentAF3) ||
					(AF4 != currentAF4) ||
					(T7 != currentT7) ||
					(T8 != currentT8) ||
					(Pz != currentPz) ||
					(CMS != currentCMS)) {
				drawEEGStatus(AF3, AF4, T7, T8, Pz, CMS);
			}

			currentAF3 = AF3;
			currentAF4 = AF4;
			currentT7 = T7;
			currentT8 = T8;
			currentPz = Pz;
			currentCMS = CMS;
		}

	};

	public void broadcastTileStatus(String value) {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.tile.event");

		intent.putExtra("id", profileID);
		intent.putExtra("name", "active");
		intent.putExtra("value", value);
		intent.putExtra("category", "inputs");

		LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent);
	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
														 int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}
}
