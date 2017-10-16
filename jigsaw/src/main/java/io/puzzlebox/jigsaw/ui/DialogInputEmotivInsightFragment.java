package io.puzzlebox.jigsaw.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
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
import io.puzzlebox.jigsaw.protocol.EmotivInsightService;

public class DialogInputEmotivInsightFragment extends DialogFragment {

	private final static String TAG = DialogInputEmotivInsightFragment.class.getSimpleName();

	public final static String profileID = "emotiv_insight";

	// UI
	View v;
	Button buttonConnectEEG;
	Button buttonDeviceEnable;

	ImageView imageViewEmotivInsight;

//	ImageView imageViewAF3;
//	ImageView imageViewAF4;
//	ImageView imageViewT7;
//	ImageView imageViewT8;
//	ImageView imageViewPz;
//	ImageView imageViewCMS;

	private int currentAF3 = 0;
	private int currentAF4 = 0;
	private int currentT7 = 0;
	private int currentT8 = 0;
	private int currentPz = 0;
	private int currentCMS = 0;

	private static Intent intentEmotivInsight;

	private OnFragmentInteractionListener mListener;

	public DialogInputEmotivInsightFragment() {
		// Required empty public constructor
	}

//	public static DialogInputEmotivInsightFragment newInstance() {
//		DialogInputEmotivInsightFragment fragment = new DialogInputEmotivInsightFragment();
//		Bundle args = new Bundle();
//		fragment.setArguments(args);
//		return fragment;
//	}

//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
////		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
//	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
									 Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.dialog_input_emotiv_insight, container, false);

		getDialog().getWindow().setTitle( getString(R.string.title_dialog_fragment_emotiv_insight));

		buttonConnectEEG = v.findViewById(R.id.buttonConnectEEG);
		buttonConnectEEG.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				connectHeadset();
			}
		});


		Button buttonDeviceCancel = v.findViewById(R.id.buttonDeviceCancel);
		buttonDeviceCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disconnectHeadset();
				broadcastTileStatus("false");
				dismiss();
			}
		});

		buttonDeviceEnable = v.findViewById(R.id.buttonDeviceEnable);
		buttonDeviceEnable.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				broadcastTileStatus("true");
				dismiss();
			}
		});

		imageViewEmotivInsight = v.findViewById(R.id.imageViewEmotivInsight);

//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
////			Log.e(TAG, "imageViewEmotivInsight.getDrawable().getDirtyBounds(): " + imageViewEmotivInsight.getDrawable().getDirtyBounds());
//			Log.e(TAG, "imageViewEmotivInsight.getDrawable().getIntrinsic*(1): " + imageViewEmotivInsight.getDrawable().getIntrinsicWidth() + ", " + imageViewEmotivInsight.getDrawable().getIntrinsicHeight());
//		}

//		imageViewEmotivInsight.setImageResource(R.drawable.device_eeg_sensor_head);



//		Resources r = getResources();
//		Drawable[] layers = new Drawable[7];
//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//			//noinspection deprecation
//			layers[0] = r.getDrawable(R.drawable.device_eeg_sensor_head);
//			//noinspection deprecation
//			layers[1] = r.getDrawable(R.drawable.device_eeg_sensor_af3_white);
//			//noinspection deprecation
//			layers[2] = r.getDrawable(R.drawable.device_eeg_sensor_af4_white);
//			//noinspection deprecation
//			layers[3] = r.getDrawable(R.drawable.device_eeg_sensor_t7_white);
//			//noinspection deprecation
//			layers[4] = r.getDrawable(R.drawable.device_eeg_sensor_t8_white);
//			//noinspection deprecation
//			layers[5] = r.getDrawable(R.drawable.device_eeg_sensor_pz_white);
//			//noinspection deprecation
//			layers[6] = r.getDrawable(R.drawable.device_eeg_sensor_tp7_cms_white);
//		} else {
//			layers[0] = r.getDrawable(R.drawable.device_eeg_sensor_head, null);
//			layers[1] = r.getDrawable(R.drawable.device_eeg_sensor_af3_white, null);
//			layers[2] = r.getDrawable(R.drawable.device_eeg_sensor_af4_white, null);
//			layers[3] = r.getDrawable(R.drawable.device_eeg_sensor_t7_white, null);
//			layers[4] = r.getDrawable(R.drawable.device_eeg_sensor_t8_white, null);
//			layers[5] = r.getDrawable(R.drawable.device_eeg_sensor_pz_white, null);
//			layers[6] = r.getDrawable(R.drawable.device_eeg_sensor_tp7_cms_white, null);
//		}

//		LayerDrawable layerDrawable = new LayerDrawable(layers);
//		imageViewEmotivInsight.setImageDrawable(layerDrawable);


		// TODO
//		imageViewAF3 = v.findViewById(R.id.imageViewEmotivInsightSensorAF3);
//		imageViewAF4 = v.findViewById(R.id.imageViewEmotivInsightSensorAF4);
//		imageViewT7 = v.findViewById(R.id.imageViewEmotivInsightSensorT7);
//		imageViewT8 = v.findViewById(R.id.imageViewEmotivInsightSensorT8);
//		imageViewPz = v.findViewById(R.id.imageViewEmotivInsightSensorPz);
//		imageViewCMS = v.findViewById(R.id.imageViewEmotivInsightSensorCMS);

		drawEEGStatus(0, 0, 0, 0, 0, 0);

		intentEmotivInsight = new Intent(getActivity(), EmotivInsightService.class);


		// Enable to attempt to automatically connect to nearby Emotiv Insight
		// headsets, without the user having to manually press the "Connect" button
//		connectHeadset();


		return v;

	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					  + " must implement OnFragmentInteractionListener");
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

	// ################################################################

	public void onPause() {

		super.onPause();

		LocalBroadcastManager.getInstance(
				  getActivity().getApplicationContext()).unregisterReceiver(
				  mSignalQualityReceiver);

		LocalBroadcastManager.getInstance(
				  getActivity()).unregisterReceiver(
				  mStatusReceiver);

	} // onPause


	// ################################################################

	public void onResume() {

		// Store access variables for window and blank point

		Window window = getDialog().getWindow();

		Point size = new Point();

		// Store dimensions of the screen in `size`
		Display display = window.getWindowManager().getDefaultDisplay();

		display.getSize(size);

//		// Set the width of the dialog proportional to a percentage of the screen width
//		window.setLayout((int) (size.x * 0.98), WindowManager.LayoutParams.WRAP_CONTENT);

		// Set the dimensions  of the dialog proportional to a percentage of the screen dimensions
//		window.setLayout((int) (size.x * 0.95), (int) (size.y * 0.935));

		window.setGravity(Gravity.CENTER);

		// Call super onResume after sizing
		super.onResume();

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				  mSignalQualityReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.signal_quality"));

		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				  mStatusReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.status"));

	}


	// ################################################################

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String name = intent.getStringExtra("name");
			String value = intent.getStringExtra("value");

			if (! name.equals("populateSelectEEG")) {
				Log.d(TAG, "[" + name + "]: " + value);
			}

			switch(name) {

				case "populateSelectEEG":
					populateSelectEEG();
					break;

				case "status":

					if (getActivity() != null) {

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

	// ################################################################

	public void connectHeadset() {

		/*
		 * Called when the "Connect" button is pressed
		 */

		Log.v(TAG, "connectHeadset()");

		if (!DeviceEmotivInsightSingleton.getInstance().lock) {
//			intentEmotivInsight = new Intent(getActivity(), EmotivInsightService.class);
			getActivity().startService(intentEmotivInsight);
		} else {
			disconnectHeadset();
		}

	} // connectHeadset


//	################################################################

	public void disconnectHeadset() {

		/*
		 * Called when "Disconnect" button is pressed
		 */

		Log.v(TAG, "disconnectHeadset()");

		if (DeviceEmotivInsightSingleton.getInstance().lock) {
			DeviceEmotivInsightSingleton.getInstance().connectEmotivInsight(-1);
		}

		getActivity().stopService(intentEmotivInsight);

		// This should actually happen via mStatusReceiver
//		buttonDeviceEnable.setText(getString(R.string.buttonStatusEmotivInsightConnect));

		buttonConnectEEG.setText(getString(R.string.buttonStatusEmotivInsightConnect));


	} // disconnectHeadset


	// ################################################################

	public void onSelectEEGRefresh() {
		Log.i(TAG, "onSelectEEGRefresh()");
	}


	// ################################################################

	public void onSelectEEGItem(Integer deviceNumber) {

		Log.i(TAG, "Selecting Insight: " + deviceNumber);

		if (deviceNumber != -1) {
			DeviceEmotivInsightSingleton.getInstance().connectEmotivInsight(deviceNumber);
		}

		// Dismiss dialog
		DialogInputEmotivInsightSelectEEGFragment mSelectEEG = (DialogInputEmotivInsightSelectEEGFragment) getActivity().getSupportFragmentManager().findFragmentByTag("mSelectEEG");

		if (mSelectEEG != null)
			mSelectEEG.dismiss();

		DeviceEmotivInsightSingleton.getInstance().selectEEGDialogVisible = false;

	}

	// ################################################################

	public void populateSelectEEG() {

		if (!DeviceEmotivInsightSingleton.getInstance().lock) {

			int number = DeviceEmotivInsightSingleton.getInstance().getInsightDeviceCount();

			DialogInputEmotivInsightSelectEEGFragment mSelectEEG = (DialogInputEmotivInsightSelectEEGFragment) getActivity().getSupportFragmentManager().findFragmentByTag("mSelectEEG");
			if (mSelectEEG != null) {
//			Log.e(TAG, "(mSelectEEG != null)");
//			break;
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


	// ################################################################

	public void showSelectEEG() {

		if (!DeviceEmotivInsightSingleton.getInstance().selectEEGDialogVisible) {

			DeviceEmotivInsightSingleton.getInstance().selectEEGDialogVisible = true;

			Log.d(TAG, "showSelectEEG()");

			FragmentManager fm = getActivity().getSupportFragmentManager();
			DialogInputEmotivInsightSelectEEGFragment mSelectEEG = new DialogInputEmotivInsightSelectEEGFragment();
			try {
				mSelectEEG.show(fm, "mSelectEEG");
			} catch (Exception e) {
				Log.e(TAG, "mSelectEEG.show(fm, \"mSelectEEG\"): " + e);
			}
		}

	}

	// ################################################################

	private void drawEEGStatus(int AF3, int AF4, int T7, int T8, int Pz, int CMS) {

		Resources r;

		int scale_x = ConfigurationSingleton.getInstance().displayWidth / 2;
		int scale_y = ConfigurationSingleton.getInstance().displayHeight / 3;


//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
////			Log.e(TAG, "imageViewEmotivInsight.getDrawable().getDirtyBounds(): " + imageViewEmotivInsight.getDrawable().getDirtyBounds());
//			Log.e(TAG, "imageViewEmotivInsight.getDrawable().getIntrinsic*(2): " + imageViewEmotivInsight.getDrawable().getIntrinsicWidth() + ", " + imageViewEmotivInsight.getDrawable().getIntrinsicHeight());
//		}

		try {
			r = getResources();
		} catch (Exception e) {
			Log.e(TAG, "drawEEGStatus: Failure to getResources: " + e);
			return;
		}

		Drawable[] layers = new Drawable[7];
		int drawableID;

//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//			// noinspection deprecation
//			layers[0] = r.getDrawable(R.drawable.device_eeg_sensor_head);
//		} else {
////			layers[0] = r.getDrawable(R.drawable.device_eeg_sensor_head, null);
//			layers[0] = r.getDrawable(R.drawable.device_eeg_sensor_head, null);
//		}

		layers[0] = new BitmapDrawable(decodeSampledBitmapFromResource(r, R.drawable.device_eeg_sensor_head, scale_x, scale_y));


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Log.e(TAG, "layers[0].getDrawable().getIntrinsic*(): " + layers[0].getIntrinsicWidth() + ", " + layers[0].getIntrinsicHeight());
		}


		switch (AF3) {
			case 0:
				drawableID = R.drawable.device_eeg_sensor_af3_white;
				break;
			case 1:
				drawableID = R.drawable.device_eeg_sensor_af3_red;
				break;
			case 2:
				drawableID = R.drawable.device_eeg_sensor_af3_orange;
				break;
			case 4:
				drawableID = R.drawable.device_eeg_sensor_af3_green;
				break;
			default:
				drawableID = R.drawable.device_eeg_sensor_af3_white;
				break;
		}

//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//			//noinspection deprecation
//			layers[1] = r.getDrawable(drawableID);
//		} else {
//			layers[1] = r.getDrawable(drawableID, null);
//		}

		layers[1] = new BitmapDrawable(decodeSampledBitmapFromResource(r, drawableID, scale_x, scale_y));

		// TODO
//		imageViewAF3.setImageResource(drawableID);


		switch (AF4) {
			case 0:
				drawableID = R.drawable.device_eeg_sensor_af4_white;
//				imageViewAF4.setImageResource(R.drawable.device_eeg_sensor_white);
				break;
			case 1:
				drawableID = R.drawable.device_eeg_sensor_af4_red;
//				imageViewAF4.setImageResource(R.drawable.device_eeg_sensor_red);
				break;
			case 2:
				drawableID = R.drawable.device_eeg_sensor_af4_orange;
//				imageViewAF4.setImageResource(R.drawable.device_eeg_sensor_orange);
				break;
			case 4:
				drawableID = R.drawable.device_eeg_sensor_af4_green;
//				imageViewAF4.setImageResource(R.drawable.device_eeg_sensor_green);
				break;
			default:
				drawableID = R.drawable.device_eeg_sensor_af4_white;
//				imageViewAF4.setImageResource(R.drawable.device_eeg_sensor_white);
				break;
		}

//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//			//noinspection deprecation
//			layers[2] = r.getDrawable(drawableID);
//		} else {
//			layers[2] = r.getDrawable(drawableID, null);
//		}

		layers[2] = new BitmapDrawable(decodeSampledBitmapFromResource(r, drawableID, scale_x, scale_y));

		// TODO
//		imageViewAF4.setImageResource(drawableID);


		switch (T7) {
			case 0:
				drawableID = R.drawable.device_eeg_sensor_t7_white;
				break;
			case 1:
				drawableID = R.drawable.device_eeg_sensor_t7_red;
				break;
			case 2:
				drawableID = R.drawable.device_eeg_sensor_t7_orange;
				break;
			case 4:
				drawableID = R.drawable.device_eeg_sensor_t7_green;
				break;
			default:
				drawableID = R.drawable.device_eeg_sensor_t7_white;
				break;
		}

//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//			//noinspection deprecation
//			layers[3] = r.getDrawable(drawableID);
//		} else {
//			layers[3] = r.getDrawable(drawableID, null);
//		}

		layers[3] = new BitmapDrawable(decodeSampledBitmapFromResource(r, drawableID, scale_x, scale_y));

		// TODO
//		imageViewT7.setImageResource(drawableID);

		switch (T8) {
			case 0:
				drawableID = R.drawable.device_eeg_sensor_t8_white;
				break;
			case 1:
				drawableID = R.drawable.device_eeg_sensor_t8_red;
				break;
			case 2:
				drawableID = R.drawable.device_eeg_sensor_t8_orange;
				break;
			case 4:
				drawableID = R.drawable.device_eeg_sensor_t8_green;
				break;
			default:
				drawableID = R.drawable.device_eeg_sensor_t8_white;
				break;
		}

//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//			//noinspection deprecation
//			layers[4] = r.getDrawable(drawableID);
//		} else {
//			layers[4] = r.getDrawable(drawableID, null);
//		}

		layers[4] = new BitmapDrawable(decodeSampledBitmapFromResource(r, drawableID, scale_x, scale_y));

		// TODO
//		imageViewT8.setImageResource(drawableID);


		switch (Pz) {
			case 0:
				drawableID = R.drawable.device_eeg_sensor_pz_white;
				break;
			case 1:
				drawableID = R.drawable.device_eeg_sensor_pz_red;
				break;
			case 2:
				drawableID = R.drawable.device_eeg_sensor_pz_orange;
				break;
			case 4:
				drawableID = R.drawable.device_eeg_sensor_pz_green;
				break;
			default:
				drawableID = R.drawable.device_eeg_sensor_pz_white;
				break;
		}

//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//			//noinspection deprecation
//			layers[5] = r.getDrawable(drawableID);
//		} else {
//			layers[5] = r.getDrawable(drawableID, null);
//		}

		layers[5] = new BitmapDrawable(decodeSampledBitmapFromResource(r, drawableID, scale_x, scale_y));

		// TODO
//		imageViewPz.setImageResource(drawableID);


		switch (CMS) {
			case 0:
				drawableID = R.drawable.device_eeg_sensor_tp7_cms_white;
				break;
			case 1:
				drawableID = R.drawable.device_eeg_sensor_tp7_cms_red;
				break;
			case 2:
				drawableID = R.drawable.device_eeg_sensor_tp7_cms_orange;
				break;
			case 4:
				drawableID = R.drawable.device_eeg_sensor_tp7_cms_green;
				break;
			default:
				drawableID = R.drawable.device_eeg_sensor_tp7_cms_white;
				break;
		}

//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//			//noinspection deprecation
//			layers[6] = r.getDrawable(drawableID);
//		} else {
//			layers[6] = r.getDrawable(drawableID, null);
//		}

		layers[6] = new BitmapDrawable(decodeSampledBitmapFromResource(r, drawableID, scale_x, scale_y));

		// TODO
//		imageViewCMS.setImageResource(drawableID);


		LayerDrawable layerDrawable = new LayerDrawable(layers);

		imageViewEmotivInsight.setImageDrawable(layerDrawable);

	}


	// ################################################################

	private BroadcastReceiver mSignalQualityReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			int AF3 = Integer.valueOf(intent.getStringExtra("AF3"));
			int AF4 = Integer.valueOf(intent.getStringExtra("AF4"));
			int T7 = Integer.valueOf(intent.getStringExtra("T7"));
			int T8 = Integer.valueOf(intent.getStringExtra("T8"));
			int Pz = Integer.valueOf(intent.getStringExtra("Pz"));
			int CMS = Integer.valueOf(intent.getStringExtra("CMS"));

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


// ################################################################

	public void broadcastTileStatus(String value) {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.tile.event");

		intent.putExtra("id", profileID);
		intent.putExtra("name", "active");
		intent.putExtra("value", value);
		intent.putExtra("category", "inputs");

		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

	}


	// ################################################################

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


	// ################################################################

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
