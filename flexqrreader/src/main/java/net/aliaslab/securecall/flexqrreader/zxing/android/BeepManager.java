/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.aliaslab.securecall.flexqrreader.zxing.android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

import androidx.preference.PreferenceManager;

import net.aliaslab.securecall.flexqrreader.R;

import java.io.Closeable;
import java.io.IOException;


/**
 * Manages beeps and vibrations for {@link CaptureActivity}.
 */
final class BeepManager implements MediaPlayer.OnErrorListener, Closeable {

  private static final String TAG = BeepManager.class.getSimpleName();

  private static final float BEEP_VOLUME = 0.10f;
  private static final long VIBRATE_DURATION = 200L;

  private final Activity activity;
  private MediaPlayer mediaPlayer;
  private boolean playBeep;
  private boolean vibrate;

  BeepManager(Activity activity) {
    this.activity = activity;
    this.mediaPlayer = null;
    updatePrefs();
  }

  synchronized void updatePrefs() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    playBeep = shouldBeep(prefs, activity);
    vibrate = false;
    if (playBeep && mediaPlayer == null) {
      // The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
      // so we now play on the music stream.
      activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
      mediaPlayer = buildMediaPlayer(activity);
    }
  }

  synchronized void playBeepSoundAndVibrate() {
    if (playBeep && mediaPlayer != null) {
      mediaPlayer.start();
    }
    if (vibrate) {
      Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
      vibrator.vibrate(VIBRATE_DURATION);
    }
  }

  private static boolean shouldBeep(SharedPreferences prefs, Context activity) {
    boolean shouldPlayBeep = true;
    if (shouldPlayBeep) {
      // See if sound settings overrides this
      AudioManager audioService = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
      if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
        shouldPlayBeep = false;
      }
    }
    return shouldPlayBeep;
  }

  private MediaPlayer buildMediaPlayer(Context activity) {
    MediaPlayer mediaPlayer = new MediaPlayer();
    try (AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.beep)) {
      mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
      mediaPlayer.setOnErrorListener(this);
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      mediaPlayer.setLooping(false);
      mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
      mediaPlayer.prepare();
      return mediaPlayer;
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
      mediaPlayer.release();
      return null;
    }
  }

  @Override
  public synchronized boolean onError(MediaPlayer mp, int what, int extra) {
    if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
      // we are finished, so put up an appropriate error toast if required and finish
      activity.finish();
    } else {
      // possibly media player error, so release and recreate
      close();
      updatePrefs();
    }
    return true;
  }

  @Override
  public synchronized void close() {
    if (mediaPlayer != null) {
      mediaPlayer.release();
      mediaPlayer = null;
    }
  }

}
