package com.cslabs.knockout.AI;

import org.andengine.util.debug.Debug;

import com.badlogic.gdx.math.Vector2;
import com.cslabs.knockout.entity.Utils;

public class Shot implements Comparable<Shot> {

	private static final String TAG = "Shot";

	// ====================================================
	// VARIABLES
	// ====================================================

	private int firerID; // ID of piece to be fired
	private float score;
	private Vector2 velocity; // velocity piece to be fired at
	private GameState nextGameState; // reference to resultant from the shot

	public float[] data = { 0, 0, 0, 0, 0, 0, 0, 0 };

	public boolean success;

	// ====================================================
	// CONSTRUCTORS
	// ====================================================
	public Shot(int ID, Vector2 v) {
		firerID = ID;
		velocity = v;
	}

	public Shot(int ID, float distance, float degree) {
		firerID = ID;
		velocity = Utils.generateVelocity(distance, degree);
	}

	public Shot(int ID, int power, float degree) {
		firerID = ID;
		velocity = Utils.generateVelocity(power, degree);
	}

	public int getFirerID() {
		return firerID;
	}

	public Vector2 getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2 velocity) {
		this.velocity = velocity;
	}

	// ====================================================
	// METHODS
	// ====================================================

	public void dumpInfo(String t) {
		Debug.i(t, "Shot has ID of " + firerID + " and velocity of " + velocity
				+ " and score of " + score);
	}

	public void dumpInfo() {
		Debug.i("Shot has ID of " + firerID + " and velocity of " + velocity
				+ " and score of " + score);
	}
	
	@Override
	public int compareTo(Shot another) {
		if (this.getScore() > another.getScore())
			return 1;
		else if (this.getScore() < another.getScore())
			return -1;
		else
			return 0;
	}

	// ====================================================
	// GETTERS & SETTERS
	// ====================================================

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public GameState getNextGameState() {
		return nextGameState;
	}

	public void setNextGameState(GameState nextGameState) {
		this.nextGameState = nextGameState;
	}

}
