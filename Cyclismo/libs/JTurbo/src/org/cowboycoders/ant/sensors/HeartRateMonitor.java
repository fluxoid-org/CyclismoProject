package org.cowboycoders.ant.sensors;

import org.cowboycoders.ant.Channel;
import org.cowboycoders.ant.NetworkKeys;
import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.messages.ChannelType;
import org.cowboycoders.ant.messages.SlaveChannelType;
import org.cowboycoders.ant.messages.data.BroadcastDataMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: pairing with a specific device id
 * Created by fluxoid on 22/12/15.
 */
public class HeartRateMonitor {

  private final Node node;
  private boolean running = false;
  private List<HeartRateListener> listeners = new ArrayList<>();
  private Channel channel;


  private class AntListener implements BroadcastListener<BroadcastDataMessage> {

    /*
     * Once an instance of this class is registered with a channel,
     * this is called every time a broadcast message is received
     * on that channel.
     *
     * (non-Javadoc)
     * @see org.cowboycoders.ant.events.BroadcastListener#receiveMessage(java.lang.Object)
     */
    @Override
    public void receiveMessage(BroadcastDataMessage message) {
      /*
			 * getData() returns the 8 byte payload. The current heart rate
			 * is contained in the last byte.
			 *
			 * Note: remember the lack of unsigned bytes in java, so unsigned values
			 * should be converted to ints for any arithmetic / display - getUnsignedData()
			 * is a utility method to do this.
			 */
      synchronized (HeartRateMonitor.this) {
        for (HeartRateListener hrl : listeners) {
          hrl.onValueChange(message.getUnsignedData()[7]);
        }
      }
    }

  }

  /*
   * See ANT+ data sheet for explanation
   */
  private static final int HRM_CHANNEL_PERIOD = 8070;

  /*
   * See ANT+ data sheet for explanation
   */
  private static final int HRM_CHANNEL_FREQ = 57;

  /*
   * This should match the device you are connecting with.
   * Some devices are put into pairing mode (which sets this bit).
   *
   * Note: Many ANT+ sport devices do not set this bit (eg. HRM strap).
   *
   * See ANT+ docs.
   */
  private static final boolean HRM_PAIRING_FLAG = false;

  /*
   * Should match device transmission id (0-255). Special rules
   * apply for shared channels. See ANT+ protocol.
   *
   * 0: wildcard, matches any value (slave only)
   */
  private static final int HRM_TRANSMISSION_TYPE = 0;

  /*
   * device type for ANT+ heart rate monitor
   */
  private static final int HRM_DEVICE_TYPE = 120;

  /*
   * You should make a note of the device id and use it in preference to the wild card
   * to pair to a specific device.
   *
   * 0: wild card, matches all device ids
   * any other number: match specific device id
   */
  private static final int HRM_DEVICE_ID = 0;


  public HeartRateMonitor(Node node) {
    this.node = node;
  }

  public void start() {
    if (running) return;
    this.running = true;

        /* must be called before any configuration takes place */
    node.start();

    channel = node.getFreeChannel();

    // Arbitrary name : useful for identifying channel
    channel.setName("C:HRM");

    // choose slave or master type. Constructors exist to set two-way/one-way and shared/non-shared variants.
    ChannelType channelType = new SlaveChannelType();

    // use ant network key "N:ANT+"
    channel.assign(NetworkKeys.ANT_SPORT, channelType);

    // registers an instance of our callback with the channel
    channel.registerRxListener(new AntListener(), BroadcastDataMessage.class);

    /******* start device specific configuration ******/

    channel.setId(HRM_DEVICE_ID, HRM_DEVICE_TYPE, HRM_TRANSMISSION_TYPE, HRM_PAIRING_FLAG);

    channel.setFrequency(HRM_CHANNEL_FREQ);

    channel.setPeriod(HRM_CHANNEL_PERIOD);

    /******* end device specific configuration ******/

    // timeout before we give up looking for device
    channel.setSearchTimeout(Channel.SEARCH_TIMEOUT_NEVER);

    // start listening
    channel.open();

  }

  public void stop() {
    if (!running) return;

    // stop listening
    channel.close();


    // resets channel configuration
    channel.unassign();

    //return the channel to the pool of available channels
    node.freeChannel(channel);
  }

  public synchronized void registerListener(HeartRateListener hrl) {
    listeners.add(hrl);
  }
}
