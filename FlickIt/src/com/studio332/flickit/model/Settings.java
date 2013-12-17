/**
 * FlickIt!
 * Settings.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Persisted blastwords game settings
 * 
 * @author lfoster
 *
 */
public final class Settings {
   private static final Settings instance = new Settings();
   private boolean soundOn = true;
   private boolean musicOn = true;
   private boolean rulesEverSeen = false;
   private List<TableInfo> tables = new ArrayList<TableInfo>();
   private int currTableIdx = 0;
   private int numPlayers = 1;
   
   private static final String PREFS_NAME = "flickit";
   
   public static Settings instance() {
      return Settings.instance;
   }
   
   private Settings() {
      
      loadTables();
      
      // NOTES: on desktop, file is store in /Users/[username]/.prefs/[name]
      Preferences p =  Gdx.app.getPreferences( PREFS_NAME );
      if ( p.contains("soundOn")) {
         this.soundOn = p.getBoolean("soundOn");
      }
      if ( p.contains("musicOn")) {
         this.musicOn = p.getBoolean("musicOn");
      }
      if ( p.contains("rulesEverSeen")) { 
         this.rulesEverSeen = p.getBoolean("rulesEverSeen");
      }
      
      // look for best scores on all of the tables
      // best score is stored as a key matching the map file
      for ( TableInfo ti : this.tables ) {
         if ( p.contains(ti.getName())) {
            ti.setHighScore( p.getInteger(ti.getName()));
         } 
      }
   }
   
   public void nextTable() {
      this.currTableIdx++;
      if ( this.currTableIdx >= this.tables.size() ) {
         this.currTableIdx = 0;
      }
   }
   
   public void prevTable() {
      this.currTableIdx--;
      if ( this.currTableIdx < 0 ) {
         this.currTableIdx = this.tables.size()-1;
      }
   }
   
   public TableInfo getCurrentTable() {
      return this.tables.get(this.currTableIdx);
   }
   
   private void loadTables() {
      FileHandle handle = Gdx.files.internal("data/tables.json");
      JsonParser p = new JsonParser();
      String jsonStr = handle.readString();
      JsonArray tableArray = p.parse(jsonStr).getAsJsonArray();
      for (int i = 0; i < tableArray.size(); i++) {
         JsonObject tableObj = tableArray.get(i).getAsJsonObject();
         this.tables.add( parseTable(tableObj));
      }
   }

   private TableInfo parseTable( JsonObject tableObj ) {
      TableInfo table = new TableInfo(tableObj.get("name").getAsString());
      JsonObject coordObj = tableObj.get("rackCenter").getAsJsonObject();
      table.setRackCenter(coordObj.get("x").getAsFloat(), coordObj.get("y").getAsFloat());
      coordObj = tableObj.get("shooterStart").getAsJsonObject();
      table.setShooterStart(coordObj.get("x").getAsFloat(), coordObj.get("y").getAsFloat());
      table.setWinScore(tableObj.get("winScore").getAsInt());
      
      // parse holes
      JsonArray objArray = tableObj.get("holes").getAsJsonArray();
      for (int i = 0; i < objArray.size(); i++) {
         coordObj = objArray.get(i).getAsJsonObject();
         table.addHole(coordObj.get("x").getAsFloat(), coordObj.get("y").getAsFloat());
      }
      
      // parse bumpers
      objArray = tableObj.get("bumpers").getAsJsonArray();
      for (int i = 0; i < objArray.size(); i++) {
         coordObj = objArray.get(i).getAsJsonObject();
         table.addBumper(coordObj.get("x").getAsFloat(), coordObj.get("y").getAsFloat());
      }
      return table;
   }
   
   public int getNumPlayers() {
      return this.numPlayers;
   }
   public void setNumPlayers( int np ) {
      this.numPlayers = np;
   }
   public int getSolitaireBest( final String tableName ) {
      for (TableInfo ti : this.tables ) {
         if ( ti.getName().equals(tableName)) {
            return ti.getHighScore();
         }
      }
      return 0;
   }
   
   public void setSolitaireBest(final String table, int score ) {
      for (TableInfo ti : this.tables ) {
         if ( ti.getName().equals(table)) {
            ti.setHighScore(score);
         }
      }
      Preferences p =  Gdx.app.getPreferences( PREFS_NAME );
      p.putInteger(table, score);
      p.flush();
   }
   
   public boolean rulesEverSeen() {
      return this.rulesEverSeen;
   }
   
   public void rulesViewed() {
      this.rulesEverSeen = true;
      Preferences p =  Gdx.app.getPreferences( PREFS_NAME );
      p.putBoolean("rulesEverSeen", this.rulesEverSeen);
      p.flush();
   }
   
   public void toggleSound() {
      this.soundOn = !this.soundOn;
      Preferences p =  Gdx.app.getPreferences( PREFS_NAME );
      p.putBoolean("soundOn", this.soundOn);
      p.flush();
   }
   
   public boolean isSoundOn() {
      return this.soundOn;
   }
   
   public void toggleMusic() {
      this.musicOn = !this.musicOn;
      Preferences p =  Gdx.app.getPreferences( PREFS_NAME );
      p.putBoolean("musicOn", this.musicOn);
      p.flush();
   }
   
   public boolean isMusicOn() {
      return this.musicOn;
   }
}
