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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import io.puzzlebox.jigsaw.data.DevicePuzzleboxOrbitSingleton;
import io.puzzlebox.jigsaw.data.ProfileSingleton;
import io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService;
import io.puzzlebox.jigsaw.R;

import static io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService.eegConnected;
import static io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService.eegConnecting;
import static io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService.eegSignal;

public class DialogProfilePuzzleboxOrbitJoystickMindwaveFragment extends DialogFragment
		implements SeekBar.OnSeekBarChangeListener {

	private final static String TAG = DialogProfilePuzzleboxOrbitJoystickMindwaveFragment.class.getSimpleName();

	public final static String profileID = "profile_puzzlebox_orbit_joystick_mindwave";

	// UI
	Button buttonDeviceEnable;

	ProgressBar progressBarAttention;
	SeekBar seekBarAttention;
	ProgressBar progressBarMeditation;
	SeekBar seekBarMeditation;
	ProgressBar progressBarSignal;
	ProgressBar progressBarPower;

	public Switch switchThrottlePitch;

	public SeekBar seekBarThrottle;
	public SeekBar seekBarYaw;
	public SeekBar seekBarPitch;

	TextView textViewScore;
	TextView textViewLastScore;
	TextView textViewHighScore;

	ImageView imageViewStatus;

	/**
	 * Configuration
	 */
	public int eegPower = 0;
	int[] thresholdValuesAttention = new int[101];
	int[] thresholdValuesMeditation = new int[101];
	int minimumPower = 0; // minimum power for the Orbit
	int maximumPower = 100; // maximum power for the Orbit

	private OnFragmentInteractionListener mListener;

	public DialogProfilePuzzleboxOrbitJoystickMindwaveFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.dialog_profile_puzzlebox_orbit_joystick_mindwave, container, false);

		getDialog().getWindow().setTitle( getString(R.string.title_dialog_fragment_puzzlebox_orbit_joystick_mindwave));

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

		switchThrottlePitch = v.findViewById(R.id.switchThrottlePitch);
		switchThrottlePitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e(TAG, "switchThrottlePitch.onClick(): " + switchThrottlePitch.isChecked());
				onSwitchClicked(switchThrottlePitch.isChecked());
			}
		});

		seekBarThrottle = v.findViewById(R.id.seekBarThrottle);
		seekBarThrottle.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultControlThrottle);
		seekBarThrottle.setOnSeekBarChangeListener(this);

		seekBarYaw = v.findViewById(R.id.seekBarYaw);
		seekBarYaw.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultControlYaw);
		seekBarYaw.setOnSeekBarChangeListener(this);

		seekBarPitch = v.findViewById(R.id.seekBarPitch);
		seekBarPitch.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultControlPitch);

		JoystickView joystickView = v.findViewById(R.id.joystickView);
		joystickView.setOnMoveListener(onMoveJoystick);


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
		DevicePuzzleboxOrbitSingleton.getInstance().startAudioHandler();

		updatePowerThresholds();

		updateControlSignal();

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
		window.setLayout((int)(size.x *0.98), WindowManager.LayoutParams.WRAP_CONTENT);

		window.setGravity(Gravity.CENTER);

		super.onResume();

		if (ProfileSingleton.getInstance().getValue(DialogOutputAudioIRFragment.profileID, "active").equals("true")) {
			playControl();
		} else {
			Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_puzzlebox_orbit_joystick_audio_ir_warning), Toast.LENGTH_LONG).show();
		}

		updatePowerThresholds();
		updatePower();

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				mPacketReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.packet"));

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				mEventReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.event"));
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
		updatePowerThresholds();
		updateControlSignal();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	public void onSwitchClicked(Boolean activated) {
		if (! activated) {
			switchThrottlePitch.setText(R.string.label_puzzlebox_orbit_joystick_mindwave_switch_text);
		} else {
			switchThrottlePitch.setText(R.string.label_puzzlebox_orbit_joystick_mindwave_switch_text_alt);
		}
	}

	private final JoystickView.OnMoveListener onMoveJoystick = new JoystickView.OnMoveListener(){
		public void onMove(int angle, int strength) {
			Log.v(TAG, "onMoveJoystick(int angle, int strength): " + angle + ", " + strength);

			if ((angle == 0) && (strength == 0)) {
				// Home position or no touch on joystick

				if (! switchThrottlePitch.isChecked()) {
					seekBarThrottle.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultJoystickThrottle);
					seekBarYaw.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultControlYaw);
				} else {
					seekBarPitch.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultControlPitch);
					seekBarYaw.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultControlYaw);
				}

			}
			else if ((angle >= 30) && (angle <= 150)) {
				// Up
				if (! switchThrottlePitch.isChecked()) {
					// Y Axis of joystick controls Throttle

					// Ensure lower half of seekBarThrottle can be accessed from the top half of throttle joystick
					int newY = (int) (seekBarThrottle.getMax() * (strength / 100.0));

					// Set a minimum about of throttle to send if anywhere above zero level
					// of Orbit. Normally it takes some small amount of throttle to trigger
					// any flight or visible reaction.
					if (newY < DevicePuzzleboxOrbitSingleton.getInstance().minimumJoystickThrottle)
						newY = DevicePuzzleboxOrbitSingleton.getInstance().minimumJoystickThrottle;

					seekBarThrottle.setProgress(newY);

				} else {
					// Y Axis of joystick controls throttle
					int newY = seekBarPitch.getMax() / 2;
					newY = (int) (newY * (strength / 100.0));
					newY = seekBarPitch.getMax() / 2 + newY;
					seekBarPitch.setProgress(newY);
				}

			}
			else if ((angle >= 210) && (angle <= 330)) {
				// Down
				if (! switchThrottlePitch.isChecked()) {
					// Y Axis of joystick controls Throttle
					seekBarThrottle.setProgress(0);
				} else {
					// Y Axis of joystick controls throttle
					int newY = seekBarPitch.getMax() / 2;
					newY = (int) (newY * (strength / 100.0));
					newY = seekBarPitch.getMax() / 2 - newY;
					seekBarPitch.setProgress(newY);
				}
			}
			if ((angle >= 150) && (angle <= 210)) {
				// Left
				int newX = seekBarYaw.getMax() / 2;
				newX = (int) (newX * (strength / 100.0));
				newX = seekBarYaw.getMax() / 2 - newX;
				seekBarYaw.setProgress(newX);
			}
			else if ((angle >= 330) || (angle <= 30)) {
				// Right
				int newX = seekBarYaw.getMax() / 2;
				newX = (int) (newX * (strength / 100.0));
				newX = seekBarYaw.getMax() / 2 + newX;
				seekBarYaw.setProgress(newX);
			}
			updateControlSignal();
		}
	};

	public void updateControlSignal() {

		// We subtract the current Yaw position from the maximum slider value
		// because smaller values instruct the helicopter to spin to the right
		// (clockwise if looking down from above) whereas intuitively moving
		// the slider to the left should cause it to spin left
		Integer[] command =  {
				seekBarThrottle.getProgress(),
				seekBarYaw.getMax() - seekBarYaw.getProgress(),
				seekBarPitch.getProgress(),
				DevicePuzzleboxOrbitSingleton.getInstance().defaultChannel};

		// Transmit zero Throttle power if not about EEG power threashold
		if (eegPower <= 0) {
			Log.e(TAG, "(eegPower <= 0)");
			command[0] = 0;
		}
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.command = command;
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.updateControlSignal();
	}

	/**
	 * @param number the puzzleboxOrbitAudioIRHandler to update loop number while mind control
	 */
	public void updateAudioHandlerLoopNumberWhileMindControl(int number) {
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.loopNumberWhileMindControl = number;
	}

	/**
	 * the puzzleboxOrbitAudioIRHandler to update channel
	 */
	public void updateAudioHandlerChannel(int channel) {
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.channel = channel;
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.updateControlSignal();
	}

	public void playControl() {
		DevicePuzzleboxOrbitSingleton.getInstance().flightActive = true;
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.ifFlip = DevicePuzzleboxOrbitSingleton.getInstance().invertControlSignal; // if checked then flip

		updateAudioHandlerLoopNumberWhileMindControl(-1); // Loop infinite for easier user testing

		updateAudioHandlerChannel(DevicePuzzleboxOrbitSingleton.getInstance().defaultChannel);

		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.mutexNotify();
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
							Toast.makeText(context, "EEG Disconnected", Toast.LENGTH_SHORT).show();
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
//					if (Integer.parseInt(value) > NeuroSkyThinkGearService.blinkRangeMax) {
//						value = "" + NeuroSkyThinkGearService.blinkRangeMax;
//					}
//					try {
//						progressBarBlink.setProgress(Integer.parseInt(value));
//					} catch (NumberFormatException e) {
//						e.printStackTrace();
//					}
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

		DevicePuzzleboxOrbitSingleton.getInstance().eegPower = eegPower;

		if (eegPower > 0) {

			/** Start playback of audio control stream */
			if (!DevicePuzzleboxOrbitSingleton.getInstance().flightActive) {
				playControl();
			}

			updateScore();

			DevicePuzzleboxOrbitSingleton.getInstance().flightActive = true;

		} else {

			/** Land the helicopter */
//			if (! DevicePuzzleboxOrbitSingleton.getInstance().demoActive ) {
////				stopControl();
//				updateControlSignal();
//			}
			resetCurrentScore();
		}

		updateControlSignal();

		Log.d(TAG, "flightActive: " + DevicePuzzleboxOrbitSingleton.getInstance().flightActive);
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
}
