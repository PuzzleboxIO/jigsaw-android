package io.puzzlebox.jigsaw.ui;

import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import io.puzzlebox.jigsaw.R;

import io.puzzlebox.jigsaw.data.ProfileSingleton;

public class TilesFragment extends Fragment {

	/**
	 * Number of items visible in carousels.
	 */
	private static final float INITIAL_ITEMS_COUNT = 2.5F;

	public TilesFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ProfileSingleton.getInstance().parseXML(requireContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_tiles, container, false);

		// Get reference to carousel container
		LinearLayout mInputCarouselContainer = v.findViewById(R.id.carousel_devices_input);
		LinearLayout mOutputCarouselContainer = v.findViewById(R.id.carousel_devices_output);
		LinearLayout mProfileCarouselContainer = v.findViewById(R.id.carousel_devices_profile);


		// Compute the width of a carousel item based on the screen width and number of initial items.
		final int imageSize = (int) (requireContext().getResources().getDisplayMetrics().widthPixels / INITIAL_ITEMS_COUNT);

		ImageView imageItem;

		// Get the array of input devices resources
		try (TypedArray devicesInputResourcesTypedArray = getResources().obtainTypedArray(R.array.devices_input_icon_array);
				TypedArray devicesOutputResourcesTypedArray = getResources().obtainTypedArray(R.array.devices_output_icon_array);
				TypedArray devicesProfileResourcesTypedArray = getResources().obtainTypedArray(R.array.devices_profile_array)) {

		// Populate the input devices carousel with items
		for (int i = 0 ; i < devicesInputResourcesTypedArray.length() ; ++i) {

			final int index = i;

			// Create new ImageView
			imageItem = new ImageView(requireActivity());

			// Set the shadow background
			imageItem.setBackgroundResource(R.drawable.shadow);

			// Set the image view resource
			imageItem.setImageResource(devicesInputResourcesTypedArray.getResourceId(i, -1));

			// Set the size of the image view to the previously computed value
			imageItem.setLayoutParams(new LinearLayout.LayoutParams(imageSize, imageSize));

			imageItem.setOnClickListener(view -> showDialog("input", index));

			TileViewAnimator tileViewAnimator = new TileViewAnimator(requireContext());
			tileViewAnimator.addView(imageItem);

			/// Add image view to the carousel container
			mInputCarouselContainer.addView(tileViewAnimator);
		}

		// Populate the output devices carousel with items
		for (int i = 0 ; i < devicesOutputResourcesTypedArray.length() ; ++i) {
			imageItem = new ImageView(requireActivity());
			imageItem.setBackgroundResource(R.drawable.shadow);
			imageItem.setImageResource(devicesOutputResourcesTypedArray.getResourceId(i, -1));
			imageItem.setLayoutParams(new LinearLayout.LayoutParams(imageSize, imageSize));

			final int index = i;
			imageItem.setOnClickListener(view -> showDialog("output", index));

			TileViewAnimator tileViewAnimator = new TileViewAnimator(requireContext());
			tileViewAnimator.addView(imageItem);

			mOutputCarouselContainer.addView(tileViewAnimator);
		}

		// Populate the device profile carousel with items
		for (int i = 0 ; i < devicesProfileResourcesTypedArray.length() ; ++i) {
			imageItem = new ImageView(requireActivity());
			imageItem.setBackgroundResource(R.drawable.shadow);
			imageItem.setImageResource(devicesProfileResourcesTypedArray.getResourceId(i, -1));
			imageItem.setLayoutParams(new LinearLayout.LayoutParams(imageSize, imageSize));

			final int index = i;
			imageItem.setOnClickListener(view -> showDialog("profile", index));

			TileViewAnimator tileViewAnimator = new TileViewAnimator(requireContext());
			tileViewAnimator.addView(imageItem);
			mProfileCarouselContainer.addView(tileViewAnimator);
		}
		}
		return v;
	}

	public void showDialog(String type, int index) {

		FragmentManager fm = getParentFragmentManager();

		switch (type) {

			case "input":

				switch (index) {
					case 0:
						// NeuroSky MindWave Mobile
						DialogInputNeuroSkyMindWaveFragment dialogInputNeuroSkyMindWaveFragment = new DialogInputNeuroSkyMindWaveFragment();
						dialogInputNeuroSkyMindWaveFragment.show(fm, getResources().getString(R.string.title_dialog_fragment_neurosky_mindwave));
						break;
					case 1:
						// Emotiv Insight — SDK may not be present in this build; use reflection
						// so this file compiles regardless of whether the SDK JARs are available.
						try {
							androidx.fragment.app.DialogFragment dialogInputEmotivInsightFragment =
									(androidx.fragment.app.DialogFragment) Class
											.forName("io.puzzlebox.jigsaw.ui.DialogInputEmotivInsightFragment")
											.getDeclaredConstructor()
											.newInstance();
							dialogInputEmotivInsightFragment.show(fm, getResources().getString(R.string.title_dialog_fragment_emotiv_insight));
						} catch (ReflectiveOperationException e) {
							android.widget.Toast.makeText(requireActivity(),
									"Emotiv Insight SDK not available in this build",
									android.widget.Toast.LENGTH_SHORT).show();
						}
						break;
					case 2:
						// Joystick
						DialogInputJoystickFragment dialogInputJoystickFragment = new DialogInputJoystickFragment();
						dialogInputJoystickFragment.show(fm, getResources().getString(R.string.title_dialog_fragment_joystick));
						break;
				}
				break;

			case "output":

				switch (index) {
					case 0:
						// Audio IR Transmitter
						DialogOutputAudioIRFragment dialogOutputAudioIRFragment = new DialogOutputAudioIRFragment();
						dialogOutputAudioIRFragment.show(fm, getResources().getString(R.string.title_dialog_fragment_audio_ir));
						break;
					case 1:
						// Session Data
						DialogOutputSessionFragment dialogOutputSessionFragment = new DialogOutputSessionFragment();
						dialogOutputSessionFragment.show(fm, getResources().getString(R.string.title_fragment_session));
						break;
				}
				break;
		}
	}

}
