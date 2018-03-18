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

//	public final static String profileID = "profile_puzzlebox_orbit_emotiv_insight";

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
//	ProgressBar progressBarBlink;

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
	//	int[] thresholdValuesMeditation = new int[101];
	int minimumPower = 0; // minimum power for the Orbit
	int maximumPower = 100; // maximum power for the Orbit

	private Handler handlerAnimation;

//	int userID = -1;

//	private static Intent intentThinkGear;

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
//		final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
		ShapeDrawable progressBarMentalCommandDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
//		String progressBarMentalCommandColor = "#FF0000";
//		String progressBarMentalCommandColor = "#FF00FF";
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

//		progressBarBlink = (ProgressBar) v.findViewById(R.id.progressBarBlink);
//		ShapeDrawable progressBarRangeDrawable = new ShapeDrawable();
////		String progressBarBlinkColor = "#FF00FF";
////		String progressBarBlinkColor = "#990099";
//		String progressBarBlinkColor = "#BBBBBB";
//		progressBarRangeDrawable.getPaint().setColor(Color.parseColor(progressBarBlinkColor));
//		ClipDrawable progressRange = new ClipDrawable(progressBarRangeDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
//		progressBarBlink.setProgressDrawable(progressRange);
//		progressBarBlink.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));
//
//		progressBarBlink.setMax(NeuroSkyThinkGearService.blinkRangeMax);


		seekBarActivityMentalCommand = v.findViewById(R.id.seekBarActivityMentalCommand);
		seekBarActivityMentalCommand.setProgress(DeviceEmotivInsightSingleton.getInstance().defaultMentalCommandPower);
		seekBarActivityMentalCommand.setOnSeekBarChangeListener(this);


		imageViewStatus = v.findViewById(R.id.imageViewStatus);

//		textViewLabelScore = (TextView) v.findViewById(R.id.textViewLabelScore);
//		textViewLabelLastScore = (TextView) v.findViewById(R.id.textViewLabelLastScore);
//		textViewLabelHighScore = (TextView) v.findViewById(R.id.textViewLabelHighScore);


		textViewScore = v.findViewById(R.id.textViewScore);
		textViewLastScore = v.findViewById(R.id.textViewLastScore);
		textViewHighScore = v.findViewById(R.id.textViewHighScore);


		// Hide the "Scores" label by default
//		textViewLabelScores.setVisibility(View.GONE);
//		viewSpaceScore.setVisibility(View.GONE);

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


//		intentThinkGear = new Intent(getActivity(), NeuroSkyThinkGearService.class);


		/*
		 * PuzzleboxOrbitAudioIRHandler
		 */

		if (!DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.isAlive()) {


			/*
			 * Prepare audio stream
			 */

			// TODO
//			maximizeAudioVolume(); // Automatically set media volume to maximum

			/* Set the hardware buttons to control the audio output */
//			getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

			/* Preload the flight control WAV file into memory */
//			DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
//			DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//				public void onLoadComplete(SoundPool soundPool,
//													int sampleId,
//													int status) {
//					DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().loaded = true;
//				}
//			});
//			DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().soundID = DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().soundPool.load(getActivity().getApplicationContext(), DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().audioFile, 1);


//			DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.start();
			DevicePuzzleboxOrbitSingleton.getInstance().startAudioHandler();


		}


//		if (DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().flightActive)
//			buttonTestFlight.setText(getResources().getString(R.string.button_stop_test));

		/*
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

	// ################################################################

	public void onPause() {

		super.onPause();

//		LocalBroadcastManager.getInstance(
//				  getActivity().getApplicationContext()).unregisterReceiver(
//				  mPacketReceiver);
//
//		LocalBroadcastManager.getInstance(
//				  getActivity().getApplicationContext()).unregisterReceiver(
//				  mEventReceiver);

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

	} // onPause


	// ################################################################

	public void onResume() {

		// Store access variables for window and blank point

		Window window = getDialog().getWindow();

		Point size = new Point();

		// Store dimensions of the screen in `size`
		Display display = window.getWindowManager().getDefaultDisplay();

		display.getSize(size);

		// Set the width of the dialog proportional to a percentage of the screen width
//		window.setLayout((int) (size.x * 0.9), WindowManager.LayoutParams.WRAP_CONTENT);
//		window.setLayout((int) (size.x * 0.975), WindowManager.LayoutParams.WRAP_CONTENT);
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

//		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
//				  mPacketReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.packet"));
//
//		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
//				  mEventReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.event"));

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				  mSignalQualityReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.signal_quality"));

		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				  mActionReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.action"));

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				  mTrainingReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.training"));

	}

	// ################################################################

	private BroadcastReceiver mSignalQualityReceiver = new BroadcastReceiver() {

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


				if (eegSignal <= 20) {
//				progressBarContactQuality.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));

					ShapeDrawable progressBarContactQualityDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null, null));
					String progressBarContactQualityColor = "#FFAA00";
					progressBarContactQualityDrawable.getPaint().setColor(Color.parseColor(progressBarContactQualityColor));
					ClipDrawable progressSignal = new ClipDrawable(progressBarContactQualityDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
					progressBarContactQuality.setProgressDrawable(progressSignal);
					progressBarContactQuality.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));


				} else {
//				progressBarContactQuality.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor("#00FF00")));

					ShapeDrawable progressBarContactQualityDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null, null));
					String progressBarContactQualityColor = "#00FF00";
					progressBarContactQualityDrawable.getPaint().setColor(Color.parseColor(progressBarContactQualityColor));
					ClipDrawable progressSignal = new ClipDrawable(progressBarContactQualityDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
					progressBarContactQuality.setProgressDrawable(progressSignal);
					progressBarContactQuality.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));


				}

				progressBarContactQuality.setProgress(eegSignal);

			}

			previousEEGSignal = eegSignal;

		}

	};


	// ################################################################

	private BroadcastReceiver mTrainingReceiver = new BroadcastReceiver() {

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

	// ################################################################

//	private BroadcastReceiver mPacketReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//
//			int eegMentalCommand = Integer.valueOf(intent.getStringExtra("Attention"));
//			int eegSignal = Integer.valueOf(intent.getStringExtra("Signal Level"));
//
//			progressBarActivityMentalCommand.setProgress(eegMentalCommand);
//			progressBarContactQuality.setProgress(eegSignal);
//
////			if ((! buttonDeviceEnable.isEnabled()) && (eegSignal == NeuroSkyThinkGearService.signalSignalMax)) {
////				// This setting requires the quality of the EEG sensor's
////				// contact with skin hit to 100% at least once since the
////				// headset was last connected.
////				buttonDeviceEnable.setEnabled(true);
////				buttonDeviceEnable.setVisibility(View.VISIBLE);
////			}
//
////			progressBarBlink.setProgress(0);
//
////			updateEEGRawHistory(SessionSingleton.getInstance().getCurrentRawEEG());
//
////			updateSessionTime();
//
//			updateStatusImage();
//
////			Log.e(TAG, "mPacketReceiver: eegConnected: " + eegConnected);
////			if (eegConnected.equals("true"))
////				setButtonText(R.id.buttonConnectEEG, "Disconnect EEG");
////			else
////				setButtonText(R.id.buttonConnectEEG, "Connect EEG");
//
//
//			updatePower();
//
//
//		}
//
//	};

	// ################################################################

	public void updateControlSignal() {

		Integer[] command =  {
				  seekBarThrottle.getProgress(),
				  DevicePuzzleboxOrbitSingleton.getInstance().defaultControlYaw,
				  DevicePuzzleboxOrbitSingleton.getInstance().defaultControlPitch,
				  DevicePuzzleboxOrbitSingleton.getInstance().defaultChannel};


		// Transmit zero Throttle power if not above EEG power threashold
		// or demo mode (test flight) is not active
//		if ((eegPower <= 0) || (! DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().demoActive)){
		if (eegPower <= 0) {
//			Log.e(TAG, "(eegPower <= 0)");
			command[0] = 0;
		}


		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.command = command;

		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.updateControlSignal();

	} // updateControlSignal


	// ################################################################

	public void updateStatusImage() {

//		if(DEBUG) {
//			Log.v(TAG, (new StringBuilder("Attention: ")).append(eegAttention).toString());
//			Log.v(TAG, (new StringBuilder("Meditation: ")).append(eegMeditation).toString());
//			Log.v(TAG, (new StringBuilder("Power: ")).append(eegPower).toString());
//			Log.v(TAG, (new StringBuilder("Signal: ")).append(eegSignal).toString());
//			Log.v(TAG, (new StringBuilder("Connecting: ")).append(eegConnecting).toString());
//			Log.v(TAG, (new StringBuilder("Connected: ")).append(eegConnected).toString());
//		}
//
		if(eegPower > 0) {
			imageViewStatus.setImageResource(R.drawable.status_4_active);
			return;
		}

		// TODO
//		if(eegSignal > 90) {
//			imageViewStatus.setImageResource(R.drawable.status_3_processing);
//			return;
//		}

		if (progressBarContactQuality.getProgress() > 20) {
			imageViewStatus.setImageResource(R.drawable.status_3_processing);
			return;
		}

		if(DeviceEmotivInsightSingleton.getInstance().lock) {
			imageViewStatus.setImageResource(R.drawable.status_2_connected);
			return;
		}
//
//		if(eegConnecting) {
//			imageViewStatus.setImageResource(R.drawable.status_1_connecting);
//			return;
//		} else {
//			imageViewStatus.setImageResource(R.drawable.status_default);
//			return;
//		}

		imageViewStatus.setImageResource(R.drawable.status_default);

	} // updateStatusImage



	// ################################################################

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

		updatePowerThresholds();

		updateControlSignal();

	} // onProgressChanged


	// ################################################################

	public void onStartTrackingTouch(SeekBar seekBar) {

		/*
		  Method required by SeekBar.OnSeekBarChangeListener
		 */


	} // onStartTrackingTouch


	// ################################################################

	public void onStopTrackingTouch(SeekBar seekBar) {

		Log.v(TAG, "onStopTrackingTouch()");


	} // onStopTrackingTouch


	// ################################################################

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

	} // updatePowerThresholds


	// ################################################################

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

			/* Start playback of audio control stream */
//			if (!DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().flightActive) {
//				playControl();
//			}

//			buttonTestFlight.setText( getResources().getString(R.string.button_stop_test) );

			updateScore();

			DevicePuzzleboxOrbitSingleton.getInstance().flightActive = true;

		} else {

			/* Land the helicopter */
//			if (! DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().demoActive ) {
//				stopControl();
//			}

//			buttonTestFlight.setText(getResources().getString(R.string.button_test_fly));

			resetCurrentScore();

		}

		updateControlSignal();


//		Log.d(TAG, "flightActive: " + DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().flightActive);


	} // updatePower


	// ################################################################

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


	} // calculateSpeed


	// ################################################################

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

//		if ((eegMeditation >= eegMeditationTarget) &&
//				  (eegMeditationTarget > DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
//			eegMeditationScore = eegMeditationTarget - DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget;

//		if (mentalCommandScore > eegMeditationScore)
//			DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent = DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent + mentalCommandScore;
//		else
//			DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent = DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent + eegMeditationScore;

		DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent = DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent + mentalCommandScore;


		textViewScore.setText(Integer.toString(DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent));

		if (DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent > DevicePuzzleboxOrbitSingleton.getInstance().scoreHigh) {
			DevicePuzzleboxOrbitSingleton.getInstance().scoreHigh = DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent;
			textViewHighScore.setText(Integer.toString(DevicePuzzleboxOrbitSingleton.getInstance().scoreHigh));
		}


//		// Catch anyone gaming the system with one slider
//		// below the minimum threshold and the other over.
//		// For example, setting Meditation to 1% will keep helicopter
//		// activated even if Attention is below target
//		if ((mentalCommandPower < mentalCommandTarget) && (eegMeditation < DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
//			resetCurrentScore();
//		if ((eegMeditation < eegMeditationTarget) && (mentalCommandPower < DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
//			resetCurrentScore();
//		if ((mentalCommandPower < DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget) && (eegMeditation < DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
//			resetCurrentScore();


	} // updateScore


	// ################################################################

	public void resetCurrentScore() {

		if (DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent > 0)
			textViewLastScore.setText(Integer.toString(DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent));
		DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent = 0;
		textViewScore.setText(Integer.toString(DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent));

	} // resetCurrentScore


	// ################################################################

	public void playControl() {

		Log.d(TAG, "playControl()");


		// TODO Convert to service

//		FragmentTabAdvanced fragmentAdvanced =
//				  (FragmentTabAdvanced) getActivity().getSupportFragmentManager().findFragmentByTag( getTabFragmentAdvanced() );
//
		if (DevicePuzzleboxOrbitSingleton.getInstance().generateAudio) {

			/*
			 * Generate signal on the fly
			 */

//			// Handle controlled descent thread if activated
//			if ((fragmentAdvanced.orbitControlledDescentTask != null) &&
//					  (fragmentAdvanced.orbitControlledDescentTask.keepDescending)) {
//				fragmentAdvanced.orbitControlledDescentTask.callStopAudio = false;
//				fragmentAdvanced.orbitControlledDescentTask.keepDescending = false;
//			}


			//			if (puzzleboxOrbitAudioIRHandler != null) {

			//				serviceBinder.ifFlip = fragmentAdvanced.checkBoxInvertControlSignal.isChecked(); // if checked then flip
			DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.ifFlip = DevicePuzzleboxOrbitSingleton.getInstance().invertControlSignal; // if checked then flip

//			int channel = 0; // default "A"
			int channel = DevicePuzzleboxOrbitSingleton.getInstance().defaultChannel;

//			if (fragmentAdvanced != null)
//				channel = fragmentAdvanced.radioGroupChannel.getCheckedRadioButtonId();

			//				if (demoFlightMode)
			//					updateAudioHandlerLoopNumberWhileMindControl(200);
			//				else
			//					updateAudioHandlerLoopNumberWhileMindControl(4500);
			//
			//			updateAudioHandlerLoopNumberWhileMindControl(5000);

			updateAudioHandlerLoopNumberWhileMindControl(-1); // Loop infinite for easier user testing

			updateAudioHandlerChannel(channel);

			DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.mutexNotify();

			//			}
//
//
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
//				if (DEBUG)
				Log.v(TAG, "Played sound");
			}

		}

	} // playControl


	// ################################################################

	public void stopControl() {

		Log.d(TAG, "stopControl()");

		// TODO Convert to service

//		FragmentTabAdvanced fragmentAdvanced =
//				  (FragmentTabAdvanced) getSupportFragmentManager().findFragmentByTag( getTabFragmentAdvanced() );
//
//
//		// Initial Controlled Descent if activated by user
//		if ((generateAudio) &&
//				  (flightActive) &&
//				  (fragmentAdvanced != null) &&
//				  (fragmentAdvanced.checkBoxControlledDescent.isChecked()) &&
//				  (puzzleboxOrbitAudioIRHandler != null)) {
//
//			fragmentAdvanced.registerControlledDescent();
//
//		} else {
//
		stopAudio();
//
//		}
//
		DevicePuzzleboxOrbitSingleton.getInstance().flightActive = false;


	} // stopControl


	// ################################################################

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


	} // stopControl


	// ################################################################

	public void testFlight(View v) {

		/*
		 * Demo mode is called when the "Test Helicopter" button is pressed.
		 * This method can be easily adjusted for testing new features
		 * during development.
		 */

		Log.v(TAG, "Test Flight clicked");


//		Button buttonTestFlight = (Button) v.findViewById(R.id.buttonTestFlight);


		if (! DevicePuzzleboxOrbitSingleton.getInstance().flightActive) {


//		demoFlightMode = true;
			DevicePuzzleboxOrbitSingleton.getInstance().flightActive = true;
			DevicePuzzleboxOrbitSingleton.getInstance().demoActive = true;
//
//		FragmentTabAdvanced fragmentAdvanced =
//				  (FragmentTabAdvanced) getSupportFragmentManager().findFragmentByTag( getTabFragmentAdvanced() );
//
//		//		if (fragmentAdvanced.checkBoxGenerateAudio.isChecked())
//		if (generateAudio && (fragmentAdvanced != null))
//			eegPower = fragmentAdvanced.seekBarThrottle.getProgress();
//		else
//			eegPower = 100;

//			buttonTestFlight.setText( getResources().getString(R.string.button_stop_test) );

			// NOTE 2017-05-10
			// Control signal should always play to keep Orbit from timing out.
			// Zero throttle will keep the Orbit from taking off
			// In order to have a manual "Stop" ability, the "Test Flight" button doubles as a "Land" button
			// Turn on sound if it was manually turned off
//			playControl();

//		demoFlightMode = false;

		} else {

//			DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().flightActive = false;
			DevicePuzzleboxOrbitSingleton.getInstance().demoActive = false;

			// Manual "Stop" ability
//			stopControl();

//			buttonTestFlight.setText(getResources().getString(R.string.button_test_fly));

		}


		updateControlSignal();


	} // testFlight


	// ################################################################

	public void resetFlight(View view) {

		Log.v(TAG, "Reset clicked");

		resetCurrentScore();

		DevicePuzzleboxOrbitSingleton.getInstance().demoActive = false;

		// Setting eegPower to zero will cause the Orbit to land if flying
		// However if the user's data is actively being received
		// the Orbit may take off again approximately one second later
		eegPower = 0;

//		seekBarActivityMentalCommand.setProgress(DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().defaultTargetAttention);

		updatePowerThresholds();

//		stopControl();

		updateControlSignal();

	} // resetFlight


	// ################################################################

//	public void demoStop(View view) {
//
////		eegPower = 0;
//
//		stopControl();
//
//	} // demoStop


	// ################################################################

//	public void updateScore() {
//
//		FragmentTabFlightThinkGear fragmentFlight =
//				  (FragmentTabFlightThinkGear) getSupportFragmentManager().findFragmentByTag( getTabFragmentFlightThinkGear() );
//
//		if (fragmentFlight != null)
//			fragmentFlight.updateScore();
//
//	} // updateScore


	// ################################################################

//	public void resetCurrentScore() {
//
//		FragmentTabFlightThinkGear fragmentFlight =
//				  (FragmentTabFlightThinkGear) getSupportFragmentManager().findFragmentByTag( getTabFragmentFlightThinkGear() );
//
//		if (fragmentFlight != null)
//			fragmentFlight.resetCurrentScore();
//
//	} // resetCurrentScore


	// ################################################################

	/**
	 * the puzzleboxOrbitAudioIRHandler to update command
	 */
	public void updateAudioHandlerCommand(Integer[] command) {

//		this.puzzleboxOrbitAudioIRHandler.command = command;
//		this.puzzleboxOrbitAudioIRHandler.updateControlSignal();
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.command = command;
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.updateControlSignal();


	} // updateServiceBinderCommand


	// ################################################################

	/**
	 * the puzzleboxOrbitAudioIRHandler to update channel
	 */
	public void updateAudioHandlerChannel(int channel) {

//		this.puzzleboxOrbitAudioIRHandler.channel = channel;
//		this.puzzleboxOrbitAudioIRHandler.updateControlSignal();
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.channel = channel;
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.updateControlSignal();


	} // updateServiceBinderChannel


	// ################################################################

	/**
	 * @param number the puzzleboxOrbitAudioIRHandler to update loop number while mind control
	 */
	public void updateAudioHandlerLoopNumberWhileMindControl(int number) {

//		this.puzzleboxOrbitAudioIRHandler.loopNumberWhileMindControl = number;
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.loopNumberWhileMindControl = number;


	} // updateServiceBinderLoopNumberWhileMindControl


	// ################################################################

	public void resetControlSignal(View view) {

//		/**
//		 * Called when the "Reset" button is pressed
//		 */
//
//		FragmentTabAdvanced fragmentAdvanced =
//				  (FragmentTabAdvanced) getSupportFragmentManager().findFragmentByTag( getTabFragmentAdvanced() );
//
//		if (fragmentAdvanced != null)
//			fragmentAdvanced.resetControlSignal();


	} // resetControlSignal


	// ################################################################

	public void setControlSignalHover(View view) {

//		/**
//		 * Called when the "Hover" button is pressed
//		 */
//
//		FragmentTabAdvanced fragmentAdvanced =
//				  (FragmentTabAdvanced) getSupportFragmentManager().findFragmentByTag( getTabFragmentAdvanced() );
//
//		if (fragmentAdvanced != null)
//			fragmentAdvanced.setControlSignalHover();


	} // setControlSignalHover


	// ################################################################

	public void setControlSignalForward(View view) {

//		/**
//		 * Called when the "Forward" button is pressed
//		 */
//
//		FragmentTabAdvanced fragmentAdvanced =
//				  (FragmentTabAdvanced) getSupportFragmentManager().findFragmentByTag( getTabFragmentAdvanced() );
//
//		if (fragmentAdvanced != null)
//			fragmentAdvanced.setControlSignalForward();


	} // setControlSignalForward


	// ################################################################

	public void setControlSignalLeft(View view) {

//		/**
//		 * Called when the "Left" button is pressed
//		 */
//
//		FragmentTabAdvanced fragmentAdvanced =
//				  (FragmentTabAdvanced) getSupportFragmentManager().findFragmentByTag( getTabFragmentAdvanced() );
//
//		if (fragmentAdvanced != null)
//			fragmentAdvanced.setControlSignalLeft();


	} // setControlSignalLeft


	// ################################################################

	public void setControlSignalRight(View view) {

//		/**
//		 * Called when the "Right" button is pressed
//		 */
//
//		FragmentTabAdvanced fragmentAdvanced =
//				  (FragmentTabAdvanced) getSupportFragmentManager().findFragmentByTag( getTabFragmentAdvanced() );
//
//		if (fragmentAdvanced != null)
//			fragmentAdvanced.setControlSignalRight();


	} // setControlSignalRight



	// ################################################################

	private BroadcastReceiver mActionReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

//			String action = intent.getAction();

			String name = intent.getStringExtra("name");
			String value = intent.getStringExtra("value");

//			Log.d(TAG, "Action [" + name + "]: " + value);


			if (name.equals("2")) {
//				Log.d(TAG, "Action [Mental Command]: " + value);

				double actionPower = Double.valueOf(value);

//				Log.d(TAG, "actionPower:" + actionPower);

				int eegMentalCommand = (int) (actionPower * 100);

				Log.d(TAG, "eegMentalCommand:" + eegMentalCommand);

				progressBarActivityMentalCommand.setProgress(eegMentalCommand);

				updateStatusImage();

				updatePower();


			}


//			switch(name) {
//
//				case "status":
//
//					switch(value) {
//
//						case "connected":
//							connectEEG.setText(getString(R.string.buttonStatusEmotivInsightDisconnect));
//
//							buttonDeviceEnable.setEnabled(true);
//							buttonDeviceEnable.setVisibility(View.VISIBLE);
//
//							break;
//
//						case "disconnected":
//							connectEEG.setText(getString(R.string.buttonStatusEmotivInsightConnect));
//
//							buttonDeviceEnable.setEnabled(false);
//							buttonDeviceEnable.setVisibility(View.INVISIBLE);
//
//							break;
//
//					}
//
//					break;
//
//			}

		}

	};


	// ################################################################

	//	public void animateLightSaber(int animateTime, int animateSteps) {
	public void animateProgressBar() {

		handlerAnimation = new Handler();
		new Thread(new TaskAnimateProgressBar()).start();

	}


	// ################################################################

	private class TaskAnimateProgressBar implements Runnable {
		@Override
		public void run() {
//			for (int i = 0; i <= 20; i++) {
			for (int i = 0; i <= 100; i += 5) {
				final int value = i;
				try {
//					Thread.sleep(1000);
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handlerAnimation.post(new Runnable() {
					@Override
					public void run() {
//						drawLightSaber(value);

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
