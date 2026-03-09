package io.puzzlebox.jigsaw.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import java.text.DecimalFormat;
import java.util.Arrays;

import io.puzzlebox.jigsaw.R;
import io.puzzlebox.jigsaw.data.SessionSingleton;
import io.puzzlebox.jigsaw.data.NeuroSkyEegState;
import androidx.core.content.ContextCompat;

public class DialogInputNeuroSkyMindWaveFragment extends DialogFragment {

	private final static String TAG = DialogInputNeuroSkyMindWaveFragment.class.getSimpleName();

	public final static String profileID = "neurosky_mindwave_mobile";

	// UI
	ProgressBar progressBarAttention;
	ProgressBar progressBarMeditation;
	ProgressBar progressBarSignal;
	ProgressBar progressBarBlink;
	XYPlot eegRawHistoryPlot;
	SimpleXYSeries eegRawHistorySeries;
	Button buttonDeviceEnable;

	View v;

	private static Intent intentThinkGear;

	public DialogInputNeuroSkyMindWaveFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.dialog_input_neurosky_mindwave, container, false);

		Window dialogWindow = requireDialog().getWindow();
		if (dialogWindow != null) dialogWindow.setTitle( getString(R.string.title_dialog_fragment_neurosky_mindwave));

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

		progressBarBlink = v.findViewById(R.id.progressBarBlink);
		ShapeDrawable progressBarRangeDrawable = new ShapeDrawable();
		String progressBarBlinkColor = "#BBBBBB";
		progressBarRangeDrawable.getPaint().setColor(Color.parseColor(progressBarBlinkColor));
		ClipDrawable progressRange = new ClipDrawable(progressBarRangeDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
		progressBarBlink.setProgressDrawable(progressRange);
		progressBarBlink.setBackground(ResourcesCompat.getDrawable(getResources(), android.R.drawable.progress_horizontal, null));

		progressBarBlink.setMax(NeuroSkyEegState.blinkRangeMax);

		// Set up the Raw EEG History plot
		eegRawHistoryPlot = v.findViewById(R.id.eegRawHistoryPlot);
		eegRawHistorySeries = new SimpleXYSeries("");

		// Set up the boundary mode, boundary values only applicable in FIXED mode.
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

		Button connectEEG = v.findViewById(R.id.buttonConnectEEG);
		connectEEG.setOnClickListener(v -> connectHeadset());

		Button buttonDeviceCancel = v.findViewById(R.id.buttonDeviceCancel);
		buttonDeviceCancel.setOnClickListener(v -> {
			broadcastTileStatus("false");
			dismiss();
		});

		buttonDeviceEnable = v.findViewById(R.id.buttonDeviceEnable);
		buttonDeviceEnable.setOnClickListener(v -> {
			broadcastTileStatus("true");
			dismiss();
		});

		if (NeuroSkyEegState.eegConnected ) {
			connectEEG.setText(R.string.buttonStatusNeuroSkyMindWaveDisconnect);
		}

		// Instantiate the Intent via reflection so this file compiles even when
		// NeuroSkyThinkGearService is excluded (NeuroSky SDK absent from build).
		try {
			intentThinkGear = new Intent(requireActivity(),
					Class.forName("io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService"));
		} catch (ClassNotFoundException e) {
			intentThinkGear = null; // NeuroSky SDK not available in this build
		}

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

		// Store access variables for window and blank point
		Window window = requireDialog().getWindow();

        if (window == null) {
            super.onResume();
            return;
        }
		int screenWidth = requireContext().getResources().getDisplayMetrics().widthPixels;

		// Set the width of the dialog proportional to a percentage of the screen width
		window.setLayout((int) (screenWidth * 0.98), WindowManager.LayoutParams.WRAP_CONTENT);

		window.setGravity(Gravity.CENTER);

		// Call super onResume after sizing
		super.onResume();

		ContextCompat.registerReceiver(requireActivity().getApplicationContext(), mPacketReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.packet"), ContextCompat.RECEIVER_NOT_EXPORTED);

		ContextCompat.registerReceiver(requireActivity().getApplicationContext(), mEventReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.event"), ContextCompat.RECEIVER_NOT_EXPORTED);
	}

	public void connectHeadset() {

		// Called when the "Connect" button is pressed

		if (intentThinkGear == null) {
			Toast.makeText(requireActivity(), "NeuroSky SDK not available in this build", Toast.LENGTH_SHORT).show();
		} else if (!NeuroSkyEegState.eegConnected) {
			requireActivity().startService(intentThinkGear);
		} else {
			disconnectHeadset();
		}
	}

	public void disconnectHeadset() {

		// Called when "Disconnect" button is pressed

		NeuroSkyEegState.disconnectIfConnected();
		if (intentThinkGear != null) requireActivity().stopService(intentThinkGear);

		updateStatusImage();

		progressBarAttention.setProgress(0);
		progressBarMeditation.setProgress(0);
		progressBarSignal.setProgress(0);
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

			if (! buttonDeviceEnable.isEnabled()) {
				buttonDeviceEnable.setEnabled(true);
				buttonDeviceEnable.setVisibility(View.VISIBLE);
			}

			progressBarBlink.setProgress(0);

			updateEEGRawHistory(SessionSingleton.getInstance().getCurrentRawEEG());

			updateStatusImage();
		}

	};

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
							setButtonText(R.id.buttonConnectEEG, getResources().getString(R.string.buttonStatusNeuroSkyMindWaveConnecting));
							break;
						case "STATE_CONNECTED":
							updateStatusImage();
							setButtonText(R.id.buttonConnectEEG, getResources().getString(R.string.buttonStatusNeuroSkyMindWaveDisconnect));
							buttonDeviceEnable.setEnabled(true);
							buttonDeviceEnable.setVisibility(View.VISIBLE);
							progressBarAttention.setProgress(0);
							progressBarMeditation.setProgress(0);
							progressBarSignal.setProgress(0);
							break;
						case "STATE_NOT_FOUND":
							Toast.makeText(context, "EEG Not Found", Toast.LENGTH_SHORT).show();
							updateStatusImage();
							setButtonText(R.id.buttonConnectEEG, getResources().getString(R.string.buttonStatusNeuroSkyMindWaveConnect));
							buttonDeviceEnable.setEnabled(false);
							buttonDeviceEnable.setVisibility(View.INVISIBLE);
							progressBarAttention.setProgress(0);
							progressBarMeditation.setProgress(0);
							progressBarSignal.setProgress(0);
							break;
						case "STATE_NOT_PAIRED":
							Toast.makeText(context, "EEG Not Paired", Toast.LENGTH_SHORT).show();
							updateStatusImage();
							setButtonText(R.id.buttonConnectEEG, getResources().getString(R.string.buttonStatusNeuroSkyMindWaveConnect));
							buttonDeviceEnable.setEnabled(false);
							buttonDeviceEnable.setVisibility(View.INVISIBLE);
							progressBarAttention.setProgress(0);
							progressBarMeditation.setProgress(0);
							progressBarSignal.setProgress(0);
							break;
						case "STATE_DISCONNECTED":
							updateStatusImage();
							setButtonText(R.id.buttonConnectEEG, getResources().getString(R.string.buttonStatusNeuroSkyMindWaveConnect));
							buttonDeviceEnable.setEnabled(false);
							buttonDeviceEnable.setVisibility(View.INVISIBLE);
							progressBarAttention.setProgress(0);
							progressBarMeditation.setProgress(0);
							progressBarSignal.setProgress(0);
							break;
						case "MSG_LOW_BATTERY":
							Toast.makeText(context, R.string.buttonStatusNeuroSkyMindWaveBatteryLow, Toast.LENGTH_SHORT).show();
							updateStatusImage();
							break;
					}

					break;

				case "eegBlink":
					Log.d(TAG, "Blink: " + value + "\n");
					if (value != null) {
						if (Integer.parseInt(value) > NeuroSkyEegState.blinkRangeMax) {
							value = "" + NeuroSkyEegState.blinkRangeMax;
						}
						try {
							progressBarBlink.setProgress(Integer.parseInt(value));
						} catch (NumberFormatException e) {
							Log.e(TAG, "Exception", e);
						}
					}
					break;
			}
		}

	};

	public void setButtonText(int buttonId, String text) {
		// Shortcut for changing the text on a button
		Button button = v.findViewById(buttonId);
		button.setText(text);
	}

	@SuppressWarnings("EmptyMethod")
	public void updateStatusImage() {
	} // updateStatusImage

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

	public void broadcastTileStatus(String value) {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.tile.event");
		intent.setPackage(requireActivity().getPackageName());

		intent.putExtra("id", profileID);
		intent.putExtra("name", "active");
		intent.putExtra("value", value);
		intent.putExtra("category", "inputs");

		requireActivity().sendBroadcast(intent);
	}
}
