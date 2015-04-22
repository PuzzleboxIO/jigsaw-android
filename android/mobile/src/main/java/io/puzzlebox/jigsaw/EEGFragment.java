package io.puzzlebox.jigsaw;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.neurosky.thinkgear.TGDevice;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;

import io.puzzlebox.jigsaw.data.SessionSingleton;

public class EEGFragment extends Fragment implements
		  SeekBar.OnSeekBarChangeListener {
//		  View.OnClickListener,

	private final static String TAG = EEGFragment.class.getSimpleName();

	private OnFragmentInteractionListener mListener;

	private static View v;

//	private Button connectEEG = null;

	/**
	 * Configuration
	 */

	//	boolean DEBUG = true;
	boolean DEBUG = false;

	int eegAttention = 0;
	int eegMeditation = 0;
	int eegPower = 0;
	int eegSignal = 0;
	boolean eegConnected = false;
	boolean eegConnecting = false;
//	boolean demoFlightMode = false;
	Number[] rawEEG = new Number[512];
	int arrayIndex = 0;


	/**
	 * UI
	 */
//	Configuration config;
	ProgressBar progressBarAttention;
	SeekBar seekBarAttention;
	ProgressBar progressBarMeditation;
	SeekBar seekBarMeditation;
	ProgressBar progressBarSignal;
	ProgressBar progressBarPower;
	Button connectButton;

//	ProgressBar progressBarRange;
//	ProgressBar progressBarBloom;

	ImageView imageViewStatus;

	int[] thresholdValuesAttention = new int[101];
	int[] thresholdValuesMeditation = new int[101];
	int minimumPower = 0; // minimum power for the bloom
	int maximumPower = 100; // maximum power for the bloom

	private final int EEG_RAW_HISTORY_SIZE = 512;            // number of points to plot in EEG history
	private XYPlot eegRawHistoryPlot = null;
	private SimpleXYSeries eegRawHistorySeries = null;



	/**
	 * Bluetooth
	 */
	BluetoothAdapter bluetoothAdapter;
	//	ArrayList<String> pairedBluetoothDevices;


	/**
	 * NeuroSky ThinkGear Device
	 */
	TGDevice tgDevice;
	int tgSignal = 0;
	//	final boolean rawEnabled = false;
	final boolean rawEnabled = true;



	public static EEGFragment newInstance(String param1, String param2) {
		EEGFragment fragment = new EEGFragment();
		Bundle args = new Bundle();
//		args.putString(ARG_PARAM1, param1);
//		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public EEGFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
//			mParam1 = getArguments().getString(ARG_PARAM1);
//			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.fragment_eeg, container, false);

//		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//		setContentView(R.layout.main);
//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

		progressBarAttention = (ProgressBar) v.findViewById(R.id.progressBarAttention);
		final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
		ShapeDrawable progressBarAttentionDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarAttentionColor = "#FF0000";
		progressBarAttentionDrawable.getPaint().setColor(Color.parseColor(progressBarAttentionColor));
		ClipDrawable progressAttention = new ClipDrawable(progressBarAttentionDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarAttention.setProgressDrawable(progressAttention);
		progressBarAttention.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarMeditation = (ProgressBar) v.findViewById(R.id.progressBarMeditation);
		ShapeDrawable progressBarMeditationDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarMeditationColor = "#0000FF";
		progressBarMeditationDrawable.getPaint().setColor(Color.parseColor(progressBarMeditationColor));
		ClipDrawable progressMeditation = new ClipDrawable(progressBarMeditationDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarMeditation.setProgressDrawable(progressMeditation);
		progressBarMeditation.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarSignal = (ProgressBar) v.findViewById(R.id.progressBarSignal);
		ShapeDrawable progressBarSignalDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarSignalColor = "#00FF00";
		progressBarSignalDrawable.getPaint().setColor(Color.parseColor(progressBarSignalColor));
		ClipDrawable progressSignal = new ClipDrawable(progressBarSignalDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarSignal.setProgressDrawable(progressSignal);
		progressBarSignal.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));
		//		progressBarSignal.setProgress(tgSignal);

		progressBarPower = (ProgressBar) v.findViewById(R.id.progressBarPower);
		ShapeDrawable progressBarPowerDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarPowerColor = "#FFFF00";
		progressBarPowerDrawable.getPaint().setColor(Color.parseColor(progressBarPowerColor));
		ClipDrawable progressPower = new ClipDrawable(progressBarPowerDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarPower.setProgressDrawable(progressPower);
		progressBarPower.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));



		// setup the Raw EEG History plot
		eegRawHistoryPlot = (XYPlot) v.findViewById(R.id.eegRawHistoryPlot);
		eegRawHistorySeries = new SimpleXYSeries("Raw EEG");

		// Use index value as xVal, instead of explicit, user provided xVals.
		//		eegRawHistorySeries.useImplicitXVals();

		// Setup the boundary mode, boundary values only applicable in FIXED mode.

		if (eegRawHistoryPlot != null) {

			eegRawHistoryPlot.setDomainBoundaries(0, EEG_RAW_HISTORY_SIZE, BoundaryMode.FIXED);
			//		eegRawHistoryPlot.setDomainBoundaries(0, EEG_RAW_HISTORY_SIZE, BoundaryMode.AUTO);
			//		eegRawHistoryPlot.setRangeBoundaries(-32767, 32767, BoundaryMode.FIXED);
			//		eegRawHistoryPlot.setRangeBoundaries(-32767, 32767, BoundaryMode.AUTO);
			eegRawHistoryPlot.setRangeBoundaries(-256, 256, BoundaryMode.GROW);

			eegRawHistoryPlot.addSeries(eegRawHistorySeries, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.BLACK, null, null));

			// Thin out domain and range tick values so they don't overlap
			eegRawHistoryPlot.setDomainStepValue(5);
			eegRawHistoryPlot.setTicksPerRangeLabel(3);

			//		eegRawHistoryPlot.setRangeLabel("Amplitude");

			// Sets the dimensions of the widget to exactly contain the text contents
			eegRawHistoryPlot.getDomainLabelWidget().pack();
			eegRawHistoryPlot.getRangeLabelWidget().pack();

			// Only display whole numbers in labels
			eegRawHistoryPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
			eegRawHistoryPlot.getGraphWidget().setRangeValueFormat(new DecimalFormat("0"));

			// Hide domain and range labels
			eegRawHistoryPlot.getGraphWidget().setDomainLabelWidth(0);
			eegRawHistoryPlot.getGraphWidget().setRangeLabelWidth(0);

			// Hide legend
			eegRawHistoryPlot.getLegendWidget().setVisible(false);

			// setGridPadding(float left, float top, float right, float bottom)
			eegRawHistoryPlot.getGraphWidget().setGridPadding(0, 0, 0, 0);


			//		eegRawHistoryPlot.getGraphWidget().setDrawMarkersEnabled(false);

			//		final PlotStatistics histStats = new PlotStatistics(1000, false);
			//		eegRawHistoryPlot.addListener(histStats);

		}



		seekBarAttention = (SeekBar) v.findViewById(R.id.seekBarAttention);
		seekBarAttention.setOnSeekBarChangeListener(this);
		seekBarMeditation = (SeekBar) v.findViewById(R.id.seekBarMeditation);
		seekBarMeditation.setOnSeekBarChangeListener(this);


//		imageViewStatus = (ImageView) v.findViewById(R.id.imageViewStatus);



		Button connectEEG = (Button) v.findViewById(R.id.buttonConnectEEG);
		connectEEG.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				connectHeadset();
			}
		});

		Button resetSession = (Button) v.findViewById(R.id.buttonResetSession);
		resetSession.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "SessionSingleton.getInstance().resetSession()");
				SessionSingleton.getInstance().resetSession();

				Toast.makeText((getActivity()),
						  "Session data reset",
						  Toast.LENGTH_SHORT).show();
			}
		});

		Button exportToCSV = (Button) v.findViewById(R.id.buttonExportCSV);
		exportToCSV.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e(TAG, "SessionSingleton.getInstance().exportDataToCSV");
				SessionSingleton.getInstance().exportDataToCSV();

				Toast.makeText((getActivity()),
						  "Session data exported to:\n" + SessionSingleton.getInstance().getTimestampPS4() + ".csv",
						  Toast.LENGTH_LONG).show();
			}
		});


		/**
		 * Prepare Bluetooth and NeuroSky ThinkGear EEG interface
		 */

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (bluetoothAdapter == null) {
			// Alert user that Bluetooth is not available
			// TODO Fix for Fragment Context
//			Toast.makeText(((OrbitTabActivity) getActivity()), "Bluetooth not available", Toast.LENGTH_LONG).show();
			Toast.makeText((getActivity()), "Bluetooth not available", Toast.LENGTH_LONG).show();

		} else {
			/** create the TGDevice */
			tgDevice = new TGDevice(bluetoothAdapter, handlerThinkGear);

			/** Retrieve a list of paired Bluetooth adapters */
			//			Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
			//			pairedBluetoothDevices = new ArrayList<String>(Arrays.asList(pairedDevices.toString()));
			/**
			 * NOTE: To get device names iterate through pairedBluetoothDevices
			 * and call the getName() method on each BluetoothDevice object.
			 */
		}


		/**
		 * Update settings according to default UI
		 */

		updateScreenLayout();

		updatePowerThresholds();
		updatePower();

		return v;

	}

//	public void onButtonPressed(Uri uri) {
//		if (mListener != null) {
//			mListener.onFragmentInteraction(uri);
//		}
//	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					  + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		public void onFragmentInteraction(Uri uri);
	}


	// ################################################################

	public void onPause() {

		Log.v(TAG, "onPause()");

		super.onPause();

		try {

			disconnectHeadset();

		} catch (Exception e) {
			Log.v(TAG, "Exception: onPause()");
			e.printStackTrace();
		}

	} // onPause


	// ################################################################

	@Override
	public void onResume() {
		super.onResume();

//		if (!mBluetoothAdapter.isEnabled()) {
//			Intent enableBtIntent = new Intent(
//					  BluetoothAdapter.ACTION_REQUEST_ENABLE);
//			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//		}
//
//		getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

		if (eegConnected)
			setButtonText(R.id.buttonConnectEEG, "Disconnect EEG");

	}


	@Override
	public void onStop() {
		super.onStop();

//		flag = false;
//
//		getActivity().unregisterReceiver(mGattUpdateReceiver);



		try {

			disconnectHeadset();


		} catch (Exception e) {
			Log.v(TAG, "Exception: onStop()");
			e.printStackTrace();
		}



	}

	@Override
	public void onDestroy() {
		super.onDestroy();

//		if (mServiceConnection != null)
//			getActivity().unbindService(mServiceConnection);


		try {

			if(bluetoothAdapter != null)
				tgDevice.close();

		} catch (Exception e) {
			Log.v(TAG, "Exception: onDestroy()");
			e.printStackTrace();
		}


	}

//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		// User chose not to enable Bluetooth.
//		if (requestCode == REQUEST_ENABLE_BT
//				  && resultCode == Activity.RESULT_CANCELED) {
//			getActivity().finish();
//			return;
//		}
//
//		super.onActivityResult(requestCode, resultCode, data);
//	}


	// ################################################################

	public void updateScreenLayout() {

//		switch(config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK){
//			case Configuration.SCREENLAYOUT_SIZE_SMALL:
//				Log.v(TAG, "screenLayout: small");
//				updateScreenLayoutSmall();
//				break;
//			case Configuration.SCREENLAYOUT_SIZE_NORMAL:
//				Log.v(TAG, "screenLayout: normal");
//				updateScreenLayoutSmall();
//				break;
//			case Configuration.SCREENLAYOUT_SIZE_LARGE:
//				Log.v(TAG, "screenLayout: large");
//				break;
//			case Configuration.SCREENLAYOUT_SIZE_XLARGE:
//				Log.v(TAG, "screenLayout: xlarge");
//				break;
//			case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:
//				Log.v(TAG, "screenLayout: undefined");
//				updateScreenLayoutSmall();
//				break;
//		}

	} // updateScreenLayout


	// ################################################################



	public void setButtonText(int buttonId, String text) {

		/**
		 * Shortcut for changing the text on a button
		 */

		Button button = (Button) v.findViewById(buttonId);
		button.setText(text);

	} // setButtonText


	// ################################################################

	public void updateStatusImage() {

		if(DEBUG) {
			Log.v(TAG, (new StringBuilder("Attention: ")).append(eegAttention).toString());
			Log.v(TAG, (new StringBuilder("Meditation: ")).append(eegMeditation).toString());
			Log.v(TAG, (new StringBuilder("Power: ")).append(eegPower).toString());
			Log.v(TAG, (new StringBuilder("Signal: ")).append(eegSignal).toString());
			Log.v(TAG, (new StringBuilder("Connecting: ")).append(eegConnecting).toString());
			Log.v(TAG, (new StringBuilder("Connected: ")).append(eegConnected).toString());
		}
//
//		if(eegPower > 0) {
//			imageViewStatus.setImageResource(R.drawable.status_4_active);
//			return;
//		}
//
//		if(eegSignal > 90) {
//			imageViewStatus.setImageResource(R.drawable.status_3_processing);
//			return;
//		}
//
//		if(eegConnected) {
//			imageViewStatus.setImageResource(R.drawable.status_2_connected);
//			return;
//		}
//
//		if(eegConnecting) {
//			imageViewStatus.setImageResource(R.drawable.status_1_connecting);
//			return;
//		} else {
//			imageViewStatus.setImageResource(R.drawable.status_default);
//			return;
//		}

	} // updateStatusImage


	// ################################################################

//	public void onClick(View v) {
//
//		Log.e(TAG, "onClick()");
//
//		switch (v.getId()) {
//
//			case R.id.buttonConnectEEG:
//
//// 				connectHeadset(v);
////				connectHeadset();
//
//				if (! eegConnected) {
////					connectHeadset(v);
//					connectHeadset();
//				} else {
//					disconnectHeadset();
//				}
//
//		}
//
//	} // onClick


	// ################################################################

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

		updatePowerThresholds();
		//		updatePower();

	} // onProgressChanged


	// ################################################################

	public void onStartTrackingTouch(SeekBar seekBar) {

		/**
		 * Method required by SeekBar.OnSeekBarChangeListener
		 */


	} // onStartTrackingTouch


	// ################################################################

	public void onStopTrackingTouch(SeekBar seekBar) {

		Log.v(TAG, "onStopTrackingTouch()");


	} // onStopTrackingTouch


	// ################################################################

private final Handler handlerThinkGear = new Handler() {

	/**
	 * Handles data packets from NeuroSky ThinkGear device
	 */

	public void handleMessage(Message msg) {

		parseEEG(msg);

	}

}; // handlerThinkGear


	// ################################################################

	public void parseEEG(Message msg) {

		switch (msg.what) {

			case TGDevice.MSG_STATE_CHANGE:

				switch (msg.arg1) {
					case TGDevice.STATE_IDLE:
						break;
					case TGDevice.STATE_CONNECTING:
						Log.d(TAG, "Connecting to EEG");
						eegConnecting = true;
						eegConnected = false;
						updateStatusImage();
						break;
					case TGDevice.STATE_CONNECTED:
						Log.d(TAG, "EEG Connected");
						setButtonText(R.id.buttonConnectEEG, "Disconnect EEG");
						eegConnecting = false;
						eegConnected = true;
						updateStatusImage();
						SessionSingleton.getInstance().resetSession();
						tgDevice.start();
						break;
					case TGDevice.STATE_NOT_FOUND:
						Log.d(TAG, "EEG headset not found");
						eegConnecting = false;
						eegConnected = false;
						updateStatusImage();
						break;
					case TGDevice.STATE_NOT_PAIRED:
						Log.d(TAG, "EEG headset not paired");
						eegConnecting = false;
						eegConnected = false;
						updateStatusImage();
						break;
					case TGDevice.STATE_DISCONNECTED:
						Log.d(TAG, "EEG Disconnected");
						eegConnecting = false;
						eegConnected = false;
						updateStatusImage();
						disconnectHeadset();
						break;
				}

				break;

			case TGDevice.MSG_POOR_SIGNAL:
				eegSignal = calculateSignal(msg.arg1);
				progressBarSignal.setProgress(eegSignal);
				updateStatusImage();
				processPacketEEG();
				break;
			case TGDevice.MSG_ATTENTION:
				eegAttention = msg.arg1;
				progressBarAttention.setProgress(eegAttention);
				updatePower();
				break;
			case TGDevice.MSG_MEDITATION:
				eegMeditation = msg.arg1;
				if (DEBUG)
					Log.v(TAG, "Meditation: " + eegMeditation);
				progressBarMeditation.setProgress(eegMeditation);
				updatePower();
//				processPacketEEG();
				break;
			case TGDevice.MSG_BLINK:
				/**
				 * Strength of detected blink. The Blink Strength ranges
				 * from 1 (small blink) to 255 (large blink). Unless a blink
				 * occurred, nothing will be returned. Blinks are only
				 * calculated if PoorSignal is less than 51.
				 */
				Log.d(TAG, "Blink: " + msg.arg1 + "\n");
				break;
			case TGDevice.MSG_RAW_DATA:

				rawEEG[arrayIndex] = msg.arg1;
				arrayIndex = arrayIndex + 1;

				if (arrayIndex == EEG_RAW_HISTORY_SIZE - 1)
					updateEEGRawHistory(rawEEG);

				break;
			case TGDevice.MSG_RAW_COUNT:
				break;
			case TGDevice.MSG_RAW_MULTI:
				//TGRawMulti rawM = (TGRawMulti)msg.obj;
				//Log.d(TAG, "Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
			case TGDevice.MSG_HEART_RATE:
				//Log.d(TAG, "Heart rate: " + msg.arg1 + "\n");
				break;
			case TGDevice.MSG_LOW_BATTERY:
				// TODO Fragment Context
				Toast.makeText((getActivity()), "EEG battery low!", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
		}

	} // handleMessage


	// ################################################################

//	public void connectHeadset(View view) {
		public void connectHeadset() {

		/**
		 * Called when the "Connect" button is pressed
		 */

		Log.v(TAG, "connectHeadset()");

		/** Stop audio stream */
		// TODO Fragment Context
//		((OrbitTabActivity)getActivity()).stopControl();
//		( getActivity() ).stopControl();

		if(bluetoothAdapter == null) {

			// Alert user that Bluetooth is not available
			// TODO
//			Toast.makeText(((OrbitTabActivity)getActivity()), "Bluetooth not available", Toast.LENGTH_LONG).show();

		} else {

			if (tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED) {
				tgDevice.connect(rawEnabled);
				// TODO
//				((OrbitTabActivity)getActivity()).maximizeAudioVolume(); // Automatically set media volume to maximum
//				( getActivity() ).maximizeAudioVolume(); // Automatically set media volume to maximum
			}


			else if (tgDevice.getState() == TGDevice.STATE_CONNECTED)
			/** "Disconnect" button was pressed */
				disconnectHeadset();

		}

	} // connectHeadset


	// ################################################################

	public void disconnectHeadset() {

		/**
		 * Called when "Disconnect" button is pressed
		 */

		eegConnecting = false;
		eegConnected = false;

		eegAttention = 0;
		eegMeditation = 0;
		eegSignal = 0;
		eegPower = 0;

		updateStatusImage();

		progressBarAttention.setProgress(eegAttention);
		progressBarMeditation.setProgress(eegMeditation);
		progressBarSignal.setProgress(eegSignal);
		progressBarPower.setProgress(eegPower);

		//TODO Fragment Context
//		String id = ((OrbitTabActivity)getActivity()).getTabFragmentAdvanced();
//
//		FragmentTabAdvanced fragmentAdvanced =
//				  (FragmentTabAdvanced) getFragmentManager().findFragmentByTag(id);
//
//		if (fragmentAdvanced != null) {
//			fragmentAdvanced.progressBarAttention.setProgress(eegAttention);
//			fragmentAdvanced.progressBarMeditation.setProgress(eegMeditation);
//			fragmentAdvanced.progressBarSignal.setProgress(eegSignal);
//			fragmentAdvanced.progressBarPower.setProgress(eegPower);
//		}

		//TODO Broken or missing classes
		setButtonText(R.id.buttonConnectEEG, "Connect");


		if (tgDevice.getState() == TGDevice.STATE_CONNECTED) {
			tgDevice.stop();
			tgDevice.close();

		// TODO Fragment Context
//		((OrbitTabActivity)getActivity()).stopControl();
//		(getActivity()).stopControl();

//			disconnectHeadset();

		}


	} // disconnectHeadset


	// ################################################################

	public int calculateSignal(int signal) {

		/**
		 * The ThinkGear protocol states that a signal level of 200 will be
		 * returned when a clean ground/reference is not detected at the ear clip,
		 *  and a value of 0 when the signal is perfectly clear. We need to
		 *  convert this information into usable settings for the Signal
		 *  progress bar
		 */

		int value;

		switch (signal) {
			case 200:
				value = 0;
			case 0:
				value = 100;
			default:
				value = (int)(100 - ((signal / 200.0) * 100));
		}

		return(value);

	} // calculateSignal


	// ################################################################

	public void updatePowerThresholds() {

		/**
		 * The "Power" level refers to the Puzzlebox Orbit helicopter's
		 * throttle setting. Typically this is an "off" or "on" state,
		 * meaning the helicopter is either flying or not flying at all.
		 * However this method could be used to increase the throttle
		 * or perhaps the forward motion of the helicopter to a level
		 * proportionate to how far past their target brainwave levels
		 * are set (via the progress bar sliders).
		 */

		int power;
		int attentionSeekValue;
		int meditationSeekValue;
		float percentOfMaxPower;

		// Reset all values to zero
		for (int i = 0; i < thresholdValuesAttention.length; i++) {
			thresholdValuesAttention[i] = 0;
			thresholdValuesMeditation[i] = 0;
		}

		attentionSeekValue = seekBarAttention.getProgress();
		if (attentionSeekValue > 0) {
			for (int i = attentionSeekValue; i < thresholdValuesAttention.length; i++) {

				/**
				 *  Slider @ 70
				 *
				 * Attention @ 70
				 * Percentage = 0% ((100-70) - (100-70)) / (100-70)
				 * Power = 60 (minimumPower)
				 *
				 * Slider @ 70
				 * Attention @ 80
				 * Percentage = 33% ((100-70) - (100-80)) / (100-70)
				 * Power = 73
				 *
				 * Slider @ 70
				 * Attention @ 90
				 * Percentage = 66% ((100-70) - (100-90)) / (100-70)
				 * Power = 86
				 *
				 * Slider @ 70
				 * Attention @ 100
				 * Percentage = 100% ((100-70) - (100-100)) / (100-70)
				 * Power = 100
				 */

				percentOfMaxPower = (float)( ((100 - attentionSeekValue) - (100 - i)) / (float)(100 - attentionSeekValue) );
				power = thresholdValuesAttention[i] + (int)( minimumPower + ((maximumPower - minimumPower) * percentOfMaxPower) );
				thresholdValuesAttention[i] = power;

			}
		}

		meditationSeekValue = seekBarMeditation.getProgress();
		if (meditationSeekValue > 0) {
			for (int i = meditationSeekValue; i < thresholdValuesMeditation.length; i++) {
				percentOfMaxPower = (float)( ((100 - meditationSeekValue) - (100 - i)) / (float)(100 - meditationSeekValue) );
				power = thresholdValuesMeditation[i] + (int)( minimumPower + ((maximumPower - minimumPower) * percentOfMaxPower) );
				thresholdValuesMeditation[i] = power;
			}
		}








	} // updatePowerThresholds


	// ################################################################

	public int calculateSpeed() {

		/**
		 * This method is used for calculating whether
		 * or not the "Attention" or "Meditation" levels
		 * are sufficient to trigger the helicopter throttle
		 */

		int attention = progressBarAttention.getProgress();
		int meditation = progressBarMeditation.getProgress();
		int attentionSeekValue = seekBarAttention.getProgress();
		int meditationSeekValue = seekBarMeditation.getProgress();

		int speed = 0;

		if (attention > attentionSeekValue)
			speed = thresholdValuesAttention[attention];
		if (meditation > meditationSeekValue)
			speed = speed + thresholdValuesMeditation[meditation];

		if (speed > maximumPower)
			speed = maximumPower;
		if (speed < minimumPower)
			speed = 0;

		// If control signal is being generated, set the
		// power level equal to the current throttle slider

		// TODO Fragment Context
//		String id = ((OrbitTabActivity)getActivity()).getTabFragmentAdvanced();
//
//		FragmentTabAdvanced fragmentAdvanced =
//				  (FragmentTabAdvanced) getFragmentManager().findFragmentByTag(id);
//
//		if (fragmentAdvanced != null) {
//			if ((fragmentAdvanced.checkBoxGenerateAudio.isChecked()) && (speed > 0)) {
//				speed = fragmentAdvanced.seekBarThrottle.getProgress();
//			}
//		}

		return(speed);

	} // calculateSpeed


	// ################################################################

	//	public int updatePower() {
	public void updatePower() {

		/**
		 * This method updates the power level of the
		 * "Throttle" and triggers the audio stream
		 * which is used to fly the helicopter
		 */

		// Set Attention and Meditation to zero if we've lost signal
		if (eegSignal < 100) {
			eegAttention = 0;
			eegMeditation = 0;
			progressBarAttention.setProgress(eegAttention);
			progressBarMeditation.setProgress(eegMeditation);
		}

		eegPower = calculateSpeed();

		progressBarPower.setProgress(eegPower);


//		processPacketEEG();


//		updateServoPosition();
//		updateBloomRGB();

//		try {
////			Log.e(TAG, "SessionSingleton.getInstance().updateTimestamp");
//			SessionSingleton.getInstance().updateTimestamp();
//
//			HashMap packet = new HashMap();
//
//			packet.put("Date", SessionSingleton.getInstance().getCurrentDate());
//			packet.put("Time", SessionSingleton.getInstance().getCurrentTimestamp());
//			packet.put("Attention", eegAttention);
//			packet.put("Meditation", eegMeditation);
//			packet.put("Signal Level", eegSignal);
//			packet.put("Power", eegPower);
//
//			Log.d(TAG, "SessionSingleton.getInstance().appendData(packet): " + packet.toString());
//			SessionSingleton.getInstance().appendData(packet);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}

		// TODO Fragment Context

//		((OrbitTabActivity)getActivity()).eegPower = eegPower;
//		((OrbitTabActivity)getActivity()).updatePower();
//		(getActivity()).eegPower = eegPower;
//		(getActivity()).updatePower();
//
//
//		String id = ((OrbitTabActivity)getActivity()).getTabFragmentAdvanced();
//
//		FragmentTabAdvanced fragmentAdvanced =
//				  (FragmentTabAdvanced) getFragmentManager().findFragmentByTag(id);
//
//		if (fragmentAdvanced != null) {
//			fragmentAdvanced.progressBarAttention.setProgress(eegAttention);
//			fragmentAdvanced.progressBarMeditation.setProgress(eegMeditation);
//			fragmentAdvanced.progressBarSignal.setProgress(eegSignal);
//			fragmentAdvanced.progressBarPower.setProgress(eegPower);
//		}


	} // updatePower


	// ################################################################


	public void processPacketEEG() {
		try {
//			Log.e(TAG, "SessionSingleton.getInstance().updateTimestamp");
			SessionSingleton.getInstance().updateTimestamp();

			HashMap packet = new HashMap();

			packet.put("Date", SessionSingleton.getInstance().getCurrentDate());
			packet.put("Time", SessionSingleton.getInstance().getCurrentTimestamp());
//			packet.put("Attention", eegAttention);
			packet.put("Attention", String.valueOf(eegAttention));
//			packet.put("Meditation", eegMeditation);
			packet.put("Meditation", String.valueOf(eegMeditation));
//			packet.put("Signal Level", eegSignal);
			packet.put("Signal Level", String.valueOf(eegSignal));
//			packet.put("Power", eegPower);
			packet.put("Power", String.valueOf(eegPower));

			Log.d(TAG, "SessionSingleton.getInstance().appendData(packet): " + packet.toString());
			SessionSingleton.getInstance().appendData(packet);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	// ################################################################

	public void updateEEGRawHistory(Number[] rawEEG) {

		if (eegRawHistoryPlot != null) {
			eegRawHistoryPlot.removeSeries(eegRawHistorySeries);

			eegRawHistorySeries = new SimpleXYSeries(Arrays.asList(rawEEG), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Raw EEG");

			//		LineAndPointFormatter format = new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.BLACK, null, null);
			//		LineAndPointFormatter format = new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.TRANSPARENT, null, null);
			LineAndPointFormatter format = new LineAndPointFormatter(Color.rgb(0, 0, 0), Color.TRANSPARENT, null, null);

			//		format.getFillPaint().setAlpha(220);

			eegRawHistoryPlot.addSeries(eegRawHistorySeries, format);


			// redraw the Plots:
			eegRawHistoryPlot.redraw();

			rawEEG = new Number[512];
			arrayIndex = 0;
		}

	} // updateEEGRawHistory




}