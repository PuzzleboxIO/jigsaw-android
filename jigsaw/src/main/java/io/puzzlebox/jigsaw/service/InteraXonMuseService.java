package io.puzzlebox.jigsaw.service;

import android.app.Service;
import android.content.Intent;
//import android.os.*;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Dependencies
 * Available from: http://www.choosemuse.com/developer-kit/
 * jigsaw-android/jigsaw/libs/libmuseandroid.jar
 * jigsaw-android/jigsaw/src/main/jniLibs/armeabi/libmuse.so
 */
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
public class InteraXonMuseService extends Service {

	/**
	 * TODO
	 * - Handle NEEDS_UPDATE connection state
	 */

	private final static String TAG = InteraXonMuseService.class.getSimpleName();

	public final static int EEG_RAW_FREQUENCY = 220; // 220 Hz sample rate

	public static boolean eegConnected = false;
	public static boolean eegConnecting = false;

	public static int eegConcentration = 0;
	public static int eegMellow = 0;
	public static int eegSignal = 0;
	public static int eegPower = 0;
	public static String acc_x = "";
	public static String acc_y = "";
	public static String acc_z = "";

	private ServiceHandler mServiceHandler;

	private static Muse muse = null;
	private ConnectionListener connectionListener = null;
	private DataListener dataListener = null;
	private boolean dataTransmission = true;
	private static MuseFileWriter fileWriter = null;

	public final static boolean rawEnabled = true;
	public final static int EEG_RAW_HISTORY_SIZE = EEG_RAW_FREQUENCY; // number of points to plot in EEG history
	private static Number[] rawEEG = new Number[EEG_RAW_HISTORY_SIZE];
	private static int arrayIndex = 0;

	// ################################################################

	public InteraXonMuseService() {
//		connectionListener = new ConnectionListener();
//		dataListener = new DataListener();
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
		connectionListener = new ConnectionListener();
		dataListener = new DataListener();

		Log.i("Muse Headband", "libmuse version=" + LibMuseVersion.SDK_VERSION);


		try {
			// Requires API 19
			fileWriter = MuseManager.getMuseFileWriter(new File(
					  getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
					  "testlibmusefile.muse"));
		} catch (Exception e) {
			fileWriter = MuseManager.getMuseFileWriter(new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "testlibmusefile.muse"));
		}

		fileWriter.addAnnotationString(1, "MainActivity onCreate");
		dataListener.setFileWriter(fileWriter);


		Log.e(TAG, "MuseDataPacketType.HORSESHOE.ordinal()" + MuseDataPacketType.HORSESHOE.ordinal());


	}


	// ################################################################

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Toast.makeText(this, "Connecting to Muse EEG", Toast.LENGTH_SHORT).show();

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);


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

	public void connectHeadset() {

		/**
		 * Called when the "Connect" button is pressed
		 */

		Log.v(TAG, "connectHeadset()");

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

			ConnectionState state = muse.getConnectionState();
			if (state == ConnectionState.CONNECTED ||
					  state == ConnectionState.CONNECTING) {
				Log.w("Muse Headband", "doesn't make sense to connect second time to the same muse");
				return;
			}

			configure_library();

			fileWriter.open();
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
//		updatedCurrentMuse();

		muse.disconnect(true);
		fileWriter.addAnnotationString(1, "Disconnect clicked");
		fileWriter.flush();
		fileWriter.close();

	} // disconnectHeadset


	// ################################################################

	public void updatedCurrentMuse() {

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

		muse = pairedMuses.get(musesSpinner.getSelectedItemPosition());

	}

	// ################################################################

	public void processPacketEEG() {
		try {
			SessionSingleton.getInstance().updateTimestamp();

			HashMap<String, String> packet;
			packet = new HashMap<>();

			packet.put("Date", SessionSingleton.getInstance().getCurrentDate());
			packet.put("Time", SessionSingleton.getInstance().getCurrentTimestamp());
//			packet.put("Attention", String.valueOf(eegAttention));
//			packet.put("Meditation", String.valueOf(eegMeditation));
			packet.put("Attention", String.valueOf(eegConcentration));
			packet.put("Meditation", String.valueOf(eegMellow));

			packet.put("Signal Level", String.valueOf(eegSignal));
////			packet.put("Power", String.valueOf(eegPower));

			Log.v(TAG, "SessionSingleton.getInstance().appendData(packet): " + packet.toString());
			SessionSingleton.getInstance().appendData(packet);

			broadcastPacketEEG(packet);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	// ################################################################

	private  void broadcastPacketEEG(HashMap<String, String> packet) {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.thinkgear.packet");

		intent.putExtra("Date", packet.get("Date"));
		intent.putExtra("Time", packet.get("Time"));
		intent.putExtra("Attention", packet.get("Attention"));
		intent.putExtra("Meditation", packet.get("Meditation"));
		intent.putExtra("Signal Level", packet.get("Signal Level"));
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


	// ################################################################

	private void configure_library() {
		muse.registerConnectionListener(connectionListener);

		muse.registerDataListener(dataListener,
				  MuseDataPacketType.ARTIFACTS);
		muse.registerDataListener(dataListener,
				  MuseDataPacketType.BATTERY);
		muse.registerDataListener(dataListener,
				  MuseDataPacketType.HORSESHOE);

		muse.registerDataListener(dataListener,
				  MuseDataPacketType.CONCENTRATION);
		muse.registerDataListener(dataListener,
				  MuseDataPacketType.MELLOW);

		muse.registerDataListener(dataListener,
				  MuseDataPacketType.EEG);

//		muse.registerDataListener(dataListener,
//				  MuseDataPacketType.ACCELEROMETER);
//		muse.registerDataListener(dataListener,
//				  MuseDataPacketType.ALPHA_RELATIVE);

		// https://sites.google.com/a/interaxon.ca/muse-developer-site/museio/presets
		muse.setPreset(MusePreset.PRESET_14);
		muse.enableDataTransmission(dataTransmission);
	}


	// ################################################################

	/**
	 * Connection listener updates UI with new connection status and logs it.
	 */
	class ConnectionListener extends MuseConnectionListener {

		ConnectionListener() {
		}

		@Override
		public void receiveMuseConnectionPacket(MuseConnectionPacket p) {

			final ConnectionState current = p.getCurrentConnectionState();
			final String status = p.getPreviousConnectionState().toString() +
					  " -> " + current;
			final String full = "Muse " + p.getSource().getMacAddress() +
					  " " + status;
			Log.i("Muse Headband", full);

			MuseVersion museVersion = muse.getMuseVersion();
			String version = museVersion.getFirmwareType() +
					  " - " + museVersion.getFirmwareVersion() +
					  " - " + Integer.toString(
					  museVersion.getProtocolVersion());

			if (current == ConnectionState.CONNECTED) {
				eegConnected = true;
				eegConnecting = false;
				broadcastEventEEG("eegStatus", "STATE_CONNECTED");
			} else if (current == ConnectionState.CONNECTING) {
				eegConnected = false;
				eegConnecting = true;
				broadcastEventEEG("eegStatus", "STATE_CONNECTING");
			} else if (current == ConnectionState.DISCONNECTED) {
				eegConnected = false;
				eegConnecting = false;
				broadcastEventEEG("eegStatus", "STATE_DISCONNECTED");
			}

		}
	}


	// ################################################################

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

		private MuseFileWriter fileWriter = null;

		DataListener() {
		}

		@Override
		public void receiveMuseDataPacket(MuseDataPacket p) {

//			if (p.getPacketType() != MuseDataPacketType.EEG) {
//				Log.d(TAG, p.getValues().toString());
//			}

//			if (p.getPacketType().compareTo(MuseDataPacketType.HORSESHOE))

//			try {
////				Log.d(TAG, p.getValues().get(MuseDataPacketType.HORSESHOE.ordinal()).toString());
//				Log.d(TAG, "p.getValues().size(): " + p.getValues().size());
//			} catch (Exception e) {
////				e.printStackTrace();
//			}

			switch (p.getPacketType()) {
				case EEG:

					SessionSingleton.getInstance().appendRawEEG(
//							  String.format(
//										 "%6.2f", p.getValues().get(Eeg.FP1.ordinal()))
							  (int)Math.round(p.getValues().get(Eeg.FP1.ordinal()))
					);
//					updateEeg(p.getValues());
//					Log.i("updateEeg", "fp1: " + );
					break;
//				case ACCELEROMETER:
//					acc_x = String.format(
//							  "%6.2f", p.getValues().get(Accelerometer.FORWARD_BACKWARD.ordinal()));
//					acc_y = String.format(
//							  "%6.2f", p.getValues().get(Accelerometer.UP_DOWN.ordinal()));
//					acc_z = String.format(
//							  "%6.2f", p.getValues().get(Accelerometer.LEFT_RIGHT.ordinal()));
//					Log.i(TAG, "Accelerometer: (" + acc_x + "," + acc_y + "," + acc_z + ") (x,y,z)");
//					break;
//				case ALPHA_RELATIVE:
////					updateAlphaRelative(p.getValues());
//					Log.d(TAG, "ALPHA_RELATIVE " + p.getValues().get(Eeg.FP1.ordinal()));
//					break;
				case BATTERY:
					fileWriter.addDataPacket(1, p);
					// It's library client responsibility to flush the buffer,
					// otherwise you may get memory overflow.
					if (fileWriter.getBufferedMessagesSize() > 8096)
						fileWriter.flush();
					break;
//				case BETA_ABSOLUTE:
//					Log.d(TAG, "BETA_ABSOLUTE: " + p.getValues().toString());
//					break;
//				case BETA_RELATIVE:
//					Log.d(TAG, "BETA_RELATIVE: " + p.getValues());
//					break;
//				case BETA_SCORE:
//					Log.d(TAG, "BETA_SCORE: " + p.getValues());
//					break;
				case HORSESHOE:
//					Log.e(TAG , "HORSESHOE");
//					Log.e(TAG, "HORSESHOE: " + p.getValues().get(MuseDataPacketType.HORSESHOE.ordinal()));
//					Log.d(TAG, "HORSESHOE: " + p.getValues().toString());

					eegSignal = (int)Math.round(p.getValues().get(0) +
							  p.getValues().get(1) +
							  p.getValues().get(2) +
							  p.getValues().get(3));

//					eegSignal = Math.round( (eegSignal / 16 ) * 100 );
					eegSignal = (int)Math.round( (eegSignal / 16.0 ) * 100 );

//					Log.e(TAG, "eegSignal: " + eegSignal);
////					Log.e(TAG, "eegSignal: " + eegSignal / 16.0);

//					eegSignal = 0;

					processPacketEEG();

				case CONCENTRATION:
//					Log.e(TAG, "CONCENTRATION: " + p.getValues());

					if (p.getValues().size() == 1) {
						if (p.getValues().get(0) == 0.0)
							eegConcentration = 0;
						else
							eegConcentration = 100 - (int)Math.round((p.getValues().get(0) * 100));
					}

//					Log.i(TAG, "eegConcentration: " + eegConcentration);

//					eegConcentration = 100 - (int)Math.round((p.getValues().get(MuseDataPacketType.CONCENTRATION.ordinal()) * 100));
					break;
				case MELLOW:
//					Log.e(TAG, "MELLOW" + p.getValues().get(MuseDataPacketType.MELLOW.ordinal()));
//					Log.e(TAG, "MELLOW" + p.getValues().toString());
//					eegMellow = 100 - (int)Math.round((p.getValues().get(MuseDataPacketType.MELLOW.ordinal()) * 100));

					if (p.getValues().size() == 1) {
						if (p.getValues().get(0) == 0.0)
							eegMellow = 0;
						else
							eegMellow = 100 - (int)Math.round((p.getValues().get(0) * 100));
					}

					break;
				default:
					break;
			}
		}


		// ################################################################

		@Override
		public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
			if (p.getHeadbandOn() && p.getBlink()) {
//				Log.i("Artifacts", "blink");
				broadcastEventEEG("eegBlink", String.valueOf(255));
			}
		}



		// ################################################################

		public void setFileWriter(MuseFileWriter fileWriter) {
			this.fileWriter  = fileWriter;
		}

	}

}