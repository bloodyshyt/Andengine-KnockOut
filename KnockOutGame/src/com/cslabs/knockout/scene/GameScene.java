package com.cslabs.knockout.scene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.background.modifier.ColorBackgroundModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.batch.SpriteGroup;
import org.andengine.entity.text.Text;
import org.andengine.extension.debugdraw.DebugRenderer;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.util.SAXUtils;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.level.EntityLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
import org.andengine.util.level.simple.SimpleLevelLoader;
import org.andengine.util.math.MathConstants;
import org.andengine.util.math.MathUtils;
import org.xml.sax.Attributes;

import android.content.Entity;
import android.graphics.Typeface;
import android.util.FloatMath;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.cslabs.knockout.Managers.ResourceManager;
import com.cslabs.knockout.Managers.SceneManager;
import com.cslabs.knockout.AI.AbstractAIBot;
import com.cslabs.knockout.AI.GameState;
import com.cslabs.knockout.AI.GreedyBot;
import com.cslabs.knockout.AI.Shot;
import com.cslabs.knockout.GameLevels.LevelLoader;
import com.cslabs.knockout.entity.Platform;
import com.cslabs.knockout.entity.Checker;
import com.cslabs.knockout.entity.PlayerNo;
import com.cslabs.knockout.entity.Utils;
import com.cslabs.knockout.entity.VirtualChecker;
import com.cslabs.knockout.factory.PlatformFactory;
import com.cslabs.knockout.factory.CheckerFactory;
import com.cslabs.knockout.utils.Stopwatch;

public class GameScene extends AbstractScene implements IOnAreaTouchListener,
		IOnSceneTouchListener, IScrollDetectorListener,
		IPinchZoomDetectorListener {

	// single instance is created only once
	private static final GameScene INSTANCE = new GameScene();

	public static GameScene getInstance() {
		return INSTANCE;
	}

	private static final String TAG = "GameScene";

	public FixedStepPhysicsWorld physicsWorld;

	// Scene constants
	private static final int CAMERA_HEIGHT = 800;
	private static final int CAMERA_WIDTH = 480;

	// XML related constants
	private static final String TAG_ENTITY = "entity";
	private static final String TAG_LEVEL_VERTICES = "vertices";

	private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
	private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
	private static final String TAG_ENTITY_ATTRIBUTE_Z = "z";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";

	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER1 = "player1";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER2 = "player2";
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM = "platform";

	// Variables for platform
	private static Platform platform;
	private int vertices;
	private ArrayList<Float> float_platform_coords = new ArrayList<Float>();
	private static float[] platform_cords;
	private int vertice_counter = 0;

	// Variables for Checkers and corresponding dots
	public static LinkedList<Checker> pieces = new LinkedList<Checker>();
	private static LinkedList<Rectangle> dots = new LinkedList<Rectangle>();

	private int P1_counter = 101;
	private int P2_counter = 201;

	private static int P1_lives = 0;
	private static int P2_lives = 0;

	// Variables for visual aids when placing shot
	float distance = 0, degree = 0;
	public static Sprite tempPiece1;
	public static Sprite tempPiece2;
	public static Sprite tempPiece;
	public static Line line;
	public static Sprite arrow;
	public static Rectangle center;

	Iterator<Body> bodies;

	// Variables for zoom/smooth camera
	private static final float MIN_ZOOM_FACTOR = 0.5f;
	private static final float MAX_ZOOM_FACTOR = 1.5f;
	private SurfaceScrollDetector mScrollDetector;
	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	private SmoothCamera mSmoothCamera;

	static Text turnText, p1, p2;

	private GreedyBot greedyBot;
	private volatile boolean botThreadRunning = false;

	public enum WorldState {
		P1(PlayerNo.P1), P2(PlayerNo.P2), FIRING(PlayerNo.NONE), GAME_OVER(
				PlayerNo.NONE);

		private final PlayerNo player;

		WorldState(PlayerNo player) {
			this.player = player;
		}

		public PlayerNo turn() {
			return player;
		}
	}

	public static WorldState currentState = WorldState.P1;
	public static WorldState nextState = WorldState.P2;
	private boolean gameOver = false;

	public GameScene() {
		super();
		physicsWorld = new FixedStepPhysicsWorld(60, 1, new Vector2(0, 0),
				false, 6, 2);
		CheckerFactory.getInstance().create(physicsWorld, vbom);
		PlatformFactory.getInstance().create(physicsWorld, vbom);
		mSmoothCamera = (SmoothCamera) ResourceManager.getInstance().camera;

	}

	@Override
	public void populate() {

		// prepare the BOX2D debug drawer
		DebugRenderer dr = new DebugRenderer(physicsWorld, vbom);
		dr.setZIndex(999);
		attachChild(dr);

		// set the background as white
		setBackground(new Background(Color.WHITE));

		loadLevel(1);
		
		//LevelLoader.getInstance().loadLevel(1, physicsWorld, vbom);

		registerUpdateHandler(physicsWorld);

		// create the text to show whose turn it is
		turnText = new Text(200, 50, ResourceManager.getInstance().mFont,
				"It is Player 1's turn", vbom);
		attachChild(turnText);

		// p1 = new Text(400, 750, ResourceManager.getInstance().mFont, "P1: "
		// + P1_lives, vbom);
		// attachChild(p1);
		//
		// p2 = new Text(400, 50, ResourceManager.getInstance().mFont, "P2: "
		// + P2_lives, vbom);
		// attachChild(p2);

		// update handler to check if all the pieces are stationary
		registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				if (currentState == WorldState.FIRING) {
					turnText.setText("Please wait");
					if (!isWorldMoving()) {
						currentState = nextState;
						updateText();

						// check if the game is over
						if (P1_lives == 0 || P2_lives == 0) {
							gameOver = true;
						}
					}
					turnText.setText("Please wait");
				} else {

					if (!botThreadRunning && pieces.size() > 0) {
						// new Thread(AIBot).start();
					}
				}
			}
		});

		setTouchAreaBindingOnActionDownEnabled(true);
		createVisualAids();
		setOnAreaTouchListener(this);

		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);

		this.setOnSceneTouchListener(this);
		this.setTouchAreaBindingOnActionDownEnabled(true);

		// test the AI
		// greedyBot = GreedyBot.getInstance();
		// GameState currentState = new GameState();
		// currentState = currentState.createGameState(this);
		// Shot shot = greedyBot.findBestShot(currentState, 1);
		// playShot(shot);

		// test the evaluate function
		// float score = greedyBot.evaluateShot(currentState, new Shot(101, new
		// Vector2(0, -40)), PlayerNo.P1);
		// Debug.i(TAG, "testing shot has a score of " + score);

		// playShot(new Shot(101, new Vector2(0, -40)));

	}

	@Override
	public void onScrollStarted(final ScrollDetector pScollDetector,
			final int pPointerID, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mSmoothCamera.getZoomFactor();
		this.mSmoothCamera.offsetCenter(-pDistanceX / zoomFactor, pDistanceY
				/ zoomFactor);
	}

	@Override
	public void onScroll(final ScrollDetector pScollDetector,
			final int pPointerID, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mSmoothCamera.getZoomFactor();
		this.mSmoothCamera.offsetCenter(-pDistanceX / zoomFactor, pDistanceY
				/ zoomFactor);
	}

	@Override
	public void onScrollFinished(final ScrollDetector pScollDetector,
			final int pPointerID, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mSmoothCamera.getZoomFactor();
		this.mSmoothCamera.offsetCenter(-pDistanceX / zoomFactor, pDistanceY
				/ zoomFactor);
	}

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector,
			final TouchEvent pTouchEvent) {
		this.mPinchZoomStartedCameraZoomFactor = this.mSmoothCamera
				.getZoomFactor();
	}

	@Override
	public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector,
			final TouchEvent pTouchEvent, final float pZoomFactor) {
		// If the camera is within zooming bounds
		final float newZoomFactor = mPinchZoomStartedCameraZoomFactor
				* pZoomFactor;
		if (newZoomFactor < MAX_ZOOM_FACTOR && newZoomFactor > MIN_ZOOM_FACTOR) {
			this.mSmoothCamera
					.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor
							* pZoomFactor);
		}

	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector,
			final TouchEvent pTouchEvent, final float pZoomFactor) {
		// Set the zoom factor one last time upon ending the pinch-to-zoom
		// functionality
		final float newZoomFactor = mPinchZoomStartedCameraZoomFactor
				* pZoomFactor;

		// If the camera is within zooming bounds
		if (newZoomFactor < MAX_ZOOM_FACTOR && newZoomFactor > MIN_ZOOM_FACTOR) {
			// Set the new zoom factor
			this.mSmoothCamera.setZoomFactor(newZoomFactor);
		}
	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene,
			final TouchEvent pSceneTouchEvent) {
		Debug.i(TAG,
				"X:" + pSceneTouchEvent.getX() + " Y:"
						+ pSceneTouchEvent.getY() + " zoom:"
						+ mSmoothCamera.getZoomFactor());
		Debug.i(TAG, "Camera center: X:" + mSmoothCamera.getCenterX() + " Y:"
				+ mSmoothCamera.getCenterY());
		Debug.i(TAG, "Camera bounds: Height:" + mSmoothCamera.getHeight()
				+ "Width:" + mSmoothCamera.getWidth());
		this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

		if (this.mPinchZoomDetector.isZooming()) {
			this.mScrollDetector.setEnabled(false);
		} else {
			if (pSceneTouchEvent.isActionDown()) {
				this.mScrollDetector.setEnabled(true);
			}
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}

		return true;
	}

	private Runnable AIBot = new Runnable() {
		public void run() {
			botThreadRunning = true;
			greedyBot = GreedyBot.getInstance();
			GameState currentState = generateGameState();
			Stopwatch timer = new Stopwatch();
			Shot shot = greedyBot.findBestShot(currentState,
					GameScene.currentState.turn());
			if (shot != null) {
				shot.dumpInfo();
				playShot(shot);
			}
			Debug.i(TAG, "Found the best shot in " + timer.elapsedTime()
					+ " seconds");

			botThreadRunning = false;
		}
	};

	private GameState generateGameState() {
		GameState state = new GameState();
		return state.createGameStateFromScene(this, currentState.turn());
	}

	private void playShot(Shot shot) {
		boolean fired = false;

		Debug.i(TAG, "Calling for flick at " + shot.getFirerID());
		int ID = shot.getFirerID();
		Vector2 v = shot.getVelocity();
		Iterator<Body> bodies = physicsWorld.getBodies();
		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof Checker) {
				Checker vc = (Checker) body.getUserData();
				if (vc.getID() == ID) {
					vc.flick(shot);
					Debug.i(TAG, "Flicking " + vc.getID());
					fired = true;
				}
			}
		}
		updateState();
	}

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
			ITouchArea pTouchArea, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {

		Checker c;
		float x1, x2, y1, y2, dY, dX;

		// Check if touched is Checker
		if (!(pTouchArea instanceof Checker)) {
			// Debug.i(TAG, "you didnt touch a checker");
			return true;
		} else {

			c = (Checker) pTouchArea;

			x1 = c.getX();
			y1 = c.getY();
			x2 = pSceneTouchEvent.getX();
			y2 = pSceneTouchEvent.getY();
			dX = x2 - x1;
			dY = y2 - y1;

			if (c.getPlayer() == PlayerNo.P1)
				tempPiece = tempPiece1;
			else if (c.getPlayer() == PlayerNo.P2)
				tempPiece = tempPiece2;
		}

		// Debug.i(TAG, "Checker of " + c.getPlayer()
		// + " touched in current state of " + currentState);

		// check if player touched his own checker
		if (c.getPlayer() != currentState.turn())
			return true;

		if (pSceneTouchEvent.isActionDown()) {
			// Debug.i(TAG, "Checker touched");
			mPinchZoomStartedCameraZoomFactor = this.mSmoothCamera
					.getZoomFactor();
			c.setVisible(false);
		}

		if (pSceneTouchEvent.isActionMove()) {

			updateVisualAids(x1, y1, x2, y2);

			distance = Utils.calculateDistance(x1, y1, x2, y2);
			float pullBackAngle = MathUtils.atan2(y2 - y1, x2 - x1);
			degree = (float) (pullBackAngle + Math.PI);
			// Debug.i(TAG, "pullBackAngle is " + pullBackAngle
			// + " firing degree is " + degree);
			if (Utils.isFingerAtEdgeofScreen(pSceneTouchEvent, mSmoothCamera)) {
				// slowly zoom out
				float currentZoomFactor = this.mSmoothCamera.getZoomFactor();
				final float newZoomFactor = (float) (currentZoomFactor * 0.95);
				if (newZoomFactor > 0.8) {
					this.mSmoothCamera.setZoomFactor(newZoomFactor);
				}
			}
		}

		if (pSceneTouchEvent.isActionUp()) {

			// Shot shot = new Shot(c.getID(), distance, degree);
			// shot.dumpInfo();
			double power = Utils.MAX_POWER * distance / Utils.FIRING_RADIUS;
			Vector2 v = new Vector2((float) (Math.cos(degree) * power),
					(float) (Math.sin(degree) * power));

			c.getBody().setLinearVelocity(v);
			clearVisualAids();
			c.setVisible(true);
			// c.flick(shot);
			updateState();

			// restore the original level of zoom
			this.mSmoothCamera.setZoomFactor(mPinchZoomStartedCameraZoomFactor);

		}
		return true;
	}

	private void updateState() {
		if (currentState == WorldState.P1) {
			nextState = WorldState.P2;
		} else if (currentState == WorldState.P2) {
			nextState = WorldState.P1;
		}
		currentState = WorldState.FIRING;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub

	}

	// Methods
	public void createVisualAids() {
		tempPiece1 = new Sprite(0, 0,
				ResourceManager.getInstance().player1TextureRegion, vbom);
		tempPiece1.setVisible(false);
		tempPiece2 = new Sprite(0, 0,
				ResourceManager.getInstance().player2TextureRegion, vbom);
		arrow = new Sprite(0, 0,
				ResourceManager.getInstance().arrowTextureRegion, vbom);
		line = new Line(0, 0, 0, 0, vbom);
		center = new Rectangle(0, 0, 5, 5, vbom);

		line.setColor(Color.BLACK);
		center.setColor(Color.BLACK);

		tempPiece1.setVisible(false);
		tempPiece2.setVisible(false);
		arrow.setVisible(false);
		line.setVisible(false);
		center.setVisible(true);

		attachChild(arrow);
		attachChild(line);
		attachChild(center);
		attachChild(tempPiece1);
		attachChild(tempPiece2);
	}

	public void updateVisualAids(float x1, float y1, float x2, float y2) {

		float distance = Utils.calculateDistance(x1, y1, x2, y2);
		float degree = MathUtils.atan2(y2 - y1, x2 - x1);

		float Ax = (float) (x1 + distance * Math.cos(degree + Math.PI));
		float Ay = (float) (y1 + distance * Math.sin(degree + Math.PI));

		float Tx = (float) (x1 + distance * Math.cos(degree));
		float Ty = (float) (y1 + distance * Math.sin(degree));

		center.setPosition(x1, y1);
		line.setPosition(x1, y1, Tx, Ty);
		tempPiece.setPosition(Tx, Ty);
		arrow.setPosition(Ax, Ay);
		arrow.setRotation(MathUtils
				.radToDeg((float) (degree + 1 * Math.PI / 4)));

		center.setVisible(true);
		tempPiece.setVisible(true);
		line.setVisible(true);
		arrow.setVisible(true);
	}

	public void clearVisualAids() {
		tempPiece1.setVisible(false);
		tempPiece2.setVisible(false);
		arrow.setVisible(false);
		line.setVisible(false);
		center.setVisible(false);
	}

	// Helper method
	private void addChecker(float x, float y, PlayerNo playerNo, int ID) {
		Checker piece = CheckerFactory.getInstance().createPlayingPiece(x, y,
				playerNo, ID);
		registerTouchArea(piece);
		attachChild(piece);
		pieces.add(piece);
	}

	private Platform addPlatform(float[] platform_cords2) {
		Platform platform = PlatformFactory.getInstance().createPlatform(
				platform_cords2);
		attachChild(platform);
		return platform;
	}

	/**
	 * Checks if the world is still moving if it is stationary, it returns the
	 * next player's turn as the new state
	 * 
	 * @return
	 */
	private boolean isWorldMoving() {
		// get list of bodies and iterate through them
		boolean movingBodies = false;
		bodies = physicsWorld.getBodies();
		while (bodies.hasNext()) {
			Body body = bodies.next();
			double velocityMagnitude = Math.sqrt(Math.pow(
					body.getLinearVelocity().x, 2)
					+ Math.pow(body.getLinearVelocity().y, 2));
			if (velocityMagnitude > 0.01)
				movingBodies = true;
		}

		if (movingBodies) {
			return true;
		} else {
			return false;
		}
	}

	private void loadLevel(int levelID) {
		final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);

		levelLoader
				.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(
						LevelConstants.TAG_LEVEL) {
					public IEntity onLoadEntity(
							final String pEntityName,
							final IEntity pParent,
							final Attributes pAttributes,
							final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData)
							throws IOException {
						final int width = SAXUtils.getIntAttributeOrThrow(
								pAttributes,
								LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
						final int height = SAXUtils.getIntAttributeOrThrow(
								pAttributes,
								LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
						vertices = SAXUtils.getIntAttributeOrThrow(pAttributes,
								TAG_LEVEL_VERTICES);

						// TODO later we will specify camera BOUNDS and create
						// invisible walls
						// on the beginning and on the end of the level.

						return null;
					}
				});

		levelLoader
				.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(
						TAG_ENTITY) {
					public IEntity onLoadEntity(
							final String pEntityName,
							final IEntity pParent,
							final Attributes pAttributes,
							final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData)
							throws IOException {
						final int x = SAXUtils.getIntAttributeOrThrow(
								pAttributes, TAG_ENTITY_ATTRIBUTE_X);
						final int y = SAXUtils.getIntAttributeOrThrow(
								pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
						final String type = SAXUtils.getAttributeOrThrow(
								pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
						if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER1)) {
							addChecker(x, y, PlayerNo.P1, P1_counter++);
							P1_lives++;
						} else if (type
								.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER2)) {
							addChecker(x, y, PlayerNo.P2, P2_counter++);
							P2_lives++;
						} else if (type
								.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM)) {
							final int z = SAXUtils.getIntAttributeOrThrow(
									pAttributes, TAG_ENTITY_ATTRIBUTE_Z);

							float_platform_coords.add((float) x);
							float_platform_coords.add((float) y);
							float_platform_coords.add((float) z);
							vertice_counter++;
							if (vertice_counter == vertices) {
								// all of vertice data has been read in, we can
								// create the platfrom
								// convert arraylist to array
								platform_cords = new float[float_platform_coords
										.size()];
								Iterator<Float> iterator = float_platform_coords
										.iterator();
								for (int i = 0; i < platform_cords.length; i++) {
									platform_cords[i] = iterator.next()
											.floatValue();
								}

								platform = addPlatform(platform_cords);
								physicsWorld
										.setContactListener(new MyContactListener(
												pieces, platform));

							}
						}
						return null;
					}
				});

		levelLoader.loadLevelFromAsset(activity.getAssets(), "levels/"
				+ levelID + ".lvl");
	}
	
	@Override
	public void onBackKeyPressed() {
		SceneManager.getInstance().showMenuScene();
	}

	private void updateText() {
		ResourceManager.getInstance().engine.runOnUpdateThread(new Runnable() {

			@Override
			public void run() {
				if (currentState == WorldState.P1)
					turnText.setText("It is Player 1's turn");
				else if (currentState == WorldState.P2)
					turnText.setText("It is Player 2's turn");
				else if (gameOver)
					turnText.setText("Game over");
			}
		});

	}

	public void updateP1Text() {
		// ResourceManager.getInstance().engine.runOnUpdateThread(new Runnable()
		// {
		//
		// @Override
		// public void run() {
		// P1_lives--;
		// Debug.i("P1 lost one, it has " + P1_lives + " left");
		// p1.setText("P1: " + P1_lives);
		// }
		// });
	}

	public void updateP2Text() {
		// ResourceManager.getInstance().engine.runOnUpdateThread(new Runnable()
		// {
		//
		// @Override
		// public void run() {
		// P2_lives--;
		// GameScene.getInstance().p2.setText("P2: " + P2_lives);
		// }
		// });
	}

	// Getters and Setters

	public static Sprite getTempPiece1() {
		return tempPiece1;
	}

	public static Sprite getTempPiece2() {
		return tempPiece2;
	}

	public static Line getLine() {
		return line;
	}

	public static Sprite getArrow() {
		return arrow;
	}

	public AbstractAIBot getGreedyBot() {
		return greedyBot;
	}

	public static Platform getPlatform() {
		return platform;
	}

	public static float[] getPlatform_cords() {
		return platform_cords;
	}

	public static LinkedList<Checker> getPieces() {
		return pieces;
	}
}
