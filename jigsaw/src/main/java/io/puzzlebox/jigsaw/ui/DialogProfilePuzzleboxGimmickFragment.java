package io.puzzlebox.jigsaw.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import io.puzzlebox.jigsaw.R;
//import io.puzzlebox.gimmick.R;

import io.puzzlebox.jigsaw.data.DevicePuzzleboxGimmickSingleton;
import io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService;
import io.puzzlebox.jigsaw.data.DevicePuzzleboxOrbitSingleton;

import static io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService.eegConnected;
import static io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService.eegSignal;

//public class DialogProfilePuzzleboxGimmickFragment extends DialogFragment
//        implements SeekBar.OnSeekBarChangeListener{
public class DialogProfilePuzzleboxGimmickFragment extends DialogProfilePuzzleboxOrbitFragment {

    private static final String TAG = DialogProfilePuzzleboxGimmickFragment.class.getSimpleName();

    /**
     * Configuration
     */
    public int eegPower = 0;

    // UI
    Button buttonDeviceEnable;

    ProgressBar progressBarAttention;
    SeekBar seekBarAttention;
    ProgressBar progressBarMeditation;
    SeekBar seekBarMeditation;
    ProgressBar progressBarSignal;
    ProgressBar progressBarPower;
//	ProgressBar progressBarBlink;

    Button buttonTestFlight;
    Button buttonResetFlight;

    TextView textViewScore;
    TextView textViewLastScore;
    TextView textViewHighScore;

    ImageView imageViewStatus;

    int[] thresholdValuesAttention = new int[101];
    int[] thresholdValuesMeditation = new int[101];
    int minimumPower = 0; // minimum power for the bloom
    int maximumPower = 100; // maximum power for the bloom

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public DialogProfilePuzzleboxGimmickFragment() {
        // Required empty public constructor
    }

    public static DialogProfilePuzzleboxGimmickFragment newInstance(String param1, String param2) {
        DialogProfilePuzzleboxGimmickFragment fragment = new DialogProfilePuzzleboxGimmickFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.dialog_profile_puzzlebox_gimmick, container, false);

        getDialog().getWindow().setTitle( getString(R.string.title_dialog_fragment_puzzlebox_orbit));

        buttonTestFlight = (Button) v.findViewById(R.id.buttonTestFlight);
        buttonTestFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testFlight(v);
            }
        });

        buttonResetFlight = (Button) v.findViewById(R.id.buttonResetFlight);
        buttonResetFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFlight(v);
            }
        });

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

        progressBarPower = (ProgressBar) v.findViewById(R.id.progressBarPower);
        ShapeDrawable progressBarPowerDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
        String progressBarPowerColor = "#FFFF00";
        progressBarPowerDrawable.getPaint().setColor(Color.parseColor(progressBarPowerColor));
        ClipDrawable progressPower = new ClipDrawable(progressBarPowerDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        progressBarPower.setProgressDrawable(progressPower);
        progressBarPower.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));

//		progressBarBlink = (ProgressBar) v.findViewById(R.id.progressBarBlink);
//		ShapeDrawable progressBarRangeDrawable = new ShapeDrawable();
////		String progressBarBlinkColor = "#FF00FF";
////		String progressBarBlinkColor = "#990099";
//		String progressBarBlinkColor = "#BBBBBB";
//		progressBarRangeDrawable.getPaint().setColor(Color.parseColor(progressBarBlinkColor));
//		ClipDrawable progressRange = new ClipDrawable(progressBarRangeDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
//		progressBarBlink.setProgressDrawable(progressRange);
//		progressBarBlink.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));
//
//		progressBarBlink.setMax(NeuroSkyNeuroSkyThinkGearService.blinkRangeMax);


        seekBarAttention = (SeekBar) v.findViewById(R.id.seekBarAttention);
        seekBarAttention.setOnSeekBarChangeListener(this);
        seekBarMeditation = (SeekBar) v.findViewById(R.id.seekBarMeditation);
        seekBarMeditation.setOnSeekBarChangeListener(this);


        imageViewStatus = (ImageView) v.findViewById(R.id.imageViewStatus);

//		textViewLabelScore = (TextView) v.findViewById(R.id.textViewLabelScore);
//		textViewLabelLastScore = (TextView) v.findViewById(R.id.textViewLabelLastScore);
//		textViewLabelHighScore = (TextView) v.findViewById(R.id.textViewLabelHighScore);


        textViewScore = v.findViewById(R.id.textViewScore);
        textViewLastScore = v.findViewById(R.id.textViewLastScore);
        textViewHighScore = v.findViewById(R.id.textViewHighScore);


        // Hide the "Scores" label by default
//		textViewLabelScores.setVisibility(View.GONE);
//		viewSpaceScore.setVisibility(View.GONE);


        Button buttonDeviceCancel = (Button) v.findViewById(io.puzzlebox.jigsaw.R.id.buttonDeviceCancel);
        buttonDeviceCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        buttonDeviceEnable = (Button) v.findViewById(io.puzzlebox.jigsaw.R.id.buttonDeviceEnable);
        buttonDeviceEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


//		intentThinkGear = new Intent(getActivity(), NeuroSkyNeuroSkyThinkGearService.class);


        /**
         * PuzzleboxOrbitAudioIRHandler
         */

        if (!DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.isAlive()) {


            /**
             * Prepare audio stream
             */

            // TODO
//			maximizeAudioVolume(); // Automatically set media volume to maximum

            /** Set the hardware buttons to control the audio output */
//			getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

            /** Preload the flight control WAV file into memory */
//			DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
//			DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//				public void onLoadComplete(SoundPool soundPool,
//													int sampleId,
//													int status) {
//					DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().loaded = true;
//				}
//			});
//			DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().soundID = DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().soundPool.load(getActivity().getApplicationContext(), DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().audioFile, 1);


//			DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.start();
            DevicePuzzleboxOrbitSingleton.getInstance().startAudioHandler();


        }


//		if (DevicePuzzleboxDevicePuzzleboxOrbitSingleton.getInstance().flightActive)
//			buttonTestFlight.setText(getResources().getString(R.string.button_stop_test));

        /**
         * Update settings according to default UI
         */

        // TODO
//		updateScreenLayout();

//		updatePowerThresholds();
////		updatePower();
//
//		updateControlSignal();


//        resetFlight(v);

        return v;
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    // ################################################################

    @Override
    public void updatePower() {

        /**
         * This method updates the power level of the
         * "Throttle" and triggers the audio stream
         * which is used to fly the helicopter
         */

        if (eegConnected) {

            if (eegSignal < 100) {
                NeuroSkyThinkGearService.eegAttention = 0;
                NeuroSkyThinkGearService.eegMeditation = 0;
                progressBarAttention.setProgress(NeuroSkyThinkGearService.eegAttention);
                progressBarMeditation.setProgress(NeuroSkyThinkGearService.eegMeditation);
            }

            NeuroSkyThinkGearService.eegPower = calculateSpeed();
            eegPower = NeuroSkyThinkGearService.eegPower;

            // TODO 2018-03-14
//            progressBarPower.setProgress(NeuroSkyNeuroSkyThinkGearService.eegPower);

            if (eegPower > 0) {


                if (DevicePuzzleboxGimmickSingleton.getInstance().x10Level < 10) {
                    broadcastCommandBluetooth("x10", DevicePuzzleboxGimmickSingleton.getInstance().x10ID + " Bright");
                    DevicePuzzleboxGimmickSingleton.getInstance().x10Level++;
                }


            } else {

                if (DevicePuzzleboxGimmickSingleton.getInstance().x10Level > 0) {
                    broadcastCommandBluetooth("x10", DevicePuzzleboxGimmickSingleton.getInstance().x10ID + " Dim");
                    DevicePuzzleboxGimmickSingleton.getInstance().x10Level--;
                }

            }


            progressBarPower.setProgress(DevicePuzzleboxGimmickSingleton.getInstance().x10Level * 10);




        }

        DevicePuzzleboxOrbitSingleton.getInstance().eegPower = eegPower;

        if (eegPower > 0) {

            updateScore();

            DevicePuzzleboxOrbitSingleton.getInstance().flightActive = true;

        } else {

            resetCurrentScore();

        }

        updateControlSignal();

    } // updatePower


    // ################################################################

    @Override
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


    } // calculateSpeed


    // ################################################################

    private  void broadcastCommandBluetooth(String name, String value) {

        Log.d(TAG, "broadcastCommandBluetooth: " + name + ": " + value);

        Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.bluetooth.command");

        intent.putExtra("name", name);
        intent.putExtra("value", value);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

    }

}
