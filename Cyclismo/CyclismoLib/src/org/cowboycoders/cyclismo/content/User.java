/*
*    Copyright (c) 2013,2016, Will Szumski
*    Copyright (c) 2013,2016, Doug Szumski
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
package org.cowboycoders.cyclismo.content;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

  private String name;
  private long id = -1L;
  private double weight = -1l;
  private byte[] settings;
  private long currentlySelectedBike = -1l;

  public User() {

  }

  private User(Parcel in) {
    id = in.readLong();
    name = in.readString();
    weight = in.readDouble();
    currentlySelectedBike = in.readLong();
    int settingsLength = in.readInt();
    if (settingsLength > 0) {
      settings = new byte[in.readInt()];
      in.readByteArray(settings);
    }
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(id);
    dest.writeString(name);
    dest.writeDouble(weight);
    dest.writeLong(currentlySelectedBike);

    dest.writeInt(settings == null ? 0 : settings.length);
    if (settings != null) {
      dest.writeByteArray(settings);
    }
  }

  public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
    @Override
    public User createFromParcel(Parcel in) {
      return new User(in);
    }

    @Override
    public User[] newArray(int size) {
      return new User[size];
    }

  };

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * Weight in kg
   *
   * @return weight
   */
  public double getWeight() {
    return weight;
  }

  /**
   * Weight in kg
   */
  public void setWeight(double weight) {
    this.weight = weight;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getCurrentlySelectedBike() {
    return currentlySelectedBike;
  }

  public void setCurrentlySelectedBike(long currentlySelectedBike) {
    this.currentlySelectedBike = currentlySelectedBike;
  }

  public byte[] getSettings() {
    return settings;
  }

  public void setSettings(byte[] newSettings) {
    this.settings = newSettings;
  }
}
