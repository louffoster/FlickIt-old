/**
 * FlickIt!
 * MessagePanel.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.studio332.flickit.FlickIt;
import com.studio332.flickit.util.Assets;

public class MessagePanel extends Group {
   private ShapeRenderer shapeRenderer;
   private Label label;
   
   public MessagePanel() {
      super();
      this.shapeRenderer = new ShapeRenderer();
      setSize(FlickIt.TGT_WIDTH*0.86f, 100f);
      setOrigin(0, 0);
      
      LabelStyle st = new LabelStyle(Assets.instance().getMessageFont(),  new Color(0.1f,0.6f,0.1f, 1f));
      this.label = new Label("Test Message", st);
      this.label.setWidth(getWidth()-20);
      this.label.setAlignment(Align.center);
      this.label.setPosition(10, 22);
      this.label.setFontScale(0.95f);
      addActor(this.label);
   }
   
   public void setMessage( String msg ) {
      this.label.setText(msg); 
   }
   
   @Override
   public void draw(SpriteBatch batch, float parentAlpha) {
      this.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
      this.shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
      this.shapeRenderer.translate(getX(), getY(), 0);
      
      // full black bar to make background of empty gauge
      batch.end();
      this.shapeRenderer.setColor( Color.BLACK);
      this.shapeRenderer.begin(ShapeType.Filled);
      this.shapeRenderer.rect(0, 0, getWidth(), getHeight());  
      
      this.shapeRenderer.setColor( Color.WHITE);
      this.shapeRenderer.rect(4,4, getWidth()-7, getHeight()-7);
      
      this.shapeRenderer.setColor( Color.BLACK);
      this.shapeRenderer.rect(6,6, getWidth()-11, getHeight()-11);
      this.shapeRenderer.end();
      
      batch.begin();
      
      super.draw(batch, parentAlpha);
   }

}
