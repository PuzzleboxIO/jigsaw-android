package io.puzzlebox.jigsaw.wear;

import android.hardware.Sensor;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SensorFragmentPagerAdapter extends FragmentStateAdapter {

    // Reference: https://github.com/drejkim/AndroidWearMotionSensors

    private final int[] sensorTypes = {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE
    };

    public SensorFragmentPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return SensorFragment.newInstance(sensorTypes[position]);
    }

    @Override
    public int getItemCount() {
        return sensorTypes.length;
    }
}
