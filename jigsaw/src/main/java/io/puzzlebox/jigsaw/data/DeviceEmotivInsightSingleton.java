package io.puzzlebox.jigsaw.data;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.emotiv.insight.IEdk;

import java.util.ArrayList;
import java.util.List;

public class DeviceEmotivInsightSingleton {

	// Emotiv Insight
	public List<String> detectedDevices = new ArrayList<>();
	//	public boolean calibrationActive = false;
//	public int neutralTrainingTime = 8;
//	public int pushTrainingTime = 5;
	public boolean lock = false;
	public int userID = -1;
	public String currentInsightTraining = "neutral";

//	public int defaultMentalCommandPower = 72;
	public int defaultMentalCommandPower = 0;

//	ServiceHandler mServiceHandler;

//	public static Intent intentEmotivInsight;

//	public IBinder mServiceBinder;

	/** Messenger for communicating with the service. */
	public Messenger mService = null;

	/** Flag indicating whether we have called bind on the service. */
	public boolean mBound;

	private static final DeviceEmotivInsightSingleton ourInstance = new DeviceEmotivInsightSingleton();

	public static DeviceEmotivInsightSingleton getInstance() {
		return ourInstance;
	}

	private DeviceEmotivInsightSingleton() {
	}

	public int getInsightDeviceCount() {
		return IEdk.IEE_GetInsightDeviceCount();
	}

	public String getInsightDeviceName(int index){
		return IEdk.IEE_GetInsightDeviceName(index);
	}

	public void connectEmotivInsight(int deviceNumber) {
		if (!mBound) return;
		// Create and send a message to the service, using a supported 'what' value
//		Message msg = Message.obtain(null, EmotivInsightService.MSG_SAY_HELLO, 0, 0);
		Message msg = Message.obtain(null, deviceNumber, 0, 0);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public boolean selectEEGDialogVisible = false;

	/*
	 * Class for interacting with the main interface of the service.
	 */
//	private ServiceConnection mConnection = new ServiceConnection() {
//		public void onServiceConnected(ComponentName className, IBinder service) {
//			// This is called when the connection with the service has been
//			// established, giving us the object we can use to
//			// interact with the service.  We are communicating with the
//			// service using a Messenger, so here we get a client-side
//			// representation of that from the raw IBinder object.
////			mService = new Messenger(service);
////			mBound = true;
//			DeviceEmotivInsightSingleton.getInstance().mService = new Messenger(service);
//			DeviceEmotivInsightSingleton.getInstance().mBound = true;
//		}
//
//		public void onServiceDisconnected(ComponentName className) {
//			// This is called when the connection with the service has been
//			// unexpectedly disconnected -- that is, its process crashed.
////			mService = null;
////			mBound = false;
//			DeviceEmotivInsightSingleton.getInstance().mService = null;
//			DeviceEmotivInsightSingleton.getInstance().mBound = false;
//		}
//	};
//
////	@Override
//	protected void onStart() {
////		super.onStart();
//		// Bind to the service
//		bindService(new Intent(this, EmotivInsightService.class), mConnection,
//				  Context.BIND_AUTO_CREATE);
//	}
//
////	@Override
////	protected void onStop() {
////		super.onStop();
//		// Unbind from the service
//		if (DeviceEmotivInsightSingleton.getInstance().mBound) {
//			unbindService(mConnection);
//			DeviceEmotivInsightSingleton.getInstance().mBound = false;
//		}
//	}

}
