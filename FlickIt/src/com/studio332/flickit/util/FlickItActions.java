/**
 * FlickIt!
 * FlickItActions.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.util;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.AlphaAction;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public class FlickItActions {
   
   static public Action fadeTo (float alpha, float duration) {
      AlphaAction action = new AlphaAction();
      action.setAlpha(alpha);
      action.setDuration(duration);
      return action;
   }
   
   static public Action dimPulse(Color orig) {
      Color dim = new Color(orig.r*0.7f, orig.g*0.7f, orig.b*0.7f, 1f);
      ColorAction ca = new ColorAction();
      ca.setEndColor(dim);
      ca.setDuration(0.4f);
      ColorAction ca2 = new ColorAction();
      ca2.setEndColor(orig);
      ca2.setDuration(0.4f);
      SequenceAction sa = new SequenceAction();
      sa.addAction(ca);
      sa.addAction(ca2);
      return sa;
   }
   
   static public Action pulse (float duration) {
      SequenceAction sa = new SequenceAction();
      sa.addAction(FlickItActions.fadeTo(0.2f, duration*0.5f));
      sa.addAction( fadeOut(duration*0.5f));
      return sa;
   }
}
