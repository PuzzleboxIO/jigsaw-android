package io.puzzlebox.jigsaw.data;

import android.bluetooth.le.ScanResult;

import java.util.ArrayList;

public class DevicePuzzleboxGimmickSingleton {

	public String x10ID = "C2";
	public int x10Level = 0;

	public boolean connected = false;

	public boolean lock = false;
	public boolean selectGimmickDialogVisible = false;

	private final static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
	private final static String PERIPHERAL_SERVICE_UUID =      "0000cc00-0000-0000-0000-000000101010";
	private final static String COMMAND_CHARACTERISTIC_UUID =  "0000cc0a-0000-1000-8000-00805f9b34fb";
	private final static String HASH_CHARACTERISTIC_UUID =     "0000cc0b-0000-1000-8000-00805f9b34fb";

	public String deviceHash = null;

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
}
