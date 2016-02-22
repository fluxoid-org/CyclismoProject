/*
*    Copyright (c) 2016, Will Szumski
*    Copyright (c) 2016, Doug Szumski
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
package org.cowboycoders.cyclismo.turbo;

import org.cowboycoders.turbotrainers.AntTurboTrainer;
import org.cowboycoders.turbotrainers.TurboTrainerInterface;

import java.util.HashMap;
import java.util.Map;

public class TurboRegistry {
  private final Map<String, TurboTrainerInterface> turbos = new HashMap<>();

  public void register(String turboName, TurboTrainerInterface genericTurboTrainer) {
    turbos.put(turboName, genericTurboTrainer);
  }

  public boolean usesAnt(String turboName) {
    TurboTrainerInterface turbo = getTurboTrainer(turboName);
    return turbo instanceof AntTurboTrainer;
  }

  public TurboTrainerInterface getTurboTrainer(String turboName) {
    return turbos.get(turboName);
  }

}
