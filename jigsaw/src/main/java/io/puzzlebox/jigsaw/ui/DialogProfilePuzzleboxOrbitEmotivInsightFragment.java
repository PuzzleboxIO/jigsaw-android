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
import android.os.Handler;
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

import com.emotiv.insight.IEmoStateDLL;
import com.emotiv.insight.MentalCommandDetection;

import io.puzzlebox.jigsaw.data.DeviceEmotivInsightSingleton;
import io.puzzlebox.jigsaw.data.ProfileSingleton;
import io.puzzlebox.jigsaw.R;
import io.puzzlebox.jigsaw.data.DevicePuzzleboxOrbitSingleton;

public class DialogProfilePuzzleboxOrbitEmotivInsightFragment extends DialogFragment
		implements SeekBar.OnSeekBarChangeListener {

	private final static String TAG = DialogProfilePuzzleboxOrbitEmotivInsightFragment.class.getSimpleName();

	/**
	 * Configuration
	 */
	public int eegPower = 0;
	public int previousEEGSignal = 0;

	// UI
	Button buttonDeviceEnable;

	ProgressBar progressBarActivityMentalCommand;
	SeekBar seekBarActivityMentalCommand;
	ProgressBar progressBarContactQuality;
	ProgressBar progressBarPower;

	ProgressBar progressBarTrainNeutral;
	ProgressBar progressBarTrainMentalCommand;

	public SeekBar seekBarThrottle;

	final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };

	Button buttonTestFlight;
	Button buttonResetFlight;

	Button buttonTrainNeutral;
	Button buttonTrainMentalCommand;

	TextView textViewScore;
	TextView textViewLastScore;
	TextView textViewHighScore;

	ImageView imageViewStatus;

	ImageView imageViewAF3;
	ImageView imageViewAF4;
	ImageView imageViewT7;
	ImageView imageViewT8;
	ImageView imageViewPz;
	ImageView imageViewCMS;

	private int currentAF3 = 0;
	private int currentAF4 = 0;
	private int currentT7 = 0;
	private int currentT8 = 0;
	private int currentPz = 0;
	private int currentCMS = 0;

	int[] thresholdValuesMentalCommand = new int[101];
	int minimumPower = 0; // minimum power for the Orbit
	int maximumPower = 100; // maximum power for the Orbit

	private Handler handlerAnimation;

	private OnFragmentInteractionListener mListener;

	public DialogProfilePuzzleboxOrbitEmotivInsightFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.dialog_profile_puzzlebox_orbit_emotiv_insight, container, false);

		getDialog().getWindow().setTitle( getString(R.string.title_dialog_fragment_puzzlebox_orbit_emotiv_insight));

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

		imageViewAF3 = v.findViewById(R.id.imageViewEmotivInsightSensorAF3);
		imageViewAF4 = v.findViewById(R.id.imageViewEmotivInsightSensorAF4);
		imageViewT7 = v.findViewById(R.id.imageViewEmotivInsightSensorT7);
		imageViewT8 = v.findViewById(R.id.imageViewEmotivInsightSensorT8);
		imageViewPz = v.findViewById(R.id.imageViewEmotivInsightSensorPz);
		imageViewCMS = v.findViewById(R.id.imageViewEmotivInsightSensorCMS);

		buttonTrainNeutral = v.findViewById(R.id.buttonTrainNeutral);
		buttonTrainNeutral.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Training Neutral");
				DeviceEmotivInsightSingleton.getInstance().currentInsightTraining = "neutral";
				MentalCommandDetection.IEE_MentalCommandSetTrainingControl(DeviceEmotivInsightSingleton.getInstance().userID, MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_REJECT.getType());
				MentalCommandDetection.IEE_MentalCommandSetTrainingControl(DeviceEmotivInsightSingleton.getInstance().userID, MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_RESET.getType());
				progressBarTrainMentalCommand.setProgress(0);
				buttonTrainMentalCommand.setText(getString(R.string.button_puzzlebox_orbit_emotiv_insight_training_neutral_untrained));
				MentalCommandDetection.IEE_MentalCommandSetTrainingAction(DeviceEmotivInsightSingleton.getInstance().userID, IEmoStateDLL.IEE_MentalCommandAction_t.MC_NEUTRAL.ToInt());
				MentalCommandDetection.IEE_MentalCommandSetTrainingControl(DeviceEmotivInsightSingleton.getInstance().userID, MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_START.getType());
				animateProgressBar();
			}
		});

		progressBarTrainNeutral = v.findViewById(R.id.progressBarTrainNeutral);

		buttonTrainMentalCommand = v.findViewById(R.id.buttonTrainMentalCommand);
		buttonTrainMentalCommand.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Training Mental Command (Push)");
				DeviceEmotivInsightSingleton.getInstance().currentInsightTraining = "mental command";
				MentalCommandDetection.IEE_MentalCommandSetTrainingControl(DeviceEmotivInsightSingleton.getInstance().userID, MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_REJECT.getType());
				MentalCommandDetection.IEE_MentalCommandSetTrainingAction(DeviceEmotivInsightSingleton.getInstance().userID, IEmoStateDLL.IEE_MentalCommandAction_t.MC_PUSH.ToInt());
				MentalCommandDetection.IEE_MentalCommandSetTrainingControl(DeviceEmotivInsightSingleton.getInstance().userID, MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_START.getType());
				animateProgressBar();
			}
		});

		progressBarTrainMentalCommand = v.findViewById(R.id.progressBarTrainMentalCommand);
		progressBarActivityMentalCommand = v.findViewById(R.id.progressBarActivityMentalCommand);
		ShapeDrawable progressBarMentalCommandDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarMentalCommandColor = "#AA00FF";
		progressBarMentalCommandDrawable.getPaint().setColor(Color.parseColor(progressBarMentalCommandColor));
		ClipDrawable progressMentalCommand = new ClipDrawable(progressBarMentalCommandDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarActivityMentalCommand.setProgressDrawable(progressMentalCommand);
		progressBarActivityMentalCommand.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarContactQuality = v.findViewById(R.id.progressBarContactQuality);
		ShapeDrawable progressBarContactQualityDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarContactQualityColor = "#00FF00";
		progressBarContactQualityDrawable.getPaint().setColor(Color.parseColor(progressBarContactQualityColor));
		ClipDrawable progressSignal = new ClipDrawable(progressBarContactQualityDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarContactQuality.setProgressDrawable(progressSignal);
		progressBarContactQuality.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarPower = (ProgressBar) v.findViewById(R.id.progressBarPower);
		ShapeDrawable progressBarPowerDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarPowerColor = "#FFFF00";
		progressBarPowerDrawable.getPaint().setColor(Color.parseColor(progressBarPowerColor));
		ClipDrawable progressPower = new ClipDrawable(progressBarPowerDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarPower.setProgressDrawable(progressPower);
		progressBarPower.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		seekBarActivityMentalCommand = v.findViewById(R.id.seekBarActivityMentalCommand);
		seekBarActivityMentalCommand.setProgress(DeviceEmotivInsightSingleton.getInstance().defaultMentalCommandPower);
		seekBarActivityMentalCommand.setOnSeekBarChangeListener(this);

		imageViewStatus = v.findViewById(R.id.imageViewStatus);
		textViewScore = v.findViewById(R.id.textViewScore);
		textViewLastScore = v.findViewById(R.id.textViewLastScore);
		textViewHighScore = v.findViewById(R.id.textViewHighScore);

		seekBarThrottle = (SeekBar) v.findViewById(R.id.seekBarThrottle);
		seekBarThrottle.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultControlThrottle);
		seekBarThrottle.setOnSeekBarChangeListener(this);

		Button buttonDeviceCancel = v.findViewById(io.puzzlebox.jigsaw.R.id.buttonDeviceCancel);
		buttonDeviceCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		buttonDeviceEnable = v.findViewById(io.puzzlebox.jigsaw.R.id.buttonDeviceEnable);
		buttonDeviceEnable.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		/*
		 * PuzzleboxOrbitAudioIRHandler
		 */
		if (!DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.isAlive()) {
			DevicePuzzleboxOrbitSingleton.getInstance().startAudioHandler();
		}
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
				mActionReceiver);

		LocalBroadcastManager.getInstance(
				getActivity()).unregisterReceiver(
				mSignalQualityReceiver);

		LocalBroadcastManager.getInstance(
				getActivity().getApplicationContext()).unregisterReceiver(
				mTrainingReceiver);

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
				mSignalQualityReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.signal_quality"));

		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				mActionReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.action"));

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				mTrainingReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.training"));
	}

	private final BroadcastReceiver mSignalQualityReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			int eegSignal = 0;

			int AF3 = Integer.valueOf(intent.getStringExtra("AF3"));
			int AF4 = Integer.valueOf(intent.getStringExtra("AF4"));
			int T7 = Integer.valueOf(intent.getStringExtra("T7"));
			int T8 = Integer.valueOf(intent.getStringExtra("T8"));
			int Pz = Integer.valueOf(intent.getStringExtra("Pz"));
			int CMS = Integer.valueOf(intent.getStringExtra("CMS"));

			// If there is no change to values no need to recaculate eegSignal and redraw all ImageViews
			if ((AF3 != currentAF3) &&
					(AF4 != currentAF4) &&
					(T7 != currentT7) &&
					(T8 != currentT8) &&
					(Pz != currentPz) &&
					(CMS != currentCMS)) {
				return;
			}

			currentAF3 = AF3;
			currentAF4 = AF4;
			currentT7 = T7;
			currentT8 = T8;
			currentPz = Pz;
			currentCMS = CMS;

			switch (AF3) {
				case 0:
					imageViewAF3.setImageResource(R.drawable.device_eeg_sensor_white);
					break;
				case 1:
					imageViewAF3.setImageResource(R.drawable.device_eeg_sensor_red);
					eegSignal += 5;
					break;
				case 2:
					imageViewAF3.setImageResource(R.drawable.device_eeg_sensor_yellow);
					eegSignal += 10;
					break;
				case 4:
					imageViewAF3.setImageResource(R.drawable.device_eeg_sensor_green);
					eegSignal += 20;
					break;
				default:
					imageViewAF3.setImageResource(R.drawable.device_eeg_sensor_white);
					break;
			}

			switch (T7) {
				case 0:
					imageViewT7.setImageResource(R.drawable.device_eeg_sensor_white);
					break;
				case 1:
					imageViewT7.setImageResource(R.drawable.device_eeg_sensor_red);
					eegSignal += 5;
					break;
				case 2:
					imageViewT7.setImageResource(R.drawable.device_eeg_sensor_yellow);
					eegSignal += 10;
					break;
				case 4:
					imageViewT7.setImageResource(R.drawable.device_eeg_sensor_green);
					eegSignal += 20;
					break;
				default:
					imageViewT7.setImageResource(R.drawable.device_eeg_sensor_white);
					break;
			}

			switch (Pz) {
				case 0:
					imageViewPz.setImageResource(R.drawable.device_eeg_sensor_white);
					break;
				case 1:
					imageViewPz.setImageResource(R.drawable.device_eeg_sensor_red);
					eegSignal += 5;
					break;
				case 2:
					imageViewPz.setImageResource(R.drawable.device_eeg_sensor_yellow);
					eegSignal += 10;
					break;
				case 4:
					imageViewPz.setImageResource(R.drawable.device_eeg_sensor_green);
					eegSignal += 20;
					break;
				default:
					imageViewPz.setImageResource(R.drawable.device_eeg_sensor_white);
					break;
			}

			switch (T8) {
				case 0:
					imageViewT8.setImageResource(R.drawable.device_eeg_sensor_white);
					break;
				case 1:
					imageViewT8.setImageResource(R.drawable.device_eeg_sensor_red);
					eegSignal += 5;
					break;
				case 2:
					imageViewT8.setImageResource(R.drawable.device_eeg_sensor_yellow);
					eegSignal += 10;
					break;
				case 4:
					imageViewT8.setImageResource(R.drawable.device_eeg_sensor_green);
					eegSignal += 20;
					break;
				default:
					imageViewT8.setImageResource(R.drawable.device_eeg_sensor_white);
					break;
			}

			switch (AF4) {
				case 0:
					imageViewAF4.setImageResource(R.drawable.device_eeg_sensor_white);
					break;
				case 1:
					imageViewAF4.setImageResource(R.drawable.device_eeg_sensor_red);
					eegSignal += 5;
					break;
				case 2:
					imageViewAF4.setImageResource(R.drawable.device_eeg_sensor_yellow);
					eegSignal += 10;
					break;
				case 4:
					imageViewAF4.setImageResource(R.drawable.device_eeg_sensor_green);
					eegSignal += 20;
					break;
				default:
					imageViewAF4.setImageResource(R.drawable.device_eeg_sensor_white);
					break;
			}

			switch (CMS) {
				case 0:
					imageViewCMS.setImageResource(R.drawable.device_eeg_sensor_cms_white);
					break;
				case 1:
					imageViewCMS.setImageResource(R.drawable.device_eeg_sensor_cms_red);
					eegSignal += 5;
					break;
				case 2:
					imageViewCMS.setImageResource(R.drawable.device_eeg_sensor_cms_yellow);
					eegSignal += 10;
					break;
				case 4:
					imageViewCMS.setImageResource(R.drawable.device_eeg_sensor_cms_green);
					eegSignal += 20;
					break;
				default:
					imageViewCMS.setImageResource(R.drawable.device_eeg_sensor_cms_white);
					break;
			}

			if (eegSignal != previousEEGSignal) {
				String progressBarContactQualityColor;
				ShapeDrawable progressBarContactQualityDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null, null));
				if (eegSignal <= 20) {
					progressBarContactQualityColor = "#FFAA00";
					progressBarContactQualityDrawable.getPaint().setColor(Color.parseColor(progressBarContactQualityColor));
					ClipDrawable progressSignal = new ClipDrawable(progressBarContactQualityDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
					progressBarContactQuality.setProgressDrawable(progressSignal);
				} else {
					progressBarContactQualityColor = "#00FF00";
					progressBarContactQualityDrawable.getPaint().setColor(Color.parseColor(progressBarContactQualityColor));
					ClipDrawable progressSignal = new ClipDrawable(progressBarContactQualityDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
					progressBarContactQuality.setProgressDrawable(progressSignal);
				}
				progressBarContactQuality.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));
				progressBarContactQuality.setProgress(eegSignal);
			}
			previousEEGSignal = eegSignal;
		}
	};

	private final BroadcastReceiver mTrainingReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String status = intent.getStringExtra("status");

			switch (DeviceEmotivInsightSingleton.getInstance().currentInsightTraining) {

				case "neutral":
					Log.d(TAG, "Neutral " + status);
					switch (status) {
						case "Training Started":
							buttonTrainNeutral.setText(getString(R.string.button_puzzlebox_orbit_emotiv_insight_training_neutral_training));
							break;
						case "Training Completed":
							buttonTrainNeutral.setText(getString(R.string.button_puzzlebox_orbit_emotiv_insight_training_neutral_trained));
							break;
						case "Training Failed":
							buttonTrainNeutral.setText(getString(R.string.button_puzzlebox_orbit_emotiv_insight_training_neutral_untrained));
							progressBarTrainNeutral.setProgress(0);
							break;
					}
					break;
				case "mental command":
					Log.d(TAG, "Mental Command " + status);
					switch (status) {
						case "Training Started":
							buttonTrainMentalCommand.setText(getString(R.string.button_puzzlebox_orbit_emotiv_insight_training_neutral_training));
							break;
						case "Training Completed":
							buttonTrainMentalCommand.setText(getString(R.string.button_puzzlebox_orbit_emotiv_insight_training_neutral_trained));
							break;
						case "Training Failed":
							buttonTrainMentalCommand.setText(getString(R.string.button_puzzlebox_orbit_emotiv_insight_training_neutral_untrained));
							progressBarTrainMentalCommand.setProgress(0);
							break;
					}
					break;
			}
		}
	};

	public void updateControlSignal() {

		Integer[] command =  {
				seekBarThrottle.getProgress(),
				DevicePuzzleboxOrbitSingleton.getInstance().defaultControlYaw,
				DevicePuzzleboxOrbitSingleton.getInstance().defaultControlPitch,
				DevicePuzzleboxOrbitSingleton.getInstance().defaultChannel};

		// Transmit zero Throttle power if not above EEG power threashold
		// or demo mode (test flight) is not active
		if (eegPower <= 0) {
			command[0] = 0;
		}

		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.command = command;

		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.updateControlSignal();

	}

	public void updateStatusImage() {

		if(eegPower > 0) {
			imageViewStatus.setImageResource(R.drawable.status_4_active);
			return;
		}

		if (progressBarContactQuality.getProgress() > 20) {
			imageViewStatus.setImageResource(R.drawable.status_3_processing);
			return;
		}

		if(DeviceEmotivInsightSingleton.getInstance().lock) {
			imageViewStatus.setImageResource(R.drawable.status_2_connected);
			return;
		}

//		if(eegConnecting) {
//			imageViewStatus.setImageResource(R.drawable.status_1_connecting);
//			return;
//		}

		imageViewStatus.setImageResource(R.drawable.status_default);
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

		updatePowerThresholds();

		updateControlSignal();
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		/*
		  Method required by SeekBar.OnSeekBarChangeListener
		 */
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	public void updatePowerThresholds() {
		/*
		 * The "Power" level refers to the Puzzlebox Orbit helicopter's
		 * throttle setting. Typically this is an "off" or "on" state,
		 * meaning the helicopter is either flying or not flying at all.
		 * However this method could be used to increase the throttle
		 * or perhaps the forward motion of the helicopter to a level
		 * proportionate to how far past their target brainwave levels
		 * are set (via the progress bar sliders).
		 */
		int power;
		int mentalCommandSeekValue;
		float percentOfMaxPower;

		// Reset all values to zero
		for (int i = 0; i < thresholdValuesMentalCommand.length; i++) {
			thresholdValuesMentalCommand[i] = 0;
		}

		mentalCommandSeekValue = seekBarActivityMentalCommand.getProgress();
		if (mentalCommandSeekValue > 0) {
			for (int i = mentalCommandSeekValue; i < thresholdValuesMentalCommand.length; i++) {
				/*
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
				percentOfMaxPower = ( ((100 - mentalCommandSeekValue) - (100 - i)) / (float)(100 - mentalCommandSeekValue) );
				power = thresholdValuesMentalCommand[i] + (int)( minimumPower + ((maximumPower - minimumPower) * percentOfMaxPower) );
				thresholdValuesMentalCommand[i] = power;
			}
		}
	}

	public void updatePower() {
		/*
		 * This method updates the power level of the
		 * "Throttle" and triggers the audio stream
		 * which is used to fly the helicopter
		 */
		eegPower = calculateSpeed();

		Log.d(TAG, "eegPower: " + eegPower);

		progressBarPower.setProgress(eegPower);

		DevicePuzzleboxOrbitSingleton.getInstance().eegPower = eegPower;

		if (eegPower > 0) {
			updateScore();
			DevicePuzzleboxOrbitSingleton.getInstance().flightActive = true;
		} else {
			resetCurrentScore();
		}
		updateControlSignal();
	}

	public int calculateSpeed() {
		/*
		 * This method is used for calculating whether
		 * or not the "Attention" or "Meditation" levels
		 * are sufficient to trigger the helicopter throttle
		 */
		int mentalCommandPower = progressBarActivityMentalCommand.getProgress();
		int mentalCommandSeekValue = seekBarActivityMentalCommand.getProgress();

		int speed = 0;

		if (mentalCommandPower > mentalCommandSeekValue)
			speed = thresholdValuesMentalCommand[mentalCommandPower];

		if (speed > maximumPower)
			speed = maximumPower;
		if (speed < minimumPower)
			speed = 0;

		return(speed);
	}

	public void updateScore() {
		/*
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
		int mentalCommandScore = 0;
		int mentalCommandPower = progressBarActivityMentalCommand.getProgress();
		int mentalCommandTarget = seekBarActivityMentalCommand.getProgress();

		if ((mentalCommandPower >= mentalCommandTarget) &&
				(mentalCommandTarget > DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
			mentalCommandScore = mentalCommandTarget - DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget;

		DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent = DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent + mentalCommandScore;

		textViewScore.setText(Integer.toString(DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent));

		if (DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent > DevicePuzzleboxOrbitSingleton.getInstance().scoreHigh) {
			DevicePuzzleboxOrbitSingleton.getInstance().scoreHigh = DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent;
			textViewHighScore.setText(Integer.toString(DevicePuzzleboxOrbitSingleton.getInstance().scoreHigh));
		}
	}

	public void resetCurrentScore() {
		if (DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent > 0)
			textViewLastScore.setText(Integer.toString(DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent));
		DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent = 0;
		textViewScore.setText(Integer.toString(DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent));
	}

	public void playControl() {
		// TODO Convert to service
		if (DevicePuzzleboxOrbitSingleton.getInstance().generateAudio) {
			DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.ifFlip = DevicePuzzleboxOrbitSingleton.getInstance().invertControlSignal; // if checked then flip

//			int channel = 0; // default "A"
			int channel = DevicePuzzleboxOrbitSingleton.getInstance().defaultChannel;

			updateAudioHandlerLoopNumberWhileMindControl(-1); // Loop infinite for easier user testing

			updateAudioHandlerChannel(channel);

			DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.mutexNotify();
		} else {
			/*
			 * Play audio control file
			 */

			/* Getting the user sound settings */
			AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
			//			float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			//			float volume = actualVolume / maxVolume;

			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) maxVolume, 0);
			/* Is the sound loaded already? */
			if (DevicePuzzleboxOrbitSingleton.getInstance().loaded) {
				//				soundPool.play(soundID, volume, volume, 1, 0, 1f);
				//				soundPool.setVolume(soundID, 1f, 1f);
				//				soundPool.play(soundID, maxVolume, maxVolume, 1, 0, 1f); // Fixes Samsung Galaxy S4 [SGH-M919]

				DevicePuzzleboxOrbitSingleton.getInstance().soundPool.play(DevicePuzzleboxOrbitSingleton.getInstance().soundID, 1f, 1f, 1, 0, 1f); // Fixes Samsung Galaxy S4 [SGH-M919]

				// TODO No visible effects of changing these variables on digital oscilloscope
				//				soundPool.play(soundID, 0.5f, 0.5f, 1, 0, 0.5f);
				Log.d(TAG, "Played sound");
			}
		}
	}

	public void stopControl() {
		stopAudio();
		DevicePuzzleboxOrbitSingleton.getInstance().flightActive = false;
	}

	public void stopAudio() {
		/*
		 * stop AudioTrack as well as destroy service.
		 */
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.keepPlaying = false;

		/*
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
		/*
		 * Demo mode is called when the "Test Helicopter" button is pressed.
		 * This method can be easily adjusted for testing new features
		 * during development.
		 */
		if (! DevicePuzzleboxOrbitSingleton.getInstance().flightActive) {
			DevicePuzzleboxOrbitSingleton.getInstance().flightActive = true;
			DevicePuzzleboxOrbitSingleton.getInstance().demoActive = true;
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
	}

	private final BroadcastReceiver mActionReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String name = intent.getStringExtra("name");
			String value = intent.getStringExtra("value");

			if (name.equals("2")) {
				double actionPower = Double.valueOf(value);
				int eegMentalCommand = (int) (actionPower * 100);
				Log.d(TAG, "eegMentalCommand:" + eegMentalCommand);

				progressBarActivityMentalCommand.setProgress(eegMentalCommand);

				updateStatusImage();

				updatePower();
			}
		}
	};

	//	public void animateLightSaber(int animateTime, int animateSteps) {
	public void animateProgressBar() {
		handlerAnimation = new Handler();
		new Thread(new TaskAnimateProgressBar()).start();
	}

	private class TaskAnimateProgressBar implements Runnable {
		@Override
		public void run() {
			for (int i = 0; i <= 100; i += 5) {
				final int value = i;
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handlerAnimation.post(new Runnable() {
					@Override
					public void run() {
						switch (DeviceEmotivInsightSingleton.getInstance().currentInsightTraining) {
							case "neutral":
								progressBarTrainNeutral.setProgress(value);
								break;
							case "mental command":
								progressBarTrainMentalCommand.setProgress(value);
						}
					}
				});
			}
		}
	}
}
