package com.cslabs.knockout.AI;

import com.cslabs.knockout.entity.Player;


public abstract class AbstractAIBot {

	public abstract Shot evaluate(GameState state, Player currentPlayer);

	public abstract Shot findBestShot2Player(GameState state, Player currentPlayer, final int depth, final int pAccuracy);
	
	public abstract Shot findBestShotMultiPlayer(GameState state, Player currentPlayer, final int depth, final int pAccuracy);
	
	public abstract boolean isShotSuccessful(final GameState state1,Player currentPlayer,  Shot shot);
}
