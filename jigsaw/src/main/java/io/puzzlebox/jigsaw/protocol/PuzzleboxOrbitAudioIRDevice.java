package io.puzzlebox.jigsaw.protocol;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

class PuzzleboxOrbitAudioIRDevice extends AsyncTask<String, Void, String> {

	//	int device_frame_cycle = 21; // 22ms frame cycle for Blade mCX2 (MLP4DSM RC)
//	int device_frame_cycle = 500; // 0.5s frame cycle for Arduino
	int device_frame_cycle = 70; // 70ms frame cycle for IR Dongle
	boolean keep_running = true;

//	byte[] commandNeutral = {0x00, 0x00, 0x00, (byte) 0xaa, 0x05, (byte) 0xff, 0x09, (byte) 0xff, 0x0d, (byte) 0xff, 0x13, 0x54, 0x14, (byte) 0xaa};
//	byte[] commandHover = {0x00, 0x00, 0x01, 0x7d, 0x05, (byte) 0xc5, 0x09, (byte) 0xde, 0x0e, 0x0b, 0x13, 0x54, 0x14, (byte) 0xaa};
//	byte[] commandMaximumThrust = {0x00, 0x00, 0x03, 0x54, 0x05, (byte) 0xc5, 0x09, (byte) 0xde, 0x0e, 0x0b, 0x13, 0x54, 0x14, (byte) 0xaa};

	int[] commandThrottleMinimum = {1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0};
	int[] commandThrottleMaximum = {1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0};

	String commandNeutral = "x000";
	String commandHover = "x085";
	String commandIdle = "x030";
	String commandMaximumThrust = "x100";

	String command = "neutral";

	protected String doInBackground(String... buffers) {
		String response = "";
		String setting;

		while (keep_running) {

			switch (command) {
				case "neutral":
					setting = commandNeutral;
					break;
				case "idle":
					setting = commandIdle;
					break;
				case "hover":
					setting = commandHover;
					break;
				case "maximum_thrust":
					setting = commandMaximumThrust;
					break;
				default:
					setting = commandNeutral;
					break;
			}

			try {
				Thread.sleep(device_frame_cycle);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return response;
	}

	public int[] halfSineGenDirect(char direction, int halfPeriodInSamples, int zeroLevelOfCosine, float amplitude, int samplesPerSecond) {

		int halfPeriod = halfPeriodInSamples / samplesPerSecond;

		int bitsPerSample = 16;
//		samplesPerSecond = 960000; // sample rate
		int period = halfPeriodInSamples * 2; // period of (in seconds)
//		float frequency = floor(samplesPerSecond / (halfPeriodInSamples * 2)); // frequency of the tone (Hz)
		int frequency = samplesPerSecond / (halfPeriodInSamples * 2); // frequency of the tone (Hz)

//		float time[] = linspace( 1 / samplesPerSecond, halfPeriod, halfPeriodInSamples);
		List<Double> time = linspace( 1 / samplesPerSecond, halfPeriod, halfPeriodInSamples);

		int[] wave = {};

		return(wave);
	}

	public static List<Double> linspace(double start, double stop, int n) {
		List<Double> result = new ArrayList<>();

		double step = (stop-start)/(n-1);

		for(int i = 0; i <= n-2; i++)
		{
			result.add(start + (i * step));
		}
		result.add(stop);

		return result;
	}

	protected void setCommand(String command) {
		this.command = command;
	}
}
