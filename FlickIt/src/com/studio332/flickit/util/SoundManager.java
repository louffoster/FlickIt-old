/**
 * FlickIt!
 * SoundManager.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.util;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.studio332.flickit.model.Settings;

public class SoundManager {
   private static final SoundManager instance = new SoundManager();
   private Music currMusic = null;
   private Map<String, Sound> sounds = new HashMap<String, Sound>();

   public static final String MENU_CLICK = "sound/menu_click.mp3";
   public static final String THUMP = "sound/thump.mp3";
   public static final String FLICK = "sound/flick.mp3";
   public static final String CLACK = "sound/clack.mp3";
   public static final String DROP = "sound/drop.mp3";
   public static final String NEW_PUCK = "sound/new_puck.mp3";
   public static final String SCORE = "sound/score.mp3";
   public static final String PAUSE = "sound/pause.mp3";
   public static final String FOUL = "sound/foul.mp3";
   public static final String SCRATCH = "sound/scratch.mp3";
   public static final String TICKING = "sound/ticking.mp3";
   public static final String CHIME = "sound/chimes.mp3";
   public static final String OVERPOWER = "sound/miss.mp3";

   public static SoundManager instance() {
      return SoundManager.instance;
   }

   public void init() {
      this.sounds.put(MENU_CLICK, Gdx.audio.newSound( Gdx.files.internal(MENU_CLICK)));
      this.sounds.put(THUMP, Gdx.audio.newSound( Gdx.files.internal(THUMP)));
      this.sounds.put(DROP, Gdx.audio.newSound( Gdx.files.internal(DROP)));
      this.sounds.put(FLICK, Gdx.audio.newSound( Gdx.files.internal(FLICK)));
      this.sounds.put(CLACK, Gdx.audio.newSound( Gdx.files.internal(CLACK)));
      this.sounds.put(NEW_PUCK, Gdx.audio.newSound( Gdx.files.internal(NEW_PUCK)));
      this.sounds.put(SCORE, Gdx.audio.newSound( Gdx.files.internal(SCORE)));
      this.sounds.put(PAUSE, Gdx.audio.newSound( Gdx.files.internal(PAUSE)));
      this.sounds.put(FOUL, Gdx.audio.newSound( Gdx.files.internal(FOUL)));
      this.sounds.put(TICKING, Gdx.audio.newSound( Gdx.files.internal(TICKING)));
      this.sounds.put(SCRATCH, Gdx.audio.newSound( Gdx.files.internal(SCRATCH)));
      this.sounds.put(CHIME, Gdx.audio.newSound( Gdx.files.internal(CHIME)));
      this.sounds.put(OVERPOWER, Gdx.audio.newSound( Gdx.files.internal(OVERPOWER)));
   }

   public void playMenuMusic() {
      if (Settings.instance().isMusicOn() == false) {
         return;
      }

      if (this.currMusic != null ) {
         this.currMusic.stop();
         this.currMusic.dispose();
      }

      this.currMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/menu_music.ogg"));
      this.currMusic.setLooping(true);
      this.currMusic.play();
   }

   public void stopMusic() {
      if (this.currMusic != null) {
         this.currMusic.stop();
         this.currMusic.dispose();
         this.currMusic = null;
      }
   }

   public void playGameMusic() {
      if (Settings.instance().isMusicOn() == false) {
         return;
      }
      
      if (this.currMusic != null && this.currMusic.isPlaying()) {
         this.currMusic.stop();
         this.currMusic.dispose();
      }
      
      String musicFile = "sound/game_music.ogg";
      this.currMusic = Gdx.audio.newMusic(Gdx.files.internal(musicFile));
      this.currMusic.setLooping(true);
      this.currMusic.play();
   }
   
   public void pause() {
      if ( this.currMusic != null ) {
         this.currMusic.pause();
      }
      
      Sound s = this.sounds.get(SoundManager.TICKING);
      if (s != null) {
         s.pause();
      }
   }
   
   public void resume() {
      if ( this.currMusic != null ) {
         this.currMusic.play();
      }
      
      Sound s = this.sounds.get(SoundManager.TICKING);
      if (s != null) {
         s.resume();
      }
   }

   public void playSound(final String sound) {
      if (Settings.instance().isSoundOn()) {
         Sound s = this.sounds.get(sound);
         if (s != null) {
            s.play();
         }
      }
   }
   
   public void playSoundDelayed( final String sound, float delay ) {
      Timer.schedule( new Task() {
         @Override
         public void run() {
           playSound(sound);
         } 
      }, delay);
   }
}
