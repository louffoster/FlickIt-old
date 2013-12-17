/**
 * FlickIt!
 * PlayStoreLinerImpl.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PlayStoreLinkerImpl implements PlayStoreLinker {
   private Context appContext;
   
   public PlayStoreLinkerImpl( Context appCtx) {
      this.appContext = appCtx;
   }
   
   @Override
   public void showStudio332() {
      Intent i = new Intent(Intent.ACTION_VIEW);
      i.setData(Uri.parse("https://play.google.com/store/apps/developer?id=Studio332&hl=en"));
      this.appContext.startActivity(i);
   }

}
