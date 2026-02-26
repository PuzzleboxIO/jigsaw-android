package io.puzzlebox.jigsaw.ui;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import io.puzzlebox.jigsaw.R;

public class SupportFragment extends Fragment {

	private final static String TAG = SupportFragment.class.getSimpleName();

	/**
	 * Configuration
	 */
	static String contactURL = "http://puzzlebox.io/cgi-bin/puzzlebox/support_contact/puzzlebox_orbit_support_gateway.py";
	String versionName = "";

	/**
	 * UI
	 */
	EditText editTextName;
	EditText editTextEmail;
	EditText editTextMessage;
	Button buttonSendMessage;

	private OnFragmentInteractionListener mListener;

	public interface OnFragmentInteractionListener {
		void onFragmentInteraction(Uri uri);
	}

	public SupportFragment() {
		// Required empty public constructor
	}

	public static SupportFragment newInstance() {
		SupportFragment fragment = new SupportFragment();
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

		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_support, container, false);

		try {
			versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(),0).versionName;
		}
		catch (PackageManager.NameNotFoundException e) {
			Log.v(TAG, e.getMessage());
		}

		editTextName = v.findViewById(R.id.editTextName);
		editTextEmail = v.findViewById(R.id.editTextEmail);
		editTextMessage = v.findViewById(R.id.editTextMessage);

		buttonSendMessage = v.findViewById(R.id.buttonSendMessage);
		buttonSendMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
		return v;
	}

	@Override
	public void onAttach(@NonNull Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public void sendMessage() {
		Toast.makeText(getActivity().getApplicationContext(), "Thank you for your feedback!", Toast.LENGTH_LONG).show();

		String name = editTextName.getText().toString();
		String email = editTextEmail.getText().toString();
		String message = editTextMessage.getText().toString();

		message = message + "\n\n" + getDeviceDetails();

		EmailMessage emailMessage = new EmailMessage();

		emailMessage.setData(contactURL, name, email, message);

		emailMessage.execute();

		editTextName.setText("");
		editTextEmail.setText("");
		editTextMessage.setText("");
	}

	public String getDeviceDetails() {

		String output;
		output = "Manufacturer: " + Build.MANUFACTURER + "\n";
		output = output + "Model: " + Build.MODEL + "\n";
		output = output + "Product: " + Build.PRODUCT + "\n";
		output = output + "Hardware: " + Build.HARDWARE + "\n";
		output = output + "Device: " + Build.DEVICE + "\n";
		output = output +
				getResources().getString(R.string.app_name) +
				" Version: " + versionName + "\n";
		output = output + "Android Version: " + Build.VERSION.SDK_INT + "\n";

		return (output);
	}

	@SuppressWarnings("deprecation") // AsyncTask deprecated in API 30, but functional through API 36
	private class EmailMessage extends AsyncTask<String, Void, Object> {

		String contactURL = "";
		String name = "";
		String email = "";
		String message = "";

		public void setData(String contact, String full_name, String email_address, String content) {
			contactURL = contact;
			name = full_name;
			email = email_address;
			message = content;
		}

		protected Object doInBackground(String... vars) {
			HttpURLConnection connection = null;
			try {
				String subject = "[" + getResources().getString(R.string.app_name)
						+ " Support] (Android " + versionName + ")";

				// Build URL-encoded POST body (replaces Apache HttpClient)
				String postBody = URLEncoder.encode("email_name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8")
						+ "&" + URLEncoder.encode("email_from", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8")
						+ "&" + URLEncoder.encode("email_subject", "UTF-8") + "=" + URLEncoder.encode(subject, "UTF-8")
						+ "&" + URLEncoder.encode("email_body", "UTF-8") + "=" + URLEncoder.encode(message, "UTF-8");

				byte[] postBytes = postBody.getBytes(StandardCharsets.UTF_8);

				URL url = new URL(contactURL);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setFixedLengthStreamingMode(postBytes.length);
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.connect();

				try (OutputStream os = connection.getOutputStream()) {
					os.write(postBytes);
				}

				int responseCode = connection.getResponseCode();
				Log.d(TAG, "Support email response code: " + responseCode);

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
			return null;
		}
	}
}
