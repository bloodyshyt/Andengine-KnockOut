package com.cslabs.knockout.entity;

import java.util.LinkedList;

import org.andengine.util.debug.Debug;


public class VirtualPlayerTurnCycle {
	
	private final static String TAG  = "PlayerTurnCycle";
	
	private Player currentPlayer, head;
	private int size = 0;
	
	LinkedList<Player> players = new LinkedList<Player>();
	
	public VirtualPlayerTurnCycle(Player currentPlayer) {
		head = currentPlayer;
		players.add(currentPlayer);
		this.currentPlayer = currentPlayer;
		this.head = currentPlayer;
		size++;
	}
	
	public void addPlayer(Player newPlayer) {
		currentPlayer.setNextPlayer(newPlayer);
		players.add(newPlayer);
		currentPlayer = newPlayer;
		size++;
	}
	
	/**
	 * Closes the cycle by getting the next player in last element 
	 * to point to the first element
	 */
	public void closeCycle() {
		players.getLast().setNextPlayer(players.getFirst());
		currentPlayer = players.getFirst();
	}
	
	public void removePlayer(Player playerToRemove) {
		
		// get reference for player to be removed
		Player pointer = getPlayer(playerToRemove);
		(pointer.getPreviousPlayer()).setNextPlayer(pointer.getNextPlayer());;
		players.remove(pointer);
	}
	
	public Player nextTurn() {
		currentPlayer = currentPlayer.getNextPlayer();
		return currentPlayer;
	}
	
	/**
	 * @param player
	 * @return requested player based on PlayerNo, traverses through 
	 * the cycle until it finds player, changes currentPlayer
	 */
	public Player setCurrentPlayer(Player player) {
		do {
			currentPlayer = currentPlayer.getNextPlayer();
		} while(currentPlayer.playerNo != player.playerNo);
		
		return currentPlayer;
	}
	
	/**
	 * @param player
	 * @return Player object based on the PlayerNo variable, does 
	 * not change current player
	 */
	public Player getPlayer(Player player) {
		Player pointer = currentPlayer;
		do {
			pointer = pointer.getNextPlayer();
		} while(pointer.playerNo != player.playerNo);
		
		return pointer;
	}
	
	public Player getPlayer(PlayerNo player) {
		Player pointer = currentPlayer;
		do {
			pointer = pointer.getNextPlayer();
		} while(pointer.playerNo != player);
		
		return pointer;
	}
	
	public boolean removeChecker(Checker c) {
		
		Debug.i(TAG, "Looking for player");
		// get reference to Player of the checker
		Player player = getPlayer(c.getPlayer());
		Debug.i(TAG, "Found player " + player);
		return player.playerCheckers.remove(c);
	}
	
	public int size() {
		return size;
	}

	public Player getCurrentPlayer() {
		return currentPlayer;
	}

}
