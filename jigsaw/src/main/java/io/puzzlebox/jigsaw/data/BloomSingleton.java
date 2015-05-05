package io.puzzlebox.jigsaw.data;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

import io.puzzlebox.jigsaw.protocol.RBLService;

/**
 * Created by sc on 5/3/15.
 */
public class BloomSingleton {

	public boolean demoActive = false;

	public BluetoothGattCharacteristic characteristicTx = null;
	public RBLService mBluetoothLeService;
	public BluetoothAdapter mBluetoothAdapter;
	public BluetoothDevice mDevice = null;
	public String mDeviceAddress;

	public boolean flag = true;
	public boolean connState = false;
	public boolean scanFlag = false;

	public byte[] data = new byte[3];
//	public static final int REQUEST_ENABLE_BT = 1;
	public final int REQUEST_ENABLE_BT = 1;
//	public static final long SCAN_PERIOD = 2000;
	public final long SCAN_PERIOD = 2000;

//	final public static char[] hexArray = { '0', '1', '2', '3', '4', '5', '6',
//			  '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	final public char[] hexArray = { '0', '1', '2', '3', '4', '5', '6',
			  '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };


//	private BluetoothGattCharacteristic characteristicTx = null;
//	private RBLService mBluetoothLeService;
//	private BluetoothAdapter mBluetoothAdapter;
//	private BluetoothDevice mDevice = null;
//	private String mDeviceAddress;
//
//	private boolean flag = true;
//	private boolean connState = false;
//	private boolean scanFlag = false;
//
//	private byte[] data = new byte[3];
//	private static final int REQUEST_ENABLE_BT = 1;
//	private static final long SCAN_PERIOD = 2000;
//
//	final private static char[] hexArray = { '0', '1', '2', '3', '4', '5', '6',
//			  '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };


	private static BloomSingleton ourInstance = new BloomSingleton();

	public static BloomSingleton getInstance() {
		return ourInstance;
	}

	private BloomSingleton() {
	}
}
