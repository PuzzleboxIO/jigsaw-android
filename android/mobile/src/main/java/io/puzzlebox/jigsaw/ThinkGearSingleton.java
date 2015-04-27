package io.puzzlebox.jigsaw;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;

import java.util.HashMap;

import io.puzzlebox.jigsaw.data.SessionSingleton;

/**
 * Created by sc on 4/24/15.
 */
public class ThinkGearSingleton {

	private static final String TAG = ThinkGearSingleton.class.getSimpleName();

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

//	int[] thresholdValuesAttention = new int[101];
//	int[] thresholdValuesMeditation = new int[101];
//	int minimumPower = 0; // minimum power for the trigger
//	int maximumPower = 100; // maximum power for the trifgger

	public final int EEG_RAW_HISTORY_SIZE = 512;            // number of points to plot in EEG history

//	/**
//	 * Bluetooth
//	 */
//	BluetoothAdapter bluetoothAdapter;
//	//	ArrayList<String> pairedBluetoothDevices;
//
//	/**
//	 * NeuroSky ThinkGear Device
//	 */
//	TGDevice tgDevice;
//	int tgSignal = 0;
	//	final boolean rawEnabled = false;
	final boolean rawEnabled = true;


//	private static OnThinkGearListener mListenerThinkGear;

	private static ThinkGearSingleton ourInstance = new ThinkGearSingleton();


	public static ThinkGearSingleton getInstance() {
		return ourInstance;
	}

	private ThinkGearSingleton() {
	}

//	private final Handler handlerThinkGear = new Handler() {
//
//		/**
//		 * Handles data packets from NeuroSky ThinkGear device
//		 */
//
//		public void handleMessage(Message msg) {
//
//			parseEEG(msg);
//
//		}
//
//	}; // handlerThinkGear

//	public interface OnThinkGearListener {
//		public void processPacketThinkGear(String msg);
//	}

////	@Override
//	public void onAttach(Activity activity) {
////		super.onAttach(activity);
//		try {
//			mListenerThinkGear = (OnThinkGearListener) activity;
//		} catch (ClassCastException e) {
//			throw new ClassCastException(activity.toString() + " must implement OnThinkGearListener");
//		}
//	}


//	public View onCreateView(View v, LayoutInflater inflater, ViewGroup container,
//	             Bundle savedInstanceState) {
////		/**
////		 * Prepare Bluetooth and NeuroSky ThinkGear EEG interface
////		 */
////
////		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
////
////		if (bluetoothAdapter == null) {
////			// Alert user that Bluetooth is not available
////			// TODO Fix for Fragment Context
//////			Toast.makeText(((OrbitTabActivity) getActivity()), "Bluetooth not available", Toast.LENGTH_LONG).show();
//////		Toast.makeText((getActivity()), "Bluetooth not available", Toast.LENGTH_LONG).show();
////			Toast.makeText((v.getContext()), "Bluetooth not available", Toast.LENGTH_LONG).show();
////
////		} else {
////			/** create the TGDevice */
////			tgDevice = new TGDevice(bluetoothAdapter, handlerThinkGear);
////
////			/** Retrieve a list of paired Bluetooth adapters */
////			//			Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
////			//			pairedBluetoothDevices = new ArrayList<String>(Arrays.asList(pairedDevices.toString()));
////			/**
////			 * NOTE: To get device names iterate through pairedBluetoothDevices
////			 * and call the getName() method on each BluetoothDevice object.
////			 */
////		}
//
//
//		return v;
//
//
//	}


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
////						updateStatusImage(); TODO
//						break;
//					case TGDevice.STATE_CONNECTED:
//						Log.d(TAG, "EEG Connected");
////						setButtonText(R.id.buttonConnectEEG, "Disconnect EEG"); TODO
//						eegConnecting = false;
//						eegConnected = true;
////						updateStatusImage(); TODO
//						SessionSingleton.getInstance().resetSession();
//						tgDevice.start();
//						break;
//					case TGDevice.STATE_NOT_FOUND:
//						Log.d(TAG, "EEG headset not found");
//						eegConnecting = false;
//						eegConnected = false;
////						updateStatusImage(); TODO
//						break;
//					case TGDevice.STATE_NOT_PAIRED:
//						Log.d(TAG, "EEG headset not paired");
//						eegConnecting = false;
//						eegConnected = false;
////						updateStatusImage(); TODO
//						break;
//					case TGDevice.STATE_DISCONNECTED:
//						Log.d(TAG, "EEG Disconnected");
//						eegConnecting = false;
//						eegConnected = false;
////						updateStatusImage(); TODO
//						disconnectHeadset();
//						break;
//				}
//
//				break;
//
//			case TGDevice.MSG_POOR_SIGNAL:
//				eegSignal = calculateSignal(msg.arg1);
////				progressBarSignal.setProgress(eegSignal); TODO
////				updateStatusImage(); TODO
//				processPacketEEG();
//				break;
//			case TGDevice.MSG_ATTENTION:
//				eegAttention = msg.arg1;
////				progressBarAttention.setProgress(eegAttention); TODO
////				updatePower();
//				break;
//			case TGDevice.MSG_MEDITATION:
//				eegMeditation = msg.arg1;
////				if (DEBUG)
////					Log.v(TAG, "Meditation: " + eegMeditation);
////				progressBarMeditation.setProgress(eegMeditation); TODO
////				updatePower();
////				processPacketEEG();
//				break;
//			case TGDevice.MSG_BLINK:
//				/**
//				 * Strength of detected blink. The Blink Strength ranges
//				 * from 1 (small blink) to 255 (large blink). Unless a blink
//				 * occurred, nothing will be returned. Blinks are only
//				 * calculated if PoorSignal is less than 51.
//				 */
//				Log.d(TAG, "Blink: " + msg.arg1 + "\n");
//				break;
//			case TGDevice.MSG_RAW_DATA:
//
//				rawEEG[arrayIndex] = msg.arg1;
//				arrayIndex = arrayIndex + 1;
//
//				if (arrayIndex == EEG_RAW_HISTORY_SIZE - 1) {
////					updateEEGRawHistory(rawEEG);
//					arrayIndex = 0; // TODO should pass data to other fragments
//				}
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
//				// TODO Fragment Context
////				Toast.makeText((getActivity()), "EEG battery low!", Toast.LENGTH_SHORT).show();
//				break;
//			default:
//				break;
//		}
//
//	} // handleMessage


	public void resetRawEEG() {
		rawEEG = new Number[512];
		arrayIndex = 0;
	}


//	// ################################################################
//
//	//	public void connectHeadset(View view) {
//	public void connectHeadset() {
//
//		/**
//		 * Called when the "Connect" button is pressed
//		 */
//
//		Log.v(TAG, "connectHeadset()");
//
//		/** Stop audio stream */
//		// TODO Fragment Context
////		((OrbitTabActivity)getActivity()).stopControl();
////		( getActivity() ).stopControl();
//
//		if(bluetoothAdapter == null) {
//
//			// Alert user that Bluetooth is not available
//			// TODO
////			Toast.makeText(((OrbitTabActivity)getActivity()), "Bluetooth not available", Toast.LENGTH_LONG).show();
//
//		} else {
//
//			if (tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED) {
//				tgDevice.connect(rawEnabled);
//				// TODO
////				((OrbitTabActivity)getActivity()).maximizeAudioVolume(); // Automatically set media volume to maximum
////				( getActivity() ).maximizeAudioVolume(); // Automatically set media volume to maximum
//			}
//
//
//			else if (tgDevice.getState() == TGDevice.STATE_CONNECTED)
//			/** "Disconnect" button was pressed */
//				disconnectHeadset();
//
//		}
//
//	} // connectHeadset
//
//
//	// ################################################################
//
//	public void disconnectHeadset() {
//
//		/**
//		 * Called when "Disconnect" button is pressed
//		 */
//
//		eegConnecting = false;
//		eegConnected = false;
//
//		eegAttention = 0;
//		eegMeditation = 0;
//		eegSignal = 0;
//		eegPower = 0;
//
////		updateStatusImage(); TODO
//
////		progressBarAttention.setProgress(eegAttention);
////		progressBarMeditation.setProgress(eegMeditation);
////		progressBarSignal.setProgress(eegSignal);
////		progressBarPower.setProgress(eegPower);
//
//		//TODO Fragment Context
////		String id = ((OrbitTabActivity)getActivity()).getTabFragmentAdvanced();
////
////		FragmentTabAdvanced fragmentAdvanced =
////				  (FragmentTabAdvanced) getFragmentManager().findFragmentByTag(id);
////
////		if (fragmentAdvanced != null) {
////			fragmentAdvanced.progressBarAttention.setProgress(eegAttention);
////			fragmentAdvanced.progressBarMeditation.setProgress(eegMeditation);
////			fragmentAdvanced.progressBarSignal.setProgress(eegSignal);
////			fragmentAdvanced.progressBarPower.setProgress(eegPower);
////		}
//
//		//TODO Broken or missing classes
////		setButtonText(R.id.buttonConnectEEG, "Connect"); TODO
//
//
//		if (tgDevice.getState() == TGDevice.STATE_CONNECTED) {
//			tgDevice.stop();
//			tgDevice.close();
//
//			// TODO Fragment Context
////		((OrbitTabActivity)getActivity()).stopControl();
////		(getActivity()).stopControl();
//
////			disconnectHeadset();
//
//		}
//
//
//	} // disconnectHeadset
//
//
//	// ################################################################
//
//	public int calculateSignal(int signal) {
//
//		/**
//		 * The ThinkGear protocol states that a signal level of 200 will be
//		 * returned when a clean ground/reference is not detected at the ear clip,
//		 *  and a value of 0 when the signal is perfectly clear. We need to
//		 *  convert this information into usable settings for the Signal
//		 *  progress bar
//		 */
//
//		int value;
//
//		switch (signal) {
//			case 200:
//				value = 0;
//			case 0:
//				value = 100;
//			default:
//				value = (int)(100 - ((signal / 200.0) * 100));
//		}
//
//		return(value);
//
//	} // calculateSignal
//
//
//	// ################################################################
//
//
//
//	public void updateStatusImage() {
//
//		if(DEBUG) {
//			Log.v(TAG, (new StringBuilder("Attention: ")).append(eegAttention).toString());
//			Log.v(TAG, (new StringBuilder("Meditation: ")).append(eegMeditation).toString());
//			Log.v(TAG, (new StringBuilder("Power: ")).append(eegPower).toString());
//			Log.v(TAG, (new StringBuilder("Signal: ")).append(eegSignal).toString());
//			Log.v(TAG, (new StringBuilder("Connecting: ")).append(eegConnecting).toString());
//			Log.v(TAG, (new StringBuilder("Connected: ")).append(eegConnected).toString());
//		}
////
////		if(eegPower > 0) {
////			imageViewStatus.setImageResource(R.drawable.status_4_active);
////			return;
////		}
////
////		if(eegSignal > 90) {
////			imageViewStatus.setImageResource(R.drawable.status_3_processing);
////			return;
////		}
////
////		if(eegConnected) {
////			imageViewStatus.setImageResource(R.drawable.status_2_connected);
////			return;
////		}
////
////		if(eegConnecting) {
////			imageViewStatus.setImageResource(R.drawable.status_1_connecting);
////			return;
////		} else {
////			imageViewStatus.setImageResource(R.drawable.status_default);
////			return;
////		}
//
//	} // updateStatusImage
//
//
//	// ################################################################
//
//	public void processPacketEEG() {
//		try {
////			Log.e(TAG, "SessionSingleton.getInstance().updateTimestamp");
//			SessionSingleton.getInstance().updateTimestamp();
//
//			HashMap packet = new HashMap();
//
//			packet.put("Date", SessionSingleton.getInstance().getCurrentDate());
//			packet.put("Time", SessionSingleton.getInstance().getCurrentTimestamp());
////			packet.put("Attention", eegAttention);
//			packet.put("Attention", String.valueOf(eegAttention));
////			packet.put("Meditation", eegMeditation);
//			packet.put("Meditation", String.valueOf(eegMeditation));
////			packet.put("Signal Level", eegSignal);
//			packet.put("Signal Level", String.valueOf(eegSignal));
////			packet.put("Power", eegPower);
//			packet.put("Power", String.valueOf(eegPower));
//
//			Log.d(TAG, "SessionSingleton.getInstance().appendData(packet): " + packet.toString());
//			SessionSingleton.getInstance().appendData(packet);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}


}
