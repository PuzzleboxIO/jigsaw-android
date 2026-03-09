package io.puzzlebox.jigsaw.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import io.puzzlebox.jigsaw.R;

public class DialogInputDeviceSensors extends DialogFragment {

    public final static String profileID = "sensors";

    // UI
    Button buttonDeviceEnable;

    public DialogInputDeviceSensors() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.dialog_input_device_sensors, container, false);

        Button buttonDeviceCancel = v.findViewById(R.id.buttonDeviceCancel);
        buttonDeviceCancel.setOnClickListener(view -> {
            broadcastTileStatus("false");
            dismiss();
        });

        buttonDeviceEnable = v.findViewById(R.id.buttonDeviceEnable);
        buttonDeviceEnable.setOnClickListener(view -> {
            broadcastTileStatus("true");
            dismiss();
        });
        return v;
    }

    // ################################################################

    @Override
    public void onResume() {
        // Store access variables for window and blank point

        Window window = requireDialog().getWindow();

        if (window == null) {
            super.onResume();
            return;
        }

        int screenWidth = requireContext().getResources().getDisplayMetrics().widthPixels;

        // Set the width of the dialog proportional to a percentage of the screen width
        window.setLayout((int)(screenWidth * 0.75), WindowManager.LayoutParams.WRAP_CONTENT);

        window.setGravity(Gravity.CENTER);

        // Call super onResume after sizing
        super.onResume();

    }

    // ################################################################

    public void broadcastTileStatus(String value) {

        Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.tile.event");
        intent.setPackage(requireActivity().getPackageName());

        intent.putExtra("id", profileID);
        intent.putExtra("name", "active");
        intent.putExtra("value", value);
        intent.putExtra("category", "inputs");

        requireActivity().sendBroadcast(intent);

    }


}
