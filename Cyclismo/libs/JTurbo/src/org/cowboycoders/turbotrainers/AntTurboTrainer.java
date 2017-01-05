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
package org.cowboycoders.turbotrainers;

import org.cowboycoders.ant.Node;

import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public abstract class AntTurboTrainer extends GenericTurboTrainer {

  public final static Logger LOGGER = Logger.getLogger(AntTurboTrainer.class
      .getName());

  private Node node;

  public Node getNode() {
    return node;
  }

  public void setNode(Node node) { this.node = node; }

  public abstract void start() throws TooFewAntChannelsAvailableException,
      TurboCommunicationException, InterruptedException, TimeoutException;

  // dangerous at moment as we are using dataChangeListeners directly
//	protected void setDataChangeListeners(
//			Set<TurboTrainerDataListener> dataChangeListeners) {
//		this.dataChangeListeners = dataChangeListeners;
//	}


}
