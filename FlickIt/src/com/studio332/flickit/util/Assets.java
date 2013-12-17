/**
 * FlickIt!
 * Assets.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.util;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class Assets {
   private static final Assets instance = new Assets();
   private Map<String, Texture> gfxMap = new HashMap<String, Texture>();
   public TextureAtlas gameAtlas;
   private BitmapFont font;
   private BitmapFont messageFont;

   public static Assets instance() {
      return Assets.instance;
   }
   
   public void load() {
      add("pixel","pixel.png");

      this.font = new BitmapFont(
            Gdx.files.internal("data/casual.fnt"), 
            Gdx.files.internal("data/casual.png"), false);
      this.messageFont = new BitmapFont(
            Gdx.files.internal("data/digital.fnt"), 
            Gdx.files.internal("data/digital.png"), false);
      
      this.gameAtlas = new TextureAtlas(Gdx.files.internal( "data/game_atlas.atlas"));
   }
   
   public BitmapFont getFont() {
      return this.font;
   }
   
   public BitmapFont getMessageFont() {
      return this.messageFont;
   }
   
   private void add( final String name, final String file) {
      Texture tx = new Texture("data/"+file);
      tx.setFilter(TextureFilter.Linear, TextureFilter.Linear);
      this.gfxMap.put(name, tx);
   }
   
   public Texture getPixel() {
      return this.gfxMap.get("pixel");
   }
   
   public AtlasRegion getAtlasRegion(final String name) {
      return Assets.instance().gameAtlas.findRegion(name);
   }
   
   public Drawable getDrawable(final String name) {
      AtlasRegion ar =  Assets.instance().getAtlasRegion(name);
      return ( new TextureRegionDrawable( ar) );
   }
}
