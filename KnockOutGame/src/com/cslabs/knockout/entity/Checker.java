package com.cslabs.knockout.entity;

import java.util.Iterator;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.IModifier;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.cslabs.knockout.GameActivity;
import com.cslabs.knockout.ResourceManager;
import com.cslabs.knockout.SceneManager;
import com.cslabs.knockout.AI.Shot;
import com.cslabs.knockout.factory.CheckerFactory;
import com.cslabs.knockout.scene.GameScene;

public class Checker extends TiledSprite implements CollidableEntity {

	private static final String TAG = "Checker";

	private final Type TYPE = Type.CHECKER;

	// global variables
	public GameActivity activity;
	public Engine engine;
	public Camera camera;
	public VertexBufferObjectManager vbom;
	private PhysicsWorld physicsWorld;

	// constants
	private static final float RADIUS = 100f;
	private static final int MAX_POWER = 40;

	// variables
	private CheckerState state = CheckerState.ALIVE;
	private PlayerNo player;
	private int ID = 0;
	private Body body;

	public Sprite center;

	public Checker(float pX, float pY, ITiledTextureRegion pTiledTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager, int ID) {
		super(pX, pY, pTiledTextureRegion, pVertexBufferObjectManager);
		vbom = ResourceManager.getInstance().vbom;
		engine = ResourceManager.getInstance().engine;
		camera = ResourceManager.getInstance().camera;
		activity = ResourceManager.getInstance().activity;

		// Assign ID and player No
		this.setID(ID);
		this.setPlayer();

		// get instance of PhysicsWorld
		physicsWorld = CheckerFactory.getInstance().getPhysicsWorld();

		// Dump info into log cat
		//dumpInfo();

	}

	public void die() {
	

		// death animation
		// set fixture as sensor
		state = CheckerState.DEAD;
		
		if (player == PlayerNo.P1) {
			GameScene.getInstance().updateP1Text();
		} else if (player == PlayerNo.P2) {
			GameScene.getInstance().updateP2Text();
		}
		
		ScaleModifier scaleModifier = new ScaleModifier(2.0f, 1, 0);
		IEntityModifierListener entityModifierListener = new IEntityModifierListener() {

			@Override
			public void onModifierStarted(IModifier<IEntity> pModifier,
					IEntity pItem) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onModifierFinished(IModifier<IEntity> pModifier,
					final IEntity pItem) {
				final PhysicsWorld physicsWorld = GameScene.getInstance().physicsWorld;
				activity.runOnUpdateThread(new Runnable() {
					@Override
					public void run() {
						removePlayingPiece();
					}
				});
			}
		};

		scaleModifier.addModifierListener(entityModifierListener);
		this.registerEntityModifier(scaleModifier);

	}

	public void dumpInfo() {
		// dumps info about the current for debugging
		Debug.i(TAG, "P: " + player + " ID: " + getID() + " dead?  " + state
				+ " X: " + this.getX() + " Y: " + this.getY());
		;
	}

	public void flick(Shot shot) {

		Vector2 velocity = shot.getVelocity();
		this.body.setLinearVelocity(velocity);

	}

	// Helper methods

	public void removePlayingPiece() {
		final PhysicsConnector myPhysicsConnector = physicsWorld
				.getPhysicsConnectorManager().findPhysicsConnectorByShape(this);

		physicsWorld.unregisterPhysicsConnector(myPhysicsConnector);
		physicsWorld.destroyBody(myPhysicsConnector.getBody());

		SceneManager.getInstance().getCurrentScene().detachChild(this);
		SceneManager.getInstance().getCurrentScene().unregisterTouchArea(this);
		
		GameScene.getInstance().getPieces().remove(this);
		Debug.i("LOG", GameScene.getInstance().getPieces().size() + "");

		System.gc();
	}
	
//	public void dumpInfo() {
//		// dumps info about the current for debugging
//		Debug.i(TAG, "P: " + player + " ID: " + getID() + " dead?  " + state
//				+ " X: " + this.getX() + " Y: " + this.getY());
//		;
//	}

	// Getters and Setters

	private void setPlayer() {
		if (getID() > 100 && getID() < 200)
			player = PlayerNo.P1;
		else if (getID() > 200 && getID() < 300)
			player = PlayerNo.P2;
		else
			Debug.e(TAG, "WARNING! setPlayer returns null!");

	}

	public PlayerNo getPlayer() {
		return player;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	@Override
	public void setBody(Body body) {
		this.body = body;

	}

	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public Type getType() {
		return TYPE;
	}

	public CheckerState getState() {
		return state;
	}

	public void setState(CheckerState state) {
		this.state = state;
	}

}