package com.cslabs.knockout.scene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

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
import com.cslabs.knockout.ResourceManager;
import com.cslabs.knockout.SceneManager;
import com.cslabs.knockout.AI.AbstractAIBot;
import com.cslabs.knockout.AI.GameState;
import com.cslabs.knockout.AI.GreedyBot;
import com.cslabs.knockout.AI.Shot;
import com.cslabs.knockout.entity.Platform;
import com.cslabs.knockout.entity.Checker;
import com.cslabs.knockout.entity.PlayerNo;
import com.cslabs.knockout.entity.Utils;
import com.cslabs.knockout.entity.VirtualChecker;
import com.cslabs.knockout.factory.PlatformFactory;
import com.cslabs.knockout.factory.CheckerFactory;

public class CopyOfGameScene extends AbstractScene implements IOnAreaTouchListener {

	private static final String TAG = "CopyGameScene";

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

	// Variables for Checkers and corresponding
	private static LinkedList<Checker> pieces = new LinkedList<Checker>();
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

	static Text turnText, p1, p2;

	private AbstractAIBot greedyBot;
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

	// single instance is created only once
	private static final CopyOfGameScene INSTANCE = new CopyOfGameScene();

	public static CopyOfGameScene getInstance() {
		return INSTANCE;
	}

	public CopyOfGameScene() {
		super();
		physicsWorld = new FixedStepPhysicsWorld(60, 1, new Vector2(0, 0),
				false, 10, 8);
		CheckerFactory.getInstance().create(physicsWorld, vbom);
		PlatformFactory.getInstance().create(physicsWorld, vbom);

	}

	@Override
	public void populate() {

		// prepare the BOX2D debug drawer
		DebugRenderer dr = new DebugRenderer(physicsWorld, vbom);
		dr.setZIndex(999);
		attachChild(dr);

		// set the background as white
		setBackground(new Background(Color.WHITE));

		loadLevel(2);

		registerUpdateHandler(physicsWorld);
		setTouchAreaBindingOnActionDownEnabled(true);
		
		// generate power tables
		
		for(int j = 0; j <= 50; j++) {
			FixedStepPhysicsWorld copy= new FixedStepPhysicsWorld(60, 1, new Vector2(0, 0),
					false, 10, 8);
			FixtureDef CHECKER_FIXTURE = PhysicsFactory
					.createFixtureDef(1f, 0.5f, 5f);
			Body body;
			body = PhysicsFactory.createCircleBody(copy, pieces.getFirst(), BodyType.DynamicBody,
					CHECKER_FIXTURE);
			body.setLinearDamping(2f);
			body.setAngularDamping(2f);
			World mWorld = copy.getmWorld();
			
			body.setLinearVelocity(0, j);
			
			for (int i = 0; i <= 200; i++) {
				mWorld.step(1.0f / 60.0f, 10, 8);
				mWorld.clearForces();

			}
			Iterator<Body> bodies =  mWorld.getBodies();
			Body mybdy = bodies.next();
			Debug.i(TAG, "For vY of " + j + " end pos is \t" + mybdy.getPosition());
			Debug.i("LOG",j + "  " + mybdy.getPosition().y);
		}


	}



	@Override
	public void onPause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub

	}


	// Helper method
	private void addPlayingPiece(float x, float y, int ID) {
		Checker piece = CheckerFactory.getInstance().createPlayingPiece(x, y,
				ID);
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
							addPlayingPiece(x, y, P1_counter++);
							P1_lives++;
						} else if (type
								.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER2)) {
							addPlayingPiece(x, y, P2_counter++);
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
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
			ITouchArea pTouchArea, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {
		// TODO Auto-generated method stub
		return false;
	}
}
