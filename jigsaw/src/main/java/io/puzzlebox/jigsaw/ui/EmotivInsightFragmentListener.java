package io.puzzlebox.jigsaw.ui;

import android.net.Uri;

/**
 * Standalone listener interface for Emotiv Insight dialog fragments.
 *
 * Defined in its own file (outside the SDK-dependent fragment classes) so that
 * host Activities can declare {@code implements EmotivInsightFragmentListener}
 * and compile successfully even when the Emotiv SDK JARs are absent from the
 * build and the actual fragment classes are excluded from compilation.
 *
 * Both {@link DialogInputEmotivInsightFragment} and
 * {@link DialogProfilePuzzleboxOrbitEmotivInsightFragment} use this interface
 * in their {@code onAttach} callbacks.
 */
public interface EmotivInsightFragmentListener {
    void onFragmentInteraction(Uri uri);
}
