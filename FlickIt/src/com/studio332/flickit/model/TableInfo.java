/**
 * FlickIt!
 * TableInfo.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

public class TableInfo {
   private String name;
   private Vector2 rackCenter;
   private Vector2 shooterStart;
   private List<Vector2> holes = new ArrayList<Vector2>();
   private List<Vector2> bumpers = new ArrayList<Vector2>();
   private int bestScore = 0;
   private int winScore;
   
   public TableInfo( final String name ) {
      this.name = name;
   }
   
   public void setWinScore( int w) {
      this.winScore = w;
   }
   
   public int getWinScore() {
      return this.winScore;
   }
   
   public void setRackCenter(float x, float y) {
      this.rackCenter = new Vector2(x, y);
   }
   
   public void setShooterStart(float x, float y) {
      this.shooterStart = new Vector2(x, y);
   }
   
   public void addHole(float x, float y) {
      this.holes.add(new Vector2(x, y));
   }
   
   public void addBumper(float x, float y) {
      this.bumpers.add(new Vector2(x, y));
   }

   public String getName() {
      return name;
   }

   public Vector2 getRackCenter() {
      return rackCenter;
   }

   public Vector2 getShooterStart() {
      return shooterStart;
   }

   public List<Vector2> getHoles() {
      return holes;
   }

   public List<Vector2> getBumpers() {
      return bumpers;
   }

   public int getHighScore() {
      return bestScore;
   }

   public void setHighScore(int hi) {
      this.bestScore = hi;
   }
}
