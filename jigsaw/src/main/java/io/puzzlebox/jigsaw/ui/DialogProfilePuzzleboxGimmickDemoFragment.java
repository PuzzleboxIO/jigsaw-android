package io.puzzlebox.jigsaw.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.media.AudioManager;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import io.puzzlebox.jigsaw.data.DevicePuzzleboxGimmickSingleton;
import io.puzzlebox.jigsaw.data.DevicePuzzleboxOrbitSingleton;
import io.puzzlebox.jigsaw.data.ProfileSingleton;
import io.puzzlebox.jigsaw.data.NeuroSkyEegState;
import io.puzzlebox.jigsaw.service.PuzzleboxGimmickBluetoothService;
import io.puzzlebox.jigsaw.R;

import static io.puzzlebox.jigsaw.data.NeuroSkyEegState.eegConnected;
import static io.puzzlebox.jigsaw.data.NeuroSkyEegState.eegConnecting;
import static io.puzzlebox.jigsaw.data.NeuroSkyEegState.eegSignal;
import androidx.core.content.ContextCompat;

public class DialogProfilePuzzleboxGimmickDemoFragment extends DialogFragment
        implements SeekBar.OnSeekBarChangeListener {

    private final static String TAG = DialogProfilePuzzleboxGimmickDemoFragment.class.getSimpleName();

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

    Button buttonTestFlight;
    Button buttonResetFlight;

    TextView textViewScore;
    TextView textViewLastScore;
    TextView textViewHighScore;

    ImageView imageViewStatus;

    final int[] thresholdValuesAttention = new int[101];
    final int[] thresholdValuesMeditation = new int[101];
    final int minimumPower = 0; // minimum power for the bloom
    final int maximumPower = 100; // maximum power for the bloom

    public DialogProfilePuzzleboxGimmickDemoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.dialog_profile_puzzlebox_gimmick_demo, container, false);

        Window dialogWindow = requireDialog().getWindow();
        if (dialogWindow != null) dialogWindow.setTitle( getString(R.string.title_dialog_fragment_puzzlebox_gimmick_demo));

        buttonTestFlight = v.findViewById(R.id.buttonTestFlight);
        buttonTestFlight.setOnClickListener(this::testFlight);

        buttonResetFlight = v.findViewById(R.id.buttonResetFlight);
        buttonResetFlight.setOnClickListener(this::resetFlight);

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

        seekBarAttention = v.findViewById(R.id.seekBarAttention);
        seekBarAttention.setOnSeekBarChangeListener(this);
        seekBarMeditation = v.findViewById(R.id.seekBarMeditation);
        seekBarMeditation.setOnSeekBarChangeListener(this);

        imageViewStatus = v.findViewById(R.id.imageViewStatus);

        textViewScore = v.findViewById(R.id.textViewScore);
        textViewLastScore = v.findViewById(R.id.textViewLastScore);
        textViewHighScore = v.findViewById(R.id.textViewHighScore);

        Button buttonDeviceCancel = v.findViewById(R.id.buttonDeviceCancel);
        buttonDeviceCancel.setOnClickListener(view -> dismiss());

        buttonDeviceEnable = v.findViewById(R.id.buttonDeviceEnable);
        buttonDeviceEnable.setOnClickListener(view -> dismiss());

        /*
         * PuzzleboxOrbitAudioIRHandler
         */
        if (!DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.isAlive()) {
            /*
             * Prepare audio stream
             */
            DevicePuzzleboxOrbitSingleton.getInstance().startAudioHandler();
        }
        resetFlight(v);
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();

        requireActivity().getApplicationContext().unregisterReceiver(
                mPacketReceiver);

        requireActivity().getApplicationContext().unregisterReceiver(
                mEventReceiver);

        stopControl();
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

        // Set the dimensions  of the dialog proportional to a percentage of the screen dimensions
//		window.setLayout((int) (size.x * 0.95), (int) (size.y * 0.935));

        window.setGravity(Gravity.CENTER);

        // Call super onResume after sizing
        super.onResume();

        if (ProfileSingleton.getInstance().getValue(io.puzzlebox.jigsaw.ui.DialogOutputAudioIRFragment.profileID, "active").equals("true")) {
            playControl();
        } else {
            Toast.makeText(requireActivity().getApplicationContext(), getString(R.string.toast_puzzlebox_orbit_joystick_audio_ir_warning), Toast.LENGTH_LONG).show();
        }

        updatePowerThresholds();
        updatePower();
        updateControlSignal();

        ContextCompat.registerReceiver(requireActivity().getApplicationContext(), mPacketReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.packet"), ContextCompat.RECEIVER_NOT_EXPORTED);

        ContextCompat.registerReceiver(requireActivity().getApplicationContext(), mEventReceiver, new IntentFilter("io.puzzlebox.jigsaw.protocol.thinkgear.event"), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private final BroadcastReceiver mPacketReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String attentionStr = intent.getStringExtra("Attention");
            String meditationStr = intent.getStringExtra("Meditation");
            String signalStr = intent.getStringExtra("Signal Level");
            int eegSignal = 0;
            if (attentionStr != null) progressBarAttention.setProgress(Integer.parseInt(attentionStr));
            if (meditationStr != null) progressBarMeditation.setProgress(Integer.parseInt(meditationStr));
            if (signalStr != null) {
                eegSignal = Integer.parseInt(signalStr);
                progressBarSignal.setProgress(eegSignal);
            }

            updateStatusImage();

            updatePower();

            PuzzleboxGimmickBluetoothService.getInstance().commandGimmick(Integer.toString(eegSignal));
        }

    };

    public void updateControlSignal() {

        Integer[] command =  {
                DevicePuzzleboxOrbitSingleton.getInstance().defaultControlThrottle,
                DevicePuzzleboxOrbitSingleton.getInstance().defaultControlYaw,
                DevicePuzzleboxOrbitSingleton.getInstance().defaultControlPitch,
                DevicePuzzleboxOrbitSingleton.getInstance().defaultChannel};

        // Transmit zero Throttle power if not above EEG power threshold
        // or demo mode (test flight) is not active
        if (eegPower <= 0) {
            command[0] = 0;
        }
        DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.command = command;
        DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.updateControlSignal();
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
                        case "STATE_CONNECTED":
                            updateStatusImage();
                            break;
                        case "STATE_NOT_FOUND":
                            Toast.makeText(context, "EEG Not Found", Toast.LENGTH_SHORT).show();
                            updateStatusImage();
                            buttonDeviceEnable.setEnabled(false);
                            buttonDeviceEnable.setVisibility(View.INVISIBLE);
                            break;
                        case "STATE_NOT_PAIRED":
                            Toast.makeText(context, "EEG Not Paired", Toast.LENGTH_SHORT).show();
                            updateStatusImage();
                            buttonDeviceEnable.setEnabled(false);
                            buttonDeviceEnable.setVisibility(View.INVISIBLE);
                            break;
                        case "STATE_DISCONNECTED":
                            updateStatusImage();
                            buttonDeviceEnable.setEnabled(false);
                            buttonDeviceEnable.setVisibility(View.INVISIBLE);
                            break;
                        case "MSG_LOW_BATTERY":
                            Toast.makeText(context, io.puzzlebox.jigsaw.R.string.buttonStatusNeuroSkyMindWaveBatteryLow, Toast.LENGTH_SHORT).show();
                            updateStatusImage();
                            break;
                    }

                    break;

                case "eegBlink":
                    Log.d(TAG, "Blink: " + value + "\n");
                    break;
            }
        }

    };

    public void updateStatusImage() {

        if(eegPower > 0) {
            imageViewStatus.setImageResource(R.drawable.status_4_active);
            return;
        }

        if(eegSignal > 90) {
            imageViewStatus.setImageResource(R.drawable.status_3_processing);
            return;
        }

        if(eegConnected) {
            imageViewStatus.setImageResource(R.drawable.status_2_connected);
            return;
        }

        if(eegConnecting) {
            imageViewStatus.setImageResource(R.drawable.status_1_connecting);
        } else {
            imageViewStatus.setImageResource(R.drawable.status_default);
        }
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

    public void updatePower() {
        /*
         * This method updates the power level of the
         * "Throttle" and triggers the audio stream
         * which is used to fly the helicopter
         */
        if (eegConnected) {

            if (eegSignal < 100) {
                NeuroSkyEegState.eegAttention = 0;
                NeuroSkyEegState.eegMeditation = 0;
                progressBarAttention.setProgress(NeuroSkyEegState.eegAttention);
                progressBarMeditation.setProgress(NeuroSkyEegState.eegMeditation);
            }

            NeuroSkyEegState.eegPower = calculateSpeed();
            eegPower = NeuroSkyEegState.eegPower;

            // TODO 2018-03-14
//            progressBarPower.setProgress(NeuroSkyEegState.eegPower);

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

            broadcastCommandBluetooth("attention", "" + NeuroSkyEegState.eegAttention);
        }

        // TODO
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
//			eegPower = InteraXonMuseService.eegPower;
//
//
//		}

        DevicePuzzleboxOrbitSingleton.getInstance().eegPower = eegPower;

        if (eegPower > 0) {
            // Start playback of audio control stream
            updateScore();
            DevicePuzzleboxOrbitSingleton.getInstance().flightActive = true;
        } else {
            // Land the helicopter
            resetCurrentScore();
        }
        updateControlSignal();
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

    public void updateScore() {
        /*
         * Score points based on target slider levels
         * If you pass your goal with either Attention or Meditation
         * the higher target of the two will count as points per second.
         *
         * Minimum threshold for points is set as "minimumScoreTarget"
         *
         * For example, assume minimumScoreTarget is 40%.
         * If your target Attention is 60%, and you go past to reach 80%
         * you will receive 20 points per second (60-40). If your
         * target is 80%, and you reach 80% you will receive 40
         * points per second (80-40).
         *
         * You can set both Attention and Meditation targets at the
         * same time. Reaching either will fly the helicopter, but you
         * will only receive points for the higher-scoring target of
         * the two.
         *
         */
        int eegAttentionScore = 0;
        int eegAttention = progressBarAttention.getProgress();
        int eegAttentionTarget = seekBarAttention.getProgress();

        int eegMeditationScore = 0;
        int eegMeditation = progressBarMeditation.getProgress();
        int eegMeditationTarget = seekBarMeditation.getProgress();

        if ((eegAttention >= eegAttentionTarget) &&
                (eegAttentionTarget > DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
            eegAttentionScore = eegAttentionTarget - DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget;

        if ((eegMeditation >= eegMeditationTarget) &&
                (eegMeditationTarget > DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
            eegMeditationScore = eegMeditationTarget - DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget;

        if (eegAttentionScore > eegMeditationScore)
            DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent = DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent + eegAttentionScore;
        else
            DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent = DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent + eegMeditationScore;

        textViewScore.setText(String.format(Locale.getDefault(), "%d", DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent));

        if (DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent > DevicePuzzleboxOrbitSingleton.getInstance().scoreHigh) {
            DevicePuzzleboxOrbitSingleton.getInstance().scoreHigh = DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent;
            textViewHighScore.setText(String.format(Locale.getDefault(), "%d", DevicePuzzleboxOrbitSingleton.getInstance().scoreHigh));
        }

        // Catch anyone gaming the system with one slider
        // below the minimum threshold and the other over.
        // For example, setting Meditation to 1% will keep helicopter
        // activated even if Attention is below target
        if ((eegAttention < eegAttentionTarget) && (eegMeditation < DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
            resetCurrentScore();
        if ((eegMeditation < eegMeditationTarget) && (eegAttention < DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
            resetCurrentScore();
        if ((eegAttention < DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget) && (eegMeditation < DevicePuzzleboxOrbitSingleton.getInstance().minimumScoreTarget))
            resetCurrentScore();
    }

    public void resetCurrentScore() {
        if (DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent > 0)
            textViewLastScore.setText(String.format(Locale.getDefault(), "%d", DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent));
        DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent = 0;
        textViewScore.setText(String.format(Locale.getDefault(), "%d", DevicePuzzleboxOrbitSingleton.getInstance().scoreCurrent));
    }

    public void playControl() {
        if (DevicePuzzleboxOrbitSingleton.getInstance().generateAudio) {
            /*
             * Generate signal on the fly
             */
            DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.ifFlip = DevicePuzzleboxOrbitSingleton.getInstance().invertControlSignal; // if checked then flip
            int channel = DevicePuzzleboxOrbitSingleton.getInstance().defaultChannel;
            updateAudioHandlerLoopNumberWhileMindControl(-1); // Loop infinite for easier user testing
            updateAudioHandlerChannel(channel);
            DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.mutexNotify();
        } else {
            /*
             * Play audio control file
             */

            /* Getting the user sound settings */
            AudioManager audioManager = (AudioManager) requireActivity().getSystemService(Context.AUDIO_SERVICE);
            float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) maxVolume, 0);
            /* Is the sound loaded already? */
            if (DevicePuzzleboxOrbitSingleton.getInstance().loaded) {
                DevicePuzzleboxOrbitSingleton.getInstance().soundPool.play(DevicePuzzleboxOrbitSingleton.getInstance().soundID, 1f, 1f, 1, 0, 1f); // Fixes Samsung Galaxy S4 [SGH-M919]

                // TODO No visible effects of changing these variables on digital oscilloscope
                //				soundPool.play(soundID, 0.5f, 0.5f, 1, 0, 0.5f);
            }
        }
    }

    public void stopControl() {
        stopAudio();
        DevicePuzzleboxOrbitSingleton.getInstance().flightActive = false;
    }

    public void stopAudio() {
        /*
         * stop AudioTrack as well as destroy service.
         */
        DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.keepPlaying = false;

        /*
         * Stop playing audio control file
         */
        if (DevicePuzzleboxOrbitSingleton.getInstance().soundPool != null) {
            try {
                DevicePuzzleboxOrbitSingleton.getInstance().soundPool.stop(DevicePuzzleboxOrbitSingleton.getInstance().soundID);
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
        }
    }

    public void testFlight(View v) {
        /*
         * Demo mode is called when the "Test Helicopter" button is pressed.
         * This method can be easily adjusted for testing new features
         * during development.
         */
        if (! DevicePuzzleboxOrbitSingleton.getInstance().flightActive) {
            DevicePuzzleboxOrbitSingleton.getInstance().flightActive = true;
            DevicePuzzleboxOrbitSingleton.getInstance().demoActive = true;
        } else {
            DevicePuzzleboxOrbitSingleton.getInstance().demoActive = false;
        }
        updateControlSignal();
    }

    public void resetFlight(View view) {
        resetCurrentScore();

        DevicePuzzleboxOrbitSingleton.getInstance().demoActive = false;

        // Setting eegPower to zero will cause the Orbit to land if flying
        // However, if the user's data is actively being received
        // the Orbit may take off again approximately one second later
        eegPower = 0;

        seekBarAttention.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultTargetAttention);
        seekBarMeditation.setProgress(DevicePuzzleboxOrbitSingleton.getInstance().defaultTargetMeditation);

        updatePowerThresholds();
        updateControlSignal();
    }

    /**
     * the puzzleboxOrbitAudioIRHandler to update channel
     */
    public void updateAudioHandlerChannel(int channel) {
        DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.channel = channel;
        DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.updateControlSignal();
    }

    /**
     * @param number the puzzleboxOrbitAudioIRHandler to update loop number while mind control
     */
    public void updateAudioHandlerLoopNumberWhileMindControl(int number) {
        DevicePuzzleboxOrbitSingleton.getInstance().puzzleboxOrbitAudioIRHandler.loopNumberWhileMindControl = number;
    }

    private  void broadcastCommandBluetooth(String name, String value) {
        Log.d(TAG, "broadcastCommandBluetooth: " + name + ": " + value);

        Intent intent = new Intent("io.puzzlebox.jigsaw.protocol.bluetooth.command");

        intent.putExtra("name", name);
        intent.putExtra("value", value);

        requireContext().sendBroadcast(intent);
    }
}
