package io.puzzlebox.jigsaw.service;

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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.List;

//import io.puzzlebox.gimmick.R;
import io.puzzlebox.jigsaw.data.DevicePuzzleboxGimmickSingleton;

public class PuzzleboxGimmickBluetoothService extends Service {

	private final static String TAG = PuzzleboxGimmickBluetoothService.class.getSimpleName();

	private static BluetoothAdapter bluetoothAdapter;
	private boolean scanning = false;
	private Handler handler;
	private BluetoothGatt gatt;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	private static String TEST_MESSAGE = "1234567890abcdefghij1234567890abcdefghij";

	private ServiceHandler mServiceHandler;

	public PuzzleboxGimmickBluetoothService() {
	}

	private static PuzzleboxGimmickBluetoothService ourInstance = new PuzzleboxGimmickBluetoothService();

	public static PuzzleboxGimmickBluetoothService getInstance() {
		return ourInstance;
	}

	// ################################################################

	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
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


	public void onPause() {
//		super.onPause();

		if (scanning) {
			scanLeDevice(false);
		}

		if (gatt != null) {
			gatt.close();
			gatt = null;
		}
	}


	// ################################################################

	@Override
	public void onCreate() {

		Log.d(TAG, "onCreate()");

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


		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
				  mCommandReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.bluetooth.command"));



		final BluetoothManager manager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);

		handler = new Handler();
		bluetoothAdapter = manager.getAdapter();

		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			// Request to enable adapter
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			// TODO Can't call this from outside of Activity
			Log.e(TAG, "Bluetooth service not enabled. User needs to turn on Bluetooth.");
//			startActivity(enableBtIntent);
		}

		createService();

	}


	// ################################################################

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d(TAG, "onStartCommand()");

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);


		// If we get killed, after returning from here, restart

//		http://developer.android.com/reference/android/app/Service.html#START_STICKY

//		START_STICKY
//		If the system kills the service after onStartCommand() returns,
// recreate the service and call onStartCommand(), but do not
// redeliver the last intent. Instead, the system calls onStartCommand()
// with a null intent, unless there were pending intents to start
// the service, in which case, those intents are delivered. This is
// suitable for media players (or similar services) that are not
// executing commands, but running indefinitely and waiting for a job.

//		START_REDELIVER_INTENT
//		If the system kills the service after onStartCommand() returns,
// recreate the service and call onStartCommand() with the last intent
// that was delivered to the service. Any pending intents are delivered
// in turn. This is suitable for services that are actively performing
// a job that should be immediately resumed, such as downloading a file.

		return START_STICKY;

	}


	// ################################################################

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		Log.d(TAG, "onBind()");
		return null;
	}


	// ################################################################

//	@Override
//	public boolean onUnbind(Intent intent) {
//		// All clients have unbound with unbindService()
//		return mAllowRebind;
//	}


	// ################################################################

//	@Override
//	public void onRebind(Intent intent) {
//		// A client is binding to the service with bindService(),
//		// after onUnbind() has already been called
//	}


	// ################################################################

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "onDestroy()");

		// TODO This cleanup should occur on service termination but is happening early for unknown reason
		// TODO See: https://stackoverflow.com/questions/7570885/service-ondestroy-is-called-directly-after-creation-anyway-the-service-does-i

//		LocalBroadcastManager.getInstance(
//				  getApplicationContext()).unregisterReceiver(
//				  mCommandReceiver);
//
//		if (scanning) {
//			scanLeDevice(false);
//		}
//
//		if (gatt != null) {
//			gatt.close();
//			gatt = null;
//		}

	}


	// ################################################################

//	private Handler handlerDecoder = new Handler(new Handler.Callback() {
//		@Override
//		public boolean handleMessage(Message msg) {
//
//			Log.e(TAG, "handleMessage: " + msg);
////			parseMessage(msg);
//			return true;
//		}
//	});


	// ################################################################

	public void createService() {

//		Log.d(TAG, "createService()");

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (bluetoothAdapter != null) {

			scanLeDevice(true);

		}
	}

	// ################################################################

	private BroadcastReceiver mCommandReceiver = new BroadcastReceiver() {

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
							scanLeDevice(true);
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
					commandGimmick(commandValue);
					break;

				case "joystick":
					commandGimmick(commandValue);
					break;


			}
		}

	};

	// ################################################################

	public void connectDecoder(Context context, String deviceSelection) {

		Log.v(TAG, "connectDecoder(): " + deviceSelection);

		deviceSelection = deviceSelection.substring(deviceSelection.indexOf("[") + 1);
		deviceSelection = deviceSelection.substring(0, deviceSelection.indexOf("]"));

		for (ScanResult sr : DevicePuzzleboxGimmickSingleton.getInstance().devicesFound) {
			if (sr.getDevice().getAddress().equals(deviceSelection)) {
				BluetoothDevice device = sr.getDevice();

				Log.e(TAG, "device.connectGatt");

				gatt = device.connectGatt(context, false, gattCallback);

//				DevicePuzzleboxGimmickSingleton.getInstance().

				break;
			}
		}

	} // connectDecoder


	// ################################################################

	public void disconnectDecoder() {

		Log.v(TAG, "disconnectDecoder()");

		try {
			gatt.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

	} // disconnectDecoder


	// ################################################################

	public void rebootDecoder() {

		Log.v(TAG, "rebootDecoder()");

		if (gatt != null) {
			for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
				String uuid = bluetoothGattService.getUuid().toString();
				Log.i("SERVICE", "found service - " + uuid);
				if (uuid.equals(DevicePuzzleboxGimmickSingleton.getInstance().getPeripheralServiceUuid())) {
					Log.i(TAG, "found Peripheral service - " + uuid);

					for (BluetoothGattCharacteristic characteristic : bluetoothGattService.getCharacteristics()) {
						for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {

							if (descriptor.getUuid().toString().equals(DevicePuzzleboxGimmickSingleton.getInstance().getCommandCharacteristicUuid())) {
								Log.i(TAG, "Command descriptor found");
								try {
									Log.i(TAG, "Command descriptor.getUuid(): " + descriptor.getUuid());
									Log.i(TAG, "Command descriptor.getPermissions(): " + descriptor.getPermissions());

//									gatt.setCharacteristicNotification(characteristic, true);
//									characteristic.setValue("Command: # reboot");
									characteristic.setValue("Command[#]: reboot");
									Log.i(TAG, "writing characteristic");
									gatt.writeCharacteristic(characteristic);


								} catch (Exception e) {
									Log.e(TAG, "Command descriptor: " + e);
								}

							}
						}
					}
				}
			}
		}

	} // rebootDecoder

	// ################################################################

	public void commandDecoder(String command) {

		Log.v(TAG, "commandDecoder()");

		if (gatt != null) {
			for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
				String uuid = bluetoothGattService.getUuid().toString();
				Log.i("SERVICE", "found service - " + uuid);
				if (uuid.equals(DevicePuzzleboxGimmickSingleton.getInstance().getPeripheralServiceUuid())) {
					Log.i(TAG, "found Peripheral service - " + uuid);

					for (BluetoothGattCharacteristic characteristic : bluetoothGattService.getCharacteristics()) {
						for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {

							if (descriptor.getUuid().toString().equals(DevicePuzzleboxGimmickSingleton.getInstance().getCommandCharacteristicUuid())) {
								Log.i(TAG, "Command descriptor found");
								try {
									Log.i(TAG, "Command descriptor.getUuid(): " + descriptor.getUuid());
									Log.i(TAG, "Command descriptor.getPermissions(): " + descriptor.getPermissions());

//									gatt.setCharacteristicNotification(characteristic, true);
//									characteristic.setValue("Command: # reboot");
									characteristic.setValue("Command[$]: " + command);
									Log.i(TAG, "writing characteristic");
									gatt.writeCharacteristic(characteristic);


								} catch (Exception e) {
									Log.e(TAG, "Command descriptor: " + e);
								}

							}
						}
					}
				}
			}
		}

	} // commandDecoder

	// ################################################################

	public void commandGimmick(String command) {

		Log.v(TAG, "commandGimmick()");

		if (gatt != null) {
			for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
				String uuid = bluetoothGattService.getUuid().toString();
				Log.i("SERVICE", "found service - " + uuid);
				if (uuid.equals(DevicePuzzleboxGimmickSingleton.getInstance().getPeripheralServiceUuid())) {
					Log.i(TAG, "found Peripheral service - " + uuid);

					for (BluetoothGattCharacteristic characteristic : bluetoothGattService.getCharacteristics()) {
						for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {

							if (descriptor.getUuid().toString().equals(DevicePuzzleboxGimmickSingleton.getInstance().getCommandCharacteristicUuid())) {
								Log.i(TAG, "Command descriptor found");
								try {
									Log.i(TAG, "Command descriptor.getUuid(): " + descriptor.getUuid());
									Log.i(TAG, "Command descriptor.getPermissions(): " + descriptor.getPermissions());

//									gatt.setCharacteristicNotification(characteristic, true);
//									characteristic.setValue("Command: # reboot");
//									characteristic.setValue("Command[$]: " + command);
//									characteristic.setValue("pg[" + command + "]");
//									characteristic.setValue("eSense[" + command + "]");
									characteristic.setValue("x10[" + command + "]");
									Log.i(TAG, "writing characteristic");
									gatt.writeCharacteristic(characteristic);


								} catch (Exception e) {
									Log.e(TAG, "Command descriptor: " + e);
								}

							}
						}
					}
				}
			}
		}

	} // commandDecoder


	// ################################################################

	private void broadcastEventBluetooth(String name, String value) {

		Intent intent = new Intent("io.puzlebox.jigsaw.protocol.bluetooth.event");

		intent.putExtra("name", name);
		intent.putExtra("value", value);

		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

	}

	private void scanLeDevice(final boolean enable) {

//		Log.d(TAG, "scanLeDevice: " + enable);

		final BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();

		DevicePuzzleboxGimmickSingleton.getInstance().devicesFound.clear();

		if (enable) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					scanning = false;
					scanner.stopScan(mScanCallback);
					broadcastEventBluetooth("command", "displayDevicesFound");
				}
			}, 10000);

			scanning = true;
			scanner.startScan(mScanCallback);
		}
	}

	private ScanCallback mScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			Log.i(TAG, "Device found: " + result.getDevice().getName() + ": " + result.toString());

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
			broadcastEventBluetooth("command", "displayDevicesFound");

		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			for (ScanResult sr : results) {
				Log.i("ScanResult - Results", sr.toString());
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
			Log.i("Scan Failed", "Error Code: " + errorCode);
		}
	};

	private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

			Log.i(TAG, "onConnectionStateChange");

			switch(newState) {
				case STATE_DISCONNECTED:
					break;
				case STATE_CONNECTING:
					break;
				case STATE_CONNECTED:
					gatt.discoverServices();
					break;
			}

		}

		//		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

			Log.i(TAG, "onCharacteristicRead()");

			if (status == BluetoothGatt.GATT_SUCCESS) {

				try {
					Log.i(TAG, "characteristic.getUuid(): " + characteristic.getUuid());
//					Log.i(TAG, "characteristic.getValue(): " + characteristic.getValue());
					Log.i(TAG, "characteristic.getStringValue(0): " + characteristic.getStringValue(0));
					Log.i(TAG, "characteristic.getDescriptors(): " + characteristic.getDescriptors());
				} catch (Exception e) {
					Log.e(TAG, "characteristic: " + e);
				}
			}
		}

		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			Log.i(TAG, "onDescriptorRead()");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				try {
					Log.i(TAG, "descriptor.getUuid(): " + descriptor.getUuid());

					// Handle Hash descriptor
					if ((descriptor.getUuid() != null) &&
							  (descriptor.getValue() != null) &&
							  (descriptor.getUuid().toString().equals(DevicePuzzleboxGimmickSingleton.getInstance().getHashCharacteristicUuid()))) {
						String text = new String(descriptor.getValue(), "UTF-8");
						Log.i(TAG, "descriptor.getValue() \"UTF-8\": " + text);
						DevicePuzzleboxGimmickSingleton.getInstance().deviceHash = text;

						// TODO 2017-02-16
						// Update Fragments

						// TODO 2018-03-10
//						Log.e(TAG, "broadcastEventBluetooth(\"loadFragment\", getResources().getString(R.string.menu_joystick)): " + getResources().getString(R.string.menu_joystick));
////						broadcastEventBluetooth("loadFragment", getResources().getString(R.string.menu_login));
//						broadcastEventBluetooth("loadFragment", getResources().getString(R.string.menu_joystick));

						// TODO 2018-03-14
//						broadcastCommandBluetooth("joystick", "button5: 1");
						broadcastCommandBluetooth("x10", DevicePuzzleboxGimmickSingleton.getInstance().x10ID + " Off");
						DevicePuzzleboxGimmickSingleton.getInstance().x10Level = 0;

					}

					Log.i(TAG, "descriptor.getPermissions(): " + descriptor.getPermissions());
				} catch (Exception e) {
					Log.e(TAG, "descriptor: " + e);
				}

			}
		}

		public void onServicesDiscovered(BluetoothGatt gatt, int status) {

			Log.i(TAG, "onServicesDiscovered()");

			List<BluetoothGattService> services = gatt.getServices();
			if (services == null) {
				Log.i(TAG, "onServicesDiscovered(): null");
				return;
			}
			for (BluetoothGattService bluetoothGattService : services) {
				String uuid = bluetoothGattService.getUuid().toString();
				Log.i("SERVICE", "found service - " + uuid);
				if (uuid.equals(DevicePuzzleboxGimmickSingleton.getInstance().getPeripheralServiceUuid())) {
					Log.i(TAG, "found Peripheral service - " + uuid);

					Log.i(TAG, "bluetoothGattService.getCharacteristics(): " + bluetoothGattService.getCharacteristics());

					for (BluetoothGattCharacteristic characteristic : bluetoothGattService.getCharacteristics()) {
						for (BluetoothGattDescriptor descriptor: characteristic.getDescriptors()) {

							if (descriptor.getUuid().toString().equals(DevicePuzzleboxGimmickSingleton.getInstance().getHashCharacteristicUuid())) {
								Log.i(TAG, "Hash characteristic found");
								try {
									Log.i(TAG, "Hash descriptor.getUuid(): " + descriptor.getUuid());
									Log.i(TAG, "Hash descriptor.getValue(): " + descriptor.getValue());
									Log.i(TAG, "Hash descriptor.getPermissions(): " + descriptor.getPermissions());

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


		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

			Log.i(TAG, "onCharacteristicChanged");

			byte data[] = characteristic.getValue();
			try {
				final String val = new String(data,"UTF-8");
				Log.i("VALUE", "notify new value - " + val);
//				runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						((TextView)findViewById(R.id.textViewOutput)).setText("onCharacteristicChanged: " + val);
//					}
//				});
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	};


	// ################################################################

	private  void broadcastCommandBluetooth(String name, String value) {

		Log.d(TAG, "broadcastCommandBluetooth: " + name + ": " + value);

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.bluetooth.command");

		intent.putExtra("name", name);
		intent.putExtra("value", value);

//		LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

	}


}
