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
package org.cowboycoders.cyclismo.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

import org.cowboycoders.cyclismo.Constants;
import org.cowboycoders.cyclismo.CourseSetupActivity;
import org.cowboycoders.cyclismo.R;
import org.cowboycoders.cyclismo.content.Bike;
import org.cowboycoders.cyclismo.content.CyclismoProviderUtils;
import org.cowboycoders.cyclismo.content.MyTracksCourseProviderUtils;
import org.cowboycoders.cyclismo.content.MyTracksProviderUtils;
import org.cowboycoders.cyclismo.content.Track;
import org.cowboycoders.cyclismo.content.User;
import org.cowboycoders.cyclismo.settings.AbstractSettingsFragment;
import org.cowboycoders.cyclismo.turbo.TurboService;
import org.cowboycoders.cyclismo.util.PreferenceEntry;
import org.cowboycoders.cyclismo.util.PreferencesUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CourseSetupFragment extends AbstractSettingsFragment {
  private final static String TAG = CourseSetupFragment.class.getSimpleName();

  public static final int PICK_COURSE_REQUEST = 1567;

  private OnSharedPreferenceChangeListener sharedListener;
  private SharedPreferences sharedPreferences;
  private UpdateSummaryCaller updateCourseModeSummaryCaller;
  private UpdateSummaryCaller updateTrackIdSummaryCaller;
  private UpdateSummaryCaller updateBikeSummaryCaller;
  private UpdateSummaryCaller updateTurboSummaryCaller;
  private List<CourseSetupObserver> observers = new ArrayList<>();
  private ListPreference courseModeListPreference;
  private Preference courseTrackIdPreference;
  private Preference bikeSelectPreference;
  private Preference turboSelectPreference;
  private PreferenceEntry courseModes;
  private TurboService turboService;

  public CourseSetupFragment addObserver(CourseSetupObserver observer) {
    observers.add(observer);
    return this;
  }

  private String setupCourseModeSelector(Context context, String turboSelectValue) {
    Log.i(TAG, "updating available course modes");
    courseModeListPreference = (ListPreference) findPreference(getString(R.string.course_mode));

    // Since the course modes are a function of the turbo we set them here rather than in the xml
    courseModes = turboService.getCourseModesForTurbo(turboSelectValue);
    courseModeListPreference.setEntries(courseModes.getEntries());
    courseModeListPreference.setEntryValues(courseModes.getEntryValues());

    String courseModeValue = PreferencesUtils.getString(context, R.string.course_mode, null);
    if (!Arrays.asList(courseModes.getEntryValues()).contains(courseModeValue)) {
      Log.d(TAG, "course mode: " + courseModeValue + " not available from turbo: " +
          turboSelectValue + ", resetting to default.");
      courseModeValue = getString(R.string.settings_courses_mode_simulation_value);
      courseModeListPreference.setValue(courseModeValue);
    }

    PreferencesUtils.SettingsSelectionSummarizer courseModeSummarizer =
        new PreferencesUtils.SettingsSelectionSummarizer() {
          @Override
          public String summarize(Object value) {
            return summarizeCourseModeSelection(value);
          }
        };
    updateCourseModeSummaryCaller = new UpdateSummaryCaller(
        courseModeListPreference,
        courseModes.getEntries(),
        courseModes.getEntryValues(),
        R.string.settings_courses_mode_summary,
        courseModeSummarizer);

    return courseModeValue;
  }

  private String setupTurboSelector(Context context) {
    turboSelectPreference = findPreference(getString(R.string.turbotrainer_selected));

    PreferencesUtils.SettingsSelectionSummarizer turboTrainerSummarizer =
        new PreferencesUtils.SettingsSelectionSummarizer() {
          @Override
          public String summarize(Object value) {
            return summarizeTurboSelection(value);
          }
        };
    updateTurboSummaryCaller = new UpdateSummaryCaller(
        turboSelectPreference,
        getResources().getStringArray(R.array.turbotrainer_options),
        getResources().getStringArray(R.array.turbotrainer_values),
        R.string.settings_turbotrainer_select_summary,
        turboTrainerSummarizer);

    return PreferencesUtils.getString(context, R.string.turbotrainer_selected, null);
  }

  private Long setupTrackSelector(Context context) {
    courseTrackIdPreference = findPreference(getString(R.string.course_track_id));

    courseTrackIdPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        startActivityForResult(preference.getIntent(), PICK_COURSE_REQUEST);
        return true;
      }
    });

    PreferencesUtils.SettingsSelectionSummarizer summarizer =
        new PreferencesUtils.SettingsSelectionSummarizer() {
          @Override
          public String summarize(Object value) {
            return summarizeTrackIdSelection(value);
          }
        };
    this.updateTrackIdSummaryCaller = new UpdateSummaryCaller(
        courseTrackIdPreference, null, null, R.string.settings_courses_route_summary, summarizer);

    return PreferencesUtils.getLong(context, R.string.course_track_id);
  }

  private Long setupBikeSelector(Context context) {
    bikeSelectPreference = findPreference(getString(R.string.course_bike_id));

    bikeSelectPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        startActivityForResult(preference.getIntent(), R.string.bike_select_request_code);
        return true;
      }
    });

    PreferencesUtils.SettingsSelectionSummarizer bikeSelectSummarizer =
        new PreferencesUtils.SettingsSelectionSummarizer() {
          @Override
          public String summarize(Object value) {
            return summarizeBikeSelection(value);
          }
        };
    this.updateBikeSummaryCaller = new UpdateSummaryCaller(
        bikeSelectPreference, null, null, R.string.settings_courses_bike_select_summary,
        bikeSelectSummarizer);

    return PreferencesUtils.getLong(context, R.string.settings_select_bike_current_selection_key);
  }

  public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "fragment onCreate");
    super.onCreate(savedInstanceState);
    final Context context = this.getActivity();
    if (context instanceof CourseSetupActivity) {
      turboService = ((CourseSetupActivity) context).getTurboService();
    } else {
      throw new IllegalArgumentException("Invalid context");
    }

    addPreferencesFromResource(R.xml.course_settings);
    sharedPreferences = getActivity().
        getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);

    // Setup menu preferences
    Long courseTrackIdValue = setupTrackSelector(context);
    Long bikeValue = setupBikeSelector(context);
    String turboValue = setupTurboSelector(context);
    String courseModeValue = setupCourseModeSelector(context, turboValue);

    // Initial configuration of settings UI
    updateTurbo(turboValue);
    updateBike(bikeValue);
    updateCourseTrackId(courseTrackIdValue);
    updateCourseMode(courseModeValue);

    // Listener for updating UI when selections change
    sharedListener = new OnSharedPreferenceChangeListener() {
      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferencesIn, String key) {
        if (key.equals(PreferencesUtils.getKey(getActivity(), R.string.course_mode))) {
          String newValue = sharedPreferences.getString(key, null);
          updateCourseMode(newValue);
        } else if (key.equals(PreferencesUtils.getKey(getActivity(), R.string.course_track_id))) {
          Long newValue = sharedPreferences.getLong(key, -1l);
          updateCourseTrackId(newValue);
        } else if (key.equals(PreferencesUtils.getKey(getActivity(), R.string
            .settings_select_bike_current_selection_key))) {
          Long newValue = sharedPreferences.getLong(key, -1l);
          updateBike(newValue);
        } else if (key.equals(PreferencesUtils.getKey(getActivity(), R.string
            .turbotrainer_selected))) {
          String newValue = sharedPreferences.getString(key, null);
          updateTurbo(newValue);
          setupCourseModeSelector(context, newValue);
        }
      }
    };

    for (CourseSetupObserver observer : observers) {
      observer.onCourseModeUpdate(courseModeValue);
      observer.onTrackIdUpdate(courseTrackIdValue);
      updateBike(bikeValue);
      observer.onTurboUpdate(turboValue);
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    this.sharedPreferences.registerOnSharedPreferenceChangeListener(sharedListener);
  }

  @Override
  public void onDestroy() {
    super.onStop();
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedListener);
  }

  protected String summarizeTrackIdSelection(Object value) {
    Long trackId = (Long) value;
    Log.i(TAG, "Selected track id: " + trackId);
    String noneSelected = this.getString(R.string.item_not_selected);
    if (trackId == -1) {
      return noneSelected;
    }
    Track track = new MyTracksCourseProviderUtils(this.getActivity().getContentResolver())
        .getTrack(trackId);
    if (track == null) {
      Log.w(TAG, "track invalid : shared preference mismatch");
      PreferencesUtils.setLong(this.getActivity(), R.string.course_track_id, -1L);
      return noneSelected;
    }
    return track.getName();
  }

  private static User getUser(Context context) {
    final CyclismoProviderUtils providerUtils = MyTracksProviderUtils.Factory.getCyclimso(context);
    final long currentUserId = PreferencesUtils.getLong(context, R.string
        .settings_select_user_current_selection_key);
    User user = providerUtils.getUser(currentUserId);
    return user;
  }

  private void updateBike(final Long newValue) {
    Log.i(TAG, "new bike selected with id: " + newValue);
    this.updateBikeSummaryCaller.updateSummary(newValue);
    for (final CourseSetupObserver observer : observers) {
      new AsyncTask<Object, Integer, Bike>() {

        @Override
        protected Bike doInBackground(Object... params) {
          long bikeId = getCurrentBikeId();
          if (bikeId == -1L) {
            observer.onBikeUpdate(null);
            return null;
          }
          Bike bike = loadBike(bikeId);
          observer.onBikeUpdate(bike);
          return bike;
        }

      }.execute(new Object());
    }
  }

  private long getCurrentBikeId() {
    User currentUser = getUser(CourseSetupFragment.this.getActivity());
    long bikeId = currentUser.getCurrentlySelectedBike();
    return bikeId;
  }

  private Bike loadBike(long id) {
    return MyTracksProviderUtils.Factory.getCyclimso(this.getActivity()).getBike(id);
  }

  protected String summarizeBikeSelection(Object value) {
    String noneSelected = this.getString(R.string.item_not_selected);
    // the bikeid coming is from the shared preference / get current users bike
    Long bikeId = getCurrentBikeId();
    if (bikeId == -1L) {
      return noneSelected;
    }
    Bike bike = loadBike(bikeId);
    if (bike == null) {
      Log.w(TAG, "bike invalid : shared preference mismatch");
      PreferencesUtils.setLong(this.getActivity(), R.string
          .settings_select_bike_current_selection_key, -1L);
      return noneSelected;
    }
    return bike.getName();
  }

  protected String summarizeCourseModeSelection(Object value) {
    String courseMode = (String) value;
    if (courseMode == null) {
      Log.w(TAG, "course mode invalid");
      PreferencesUtils.setLong(this.getActivity(), R.string.course_mode, -1L);
      return this.getString(R.string.course_mode);
    }
    return courseMode;
  }

  protected String summarizeTurboSelection(Object value) {
    String turbo = (String) value;
    if (turbo == null) {
      Log.w(TAG, "turbo invalid");
      PreferencesUtils.setLong(this.getActivity(), R.string.turbotrainer_selected, -1L);
      return this.getString(R.string.item_not_selected);
    }
    return turbo;
  }

  private void updateCourseTrackId(Long newValue) {
    Log.i(TAG, "new track selected with id: " + newValue);
    updateTrackIdSummaryCaller.updateSummary(newValue);
    for (CourseSetupObserver observer : observers) {
      observer.onTrackIdUpdate(newValue);
    }
  }

  private void updateCourseMode(String newValue) {
    Log.i(TAG, "course mode: " + newValue);
    updateCourseModeSummaryCaller.updateSummary(newValue);
    for (CourseSetupObserver observer : observers) {
      observer.onCourseModeUpdate(newValue);
    }
    // TODO: Re-enable this if we have mode which doesn't need a track
    //if (!newValue.equals(getString(R.string.settings_courses_mode_simulation_value))) {
    //  courseTrackIdPreference.setEnabled(false);
    //}
  }

  private void updateTurbo(final String newValue) {
    Log.i(TAG, "new turbo selected with id: " + newValue);
    updateTurboSummaryCaller.updateSummary(newValue);
    for (CourseSetupObserver observer : observers) {
      observer.onTurboUpdate(newValue);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == PICK_COURSE_REQUEST) {
      if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
        if (data != null) {
          Long trackId = data.getLongExtra(getString(R.string.course_track_id), -1L);
          updateCourseTrackId(trackId);
        } else {
          Log.d(TAG, "onActivityResult : data was null");
          updateCourseTrackId(-1L);
        }
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  public interface CourseSetupObserver {
    void onTrackIdUpdate(Long trackId);
    void onBikeUpdate(Bike bike);
    void onCourseModeUpdate(String modeString);
    void onTurboUpdate(String turbo);
  }
}