package io.puzzlebox.jigsaw.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import io.puzzlebox.jigsaw.data.DevicePuzzleboxOrbitSingleton;

public class PuzzleboxOrbitAudioIRService extends Service {

	/**
	 * default field values.
	 */

	AudioTrack track;
	//	public int sampleRate = 44100;
	public int sampleRate = 48000;

	short[] audioData = new short[6144];
	int throttle=80;
	int yaw=78;
	int pitch=31;
	int channel=1;
	Integer[] command={throttle,yaw,pitch,channel};
	int loopNumberWhileMindControl=20;

	/**
	 * Tone Generator
	 * This is used to generate a carrier signal, to be played out the right
	 * audio channel when the IR dongle is attached
	 * Reference link - http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android
	 */
	private final int duration = 10; // seconds
	private final int sampleRateTone = 8000;
	private final int numSamples = duration * sampleRateTone;
	private final double sample[] = new double[numSamples];
	private final double freqOfTone = 440; // Hz

	final byte generatedSnd[] = new byte[2 * numSamples];

	public PuzzleboxOrbitAudioIRService()
	{
		int minSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
		track = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,minSize, AudioTrack.MODE_STREAM);
	}

	public PuzzleboxOrbitAudioIRService(int sps, boolean flip)
	{
		sampleRate = sps;
		int minSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
		track = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,minSize, AudioTrack.MODE_STREAM);
	}

	private final IBinder binder = new OrbitBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try{
			new DoBackgroundTask().execute(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		releasetrack();
		super.onDestroy();
	}

	private final double sampleTime = 1/(double)sampleRate;

	/**
	 * Half periods in the audio code, in seconds.
	 *
	 * Four periods exist in the wave
	 */
	private final double longHIGH = 0.000829649;
	private final double longLOW = 0.000797027;
	private final double shortHIGH = 0.000412649;
	private final double shortLOW = 0.000378351;

	/**
	 * Pre-calculated and stored half sine waves.
	 */
	private final float[] waveLongHIGH=halfSineGen('u',longHIGH);
	private final float[] waveLongLOW=halfSineGen('d',longLOW);
	private final float[] waveShortHIGH=halfSineGen('u',shortHIGH);
	private final float[] waveShortLOW=halfSineGen('d',shortLOW);

	/**
	 * Pre-assembled audio code bit array in wave form.
	 *
	 * waveBit is an array of two wave, each an array of numbers
	 * waveBit[0] is the first wave, waveBit[1] is the second wave
	 */
	private final float[] waveBit[]= {concatFloat(waveShortHIGH,waveShortLOW),concatFloat(waveLongHIGH,waveLongLOW)};

	public void send(float[] samples)
	{
		assembleRightChannel(samples);
		track.write(audioData,0,2*samples.length);
	}

	private void assembleRightChannel(float[] samples)
	{
		if(audioData.length < 2*samples.length)
			audioData=new short[2*samples.length];

		float increment = (float)((2* Math.PI)*500/sampleRate);
		float angle = 0;

		for (int i=0; i< samples.length; i++){
			audioData[2*i+1]=(short)(Math.sin(angle)* Short.MAX_VALUE);
			audioData[2*i]=(short)(samples[i]* Short.MAX_VALUE);
			angle += increment;
		}
	}

	/**
	 * release low level resources when not use them anymore.
	 */
	public void releasetrack(){
		track.release();
	} // releasetrack

	/**
	 * Turn throttle, pitch, yaw and channel into IR code (in wave form).
	 * @param throttle: 0~127, nothing will happen if this value is below 30.
	 * @param yaw: 0~127, normally 78 will keep orbit from rotating.
	 * @param pitch: 0~63, normally 31 will stop the top propeller.
	 * @param channel: 1=Channel A, 0=Channel B 2= Channel C, depend on which channel you want to pair to the orbit. You can fly at most 3 orbit in a same room. 
	 * @return
	 */
	public int command2code(int throttle, int yaw, int pitch, int channel){
		int code = throttle << 21;
		code += 1 << 20 ;
		code += yaw << 12;
		code += pitch << 4 ;
		//  weird, if use int code= throttle << 21 + 1<<20 + yaw <<12 +pitch<<4; it won't work.
		code += ((channel >>> 1) & 1) << 19;
		code += (channel & 1) << 11;

		int checkSum=0;
		for (int i=0; i<7; i++)
			checkSum += (code >> 4*i) & 15; //15=0x0F=0b00001111
		checkSum = 16-(checkSum & 15);
		return code + checkSum;
	}

	/**
	 * Generate one complete fly command in wave form on the fly.
	 * @param code: the control code array need to be turned into
	 * @return float[]: fully assembled fly command in a float array, to be written in buffer and sent out.
	 */
	public float[] code2wave(int code){

		float[] wave = halfSineGen('d',longLOW);

		// longHIGH-sampleTime*2 - float number used to tune the period of the wave

		float[] tempWave = concatFloat(halfSineGen('u',longHIGH-sampleTime*2),halfSineGen('d',shortLOW+sampleTime*2));
		wave = concatFloat(wave,tempWave);
		wave = concatFloat(wave,tempWave);

		// Takes out each bit 
		for (int i=27; i>=0; i--)
			wave=concatFloat(wave,waveBit[((code >>> i) & 1)]);

		wave=concatFloat(wave,waveLongHIGH);

		if (DevicePuzzleboxOrbitSingleton.getInstance().invertControlSignal)
			for (int i=0; i<wave.length; i++)
				wave[i]=-wave[i];

		wave=concatFloat(wave,new float[4096]);

		return wave;
	}

	/**
	 * Generate the initial wave required by IR dongle.
	 * Not a very interesting method to look into.
	 * @return
	 */
	public float[] initialWave(){
		final double initLongHIGH=0.001-sampleTime*1; //seconds
		final double initLongZERO=0.002+sampleTime*1;
		final double initMediumLOW=0.0005-sampleTime*1;
		final double initShortHIGH=0.0001+sampleTime*1;
		final double initShortLOW=0.00018;
		final double initPause=0.010;

		final float[] waveInitLongHIGH=halfSineGen('u',initLongHIGH);
		final int waveInitLongZEROLength= (int) Math.floor(initLongZERO * sampleRate);
		final float[] waveInitMediumLOW=halfSineGen('d',initMediumLOW);
		final float[] waveInitShortHIGH=halfSineGen('u',initShortHIGH);
		final float[] waveInitShortLOW=halfSineGen('d',initShortLOW);

		final float[] waveLongHLongZERO= concatFloat(waveInitLongHIGH,new float[waveInitLongZEROLength]);
		final float[] waveMediumLShortH = concatFloat(waveInitMediumLOW,waveInitShortHIGH);
		final float[] waveShortHShortL = concatFloat(waveInitShortHIGH, waveInitShortLOW);

		float[] initWaveOriginal = concatFloat(waveLongHLongZERO,waveLongHLongZERO);
		initWaveOriginal = concatFloat(initWaveOriginal,waveInitLongHIGH);
		initWaveOriginal = concatFloat(initWaveOriginal, waveMediumLShortH);

		float[] initWave123 = concatFloat(initWaveOriginal,waveInitMediumLOW);
		float[] initWave456 = concatFloat(initWaveOriginal,waveInitShortLOW);

		for (int i=0; i<4; i++){
			initWave123 = concatFloat(initWave123,waveShortHShortL);
			initWave456 = concatFloat(initWave456,waveShortHShortL);
		}

		initWave123 = concatFloat(initWave123,waveInitShortHIGH);
		initWave123 = concatFloat(initWave123,waveMediumLShortH);

		initWave456 = concatFloat(initWave456,waveInitShortHIGH);
		initWave456 = concatFloat(initWave456,waveMediumLShortH);

		if (DevicePuzzleboxOrbitSingleton.getInstance().invertControlSignal){
			for (int i=0; i<initWave123.length; i++)
				initWave123[i]=-initWave123[i];

			for (int i=0; i<initWave456.length; i++)
				initWave456[i]=-initWave456[i];
		}

		int initPauseInSamples=(int) Math.floor(initPause * sampleRate);
		initWave123 = concatFloat(initWave123,new float[initPauseInSamples]);
		initWave456 = concatFloat(initWave456,new float[initPauseInSamples]);

		float[] initWave123Pattern=initWave123;
		float[] initWave456Pattern=initWave456;

		for (int i=0; i<2; i++){
			initWave123 = concatFloat(initWave123, initWave123Pattern);
			initWave456 = concatFloat(initWave456, initWave456Pattern);
		}

		return concatFloat(initWave123,initWave456);

	}

	/**
	 * Connect two array together, use native system operation for max efficiency.
	 * @param A first array
	 * @param B second array
	 * @return the combined array
	 */
	public float[] concatFloat(float[] A, float[] B){
		float[] C = new float[A.length+B.length];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 0, C, A.length, B.length);
		return C;
	}

	/**
	 * Generate half sine signal.
	 * This is the smallest component of the wave.
	 * @param dir: 'u' or 'd', means it's the upper half or lower half or sine wave. 
	 * @param halfPeriod: half of the period of sine wave, in seconds
	 * @return float[]
	 */
	public float[] halfSineGen(char dir,double halfPeriod) {
		int halfPeriodInSamples = (int) Math.floor(halfPeriod * sampleRate);
		float halfSine[] = new float[halfPeriodInSamples];
		double increment = Math.PI/(halfPeriod*sampleRate);
		double angle = 0;

		if (dir == 'u')
			for (int i =0; i<halfPeriodInSamples;i++)
			{
				halfSine[i]=(float) Math.sin(angle);
				angle += increment;
			}
		else if (dir == 'd'){
			angle = Math.PI;
			for (int i =0; i<halfPeriodInSamples;i++)
			{
				halfSine[i]=(float) Math.sin(angle);
				angle += increment;
			}
		}
		return halfSine;
	}

	void genTone(){
		/**
		 * Generate a carrier signal for communication
		 * with the IR Dongle
		 */

		/** Fill out the array */
		for (int i = 0; i < numSamples; ++i) {
			sample[i] = Math.sin(2 * Math.PI * i / (sampleRateTone / freqOfTone));
		}

		/**
		 *  Convert to 16 bit pcm sound array.
		 *  The sample buffer is assumed to be normalized.
		 */
		int idx = 0;
		for (final double dVal : sample) {
			/** Scale to maximum amplitude */
			final short val = (short) ((dVal * 32767));
			/** In 16 bit wav PCM, first byte is the low order byte */
			generatedSnd[idx++] = (byte) (val & 0x00ff);
			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
		}
	}

	void playTone(){
		/**
		 * Play the generated carrier signal
		 */
		final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				sampleRateTone, AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
				AudioTrack.MODE_STATIC);
		audioTrack.write(generatedSnd, 0, generatedSnd.length);
		audioTrack.play();
	}

	private class DoBackgroundTask extends AsyncTask< Integer , Void , Integer > {
		protected Integer doInBackground(Integer... command) {

			int throttle=command[0].intValue();
			int yaw=command[1].intValue();
			int pitch=command[2].intValue();
			int channel=command[3].intValue();

			int code=command2code(throttle,yaw,pitch,channel);
			float[] wave=code2wave(code);

			track.play();

			for (int j = 0; j<4; j++)
				send(wave);

			send(initialWave());

			for (int j = 0; j<loopNumberWhileMindControl; j++)
				send(wave);

			track.stop();
			return 0;
		}
	}

	public class OrbitBinder extends Binder {
		PuzzleboxOrbitAudioIRService getService() {
			return PuzzleboxOrbitAudioIRService.this;
		}
	}
}
