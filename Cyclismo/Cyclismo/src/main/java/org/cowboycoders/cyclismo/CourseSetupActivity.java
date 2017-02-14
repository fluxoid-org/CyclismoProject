/*
*    Copyright (c) 2013, Will Szumski
*    Copyright (c) 2013, Doug Szumski
*
*    This file is part of Cyclismo.
*
*    Cyclismo is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    Cyclismo is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with Cyclismo.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.cowboycoders.cyclismo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import org.cowboycoders.cyclismo.content.Bike;
import org.cowboycoders.cyclismo.fragments.CourseSetupFragment;
import org.cowboycoders.cyclismo.fragments.CourseSetupFragment.CourseSetupObserver;
import org.cowboycoders.cyclismo.services.ITrackRecordingService;
import org.cowboycoders.cyclismo.services.TrackRecordingServiceConnection;
import org.cowboycoders.cyclismo.turbo.TurboService;
import org.cowboycoders.cyclismo.util.IntentUtils;
import org.cowboycoders.cyclismo.util.PreferencesUtils;


public class CourseSetupActivity extends Activity {

  public CourseSetupActivity() {
    super();
  }

  private static final String TAG = CourseSetupActivity.class.getSimpleName();

  protected Long trackId;
  protected String modeString;
  protected Integer constantCoursePower;
  private Bike bike;
  private String turboString;
  private Button goButton;
  private boolean startNewRecording = false;
  private boolean mIsBound;

  TurboService turboService;

  CourseSetupObserver courseSetupObserver = new CourseSetupFragment.CourseSetupObserver() {

    @Override
    public void onTrackIdUpdate(Long trackIdIn) {
      setTrackId(trackIdIn);
      validate();
    }

    @Override
    public void onCourseModeUpdate(String modeStringIn) {
      setModeString(modeStringIn);
      validate();
    }

    @Override
    public void onConstantCoursePowerUpdate(Integer power) {
      setConstantCoursePower(power);
      validate();
    }

    @Override
    public void onBikeUpdate(Bike bikeIn) {
      setBike(bikeIn);
      validate();
    }

    @Override
    public void onTurboUpdate(String turbo) {
      setTurboString(turbo);
      validate();
    }
  };

  /**
   * long reference assignment non atomic
   *
   * @return the trackId
   */
  private synchronized Long getTrackId() {
    return trackId;
  }

  /**
   * long reference assignment non atomic
   *
   * @param trackId the trackId to set
   */
  private synchronized void setTrackId(Long trackId) {
    Log.d(TAG, "setTrackID: " + trackId);
    this.trackId = trackId;
  }

  private String getTurboString() { return turboString; }

  private void setTurboString(String turboString) { this.turboString = turboString; }

  private String getModeString() {
    return modeString;
  }

  private void setModeString(String modeString) {
    this.modeString = modeString;
  }

  private void setConstantCoursePower(Integer power) {
    constantCoursePower = power;
  }

  private Integer getConstantCoursePower() {
    return constantCoursePower;
  }

  private ServiceConnection mConnection = new ServiceConnection() {

    public void onServiceConnected(ComponentName className, IBinder binder) {
      turboService = ((TurboService.TurboBinder) binder).getService();
      Toast.makeText(CourseSetupActivity.this, "Connected to turbo service",
          Toast.LENGTH_SHORT).show();
      CourseSetupFragment courseSetupFragment = new CourseSetupFragment();
      courseSetupFragment.addObserver(courseSetupObserver);
      getFragmentManager().beginTransaction().replace(
          R.id.course_select_preferences, courseSetupFragment).commit();
    }

    public void onServiceDisconnected(ComponentName className) {
      Toast.makeText(CourseSetupActivity.this, "Disconnected from turbo service",
          Toast.LENGTH_SHORT).show();
    }
  };

  void startTurboService() {
    if (mIsBound) {
      Log.d(TAG, "Starting turbo service");
      PreferencesUtils.setLong(CourseSetupActivity.this.getApplicationContext(),
          R.string.recording_course_track_id_key, getTrackId());
      turboService.start(getTrackId(), CourseSetupActivity.this);
      // no longer needed
      doUnbindTurboService();
      startRecording();
    } else {
      Log.e(TAG, "Turbo service not bound when attempting to start");
    }
  }

  void doUnbindTurboService() {
    if (mIsBound) {
      // Detach our existing connection.
      unbindService(mConnection);
      mIsBound = false;
    }
  }

  private void startServiceInBackround() {
    Intent intent = new Intent(this, TurboService.class);
    this.startService(intent);
  }

  private TrackRecordingServiceConnection trackRecordingServiceConnection;

  private Runnable bindChangedCallback = new Runnable() {

    @Override
    public void run() {

      boolean success = true;

      if (!startNewRecording) {
        return;
      }

      ITrackRecordingService service = trackRecordingServiceConnection.getServiceIfBound();
      if (service == null) {
        Log.d(TAG, "service not available to start a new recording");
        return;
      }
      try {
        long id = service.startNewTrack();
        service.pauseCurrentTrack();
        startNewRecording = false;
        Intent intent = IntentUtils.newIntent(CourseSetupActivity.this, TrackDetailActivity.class)
            .putExtra(TrackDetailActivity.EXTRA_TRACK_ID, id)
            .putExtra(TrackDetailActivity.EXTRA_USE_COURSE_PROVIDER, false)
            .putExtra(TrackDetailActivity.EXTRA_COURSE_TRACK_ID, trackId);
        startActivity(intent);
        Toast.makeText(
            CourseSetupActivity.this, R.string.track_list_record_success, Toast.LENGTH_SHORT).show();
      } catch (Exception e) {
        Toast.makeText(CourseSetupActivity.this, R.string.track_list_record_error, Toast.LENGTH_LONG)
            .show();
        Log.e(TAG, "Unable to start a new recording.", e);
        success = false;
      }

      CourseSetupActivity.this.finish(success);
    }

  };

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setVolumeControlStream(TextToSpeech.Engine.DEFAULT_STREAM);
    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
    this.setContentView(R.layout.course_select);

    trackRecordingServiceConnection = new TrackRecordingServiceConnection(
        this, bindChangedCallback);

    this.goButton = (Button) this.findViewById(R.id.course_select_go);
    goButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startServiceInBackround();
        startTurboService();
      }
    });
    Button cancelButton = (Button) this.findViewById(R.id.course_select_cancel);
    cancelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        CourseSetupActivity.this.finish(false);
      }
    });

  }

  private synchronized void setBike(Bike bike) {
    this.bike = bike;
  }

  private synchronized Bike getBike() {
    return bike;
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.d(TAG, "started");
    bindService(new Intent(this, TurboService.class), mConnection, Context.BIND_AUTO_CREATE);
    mIsBound = true;
  }

  protected void finish(boolean trackStarted) {
    Intent resultData = new Intent();
    if (trackStarted) {
      setResult(Activity.RESULT_OK, resultData);
    } else {
      setResult(Activity.RESULT_CANCELED, resultData);
    }
    finish();
  }


  /**
   * this disables/enables the go button
   */
  private void validate() {
    boolean valid = true;

    String modeString = getModeString();
    if (modeString == null || getTurboString() == null || getBike() == null ) {
      updateUi(false);
      return;
    }

    // Constant power mode requires a power to be set
    // TODO: This will need updating when course power tracking is updated
    if (modeString.equals(getString(R.string.settings_courses_mode_constant_power_value))) {
      Integer constantCoursePower = getConstantCoursePower();
      if (constantCoursePower == null || constantCoursePower < 0) {
        valid = false;
      }
    }

    // At the moment, all modes require a track. Later on we may have other modes which don't.
    Long localTrackId = getTrackId();
    if (localTrackId == null || localTrackId.equals(-1L)) {
      valid = false;
    }

    updateUi(valid);
  }

  @Override
  public void finish() {
    super.finish();
  }

  private void startRecording() {
    startNewRecording = true;
    trackRecordingServiceConnection.startAndBind();

    /*
     * If the binding has happened, then invoke the callback to start a new
     * recording. If the binding hasn't happened, then invoking the callback
     * will have no effect. But when the binding occurs, the callback will get
     * invoked.
     */
    bindChangedCallback.run();
  }

  @Override
  protected void onStop() {
    super.onStop();
    doUnbindTurboService();
    trackRecordingServiceConnection.unbind();
  }

  private void updateUi(final boolean valid) {
    Log.d(TAG, "updating ui: " + valid);
    this.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        goButton.setEnabled(valid);
      }
    });
  }

  public TurboService getTurboService() {
    if (mIsBound) {
      return turboService;
    } else {
      return null;
    }
  }
}
