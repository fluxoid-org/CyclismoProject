package com.google.android.apps.mytracks.turbo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.interfaces.AndroidAntTransceiver;

/**
 * Local service
 * @author will
 *
 */
public class AntHubService extends Service {
  
  private Node node;
  private AndroidAntTransceiver transceiver;
  
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

  private static final String TAG = "AntHub  - Service.";
  
  public class LocalBinder extends Binder
  {
      public AntHubService getService()
      {
          return AntHubService.this;
      }
  }
  
  private final LocalBinder mBinder = new LocalBinder();
  

  @Override
  public IBinder onBind(Intent intent)
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
  public boolean onUnbind(Intent intent)
  {
      Log.i(TAG, "All clients unbound.");
      // TODO Auto-generated method stub
      super.onUnbind(intent);
      return true;
  }

  @Override
  public void onCreate()
  {
      Log.i(TAG, "Service created.");
      super.onCreate();
      this.transceiver = new AndroidAntTransceiver(this);
      this.node = new Node(transceiver);
  }

  

  /* (non-Javadoc)
   * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    handleCommand(intent);
    return START_STICKY;
  }

  private void handleCommand(Intent intent) {
    // TODO Auto-generated method stub
  }

  @Override
  public void onDestroy()
  {
      this.node.stop();
      super.onDestroy();
      Log.i(TAG, "Service destroyed.");
  }



}
