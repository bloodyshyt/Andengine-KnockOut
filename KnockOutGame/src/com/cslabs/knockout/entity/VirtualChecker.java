package com.cslabs.knockout.entity;

import java.util.LinkedList;

import org.andengine.util.debug.Debug;

import com.badlogic.gdx.physics.box2d.Body;


public class VirtualChecker {
	private static final String TAG = "VirtualChecker";

	private int ID;
	private PlayerNo player;
	public CheckerState state = CheckerState.ALIVE;
	private Body body;

	public VirtualChecker() {
	}

	public VirtualChecker(Checker c) {
		ID = c.getID();
		player = c.getPlayer();
		state = c.getState();
	}

	public VirtualChecker clone() {
		VirtualChecker copy = new VirtualChecker();
		copy.setState(this.state);
		copy.setID(this.ID);
		copy.setPlayer(this.player);
		return copy;
	}

	public CheckerState getState() {
		return state;
	}

	public void setState(CheckerState state) {
		this.state = state;
	}

	public int getID() {
		return ID;
	}

	public PlayerNo getPlayer() {
		return player;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public void setPlayer(PlayerNo player) {
		this.player = player;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}
	
	public void dumpInfo(String s) {
		Debug.i(TAG, s + " info, ID " + ID + " PlayerNo: " + player + " state: " + state + "body: " + body);
	}
}
