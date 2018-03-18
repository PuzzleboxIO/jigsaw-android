package io.puzzlebox.jigsaw.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
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
import io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService;

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

	private OnFragmentInteractionListener mListener;

	public DialogInputNeuroSkyMindWaveFragment() {
		// Required empty public constructor
	}

	public static DialogInputNeuroSkyMindWaveFragment newInstance(String param1, String param2) {
		DialogInputNeuroSkyMindWaveFragment fragment = new DialogInputNeuroSkyMindWaveFragment();
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
//		View v = inflater.inflate(R.layout.dialog_input_neurosky_mindwave, container, false);
		v = inflater.inflate(R.layout.dialog_input_neurosky_mindwave, container, false);

		getDialog().getWindow().setTitle( getString(R.string.title_dialog_fragment_neurosky_mindwave));
//		getDialog().getWindow().setTitle( getString(R.string.label_neurosky_mindwave_instruction));

		progressBarAttention = (ProgressBar) v.findViewById(R.id.progressBarAttention);
		final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
		ShapeDrawable progressBarAttentionDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarAttentionColor = "#FF0000";
		progressBarAttentionDrawable.getPaint().setColor(Color.parseColor(progressBarAttentionColor));
		ClipDrawable progressAttention = new ClipDrawable(progressBarAttentionDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarAttention.setProgressDrawable(progressAttention);
		progressBarAttention.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarMeditation = (ProgressBar) v.findViewById(R.id.progressBarMeditation);
		ShapeDrawable progressBarMeditationDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarMeditationColor = "#0000FF";
		progressBarMeditationDrawable.getPaint().setColor(Color.parseColor(progressBarMeditationColor));
		ClipDrawable progressMeditation = new ClipDrawable(progressBarMeditationDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarMeditation.setProgressDrawable(progressMeditation);
		progressBarMeditation.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarSignal = (ProgressBar) v.findViewById(R.id.progressBarSignal);
		ShapeDrawable progressBarSignalDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
		String progressBarSignalColor = "#00FF00";
		progressBarSignalDrawable.getPaint().setColor(Color.parseColor(progressBarSignalColor));
		ClipDrawable progressSignal = new ClipDrawable(progressBarSignalDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarSignal.setProgressDrawable(progressSignal);
		progressBarSignal.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarBlink = (ProgressBar) v.findViewById(R.id.progressBarBlink);
		ShapeDrawable progressBarRangeDrawable = new ShapeDrawable();
//		String progressBarBlinkColor = "#FF00FF";
//		String progressBarBlinkColor = "#990099";
		String progressBarBlinkColor = "#BBBBBB";
		progressBarRangeDrawable.getPaint().setColor(Color.parseColor(progressBarBlinkColor));
		ClipDrawable progressRange = new ClipDrawable(progressBarRangeDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBarBlink.setProgressDrawable(progressRange);
		progressBarBlink.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

		progressBarBlink.setMax(NeuroSkyThinkGearService.blinkRangeMax);


		// setup the Raw EEG History plot
		eegRawHistoryPlot = (XYPlot) v.findViewById(R.id.eegRawHistoryPlot);
		eegRawHistorySeries = new SimpleXYSeries("");

		// Use index value as xVal, instead of explicit, user provided xVals.
		//		eegRawHistorySeries.useImplicitXVals();

		// Setup the boundary mode, boundary values only applicable in FIXED mode.

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


		Button connectEEG = (Button) v.findViewById(R.id.buttonConnectEEG);
		connectEEG.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				connectHeadset();
			}
		});


		Button buttonDeviceCancel = (Button) v.findViewById(R.id.buttonDeviceCancel);
		buttonDeviceCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				broadcastTileStatus("false");
				dismiss();
			}
		});


		buttonDeviceEnable = (Button) v.findViewById(R.id.buttonDeviceEnable);
		buttonDeviceEnable.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				broadcastTileStatus("true");
				dismiss();
			}
		});



		if (NeuroSkyThinkGearService.eegConnected ) {
			connectEEG.setText(R.string.buttonStatusNeuroSkyMindWaveDisconnect);
		}

		intentThinkGear = new Intent(getActivity(), NeuroSkyThinkGearService.class);


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


	// ################################################################

	public void onPause() {

		super.onPause();

		LocalBroadcastManager.getInstance(
				  getActivity().getApplicationContext()).unregisterReceiver(
				  mPacketReceiver);

		LocalBroadcastManager.getInstance(
				  getActivity().getApplicationContext()).unregisterReceiver(
				  mEventReceiver);

	} // onPause


	// ################################################################

	public void onResume() {

		// Store access variables for window and blank point

		Window window = getDialog().getWindow();

		Point size = new Point();

		// Store dimensions of the screen in `size`
		Display display = window.getWindowManager().getDefaultDisplay();

		display.getSize(size);

		// Set the width of the dialog proportional to a percentage of the screen width
//		window.setLayout((int) (size.x * 0.9), WindowManager.LayoutParams.WRAP_CONTENT);
//		window.setLayout((int) (size.x * 0.975), WindowManager.LayoutParams.WRAP_CONTENT);
		window.setLayout((int) (size.x * 0.98), WindowManager.LayoutParams.WRAP_CONTENT);

		// Set the dimensions  of the dialog proportional to a percentage of the screen dimensions
//		window.setLayout((int) (size.x * 0.95), (int) (size.y * 0.935));

		window.setGravity(Gravity.CENTER);

		// Call super onResume after sizing
		super.onResume();

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				  mPacketReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.packet"));

		LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
				  mEventReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.event"));

	}


	// ################################################################

	public void connectHeadset() {

		/**
		 * Called when the "Connect" button is pressed
		 */

		Log.v(TAG, "connectHeadset(): + spinnerEEG.getSelectedItem()");

		if (! NeuroSkyThinkGearService.eegConnected) {
			getActivity().startService(intentThinkGear);
		} else {
			disconnectHeadset();
		}

	} // connectHeadset


//	################################################################

	public void disconnectHeadset() {

		/**
		 * Called when "Disconnect" button is pressed
		 */

		Log.v(TAG, "disconnectHeadset()");


		NeuroSkyThinkGearService.disconnectHeadset();
		getActivity().stopService(intentThinkGear);



		updateStatusImage();

		progressBarAttention.setProgress(0);
		progressBarMeditation.setProgress(0);
		progressBarSignal.setProgress(0);
//		progressBarPower.setProgress(0);

//		setButtonText(R.id.buttonConnectEEG, "Connect EEG");


//		spinnerEEG.setEnabled(true);



	} // disconnectHeadset


	// ################################################################

	private BroadcastReceiver mPacketReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			int eegAttention = Integer.valueOf(intent.getStringExtra("Attention"));
			int eegMeditation = Integer.valueOf(intent.getStringExtra("Meditation"));
			int eegSignal = Integer.valueOf(intent.getStringExtra("Signal Level"));

//			Log.e(TAG, "eegAttention: " + eegAttention);

			progressBarAttention.setProgress(eegAttention);
			progressBarMeditation.setProgress(eegMeditation);
			progressBarSignal.setProgress(eegSignal);

			// TODO re-enable when extra safety desired
//			if ((! buttonDeviceEnable.isEnabled()) && (eegSignal == NeuroSkyNeuroSkyThinkGearService.signalSignalMax)) {
//				// This setting requires the quality of the EEG sensor's
//				// contact with skin hit to 100% at least once since the
//				// headset was last connected.
//				buttonDeviceEnable.setEnabled(true);
//				buttonDeviceEnable.setVisibility(View.VISIBLE);
//			}

			if (! buttonDeviceEnable.isEnabled()) {
				buttonDeviceEnable.setEnabled(true);
				buttonDeviceEnable.setVisibility(View.VISIBLE);
			}

			progressBarBlink.setProgress(0);

			updateEEGRawHistory(SessionSingleton.getInstance().getCurrentRawEEG());

//			updateSessionTime();

			updateStatusImage();

//			Log.e(TAG, "mPacketReceiver: eegConnected: " + eegConnected);
//			if (eegConnected.equals("true"))
//				setButtonText(R.id.buttonConnectEEG, "Disconnect EEG");
//			else
//				setButtonText(R.id.buttonConnectEEG, "Connect EEG");

		}

	};

	// ################################################################

	private BroadcastReceiver mEventReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

//			String action = intent.getAction();

			String name = intent.getStringExtra("name");
			String value = intent.getStringExtra("value");

			switch(name) {

				case "eegStatus":

					switch(value) {
						case "STATE_CONNECTING":
							updateStatusImage();
							setButtonText(R.id.buttonConnectEEG, getResources().getString(R.string.buttonStatusNeuroSkyMindWaveConnecting));
							break;
						case "STATE_CONNECTED":
//							Toast.makeText(context, "EEG Connected", Toast.LENGTH_SHORT).show();
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
//							Toast.makeText(context, "EEG Disconnected", Toast.LENGTH_SHORT).show();
							updateStatusImage();
							setButtonText(R.id.buttonConnectEEG, getResources().getString(R.string.buttonStatusNeuroSkyMindWaveConnect));
							buttonDeviceEnable.setEnabled(false);
							buttonDeviceEnable.setVisibility(View.INVISIBLE);
							progressBarAttention.setProgress(0);
							progressBarMeditation.setProgress(0);
							progressBarSignal.setProgress(0);
							break;
						case "MSG_LOW_BATTERY":
//							Toast.makeText(context, "EEG Battery Low", Toast.LENGTH_SHORT).show();
							Toast.makeText(context, R.string.buttonStatusNeuroSkyMindWaveBatteryLow, Toast.LENGTH_SHORT).show();
							updateStatusImage();
							break;
					}

					break;

				case "eegBlink":
					Log.d(TAG, "Blink: " + value + "\n");
					if (Integer.parseInt(value) > NeuroSkyThinkGearService.blinkRangeMax) {
						value = "" + NeuroSkyThinkGearService.blinkRangeMax;
					}
					try {
						progressBarBlink.setProgress(Integer.parseInt(value));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					break;

			}

		}

	};

	// ################################################################

	public void setButtonText(int buttonId, String text) {

		/**
		 * Shortcut for changing the text on a button
		 */

		Button button = (Button) v.findViewById(buttonId);
		button.setText(text);

	} // setButtonText


	// ################################################################

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

	} // updateStatusImage


	// ################################################################

	public void updateEEGRawHistory(Number[] rawEEG) {

		if (eegRawHistoryPlot != null) {
			eegRawHistoryPlot.removeSeries(eegRawHistorySeries);

			eegRawHistorySeries = new SimpleXYSeries(Arrays.asList(rawEEG), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Raw EEG");

			//		LineAndPointFormatter format = new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.BLACK, null, null);
			//		LineAndPointFormatter format = new LineAndPointFormatter(Color.rgb(200, 100, 100), Color.TRANSPARENT, null, null);
			LineAndPointFormatter format = new LineAndPointFormatter(Color.rgb(0, 0, 0), Color.TRANSPARENT, null, null);

			//		format.getFillPaint().setAlpha(220);

			eegRawHistoryPlot.addSeries(eegRawHistorySeries, format);


			// redraw the Plots:
			eegRawHistoryPlot.redraw();

//			rawEEG = new Number[512];
//			arrayIndex = 0;

//			ThinkGearSingleton.getInstance().resetRawEEG();

		}

	} // updateEEGRawHistory


	// ################################################################

	public void broadcastTileStatus(String value) {

		Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.tile.event");

		intent.putExtra("id", profileID);
		intent.putExtra("name", "active");
		intent.putExtra("value", value);
		intent.putExtra("category", "inputs");

		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

	}


}
