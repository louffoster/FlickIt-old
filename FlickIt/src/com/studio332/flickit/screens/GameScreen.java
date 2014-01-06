/**
 * FlickIt!
 * GameScreen.java
 * 
 * Created by Lou Foster
 * Copyright Studio332 2013. All rights reserved.
 */
package com.studio332.flickit.screens;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.studio332.flickit.FlickIt;
import com.studio332.flickit.model.Constants;
import com.studio332.flickit.model.Game;
import com.studio332.flickit.model.Game.State;
import com.studio332.flickit.model.StateListener;
import com.studio332.flickit.model.TableInfo;
import com.studio332.flickit.objects.Bumper;
import com.studio332.flickit.objects.Hole;
import com.studio332.flickit.objects.Popup;
import com.studio332.flickit.objects.Puck;
import com.studio332.flickit.objects.Puck.Status;
import com.studio332.flickit.objects.ShotMeter;
import com.studio332.flickit.util.Assets;
import com.studio332.flickit.util.FlickItActions;
import com.studio332.flickit.util.Overlay;
import com.studio332.flickit.util.SoundManager;
import com.studio332.flickit.util.Util;

public class GameScreen extends AbstractScreen implements ContactListener, StateListener {

   private World world;
   private float dtAccumulator = 0f;
   private Group tgtGroup;
   private Game game;
   private Label timer;
   private Label score;
   private Overlay overlay;
   private ShotMeter shotMeter;
   private Image angleArrow;
   private ShapeRenderer shapeRenderer;
   //Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
   
   private static final float RACK_FADE_IN_TIME = 0.5f;
   private static final float SHOT_LINE_Y = 700f;
   
   public GameScreen(FlickIt game, TableInfo layout, int numPlayers) {
      super(game);
      this.game = new Game(numPlayers, layout);
      this.game.setListener(this);
      this.shapeRenderer = new ShapeRenderer();
   }
   
   @Override
   public void show() {
      super.show();
      this.game.setState(State.INIT);
      
      // create the box2d world and table
      this.world = new World(new Vector2(0,0), true);
      this.world.setContactListener( this );
      World.setVelocityThreshold(0f);  // eliminate pucks stucking to walls
      defineBoard();
      
      // add holes 
      for ( Vector2 pos : this.game.getTableInfo().getHoles()) {
         Hole hole = new Hole(this.world);
         hole.setPosition(FlickIt.TGT_WIDTH*pos.x,FlickIt.TGT_HEIGHT* pos.y);
         this.stage.addActor(hole);
      }
      
      // add BUMPERS 
      for ( Vector2 pos : this.game.getTableInfo().getBumpers()) {
         Bumper b = new Bumper(this.world);
         b.setPosition(FlickIt.TGT_WIDTH*pos.x,FlickIt.TGT_HEIGHT* pos.y);
         this.stage.addActor(b);
      }
          
      // show the initial game start popup
      Popup p = new Popup(this.game);
      addScoreAndTimer();
      if ( this.game.getNumPlayers() == 1) {
         p.initSolitareStart();
      } else {
         p.showTurnStart();
      }
      this.stage.addActor(p);
      SoundManager.instance().playGameMusic();
      
      // overlay used to fade screen and flash colors
      this.overlay = new Overlay();
      this.stage.addActor(this.overlay);
      this.overlay.setZIndex(100);
      
      this.stage.addListener( new InputListener() {
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if ( game.getState() == State.PLACE_SHOOTER ) {
               if ( y > shotMeter.getMeterHeight() && y < SHOT_LINE_Y ) {
                  addShooter(x, y);
                  game.shooterPlaced();
                  shotMeter.clearMessage();
                  attachArrowToShooter();
               } else {
                  SoundManager.instance().playSound(SoundManager.SCRATCH);
               }
            } else if ( game.isPlaying() && y > shotMeter.getMeterHeight() ) {
               aimPuck(x,y);
               return true;
            }
            return false;
         }
         
         @Override
         public void touchDragged(InputEvent event, float x, float y, int pointer) {
            if ( game.isPlaying() && y > shotMeter.getMeterHeight() ) {
               aimPuck(x,y);
            }
         }
      });
   }
   
   private void aimPuck(float x, float y) {
      double dY = this.angleArrow.getY() - y;
      double dX = this.angleArrow.getX() - x;
      float angle = (float) (Math.atan2(dY, dX) * 180 / Math.PI + 90);
      this.angleArrow.setRotation(angle);
      Puck shooter = findCurrentShooter();
      if (shooter != null) {
         shooter.setFlickAngle(angle);
      }
   }
   
   private void addScoreAndTimer() {
      LabelStyle st = new LabelStyle(Assets.instance().getMessageFont(), new Color(0.1f,0.6f,0.1f, 0.75f));
      if ( this.game.getNumPlayers() == 1) {
         this.timer = new Label("0", st);
         this.timer.setFontScale(0.92f);
         this.timer.setPosition(-2f, FlickIt.TGT_HEIGHT-this.timer.getHeight()/1.62f);
         this.stage.addActor(this.timer);
         updateTimer();
      }
      
      this.score = new Label("00", st);
      this.score.setPosition(FlickIt.TGT_WIDTH-this.score.getWidth()-25, 
            FlickIt.TGT_HEIGHT-this.score.getHeight()/1.4f);
      this.score.setFontScale(1.25f);
      this.stage.addActor(this.score);
      updateScore();  
   }
   
   private void updateTimer() {
      String time = Util.formatTime(Math.round(Game.GAME_TIME_SEC-this.game.getElapsedTime()));
      this.timer.setText(time);
   }
   
   private void updateScore() {
      if ( this.score != null ) {
         int playerNum = this.game.getCurrPlayer();
         String time = String.format("%02d", this.game.getScore(playerNum));
         this.score.setText(time);
      }
   }
   
   private void setupFirstRack() {
      this.tgtGroup = new Group();
      this.stage.addActor(this.tgtGroup);
      rackTargets();
      
      this.angleArrow = new Image( Assets.instance().getDrawable("arrow"));
      this.stage.addActor(this.angleArrow);
      this.angleArrow.setVisible(false);
   }
   
   private void attachArrowToShooter() {
      this.stage.addAction( Actions.sequence(Actions.delay(0.3f), new Action() {

         @Override
         public boolean act(float delta) {
            Puck shooter = findCurrentShooter();
            angleArrow.setRotation(0);
            angleArrow.setOrigin(angleArrow.getWidth()/2, 0);
            angleArrow.setPosition(
                  shooter.getX()+(shooter.getWidth()-angleArrow.getWidth())/2, 
                  shooter.getY()+shooter.getHeight()/2);
            angleArrow.setVisible(true);
            angleArrow.setZIndex(450);
            shooter.setZIndex(500);
            shooter.setFlickAngle(0);
            shotMeter.enable();
            updateScore();
            return true;
         }
         
      }) );
     
   }
   
   private void addShooter(float x, float y) {
      if (this.game.getState() != State.TIME_UP && this.game.getState() != State.GAME_OVER) {
         final Puck newShooter = Puck.createShooter(game, world, this.game.getCurrPlayer());
         newShooter.setName("shooter-" + this.game.getCurrPlayer());
         newShooter.getColor().a = 0f;
         newShooter.setFlickAngle(0);
         this.angleArrow.setRotation(0);
         this.angleArrow.setVisible(false);
         this.shotMeter.setShotListener(newShooter);

         newShooter.setPosition(x, y);
         stage.addActor(newShooter);
         SoundManager.instance().playSound(SoundManager.NEW_PUCK);
         newShooter.addAction(Actions.sequence(Actions.fadeIn(0.1f), new Action() {
            @Override
            public boolean act(float delta) {
               newShooter.pulse();
               newShooter.setZIndex(500);
               return true;
            }
         }));
      }
   }
   
   private void rackTargets() {
      this.tgtGroup.getColor().a  = 0;
      Vector2 rawCenter = this.game.getTableInfo().getRackCenter();
      Vector2 centerPos = new Vector2(FlickIt.TGT_WIDTH*rawCenter.x, FlickIt.TGT_HEIGHT*rawCenter.y);
      Puck p = Puck.createTarget(this.game, this.world, 1);
      p.setPosition(centerPos.x, centerPos.y-55);
      this.tgtGroup.addActor(p);
      float[] dX = {-55, 55, 0, -103, 103};
      float[] dY = {40, 40, 138, 138, 138};
      for (int i=2; i<=6; i++) {
         Puck q = Puck.createTarget(this.game, this.world, i);
         q.setPosition(centerPos.x+dX[i-2], centerPos.y+dY[i-2]);
         q.setName("target-"+i);
         this.tgtGroup.addActor(q);
         q.setZIndex(400+i);
      }
      this.tgtGroup.addAction( Actions.fadeIn(RACK_FADE_IN_TIME));
      this.game.resetTargets();
   }

   private void defineBoard() {
      // table background
      Image img = new Image(Assets.instance().getDrawable("wood"));
      this.stage.addActor(img);
      this.shotMeter = new ShotMeter(this.game);
      this.stage.addActor(this.shotMeter);
      
      // create the walls around the table
      BodyDef groundBodyDef = new BodyDef();
      Body groundBody = this.world.createBody(groundBodyDef);
      float sz = Constants.pixelsToBox(150f);
      float bottomH = Constants.pixelsToBox(150f);
      float w = Constants.pixelsToBox(FlickIt.TGT_WIDTH);
      float h = Constants.pixelsToBox(FlickIt.TGT_HEIGHT);
      float[] xVert = { sz, w - sz, w, w, w - sz, sz, 0, 0 };
      float[] yVert = { bottomH, bottomH, sz+bottomH, h - sz, h, h, h - sz, sz+bottomH };
      for (int i = 0; i < xVert.length; i++) {
         int j = i + 1;
         if (j == xVert.length) {
            j = 0;
         }

         EdgeShape wallEdge = new EdgeShape();
         wallEdge.set(xVert[i], yVert[i], xVert[j], yVert[j]);
         FixtureDef wallFixDef = new FixtureDef();
         wallFixDef.friction = 0.9f;
         wallFixDef.restitution = 0.7f;//85f;
         wallFixDef.shape = wallEdge;
         groundBody.createFixture(wallFixDef);
         wallEdge.dispose();
      }

      // Define the table floor
      BodyDef bd = new BodyDef();
      Body floorBody = this.world.createBody(bd);
      floorBody.setUserData("floor");
      PolygonShape ps = new PolygonShape();
      ps.setAsBox(w, h);
      floorBody.createFixture(ps, 0);
      ps.dispose();
   }
   
   /**
    * Main update loop for game
    * @param deltaT
    */
   private void updateGame(float deltaT) {
      this.game.update(deltaT);
      if ( this.game.getNumPlayers() == 1) {
         updateTimer();
      }
      
      if ( this.game.isFlicked() || this.game.getState().equals(State.TIME_UP) ) {
         this.angleArrow.setVisible(false);
         this.shotMeter.disable();
      }
      
      Array<Body> bodies = new Array<Body>();
      this.world.getBodies(bodies);
      boolean pucksMoving = false;

      for (Body b : bodies) {
         Object userData = b.getUserData();
         if (userData == null || userData instanceof String) {
            continue;
         }

         Actor obj = (Actor) userData;
         if (Constants.isPuck(obj)) {
            Puck puck = (Puck)obj;
            if ( puck.getStatus() == Status.SUNK) {
               this.world.destroyBody(b);
               if (puck.isShooter() ) {
                  shooterSunk(puck);
               } else {
                  targetSunk(puck);
               }
            } else {
               // see if any pucks are still moving
               float velocity = b.getLinearVelocity().len2();
               if (velocity > 0.08) {
                  pucksMoving = true;
               } else {
                  if (velocity > 0.00) {
                     puck.stop();
                  }
               }
            }
         }
      }
      
      // if no pucks are moving and the time is up, show game over
      if ( this.game.getState().equals(State.TIME_UP) && pucksMoving == false && !this.shotMeter.isPressed() ) {
         this.game.setState(State.GAME_OVER);
         this.angleArrow.setVisible(false);
         this.shotMeter.disable();
         Puck shooter = findCurrentShooter();
         if ( shooter != null ) {
            shooter.stopPulse();
         }
         
         if (!isPopupVisible() ) {
            Popup p = new Popup(this.game);
            p.initSolitaireOver();
            this.stage.addActor(p);
         }
      }
      
      // Only when all pucks have stopped and all score/scratch/opponent sunk
      // events are handled can we call the shot complete
      if (this.game.getState() == State.FLICKED && pucksMoving == false) {
         boolean win =  (this.game.getNumPlayers() > 1 && 
               this.game.getScore( this.game.getCurrPlayer()) >= game.getTableInfo().getWinScore());
         this.game.shotComplete();
         if (!isPopupVisible()) {
            if (win) {
               SoundManager.instance().stopMusic();
               this.angleArrow.setVisible(false);
               this.shotMeter.disable();
               Popup p = new Popup(this.game);
               p.showMultiplayerWinner();
               this.stage.addActor(p);
            } else {
               if (this.game.getState() == State.TURN_START) {
                  Popup p = new Popup(this.game);
                  p.showTurnStart();
                  this.stage.addActor(p);
               } else {
                  // same player still shooting, just reattach arrow and pulse shooter
                  Puck curr = findCurrentShooter();
                  if (curr != null) {
                     curr.pulse();
                     attachArrowToShooter();
                  }
               }
            }
         }
      }
   }
   
   private void animateScore( float x, float y, int score ) {
      String img = "plus1";
      final Image scoreImg = new Image( Assets.instance().getDrawable(img) );
      scoreImg.scale(1.5f);
      scoreImg.getColor().a = 0f;
      scoreImg.setPosition( x,y);
      Action moveFade = parallel( 
            fadeOut(1.5f), 
            moveBy(0f, 400f, 1.5f, Interpolation.sineOut), 
            Actions.scaleTo(0.0f, 0.0f, 1.5f));
      Action s = sequence( fadeIn(0.1f), moveFade, new Action() {
         @Override
         public boolean act(float delta) {
            scoreImg.remove();
            return false;
         }
         
      });
      this.stage.addActor(scoreImg);
      scoreImg.addAction( s );
   }
    
   private void shooterSunk(final Puck shooter) {      
      this.game.shooterSunk( shooter.getNumber() );
      if ( shooter.getNumber() == this.game.getCurrPlayer() ) {
         this.shotMeter.setShotListener(null);
      }
      
      if ( this.game.getNumPlayers() == 1 ) {
         // delay is the penalty for scratching.
         float delay = 1.5f;
   
         // delay a bit, then bring puck pack
         Timer.schedule( new Task() {
            @Override
            public void run() {
               game.setState(State.PLACE_SHOOTER);
               shotMeter.showMessage("Tap to place your shooter below the line");
            } 
         }, delay);
      } else {
         if ( shooter.getNumber() != this.game.getCurrPlayer() ) {
            SoundManager.instance().playSoundDelayed(SoundManager.SCORE, 0.4f);
            animateScore(shooter.getX()+shooter.getWidth()/2, shooter.getY()+shooter.getHeight()/2, 1);
         }
      }
   }
   
   private void targetSunk(Puck target) {
      target.remove();
      boolean inOrder = this.game.targetSunk(target.getNumber());
      if ( inOrder ) {
         SoundManager.instance().playSoundDelayed(SoundManager.SCORE, 0.4f);
         animateScore(target.getX()+target.getWidth()/2, target.getY()+target.getHeight()/2, 1);
      } else {
         SoundManager.instance().playSoundDelayed(SoundManager.FOUL, 0.4f);
      }
      updateScore();
     
      if ( this.game.hasTargets() == false ) {
         rackTargets();
      }
   }
   
   @Override
   public void render(float delta) {
      
      // Call box2d with a fixed timestep
      final int VELOCITY_ITERATIONS = 6;
      final int POSITION_ITERATIONS = 2;
      final float FIXED_TIMESTEP = 1.0f / 60.0f;
      this.dtAccumulator += delta;
      while (this.dtAccumulator > FIXED_TIMESTEP) {
         this.dtAccumulator -= FIXED_TIMESTEP;
         if ( this.game.isPlaying() ) {
            this.world.step(FIXED_TIMESTEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            updateGame(FIXED_TIMESTEP);
         } 
      }
      
      super.render(delta);
      
      if ( this.game.getState() == State.PLACE_SHOOTER  ) {
         if ( isPopupVisible() == false ) {
            this.shapeRenderer.setProjectionMatrix(getBatch().getProjectionMatrix());
            this.shapeRenderer.setTransformMatrix(getBatch().getTransformMatrix());
            this.shapeRenderer.translate(0,0, 0);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            
            this.shapeRenderer.setColor( new Color(1f,0,0,0.5f));
            this.shapeRenderer.begin(ShapeType.Filled);
            this.shapeRenderer.rect(0, 700, FlickIt.TGT_WIDTH, 20f);
            this.shapeRenderer.end();
            this.shapeRenderer.setColor( new Color(0.2f,0,0,0.7f));
            this.shapeRenderer.begin(ShapeType.Line);
            this.shapeRenderer.rect(-1, SHOT_LINE_Y, FlickIt.TGT_WIDTH+2, 20f);
            this.shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
         }
      }
      
      //debugRender();
   }
   
//   private void debugRender() {
//      Matrix4 m = stage.getCamera().combined;
//      m.scl(Constants.PIXELS_PER_METER);
//      debugRenderer.render(world, stage.getCamera().combined);
//   }

   @Override
   public void endContact(Contact contact) {
      Fixture fixtureA = contact.getFixtureA();
      Fixture fixtureB = contact.getFixtureB();
      if ( fixtureA != null  && fixtureB != null ) {
         Object userA = fixtureA.getBody().getUserData();
         Object userB = fixtureB.getBody().getUserData();
         if ( userA == null || userB == null ) {
            SoundManager.instance().playSound(SoundManager.THUMP);
         } else {
            if ( (userA instanceof Hole) == false && (userB instanceof Hole) == false ) {
               // neither is hole or wall. clack sound
               SoundManager.instance().playSound(SoundManager.CLACK);
               if ( (userA instanceof Bumper) || (userB instanceof Bumper) ) {
                  // if either is a bumper, done
                  return;
               }
               
               // are both target pucks or both shooters?
               Puck puckA = (Puck)userA;
               Puck puckB = (Puck)userB;
               if ( (puckA.isShooter() == false && puckB.isShooter() == false) ||  
                    (puckA.isShooter() == true  && puckB.isShooter() == true)) {
                  // if they are both targets or both shooters, colliding, nothing more to do
                  return;
               }
               
               // this is a shooter / target collision
               if ( this.game.isShotFirstHit() ) {
                  this.game.setFirstHit();
                  boolean lowest = false;
                  if ( puckA.isShooter() ) {
                     // A is shooter, B must be target
                     //System.err.println("First hit is "+puckB.getNumber());
                     lowest = this.game.isLowest(puckB.getNumber());
                  } else {
                     // B is shooter
                     //System.err.println("First hit is "+puckA.getNumber());
                     lowest = this.game.isLowest(puckA.getNumber());
                  }
                  this.game.setLowestHitFirst(lowest);
                  
               }
            }
         }
      }
   }
   
   private Puck findCurrentShooter() {
      String name = "shooter-"+this.game.getCurrPlayer();
      Puck shooter = null;
      for (Actor actor : this.stage.getActors() ) {
         if (actor.getName() != null && actor.getName().equals(name)) {
            shooter =  (Puck)actor;
         }
      }
      return shooter;
   }

   @Override
   public void beginContact(Contact contact) {      
   }

   @Override
   public void preSolve(Contact contact, Manifold oldManifold) { 
   }

   @Override
   public void postSolve(Contact contact, ContactImpulse impulse) {      
   }
   
   private void removePopup() {
      for (Actor a : this.stage.getActors()) {
         if (a.getName() == null) {
            continue;
         }
         if (a.getName().equals("popup")) {
            Popup popup = (Popup)a;
            popup.fadeOut();
         }
      }
   }

   @Override
   public void startGame() {
      setupFirstRack();      
      removePopup();
      this.stage.addAction(Actions.sequence(Actions.delay(0.75f), new Action() {
         @Override
         public boolean act(float delta) {
            shotMeter.showMessage("Tap to place your shooter below the line");
            game.setState(State.PLACE_SHOOTER);
            return true;
         }
      }));
   }

   @Override
   public void startTurn() {
      removePopup();
      updateScore();
      this.shotMeter.updateShooterIcon();
      if ( this.game.placeShooter() ) {
         this.stage.addAction(Actions.sequence(Actions.delay(0.75f), new Action() {
            @Override
            public boolean act(float delta) {
               shotMeter.showMessage("Tap to place your shooter below the line");
               game.setState(State.PLACE_SHOOTER);
               return true;
            }
         }));
      } else {
         this.shotMeter.setShotListener( findCurrentShooter() );
         attachArrowToShooter();
         this.game.setState(State.PLAYING);
      }
   }

   @Override
   public void restart() {
      this.stage.addAction( Actions.sequence(Actions.fadeOut(0.5f), Actions.delay(0.25f),new Action() {

         @Override
         public boolean act(float delta) {
            flickIt.showGameScreen();
            return true;
         }
         
      }) );
   }
   
   private boolean isPopupVisible() {
      for (Actor a : this.stage.getActors()) {
         if (a.getName() == null) {
            continue;
         }
         if (a.getName().equals("popup")) {
           return true;
         }
      }
      return false;
   }
   
   @Override
   public void unpause() {
      SoundManager.instance().resume();
      for (Actor a : this.stage.getActors()) {
         if (a.getName() == null) {
            continue;
         }
         if (a.getName().equals("popup")) {
            ((Popup) a).fadeOut();
            break;
         }
      }
   }

   @Override
   public void quit() {
      SoundManager.instance().stopMusic();
      SoundManager.instance().playSound(SoundManager.MENU_CLICK);
      removePopup();
      this.stage.addAction( sequence(Actions.delay(0.5f), Actions.fadeOut(0.5f), new Action() {
         @Override
         public boolean act(float delta) {
            SoundManager.instance().playSound(SoundManager.DROP);
            flickIt.showMenu();
            return true;
         } 
      }));
   }
   
   @Override
   public void timerWarning() {
      this.overlay.addAction( Actions.repeat(10, FlickItActions.pulse(1.0f)));
      SoundManager.instance().playSound(SoundManager.TICKING);
   }
   
   @Override
   public void timeUp() {
      SoundManager.instance().stopMusic();
      SoundManager.instance().playSound(SoundManager.FOUL);
      Puck s = findCurrentShooter();
      if (s != null ) {
         s.stopPulse();
      }
   }
   
   @Override
   protected boolean backClicked() {
      if ( this.game.getState() == State.INIT ) {
         this.game.startGame();
         return true;
      } else if ( this.game.getState() == State.TURN_START ) {
         this.game.startTurn();
         return true;
      } else if ( this.game.isPlaying() ) {
         this.game.pauseGame();
         this.shotMeter.disable();
         Popup p = new Popup(this.game);
         p.initPause();
         this.stage.addActor(p);
         SoundManager.instance().playSound(SoundManager.PAUSE);

         return true;
      } else if (this.game.getState().equals(State.PAUSED)) {
         this.game.resumeGame();
         return true;
      }
      return false;
   }
}
