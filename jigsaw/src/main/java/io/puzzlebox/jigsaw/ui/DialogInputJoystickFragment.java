package io.puzzlebox.jigsaw.ui;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;

import io.puzzlebox.jigsaw.R;

public class DialogInputJoystickFragment extends DialogFragment
		implements SeekBar.OnSeekBarChangeListener {

	private final static String TAG = DialogInputJoystickFragment.class.getSimpleName();

	public final static String profileID = "joystick";

	// UI
	public SeekBar seekBarX;
	public SeekBar seekBarY;
	Button buttonDeviceEnable;

	private OnFragmentInteractionListener mListener;

	public DialogInputJoystickFragment() {
		// Required empty public constructor
	}

	public static DialogInputJoystickFragment newInstance() {
		DialogInputJoystickFragment fragment = new DialogInputJoystickFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.dialog_input_joystick, container, false);

		Window dialogWindow = requireDialog().getWindow();
		if (dialogWindow != null) dialogWindow.setTitle(getString(R.string.title_dialog_fragment_joystick));

		seekBarX = v.findViewById(R.id.seekBarX);
		seekBarX.setProgress(seekBarX.getMax() / 2);
		seekBarX.setOnSeekBarChangeListener(this);

		seekBarY = v.findViewById(R.id.seekBarY);
		seekBarY.setProgress(seekBarY.getMax() / 2);
		seekBarY.setOnSeekBarChangeListener(this);

		JoystickView joystickView = v.findViewById(R.id.joystickView);
		joystickView.setOnMoveListener(onMoveJoystick);

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

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public interface OnFragmentInteractionListener {
		@SuppressWarnings("EmptyMethod")
		void onFragmentInteraction(Uri uri);
	}

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

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
//		updateControlSignal();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	private final JoystickView.OnMoveListener onMoveJoystick = new JoystickView.OnMoveListener(){
		public void onMove(int angle, int strength) {
			Log.v(TAG, "onMoveJoystick(int angle, int strength): " + angle + ", " + strength);

			if ((angle == 0) && (strength == 0)) {
				// Reset values when let go
				seekBarX.setProgress(seekBarX.getMax() / 2);
				seekBarY.setProgress(seekBarY.getMax() / 2);
			}
			else if ((angle >= 60) && (angle <= 120)) {
				// Up
				int newY = seekBarY.getMax() / 2;
				newY = (int) (newY * (strength / 100.0));
				newY = seekBarY.getMax() / 2 + newY;
				seekBarY.setProgress(newY);
			}
			else if ((angle >= 240) && (angle <= 300)) {
				// Down
				int newY = seekBarY.getMax() / 2;
				newY = (int) (newY * (strength / 100.0));
				newY = seekBarY.getMax() / 2 - newY;
				seekBarY.setProgress(newY);
			}
			else if ((angle >= 150) && (angle <= 210)) {
				// Left
				int newX = seekBarX.getMax() / 2;
				newX = (int) (newX * (strength / 100.0));
				newX = seekBarX.getMax() / 2 - newX;
				seekBarX.setProgress(newX);
			}
			else if ((angle >= 330) || (angle <= 30)) {
				// Right
				int newX = seekBarX.getMax() / 2;
				newX = (int) (newX * (strength / 100.0));
				newX = seekBarX.getMax() / 2 + newX;
				seekBarX.setProgress(newX);
			}
		}
	};

	public void broadcastTileStatus(String value) {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.tile.event");

		intent.putExtra("id", profileID);
		intent.putExtra("name", "active");
		intent.putExtra("value", value);
		intent.putExtra("category", "inputs");

		requireActivity().sendBroadcast(intent);
	}
}
