package io.puzzlebox.jigsaw.data;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.emotiv.insight.IEdk;

import java.util.ArrayList;
import java.util.List;

public class DeviceEmotivInsightSingleton {

	private static final String TAG = DeviceEmotivInsightSingleton.class.getSimpleName();

	// Emotiv Insight
	public List<String> detectedDevices = new ArrayList<>();
	public boolean lock = false;
	public int userID = -1;
	public String currentInsightTraining = "neutral";

	public final int defaultMentalCommandPower = 0;

	/** Messenger for communicating with the service. */
	public Messenger mService = null;
	public boolean mBound = false;

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
		if (!mBound || mService == null) return;
		// Create and send a message to the service, using a supported 'what' value
		Message msg = Message.obtain(null, deviceNumber, 0, 0);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			Log.e(TAG, "Exception", e);
		}
	}

	public boolean selectEEGDialogVisible = false;
}
