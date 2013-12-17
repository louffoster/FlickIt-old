/**
 * FlickIt!
 * Hole.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.studio332.flickit.model.Constants;
import com.studio332.flickit.objects.Puck.Status;
import com.studio332.flickit.util.Assets;
import com.studio332.flickit.util.SoundManager;

public class Hole extends Image {
   private Body body;
   private float sinkDist;
   private float pullDist;

   public Hole(World world) {
      super(Assets.instance().getDrawable("hole"));
      setName(Constants.HOLE);

      // create the hole body
      BodyDef bodyDef = new BodyDef();
      bodyDef.type = BodyType.StaticBody;
      this.body = world.createBody(bodyDef);
      this.body.setUserData(this);

      // make the physical shape much smaller
      // so the puck has to go nearly in before
      // triggering a hit and scoring
      CircleShape circle = new CircleShape();
      circle.setRadius(Constants.pixelsToBox(getWidth() * 0.5f) );

      FixtureDef shapeDef = new FixtureDef();
      shapeDef.shape = circle;
      shapeDef.density = 0.0f;
      shapeDef.friction = 0.0f;
      shapeDef.restitution = 0.0f;
      shapeDef.isSensor = true;
      this.body.createFixture(shapeDef);
      circle.dispose();
      
      // calculate some distances
      this.sinkDist = Constants.pixelsToBox(getWidth()) * 0.35f; 
      this.pullDist = Constants.pixelsToBox(getWidth()) * 0.70f; //.725 .55
   }

   @Override
   public void draw(SpriteBatch batch, float parentAlpha) {
      final float intenstity = 4000f;//3000f;//1500f;//90f;
      Array<Body> bodies = new Array<Body>();
      this.body.getWorld().getBodies(bodies);
      Vector2 holeCenter = this.body.getPosition();
      for (Body b : bodies) {
         Object userData = b.getUserData();
         if (userData == null || userData instanceof String) {
            continue;
         }
         Actor obj = (Actor) userData;
         if (Constants.isPuck(obj)) {
            // Grab the puck and see if it is sunk. Skip it if so
            Puck puck = (Puck) obj;
            if (puck.getStatus() != Puck.Status.READY) {
               continue;
            }

            // Get the distance between the two objects.
            Vector2 position = b.getPosition();
            float dist = holeCenter.dst(position);

            // near the hole
            if (dist <= this.sinkDist) {
               // flag pending state so game layer can handle properly
               if (puck.getStatus() == Puck.Status.READY) {
                  puck.setStaus(Status.SUNK);
                  puck.stop();
                  puck.remove();
                  SoundManager.instance().playSound(SoundManager.DROP);
               }
            } else if (dist <= this.pullDist) {
               Vector2 distV = holeCenter.sub(position);
               float force = intenstity / distV.len2();
               Vector2 forceV = distV.scl(force);
               forceV = forceV.nor();

               // pull the puck towards the hole
               b.applyLinearImpulse(forceV, position, true);
            }
         }
      }

      super.draw(batch, parentAlpha);
   }

   @Override
   public void setPosition(float x, float y) {
      float scaledX = Constants.pixelsToBox(x);
      float scaledY = Constants.pixelsToBox(y);
      this.body.setTransform(new Vector2(scaledX, scaledY), this.body.getAngle());
      super.setPosition(x-getWidth()/2, y-getHeight()/2);
   }
}
