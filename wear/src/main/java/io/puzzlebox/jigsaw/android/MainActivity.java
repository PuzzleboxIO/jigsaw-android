package io.puzzlebox.jigsaw.android;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
//import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener, MessageApi.MessageListener,
        SensorEventListener {
//		NodeApi.NodeListener, SensorEventListener {

	//    private static final String TAG = MyActivity.class.getName();
	private static final String TAG = "io.puzzlebox.jigsawwear";

	private GoogleApiClient mGoogleApiClient;
	//        private ListView mDataItemList;
//        private DataItemAdapter mDataItemListAdapter;
	private Handler mHandler;

	private TextView value_x;
	private TextView value_y;
	private TextView value_z;

	private CountDownLatch latch;

	private SensorManager mSensorManager;
	private Sensor mOrientationSensor = null;

	private static final String ROLL_PATH = "/roll";
	private static final String ROLL_KEY = "roll";
	private static final String PITCH_PATH = "/pitch";
	private static final String PITCH_KEY = "pitch";

	private float tiltX = 0;
	private float tiltY = 0;
	private float referenceTiltX = 0;
	private float referenceTiltY = 0;
	private int accuracy;
	private int roll = 0;
	private int pitch = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler();
		setContentView(R.layout.activity_main);
		latch = new CountDownLatch(1);
		final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
		stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
			@Override
			public void onLayoutInflated(WatchViewStub stub) {
				value_x = (TextView) stub.findViewById(R.id.value_x);
//                value_x.setText("Roll: Reading...");
				value_x.setText("Roll: " + roll);
				value_y = (TextView) stub.findViewById(R.id.value_y);
//                value_y.setText("Pitch: Reading...");
				value_y.setText("Pitch: " + pitch);
				value_z = (TextView) stub.findViewById(R.id.value_z);
				value_z.setText("Z: Reading...");

				latch.countDown();
			}
		});

		mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));


		if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
			List<Sensor> gravSensors = mSensorManager.getSensorList(Sensor.TYPE_GRAVITY);
			for (int i=0; i < gravSensors.size(); i++) {
				if (gravSensors.get(i).getVersion() >= 3) {
					// Use the version 3 gravity sensor.
					mOrientationSensor = gravSensors.get(i);
					Log.v(TAG, "Tilt Control: Using Gravity Sensor (version 3+)");
					break;
				}
			}

			if (mOrientationSensor == null) {
				// Fall back to using an earlier version gravity sensor.
				for (int i=0; i < gravSensors.size(); i++) {
					mOrientationSensor = gravSensors.get(i);
					Log.v(TAG, "Tilt Control: Using Gravity Sensor");
					break;
				}
			}
		}

		else

		if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
			mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			Log.v(TAG, "Tilt Control: Using Accelerometer Sensor");
		}

		else {

			// Use the accelerometer.
			if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
				mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				Log.v(TAG, "Tilt Control: Using Accelerometer Sensor");
			}

		}

		// if we can't access the orientation sensor then exit:
		if (mOrientationSensor == null) {
			mSensorManager.unregisterListener(this);
		} else {
			mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_UI);
		}




		// Stores data events received by the local broadcaster.
//        mDataItemListAdapter = new DataItemAdapter(this, android.R.layout.simple_list_item_1);
//        mDataItemList.setAdapter(mDataItemListAdapter);

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

//        sendTest();

	}


	@Override
	protected void onStart() {
		super.onStart();

//        mSensorManager.registerListener(this, this.mOrientationSensor, 3);
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		try {
			latch.await();
			if ((sensorEvent.values[0] != tiltX) || (sensorEvent.values[1] != tiltY)) {

				tiltX = sensorEvent.values[0];
				tiltY = sensorEvent.values[1];
				accuracy = sensorEvent.accuracy;

				if ((int)tiltX != roll) {
					roll = (int) tiltX;

					value_x.setText("Roll: " + roll);

					sendTest();

				} else if ((int)tiltY != pitch) {
					pitch = (int) tiltY;

					value_y.setText("Pitch: " + pitch);

					sendTest();

				}

//                value_x.setText("Roll: " + String.valueOf(String.format("%.02f", sensorEvent.values[0])));
//                value_y.setText("Pitch: " + String.valueOf(String.format("%.02f", sensorEvent.values[1])));
////                value_z.setText("Z: " + String.valueOf(String.format("%.02f", sensorEvent.values[2])));

			}

		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage(), e);
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {
		Log.d(TAG, "accuracy changed: " + i);
	}

	@Override
	protected void onStop() {
		super.onStop();

		mSensorManager.unregisterListener(this);
	}



	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "onConnected(): Successfully connected to Google API client");
		Wearable.DataApi.addListener(mGoogleApiClient, this);
		Wearable.MessageApi.addListener(mGoogleApiClient, this);
//		Wearable.NodeApi.addListener(mGoogleApiClient, this);
//		Wearable.NodeClient.addListener(mGoogleApiClient, this);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.d(TAG, "onConnectionSuspended(): Connection to Google API client was suspended");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + result);
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEvents) {
		Log.d(TAG, "onDataChanged(): " + dataEvents);

		final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
		dataEvents.close();
		for (DataEvent event : events) {
			if (event.getType() == DataEvent.TYPE_CHANGED) {
				String path = event.getDataItem().getUri().getPath();
//                    if (DataLayerListenerService.IMAGE_PATH.equals(path)) {
//                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
//                        Asset photo = dataMapItem.getDataMap()
//                                .getAsset(DataLayerListenerService.IMAGE_KEY);
//                        final Bitmap bitmap = loadBitmapFromAsset(mGoogleApiClient, photo);
//                        mHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.d(TAG, "Setting background image..");
//                                mLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
//                            }
//                        });

//                    } else if (DataLayerListenerService.COUNT_PATH.equals(path)) {
//                        LOGD(TAG, "Data Changed for COUNT_PATH");
//                        generateEvent("DataItem Changed", event.getDataItem().toString());


//                    } else
				if (DataLayerListenerService.PITCH_PATH.equals(path)) {
					Log.d(TAG, "Data Changed for PITCH_PATH");
//                    generateEvent("DataItem Test", event.getDataItem().toString());
					DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

					float pitch = dataMapItem.getDataMap().getFloat(PITCH_KEY);

					Log.d(TAG, "Pitch: " + String.valueOf(String.format("%.02f", pitch)));
					generateEvent("Pitch: ", String.valueOf(String.format("%.02f", pitch)));

//                    sendTest();

				} else {
					Log.d(TAG, "Unrecognized path: " + path);
				}

			} else if (event.getType() == DataEvent.TYPE_DELETED) {
				generateEvent("DataItem Deleted", event.getDataItem().toString());
			} else {
				generateEvent("Unknown data event type", "Type = " + event.getType());
			}
		}
	}


	private void generateEvent(final String title, final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
//                    value_z.setText.setVisibility(View.INVISIBLE);
//                    mDataItemListAdapter.add(new Event(title, text));
				value_z.setText(text);
			}
		});
	}


	@Override
	public void onMessageReceived(MessageEvent event) {
		Log.d(TAG, "onMessageReceived: " + event);
		generateEvent("Message", "What up yoda");
//                generateEvent("What up yoda", event.toString());

	}

//	@Override // TOOD
	public void onPeerConnected(Node node) {
		generateEvent("Node Connected", node.getId());
	}

//	@Override // TODO
	public void onPeerDisconnected(Node node) {
		generateEvent("Node Disconnected", node.getId());
	}

	private static class DataItemAdapter extends ArrayAdapter<Event> {

		private final Context mContext;

		public DataItemAdapter(Context context, int unusedResource) {
			super(context, unusedResource);
			mContext = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(android.R.layout.two_line_list_item, null);
				convertView.setTag(holder);
				holder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
				holder.text2 = (TextView) convertView.findViewById(android.R.id.text2);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			Event event = getItem(position);
			holder.text1.setText(event.title);
			holder.text2.setText(event.text);
			return convertView;
		}

		private class ViewHolder {

			TextView text1;
			TextView text2;
		}
	}

	private class Event {

		String title;
		String text;

		public Event(String title, String text) {
			this.title = title;
			this.text = text;
		}
	}


	private void sendTest() {

		Log.d(TAG, "sendTest()");

		PutDataMapRequest dataMap = PutDataMapRequest.create(PITCH_PATH);
		dataMap.getDataMap().putDouble(ROLL_PATH, tiltX);
		dataMap.getDataMap().putDouble(PITCH_PATH, tiltY);

//        PutDataMapRequest dataMap = PutDataMapRequest.create(IMAGE_PATH);
//        dataMap.getDataMap().putAsset(IMAGE_KEY, asset);
		dataMap.getDataMap().putLong("time", new Date().getTime());

		PutDataRequest request;
		request = dataMap.asPutDataRequest();

		Wearable.DataApi.putDataItem(mGoogleApiClient, request)
				.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
					@Override
					public void onResult(DataApi.DataItemResult dataItemResult) {
						Log.d(TAG, "Sending test was successful: " + dataItemResult.getStatus()
								.isSuccess());
					}
				});

	}


}


//public class MainActivity extends Activity {
//
//	private TextView mTextView;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//		final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
//		stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
//			@Override
//			public void onLayoutInflated(WatchViewStub stub) {
//				mTextView = (TextView) stub.findViewById(R.id.text);
//			}
//		});
//	}
//}
