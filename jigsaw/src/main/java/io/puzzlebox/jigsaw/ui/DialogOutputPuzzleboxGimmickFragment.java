package io.puzzlebox.jigsaw.ui;

import android.annotation.SuppressLint;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import android.util.Log;
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
import androidx.core.content.ContextCompat;

public class DialogOutputPuzzleboxGimmickFragment extends DialogFragment {

    private static final String TAG = DialogOutputPuzzleboxGimmickFragment.class.getSimpleName();

    Button buttonConnectGimmick;
    Button buttonDeviceEnable;

    public DialogOutputPuzzleboxGimmickFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Window activityWindow = requireActivity().getWindow();
        if (activityWindow != null) activityWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.dialog_output_puzzlebox_gimmick, container, false);

        Window dialogWindow = requireDialog().getWindow();
        if (dialogWindow != null) dialogWindow.setTitle( getString(R.string.title_dialog_fragment_gimmick));

        buttonConnectGimmick = v.findViewById(R.id.buttonConnectGimmick);
        buttonConnectGimmick.setOnClickListener(view -> {
            Log.e(TAG, "buttonConnectGimmick()");
            connectGimmick();
        });

        Button buttonDeviceCancel = v.findViewById(R.id.buttonDeviceCancel);
        buttonDeviceCancel.setOnClickListener(view -> {
            disconnectGimmick();
            broadcastTileStatus("false");
            Log.e(TAG, "buttonDeviceCancel()");
            dismiss();
        });

        buttonDeviceEnable = v.findViewById(R.id.buttonDeviceEnable);
        buttonDeviceEnable.setOnClickListener(view -> {
            broadcastTileStatus("true");
            Log.e(TAG, "buttonDeviceEnable()");
            dismiss();
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

    public void broadcastTileStatus(String value) {

        Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.tile.event");

        intent.putExtra("id", "puzzlebox_gimmick");
        intent.putExtra("name", "active");
        intent.putExtra("value", value);
        intent.putExtra("category", "outputs");

        requireActivity().sendBroadcast(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(
                mStatusReceiver);
    }

    @Override
    public void onResume() {

        // Store access variables for window and blank point
        Window window = requireDialog().getWindow();

        if (window == null) {
            super.onResume();
            return;
        }

        window.setGravity(Gravity.CENTER);

        // Call super onResume after sizing
        super.onResume();

        ContextCompat.registerReceiver(requireActivity(), mStatusReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.puzzlebox.gimmick.status"), ContextCompat.RECEIVER_NOT_EXPORTED);

    }

    private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String name = intent.getStringExtra("name");
            String value = intent.getStringExtra("value");
            if (name == null) return;

            if (! "populateSelectGimmick".equals(name)) {
                Log.d(TAG, "[" + name + "]: " + value);
            }

            switch(name) {

                case "populateSelectGimmick":
                    break;

                case "status":

                    if (getActivity() != null && value != null) {

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
                        onSelectGimmickItem(value);
                    }
                    break;
            }
        }

    };

    public void onSelectGimmickItem(String deviceNumber) {

        Log.i(TAG, "Selecting Gimmick: " + deviceNumber);

        buttonConnectGimmick.setText(getResources().getString(R.string.buttonStatusPuzzleboxGimmickConnecting));

        broadcastCommandBluetooth(deviceNumber);

        // Dismiss dialog
        DialogOutputPuzzleboxGimmickSelectFragment mSelectGimmick = (DialogOutputPuzzleboxGimmickSelectFragment) requireActivity().getSupportFragmentManager().findFragmentByTag("mSelectGimmick");

        if (mSelectGimmick != null)
            mSelectGimmick.dismiss();

        DevicePuzzleboxGimmickSingleton.getInstance().selectGimmickDialogVisible = false;
    }

    private  void broadcastCommandBluetooth(String value) {

        Log.d(TAG, "broadcastCommandBluetooth: connect: " + value);

        Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.bluetooth.command");

        intent.putExtra("name", "connect");
        intent.putExtra("value", value);

        requireContext().sendBroadcast(intent);
    }

    public void connectGimmick() {
        /*
         * Called when the "Connect" button is pressed
         */

        if (!DevicePuzzleboxGimmickSingleton.getInstance().lock) {
            showSelectGimmick();
        } else {
            disconnectGimmick();
        }
    }

    public void disconnectGimmick() {
        /*
         * Called when "Disconnect" button is pressed
         */

        buttonConnectGimmick.setText(getString(R.string.buttonStatusEmotivInsightConnect));
    }

    @SuppressLint("MissingPermission")
    public void displayDevicesFound() {

        Log.d(TAG, "displayDevicesFound: " + DevicePuzzleboxGimmickSingleton.getInstance().devicesFound.size());

        ArrayList<String> deviceNames = new ArrayList<>();

        for (ScanResult sr : DevicePuzzleboxGimmickSingleton.getInstance().devicesFound) {
            if (sr.getDevice().getName() != null) {
                deviceNames.add(sr.getDevice().getName() + " [" + sr.getDevice().getAddress() + "] [RSSI: " + sr.getRssi() + "]");
            }
        }

        if (deviceNames.isEmpty()) {
            deviceNames.add(getResources().getString(R.string.scan_default_list));
        }
    }

    public void showSelectGimmick() {

        if (!DevicePuzzleboxGimmickSingleton.getInstance().selectGimmickDialogVisible) {

            DevicePuzzleboxGimmickSingleton.getInstance().selectGimmickDialogVisible = true;

            FragmentManager fm = requireActivity().getSupportFragmentManager();
            DialogOutputPuzzleboxGimmickSelectFragment mSelectGimmick = new DialogOutputPuzzleboxGimmickSelectFragment();
            try {
                mSelectGimmick.show(fm, "mSelectGimmick");
            } catch (Exception e) {
                Log.e(TAG, "showSelectGimmick.show(fm, \"showSelectGimmick\"): " + e);
            }
        }
    }
}
