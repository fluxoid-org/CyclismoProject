package org.cowboycoders.cyclisimo.maps;

import org.cowboycoders.cyclisimo.R;

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
