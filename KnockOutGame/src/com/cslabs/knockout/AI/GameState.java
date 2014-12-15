package com.cslabs.knockout.AI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.util.debug.Debug;
import org.andengine.util.math.MathConstants;
import org.andengine.util.math.MathUtils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.cslabs.knockout.entity.CheckerState;
import com.cslabs.knockout.entity.Platform;
import com.cslabs.knockout.entity.Checker;
import com.cslabs.knockout.entity.PlayerNo;
import com.cslabs.knockout.entity.Utils;
import com.cslabs.knockout.entity.VirtualChecker;
import com.cslabs.knockout.entity.VirtualPlatform;
import com.cslabs.knockout.factory.PlatformFactory;
import com.cslabs.knockout.factory.CheckerFactory;
import com.cslabs.knockout.scene.GameScene;
import com.cslabs.knockout.scene.GameScene.WorldState;
import com.cslabs.knockout.utils.Stopwatch;

public class GameState extends FixedStepPhysicsWorld {

	private static final String TAG = "GameState";

	// --GameState variables-----------------------------------

	// List all ALIVE checkers
	private LinkedList<VirtualChecker> virtualCheckers = new LinkedList<VirtualChecker>();
	// List of P1 and P2 checkers
	public LinkedList<VirtualChecker> P1Checkers = new LinkedList<VirtualChecker>();
	public LinkedList<VirtualChecker> P2Checkers = new LinkedList<VirtualChecker>();
	// Pointers to player and opponent checkers (assigned on generate shots
	// method)
	public LinkedList<VirtualChecker> playerCheckers, opponentCheckers;

	public VirtualPlatform virtualPlatform;

	private Platform platform;
	private float[] platform_cords;

	private WorldState currentWorldState;
	public PlayerNo player, opponent;

	// variables for raycasting
	Line RayCastLine;
	static boolean fixtureHit;
	float RayCastAngle = 0f;
	float RaycastBounceLineLength = 200f;
	float[] RayCastStart = { 0, 0 };

	// variable for POWER formula
	Vector2 fixturePosition;

	LinkedList<Checker> checkers = new LinkedList<Checker>();

	public GameState() {
		super(60, 1, new Vector2(0, 0), false, 6, 2);
	}

	public GameState(int pStepsPerSecond, int pMaximumStepsPerUpdate,
			Vector2 pGravity, boolean pAllowSleep, int pVelocityIterations,
			int pPositionIterations) {
		super(pStepsPerSecond, pMaximumStepsPerUpdate, pGravity, pAllowSleep,
				pVelocityIterations, pPositionIterations);
		// TODO Auto-generated constructor stub
	}

	public GameState createGameStateFromScene(GameScene scene,
			PlayerNo currentPlayer) {
		Debug.i(TAG, "creating new world");
		// Obtain bodies from world
		checkers = scene.getPieces();
		platform = scene.getPlatform();
		platform_cords = scene.getPlatform_cords();

		// Add bodies
		for (Checker c : checkers) {
			VirtualChecker vc = CheckerFactory.getInstance()
					.cloneCheckerIntoPhyiscsWorld(this, c);
			virtualCheckers.add(vc);
			if (vc.getPlayer() == PlayerNo.P1)
				P1Checkers.add(vc);
			else if (vc.getPlayer() == PlayerNo.P2)
				P2Checkers.add(vc);
		}
		Debug.i(TAG, "We have " + this.getBodyCount() + " bodies");

		// add the platform in
		virtualPlatform = PlatformFactory.getInstance()
				.clonePlatformIntoPhysicsWorld(this, platform);
		// dumpInfo(TAG);
		return this;

	}

	public GameState copyGameState() {

		GameState copy = new GameState();

		// Add bodies
		for (VirtualChecker c : this.virtualCheckers) {
			VirtualChecker vc = CheckerFactory.getInstance()
					.cloneCheckerIntoPhyiscsWorld(copy, c);
			copy.virtualCheckers.add(vc);
			if (vc.getPlayer() == PlayerNo.P1)
				copy.P1Checkers.add(vc);
			else if (vc.getPlayer() == PlayerNo.P2)
				copy.P2Checkers.add(vc);
		}

		copy.virtualPlatform = PlatformFactory.getInstance()
				.clonePlatformIntoPhysicsWorld(copy, this.virtualPlatform);

		// add current player
		copy.player = player;
		copy.opponent = opponent;

		return copy;

	}

	public ArrayList<Shot> generateShots(PlayerNo currentPlayer) {

		Debug.i(TAG, "generating shots for " + currentPlayer + " P1:"
				+ P1Checkers.size() + " P2:" + P2Checkers.size());

		Stopwatch timer = new Stopwatch();

		ArrayList<Shot> shots = new ArrayList<Shot>();

		// Assign playerCheckers and opponentCheckers
		if (currentPlayer == PlayerNo.P1) {
			playerCheckers = P1Checkers;
			opponentCheckers = P2Checkers;
		} else if (currentPlayer == PlayerNo.P2) {
			playerCheckers = P2Checkers;
			opponentCheckers = P1Checkers;
		}

		Debug.i(TAG, "player has " + playerCheckers.size()
				+ " and opponent has " + opponentCheckers.size());

		Body playerBody, oppBody;
		float a;
		float b;
		Double theta, deta;
		float distance;
		float x1, x2, y1, y2;

		// Generate straight shots first
		for (VirtualChecker p : playerCheckers) {
			for (VirtualChecker o : opponentCheckers) {
				float bestScore = 0;
				Shot bestShot = null;
				playerBody = p.getBody();
				oppBody = o.getBody();

				// calculate angles
				x1 = playerBody.getPosition().x;
				y1 = playerBody.getPosition().y;
				x2 = oppBody.getPosition().x;
				y2 = oppBody.getPosition().y;
				theta = Math.atan2(y2 - y1, x2 - x1);
				distance = Utils.calculateDistance(x1, y1, x2, y2);
				deta = Math.atan2(
						16 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
						distance);
				b = (float) (theta + deta);
				a = (float) (theta - deta);
				// Debug.i(TAG, "Angle range is " + MathUtils.radToDeg(a) + " "
				// + MathUtils.radToDeg(b));

				for (double i = a; i < b; i += MathUtils.degToRad(1)) {
					if (generateRaycast(playerBody, (float) i)) {
						int lowerPower = (int) ((distance / 0.4828) * 1.2);
						for (int power = lowerPower; power < Utils.MAX_POWER; power += 10) {
							Shot shot = new Shot(p.getID(),
									(int) (power), (float) i);
							if (isShotSuccessful(shot, currentPlayer)) {
								if (shot.getScore() > bestScore) {
									bestScore = shot.getScore();
									bestShot = shot;
								}
							} else
								power += 10;

						}
					}
				}
				if (bestShot != null) {
					shots.add(bestShot);
				}
			}
		}

		Debug.i(TAG,
				"generated " + shots.size() + " shots in "
						+ timer.elapsedTime() + " seconds");
		return shots;

	}

	public ArrayList<Shot> GenerateShotsForPlayer(PlayerNo currentPlayer) {
		// uses a table to reduce the number of shots generated
		Stopwatch timer = new Stopwatch();

		// Assign playerCheckers and opponentCheckers
		if (currentPlayer == PlayerNo.P1) {
			playerCheckers = P1Checkers;
			opponentCheckers = P2Checkers;
		} else if (currentPlayer == PlayerNo.P2) {
			playerCheckers = P2Checkers;
			opponentCheckers = P1Checkers;
		}

		Debug.i("GenerateShotsForPlayer: Assigned the player and opponent checkers of size "
				+ playerCheckers.size()
				+ " and "
				+ opponentCheckers.size()
				+ " respectively from " + this.getBodyCount() + " bodies");

		ArrayList<Shot> shots = new ArrayList<Shot>();
		Body bodyOfFirer;

		for (VirtualChecker vc : playerCheckers) {
			bodyOfFirer = vc.getBody();
			Debug.i(TAG, "For ID: " + vc.getID());
			for (int i = 0; i < 360; i = i + 2) {
				if (generateRaycast(bodyOfFirer, i)) {
					// calculate distance
					Debug.i(TAG, "Hit at angle " + i);
					float x1, x2, y1, y2;
					x1 = bodyOfFirer.getPosition().x;
					y1 = bodyOfFirer.getPosition().y;
					x2 = fixturePosition.x;
					y2 = fixturePosition.y;

					double distance = Math.sqrt(Math.pow((x1 - x2), 2)
							+ Math.pow((y1 - y2), 2));

					int lowerPower = (int) ((distance / 0.4828) * 1.2);

					for (int pwer = lowerPower; pwer <= Utils.MAX_POWER; pwer += 10) {
						shots.add(new Shot(vc.getID(), pwer, MathUtils
								.degToRad(i)));
					}

				}
			}
		}

		Debug.i(TAG,
				"Generated " + shots.size() + " shots in "
						+ timer.elapsedTime() + " seconds");

		return shots;
	}

	/**
	 * @param state
	 * @param bodyOfFirer
	 * @param degree
	 *            (degrees and rads)
	 * @return returns true if generataed raycast hits a fixture
	 */

	/**
	 * @param shot
	 *            : shot to be simulated
	 * @return next GameState with player and opponent variables swapped
	 */
	public GameState simulate(Shot shot) {

		// create a new copy of gamestate
		GameState copy = copyGameState();

		// Get list of bodies in the copy of world and find piece being fired
		Body bodyOfFirer = copy.getBodyFromID(shot.getFirerID());
		bodyOfFirer.setLinearVelocity(shot.getVelocity());

		// step the world forward
		World mWorld = copy.getmWorld();
		for (int i = 0; i <= 200; i++) {
			mWorld.step(1.0f / 60.0f, 10, 8);
			mWorld.clearForces();
		}

		// updates the states of the new GameState copy
		copy.updateCheckerLists();

		return copy;
	}

	public boolean isShotSuccessful(Shot shot, PlayerNo currentPlayer) {

		int[] state1, state2;

		GameState nextState = this.simulate(shot);

		state1 = this.returnNumOfPlayerCheckers(currentPlayer);
		state2 = nextState.returnNumOfPlayerCheckers(currentPlayer);

		int kills = state1[1] - state2[1];
		int deaths = state1[0] - state2[0];

		float score = (float) (800 * kills - 1000 * deaths);

		// Debug.i(TAG, "shot successful " + state1[0] + " " + state1[1] + " "
		// + state2[0] + " " + state2[1] + "with " + kills + " kills and "
		// + deaths + " deaths");

		if (score > 0) {
			shot.setScore(score);
			return true;
		} else {
			return false;
		}
	}

	public int[] returnNumOfPlayerCheckers(PlayerNo currentPlayer) {
		// Assign playerCheckers and opponentCheckers
		if (currentPlayer == PlayerNo.P1) {
			playerCheckers = P1Checkers;
			opponentCheckers = P2Checkers;
		} else if (currentPlayer == PlayerNo.P2) {
			playerCheckers = P2Checkers;
			opponentCheckers = P1Checkers;
		}
		int[] num = { playerCheckers.size(), opponentCheckers.size() };

		return num;
	}

	public static boolean isFixtureHit() {
		return fixtureHit;
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

	/**
	 * steps the world forward by one. Gets list of contacts and updates
	 * checkers that are still on the platform those are not are considered dead
	 * and removed from the corresponding lists
	 */
	public void updateCheckerLists() {

		// stepping the world by one is necessary to populate the contacts list
		mWorld.step(1.0f / 60.0f, 10, 8);

		List<Contact> contacts = mWorld.getContactList();
		Iterator<Contact> iter = contacts.iterator();

		// set all pieces as dead
		Iterator<Body> bodies = this.getBodies();
		while (bodies.hasNext()) {
			Body body = bodies.next();
			if (body.getUserData() instanceof VirtualChecker) {
				VirtualChecker vc = (VirtualChecker) body.getUserData();
				vc.setState(CheckerState.DEAD);
				// Debug.i(TAG, "Checker set as dead");
			}
		}

		while (iter.hasNext()) {
			Contact contact = iter.next();
			if (contact.isTouching())
				checkCollision(contact);
		}

		for (Iterator<VirtualChecker> iterator = virtualCheckers.iterator(); iterator
				.hasNext();) {
			VirtualChecker vc = iterator.next();
			if (vc.getState() == CheckerState.DEAD) {
				iterator.remove();
				if (vc.getPlayer() == PlayerNo.P1) {
					P1Checkers.remove(vc);
				} else if (vc.getPlayer() == PlayerNo.P2) {
					P2Checkers.remove(vc);
				}
			}
		}

	}

	 public void dumpInfo(String s) {
	 Debug.i(TAG, "dumping bodies info of " + this.getBodyCount());
	 Iterator<Body> bodies = this.getBodies();
	 while (bodies.hasNext()) {
	 Body body = bodies.next();
	 if (body.getUserData() instanceof VirtualChecker) {
	 Debug.i(TAG,
	 "In "
	 + s
	 + " "
	 + ((VirtualChecker) body.getUserData()).getID()
	 + " at "
	 + body.getPosition()
	 .mul(PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT)
	 + " "
	 + ((VirtualChecker) body.getUserData())
	 .getState());
	 }
	 }
	 }

	private boolean generateRaycast(Body bodyOfFirer, float degree) {

		fixtureHit = false;

		// calculate coordinates of raycastline
		Vector2 position = bodyOfFirer.getPosition();
		float x1 = position.x;
		float y1 = position.y;

		float radiusInMeters = 18 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
		float raycastLengthInMeters = 400 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

		float Ax = (float) (x1 + radiusInMeters * Math.cos(degree));
		float Ay = (float) (y1 + radiusInMeters * Math.sin(degree));

		float Bx = (float) (Ax + raycastLengthInMeters * Math.cos(degree));
		float By = (float) (Ay + raycastLengthInMeters * Math.sin(degree));

		rayCast(rayCastCallback, new Vector2(Ax, Ay), new Vector2(Bx, By));

		return fixtureHit;
	}

	private boolean generateRaycast2(Body bodyOfFirer, float degree) {
		// for testing new method of genreating shots

		fixtureHit = false;

		// calculate coordinates of raycastline
		Vector2 position = bodyOfFirer.getPosition();
		float x1 = position.x;
		float y1 = position.y;

		float radiusInMeters = 18 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
		float raycastLengthInMeters = 400 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

		float Ax = (float) (x1 + radiusInMeters * Math.cos(degree));
		float Ay = (float) (y1 + radiusInMeters * Math.sin(degree));

		float Bx = (float) (Ax + raycastLengthInMeters * Math.cos(degree));
		float By = (float) (Ay + raycastLengthInMeters * Math.sin(degree));

		rayCast(rayCastForOpponentCheckers, new Vector2(Ax, Ay), new Vector2(
				Bx, By));

		return fixtureHit;
	}

	private void checkCollision(Contact contact) {

		// Debug.i(TAG, "contact: "
		// + contact.getFixtureA().getBody().getUserData() + " "
		// + contact.getFixtureB().getBody().getUserData());

		// Both are playing pieces
		if (contact.getFixtureA().getBody().getUserData() instanceof VirtualChecker
				&& contact.getFixtureB().getBody().getUserData() instanceof VirtualChecker) {

			// VirtualChecker p1, p2;
			// p1 = (VirtualChecker)
			// contact.getFixtureA().getBody().getUserData();
			// p2 = (VirtualChecker)
			// contact.getFixtureB().getBody().getUserData();
			// Debug.i(TAG, p1.getID() + " and " + p2.getID() + " collided");

		}
		// Piece and platform
		else if (contact.getFixtureA().getBody().getUserData() instanceof VirtualChecker
				&& contact.getFixtureB().getBody().getUserData() instanceof VirtualPlatform) {

			VirtualChecker p1 = (VirtualChecker) contact.getFixtureA()
					.getBody().getUserData();
			p1.setState(CheckerState.ALIVE);
			// Debug.i(TAG, "checker found on the platform");

		}
		// Piece and platform
		else if (contact.getFixtureA().getBody().getUserData() instanceof VirtualPlatform
				&& contact.getFixtureB().getBody().getUserData() instanceof VirtualChecker) {

			VirtualChecker p1 = (VirtualChecker) contact.getFixtureB()
					.getBody().getUserData();
			p1.setState(CheckerState.ALIVE);
			// Debug.i(TAG, "checker found on the platform");

		}
	}

	RayCastCallback rayCastCallback = new RayCastCallback() {

		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point,
				Vector2 normal, float fraction) {
			if (fixture.getBody().getUserData() instanceof VirtualChecker
					&& ((VirtualChecker) (fixture.getBody().getUserData()))
							.getState() == CheckerState.ALIVE) {
				fixtureHit = true;
				fixturePosition = fixture.getBody().getPosition();
			}
			return 0;
		}
	};

	RayCastCallback rayCastForOpponentCheckers = new RayCastCallback() {

		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point,
				Vector2 normal, float fraction) {
			if (fixture.getBody().getUserData() instanceof VirtualChecker
					&& ((VirtualChecker) (fixture.getBody().getUserData()))
							.getPlayer() == opponent
					&& ((VirtualChecker) (fixture.getBody().getUserData()))
							.getState() == CheckerState.ALIVE) {
				fixtureHit = true;
				fixturePosition = fixture.getBody().getPosition();
			}
			return 0;
		}
	};

	public boolean gameOver(PlayerNo currentPlayer) {
		if (currentPlayer == PlayerNo.P1) {
			playerCheckers = P1Checkers;
			opponentCheckers = P2Checkers;
		} else if (currentPlayer == PlayerNo.P2) {
			playerCheckers = P2Checkers;
			opponentCheckers = P1Checkers;
		}

		if (playerCheckers.size() == 0)
			return true;
		else
			return false;
	}
}
