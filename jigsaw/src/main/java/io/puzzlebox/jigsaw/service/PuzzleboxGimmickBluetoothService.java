package io.puzzlebox.jigsaw.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import androidx.annotation.NonNull;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.List;

import io.puzzlebox.jigsaw.data.DevicePuzzleboxGimmickSingleton;
import androidx.core.content.ContextCompat;

@SuppressLint("MissingPermission")
public class PuzzleboxGimmickBluetoothService extends Service {

	private final static String TAG = PuzzleboxGimmickBluetoothService.class.getSimpleName();

	private static BluetoothAdapter bluetoothAdapter;
	private Handler handler;
	private BluetoothGatt gatt;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	private ServiceHandler mServiceHandler;

	public PuzzleboxGimmickBluetoothService() {
	}

	private static final PuzzleboxGimmickBluetoothService ourInstance = new PuzzleboxGimmickBluetoothService();

	public static PuzzleboxGimmickBluetoothService getInstance() {
		return ourInstance;
	}

	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		@Override
		public void handleMessage(@NonNull Message msg) {
			long endTime = System.currentTimeMillis() + 5*1000;
			while (System.currentTimeMillis() < endTime) {
				synchronized (this) {
					try {
						wait(endTime - System.currentTimeMillis());
					} catch (Exception e) {
						Log.e(TAG, "Exception", e);
					}
				}
			}
			stopSelf(msg.arg1);
		}
	}

	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		Looper mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

		ContextCompat.registerReceiver(getApplicationContext(), mCommandReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.bluetooth.command"), ContextCompat.RECEIVER_NOT_EXPORTED);

		final BluetoothManager manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);

		handler = new Handler(Looper.getMainLooper());
		bluetoothAdapter = manager.getAdapter();

		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			// TODO Can't call this from outside of Activity
			Log.e(TAG, "Bluetooth service not enabled. User needs to turn on Bluetooth.");
		}

		createService();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void createService() {
		final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter = manager.getAdapter();

		if (bluetoothAdapter != null) {
			scanLeDevice(true);
		}
	}

	private final BroadcastReceiver mCommandReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String commandName = String.valueOf(intent.getStringExtra("name"));
			String commandValue = String.valueOf(intent.getStringExtra("value"));

			Log.d(TAG, "commandName: " + commandName);

			switch (commandName) {
				case "scan":
					switch (commandValue) {
						case "true":
							scanLeDevice(true);
							break;
						case "false":
							scanLeDevice(false);
							break;
					}
					break;
				case "connect":
					connectDecoder(context, commandValue);
					break;
				case "disconnect":
					disconnectDecoder();
					break;
				case "reboot":
					rebootDecoder();
					break;
				case "command":
					commandDecoder(commandValue);
					break;
				case "x10":
				case "joystick":
					commandGimmick(commandValue);
					break;
			}
		}

	};

	public void connectDecoder(Context context, String deviceSelection) {

		Log.d(TAG, "connectDecoder(): " + deviceSelection);

		deviceSelection = deviceSelection.substring(deviceSelection.indexOf("[") + 1);
		deviceSelection = deviceSelection.substring(0, deviceSelection.indexOf("]"));

		for (ScanResult sr : DevicePuzzleboxGimmickSingleton.getInstance().devicesFound) {
			if (sr.getDevice().getAddress().equals(deviceSelection)) {
				BluetoothDevice device = sr.getDevice();

				Log.d(TAG, "device.connectGatt");

				gatt = device.connectGatt(context, false, gattCallback);
				break;
			}
		}
	}

	public void disconnectDecoder() {

		Log.v(TAG, "disconnectDecoder()");

		try {
			gatt.disconnect();
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
		}
	}

	public void rebootDecoder() {
		if (gatt != null) {
			for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
				String uuid = bluetoothGattService.getUuid().toString();
				Log.d("SERVICE", "found service - " + uuid);
				if (uuid.equals(DevicePuzzleboxGimmickSingleton.getInstance().getPeripheralServiceUuid())) {
					Log.d(TAG, "found Peripheral service - " + uuid);

					for (BluetoothGattCharacteristic characteristic : bluetoothGattService.getCharacteristics()) {
						for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {

							if (descriptor.getUuid().toString().equals(DevicePuzzleboxGimmickSingleton.getInstance().getCommandCharacteristicUuid())) {
								Log.d(TAG, "Command descriptor found");
								try {
									Log.d(TAG, "Command descriptor.getUuid(): " + descriptor.getUuid());
									Log.d(TAG, "Command descriptor.getPermissions(): " + descriptor.getPermissions());
									writeCharacteristicCompat(gatt, characteristic, "Command[#]: reboot".getBytes(StandardCharsets.UTF_8));
								} catch (Exception e) {
									Log.e(TAG, "Command descriptor: " + e);
								}
							}
						}
					}
				}
			}
		}
	}

	public void commandDecoder(String command) {
		if (gatt != null) {
			for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
				String uuid = bluetoothGattService.getUuid().toString();
				Log.d("SERVICE", "found service - " + uuid);
				if (uuid.equals(DevicePuzzleboxGimmickSingleton.getInstance().getPeripheralServiceUuid())) {
					Log.d(TAG, "found Peripheral service - " + uuid);

					for (BluetoothGattCharacteristic characteristic : bluetoothGattService.getCharacteristics()) {
						for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {

							if (descriptor.getUuid().toString().equals(DevicePuzzleboxGimmickSingleton.getInstance().getCommandCharacteristicUuid())) {
								Log.d(TAG, "Command descriptor found");
								try {
									Log.d(TAG, "Command descriptor.getUuid(): " + descriptor.getUuid());
									Log.d(TAG, "Command descriptor.getPermissions(): " + descriptor.getPermissions());
									writeCharacteristicCompat(gatt, characteristic, ("Command[$]: " + command).getBytes(StandardCharsets.UTF_8));
								} catch (Exception e) {
									Log.e(TAG, "Command descriptor: " + e);
								}
							}
						}
					}
				}
			}
		}
	}

	public void commandGimmick(String command) {
		if (gatt != null) {
			for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
				String uuid = bluetoothGattService.getUuid().toString();
				Log.d("SERVICE", "found service - " + uuid);
				if (uuid.equals(DevicePuzzleboxGimmickSingleton.getInstance().getPeripheralServiceUuid())) {
					Log.d(TAG, "found Peripheral service - " + uuid);

					for (BluetoothGattCharacteristic characteristic : bluetoothGattService.getCharacteristics()) {
						for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {

							if (descriptor.getUuid().toString().equals(DevicePuzzleboxGimmickSingleton.getInstance().getCommandCharacteristicUuid())) {
								Log.d(TAG, "Command descriptor found");
								try {
									Log.d(TAG, "Command descriptor.getUuid(): " + descriptor.getUuid());
									Log.d(TAG, "Command descriptor.getPermissions(): " + descriptor.getPermissions());
									writeCharacteristicCompat(gatt, characteristic, ("x10[" + command + "]").getBytes(StandardCharsets.UTF_8));
								} catch (Exception e) {
									Log.e(TAG, "Command descriptor: " + e);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Write a characteristic value compatible with both API 33+ and older versions.
	 * BluetoothGattCharacteristic.setValue() and the single-arg gatt.writeCharacteristic()
	 * are deprecated in API 33. The new three-argument form is used on API 33+.
	 */
	private void writeCharacteristicCompat(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			gatt.writeCharacteristic(characteristic, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
		} else {
			characteristic.setValue(value);
			gatt.writeCharacteristic(characteristic);
		}
	}

	private void broadcastEventBluetooth() {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.bluetooth.event");

		intent.putExtra("name", "command");
		intent.putExtra("value", "displayDevicesFound");

		this.sendBroadcast(intent);
	}

	private void scanLeDevice(final boolean enable) {

		final BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();

		DevicePuzzleboxGimmickSingleton.getInstance().devicesFound.clear();

		if (enable) {
			handler.postDelayed(() -> {
				scanner.stopScan(mScanCallback);
				broadcastEventBluetooth();
			}, 10000);

			scanner.startScan(mScanCallback);
		}
	}

	private final ScanCallback mScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			Log.d(TAG, "Device found: " + result.getDevice().getName() + ": " + result);

			boolean found = false;

			for (ScanResult sr : DevicePuzzleboxGimmickSingleton.getInstance().devicesFound) {
				if (result.getDevice().getAddress().equals( sr.getDevice().getAddress() )) {
					found = true;
				}
			}
			if (!found){
				DevicePuzzleboxGimmickSingleton.getInstance().devicesFound.add(result);
			}

			// Update Fragments
			broadcastEventBluetooth();
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			for (ScanResult sr : results) {
				Log.d("ScanResult - Results", sr.toString());
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
			Log.d("Scan Failed", "Error Code: " + errorCode);
		}
	};

	private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

			Log.d(TAG, "onConnectionStateChange");

			switch(newState) {
				case STATE_DISCONNECTED:
					DevicePuzzleboxGimmickSingleton.getInstance().connected = false;
					broadcastStatusBluetooth("disconnected");
					break;
				case STATE_CONNECTING:
					broadcastStatusBluetooth("connecting");
					break;
				case STATE_CONNECTED:
					gatt.discoverServices();
					DevicePuzzleboxGimmickSingleton.getInstance().connected = true;
					broadcastStatusBluetooth("connected");
					break;
			}
		}

		// API 33+ characteristic read callback (new signature — called on API 33+)
		@Override
		public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				try {
					Log.d(TAG, "characteristic.getUuid(): " + characteristic.getUuid());
					Log.d(TAG, "characteristic value: " + new String(value, StandardCharsets.UTF_8));
					Log.d(TAG, "characteristic.getDescriptors(): " + characteristic.getDescriptors());
				} catch (Exception e) {
					Log.e(TAG, "characteristic: " + e);
				}
			}
		}

		// Legacy callback for API < 33 — delegates to new signature
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			onCharacteristicRead(gatt, characteristic,
					characteristic.getValue() != null ? characteristic.getValue() : new byte[0], status);
		}

		// API 33+ descriptor read callback
		@Override
		public void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value) {
			Log.d(TAG, "onDescriptorRead()");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				try {
					Log.d(TAG, "descriptor.getUuid(): " + descriptor.getUuid());

					if ((descriptor.getUuid() != null) &&
							(value.length > 0) &&
							(descriptor.getUuid().toString().equals(DevicePuzzleboxGimmickSingleton.getInstance().getHashCharacteristicUuid()))) {
						String text = new String(value, StandardCharsets.UTF_8);
						Log.d(TAG, "descriptor value UTF-8: " + text);
						broadcastCommandBluetooth(DevicePuzzleboxGimmickSingleton.getInstance().x10ID + " Off");
						DevicePuzzleboxGimmickSingleton.getInstance().x10Level = 0;
					}
					Log.d(TAG, "descriptor.getPermissions(): " + descriptor.getPermissions());
				} catch (Exception e) {
					Log.e(TAG, "descriptor: " + e);
				}
			}
		}

		// Legacy descriptor read callback for API < 33
		@Override
		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			onDescriptorRead(gatt, descriptor, status,
					descriptor.getValue() != null ? descriptor.getValue() : new byte[0]);
		}

		public void onServicesDiscovered(BluetoothGatt gatt, int status) {

			List<BluetoothGattService> services = gatt.getServices();
			if (services == null) {
				Log.d(TAG, "onServicesDiscovered(): null");
				return;
			}
			for (BluetoothGattService bluetoothGattService : services) {
				String uuid = bluetoothGattService.getUuid().toString();
				Log.d("SERVICE", "found service - " + uuid);
				if (uuid.equals(DevicePuzzleboxGimmickSingleton.getInstance().getPeripheralServiceUuid())) {
					Log.d(TAG, "found Peripheral service - " + uuid);

					Log.d(TAG, "bluetoothGattService.getCharacteristics(): " + bluetoothGattService.getCharacteristics());

					for (BluetoothGattCharacteristic characteristic : bluetoothGattService.getCharacteristics()) {
						for (BluetoothGattDescriptor descriptor: characteristic.getDescriptors()) {

							if (descriptor.getUuid().toString().equals(DevicePuzzleboxGimmickSingleton.getInstance().getHashCharacteristicUuid())) {
								Log.d(TAG, "Hash characteristic found");
								try {
									Log.d(TAG, "Hash descriptor.getUuid(): " + descriptor.getUuid());
									Log.d(TAG, "Hash descriptor.getPermissions(): " + descriptor.getPermissions());
									gatt.readDescriptor(descriptor);
								} catch (Exception e) {
									Log.e(TAG, "Hash descriptor: " + e);
								}
							}
						}
					}
				}
			}
		}

		// API 33+ characteristic changed callback (new signature — called on API 33+)
		@Override
		public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
			final String val = new String(value, StandardCharsets.UTF_8);
			Log.d("VALUE", "notify new value - " + val);
		}

		// Legacy characteristic changed callback for API < 33
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			onCharacteristicChanged(gatt, characteristic,
					characteristic.getValue() != null ? characteristic.getValue() : new byte[0]);
		}
	};

	private void broadcastCommandBluetooth(String value) {
		Log.d(TAG, "broadcastCommandBluetooth: x10: " + value);

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.bluetooth.command");
		intent.setPackage(getPackageName());

		intent.putExtra("name", "x10");
		intent.putExtra("value", value);

		this.sendBroadcast(intent);
	}

	private void broadcastStatusBluetooth(String value) {
		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.puzzlebox.gimmick.status");
		intent.setPackage(getPackageName());

		intent.putExtra("name", "status");
		intent.putExtra("value", value);

		this.sendBroadcast(intent);
	}
}
