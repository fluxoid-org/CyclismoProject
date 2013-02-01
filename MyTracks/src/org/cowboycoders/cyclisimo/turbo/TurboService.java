package org.cowboycoders.cyclisimo.turbo;

import bushido.BushidoHeadunit;
import bushido.TurboCommunicationException;
import bushido.TurboTrainerDataListener;
import bushido.TurboTrainerInterface;

import org.cowboycoders.cyclisimo.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.cowboycoders.ant.Node;
import org.cowboycoders.cyclisimo.TrackEditActivity;
import org.cowboycoders.cyclisimo.services.TrackRecordingServiceConnection;
import org.cowboycoders.cyclisimo.util.IntentUtils;
import org.cowboycoders.cyclisimo.util.PreferencesUtils;
import org.cowboycoders.cyclisimo.util.TrackRecordingServiceConnectionUtils;
import org.cowboycoders.cyclisimo.util.UnitConversions;
import org.cowboycoders.location.LatLongAlt;

public class TurboService extends Service {
  
  private static final int RESULT_ERROR = 0;
  
  private Binder turboBinder = new TurboBinder();
  
  public static String TAG = "TurboService";
  
  public static String COURSE_TRACK_ID = "COURSE_TRACK_ID";
  
  public static double TARGET_TRACKPOINT_DISTANCE_METRES = 5;
  
  private final static String MOCK_LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;//LocationManager.NETWORK_PROVIDER; 
  
  private static int GPS_ACCURACY = 5; //m
  
  private List<LatLongAlt> latLongAlts;

  private boolean running = false;

  private double distanceBetweenPoints;
  
  private double lastSubmittedDistance = 0;
  
  private int currentLatLongIndex = 1;
  
  private boolean mIsBound = false;
  
  private Node antNode;
  
  public static int ONGOING_NOTIFICATION = 1786;
  
  double lastRecordedSpeed = 0.0; //kmh
  

  private TurboTrainerInterface turboTrainer;
  
  TurboTrainerDataListener dataListener = new TurboTrainerDataListener() {

    @Override
    public void onSpeedChange(double speed) {
      Intent intent = new Intent(getString(R.string.sensor_data_speed_kmh));
      intent.putExtra(getString(R.string.sensor_data_double_value),speed);
      sendBroadcast(intent);
      lastRecordedSpeed = speed;
    }

    @Override
    public void onPowerChange(double power) {
      Intent intent = new Intent(getString(R.string.sensor_data_power));
      intent.putExtra(getString(R.string.sensor_data_double_value),power);
      sendBroadcast(intent);
    }

    @Override
    public void onCadenceChange(double cadence) {
      Log.d(TAG, "cadence: " + cadence);
      Intent intent = new Intent(getString(R.string.sensor_data_cadence));
      intent.putExtra(getString(R.string.sensor_data_double_value),cadence);
      sendBroadcast(intent);
    }

    @Override
    public void onDistanceChange(double distance) {
      Log.d(TAG,"distance:" + distance);
      if (distance - lastSubmittedDistance >= distanceBetweenPoints) {
        LatLongAlt currentLocation = latLongAlts.get(currentLatLongIndex);
        updateLocation(currentLocation);
        lastSubmittedDistance = distance;
        if (currentLatLongIndex >= latLongAlts.size() -2 ) {
          // allow cool down
          turboTrainer.setSlope(0);
          doFinish();
        }
        double gradient = org.cowboycoders.location.LocationUtils
            .getLocalisedGradient(latLongAlts.get(currentLatLongIndex), latLongAlts.get(currentLatLongIndex + 1));
        turboTrainer.setSlope(gradient);
        Log.d(TAG,"New Gradient: " + gradient);
        distanceBetweenPoints = org.cowboycoders.location.LocationUtils.distance(latLongAlts.get(currentLatLongIndex), latLongAlts.get(currentLatLongIndex +1));
        Log.d(TAG,"distance between points: " + distanceBetweenPoints);
        currentLatLongIndex++;
      }
      
    }

    @Override
    public void onHeartRateChange(double heartRate) {
      if (heartRate > 0.) {
        Intent intent = new Intent(getString(R.string.sensor_data_heart_rate));
        intent.putExtra(getString(R.string.sensor_data_double_value),heartRate);
        sendBroadcast(intent);
      }
    }
    
  };
  
  public void doFinish() {
    long recordingTrackId = PreferencesUtils.getLong(
        this, R.string.recording_track_id_key);
    //if (recordingTrackId != PreferencesUtils.RECORDING_TRACK_ID_DEFAULT) {
      Intent intent = IntentUtils.newIntent(getBaseContext(), TrackEditActivity.class)
          .putExtra(TrackEditActivity.EXTRA_TRACK_ID, recordingTrackId)
          .putExtra(TrackEditActivity.EXTRA_NEW_TRACK, true)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      getApplication().startActivity(intent);
      TrackRecordingServiceConnectionUtils.stopRecording(this, trackRecordingServiceConnection, false);
    //}
    this.stopSelf();
  }
  
  void doUnbindService() {
    if (mIsBound) {
        unbindService(mConnection);
        mIsBound = false;
    }
}

  
  /* (non-Javadoc)
   * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    return START_STICKY;
  }


  public synchronized void start(final long trackId, final Context context) throws TurboCommunicationException {
    if (running ) {
      return;
    }
    running = true;
        
        context.getClass();
        CourseLoader cl = new CourseLoader(context,trackId);
        try {
          latLongAlts = cl.getLatLongAlts();
        } catch (InterruptedException e) {
          running = false;
          String error = "interrupted whilst loading course";
          Log.e(TAG,error);
          this.stopSelfResult(TurboService.RESULT_ERROR);
          throw new TurboCommunicationException(error);
        }
        
        latLongAlts = org.cowboycoders.location.LocationUtils.interpolatePoints(latLongAlts, TARGET_TRACKPOINT_DISTANCE_METRES);
        
        this.distanceBetweenPoints = org.cowboycoders.location.LocationUtils.distance(latLongAlts.get(0), latLongAlts.get(1));
            
        Log.d(TAG,"latlong length: " + latLongAlts.size());
        
        Log.d(TAG,"distance between points: " + distanceBetweenPoints);
        
        enableMockLocations();
        
        // start in background as otherwise it is destroyed in onDestory() before we can disconnect
        startServiceInBackround();
        
        doBindService();
        
        registerRecordingReceiver();
        
        Intent notificationIntent = new Intent(this, TurboService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        
        Notification noti = new Notification.Builder(this)
        .setContentTitle(getString(R.string.turbo_mode_service_running_notification_title))
        .setContentText(getString(R.string.turbo_mode_service_running_notification_text))
        .setSmallIcon(R.drawable.track_bike)
        .setContentIntent(pendingIntent)
        .setOngoing(true)
        .getNotification(); // build api 16 only ;/
            
        startForeground(ONGOING_NOTIFICATION, noti);
        
        //currentLatLongIndex = latLongAlts.size() -3; puts in near end so we can test ending
        
       
    
  }
  
  private ServiceConnection mConnection = new ServiceConnection() {


    public void onServiceConnected(ComponentName className, IBinder binder) {
      AntHubService s = ((AntHubService.LocalBinder) binder).getService();
      antNode = s.getNode();
      TurboService.this.turboTrainer = new BushidoHeadunit(antNode);
  
      
      
      new Thread() {
        public void run() {
          try {
            turboTrainer.start();
            turboTrainer.registerDataListener(dataListener);
            updateLocation(latLongAlts.get(0));
          } catch (InterruptedException e1) {
            throw new TurboCommunicationException(e1);
          } catch (TimeoutException e1) {
            throw new TurboCommunicationException(e1);
          }
        }
      }.start();
 
      
      
      Toast.makeText(TurboService.this, "Connected to AntHub service",
          Toast.LENGTH_SHORT).show();
    }

    public void onServiceDisconnected(ComponentName className) {
      Toast.makeText(TurboService.this, "Connected to AntHub service",
          Toast.LENGTH_SHORT).show();
  }
  
  };

  private TrackRecordingServiceConnection trackRecordingServiceConnection;

  
  void doBindService() {
    bindService(new Intent(this, AntHubService.class), mConnection,
        Context.BIND_AUTO_CREATE);
    mIsBound  = true;
  }
  
  private void startServiceInBackround() {
    Intent intent = new Intent(this, AntHubService.class);
    this.startService(intent);
  }
  
  private void enableMockLocations() {
    LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    if (locationManager.isProviderEnabled(MOCK_LOCATION_PROVIDER)) {
      try {
      locationManager.addTestProvider (MOCK_LOCATION_PROVIDER,
          "requiresNetwork" == "",
          "requiresSatellite" == "",
          "requiresCell" == "",
          "hasMonetaryCost" == "",
          "supportsAltitude" == "",
          "supportsSpeed" == "",
          "supportsBearing" == "",
           android.location.Criteria.POWER_LOW,
           android.location.Criteria.ACCURACY_FINE);
      
      locationManager.setTestProviderEnabled(MOCK_LOCATION_PROVIDER, true);
      } catch (SecurityException e) {
        //TODO : ADD NOTIFICATION
        this.doFinish();
        Log.e(TAG,e.toString());
      } 
    }
  }
  
  private void disableMockLocations() {
    LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      try {
      // is this the same as the below? probably
      locationManager.setTestProviderEnabled(MOCK_LOCATION_PROVIDER, false);
      locationManager.clearTestProviderEnabled(MOCK_LOCATION_PROVIDER);
      locationManager.clearTestProviderLocation(MOCK_LOCATION_PROVIDER);
      locationManager.clearTestProviderStatus(MOCK_LOCATION_PROVIDER);
      locationManager.removeTestProvider(MOCK_LOCATION_PROVIDER);
      } catch (SecurityException e) {
        //TODO : ADD NOTIFICATION
        Log.e(TAG,e.toString());
      } 
    }
  }
  

  private synchronized void updateLocation(LatLongAlt pos) {
   LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
   if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
     try {
     float locSpeed = (float) (lastRecordedSpeed / UnitConversions.MS_TO_KMH);
     Location loc = new Location(MOCK_LOCATION_PROVIDER);
     Log.d(TAG,"alt: " + pos.getAltitude());
     Log.d(TAG,"lat: " + pos.getLatitude());
     Log.d(TAG,"long: " + pos.getLongitude());
     loc.setLatitude(pos.getLatitude());
     loc.setLongitude(pos.getLongitude());
     loc.setAltitude(pos.getAltitude());
     loc.setTime(System.currentTimeMillis());
     loc.setSpeed(locSpeed);
     loc.setAccuracy(GPS_ACCURACY);
     locationManager.setTestProviderStatus(MOCK_LOCATION_PROVIDER,
         LocationProvider.AVAILABLE, null, System.currentTimeMillis());
     locationManager.setTestProviderLocation(MOCK_LOCATION_PROVIDER, loc);
     Log.e(TAG,"updated location");
     } catch (SecurityException e) {
       //TDO: ADD NOTIFICATION HERE
       this.doFinish();
       Log.e(TAG,e.toString());
     } 
     
     return;
   }
   Log.e(TAG,"no gps provider");
    
  }
  
  @Override
  public synchronized void onDestroy() {
    unregisterRecordingReceiver();
    disableMockLocations();
    running = false;
    
    new Thread() {
      public void run() {
        shutDownTurbo();
      }
    }.start();
    
    // no longer need ant
    this.doUnbindService();
    
    super.onDestroy();
  }
  
  // this takes time
  private boolean shutDownTurbo() {
    boolean shutDownSuccess = true;
    if (turboTrainer != null) {
      try {
        turboTrainer.unregisterDataListener(dataListener);
        turboTrainer.stop();
      } catch (InterruptedException e) {
        Log.e(TAG,"Interrupted stopping turbo trainer link");
        shutDownSuccess = false;
      } catch (TimeoutException e) {
        shutDownSuccess = false;
        Log.e(TAG,"Timeout stopping turbo trainer");
      }
      
      String shutdownMessage;
      if (shutDownSuccess) {
        shutdownMessage = "Shutdown turbo trainer sucessfully";
      } else {
        shutdownMessage = "Error shutting down turbo trainer";
      }
      
      // NOU IN UI
      //Toast.makeText(this.getBaseContext(), shutdownMessage, Toast.LENGTH_LONG).show();
      Log.i(TAG,shutdownMessage);
      
    }
    Intent intent = new Intent().setAction(this.getString(R.string.anthub_action_shutdown));
    sendBroadcast(intent);
    return shutDownSuccess;
    
  }

  public class TurboBinder extends Binder {
    public TurboService getService() {
      return TurboService.this;
    }
  }


  @Override
  public IBinder onBind(Intent arg0) {
    return turboBinder;
  }

  /* (non-Javadoc)
   * @see android.app.Service#onCreate()
   */
  @Override
  public void onCreate() {
    super.onCreate();
    trackRecordingServiceConnection = new TrackRecordingServiceConnection(
        this, bindChangedCallback);
  }
  
  // can th is be null
  private final Runnable bindChangedCallback = new Runnable() {
    @Override
  public void run() {
    // After binding changes (is available), update the total time in
    // trackController.
  }
};
  
  // start track controller stuff

private final BroadcastReceiver receiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
     String action = intent.getAction();
     if(action.equals(TRACK_STOPPED_ACTION)){
       TurboService.this.doFinish();
     }
     else if(action.equals(TRACK_PAUSED_ACTION)){
          //paused
     }     
  }
};


private static String TRACK_STOPPED_ACTION;
private static String TRACK_PAUSED_ACTION;

public void registerRecordingReceiver() {
  TRACK_STOPPED_ACTION = TurboService.this.getString(R.string.track_stopped_broadcast_action);
  TRACK_PAUSED_ACTION = TurboService.this.getString(R.string.track_paused_broadcast_action);
  IntentFilter filter = new IntentFilter();
  filter.addAction(TRACK_STOPPED_ACTION);
  filter.addAction(TRACK_PAUSED_ACTION);

  registerReceiver(receiver, filter);
}

public void unregisterRecordingReceiver() {
  unregisterReceiver(receiver);
}

//if (context.getString(R.string.track_paused_broadcast_action).equals(action)
//    || context.getString(R.string.track_resumed_broadcast_action).equals(action)
//    || context.getString(R.string.track_started_broadcast_action).equals(action)
//    || context.getString(R.string.track_stopped_broadcast_action).equals(action)
//    || context.getString(R.string.track_update_broadcast_action).equals(action)) 
//  


}
