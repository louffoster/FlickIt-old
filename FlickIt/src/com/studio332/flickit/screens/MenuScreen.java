/**
 * FlickIt!
 * MenuScreen.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.studio332.flickit.FlickIt;
import com.studio332.flickit.model.Settings;
import com.studio332.flickit.util.Assets;
import com.studio332.flickit.util.SoundManager;

public class MenuScreen extends AbstractScreen {
   private enum ScreenType {MAIN, SETTINGS, RULES, CREDITS};
   private ScreenType screenType;
   private Group menuGroup;
   private Group settingsGroup;
   private Group rulesGroup;
   private Group chalkboard;
   private Group creditsGroup;
   
   public MenuScreen(FlickIt game) {
      super(game);
   }
   
   @Override
   public void show() {
      super.show();
      
      Image img = new Image( Assets.instance().getDrawable("wood"));
      this.stage.addActor(img);
      
      Image logo = new Image( Assets.instance().getDrawable("logo3"));
      logo.setPosition((FlickIt.TGT_WIDTH-logo.getWidth())/2, 1300);
      this.stage.addActor(logo); 
      
      // menu board
      this.chalkboard = new Group();
      Image cb = new Image(Assets.instance().getDrawable("menu"));
      this.chalkboard.addActor(cb);
      this.chalkboard.setSize(cb.getWidth(), cb.getHeight());
      this.stage.addActor(this.chalkboard);
      this.chalkboard.setPosition((FlickIt.TGT_WIDTH-cb.getWidth())/2, FlickIt.TGT_HEIGHT*0.2f);
      this.screenType = ScreenType.MAIN;
      
      // font styles for chalkboard
      TextButtonStyle tbs = new TextButtonStyle();
      tbs.font = Assets.instance().getFont();
      tbs.fontColor = new Color(1f,1f,1f, .5f);
      LabelStyle st = new LabelStyle(Assets.instance().getFont(), new Color(1f,1f,1f, .5f));
      
      createSettingsScreen(tbs, st);
      createRulesGroup(st);
      createCreditsGroup(st);

      // create a group for the main menu options
      this.menuGroup = new Group();
      this.menuGroup.setSize(this.chalkboard.getWidth(), this.chalkboard.getHeight());
      this.chalkboard.addActor(this.menuGroup);
      
      Label p = new Label("Players", st);
      p.setFontScale(1.2f);
      p.setWidth(this.menuGroup.getWidth());
      p.setAlignment(Align.center);
      p.setPosition(0, this.menuGroup.getHeight()-130);

      this.menuGroup.addActor(p);
      
      final Image underline = new Image(Assets.instance().getDrawable("underline"));
      this.menuGroup.addActor(underline);
      underline.setPosition(100+5, this.menuGroup.getHeight()-240+2);
      
      for (int i=1;i<=4;i++) {
         TextButton txt = new TextButton("   "+i+"   ", tbs);
         txt.setHeight(80);
         txt.setWidth(80);
         txt.setName(""+i);
         txt.setPosition(100+(i-1)*130, this.menuGroup.getHeight()-240);
         this.menuGroup.addActor(txt);
         
         if ( Settings.instance().getNumPlayers() > 1 ) {
            if (Settings.instance().getNumPlayers() == i ) {
               underline.setX( txt.getX()+5 );
            }
         }
         
         txt.addListener( new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
               SoundManager.instance().playSound(SoundManager.MENU_CLICK);
               int numPlayers = Integer.parseInt(event.getListenerActor().getName());
               Settings.instance().setNumPlayers(numPlayers);
               underline.setX( event.getListenerActor().getX()+5 );
               super.clicked(event, x, y);
            }
         });
      }
      
      Label t = new Label("Table", st);
      t.setFontScale(1.2f);
      t.setWidth(this.menuGroup.getWidth());
      t.setAlignment(Align.center);
      t.setPosition(0, 330);
      this.menuGroup.addActor(t);
      addTableSelector(this.menuGroup);
      
      TextButton txt = new TextButton("Play", tbs);
      txt.setPosition(530, 80);
      this.menuGroup.addActor(txt);
      
      // settings
      TextButton settings = new TextButton("Options", tbs);
      settings.setPosition(25,80);
      this.menuGroup.addActor(settings);
      settings.addListener( new InputListener() {
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            SoundManager.instance().playSound(SoundManager.MENU_CLICK);
            chalkboard.removeActor(menuGroup);
            chalkboard.addActor(settingsGroup);
            screenType = ScreenType.SETTINGS;
            return false;
         }
      });
      
      // rules
      TextButton rules = new TextButton("Rules", tbs);
      rules.setPosition(300, 80);
      this.menuGroup.addActor(rules);
      
      rules.addListener( new InputListener() {
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            SoundManager.instance().playSound(SoundManager.MENU_CLICK);
            chalkboard.removeActor(menuGroup);
            chalkboard.addActor(rulesGroup);
            screenType = ScreenType.RULES;
            return false;
         }
      });

      txt.addListener( new InputListener() {
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            SoundManager.instance().playSound(SoundManager.MENU_CLICK);
            chalkboard.addAction( Actions.sequence( Actions.moveBy(0, -1100,0.6f,Interpolation.swingIn), new Action() {
               @Override
               public boolean act(float delta) {
                  SoundManager.instance().playSound(SoundManager.DROP);
                  flickIt.showGameScreen();
                  return true;
               } 
            }));
            return true;
         }
      });
      
      SoundManager.instance().playMenuMusic();
   }

   private void createCreditsGroup(LabelStyle st) {
      this.creditsGroup = new Group();
      this.creditsGroup.setSize(this.chalkboard.getWidth(), this.chalkboard.getHeight());
      
      Label f = new Label("FlickIt!", st);
      f.setFontScale(1.2f);
      f.setWidth(this.creditsGroup.getWidth());
      f.setAlignment(Align.center);
      f.setPosition(0, 590);
      this.creditsGroup.addActor(f);
      
      Label p = new Label("Developed by Studio332", st);
      p.setFontScale(0.9f);
      p.setWidth(this.creditsGroup.getWidth());
      p.setAlignment(Align.center);
      p.setPosition(-20, 500);
      this.creditsGroup.addActor(p);
      
      int y = 390;
      String [] labels = {"Programming:", "Art Direction:", "Music:", "QA:"};
      for (int i=0; i<labels.length; i++) {
         Label l = new Label(labels[i], st);
         l.setFontScale(0.75f);
         l.setWidth(this.creditsGroup.getWidth());
         l.setAlignment(Align.right);
         l.setPosition(-390, y);
         this.creditsGroup.addActor(l);
         y-=75;
      }
      
      y = 390;
      String [] vals = {"Lou Foster", "Bryan Glickman", "Allen Foster", "Jackson Foster\nNancy Hopkins"};
      for (int i=0; i<vals.length; i++) {
         Label l = new Label(vals[i], st);
         l.setFontScale(0.75f);
         l.setWidth(this.creditsGroup.getWidth());
         l.setPosition(330, y);
         this.creditsGroup.addActor(l);
         if (i< vals.length-2) {
            y-=75;
         } else {
            y-=125;
         }
      }
   }

   private void createRulesGroup(LabelStyle st) {
      this.rulesGroup = new Group();
      this.rulesGroup.setSize(this.chalkboard.getWidth(), this.chalkboard.getHeight());
      
      Label p = new Label("Rules", st);
      p.setFontScale(1.2f);
      p.setWidth(this.settingsGroup.getWidth());
      p.setAlignment(Align.center);
      p.setPosition(0, 590);
      this.rulesGroup.addActor(p);
      
      Label r1 = new Label("1: Always hit lowest target first", st);
      r1.setFontScale(0.65f);
      r1.setPosition(40, 500);
      this.rulesGroup.addActor(r1);
      Label r2 = new Label("2: Subsequent sunk targets score", st);
      r2.setFontScale(0.65f);
      r2.setPosition(40, 450);
      this.rulesGroup.addActor(r2);
      
      Label mh = new Label("Multiplayer Extras", st);
      mh.setFontScale(0.85f);
      mh.setWidth(this.settingsGroup.getWidth());
      mh.setAlignment(Align.center);
      mh.setPosition(0, 360);
      this.rulesGroup.addActor(mh);
      
      Label m1 = new Label("* Sink opponents for points", st);
      m1.setFontScale(0.65f);
      m1.setPosition(40, 280);
      this.rulesGroup.addActor(m1);
      Label m2 = new Label("* Opponents can be used to hit\n  lowest target", st);
      m2.setFontScale(0.65f);
      m2.setPosition(40, 170);
      this.rulesGroup.addActor(m2);
      Label m3 = new Label("* Keep shooting until you miss", st);
      m3.setFontScale(0.65f);
      m3.setPosition(40, 110);
      this.rulesGroup.addActor(m3);
   }

   private void createSettingsScreen(TextButtonStyle tbs, LabelStyle st) {
      this.settingsGroup = new Group();
      this.settingsGroup.setSize(this.chalkboard.getWidth(), this.chalkboard.getHeight());
      
      Label p = new Label("Options", st);
      p.setFontScale(1.2f);
      p.setWidth(this.settingsGroup.getWidth());
      p.setAlignment(Align.center);
      p.setPosition(0, this.settingsGroup.getHeight()-130);
      this.settingsGroup.addActor(p);
      
      String lbl = "Sound: ON";
      if ( !Settings.instance().isSoundOn() ) {
         lbl = "Sound: OFF";
      }
      final TextButton snd = new TextButton(lbl, tbs);
      snd.setPosition(200, 375);
      this.settingsGroup.addActor(snd);
      snd.addListener( new ClickListener() {
         @Override
         public void clicked(InputEvent event, float x, float y) {
            SoundManager.instance().playSound(SoundManager.MENU_CLICK);
            Settings.instance().toggleSound();
            if ( Settings.instance().isSoundOn() ) {
               snd.setText("Sound: ON");
            } else {
               snd.setText("Sound: OFF");
            }
         }
      });
      
      lbl = "Music: ON";
      if ( !Settings.instance().isMusicOn() ) {
         lbl = "Music: OFF";
      }
      final TextButton music = new TextButton(lbl, tbs);
      music.setPosition(208, 275);
      this.settingsGroup.addActor(music);
      music.addListener( new ClickListener() {
         @Override
         public void clicked(InputEvent event, float x, float y) {
            SoundManager.instance().playSound(SoundManager.MENU_CLICK);
            Settings.instance().toggleMusic();
            if ( Settings.instance().isMusicOn() ) {
               SoundManager.instance().playMenuMusic();
               music.setText("Music: ON");
            } else {
               SoundManager.instance().stopMusic();
               music.setText("Music: OFF");
            }
         }
      });
      
      TextButton credits = new TextButton("Credits", tbs);
      credits.setPosition(25, 80);
      this.settingsGroup.addActor(credits); 
      credits.addListener( new InputListener() {
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            SoundManager.instance().playSound(SoundManager.MENU_CLICK);
            chalkboard.removeActor(settingsGroup);
            chalkboard.addActor(creditsGroup);
            screenType = ScreenType.CREDITS;
            return false;
         }
      });
      
      // link to studio332 in play store
      TextButton games = new TextButton("More Games", tbs);
      games.setPosition(335, 80);
      this.settingsGroup.addActor(games); 
      games.addListener( new InputListener() {
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            SoundManager.instance().playSound(SoundManager.MENU_CLICK);
            MenuScreen.this.flickIt.linkToPlayStore();
            return false;
         }
      });
      
   }

   private void addTableSelector(Group menuGroup) {
      LabelStyle st = new LabelStyle(Assets.instance().getFont(), new Color(1f,1f,1f, .5f));
      final Label table = new Label(Settings.instance().getCurrentTable().getName(), st);
      table.setWidth(menuGroup.getWidth());
      table.setAlignment(Align.center);
      table.setPosition(0, 225);
      menuGroup.addActor(table);
      
      
      Image prev = new Image(Assets.instance().getDrawable("left-arrow"));
      prev.setPosition(100, 210);
      menuGroup.addActor(prev);
      prev.addListener( new InputListener() {
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Settings.instance().prevTable();
            table.setText(Settings.instance().getCurrentTable().getName());
            return false;
         }
      });
      
      Image next = new Image(Assets.instance().getDrawable("right-arrow"));
      next.setPosition(505, 210);
      menuGroup.addActor(next);
      next.addListener( new InputListener() {
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Settings.instance().nextTable();
            table.setText(Settings.instance().getCurrentTable().getName());
            return false;
         }
      });
   }
   
   @Override
   protected boolean backClicked() {
      if ( this.screenType == ScreenType.CREDITS ) {
         this.chalkboard.removeActor(this.creditsGroup);
         this.chalkboard.addActor(this.settingsGroup);
         this.screenType = ScreenType.SETTINGS;
         return true;
      } 
      if ( this.screenType != ScreenType.MAIN ) {
         this.chalkboard.removeActor(this.settingsGroup);
         this.chalkboard.removeActor(this.rulesGroup);
         this.chalkboard.addActor(this.menuGroup);
         this.screenType = ScreenType.MAIN;
         return true;
      }
      return false;
   }

}
