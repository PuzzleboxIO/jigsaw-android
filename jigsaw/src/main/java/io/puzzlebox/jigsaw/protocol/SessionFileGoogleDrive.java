package io.puzzlebox.jigsaw.protocol;

import android.app.Activity;

/**
 * Google Drive integration — stubbed out.
 *
 * The original implementation used the deprecated Drive Android API
 * (com.google.android.gms.drive) which was shut down in December 2019.
 *
 * Export to Google Drive (and other storage providers) is now handled via the
 * Storage Access Framework (SAF) in DialogOutputSessionFragment.exportSession().
 * The system file picker surfaces Google Drive automatically when installed —
 * no OAuth, API keys, or this class are needed.
 *
 * This class is retained as a stub to avoid breaking the build. The activity
 * entry in AndroidManifest.xml remains commented out.
 */
public class SessionFileGoogleDrive extends Activity {
}
