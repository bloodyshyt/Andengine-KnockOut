package com.cslabs.knockout.scene;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.TextMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ColorMenuItemDecorator;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.math.MathUtils;

import android.opengl.GLES20;
import android.view.KeyEvent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.cslabs.knockout.GameActivity;
import com.cslabs.knockout.AI.AIBotWrapper;
import com.cslabs.knockout.AI.Shot;
import com.cslabs.knockout.AI.TestPhysicsWorld;
import com.cslabs.knockout.GameLevels.AIBots.AIBotTypes;
import com.cslabs.knockout.GameLevels.Levels;
import com.cslabs.knockout.GameLevels.Levels.CheckerDef;
import com.cslabs.knockout.GameLevels.Levels.LevelDef;
import com.cslabs.knockout.GameLevels.Levels.PlatformDef;
import com.cslabs.knockout.Managers.ResourceManager;
import com.cslabs.knockout.Managers.SceneManager;
import com.cslabs.knockout.entity.Checker;
import com.cslabs.knockout.entity.CheckerState;
import com.cslabs.knockout.entity.Platform;
import com.cslabs.knockout.entity.Player;
import com.cslabs.knockout.entity.PlayerNo;
import com.cslabs.knockout.entity.PlayerTurnCycle;
import com.cslabs.knockout.entity.Utils;
import com.cslabs.knockout.factory.CheckerFactory;
import com.cslabs.knockout.factory.PlatformFactory;
import com.cslabs.knockout.utils.Stopwatch;

public class GameScene extends AbstractScene implements IOnAreaTouchListener,
		IOnSceneTouchListener, IScrollDetectorListener,
		IPinchZoomDetectorListener, IOnMenuItemClickListener {

	// single instance is created only once
	private static final GameScene INSTANCE = new GameScene();
	private static final String TAG = "GameScene";

	private static Thread AIBotThread;
	private static Runnable AIBot;

	private static boolean firstTurn = true;

	// ====================================================
	// CONSTANTS
	// ====================================================
	private static final int CAMERA_HEIGHT = 800;
	private static final int CAMERA_WIDTH = 480;

	// ====================================================
	// VARIABLES
	// ====================================================
	public FixedStepPhysicsWorld physicsWorld;

	private static PlayerTurnCycle playerTurnCycle;
	private Player currentPlayer;
	public volatile ArrayList<Checker> gameCheckers = new ArrayList<Checker>();

	/*
	 * public static AIBotWrapper[] botWrappers = { new AIBotWrapper(0, 4,
	 * AIBotTypes.GREEDYBOT), new AIBotWrapper(2 , 5, AIBotTypes.MINIMAX), null,
	 * null };
	 */
	public static AIBotWrapper[] botWrappers;
	/*
	 * public static AIBotWrapper[] botWrappers = { new AIBotWrapper(0, 5,
	 * AIBotTypes.GREEDYBOT), new AIBotWrapper(0, 5, AIBotTypes.GREEDYBOT), new
	 * AIBotWrapper(0, 5, AIBotTypes.GREEDYBOT), null };
	 */
	Iterator<Body> bodies;
	
	private static LevelDef cLevelDef;

	// Variables for platform
	public static Platform platform;

	// Visual aid when placing shoot
	float distance = 0, degree = 0;
	public static Sprite tempPiece1;
	public static Sprite tempPiece2;
	public static Sprite tempPiece;
	public static Line line;
	public static Sprite arrow;
	public static Rectangle center;

	// Variables for zoom/smooth camera
	private static final float MIN_ZOOM_FACTOR = 0.8f;
	private static final float MAX_ZOOM_FACTOR = 1.2f;
	private SurfaceScrollDetector mScrollDetector;
	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	private SmoothCamera mSmoothCamera;

	private volatile boolean botThreadRunning = false;
	//private MenuScene mMenuScene;
	private static volatile int[] playerLives = new int[4];

	// game over pop up menu
	protected static final int MENU_TEXT = 0;
	protected static final int MENU_RESET = MENU_TEXT + 1;
	protected static final int MENU_QUIT = MENU_RESET + 1;

	private static WorldState currentState = WorldState.PLAYER_TURN;

	public GameScene() {
		super();
		physicsWorld = new FixedStepPhysicsWorld(60, 1, new Vector2(0, 0),
				false, 6, 2);
		CheckerFactory.getInstance().create(physicsWorld, vbom);
		PlatformFactory.getInstance().create(physicsWorld, vbom);
		mSmoothCamera = (SmoothCamera) ResourceManager.getInstance().camera;

	}
	
	public GameScene(LevelDef pLevelDef, AIBotWrapper[] pBots) {
		super();
		cLevelDef = pLevelDef;
		botWrappers = pBots;
		physicsWorld = new FixedStepPhysicsWorld(60, 1, new Vector2(0, 0),
				false, 6, 2);
		CheckerFactory.getInstance().create(physicsWorld, vbom);
		PlatformFactory.getInstance().create(physicsWorld, vbom);
		mSmoothCamera = (SmoothCamera) ResourceManager.getInstance().camera;

	}

	// ====================================================
	// ABSTRACT SCENE OVERRIDEN METHODS
	// ====================================================
	@Override
	public void populate() {
		// center the camera
		camera.setCenter(GameActivity.CAMERA_WIDTH/2, GameActivity.CAMERA_HEIGHT/2);

		// set the background as white
		setBackground(new Background(Color.WHITE));

		loadLevel(cLevelDef);
		

		registerUpdateHandler(physicsWorld);

		// update handler to check if all the pieces are stationary
		registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUpdate(float pSecondsElapsed) {

				if (firstTurn) {
					playTurn(currentPlayer);
					firstTurn = false;
				}

				if (currentState == WorldState.STATE_FIRING) {
					if (!isWorldMoving()) {
						// check if any players lost all of their checkers
						// check if only one player remains or all players are
						// dead

						Debug.i(TAG, "Checking if the game is over");
						playerWon();

						currentState = WorldState.PLAYER_TURN;
						currentPlayer = playerTurnCycle.nextTurn();
						playTurn(currentPlayer);

					}
				} else {

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

	}

	// ====================================================
	// LEVEL LOADING
	// ====================================================
	//private void loadLevel(final int pWorldIndex, final int pLevelIndex) {
	private void loadLevel(LevelDef pLevelDef) {
		int p1index, p2index, p3index, p4index;

		p1index = 101;
		p2index = 201;
		p3index = 301;
		p4index = 401;

		// get LevelDef object
		LevelDef currentLevel = pLevelDef;

		// load player checkers
		if (currentLevel.mP1Checkers != null) {
			LinkedList<Checker> P1Checkers = new LinkedList<Checker>();
			for (CheckerDef cDef : currentLevel.mP1Checkers) {
				Checker c = addChecker(cDef.mX, cDef.mY, PlayerNo.P1, p1index++);
				P1Checkers.add(c);
			}
			Debug.i(TAG, "P1Checkers has size " + P1Checkers.size());
			playerTurnCycle = new PlayerTurnCycle(new Player(P1Checkers,
					PlayerNo.P1, botWrappers[0]));
		}

		if (currentLevel.mP2Checkers != null) {
			LinkedList<Checker> P2Checkers = new LinkedList<Checker>();
			for (CheckerDef cDef : currentLevel.mP2Checkers) {
				Checker c = addChecker(cDef.mX, cDef.mY, PlayerNo.P2, p2index++);
				P2Checkers.add(c);
			}

			playerTurnCycle.addPlayer(new Player(P2Checkers, PlayerNo.P2,
					botWrappers[1]));
		}

		if (currentLevel.mP3Checkers != null) {
			LinkedList<Checker> P3Checkers = new LinkedList<Checker>();
			for (CheckerDef cDef : currentLevel.mP3Checkers) {
				Checker c = addChecker(cDef.mX, cDef.mY, PlayerNo.P3, p3index++);
				P3Checkers.add(c);
			}

			playerTurnCycle.addPlayer(new Player(P3Checkers, PlayerNo.P3,
					botWrappers[2]));
		}

		if (currentLevel.mP4Checkers != null) {
			LinkedList<Checker> P4Checkers = new LinkedList<Checker>();
			for (CheckerDef cDef : currentLevel.mP4Checkers) {
				Checker c = addChecker(cDef.mX, cDef.mY, PlayerNo.P4, p4index++);
				P4Checkers.add(c);
			}

			playerTurnCycle.addPlayer(new Player(P4Checkers, PlayerNo.P4,
					botWrappers[3]));
		}

		playerTurnCycle.closeCycle();
		currentPlayer = playerTurnCycle.getHead();

		// load platform
		PlatformDef currPlatform = currentLevel.mPlatformDef;
		platform = addPlatform(currPlatform.coords);
	}

	private Checker addChecker(float x, float y, PlayerNo playerNo, int ID) {
		Checker piece = CheckerFactory.getInstance().createPlayingPiece(x, y,
				playerNo, ID);
		playerLives[playerNo.getValue() - 1]++;
		registerTouchArea(piece);
		attachChild(piece);
		gameCheckers.add(piece);
		return piece;
	}

	private Platform addPlatform(float[] platform_cords2) {
		Platform platform = PlatformFactory.getInstance().createPlatform(
				platform_cords2);
		attachChild(platform);
		physicsWorld.setContactListener(new MyContactListener(gameCheckers,
				platform));
		return platform;
	}

	// ====================================================
	// ZOOM/SMOOTH CAMERA LOGIC
	// ====================================================

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
		// Debug.i(TAG,
		// "X:" + pSceneTouchEvent.getX() + " Y:"
		// + pSceneTouchEvent.getY() + " zoom:"
		// + mSmoothCamera.getZoomFactor());
		// Debug.i(TAG, "Camera center: X:" + mSmoothCamera.getCenterX() + " Y:"
		// + mSmoothCamera.getCenterY());
		// Debug.i(TAG, "Camera bounds: Height:" + mSmoothCamera.getHeight()
		// + "Width:" + mSmoothCamera.getWidth());
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

	// ====================================================
	// HUMAN TOUCH INTERACTION
	// ====================================================

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
			ITouchArea pTouchArea, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {

		// check if current player is CPU or checkers are moving
		if (currentPlayer.isCPU || currentState == WorldState.STATE_FIRING)
			return true;

		// check if pTouchArea is checker and belongs to currentPlayer
		if (!(pTouchArea instanceof Checker))
			return true;
		else if (((Checker) pTouchArea).getPlayer() != currentPlayer.playerNo)
			return true;

		Checker c;
		float x1, x2, y1, y2;

		c = (Checker) pTouchArea;

		x1 = c.getX();
		y1 = c.getY();
		x2 = pSceneTouchEvent.getX();
		y2 = pSceneTouchEvent.getY();

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
				final float newZoomFactor = (float) (currentZoomFactor * 0.99);

				if (newZoomFactor < MAX_ZOOM_FACTOR
						&& newZoomFactor > MIN_ZOOM_FACTOR) {
					// Set the new zoom factor
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
			currentState = WorldState.STATE_FIRING;
			// restore the original level of zoom
			this.mSmoothCamera.setZoomFactor(mPinchZoomStartedCameraZoomFactor);

		}
		return true;
	}

	// ====================================================
	// Helper Methods
	// ====================================================

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
			if(body.getUserData() instanceof Checker) {
				Checker c = (Checker) body.getUserData();
				if(c.getState() == CheckerState.ALIVE) {
					double velocityMagnitude = Math.sqrt(Math.pow(
							body.getLinearVelocity().x, 2)
							+ Math.pow(body.getLinearVelocity().y, 2));
					if (velocityMagnitude > 0.01)
						movingBodies = true;
				}
			}
		}

		if (movingBodies) {
			return true;
		} else {
			return false;
		}
	}

	private void playTurn(final Player player) {
		Debug.i(TAG, "turn for " + player.playerNo + " isCPU? " + player.isCPU);
		if (player.isCPU) {
			if (AIBotThread == null) {
				// run find best shot in a separate thread
				AIBot = new Runnable() {
					public void run() {
						Debug.i(TAG, "Strating new thread for "
								+ player.playerNo);
						botThreadRunning = true;
						TestPhysicsWorld testWorld = new TestPhysicsWorld()
								.createWorldFromScene(physicsWorld);
						Stopwatch timer = new Stopwatch();
						Shot shot = botWrappers[player.playerNo.getValue() - 1]
								.findBestShot(testWorld, player);
						if (shot != null) {
							shot.dumpInfo();
							playShot(shot);
							stopAIBotThread();
						}
						Debug.i(TAG,
								"Found the best shot in " + timer.elapsedTime()
										+ " seconds");

						botThreadRunning = false;
					}
				};
				AIBotThread = new Thread(AIBot);
				AIBotThread.start();
			}
		}
	}

	private void stopAIBotThread() {
		// use to stop the thread myThread
		if (AIBotThread != null) {
			Thread dummy = AIBotThread;
			AIBotThread = null;
			dummy.interrupt();
		}
	}

	private void playShot(Shot shot) {

		int ID = shot.getFirerID();
		Iterator<Body> bodies = physicsWorld.getBodies();
		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof Checker) {
				Checker vc = (Checker) body.getUserData();
				if (vc.getID() == ID) {
					vc.flick(shot);
					Debug.i(TAG, "Flicking " + vc.getID());
				}
			}
		}
		currentState = WorldState.STATE_FIRING;
	}

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

		tempPiece = (currentPlayer.playerNo == PlayerNo.P1) ? tempPiece1
				: tempPiece2;

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

	private boolean playerWon() {

		for (int j = 0; j < playerLives.length; j++) {
			playerLives[j] = 0;
		}
		for (Checker c : gameCheckers) {
			if (c.getState() == CheckerState.ALIVE) {
				playerLives[c.getPlayer().getValue() - 1]++;
			}
		}
		Debug.i(TAG, playerLives[0] + " " + playerLives[1] + " "
				+ playerLives[2] + " " + playerLives[3]);
		int nPlayers = 0;
		int playerIndex = 0;
		for (int i = 0; i < playerLives.length; i++) {
			if (playerLives[i] > 0) {
				nPlayers++;
				playerIndex = i + 1;
			}
		}

		if (nPlayers == 1) {
			Debug.i(TAG, "Current no of players " + nPlayers);
			this.setChildScene(createMenuScene("Player " + playerIndex
					+ " wins!"), false, true, true);
			return true;
		} else {
			Debug.i(TAG, "Current no of players " + nPlayers);
			return false;
		}
	}

	public void removeChecker(Checker checker) {
		gameCheckers.remove(checker);
		if (playerTurnCycle.removeChecker(checker)) {
			Debug.i(TAG, "Checker successfully removed from playerTurnCycle");
		}
	}

	// ====================================================
	// GAME OVER MENU
	// ====================================================

	@Override
	public boolean onMenuItemClicked(final MenuScene pMenuScene,
			final IMenuItem pMenuItem, final float pMenuItemLocalX,
			final float pMenuItemLocalY) {
		switch (pMenuItem.getID()) {
		case MENU_RESET:
			SceneManager.getInstance().showGameScene();
			return true;
		case MENU_QUIT:
			/* End Activity. */
			SceneManager.getInstance().showMenuScene();
			return true;
		default:
			return false;
		}
	}

	protected MenuScene createMenuScene(String menuText) {
		final MenuScene menuScene = new MenuScene(this.camera);

		final IMenuItem gameOvertext = new ColorMenuItemDecorator(
				new TextMenuItem(MENU_TEXT, res.mFont, menuText, vbom),
				new Color(1, 0, 0), new Color(0, 0, 0));
		gameOvertext.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(gameOvertext);

		final IMenuItem resetMenuItem = new ColorMenuItemDecorator(
				new TextMenuItem(MENU_RESET, res.mFont, "RESET", vbom),
				new Color(1, 0, 0), new Color(0, 0, 0));
		resetMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(resetMenuItem);

		final IMenuItem quitMenuItem = new ColorMenuItemDecorator(
				new TextMenuItem(MENU_QUIT, res.mFont, "QUIT", vbom),
				new Color(1, 0, 0), new Color(0, 0, 0));
		quitMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(quitMenuItem);

		menuScene.buildAnimations();

		menuScene.setBackgroundEnabled(false);

		menuScene.setOnMenuItemClickListener(this);
		return menuScene;
	}

	// ====================================================
	// INNER CLASSES
	// ====================================================

	public enum WorldState {
		STATE_FIRING, PLAYER_TURN;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub

	}

	public static GameScene getInstance() {
		// TODO Auto-generated method stub
		return INSTANCE;
	}
}
