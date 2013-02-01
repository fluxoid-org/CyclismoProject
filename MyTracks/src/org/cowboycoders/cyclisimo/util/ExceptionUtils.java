package org.cowboycoders.cyclisimo.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {
  
  private ExceptionUtils() {
    
  }
  
  public static String getStackTraceAsString(Exception e) {
    StringWriter writer = new StringWriter();
    writer.append(("Caught Exception: "));
    writer.append(e.toString());
    writer.append("\n\n");
    e.printStackTrace(new PrintWriter(writer));
    return writer.getBuffer().toString();
  }

}
