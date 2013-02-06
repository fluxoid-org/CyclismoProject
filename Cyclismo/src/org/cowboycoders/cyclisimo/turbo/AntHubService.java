package org.cowboycoders.cyclisimo.turbo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.interfaces.AndroidAntTransceiver;
import org.cowboycoders.cyclisimo.R;

/**
 * Local service
 * @author will
 *
 */
public class AntHubService extends Service {
  
  private Node node;
  private AndroidAntTransceiver transceiver;
  private static String WAKE_LOCK = AntHubService.class.getSimpleName();
  
  //private AntHubService() {
  //  super();
  //}
  
//  private static AntHub sharedHub = null;
  
//  public synchronized static AntHub getInstance(Context context) {
//    if(sharedHub == null) {
//      sharedHub = new AntHub(context);
//    }
//    return sharedHub;
//  }

  /**
   * @return the node
   */
  public Node getNode() {
    return node;
  }
  
  /**
   * @return the node
   */
  public AndroidAntTransceiver getTransceiver() {
    return transceiver;
  }

  private static final String TAG = "AntHub  - Service.";
  
  public class LocalBinder extends Binder
  {
      public AntHubService getService()
      {
          return AntHubService.this;
      }
  }
  
  private final LocalBinder mBinder = new LocalBinder();
  private int startServiceCount;
  private WakeLock wakeLock;
  
  @Override
  public synchronized IBinder onBind(Intent intent)
  {   
      Log.i(TAG, "First Client bound.");
      return mBinder;
  }

  @Override
  public void onRebind(Intent intent)
  {
      Log.i(TAG, "Client rebound");
      super.onRebind(intent);
  }

  @Override
  public synchronized boolean onUnbind(Intent intent)
  {   
      Log.i(TAG, "All clients unbound.");
      // TODO Auto-generated method stub
      super.onUnbind(intent);
      doFinish();
      return false;
  }

  @Override
  public void onCreate()
  {
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    this.wakeLock = pm.newWakeLock(
           PowerManager.SCREEN_DIM_WAKE_LOCK, AntHubService.WAKE_LOCK);
    this.wakeLock.acquire();
      Log.i(TAG, "Service created.");
      super.onCreate();
      this.transceiver = new AndroidAntTransceiver(this);
      this.node = new Node(transceiver);
      registerRecordingReceiver();
  }
  

  

  /* (non-Javadoc)
   * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
   */
  @Override
  public synchronized int onStartCommand(Intent intent, int flags, int startId) {
    this.startServiceCount ++;
    //throw new UnsupportedOperationException("use bind instead");
    // don't support sticky mode yet
    // would need broadcast receiver to shut down?
    //handleCommand(intent);
    return START_STICKY;
    //return super.onStartCommand(intent, flags, startId);
  }

  private void handleCommand(Intent intent) {
    // TODO Auto-generated method stub
  }
  
  @Override
  public void onDestroy()
  { 
      this.wakeLock.release();
      this.node.stop();
      unregisterRecordingReceiver();
      super.onDestroy();
      Log.i(TAG, "Service destroyed.");
  }

  private final BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
       String action = intent.getAction();
       if(action.equals(ANT_KILL_ACTION)){
         --startServiceCount;
         doFinish();
         // each startService() should be paired with sending and intent when finished
       }
    }
  };


  private static String ANT_KILL_ACTION;

  public void registerRecordingReceiver() {
    ANT_KILL_ACTION = AntHubService.this.getString(R.string.anthub_action_shutdown);
    IntentFilter filter = new IntentFilter();
    filter.addAction(ANT_KILL_ACTION);

    registerReceiver(receiver, filter);
  }

  protected void doFinish() {
    if (startServiceCount <= 0) {
      this.stopSelf();
    }
    
  }

  public void unregisterRecordingReceiver() {
    unregisterReceiver(receiver);
  }

}
