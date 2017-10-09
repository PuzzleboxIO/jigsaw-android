package io.puzzlebox.jigsaw.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import io.puzzlebox.jigsaw.R;
import io.puzzlebox.jigsaw.data.DeviceEmotivInsightSingleton;

public class DialogInputEmotivInsightSelectEEGFragment extends DialogFragment {

	private final String TAG = DialogInputEmotivInsightSelectEEGFragment.class.getSimpleName();

	LinearLayout dynamicLayout;

	private OnFragmentInteractionListener mListener;

	public DialogInputEmotivInsightSelectEEGFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
//		setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme); // creates a full-screen dialog window
//		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme);
//		setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.dialog_input_emotiv_insight_select_eeg, null);

		// External fonts
//		String fontPath = "fonts/HelveticaNeueforTarget-Rm.otf";
//		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), fontPath);
//		String fontPathBold = "fonts/HelveticaNeueforTarget-Bd.otf";
//		Typeface tfBold = Typeface.createFromAsset(getActivity().getAssets(), fontPath);
//		String fontPathMedium = "fonts/HelveticaNeueforTarget-Md.otf";
//		Typeface tfMedium = Typeface.createFromAsset(getActivity().getAssets(), fontPath);
//
//		float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
//
//		TextView textViewSelectEEG = (TextView) v.findViewById(R.id.textViewSelectEEG);
//		textViewSelectEEG.setTypeface(tf);
//		textViewSelectEEG.setTextSize(pixels);


		Button buttonRefresh = v.findViewById(R.id.buttonRefresh);
		buttonRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "buttonChoose onClick(): " + -1);
				if (mListener != null) {
					mListener.onSelectEEGItem(-1);
				}
			}

		});


		dynamicLayout = v.findViewById(R.id.dynamicLayoutSelectEEG);

		for (int i = 0; i < DeviceEmotivInsightSingleton.getInstance().detectedDevices.size(); i++) {

			String deviceName = DeviceEmotivInsightSingleton.getInstance().detectedDevices.get(i);

			Log.d(TAG, "Adding Insight: " + i + ": \"" + deviceName + "\" ");

			if (savedInstanceState == null) {

				DialogInputEmotivInsightSelectItemFragment item = io.puzzlebox.jigsaw.ui.DialogInputEmotivInsightSelectItemFragment.newInstance(
						  R.id.dynamicLayoutSelectEEG,
						  i,
						  deviceName);

				try {
					FragmentTransaction ft = getChildFragmentManager().beginTransaction();
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
					ft.add(R.id.dynamicLayoutSelectEEG, item, "device" + i);
					ft.commit();
				} catch (Exception e) {
					Log.e(TAG, "ft Exception:" + e.toString());
				}

			}

		}

		return v;
	}

//    @Override
//    public void show(FragmentManager manager, String tag) {
//        if (manager.findFragmentByTag(tag) == null) {
//            super.show(manager, tag);
//        }
//    }


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
//			throw new RuntimeException(context.toString()
//					  + " must implement OnFragmentInteractionListener");
			Log.e(TAG, context.toString()  + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public interface OnFragmentInteractionListener {
		void onSelectEEGRefresh();
		void onSelectEEGItem(Integer deviceNumber);
	}



}
