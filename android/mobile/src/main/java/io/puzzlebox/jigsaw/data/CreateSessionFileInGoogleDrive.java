package io.puzzlebox.jigsaw.data;

/**
 * Created by sc on 4/24/15.
 */
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
//		  import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;

/**
 * Android Drive Quickstart activity. This activity takes a photo and saves it
 * in Google Drive. The user is prompted with a pre-made dialog which allows
 * them to choose the file location.
 */
public class CreateSessionFileInGoogleDrive extends Activity implements ConnectionCallbacks,
		  OnConnectionFailedListener {

	private static final String TAG = CreateSessionFileInGoogleDrive.class.getSimpleName();
	private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
	private static final int REQUEST_CODE_CREATOR = 2;
	private static final int REQUEST_CODE_RESOLUTION = 3;

	private GoogleApiClient mGoogleApiClient;
	//	private Bitmap mBitmapToSave;
	private String dataToSave;

	/**
	 * Create a new file and save it to Drive.
	 */
	private void saveFileToDrive() {
		// Start by creating a new contents, and setting a callback.
		Log.i(TAG, "Creating new contents.");

//		final Bitmap image = mBitmapToSave;
//		final String data = dataToSave;

//		final String dataFile = SessionSingleton.getInstance().getExportDataCSV().toString();
		final String dataFile = SessionSingleton.getInstance().getExportDataCSV();
//		dataToSave = dataFile;
//		final String dataFile = dataToSave;

		Drive.DriveApi.newDriveContents(mGoogleApiClient)
				  .setResultCallback(new ResultCallback<DriveContentsResult>() {

					  @Override
					  public void onResult(DriveContentsResult result) {
						  // If the operation was not successful, we cannot do anything
						  // and must
						  // fail.
						  if (!result.getStatus().isSuccess()) {
							  Log.i(TAG, "Failed to create new contents.");
							  return;
						  }
						  // Otherwise, we can write our data to the new contents.
						  Log.i(TAG, "New contents created.");
						  // Get an output stream for the contents.
						  OutputStream outputStream = result.getDriveContents().getOutputStream();
						  // Write the bitmap data from it.
//						  ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();

//						  image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);

//						  Log.e(TAG, "SessionSingleton.getInstance().getExportDataCSV().toString(): " + SessionSingleton.getInstance().getExportDataCSV());
//						  Log.e(TAG, "dataFile:" + dataFile);

						  try {
//							  outputStream.write(bitmapStream.toByteArray());
							  outputStream.write(dataFile.getBytes(Charset.forName("UTF-8")));
//							  outputStream.write(dataFile.get);
						  } catch (IOException e1) {
							  Log.i(TAG, "Unable to write file contents.");
						  }

						  // Create the initial metadata - MIME type and title.
						  // Note that the user will be able to change the title later.
//						  MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
//									 .setMimeType("image/jpeg").setTitle("Android Photo.png").build();


						  MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
									 .setTitle(SessionSingleton.getInstance().getTimestampPS4() + ".csv")
									 .setMimeType("text/csv")
									 .build();


						  // Create an intent for the file chooser, and start it.
						  IntentSender intentSender = Drive.DriveApi
									 .newCreateFileActivityBuilder()
									 .setInitialMetadata(metadataChangeSet)
									 .setInitialDriveContents(result.getDriveContents())
									 .build(mGoogleApiClient);
						  try {
							  startIntentSenderForResult(
										 intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
						  } catch (SendIntentException e) {
							  Log.i(TAG, "Failed to launch file chooser.");
						  }
					  }
				  });
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mGoogleApiClient == null) {
			// Create the API client and bind it to an instance variable.
			// We use this instance as the callback for connection and connection
			// failures.
			// Since no account name is passed, the user is prompted to choose.
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					  .addApi(Drive.API)
					  .addScope(Drive.SCOPE_FILE)
					  .addConnectionCallbacks(this)
					  .addOnConnectionFailedListener(this)
					  .build();
		}
		// Connect the client. Once connected, the camera is launched.
		mGoogleApiClient.connect();
	}

	@Override
	protected void onPause() {
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
		super.onPause();
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		Log.e(TAG, "onActivityResult: " + requestCode);
		switch (requestCode) {
			case REQUEST_CODE_CAPTURE_IMAGE:
				// Called after a photo has been taken.
				if (resultCode == Activity.RESULT_OK) {
					// Store the image data as a bitmap for writing later.
//					mBitmapToSave = (Bitmap) data.getExtras().get("data");
					Log.e(TAG, "mBitmapToSave = (Bitmap) data.getExtras().get(\"data\");");
					Log.e(TAG, "dataToSave = SessionSingleton.getInstance().getExportDataCSV();");
					dataToSave = SessionSingleton.getInstance().getExportDataCSV();
				}
				break;
			case REQUEST_CODE_CREATOR:
				// Called after a file is saved to Drive.
				if (resultCode == RESULT_OK) {
					Log.i(TAG, "Image successfully saved.");
//					mBitmapToSave = null;
					dataToSave = null;
					Log.e(TAG, "dataToSave = null;");
					// Just start the camera again for another photo.
//					startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
//							  REQUEST_CODE_CAPTURE_IMAGE);

				} else {
					Log.e(TAG, "Warning: Error saving file to drive: " + resultCode);
				}

				this.finish();

				break;
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Called whenever the API client fails to connect.
		Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
		if (!result.hasResolution()) {
			// show the localized error dialog.
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
			return;
		}
		// The failure has a resolution. Resolve it.
		// Called typically when the app is not yet authorized, and an
		// authorization
		// dialog is displayed to the user.
		try {
			result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
		} catch (SendIntentException e) {
			Log.e(TAG, "Exception while starting resolution activity", e);
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "API client connected.");
//		if (mBitmapToSave == null) {
//			// This activity has no UI of its own. Just start the camera.
//			startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
//					  REQUEST_CODE_CAPTURE_IMAGE);
//			return;
//		}

//		if (dataToSave == null) {
//			Log.e(TAG, "onConnected() (dataToSave == null)");
//		 return;
//		}

		saveFileToDrive();
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.i(TAG, "GoogleApiClient connection suspended");
	}
}