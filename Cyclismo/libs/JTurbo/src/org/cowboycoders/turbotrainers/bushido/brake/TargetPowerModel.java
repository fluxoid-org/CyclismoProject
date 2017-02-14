package org.cowboycoders.turbotrainers.bushido.brake;

import org.cowboycoders.turbotrainers.Parameters;
import org.cowboycoders.turbotrainers.Parameters.CommonParametersInterface;

public class TargetPowerModel extends BrakeModel {

  @Override
  public void setParameters(CommonParametersInterface parameters)
      throws IllegalArgumentException {
    Parameters.TargetPower castParameters;
    try {
      castParameters = (Parameters.TargetPower) parameters;
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Expecting target power", e);
    }
    // TODO: If this has been set before we don't need to set it?
    setTotalWeight(castParameters.getTotalWeight());
    setTargetPower(castParameters.getPower());
  }

}
