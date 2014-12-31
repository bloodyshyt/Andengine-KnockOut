package com.cslabs.knockout.factory;

import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.cslabs.knockout.Managers.ResourceManager;
import com.cslabs.knockout.entity.Checker;
import com.cslabs.knockout.entity.PlayerNo;
import com.cslabs.knockout.entity.VirtualChecker;

public class CheckerFactory {

	private static final String TAG = "CheckerFactory";

	private static CheckerFactory INSTANCE = new CheckerFactory();
	private VertexBufferObjectManager vbom;
	private PhysicsWorld physicsWorld;

	public PhysicsWorld getPhysicsWorld() {
		return physicsWorld;
	}

	public static final FixtureDef CHECKER_FIXTURE = PhysicsFactory
			.createFixtureDef(1f, 0.5f, 5f);

	private CheckerFactory() {
	}

	public static CheckerFactory getInstance() {
		return INSTANCE;
	}

	public void create(PhysicsWorld physicsWorld, VertexBufferObjectManager vbom) {
		this.physicsWorld = physicsWorld;
		this.vbom = vbom;

	}

	public Checker createPlayingPiece(float x, float y, PlayerNo playerNo,
			int ID) {
		// TO DO logic if piece is CPU is not added yet
		Checker piece = null;
		ITextureRegion playerTextureRegion = null;
		switch (playerNo) {
		case P1:
			playerTextureRegion = ResourceManager.getInstance().player1TextureRegion;
			break;
		case P2:
			playerTextureRegion = ResourceManager.getInstance().player2TextureRegion;
			break;
		case P3:
			playerTextureRegion = ResourceManager.getInstance().player3TextureRegion;
			break;
		case P4:
			playerTextureRegion = ResourceManager.getInstance().player4TextureRegion;
			break;
		default:
			break;
		}

		piece = new Checker(x, y, playerTextureRegion, vbom, playerNo, ID);

		Body playingPieceBody = PhysicsFactory.createCircleBody(physicsWorld,
				piece, BodyType.DynamicBody, CHECKER_FIXTURE);

		playingPieceBody.setLinearDamping(2f);
		playingPieceBody.setAngularDamping(2f);
		playingPieceBody.setUserData(piece);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(piece,
				playingPieceBody));

		piece.setBody(playingPieceBody);
		return piece;
	}

	/*
	 * public void copyCheckerIntoPhysicsWorld(Checker p, VirtualChecker vc,
	 * FixedStepPhysicsWorld world) {
	 * 
	 * Body body; body = PhysicsFactory.createCircleBody(world, p,
	 * BodyType.DynamicBody, CHECKER_FIXTURE); body.setLinearDamping(2f);
	 * body.setAngularDamping(2f); body.setUserData(vc); vc.setBody(body); }
	 * 
	 * public VirtualChecker cloneCheckerIntoPhyiscsWorld( FixedStepPhysicsWorld
	 * world, Checker p) { Body body = p.getBody(); final FixtureDef pFixture =
	 * PhysicsFactory.createFixtureDef(1f, 0.5f, 5f);
	 * 
	 * final BodyDef circleBodyDef = new BodyDef(); circleBodyDef.type =
	 * BodyType.DynamicBody; circleBodyDef.position.x = body.getPosition().x;
	 * circleBodyDef.position.y = body.getPosition().y; circleBodyDef.angle =
	 * body.getAngle(); circleBodyDef.linearDamping = 2f;
	 * circleBodyDef.angularDamping = 2f;
	 * 
	 * final Body circleBody = world.createBody(circleBodyDef); final
	 * CircleShape circleShape = new CircleShape(); pFixture.shape =
	 * circleShape;
	 * 
	 * //final float radius = 16 /
	 * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT; final float radius =
	 * body.getFixtureList().get(0).getShape().getRadius(); Debug.i(TAG,
	 * "radius of body to be added is " + radius);
	 * circleShape.setRadius(radius);
	 * 
	 * circleBody.createFixture(pFixture); circleShape.dispose();
	 * 
	 * VirtualChecker vc = new VirtualChecker(p); vc = vc.clone();
	 * vc.setBody(circleBody); circleBody.setUserData(vc); return vc; }
	 * 
	 * public VirtualChecker cloneCheckerIntoPhyiscsWorld( FixedStepPhysicsWorld
	 * world, VirtualChecker p) { Body body = p.getBody(); final FixtureDef
	 * pFixture = PhysicsFactory.createFixtureDef(1f, 0.5f, 5f);
	 * 
	 * final BodyDef circleBodyDef = new BodyDef(); circleBodyDef.type =
	 * BodyType.DynamicBody; circleBodyDef.position.x = body.getPosition().x;
	 * circleBodyDef.position.y = body.getPosition().y; circleBodyDef.angle =
	 * body.getAngle(); circleBodyDef.linearDamping = 2f;
	 * circleBodyDef.angularDamping = 2f;
	 * 
	 * final Body circleBody = world.createBody(circleBodyDef); final
	 * CircleShape circleShape = new CircleShape(); pFixture.shape =
	 * circleShape;
	 * 
	 * final float radius = 16 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
	 * circleShape.setRadius(radius);
	 * 
	 * circleBody.createFixture(pFixture); circleShape.dispose();
	 * 
	 * VirtualChecker vc = p.clone(); vc.setBody(circleBody);
	 * circleBody.setUserData(vc); return vc; }
	 */
}
