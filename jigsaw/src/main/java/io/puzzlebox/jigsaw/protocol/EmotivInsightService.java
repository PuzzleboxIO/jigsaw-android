package io.puzzlebox.jigsaw.protocol;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;
import com.emotiv.insight.IEmoStateDLL;
import com.emotiv.insight.MentalCommandDetection;

import io.puzzlebox.jigsaw.data.DeviceEmotivInsightSingleton;

public class EmotivInsightService extends Service {

	private final static String TAG = EmotivInsightService.class.getSimpleName();

//	int[] contactQualityAllChannels;
//	int[] contactQualityAllChannelsPrevious;

//	BroadcastReceiver mConnectReceiver;

	//	IEmoStateDLL.IEE_SignalStrength_enum[] wirelessState;
//	int[] batteryChargeLevel;

	private boolean threadAlive = false;

	private ServiceHandler mServiceHandler;

	public EmotivInsightService() {
	}

	// ################################################################

	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {


			Log.d(TAG, "handleMessage: " + msg);

			// Normally we would do some work here, like download a file.
			// For our sample, we just sleep for 5 seconds.
			long endTime = System.currentTimeMillis() + 5*1000;
			while (System.currentTimeMillis() < endTime) {
				synchronized (this) {
					try {
						wait(endTime - System.currentTimeMillis());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			// Stop the service using the startId, so that we don't stop
			// the service in the middle of handling another job
			stopSelf(msg.arg1);
		}
	}


	// ################################################################

	@Override
	public void onCreate() {
		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				  Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		Looper mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

		// get an instance of the receiver in your service
//		IntentFilter filter = new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.connect");
//		mConnectReceiver = new ConnectReceiver();
//		this.registerReceiver(mConnectReceiver, filter);

//		initEmotivEngine();

		super.onCreate();

	}

	@Override
	public void onDestroy() {
//		this.unregisterReceiver(mConnectReceiver);
		IEdk.IEE_EngineDisconnect();
		threadAlive = false;
		super.onDestroy();
	}


	// ################################################################

	private void initEmotivEngine() {
		Log.e("", "connect " + IEdk.IEE_EngineConnect(this, ""));

		threadAlive = true;

		Thread processingThread=new Thread()
		{
			@Override
			public void run() {

				super.run();
//				while(true)
				while(threadAlive)
				{
					try
					{
						handler.sendEmptyMessage(0); // getNextEvent
						handler.sendEmptyMessage(1); //check bluetooth status and connect to emotiv_insight_slotted headset
						Thread.sleep(5);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		};
		processingThread.start();
	}

	// ################################################################

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

//			Log.d(TAG, "message: " + msg);

			switch (msg.what) {
				case 0:
					int state = IEdk.IEE_EngineGetNextEvent();
					if (state == IEdkErrorCode.EDK_OK.ToInt()) {
						int eventType = IEdk.IEE_EmoEngineEventGetType();
						DeviceEmotivInsightSingleton.getInstance().userID = IEdk.IEE_EmoEngineEventGetUserId();
						if(DeviceEmotivInsightSingleton.getInstance().userID < 0) {
							return;
						}
						if(eventType == IEdk.IEE_Event_t.IEE_UserAdded.ToInt()){
							Log.e("Emotiv", "User added: " + DeviceEmotivInsightSingleton.getInstance().userID);
//                            textStatus.setText("Status: Connected");
							Log.d(TAG, "Status: Connected");

							Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.emotiv.insight.status");
							intent.putExtra("name", "status");
							intent.putExtra("value", "connected");
							LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

						}
						if(eventType == IEdk.IEE_Event_t.IEE_UserRemoved.ToInt()) {
							Log.e("Emotiv", "User removed: " + DeviceEmotivInsightSingleton.getInstance().userID);
//                            textStatus.setText("Status: Disconnected");
							Log.d(TAG, "Status: Disconnected");
							Log.e(TAG, "lock: false");
							DeviceEmotivInsightSingleton.getInstance().lock = false;

							Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.emotiv.insight.status");
							intent.putExtra("name", "status");
							intent.putExtra("value", "disconnected");
							LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

							broadcastSignalQualityReset();
						}
						if (eventType == IEdk.IEE_Event_t.IEE_EmoStateUpdated.ToInt()) {
							IEdk.IEE_EmoEngineEventGetEmoState();

//							batteryChargeLevel = IEmoStateDLL.IS_GetBatteryChargeLevel();
//							Log.d(TAG, "batteryChargeLevel: " + batteryChargeLevel.toString());

							broadcastSignalQuality();

//							Log.d(TAG, "Action [" + IEmoStateDLL.IS_MentalCommandGetCurrentAction() + "]: " + IEmoStateDLL.IS_MentalCommandGetCurrentActionPower());

							Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.emotiv.insight.action");
							intent.putExtra("name", Integer.toString(IEmoStateDLL.IS_MentalCommandGetCurrentAction()));
							intent.putExtra("value", Double.toString(IEmoStateDLL.IS_MentalCommandGetCurrentActionPower()));
							LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

						}
						if (eventType == IEdk.IEE_Event_t.IEE_MentalCommandEvent.ToInt()) {
							int mcType = MentalCommandDetection.IEE_MentalCommandEventGetType();
							if(mcType == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingStarted.getType()) {
								Log.d("Emotiv", "Training started");
//								alert.show();
								broadcastTrainingStatus("Training Started");
							}
							else if(mcType == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingSucceeded.getType()) {
								Log.d("Emotiv", "Training succeeded");
								MentalCommandDetection.IEE_MentalCommandSetTrainingControl(DeviceEmotivInsightSingleton.getInstance().userID, MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_ACCEPT.getType());
							}
							else if(mcType == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingCompleted.getType()) {
								Log.d("Emotiv", "Training completed");
//								alert.dismiss();
//								success.show();
								broadcastTrainingStatus("Training Completed");
							}
							else if(mcType == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingRejected.getType()) {
								Log.d("Emotiv", "Training rejected");
								broadcastTrainingStatus("Training Rejected");

							}
							else if(mcType == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingReset.getType()) {
								Log.d("Emotiv", "Training reset");
							}
							else if(mcType == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingFailed.getType()) {
								Log.d("Emotiv", "Training failed");
//								alert.dismiss();
//								failed.show();
								broadcastTrainingStatus("Training Failed");
							}
						}
					}
					break;
				case 1:
					int number = IEdk.IEE_GetInsightDeviceCount();

					if(number > 0) {
						if(!DeviceEmotivInsightSingleton.getInstance().lock) {

							Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.emotiv.insight.status");

							intent.putExtra("name", "populateSelectEEG");
							intent.putExtra("value", "");

							LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);


						}
					}
//					else {
//						// TODO - 2017.09.25 Is this still correct behaviour?
////						Log.e(TAG, "TODO: IEdk.IEE_GetInsightDeviceCount() <= 0");
////						DeviceEmotivInsightSingleton.getInstance().lock = false;
//					}
					break;
			}
		}
	};


	// ################################################################

	private void broadcastSignalQuality() {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.emotiv.insight.signal_quality");

//		contactQualityAllChannels = IEmoStateDLL.IS_GetContactQualityFromAllChannels();

//		String CMS = String.valueOf(contactQualityAllChannels[0]);
//		String AF3 = String.valueOf(contactQualityAllChannels[1]);
//		String T7 = String.valueOf(contactQualityAllChannels[2]);
//		String Pz = String.valueOf(contactQualityAllChannels[3]);
//		String T8 = String.valueOf(contactQualityAllChannels[4]);
//		String AF4 = String.valueOf(contactQualityAllChannels[5]);

		String AF3 = String.valueOf(IEmoStateDLL.IS_GetContactQuality(IEmoStateDLL.IEE_InputChannels_t.IEE_CHAN_AF3.ToInt()));
		String AF4 = String.valueOf(IEmoStateDLL.IS_GetContactQuality(IEmoStateDLL.IEE_InputChannels_t.IEE_CHAN_AF4.ToInt()));
		String T7 = String.valueOf(IEmoStateDLL.IS_GetContactQuality(IEmoStateDLL.IEE_InputChannels_t.IEE_CHAN_T7.ToInt()));
		String T8 = String.valueOf(IEmoStateDLL.IS_GetContactQuality(IEmoStateDLL.IEE_InputChannels_t.IEE_CHAN_T8.ToInt()));
		String Pz = String.valueOf(IEmoStateDLL.IS_GetContactQuality(IEmoStateDLL.IEE_InputChannels_t.IEE_CHAN_Pz.ToInt()));
		String CMS = String.valueOf(IEmoStateDLL.IS_GetContactQuality(IEmoStateDLL.IEE_InputChannels_t.IEE_CHAN_CMS.ToInt()));


////		if (contactQualityAllChannels != contactQualityAllChannelsPrevious) {
//
//		Log.d(TAG, "Status:" +
//				  " AF3:" + AF3 +
//				  ", AF4:" + AF4 +
//				  ", T7:" + T7 +
//				  ", T8:" + T8 +
//				  ", Pz:" + Pz +
//				  ", CMS:" + CMS
//		);
//
////		}
////
////		contactQualityAllChannelsPrevious = contactQualityAllChannels;

		if (AF3.equals("null"))
			AF3 = "0";
		if (AF4.equals("null"))
			AF4 = "0";
		if (T7.equals("null"))
			T7 = "0";
		if (T8.equals("null"))
			T8 = "0";
		if (Pz.equals("null"))
			Pz = "0";
		if (CMS.equals("null"))
			CMS = "0";

		intent.putExtra("AF3", AF3);
		intent.putExtra("AF4", AF4);
		intent.putExtra("T7", T7);
		intent.putExtra("T8", T8);
		intent.putExtra("Pz", Pz);
		intent.putExtra("CMS", CMS);

		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

	}


	// ################################################################

	private void broadcastSignalQualityReset() {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.emotiv.insight.signal_quality");

		intent.putExtra("AF3", "0");
		intent.putExtra("AF4", "0");
		intent.putExtra("T7", "0");
		intent.putExtra("T8", "0");
		intent.putExtra("Pz", "0");
		intent.putExtra("CMS", "0");

		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

	}

	// ################################################################

	private void broadcastTrainingStatus(String status) {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.emotiv.insight.training");

		intent.putExtra("status", status);

		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

	}


	// ################################################################

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			int deviceNumber = msg.what;

			Log.d(TAG, "[IncomingHandler] deviceNumber:" + deviceNumber);

			if (deviceNumber >=0 ) {

				// Connect to EEG
				IEdk.IEE_ConnectInsightDevice(deviceNumber);

				if (!DeviceEmotivInsightSingleton.getInstance().lock) {
					Log.e(TAG, "lock: true");
					DeviceEmotivInsightSingleton.getInstance().lock = true;
				}

			} else {

				// Negative number sent, we want to disconnect
				IEdk.IEE_EngineDisconnect();

				if (DeviceEmotivInsightSingleton.getInstance().lock) {
					Log.e(TAG, "lock: false");
					DeviceEmotivInsightSingleton.getInstance().lock = false;
				}

				broadcastSignalQualityReset();

			}

		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/**
	 * When binding to the service, we return an interface to our messenger
	 * for sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
//		Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
		return mMessenger.getBinder();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		initEmotivEngine();
//		return Service.START_NOT_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

}
