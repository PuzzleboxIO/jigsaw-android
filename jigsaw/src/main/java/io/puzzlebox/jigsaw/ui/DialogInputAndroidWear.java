package io.puzzlebox.jigsaw.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import io.puzzlebox.jigsaw.R;

public class DialogInputAndroidWear extends DialogFragment {

    public final static String profileID = "wear";

    // UI
    Button buttonDeviceEnable;

    private OnFragmentInteractionListener mListener;

    public DialogInputAndroidWear() {
        // Required empty public constructor
    }

    public static DialogInputAndroidWear newInstance() {
        DialogInputAndroidWear fragment = new DialogInputAndroidWear();
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.dialog_input_android_wear, container, false);

        Button buttonDeviceCancel = v.findViewById(R.id.buttonDeviceCancel);
        buttonDeviceCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                broadcastTileStatus("false");
                dismiss();
            }
        });

        buttonDeviceEnable = v.findViewById(R.id.buttonDeviceEnable);
        buttonDeviceEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                broadcastTileStatus("true");
                dismiss();
            }
        });

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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void onResume() {
        // Store access variables for window and blank point

        Window window = getDialog().getWindow();

        Point size = new Point();

        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();

        display.getSize(size);

        // Set the width of the dialog proportional to a percentage of the screen width
        window.setLayout((int)(size.x *0.75), WindowManager.LayoutParams.WRAP_CONTENT);

        window.setGravity(Gravity.CENTER);

        // Call super onResume after sizing
        super.onResume();
    }

    public void broadcastTileStatus(String value) {

        Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.tile.event");

        intent.putExtra("id", profileID);
        intent.putExtra("name", "active");
        intent.putExtra("value", value);
        intent.putExtra("category", "inputs");

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }
}
