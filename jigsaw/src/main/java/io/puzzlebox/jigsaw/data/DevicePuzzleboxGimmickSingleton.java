package io.puzzlebox.jigsaw.data;

import android.bluetooth.le.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sc on 11/10/16.
 */
public class DevicePuzzleboxGimmickSingleton {

	private static final String TAG = DevicePuzzleboxGimmickSingleton.class.getSimpleName();

//	public List<String> detectedDevices = new ArrayList<>();
	public boolean lock = false;
	public boolean selectGimmickDialogVisible = false;

	private static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
	//	private static String PERIPHERAL_SERVICE_UUID =      "0000cc00-0000-1000-8000-00805f9b34fb";
	private static String PERIPHERAL_SERVICE_UUID =      "0000cc00-0000-0000-0000-000000101010";
	private static String COMMAND_CHARACTERISTIC_UUID =  "0000cc0a-0000-1000-8000-00805f9b34fb";
	private static String HASH_CHARACTERISTIC_UUID =     "0000cc0b-0000-1000-8000-00805f9b34fb";

	public String deviceHash = null;

	//	private ArrayList<ScanResult> devicesFound = new ArrayList<>();
	public ArrayList<ScanResult> devicesFound = new ArrayList<>();

	private static DevicePuzzleboxGimmickSingleton ourInstance = new DevicePuzzleboxGimmickSingleton();

	public static DevicePuzzleboxGimmickSingleton getInstance() {
		return ourInstance;
	}

	private DevicePuzzleboxGimmickSingleton() {
	}

	public String getClientCharacteristic() {
		return CLIENT_CHARACTERISTIC_CONFIG;
	}

	public String getPeripheralServiceUuid() {
		return PERIPHERAL_SERVICE_UUID;
	}

	public String getCommandCharacteristicUuid() {
		return COMMAND_CHARACTERISTIC_UUID;
	}

	public String getHashCharacteristicUuid() {
		return HASH_CHARACTERISTIC_UUID;
	}

//	public void clearDevicesFound(){
//		devicesFound.clear();
//	}
//
//	public void addDevicesFound(ScanResult result) {
//		devicesFound.add(result);
//	}
//
//	public ArrayList<String> getDevicesFoundNames() {
//
//		ArrayList<String> deviceNames = new ArrayList<>();
//
//		if (devicesFound.isEmpty()) {
//			deviceNames.add(Resources.getSystem().getString(R.string.scan_default_list));
//		} else {
//			for (ScanResult sr : devicesFound) {
//				if (sr.getDevice().getName() != null) {
////					deviceNames.add(sr.getDevice().getName());
//					deviceNames.add(sr.getDevice().getName() + " [" + sr.getDevice().getAddress() + "]");
//				}
//			}
//		}
//		return deviceNames;
//	}

}
