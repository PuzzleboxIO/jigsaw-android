/**
 * Puzzlebox Jigsaw
 * Copyright 2015 Puzzlebox Productions, LLC
 * License: GNU Affero General Public License Version 3
 */

package io.puzzlebox.jigsaw.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import java.text.DecimalFormat;
import java.util.Arrays;

import io.puzzlebox.jigsaw.R;
import io.puzzlebox.jigsaw.data.SessionSingleton;

import static android.view.MenuItem.SHOW_AS_ACTION_ALWAYS;

public class SessionFragment extends Fragment {

	private static EditText editTextSessionProfile;
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

	public SessionFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_session, container, false);

		editTextSessionProfile = v.findViewById(R.id.editTextSessionProfile);

		if (SessionSingleton.getInstance().getSessionName() == null)
			SessionSingleton.getInstance().setSessionName(getString(R.string.session_profile));

		editTextSessionProfile.setText(SessionSingleton.getInstance().getSessionName());

		editTextSessionProfile.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				SessionSingleton.getInstance().setSessionName(editTextSessionProfile.getText().toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		textViewSessionTime = v.findViewById(R.id.textViewSessionTime);

		Button resetSession = v.findViewById(R.id.buttonResetSession);
		resetSession.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetSession();
			}
		});

		// setup the Session History plot
		sessionPlot1 = v.findViewById(R.id.sessionPlot1);
		sessionPlotSeries1 = new SimpleXYSeries("Session Plot");

		// Setup the boundary mode, boundary values only applicable in FIXED mode.

		if (sessionPlot1 != null) {

			sessionPlot1.setDomainBoundaries(0, 30, BoundaryMode.FIXED);
			sessionPlot1.setRangeBoundaries(0, 100, BoundaryMode.FIXED);

			sessionPlot1.addSeries(sessionPlotSeries1, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.RED, null, null));

			// Thin out domain and range tick values so they don't overlap
			sessionPlot1.setDomainStepValue(1);
			sessionPlot1.setTicksPerRangeLabel(10);

			sessionPlot1.setRangeLabel("");

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
		}

		// setup the Session History plot
		sessionPlot2 = v.findViewById(R.id.sessionPlot2);
		sessionPlotSeries2 = new SimpleXYSeries("Session Plot");

		// Setup the boundary mode, boundary values only applicable in FIXED mode.

		if (sessionPlot2 != null) {

			sessionPlot2.setDomainBoundaries(0, 30, BoundaryMode.FIXED);
			sessionPlot2.setRangeBoundaries(0, 100, BoundaryMode.FIXED);

			sessionPlot2.addSeries(sessionPlotSeries2, new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.RED, null, null));

			// Thin out domain and range tick values so they don't overlap
			sessionPlot2.setDomainStepValue(1);
			sessionPlot2.setTicksPerRangeLabel(10);

			sessionPlot2.setRangeLabel("");

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
		}
		return v;
	}

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
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		void onFragmentInteraction(Uri uri);
	}

	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(
				getActivity().getApplicationContext()).unregisterReceiver(
				mPacketReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		updateSessionTime();
		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				mPacketReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.packet"));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add("Share")
				.setOnMenuItemClickListener(this.mShareButtonClickListener)
				.setIcon(android.R.drawable.ic_menu_share)
				.setShowAsAction(SHOW_AS_ACTION_ALWAYS);

		super.onCreateOptionsMenu(menu, inflater);
	}

	MenuItem.OnMenuItemClickListener mShareButtonClickListener = new MenuItem.OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			Intent i = SessionSingleton.getInstance().getExportSessionIntent(getActivity().getApplicationContext());
			exportSession();
			return false;
		}
	};

	public void exportSession() {
		Intent i = SessionSingleton.getInstance().getExportSessionIntent(getActivity().getApplicationContext());
		if (i != null) {
			startActivity(i);
		} else {
			Toast.makeText(getActivity().getApplicationContext(), "Error export session data for sharing", Toast.LENGTH_SHORT).show();
		}
	}

	private void resetSession() {
		SessionSingleton.getInstance().resetSession();
		textViewSessionTime.setText( R.string.session_time );
		Toast.makeText((getActivity().getApplicationContext()),
				"Session data reset",
				Toast.LENGTH_SHORT).show();
	}

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

			sessionPlotSeries2 = updateSessionPlotHistory(
					  "Meditation",
					  SessionSingleton.getInstance().getSessionRangeValues(
								 "Meditation", 30),
					  Color.BLUE,
					  sessionPlot2,
					  sessionPlotSeries2);
		}

	};

	private void updateSessionTime() {
		textViewSessionTime.setText( SessionSingleton.getInstance().getSessionTimestamp() );
	}

	public SimpleXYSeries updateSessionPlotHistory(String name,
		                                     Number[] values,
		                                     Integer color,
		                                     XYPlot mPlot,
		                                     SimpleXYSeries mSeries) {
		if (mPlot != null) {
			mPlot.removeSeries(mSeries);

			mSeries = new SimpleXYSeries(Arrays.asList(values), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, name);

			LineAndPointFormatter format = new LineAndPointFormatter(color, color, null, null);

			mPlot.addSeries(mSeries, format);

			// Redraw the plots:
			mPlot.redraw();

			return mSeries;
		} else
			return null;
	}
}
