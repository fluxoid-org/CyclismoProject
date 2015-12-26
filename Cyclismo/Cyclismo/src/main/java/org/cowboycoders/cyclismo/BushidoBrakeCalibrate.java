package org.cowboycoders.cyclismo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.cowboycoders.cyclismo.turbo.TurboService;
import org.cowboycoders.turbotrainers.TurboTrainerDataListener;
import org.cowboycoders.turbotrainers.bushido.brake.BushidoBrake;

import java.text.DecimalFormat;


public class BushidoBrakeCalibrate extends Activity {

  private boolean mIsBound = false;

  private BushidoBrake.CalibrationCallback calibrationCallback = new BushidoBrake.CalibrationCallback() {


    /**
     * Called when user is required to speed up to 40 km/h
     */
    @Override
    public void onRequestStartPedalling() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          statusMessage.setText(getString(R.string.bushido_brake_calibrate_start));
        }
      });

    }

    /**
     * Called once user has reached 25 mph (40 kmh). Must instruct user to
     * stop pedalling until requested.
     */
    @Override
    public void onReachedCalibrationSpeed() {
      //produce a beep to signal that the user should slow down
      ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
      toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500);
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          statusMessage.setText(getString(R.string.bushido_brake_calibrate_stop));
        }
      });
    }

    /**
     * Called after onReachedCalibrationSpeed() and once user has slowed
     * down to zero kmh. User must start pedalling to receive new
     * calibration value
     */
    @Override
    public void onRequestResumePedalling() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          statusMessage.setText(getString(R.string.bushido_brake_calibrate_resume));
        }
      });
    }

    /**
     * Only called on success
     *
     * @param calibrationValue new calibration value
     */
    @Override
    public void onSuccess(final double calibrationValue) {
      final String prefix = getString(R.string.bushido_brake_calibrate_calibration_value);
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          statusMessage.setText(prefix + calibrationValue);
        }
      });

    }

    /**
     * Only called if calibration fails
     *
     * @param exception reason for failure, may be caused by:
     *                  InterruptedException, TimeoutException
     */
    @Override
    public void onFailure(BushidoBrake.CalibrationException exception) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          statusMessage.setText(getString(R.string.bushido_brake_calibrate_failed));
        }
      });

    }

    /**
     * Called when below speed
     *
     * @param speed current Speed
     */
    @Override
    public void onBelowSpeedReminder(double speed) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          statusMessage.setText(getString(R.string.bushido_brake_calibrate_start));
        }
      });

    }
  };

  private TurboTrainerDataListener dataListener = new TurboTrainerDataListener() {
    @Override
    public void onSpeedChange(final double speed) {
      //TODO: unit conversion
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          DecimalFormat df = new DecimalFormat("#.00");
          speedView.setText(df.format(speed) + " km/h");
        }
      });
    }

    @Override
    public void onPowerChange(double power) {

    }

    @Override
    public void onCadenceChange(double cadence) {

    }

    @Override
    public void onDistanceChange(double distance) {

    }

    @Override
    public void onHeartRateChange(double heartRate) {

    }
  };

  private ServiceConnection mConnection = new ServiceConnection() {

    public void onServiceConnected(ComponentName className, IBinder binder) {
      TurboService s = ((TurboService.TurboBinder) binder).getService();
      s.startBushidoCalibrate(calibrationCallback, dataListener);
      Toast.makeText(BushidoBrakeCalibrate.this, "Connected to turbo service",
              Toast.LENGTH_SHORT).show();
      // no longer needed
      doUnbindService();
    }

    public void onServiceDisconnected(ComponentName className) {
      Toast.makeText(BushidoBrakeCalibrate.this, "Disconnected from turbo service",
              Toast.LENGTH_SHORT).show();
    }
  };

  private Button startButton;
  private Button finishButton;
  private boolean startButtonEnabled = true;
  private TextView statusMessage;
  private TextView speedView;


  void doBindService() {
    bindService(new Intent(this, TurboService.class), mConnection,
            Context.BIND_AUTO_CREATE);
    mIsBound = true;
  }

  void doUnbindService() {
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.bushido_brake_calibrate);

    this.startButton = (Button) this.findViewById(R.id.generic_edit_save);
    this.finishButton = (Button) this.findViewById(R.id.generic_edit_cancel);
    this.statusMessage = (TextView) this.findViewById(R.id.bushido_brake_calibrate_speed_title);
    this.speedView = (TextView) this.findViewById(R.id.bushido_brake_calibration_current_speed);
    startButton.setText(getString(R.string.generic_go));
    finishButton.setText(getString(R.string.generic_cancel));
    startButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        startButtonEnabled = false;
        updateUi();
        startServiceInBackround();
        doBindService();
      }

    });


    finishButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        startButtonEnabled = true;
        finish();
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();

  }

  private void updateUi() {
    this.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        startButton.setEnabled(startButtonEnabled);
      }

    });

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_bushido_brake_calibrate, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
