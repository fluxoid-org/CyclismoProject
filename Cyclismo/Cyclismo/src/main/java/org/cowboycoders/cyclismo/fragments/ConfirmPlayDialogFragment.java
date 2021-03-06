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
/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.cowboycoders.cyclismo.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.cowboycoders.cyclismo.R;
import org.cowboycoders.cyclismo.io.file.SaveActivity;
import org.cowboycoders.cyclismo.io.file.TrackWriterFactory.TrackFileFormat;
import org.cowboycoders.cyclismo.util.AnalyticsUtils;
import org.cowboycoders.cyclismo.util.IntentUtils;
import org.cowboycoders.cyclismo.util.PreferencesUtils;

/**
 * A DialogFragment to confirm play in Google Earth.
 * 
 * @author Jimmy Shih
 */
public class ConfirmPlayDialogFragment extends DialogFragment {

  public static final String CONFIRM_PLAY_DIALOG_TAG = "confirmPlayDialog";
  private static final String KEY_TRACK_ID = "trackId";

  private FragmentActivity activity;
  private CheckBox checkBox;

  public static ConfirmPlayDialogFragment newInstance(long trackId) {
    Bundle bundle = new Bundle();
    bundle.putLong(KEY_TRACK_ID, trackId);

    ConfirmPlayDialogFragment confirmPlayDialogFragment = new ConfirmPlayDialogFragment();
    confirmPlayDialogFragment.setArguments(bundle);
    return confirmPlayDialogFragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activity = getActivity();
    if (!PreferencesUtils.getBoolean(getActivity(), R.string.show_confirm_play_earth_dialog_key,
        PreferencesUtils.SHOW_CONFIRM_PLAY_DIALOG_DEFAULT)) {
      dismiss();
      playTrack();
    }
  }
  
  private void playTrack() {
    long trackId = getArguments().getLong(KEY_TRACK_ID);
    AnalyticsUtils.sendPageViews(activity, "/action/play");
    Intent intent = IntentUtils.newIntent(activity, SaveActivity.class)
        .putExtra(SaveActivity.EXTRA_TRACK_ID, trackId)
        .putExtra(SaveActivity.EXTRA_TRACK_FILE_FORMAT, (Parcelable) TrackFileFormat.KML)
        .putExtra(SaveActivity.EXTRA_PLAY_TRACK, true);
    startActivity(intent);
  }
  
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    View view = activity.getLayoutInflater().inflate(R.layout.confirm_dialog, null);
    TextView textView = (TextView) view.findViewById(R.id.confirm_dialog_message);
    textView.setText(R.string.track_detail_play_confirm_message);
    checkBox = (CheckBox) view.findViewById(R.id.confirm_dialog_check_box);
   
    return new AlertDialog.Builder(activity)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
          public void onClick(DialogInterface dialog, int which) {
            PreferencesUtils.setBoolean(
                activity, R.string.show_confirm_play_earth_dialog_key, !checkBox.isChecked());
            playTrack();
          }
        })
        .setTitle(R.string.generic_confirm_title)
        .setView(view)
        .create();
  }
}