package com.cslabs.knockout.AI;

import org.andengine.util.debug.Debug;

import com.cslabs.knockout.GameLevels.AIBots.AIBotTypes;
import com.cslabs.knockout.entity.Player;

public class AIBotWrapper {
	private int intelligence, accuracy;
	private AIBotTypes botType;
	
	public AIBotWrapper(final int intelligence, final int accuracy, final AIBotTypes botType) {
		this.intelligence = intelligence;
		this.accuracy = accuracy;
		this.botType = botType;
	}
	
	public Shot findBestShot(TestPhysicsWorld state, Player currentPlayer) {
		switch (botType) {
		case MINIMAX:
			Debug.i("AIBotWrapper", "Calling minimax bot");
			return MiniMaxBot.getInstance().findBestShot2Player(state, currentPlayer, intelligence, accuracy);
		case GREEDYBOT:
			Debug.i("AIBotWrapper", "Calling greedybot bot");
			return GreedyBot.getInstance().findBestShot2Player(state, currentPlayer, intelligence, accuracy);
		default:
			return null;
		}
	}
	
	public boolean isShotSuccessful(TestPhysicsWorld state, VirtualGameState virtualState, Player currentPlayer, Shot shot) {
		switch (botType) {
		case MINIMAX:
			Debug.i("AIBotWrapper", "Calling minimax bot");
			//return MiniMaxBot.getInstance().isShotSuccessful(state, virtualState, currentPlayer, shot);
		case GREEDYBOT:
			Debug.i("AIBotWrapper", "Calling minimax bot");
			//return GreedyBot.getInstance().isShotSuccessful(state, virtualState, currentPlayer, shot);
		default:
			return false;
		}
	}
	
	
}
