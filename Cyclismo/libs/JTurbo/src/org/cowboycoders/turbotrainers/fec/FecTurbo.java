/*
 *    Copyright (c) 2017, Will Szumski
 *    Copyright (c) 2017, Doug Szumski
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
package org.cowboycoders.turbotrainers.fec;

import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.events.BroadcastListener;
import org.cowboycoders.ant.profiles.FecProfile;
import org.cowboycoders.ant.profiles.common.FilteredBroadcastMessenger;
import org.cowboycoders.ant.profiles.common.events.BufferedEventPrioritiser;
import org.cowboycoders.ant.profiles.common.events.CadenceUpdate;
import org.cowboycoders.ant.profiles.common.events.CoastEvent;
import org.cowboycoders.ant.profiles.common.events.DistanceUpdate;
import org.cowboycoders.ant.profiles.common.events.EventPrioritiser;
import org.cowboycoders.ant.profiles.common.events.HeartRateUpdate;
import org.cowboycoders.ant.profiles.common.events.InstantPowerUpdate;
import org.cowboycoders.ant.profiles.common.events.PrioritisedEventBuilder;
import org.cowboycoders.ant.profiles.common.events.SpeedUpdate;
import org.cowboycoders.ant.profiles.common.events.interfaces.TaggedTelemetryEvent;
import org.cowboycoders.ant.profiles.fitnessequipment.Capabilities;
import org.cowboycoders.ant.profiles.fitnessequipment.Config;
import org.cowboycoders.ant.profiles.fitnessequipment.Defines;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.CalibrationProgress;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.CalibrationResponse;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.GeneralData;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.TorqueData;
import org.cowboycoders.ant.profiles.fitnessequipment.pages.TrainerData;
import org.cowboycoders.turbotrainers.AntTurboTrainer;
import org.cowboycoders.turbotrainers.Mode;
import org.cowboycoders.turbotrainers.Parameters.TargetPower;
import org.cowboycoders.turbotrainers.Parameters.TargetSlope;
import org.cowboycoders.turbotrainers.Parameters.CommonParametersInterface;
import org.cowboycoders.turbotrainers.TurboTrainerDataListener;
import org.fluxoid.utils.FixedPeriodUpdater;
import org.fluxoid.utils.FixedPeriodUpdaterWithReset;
import org.fluxoid.utils.UpdateCallback;

import java.util.EnumSet;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class FecTurbo extends AntTurboTrainer {

  public static final Mode[] SUPPORTED_MODES = new Mode[]{
      Mode.TARGET_POWER, Mode.TARGET_SLOPE,
  };
  public final static Logger LOGGER = Logger.getLogger(FecTurbo.class
      .getName());
  private static final int UPDATE_PERIOD_MS = 1000;
  // Should be > update period from turbo (2s for Bushido)
  private static final int RESET_PERIOD_MS = 5000;
  private FecProfile fecProfile;

  private TurboTrainerDataListener dispatchListener = new TurboTrainerDataListener() {
    @Override
    public void onSpeedChange(double speedKmph) {
      synchronized (dataChangeListeners) {
        for (TurboTrainerDataListener dcl : dataChangeListeners) {
          dcl.onSpeedChange(speedKmph);
        }
      }
    }

    @Override
    public void onPowerChange(final double powerWatts) {
      synchronized (dataChangeListeners) {
        for (TurboTrainerDataListener dcl : dataChangeListeners) {
          dcl.onPowerChange(powerWatts);
        }
      }
    }

    @Override
    public void onCadenceChange(final double cadenceRpm) {
      synchronized (dataChangeListeners) {
        for (TurboTrainerDataListener dcl : dataChangeListeners) {
          dcl.onCadenceChange(cadenceRpm);
        }
      }
    }

    @Override
    public void onDistanceChange(final double distanceMeters) {
      synchronized (dataChangeListeners) {
        for (TurboTrainerDataListener dcl : dataChangeListeners) {
          dcl.onDistanceChange(distanceMeters);
        }
      }
    }

    @Override
    public void onHeartRateChange(double heartRateBpm) {
      // Not supported
    }
  };

  private FixedPeriodUpdater speedUpdater = new FixedPeriodUpdaterWithReset(0.0, new
      UpdateCallback() {
        @Override
        public void onUpdate(Object newValue) {
          dispatchListener.onSpeedChange((Double) newValue);
        }
      }, UPDATE_PERIOD_MS, RESET_PERIOD_MS) {
    @Override
    public Object getResetValue() {
      return 0.0;
    }
  };

  private FixedPeriodUpdater powerUpdater = new FixedPeriodUpdaterWithReset(0.0, new
      UpdateCallback() {
        @Override
        public void onUpdate(Object newValue) {
          dispatchListener.onPowerChange((Double) newValue);
        }
      }, UPDATE_PERIOD_MS, RESET_PERIOD_MS) {
    @Override
    public Object getResetValue() {
      return 0.0;
    }
  };

  private FixedPeriodUpdater cadenceUpdater = new FixedPeriodUpdaterWithReset(0.0, new
      UpdateCallback() {
        @Override
        public void onUpdate(Object newValue) {
          dispatchListener.onCadenceChange((Double) newValue);
        }
      }, UPDATE_PERIOD_MS, RESET_PERIOD_MS) {
    @Override
    public Object getResetValue() {
      return 0.0;
    }
  };

  {
    setSupportedModes(SUPPORTED_MODES);
  }

  public void start() throws InterruptedException, TimeoutException {
    Node node = getNode();
    // FIXME: Sometimes the channels aren't freed after use. Stop the node as a workaround for now.
    node.stop();
    node.start();

    fecProfile = new FecProfile() {
      @Override
      public void onEquipmentStateChange(Defines.EquipmentState equipmentState, Defines
          .EquipmentState equipmentState1) {
        //TODO
      }

      @Override
      public void onCapabilitiesReceived(Capabilities capabilities) {
        //TODO
      }

      @Override
      public void onConfigRecieved(Config config) {
        //TODO
      }

      @Override
      public void onCalibrationUpdate(CalibrationProgress calibrationProgress) {
        //TODO
      }

      @Override
      public void onCalibrationStatusReceieved(CalibrationResponse calibrationResponse) {
        //TODO
      }

      @Override
      public void onConnect() {
        //TODO
      }

      @Override
      public void onDisconnect() {
        //TODO
      }

      @Override
      public void onStatusChange(EnumSet<Defines.TrainerStatusFlag> oldStatus, EnumSet<Defines
          .TrainerStatusFlag> newStatus) {
        //TODO
      }
    };
    fecProfile.start(node);

    FilteredBroadcastMessenger prioritisedBus = new FilteredBroadcastMessenger();
    EventPrioritiser.PrioritisedEvent[] priorities = new EventPrioritiser.PrioritisedEvent[]{
        new PrioritisedEventBuilder(SpeedUpdate.class)
            .setTagPriorities(GeneralData.class, TorqueData.class)
            .createPrioritisedEvent(),
        new PrioritisedEventBuilder(DistanceUpdate.class)
            .setTagPriorities(GeneralData.class, TorqueData.class)
            .createPrioritisedEvent(),
        new PrioritisedEventBuilder(CoastEvent.class)
            .setTagPriorities(TrainerData.class, TorqueData.class)
            .createInheritedPrioritisedEvent()
    };
    BufferedEventPrioritiser prioritiser = new BufferedEventPrioritiser(prioritisedBus, priorities);
    fecProfile.getDataHub().addListener(TaggedTelemetryEvent.class, prioritiser);

    prioritisedBus.addListener(SpeedUpdate.class, new BroadcastListener<SpeedUpdate>() {
      @Override
      public void receiveMessage(SpeedUpdate message) {
        speedUpdater.update(message.getSpeed().doubleValue());
      }
    });

    prioritisedBus.addListener(InstantPowerUpdate.class, new
        BroadcastListener<InstantPowerUpdate>() {
          @Override
          public void receiveMessage(InstantPowerUpdate message) {
            powerUpdater.update(message.getPower().doubleValue());
          }
        });

    prioritisedBus.addListener(CadenceUpdate.class, new BroadcastListener<CadenceUpdate>() {
      @Override
      public void receiveMessage(CadenceUpdate message) {
        cadenceUpdater.update((double) message.getCadence());
      }
    });

    prioritisedBus.addListener(DistanceUpdate.class, new BroadcastListener<DistanceUpdate>() {
      @Override
      public void receiveMessage(DistanceUpdate message) {
        dispatchListener.onDistanceChange(message.getDistance().doubleValue());
      }
    });

    prioritisedBus.addListener(HeartRateUpdate.class, new BroadcastListener<HeartRateUpdate>() {
      @Override
      public void receiveMessage(HeartRateUpdate message) {
        dispatchListener.onHeartRateChange(message.getHeartRate());
      }
    });

    //FIXME: these need to be called after the line above. DO NOT MOVE.
    //This is because start(model) sets the data model on the resistance controller.
    //These calls delegate to the resistance controller. If the resistance controller
    //calls getDataModel() we dereference a null pointer.
    speedUpdater.start();
    powerUpdater.start();
    cadenceUpdater.start();
  }

  public void stop() throws InterruptedException, TimeoutException {
    // TODO: Implement
    //fecProfile.stop();
  }

  @Override
  public boolean supportsSpeed() {
    return true;
  }

  @Override
  public boolean supportsPower() {
    return true;
  }

  @Override
  public boolean supportsCadence() {
    return true;
  }

  @Override
  public boolean supportsHeartRate() {
    return false;
  }

  @Override
  public void setParameters(CommonParametersInterface parameters)
      throws IllegalArgumentException {
    if (parameters instanceof TargetPower) {
      TargetPower cast = (TargetPower) parameters;
      fecProfile.setTargetPower(cast.getPower());
    } else if (parameters instanceof TargetSlope) {
      TargetSlope cast = (TargetSlope) parameters;
      // Set gradient in percent - it seems not all FEC turbos behave this way?
      fecProfile.setBasicResistance(cast.getSlope());
    }
    // TODO: Implement target speed. See TurboService.
    // The FEC turbo should automatically switch modes according to the quantity set in the profile.
    // Later we plan to support Cyclismo models, rather than relying on a FEC blackbox.
  }
}