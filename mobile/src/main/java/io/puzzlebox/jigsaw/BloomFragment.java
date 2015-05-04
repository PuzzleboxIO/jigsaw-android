/**
 * Puzzlebox Jigsaw
 * Copyright 2015 Puzzlebox Productions, LLC
 * License: GNU Affero General Public License Version 3
 */

package io.puzzlebox.jigsaw;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.neurosky.thinkgear.TGDevice;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.puzzlebox.jigsaw.data.BloomSingleton;
import io.puzzlebox.jigsaw.data.SessionSingleton;
import io.puzzlebox.jigsaw.protocol.MuseService;
import io.puzzlebox.jigsaw.protocol.RBLGattAttributes;
import io.puzzlebox.jigsaw.protocol.RBLService;
import io.puzzlebox.jigsaw.protocol.ThinkGearService;

import static android.view.MenuItem.SHOW_AS_ACTION_ALWAYS;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BloomFragment extends Fragment
		  implements SeekBar.OnSeekBarChangeListener {
//		  implements View.OnClickListener,

	private final static String TAG = BloomFragment.class.getSimpleName();

	private OnFragmentInteractionListener mListener;

//	private static XYPlot sessionPlot1 = null;
//	private static SimpleXYSeries sessionPlotSeries1 = null;
//	private static XYPlot sessionPlot2 = null;
//	private static SimpleXYSeries sessionPlotSeries2 = null;

	View v;

	private Button connectBloom = null;
	private Button connectEEG = null;

	private Button buttonDemo = null;
	private Button buttonOpen = null;
	private Button buttonClose = null;

	private TextView rssiValue = null;
	private SeekBar servoSeekBar;

	private static TextView textViewSessionTime;

//	private BluetoothGattCharacteristic characteristicTx = null;
//	private RBLService mBluetoothLeService;
//	private BluetoothAdapter mBluetoothAdapter;
//	private BluetoothDevice mDevice = null;
//	private String mDeviceAddress;
//
//	private boolean flag = true;
//	private boolean connState = false;
//	private boolean scanFlag = false;
//
//	private byte[] data = new byte[3];
//	private static final int REQUEST_ENABLE_BT = 1;
//	private static final long SCAN_PERIOD = 2000;
//
//	final private static char[] hexArray = { '0', '1', '2', '3', '4', '5', '6',
//			  '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };


	/**
	 * Configuration
	 */
	int eegPower = 0;

	int bloomRange = 0;
	int bloomRangeMax = 128;
	int bloomServoPercentage = 0;
	int bloomColorRed = 0;
	int bloomColorGreen = 0;
	int bloomColorBlue = 0;


	/**
	 * UI
	 */
	Configuration config;
	ProgressBar progressBarAttention;
	SeekBar seekBarAttention;
	ProgressBar progressBarMeditation;
	SeekBar seekBarMeditation;
	ProgressBar progressBarSignal;
	ProgressBar progressBarPower;
	Button connectButton;

	ProgressBar progressBarRange;
	ProgressBar progressBarBloom;

	ImageView imageViewStatus;

	int[] thresholdValuesAttention = new int[101];
	int[] thresholdValuesMeditation = new int[101];
	int minimumPower = 0; // minimum power for the bloom
	int maximumPower = 100; // maximum power for the bloom


	// ################################################################

	public static BloomFragment newInstance() {
		BloomFragment fragment = new BloomFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}


	// ################################################################

	public BloomFragment() {
		// Required empty public constructor
	}


	// ################################################################

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	// ################################################################

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment

		v = inflater.inflate(R.layout.fragment_bloom, container, false);

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

		progressBarRange = (ProgressBar) v.findViewById(R.id.progressBarRange);
//		ShapeDrawable progressBarRangeDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		ShapeDrawable progressBarRangeDrawable = new ShapeDrawable();
//		String progressBarRangeColor = "#FF00FF";
		String progressBarRangeColor = "#990099";
		progressBarRangeDrawable.getPaint().setColor(Color.parseColor(progressBarRangeColor));
		ClipDrawable progressRange = new ClipDrawable(progressBarRangeDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarRange.setProgressDrawable(progressRange);
		progressBarRange.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

//		progressBarRange.setMax(128 + 127);
		progressBarRange.setMax(bloomRangeMax);


//		progressBarBloom = (ProgressBar) v.findViewById(R.id.progressBarBloom);
//		ShapeDrawable progressBarBloomDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
//		String progressBarBloomColor = "#7F0000";
//		progressBarBloomDrawable.getPaint().setColor(Color.parseColor(progressBarBloomColor));
//		ClipDrawable progressBloom = new ClipDrawable(progressBarBloomDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
//		progressBarBloom.setProgressDrawable(progressBloom);
//		progressBarBloom.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));


//		// setup the Raw EEG History plot
//		eegRawHistoryPlot = (XYPlot) v.findViewById(R.id.eegRawHistoryPlot);
//		eegRawHistorySeries = new SimpleXYSeries("Raw EEG");
//
//		// Use index value as xVal, instead of explicit, user provided xVals.
//		//		eegRawHistorySeries.useImplicitXVals();
//
//		// Setup the boundary mode, boundary values only applicable in FIXED mode.
//
//		if (eegRawHistoryPlot != null) {
//
//			eegRawHistoryPlot.setDomainBoundaries(0, EEG_RAW_HISTORY_SIZE, BoundaryMode.FIXED);
//			//		eegRawHistoryPlot.setDomainBoundaries(0, EEG_RAW_HISTORY_SIZE, BoundaryMode.AUTO);
//			//		eegRawHistoryPlot.setRangeBoundaries(-32767, 32767, BoundaryMode.FIXED);
//			//		eegRawHistoryPlot.setRangeBoundaries(-32767, 32767, BoundaryMode.AUTO);
//			eegRawHistoryPlot.setRangeBoundaries(-256, 256, BoundaryMode.GROW);
//
//			eegRawHistoryPlot.addSeries(eegRawHistorySeries, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.BLACK, null, null));
//
//			// Thin out domain and range tick values so they don't overlap
//			eegRawHistoryPlot.setDomainStepValue(5);
//			eegRawHistoryPlot.setTicksPerRangeLabel(3);
//
//			//		eegRawHistoryPlot.setRangeLabel("Amplitude");
//
//			// Sets the dimensions of the widget to exactly contain the text contents
//			eegRawHistoryPlot.getDomainLabelWidget().pack();
//			eegRawHistoryPlot.getRangeLabelWidget().pack();
//
//			// Only display whole numbers in labels
//			eegRawHistoryPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
//			eegRawHistoryPlot.getGraphWidget().setRangeValueFormat(new DecimalFormat("0"));
//
//			// Hide domain and range labels
//			eegRawHistoryPlot.getGraphWidget().setDomainLabelWidth(0);
//			eegRawHistoryPlot.getGraphWidget().setRangeLabelWidth(0);
//
//			// Hide legend
//			eegRawHistoryPlot.getLegendWidget().setVisible(false);
//
//			// setGridPadding(float left, float top, float right, float bottom)
//			eegRawHistoryPlot.getGraphWidget().setGridPadding(0, 0, 0, 0);
//
//
//			//		eegRawHistoryPlot.getGraphWidget().setDrawMarkersEnabled(false);
//
//			//		final PlotStatistics histStats = new PlotStatistics(1000, false);
//			//		eegRawHistoryPlot.addListener(histStats);
//
//		}


		seekBarAttention = (SeekBar) v.findViewById(R.id.seekBarAttention);
		seekBarAttention.setOnSeekBarChangeListener(this);
		seekBarMeditation = (SeekBar) v.findViewById(R.id.seekBarMeditation);
		seekBarMeditation.setOnSeekBarChangeListener(this);


//		imageViewStatus = (ImageView) v.findViewById(R.id.imageViewStatus);


		servoSeekBar = (SeekBar) v.findViewById(R.id.ServoSeekBar);
		servoSeekBar.setEnabled(false);
//		servoSeekBar.setMax(180);
		servoSeekBar.setMax(100);
		servoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
			                              boolean fromUser) {
				byte[] buf = new byte[] { (byte) 0x03, (byte) 0x00, (byte) 0x00 };

				buf[1] = (byte) servoSeekBar.getProgress();

				BloomSingleton.getInstance().characteristicTx.setValue(buf);
				BloomSingleton.getInstance().mBluetoothLeService.writeCharacteristic(BloomSingleton.getInstance().characteristicTx);
			}
		});


//		rssiValue = (TextView) v.findViewById(R.id.rssiValue);

		connectBloom = (Button) v.findViewById(R.id.connectBloom);
		if (BloomSingleton.getInstance().connState)
			setButtonEnable();


		connectBloom.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!BloomSingleton.getInstance().scanFlag) {
					scanLeDevice();

					Timer mTimer = new Timer();
					mTimer.schedule(new TimerTask() {

						@Override
						public void run() {
							if (BloomSingleton.getInstance().mDevice != null) {
								BloomSingleton.getInstance().mDeviceAddress = BloomSingleton.getInstance().mDevice.getAddress();
								BloomSingleton.getInstance().mBluetoothLeService.connect(BloomSingleton.getInstance().mDeviceAddress);
								BloomSingleton.getInstance().scanFlag = true;
							} else {
								getActivity().runOnUiThread(new Runnable() {
									public void run() {
										Toast toast = Toast
												  .makeText(
															 getActivity(),
															 "Error connecting to Puzzlebox Bloom",
															 Toast.LENGTH_SHORT);
										toast.setGravity(0, 0, Gravity.CENTER);
										toast.show();
									}
								});
							}
						}
					}, BloomSingleton.getInstance().SCAN_PERIOD);
				}

				System.out.println(BloomSingleton.getInstance().connState);
//				Log.e(TAG, connState);
//				if (connState == false) {
				if (BloomSingleton.getInstance().connState == false && BloomSingleton.getInstance().mDeviceAddress != null) {
					BloomSingleton.getInstance().mBluetoothLeService.connect(BloomSingleton.getInstance().mDeviceAddress);
				} else {
					if (BloomSingleton.getInstance().mBluetoothLeService != null) {
						BloomSingleton.getInstance().mBluetoothLeService.disconnect();
						BloomSingleton.getInstance().mBluetoothLeService.close();
						setButtonDisable();
					}
				}
			}
		});


		buttonOpen = (Button) v.findViewById(R.id.buttonOpen);
		buttonOpen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				byte[] buf = new byte[] { (byte) 0x01, (byte) 0x00, (byte) 0x00 };
				BloomSingleton.getInstance().characteristicTx.setValue(buf);
				BloomSingleton.getInstance().mBluetoothLeService.writeCharacteristic(BloomSingleton.getInstance().characteristicTx);
			}
		});
		buttonOpen.setVisibility(View.GONE);

		buttonClose = (Button) v.findViewById(R.id.buttonClose);
		buttonClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				byte[] buf = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00 };
				BloomSingleton.getInstance().characteristicTx.setValue(buf);
				BloomSingleton.getInstance().mBluetoothLeService.writeCharacteristic(BloomSingleton.getInstance().characteristicTx);
			}
		});
		buttonClose.setVisibility(View.GONE);

		buttonDemo = (Button) v.findViewById(R.id.buttonDemo);
		buttonDemo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				byte[] buf;
//				if (! BloomSingleton.getInstance().demoActive) {
				BloomSingleton.getInstance().demoActive = true;

				// bloomOpen()
//				buf = new byte[]{(byte) 0x01, (byte) 0x00, (byte) 0x00};
//				BloomSingleton.getInstance().characteristicTx.setValue(buf);
//				BloomSingleton.getInstance().mBluetoothLeService.writeCharacteristic(BloomSingleton.getInstance().characteristicTx);

				// loopRGB()
				buf = new byte[]{(byte) 0x06, (byte) 0x00, (byte) 0x00};
				BloomSingleton.getInstance().characteristicTx.setValue(buf);
				BloomSingleton.getInstance().mBluetoothLeService.writeCharacteristic(BloomSingleton.getInstance().characteristicTx);

				// Set Red to 0
				buf = new byte[]{(byte) 0x0A, (byte) 0x00, (byte) 0x00}; // R = 0
				BloomSingleton.getInstance().characteristicTx.setValue(buf);
				BloomSingleton.getInstance().mBluetoothLeService.writeCharacteristic(BloomSingleton.getInstance().characteristicTx);

				// bloomClose()
//				buf = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00};
//				BloomSingleton.getInstance().characteristicTx.setValue(buf);
//				BloomSingleton.getInstance().mBluetoothLeService.writeCharacteristic(BloomSingleton.getInstance().characteristicTx);


//				} else {
//					BloomSingleton.getInstance().demoActive = false;
////					buf = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00};
////					BloomSingleton.getInstance().characteristicTx.setValue(buf);
////					BloomSingleton.getInstance().mBluetoothLeService.writeCharacteristic(BloomSingleton.getInstance().characteristicTx);
//					buf = new byte[]{(byte) 0x0A, (byte) 0x00, (byte) 0x00}; // R = 0
//					BloomSingleton.getInstance().characteristicTx.setValue(buf);
//					BloomSingleton.getInstance().mBluetoothLeService.writeCharacteristic(BloomSingleton.getInstance().characteristicTx);
////					buf = new byte[]{(byte) 0x0A, (byte) 0x01, (byte) 0x00}; // G = 0
////					BloomSingleton.getInstance().characteristicTx.setValue(buf);
////					BloomSingleton.getInstance().mBluetoothLeService.writeCharacteristic(BloomSingleton.getInstance().characteristicTx);
////					buf = new byte[]{(byte) 0x0A, (byte) 0x02, (byte) 0x00}; // B = 0
////					BloomSingleton.getInstance().characteristicTx.setValue(buf);
////					BloomSingleton.getInstance().mBluetoothLeService.writeCharacteristic(BloomSingleton.getInstance().characteristicTx);
//				}
			}
		});


		if (!getActivity().getPackageManager().hasSystemFeature(
				  PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(getActivity(), "Bluetooth LE not supported", Toast.LENGTH_SHORT)
					  .show();
			getActivity().finish();
		}

		final BluetoothManager mBluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
		BloomSingleton.getInstance().mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (BloomSingleton.getInstance().mBluetoothAdapter == null) {
			Toast.makeText(getActivity(), "Bluetooth LE not supported", Toast.LENGTH_SHORT)
					  .show();
			getActivity().finish();
			return v;
		}

		Intent gattServiceIntent = new Intent(getActivity(),
				  RBLService.class);
//		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		getActivity().bindService(gattServiceIntent, mServiceConnection, getActivity().BIND_AUTO_CREATE);


//		// setup the Session History plot
//		sessionPlot1 = (XYPlot) v.findViewById(R.id.sessionPlot1);
//		sessionPlotSeries1 = new SimpleXYSeries("Session Plot");
//
//		// Setup the boundary mode, boundary values only applicable in FIXED mode.
//
//		if (sessionPlot1 != null) {
//
//			sessionPlot1.setDomainBoundaries(0, 30, BoundaryMode.FIXED);
////			sessionPlot1.setRangeBoundaries(0, 100, BoundaryMode.GROW);
//			sessionPlot1.setRangeBoundaries(0, 100, BoundaryMode.FIXED);
//
////			sessionPlot1.addSeries(sessionPlotSeries1, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.BLACK, null, null));
//			sessionPlot1.addSeries(sessionPlotSeries1, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.RED, null, null));
//
//			// Thin out domain and range tick values so they don't overlap
//			sessionPlot1.setDomainStepValue(1);
//			sessionPlot1.setTicksPerRangeLabel(10);
//
//			sessionPlot1.setRangeLabel("Attention");
//
//			// Sets the dimensions of the widget to exactly contain the text contents
//			sessionPlot1.getDomainLabelWidget().pack();
//			sessionPlot1.getRangeLabelWidget().pack();
//
//			// Only display whole numbers in labels
//			sessionPlot1.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
//			sessionPlot1.getGraphWidget().setRangeValueFormat(new DecimalFormat("0"));
//
//			// Hide domain and range labels
//			sessionPlot1.getGraphWidget().setDomainLabelWidth(0);
//			sessionPlot1.getGraphWidget().setRangeLabelWidth(0);
//
//			// Hide legend
//			sessionPlot1.getLegendWidget().setVisible(false);
//
//			// setGridPadding(float left, float top, float right, float bottom)
//			sessionPlot1.getGraphWidget().setGridPadding(0, 0, 0, 0);
//
//
//			//		sessionPlot1.getGraphWidget().setDrawMarkersEnabled(false);
//
//			//		final PlotStatistics histStats = new PlotStatistics(1000, false);
//			//		sessionPlot1.addListener(histStats);
//
//		}
//
//
//
//		// setup the Session History plot
//		sessionPlot2 = (XYPlot) v.findViewById(R.id.sessionPlot2);
//		sessionPlotSeries2 = new SimpleXYSeries("Session Plot");
//
//		// Setup the boundary mode, boundary values only applicable in FIXED mode.
//
//		if (sessionPlot2 != null) {
//
//			sessionPlot2.setDomainBoundaries(0, 30, BoundaryMode.FIXED);
////			sessionPlot2.setRangeBoundaries(0, 100, BoundaryMode.GROW);
//			sessionPlot2.setRangeBoundaries(0, 100, BoundaryMode.FIXED);
//
////			sessionPlot2.addSeries(sessionPlotSeries2, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.BLACK, null, null));
//			sessionPlot2.addSeries(sessionPlotSeries2, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.RED, null, null));
//
//			// Thin out domain and range tick values so they don't overlap
//			sessionPlot2.setDomainStepValue(1);
//			sessionPlot2.setTicksPerRangeLabel(10);
//
//			sessionPlot2.setRangeLabel("Meditation");
//
//			// Sets the dimensions of the widget to exactly contain the text contents
//			sessionPlot2.getDomainLabelWidget().pack();
//			sessionPlot2.getRangeLabelWidget().pack();
//
//			// Only display whole numbers in labels
//			sessionPlot2.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
//			sessionPlot2.getGraphWidget().setRangeValueFormat(new DecimalFormat("0"));
//
//			// Hide domain and range labels
//			sessionPlot2.getGraphWidget().setDomainLabelWidth(0);
//			sessionPlot2.getGraphWidget().setRangeLabelWidth(0);
//
//			// Hide legend
//			sessionPlot2.getLegendWidget().setVisible(false);
//
//			// setGridPadding(float left, float top, float right, float bottom)
//			sessionPlot2.getGraphWidget().setGridPadding(0, 0, 0, 0);
//
//
//			//		sessionPlot2.getGraphWidget().setDrawMarkersEnabled(false);
//
//			//		final PlotStatistics histStats = new PlotStatistics(1000, false);
//			//		sessionPlot2.addListener(histStats);
//
//		}


		/**
		 * Update settings according to default UI
		 */

		updateScreenLayout();

//		updatePowerThresholds();
//		updatePower();

		return v;

	}


	// ################################################################

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


	// ################################################################

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}


	// ################################################################

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


		LocalBroadcastManager.getInstance(
				  getActivity().getApplicationContext()).unregisterReceiver(
				  mPacketReceiver);


	} // onPause


	// ################################################################

	@Override
	public void onResume() {
		super.onResume();

		updatePowerThresholds();
		updatePower();

		if (!BloomSingleton.getInstance().mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					  BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, BloomSingleton.getInstance().REQUEST_ENABLE_BT);
		}

		getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

//		if (BloomSingleton.getInstance().connState)
//			setButtonText(R.id.connectBloom, "Disconnect Bloom");

//		updateSessionTime();

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				  mPacketReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.packet"));


	}


	// ################################################################

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		menu.add("Share")
				  .setOnMenuItemClickListener(this.mShareButtonClickListener)
				  .setIcon(android.R.drawable.ic_menu_share)
				  .setShowAsAction(SHOW_AS_ACTION_ALWAYS);

		super.onCreateOptionsMenu(menu, inflater);

	}


	// ################################################################

	MenuItem.OnMenuItemClickListener mShareButtonClickListener = new MenuItem.OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {

			Intent i = SessionSingleton.getInstance().getExportSessionIntent(getActivity().getApplicationContext(), item);

			if (i != null) {
				startActivity(i);
			} else {
				Toast.makeText(getActivity().getApplicationContext(), "Error export session data for sharing", Toast.LENGTH_SHORT).show();
			}

			return false;
		}
	};


	// ################################################################

//	private void resetSession() {
//
//		Log.d(TAG, "SessionSingleton.getInstance().resetSession()");
//		SessionSingleton.getInstance().resetSession();
//
//		textViewSessionTime.setText( R.string.session_time );
//
//		Toast.makeText((getActivity().getApplicationContext()),
//				  "Session data reset",
//				  Toast.LENGTH_SHORT).show();
//
//	}


	// ################################################################

	private BroadcastReceiver mPacketReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			int eegAttention = Integer.valueOf(intent.getStringExtra("Attention"));
			int eegMeditation = Integer.valueOf(intent.getStringExtra("Meditation"));
			int eegSignal = Integer.valueOf(intent.getStringExtra("Signal Level"));

//			Log.e(TAG, "eegAttention: " + eegAttention);

			progressBarAttention.setProgress(eegAttention);
			progressBarMeditation.setProgress(eegMeditation);
			progressBarSignal.setProgress(eegSignal);

			updatePower();


//			updateSessionTime();

//			sessionPlotSeries1 = updateSessionPlotHistory(
//					  "Attention",
//					  SessionSingleton.getInstance().getSessionRangeValues(
//								 "Attention", 30),
//					  Color.RED,
//					  sessionPlot1,
//					  sessionPlotSeries1);
////			updateSessionPlotHistory(
////					  SessionSingleton.getInstance().getSessionRangeValues(
////								 "Attention", 30));
//
////			updateSessionPlotHistory2(
////					  SessionSingleton.getInstance().getSessionRangeValues(
////								 "Meditation", 30));
//
//			sessionPlotSeries2 = updateSessionPlotHistory(
//					  "Meditation",
//					  SessionSingleton.getInstance().getSessionRangeValues(
//								 "Meditation", 30),
//					  Color.BLUE,
//					  sessionPlot2,
//					  sessionPlotSeries2);


		}

	};


	// ################################################################

//	private void updateSessionTime() {
//
//		textViewSessionTime.setText( SessionSingleton.getInstance().getSessionTimestamp() );
//
//	}


	// ################################################################

//	public SimpleXYSeries updateSessionPlotHistory(String name,
//	                                               Number[] values,
//	                                               Integer color,
//	                                               XYPlot mPlot,
//	                                               SimpleXYSeries mSeries) {
//
////		if (sessionPlot1 != null) {
////			sessionPlot1.removeSeries(sessionPlotSeries1);
//
//		if (mPlot != null) {
//			mPlot.removeSeries(mSeries);
//
//			mSeries = new SimpleXYSeries(Arrays.asList(values), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, name);
//
//			LineAndPointFormatter format = new LineAndPointFormatter(color, color, null, null);
//
//			//		format.getFillPaint().setAlpha(220);
//
//			mPlot.addSeries(mSeries, format);
//
//
//			// Redraw the plots:
//			mPlot.redraw();
//
//			return mSeries;
//		} else
//			return null;
//
//	} // updateSessionPlotHistory


	// ################################################################

	private void displayData(String data) {
		if (data != null) {
//			rssiValue.setText(data);

			try{
//				progressBarRange.setProgress( Integer.parseInt(data) );

				// -128 to 127 [https://stackoverflow.com/questions/21609544/bluetooth-rssi-values-are-always-in-dbm-in-all-android-devices]
				bloomRange = Integer.parseInt(data);

				bloomRange = bloomRange + 128;

				if (bloomRange > bloomRangeMax)
					bloomRange = bloomRangeMax;

				progressBarRange.setProgress( bloomRange );

			} catch (Exception e) {
				Log.e(TAG, "Exception: displayData(" + data + ")" + e.toString());
			}

		}
	}


	// ################################################################

//	private void readAnalogInValue(byte[] data) {
//		for (int i = 0; i < data.length; i += 3) {
//			if (data[i] == 0x0A) {
//				if (data[i + 1] == 0x01)
//					digitalInBtn.setChecked(false);
//				else
//					digitalInBtn.setChecked(true);
//			} else if (data[i] == 0x0B) {
//				int Value;
//
//				Value = ((data[i + 1] << 8) & 0x0000ff00)
//						  | (data[i + 2] & 0x000000ff);
//
//				AnalogInValue.setText(Value + "");
//			}
//		}
//	}


	// ################################################################

	private void setButtonEnable() {
		BloomSingleton.getInstance().flag = true;
		BloomSingleton.getInstance().connState = true;

		servoSeekBar.setEnabled(BloomSingleton.getInstance().flag);
		connectBloom.setText("Disconnect Bloom");

		buttonDemo.setEnabled(true);
	}


	// ################################################################

	private void setButtonDisable() {
		BloomSingleton.getInstance().flag = false;
		BloomSingleton.getInstance().connState = false;

		servoSeekBar.setEnabled(BloomSingleton.getInstance().flag);
		connectBloom.setText("Connect Bloom");

		buttonDemo.setEnabled(false);

		progressBarRange.setProgress(0);
	}


	// ################################################################

	private void startReadRssi() {
		new Thread() {
			public void run() {

				while (BloomSingleton.getInstance().flag) {
					BloomSingleton.getInstance().mBluetoothLeService.readRssi();
					try {
						sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}


	// ################################################################

	private void getGattService(BluetoothGattService gattService) {
		if (gattService == null)
			return;

		setButtonEnable();
		startReadRssi();

		BloomSingleton.getInstance().characteristicTx = gattService
				  .getCharacteristic(RBLService.UUID_BLE_SHIELD_TX);

		BluetoothGattCharacteristic characteristicRx = gattService
				  .getCharacteristic(RBLService.UUID_BLE_SHIELD_RX);
		BloomSingleton.getInstance().mBluetoothLeService.setCharacteristicNotification(characteristicRx,
				  true);
		BloomSingleton.getInstance().mBluetoothLeService.readCharacteristic(characteristicRx);
	}


	// ################################################################

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(RBLService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(RBLService.ACTION_GATT_RSSI);

		return intentFilter;
	}


	// ################################################################

	private void scanLeDevice() {
		new Thread() {

			@Override
			public void run() {
				BloomSingleton.getInstance().mBluetoothAdapter.startLeScan(mLeScanCallback);

				try {
					Thread.sleep(BloomSingleton.getInstance().SCAN_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				BloomSingleton.getInstance().mBluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}.start();
	}


	// ################################################################

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
		                     final byte[] scanRecord) {

			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					byte[] serviceUuidBytes = new byte[16];
					String serviceUuid = "";
					for (int i = 32, j = 0; i >= 17; i--, j++) {
						serviceUuidBytes[j] = scanRecord[i];
					}
					serviceUuid = bytesToHex(serviceUuidBytes);
					if (stringToUuidString(serviceUuid).equals(
							  RBLGattAttributes.BLE_SHIELD_SERVICE
										 .toUpperCase(Locale.ENGLISH))) {
						BloomSingleton.getInstance().mDevice = device;
					}
				}
			});
		}
	};


	// ################################################################

	private String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = BloomSingleton.getInstance().hexArray[v >>> 4];
			hexChars[j * 2 + 1] = BloomSingleton.getInstance().hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}


	// ################################################################

	private String stringToUuidString(String uuid) {
		StringBuffer newString = new StringBuffer();
		newString.append(uuid.toUpperCase(Locale.ENGLISH).substring(0, 8));
		newString.append("-");
		newString.append(uuid.toUpperCase(Locale.ENGLISH).substring(8, 12));
		newString.append("-");
		newString.append(uuid.toUpperCase(Locale.ENGLISH).substring(12, 16));
		newString.append("-");
		newString.append(uuid.toUpperCase(Locale.ENGLISH).substring(16, 20));
		newString.append("-");
		newString.append(uuid.toUpperCase(Locale.ENGLISH).substring(20, 32));

		return newString.toString();
	}


	// ################################################################

//	@Override
//	public void onStop() {
//		super.onStop();
//
//		BloomSingleton.getInstance().flag = false;
//
//		getActivity().unregisterReceiver(mGattUpdateReceiver);
//
//	}


	// ################################################################

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mServiceConnection != null)
			getActivity().unbindService(mServiceConnection);


	}


	// ################################################################

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == BloomSingleton.getInstance().REQUEST_ENABLE_BT
				  && resultCode == Activity.RESULT_CANCELED) {
			getActivity().finish();
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}


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

//	public void updateScreenLayoutSmall() {
//
////		String button_test_fly_small = getResources().getString(R.string.button_test_fly_small);
////		setButtonText(R.id.buttonTestFly, button_test_fly_small);
////
////		textViewLabelScores.setVisibility(View.VISIBLE);
////		viewSpaceScore.setVisibility(View.VISIBLE);
//
//
//		ViewGroup.LayoutParams layoutParams;
//
////		layoutParams = (android.view.ViewGroup.LayoutParams) viewSpaceScoreLast.getLayoutParams();
////		layoutParams.width = 10;
////		viewSpaceScoreLast.setLayoutParams(layoutParams);
////
////		layoutParams = (android.view.ViewGroup.LayoutParams) viewSpaceScoreHigh.getLayoutParams();
////		layoutParams.width = 10;
////		viewSpaceScoreHigh.setLayoutParams(layoutParams);
////
////
////		String labelScore = getResources().getString(R.string.textview_label_score_small);
////		textViewLabelScore.setText(labelScore);
////
////		String labelLastScore = getResources().getString(R.string.textview_label_last_score_small);
////		textViewLabelLastScore.setText(labelLastScore);
////
////		String labelHighScore = getResources().getString(R.string.textview_label_high_score_small);
////		textViewLabelHighScore.setText(labelHighScore);
//
//
////		// HTC Droid DNA - AndroidPlot has issues with OpenGL Render
////		if ((Build.MANUFACTURER.contains("HTC")) &&
////				  (Build.MODEL.contains("HTC6435LVW"))) {
////
////			Log.v(TAG, "Device detected: HTC Droid DNA");
////			hideEEGRawHistory();
////
////		}
//
//
//	} // updateScreenLayoutSmall


	// ################################################################

//	public void setButtonText(int buttonId, String text) {
//
//		/**
//		 * Shortcut for changing the text on a button
//		 */
//
//		Button button = (Button) v.findViewById(buttonId);
//		button.setText(text);
//
//	} // setButtonText


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

	public void updatePower() {

		/**
		 * This method updates the power level of the
		 * "Throttle" and triggers the audio stream
		 * which is used to fly the helicopter
		 */

		if (ThinkGearService.eegConnected) {

			if (ThinkGearService.eegSignal < 100) {
				ThinkGearService.eegAttention = 0;
				ThinkGearService.eegMeditation = 0;
				progressBarAttention.setProgress(ThinkGearService.eegAttention);
				progressBarMeditation.setProgress(ThinkGearService.eegMeditation);
			}

			ThinkGearService.eegPower = calculateSpeed();
			eegPower = ThinkGearService.eegPower;

			progressBarPower.setProgress(ThinkGearService.eegPower);


		}

		if (MuseService.eegConnected) {

//			Log.d(TAG, "MuseService.eegConnected: eegSignal: " + MuseService.eegSignal);
//			if (MuseService.eegSignal < 100) {
//				MuseService.eegConcentration = 0;
//				MuseService.eegMellow = 0;
//				progressBarAttention.setProgress(MuseService.eegConcentration);
//				progressBarMeditation.setProgress(MuseService.eegMellow);
//			}

			MuseService.eegPower = calculateSpeed();

			progressBarPower.setProgress(MuseService.eegPower);
			eegPower = MuseService.eegPower;


		}


		updateServoPosition();
		updateBloomRGB();


	} // updatePower


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


		return(speed);


	} // calculateSpeed


	// ################################################################

	public void updateServoPosition() {


		if (eegPower > 0)
			bloomServoPercentage = bloomServoPercentage + 3;
		else
			bloomServoPercentage = bloomServoPercentage - 1;

		if (bloomServoPercentage > 100)
			bloomServoPercentage = 100;

		if (bloomServoPercentage < 0)
			bloomServoPercentage = 0;


//		progressBarBloom.setProgress(bloomServoPercentage);

		servoSeekBar.setProgress(bloomServoPercentage);


//		if (characteristicTx != null) {
//
//			byte[] buf = new byte[]{(byte) 0x03, (byte) 0x00, (byte) 0x00};
//
////		buf[1] = (byte) servoSeekBar.getProgress();
//			buf[1] = (byte) bloomServoPercentage;
//
//			characteristicTx.setValue(buf);
//			mBluetoothLeService.writeCharacteristic(characteristicTx);
//
//		}


	}


	// ################################################################

	public void updateBloomRGB() {


		boolean sendRed = false;
		boolean sendBlue = false;

		int attentionSeekValue = seekBarAttention.getProgress();
		int meditationSeekValue = seekBarMeditation.getProgress();

		if (eegPower > 0) {

			if (attentionSeekValue > 0) {
				bloomColorRed = bloomColorRed + 8;
				sendRed = true;
			}
			if (meditationSeekValue > 0) {
				bloomColorBlue = bloomColorBlue + 8;
				sendBlue = true;
			}

		} else {


			if (attentionSeekValue > 0) {
				bloomColorRed = bloomColorRed - 6;
				sendRed = true;
			}
			if (meditationSeekValue > 0) {
				bloomColorBlue = bloomColorBlue - 6;
				sendBlue = true;
			}

		}

		if (bloomColorRed > 255)
			bloomColorRed = 255;
		if (bloomColorBlue > 255)
			bloomColorBlue = 255;
		if (bloomColorGreen > 255)
			bloomColorGreen = 255;

		if (bloomColorRed < 0)
			bloomColorRed = 0;
		if (bloomColorBlue < 0)
			bloomColorBlue = 0;
		if (bloomColorGreen < 0)
			bloomColorGreen = 0;


		if (sendRed)
			if (BloomSingleton.getInstance().characteristicTx != null) {

				byte[] buf = new byte[]{(byte) 0x0A, (byte) 0x00, (byte) bloomColorRed};

				BloomSingleton.getInstance().characteristicTx.setValue(buf);
				BloomSingleton.getInstance().mBluetoothLeService.writeCharacteristic(BloomSingleton.getInstance().characteristicTx);

			}


		if (sendBlue)
			if (BloomSingleton.getInstance().characteristicTx != null) {

				byte[] buf = new byte[]{(byte) 0x0A, (byte) 0x02, (byte) bloomColorBlue};

				BloomSingleton.getInstance().characteristicTx.setValue(buf);
				BloomSingleton.getInstance().mBluetoothLeService.writeCharacteristic(BloomSingleton.getInstance().characteristicTx);

			}

	}


	// ################################################################

//	public void hideEEGRawHistory() {
//
//		Log.v(TAG, "hideEEGRawHistory()");
//
//		if (eegRawHistoryPlot != null)
//			eegRawHistoryPlot.setVisibility(View.GONE);
//
//
//		//			removeView*(View)
//		//			eegRawHistoryPlot.remove
//		//			(XYPlot) v.findViewById(R.id.eegRawHistoryPlot)
//
//
//	} // hideEEGRawHistory
//
//
//	// ################################################################
//
//	public void updateEEGRawHistory(Number[] rawEEG) {
//
//		if (eegRawHistoryPlot != null) {
//			eegRawHistoryPlot.removeSeries(eegRawHistorySeries);
//
//			eegRawHistorySeries = new SimpleXYSeries(Arrays.asList(rawEEG), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Raw EEG");
//
//			//		LineAndPointFormatter format = new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.BLACK, null, null);
//			//		LineAndPointFormatter format = new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.TRANSPARENT, null, null);
//			LineAndPointFormatter format = new LineAndPointFormatter(Color.rgb(0, 0, 0), Color.TRANSPARENT, null, null);
//
//			//		format.getFillPaint().setAlpha(220);
//
//			eegRawHistoryPlot.addSeries(eegRawHistorySeries, format);
//
//
//			// redraw the Plots:
//			eegRawHistoryPlot.redraw();
//
//			rawEEG = new Number[512];
//			arrayIndex = 0;
//		}
//
//	} // updateEEGRawHistory


	// ################################################################

//	public void updateScore() {
//
//		/**
//		 * Score points based on target slider levels
//		 * If you pass your goal with either Attention or Meditation
//		 * the higher target of the two will counts as points per second.
//		 *
//		 * Minimum threshold for points is set as "minimumScoreTarget"
//		 *
//		 * For example, assume minimumScoreTarget is 40%.
//		 * If your target Attention is 60% and you go past to reach 80%
//		 * you will receive 20 points per second (60-40). If your
//		 * target is 80% and you reach 80% you will receive 40
//		 * points per second (80-40).
//		 *
//		 * You can set both Attention and Meditation targets at the
//		 * same time. Reaching either will fly the helicopter but you
//		 * will only receive points for the higher-scoring target of
//		 * the two.
//		 *
//		 */
//
//		int eegAttentionScore = 0;
//		int eegAttention = progressBarAttention.getProgress();
//		int eegAttentionTarget = seekBarAttention.getProgress();
//
//		int eegMeditationScore = 0;
//		int eegMeditation = progressBarMeditation.getProgress();
//		int eegMeditationTarget = seekBarMeditation.getProgress();
//
//		if ((eegAttention >= eegAttentionTarget) &&
//				  (eegAttentionTarget > minimumScoreTarget))
//			eegAttentionScore = eegAttentionTarget - minimumScoreTarget;
//
//		if ((eegMeditation >= eegMeditationTarget) &&
//				  (eegMeditationTarget > minimumScoreTarget))
//			eegMeditationScore = eegMeditationTarget - minimumScoreTarget;
//
//		if (eegAttentionScore > eegMeditationScore)
//			scoreCurrent = scoreCurrent + eegAttentionScore;
//		else
//			scoreCurrent = scoreCurrent + eegMeditationScore;
//
//		textViewScore.setText(Integer.toString(scoreCurrent));
//
//		if (scoreCurrent > scoreHigh) {
//			scoreHigh = scoreCurrent;
//			textViewHighScore.setText(Integer.toString(scoreHigh));
//		}
//
//
//		// Catch anyone gaming the system with one slider
//		// below the minimum threshold and the other over.
//		// For example, setting Meditation to 1% will keep helicopter
//		// activated even if Attention is below target
//		if ((eegAttention < eegAttentionTarget) && (eegMeditation < minimumScoreTarget))
//			resetCurrentScore();
//		if ((eegMeditation < eegMeditationTarget) && (eegAttention < minimumScoreTarget))
//			resetCurrentScore();
//		if ((eegAttention < minimumScoreTarget) && (eegMeditation < minimumScoreTarget))
//			resetCurrentScore();
//
//
//	} // updateScore


	// ################################################################

//	public void resetCurrentScore() {
//
//		if (scoreCurrent > 0)
//			textViewLastScore.setText(Integer.toString(scoreCurrent));
//		scoreCurrent = 0;
//		textViewScore.setText(Integer.toString(scoreCurrent));
//
//	} // resetCurrentScore

	// ################################################################

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
		                               IBinder service) {
			BloomSingleton.getInstance().mBluetoothLeService = ((RBLService.LocalBinder) service)
					  .getService();
			if (!BloomSingleton.getInstance().mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				getActivity().finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			BloomSingleton.getInstance().mBluetoothLeService = null;
		}
	};


	// ################################################################

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (RBLService.ACTION_GATT_DISCONNECTED.equals(action)) {
//				Toast.makeText(getApplicationContext(), "Disconnected",
				Toast.makeText(getActivity(), "Bloom Disconnected",
						  Toast.LENGTH_SHORT).show();
				setButtonDisable();
			} else if (RBLService.ACTION_GATT_SERVICES_DISCOVERED
					  .equals(action)) {
				Toast.makeText(getActivity(), "Bloom Connected",
						  Toast.LENGTH_SHORT).show();

				getGattService(BloomSingleton.getInstance().mBluetoothLeService.getSupportedGattService());
			} else if (RBLService.ACTION_DATA_AVAILABLE.equals(action)) {
				BloomSingleton.getInstance().data = intent.getByteArrayExtra(RBLService.EXTRA_DATA);

//				readAnalogInValue(data);
			} else if (RBLService.ACTION_GATT_RSSI.equals(action)) {
				displayData(intent.getStringExtra(RBLService.EXTRA_DATA));
			}
		}
	};


}
