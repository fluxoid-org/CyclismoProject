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
 * Copyright 2008 Google Inc.
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

package org.cowboycoders.cyclismo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import com.google.common.annotations.VisibleForTesting;

import org.cowboycoders.cyclismo.content.Waypoint;
import org.cowboycoders.cyclismo.stats.ExtremityMonitor;
import org.cowboycoders.cyclismo.util.IntentUtils;
import org.cowboycoders.cyclismo.util.StringUtils;
import org.cowboycoders.cyclismo.util.UnitConversions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Visualization of the chart.
 * 
 * @author Sandor Dornbush
 * @author Leif Hendrik Wilden
 */
public class AltitudeProfileView extends View {

  public static final String TAG = AltitudeProfileView.class.getSimpleName();

  public static final float SMALL_TEXT_SIZE = 12f;

  public static final int Y_AXIS_INTERVALS = 5;

  public static final int NUM_SERIES = 1;
  public static final int ELEVATION_SERIES = 0;

  private static final int TARGET_X_AXIS_INTERVALS = 4;

  private static final int MIN_ZOOM_LEVEL = 1;
  private static final int MAX_ZOOM_LEVEL = 10;

  private static final NumberFormat X_NUMBER_FORMAT = NumberFormat.getIntegerInstance();
  private static final NumberFormat X_FRACTION_FORMAT = NumberFormat.getNumberInstance();
  static {
    X_FRACTION_FORMAT.setMaximumFractionDigits(1);
    X_FRACTION_FORMAT.setMinimumFractionDigits(1);
  }

  private static final int BORDER = 8;
  private static final int SPACER = 4;
  private static final int Y_AXIS_OFFSET = 16;

  private final ChartValueSeries[] series = new ChartValueSeries[NUM_SERIES];
  private final ChartValueSeries[] seriesOverlay = new ChartValueSeries[NUM_SERIES];
  private final ArrayList<double[]> chartData = new ArrayList<double[]>();
  private final ArrayList<double[]> chartDataOverlay = new ArrayList<double[]>();
  private final ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
  private final ExtremityMonitor xExtremityMonitor = new ExtremityMonitor();
  private final ExtremityMonitor xExtremityMonitorOverlay = new ExtremityMonitor();
  private double maxX = 1.0;

  private final Paint axisPaint;
  private final Paint xAxisMarkerPaint;
  private final Paint gridPaint;
  private final Paint markerPaint;

  private final Drawable pointer;
  private final Drawable statisticsMarker;
  private final Drawable waypointMarker;
  private final int markerWidth;
  private final int markerHeight;

  private final Scroller scroller;
  private VelocityTracker velocityTracker = null;
  private float lastMotionEventX = -1;
  private int zoomLevel = 1;

  private int leftBorder = BORDER;
  private int topBorder = BORDER;
  private int bottomBorder = BORDER;
  private int rightBorder = BORDER;
  private int spacer = SPACER;
  private int yAxisOffset = Y_AXIS_OFFSET;

  private int width = 0;
  private int height = 0;
  private int effectiveWidth = 0;
  private int effectiveHeight = 0;

  private boolean chartByDistance = true;
  private boolean metricUnits = true;
  private ArrayList<double[]> dataPoints;


  /**
   * Constructor.
   *
   * @param context the context
   */
  public AltitudeProfileView(Context context) {
    super(context);

    initSeries(series);
    initSeries(seriesOverlay);

    //FIXME: this a workaround for the paths becoming too large at high zoom levels
    // see: http://code.google.com/p/osmdroid/issues/detail?id=454,
    // http://stackoverflow.com/questions/15039829/drawing-paths-and-hardware-acceleration
    this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

    for(ChartValueSeries c : seriesOverlay) {
      c.setEnabled(false);
    }

    float scale = context.getResources().getDisplayMetrics().density;

    axisPaint = new Paint();
    axisPaint.setStyle(Style.STROKE);
    axisPaint.setColor(context.getResources().getColor(R.color.black));
    axisPaint.setAntiAlias(true);
    axisPaint.setTextSize(SMALL_TEXT_SIZE * scale);
    axisPaint.setStyle(Style.FILL_AND_STROKE);

    xAxisMarkerPaint = new Paint(axisPaint);
    xAxisMarkerPaint.setTextAlign(Align.CENTER);
    xAxisMarkerPaint.setStyle(Style.FILL_AND_STROKE);

    gridPaint = new Paint();
    gridPaint.setStyle(Style.STROKE);
    gridPaint.setColor(context.getResources().getColor(R.color.gray));
    gridPaint.setAntiAlias(false);
    gridPaint.setPathEffect(new DashPathEffect(new float[] { 3, 2 }, 0));

    markerPaint = new Paint();
    markerPaint.setStyle(Style.STROKE);
    markerPaint.setColor(context.getResources().getColor(R.color.gray));
    markerPaint.setAntiAlias(false);

    pointer = context.getResources().getDrawable(R.drawable.location_marker);
    pointer.setBounds(0, 0, pointer.getIntrinsicWidth(), pointer.getIntrinsicHeight());

    statisticsMarker = getResources().getDrawable(R.drawable.yellow_pushpin);
    markerWidth = statisticsMarker.getIntrinsicWidth();
    markerHeight = statisticsMarker.getIntrinsicHeight();
    statisticsMarker.setBounds(0, 0, markerWidth, markerHeight);

    waypointMarker = getResources().getDrawable(R.drawable.blue_pushpin);
    waypointMarker.setBounds(0, 0, markerWidth, markerHeight);

    scroller = new Scroller(context);
    setFocusable(true);
    setClickable(true);
    updateDimensions();
  }

  private void initSeries(ChartValueSeries[] seriesIn) {
    Context context = getContext();
    seriesIn[ELEVATION_SERIES] = new ChartValueSeries(context,
        Integer.MIN_VALUE,
        Integer.MAX_VALUE,
        new int[] { 5, 10, 25, 50, 100, 250, 500, 1000, 2500, 5000 },
        R.string.description_elevation_metric,
        R.string.description_elevation_imperial,
        R.color.elevation_border,
        R.color.elevation_border);
  }

  /**
   * Sets the enabled value for a chart value series.
   *
   * @param index the chart value series index
   */
  public void setChartValueSeriesEnabled(int index, boolean enabled) {
    series[index].setEnabled(enabled);
  }

  /**
   * Sets the enabled value for a chart value series.
   *
   * @param index the chart value series index
   */
  public void setOverlayChartValueSeriesEnabled(int index, boolean enabled) {
    seriesOverlay[index].setEnabled(enabled);
  }

  /**
   * Sets chart by distance. It is expected that after changing this value, data
   * will be reloaded.
   *
   * @param value true for by distance, false for by time
   */
  public void setChartByDistance(boolean value) {
    chartByDistance = value;
  }

  /**
   * Sets metric units.
   *
   * @param value true to use metric units
   */
  public void setMetricUnits(boolean value) {
    metricUnits = value;
  }


  /**
   * Adds data points ( for current location marker)
   *
   * @param dataPoints an array of data points to be added
   */
  public void addDataPoints(ArrayList<double[]> dataPoints) {
    synchronized (chartData) {
      chartData.addAll(dataPoints);
      for (int i = 0; i < dataPoints.size(); i++) {
        double[] dataPoint = dataPoints.get(i);
        xExtremityMonitor.update(dataPoint[0]);
        Log.d(TAG, "x axis val: " +  dataPoint[0]);
        for (int j = 0; j < series.length; j++) {
          if (!Double.isNaN(dataPoint[j+1])) {
            series[j].update(dataPoint[j + 1]);
            Log.d(TAG, "elevation: " + dataPoint[j+1]);
          }
        }
      }
      updateDimensions();
      //updatePaths(series, chartData);
    }
  }


  public void addAltitudeData(ArrayList<double[]> dataPoints) {
    this.dataPoints = dataPoints;
    synchronized (chartDataOverlay) {
      Log.v(TAG, "adding overlay");
      chartDataOverlay.clear();
      chartDataOverlay.addAll(dataPoints);
      xExtremityMonitorOverlay.reset();
      for (int i = 0; i < dataPoints.size(); i++) {
        double[] dataPoint = dataPoints.get(i);
        xExtremityMonitorOverlay.update(dataPoint[0]);
        for (int j = 0; j < seriesOverlay.length; j++) {
          if (!Double.isNaN(dataPoint[j + 1])) {
            seriesOverlay[j].update(dataPoint[j + 1]);
          }
        }
      }
      updateDimensions();
      updatePaths(seriesOverlay,chartDataOverlay);
    }
  }

  /**
   * Clears all data.
   */
  public void reset() {
    Log.d(TAG, "calling reset");
    synchronized (chartData) {
      chartData.clear();
      xExtremityMonitor.reset();
      zoomLevel = 1;
      updateDimensions();
    }
    forceRedraw();


  }

  private void forceRedraw() {
    // this is to make sure the course data is redrawn,
    // there may be a better way
    this.post(new Runnable() {
      @Override
      public void run() {
        updateAllPaths();
        invalidate();
      }
    });
  }

  /**
   * Resets scroll. To be called on the UI thread.
   */
  public void resetScroll() {
    scrollTo(0, 0);
  }

  /**
   * Adds a waypoint.
   *
   * @param waypoint the waypoint
   */
  public void addWaypoint(Waypoint waypoint) {
    synchronized (waypoints) {
      waypoints.add(waypoint);
    }
  }

  /**
   * Clears the waypoints.
   */
  public void clearWaypoints() {
    synchronized (waypoints) {
      waypoints.clear();
    }
  }

  /**
   * Returns true if can zoom in.
   */
  public boolean canZoomIn() {
    return zoomLevel < MAX_ZOOM_LEVEL;
  }

  /**
   * Returns true if can zoom out.
   */
  public boolean canZoomOut() {
    return zoomLevel > MIN_ZOOM_LEVEL;
  }

  /**
   * Zooms in one level.
   */
  public void zoomIn() {
    if (canZoomIn()) {
      zoomLevel++;
      Log.d(TAG, "zoomLevel:" + zoomLevel);
      updateAllPaths();
      invalidate();
    }
  }

  private void updateAllPaths() {
    updatePaths(seriesOverlay,chartDataOverlay);
  }

  /**
   * Zooms out one level.
   */
  public void zoomOut() {
    if (canZoomOut()) {
      zoomLevel--;
      scroller.abortAnimation();
      int scrollX = getScrollX();
      int maxWidth = effectiveWidth * (zoomLevel - 1);
      if (scrollX > maxWidth) {
        scrollX = maxWidth;
        scrollTo(scrollX, 0);
      }
      updateAllPaths();
      invalidate();
    }
  }

  /**
   * Initiates flinging.
   *
   * @param velocityX velocity of fling in pixels per second
   */
  public void fling(int velocityX) {
    int maxWidth = effectiveWidth * (zoomLevel - 1);
    scroller.fling(getScrollX(), 0, velocityX, 0, 0, maxWidth, 0, 0);
    invalidate();
  }

  /**
   * Scrolls the view horizontally by a given amount.
   *
   * @param deltaX the number of pixels to scroll
   */
  public void scrollBy(int deltaX) {
    int scrollX = getScrollX() + deltaX;
    if (scrollX < 0) {
      scrollX = 0;
    }
    int maxWidth = effectiveWidth * (zoomLevel - 1);
    if (scrollX > maxWidth) {
      scrollX = maxWidth;
    }
    scrollTo(scrollX, 0);
  }

  /**
   * Called by the parent to indicate that the mScrollX/Y values need to be
   * updated. Triggers a redraw during flinging.
   */
  @Override
  public void computeScroll() {
    if (scroller.computeScrollOffset()) {
      int oldX = getScrollX();
      int x = scroller.getCurrX();
      scrollTo(x, 0);
      if (oldX != x) {
        onScrollChanged(x, 0, oldX, 0);
        postInvalidate();
      }
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
    velocityTracker.addMovement(event);
    float x = event.getX();
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        // Stop the fling
        if (!scroller.isFinished()) {
          scroller.abortAnimation();
        }
        lastMotionEventX = x;
        break;
      case MotionEvent.ACTION_MOVE:
        if (lastMotionEventX == -1) {
          break;
        }
        // Scroll to follow the motion event
        int deltaX = (int) (lastMotionEventX - x);
        lastMotionEventX = x;
        if (deltaX < 0) {
          if (getScrollX() > 0) {
            scrollBy(deltaX);
          }
        } else if (deltaX > 0) {
          int availableToScroll = effectiveWidth * (zoomLevel - 1) - getScrollX();
          if (availableToScroll > 0) {
            scrollBy(Math.min(availableToScroll, deltaX));
          }
        }
        break;
      case MotionEvent.ACTION_UP:
        // Check if the y event is within markerHeight of the marker center
        if (Math.abs(event.getY() - topBorder - spacer - markerHeight / 2) < markerHeight) {
          int minDistance = Integer.MAX_VALUE;
          Waypoint nearestWaypoint = null;
          synchronized (waypoints) {
            for (int i = 0; i < waypoints.size(); i++) {
              Waypoint waypoint = waypoints.get(i);
              int distance = Math.abs(
                  getX(getWaypointXValue(waypoint)) - (int) event.getX() - getScrollX());
              if (distance < minDistance) {
                minDistance = distance;
                nearestWaypoint = waypoint;
              }
            }
          }
          if (nearestWaypoint != null && minDistance < markerWidth) {
            Intent intent = IntentUtils.newIntent(getContext(), MarkerDetailActivity.class)
                .putExtra(MarkerDetailActivity.EXTRA_MARKER_ID, nearestWaypoint.getId());
            getContext().startActivity(intent);
            return true;
          }
        }

        VelocityTracker myVelocityTracker = velocityTracker;
        myVelocityTracker.computeCurrentVelocity(1000);
        int initialVelocity = (int) myVelocityTracker.getXVelocity();
        if (Math.abs(initialVelocity) > ViewConfiguration.getMinimumFlingVelocity()) {
          fling(-initialVelocity);
        }
        if (velocityTracker != null) {
          velocityTracker.recycle();
          velocityTracker = null;
        }
        break;
    }
    return true;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    updateEffectiveDimensionsIfChanged(
        MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    synchronized (chartDataOverlay) {

      canvas.save();
      
      canvas.drawColor(Color.WHITE);

      canvas.save();
      
      clipToGraphArea(canvas);
      drawDataSeries(canvas, seriesOverlay);
      drawWaypoints(canvas);
      //drawGrid(canvas);
      
      canvas.restore();
      drawXAxis(canvas);
      drawYAxis(canvas, seriesOverlay);
      
      canvas.restore();

      drawPointer(canvas);

    }
  }

  /**
   * Clips a canvas to the graph area.
   * 
   * @param canvas the canvas
   */
  private void clipToGraphArea(Canvas canvas) {
    int x = getScrollX() + leftBorder;
    int y = topBorder;
    canvas.clipRect(x, y, x + effectiveWidth, y + effectiveHeight);
  }

  /**
   * Draws the data series.
   * 
   * @param canvas the canvas
   */
  private void drawDataSeries(Canvas canvas, ChartValueSeries [] seriesIn) {
    for (ChartValueSeries chartValueSeries : seriesIn) {
      if (chartValueSeries.isEnabled() && chartValueSeries.hasData()) {
        chartValueSeries.drawPath(canvas);
      }
    }
  }

  /**
   * Draws the waypoints.
   * 
   * @param canvas the canvas
   */
  private void drawWaypoints(Canvas canvas) {
    synchronized (waypoints) {
      for (int i = 0; i < waypoints.size(); i++) {
        final Waypoint waypoint = waypoints.get(i);
        if (waypoint.getLocation() == null) {
          continue;
        }
        double xValue = getWaypointXValue(waypoint);
        if (xValue > maxX) {
          continue;
        }
        canvas.save();
        float x = getX(getWaypointXValue(waypoint));
        canvas.drawLine(
            x, topBorder + spacer + markerHeight / 2, x, topBorder + effectiveHeight, markerPaint);
        canvas.translate(
            x - (float) (markerWidth * MapOverlay.WAYPOINT_X_ANCHOR), topBorder + spacer);
        if (waypoints.get(i).getType() == Waypoint.TYPE_STATISTICS) {
          statisticsMarker.draw(canvas);
        } else {
          waypointMarker.draw(canvas);
        }
        canvas.restore();
      }
    }
  }

  /**
   * Draws the grid.
   * 
   * @param canvas the canvas
   */
  private void drawGrid(Canvas canvas) {
    // X axis grid
    ArrayList<Double> xAxisMarkerPositions = getXAxisMarkerPositions(getXAxisInterval());
    for (int i = 0; i < xAxisMarkerPositions.size(); i++) {
      int x = getX(xAxisMarkerPositions.get(i));
      canvas.drawLine(x, topBorder, x, topBorder + effectiveHeight, gridPaint);
    }
    // Y axis grid
    float rightEdge = getX(maxX);
    for (int i = 0; i <= Y_AXIS_INTERVALS; i++) {
      double percentage = (double) i / Y_AXIS_INTERVALS;
      int range = effectiveHeight - 2 * yAxisOffset;
      int y = topBorder + yAxisOffset + (int) (percentage * range);
      canvas.drawLine(leftBorder, y, rightEdge, y, gridPaint);
    }
  }


  /**
   * Draws the x axis.
   * 
   * @param canvas the canvas
   */
  private void drawXAxis(Canvas canvas) {
    int x = getScrollX() + leftBorder;
    int y = topBorder + effectiveHeight;
    canvas.drawLine(x, y, x + effectiveWidth, y, axisPaint);
    String label = getXAxisLabel();
    Rect rect = getRect(axisPaint, label);
    canvas.drawText(label, x + effectiveWidth + spacer, y + ((int) rect.height() / 2), axisPaint);
    
    double interval = getXAxisInterval();
    ArrayList<Double> markerPositions = getXAxisMarkerPositions(interval);
    NumberFormat numberFormat = interval < 1 ? X_FRACTION_FORMAT : X_NUMBER_FORMAT;
    for (int i = 0; i < markerPositions.size(); i++) {
      drawXAxisMarker(canvas, markerPositions.get(i), numberFormat);
    }
  }

  // returns the
  private double getXMid() {
    //based on marker positions, so that it shares the same logic
    double interval = getXAxisInterval();
    ArrayList<Double> markerPositions = getXAxisMarkerPositions(interval);
    return markerPositions.get(markerPositions.size() /2 );
  }

  /**
   * Gets the x axis label.
   */
  private String getXAxisLabel() {
    Context context = getContext();
    if (chartByDistance) {
      return metricUnits ? context.getString(R.string.unit_kilometer)
          : context.getString(R.string.unit_mile);
    } else {
      return context.getString(R.string.description_time);
    }
  }

  /**
   * Draws a x axis marker.
   * 
   * @param canvas
   * @param value value
   * @param numberFormat the number format
   */
  private void drawXAxisMarker(Canvas canvas, double value, NumberFormat numberFormat) {
    String marker = chartByDistance ? numberFormat.format(value)
        : StringUtils.formatElapsedTime((long) value);
    Rect rect = getRect(xAxisMarkerPaint, marker);
    canvas.drawText(marker, getX(value), topBorder + effectiveHeight + spacer + rect.height(),
        xAxisMarkerPaint);
  }

  /**
   * Gets the x axis interval.
   */
  private double getXAxisInterval() {
    double interval = (maxX / zoomLevel) / (double) TARGET_X_AXIS_INTERVALS;
    if (interval < 1) {
      interval = .5;
    } else if (interval < 5) {
      interval = 2;
    } else if (interval < 10) {
      interval = 5;
    } else {
      interval = (interval / 10) * 10;
    }
    return interval;
  }

  /**
   * Gets the x axis marker positions.
   */
  private ArrayList<Double> getXAxisMarkerPositions(double interval) {
    ArrayList<Double> markers = new ArrayList<Double>();
    markers.add(0d);
    for (int i = 1; i * interval < maxX; i++) {
      markers.add(i * interval);
    }
    // At least 2 markers
    if (markers.size() < 2) {
      markers.add(maxX);
    }
    return markers;
  }

  /**
   * Draws the y axis.
   * 
   * @param canvas the canvas
   */
  private void drawYAxis(Canvas canvas, ChartValueSeries [] seriesIn) {
    int x = getScrollX() + leftBorder;
    int y = topBorder;
    canvas.drawLine(x, y, x, y + effectiveHeight, axisPaint);
    
    int markerXPosition = x - spacer;
    for (int i = 0; i < seriesIn.length; i++) {
      int index = seriesIn.length - 1 - i;
      ChartValueSeries chartValueSeries = seriesIn[index];
      if (chartValueSeries.isEnabled() && chartValueSeries.hasData()) {
        markerXPosition -= drawYAxisMarkers(chartValueSeries, canvas, markerXPosition) + spacer;
      }
    }
  }
  
  private ChartValueSeries[] getCombinedSeries() {
    ChartValueSeries [] seriesLocal = Arrays.copyOf(series, series.length);
    for (int i = 0 ; i < seriesLocal.length ; i++) {
      if (seriesOverlay[i].isEnabled()) {
        seriesLocal[i] = seriesOverlay[i];
      }
    }
    return seriesLocal;
  }

  /**
   * Draws the y axis markers for a chart value series.
   * 
   * @param chartValueSeries the chart value series
   * @param canvas the canvas
   * @param xPosition the right most x position
   * @return the maximum marker width.
   */
  private float drawYAxisMarkers(ChartValueSeries chartValueSeries, Canvas canvas, int xPosition) {
    int interval = chartValueSeries.getInterval();
    float maxMarkerWidth = 0;
    for (int i = 0; i <= Y_AXIS_INTERVALS; i++) {
      maxMarkerWidth = Math.max(maxMarkerWidth, drawYAxisMarker(chartValueSeries, canvas, xPosition,
          i * interval + chartValueSeries.getMinMarkerValue()));
    }
    return maxMarkerWidth;
  }

  /**
   * Draws a y axis marker.
   * 
   * @param chartValueSeries the chart value series
   * @param canvas the canvas
   * @param xPosition the right most x position
   * @param yValue the y value
   * @return the marker width.
   */
  private float drawYAxisMarker(
      ChartValueSeries chartValueSeries, Canvas canvas, int xPosition, int yValue) {
    String marker = chartValueSeries.formatMarker(yValue);
    Paint paint = chartValueSeries.getMarkerPaint();
    Rect rect = getRect(paint, marker);
    int yPosition = getY(chartValueSeries, yValue) + (int) (rect.height() / 2);
    canvas.drawText(marker, xPosition, yPosition, paint);
    return paint.measureText(marker);
  }

  /**
   * Draws the current pointer.
   * 
   * @param canvas the canvas
   */
  private void drawPointer(Canvas canvas) {
    int index = -1;
    for (int i = 0; i < seriesOverlay.length; i++) {
      ChartValueSeries chartValueSeries = seriesOverlay[i];
      if (chartValueSeries.isEnabled() && chartValueSeries.hasData()) {
        index = i;
        break;
      }
    }
    double currentXValue = xExtremityMonitor.hasData() ? xExtremityMonitor.getMax() : 0.0;
    if (index != -1 && chartData.size() > 0) {
      int dx = getX(currentXValue) - pointer.getIntrinsicWidth() / 2;
      int dy = getY(seriesOverlay[index], chartData.get(chartData.size() - 1)[index + 1])
          - pointer.getIntrinsicHeight();
      Log.d(TAG, "y chartDat: " + chartData.get(chartData.size() - 1)[index + 1]);
      Log.d(TAG, "drawing pointer, dx:" + dx);
      Log.d(TAG, "drawing pointer, dy:" + dy);
      Log.d(TAG, "chartData size: " + chartData.size());
      canvas.translate(dx, dy);
      pointer.draw(canvas);
    }
  }

  /**
   * Updates paths. The path needs to be updated any time after the data or the
   * dimensions change.
   */
  private void updatePaths(ChartValueSeries[] seriesIn, ArrayList<double[]> chartDataIn) {
    synchronized (chartDataIn) {
      for (ChartValueSeries chartValueSeries : seriesIn) {
        chartValueSeries.getPath().reset();
      }
      drawPaths(seriesIn, chartDataIn);
      closePaths(seriesIn,chartDataIn);
    }
  }

  /**
   * Draws all paths.
   */
  private void drawPaths(ChartValueSeries [] seriesIn , ArrayList<double[]> chartDataIn) {
    boolean[] hasMoved = new boolean[seriesIn.length];
    
    for (int i = 0; i < chartDataIn.size(); i++) {
      double[] dataPoint = chartDataIn.get(i);
      for (int j = 0; j < seriesIn.length; j++) {
        double value = dataPoint[j + 1];
        if (Double.isNaN(value)) {
          continue;
        }
        ChartValueSeries chartValueSeries = seriesIn[j];
        Path path = chartValueSeries.getPath();
        int x = getX(dataPoint[0]);
        int y = getY(chartValueSeries, value);
        //Log.d("TAG", "updatePath x:" + x);
        //Log.d("TAG", "updatePath y:" + y);
        if (!hasMoved[j]) {
          hasMoved[j] = true;
          path.moveTo(x, y);
        } else {
          path.lineTo(x, y);
        }
      }
    }
    
  }

  /**
   * Closes all paths.
   */
  private void closePaths(ChartValueSeries [] seriesIn, ArrayList<double[]> chartDataIn) {
    closePath(seriesIn,chartDataIn);
  }

  private void closePath(ChartValueSeries[] seriesIn, ArrayList<double[]> chartDataIN) {
    for (int i = 0; i < seriesIn.length; i++) {
      int first = getFirstPopulatedChartDataIndex(i,chartDataIN);
      if (first != -1) {
        Log.d(TAG, "closingPaths");
        int xCorner = getX(chartDataIN.get(first)[0]);
        int yCorner = topBorder + effectiveHeight;
        Log.d(TAG, "closePath, x corner:" +  chartDataIN.get(chartDataIN.size() - 1)[0]);
        ChartValueSeries chartValueSeries = seriesIn[i];
        Log.d(TAG, "closePath, y corner:" +  getY(chartValueSeries, chartDataIN.get(first)[i + 1]));
        Path path = chartValueSeries.getPath();
        // Bottom right corner
        path.lineTo(getX(chartDataIN.get(chartDataIN.size() - 1)[0]), yCorner);
        // Bottom left corner
        path.lineTo(xCorner, yCorner);
        // Top right corner
        path.lineTo(xCorner, getY(chartValueSeries, chartDataIN.get(first)[i + 1]));
      }
    }
  }

  /**
   * Finds the index of the first data point containing data for a series.
   * Returns -1 if no data point contains data for the series.
   * 
   * @param seriesIndex the series's index
   */
  private int getFirstPopulatedChartDataIndex(int seriesIndex, ArrayList<double[]> chartDataIn) {
    for (int i = 0; i < chartDataIn.size(); i++) {
      if (!Double.isNaN(chartDataIn.get(i)[seriesIndex + 1])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Updates the chart dimensions.
   */
  private void updateDimensions() {
    ChartValueSeries [] seriesLocal = getCombinedSeries();
    double maxXOverlay = xExtremityMonitorOverlay.hasData() ? xExtremityMonitorOverlay.getMax() : 1.0;
    maxX = xExtremityMonitor.hasData() ? xExtremityMonitor.getMax() : 1.0;
    maxX = maxXOverlay > maxX ? maxXOverlay : maxX;
    
    for (ChartValueSeries chartValueSeries : seriesLocal) {
      chartValueSeries.updateDimension();
    }
    float density = getContext().getResources().getDisplayMetrics().density;
    spacer = (int) (density * SPACER);
    yAxisOffset = (int) (density * Y_AXIS_OFFSET);

    int markerLength = getMarkerLength(seriesLocal);

    leftBorder = (int) (density * BORDER + markerLength);
    topBorder = (int) (density * BORDER + spacer);
    bottomBorder = (int) (density * BORDER + getRect(xAxisMarkerPaint, "1").height() + spacer);
    rightBorder = (int) (density * BORDER + getRect(axisPaint, getXAxisLabel()).width() + spacer);
    updateEffectiveDimensions();
  }
  

  private int getMarkerLength(ChartValueSeries[] seriesIn) {
    float density = getContext().getResources().getDisplayMetrics().density;
    spacer = (int) (density * SPACER);

    int markerLength = 0;
    for (int i = 0; i < seriesIn.length; i ++) {
      ChartValueSeries chartValueSeries = seriesIn[i];
      if (chartValueSeries.isEnabled() && chartValueSeries.hasData()) {
        Rect rect = getRect(chartValueSeries.getMarkerPaint(), chartValueSeries.getLargestMarker());
        markerLength += rect.width() + spacer;
      }
    }
    
    return markerLength;
  }

  /**
   * Updates the effective dimensions.
   */
  private void updateEffectiveDimensions() {
    effectiveWidth = Math.max(0, width - leftBorder - rightBorder);
    effectiveHeight = Math.max(0, height - topBorder - bottomBorder);
  }

  /**
   * Updates the effective dimensions if changed.
   * 
   * @param newWidth the new width
   * @param newHeight the new height
   */
  private void updateEffectiveDimensionsIfChanged(int newWidth, int newHeight) {
    if (width != newWidth || height != newHeight) {
      width = newWidth;
      height = newHeight;
      updateEffectiveDimensions();
      updateAllPaths();
    }
  }

  /**
   * Gets the x position for a value.
   *
   * @param value the value
   */
  private int getX(double value) {
    if (value > maxX) {
      value = maxX;
    }
    double percentage = value / maxX;
    return leftBorder + (int) (percentage * effectiveWidth * zoomLevel);
  }

  /**
   * Gets the y position for a value in a chart value series
   *
   * @param chartValueSeries the chart value series
   * @param value the value
   */
  private int getY(ChartValueSeries chartValueSeries, double value) {
    int effectiveSpread = chartValueSeries.getInterval() * Y_AXIS_INTERVALS;
    double percentage = (value - chartValueSeries.getMinMarkerValue()) / effectiveSpread;
    int rangeHeight = effectiveHeight - 2 * yAxisOffset;
    return topBorder + yAxisOffset + (int) ((1 - percentage) * rangeHeight);
  }

  /**
   * Gets a waypoint's x value.
   *
   * @param waypoint the waypoint
   */
  private double getWaypointXValue(Waypoint waypoint) {
    if (chartByDistance) {
      double lenghtInKm = waypoint.getLength() * UnitConversions.M_TO_KM;
      return metricUnits ? lenghtInKm : lenghtInKm * UnitConversions.KM_TO_MI;
    } else {
      return waypoint.getDuration();
    }
  }

  /**
   * Gets a paint's Rect for a string.
   *
   * @param paint the paint
   * @param string the string
   */
  private Rect getRect(Paint paint, String string) {
    Rect rect = new Rect();
    paint.getTextBounds(string, 0, string.length(), rect);
    return rect;
  }


  /**
   * Returns the status of metricUnits.
   * 
   * @return the status of metricUnits
   */
  @VisibleForTesting
  public boolean isMetricUnits() {
    return metricUnits;
  }
}
