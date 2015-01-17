package com.cslabs.knockout.AI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.util.debug.Debug;
import org.andengine.util.math.MathUtils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.cslabs.knockout.entity.Checker;
import com.cslabs.knockout.entity.Platform;
import com.cslabs.knockout.entity.Player;
import com.cslabs.knockout.entity.Utils;
import com.cslabs.knockout.entity.VirtualChecker;
import com.cslabs.knockout.entity.VirtualPlatform;
import com.cslabs.knockout.utils.Stopwatch;

public class TestPhysicsWorld extends FixedStepPhysicsWorld {

	private static final String TAG = "TestPhysicsWorld";

	Platform platform;
	ArrayList<VirtualChecker> gameVirtualCheckers = new ArrayList<VirtualChecker>();

	private VirtualGameState defaultState;

	public TestPhysicsWorld(int pStepsPerSecond, int pMaximumStepsPerUpdate,
			Vector2 pGravity, boolean pAllowSleep) {
		super(pStepsPerSecond, pMaximumStepsPerUpdate, pGravity, pAllowSleep);
	}

	public TestPhysicsWorld() {
		super(60, 1, new Vector2(0, 0), false, 6, 2);
	}

	public TestPhysicsWorld createWorldFromScene(PhysicsWorld world) {
		Iterator<Body> bodies = world.getBodies();
		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof Checker) {
				copyCheckerIntoPhysicsWorld(this, body);
			}
			if (body.getUserData() instanceof Platform) {
				copyPlatformIntoPhysicsWorld(this, body);
			}
		}

		return this;
	}

	public VirtualGameState extractState() {

		ArrayList<VirtualChecker> virtualCheckers = new ArrayList<VirtualChecker>();
		VirtualChecker vc;

		Iterator<Body> bodies = this.getBodies();

		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof VirtualChecker) {
				vc = (VirtualChecker) body.getUserData();
				virtualCheckers.add(new VirtualChecker(vc.getID(), vc
						.getPlayer(), body.getPosition().x,
						body.getPosition().y, vc.isAlive()));
			}
		}

		return new VirtualGameState(virtualCheckers);
	}

	public boolean resetWorld() {
		if (defaultState != null) {
			this.setWorldFromVirtualGameState(defaultState);
			return true;
		}

		return false;
	}

	public void setWorldFromVirtualGameState(VirtualGameState pVirtualGameState) {

		Body body;
		for (VirtualChecker vc : pVirtualGameState.gameVirtualCheckers) {
			body = this.getBodyFromID(vc.getID());
			if (vc.isAlive()) {
				body.setTransform(new Vector2(vc.x, vc.y), 0);
				((VirtualChecker) body.getUserData()).setAsAlive();
				for (int i = 0; i < body.getFixtureList().size(); i++)
					body.getFixtureList().get(i).setSensor(false);
			} else {
				// throw body at some obscure location
				body.setTransform(new Vector2(50, 50), 0);
				((VirtualChecker) body.getUserData()).setAsAlive();
				// set body as sensor
				for (int i = 0; i < body.getFixtureList().size(); i++)
					body.getFixtureList().get(i).setSensor(true);
			}
		}
	}

	public ArrayList<Shot> generateShots(VirtualGameState currentState,
			Player currentPlayer) {

		this.setWorldFromVirtualGameState(currentState);
		return generateShots(currentPlayer);
	}

	public ArrayList<Shot> generateShots(Player currentPlayer) {
		// for now we don't use the raycasting, it might save time
		Stopwatch timer = new Stopwatch();
		int counter = 0;

		// construct an arraylist of player and opponent bodies to iterate
		// through and generate shots
		ArrayList<Body> playerBodies = new ArrayList<Body>();
		ArrayList<Body> opponentBodies = new ArrayList<Body>();

		ArrayList<Shot> shots = new ArrayList<Shot>();

		Iterator<Body> bodies = this.getBodies();
		Body body;

		while (bodies.hasNext()) {
			body = bodies.next();
			if (body.getUserData() instanceof VirtualChecker) {
				if (((VirtualChecker) body.getUserData()).getPlayer() == currentPlayer.playerNo) {
					playerBodies.add(body);
				} else
					opponentBodies.add(body);
			}
		}

		for (Body playerCheckerBody : playerBodies) {
			for (Body opponentCheckerBody : opponentBodies) {
				float[] angleRange = calAngleRange(playerCheckerBody,
						opponentCheckerBody);
				float distance = calDistanceBetween2Bodies(playerCheckerBody,
						opponentCheckerBody);

				// float bestScore = Float.MIN_VALUE;
				int step = 4;
				double change = (angleRange[1] - angleRange[0]) / step;
				// for (double degree = angleRange[0]; degree < angleRange[1];
				// degree += MathUtils.degToRad(2)) {
				for (double degree = angleRange[0]; degree < angleRange[1]; degree += change) {
					int lowerPower = (int) Math.min((distance / 0.4828) * 1.3,
							Utils.MAX_POWER);

					for (int power = Utils.MAX_POWER; power > lowerPower; power -= 5) {
						Shot shot = new Shot(
								((VirtualChecker) playerCheckerBody.getUserData())
										.getID(), (int) (power), (float) degree);
						VirtualGameState next = simulate(shot);
						shots.add(shot);
						counter++;
					}
				}
			}
		}
		Debug.i(TAG,
				"Generated " + counter + " shots in " + timer.elapsedTime()
						+ " seconds");
		return shots;
	}

	public Shot generateSafeShot(Player currentPlayer) {
		// for each player checker, generate 20 random shots
		// best safe shot is the one with the maximum
		float maxSumDist = Float.MIN_VALUE;
		Shot safeShot = null;
		
		ArrayList<Body> playerBodies = new ArrayList<Body>();
		ArrayList<Body> opponentBodies = new ArrayList<Body>();
		ArrayList<Shot> shots = new ArrayList<Shot>();
		Iterator<Body> bodies = this.getBodies();
		Body body;

		while (bodies.hasNext()) {
			body = bodies.next();
			if (body.getUserData() instanceof VirtualChecker) {
				if (((VirtualChecker) body.getUserData()).getPlayer() == currentPlayer.playerNo) {
					playerBodies.add(body);
				} else
					opponentBodies.add(body);
			}
		}

		for (Body currBody : playerBodies) {
			for (int i = 0; i < 20; i++) {
				int power = Utils.randInt(0, Utils.MAX_POWER);
				int degree = Utils.randInt(0, 360);
				shots.add(new Shot(((VirtualChecker) currBody.getUserData())
						.getID(), power, MathUtils.degToRad(degree)));
			}
		}
	
		for(Shot shot : shots) {
			VirtualGameState nextState = this.simulate(shot);
			float sumDist = 0;
			ArrayList<VirtualChecker> vcs = nextState.gameVirtualCheckers;
			for (int i = 0; i < vcs.size(); i++) {
				for (int j = 1; j < vcs.size(); j++) {
					if (vcs.get(i).getPlayer() == currentPlayer.playerNo
							&& vcs.get(j).getPlayer() != currentPlayer.playerNo) {
						VirtualChecker a = vcs.get(i);
						VirtualChecker b = vcs.get(j);
						
						sumDist += Utils.calculateDistance(a.x, a.y, b.x, b.y);
						if(sumDist > maxSumDist) {
							maxSumDist = sumDist;
							safeShot = shot;
						}
					}
				}
			}
			
		}
		return safeShot;
	}

	/**
	 * @param shot
	 * @return VirtualGameState of the resultant world, the testPhysicsWorld is
	 *         set back to the state prior to simulation
	 */
	public VirtualGameState simulate(Shot shot) {

		VirtualGameState currentState = this.extractState();

		Body body = this.getBodyFromID(shot.getFirerID());
		body.setLinearVelocity(shot.getVelocity());

		// step the world forward
		World mWorld = this.getmWorld();
		for (int i = 0; i <= 200; i++) {
			mWorld.step(1.0f / 60.0f, 10, 8);
			mWorld.clearForces();
		}

		// update the isAlive status of virtual checkers
		this.updateVirtualCheckerState();

		VirtualGameState newState = this.extractState();

		// set the world back as before
		this.setWorldFromVirtualGameState(currentState);

		return newState;

	}

	public VirtualGameState simulate(VirtualGameState intitialState, Shot shot) {
		this.setWorldFromVirtualGameState(intitialState);
		VirtualGameState nextState = this.simulate(shot);
		this.setWorldFromVirtualGameState(intitialState);
		return nextState;
	}

	// helper methods
	private static void copyPlatformIntoPhysicsWorld(TestPhysicsWorld world,
			Body body) {

		Platform userData = (Platform) body.getUserData();

		final BodyDef boxBodyDef = new BodyDef();

		boxBodyDef.type = BodyType.StaticBody;
		boxBodyDef.position.x = body.getPosition().x;
		boxBodyDef.position.y = body.getPosition().y;

		final FixtureDef pFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0);
		pFixtureDef.isSensor = true;

		final Body boxBody = world.createBody(boxBodyDef);

		final PolygonShape boxPoly = new PolygonShape();

		boxPoly.set(userData.getpVertices());
		pFixtureDef.shape = boxPoly;

		boxBody.createFixture(pFixtureDef);

		boxPoly.dispose();

		VirtualPlatform vp = new VirtualPlatform(userData);
		vp = vp.clone();
		vp.setBody(boxBody);
		boxBody.setUserData(vp);
	}

	private static void copyCheckerIntoPhysicsWorld(TestPhysicsWorld world,
			Body body) {

		Checker userData = (Checker) body.getUserData();

		final FixtureDef pFixture = PhysicsFactory.createFixtureDef(1f, 0.5f,
				5f);

		final BodyDef circleBodyDef = new BodyDef();
		circleBodyDef.type = BodyType.DynamicBody;
		circleBodyDef.position.x = body.getPosition().x;
		circleBodyDef.position.y = body.getPosition().y;
		circleBodyDef.angle = body.getAngle();
		circleBodyDef.linearDamping = 2f;
		circleBodyDef.angularDamping = 2f;

		final Body circleBody = world.createBody(circleBodyDef);
		final CircleShape circleShape = new CircleShape();
		pFixture.shape = circleShape;

		final float radius = body.getFixtureList().get(0).getShape()
				.getRadius();
		circleShape.setRadius(radius);

		circleBody.createFixture(pFixture);
		circleShape.dispose();

		circleBody.setUserData(new VirtualChecker(userData));
	}

	private static float[] calAngleRange(Body body1, Body body2) {
		float a, b;
		Double theta, deta;
		float distance;
		float x1, x2, y1, y2;

		x1 = body1.getPosition().x;
		y1 = body1.getPosition().y;
		x2 = body2.getPosition().x;
		y2 = body2.getPosition().y;

		theta = Math.atan2(y2 - y1, x2 - x1);
		distance = calDistanceBetween2Bodies(body1, body2);
		deta = Math.atan2(body2.getFixtureList().get(0).getShape().getRadius(),
				distance);

		b = (float) (theta + deta);
		a = (float) (theta - deta);

		return new float[] { a, b };
	}

	private static final float calDistanceBetween2Bodies(Body a, Body b) {
		return Utils.calculateDistance(a.getPosition().x, a.getPosition().y,
				b.getPosition().x, b.getPosition().y);
	}

	private Body getBodyFromID(int ID) {
		Iterator<Body> bodies = this.getBodies();
		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof VirtualChecker) {
				VirtualChecker vc = (VirtualChecker) body.getUserData();
				if (vc.getID() == ID)
					return body;
			}
		}

		return null;
	}

	private void updateVirtualCheckerState() {
		List<Contact> contacts = mWorld.getContactList();
		Iterator<Contact> iter = contacts.iterator();

		// set all pieces as dead
		Iterator<Body> bodies = this.getBodies();
		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof VirtualChecker) {
				VirtualChecker vc = (VirtualChecker) body.getUserData();
				vc.setAsDead();
			}
		}

		while (iter.hasNext()) {
			Contact contact = iter.next();
			if (contact.isTouching())
				// goes thorugh all contact objects that represent a contact
				// between virtual checker
				// and platform, these virtual checkers are set as ALIVE
				checkCollision(contact);
		}
	}

	private void checkCollision(Contact contact) {

		// Both are playing pieces
		if (contact.getFixtureA().getBody().getUserData() instanceof VirtualChecker
				&& contact.getFixtureB().getBody().getUserData() instanceof VirtualChecker) {

		}
		// Piece and platform
		else if (contact.getFixtureA().getBody().getUserData() instanceof VirtualChecker
				&& contact.getFixtureB().getBody().getUserData() instanceof VirtualPlatform) {

			VirtualChecker p1 = (VirtualChecker) contact.getFixtureA()
					.getBody().getUserData();
			p1.setAsAlive();
		}
		// Piece and platform
		else if (contact.getFixtureA().getBody().getUserData() instanceof VirtualPlatform
				&& contact.getFixtureB().getBody().getUserData() instanceof VirtualChecker) {

			VirtualChecker p1 = (VirtualChecker) contact.getFixtureB()
					.getBody().getUserData();
			p1.setAsAlive();
		}
	}

}
