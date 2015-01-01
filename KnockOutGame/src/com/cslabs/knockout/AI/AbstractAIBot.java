package com.cslabs.knockout.AI;

import com.cslabs.knockout.entity.Player;

public abstract class AbstractAIBot {

	public abstract float evaluate(TestPhysicsWorld state,
			VirtualGameState virtualState, Player currentPlayer);

	public abstract Shot findBestShot2Player(TestPhysicsWorld state,Player currentPlayer,
			final int depth, final int pAccuracy);

	public abstract Shot findBestShotMultiPlayer(TestPhysicsWorld state, final int depth, final int pAccuracy);

	public abstract boolean isShotSuccessful(TestPhysicsWorld state, Player currentPlayer, Shot shot);
}
