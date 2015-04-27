package io.puzzlebox.jigsaw;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.neurosky.thinkgear.TGDevice;

import java.util.HashMap;

import io.puzzlebox.jigsaw.data.SessionSingleton;

public class ThinkGearService extends Service {

	private final static String TAG = ThinkGearService.class.getSimpleName();

	/**
	 * Configuration
	 */

	//	boolean DEBUG = true;
	boolean DEBUG = false;

	boolean eegConnected = false;
	boolean eegConnecting = false;

	int eegAttention = 0;
	int eegMeditation = 0;
	int eegPower = 0;
	int eegSignal = 0;

	final boolean rawEnabled = true;
	public final int EEG_RAW_HISTORY_SIZE = 512;            // number of points to plot in EEG history
	Number[] rawEEG = new Number[EEG_RAW_HISTORY_SIZE];
	int arrayIndex = 0;

	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;

	/**
	 * Bluetooth
	 */
	BluetoothAdapter bluetoothAdapter;
	//	ArrayList<String> pairedBluetoothDevices;

	/**
	 * NeuroSky ThinkGear Device
	 */
	TGDevice tgDevice;


	// ################################################################

	public ThinkGearService() {
	}

	private final IBinder binder = new ThinkGearBinder();

//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
//
//		createService();
//
//		return super.onStartCommand(intent,flags,startId);
//	}

//	@Override
//	public IBinder onBind(Intent intent) {
//		// TODO: Return the communication channel to the service.
////		throw new UnsupportedOperationException("Not yet implemented");
//		return binder;
//	}

	public class ThinkGearBinder extends Binder {
		ThinkGearService getService() {
			return ThinkGearService.this;
		}
	} // ThinkGearBinder


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
		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				  Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);


		createService();

	}


	// ################################################################

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);


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
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
	}


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

	//	public void createService(View v) {
	public void createService() {
		/**
		 * Prepare Bluetooth and NeuroSky ThinkGear EEG interface
		 */

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (bluetoothAdapter == null) {
			// Alert user that Bluetooth is not available
			// TODO Fix for Fragment Context
//			Toast.makeText(((OrbitTabActivity) getActivity()), "Bluetooth not available", Toast.LENGTH_LONG).show();
//		Toast.makeText((getActivity()), "Bluetooth not available", Toast.LENGTH_LONG).show();
//			Toast.makeText((v.getContext()), "Bluetooth not available", Toast.LENGTH_LONG).show();
//			Toast.makeText((v.getContext()), "Bluetooth not available", Toast.LENGTH_LONG).show(); TODO

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
	}


	// ################################################################

	public void parseEEG(Message msg) {

		switch (msg.what) {

			case TGDevice.MSG_STATE_CHANGE:

				switch (msg.arg1) {
					case TGDevice.STATE_IDLE:
						break;
					case TGDevice.STATE_CONNECTING:
						Log.d(TAG, "Connecting to EEG");
//						eegConnecting = true;
//						eegConnected = false;
						eegConnecting = true;
						eegConnected = false;
//						updateStatusImage(); TODO
						break;
					case TGDevice.STATE_CONNECTED:
						Log.d(TAG, "EEG Connected");
//						setButtonText(R.id.buttonConnectEEG, "Disconnect EEG"); TODO
//						eegConnecting = false;
//						eegConnected = true;
						eegConnecting = false;
						eegConnected = true;
//						updateStatusImage(); TODO
						SessionSingleton.getInstance().resetSession();
						tgDevice.start();
						break;
					case TGDevice.STATE_NOT_FOUND:
						Log.d(TAG, "EEG headset not found");
//						eegConnecting = false;
//						eegConnected = false;
						eegConnecting = false;
						eegConnected = false;
//						updateStatusImage(); TODO
						break;
					case TGDevice.STATE_NOT_PAIRED:
						Log.d(TAG, "EEG headset not paired");
//						eegConnecting = false;
//						eegConnected = false;
						eegConnecting = false;
						eegConnected = false;
//						updateStatusImage(); TODO
						break;
					case TGDevice.STATE_DISCONNECTED:
						Log.d(TAG, "EEG Disconnected");
//						eegConnecting = false;
//						eegConnected = false;
						eegConnecting = false;
						eegConnected = false;
//						updateStatusImage(); TODO
						disconnectHeadset();
						break;
				}

				break;

			case TGDevice.MSG_POOR_SIGNAL:
//				eegSignal = calculateSignal(msg.arg1);
				eegSignal = calculateSignal(msg.arg1);
//				progressBarSignal.setProgress(eegSignal); TODO
//				updateStatusImage(); TODO
				processPacketEEG();
				break;
			case TGDevice.MSG_ATTENTION:
//				eegAttention = msg.arg1;
				eegAttention = msg.arg1;
//				progressBarAttention.setProgress(eegAttention); TODO
//				updatePower();
				break;
			case TGDevice.MSG_MEDITATION:
//				eegMeditation = msg.arg1;
				eegMeditation = msg.arg1;
//				if (DEBUG)
//					Log.v(TAG, "Meditation: " + eegMeditation);
//				progressBarMeditation.setProgress(eegMeditation); TODO
//				updatePower();
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

				// TODO Raw disabled for now
//				rawEEG[arrayIndex] = msg.arg1;
//				arrayIndex = arrayIndex + 1;
//
//				if (arrayIndex == EEG_RAW_HISTORY_SIZE - 1) {
////					updateEEGRawHistory(rawEEG);
//					arrayIndex = 0; // TODO should pass data to other fragments
//				}

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
//				Toast.makeText((getActivity()), "EEG battery low!", Toast.LENGTH_SHORT).show();
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
//				tgDevice.connect(rawEnabled);
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

//		eegConnecting = false;
//		eegConnected = false;
//
//		eegAttention = 0;
//		eegMeditation = 0;
//		eegSignal = 0;
//		eegPower = 0;

		eegConnecting = false;
		eegConnected = false;

		eegAttention = 0;
		eegMeditation = 0;
		eegSignal = 0;
		eegPower = 0;

//		updateStatusImage(); TODO

//		progressBarAttention.setProgress(eegAttention);
//		progressBarMeditation.setProgress(eegMeditation);
//		progressBarSignal.setProgress(eegSignal);
//		progressBarPower.setProgress(eegPower);

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
//		setButtonText(R.id.buttonConnectEEG, "Connect"); TODO


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

	public void processPacketEEG() {
		try {
//			Log.e(TAG, "SessionSingleton.getInstance().updateTimestamp");
			SessionSingleton.getInstance().updateTimestamp();

			HashMap packet = new HashMap();

			packet.put("Date", SessionSingleton.getInstance().getCurrentDate());
			packet.put("Time", SessionSingleton.getInstance().getCurrentTimestamp());
			packet.put("Attention", String.valueOf(eegAttention));
			packet.put("Meditation", String.valueOf(eegMeditation));
			packet.put("Signal Level", String.valueOf(eegSignal));
			packet.put("Power", String.valueOf(eegPower));

			Log.d(TAG, "SessionSingleton.getInstance().appendData(packet): " + packet.toString());
			SessionSingleton.getInstance().appendData(packet);

			broadcastPacketEEG(packet);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ################################################################

	private void broadcastPacketEEG(HashMap packet) {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.thinkgear.packet");

		intent.putExtra("Date", packet.get("Date").toString());
		intent.putExtra("Time", packet.get("Time").toString());
		intent.putExtra("Attention", packet.get("Attention").toString());
		intent.putExtra("Meditation", packet.get("Meditation").toString());
		intent.putExtra("Signal Level", packet.get("Signal Level").toString());
		intent.putExtra("Power", packet.get("Power").toString());

		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

	}


}
