/**
 * FlickIt!
 * Constants.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.model;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class Constants {
   public static final String TARGET = "target";
   public static final String HOLE = "hole";
   public static final String BUMPER = "bumper";
   public static final String SHOOTER = "shooter";
   public static final float PIXELS_PER_METER = 100;
   
   public static float pixelsToBox(float px) {
      return px / PIXELS_PER_METER;
   }
   
   public static float boxToPixels(float box) {
      return box * PIXELS_PER_METER;
   }
   
   public static boolean isPuck( Actor actor ) {
      String name = actor.getName();
      return  (name.contains(SHOOTER) || name.contains(TARGET));
   }
}
