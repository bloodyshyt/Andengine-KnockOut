package com.cslabs.knockout.entity;

import com.badlogic.gdx.physics.box2d.Body;

public class VirtualChecker {
	private static final String TAG = "VirtualChecker";

	private int ID;
	private PlayerNo player;
	private boolean alive = true;

	public VirtualChecker(int ID, PlayerNo playerNo) {
		this.ID = ID;
		this.player = playerNo;
	}

	public VirtualChecker(Checker c) {
		this(c.getID(), c.getPlayer());
	}

	public VirtualChecker clone() {
		return new VirtualChecker(this.ID, this.player);
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

	public boolean isAlive() {
		return alive;
	}
	
	public void setAsDead() {
		alive = false;
	}
	
	public void setAsAlive() {
		alive = true;
	}
}
