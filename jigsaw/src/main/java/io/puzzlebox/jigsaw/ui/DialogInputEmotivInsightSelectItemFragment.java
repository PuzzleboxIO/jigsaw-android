package io.puzzlebox.jigsaw.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.puzzlebox.jigsaw.R;

public class DialogInputEmotivInsightSelectItemFragment extends Fragment {

	private final String TAG = DialogInputEmotivInsightSelectItemFragment.class.getSimpleName();

	private static final String ARG_PARENT_ID = "paramParentId";
	private static final String ARG_INSIGHT_ID = "paramInsightId";
	private static final String ARG_NAME = "paramName";

	private int mParamParentId;
	private int mParamInsightId;
	private String mParamName;

	private int mId;

	private OnFragmentInteractionListener mListener;

	public static DialogInputEmotivInsightSelectItemFragment newInstance(Integer paramParentId,
																		 Integer paramInsightId,
																		 String paramName) {
		DialogInputEmotivInsightSelectItemFragment fragment = new DialogInputEmotivInsightSelectItemFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_PARENT_ID, paramParentId);
		args.putInt(ARG_INSIGHT_ID, paramInsightId);
		args.putString(ARG_NAME, paramName);
		fragment.setArguments(args);
		return fragment;
	}

	public DialogInputEmotivInsightSelectItemFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParamParentId = getArguments().getInt(ARG_PARENT_ID);
			mParamInsightId = getArguments().getInt(ARG_INSIGHT_ID);
			mParamName = getArguments().getString(ARG_NAME);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.dialog_input_emotiv_insight_select_item, container, false);

		mId = mParamInsightId; // we use a separate variable here because parameters may overwrite each other

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
		TextView textViewSelectEEGNumber = v.findViewById(R.id.textViewSelectEEGNumber);
//		textViewSelectEEGNumber.setTypeface(tf);
//		textViewSelectEEGNumber.setTextSize(pixels);
		textViewSelectEEGNumber.setText("#" + String.valueOf(mParamInsightId) + ": ");

		TextView textViewSelectEEGName = v.findViewById(R.id.textViewSelectEEGName);
//		textViewSelectEEGName.setTypeface(tf);
//		textViewSelectEEGName.setTextSize(pixels);
		textViewSelectEEGName.setText(mParamName);

		LinearLayout layoutSelectEEG = v.findViewById(R.id.layoutSelectEEG);
		layoutSelectEEG.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.emotiv.insight.status");
				intent.putExtra("name", "select");
				intent.putExtra("value", Integer.toString(mId));
				LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
			}
		});

		Button buttonChoose = v.findViewById(R.id.buttonChoose);
		buttonChoose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.emotiv.insight.status");
				intent.putExtra("name", "select");
				intent.putExtra("value", Integer.toString(mId));
				LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

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
			Log.e(TAG, context.toString()  + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public interface OnFragmentInteractionListener {
		void onSelectEEGItem(Integer deviceNumber);
	}
}
