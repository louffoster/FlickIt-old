/**
 * FlickIt!
 * Game.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Game {
   public enum State {
      INIT, TURN_START, PLACE_SHOOTER, 
      AWAIT_FIRST_FLICK, PLAYING, 
      FLICKED, PAUSED, TIME_UP, GAME_OVER };
   public static final int GAME_TIME_SEC = 120;
   private static final int NUM_TARGETS  = 6;
   
   private int currPlayer;
   private final int numPlayers;
   private boolean[] needToPlaceShooter = {true,true,true,true};
   private List<Integer> targets = new ArrayList<Integer>();
   private Map<Integer,Integer> score = new HashMap<Integer, Integer>();
   private float elapsedTime;
   private State state;
   private State priorState = null;
   private TableInfo tableInfo;
   private StateListener listener;
   private boolean warningIssued = false;
   private boolean lowestHitFirst = false;
   private boolean firstHit = true;
   private boolean shotScored = false;
   private boolean timerStarted = false;
   
   public Game( int numPlayers, TableInfo tableInfo ) {
      this.numPlayers = numPlayers;
      this.state = State.INIT;
      this.currPlayer = 1;
      this.tableInfo = tableInfo;
      for ( int i=1; i<=5; i++) {
         this.score.put(i, 0);
      }
   }
   
   public void setListener( StateListener l ) {
      this.listener = l;
   }
   
   public TableInfo getTableInfo() {
      return this.tableInfo;
   }
   
   public void quit() {
      if ( this.listener != null ) {
         this.listener.quit();
      }
   }
   
   public void restart() {
      if ( this.listener != null ) {
         this.listener.restart();
      }
   }
   
   public void shooterSunk( int shooterNumber ) {
      this.needToPlaceShooter[shooterNumber-1] = true;
      if ( this.currPlayer != shooterNumber ) {
         this.shotScored = true;
         int score = this.score.get(this.currPlayer);
         score+=1;
         this.score.put(this.currPlayer, score);
      }
   }
   
   public void shooterPlaced() {
      this.needToPlaceShooter[this.currPlayer-1] = false;
      if ( this.timerStarted == false ) {
         this.state = State.AWAIT_FIRST_FLICK;
      } else {
         this.state = State.PLAYING;
      }
   }
   
   public void setState( State newStatus) {
      this.state = newStatus;
   }
   
   public void startTurn() {
      if ( this.listener != null ) {
         this.listener.startTurn();
      }
   }
   
   public boolean placeShooter() {
      return this.needToPlaceShooter[this.currPlayer-1];
   }
   
   public void shotComplete() {
      if ( this.numPlayers == 1) {
         this.state = State.PLAYING;
      } else {
         if ( this.shotScored && this.needToPlaceShooter[this.currPlayer-1] == false ) {
            // if a puck was sunk, keep playing
            this.state = State.PLAYING;
         } else { 
            // No score; player turn is over.
            this.currPlayer++;
            if (this.currPlayer > this.numPlayers) {
               this.currPlayer = 1;
            }
            this.state = State.TURN_START;
         }
      }
   }
 
   public void startGame() {
      if ( this.numPlayers == 1) {
         this.state = State.AWAIT_FIRST_FLICK;
      } else { 
         this.state = State.PLAYING;
      }
      if ( this.listener != null ) {
         this.listener.startGame();
      }
   }
   
   public boolean isPlaying() {
     return (this.state == State.AWAIT_FIRST_FLICK || this.state == State.PLAYING || 
             this.state == State.FLICKED || this.state == State.TIME_UP || 
             this.state == State.PLACE_SHOOTER);
   }
   
   public boolean isFlicked() {
      return (this.state == State.FLICKED);
   }
   
   public State getState() {
      return this.state;
   }
   
   public void puckFlicked() {
      this.state = State.FLICKED;
      this.shotScored = false;
      this.lowestHitFirst = false;
      this.firstHit = true;
   }
   
   public void pauseGame() {
      this.priorState = this.state;
      this.state = State.PAUSED;
   }

   public void resumeGame() {
      this.state =  this.priorState;
      this.priorState = null;
      if ( this.listener != null ) {
         this.listener.unpause();
      }
   }
   
   public boolean isShotFirstHit() {
      return this.firstHit;
   }
   
   public void setFirstHit() {
      this.firstHit = false;
   }
   
   public boolean isLowest( int targetNum ) {
      Collections.sort(this.targets);
      int low = this.targets.get(0);
      return ( low == targetNum );
   }
   
   public void setLowestHitFirst(boolean wasLowest) {
      this.lowestHitFirst = wasLowest;
   }
   
   public void resetTargets() {
      this.targets.clear();
      for ( int i=1; i<=NUM_TARGETS; i++) {
         this.targets.add(i);
      }
   }
   
   public boolean hasTargets() {
      return (this.targets.size() > 0);
   }
   
   public boolean targetSunk( int puckNum ) {
      int score = this.score.get(this.currPlayer);
      this.targets.remove((Integer)puckNum);
      boolean good = false;
      if ( this.lowestHitFirst ) {
         score+=1;
         good = true;
         this.shotScored = true;
      }
      this.score.put(this.currPlayer, score);
      return good;
   }
   
   public int getNumPlayers() {
      return this.numPlayers;
   }
   
   public float getElapsedTime() {
      return this.elapsedTime;
   }

   /**
    * Main game mode update call.
    * 
    * @param deltaSec
    */
   public void update(float deltaSec) {
      boolean initialPlace = (this.state==State.PLACE_SHOOTER && this.timerStarted==false);
      if ( initialPlace ) {
         return;
      }
      if ( this.state == State.PLAYING || this.state == State.FLICKED || this.state==State.PLACE_SHOOTER ) {
         this.elapsedTime += deltaSec;
         this.timerStarted = true;

         if ( this.numPlayers == 1) {
            if (Game.GAME_TIME_SEC - this.elapsedTime <= 10f && this.warningIssued == false) {
               if (this.listener != null) {
                  this.warningIssued = true;
                  this.listener.timerWarning();
               }
            }
            if (Game.GAME_TIME_SEC - this.elapsedTime <=0 ) {
               this.state = State.TIME_UP;
               if ( this.listener != null ) {
                  this.listener.timeUp();
               }
            }
         }
      }
   }

   public int getScore(int playerNum) {
      return this.score.get(playerNum);
   }
   
   public int getCurrPlayer() {
      return this.currPlayer;
   }

   public int getWinner() {
      int winner = 0;
      for ( Entry<Integer, Integer> ent : this.score.entrySet()) {
         int score = ent.getValue();
         if (score >= Settings.instance().getCurrentTable().getWinScore() ) { 
            winner =  ent.getKey();
         }
      }
      return winner;
   }
}
