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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BloomFragment extends Fragment
		  implements View.OnClickListener,
		  SeekBar.OnSeekBarChangeListener {

	private final static String TAG = BloomFragment.class.getSimpleName();

	private OnFragmentInteractionListener mListener;

	View v;

	private Button connectBloom = null;
	private Button connectEEG = null;

	private Button buttonDemo = null;
	private Button buttonOpen = null;
	private Button buttonClose = null;

	private TextView rssiValue = null;
	private SeekBar servoSeekBar;

	private BluetoothGattCharacteristic characteristicTx = null;
	private RBLService mBluetoothLeService;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mDevice = null;
	private String mDeviceAddress;

	private boolean flag = true;
	private boolean connState = false;
	private boolean scanFlag = false;

	private byte[] data = new byte[3];
	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 2000;

	final private static char[] hexArray = { '0', '1', '2', '3', '4', '5', '6',
			  '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };


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
	boolean demoFlightMode = false;
	Number[] rawEEG = new Number[512];
	int arrayIndex = 0;


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



	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
		                               IBinder service) {
			mBluetoothLeService = ((RBLService.LocalBinder) service)
					  .getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				getActivity().finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

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

				getGattService(mBluetoothLeService.getSupportedGattService());
			} else if (RBLService.ACTION_DATA_AVAILABLE.equals(action)) {
				data = intent.getByteArrayExtra(RBLService.EXTRA_DATA);

//				readAnalogInValue(data);
			} else if (RBLService.ACTION_GATT_RSSI.equals(action)) {
				displayData(intent.getStringExtra(RBLService.EXTRA_DATA));
			}
		}
	};





//	public static BloomFragment newInstance(String param1, String param2) {
	public static BloomFragment newInstance() {
		BloomFragment fragment = new BloomFragment();
		Bundle args = new Bundle();
//		args.putString(ARG_PARAM1, param1);
//		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public BloomFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		if (getArguments() != null) {
//			mParam1 = getArguments().getString(ARG_PARAM1);
//			mParam2 = getArguments().getString(ARG_PARAM2);
//		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
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

			@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
			                              boolean fromUser) {
				byte[] buf = new byte[] { (byte) 0x03, (byte) 0x00, (byte) 0x00 };

				buf[1] = (byte) servoSeekBar.getProgress();

				characteristicTx.setValue(buf);
				mBluetoothLeService.writeCharacteristic(characteristicTx);
			}
		});



//		rssiValue = (TextView) v.findViewById(R.id.rssiValue);

		connectBloom = (Button) v.findViewById(R.id.connectBloom);
		connectBloom.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (scanFlag == false) {
					scanLeDevice();

					Timer mTimer = new Timer();
					mTimer.schedule(new TimerTask() {

						@Override
						public void run() {
							if (mDevice != null) {
								mDeviceAddress = mDevice.getAddress();
								mBluetoothLeService.connect(mDeviceAddress);
								scanFlag = true;
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
					}, SCAN_PERIOD);
				}

				System.out.println(connState);
//				Log.e(TAG, connState);
				if (connState == false) {
					mBluetoothLeService.connect(mDeviceAddress);
				} else {
					mBluetoothLeService.disconnect();
					mBluetoothLeService.close();
					setButtonDisable();
				}
			}
		});





		connectEEG = (Button) v.findViewById(R.id.buttonConnectEEG);
		connectEEG.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				connectHeadset(v);

			}
		});




		buttonOpen = (Button) v.findViewById(R.id.buttonOpen);
		buttonOpen.setOnClickListener(new View.OnClickListener() {
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
			@Override
			public void onClick(View v) {
				byte[] buf = new byte[] { (byte) 0x01, (byte) 0x00, (byte) 0x00 };
				characteristicTx.setValue(buf);
				mBluetoothLeService.writeCharacteristic(characteristicTx);
			}
		});
		buttonOpen.setVisibility(View.GONE);

		buttonClose = (Button) v.findViewById(R.id.buttonClose);
		buttonClose.setOnClickListener(new View.OnClickListener() {
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
			@Override
			public void onClick(View v) {
				byte[] buf = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00 };
				characteristicTx.setValue(buf);
				mBluetoothLeService.writeCharacteristic(characteristicTx);
			}
		});
		buttonClose.setVisibility(View.GONE);

		buttonDemo = (Button) v.findViewById(R.id.buttonDemo);
		buttonDemo.setOnClickListener(new View.OnClickListener() {
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
			@Override
			public void onClick(View v) {
				byte[] buf = new byte[] { (byte) 0x05, (byte) 0x00, (byte) 0x00 };
				characteristicTx.setValue(buf);
				mBluetoothLeService.writeCharacteristic(characteristicTx);
			}
		});


		if (!getActivity().getPackageManager().hasSystemFeature(
				  PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(getActivity(), "Ble not supported", Toast.LENGTH_SHORT)
					  .show();
			getActivity().finish();
		}

		final BluetoothManager mBluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(getActivity(), "Ble not supported", Toast.LENGTH_SHORT)
					  .show();
			getActivity().finish();
			return v;
		}

		Intent gattServiceIntent = new Intent(getActivity(),
				  RBLService.class);
//		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		getActivity().bindService(gattServiceIntent, mServiceConnection, getActivity().BIND_AUTO_CREATE);


//		return inflater.inflate(R.layout.fragment_remote_control, container, false);


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
		// TODO: Update argument type and name
		public void onFragmentInteraction(Uri uri);
	}





	// ################################################################

	public void onPause() {

		Log.v(TAG, "onPause()");

		super.onPause();

		try {

			disconnectHeadset();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.v(TAG, "Exception: onPause()");
			e.printStackTrace();
		}

	} // onPause


	// ################################################################

	@Override
	public void onResume() {
		super.onResume();

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					  BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

		if (eegConnected)
			setButtonText(R.id.buttonConnectEEG, "Disconnect EEG");

	}



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

	private void setButtonEnable() {
		flag = true;
		connState = true;

		servoSeekBar.setEnabled(flag);
		connectBloom.setText("Disconnect Bloom");
	}

	private void setButtonDisable() {
		flag = false;
		connState = false;

		servoSeekBar.setEnabled(flag);
		connectBloom.setText("Connect Bloom");
	}

	private void startReadRssi() {
		new Thread() {
			public void run() {

				while (flag) {
					mBluetoothLeService.readRssi();
					try {
						sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void getGattService(BluetoothGattService gattService) {
		if (gattService == null)
			return;

		setButtonEnable();
		startReadRssi();

		characteristicTx = gattService
				  .getCharacteristic(RBLService.UUID_BLE_SHIELD_TX);

		BluetoothGattCharacteristic characteristicRx = gattService
				  .getCharacteristic(RBLService.UUID_BLE_SHIELD_RX);
		mBluetoothLeService.setCharacteristicNotification(characteristicRx,
				  true);
		mBluetoothLeService.readCharacteristic(characteristicRx);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(RBLService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(RBLService.ACTION_GATT_RSSI);

		return intentFilter;
	}

	private void scanLeDevice() {
		new Thread() {

			@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
			@Override
			public void run() {
				mBluetoothAdapter.startLeScan(mLeScanCallback);

				try {
					Thread.sleep(SCAN_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				mBluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}.start();
	}

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
						mDevice = device;
					}
				}
			});
		}
	};

	private String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

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

	@Override
	public void onStop() {
		super.onStop();

		flag = false;

		getActivity().unregisterReceiver(mGattUpdateReceiver);



		try {

			disconnectHeadset();


		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.v(TAG, "Exception: onStop()");
			e.printStackTrace();
		}



	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mServiceConnection != null)
			getActivity().unbindService(mServiceConnection);


		try {

			if(bluetoothAdapter != null)
				tgDevice.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.v(TAG, "Exception: onDestroy()");
			e.printStackTrace();
		}


	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT
				  && resultCode == Activity.RESULT_CANCELED) {
			getActivity().finish();
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}



	// ################################################################

//	public void onStop() {
//
//		Log.v(TAG, "onStop()");
//
//		super.onStop();
//
//		try {
//
//			disconnectHeadset();
//
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			Log.v(TAG, "Exception: onStop()");
//			e.printStackTrace();
//		}
//
//	} // onStop


	// ################################################################






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

	public void updateScreenLayoutSmall() {

//		String button_test_fly_small = getResources().getString(R.string.button_test_fly_small);
//		setButtonText(R.id.buttonTestFly, button_test_fly_small);
//
//		textViewLabelScores.setVisibility(View.VISIBLE);
//		viewSpaceScore.setVisibility(View.VISIBLE);


		ViewGroup.LayoutParams layoutParams;

//		layoutParams = (android.view.ViewGroup.LayoutParams) viewSpaceScoreLast.getLayoutParams();
//		layoutParams.width = 10;
//		viewSpaceScoreLast.setLayoutParams(layoutParams);
//
//		layoutParams = (android.view.ViewGroup.LayoutParams) viewSpaceScoreHigh.getLayoutParams();
//		layoutParams.width = 10;
//		viewSpaceScoreHigh.setLayoutParams(layoutParams);
//
//
//		String labelScore = getResources().getString(R.string.textview_label_score_small);
//		textViewLabelScore.setText(labelScore);
//
//		String labelLastScore = getResources().getString(R.string.textview_label_last_score_small);
//		textViewLabelLastScore.setText(labelLastScore);
//
//		String labelHighScore = getResources().getString(R.string.textview_label_high_score_small);
//		textViewLabelHighScore.setText(labelHighScore);


//		// HTC Droid DNA - AndroidPlot has issues with OpenGL Render
//		if ((Build.MANUFACTURER.contains("HTC")) &&
//				  (Build.MODEL.contains("HTC6435LVW"))) {
//
//			Log.v(TAG, "Device detected: HTC Droid DNA");
//			hideEEGRawHistory();
//
//		}


	} // updateScreenLayoutSmall


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

	public void onClick(View v) {

		Log.e(TAG, "onClick()");

		switch (v.getId()) {

			case R.id.buttonConnectEEG:

				connectHeadset(v);

		}

	} // onClick


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
						if (DEBUG)
							Log.v(TAG, "Connecting to EEG");
//						appendDebugConsole("Connecting to EEG\n");
						eegConnecting = true;
						eegConnected = false;
						updateStatusImage();
						break;
					case TGDevice.STATE_CONNECTED:
						if (DEBUG)
							Log.v(TAG, "EEG Connected");
//						appendDebugConsole("Bluetooth Connected\n");
						setButtonText(R.id.buttonConnectEEG, "Disconnect EEG");
						eegConnecting = false;
						eegConnected = true;
						updateStatusImage();
						tgDevice.start();
						break;
					case TGDevice.STATE_NOT_FOUND:
						if (DEBUG)
							Log.v(TAG, "EEG headset not found");
//						appendDebugConsole("EEG headset not found\n");
						eegConnecting = false;
						eegConnected = false;
						updateStatusImage();
						break;
					case TGDevice.STATE_NOT_PAIRED:
						if (DEBUG)
							Log.v(TAG, "EEG headset not paired");
//						appendDebugConsole("EEG headset not paired\n");
						eegConnecting = false;
						eegConnected = false;
						updateStatusImage();
						break;
					case TGDevice.STATE_DISCONNECTED:
						if (DEBUG)
							Log.v(TAG, "EEG Disconnected");
//						appendDebugConsole("EEG Disconnected\n");
						eegConnecting = false;
						eegConnected = false;
						updateStatusImage();
						disconnectHeadset();
						break;
				}

				break;

			case TGDevice.MSG_POOR_SIGNAL:
				//			Log.v(TAG, "PoorSignal: " + msg.arg1);
				eegSignal = calculateSignal(msg.arg1);
				progressBarSignal.setProgress(eegSignal);
				updateStatusImage();
				break;
			case TGDevice.MSG_ATTENTION:
				//			Log.v(TAG, "Attention: " + eegAttention);
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

				break;
			case TGDevice.MSG_BLINK:
				//tv.append("Blink: " + msg.arg1 + "\n");
				break;
			case TGDevice.MSG_RAW_DATA:

				rawEEG[arrayIndex] = msg.arg1;
				arrayIndex = arrayIndex + 1;

				if (arrayIndex == EEG_RAW_HISTORY_SIZE - 1)
					updateEEGRawHistory(rawEEG);

				break;
			case TGDevice.MSG_RAW_COUNT:
				//tv.append("Raw Count: " + msg.arg1 + "\n");
				break;
			case TGDevice.MSG_RAW_MULTI:
				//TGRawMulti rawM = (TGRawMulti)msg.obj;
				//tv.append("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
			case TGDevice.MSG_HEART_RATE:
				//				appendDebugConsole("Heart rate: " + msg.arg1 + "\n");
				break;
			case TGDevice.MSG_LOW_BATTERY:
				// TODO Fragment Context
//				Toast.makeText(((OrbitTabActivity)getActivity()), "EEG battery low!", Toast.LENGTH_SHORT).show();
				Toast.makeText((getActivity()), "EEG battery low!", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
		}

	} // handleMessage


	// ################################################################

	public void connectHeadset(View view) {

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
//		setButtonText(R.id.buttonConnect, "Connect");
//
//
//		if (tgDevice.getState() == TGDevice.STATE_CONNECTED) {
//			tgDevice.stop();
//			tgDevice.close();
//
		// TODO Fragment Context
//		((OrbitTabActivity)getActivity()).stopControl();
//		(getActivity()).stopControl();

//			disconnectHeadset();
//
//		}


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



		updateServoPosition();
		updateBloomRGB();




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

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
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
			if (characteristicTx != null) {

				byte[] buf = new byte[]{(byte) 0x0A, (byte) 0x00, (byte) bloomColorRed};

				characteristicTx.setValue(buf);
				mBluetoothLeService.writeCharacteristic(characteristicTx);

			}


		if (sendBlue)
			if (characteristicTx != null) {

				byte[] buf = new byte[]{(byte) 0x0A, (byte) 0x02, (byte) bloomColorBlue};

				characteristicTx.setValue(buf);
				mBluetoothLeService.writeCharacteristic(characteristicTx);

			}

	}



	// ################################################################

	public void hideEEGRawHistory() {

		Log.v(TAG, "hideEEGRawHistory()");

		if (eegRawHistoryPlot != null)
			eegRawHistoryPlot.setVisibility(View.GONE);


		//			removeView*(View)
		//			eegRawHistoryPlot.remove
		//			(XYPlot) v.findViewById(R.id.eegRawHistoryPlot)


	} // hideEEGRawHistory


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

	public void demoMode(View view) {

		/**
		 * Demo mode is called when the "Test Helicopter" button is pressed.
		 * This method can be easily adjusted for testing new features
		 * during development.
		 */

		Log.v(TAG, "Sending Test Signal to Helicopter");
//		appendDebugConsole("Sending Test Signal to Helicopter\n");
//
//		demoFlightMode = true;
//		flightActive = true;
//
//		FragmentTabAdvanced fragmentAdvanced =
//				  (FragmentTabAdvanced) getSupportFragmentManager().findFragmentByTag( getTabFragmentAdvanced() );
//
//		//		if (fragmentAdvanced.checkBoxGenerateAudio.isChecked())
//		if (generateAudio && (fragmentAdvanced != null))
//			eegPower = fragmentAdvanced.seekBarThrottle.getProgress();
//		else
//			eegPower = 100;
//
//		playControl();
//
//		demoFlightMode = false;


	} // demoMode






}
