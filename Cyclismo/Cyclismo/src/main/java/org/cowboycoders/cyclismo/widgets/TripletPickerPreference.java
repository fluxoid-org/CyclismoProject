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

package org.cowboycoders.cyclismo.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import org.cowboycoders.cyclismo.R;

import java.util.ArrayList;
import java.util.List;


public class TripletPickerPreference extends DialogPreference {
  private final int DEFAULT_MAX = 999;
  private final int DEFAULT_MIN = 1;
  private final int DEFAULT_VALUE = 0;
  private final int DIGIT_MAX = 9;
  private final int DIGIT_MIN = 0;

  private int min, max;

  private List<NumberPicker> digits;

  public TripletPickerPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray tripletPickerType = context.obtainStyledAttributes(attrs,
        R.styleable.TripletPickerPreference, 0, 0);

    max = tripletPickerType.getInt(R.styleable.TripletPickerPreference_max, DEFAULT_MAX);
    min = tripletPickerType.getInt(R.styleable.TripletPickerPreference_min, DEFAULT_MIN);

    tripletPickerType.recycle();
  }

  @Override
  protected View onCreateDialogView() {
    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.triplet_picker_dialog, null);

    // This must be updated each time to keep the NumberPicker objects up-to-date.
    digits = new ArrayList();
    digits.add((NumberPicker) view.findViewById(R.id.triplet_picker_units));
    digits.add((NumberPicker) view.findViewById(R.id.triplet_picker_tens));
    digits.add((NumberPicker) view.findViewById(R.id.triplet_picker_hundreds));

    int number = getPersistedInt(DEFAULT_VALUE);
    for (NumberPicker digit : digits) {
      digit.setMaxValue(DIGIT_MAX);
      digit.setMinValue(DIGIT_MIN);
      digit.setWrapSelectorWheel(true);
      digit.setValue(number % 10);
      number = number / 10;
    }

    return view;
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      int number = 0;
      for (int i = 0; i < digits.size(); ++i) {
        number += digits.get(i).getValue() * Math.pow(10, i);
      }
      // Clamp the number to within the allowed range
      if (number > max) {
        number = max;
      } else if (number < min) {
        number = min;
      }
      persistInt(number);
    }
  }

}