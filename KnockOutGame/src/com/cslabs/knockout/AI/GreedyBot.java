package com.cslabs.knockout.AI;

import java.util.ArrayList;
import java.util.List;

import org.andengine.util.debug.Debug;

import com.badlogic.gdx.physics.box2d.Body;
import com.cslabs.knockout.entity.Player;
import com.cslabs.knockout.entity.Utils;
import com.cslabs.knockout.entity.VirtualChecker;

public class GreedyBot extends AbstractAIBot {

	// single instance is created only once
	private static final GreedyBot INSTANCE = new GreedyBot();

	private TestPhysicsWorld world;
	private VirtualGameState initialState;
	private Player currentPlayer;

	@Override
	public Shot findBestShot2Player(TestPhysicsWorld state, Player currentPlayer, int depth,
			int pAccuracy) {
		world = state;
		initialState = world.extractState();
		this.currentPlayer = currentPlayer;

		ArrayList<Shot> shots = state.generateShots(currentPlayer);
		Shot bestShot = null;
		float bestScore = Float.MIN_VALUE;
		float score;
		for (Shot shot : shots) {
			score = evaluateShot(world, shot);
			if(score > bestScore) {
				bestScore = score;
				bestShot = shot;
			}
		}
		
		return bestShot;
	}

	@Override
	public Shot findBestShotMultiPlayer(TestPhysicsWorld state,int depth, int pAccuracy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float evaluate(TestPhysicsWorld state, VirtualGameState virtualState,
			Player currentPlayer) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isShotSuccessful(TestPhysicsWorld state,Player currentPlayer, Shot shot) {
		// TODO Auto-generated method stub
		return false;
	}

	private float evaluateShot(TestPhysicsWorld world, Shot shot) {
		VirtualGameState nextState = world.simulate(shot);

		int[] state1 = initialState.getNumOfPlayerAndOpponent(currentPlayer);
		int[] state2 = nextState.getNumOfPlayerAndOpponent(currentPlayer);

		int deaths = state1[0] - state2[0];
		int kills = state1[1] - state2[1];
		float sumDist = 0;
		ArrayList<VirtualChecker> vcs = nextState.gameVirtualCheckers;
		for (int i = 0; i < vcs.size(); i++) {
			for (int j = 1; j < vcs.size(); j++) {
				if (vcs.get(i).getPlayer() == currentPlayer.playerNo
						&& vcs.get(j).getPlayer() != currentPlayer.playerNo) {
					VirtualChecker a = vcs.get(i);
					VirtualChecker b = vcs.get(j);
					
					sumDist += Utils.calculateDistance(a.x, a.y, b.x, b.y);
				}
			}
		}
		
		return (float) (800 * kills - 1000 * deaths - 0.1 * sumDist);

	}

	public static GreedyBot getInstance() {
		return INSTANCE;
	}

}
