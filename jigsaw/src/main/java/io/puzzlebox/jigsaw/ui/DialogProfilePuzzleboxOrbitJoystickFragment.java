package io.puzzlebox.jigsaw.ui;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import io.puzzlebox.jigsaw.data.ConfigurationSingleton;
import io.puzzlebox.jigsaw.data.DevicePuzzleboxOrbitSingleton;
import io.puzzlebox.jigsaw.data.ProfileSingleton;
import io.puzzlebox.jigsaw.R;

public class DialogProfilePuzzleboxOrbitJoystickFragment extends DialogFragment {

	private final static String TAG = DialogProfilePuzzleboxOrbitJoystickFragment.class.getSimpleName();

	public final static String profileID = "profile_puzzlebox_orbit_joystick";

	// UI
	public SeekBar seekBarThrottle;
	public SeekBar seekBarYaw;
	public SeekBar seekBarPitch;
	Button buttonDeviceEnable;

	private static final int paddingJoysticks = 20;

	private OnFragmentInteractionListener mListener;

	public DialogProfilePuzzleboxOrbitJoystickFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.dialog_profile_puzzlebox_orbit_joystick, container, false);

		getDialog().getWindow().setTitle( getString(R.string.title_dialog_fragment_puzzlebox_orbit_joystick));

		seekBarThrottle = v.findViewById(R.id.seekBarThrottle);
		seekBarThrottle.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultJoystickThrottle);

		seekBarYaw = v.findViewById(R.id.seekBarYaw);
		seekBarYaw.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultJoystickYaw);

		seekBarPitch = v.findViewById(R.id.seekBarPitch);
		seekBarPitch.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultJoystickPitch);

		LinearLayout llJoysticks = v.findViewById(R.id.linearLayoutJoysticks);

		JoystickView joystickViewThrottle = v.findViewById(R.id.joystickViewThrottle);
		joystickViewThrottle.setOnMoveListener(onMoveJoystickThrottle);

		JoystickView joystickViewYawPitch = v.findViewById(R.id.joystickViewYawPitch);
		joystickViewYawPitch.setOnMoveListener(onMoveJoystickYawPitch);

		ViewGroup.LayoutParams lp = llJoysticks.getLayoutParams();
		lp = joystickViewThrottle.getLayoutParams();

		if (((int) (ConfigurationSingleton.getInstance().displayWidth / 2))
				< (lp.width * 2 + paddingJoysticks * 2)) {

			lp.width = ((int) (ConfigurationSingleton.getInstance().displayWidth / 2)) - paddingJoysticks;
			joystickViewThrottle.setLayoutParams(lp);

			lp = joystickViewYawPitch.getLayoutParams();
			lp.width = ((int) (ConfigurationSingleton.getInstance().displayWidth / 2)) - paddingJoysticks;
			joystickViewYawPitch.setLayoutParams(lp);
		}

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
		window.setLayout((int)(size.x *0.99), WindowManager.LayoutParams.WRAP_CONTENT);

		window.setGravity(Gravity.CENTER);

		super.onResume();

		if (ProfileSingleton.getInstance().getValue(DialogOutputAudioIRFragment.profileID, "active").equals("true"))
			playControl();
		else
			Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_puzzlebox_orbit_joystick_audio_ir_warning), Toast.LENGTH_LONG).show();
	}

	private final JoystickView.OnMoveListener onMoveJoystickYawPitch = new JoystickView.OnMoveListener(){
		public void onMove(int angle, int strength) {
			Log.v(TAG, "onMoveJoystickYawPitch(int angle, int strength): " + angle + ", " + strength);

			if ((angle == 0) && (strength == 0)) {
				seekBarYaw.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultJoystickYaw);
				seekBarPitch.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultJoystickPitch);
			}

			if ((angle >= 60) && (angle <= 120)) {
				// Up
				int newX = seekBarPitch.getMax() / 2;
				newX = (int) (newX * (strength / 100.0));
				newX = seekBarPitch.getMax() / 2 + newX;
				seekBarPitch.setProgress(newX);
			}
			else if ((angle >= 240) && (angle <= 300)) {
				// Down
				int newX = seekBarPitch.getMax() / 2;
				newX = (int) (newX * (strength / 100.0));
				newX = seekBarPitch.getMax() / 2 - newX;
				seekBarPitch.setProgress(newX);
			}
			else if ((angle >= 150) && (angle <= 210)) {
				// Left
				int newY = seekBarYaw.getMax() / 2;
				newY = (int) (newY * (strength / 100.0));
				newY = seekBarYaw.getMax() / 2 - newY;
				seekBarYaw.setProgress(newY);
			}
			else if ((angle >= 330) || (angle <= 30)) {
				// Right
				int newY = seekBarYaw.getMax() / 2;
				newY = (int) (newY * (strength / 100.0));
				newY = seekBarYaw.getMax() / 2 + newY;
				seekBarYaw.setProgress(newY);
			}

			updateControlSignal();
		}
	};

	private final JoystickView.OnMoveListener onMoveJoystickThrottle = new JoystickView.OnMoveListener(){
		public void onMove(int angle, int strength) {
			Log.v(TAG, "onMoveJoystickThrottle(int angle, int strength): " + angle + ", " + strength);

			if ((angle == 0) && (strength == 0)) {
				seekBarThrottle.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultJoystickThrottle);
			}
			else if ((angle >= 30) && (angle <= 150)) {
				// Up

				// Ensure lower half of seekBarThrottle can be accessed from the top half of throttle joystick
				int newX = (int) (seekBarThrottle.getMax() * (strength / 100.0));

				// Set a minimum about of throttle to send if anywhere above zero level
				// of Orbit. Normally it takes some small amount of throttle to trigger
				// any flight or visible reaction.
				if (newX < DevicePuzzleboxOrbitSingleton.getInstance().minimumJoystickThrottle)
					newX = DevicePuzzleboxOrbitSingleton.getInstance().minimumJoystickThrottle;

				seekBarThrottle.setProgress(newX);
			}
			else if ((angle >= 210) && (angle <= 330)) {
				// Down
				seekBarThrottle.setProgress(0);
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
				1};

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
		// Stop AudioTrack as well as destroy service.
		DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.keepPlaying = false;

		// Stop playing audio control file
		if (DevicePuzzleboxOrbitSingleton.getInstance().soundPool != null) {
			try {
				DevicePuzzleboxOrbitSingleton.getInstance().soundPool.stop(DevicePuzzleboxOrbitSingleton.getInstance().soundID);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
