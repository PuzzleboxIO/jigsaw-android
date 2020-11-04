package io.puzzlebox.jigsaw.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
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

	/**
	 * Input carousel container layout
	 */
	private LinearLayout mInputCarouselContainer;
	private LinearLayout mOutputCarouselContainer;
	private LinearLayout mProfileCarouselContainer;

	private OnFragmentInteractionListener mListener;

	public static TilesFragment newInstance() {
		TilesFragment fragment = new TilesFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}
	public TilesFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ProfileSingleton.getInstance().parseXML(getContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_tiles, container, false);

		// Get reference to carousel container
		mInputCarouselContainer = v.findViewById(R.id.carousel_devices_input);
		mOutputCarouselContainer = v.findViewById(R.id.carousel_devices_output);
		mProfileCarouselContainer = v.findViewById(R.id.carousel_devices_profile);


		// Compute the width of a carousel item based on the screen width and number of initial items.
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int imageWidth = (int) (displayMetrics.widthPixels / INITIAL_ITEMS_COUNT);

		// Get the array of input devices resources
		final TypedArray devicesInputResourcesTypedArray = getResources().obtainTypedArray(R.array.devices_input_icon_array);
		final TypedArray devicesOutputResourcesTypedArray = getResources().obtainTypedArray(R.array.devices_output_icon_array);
		final TypedArray devicesProfileResourcesTypedArray = getResources().obtainTypedArray(R.array.devices_profile_array);

		ImageView imageItem;

		// Populate the input devices carousel with items
		for (int i = 0 ; i < devicesInputResourcesTypedArray.length() ; ++i) {

			final int index = i;

			// Create new ImageView
			imageItem = new ImageView(getActivity());

			// Set the shadow background
			imageItem.setBackgroundResource(R.drawable.shadow);

			// Set the image view resource
			imageItem.setImageResource(devicesInputResourcesTypedArray.getResourceId(i, -1));

			// Set the size of the image view to the previously computed value
			imageItem.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageWidth));

			imageItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog("input", index);
				}
			});

			TileViewAnimator tileViewAnimator = new TileViewAnimator(getContext());
			tileViewAnimator.addView(imageItem);

			/// Add image view to the carousel container
			mInputCarouselContainer.addView(tileViewAnimator);
		}

		// Populate the output devices carousel with items
		for (int i = 0 ; i < devicesOutputResourcesTypedArray.length() ; ++i) {
			imageItem = new ImageView(getActivity());
			imageItem.setBackgroundResource(R.drawable.shadow);
			imageItem.setImageResource(devicesOutputResourcesTypedArray.getResourceId(i, -1));
			imageItem.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageWidth));

			final int index = i;
			imageItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog("output", index);
				}
			});

			TileViewAnimator tileViewAnimator = new TileViewAnimator(getContext());
			tileViewAnimator.addView(imageItem);

			mOutputCarouselContainer.addView(tileViewAnimator);
		}

		// Populate the device profile carousel with items
		for (int i = 0 ; i < devicesProfileResourcesTypedArray.length() ; ++i) {
			imageItem = new ImageView(getActivity());
			imageItem.setBackgroundResource(R.drawable.shadow);
			imageItem.setImageResource(devicesProfileResourcesTypedArray.getResourceId(i, -1));
			imageItem.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageWidth));

			final int index = i;
			imageItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog("profile", index);
				}
			});

			TileViewAnimator tileViewAnimator = new TileViewAnimator(getContext());
			tileViewAnimator.addView(imageItem);
			mProfileCarouselContainer.addView(tileViewAnimator);
		}
		return v;
	}

	public void showDialog(String type, int index) {

		FragmentManager fm = getFragmentManager();

		switch (type) {

			case "input":

				switch (index) {
					case 0:
						// NeuroSky MindWave Mobile
						DialogInputNeuroSkyMindWaveFragment dialogInputNeuroSkyMindWaveFragment = new DialogInputNeuroSkyMindWaveFragment();
						dialogInputNeuroSkyMindWaveFragment.show(fm, getResources().getString(R.string.title_dialog_fragment_neurosky_mindwave));
						break;
					case 1:
						// Emotiv Insight
						DialogInputEmotivInsightFragment dialogInputEmotivInsightFragment = new DialogInputEmotivInsightFragment();
						dialogInputEmotivInsightFragment.show(fm, getResources().getString(R.string.title_dialog_fragment_emotiv_insight));
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
}
