package io.puzzlebox.jigsaw.ui;

import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.util.ArrayList;

import io.puzzlebox.jigsaw.R;
import io.puzzlebox.jigsaw.data.DevicePuzzleboxGimmickSingleton;

public class DialogOutputPuzzleboxGimmickFragment extends DialogFragment {
//public class DialogOutputPuzzleboxGimmickFragment extends ListFragment { // TODO
//public class DialogOutputPuzzleboxGimmickFragment extends DialogFragment implements OnListDialogItemSelect {

    private static final String TAG = DialogOutputPuzzleboxGimmickFragment.class.getSimpleName();

    public final static String profileID = "puzzlebox_gimmick";

    Button buttonConnectGimmick;
    Button buttonDeviceEnable;

    private static Intent intentPuzzleboxGimmick;

    private OnFragmentInteractionListener mListener;

//    TextView textViewScanTitle;

    private OnFragmentLoadListener mListenerFragment;

    public interface OnFragmentLoadListener {
        void loadFragment(String backStackName);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public DialogOutputPuzzleboxGimmickFragment() {
        // Required empty public constructor
    }

    public static DialogOutputPuzzleboxGimmickFragment newInstance() {
        DialogOutputPuzzleboxGimmickFragment fragment = new DialogOutputPuzzleboxGimmickFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.dialog_output_puzzlebox_gimmick, container, false);



        getDialog().getWindow().setTitle( getString(R.string.title_dialog_fragment_gimmick));

        buttonConnectGimmick = v.findViewById(R.id.buttonConnectGimmick);
        buttonConnectGimmick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "buttonConnectGimmick()");
                connectGimmick();
            }
        });


        Button buttonDeviceCancel = v.findViewById(R.id.buttonDeviceCancel);
        buttonDeviceCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                disconnectHeadset();
                disconnectGimmick();
                broadcastTileStatus("false");
                Log.e(TAG, "buttonDeviceCancel()");
                dismiss();
            }
        });

        buttonDeviceEnable = v.findViewById(R.id.buttonDeviceEnable);
        buttonDeviceEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                broadcastTileStatus("true");
                Log.e(TAG, "buttonDeviceEnable()");
                dismiss();
            }
        });


        if (! DevicePuzzleboxGimmickSingleton.getInstance().connected) {
            buttonConnectGimmick.setText(R.string.buttonStatusPuzzleboxGimmickConnect);
        } else {
            buttonConnectGimmick.setText(R.string.buttonStatusPuzzleboxGimmickDisconnect);
            buttonDeviceEnable.setVisibility(View.VISIBLE);
            buttonDeviceEnable.setEnabled(true);
        }


        displayDevicesFound();


        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    // ################################################################

    public void broadcastTileStatus(String value) {

        Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.tile.event");

        intent.putExtra("id", profileID);
        intent.putExtra("name", "active");
        intent.putExtra("value", value);
        intent.putExtra("category", "outputs");
//        intent.putExtra("category", "inputs");

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

    }


    // ################################################################

    public void onPause() {

        super.onPause();

        LocalBroadcastManager.getInstance(
                getActivity()).unregisterReceiver(
                mStatusReceiver);

    } // onPause


    // ################################################################

    public void onResume() {

        // Store access variables for window and blank point

        Window window = getDialog().getWindow();

        Point size = new Point();

        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();

        display.getSize(size);

//		// Set the width of the dialog proportional to a percentage of the screen width
//		window.setLayout((int) (size.x * 0.98), WindowManager.LayoutParams.WRAP_CONTENT);

        // Set the dimensions  of the dialog proportional to a percentage of the screen dimensions
//		window.setLayout((int) (size.x * 0.95), (int) (size.y * 0.935));

        window.setGravity(Gravity.CENTER);

        // Call super onResume after sizing
        super.onResume();

//        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
//                mSignalQualityReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.signal_quality"));

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mStatusReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.puzzlebox.gimmick.status"));

    }


    // ################################################################

    private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String name = intent.getStringExtra("name");
            String value = intent.getStringExtra("value");

            if (! name.equals("populateSelectGimmick")) {
                Log.d(TAG, "[" + name + "]: " + value);
            }

            switch(name) {

                case "populateSelectGimmick":
//                    populateSelectEEG(); // TODO
                    break;

                case "status":

                    if (getActivity() != null) {

                        switch (value) {

                            case "connected":
                                buttonConnectGimmick.setText(getString(R.string.buttonStatusPuzzleboxGimmickDisconnect));

                                buttonDeviceEnable.setEnabled(true);
                                buttonDeviceEnable.setVisibility(View.VISIBLE);

                                break;

                            case "disconnected":
                                buttonConnectGimmick.setText(getString(R.string.buttonStatusPuzzleboxGimmickConnect));

                                buttonDeviceEnable.setEnabled(false);
                                buttonDeviceEnable.setVisibility(View.INVISIBLE);

                                break;

                            case "connecting":
                                buttonConnectGimmick.setText(getString(R.string.buttonStatusPuzzleboxGimmickConnecting));
                                break;

                        }

                    }

                    break;

                case "select":

                    if (value != null) {

                        Log.e(TAG, "here");

                        onSelectGimmickItem(value);

//                        int mId = Integer.parseInt(value);
//                        if (mId == -1) {
//                            onSelectGimmickRefresh();
//                        } else {
//                            Log.d(TAG, "onSelectEEGItem(" + mId + ")");
//                            onSelectGimmickItem(mId);
//                        }
                    }
                    break;

            }

        }

    };

    // ################################################################



    // ################################################################

    public void onSelectGimmickRefresh() {
        Log.i(TAG, "onSelectEEGRefresh()");
    }


    // ################################################################

    //    public void onSelectGimmickItem(Integer deviceNumber) {
    public void onSelectGimmickItem(String deviceNumber) {

        Log.i(TAG, "Selecting Gimmick: " + deviceNumber);

        buttonConnectGimmick.setText(getResources().getString(R.string.buttonStatusPuzzleboxGimmickConnecting));

        broadcastCommandBluetooth("connect", deviceNumber);

        // Dismiss dialog
        DialogOutputPuzzleboxGimmickSelectFragment mSelectGimmick = (DialogOutputPuzzleboxGimmickSelectFragment) getActivity().getSupportFragmentManager().findFragmentByTag("mSelectGimmick");

        if (mSelectGimmick != null)
            mSelectGimmick.dismiss();

        DevicePuzzleboxGimmickSingleton.getInstance().selectGimmickDialogVisible = false;

//        buttonDeviceEnable.setVisibility(View.VISIBLE);
//        buttonDeviceEnable.setEnabled(true);

    }


    // ################################################################

//    public void onPause() {
//
//        super.onPause();
//
//        LocalBroadcastManager.getInstance(
//                getActivity().getApplicationContext()).unregisterReceiver(
//                mSignalQualityReceiver);
//
//        LocalBroadcastManager.getInstance(
//                getActivity()).unregisterReceiver(
//                mStatusReceiver);
//
//    } // onPause


    // ################################################################

//    public void onResume() {
//
//        // Store access variables for window and blank point
//
//        Window window = getDialog().getWindow();
//
//        Point size = new Point();
//
//        // Store dimensions of the screen in `size`
//        Display display = window.getWindowManager().getDefaultDisplay();
//
//        display.getSize(size);
//
////		// Set the width of the dialog proportional to a percentage of the screen width
////		window.setLayout((int) (size.x * 0.98), WindowManager.LayoutParams.WRAP_CONTENT);
//
//        // Set the dimensions  of the dialog proportional to a percentage of the screen dimensions
////		window.setLayout((int) (size.x * 0.95), (int) (size.y * 0.935));
//
//        window.setGravity(Gravity.CENTER);
//
//        // Call super onResume after sizing
//        super.onResume();
//
//        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
//                mSignalQualityReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.signal_quality"));
//
//        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
//                mStatusReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.emotiv.insight.status"));
//
//    }


    // ################################################################

    private  void broadcastCommandBluetooth(String name, String value) {

        Log.d(TAG, "broadcastCommandBluetooth: " + name + ": " + value);

        Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.bluetooth.command");

        intent.putExtra("name", name);
        intent.putExtra("value", value);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

    }

    // ################################################################

    private BroadcastReceiver mEventReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String commandName = String.valueOf(intent.getStringExtra("name"));
            String commandValue = String.valueOf(intent.getStringExtra("value"));

            Log.d(TAG, "commandName: " + commandName + ", commandValue: " + commandValue);

            switch (commandName) {

                case "command":

                    switch (commandValue) {
                        case "displayDevicesFound":
                            displayDevicesFound();
                            break;
                    }

                    break;
                case "loadFragment":
                    if (mListenerFragment != null)
                        mListenerFragment.loadFragment(commandValue);
                    else
                        Log.d(TAG, "mListenerFragment was null");

            }

        }

    };


    // ################################################################

    public void connectGimmick() {

		/*
		 * Called when the "Connect" button is pressed
		 */

        Log.v(TAG, "connectHeadset()");

        if (!DevicePuzzleboxGimmickSingleton.getInstance().lock) {
//			intentEmotivInsight = new Intent(getActivity(), EmotivInsightService.class);
//            getActivity().startService(intentEmotivInsight); // TODO
            showSelectGimmick();
        } else {
//            disconnectHeadset();
            disconnectGimmick();
        }

    } // connectHeadset

    //    public void connectHeadset() {
//
//		/*
//		 * Called when the "Connect" button is pressed
//		 */
//
//        Log.v(TAG, "connectHeadset()");
//
//        if (!DeviceEmotivInsightSingleton.getInstance().lock) {
////			intentEmotivInsight = new Intent(getActivity(), EmotivInsightService.class);
//            getActivity().startService(intentEmotivInsight);
//        } else {
//            disconnectHeadset();
//        }
//
//    } // connectHeadset


//	################################################################


    public void disconnectGimmick() {

		/*
		 * Called when "Disconnect" button is pressed
		 */

        Log.v(TAG, "disconnectHeadset()");

        if (DevicePuzzleboxGimmickSingleton.getInstance().lock) {
//            DevicePuzzleboxGimmickSingleton.getInstance().connectEmotivInsight(-1);
        }

//        getActivity().stopService(intentEmotivInsight); // TODO

        // This should actually happen via mStatusReceiver
//		buttonDeviceEnable.setText(getString(R.string.buttonStatusEmotivInsightConnect));

        buttonConnectGimmick.setText(getString(R.string.buttonStatusEmotivInsightConnect));


    } // disconnectHeadset


//    public void disconnectHeadset() {
//
//		/*
//		 * Called when "Disconnect" button is pressed
//		 */
//
//        Log.v(TAG, "disconnectHeadset()");
//
//        if (DeviceEmotivInsightSingleton.getInstance().lock) {
//            DeviceEmotivInsightSingleton.getInstance().connectEmotivInsight(-1);
//        }
//
//        getActivity().stopService(intentEmotivInsight);
//
//        // This should actually happen via mStatusReceiver
////		buttonDeviceEnable.setText(getString(R.string.buttonStatusEmotivInsightConnect));
//
//        buttonConnectEEG.setText(getString(R.string.buttonStatusEmotivInsightConnect));
//
//
//    } // disconnectHeadset


    // ################################################################


    public void displayDevicesFound() {

//        Log.e(TAG, "displayDevicesFound: " + DataSingleton.getInstance().devicesFound.size());
        Log.e(TAG, "displayDevicesFound: " + DevicePuzzleboxGimmickSingleton.getInstance().devicesFound.size());

        ArrayList<String> deviceNames = new ArrayList<>();

//        for (ScanResult sr : DataSingleton.getInstance().devicesFound) {
        for (ScanResult sr : DevicePuzzleboxGimmickSingleton.getInstance().devicesFound) {
            if (sr.getDevice().getName() != null) {
//				deviceNames.add(sr.getDevice().getName() + " [" + sr.getDevice().getAddress() + "]");
                deviceNames.add(sr.getDevice().getName() + " [" + sr.getDevice().getAddress() + "] [Rssi: " + sr.getRssi() + "]");
            }
        }

        if (deviceNames.isEmpty()) {
            deviceNames.add(getResources().getString(R.string.scan_default_list));
        }

//        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
//                android.R.layout.simple_list_item_1, deviceNames);
//        setListAdapter(adapter);

    }

//    @Override
//    public void onListItemClick(ListView l, View v, int position, long id) {
//
//        String deviceSelection = (String) getListAdapter().getItem(position);
//        Toast.makeText(getActivity(), deviceSelection, Toast.LENGTH_SHORT).show();
//
//        Log.i(TAG, "Device selected: " + deviceSelection);
//
//        textViewScanTitle.setText(getResources().getString(R.string.scan_connecting));
//
//        broadcastCommandBluetooth("connect", deviceSelection);
//
//    }

    // ################################################################

//    public void populateSelectGimmick() {
//
//        if (!DevicePuzzleboxGimmickSingleton.getInstance().lock) {
//
//            int number = DevicePuzzleboxGimmickSingleton.getInstance().getInsightDeviceCount();
//
//            DialogOutputPuzzleboxGimmickSelectFragment mSelectGimmick = (DialogOutputPuzzleboxGimmickSelectFragment) getActivity().getSupportFragmentManager().findFragmentByTag("mSelectGimmick");
//            if (mSelectGimmick != null) {
//			Log.e(TAG, "(mSelectGimmick != null)");
////			break;
//                return;
//            }
//
//            DevicePuzzleboxGimmickSingleton.getInstance().detectedDevices = new ArrayList<>();
//
//            for (int deviceCount = 0; deviceCount < number; deviceCount++) {
//                String deviceName = DevicePuzzleboxGimmickSingleton.getInstance().getInsightDeviceName(deviceCount);
//                Log.d(TAG, "Insight detected: " + deviceCount + ": \"" + deviceName + "\" ");
//
//                if (!DevicePuzzleboxGimmickSingleton.getInstance().detectedDevices.contains(deviceName)) {
//                    DevicePuzzleboxGimmickSingleton.getInstance().detectedDevices.add(deviceCount, deviceName);
//                }
//
//                showSelectGimmick();
//
//            }
//        }
//        else {
//            Log.e(TAG, "populateSelectEEG called on locked device");
//        }
//
//    }

    // ################################################################

    public void showSelectGimmick() {

        if (!DevicePuzzleboxGimmickSingleton.getInstance().selectGimmickDialogVisible) {

            DevicePuzzleboxGimmickSingleton.getInstance().selectGimmickDialogVisible = true;

            Log.d(TAG, "showSelectGimmick()");

            FragmentManager fm = getActivity().getSupportFragmentManager();
            DialogOutputPuzzleboxGimmickSelectFragment mSelectGimmick = new DialogOutputPuzzleboxGimmickSelectFragment();
            try {
                mSelectGimmick.show(fm, "mSelectGimmick");
            } catch (Exception e) {
                Log.e(TAG, "showSelectGimmick.show(fm, \"showSelectGimmick\"): " + e);
            }
        }

    }


}
