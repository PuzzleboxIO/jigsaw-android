package io.puzzlebox.jigsaw.ui;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import io.puzzlebox.jigsaw.R;

public class DialogAudioIRFragment extends DialogFragment {

	private final static String TAG = DialogJoystickFragment.class.getSimpleName();

	// UI
	public Switch switchDetectTransmitter;
	public Switch switchDetectVolume;
	Button buttonDeviceEnable;

	AudioManager audioManager;
	public int volumeMax;

	public boolean warningDetectTransmitterDisplayed = false;
	public boolean warningDetectVolumeDisplayed = false;

	private OnFragmentInteractionListener mListener;

	public DialogAudioIRFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
									 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.dialog_output_audio_ir, container, false);

		switchDetectTransmitter = (Switch) v.findViewById(R.id.switchDetectTransmitter);
		switchDetectTransmitter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchDetectTransmitterClicked(v);
			}
		});

		switchDetectVolume = (Switch) v.findViewById(R.id.switchDetectVolume);
		switchDetectVolume.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchDetectVolumeClicked(v);
			}
		});

		Button buttonDeviceCancel = (Button) v.findViewById(R.id.buttonDeviceCancel);
		buttonDeviceCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		buttonDeviceEnable = (Button) v.findViewById(R.id.buttonDeviceEnable);
		buttonDeviceEnable.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

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

	@Override
	public void onResume() {
		super.onResume();

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
			Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_audio_ir_detect_transmitter_warning), Toast.LENGTH_LONG).show();
			warningDetectTransmitterDisplayed = true;
		}

//		Log.i("WiredHeadsetOn = ", audioManager.isWiredHeadsetOn()+"");
//		Log.i("MusicActive = ", audioManager.isMusicActive()+"");
//		Log.i("SpeakerphoneOn = ", audioManager.isSpeakerphoneOn()+"");

	}

	public void switchDetectVolumeClicked(View v) {
		Log.v(TAG, "switchDetectVolumeClicked: " + switchDetectVolume.isChecked());

		// Check to see if volume is max and if not Toast a warning message
		if ((! checkVolumeMax()) &&
				  (switchDetectVolume.isChecked()) &&
				  (! warningDetectVolumeDisplayed)) {
			Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_audio_ir_detect_volume_max_warning), Toast.LENGTH_LONG).show();
			warningDetectVolumeDisplayed = true;
		}

		updateReadyButton();

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
