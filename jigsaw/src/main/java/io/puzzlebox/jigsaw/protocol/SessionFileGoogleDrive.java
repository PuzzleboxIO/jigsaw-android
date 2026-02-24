package io.puzzlebox.jigsaw.protocol;

import android.app.Activity;

/**
 * Google Drive integration — stubbed out.
 *
 * The original implementation used the deprecated Drive Android API
 * (com.google.android.gms.drive) which was shut down in December 2019.
 * The replacement is the Google Drive REST API via the Google API Client Library
 * or the Storage Access Framework (SAF), which requires a full rewrite.
 *
 * This class is retained as a stub to avoid breaking the build. The activity
 * entry in AndroidManifest.xml remains commented out.
 */
public class SessionFileGoogleDrive extends Activity {
    // TODO: Reimplement using Google Drive REST API or SAF if Drive export is needed.
}
