package org.cowboycoders.cyclisimo.maps;

import android.content.Context;

import org.cowboycoders.cyclisimo.R;

public class CourseTrackPath extends SingleColorTrackPath {
  

  private Context context;

  public CourseTrackPath(Context context) {
    super(context);
    this.context = context;
  }
  
  @Override
  public int getColor() {
    return context.getResources().getColor(R.color.course_underlay);
  }

}
