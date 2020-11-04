package io.puzzlebox.jigsaw.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import io.puzzlebox.jigsaw.data.DevicePuzzleboxOrbitSingleton;
import io.puzzlebox.jigsaw.data.ProfileSingleton;
import io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService;
import io.puzzlebox.jigsaw.R;

import static io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService.eegConnected;
import static io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService.eegConnecting;
import static io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService.eegSignal;

public class DialogProfilePuzzleboxOrbitFragment extends DialogFragment
		implements SeekBar.OnSeekBarChangeListener {

	private final static String TAG = DialogProfilePuzzleboxOrbitFragment.class.getSimpleName();

	public final static String profileID = "profile_puzzlebox_orbit_mobile_edition";

	/**
	 * Configuration
	 */
	public int eegPower = 0;

	// UI
	Button buttonDeviceEnable;

	ProgressBar progressBarAttention;
	SeekBar seekBarAttention;
	ProgressBar progressBarMeditation;
	SeekBar seekBarMeditation;
	ProgressBar progressBarSignal;
	ProgressBar progressBarPower;

	Button buttonTestFlight;
	Button buttonResetFlight;

	TextView textViewScore;
	TextView textViewLastScore;
	TextView textViewHighScore;

	ImageView imageViewStatus;

	int[] thresholdValuesAttention = new int[101];
	int[] thresholdValuesMeditation = new int[101];
	int minimumPower = 0; // minimum power for the bloom
	int maximumPower = 100; // maximum power for the bloom

	private OnFragmentInteractionListener mListener;

	public DialogProfilePuzzleboxOrbitFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.dialog_profile_puzzlebox_orbit, container, false);

		getDialog().getWindow().setTitle( getString(R.string.title_dialog_fragment_puzzlebox_orbit));

		buttonTestFlight = v.findViewById(R.id.buttonTestFlight);
		buttonTestFlight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				testFlight(v);
			}
		});

		buttonResetFlight = v.findViewById(R.id.buttonResetFlight);
		buttonResetFlight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetFlight(v);
			}
		});

		progressBarAttention = v.findViewById(R.id.progressBarAttention);
		final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
		ShapeDrawable progressBarAttentionDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarAttentionColor = "#FF0000";
		progressBarAttentionDrawable.getPaint().setColor(Color.parseColor(progressBarAttentionColor));
		ClipDrawable progressAttention = new ClipDrawable(progressBarAttentionDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarAttention.setProgressDrawable(progressAttention);
		progressBarAttention.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarMeditation = v.findViewById(R.id.progressBarMeditation);
		ShapeDrawable progressBarMeditationDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarMeditationColor = "#0000FF";
		progressBarMeditationDrawable.getPaint().setColor(Color.parseColor(progressBarMeditationColor));
		ClipDrawable progressMeditation = new ClipDrawable(progressBarMeditationDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarMeditation.setProgressDrawable(progressMeditation);
		progressBarMeditation.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarSignal = v.findViewById(R.id.progressBarSignal);
		ShapeDrawable progressBarSignalDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarSignalColor = "#00FF00";
		progressBarSignalDrawable.getPaint().setColor(Color.parseColor(progressBarSignalColor));
		ClipDrawable progressSignal = new ClipDrawable(progressBarSignalDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarSignal.setProgressDrawable(progressSignal);
		progressBarSignal.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarPower = v.findViewById(R.id.progressBarPower);
		ShapeDrawable progressBarPowerDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarPowerColor = "#FFFF00";
		progressBarPowerDrawable.getPaint().setColor(Color.parseColor(progressBarPowerColor));
		ClipDrawable progressPower = new ClipDrawable(progressBarPowerDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarPower.setProgressDrawable(progressPower);
		progressBarPower.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		seekBarAttention = v.findViewById(R.id.seekBarAttention);
		seekBarAttention.setOnSeekBarChangeListener(this);
		seekBarMeditation = v.findViewById(R.id.seekBarMeditation);
		seekBarMeditation.setOnSeekBarChangeListener(this);

		imageViewStatus = v.findViewById(R.id.imageViewStatus);

		textViewScore = v.findViewById(R.id.textViewScore);
		textViewLastScore = v.findViewById(R.id.textViewLastScore);
		textViewHighScore = v.findViewById(R.id.textViewHighScore);

		Button buttonDeviceCancel = v.findViewById(R.id.buttonDeviceCancel);
		buttonDeviceCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		buttonDeviceEnable = v.findViewById(R.id.buttonDeviceEnable);
		buttonDeviceEnable.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		/**
		 * PuzzleboxOrbitAudioIRHandler
		 */

		if (!DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.isAlive()) {

			/**
			 * Prepare audio stream
			 */

			// TODO
//			maximizeAudioVolume(); // Automatically set media volume to maximum

			// Set the hardware buttons to control the audio output
//			getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

			// Preload the flight control WAV file into memory
//			DevicePuzzleboxOrbitSingleton.getInstance().soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
//			DevicePuzzleboxOrbitSingleton.getInstance().soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//				public void onLoadComplete(SoundPool soundPool,
//													int sampleId,
//													int status) {
//					DevicePuzzleboxOrbitSingleton.getInstance().loaded = true;
//				}
//			});
//			DevicePuzzleboxOrbitSingleton.getInstance().soundID = DevicePuzzleboxOrbitSingleton.getInstance().soundPool.load(getActivity().getApplicationContext(), DevicePuzzleboxOrbitSingleton.getInstance().audioFile, 1);

//			DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.start();
			DevicePuzzleboxOrbitSingleton.getInstance().startAudioHandler();
		}

		/**
		 * Update settings according to default UI
		 */
		// TODO
//		updateScreenLayout();

//		updatePowerThresholds();
////		updatePower();
//
//		updateControlSignal();

		resetFlight(v);

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

	public void onPause() {
		super.onPause();

		LocalBroadcastManager.getInstance(
				getActivity().getApplicationContext()).unregisterReceiver(
				mPacketReceiver);

		LocalBroadcastManager.getInstance(
				getActivity().getApplicationContext()).unregisterReceiver(
				mEventReceiver);

		stopControl();
	}

	public void onResume() {

		// Store access variables for window and blank point
		Window window = getDialog().getWindow();

		Point size = new Point();

		// Store dimensions of the screen in `size`
		Display display = window.getWindowManager().getDefaultDisplay();

		display.getSize(size);

		// Set the width of the dialog proportional to a percentage of the screen width
		window.setLayout((int) (size.x * 0.98), WindowManager.LayoutParams.WRAP_CONTENT);

		// Set the dimensions  of the dialog proportional to a percentage of the screen dimensions
//		window.setLayout((int) (size.x * 0.95), (int) (size.y * 0.935));

		window.setGravity(Gravity.CENTER);

		// Call super onResume after sizing
		super.onResume();

		if (ProfileSingleton.getInstance().getValue(io.puzzlebox.jigsaw.ui.DialogOutputAudioIRFragment.profileID, "active").equals("true")) {
			playControl();
		} else {
			Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_puzzlebox_orbit_joystick_audio_ir_warning), Toast.LENGTH_LONG).show();
		}

		updatePowerThresholds();
		updatePower();
		updateControlSignal();

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				mPacketReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.packet"));

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				mEventReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.event"));

	}

	private final BroadcastReceiver mPacketReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			int eegAttention = Integer.valueOf(intent.getStringExtra("Attention"));
			int eegMeditation = Integer.valueOf(intent.getStringExtra("Meditation"));
			int eegSignal = Integer.valueOf(intent.getStringExtra("Signal Level"));

			progressBarAttention.setProgress(eegAttention);
			progressBarMeditation.setProgress(eegMeditation);
			progressBarSignal.setProgress(eegSignal);

			updateStatusImage();

			updatePower();
		}

	};

	public void updateControlSignal() {

		Integer[] command =  {
				DevicePuzzleboxOrbitSingleton.getInstance().defaultControlThrottle,
				DevicePuzzleboxOrbitSingleton.getInstance().defaultControlYaw,
				DevicePuzzleboxOrbitSingleton.getInstance().defaultControlPitch,
				DevicePuzzleboxOrbitSingleton.getInstance().defaultChannel};


		// Transmit zero Throttle power if not above EEG power threashold
		// or demo mode (test flight) is not active
//		if ((eegPower <= 0) || (! DevicePuzzleboxOrbitSingleton.getInstance().demoActive)){
		if (eegPower <= 0) {
			command[0] = 0;
		}

		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.command = command;

		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.updateControlSignal();
	}

	private final BroadcastReceiver mEventReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String name = intent.getStringExtra("name");
			String value = intent.getStringExtra("value");

			switch(name) {

				case "eegStatus":

					switch(value) {
						case "STATE_CONNECTING":
							updateStatusImage();
							break;
						case "STATE_CONNECTED":
							updateStatusImage();
							break;
						case "STATE_NOT_FOUND":
							Toast.makeText(context, "EEG Not Found", Toast.LENGTH_SHORT).show();
							updateStatusImage();
							buttonDeviceEnable.setEnabled(false);
							buttonDeviceEnable.setVisibility(View.INVISIBLE);
							break;
						case "STATE_NOT_PAIRED":
							Toast.makeText(context, "EEG Not Paired", Toast.LENGTH_SHORT).show();
							updateStatusImage();
							buttonDeviceEnable.setEnabled(false);
							buttonDeviceEnable.setVisibility(View.INVISIBLE);
							break;
						case "STATE_DISCONNECTED":
							updateStatusImage();
							buttonDeviceEnable.setEnabled(false);
							buttonDeviceEnable.setVisibility(View.INVISIBLE);
							break;
						case "MSG_LOW_BATTERY":
							Toast.makeText(context, io.puzzlebox.jigsaw.R.string.buttonStatusNeuroSkyMindWaveBatteryLow, Toast.LENGTH_SHORT).show();
							updateStatusImage();
							break;
					}

					break;

				case "eegBlink":
					Log.d(TAG, "Blink: " + value + "\n");
					break;
			}
		}

	};

	public void updateStatusImage() {

		if(eegPower > 0) {
			imageViewStatus.setImageResource(R.drawable.status_4_active);
			return;
		}

		if(eegSignal > 90) {
			imageViewStatus.setImageResource(R.drawable.status_3_processing);
			return;
		}

		if(eegConnected) {
			imageViewStatus.setImageResource(R.drawable.status_2_connected);
			return;
		}

		if(eegConnecting) {
			imageViewStatus.setImageResource(R.drawable.status_1_connecting);
		} else {
			imageViewStatus.setImageResource(R.drawable.status_default);
		}
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
		updatePowerThresholds();
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		/**
		 * Method required by SeekBar.OnSeekBarChangeListener
		 */
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		Log.v(TAG, "onStopTrackingTouch()");
	}

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

				percentOfMaxPower = ( ((100 - attentionSeekValue) - (100 - i)) / (float)(100 - attentionSeekValue) );
				power = thresholdValuesAttention[i] + (int)( minimumPower + ((maximumPower - minimumPower) * percentOfMaxPower) );
				thresholdValuesAttention[i] = power;
			}
		}

		meditationSeekValue = seekBarMeditation.getProgress();
		if (meditationSeekValue > 0) {
			for (int i = meditationSeekValue; i < thresholdValuesMeditation.length; i++) {
				percentOfMaxPower = ( ((100 - meditationSeekValue) - (100 - i)) / (float)(100 - meditationSeekValue) );
				power = thresholdValuesMeditation[i] + (int)( minimumPower + ((maximumPower - minimumPower) * percentOfMaxPower) );
				thresholdValuesMeditation[i] = power;
			}
		}
	}

	public void updatePower() {

		/**
		 * This method updates the power level of the
		 * "Throttle" and triggers the audio stream
		 * which is used to fly the helicopter
		 */

		if (eegConnected) {

			if (eegSignal < 100) {
				NeuroSkyThinkGearService.eegAttention = 0;
				NeuroSkyThinkGearService.eegMeditation = 0;
				progressBarAttention.setProgress(NeuroSkyThinkGearService.eegAttention);
				progressBarMeditation.setProgress(NeuroSkyThinkGearService.eegMeditation);
			}

			NeuroSkyThinkGearService.eegPower = calculateSpeed();
			eegPower = NeuroSkyThinkGearService.eegPower;

			progressBarPower.setProgress(NeuroSkyThinkGearService.eegPower);
		}

		// TODO
//		if (InteraXonMuseService.eegConnected) {
//
////			Log.d(TAG, "InteraXonMuseService.eegConnected: eegSignal: " + InteraXonMuseService.eegSignal);
////			if (InteraXonMuseService.eegSignal < 100) {
////				InteraXonMuseService.eegConcentration = 0;
////				InteraXonMuseService.eegMellow = 0;
////				progressBarAttention.setProgress(InteraXonMuseService.eegConcentration);
////				progressBarMeditation.setProgress(InteraXonMuseService.eegMellow);
////			}
//
//			InteraXonMuseService.eegPower = calculateSpeed();
//
//			progressBarPower.setProgress(InteraXonMuseService.eegPower);
//			eegPower = InteraXonMuseService.eegPower;
//
//
//		}

		DevicePuzzleboxOrbitSingleton.getInstance().eegPower = eegPower;

		if (eegPower > 0) {

			/** Start playback of audio control stream */
			updateScore();

			DevicePuzzleboxOrbitSingleton.getInstance().flightActive = true;
		} else {

			// Land the helicopter
//			if (! DevicePuzzleboxOrbitSingleton.getInstance().demoActive ) {
//				stopControl();
//			}

//			buttonTestFlight.setText(getResources().getString(R.string.button_test_fly));

			resetCurrentScore();
		}

		updateControlSignal();
	}

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
	}

	public void updateScore() {

		/**
		 * Score points based on target slider levels
		 * If you pass your goal with either Attention or Meditation
		 * the higher target of the two will counts as points per second.
		 *
		 * Minimum threshold for points is set as "minimumScoreTarget"
		 *
		 * For example, assume minimumScoreTarget is 40%.
		 * If your target Attention is 60% and you go past to reach 80%
		 * you will receive 20 points per second (60-40). If your
		 * target is 80% and you reach 80% you will receive 40
		 * points per second (80-40).
		 *
		 * You can set both Attention and Meditation targets at the
		 * same time. Reaching either will fly the helicopter but you
		 * will only receive points for the higher-scoring target of
		 * the two.
		 *
		 */

		int eegAttentionScore = 0;
		int eegAttention = progressBarAttention.getProgress();
		int eegAttentionTarget = seekBarAttention.getProgress();

		int eegMeditationScore = 0;
		int eegMeditation = progressBarMeditation.getProgress();
		int eegMeditationTarget = seekBarMeditation.getProgress();

		if ((eegAttention >= eegAttentionTarget) &&
				(eegAttentionTarget > DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
			eegAttentionScore = eegAttentionTarget - DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget;

		if ((eegMeditation >= eegMeditationTarget) &&
				(eegMeditationTarget > DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
			eegMeditationScore = eegMeditationTarget - DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget;

		if (eegAttentionScore > eegMeditationScore)
			DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent = DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent + eegAttentionScore;
		else
			DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent = DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent + eegMeditationScore;

		textViewScore.setText(Integer.toString(DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent));

		if (DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent > DevicePuzzleboxOrbitSingleton.getInstance().scoreHigh) {
			DevicePuzzleboxOrbitSingleton.getInstance().scoreHigh = DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent;
			textViewHighScore.setText(Integer.toString(DevicePuzzleboxOrbitSingleton.getInstance().scoreHigh));
		}

		// Catch anyone gaming the system with one slider
		// below the minimum threshold and the other over.
		// For example, setting Meditation to 1% will keep helicopter
		// activated even if Attention is below target
		if ((eegAttention < eegAttentionTarget) && (eegMeditation < DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
			resetCurrentScore();
		if ((eegMeditation < eegMeditationTarget) && (eegAttention < DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
			resetCurrentScore();
		if ((eegAttention < DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget) && (eegMeditation < DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
			resetCurrentScore();
	}

	public void resetCurrentScore() {
		if (DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent > 0)
			textViewLastScore.setText(Integer.toString(DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent));
		DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent = 0;
		textViewScore.setText(Integer.toString(DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent));
	}

	public void playControl() {

		if (DevicePuzzleboxOrbitSingleton.getInstance().generateAudio) {

			/**
			 * Generate signal on the fly
			 */
			DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.ifFlip = DevicePuzzleboxOrbitSingleton.getInstance().invertControlSignal; // if checked then flip

			int channel = DevicePuzzleboxOrbitSingleton.getInstance().defaultChannel;

			updateAudioHandlerLoopNumberWhileMindControl(-1); // Loop infinite for easier user testing

			updateAudioHandlerChannel(channel);

			DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.mutexNotify();

		} else {

			/**
			 * Play audio control file
			 */

			/** Getting the user sound settings */
			AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
			float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) maxVolume, 0);
			/** Is the sound loaded already? */
			if (DevicePuzzleboxOrbitSingleton.getInstance().loaded) {
				//				soundPool.play(soundID, volume, volume, 1, 0, 1f);
				//				soundPool.setVolume(soundID, 1f, 1f);
				//				soundPool.play(soundID, maxVolume, maxVolume, 1, 0, 1f); // Fixes Samsung Galaxy S4 [SGH-M919]

				DevicePuzzleboxOrbitSingleton.getInstance().soundPool.play(DevicePuzzleboxOrbitSingleton.getInstance().soundID, 1f, 1f, 1, 0, 1f); // Fixes Samsung Galaxy S4 [SGH-M919]

				// TODO No visible effects of changing these variables on digital oscilloscope
				//				soundPool.play(soundID, 0.5f, 0.5f, 1, 0, 0.5f);
			}
		}
	}

	public void stopControl() {
		stopAudio();
		DevicePuzzleboxOrbitSingleton.getInstance().flightActive = false;
	}

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
	}

	public void testFlight(View v) {

		/**
		 * Demo mode is called when the "Test Helicopter" button is pressed.
		 * This method can be easily adjusted for testing new features
		 * during development.
		 */

		if (! DevicePuzzleboxOrbitSingleton.getInstance().flightActive) {
			DevicePuzzleboxOrbitSingleton.getInstance().flightActive = true;
			DevicePuzzleboxOrbitSingleton.getInstance().demoActive = true;

			// NOTE 2017-05-10
			// Control signal should always play to keep Orbit from timing out.
			// Zero throttle will keep the Orbit from taking off
			// In order to have a manual "Stop" ability, the "Test Flight" button doubles as a "Land" button
			// Turn on sound if it was manually turned off
//			playControl();
		} else {
			DevicePuzzleboxOrbitSingleton.getInstance().demoActive = false;
		}

		updateControlSignal();

	}

	public void resetFlight(View view) {
		resetCurrentScore();

		DevicePuzzleboxOrbitSingleton.getInstance().demoActive = false;

		// Setting eegPower to zero will cause the Orbit to land if flying
		// However if the user's data is actively being received
		// the Orbit may take off again approximately one second later
		eegPower = 0;

		seekBarAttention.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultTargetAttention);
		seekBarMeditation.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultTargetMeditation);

		updatePowerThresholds();

		updateControlSignal();
	}

	/**
	 * the puzzleboxOrbitAudioIRHandler to update command
	 */
	public void updateAudioHandlerCommand(Integer[] command) {
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.command = command;
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.updateControlSignal();
	}

	/**
	 * the puzzleboxOrbitAudioIRHandler to update channel
	 */
	public void updateAudioHandlerChannel(int channel) {
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.channel = channel;
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.updateControlSignal();
	}

	/**
	 * @param number the puzzleboxOrbitAudioIRHandler to update loop number while mind control
	 */
	public void updateAudioHandlerLoopNumberWhileMindControl(int number) {
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.loopNumberWhileMindControl = number;
	}

	public void resetControlSignal(View view) {
	}

	public void setControlSignalHover(View view) {
	}

	public void setControlSignalForward(View view) {
	}

	public void setControlSignalLeft(View view) {
	}

	public void setControlSignalRight(View view) {
	} // setControlSignalRight

	private void broadcastCommandBluetooth(String name, String value) {
		Log.d(TAG, "broadcastCommandBluetooth: " + name + ": " + value);

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.bluetooth.command");

		intent.putExtra("name", name);
		intent.putExtra("value", value);

		LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
	}
}
