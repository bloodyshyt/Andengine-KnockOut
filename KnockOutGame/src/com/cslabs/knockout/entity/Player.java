package com.cslabs.knockout.entity;

import java.util.LinkedList;

import com.cslabs.knockout.AI.AIBotWrapper;
import com.cslabs.knockout.AI.Shot;

public class Player {

	public LinkedList<Checker> playerCheckers;
	public PlayerNo playerNo;
	private Player nextPlayer, previousPlayer;
	private AIBotWrapper AIBot;
	public boolean isCPU = false;
	
	public Player(LinkedList<Checker> playerCheckers, PlayerNo playerNo, AIBotWrapper AIBot) {
		this.playerCheckers = playerCheckers;
		this.playerNo = playerNo;
		this.AIBot = AIBot;
		if(this.AIBot != null) {
			isCPU = true;
		}
	}
	
	// setters and getters

	public Player getNextPlayer() {
		return nextPlayer;
	}

	public void setNextPlayer(Player nextPlayer) {
		this.nextPlayer = nextPlayer;
	}

	public Player getPreviousPlayer() {
		return previousPlayer;
	}

	public void setPreviousPlayer(Player previousPlayer) {
		this.previousPlayer = previousPlayer;
	}
	
}
