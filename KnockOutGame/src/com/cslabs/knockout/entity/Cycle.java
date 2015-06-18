package com.cslabs.knockout.entity;

import java.util.LinkedList;

import org.andengine.util.debug.Debug;

public class Cycle extends LinkedList<Player> {
	
	private static final String TAG = "PlayerCycle";

	//=========================================
	// VARIABLES
	//=========================================
	private Player mCurPlayer;
	private final Player mHead;
	private int index;
	
	public Cycle(Player pHead) {
		mCurPlayer =  pHead;
		mHead = pHead;
		index = 0;
	}
	
	public void addPlayer(Player pPlayer) {
		this.add(pPlayer);
	}
	
	public boolean removePlayer(Player pPlayer) {
		// locate the player and record index
		int playerIndex = this.indexOf(pPlayer);
		if(playerIndex == -1) return false;
		
		// delete the player and update the index if necessary
		this.remove(playerIndex);
		if(playerIndex < index) index--;
		
		return true;
	}
	
	//=========================================
	// GETTERS AND SETTERS
	//=========================================
	/**
	 * @return returns the next player after the current player. Each call will 
	 * return a different player
	 */
	public Player getNextPlayer() {
		Player p = this.get(index);
		index = (index == this.size() - 1) ? 0 : index++;
		mCurPlayer = this.get(index);	// update the current player
		
		return p;
	}
	
	public Player getCurrentPlayer() {
		return mCurPlayer;
	}
	
	public int getIndex() {
		return index;
	}
	
}
