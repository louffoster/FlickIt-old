/**
 * FlickIt!
 * Main.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
   public static void main(String[] args) {
      LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
      cfg.title = "FlickIt";
      cfg.useGL20 = true;
      cfg.width = 512;//800;
      cfg.height = 800;//512;
      
      new LwjglApplication(new FlickIt( new PlayStoreLinkerStub()), cfg);
   }
}
