package io.puzzlebox.jigsaw.data;

import android.util.Log;

/**
 * Shared static state for the NeuroSky ThinkGear connection.
 ** <p>
 * All public fields and constants live here rather than in
 * NeuroSkyThinkGearService so that UI classes can read them without importing
 * the SDK-dependent service class. This allows the project to compile even
 * when the NeuroSky JARs are absent — in that case NeuroSkyThinkGearService
 * is excluded from the build via jigsaw/build.gradle sourceSets.
 */
public final class NeuroSkyEegState {

    private static final String TAG = "NeuroSkyEegState";

    // ── Connection state ──────────────────────────────────────────────────────
    public static boolean eegConnected  = false;
    public static boolean eegConnecting = false;

    // ── EEG metrics (0–100 scale after normalisation) ─────────────────────────
    public static int eegAttention  = 0;
    public static int eegMeditation = 0;
    public static int eegPower      = 0;
    public static int eegSignal     = 0;

    // ── Constants (mirrored from NeuroSkyThinkGearService for UI use) ─────────
    public static final int     EEG_RAW_FREQUENCY   = 512; // 512 Hz sample rate
    public static final boolean rawEnabled           = true;
    public static final int     blinkRangeMax        = 128;

    private NeuroSkyEegState() {}

    // ── Disconnect helper ─────────────────────────────────────────────────────

    /**
     * Delegates to {@code NeuroSkyThinkGearService.disconnectHeadset()} via
     * reflection so callers do not need a compile-time reference to the
     * SDK-dependent service class.
     ** <p>
     * If the SDK is absent from this build the state fields are reset directly.
     */
    public static void disconnectIfConnected() {
        try {
            Class<?> cls = Class.forName("io.puzzlebox.jigsaw.service.NeuroSkyThinkGearService");
            cls.getMethod("disconnectHeadset").invoke(null);
        } catch (ClassNotFoundException e) {
            // SDK not present — reset state manually
            eegConnected  = false;
            eegConnecting = false;
            eegAttention  = 0;
            eegMeditation = 0;
            eegSignal     = 0;
            eegPower      = 0;
        } catch (Exception e) {
            Log.e(TAG, "disconnectIfConnected failed", e);
        }
    }
}
