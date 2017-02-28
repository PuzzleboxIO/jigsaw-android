package io.puzzlebox.jigsaw.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageButton;

import io.puzzlebox.jigsaw.R;

public class JoystickFragment extends Fragment {

	private final static String TAG = JoystickFragment.class.getSimpleName();


	// Reference: http://gimx.fr/wiki/index.php?title=Controller_Maps

	public final int joystick_axis_x_minimum_dualshock4 = -128;
	public final int joystick_axis_x_maximum_dualshock4 = 127;
	public final int joystick_axis_y_minimum_dualshock4 = -128;
	public final int joystick_axis_y_maximum_dualshock4 = 127;

	public final int joystick_axis_x_minimum_xboxonepad = -32768;
	public final int joystick_axis_x_maximum_xboxonepad= 32767;
	public final int joystick_axis_y_minimum_xboxonepad = -32768;
	public final int joystick_axis_y_maximum_xboxonepad = 32767;


	private JoystickView joystickView;


	private OnFragmentInteractionListener mListener;

	public JoystickFragment() {
		// Required empty public constructor
	}

	public interface OnFragmentLoadListener {
		void loadFragment(String backStackName);
	}

	public static JoystickFragment newInstance(String param1, String param2) {
		JoystickFragment fragment = new JoystickFragment();
		Bundle args = new Bundle();
//		args.putString(ARG_PARAM1, param1);
//		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
//			mParam1 = getArguments().getString(ARG_PARAM1);
//			mParam2 = getArguments().getString(ARG_PARAM2);
		}

	}

	public String moveDualShock4(int angle, int strength) {

		int x = 0;
		int y = 0;

		// Reference: https://github.com/controlwear/virtual-joystick-android/blob/master/misc/virtual-joystick.png?raw=true

		// TODO: Correct calulation based on drawing a line from the center values at a certain angle for a certain percentage towards the edge of the matrix of possible values
//		switch (angle) {
//			case 0-90:
//				// Upper Right
//				break;
//			case 91-180:
//				// Upper Left
//				break;
//			case 181-240:
//				// Lower Left
//				break;
//			case 241-359:
//				// Lower Right
//				break;
//			default:
//				break;
//		}


		if (((angle >= 0) && (angle <= 45)) || ((angle <= 360) && (angle >= 315))) {
			// Right
			if (strength >= 75) {
				x = joystick_axis_x_maximum_dualshock4;
			} else if (strength >= 50) {
				x = joystick_axis_x_maximum_dualshock4 / 2;
			} else if (strength >= 25) {
				x = joystick_axis_x_maximum_dualshock4 / 4;
			}
		} else if ((angle >= 45) && (angle <= 135)) {
			// Up
			if (strength >= 75) {
				y = joystick_axis_y_maximum_dualshock4;
			} else if (strength >= 50) {
				y = joystick_axis_y_maximum_dualshock4 / 2;
			} else if (strength >= 25) {
				y = joystick_axis_y_maximum_dualshock4 / 4;
			}
		} else if ((angle >= 135) && (angle <= 225)) {
			// Left
			if (strength >= 75) {
				x = joystick_axis_x_minimum_dualshock4;
			} else if (strength >= 50) {
				x = joystick_axis_x_minimum_dualshock4 / 2;
			} else if (strength >= 25) {
				x = joystick_axis_x_minimum_dualshock4 / 4;
			}
		} else if ((angle >= 225) && (angle <= 315)) {
			// Down
			if (strength >= 75) {
				y = joystick_axis_y_minimum_dualshock4;
			} else if (strength >= 50) {
				y = joystick_axis_y_minimum_dualshock4 / 2;
			} else if (strength >= 25) {
				y = joystick_axis_y_minimum_dualshock4 / 4;
			}
		}





		switch (angle) {
			case 0-45:
				// Upper Right
				break;
			case 91-180:
				// Upper Left
				break;
			case 181-240:
				// Lower Left
				break;
			case 241-359:
				// Lower Right
				break;
			default:
				break;
		}


		return "ls: " + x + ", " + y;

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
									 Bundle savedInstanceState) {


		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_joystick, container, false);


		JoystickView joystick = (JoystickView) v.findViewById(R.id.joystickViewLeft);
		joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
			@Override
			public void onMove(int angle, int strength) {
				// do whatever you want
				Log.v(TAG, "onMove(int angle, int strength): " + angle + ", " + strength);
				String command = moveDualShock4(angle, strength);
				Log.v(TAG, "moveDualShock4(): command: \"" + command + "\"");
//				broadcastCommandBluetooth("joystick", "ls: " + angle + "," + strength);
				broadcastCommandBluetooth("joystick", command);
			}
		});

		final ImageButton imageButton1 = (ImageButton) v.findViewById(R.id.imageButton1);
//		imageButton1.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Log.v(TAG, "imageButton1 onClick(View v): " + v.toString());
//				broadcastCommandBluetooth("joystick", "button1: on");
//			}
//		});
		imageButton1.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						// PRESSED
						imageButton1.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed_arcade, null));
						broadcastCommandBluetooth("joystick", "button1: 1");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_UP:
						// RELEASED
						imageButton1.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						broadcastCommandBluetooth("joystick", "button1: 0");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_CANCEL:
						imageButton1.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						broadcastCommandBluetooth("joystick", "button1: 0");
						return true;
				}
				return false;
			}
		});

		final ImageButton imageButton2 = (ImageButton) v.findViewById(R.id.imageButton2);
//		imageButton2.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Log.v(TAG, "imageButton2 onClick(View v): " + v.toString());
//			}
//		});
		imageButton2.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						// PRESSED
						broadcastCommandBluetooth("joystick", "button2: 1");
//						imageButton2.setImageDrawable(getResources().getDrawable(R.drawable.button_pressed_arcade));
						imageButton2.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed_arcade, null));
//						return true; // if you want to handle the touch event
						break;
					case MotionEvent.ACTION_UP:
						// RELEASED
						broadcastCommandBluetooth("joystick", "button2: 0");
//						imageButton2.setImageDrawable(getResources().getDrawable(R.drawable.button_arcade));
						imageButton2.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
//						return true; // if you want to handle the touch event'
						break;
					case MotionEvent.ACTION_CANCEL:
						broadcastCommandBluetooth("joystick", "button2: 0");
//						imageButton2.setImageDrawable(getResources().getDrawable(R.drawable.button_arcade));
						imageButton2.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
//						return true;
						break;
				}
//				return false;
				return true;
			}
		});

		final ImageButton imageButton3 = (ImageButton) v.findViewById(R.id.imageButton3);
//		imageButton3.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Log.v(TAG, "imageButton3 onClick(View v): " + v.toString());
//			}
//		});
		imageButton3.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						// PRESSED
						imageButton3.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed_arcade, null));
						broadcastCommandBluetooth("joystick", "button3: 1");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_UP:
						// RELEASED
						imageButton3.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						broadcastCommandBluetooth("joystick", "button3: 0");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_CANCEL:
						imageButton3.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						broadcastCommandBluetooth("joystick", "button3: 0");
						return true;
				}
				return false;
			}
		});

		final ImageButton imageButton4 = (ImageButton) v.findViewById(R.id.imageButton4);
//		imageButton4.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Log.v(TAG, "imageButton4 onClick(View v): " + v.toString());
//			}
//		});
		imageButton4.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						// PRESSED
						broadcastCommandBluetooth("joystick", "button4: 1");
						imageButton4.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed_arcade, null));

						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_UP:
						// RELEASED
						imageButton4.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));

						broadcastCommandBluetooth("joystick", "button4: 0");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_CANCEL:
						imageButton4.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));

						broadcastCommandBluetooth("joystick", "button4: 0");
						return true;
				}
				return false;
			}
		});

		final ImageButton imageButton5 = (ImageButton) v.findViewById(R.id.imageButton5);
//		imageButton5.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Log.v(TAG, "imageButton5 onClick(View v): " + v.toString());
//			}
//		});
		imageButton5.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						// PRESSED
						imageButton5.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed_arcade, null));
						broadcastCommandBluetooth("joystick", "button5: 1");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_UP:
						// RELEASED
						imageButton5.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						broadcastCommandBluetooth("joystick", "button5: 0");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_CANCEL:
						imageButton5.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						broadcastCommandBluetooth("joystick", "button5: 0");
						return true;
				}
				return false;
			}
		});

		final ImageButton imageButton6 = (ImageButton) v.findViewById(R.id.imageButton6);
//		imageButton6.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Log.v(TAG, "imageButton6 onClick(View v): " + v.toString());
//			}
//		});
		imageButton6.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						// PRESSED
						imageButton6.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed_arcade, null));
						broadcastCommandBluetooth("joystick", "button6: 1");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_UP:
						// RELEASED
						imageButton6.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						broadcastCommandBluetooth("joystick", "button6: 0");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_CANCEL:
						imageButton6.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						broadcastCommandBluetooth("joystick", "button6: 0");
						return true;
				}
				return false;
			}
		});

		final ImageButton imageButton7 = (ImageButton) v.findViewById(R.id.imageButton7);
//		imageButton7.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Log.v(TAG, "imageButton7 onClick(View v): " + v.toString());
//			}
//		});
		imageButton7.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						// PRESSED
						imageButton7.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed_arcade, null));
						broadcastCommandBluetooth("joystick", "button7: 1");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_UP:
						// RELEASED
						imageButton7.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						broadcastCommandBluetooth("joystick", "button7: 0");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_CANCEL:
						broadcastCommandBluetooth("joystick", "button7: 0");
						imageButton7.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						return true;
				}
				return false;
			}
		});

		final ImageButton imageButton8 = (ImageButton) v.findViewById(R.id.imageButton8);
//		imageButton8.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Log.v(TAG, "imageButton8 onClick(View v): " + v.toString());
//			}
//		});
		imageButton8.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						// PRESSED
						imageButton8.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed_arcade, null));
						broadcastCommandBluetooth("joystick", "button8: 1");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_UP:
						// RELEASED
						imageButton8.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						broadcastCommandBluetooth("joystick", "button8: 0");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_CANCEL:
						imageButton8.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						broadcastCommandBluetooth("joystick", "button8: 0");
						return true;
				}
				return false;
			}
		});

		final ImageButton imageButton9 = (ImageButton) v.findViewById(R.id.imageButton9);
//		imageButton9.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Log.v(TAG, "imageButton9 onClick(View v): " + v.toString());
//			}
//		});
		imageButton9.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						// PRESSED
						imageButton9.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed_arcade, null));
						broadcastCommandBluetooth("joystick", "button9: 1");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_UP:
						// RELEASED
						imageButton9.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						broadcastCommandBluetooth("joystick", "button9: 0");
						return true; // if you want to handle the touch event
					case MotionEvent.ACTION_CANCEL:
						imageButton9.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.button_arcade, null));
						broadcastCommandBluetooth("joystick", "button9: 0");
						return true;
				}
				return false;
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

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		void onFragmentInteraction(Uri uri);
	}


	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {

		Log.e(TAG, "setUserVisibleHint(isVisibleToUser): " + isVisibleToUser);

		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser) {
			Activity a = getActivity();
			if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
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


