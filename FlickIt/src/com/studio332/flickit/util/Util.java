/**
 * FlickIt!
 * Util.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.util;

/**
 * collection of general utility functions
 * 
 * @author lfoster
 *
 */
public abstract class Util {
   
   public static String formatTime(final int timeSec) {
      int s = timeSec;
      int m = s / 60;
      s -= (m * 60);
      return String.format("%02d:%02d", m, s);
   }
}
