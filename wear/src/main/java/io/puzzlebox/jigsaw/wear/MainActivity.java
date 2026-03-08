package io.puzzlebox.jigsaw.wear;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends FragmentActivity {

	// Reference: https://github.com/drejkim/AndroidWearMotionSensors

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		ViewPager2 pager = findViewById(R.id.pager);
		pager.setAdapter(new SensorFragmentPagerAdapter(this));
	}
}
