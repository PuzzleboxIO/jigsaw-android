package io.puzzlebox.jigsaw.ui;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import io.puzzlebox.jigsaw.R;
import io.puzzlebox.jigsaw.data.DevicePuzzleboxOrbitSingleton;

//public class DialogOutputAudioIRFragment extends io.puzzlebox.jigsaw.ui.DialogOutputAudioIRFragment {
public class DialogOutputAudioIRFragment extends DialogFragment {

	private final static String TAG = DialogOutputAudioIRFragment.class.getSimpleName();

	public final static String profileID = "puzzlebox_orbit_ir";

	// UI
	public Switch switchDetectTransmitter;
	public Switch switchDetectVolume;
	public Switch switchInvertControlSignal;
	Button buttonDeviceEnable;
	Button buttonTestAudioIR;

	AudioManager audioManager;
	public int volumeMax;

	public boolean warningDetectTransmitterDisplayed = false;
	public boolean warningDetectVolumeDisplayed = false;

	private OnFragmentInteractionListener mListener;

	public DialogOutputAudioIRFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
									 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(io.puzzlebox.jigsaw.R.layout.dialog_output_audio_ir, container, false);

		getDialog().getWindow().setTitle( getString(io.puzzlebox.jigsaw.R.string.title_dialog_fragment_audio_ir));
//		getDialog().getWindow().setTitle( getString(R.string.label_audio_ir_instruction));

		switchDetectTransmitter = (Switch) v.findViewById(io.puzzlebox.jigsaw.R.id.switchDetectTransmitter);
		switchDetectTransmitter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchDetectTransmitterClicked(v);
			}
		});

		switchDetectVolume = (Switch) v.findViewById(io.puzzlebox.jigsaw.R.id.switchDetectVolume);
		switchDetectVolume.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchDetectVolumeClicked(v);
			}
		});

		switchInvertControlSignal = (Switch) v.findViewById(io.puzzlebox.jigsaw.R.id.switchInvertControlSignal);
		switchInvertControlSignal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchInvertControlSignalClicked(v);
			}
		});

		buttonTestAudioIR = (Button) v.findViewById(io.puzzlebox.jigsaw.R.id.buttonTestAudioIR);
		buttonTestAudioIR.setVisibility(View.VISIBLE);
		buttonTestAudioIR.setEnabled(true);
		buttonTestAudioIR.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				demoMode(v);
			}
		});

		Button buttonDeviceCancel = (Button) v.findViewById(io.puzzlebox.jigsaw.R.id.buttonDeviceCancel);
		buttonDeviceCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				broadcastTileStatus("false");
				dismiss();
			}
		});

		buttonDeviceEnable = (Button) v.findViewById(io.puzzlebox.jigsaw.R.id.buttonDeviceEnable);
		buttonDeviceEnable.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateTileStatus();
				dismissAudioIR();
				dismiss();
			}
		});

		audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		maximizeAudioVolume();

		/**
		 * PuzzleboxOrbitAudioIRHandler
		 */
//		if (!DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.isAlive()) {
//
//			try {
//				DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.start();
//			} catch (Exception e) {
//				Log.w(TAG, "Exception starting PuzzleboxOrbitAudioIRHandler: " + e.getStackTrace());
//
//				DevicePuzzleboxOrbitSingleton.getInstance().resetAudioHandler();
//
//			}
//		}
		DevicePuzzleboxOrbitSingleton.getInstance().startAudioHandler();


		return v;
	}


	// ################################################################

	public void demoMode(View v) {

		/**
		 * Demo mode is called when the "Test Helicopter" button is pressed.
		 * This method can be easily adjusted for testing new features
		 * during development.
		 */

		Log.v(TAG, "Test Signal clicked");

		if (! DevicePuzzleboxOrbitSingleton.getInstance().flightActive) {

			DevicePuzzleboxOrbitSingleton.getInstance().flightActive = true;
			DevicePuzzleboxOrbitSingleton.getInstance().demoActive = true;

			buttonTestAudioIR.setText( getResources().getString(R.string.buttonTestAudioIRStop) );

			playControl();

		} else {

			DevicePuzzleboxOrbitSingleton.getInstance().flightActive = false;
			DevicePuzzleboxOrbitSingleton.getInstance().demoActive = false;

			stopControl();

			buttonTestAudioIR.setText(getResources().getString(R.string.buttonTestAudioIR));

		}


	} // testFlight


	// ################################################################

	public void dismissAudioIR() {

		DevicePuzzleboxOrbitSingleton.getInstance().flightActive = false;
		DevicePuzzleboxOrbitSingleton.getInstance().demoActive = false;

		stopControl();

		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.shutdown();

	}


	// ################################################################

//	public void demoStop(View view) {
//
//		stopControl();
//
//	} // demoStop


	// ################################################################

	public void playControl() {

		Log.d(TAG, "playControl()");

//		Integer[] command =  {
//				  DevicePuzzleboxOrbitSingleton.getInstance().defaultControlThrottle,
//				  DevicePuzzleboxOrbitSingleton.getInstance().defaultControlYaw,
//				  DevicePuzzleboxOrbitSingleton.getInstance().defaultControlPitch,
//				  DevicePuzzleboxOrbitSingleton.getInstance().defaultChannel};
//
//		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.command = command;
//
//		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.updateControlSignal();


		DevicePuzzleboxOrbitSingleton.getInstance().resetControlSignal();


		DevicePuzzleboxOrbitSingleton.getInstance().flightActive = true;

		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.ifFlip = DevicePuzzleboxOrbitSingleton.getInstance().invertControlSignal; // if checked then flip

		int channel = DevicePuzzleboxOrbitSingleton.getInstance().defaultChannel;

		updateAudioHandlerLoopNumberWhileMindControl(-1); // Loop infinite for easier user testing

		updateAudioHandlerChannel(channel);

		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.mutexNotify();

	} // playControl


	// ################################################################

	public void stopControl() {

//		Log.d(TAG, "stopControl()");

		stopAudio();

		DevicePuzzleboxOrbitSingleton.getInstance().flightActive = false;

	} // stopControl


	// ################################################################

	public void stopAudio() {

		/**
		 * stop AudioTrack as well as destroy service.
		 */

		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.keepPlaying = false;

		/**
		 * Stop playing audio control file
		 */

		if (DevicePuzzleboxOrbitSingleton.getInstance().soundPool != null) {
			try {
				DevicePuzzleboxOrbitSingleton.getInstance().soundPool.stop(DevicePuzzleboxOrbitSingleton.getInstance().soundID);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


	} // stopControl

	// ################################################################

	/**
	 * @param number the puzzleboxOrbitAudioIRHandler to update loop number while mind control
	 */
	public void updateAudioHandlerLoopNumberWhileMindControl(int number) {

//		this.puzzleboxOrbitAudioIRHandler.loopNumberWhileMindControl = number;
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.loopNumberWhileMindControl = number;


	} // updateServiceBinderLoopNumberWhileMindControl


	// ################################################################

	/**
	 * the puzzleboxOrbitAudioIRHandler to update channel
	 */
	public void updateAudioHandlerChannel(int channel) {

		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.channel = channel;
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.updateControlSignal();


	} // updateServiceBinderChannel

	// ################################################################

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public interface OnFragmentInteractionListener {
		void onFragmentInteraction(Uri uri);
	}

	// ################################################################

	public void updateTileStatus() {

//		Log.e(TAG, "updateTileStatus()");

		String value;
		if (buttonDeviceEnable.isEnabled())
			value = "true";
		else
			value = "false";

		broadcastTileStatus(value);

	}

	// ################################################################

	public void broadcastTileStatus(String value) {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.tile.event");

		intent.putExtra("id", profileID);
		intent.putExtra("name", "active");
		intent.putExtra("value", value);
		intent.putExtra("category", "outputs");

//		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

	}

	// ################################################################

	public void onPause() {

		super.onPause();

		stopControl();

	} // onPause


	// ################################################################

	@Override
	public void onResume() {
		super.onResume();



//		audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
//		volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//
//		maximizeAudioVolume();
//
//		/**
//		 * PuzzleboxOrbitAudioIRHandler
//		 */
//		if (!DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.isAlive()) {
//			DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.start();
//		}



		if ((audioManager != null) &&
				  ((audioManager.isWiredHeadsetOn()))) {
			switchDetectTransmitter.setChecked(true);

			if (!checkVolumeMax()) {
				Log.d(TAG, "Attempting to set Media volume to max");
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeMax, 0);
			}

			if (checkVolumeMax()) {
				switchDetectVolume.setChecked(true);
			}

		}

		DevicePuzzleboxOrbitSingleton.getInstance().resetControlSignal();

		updateReadyButton();

	}

	// ################################################################

	public void switchDetectTransmitterClicked(View v) {
		Log.v(TAG, "switchDetectTransmitterClicked: " + switchDetectTransmitter.isChecked());

		if (switchDetectVolume.isChecked()) {
			// Volume must be turned up to max each time transmitter is connected
			switchDetectVolume.setChecked(false);
		}

		// When Audio IR is not prepared hide Ready button from view
		buttonDeviceEnable.setEnabled(false);
		buttonDeviceEnable.setVisibility(View.INVISIBLE);

		// Check to see if headphone jack detects transmitter and if not Toast a warning message
		if (! audioManager.isWiredHeadsetOn() &&
				  (! warningDetectTransmitterDisplayed)) {
			Toast.makeText(getActivity().getApplicationContext(), getString(io.puzzlebox.jigsaw.R.string.toast_audio_ir_detect_transmitter_warning), Toast.LENGTH_LONG).show();
			warningDetectTransmitterDisplayed = true;
		}

//		Log.i("WiredHeadsetOn = ", audioManager.isWiredHeadsetOn()+"");
//		Log.i("MusicActive = ", audioManager.isMusicActive()+"");
//		Log.i("SpeakerphoneOn = ", audioManager.isSpeakerphoneOn()+"");

	}

	// ################################################################

	public void switchDetectVolumeClicked(View v) {
		Log.v(TAG, "switchDetectVolumeClicked: " + switchDetectVolume.isChecked());

		// Check to see if volume is max and if not Toast a warning message
		if ((! checkVolumeMax()) &&
				  (switchDetectVolume.isChecked()) &&
				  (! warningDetectVolumeDisplayed)) {
			Toast.makeText(getActivity().getApplicationContext(), getString(io.puzzlebox.jigsaw.R.string.toast_audio_ir_detect_volume_max_warning), Toast.LENGTH_LONG).show();
			warningDetectVolumeDisplayed = true;
		}

		updateReadyButton();

	}

	public void switchInvertControlSignalClicked(View v) {
		Log.v(TAG, "switchInvertControlSignalClicked: " + switchInvertControlSignal.isChecked());

		// Invert the audio wave comprising the control signal before it is sent to the IR transmitter.
		// This is necessary for some audio hardware.
		// For details and an example see: https://puzzlebox.io/orbit/development/wiki/AddingSupportIR

		DevicePuzzleboxOrbitSingleton.getInstance().invertControlSignal = switchInvertControlSignal.isChecked();

	}


	public boolean checkVolumeMax() {
		boolean value = false;
		if ((audioManager != null) &&
				  (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) ==
							 volumeMax)) {
//							 audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))) {
			value = true;
		}
		else {
			Log.i(TAG, "getStreamVolume(AudioManager.STREAM_MUSIC):" +
					  audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
			Log.i(TAG, "getStreamMaxVolume(AudioManager.STREAM_MUSIC):" +
					  audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		}

		return value;
	}

	public void updateReadyButton() {
		if ((switchDetectVolume.isChecked()) && (switchDetectTransmitter.isChecked())) {
			// Once Audio IR is prepared present Ready button
			buttonDeviceEnable.setEnabled(true);
			buttonDeviceEnable.setVisibility(View.VISIBLE);
		} else {
			// When Audio IR is not prepared hide Ready button from view
			buttonDeviceEnable.setEnabled(false);
			buttonDeviceEnable.setVisibility(View.INVISIBLE);
		}
	}


	// ################################################################

	// TODO
	public void maximizeAudioVolume() {

		AudioManager audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

		if (currentVolume < audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {

			Log.v(TAG, "Previous volume:" + currentVolume);

			Toast.makeText(getActivity().getApplicationContext(), "Automatically setting volume to maximum", Toast.LENGTH_SHORT).show();

			AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					  audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
					  AudioManager.FLAG_SHOW_UI);

		}


	} // maximizeAudioVolume


	// ################################################################



}
