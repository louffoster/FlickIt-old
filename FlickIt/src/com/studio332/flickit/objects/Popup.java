/**
 * FlickIt!
 * Popup.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.studio332.flickit.FlickIt;
import com.studio332.flickit.model.Game;
import com.studio332.flickit.model.Game.State;
import com.studio332.flickit.model.Settings;
import com.studio332.flickit.util.Assets;
import com.studio332.flickit.util.SoundManager;

public class Popup extends Group {
   private Game game;
   private static final float FADE_TIME = 0.3f;
   
   public Popup( Game game ) {
      super();
      this.game = game;
      
      // NOTE: the blackboard itself is 600px wide
      Image i = new Image(Assets.instance().getDrawable("chalkboard"));
      addActor(i);
      setWidth(i.getWidth());
      setHeight(i.getHeight());
      
      setName("popup");
      setPosition((FlickIt.TGT_WIDTH-getWidth())/2, (FlickIt.TGT_HEIGHT-getHeight())/2);
      getColor().a = 0;
      setTouchable(Touchable.enabled);
   }
   
   public void initPause() {
      if ( this.game.getNumPlayers() == 1) {
         initSolitairePause();
      } else {
         initMultiplayerPause();
      }
   }
   
   public void initSolitaireOver() {
      LabelStyle st = new LabelStyle(Assets.instance().getFont(),  new Color(1f,1f,1f, .5f));
      Label txt = new Label("Time is up!", st);
      txt.setWidth(375);
      txt.setPosition(120, 380);
      txt.setAlignment(Align.center);
      txt.setFontScale(1.25f);
      addActor(txt);
      
      // show current score
      int score = this.game.getScore(1);
      Label l1 = new Label("Final Score: "+String.format("%02d", score), st);
      l1.setWidth(375);
      l1.setAlignment(Align.center);
      l1.setPosition(120, 270);
      addActor(l1);
      
      // is this a new best?
      int best = Settings.instance().getSolitaireBest(this.game.getTableInfo().getName());
      if (score > best ) {
         Label t1 = new Label("CONGRATULATIONS", st);
         t1.setFontScale(0.9f);
         t1.setWidth(375);
         t1.setPosition(120, 200);
         t1.setAlignment(Align.center);
         addActor(t1);
         
         Label t2 = new Label("New record!", st);
         t2.setWidth(375);
         t2.setPosition(120, 135);
         t2.setAlignment(Align.center);
         addActor(t2);
         
         // save new mest time
         Settings.instance().setSolitaireBest(game.getTableInfo().getName(), score);
      } else {
         
         Label t3 = new Label("High Score: "+String.format("%02d", best), st);
         t3.setPosition(165, 200);
         t3.setWidth(375);
         t3.setPosition(120, 200);
         t3.setAlignment(Align.center);
         addActor(t3);
      }
      
      addControls(st, false);
      addAction( Actions.fadeIn(FADE_TIME));
   }
   
   private void initMultiplayerPause() {
      SoundManager.instance().pause();
      LabelStyle st = new LabelStyle(Assets.instance().getFont(),  new Color(1f,1f,1f, .5f));
      Label txt = new Label("PAUSED", st);
      txt.setWidth(375);
      txt.setPosition(120, 400);
      txt.setAlignment(Align.center);
      addActor(txt);
      
      showScoreboard(st,0);
      
      addControls(st, true);
      addAction( Actions.fadeIn(FADE_TIME));
   }
   
   private void initSolitairePause() {
      SoundManager.instance().pause();
      LabelStyle st = new LabelStyle(Assets.instance().getFont(),  new Color(1f,1f,1f, .5f));
      Label txt = new Label("PAUSED", st);
      txt.setWidth(375);
      txt.setPosition(120, 380);
      txt.setAlignment(Align.center);
      txt.setFontScale(1.25f);
      addActor(txt);
      
      // show current score
      int score = this.game.getScore(1);
      Label l1 = new Label("Current Score: "+String.format("%02d", score), st);
      l1.setWidth(375);
      l1.setAlignment(Align.center);
      l1.setPosition(120, 270);
      addActor(l1);
      
      // show HIGH score
      int hiScore = this.game.getTableInfo().getHighScore();
      Label l2 = new Label("High Score: "+String.format("%02d", hiScore), st);
      l2.setWidth(375);
      l2.setAlignment(Align.center);
      l2.setPosition(120, 200);
      addActor(l2);
      
      addControls(st, true);
      
      addAction( Actions.fadeIn(FADE_TIME));
   }
   
   private void addControls(LabelStyle st, boolean includeResume) {
      TextButtonStyle tbs = new TextButtonStyle();
      tbs.font = Assets.instance().getFont();
      tbs.fontColor = new Color(1f,1f,1f, .5f);
      float restartX = 30f;
      if ( includeResume ) {
         restartX = 260f;
         TextButton resume = new TextButton("Resume", tbs);
         resume.setPosition(30, 60);
         addActor(resume);
         resume.addListener( new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
               SoundManager.instance().playSound(SoundManager.MENU_CLICK);
               game.resumeGame();
            }
         });
      }
      
      TextButton rsb = new TextButton("Restart", tbs);
      rsb.setPosition(restartX, 60);
      addActor(rsb);
      rsb.addListener( new ClickListener() {
         @Override
         public void clicked(InputEvent event, float x, float y) {
            SoundManager.instance().playSound(SoundManager.MENU_CLICK);
            remove();
            game.restart();
         }
      });
      
      TextButton qb = new TextButton("Quit", tbs);
      qb.setPosition(495, 60);
      addActor(qb);
      qb.addListener( new ClickListener() {
         @Override
         public void clicked(InputEvent event, float x, float y) {
            SoundManager.instance().playSound(SoundManager.MENU_CLICK);
            game.quit();
         }
      });
   }
   
   /**
    * Show turn start pupup for multiplayer game. Lists current
    * player, scores and score needed to win
    */
   public void showTurnStart() {
      LabelStyle st = new LabelStyle(Assets.instance().getFont(),  new Color(1f,1f,1f, .5f));
      Label txt = new Label("Get Ready ", st);
      txt.setAlignment(Align.center);
      txt.setPosition(130, 400);
      addActor(txt);
      
      Image puck = new Image( Assets.instance().getDrawable("shooter-"+this.game.getCurrPlayer()));
      puck.setScale(0.9f);
      puck.setPosition(410,380);
      addActor(puck);
      
      showScoreboard(st, 0);
      
      Label win = new Label("Points to win "+this.game.getTableInfo().getWinScore(), st);
      win.setFontScale(0.7f);
      win.setPosition(285, 50);
      addActor(win);
      
      // fade in popup and listen for taps to clear
      fadeInAndListen();
   }
   
   private void showScoreboard(LabelStyle st, int offset) {
      float y=315-(25*(4-this.game.getNumPlayers()));
      y-=offset;
      for (int i=1;i<=this.game.getNumPlayers(); i++) {
         Image p = new Image( Assets.instance().getDrawable("shooter-"+i));
         p.setScale(0.4f);
         p.setPosition(220,y);
         addActor(p);
         
         Label s = new Label ("- "+String.format("%02d", this.game.getScore(i)), st);
         s.setFontScale(0.8f);
         s.setPosition(290,y-10);
         addActor(s);
         
         y-=(55+(4*(5-this.game.getNumPlayers())));
      }
   }
   
   public void showMultiplayerWinner() {
      LabelStyle st = new LabelStyle(Assets.instance().getFont(),  new Color(1f,1f,1f, .5f));
      Label txt = new Label("Winner ", st);
      txt.setAlignment(Align.center);
      txt.setPosition(160, 400);
      addActor(txt);
      
      int winner = this.game.getWinner();
      Image puck = new Image( Assets.instance().getDrawable("shooter-"+winner));
      puck.setScale(0.9f);
      puck.setPosition(378,380);
      addActor(puck);
      
      showScoreboard(st,-10);
      
      addControls(st, false);
      addAction( Actions.fadeIn(FADE_TIME));
   }

   public void initSolitareStart() {
      
      LabelStyle st = new LabelStyle(Assets.instance().getFont(),  new Color(1f,1f,1f, .5f));
      Label txt = new Label(
            "Sink the target pucks as fast as you can. Always hit the lowest numbered target first.\n\nGood Luck!", 
            st);
      txt.setWidth(getWidth()-40);
      txt.setHeight(getHeight());
      txt.setWrap(true);
      txt.setPosition(5, 65);
      txt.setAlignment(Align.center);
      txt.setFontScale(0.85f);
      addActor(txt);
      
      String hiMsg = "High Score: ";
      int hiScore = this.game.getTableInfo().getHighScore();
      if ( hiScore > 0 ) {
         hiMsg += String.format("%02d", hiScore);
      } else {
         hiMsg += "None";
      }
      Label time = new Label(hiMsg, st);
      time.setFontScale(0.9f);
      time.setPosition(35, 65);
      addActor(time);
      
      // fade in popup and listen for taps to clear
      fadeInAndListen();     
   }
   
   private void fadeInAndListen() {
      addAction( Actions.fadeIn(FADE_TIME));
      addListener( new InputListener() {
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            return true;
         }
         @Override
         public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if ( game.getState() == State.INIT) {
               game.startGame();
            } else {
               game.startTurn();
            }
         }
      });
   }

   public void fadeOut() {
      setZIndex(1000);
      addAction( Actions.sequence( Actions.moveBy(0, -1100,0.75f,Interpolation.swingIn), new Action(){
         @Override
         public boolean act(float delta) {
            Popup.this.remove();

            Popup.this.clearListeners();
            return true;
         }
      }));
   }
}
