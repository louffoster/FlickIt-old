/**
 * FlickIt!
 * StateListener.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.model;

public interface StateListener {
   public void startGame();
   public void startTurn();
   public void restart();
   public void quit();
   public void unpause();
   public void timerWarning();
   public void timeUp();
}
