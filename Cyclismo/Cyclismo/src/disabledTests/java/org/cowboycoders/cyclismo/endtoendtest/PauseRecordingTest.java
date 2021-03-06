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
package org.cowboycoders.cyclismo.endtoendtest;

import android.app.Instrumentation;
import android.location.Location;
import android.test.ActivityInstrumentationTestCase2;

import org.cowboycoders.cyclismo.TrackListActivity;
import org.cowboycoders.cyclismo.content.MyTracksProviderUtils;
import org.cowboycoders.cyclismo.content.MyTracksProviderUtils.LocationIterator;
import org.cowboycoders.cyclismo.services.TrackRecordingService;


/**
 * Tests the pause and resume of recording.
 * 
 * @author Youtao Liu
 */
public class PauseRecordingTest extends ActivityInstrumentationTestCase2<TrackListActivity> {

  private Instrumentation instrumentation;
  private TrackListActivity activityMyTracks;

  public PauseRecordingTest() {
    super(TrackListActivity.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    instrumentation = getInstrumentation();
    activityMyTracks = getActivity();
    EndToEndTestUtils.setupForAllTest(instrumentation, activityMyTracks);
  }

  /**
   * Tests the pause recording feature. Stops the recording after pause.
   */
  public void testPauseRecording_stopAfterPause() {
    int gpsSignalNumber = 3;

    EndToEndTestUtils.checkNotRecording();
    // Start recording
    EndToEndTestUtils.startRecording();
    EndToEndTestUtils.checkUnderRecording();
    EndToEndTestUtils.sendGps(gpsSignalNumber);

    // Pause
    EndToEndTestUtils.pauseRecording();
    EndToEndTestUtils.checkUnderPaused();
    EndToEndTestUtils.sendGps(gpsSignalNumber, gpsSignalNumber);

    // Stop
    EndToEndTestUtils.stopRecording(true);
    checkPointsInPausedTrack(gpsSignalNumber, 1, 0);
    EndToEndTestUtils.SOLO.goBack();
    EndToEndTestUtils.checkNotRecording();
  }

  /**
   * Tests the pause recording feature. Stops the recording after resume.
   */
  public void testPauseRecording_stopAfterResume() {
    int gpsSignalNumber = 3;

    EndToEndTestUtils.checkNotRecording();
    // Start recording
    EndToEndTestUtils.startRecording();
    EndToEndTestUtils.checkUnderRecording();
    EndToEndTestUtils.sendGps(gpsSignalNumber);

    // Pause
    EndToEndTestUtils.pauseRecording();
    EndToEndTestUtils.checkUnderPaused();

    // Send Gps signal after pause.
    // Add 10 to make these signals are apparently different.
    EndToEndTestUtils.sendGps(gpsSignalNumber, gpsSignalNumber + 10, 10);

    // Resume
    EndToEndTestUtils.resumeRecording();
    EndToEndTestUtils.checkUnderRecording();
    EndToEndTestUtils.sendGps(gpsSignalNumber, gpsSignalNumber);

    // Stop
    EndToEndTestUtils.stopRecording(true);
    checkPointsInPausedTrack(gpsSignalNumber * 2, 1, 1);
    EndToEndTestUtils.SOLO.goBack();
    EndToEndTestUtils.checkNotRecording();
  }

  /**
   * Check the points in the last track.
   * 
   * @param expectPointNumber the number of location points
   * @param expectPauseNumber the number of pause points
   * @param expectResumeNumber the number of resume points
   */
  private void checkPointsInPausedTrack(int expectPointNumber, int expectPauseNumber,
      int expectResumeNumber) {
    if (EndToEndTestUtils.isEmulator) {
      int numberOfPoints = 0;
      int numberOfPausePoint = 0;
      int numberOfResumePoint = 0;

      MyTracksProviderUtils providerUtils = MyTracksProviderUtils.Factory.get(activityMyTracks
          .getApplicationContext());
      long trackId = providerUtils.getLastTrack().getId();
      LocationIterator points = providerUtils.getTrackPointLocationIterator(trackId, 0, false,
          MyTracksProviderUtils.DEFAULT_LOCATION_FACTORY);
      while (points.hasNext()) {
        Location onePoint = points.next();
        double latitude = onePoint.getLatitude();
        double longitude = onePoint.getLongitude();
        if (latitude == TrackRecordingService.PAUSE_LATITUDE) {
          numberOfPausePoint++;
        } else if (latitude == TrackRecordingService.RESUME_LATITUDE) {
          numberOfResumePoint++;
        } else {
          assertEquals(numberOfPoints, (int) ((latitude - EndToEndTestUtils.START_LATITUDE)
              / EndToEndTestUtils.DELTA_LADITUDE + 0.5));
          assertEquals(numberOfPoints, (int) ((longitude - EndToEndTestUtils.START_LONGITUDE)
              / EndToEndTestUtils.DELTA_LONGITUDE + 0.5));
          numberOfPoints++;
        }
      }

      assertEquals(expectPointNumber, numberOfPoints);
      assertEquals(expectPauseNumber, numberOfPausePoint);
      assertEquals(expectResumeNumber, numberOfResumePoint);
    }
  }
  
  @Override
  protected void tearDown() throws Exception {
    EndToEndTestUtils.SOLO.finishOpenedActivities();
    super.tearDown();
  }

}
