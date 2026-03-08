package io.puzzlebox.jigsaw.data;

import android.media.SoundPool;
import android.util.Log;

import io.puzzlebox.jigsaw.protocol.PuzzleboxOrbitAudioIRHandler;

public class DevicePuzzleboxOrbitSingleton {

	private final static String TAG = DevicePuzzleboxOrbitSingleton.class.getSimpleName();

	private static final DevicePuzzleboxOrbitSingleton ourInstance = new DevicePuzzleboxOrbitSingleton();

	public static DevicePuzzleboxOrbitSingleton getInstance() {
		return ourInstance;
	}

	private DevicePuzzleboxOrbitSingleton() {
	}

	/**
	 * Flight Configuration
	 */
	public final int defaultTargetAttention = 72;
	public final int defaultTargetMeditation = 0;

	public final int minimumScoreTarget = 40;
	public int scoreCurrent = 0;
	public int scoreHigh = 0;

	public boolean flightActive = false;

	/**
	 * Joystick Configuration
	 */
	public final int defaultJoystickThrottle = 0;
	public final int minimumJoystickThrottle = 20;
	//	public int defaultJoystickYaw = 63;
	public final int defaultJoystickYaw = 49;
	public final int defaultJoystickPitch = 31;

	/**
	 * Advanced Configuration
	 */
	public final int defaultControlThrottle = 80;
	public final int defaultControlYaw = 49;
	public final int defaultControlPitch = 31;

	//	public int defaultChannel = 0; // B
	public final int defaultChannel = 1; // A

	public final boolean generateAudio = true;
	public boolean invertControlSignal = false;

	public final boolean loaded = false;

	public PuzzleboxOrbitAudioIRHandler puzzleboxOrbitAudioIRHandler = new PuzzleboxOrbitAudioIRHandler();

	public SoundPool soundPool = null;
	public int soundID = 0;

	public int eegPower = 0;
	public boolean demoActive = false;

	public void resetControlSignal() {

		puzzleboxOrbitAudioIRHandler.command = new Integer[]{
				defaultControlThrottle,
				defaultControlYaw,
				defaultControlPitch,
				defaultChannel};

		puzzleboxOrbitAudioIRHandler.updateControlSignal();
	}

	public void startAudioHandler() {

		if (!DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.isAlive()) {

			try {
				puzzleboxOrbitAudioIRHandler.start();
			} catch (Exception e) {
				Log.e(TAG, "Exception starting PuzzleboxOrbitAudioIRHandler", e);

				DevicePuzzleboxOrbitSingleton.getInstance().resetAudioHandler();

			}
		}
	}

	public void resetAudioHandler() {
		puzzleboxOrbitAudioIRHandler.shutdown();
		puzzleboxOrbitAudioIRHandler = new PuzzleboxOrbitAudioIRHandler();
		puzzleboxOrbitAudioIRHandler.start();
	}
}
