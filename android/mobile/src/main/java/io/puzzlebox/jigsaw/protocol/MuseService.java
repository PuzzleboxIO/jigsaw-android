package io.puzzlebox.jigsaw.protocol;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.LibMuseVersion;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MuseFileWriter;
import com.interaxon.libmuse.MuseManager;
import com.interaxon.libmuse.MusePreset;
import com.interaxon.libmuse.MuseVersion;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.puzzlebox.jigsaw.data.SessionSingleton;

/**
 * Created by sc on 5/2/15.
 */
public class MuseService extends Service {

	private final static String TAG = MuseService.class.getSimpleName();

	public static boolean eegConnected = false;

	public static int eegConcentration = 0;
	public static int eegMellow = 0;

	private ServiceHandler mServiceHandler;

	private Muse muse = null;
	private ConnectionListener connectionListener = null;
	private DataListener dataListener = null;
	//	private MuseDataListener dataListener = null;
	private boolean dataTransmission = true;
	private MuseFileWriter fileWriter = null;

	// ################################################################

	//	public MuseService() {
//	}
	public MuseService() {
		// Required empty public constructor
//		WeakReference<Activity> weakActivity =
//				  new WeakReference<>(getActivity());
//
//		connectionListener = new ConnectionListener(weakActivity);
//		dataListener = new DataListener(weakActivity);

		connectionListener = new ConnectionListener();
		dataListener = new DataListener();
	}

	// ################################################################

	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
			// Normally we would do some work here, like download a file.
			// For our sample, we just sleep for 5 seconds.
			long endTime = System.currentTimeMillis() + 5*1000;
			while (System.currentTimeMillis() < endTime) {
				synchronized (this) {
					try {
						wait(endTime - System.currentTimeMillis());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			// Stop the service using the startId, so that we don't stop
			// the service in the middle of handling another job
			stopSelf(msg.arg1);
		}
	}

	// ################################################################

	@Override
	public void onCreate() {

		Log.d(TAG, "onCreate()");

		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				  android.os.Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		Looper mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

		// Create listeners and pass reference to activity to them
//		WeakReference<Activity> weakActivity =
//				  new WeakReference<Activity>(this);
//
//		connectionListener = new ConnectionListener(weakActivity);
//		dataListener = new DataListener(weakActivity);

		Log.i("Muse Headband", "libmuse version=" + LibMuseVersion.SDK_VERSION);

		// API 19
		fileWriter = MuseManager.getMuseFileWriter(new File(
				  getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
				  "testlibmusefile.muse"));

		fileWriter.addAnnotationString(1, "MainActivity onCreate");
		dataListener.setFileWriter(fileWriter);

		createService();

	}


	// ################################################################

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

//		Toast.makeText(this, "Starting ThinkGear Service", Toast.LENGTH_SHORT).show();
//		Toast.makeText(this, "Connecting to NeuroSky EEG", Toast.LENGTH_SHORT).show();
		Toast.makeText(this, "Connecting to Muse EEG", Toast.LENGTH_SHORT).show();

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);

//		mContext = this.getApplicationContext();

//		createService();

		connectHeadset();


		// If we get killed, after returning from here, restart

//		http://developer.android.com/reference/android/app/Service.html#START_STICKY

//		START_STICKY
//		If the system kills the service after onStartCommand() returns,
// recreate the service and call onStartCommand(), but do not
// redeliver the last intent. Instead, the system calls onStartCommand()
// with a null intent, unless there were pending intents to start
// the service, in which case, those intents are delivered. This is
// suitable for media players (or similar services) that are not
// executing commands, but running indefinitely and waiting for a job.

//		START_REDELIVER_INTENT
//		If the system kills the service after onStartCommand() returns,
// recreate the service and call onStartCommand() with the last intent
// that was delivered to the service. Any pending intents are delivered
// in turn. This is suitable for services that are actively performing
// a job that should be immediately resumed, such as downloading a file.

		return START_STICKY;

	}


	// ################################################################

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}


	// ################################################################

//	@Override
//	public boolean onUnbind(Intent intent) {
//		// All clients have unbound with unbindService()
//		return mAllowRebind;
//	}


	// ################################################################

//	@Override
//	public void onRebind(Intent intent) {
//		// A client is binding to the service with bindService(),
//		// after onUnbind() has already been called
//	}


	// ################################################################

	@Override
	public void onDestroy() {
		super.onDestroy();
//		Toast.makeText(this, "Destroying ThinkGear Service", Toast.LENGTH_SHORT).show();
		Log.e(TAG, "onDestroy()");
	}


	// ################################################################

//	private Handler handlerThinkGear = new Handler(new Handler.Callback() {
//		@Override
//		public boolean handleMessage(Message msg) {
//			parseEEG(msg);
//			return true;
//		}
//	});


	// ################################################################

	public void createService() {
		/**
		 * Prepare Bluetooth and NeuroSky ThinkGear EEG interface
		 */

//		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//		if (bluetoothAdapter != null) {
//
//			/** create the TGDevice */
//			tgDevice = new TGDevice(bluetoothAdapter, handlerThinkGear);
//
//			/** Retrieve a list of paired Bluetooth adapters */
//			//			Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//			//			pairedBluetoothDevices = new ArrayList<String>(Arrays.asList(pairedDevices.toString()));
//			/**
//			 * NOTE: To get device names iterate through pairedBluetoothDevices
//			 * and call the getName() method on each BluetoothDevice object.
//			 */
//		}
	}


	// ################################################################

//	public void parseEEG(Message msg) {
//
//		switch (msg.what) {
//
//			case TGDevice.MSG_STATE_CHANGE:
//
//				switch (msg.arg1) {
//					case TGDevice.STATE_IDLE:
//						break;
//					case TGDevice.STATE_CONNECTING:
//						Log.d(TAG, "Connecting to EEG");
//						eegConnecting = true;
//						eegConnected = false;
//						broadcastEventEEG("eegStatus", "STATE_CONNECTING");
//						break;
//					case TGDevice.STATE_CONNECTED:
//						Log.d(TAG, "EEG Connected");
//						eegConnecting = false;
//						eegConnected = true;
//						broadcastEventEEG("eegStatus", "STATE_CONNECTED");
//						SessionSingleton.getInstance().resetSession();
//						tgDevice.start();
//						break;
//					case TGDevice.STATE_NOT_FOUND:
//						Log.d(TAG, "EEG headset not found");
//						eegConnecting = false;
//						eegConnected = false;
//						broadcastEventEEG("eegStatus", "STATE_NOT_FOUND");
//						break;
//					case TGDevice.STATE_NOT_PAIRED:
//						Log.d(TAG, "EEG headset not paired");
//						eegConnecting = false;
//						eegConnected = false;
//						broadcastEventEEG("eegStatus", "STATE_NOT_PAIRED");
//						break;
//					case TGDevice.STATE_DISCONNECTED:
//						Log.d(TAG, "EEG Disconnected");
//						eegConnecting = false;
//						eegConnected = false;
//						broadcastEventEEG("eegStatus", "STATE_DISCONNECTED");
//						disconnectHeadset();
//						break;
//				}
//
//				break;
//
//			case TGDevice.MSG_POOR_SIGNAL:
//				eegSignal = calculateSignal(msg.arg1);
//				processPacketEEG();
//				break;
//			case TGDevice.MSG_ATTENTION:
//				eegAttention = msg.arg1;
//				break;
//			case TGDevice.MSG_MEDITATION:
//				eegMeditation = msg.arg1;
//				break;
//			case TGDevice.MSG_BLINK:
//				/**
//				 * Strength of detected blink. The Blink Strength ranges
//				 * from 1 (small blink) to 255 (large blink). Unless a blink
//				 * occurred, nothing will be returned. Blinks are only
//				 * calculated if PoorSignal is less than 51.
//				 */
//				Log.d(TAG, "Blink: " + msg.arg1 + "\n");
//				broadcastEventEEG("eegBlink", String.valueOf(msg.arg1));
//				break;
//			case TGDevice.MSG_RAW_DATA:
//
//				SessionSingleton.getInstance().appendRawEEG(msg.arg1);
//
////				try {
////					rawEEG[arrayIndex] = msg.arg1;
////					arrayIndex = arrayIndex + 1;
////				} catch (ArrayIndexOutOfBoundsException e) {
////					Log.e(TAG, "ArrayIndexOutOfBoundsException:" + e);
////					e.printStackTrace();
////				} catch (Exception e) {
////					e.printStackTrace();
////				}
//
////				if (arrayIndex == EEG_RAW_HISTORY_SIZE - 1) {
//////					updateEEGRawHistory(rawEEG);
////					arrayIndex = 0; // TODO should pass data to other fragments
////				}
//
//				break;
//			case TGDevice.MSG_RAW_COUNT:
//				break;
//			case TGDevice.MSG_RAW_MULTI:
//				//TGRawMulti rawM = (TGRawMulti)msg.obj;
//				//Log.d(TAG, "Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
//			case TGDevice.MSG_HEART_RATE:
//				//Log.d(TAG, "Heart rate: " + msg.arg1 + "\n");
//				break;
//			case TGDevice.MSG_LOW_BATTERY:
////				Toast.makeText((getActivity()), "EEG battery low!", Toast.LENGTH_SHORT).show();
//				broadcastEventEEG("eegStatus", "MSG_LOW_BATTERY");
//				break;
//			default:
//				break;
//		}

//	} // handleMessage


	// ################################################################

	public void connectHeadset() {

		/**
		 * Called when the "Connect" button is pressed
		 */

		Log.v(TAG, "connectHeadset()");

//		if(bluetoothAdapter == null) {
//
//			// Alert user that Bluetooth is not available
//			Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
//
//		} else {
//
//			if (tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED) {
//				tgDevice.connect(rawEnabled);
//			}
//
//
//			else if (tgDevice.getState() == TGDevice.STATE_CONNECTED)
//			/** "Disconnect" button was pressed */
//				disconnectHeadset();
//
//		}

		Spinner musesSpinner = new AppCompatSpinner(getApplicationContext());

		MuseManager.refreshPairedMuses();

		List<Muse> pairedMuses = MuseManager.getPairedMuses();
		List<String> spinnerItems = new ArrayList<String>();
		for (Muse m: pairedMuses) {
			String dev_id = m.getName() + "-" + m.getMacAddress();
			Log.i("Muse Headband", dev_id);
			spinnerItems.add(dev_id);
		}

		ArrayAdapter<String> adapterArray = new ArrayAdapter<String> (
				  getApplicationContext(), android.R.layout.simple_spinner_item, spinnerItems);
		musesSpinner.setAdapter(adapterArray);


		if (pairedMuses.size() < 1 ||
				  musesSpinner.getAdapter().getCount() < 1) {
			Log.w("Muse Headband", "There is nothing to connect to");
		}
		else {
			muse = pairedMuses.get(musesSpinner.getSelectedItemPosition());

			Log.e(TAG, "ConnectionState state = muse.getConnectionState();");

			ConnectionState state = muse.getConnectionState();
			if (state == ConnectionState.CONNECTED ||
					  state == ConnectionState.CONNECTING) {
				Log.w("Muse Headband", "doesn't make sense to connect second time to the same muse");
				return;
			}

			Log.e(TAG, "configure_library();");

			configure_library();

			Log.e(TAG, "fileWriter.open();");
			fileWriter.open();
			Log.e(TAG, "fileWriter.addAnnotationString(1, \"Connect clicked\");");
			fileWriter.addAnnotationString(1, "Connect clicked");
			/**
			 * In most cases libmuse native library takes care about
			 * exceptions and recovery mechanism, but native code still
			 * may throw in some unexpected situations (like bad bluetooth
			 * connection). Print all exceptions here.
			 */
			try {
				muse.runAsynchronously();
			} catch (Exception e) {
				Log.e("ERROR: Muse Headband", e.toString());
			}
		}


		Log.e(TAG, "end of connectHeadset");

	} // connectHeadset


	// ################################################################

	public static void disconnectHeadset() {

		/**
		 * Called when "Disconnect" button is pressed
		 */

//		eegConnecting = false;
//		eegConnected = false;
//
//		eegAttention = 0;
//		eegMeditation = 0;
//		eegSignal = 0;
//		eegPower = 0;
//
//		if (tgDevice.getState() == TGDevice.STATE_CONNECTED) {
//			tgDevice.stop();
//			tgDevice.close();
//		}

	} // disconnectHeadset


	// ################################################################

	public void processPacketEEG() {
		try {
//			Log.e(TAG, "SessionSingleton.getInstance().updateTimestamp");
			SessionSingleton.getInstance().updateTimestamp();

			HashMap<String, String> packet;
			packet = new HashMap<>();

			packet.put("Date", SessionSingleton.getInstance().getCurrentDate());
			packet.put("Time", SessionSingleton.getInstance().getCurrentTimestamp());
//			packet.put("Attention", String.valueOf(eegAttention));
//			packet.put("Meditation", String.valueOf(eegMeditation));
//			packet.put("Signal Level", String.valueOf(eegSignal));
////			packet.put("Power", String.valueOf(eegPower));

			Log.d(TAG, "SessionSingleton.getInstance().appendData(packet): " + packet.toString());
			SessionSingleton.getInstance().appendData(packet);

			//				if (arrayIndex == EEG_RAW_HISTORY_SIZE - 1) {
//			SessionSingleton.getInstance().appendRawEEG(rawEEG);
//					arrayIndex = 0;
////				}

			broadcastPacketEEG(packet);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	// ################################################################

	private  void broadcastPacketEEG(HashMap<String, String> packet) {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.thinkgear.packet");

		intent.putExtra("Date", packet.get("Date")).putExtra("Time", packet.get("Time")).putExtra("Attention", packet.get("Attention"));
//		intent.putExtra("Meditation", packet.get("Meditation"));
//		intent.putExtra("Signal Level", packet.get("Signal Level"));
////			intent.putExtra("Power", packet.get("Power"));
//		intent.putExtra("eegConnected", String.valueOf(eegConnected));
//		intent.putExtra("eegConnecting", String.valueOf(eegConnecting));

		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

	}


	// ################################################################

	private  void broadcastEventEEG(String name, String value) {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.thinkgear.event");

		intent.putExtra("name", name);
		intent.putExtra("value", value);

		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

	}



	private void configure_library() {
		muse.registerConnectionListener(connectionListener);
		muse.registerDataListener(dataListener,
				  MuseDataPacketType.ACCELEROMETER);
		muse.registerDataListener(dataListener,
				  MuseDataPacketType.EEG);
		muse.registerDataListener(dataListener,
				  MuseDataPacketType.ALPHA_RELATIVE);
		muse.registerDataListener(dataListener,
				  MuseDataPacketType.ARTIFACTS);
		muse.registerDataListener(dataListener,
				  MuseDataPacketType.BATTERY);
		muse.setPreset(MusePreset.PRESET_14);
		muse.enableDataTransmission(dataTransmission);
	}



//
//	/**
//	 * Connection listener updates UI with new connection status and logs it.
//	 */
//	class ConnectionListener extends MuseConnectionListener {
//
//		private final static String TAG = MuseConnectionListener.class.getSimpleName();
//
//		final WeakReference<Activity> activityRef;
//
//		ConnectionListener(final WeakReference<Activity> activityRef) {
//			this.activityRef = activityRef;
//		}
//
//		@Override
//		public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
//			final ConnectionState current = p.getCurrentConnectionState();
//			final String status = p.getPreviousConnectionState().toString() +
//					  " -> " + current;
//			final String full = "Muse " + p.getSource().getMacAddress() +
//					  " " + status;
//			Log.i("Muse Headband", full);
//
//
//			if (current == ConnectionState.CONNECTED) {
//				Log.e(TAG, "ConnectionState.CONNECTED");
//
////			Activity activity = activityRef.get();
////			// UI thread is used here only because we need to update
////			// TextView values. You don't have to use another thread, unless
////			// you want to run disconnect() or connect() from connection packet
////			// handler. In this case creating another thread is required.
////			if (activity != null) {
////				activity.runOnUiThread(new Runnable() {
////					@Override
////					public void run() {
//////						TextView statusText =
//////								  (TextView) findViewById(R.id.con_status);
//////						statusText.setText(status);
//////						TextView museVersionText =
//////								  (TextView) findViewById(R.id.version);
////						if (current == ConnectionState.CONNECTED) {
////							Log.e(TAG, "ConnectionState.CONNECTED");
//////							MuseVersion museVersion = muse.getMuseVersion();
//////							String version = museVersion.getFirmwareType() +
//////									  " - " + museVersion.getFirmwareVersion() +
//////									  " - " + Integer.toString(
//////									  museVersion.getProtocolVersion());
//////							museVersionText.setText(version);
////						} else {
//////							museVersionText.setText(R.string.undefined);
////						}
////					}
////				});
//			}
//		}
//	}
//
//	/**
//	 * Data listener will be registered to listen for: Accelerometer,
//	 * Eeg and Relative Alpha bandpower packets. In all cases we will
//	 * update UI with new values.
//	 * We also will log message if Artifact packets contains "blink" flag.
//	 * DataListener methods will be called from execution thread. If you are
//	 * implementing "serious" processing algorithms inside those listeners,
//	 * consider to create another thread.
//	 */
//	class DataListener extends MuseDataListener {
//
//		private final String TAG = MuseDataListener.class.getSimpleName();
//
//		final WeakReference<Activity> activityRef;
//		private MuseFileWriter fileWriter;
//
//		DataListener(final WeakReference<Activity> activityRef) {
//			this.activityRef = activityRef;
//		}
//
//		@Override
//		public void receiveMuseDataPacket(MuseDataPacket p) {
//			switch (p.getPacketType()) {
//				case EEG:
//					updateEeg(p.getValues());
//					break;
//				case ACCELEROMETER:
//					updateAccelerometer(p.getValues());
//					break;
//				case ALPHA_RELATIVE:
//					updateAlphaRelative(p.getValues());
//					break;
//				case BATTERY:
//					fileWriter.addDataPacket(1, p);
//					// It's library client responsibility to flush the buffer,
//					// otherwise you may get memory overflow.
//					if (fileWriter.getBufferedMessagesSize() > 8096)
//						fileWriter.flush();
//					break;
//				default:
//					break;
//			}
//		}
//
//		@Override
//		public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
//			if (p.getHeadbandOn() && p.getBlink()) {
//				Log.i("Artifacts", "blink");
//			}
//		}
//
//		private void updateAccelerometer(final ArrayList<Double> data) {
////			Activity activity = activityRef.get();
////			if (activity != null) {
////				activity.runOnUiThread(new Runnable() {
////					@Override
////					public void run() {
////						TextView acc_x = (TextView) findViewById(R.id.acc_x);
////						TextView acc_y = (TextView) findViewById(R.id.acc_y);
////						TextView acc_z = (TextView) findViewById(R.id.acc_z);
////						acc_x.setText(String.format(
////								  "%6.2f", data.get(Accelerometer.FORWARD_BACKWARD.ordinal())));
////						acc_y.setText(String.format(
////								  "%6.2f", data.get(Accelerometer.UP_DOWN.ordinal())));
////						acc_z.setText(String.format(
////								  "%6.2f", data.get(Accelerometer.LEFT_RIGHT.ordinal())));
////					}
////				});
////			}
//		}
//
//		private void updateEeg(final ArrayList<Double> data) {
//
//			Log.e(TAG, String.format(
//					  "%6.2f", data.get(Eeg.FP1.ordinal())));
//
////			Activity activity = activityRef.get();
////			if (activity != null) {
////				activity.runOnUiThread(new Runnable() {
////					@Override
////					public void run() {
////						TextView tp9 = (TextView) findViewById(R.id.eeg_tp9);
////						TextView fp1 = (TextView) findViewById(R.id.eeg_fp1);
////						TextView fp2 = (TextView) findViewById(R.id.eeg_fp2);
////						TextView tp10 = (TextView) findViewById(R.id.eeg_tp10);
////						tp9.setText(String.format(
////								  "%6.2f", data.get(Eeg.TP9.ordinal())));
////						fp1.setText(String.format(
////								  "%6.2f", data.get(Eeg.FP1.ordinal())));
////						fp2.setText(String.format(
////								  "%6.2f", data.get(Eeg.FP2.ordinal())));
////						tp10.setText(String.format(
////								  "%6.2f", data.get(Eeg.TP10.ordinal())));
////					}
////				});
////			}
//		}
//
//		private void updateAlphaRelative(final ArrayList<Double> data) {
////			Activity activity = activityRef.get();
////			if (activity != null) {
////				activity.runOnUiThread(new Runnable() {
////					@Override
////					public void run() {
////						TextView elem1 = (TextView) findViewById(R.id.elem1);
////						TextView elem2 = (TextView) findViewById(R.id.elem2);
////						TextView elem3 = (TextView) findViewById(R.id.elem3);
////						TextView elem4 = (TextView) findViewById(R.id.elem4);
////						elem1.setText(String.format(
////								  "%6.2f", data.get(Eeg.TP9.ordinal())));
////						elem2.setText(String.format(
////								  "%6.2f", data.get(Eeg.FP1.ordinal())));
////						elem3.setText(String.format(
////								  "%6.2f", data.get(Eeg.FP2.ordinal())));
////						elem4.setText(String.format(
////								  "%6.2f", data.get(Eeg.TP10.ordinal())));
////					}
////				});
////			}
//		}
//
//		public void setFileWriter(MuseFileWriter fileWriter) {
//			this.fileWriter  = fileWriter;
//		}
//	}



	/**
	 * Connection listener updates UI with new connection status and logs it.
	 */
	class ConnectionListener extends MuseConnectionListener {

//	final WeakReference<Activity> activityRef;

		//	ConnectionListener(final WeakReference<Activity> activityRef) {
//		this.activityRef = activityRef;
//	}
		ConnectionListener() {
		}

		@Override
		public void receiveMuseConnectionPacket(MuseConnectionPacket p) {

//			final View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_muse, mContainer, false);

			final ConnectionState current = p.getCurrentConnectionState();
			final String status = p.getPreviousConnectionState().toString() +
					  " -> " + current;
			final String full = "Muse " + p.getSource().getMacAddress() +
					  " " + status;
			Log.i("Muse Headband", full);
//		Activity activity = activityRef.get();
			// UI thread is used here only because we need to update
			// TextView values. You don't have to use another thread, unless
			// you want to run disconnect() or connect() from connection packet
			// handler. In this case creating another thread is required.
//		if (activity != null) {
//			activity.runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					statusText.setText(status);
//					if (current == ConnectionState.CONNECTED) {
//						MuseVersion museVersion = muse.getMuseVersion();
//						String version = museVersion.getFirmwareType() +
//								  " - " + museVersion.getFirmwareVersion() +
//								  " - " + Integer.toString(
//								  museVersion.getProtocolVersion());
////							museVersionText.setText(version);
//					} else {
////							museVersionText.setText(R.string.undefined);
//					}
//				}
//			});
//		}
		}
	}

	/**
	 * Data listener will be registered to listen for: Accelerometer,
	 * Eeg and Relative Alpha bandpower packets. In all cases we will
	 * update UI with new values.
	 * We also will log message if Artifact packets contains "blink" flag.
	 * DataListener methods will be called from execution thread. If you are
	 * implementing "serious" processing algorithms inside those listeners,
	 * consider to create another thread.
	 */
	class DataListener extends MuseDataListener {

//	final WeakReference<Activity> activityRef;
//	private MuseFileWriter fileWriter;

		DataListener() {
		}
//	DataListener(final WeakReference<Activity> activityRef) {
//		this.activityRef = activityRef;
//	}

		@Override
		public void receiveMuseDataPacket(MuseDataPacket p) {
			switch (p.getPacketType()) {
				case EEG:
//					updateEeg(p.getValues());
//					Log.e("updateEeg", "fp1: " + String.format(
//							  "%6.2f", p.getValues().get(Eeg.FP1.ordinal())));
					break;
//				case ACCELEROMETER:
//					updateAccelerometer(p.getValues());
//					break;
//				case ALPHA_RELATIVE:
//					updateAlphaRelative(p.getValues());
//					break;
				case BATTERY:
					fileWriter.addDataPacket(1, p);
					// It's library client responsibility to flush the buffer,
					// otherwise you may get memory overflow.
					if (fileWriter.getBufferedMessagesSize() > 8096)
						fileWriter.flush();
					break;
				case BETA_ABSOLUTE:
					Log.d(TAG, "BETA_ABSOLUTE: " + p.getValues());
					break;
				case BETA_RELATIVE:
					Log.d(TAG, "BETA_RELATIVE: " + p.getValues());
					break;
				case BETA_SCORE:
					Log.d(TAG, "BETA_SCORE: " + p.getValues());
					break;
				case CONCENTRATION:
					Log.e(TAG, "CONCENTRATION" + p.getValues());
					eegConcentration = 100 - (int)Math.round((p.getValues().get(0) * 100));
					break;
				case MELLOW:
					Log.e(TAG, "MELLOW" + p.getValues());
					eegMellow = 100 - (int)Math.round((p.getValues().get(0) * 100));
					break;
				default:
					break;
			}
		}

		@Override
		public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
			if (p.getHeadbandOn() && p.getBlink()) {
				Log.i("Artifacts", "blink");
				broadcastEventEEG("eegBlink", String.valueOf(255));
			}
		}

//		private void updateAccelerometer(final ArrayList<Double> data) {
//
////			final View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_muse, mContainer, false);
//
////			Activity activity = activityRef.get();
////			if (activity != null) {
////				activity.runOnUiThread(new Runnable() {
////					@Override
////					public void run() {
////						acc_x = (TextView) v.findViewById(R.id.acc_x);
////						acc_y = (TextView) v.findViewById(R.id.acc_y);
////						acc_z = (TextView) v.findViewById(R.id.acc_z);
////						acc_x.setText(String.format(
////								  "%6.2f", data.get(Accelerometer.FORWARD_BACKWARD.ordinal())));
////						acc_y.setText(String.format(
////								  "%6.2f", data.get(Accelerometer.UP_DOWN.ordinal())));
////						acc_z.setText(String.format(
////								  "%6.2f", data.get(Accelerometer.LEFT_RIGHT.ordinal())));
////					}
////				});
////			}
//		}

//		private void updateEeg(final ArrayList<Double> data) {
//
////			Log.e("updateEeg", "fp1: " + String.format(
////					  "%6.2f", data.get(Eeg.FP1.ordinal())));
//
////			tp9.setText(String.format(
////					  "%6.2f", data.get(Eeg.TP9.ordinal())));
////			fp1.setText(String.format(
////					  "%6.2f", data.get(Eeg.FP1.ordinal())));
////			fp2.setText(String.format(
////					  "%6.2f", data.get(Eeg.FP2.ordinal())));
////			tp10.setText(String.format(
////					  "%6.2f", data.get(Eeg.TP10.ordinal())));
//
////			final View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_muse, mContainer, false);
//
////		Activity activity = activityRef.get();
////		if (activity != null) {
////			activity.runOnUiThread(new Runnable() {
////				@Override
////				public void run() {
//////						TextView tp9 = (TextView) v.findViewById(R.id.eeg_tp9);
//////						TextView fp1 = (TextView) v.findViewById(R.id.eeg_fp1);
//////						TextView fp2 = (TextView) v.findViewById(R.id.eeg_fp2);
//////						TextView tp10 = (TextView) v.findViewById(R.id.eeg_tp10);
////					tp9.setText(String.format(
////							  "%6.2f", data.get(Eeg.TP9.ordinal())));
////					fp1.setText(String.format(
////							  "%6.2f", data.get(Eeg.FP1.ordinal())));
////					fp2.setText(String.format(
////							  "%6.2f", data.get(Eeg.FP2.ordinal())));
////					tp10.setText(String.format(
////							  "%6.2f", data.get(Eeg.TP10.ordinal())));
////				}
////			});
////		}
//		}
//
//		private void updateAlphaRelative(final ArrayList<Double> data) {
////			final View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_muse, mContainer, false);
//
////			Activity activity = activityRef.get();
////			if (activity != null) {
////				activity.runOnUiThread(new Runnable() {
////					@Override
////					public void run() {
//////						elem1 = (TextView) v.findViewById(R.id.elem1);
//////						elem2 = (TextView) v.findViewById(R.id.elem2);
//////						elem3 = (TextView) v.findViewById(R.id.elem3);
//////						elem4 = (TextView) v.findViewById(R.id.elem4);
////						elem1.setText(String.format(
////								  "%6.2f", data.get(Eeg.TP9.ordinal())));
////						elem2.setText(String.format(
////								  "%6.2f", data.get(Eeg.FP1.ordinal())));
////						elem3.setText(String.format(
////								  "%6.2f", data.get(Eeg.FP2.ordinal())));
////						elem4.setText(String.format(
////								  "%6.2f", data.get(Eeg.TP10.ordinal())));
////					}
////				});
////			}
//		}

		public void setFileWriter(MuseFileWriter fileWriter) {
			this.fileWriter  = fileWriter;
		}


		private Muse muse = null;
		private ConnectionListener connectionListener = null;
		private DataListener dataListener = null;
		private boolean dataTransmission = true;
		private MuseFileWriter fileWriter = null;

//	public MainActivity() {
//		// Create listeners and pass reference to activity to them
//		WeakReference<Activity> weakActivity =
//				  new WeakReference<Activity>(this);
//
//		connectionListener = new ConnectionListener(weakActivity);
//		dataListener = new DataListener(weakActivity);
//	}

//	@Override
////	protected void onCreate(Bundle savedInstanceState) {
//	public void onCreate(Bundle savedInstanceState) {
//
////		final View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_muse, mContainer, false);
//
//
//		super.onCreate(savedInstanceState);
////		setContentView(R.layout.activity_main);
//
//
//
////		Button refreshButton = (Button) v.findViewById(R.id.refresh);
//////		refreshButton.setOnClickListener(this);
////		refreshButton.setOnClickListener(
////
////				  new View.OnClickListener() {
////					  @Override
////					  public void onClick(View v) {
////						  Spinner musesSpinner = (Spinner) v.findViewById(R.id.muses_spinner);
////
////						  {
////							  MuseManager.refreshPairedMuses();
////							  List<Muse> pairedMuses = MuseManager.getPairedMuses();
////							  List<String> spinnerItems = new ArrayList<String>();
////							  for (Muse m : pairedMuses) {
////								  String dev_id = m.getName() + "-" + m.getMacAddress();
////								  Log.i("Muse Headband", dev_id);
////								  spinnerItems.add(dev_id);
////							  }
////							  ArrayAdapter<String> adapterArray = new ArrayAdapter<String>(
////									    getActivity().getApplicationContext(), android.R.layout.simple_spinner_item, spinnerItems);
////							  musesSpinner.setAdapter(adapterArray);
////
////						  }
////					  }
////				  });
////		Button connectButton = (Button) v.findViewById(R.id.connect);
////		connectButton.setOnClickListener(this);
////		Button disconnectButton = (Button) v.findViewById(R.id.disconnect);
////		disconnectButton.setOnClickListener(this);
////		Button pauseButton = (Button) v.findViewById(R.id.pause);
////		pauseButton.setOnClickListener(this);
//		fileWriter = MuseManager.getMuseFileWriter(new File(
//				  android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + (Environment.DIRECTORY_DOCUMENTS) + "/",
//				  "testlibmusefile.muse"));
////				  getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
////				  "testlibmusefile.muse"));
//
////		filepath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
//
//
//		Log.i("Muse Headband", "libmuse version=" + LibMuseVersion.SDK_VERSION);
//		fileWriter.addAnnotationString(1, "MainActivity onCreate");
//		dataListener.setFileWriter(fileWriter);
//	}
//
//	@Override
//	public void onClick(View v) {
//
//		Log.e(TAG, "onClick");
//
////		Spinner musesSpinner = (Spinner) v.findViewById(R.id.muses_spinner);
//		if (v.getId() == R.id.refresh) {
//			MuseManager.refreshPairedMuses();
//			List<Muse> pairedMuses = MuseManager.getPairedMuses();
//			List<String> spinnerItems = new ArrayList<String>();
//			for (Muse m: pairedMuses) {
//				String dev_id = m.getName() + "-" + m.getMacAddress();
//				Log.i("Muse Headband", dev_id);
//				spinnerItems.add(dev_id);
//			}
//			ArrayAdapter<String> adapterArray = new ArrayAdapter<String> (
//					  getActivity().getApplicationContext(), android.R.layout.simple_spinner_item, spinnerItems);
//			musesSpinner.setAdapter(adapterArray);
//		}
//		else if (v.getId() == R.id.connect) {
//			List<Muse> pairedMuses = MuseManager.getPairedMuses();
//			if (pairedMuses.size() < 1 ||
//					  musesSpinner.getAdapter().getCount() < 1) {
//				Log.w("Muse Headband", "There is nothing to connect to");
//			}
//			else {
//				muse = pairedMuses.get(musesSpinner.getSelectedItemPosition());
//				ConnectionState state = muse.getConnectionState();
//				if (state == ConnectionState.CONNECTED ||
//						  state == ConnectionState.CONNECTING) {
//					Log.w("Muse Headband", "doesn't make sense to connect second time to the same muse");
//					return;
//				}
//				configure_library();
//				fileWriter.open();
//				fileWriter.addAnnotationString(1, "Connect clicked");
//				/**
//				 * In most cases libmuse native library takes care about
//				 * exceptions and recovery mechanism, but native code still
//				 * may throw in some unexpected situations (like bad bluetooth
//				 * connection). Print all exceptions here.
//				 */
//				try {
//					muse.runAsynchronously();
//				} catch (Exception e) {
//					Log.e("Muse Headband", e.toString());
//				}
//			}
//		}
//		else if (v.getId() == R.id.disconnect) {
//			if (muse != null) {
//				/**
//				 * true flag will force libmuse to unregister all listeners,
//				 * BUT AFTER disconnecting and sending disconnection event.
//				 * If you don't want to receive disconnection event (for ex.
//				 * you call disconnect when application is closed), then
//				 * unregister listeners first and then call disconnect:
//				 * muse.unregisterAllListeners();
//				 * muse.disconnect(false);
//				 */
//				muse.disconnect(true);
//				fileWriter.addAnnotationString(1, "Disconnect clicked");
//				fileWriter.flush();
//				fileWriter.close();
//			}
//		}
//		else if (v.getId() == R.id.pause) {
//			dataTransmission = !dataTransmission;
//			if (muse != null) {
//				muse.enableDataTransmission(dataTransmission);
//			}
//		}
//	}

//		private void configure_library() {
//			muse.registerConnectionListener(connectionListener);
//			muse.registerDataListener(dataListener,
//					  MuseDataPacketType.ACCELEROMETER);
//			muse.registerDataListener(dataListener,
//					  MuseDataPacketType.EEG);
//			muse.registerDataListener(dataListener,
//					  MuseDataPacketType.ALPHA_RELATIVE);
//			muse.registerDataListener(dataListener,
//					  MuseDataPacketType.ARTIFACTS);
//			muse.registerDataListener(dataListener,
//					  MuseDataPacketType.BATTERY);
//			muse.setPreset(MusePreset.PRESET_14);
//			muse.enableDataTransmission(dataTransmission);
//		}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getActivity().getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

	}

}