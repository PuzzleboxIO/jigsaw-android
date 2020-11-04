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
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
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

// TODO 2017-02-15 Disable Muse
//import io.puzzlebox.jigsaw.service.InteraXonMuseService;

import io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService;

import static android.view.MenuItem.SHOW_AS_ACTION_ALWAYS;

public class EEGFragment extends Fragment implements
		SeekBar.OnSeekBarChangeListener {

	/**
	 * TODO
	 *
	 * - Progress Bars colors no longer edge-to-edge
	 * - Power calculation not appearing in exported CSV files
	 */

	private final static String TAG = EEGFragment.class.getSimpleName();

	private static OnFragmentInteractionListener mListener;

	private static View v;

	/**
	 * Configuration
	 */
	private static final int[] thresholdValuesAttention = new int[101];
	private static final int[] thresholdValuesMeditation = new int[101];
	private static final int minimumPower = 0; // minimum power for the action
	private static final int maximumPower = 100; // maximum power for the action
	private static final boolean enableMuse = false;

	/**
	 * UI
	 */
	private static ProgressBar progressBarAttention;
	private static SeekBar seekBarAttention;
	private static ProgressBar progressBarMeditation;
	private static SeekBar seekBarMeditation;
	private static ProgressBar progressBarSignal;
	private static ProgressBar progressBarPower;
	private static ProgressBar progressBarBlink;

	private static Spinner spinnerEEG;

	private static TextView textViewSessionTime;

	private static XYPlot eegRawHistoryPlot = null;
	private static SimpleXYSeries eegRawHistorySeries = null;

	private static Intent intentThinkGear;
	private static Intent intentMuse;

	public static EEGFragment newInstance() {
		EEGFragment fragment = new EEGFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public EEGFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.fragment_eeg, container, false);

		progressBarAttention = v.findViewById(R.id.progressBarAttention);
		final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
		ShapeDrawable progressBarAttentionDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarAttentionColor = "#FF0000";
		progressBarAttentionDrawable.getPaint().setColor(Color.parseColor(progressBarAttentionColor));
		ClipDrawable progressAttention = new ClipDrawable(progressBarAttentionDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarAttention.setProgressDrawable(progressAttention);
		progressBarAttention.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarMeditation = v.findViewById(R.id.progressBarMeditation);
		ShapeDrawable progressBarMeditationDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarMeditationColor = "#0000FF";
		progressBarMeditationDrawable.getPaint().setColor(Color.parseColor(progressBarMeditationColor));
		ClipDrawable progressMeditation = new ClipDrawable(progressBarMeditationDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarMeditation.setProgressDrawable(progressMeditation);
		progressBarMeditation.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarSignal = v.findViewById(R.id.progressBarSignal);
		ShapeDrawable progressBarSignalDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarSignalColor = "#00FF00";
		progressBarSignalDrawable.getPaint().setColor(Color.parseColor(progressBarSignalColor));
		ClipDrawable progressSignal = new ClipDrawable(progressBarSignalDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarSignal.setProgressDrawable(progressSignal);
		progressBarSignal.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarPower = v.findViewById(R.id.progressBarPower);
		ShapeDrawable progressBarPowerDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarPowerColor = "#FFFF00";
		progressBarPowerDrawable.getPaint().setColor(Color.parseColor(progressBarPowerColor));
		ClipDrawable progressPower = new ClipDrawable(progressBarPowerDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarPower.setProgressDrawable(progressPower);
		progressBarPower.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarBlink = v.findViewById(R.id.progressBarBlink);
		ShapeDrawable progressBarRangeDrawable = new ShapeDrawable();
		String progressBarRangeColor = "#BBBBBB";
		progressBarRangeDrawable.getPaint().setColor(Color.parseColor(progressBarRangeColor));
		ClipDrawable progressRange = new ClipDrawable(progressBarRangeDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarBlink.setProgressDrawable(progressRange);
		progressBarBlink.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarBlink.setMax(NeuroSkyThinkGearService.blinkRangeMax);

		// setup the Raw EEG History plot
		eegRawHistoryPlot = v.findViewById(R.id.eegRawHistoryPlot);
		eegRawHistorySeries = new SimpleXYSeries("");

		if (eegRawHistoryPlot != null) {
			eegRawHistoryPlot.setDomainBoundaries(0, NeuroSkyThinkGearService.EEG_RAW_FREQUENCY, BoundaryMode.FIXED);
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

		seekBarAttention = v.findViewById(R.id.seekBarAttention);
		seekBarAttention.setOnSeekBarChangeListener(this);
		seekBarMeditation = v.findViewById(R.id.seekBarMeditation);
		seekBarMeditation.setOnSeekBarChangeListener(this);

		String[] items = new String[] {"NeuroSky MindWave Mobile", "Emotiv Insight", "InteraXon Muse"};

//		if (NeuroSkyNeuroSkyThinkGearService.eegConnected || NeuroSkyNeuroSkyThinkGearService.eegConnecting)
//			items = new String[] {"NeuroSky MindWave Mobile", "Emotiv Insight", "InteraXon Muse"};

		// TODO 2017-02-15 Disable Muse
//		if (InteraXonMuseService.eegConnected || InteraXonMuseService.eegConnecting)
//			items = new String[] {"InteraXon Muse", "Emotiv Insight", "NeuroSky MindWave Mobile"};

		spinnerEEG = v.findViewById(R.id.spinnerEEG);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
				R.layout.spinner_item, items);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerEEG.setAdapter(adapter);

		if (android.os.Build.VERSION.SDK_INT >= 16)
			spinnerEEG.setPopupBackgroundDrawable(new ColorDrawable(Color.DKGRAY));

		textViewSessionTime = v.findViewById(R.id.textViewSessionTime);

		Button connectEEG = v.findViewById(R.id.buttonConnectEEG);
		connectEEG.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				connectHeadset();
			}
		});

		if (NeuroSkyThinkGearService.eegConnected ) {
			connectEEG.setText("Disconnect EEG");
			spinnerEEG.setEnabled(false);
		}

		// TODO 2017-02-15 Disable Muse
//		if (InteraXonMuseService.eegConnected) {
//			buttonConnectEEG.setText("Disconnect EEG");
////			spinnerEEG.setSelection(spinnerEEG.getPosition(DEFAULT_CURRENCY_TYPE));
////			spinnerEEG.setSelection(spinnerEEG.getAdapter(). .getPosition(DEFAULT_CURRENCY_TYPE));
//			spinnerEEG.setEnabled(false);
//		}

		Button resetSession = v.findViewById(R.id.buttonResetSession);
		resetSession.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetSession();
			}
		});

		intentThinkGear = new Intent(getActivity(), NeuroSkyThinkGearService.class);

		// TODO 2017-02-15 Disable Muse
//		intentMuse = new Intent(getActivity(), InteraXonMuseService.class);

		/**
		 * Update settings according to default UI
		 */
		updateScreenLayout();

		updatePowerThresholds();
		updatePower();

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

		LocalBroadcastManager.getInstance(
				getActivity().getApplicationContext()).unregisterReceiver(
				mEventReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		updateSessionTime();

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				mPacketReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.packet"));

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				mEventReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.event"));
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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

			if (i != null) {
				startActivity(i);
			} else {
				Toast.makeText(getActivity().getApplicationContext(), "Error export session data for sharing", Toast.LENGTH_SHORT).show();
			}

			return false;
		}
	};

	public void updateScreenLayout() {
//		switch(config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK){
//			case Configuration.SCREENLAYOUT_SIZE_SMALL:
//				Log.v(TAG, "screenLayout: small");
//				updateScreenLayoutSmall();
//				break;
//			case Configuration.SCREENLAYOUT_SIZE_NORMAL:
//				Log.v(TAG, "screenLayout: normal");
//				updateScreenLayoutSmall();
//				break;
//			case Configuration.SCREENLAYOUT_SIZE_LARGE:
//				Log.v(TAG, "screenLayout: large");
//				break;
//			case Configuration.SCREENLAYOUT_SIZE_XLARGE:
//				Log.v(TAG, "screenLayout: xlarge");
//				break;
//			case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:
//				Log.v(TAG, "screenLayout: undefined");
//				updateScreenLayoutSmall();
//				break;
//		}
	}

	public void setButtonText(int buttonId, String text) {
		/**
		 * Shortcut for changing the text on a button
		 */
		Button button = v.findViewById(buttonId);
		button.setText(text);
	}

	public void updateStatusImage() {
//		if(DEBUG) {
//			Log.v(TAG, (new StringBuilder("Attention: ")).append(eegAttention).toString());
//			Log.v(TAG, (new StringBuilder("Meditation: ")).append(eegMeditation).toString());
//			Log.v(TAG, (new StringBuilder("Power: ")).append(eegPower).toString());
//			Log.v(TAG, (new StringBuilder("Signal: ")).append(eegSignal).toString());
//			Log.v(TAG, (new StringBuilder("Connecting: ")).append(eegConnecting).toString());
//			Log.v(TAG, (new StringBuilder("Connected: ")).append(eegConnected).toString());
//		}
//
//		if(eegPower > 0) {
//			imageViewStatus.setImageResource(R.drawable.status_4_active);
//			return;
//		}
//
//		if(eegSignal > 90) {
//			imageViewStatus.setImageResource(R.drawable.status_3_processing);
//			return;
//		}
//
//		if(eegConnected) {
//			imageViewStatus.setImageResource(R.drawable.status_2_connected);
//			return;
//		}
//
//		if(eegConnecting) {
//			imageViewStatus.setImageResource(R.drawable.status_1_connecting);
//			return;
//		} else {
//			imageViewStatus.setImageResource(R.drawable.status_default);
//			return;
//		}
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
		updatePowerThresholds();
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		/**
		 * Method required by SeekBar.OnSeekBarChangeListener
		 */
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	public void connectHeadset() {
		/**
		 * Called when the "Connect" button is pressed
		 */
		switch (String.valueOf(spinnerEEG.getSelectedItem())) {

			case "NeuroSky MindWave Mobile":
				if (! NeuroSkyThinkGearService.eegConnected) {
					getActivity().startService(intentThinkGear);
				} else {
					disconnectHeadset();
				}
				break;

			case "Emotiv Insight":
				Toast.makeText(getActivity().getApplicationContext(), "Emotiv Insight support only available in developer edition", Toast.LENGTH_SHORT).show();
				break;

			// TODO 2017-02-15 Disable Muse
//			case "InteraXon Muse":
//
//				if (enableMuse) {
//					if (!InteraXonMuseService.eegConnected) {
//						getActivity().startService(intentMuse);
//					} else {
//						disconnectHeadset();
//					}
//				} else {
//					Toast.makeText(getActivity().getApplicationContext(), "InteraXon Muse support only available in developer edition", Toast.LENGTH_SHORT).show();
//				}
//
//				break;
		}
	} // connectHeadset

	public void disconnectHeadset() {
		/**
		 * Called when "Disconnect" button is pressed
		 */
		switch (String.valueOf(spinnerEEG.getSelectedItem())) {

			case "NeuroSky MindWave Mobile":
				NeuroSkyThinkGearService.disconnectHeadset();
				getActivity().stopService(intentThinkGear);
				break;

			case "Emotiv Insight":
				Toast.makeText(getActivity().getApplicationContext(), "Emotiv Insight support only available in developer edition", Toast.LENGTH_SHORT).show();
				break;

			// TODO 2017-02-15 Disable Muse
//			case "InteraXon Muse":
//				if (enableMuse) {
//					InteraXonMuseService.disconnectHeadset();
//					getActivity().stopService(intentMuse);
//				} else {
//					Toast.makeText(getActivity().getApplicationContext(), "InteraXon Muse support only available in developer edition", Toast.LENGTH_SHORT).show();
//				}
//
//				break;
		}

		updateStatusImage();

		progressBarAttention.setProgress(0);
		progressBarMeditation.setProgress(0);
		progressBarSignal.setProgress(0);
		progressBarPower.setProgress(0);

		spinnerEEG.setEnabled(true);
	}

	public void updatePowerThresholds() {

		/**
		 * The "Power" level refers to the Puzzlebox Orbit helicopter's
		 * throttle setting. Typically this is an "off" or "on" state,
		 * meaning the helicopter is either flying or not flying at all.
		 * However this method could be used to increase the throttle
		 * or perhaps the forward motion of the helicopter to a level
		 * proportionate to how far past their target brainwave levels
		 * are set (via the progress bar sliders).
		 */

		int power;
		int attentionSeekValue;
		int meditationSeekValue;
		float percentOfMaxPower;

		// Reset all values to zero
		for (int i = 0; i < thresholdValuesAttention.length; i++) {
			thresholdValuesAttention[i] = 0;
			thresholdValuesMeditation[i] = 0;
		}

		attentionSeekValue = seekBarAttention.getProgress();
		if (attentionSeekValue > 0) {
			for (int i = attentionSeekValue; i < thresholdValuesAttention.length; i++) {

				/**
				 *  Slider @ 70
				 *
				 * Attention @ 70
				 * Percentage = 0% ((100-70) - (100-70)) / (100-70)
				 * Power = 60 (minimumPower)
				 *
				 * Slider @ 70
				 * Attention @ 80
				 * Percentage = 33% ((100-70) - (100-80)) / (100-70)
				 * Power = 73
				 *
				 * Slider @ 70
				 * Attention @ 90
				 * Percentage = 66% ((100-70) - (100-90)) / (100-70)
				 * Power = 86
				 *
				 * Slider @ 70
				 * Attention @ 100
				 * Percentage = 100% ((100-70) - (100-100)) / (100-70)
				 * Power = 100
				 */

				percentOfMaxPower = ( ((100 - attentionSeekValue) - (100 - i)) / (float)(100 - attentionSeekValue) );
				power = thresholdValuesAttention[i] + (int)( minimumPower + ((maximumPower - minimumPower) * percentOfMaxPower) );
				thresholdValuesAttention[i] = power;
			}
		}

		meditationSeekValue = seekBarMeditation.getProgress();
		if (meditationSeekValue > 0) {
			for (int i = meditationSeekValue; i < thresholdValuesMeditation.length; i++) {
				percentOfMaxPower = ( ((100 - meditationSeekValue) - (100 - i)) / (float)(100 - meditationSeekValue) );
				power = thresholdValuesMeditation[i] + (int)( minimumPower + ((maximumPower - minimumPower) * percentOfMaxPower) );
				thresholdValuesMeditation[i] = power;
			}
		}
	}

	public int calculateSpeed() {

		/**
		 * This method is used for calculating whether
		 * or not the "Attention" or "Meditation" levels
		 * are sufficient to trigger the helicopter throttle
		 */

		int attention = progressBarAttention.getProgress();
		int meditation = progressBarMeditation.getProgress();
		int attentionSeekValue = seekBarAttention.getProgress();
		int meditationSeekValue = seekBarMeditation.getProgress();

		int speed = 0;

		if (attention > attentionSeekValue)
			speed = thresholdValuesAttention[attention];
		if (meditation > meditationSeekValue)
			speed = speed + thresholdValuesMeditation[meditation];

		if (speed > maximumPower)
			speed = maximumPower;
		if (speed < minimumPower)
			speed = 0;

		return(speed);

	}

	public void updatePower() {

		/**
		 * This method updates the power level of the
		 * "Throttle" and triggers the audio stream
		 * which is used to fly the helicopter
		 */

		if (NeuroSkyThinkGearService.eegConnected) {

			if (NeuroSkyThinkGearService.eegSignal < 100) {
				NeuroSkyThinkGearService.eegAttention = 0;
				NeuroSkyThinkGearService.eegMeditation = 0;
				progressBarAttention.setProgress(NeuroSkyThinkGearService.eegAttention);
				progressBarMeditation.setProgress(NeuroSkyThinkGearService.eegMeditation);
			}

			NeuroSkyThinkGearService.eegPower = calculateSpeed();

			progressBarPower.setProgress(NeuroSkyThinkGearService.eegPower);


		}

		// TODO 2017-02-15 Disable Muse
//		if (InteraXonMuseService.eegConnected) {
//
////			Log.d(TAG, "InteraXonMuseService.eegConnected: eegSignal: " + InteraXonMuseService.eegSignal);
////			if (InteraXonMuseService.eegSignal < 100) {
////				InteraXonMuseService.eegConcentration = 0;
////				InteraXonMuseService.eegMellow = 0;
////				progressBarAttention.setProgress(InteraXonMuseService.eegConcentration);
////				progressBarMeditation.setProgress(InteraXonMuseService.eegMellow);
////			}
//
//			InteraXonMuseService.eegPower = calculateSpeed();
//
//			progressBarPower.setProgress(InteraXonMuseService.eegPower);
//
//
//		}
	}

	public void hideEEGRawHistory() {
		if (eegRawHistoryPlot != null)
			eegRawHistoryPlot.setVisibility(View.GONE);
	}

	public void updateEEGRawHistory(Number[] rawEEG) {

		if (eegRawHistoryPlot != null) {
			eegRawHistoryPlot.removeSeries(eegRawHistorySeries);

			eegRawHistorySeries = new SimpleXYSeries(Arrays.asList(rawEEG), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Raw EEG");

			LineAndPointFormatter format = new LineAndPointFormatter(Color.rgb(0, 0, 0), Color.TRANSPARENT, null, null);

			eegRawHistoryPlot.addSeries(eegRawHistorySeries, format);

			// redraw the Plots:
			eegRawHistoryPlot.redraw();
		}
	}

	private void resetSession() {
		SessionSingleton.getInstance().resetSession();
		textViewSessionTime.setText( R.string.session_time );
		Toast.makeText((getActivity().getApplicationContext()),
				"Session data reset",
				Toast.LENGTH_SHORT).show();
	}

	private final BroadcastReceiver mPacketReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			int eegAttention = Integer.valueOf(intent.getStringExtra("Attention"));
			int eegMeditation = Integer.valueOf(intent.getStringExtra("Meditation"));
			int eegSignal = Integer.valueOf(intent.getStringExtra("Signal Level"));

			progressBarAttention.setProgress(eegAttention);
			progressBarMeditation.setProgress(eegMeditation);
			progressBarSignal.setProgress(eegSignal);

			updatePower();

			progressBarBlink.setProgress(0);

			updateEEGRawHistory(SessionSingleton.getInstance().getCurrentRawEEG());

			updateSessionTime();

			updateStatusImage();
		}

	};

	private void updateSessionTime() {
		textViewSessionTime.setText( SessionSingleton.getInstance().getSessionTimestamp() );
	}

	private final BroadcastReceiver mEventReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String name = intent.getStringExtra("name");
			String value = intent.getStringExtra("value");
			switch(name) {

				case "eegStatus":

					switch(value) {
						case "STATE_CONNECTING":
							updateStatusImage();
							setButtonText(R.id.buttonConnectEEG, "Connecting");
							spinnerEEG.setEnabled(false);
							break;
						case "STATE_CONNECTED":
							updateStatusImage();
							setButtonText(R.id.buttonConnectEEG, "Disconnect EEG");
							spinnerEEG.setEnabled(false);
							break;
						case "STATE_NOT_FOUND":
							Toast.makeText(context, "EEG Not Found", Toast.LENGTH_SHORT).show();
							updateStatusImage();
							setButtonText(R.id.buttonConnectEEG, "Connect EEG");
							spinnerEEG.setEnabled(true);
							break;
						case "STATE_NOT_PAIRED":
							Toast.makeText(context, "EEG Not Paired", Toast.LENGTH_SHORT).show();
							updateStatusImage();
							setButtonText(R.id.buttonConnectEEG, "Connect EEG");
							break;
						case "STATE_DISCONNECTED":
							updateStatusImage();
							setButtonText(R.id.buttonConnectEEG, "Connect EEG");
							spinnerEEG.setEnabled(true);
							break;
						case "MSG_LOW_BATTERY":
							Toast.makeText(context, "EEG Battery Low", Toast.LENGTH_SHORT).show();
							updateStatusImage();
							break;
					}

					break;

				case "eegBlink":
					Log.d(TAG, "Blink: " + value + "\n");
					try {
						progressBarBlink.setProgress(Integer.parseInt(value));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					break;
			}
		}

	};
}
