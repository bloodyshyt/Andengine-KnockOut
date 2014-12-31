package com.cslabs.knockout.AI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.andengine.entity.primitive.Line;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.util.debug.Debug;
import org.andengine.util.math.MathUtils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.cslabs.knockout.entity.Checker;
import com.cslabs.knockout.entity.CheckerState;
import com.cslabs.knockout.entity.Platform;
import com.cslabs.knockout.entity.Player;
import com.cslabs.knockout.entity.PlayerNo;
import com.cslabs.knockout.entity.Utils;
import com.cslabs.knockout.entity.VirtualChecker;
import com.cslabs.knockout.entity.VirtualPlatform;

/**
 * This class is the serialiable game state object that will form the nodes of
 * search trees in all AIBot implementations as of now it only supports 2 player
 * modes. Logic to for up to 4 players will be added later when all the other
 * bugs like memory leaks are fixed
 * 
 *** @author Raynold Ng - cs labs
 **/

public class GameState extends FixedStepPhysicsWorld {

	public GameState(int pStepsPerSecond, int pMaximumStepsPerUpdate,
			Vector2 pGravity, boolean pAllowSleep) {
		super(pStepsPerSecond, pMaximumStepsPerUpdate, pGravity, pAllowSleep);
		// TODO Auto-generated constructor stub
	}

	private static final String TAG = "GameState";

	public int[] nPlayerCheckers = new int[5];
	


	// variables for raycasting
	static Line RayCastLine;
	static boolean fixtureHit;
	static float RayCastAngle = 0f;
	static float RaycastBounceLineLength = 800f;
	static float[] RayCastStart = { 0, 0 };

	// variable for POWER formula
	Vector2 fixturePosition;

	public GameState() {
		super(60, 1, new Vector2(0, 0), false, 6, 2);
	}

	public GameState createFromScene(PhysicsWorld world) {
		GameState newState = new GameState();
		Iterator<Body> bodies = world.getBodies();
		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof Checker) {
				copyCheckerIntoPhysicsWorld(newState,
						(Checker) body.getUserData(), body);
				newState.nPlayerCheckers[((Checker) body.getUserData())
						.getPlayer().getValue()]++;
			}
			if (body.getUserData() instanceof Platform) {
				copyPlatformIntoPhysicsWorld(newState,
						(Platform) body.getUserData(), body);
			}
		}

		return newState;
	}

	public GameState clone() {
		GameState newState = new GameState();
		Iterator<Body> bodies = this.getBodies();
		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof VirtualChecker) {
				copyVirtualCheckerIntoPhysicsWorld(newState,
						(VirtualChecker) body.getUserData(), body);
			}
			if (body.getUserData() instanceof VirtualPlatform) {
				copyVirtualPlatformIntoPhysicsWorld(newState,
						(VirtualPlatform) body.getUserData(), body);
			}
		}

		return newState;
	}

	public ArrayList<Shot> generateShots(Player currentPlayer, AbstractAIBot bot) {

		ArrayList<Shot> shots = new ArrayList<Shot>();

		// convert iterable of bodies into a list
		List<Body> listOfBodies = Utils.copyIterator(this.getBodies());
		Debug.i(TAG, "list of " + listOfBodies.size() + " bodies");
		ArrayList<Body> playerBodies = new ArrayList<Body>();
		ArrayList<Body> opponentBodies = new ArrayList<Body>();

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

		//Debug.i(TAG, "Player has " + playerBodies.size() + " opponent has "
		//		+ opponentBodies.size() + " bodies");

		for (Body playerCheckerBody : playerBodies) {
			for (Body opponentCheckerBody : opponentBodies) {
				float[] angleRange = calAngleRange(playerCheckerBody,
						opponentCheckerBody);
				float distance = calDistanceBetween2Bodies(playerCheckerBody,
						opponentCheckerBody);

				float bestScore = Float.MIN_VALUE;
				Shot bestShot = null;

				for (double degree = angleRange[0]; degree < angleRange[1]; degree += MathUtils
						.degToRad(1)) {
					if (generateRaycast(playerCheckerBody, (float) degree)) {
						int lowerPower = (int) ((distance / 0.4828) * 1.2);
						for (int power = lowerPower; power < Utils.MAX_POWER; power += 10) {
							Shot shot = new Shot(
									((VirtualChecker) playerCheckerBody
											.getUserData()).getID(),
									(int) (power),
									(float) degree);
							if (isShotSuccessful(shot, currentPlayer, bot)) {
								if (shot.getScore() > bestScore) {
									bestScore = shot.getScore();
									bestShot = shot;
								} 
							} else
								power += 10;

						}
					}
				}
				if(bestShot != null) shots.add(bestShot);
			}
		}

		//Debug.i(TAG, "Generated " + shots.size() + " shots");

		return shots;

	}

	public GameState simulate(Shot shot) {
		GameState nextState = this.clone();

		Body bodyOfFirer = nextState.getBodyFromID(shot.getFirerID());
		bodyOfFirer.setLinearVelocity(shot.getVelocity());

		// step the world forward
		World mWorld = nextState.getmWorld();
		for (int i = 0; i <= 200; i++) {
			mWorld.step(1.0f / 60.0f, 10, 8);
			mWorld.clearForces();
		}

		// updates the states of the new GameState copy
		nextState.updateCheckerLists();

		return nextState;

	}

	// helper methods

	private static void copyPlatformIntoPhysicsWorld(GameState state,
			Platform userData, Body body) {
		final BodyDef boxBodyDef = new BodyDef();

		boxBodyDef.type = BodyType.StaticBody;
		boxBodyDef.position.x = body.getPosition().x;
		boxBodyDef.position.y = body.getPosition().y;

		final FixtureDef pFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0);
		pFixtureDef.isSensor = true;

		final Body boxBody = state.createBody(boxBodyDef);

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

	private static void copyVirtualPlatformIntoPhysicsWorld(GameState state,
			VirtualPlatform userData, Body body) {
		final BodyDef boxBodyDef = new BodyDef();

		boxBodyDef.type = BodyType.StaticBody;
		boxBodyDef.position.x = body.getPosition().x;
		boxBodyDef.position.y = body.getPosition().y;

		final FixtureDef pFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0);
		pFixtureDef.isSensor = true;

		final Body boxBody = state.createBody(boxBodyDef);

		final PolygonShape boxPoly = new PolygonShape();

		boxPoly.set(userData.getpVertices());
		pFixtureDef.shape = boxPoly;

		boxBody.createFixture(pFixtureDef);

		boxPoly.dispose();

		boxBody.setUserData(userData.clone());
	}

	private static void copyCheckerIntoPhysicsWorld(GameState state,
			Checker userData, Body body) {
		final FixtureDef pFixture = PhysicsFactory.createFixtureDef(1f, 0.5f,
				5f);

		final BodyDef circleBodyDef = new BodyDef();
		circleBodyDef.type = BodyType.DynamicBody;
		circleBodyDef.position.x = body.getPosition().x;
		circleBodyDef.position.y = body.getPosition().y;
		circleBodyDef.angle = body.getAngle();
		circleBodyDef.linearDamping = 2f;
		circleBodyDef.angularDamping = 2f;

		final Body circleBody = state.createBody(circleBodyDef);
		final CircleShape circleShape = new CircleShape();
		pFixture.shape = circleShape;

		final float radius = body.getFixtureList().get(0).getShape()
				.getRadius();
		circleShape.setRadius(radius);

		circleBody.createFixture(pFixture);
		circleShape.dispose();

		circleBody.setUserData(new VirtualChecker(userData));
	}

	private static void copyVirtualCheckerIntoPhysicsWorld(GameState state,
			VirtualChecker userData, Body body) {
		final FixtureDef pFixture = PhysicsFactory.createFixtureDef(1f, 0.5f,
				5f);

		final BodyDef circleBodyDef = new BodyDef();
		circleBodyDef.type = BodyType.DynamicBody;
		circleBodyDef.position.x = body.getPosition().x;
		circleBodyDef.position.y = body.getPosition().y;
		circleBodyDef.angle = body.getAngle();
		circleBodyDef.linearDamping = 2f;
		circleBodyDef.angularDamping = 2f;

		final Body circleBody = state.createBody(circleBodyDef);
		final CircleShape circleShape = new CircleShape();
		pFixture.shape = circleShape;

		final float radius = body.getFixtureList().get(0).getShape()
				.getRadius();
		circleShape.setRadius(radius);

		circleBody.createFixture(pFixture);
		circleShape.dispose();

		circleBody.setUserData(userData.clone());
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

	public Body getBodyFromID(int ID) {
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

	public void updateCheckerLists() {

		// stepping the world by one is necessary to populate the contacts list
		mWorld.step(1.0f / 60.0f, 10, 8);

		// flush nPlayerCheckers array
		nPlayerCheckers = new int[] { 0, 0, 0, 0, 0 };

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
				checkCollision(contact);
		}

		// remove all bodies that have been flagged as dead
		bodies = this.getBodies();
		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof VirtualChecker) {
				VirtualChecker vc = (VirtualChecker) body.getUserData();
				if (!vc.isAlive()) {
					body.setActive(false);
					this.destroyBody(body);
				} else {
					// increment corresponding nPlayerChecker counter
					nPlayerCheckers[vc.getPlayer().getValue()]++;
				}
			}
		}

	}

	/**
	 * @param currentPlayer
	 * @return returns int[2] where numOfPlayerAndOpponentCheckers[0] is number
	 *         of player checkers, numOfPlayerAndOpponentCheckers[1] is sum of
	 *         all the other checkers
	 */
	public int[] getNumOfPlayerAndOpponentCheckers(Player currentPlayer) {
		Iterator<Body> bodies = this.getBodies();
		int[] numOfPlayerAndOpponentCheckers = new int[] { 0, 0 };
		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof VirtualChecker) {
				VirtualChecker vc = (VirtualChecker) body.getUserData();
				if (vc.getPlayer() == currentPlayer.playerNo) {
					numOfPlayerAndOpponentCheckers[0]++;
				} else {
					numOfPlayerAndOpponentCheckers[1]++;
				}
			}
		}

		bodies = null;

		return numOfPlayerAndOpponentCheckers;
	}

	public ArrayList<VirtualChecker> getPlayerVirtualCheckers(
			Player currentPlayer) {
		Iterator<Body> bodies = this.getBodies();
		ArrayList<VirtualChecker> list = new ArrayList<VirtualChecker>();

		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof VirtualChecker) {
				VirtualChecker vc = (VirtualChecker) body.getUserData();
				if (vc.getPlayer() == currentPlayer.playerNo) {
					list.add(vc);
				}
			}
		}

		return list;
	}

	public ArrayList<VirtualChecker> getOpponentVirtualCheckers(
			Player currentPlayer) {
		Iterator<Body> bodies = this.getBodies();
		ArrayList<VirtualChecker> list = new ArrayList<VirtualChecker>();

		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof VirtualChecker) {
				VirtualChecker vc = (VirtualChecker) body.getUserData();
				if (vc.getPlayer() != currentPlayer.playerNo) {
					list.add(vc);
				}
			}
		}

		return list;
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

	public boolean isShotSuccessful(Shot shot, Player currentPlayer, AbstractAIBot bot) {

		 return bot.isShotSuccessful(this, currentPlayer, shot);
		/*
		int[] state1, state2;
		

		GameState nextState = this.simulate(shot);

		state1 = this.getNumOfPlayerAndOpponentCheckers(currentPlayer);
		state2 = nextState.getNumOfPlayerAndOpponentCheckers(currentPlayer);

		int kills = state1[1] - state2[1];
		int deaths = state1[0] - state2[0];

		float score = (float) (800 * kills - 1000 * deaths);

		Debug.i(TAG, "is shot successful? " + state1[0] + " " + state1[1] + " "
				+ state2[0] + " " + state2[1] + " with " + kills
				+ " kills and " + deaths + " deaths");

		if (score > 0) {
			shot.setScore(score);
			shot.setNextGameState(nextState);
			return true;
		} else {
			return false;
		}*/
	}

	public void dumpInfo() {
		Iterator<Body> bodies = this.getBodies();
		VirtualChecker vc;
		VirtualPlatform vp;
		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof VirtualChecker) {
				vc = (VirtualChecker) body.getUserData();
				//Debug.i(TAG,
				//		"VC of player:" + vc.getPlayer() + " ID: " + vc.getID());
			
			}
			if (body.getUserData() instanceof VirtualPlatform) {
				vp = (VirtualPlatform) body.getUserData();
				//Debug.i(TAG, "Found a virtual platform");
			}
		}
	}

	// raycast helper methods

	private boolean generateRaycast(Body bodyOfFirer, float degree) {

		fixtureHit = false;

		// calculate coordinates of raycastline
		Vector2 position = bodyOfFirer.getPosition();
		float x1 = position.x;
		float y1 = position.y;

		float radiusInMeters = 18 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
		float raycastLengthInMeters = 800 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

		float Ax = (float) (x1 + radiusInMeters * Math.cos(degree));
		float Ay = (float) (y1 + radiusInMeters * Math.sin(degree));

		float Bx = (float) (Ax + raycastLengthInMeters * Math.cos(degree));
		float By = (float) (Ay + raycastLengthInMeters * Math.sin(degree));

		rayCast(rayCastCallback, new Vector2(Ax, Ay), new Vector2(Bx, By));

		return fixtureHit;
	}

	RayCastCallback rayCastCallback = new RayCastCallback() {

		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point,
				Vector2 normal, float fraction) {
			if (fixture.getBody().getUserData() instanceof VirtualChecker
					&& ((VirtualChecker) (fixture.getBody().getUserData()))
							.isAlive()) {
				fixtureHit = true;
				fixturePosition = fixture.getBody().getPosition();
			}
			return 0;
		}
	};

}
