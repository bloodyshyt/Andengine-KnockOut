package com.cslabs.knockout.entity;

import java.util.LinkedList;

public class Cycle extends LinkedList<Player> {

	private static final String TAG = "PlayerCycle";

	// =========================================
	// VARIABLES
	// =========================================
	private Player mCurPlayer;
	private final Player mHead;
	private int index;

	public Cycle(Player pHead) {
		mCurPlayer = pHead;
		mHead = pHead;
		index = 0;
	}

	public void addPlayer(Player pPlayer) {
		this.add(pPlayer);
	}

	public boolean removePlayer(Player pPlayer) {
		// locate the player and record index
		int playerIndex = this.indexOf(pPlayer);
		if (playerIndex == -1)
			return false;

		// delete the player and update the index if necessary
		this.remove(playerIndex);
		if (playerIndex < index)
			index--;

		return true;
	}

	public Player getPlayer(PlayerNo player) {
		Player pointer = mCurPlayer;
		do {
			pointer = pointer.getNextPlayer();
		} while(pointer.playerNo != player);
		
		return pointer;
	}
	
	public boolean removeChecker(Checker c) {

		// get reference to Player of the checker
		Player player = getPlayer(c.getPlayer());
		return player.playerCheckers.remove(c);
	}

	// =========================================
	// GETTERS AND SETTERS
	// =========================================
	/**
	 * @return returns the next player after the current player. Each call will
	 *         return a different player
	 */
	public Player getNextPlayer() {
		Player p = this.get(index);
		index = (index == this.size() - 1) ? 0 : index++;
		mCurPlayer = this.get(index); // update the current player

		return p;
	}

	public Player getCurrentPlayer() {
		return mCurPlayer;
	}

	public int getIndex() {
		return index;
	}

	public Player getHeadPlayer() {
		return mHead;
	}

}
