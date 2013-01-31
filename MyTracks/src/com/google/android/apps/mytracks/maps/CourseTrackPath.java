package com.google.android.apps.mytracks.maps;

import com.google.android.maps.mytracks.R;

import android.content.Context;

public class CourseTrackPath extends SingleColorTrackPath {
  

  private Context context;

  public CourseTrackPath(Context context) {
    super(context);
    this.context = context;
  }
  
  @Override
  public int getColor() {
    return context.getResources().getColor(R.color.slow_path);
  }

}
