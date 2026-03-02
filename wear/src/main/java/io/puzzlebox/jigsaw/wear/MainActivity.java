package io.puzzlebox.jigsaw.wear;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;

import io.puzzlebox.jigsaw.wear.R;

public class MainActivity extends Activity {

	// Reference: https://github.com/drejkim/AndroidWearMotionSensors

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		WatchViewStub stub = findViewById(R.id.watch_view_stub);
		stub.setOnLayoutInflatedListener(stub2 -> {
			final GridViewPager pager = findViewById(R.id.pager);
			pager.setAdapter(new SensorFragmentPagerAdapter(getFragmentManager()));

			DotsPageIndicator indicator = findViewById(R.id.page_indicator);
			indicator.setPager(pager);
		});
	}
}
