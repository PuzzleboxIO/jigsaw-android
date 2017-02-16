package io.puzzlebox.jigsaw.ui;

import android.app.Activity;
import android.content.Context;
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

	private JoystickView joystickView;

	private OnFragmentInteractionListener mListener;

	public JoystickFragment() {
		// Required empty public constructor
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
			}
		});

		ImageButton imageButton1 = (ImageButton) v.findViewById(R.id.imageButton1);
		imageButton1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "imageButton1 onClick(View v): " + v.toString());
			}
		});

		ImageButton imageButton2 = (ImageButton) v.findViewById(R.id.imageButton2);
		imageButton2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "imageButton2 onClick(View v): " + v.toString());
			}
		});

		ImageButton imageButton3 = (ImageButton) v.findViewById(R.id.imageButton3);
		imageButton3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "imageButton3 onClick(View v): " + v.toString());
			}
		});


		ImageButton imageButton4 = (ImageButton) v.findViewById(R.id.imageButton4);
		imageButton4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "imageButton4 onClick(View v): " + v.toString());
			}
		});

		ImageButton imageButton5 = (ImageButton) v.findViewById(R.id.imageButton5);
		imageButton5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "imageButton5 onClick(View v): " + v.toString());
			}
		});

		ImageButton imageButton6 = (ImageButton) v.findViewById(R.id.imageButton6);
		imageButton6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "imageButton6 onClick(View v): " + v.toString());
			}
		});


		ImageButton imageButton7 = (ImageButton) v.findViewById(R.id.imageButton7);
		imageButton7.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "imageButton7 onClick(View v): " + v.toString());
			}
		});

		ImageButton imageButton8 = (ImageButton) v.findViewById(R.id.imageButton8);
		imageButton8.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "imageButton8 onClick(View v): " + v.toString());
			}
		});

		ImageButton imageButton9 = (ImageButton) v.findViewById(R.id.imageButton9);
		imageButton9.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "imageButton9 onClick(View v): " + v.toString());
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


}


