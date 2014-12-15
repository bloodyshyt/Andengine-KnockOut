package com.cslabs.knockout.AI;

import com.cslabs.knockout.entity.PlayerNo;

public abstract class AbstractAIBot {

	public abstract Shot evaluate(GameState state, PlayerNo currentPlayer);

	public abstract Shot findBestShot(GameState state, PlayerNo currentPlayer);
	

}
