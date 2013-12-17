/**
 * FlickIt!
 * ShotMeter.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.studio332.flickit.model.Game;
import com.studio332.flickit.util.Assets;
import com.studio332.flickit.util.SoundManager;

public class ShotMeter extends Group {
   private Image puck;
   private ShapeRenderer shapeRenderer;
   private final float meterB = 42f; 
   private final float meterL = 44f;
   private boolean down =false;
   private float powerLevel = 0f;
   private Game game;
   private float elapsed = 0;
   private ShotMeterListener shotListener = null;
   private Array<Image> lights = new Array<Image>();
   private MessagePanel msgPanel;
   private long meterDownTime;
   
   public ShotMeter(Game g) {
      super();
      this.game = g;
      Image overlay = new Image(Assets.instance().getDrawable("board-bottom"));
      setSize(overlay.getWidth(), overlay.getHeight());
      setTouchable(Touchable.disabled);
      
      this.puck = new Image( Assets.instance().getDrawable("shooter-1"));
      this.puck.setPosition(getWidth()-this.puck.getWidth()-22, 15);
      
      this.shapeRenderer = new ShapeRenderer();
      
      float x=meterL;
      for (int i=0;i<110;i++) {
         Image l = null;
         if ( i<80) {
            l = new Image(Assets.instance().getDrawable("green-dim"));
         } else if ( i<100) {
            l = new Image(Assets.instance().getDrawable("yellow-dim"));
         } else {
            l = new Image(Assets.instance().getDrawable("red-dim"));
         }
         l.setPosition(x, meterB-1);
         this.addActor(l);
         this.lights.add(l);
         x+=9;
      }
      
      addActor(overlay);
      addActor(this.puck);
      
      this.msgPanel = new MessagePanel();
      this.msgPanel.setPosition(20, -msgPanel.getHeight());
      addActor(this.msgPanel);

      setY(-148f);
      addAction(Actions.moveTo(0, 0, 0.4f));
      
      this.addListener( new InputListener() {
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if ( y < getMeterHeight()  ) {
               down = true;
               elapsed = 0;
               meterDownTime = System.currentTimeMillis();
               return true;
            }
            return false;
         }
         
         @Override
         public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            applyPower();
         }
      });
   }
   
   public boolean isPressed() {
      return this.down;
   }
   
   public void setShotListener( Puck shooter ) {
      this.shotListener = shooter;
   }
   
   public void updateShooterIcon() {
      this.puck.setDrawable( Assets.instance().getDrawable("shooter-"+this.game.getCurrPlayer()));
   }
   
   public void disable() {
      this.setTouchable(Touchable.disabled);
   }
   public void enable() {
      this.setTouchable(Touchable.enabled);
   }
   
   public void showMessage(String msg) {
      this.setTouchable(Touchable.disabled);
      this.msgPanel.setMessage(msg);
      this.msgPanel.addAction(Actions.sequence(Actions.moveTo(20, 20, 0.4f), new Action() {

         @Override
         public boolean act(float delta) {
            SoundManager.instance().playSound(SoundManager.CHIME);
            return true;
         }
         
      }));
   }
   
   public void clearMessage() {
      this.msgPanel.addAction(Actions.moveTo(20, -this.msgPanel.getHeight(), 0.4f));
   }
   
   public float getMeterHeight() {
      return 145f;
   }

   private void applyPower() {
      if ( this.down) {
         this.down = false;
         if ( System.currentTimeMillis()-this.meterDownTime > 100 ) {
            if ( this.shotListener != null ) {
               
               if ( this.powerLevel <=100) {
                  this.shotListener.flick(this.powerLevel);
               } else {
                  this.shotListener.badFlick();
               }
            }
         } 
         this.powerLevel = 0;
         this.addAction(Actions.sequence(Actions.delay(.25f), new Action() {
   
            @Override
            public boolean act(float delta) {
               for ( int i=0;i<lights.size;i++) {
                  Image l = lights.get(i);
                  if ( i<80 ) {
                     l.setDrawable( Assets.instance().getDrawable("green-dim"));
                  } else if ( i<100) {
                     l.setDrawable( Assets.instance().getDrawable("yellow-dim"));
                  } else {
                     l.setDrawable( Assets.instance().getDrawable("red-dim"));
                  }
               }
               return true;
            }
            
         }));
      }
   }
   
   private void handlePowerUp( float dt) {
      if ( this.down ) {
         if (this.powerLevel < 110 ) {
            this.elapsed += dt;
            float threshold = 0.005f;
            if ( this.powerLevel < 10 ) {
               threshold = 0.04f;
            } else if ( this.powerLevel < 15 ) {
               threshold = 0.03f;
            } else if ( this.powerLevel < 25 ) {
               threshold = 0.02f;
            } else if ( this.powerLevel < 50 ) {
               threshold = 0.01f;
            }

            if ( this.elapsed >= threshold) {
               this.elapsed = 0;
               for (int l=0;l<2;l++) {
                  Image i = this.lights.get((int)this.powerLevel);
                  if ( this.powerLevel < 80 ) {
                     i.setDrawable( Assets.instance().getDrawable("green-lit"));
                  } else if ( this.powerLevel < 100 ) {
                     i.setDrawable( Assets.instance().getDrawable("yellow-lit"));
                  } else {
                     i.setDrawable( Assets.instance().getDrawable("red-lit"));
                  }
   
                  this.powerLevel+=1;
               }
            }
         } else {
            applyPower();
         }
      }
   }

   @Override
   public void draw(SpriteBatch batch, float parentAlpha) {
      handlePowerUp( Gdx.graphics.getDeltaTime() );
      
      this.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
      this.shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
      this.shapeRenderer.translate(getX(), getY(), 0);
      
      // full black bar to make background of empty gauge
      batch.end();
      this.shapeRenderer.setColor( Color.BLACK);
      this.shapeRenderer.begin(ShapeType.Filled);
      this.shapeRenderer.rect(0, 0, getWidth(), 150f);
      this.shapeRenderer.end();
      
      batch.begin();
      
      super.draw(batch, parentAlpha);
   }
   
   /**
    * Listener for the shots fired off from the meter
    * @author lfoster
    *
    */
   public interface ShotMeterListener {
      public void flick( float power );
      public void badFlick();
   }

}
