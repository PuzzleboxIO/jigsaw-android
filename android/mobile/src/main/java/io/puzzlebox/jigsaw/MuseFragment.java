package io.puzzlebox.jigsaw;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.interaxon.libmuse.Accelerometer;
import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.LibMuseVersion;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MuseFileWriter;
import com.interaxon.libmuse.MuseManager;
import com.interaxon.libmuse.MusePreset;
import com.interaxon.libmuse.MuseVersion;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MuseFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MuseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MuseFragment extends Fragment implements View.OnClickListener {

	private final static String TAG = MuseFragment.class.getSimpleName();

	Spinner musesSpinner;

	TextView statusText;
	TextView museVersionText;

	TextView tp9;
	TextView fp1;
	TextView fp2;
	TextView tp10;

	TextView acc_x;
	TextView acc_y;
	TextView acc_z;

	TextView elem1;
	TextView elem2;
	TextView elem3;
	TextView elem4;

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	private OnFragmentInteractionListener mListener;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment MuseFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static MuseFragment newInstance(String param1, String param2) {
		MuseFragment fragment = new MuseFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public MuseFragment() {
		// Required empty public constructor
		WeakReference<Activity> weakActivity =
				  new WeakReference<Activity>(getActivity());

		connectionListener = new ConnectionListener(weakActivity);
		dataListener = new DataListener(weakActivity);
	}

//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		if (getArguments() != null) {
//			mParam1 = getArguments().getString(ARG_PARAM1);
//			mParam2 = getArguments().getString(ARG_PARAM2);
//		}
//	}

//	private ViewGroup mContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
//		mContainer = container;

//		super.onCreateView(savedInstanceState);

		View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_muse, container, false);

		Button connectButton = (Button) v.findViewById(R.id.connect);
		connectButton.setOnClickListener(this);
		Button disconnectButton = (Button) v.findViewById(R.id.disconnect);
		disconnectButton.setOnClickListener(this);
		Button pauseButton = (Button) v.findViewById(R.id.pause);
		pauseButton.setOnClickListener(this);

		musesSpinner = (Spinner) v.findViewById(R.id.muses_spinner);

		Button refreshButton = (Button) v.findViewById(R.id.refresh);
//		refreshButton.setOnClickListener(this);
		refreshButton.setOnClickListener(

				  new View.OnClickListener() {
					  @Override
					  public void onClick(View v) {


						  {
							  MuseManager.refreshPairedMuses();
							  List<Muse> pairedMuses = MuseManager.getPairedMuses();
							  List<String> spinnerItems = new ArrayList<String>();
							  for (Muse m : pairedMuses) {
								  String dev_id = m.getName() + "-" + m.getMacAddress();
								  Log.i("Muse Headband", dev_id);
								  spinnerItems.add(dev_id);
							  }
							  ArrayAdapter<String> adapterArray = new ArrayAdapter<String>(
										 getActivity().getApplicationContext(), android.R.layout.simple_spinner_item, spinnerItems);
							  musesSpinner.setAdapter(adapterArray);

						  }
					  }
				  });

		statusText =
				  (TextView) v.findViewById(R.id.con_status);
//		statusText.setText(status);
		museVersionText =
				  (TextView) v.findViewById(R.id.version);

		tp9 = (TextView) v.findViewById(R.id.eeg_tp9);
		fp1 = (TextView) v.findViewById(R.id.eeg_fp1);
		fp2 = (TextView) v.findViewById(R.id.eeg_fp2);
		tp10 = (TextView) v.findViewById(R.id.eeg_tp10);

		acc_x = (TextView) v.findViewById(R.id.acc_x);
		acc_y = (TextView) v.findViewById(R.id.acc_y);
		acc_z = (TextView) v.findViewById(R.id.acc_z);

		elem1 = (TextView) v.findViewById(R.id.elem1);
		elem2 = (TextView) v.findViewById(R.id.elem2);
		elem3 = (TextView) v.findViewById(R.id.elem3);
		elem4 = (TextView) v.findViewById(R.id.elem4);

//		return inflater.inflate(R.layout.fragment_muse, container, false);

		return v;

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					  + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(Uri uri);
	}


	/**
	 * Connection listener updates UI with new connection status and logs it.
	 */
	class ConnectionListener extends MuseConnectionListener {

		final WeakReference<Activity> activityRef;

		ConnectionListener(final WeakReference<Activity> activityRef) {
			this.activityRef = activityRef;
		}

		@Override
		public void receiveMuseConnectionPacket(MuseConnectionPacket p) {

//			final View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_muse, mContainer, false);

			final ConnectionState current = p.getCurrentConnectionState();
			final String status = p.getPreviousConnectionState().toString() +
					  " -> " + current;
			final String full = "Muse " + p.getSource().getMacAddress() +
					  " " + status;
			Log.i("Muse Headband", full);
			Activity activity = activityRef.get();
			// UI thread is used here only because we need to update
			// TextView values. You don't have to use another thread, unless
			// you want to run disconnect() or connect() from connection packet
			// handler. In this case creating another thread is required.
			if (activity != null) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						statusText.setText(status);
						if (current == ConnectionState.CONNECTED) {
							MuseVersion museVersion = muse.getMuseVersion();
							String version = museVersion.getFirmwareType() +
									  " - " + museVersion.getFirmwareVersion() +
									  " - " + Integer.toString(
									  museVersion.getProtocolVersion());
//							museVersionText.setText(version);
						} else {
//							museVersionText.setText(R.string.undefined);
						}
					}
				});
			}
		}
	}

	/**
	 * Data listener will be registered to listen for: Accelerometer,
	 * Eeg and Relative Alpha bandpower packets. In all cases we will
	 * update UI with new values.
	 * We also will log message if Artifact packets contains "blink" flag.
	 * DataListener methods will be called from execution thread. If you are
	 * implementing "serious" processing algorithms inside those listeners,
	 * consider to create another thread.
	 */
	class DataListener extends MuseDataListener {

		final WeakReference<Activity> activityRef;
		private MuseFileWriter fileWriter;

		DataListener(final WeakReference<Activity> activityRef) {
			this.activityRef = activityRef;
		}

		@Override
		public void receiveMuseDataPacket(MuseDataPacket p) {
			switch (p.getPacketType()) {
				case EEG:
					updateEeg(p.getValues());
					break;
				case ACCELEROMETER:
					updateAccelerometer(p.getValues());
					break;
				case ALPHA_RELATIVE:
					updateAlphaRelative(p.getValues());
					break;
				case BATTERY:
					fileWriter.addDataPacket(1, p);
					// It's library client responsibility to flush the buffer,
					// otherwise you may get memory overflow.
					if (fileWriter.getBufferedMessagesSize() > 8096)
						fileWriter.flush();
					break;
				default:
					break;
			}
		}

		@Override
		public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
			if (p.getHeadbandOn() && p.getBlink()) {
				Log.i("Artifacts", "blink");
			}
		}

		private void updateAccelerometer(final ArrayList<Double> data) {

//			final View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_muse, mContainer, false);

//			Activity activity = activityRef.get();
//			if (activity != null) {
//				activity.runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						acc_x = (TextView) v.findViewById(R.id.acc_x);
//						acc_y = (TextView) v.findViewById(R.id.acc_y);
//						acc_z = (TextView) v.findViewById(R.id.acc_z);
//						acc_x.setText(String.format(
//								  "%6.2f", data.get(Accelerometer.FORWARD_BACKWARD.ordinal())));
//						acc_y.setText(String.format(
//								  "%6.2f", data.get(Accelerometer.UP_DOWN.ordinal())));
//						acc_z.setText(String.format(
//								  "%6.2f", data.get(Accelerometer.LEFT_RIGHT.ordinal())));
//					}
//				});
//			}
		}

		private void updateEeg(final ArrayList<Double> data) {

			Log.e(TAG, "fp1: " + String.format(
					  "%6.2f", data.get(Eeg.FP1.ordinal())));

//			tp9.setText(String.format(
//					  "%6.2f", data.get(Eeg.TP9.ordinal())));
//			fp1.setText(String.format(
//					  "%6.2f", data.get(Eeg.FP1.ordinal())));
//			fp2.setText(String.format(
//					  "%6.2f", data.get(Eeg.FP2.ordinal())));
//			tp10.setText(String.format(
//					  "%6.2f", data.get(Eeg.TP10.ordinal())));

//			final View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_muse, mContainer, false);

			Activity activity = activityRef.get();
			if (activity != null) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
//						TextView tp9 = (TextView) v.findViewById(R.id.eeg_tp9);
//						TextView fp1 = (TextView) v.findViewById(R.id.eeg_fp1);
//						TextView fp2 = (TextView) v.findViewById(R.id.eeg_fp2);
//						TextView tp10 = (TextView) v.findViewById(R.id.eeg_tp10);
						tp9.setText(String.format(
								  "%6.2f", data.get(Eeg.TP9.ordinal())));
						fp1.setText(String.format(
								  "%6.2f", data.get(Eeg.FP1.ordinal())));
						fp2.setText(String.format(
								  "%6.2f", data.get(Eeg.FP2.ordinal())));
						tp10.setText(String.format(
								  "%6.2f", data.get(Eeg.TP10.ordinal())));
					}
				});
			}
		}

		private void updateAlphaRelative(final ArrayList<Double> data) {
//			final View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_muse, mContainer, false);

//			Activity activity = activityRef.get();
//			if (activity != null) {
//				activity.runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
////						elem1 = (TextView) v.findViewById(R.id.elem1);
////						elem2 = (TextView) v.findViewById(R.id.elem2);
////						elem3 = (TextView) v.findViewById(R.id.elem3);
////						elem4 = (TextView) v.findViewById(R.id.elem4);
//						elem1.setText(String.format(
//								  "%6.2f", data.get(Eeg.TP9.ordinal())));
//						elem2.setText(String.format(
//								  "%6.2f", data.get(Eeg.FP1.ordinal())));
//						elem3.setText(String.format(
//								  "%6.2f", data.get(Eeg.FP2.ordinal())));
//						elem4.setText(String.format(
//								  "%6.2f", data.get(Eeg.TP10.ordinal())));
//					}
//				});
//			}
		}

		public void setFileWriter(MuseFileWriter fileWriter) {
			this.fileWriter  = fileWriter;
		}
	}

	private Muse muse = null;
	private ConnectionListener connectionListener = null;
	private DataListener dataListener = null;
	private boolean dataTransmission = true;
	private MuseFileWriter fileWriter = null;

//	public MainActivity() {
//		// Create listeners and pass reference to activity to them
//		WeakReference<Activity> weakActivity =
//				  new WeakReference<Activity>(this);
//
//		connectionListener = new ConnectionListener(weakActivity);
//		dataListener = new DataListener(weakActivity);
//	}

	@Override
//	protected void onCreate(Bundle savedInstanceState) {
	public void onCreate(Bundle savedInstanceState) {

//		final View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_muse, mContainer, false);


		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);



//		Button refreshButton = (Button) v.findViewById(R.id.refresh);
////		refreshButton.setOnClickListener(this);
//		refreshButton.setOnClickListener(
//
//				  new View.OnClickListener() {
//					  @Override
//					  public void onClick(View v) {
//						  Spinner musesSpinner = (Spinner) v.findViewById(R.id.muses_spinner);
//
//						  {
//							  MuseManager.refreshPairedMuses();
//							  List<Muse> pairedMuses = MuseManager.getPairedMuses();
//							  List<String> spinnerItems = new ArrayList<String>();
//							  for (Muse m : pairedMuses) {
//								  String dev_id = m.getName() + "-" + m.getMacAddress();
//								  Log.i("Muse Headband", dev_id);
//								  spinnerItems.add(dev_id);
//							  }
//							  ArrayAdapter<String> adapterArray = new ArrayAdapter<String>(
//									    getActivity().getApplicationContext(), android.R.layout.simple_spinner_item, spinnerItems);
//							  musesSpinner.setAdapter(adapterArray);
//
//						  }
//					  }
//				  });
//		Button connectButton = (Button) v.findViewById(R.id.connect);
//		connectButton.setOnClickListener(this);
//		Button disconnectButton = (Button) v.findViewById(R.id.disconnect);
//		disconnectButton.setOnClickListener(this);
//		Button pauseButton = (Button) v.findViewById(R.id.pause);
//		pauseButton.setOnClickListener(this);
		fileWriter = MuseManager.getMuseFileWriter(new File(
				  android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + (Environment.DIRECTORY_DOCUMENTS) + "/",
				  "testlibmusefile.muse"));
//				  getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
//				  "testlibmusefile.muse"));

//		filepath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();


		Log.i("Muse Headband", "libmuse version=" + LibMuseVersion.SDK_VERSION);
		fileWriter.addAnnotationString(1, "MainActivity onCreate");
		dataListener.setFileWriter(fileWriter);
	}

	@Override
	public void onClick(View v) {

		Log.e(TAG, "onClick");

//		Spinner musesSpinner = (Spinner) v.findViewById(R.id.muses_spinner);
		if (v.getId() == R.id.refresh) {
			MuseManager.refreshPairedMuses();
			List<Muse> pairedMuses = MuseManager.getPairedMuses();
			List<String> spinnerItems = new ArrayList<String>();
			for (Muse m: pairedMuses) {
				String dev_id = m.getName() + "-" + m.getMacAddress();
				Log.i("Muse Headband", dev_id);
				spinnerItems.add(dev_id);
			}
			ArrayAdapter<String> adapterArray = new ArrayAdapter<String> (
					  getActivity().getApplicationContext(), android.R.layout.simple_spinner_item, spinnerItems);
			musesSpinner.setAdapter(adapterArray);
		}
		else if (v.getId() == R.id.connect) {
			List<Muse> pairedMuses = MuseManager.getPairedMuses();
			if (pairedMuses.size() < 1 ||
					  musesSpinner.getAdapter().getCount() < 1) {
				Log.w("Muse Headband", "There is nothing to connect to");
			}
			else {
				muse = pairedMuses.get(musesSpinner.getSelectedItemPosition());
				ConnectionState state = muse.getConnectionState();
				if (state == ConnectionState.CONNECTED ||
						  state == ConnectionState.CONNECTING) {
					Log.w("Muse Headband", "doesn't make sense to connect second time to the same muse");
					return;
				}
				configure_library();
				fileWriter.open();
				fileWriter.addAnnotationString(1, "Connect clicked");
				/**
				 * In most cases libmuse native library takes care about
				 * exceptions and recovery mechanism, but native code still
				 * may throw in some unexpected situations (like bad bluetooth
				 * connection). Print all exceptions here.
				 */
				try {
					muse.runAsynchronously();
				} catch (Exception e) {
					Log.e("Muse Headband", e.toString());
				}
			}
		}
		else if (v.getId() == R.id.disconnect) {
			if (muse != null) {
				/**
				 * true flag will force libmuse to unregister all listeners,
				 * BUT AFTER disconnecting and sending disconnection event.
				 * If you don't want to receive disconnection event (for ex.
				 * you call disconnect when application is closed), then
				 * unregister listeners first and then call disconnect:
				 * muse.unregisterAllListeners();
				 * muse.disconnect(false);
				 */
				muse.disconnect(true);
				fileWriter.addAnnotationString(1, "Disconnect clicked");
				fileWriter.flush();
				fileWriter.close();
			}
		}
		else if (v.getId() == R.id.pause) {
			dataTransmission = !dataTransmission;
			if (muse != null) {
				muse.enableDataTransmission(dataTransmission);
			}
		}
	}

	private void configure_library() {
		muse.registerConnectionListener(connectionListener);
		muse.registerDataListener(dataListener,
				  MuseDataPacketType.ACCELEROMETER);
		muse.registerDataListener(dataListener,
				  MuseDataPacketType.EEG);
		muse.registerDataListener(dataListener,
				  MuseDataPacketType.ALPHA_RELATIVE);
		muse.registerDataListener(dataListener,
				  MuseDataPacketType.ARTIFACTS);
		muse.registerDataListener(dataListener,
				  MuseDataPacketType.BATTERY);
		muse.setPreset(MusePreset.PRESET_14);
		muse.enableDataTransmission(dataTransmission);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getActivity().getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}




}
