package io.puzzlebox.jigsaw.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import java.text.DecimalFormat;

import io.puzzlebox.jigsaw.R;
import io.puzzlebox.jigsaw.protocol.ThinkGearService;

public class DialogNeuroSkyMindWaveFragment extends DialogFragment {

	private OnFragmentInteractionListener mListener;

	public DialogNeuroSkyMindWaveFragment() {
		// Required empty public constructor
	}

	public static DialogNeuroSkyMindWaveFragment newInstance(String param1, String param2) {
		DialogNeuroSkyMindWaveFragment fragment = new DialogNeuroSkyMindWaveFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
									 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.dialog_fragment_neurosky_mindwave, container, false);


		ProgressBar progressBarAttention = (ProgressBar) v.findViewById(R.id.progressBarAttention);
		final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
		ShapeDrawable progressBarAttentionDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarAttentionColor = "#FF0000";
		progressBarAttentionDrawable.getPaint().setColor(Color.parseColor(progressBarAttentionColor));
		ClipDrawable progressAttention = new ClipDrawable(progressBarAttentionDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarAttention.setProgressDrawable(progressAttention);
		progressBarAttention.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		ProgressBar progressBarMeditation = (ProgressBar) v.findViewById(R.id.progressBarMeditation);
		ShapeDrawable progressBarMeditationDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarMeditationColor = "#0000FF";
		progressBarMeditationDrawable.getPaint().setColor(Color.parseColor(progressBarMeditationColor));
		ClipDrawable progressMeditation = new ClipDrawable(progressBarMeditationDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarMeditation.setProgressDrawable(progressMeditation);
		progressBarMeditation.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		ProgressBar progressBarSignal = (ProgressBar) v.findViewById(R.id.progressBarSignal);
		ShapeDrawable progressBarSignalDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarSignalColor = "#00FF00";
		progressBarSignalDrawable.getPaint().setColor(Color.parseColor(progressBarSignalColor));
		ClipDrawable progressSignal = new ClipDrawable(progressBarSignalDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarSignal.setProgressDrawable(progressSignal);
		progressBarSignal.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		ProgressBar progressBarBlink = (ProgressBar) v.findViewById(R.id.progressBarBlink);
		ShapeDrawable progressBarRangeDrawable = new ShapeDrawable();
//		String progressBarBlinkColor = "#FF00FF";
//		String progressBarBlinkColor = "#990099";
		String progressBarBlinkColor = "#BBBBBB";
		progressBarRangeDrawable.getPaint().setColor(Color.parseColor(progressBarBlinkColor));
		ClipDrawable progressRange = new ClipDrawable(progressBarRangeDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarBlink.setProgressDrawable(progressRange);
		progressBarBlink.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarBlink.setMax(ThinkGearService.blinkRangeMax);


		// setup the Raw EEG History plot
		XYPlot eegRawHistoryPlot = (XYPlot) v.findViewById(R.id.eegRawHistoryPlot);
		SimpleXYSeries eegRawHistorySeries = new SimpleXYSeries("");

		// Use index value as xVal, instead of explicit, user provided xVals.
		//		eegRawHistorySeries.useImplicitXVals();

		// Setup the boundary mode, boundary values only applicable in FIXED mode.

		if (eegRawHistoryPlot != null) {

			eegRawHistoryPlot.setDomainBoundaries(0, ThinkGearService.EEG_RAW_FREQUENCY, BoundaryMode.FIXED);
			eegRawHistoryPlot.setRangeBoundaries(0, 1, BoundaryMode.GROW);

			eegRawHistoryPlot.addSeries(eegRawHistorySeries, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.BLACK, null, null));

			// Thin out domain and range tick values so they don't overlap
			eegRawHistoryPlot.setDomainStepValue(5);
			eegRawHistoryPlot.setTicksPerRangeLabel(3);

			// Sets the dimensions of the widget to exactly contain the text contents
			eegRawHistoryPlot.getDomainLabelWidget().pack();
			eegRawHistoryPlot.getRangeLabelWidget().pack();

			// Only display whole numbers in labels
			eegRawHistoryPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
			eegRawHistoryPlot.getGraphWidget().setRangeValueFormat(new DecimalFormat("0"));

			// Hide domain and range labels
			eegRawHistoryPlot.getGraphWidget().setDomainLabelWidth(0);
			eegRawHistoryPlot.getGraphWidget().setRangeLabelWidth(0);

			// Hide legend
			eegRawHistoryPlot.getLegendWidget().setVisible(false);

			// setGridPadding(float left, float top, float right, float bottom)
			eegRawHistoryPlot.getGraphWidget().setGridPadding(0, 0, 0, 0);

		}

		return v;
	}

//	@Override
//	public void onStart() {
//		super.onStart();
//
//		Dialog dialog = getDialog();
//		if (dialog != null) {
//			dialog.setTitle("NeuroSky MindWave Mobile"); // title_dialog_fragment_neurosky_mindwave
////			setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
////			dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
////			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
////			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//		}
//	}

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
//		window.setLayout((int) (size.x * 0.9), WindowManager.LayoutParams.WRAP_CONTENT);
		window.setLayout((int) (size.x * 0.975), WindowManager.LayoutParams.WRAP_CONTENT);

		// Set the dimensions  of the dialog proportional to a percentage of the screen dimensions
//		window.setLayout((int) (size.x * 0.95), (int) (size.y * 0.935));

		window.setGravity(Gravity.CENTER);

		// Call super onResume after sizing
		super.onResume();

	}
}
