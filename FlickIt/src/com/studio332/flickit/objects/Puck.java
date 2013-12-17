/**
 * FlickIt!
 * Puck.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.studio332.flickit.model.Constants;
import com.studio332.flickit.model.Game;
import com.studio332.flickit.objects.ShotMeter.ShotMeterListener;
import com.studio332.flickit.util.Assets;
import com.studio332.flickit.util.FlickItActions;
import com.studio332.flickit.util.SoundManager;

public class Puck extends Image implements ShotMeterListener {

   public enum Status {READY, SUNK};
   private enum Type {SHOOTER, TARGET};
   private int number;
   private Body body;
   private Status status;
   private Type type;
   private Game game;
   private float flickAngle = 0f;
   
   public static Puck createTarget(Game game, World world, int tgtNumber ) {
      Drawable d = Assets.instance().getDrawable("target-"+tgtNumber);
      Puck p =  new Puck(game, world, d, Type.TARGET, tgtNumber);
      return p;
   }
   
   public static Puck createShooter(Game game, World world, int playerNum ) {
      Drawable d = Assets.instance().getDrawable("shooter-"+playerNum);
      Puck p =  new Puck(game, world, d, Type.SHOOTER, playerNum);
      return p;
   }
   
   private Puck(Game g, World world, Drawable d, Type t, int num) {
      super( d );
      setOrigin(getWidth()/2, getHeight()/2);
      this.game = g;
      this.flickAngle = 0f;
            
      setName(Constants.TARGET);
      if ( t.equals(Type.SHOOTER)) {
         setName(Constants.SHOOTER);
      }
      this.type = t;
      this.number = num;
      this.status = Status.READY;
      
      // First we create a body definition
      BodyDef bodyDef = new BodyDef();
      bodyDef.type = BodyType.DynamicBody;

      // Create our body in the world using our body definition
      this.body = world.createBody(bodyDef);
      this.body.setUserData( this );
      CircleShape circle = new CircleShape();
      circle.setRadius(Constants.pixelsToBox(getWidth()*0.5f));
      this.body.setAngularDamping(1.0f);

      // Create a fixture definition to apply our shape to
      FixtureDef fixtureDef = new FixtureDef();
      fixtureDef.shape = circle;
      fixtureDef.density = 3.0f; 
      fixtureDef.friction = 0.8f;
      fixtureDef.restitution = 0.4f; 

      // Create our fixture and attach it to the body
      body.createFixture(fixtureDef);

      // Remember to dispose of any shapes after you're done with them!
      // BodyDef and FixtureDef don't need disposing, but shapes do.
      circle.dispose();
      
      Body boardBody = null;
      Array<Body> bodies = new Array<Body>();
      world.getBodies(bodies);
      for ( Body b : bodies ) {
         Object ud = b.getUserData();
         if ( ud != null && ud instanceof String ){
            if ( ((String)ud).equals("floor")) {
               boardBody = b;
               break;
            }
         } 
      }

      FrictionJointDef frictionJointDef = new FrictionJointDef();
      frictionJointDef.initialize(this.body, boardBody, this.body.getWorldCenter());
      frictionJointDef.maxForce = 22;
      world.createJoint( frictionJointDef );
   }
   
   public int getNumber() {
      return this.number;
   }
   
   public void pulse() {
      addAction( Actions.forever(FlickItActions.dimPulse( getColor())));
   }
   
   public void stopPulse() {
      clearActions();
      setColor(new Color(1f,1f,1f,1f));
   }
   
   public void stop() {
      Vector2 stopV = new Vector2(0, 0);
      this.body.setAwake(false);
      this.body.setLinearVelocity(stopV);
      this.body.setTransform(this.body.getPosition(), this.body.getAngle());
   }
   
   public void setStaus( Status newS ) {
      this.status = newS;
   }
   
   public Status getStatus() {
      return this.status;
   }
   
   @Override
   public void setPosition(float x, float y) {
      float scaledX = Constants.pixelsToBox(x);
      float scaledY = Constants.pixelsToBox(y);
      this.body.setTransform(new Vector2(scaledX,scaledY), this.body.getAngle());
      super.setPosition(x-getWidth()/2, y-getHeight()/2);
   }
   
   @Override
   public void draw(SpriteBatch batch, float parentAlpha) {
      setPosition(
            Constants.boxToPixels(body.getPosition().x), 
            Constants.boxToPixels(body.getPosition().y) );
      setRotation( this.body.getAngle()*MathUtils.radDeg);
      
      super.draw(batch, parentAlpha);
   }

   public boolean isShooter() {
      return (this.type.equals(Type.SHOOTER));
   }
   
   public void setFlickAngle( float angle ) {
      this.flickAngle = angle;
      if ( this.flickAngle < 0 ) {
         this.flickAngle += 360;
      }
   }
   
   
   public float getFlickAngle() {
      return this.flickAngle;
   }

   @Override
   public void flick(float powerPercent) {
      float basePower = 100f;
      float flickPower = basePower * powerPercent;
      float fx = (float) (flickPower*Math.sin(this.flickAngle*MathUtils.degreesToRadians))*-1f;
      float fy = (float) (flickPower*Math.cos(this.flickAngle*MathUtils.degreesToRadians));
      this.body.applyForceToCenter(fx, fy, true);
      this.game.puckFlicked();
      stopPulse();
   }

   @Override
   public void badFlick() {
      SoundManager.instance().playSound(SoundManager.OVERPOWER);

      float flickPower = 1000;
      int foo = MathUtils.random(-45, 45);
      this.flickAngle+=foo;
      float fx = (float) (flickPower*Math.sin(this.flickAngle*MathUtils.degreesToRadians))*-1f;
      float fy = (float) (flickPower*Math.cos(this.flickAngle*MathUtils.degreesToRadians));
      this.body.applyForceToCenter(fx, fy, true);
      this.game.puckFlicked();
      stopPulse();
   }
}
