jigsaw-android
============


Puzzlebox Jigsaw for Android


Copyright Puzzlebox Productions, LLC (2015-2023)
Copyright Steve Castellotti (2023-2026)

by Puzzlebox Productions, LLC
http://puzzlebox.io/jigsaw


License: GNU Affero General Public License v3.0
https://www.gnu.org/licenses/agpl-3.0.html


============

Download APK:

Google Play: https://play.google.com/store/apps/details?id=io.puzzlebox.jigsaw.android


============

Instructions:

Third-party SDK libraries are optional. The build succeeds without them; features
that depend on a missing SDK are gracefully disabled at runtime with a toast message.

Place SDK files in `jigsaw/libs/` as listed below.

---

**NeuroSky MindWave Mobile EEG**
https://store.neurosky.com/products/android-developer-tools-4

```
jigsaw/libs/ThinkGear.jar
jigsaw/libs/libStreamSDK_v1.2.0.jar
jigsaw/libs/NskAlgoSdk.jar
jigsaw/src/release/jniLibs/armeabi-v7a/libNSUART.so
jigsaw/src/release/jniLibs/armeabi-v7a/libNskAlgoSdk.so
```

SDK presence is detected by checking for `libStreamSDK_v1.2.0.jar`.
When absent, `NeuroSkyThinkGearService.java` is excluded from compilation.

---

**Emotiv EPOC / Emotiv Insight**
https://www.emotiv.com/developer/

```
jigsaw/libs/community-3.3.4.aar
jigsaw/libs/community-emotiv-classes.jar
jigsaw/src/release/jniLibs/armeabi-v7a/libbedk.so
```

SDK presence is detected by checking for `community-emotiv-classes.jar`.
When absent, all Emotiv source files are excluded from compilation and the AAR
is not linked. The `community-3.3.4.aar` is also referenced by `orbit-android`
via its `jigsaw` symlink.

---

**InterAxon Muse** *(currently disabled)*
https://sites.google.com/a/interaxon.ca/muse-developer-site/download

```
jigsaw/libs/libmuseandroid.jar
```

SDK presence is detected by checking for `libmuseandroid.jar`.
When absent, `InteraXonMuseService.java` is excluded from compilation.


============

Open Source libraries (included):

AndroidPlot
http://androidplot.com/download/

opencsv
http://sourceforge.net/projects/opencsv/

usb-serial-for-android
https://github.com/mik3y/usb-serial-for-android/releases
