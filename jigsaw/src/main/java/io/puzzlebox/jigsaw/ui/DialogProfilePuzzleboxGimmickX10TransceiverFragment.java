package io.puzzlebox.jigsaw.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import io.puzzlebox.jigsaw.R;
import io.puzzlebox.jigsaw.data.DevicePuzzleboxGimmickSingleton;

public class DialogProfilePuzzleboxGimmickX10TransceiverFragment extends DialogFragment {

    private static final String TAG = DialogProfilePuzzleboxGimmickX10TransceiverFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public DialogProfilePuzzleboxGimmickX10TransceiverFragment() {
        // Required empty public constructor
    }

    public static DialogProfilePuzzleboxGimmickX10TransceiverFragment newInstance(String param1, String param2) {
        DialogProfilePuzzleboxGimmickX10TransceiverFragment fragment = new DialogProfilePuzzleboxGimmickX10TransceiverFragment();
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
        View v = inflater.inflate(R.layout.dialog_profile_puzzlebox_gimmick_x10_transceiver, container, false);

        EditText editTextChannel = v.findViewById(R.id.editTextChannel);
        editTextChannel.setText(DevicePuzzleboxGimmickSingleton.getInstance().x10ID);

        Button buttonStatusPuzzleboxGimmickX10Off = v.findViewById(R.id.buttonStatusPuzzleboxGimmickX10Off);
        buttonStatusPuzzleboxGimmickX10Off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "buttonStatusPuzzleboxGimmickX10Off()");
                broadcastCommandBluetooth("x10", DevicePuzzleboxGimmickSingleton.getInstance().x10ID + " Off");
                DevicePuzzleboxGimmickSingleton.getInstance().x10Level = 0;
            }
        });
        Button buttonStatusPuzzleboxGimmickX10On = v.findViewById(R.id.buttonStatusPuzzleboxGimmickX10On);
        buttonStatusPuzzleboxGimmickX10On.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "buttonStatusPuzzleboxGimmickX10On()");
                broadcastCommandBluetooth("x10", DevicePuzzleboxGimmickSingleton.getInstance().x10ID + " On");
                DevicePuzzleboxGimmickSingleton.getInstance().x10Level = 10;
            }
        });
        Button buttonStatusPuzzleboxGimmickX10Dim = v.findViewById(R.id.buttonStatusPuzzleboxGimmickX10Dim);
        buttonStatusPuzzleboxGimmickX10Dim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "buttonStatusPuzzleboxGimmickX10Dim()");
                if (DevicePuzzleboxGimmickSingleton.getInstance().x10Level > 0) {
                    broadcastCommandBluetooth("x10", DevicePuzzleboxGimmickSingleton.getInstance().x10ID + " Dim");
                    DevicePuzzleboxGimmickSingleton.getInstance().x10Level--;
                }
            }
        });
        Button buttonStatusPuzzleboxGimmickX10Bright = v.findViewById(R.id.buttonStatusPuzzleboxGimmickX10Bright);
        buttonStatusPuzzleboxGimmickX10Bright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "buttonStatusPuzzleboxGimmickX10Bright()");
                if (DevicePuzzleboxGimmickSingleton.getInstance().x10Level < 10) {
                    broadcastCommandBluetooth("x10", DevicePuzzleboxGimmickSingleton.getInstance().x10ID + " Bright");
                    DevicePuzzleboxGimmickSingleton.getInstance().x10Level++;
                }
            }
        });

        Button buttonDeviceCancel = v.findViewById(io.puzzlebox.jigsaw.R.id.buttonDeviceCancel);
        buttonDeviceCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button buttonDeviceEnable = v.findViewById(io.puzzlebox.jigsaw.R.id.buttonDeviceEnable);
        buttonDeviceEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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


    // ################################################################

    private  void broadcastCommandBluetooth(String name, String value) {

        Log.d(TAG, "broadcastCommandBluetooth: " + name + ": " + value);

        Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.bluetooth.command");

        intent.putExtra("name", name);
        intent.putExtra("value", value);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

    }

}
