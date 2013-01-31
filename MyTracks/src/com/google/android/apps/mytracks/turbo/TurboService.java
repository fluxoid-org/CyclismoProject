package com.google.android.apps.mytracks.turbo;

import bushido.BushidoHeadunit;
import bushido.TurboCommunicationException;
import bushido.TurboTrainerDataListener;
import bushido.TurboTrainerInterface;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import org.cowboycoders.location.LatLongAlt;

public class TurboService extends Service {
  
  private static final int RESULT_ERROR = 0;
  
  private Binder turboBinder = new TurboBinder();
  
  public static String TAG = "TurboService";
  
  public static String COURSE_TRACK_ID = "COURSE_TRACK_ID";
  
  private List<LatLongAlt> latLongAlts;

  private boolean running = false;

  private double distanceBetweenPoints;
  
  private double lastSubmittedDistance = 0;
  
  private int currentLatLongIndex = 0;

  private TurboTrainerInterface turboTrainer;
  
  TurboTrainerDataListener dataListener = new TurboTrainerDataListener() {

    @Override
    public void onSpeedChange(double speed) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void onPowerChange(double power) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void onCadenceChange(double cadence) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void onDistanceChange(double distance) {
      if (distance - lastSubmittedDistance >= distanceBetweenPoints) {
        LatLongAlt currentLocation = latLongAlts.get(currentLatLongIndex++);
        updateLocation(currentLocation);
        lastSubmittedDistance = distance;
      }
      
    }

    @Override
    public void onHeartRateChange(double heartRate) {
      // TODO Auto-generated method stub
      
    }
    
  };
  
  
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
        
        latLongAlts = org.cowboycoders.location.LocationUtils.interpolatePoints(latLongAlts, 5);
        
        //any members will do as points are equi-spaced
        this.distanceBetweenPoints = org.cowboycoders.location.LocationUtils.distance(latLongAlts.get(0), latLongAlts.get(1));
            
        
        Log.d(TAG,"latlong length: " + latLongAlts.size());
        
        startServiceInBackround();
        
        doBindService();
       
    
  }
  
  private ServiceConnection mConnection = new ServiceConnection() {

    public void onServiceConnected(ComponentName className, IBinder binder) {
      AntHubService s = ((AntHubService.LocalBinder) binder).getService();
      Node antNode = s.getNode();
      TurboService.this.turboTrainer = new BushidoHeadunit(antNode);
      
      try {
        turboTrainer.start();
      } catch (InterruptedException e1) {
        throw new TurboCommunicationException(e1);
      } catch (TimeoutException e1) {
        throw new TurboCommunicationException(e1);
      }
      Toast.makeText(TurboService.this, "Connected to AntHub service",
          Toast.LENGTH_SHORT).show();
    }

    public void onServiceDisconnected(ComponentName className) {
      Toast.makeText(TurboService.this, "Connected to AntHub service",
          Toast.LENGTH_SHORT).show();
  }
  
  };
  
  void doBindService() {
    bindService(new Intent(this, AntHubService.class), mConnection,
        Context.BIND_AUTO_CREATE);
  }
  
  private void startServiceInBackround() {
    Intent intent = new Intent(this, AntHubService.class);
    this.startService(intent);
  }
  

  private synchronized void updateLocation(LatLongAlt pos) {
    String mocLocationProvider = LocationManager.GPS_PROVIDER;//LocationManager.NETWORK_PROVIDER; 
   LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
   if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
     try {
     locationManager.addTestProvider (LocationManager.GPS_PROVIDER,
         "requiresNetwork" == "",
         "requiresSatellite" == "",
         "requiresCell" == "",
         "hasMonetaryCost" == "",
         "supportsAltitude" == "",
         "supportsSpeed" == "",
         "supportsBearing" == "",
          android.location.Criteria.POWER_LOW,
          android.location.Criteria.ACCURACY_FINE);
     
     locationManager.setTestProviderEnabled(mocLocationProvider, true);
     Location loc = new Location(mocLocationProvider);
     Log.d(TAG,"alt: " + pos.getAltitude());
     Log.d(TAG,"lat: " + pos.getLatitude());
     Log.d(TAG,"long: " + pos.getLongitude());
     loc.setLatitude(pos.getLatitude());
     loc.setLongitude(pos.getLongitude());
     loc.setAltitude(pos.getAltitude());
     loc.setTime(System.currentTimeMillis());
     loc.setAccuracy(10);
     locationManager.setTestProviderStatus(mocLocationProvider,
         LocationProvider.AVAILABLE, null, System.currentTimeMillis());
     locationManager.setTestProviderLocation(mocLocationProvider, loc);
     Log.e(TAG,"updated location");
     } catch (SecurityException e) {
       running = false;
       this.stopSelfResult(TurboService.RESULT_ERROR);
       Log.e(TAG,e.toString());
     } 
     
     return;
   }
   Log.e(TAG,"no gps provider");
    
  }
  
  @Override
  public synchronized void onDestroy() {
    boolean shutDownSucess = true;
    if (turboTrainer != null) {
      try {
        turboTrainer.unregisterDataListener(dataListener);
        turboTrainer.stop();
      } catch (InterruptedException e) {
        Log.e(TAG,"Interrupted stopping turbo trainer link");
        shutDownSucess = false;
      } catch (TimeoutException e) {
        shutDownSucess = false;
        Log.e(TAG,"Timeout stopping turbo trainer");
      }
    }
    String shutdownMessage;
    if (shutDownSucess) {
      shutdownMessage = "Shutdown turbo trainer sucessfully";
    } else {
      shutdownMessage = "Error shutting down turbo trainer";
    }
    
    Toast.makeText(this.getBaseContext(), shutdownMessage, Toast.LENGTH_LONG).show();
    
    super.onDestroy();
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

}
