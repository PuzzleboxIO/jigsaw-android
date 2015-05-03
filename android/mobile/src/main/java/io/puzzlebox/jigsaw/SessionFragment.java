package io.puzzlebox.jigsaw;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import java.text.DecimalFormat;
import java.util.Arrays;

import io.puzzlebox.jigsaw.data.SessionSingleton;

import static android.view.MenuItem.SHOW_AS_ACTION_ALWAYS;

public class SessionFragment extends Fragment {

	private final static String TAG = SessionFragment.class.getSimpleName();

	private static TextView textViewSessionTime;

	private static XYPlot sessionPlot1 = null;
	private static SimpleXYSeries sessionPlotSeries1 = null;
	private static XYPlot sessionPlot2 = null;
	private static SimpleXYSeries sessionPlotSeries2 = null;


	private OnFragmentInteractionListener mListener;

	public static SessionFragment newInstance() {
		SessionFragment fragment = new SessionFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}


	// ################################################################

	public SessionFragment() {
		// Required empty public constructor
	}


	// ################################################################

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}


	// ################################################################

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_session, container, false);

		textViewSessionTime = (TextView) v.findViewById(R.id.textViewSessionTime);

//		Button exportToCSV = (Button) v.findViewById(R.id.buttonExportCSV);
//		exportToCSV.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Log.d(TAG, "SessionSingleton.getInstance().exportDataToCSV");
//				SessionSingleton.getInstance().exportDataToCSV(null, null);
//			}
//		});

		Button resetSession = (Button) v.findViewById(R.id.buttonResetSession);
		resetSession.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				resetSession();

			}
		});


		// setup the Session History plot
		sessionPlot1 = (XYPlot) v.findViewById(R.id.sessionPlot1);
		sessionPlotSeries1 = new SimpleXYSeries("Session Plot");

		// Setup the boundary mode, boundary values only applicable in FIXED mode.

		if (sessionPlot1 != null) {

			sessionPlot1.setDomainBoundaries(0, 30, BoundaryMode.FIXED);
//			sessionPlot1.setRangeBoundaries(0, 100, BoundaryMode.GROW);
			sessionPlot1.setRangeBoundaries(0, 100, BoundaryMode.FIXED);

//			sessionPlot1.addSeries(sessionPlotSeries1, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.BLACK, null, null));
			sessionPlot1.addSeries(sessionPlotSeries1, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.RED, null, null));

			// Thin out domain and range tick values so they don't overlap
			sessionPlot1.setDomainStepValue(1);
			sessionPlot1.setTicksPerRangeLabel(10);

			sessionPlot1.setRangeLabel("Attention");

			// Sets the dimensions of the widget to exactly contain the text contents
			sessionPlot1.getDomainLabelWidget().pack();
			sessionPlot1.getRangeLabelWidget().pack();

			// Only display whole numbers in labels
			sessionPlot1.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
			sessionPlot1.getGraphWidget().setRangeValueFormat(new DecimalFormat("0"));

			// Hide domain and range labels
			sessionPlot1.getGraphWidget().setDomainLabelWidth(0);
			sessionPlot1.getGraphWidget().setRangeLabelWidth(0);

			// Hide legend
			sessionPlot1.getLegendWidget().setVisible(false);

			// setGridPadding(float left, float top, float right, float bottom)
			sessionPlot1.getGraphWidget().setGridPadding(0, 0, 0, 0);


			//		sessionPlot1.getGraphWidget().setDrawMarkersEnabled(false);

			//		final PlotStatistics histStats = new PlotStatistics(1000, false);
			//		sessionPlot1.addListener(histStats);

		}



		// setup the Session History plot
		sessionPlot2 = (XYPlot) v.findViewById(R.id.sessionPlot2);
		sessionPlotSeries2 = new SimpleXYSeries("Session Plot");

		// Setup the boundary mode, boundary values only applicable in FIXED mode.

		if (sessionPlot2 != null) {

			sessionPlot2.setDomainBoundaries(0, 30, BoundaryMode.FIXED);
//			sessionPlot2.setRangeBoundaries(0, 100, BoundaryMode.GROW);
			sessionPlot2.setRangeBoundaries(0, 100, BoundaryMode.FIXED);

//			sessionPlot2.addSeries(sessionPlotSeries2, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.BLACK, null, null));
			sessionPlot2.addSeries(sessionPlotSeries2, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.RED, null, null));

			// Thin out domain and range tick values so they don't overlap
			sessionPlot2.setDomainStepValue(1);
			sessionPlot2.setTicksPerRangeLabel(10);

			sessionPlot2.setRangeLabel("Meditation");

			// Sets the dimensions of the widget to exactly contain the text contents
			sessionPlot2.getDomainLabelWidget().pack();
			sessionPlot2.getRangeLabelWidget().pack();

			// Only display whole numbers in labels
			sessionPlot2.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
			sessionPlot2.getGraphWidget().setRangeValueFormat(new DecimalFormat("0"));

			// Hide domain and range labels
			sessionPlot2.getGraphWidget().setDomainLabelWidth(0);
			sessionPlot2.getGraphWidget().setRangeLabelWidth(0);

			// Hide legend
			sessionPlot2.getLegendWidget().setVisible(false);

			// setGridPadding(float left, float top, float right, float bottom)
			sessionPlot2.getGraphWidget().setGridPadding(0, 0, 0, 0);


			//		sessionPlot2.getGraphWidget().setDrawMarkersEnabled(false);

			//		final PlotStatistics histStats = new PlotStatistics(1000, false);
			//		sessionPlot2.addListener(histStats);

		}



		return v;

	}


	// ################################################################

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}


	// ################################################################

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}


	// ################################################################

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		public void onFragmentInteraction(Uri uri);
	}


	// ################################################################

	public void onPause() {

		Log.v(TAG, "onPause()");

		super.onPause();

		LocalBroadcastManager.getInstance(
				getActivity().getApplicationContext()).unregisterReceiver(
				mPacketReceiver);

	} // onPause


	// ################################################################

	@Override
	public void onResume() {

		Log.v(TAG, "onResume()");

		super.onResume();

		updateSessionTime();

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				mPacketReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.packet"));

	}


	// ################################################################

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		menu.add("Share")
				.setOnMenuItemClickListener(this.mShareButtonClickListener)
				.setIcon(android.R.drawable.ic_menu_share)
				.setShowAsAction(SHOW_AS_ACTION_ALWAYS);

		super.onCreateOptionsMenu(menu, inflater);

	}


	// ################################################################

	MenuItem.OnMenuItemClickListener mShareButtonClickListener = new MenuItem.OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {

			Intent i = SessionSingleton.getInstance().getExportSessionIntent(getActivity().getApplicationContext(), item);

			if (i != null) {
				startActivity(i);
			} else {
				Toast.makeText(getActivity().getApplicationContext(), "Error export session data for sharing", Toast.LENGTH_SHORT).show();
			}

			return false;
		}
	};


	// ################################################################

	private void resetSession() {

		Log.d(TAG, "SessionSingleton.getInstance().resetSession()");
		SessionSingleton.getInstance().resetSession();

		textViewSessionTime.setText( R.string.session_time );

		Toast.makeText((getActivity().getApplicationContext()),
				"Session data reset",
				Toast.LENGTH_SHORT).show();

	}


	// ################################################################

	private BroadcastReceiver mPacketReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			updateSessionTime();

			sessionPlotSeries1 = updateSessionPlotHistory(
					  "Attention",
					  SessionSingleton.getInstance().getSessionRangeValues(
								 "Attention", 30),
					  Color.RED,
					  sessionPlot1,
					  sessionPlotSeries1);
//			updateSessionPlotHistory(
//					  SessionSingleton.getInstance().getSessionRangeValues(
//								 "Attention", 30));

//			updateSessionPlotHistory2(
//					  SessionSingleton.getInstance().getSessionRangeValues(
//								 "Meditation", 30));

			sessionPlotSeries2 = updateSessionPlotHistory(
					  "Meditation",
					  SessionSingleton.getInstance().getSessionRangeValues(
								 "Meditation", 30),
					  Color.BLUE,
					  sessionPlot2,
					  sessionPlotSeries2);


		}

	};


	// ################################################################

	private void updateSessionTime() {

		textViewSessionTime.setText( SessionSingleton.getInstance().getSessionTimestamp() );

	}


	// ################################################################

//	public void updateSessionPlotHistory(Number[] values) {
		public SimpleXYSeries updateSessionPlotHistory(String name,
		                                     Number[] values,
		                                     Integer color,
		                                     XYPlot mPlot,
		                                     SimpleXYSeries mSeries) {

//		if (sessionPlot1 != null) {
//			sessionPlot1.removeSeries(sessionPlotSeries1);

		if (mPlot != null) {
			mPlot.removeSeries(mSeries);

			mSeries = new SimpleXYSeries(Arrays.asList(values), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, name);

			LineAndPointFormatter format = new LineAndPointFormatter(color, color, null, null);

			//		format.getFillPaint().setAlpha(220);

			mPlot.addSeries(mSeries, format);


			// Redraw the plots:
			mPlot.redraw();

			return mSeries;
		} else
			return null;

	} // updateSessionPlotHistory



}
