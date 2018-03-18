package io.puzzlebox.jigsaw.protocol;

//import java.io.IOException;
//import java.lang.Math;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

//import com.hoho.android.usbserial.driver.UsbSerialDriver;

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
//		byte[] setting = commandNeutral;
		String setting = commandNeutral;
//		String setting = commandHover;
		
		while (keep_running) {

			if (command == "neutral") {
				setting = commandNeutral;
			} else if (command == "idle") {
				setting = commandIdle;
			} else if (command == "hover") {
				setting = commandHover;
			} else if (command == "maximum_thrust") {
				setting = commandMaximumThrust;
			}

//			if (mSerialDevice != null) {

//				try {
//					mSerialDevice.write(setting.getBytes(), device_frame_cycle);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					//tv.append("Error writing command to serial device\n");
//					e.printStackTrace();
//				}
//
//			} else {
//				keep_running = false;
//				//tv.append("Attempted to write command but no serial device found\n");
//			}


			try {
				Thread.sleep(device_frame_cycle);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} // while

		return response;

	} // doInBackground


	// #############################################################

	//	protected void setTextView(TextView tv) {
	//		
	//		this.tv = tv;
	//		
	//	}


	// #############################################################

//	protected void setSerialDevice(UsbSerialDriver mSerialDevice) {
//
//		this.mSerialDevice = mSerialDevice;
//
//	}

	
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
	
	
//	public static float[] linspace(int start, int stop, int n) {
//
//		float[] result = {};
//
//	   float step = (stop-start)/(n-1);
//
//	   for(int i = 0; i <= n-2; i++) {
//	       result.add(start + (i * step));
//	   }
//	   result.add(stop);
//
//	   return result;
//	}
	
	public static List<Double> linspace(double start, double stop, int n)
	{
	   List<Double> result = new ArrayList<Double>();

	   double step = (stop-start)/(n-1);

	   for(int i = 0; i <= n-2; i++)
	   {
	       result.add(start + (i * step));
	   }
	   result.add(stop);

	   return result;
	}
	
//	function wave = halfSineGenDirect(dir,halfPeriod_in_samples,zero,amp,sps) 
//
//			% dir: 'u' means UP, 'd' means down.
//			% halfPeriod: half period of sine signal[us].
//			% zero: zero level of cosine signal.
//			% amp: amplitude of cosine signal, in percentage. 
//
//			halfPeriod=halfPeriod_in_samples/sps;
//
//			bps = 16;       % bits per sample
//			%sps = 960000;     % sample rate [samples/s]
//			period = halfPeriod*2;   %period of [s]
//			freq = floor(sps/(halfPeriod_in_samples*2));       % frequency of the tone [Hz]
//
//			%linspace is an octave function that generates a sequence with certain step.
//			time = linspace(1/sps, halfPeriod, halfPeriod_in_samples);
//			if (dir== 'u') 
//				wave = sin(time*2*pi*freq)'.*amp+zero;%'u' indicates above zero line
//			elseif (dir=='d')
//				wave = sin((time+halfPeriod)*2*pi*freq)'.*amp+zero;%'d' indicates below zero line
//			endif

	
	// #############################################################

	protected void setCommand(String command) {

		this.command = command;

	}


	// #############################################################

//	protected void onPostExecute(String buffer) {
//
//		// pass
//
//	} // onPostExecute


} // class SerialDevice
