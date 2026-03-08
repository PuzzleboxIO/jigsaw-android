package io.puzzlebox.jigsaw.ui;


import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import io.puzzlebox.jigsaw.R;

public class DialogInputEmotivInsightSelectItemFragment extends Fragment {

	private final String TAG = DialogInputEmotivInsightSelectItemFragment.class.getSimpleName();

	private static final String ARG_PARENT_ID = "paramParentId";
	private static final String ARG_INSIGHT_ID = "paramInsightId";
	private static final String ARG_NAME = "paramName";

	private int mParamInsightId;
	private String mParamName;

	private int mId;

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

		TextView textViewSelectEEGNumber = v.findViewById(R.id.textViewSelectEEGNumber);
		textViewSelectEEGNumber.setText(String.format(Locale.getDefault(), "#%d: ", mParamInsightId));

		TextView textViewSelectEEGName = v.findViewById(R.id.textViewSelectEEGName);
		textViewSelectEEGName.setText(mParamName);

		LinearLayout layoutSelectEEG = v.findViewById(R.id.layoutSelectEEG);
		layoutSelectEEG.setOnClickListener(view -> {
			Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.emotiv.insight.status");
			intent.putExtra("name", "select");
			intent.putExtra("value", Integer.toString(mId));
			requireActivity().sendBroadcast(intent);
		});

		Button buttonChoose = v.findViewById(R.id.buttonChoose);
		buttonChoose.setOnClickListener(view -> {
			Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.emotiv.insight.status");
			intent.putExtra("name", "select");
			intent.putExtra("value", Integer.toString(mId));
			requireActivity().sendBroadcast(intent);
		});
		return v;
	}

}
