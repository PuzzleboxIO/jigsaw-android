package io.puzzlebox.jigsaw.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Dependencies
 * Available from: http://store.neurosky.com/products/developer-tools-3-android
 * jigsaw-android/jigsaw/libs/ThinkGear.jar
 * jigsaw-android/jigsaw/libs/libStreamSDK_v1.2.0.jar
 */
import com.neurosky.thinkgear.TGDevice;

import java.util.HashMap;

import io.puzzlebox.jigsaw.data.SessionSingleton;

public class NeuroSkyThinkGearService extends Service {

	private final static String TAG = NeuroSkyThinkGearService.class.getSimpleName();

	/**
	 * Configuration
	 */
	public final static int EEG_RAW_FREQUENCY = 512; // 512 Hz sample rate

	public static boolean eegConnected = false;
	public static boolean eegConnecting = false;

	public static int eegAttention = 0;
	public static int eegMeditation = 0;
	public static int eegPower = 0;
	public static int eegSignal = 0;

	public static int signalSignalMax = 100;

	public static int blinkRangeMax = 128;

	public final static boolean rawEnabled = true;
	public final static int EEG_RAW_HISTORY_SIZE = EEG_RAW_FREQUENCY; // number of points to plot in EEG history
	private static final Number[] rawEEG = new Number[EEG_RAW_HISTORY_SIZE];
	private static final int arrayIndex = 0;

	private ServiceHandler mServiceHandler;

	/**
	 * Bluetooth
	 */
	private static BluetoothAdapter bluetoothAdapter;

	/**
	 * NeuroSky ThinkGear Device
	 */
	private static TGDevice tgDevice;

	public NeuroSkyThinkGearService() {
	}

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

	@Override
	public void onCreate() {
		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		Looper mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

		createService();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Toast.makeText(this, "Connecting to NeuroSky EEG", Toast.LENGTH_SHORT).show();

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);

		connectHeadset();

//		http://developer.android.com/reference/android/app/Service.html#START_STICKY
//		If the system kills the service after onStartCommand() returns,
// recreate the service and call onStartCommand(), but do not
// redeliver the last intent. Instead, the system calls onStartCommand()
// with a null intent, unless there were pending intents to start
// the service, in which case, those intents are delivered. This is
// suitable for media players (or similar services) that are not
// executing commands, but running indefinitely and waiting for a job.
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private final Handler handlerThinkGear = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			parseEEG(msg);
			return true;
		}
	});

	public void createService() {
		/**
		 * Prepare Bluetooth and NeuroSky ThinkGear EEG interface
		 */

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (bluetoothAdapter != null) {

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
	}

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
						broadcastEventEEG("eegStatus", "STATE_CONNECTING");
						break;
					case TGDevice.STATE_CONNECTED:
						Log.d(TAG, "EEG Connected");
						eegConnecting = false;
						eegConnected = true;
						broadcastEventEEG("eegStatus", "STATE_CONNECTED");
						SessionSingleton.getInstance().resetSession();
						tgDevice.start();
						break;
					case TGDevice.STATE_NOT_FOUND:
						Log.d(TAG, "EEG headset not found");
						eegConnecting = false;
						eegConnected = false;
						broadcastEventEEG("eegStatus", "STATE_NOT_FOUND");
						break;
					case TGDevice.STATE_NOT_PAIRED:
						Log.d(TAG, "EEG headset not paired");
						eegConnecting = false;
						eegConnected = false;
						broadcastEventEEG("eegStatus", "STATE_NOT_PAIRED");
						break;
					case TGDevice.STATE_DISCONNECTED:
						Log.d(TAG, "EEG Disconnected");
						eegConnecting = false;
						eegConnected = false;
						broadcastEventEEG("eegStatus", "STATE_DISCONNECTED");
						disconnectHeadset();
						break;
				}
				break;
			case TGDevice.MSG_POOR_SIGNAL:
				eegSignal = calculateSignal(msg.arg1);
				processPacketEEG();
				break;
			case TGDevice.MSG_ATTENTION:
				eegAttention = msg.arg1;
				break;
			case TGDevice.MSG_MEDITATION:
				eegMeditation = msg.arg1;
				break;
			case TGDevice.MSG_BLINK:
				/**
				 * Strength of detected blink. The Blink Strength ranges
				 * from 1 (small blink) to 255 (large blink). Unless a blink
				 * occurred, nothing will be returned. Blinks are only
				 * calculated if PoorSignal is less than 51.
				 */
				Log.v(TAG, "Blink: " + msg.arg1 + "\n");
				broadcastEventEEG("eegBlink", String.valueOf(msg.arg1));
				break;
			case TGDevice.MSG_RAW_DATA:
				SessionSingleton.getInstance().appendRawEEG(msg.arg1);
				break;
			case TGDevice.MSG_RAW_COUNT:
				break;
			case TGDevice.MSG_RAW_MULTI:
				// TGRawMulti rawM = (TGRawMulti)msg.obj;
				// Log.d(TAG, "Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
			case TGDevice.MSG_HEART_RATE:
				// Log.d(TAG, "Heart rate: " + msg.arg1 + "\n");
				break;
			case TGDevice.MSG_LOW_BATTERY:
				broadcastEventEEG("eegStatus", "MSG_LOW_BATTERY");
				break;
			default:
				break;
		}
	}

	public void connectHeadset() {
		/**
		 * Called when the "Connect" button is pressed
		 */
		if(bluetoothAdapter == null) {
			// Alert user that Bluetooth is not available
			Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
		} else {
			if (tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED) {
				tgDevice.connect(rawEnabled);
			}
			else if (tgDevice.getState() == TGDevice.STATE_CONNECTED)
			/** "Disconnect" button was pressed */
				disconnectHeadset();
		}
	}

	public static void disconnectHeadset() {
		/**
		 * Called when "Disconnect" button is pressed
		 */
		eegConnecting = false;
		eegConnected = false;

		eegAttention = 0;
		eegMeditation = 0;
		eegSignal = 0;
		eegPower = 0;

		if (tgDevice.getState() == TGDevice.STATE_CONNECTED) {
			tgDevice.stop();
			tgDevice.close();
		}
	}

	public static int calculateSignal(int signal) {
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
				break;
			case 0:
				value = 100;
				break;
			default:
				value = (int)(100 - ((signal / 200.0) * 100));
				break;
		}
		return(value);
	}

	public void processPacketEEG() {
		try {
			SessionSingleton.getInstance().updateTimestamp();

			HashMap<String, String> packet;
			packet = new HashMap<>();

			packet.put("Date", SessionSingleton.getInstance().getCurrentDate());
			packet.put("Time", SessionSingleton.getInstance().getCurrentTimestamp());
			packet.put("Attention", String.valueOf(eegAttention));
			packet.put("Meditation", String.valueOf(eegMeditation));
			packet.put("Signal Level", String.valueOf(eegSignal));

			Log.v(TAG, "SessionSingleton.getInstance().appendData(packet): " + packet.toString());
			SessionSingleton.getInstance().appendData(packet);

			broadcastPacketEEG(packet);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private  void broadcastPacketEEG(HashMap<String, String> packet) {
		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.thinkgear.packet");

		intent.putExtra("Date", packet.get("Date"));
		intent.putExtra("Time", packet.get("Time"));
		intent.putExtra("Attention", packet.get("Attention"));
		intent.putExtra("Meditation", packet.get("Meditation"));
		intent.putExtra("Signal Level", packet.get("Signal Level"));

		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private  void broadcastEventEEG(String name, String value) {
		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.thinkgear.event");

		intent.putExtra("name", name);
		intent.putExtra("value", value);

		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
}
