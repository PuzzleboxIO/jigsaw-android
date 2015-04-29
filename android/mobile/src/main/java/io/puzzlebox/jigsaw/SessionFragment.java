package io.puzzlebox.jigsaw;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import io.puzzlebox.jigsaw.data.SessionSingleton;

public class SessionFragment extends Fragment {

	private final static String TAG = SessionFragment.class.getSimpleName();

	private OnFragmentInteractionListener mListener;

	public static SessionFragment newInstance(String param1, String param2) {
		SessionFragment fragment = new SessionFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}


	// ################################################################

	public SessionFragment() {
		// Required empty public constructor
	}


	// ################################################################

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	// ################################################################

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_session, container, false);

		Button exportToCSV = (Button) v.findViewById(R.id.buttonExportCSV);
		exportToCSV.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "SessionSingleton.getInstance().exportDataToCSV");
//				SessionSingleton.getInstance().exportDataToCSV(SessionSingleton.getInstance().getTimestampPS4());
				SessionSingleton.getInstance().exportDataToCSV(null, null);
			}
		});

		Button resetSession = (Button) v.findViewById(R.id.buttonResetSession);
		resetSession.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "SessionSingleton.getInstance().resetSession()");
				SessionSingleton.getInstance().resetSession();

				Toast.makeText((getActivity()),
						  "Session data reset",
						  Toast.LENGTH_SHORT).show();
			}
		});

		return v;

	}


	// ################################################################

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					  + " must implement OnFragmentInteractionListener");
		}
	}


	// ################################################################

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}


	// ################################################################

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		public void onFragmentInteraction(Uri uri);
	}


}
