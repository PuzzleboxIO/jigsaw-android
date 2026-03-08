/**
 * Puzzlebox Jigsaw
 * Copyright 2015 Puzzlebox Productions, LLC
 * License: GNU Affero General Public License Version 3
 */

package io.puzzlebox.jigsaw.ui;

import android.annotation.SuppressLint;
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
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
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

import io.puzzlebox.jigsaw.data.NeuroSkyEegState;

import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import androidx.core.content.ContextCompat;

public class EEGFragment extends Fragment implements
		SeekBar.OnSeekBarChangeListener {

	/**
	 * TODO
	 * - Progress Bars colors no longer edge-to-edge
	 * - Power calculation not appearing in exported CSV files
	 */

	private final static String TAG = EEGFragment.class.getSimpleName();

	private View v;

	/**
	 * Configuration
	 */
	private static final int[] thresholdValuesAttention = new int[101];
	private static final int[] thresholdValuesMeditation = new int[101];
	private static final int minimumPower = 0; // minimum power for the action
	private static final int maximumPower = 100; // maximum power for the action
	/**
	 * UI
	 */
	private ProgressBar progressBarAttention;
	private SeekBar seekBarAttention;
	private ProgressBar progressBarMeditation;
	private SeekBar seekBarMeditation;
	private ProgressBar progressBarSignal;
	private ProgressBar progressBarPower;
	private ProgressBar progressBarBlink;

	private Spinner spinnerEEG;

	private TextView textViewSessionTime;

	private XYPlot eegRawHistoryPlot = null;
	private SimpleXYSeries eegRawHistorySeries = null;

	private Intent intentThinkGear;

	public EEGFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@SuppressLint("SourceLockedOrientationActivity")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.fragment_eeg, container, false);

		progressBarAttention = v.findViewById(R.id.progressBarAttention);
		final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
		ShapeDrawable progressBarAttentionDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarAttentionColor = "#FF0000";
		progressBarAttentionDrawable.getPaint().setColor(Color.parseColor(progressBarAttentionColor));
		ClipDrawable progressAttention = new ClipDrawable(progressBarAttentionDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
		progressBarAttention.setProgressDrawable(progressAttention);
		progressBarAttention.setBackground(ResourcesCompat.getDrawable(getResources(), android.R.drawable.progress_horizontal, null));

		progressBarMeditation = v.findViewById(R.id.progressBarMeditation);
		ShapeDrawable progressBarMeditationDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarMeditationColor = "#0000FF";
		progressBarMeditationDrawable.getPaint().setColor(Color.parseColor(progressBarMeditationColor));
		ClipDrawable progressMeditation = new ClipDrawable(progressBarMeditationDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
		progressBarMeditation.setProgressDrawable(progressMeditation);
		progressBarMeditation.setBackground(ResourcesCompat.getDrawable(getResources(), android.R.drawable.progress_horizontal, null));

		progressBarSignal = v.findViewById(R.id.progressBarSignal);
		ShapeDrawable progressBarSignalDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarSignalColor = "#00FF00";
		progressBarSignalDrawable.getPaint().setColor(Color.parseColor(progressBarSignalColor));
		ClipDrawable progressSignal = new ClipDrawable(progressBarSignalDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
		progressBarSignal.setProgressDrawable(progressSignal);
		progressBarSignal.setBackground(ResourcesCompat.getDrawable(getResources(), android.R.drawable.progress_horizontal, null));

		progressBarPower = v.findViewById(R.id.progressBarPower);
		ShapeDrawable progressBarPowerDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarPowerColor = "#FFFF00";
		progressBarPowerDrawable.getPaint().setColor(Color.parseColor(progressBarPowerColor));
		ClipDrawable progressPower = new ClipDrawable(progressBarPowerDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
		progressBarPower.setProgressDrawable(progressPower);
		progressBarPower.setBackground(ResourcesCompat.getDrawable(getResources(), android.R.drawable.progress_horizontal, null));

		progressBarBlink = v.findViewById(R.id.progressBarBlink);
		ShapeDrawable progressBarRangeDrawable = new ShapeDrawable();
		String progressBarRangeColor = "#BBBBBB";
		progressBarRangeDrawable.getPaint().setColor(Color.parseColor(progressBarRangeColor));
		ClipDrawable progressRange = new ClipDrawable(progressBarRangeDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
		progressBarBlink.setProgressDrawable(progressRange);
		progressBarBlink.setBackground(ResourcesCompat.getDrawable(getResources(), android.R.drawable.progress_horizontal, null));

		progressBarBlink.setMax(NeuroSkyEegState.blinkRangeMax);

		// Set up the Raw EEG History plot
		eegRawHistoryPlot = v.findViewById(R.id.eegRawHistoryPlot);
		eegRawHistorySeries = new SimpleXYSeries("");

		if (eegRawHistoryPlot != null) {
			eegRawHistoryPlot.setDomainBoundaries(0, NeuroSkyEegState.EEG_RAW_FREQUENCY, BoundaryMode.FIXED);
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

		// TODO 2017-02-15 Disable Muse
//		if (InteraXonMuseService.eegConnected || InteraXonMuseService.eegConnecting)
//			items = new String[] {"InteraXon Muse", "Emotiv Insight", "NeuroSky MindWave Mobile"};

		spinnerEEG = v.findViewById(R.id.spinnerEEG);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity().getApplicationContext(),
				R.layout.spinner_item, items);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerEEG.setAdapter(adapter);

		spinnerEEG.setPopupBackgroundDrawable(new ColorDrawable(Color.DKGRAY));

		textViewSessionTime = v.findViewById(R.id.textViewSessionTime);

		Button connectEEG = v.findViewById(R.id.buttonConnectEEG);
		connectEEG.setOnClickListener(v -> connectHeadset());

		if (NeuroSkyEegState.eegConnected ) {
			connectEEG.setText(R.string.buttonDisconnectEEG);
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
		resetSession.setOnClickListener(v -> resetSession());

		// Instantiate the Intent via reflection so this file compiles even when
		// NeuroSkyThinkGearService is excluded (NeuroSky SDK absent from build).
		try {
			intentThinkGear = new Intent(requireActivity(),
					Class.forName("io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService"));
		} catch (ClassNotFoundException e) {
			intentThinkGear = null; // NeuroSky SDK not available in this build
		}

		// TODO 2017-02-15 Disable Muse
//		intentMuse = new Intent(getActivity(), InteraXonMuseService.class);

		/*
		 * Update settings according to default UI
		 */
		updateScreenLayout();

		updatePowerThresholds();
		updatePower();

		requireActivity().addMenuProvider(new MenuProvider() {
			@Override
			public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
				menu.add("Share")
						.setOnMenuItemClickListener(mShareButtonClickListener)
						.setIcon(android.R.drawable.ic_menu_share)
						.setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
			}

			@Override
			public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
				return false;
			}
		}, getViewLifecycleOwner());

		return v;
	}

	@Override
	public void onPause() {
		super.onPause();

		requireActivity().getApplicationContext().unregisterReceiver(
				mPacketReceiver);

		requireActivity().getApplicationContext().unregisterReceiver(
				mEventReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		updateSessionTime();

		ContextCompat.registerReceiver(requireActivity().getApplicationContext(), mPacketReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.packet"), ContextCompat.RECEIVER_NOT_EXPORTED);

		ContextCompat.registerReceiver(requireActivity().getApplicationContext(), mEventReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.event"), ContextCompat.RECEIVER_NOT_EXPORTED);
	}

	final MenuItem.OnMenuItemClickListener mShareButtonClickListener = item -> {
		Intent i = SessionSingleton.getInstance().getExportSessionIntent(requireActivity().getApplicationContext());

		if (i != null) {
			startActivity(i);
		} else {
			Toast.makeText(requireActivity().getApplicationContext(), "Error export session data for sharing", Toast.LENGTH_SHORT).show();
		}

		return false;
	};

	@SuppressWarnings("EmptyMethod")
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
		/*
		 * Shortcut for changing the text on a button
		 */
		Button button = v.findViewById(buttonId);
		button.setText(text);
	}

	@SuppressWarnings("EmptyMethod")
	public void updateStatusImage() {
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
		updatePowerThresholds();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		/*
		 * Method required by SeekBar.OnSeekBarChangeListener
		 */
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	public void connectHeadset() {
		/*
		 * Called when the "Connect" button is pressed
		 */
		switch (String.valueOf(spinnerEEG.getSelectedItem())) {

			case "NeuroSky MindWave Mobile":
				if (intentThinkGear == null) {
					Toast.makeText(requireActivity(), "NeuroSky SDK not available in this build", Toast.LENGTH_SHORT).show();
				} else if (!NeuroSkyEegState.eegConnected) {
					requireActivity().startService(intentThinkGear);
				} else {
					disconnectHeadset();
				}
				break;

			case "Emotiv Insight":
				Toast.makeText(requireActivity().getApplicationContext(), "Emotiv Insight support only available in developer edition", Toast.LENGTH_SHORT).show();
				break;

			}
	} // connectHeadset

	public void disconnectHeadset() {
		/*
		 * Called when "Disconnect" button is pressed
		 */
		switch (String.valueOf(spinnerEEG.getSelectedItem())) {

			case "NeuroSky MindWave Mobile":
				NeuroSkyEegState.disconnectIfConnected();
				if (intentThinkGear != null) requireActivity().stopService(intentThinkGear);
				break;

			case "Emotiv Insight":
				Toast.makeText(requireActivity().getApplicationContext(), "Emotiv Insight support only available in developer edition", Toast.LENGTH_SHORT).show();
				break;

			}

		updateStatusImage();

		progressBarAttention.setProgress(0);
		progressBarMeditation.setProgress(0);
		progressBarSignal.setProgress(0);
		progressBarPower.setProgress(0);

		spinnerEEG.setEnabled(true);
	}

	public void updatePowerThresholds() {

		/*
		 * The "Power" level refers to the Puzzlebox Orbit helicopter's
		 * throttle setting. Typically, this is an "off" or "on" state,
		 * meaning the helicopter is either flying or not flying at all.
		 * However, this method could be used to increase the throttle
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

				/*
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

		/*
		 * This method is used for calculating whether
		 * the "Attention" or "Meditation" levels
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

		/*
		 * This method updates the power level of the
		 * "Throttle" and triggers the audio stream
		 * which is used to fly the helicopter
		 */

		if (NeuroSkyEegState.eegConnected) {

			if (NeuroSkyEegState.eegSignal < 100) {
				NeuroSkyEegState.eegAttention = 0;
				NeuroSkyEegState.eegMeditation = 0;
				progressBarAttention.setProgress(NeuroSkyEegState.eegAttention);
				progressBarMeditation.setProgress(NeuroSkyEegState.eegMeditation);
			}

			NeuroSkyEegState.eegPower = calculateSpeed();

			progressBarPower.setProgress(NeuroSkyEegState.eegPower);


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
		Toast.makeText((requireActivity().getApplicationContext()),
				"Session data reset",
				Toast.LENGTH_SHORT).show();
	}

	private final BroadcastReceiver mPacketReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String attentionStr = intent.getStringExtra("Attention");
			String meditationStr = intent.getStringExtra("Meditation");
			String signalStr = intent.getStringExtra("Signal Level");
			if (attentionStr != null) progressBarAttention.setProgress(Integer.parseInt(attentionStr));
			if (meditationStr != null) progressBarMeditation.setProgress(Integer.parseInt(meditationStr));
			if (signalStr != null) progressBarSignal.setProgress(Integer.parseInt(signalStr));

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
			if (name == null) return;
			switch(name) {

				case "eegStatus":
					if (value == null) break;
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
						if (value != null) progressBarBlink.setProgress(Integer.parseInt(value));
					} catch (NumberFormatException e) {
						Log.e(TAG, "Exception", e);
					}
					break;
			}
		}

	};
}
