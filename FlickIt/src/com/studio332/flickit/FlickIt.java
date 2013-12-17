/**
 * FlickIt!
 * FlickIt.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.studio332.flickit.model.Settings;
import com.studio332.flickit.model.TableInfo;
import com.studio332.flickit.screens.GameScreen;
import com.studio332.flickit.screens.MenuScreen;
import com.studio332.flickit.util.Assets;
import com.studio332.flickit.util.SoundManager;

public class FlickIt extends Game  {
   public static final float TGT_WIDTH = 1200;
   public static final float TGT_HEIGHT = 1824;
   private PlayStoreLinker playStoreLinker = null;
   
   public FlickIt(PlayStoreLinker pl) {
      super();
      this.playStoreLinker = pl;
   }
   
   public void linkToPlayStore( ) {
      if ( this.playStoreLinker != null ) {
         this.playStoreLinker.showStudio332();
      }
   }

   @Override
   public void create() {
      Texture.setEnforcePotImages(false);
      Assets.instance().load();
      SoundManager.instance().init();
      showMenu();
   }
 
   public void showGameScreen( ) {
      TableInfo layout = Settings.instance().getCurrentTable();
      setScreen( new GameScreen(this, layout, Settings.instance().getNumPlayers()) );
   }
   
   public void showMenu() {
      setScreen( new MenuScreen(this) );
   }

   @Override
   public void resize(int width, int height) {
   }

   @Override
   public void render() {
      super.render();   // passes the render along to the current screen
      // output the current FPS
      //fpsLogger.log();
   }

   @Override
   public void pause() {
   }

   @Override
   public void resume() {
   }

   @Override
   public void dispose() {
      getScreen().dispose();
   }
}
