/**
 * FlickIt!
 * Bumper.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.objects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.studio332.flickit.model.Constants;
import com.studio332.flickit.util.Assets;

public class Bumper extends Image {
   private Body body;

   public Bumper(World world) {
      super(Assets.instance().getDrawable("bumper"));
      setName(Constants.BUMPER);

      // create the hole body
      BodyDef bodyDef = new BodyDef();
      bodyDef.type = BodyType.StaticBody;
      this.body = world.createBody(bodyDef);
      this.body.setUserData(this);
      CircleShape circle = new CircleShape();
      circle.setRadius(Constants.pixelsToBox(getWidth() * 0.5f));

      FixtureDef shapeDef = new FixtureDef();
      shapeDef.shape = circle;
      shapeDef.density = 1.0f;
      shapeDef.friction = 0.9f;
      shapeDef.restitution = 0.65f;
      this.body.createFixture(shapeDef);
      circle.dispose();
   }
   
   @Override
   public void setPosition(float x, float y) {
      float scaledX = Constants.pixelsToBox(x);
      float scaledY = Constants.pixelsToBox(y);
      this.body.setTransform(new Vector2(scaledX, scaledY), this.body.getAngle());
      super.setPosition(x-getWidth()/2, y-getHeight()/2);
   }
}
