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


import org.cowboycoders.ant.Node;
import org.cowboycoders.ant.interfaces.AntTransceiver;
import org.cowboycoders.ant.messages.StandardMessage;
import org.cowboycoders.ant.messages.commands.ResetMessage;
import org.cowboycoders.ant.utils.AntLoggerImpl;
import org.cowboycoders.ant.utils.SimplePidLogger;
import org.cowboycoders.pid.GainController;
import org.cowboycoders.pid.GainParameters;
import org.cowboycoders.pid.OutputControlParameters;
import org.cowboycoders.turbotrainers.Mode;
import org.cowboycoders.turbotrainers.TurboTrainerDataListener;
import org.cowboycoders.turbotrainers.bushido.brake.BushidoBrake;
import org.cowboycoders.turbotrainers.bushido.brake.BushidoBrake.VersionRequestCallback;
import org.cowboycoders.turbotrainers.bushido.brake.ConstantResistanceController;
import org.cowboycoders.turbotrainers.bushido.brake.SpeedPidBrakeController;
import org.cowboycoders.turbotrainers.bushido.brake.SpeedResistanceMapper;
import org.cowboycoders.turbotrainers.bushido.brake.SpeedResistancePowerMapper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.TimeoutException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;


public class BrakeControllerTest {

  private static AntTransceiver antchip = new AntTransceiver(0);

  @BeforeClass
  public static void beforeClass() {
    AntTransceiver.LOGGER.setLevel(Level.SEVERE);
    ConsoleHandler handler = new ConsoleHandler();
    // PUBLISH this level
    handler.setLevel(Level.ALL);
    AntTransceiver.LOGGER.addHandler(handler);
    //Node.LOGGER.setLevel(Level.ALL);
    //Node.LOGGER.addHandler(handler);
    StandardMessage msg = new ResetMessage();
    //StandardMessage msg = new BroadcastDataMessage();
    //antchip.start();
    //antchip.send(msg.encode());
    //antchip.send(msg.encode());
    //antchip.stop();
  }

  @AfterClass
  public static void afterClass() {
    antchip.stop();
    //antchip.stop();
  }

  @Before
  public void before() throws InterruptedException {
    //Thread.sleep(1000);
  }


  TurboTrainerDataListener dataListener = new TurboTrainerDataListener() {

    @Override
    public void onSpeedChange(double speed) {
      System.out.println("speed: " + speed);

    }

    @Override
    public void onPowerChange(double power) {
      System.out.println("power: " + power);

    }

    @Override
    public void onCadenceChange(double cadence) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onDistanceChange(double distance) {
      //System.out.println("Distance: " + distance);
      //System.out.println("Distance real: " + b.getRealDistance());

    }

    @Override
    public void onHeartRateChange(double heartRate) {
      // TODO Auto-generated method stub

    }


  };

  VersionRequestCallback versionCallback = new VersionRequestCallback() {

    @Override
    public void onVersionReceived(String versionString) {
      System.out.println(versionString);

    }

  };

  BushidoBrake b;

  AntLoggerImpl antLogger = new AntLoggerImpl();

  //@Test
  public void testRequestVersion() throws InterruptedException, TimeoutException {
    Node n = new Node(BrakeControllerTest.antchip);
    n.registerAntLogger(antLogger);
    SpeedPidBrakeController pid = new SpeedPidBrakeController();
    b = new BushidoBrake();
    b.setMode(Mode.TARGET_SLOPE);
    b.setNode(n);
    b.overrideDefaultResistanceController(pid);
    b.registerDataListener(dataListener);
    b.startConnection();

    for (int i = 0; i < 100; i++) {
      b.requestVersion(versionCallback);
      Thread.sleep(500);
    }

    b.stop();
    n.stop();
  }

  GainController gainController = new GainController() {

    @Override
    public GainParameters getGain(OutputControlParameters parameters) {
      //we could ease them until on target, so we don't get a sudden jerk
      //if (parameters.getSetPoint() < 7.4) {
      //	return new GainParameters(2,0.5,0);
      //}
      //return new GainParameters(-1.8,-0.5,-0.2);
      //return new GainParameters(-4.6,-0.8,-0.3); /ok
      return new GainParameters(-15.0, -0.5, 0.);
    }

  };

  //@Test
  public void testBrakeSlopeController() throws InterruptedException, TimeoutException {
    Node n = new Node(BrakeControllerTest.antchip);
    n.registerAntLogger(antLogger);
    SimplePidLogger pidLogger = new SimplePidLogger();
    SpeedPidBrakeController pid = new SpeedPidBrakeController();
    b = new BushidoBrake();
    b.setMode(Mode.TARGET_SLOPE);
    b.setNode(n);
    b.overrideDefaultResistanceController(pid);
    b.registerDataListener(dataListener);
    b.startConnection();
    pid.getPidParameterController().registerPidUpdateLister(pidLogger);
    pid.getPidParameterController().setGainController(gainController);
    pidLogger.newLog(pid.getPidParameterController());

    Thread.sleep(60000);

    b.stop();
    n.stop();
  }

  @Test
  public void testPolynomialController() throws InterruptedException, TimeoutException {
    Node n = new Node(BrakeControllerTest.antchip);
    n.registerAntLogger(antLogger);
    SpeedResistanceMapper mapper = new SpeedResistanceMapper();
    mapper.enableLogging(new File("./logs/polylog"));
    b = new BushidoBrake();
    b.setNode(n);
    b.setMode(Mode.TARGET_SLOPE);
    b.overrideDefaultResistanceController(mapper);
    b.registerDataListener(dataListener);
    b.startConnection();

    Thread.sleep(60000);

    b.stop();
    n.stop();
  }

  //@Test This is broken, because the surface fit mapper isn't supported / working
  public void testSurfaceFitController() throws InterruptedException, TimeoutException {
    Node n = new Node(BrakeControllerTest.antchip);
    n.registerAntLogger(antLogger);
    SpeedResistancePowerMapper mapper = new SpeedResistancePowerMapper();
    mapper.enableLogging(new File("./logs/surfacelog_newbounds"));
    b = new BushidoBrake();
    b.setMode(Mode.TARGET_SLOPE);
    b.setNode(n);
    b.overrideDefaultResistanceController(mapper);
    b.registerDataListener(dataListener);
    b.startConnection();

    Thread.sleep(240000);

    b.stop();
    n.stop();
  }

  //@Test
  public void testFixedResistanceCOntroller() throws InterruptedException, TimeoutException {
    Node n = new Node(BrakeControllerTest.antchip);
    n.registerAntLogger(antLogger);
    ConstantResistanceController mapper = new ConstantResistanceController();
    mapper.setAbsoluteResistance(1000);
    mapper.enableLogging(new File("./logs/constant_reslog"));
    b = new BushidoBrake();
    b.setMode(Mode.TARGET_SLOPE);
    b.setNode(n);
    b.overrideDefaultResistanceController(mapper);
    b.registerDataListener(dataListener);
    b.startConnection();

    Thread.sleep(60000);

    b.stop();
    n.stop();
  }


}
