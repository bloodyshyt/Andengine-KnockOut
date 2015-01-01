package com.cslabs.knockout.AI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.andengine.util.debug.Debug;

import com.cslabs.knockout.entity.Player;
import com.cslabs.knockout.entity.Utils;
import com.cslabs.knockout.entity.VirtualChecker;

public class MiniMaxBot extends AbstractAIBot {

	// single instance is only created once
	private static final MiniMaxBot INSTANCE = new MiniMaxBot();
	private static final String TAG = "MiniMaxBot";

	private TestPhysicsWorld world;
	private VirtualGameState initialState;
	private Player maxPlayer;

	@Override
	public float evaluate(TestPhysicsWorld state,
			VirtualGameState currentState, Player currentPlayer) {
		
		ArrayList<Shot> possibleMoves = world.generateShots(currentState, currentPlayer);
		List<Shot> sortedMoves = filterMoves(currentState, currentPlayer, possibleMoves, 3);
		
		float score = 0.0f;
		
		for(Shot shot : sortedMoves) {
			score += evaluateShot(currentState, shot, currentPlayer);
		}
		
		//Debug.i(TAG, "Evaluating state returned score of " + score);
		
		return score;

	}

	@Override
	public Shot findBestShot2Player(TestPhysicsWorld state,
			Player currentPlayer, int depth, int pAccuracy) {

		world = state;
		initialState = world.extractState();
		this.maxPlayer = currentPlayer;

		ArrayList<Shot> shots = world.generateShots(currentPlayer);
		List<Shot> filteredShots = filterMoves(initialState, currentPlayer, shots, 3);

		Shot bestShot = null;
		float bestScore = Float.MIN_VALUE;
		float score;
		
		Debug.i(TAG, "Expanding on " + filteredShots.size() + " shots");
		
		for (Shot shot : filteredShots) {
			// for each shot create a minimax tree
			score = minimax(initialState, currentPlayer, depth);
			
			Debug.i(TAG, "Score of " + score);
			
			if (score > bestScore) {
				bestScore = score;
				bestShot = shot;
			}
		}

		if(bestShot != null) bestShot.dumpInfo();
		return bestShot;
	}

	private float minimax(VirtualGameState currentState, Player currentPlayer,
			int depth) {
		
		//Debug.i(TAG, "In minimax depth " + depth + " player " + currentPlayer.playerNo);
		

		if (depth == 1)
			return evaluate(world, currentState, currentPlayer);
		if(currentState.isGameOver(currentPlayer)) {
			Debug.i(TAG, "Game is OVER!");
			return Float.MIN_VALUE;
		}
			

		float max = Float.MIN_VALUE, min = Float.MAX_VALUE;

		ArrayList<Shot> shots = world
				.generateShots(currentState, currentPlayer);
		List<Shot> filterdShots = filterMoves(currentState, currentPlayer, shots, 3);
		if(filterdShots.size() == 0) return Float.MIN_VALUE;
		
		if (currentPlayer == maxPlayer) {
			for (Shot shot : filterdShots)
				max = Math.max(
						max,
						minimax(shot.getNextState(),
								currentPlayer.getNextPlayer(), depth - 1));
			return max;
		} else {
			for (Shot shot : filterdShots)
				min = Math.min(
						min,
						minimax(shot.getNextState(),
								currentPlayer.getNextPlayer(), depth - 1));
			return min;
		}

	}

	@Override
	public Shot findBestShotMultiPlayer(TestPhysicsWorld state, int depth,
			int pAccuracy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isShotSuccessful(TestPhysicsWorld state,
			Player currentPlayer, Shot shot) {
		// TODO Auto-generated method stub
		return false;
	}

	public float evaluateShot(VirtualGameState currentState, Shot shot,
			Player currentPlayer) {

		// run simulation
		VirtualGameState nextState = world.simulate(currentState, shot);

		// pass reference of nextState into Shot object
		shot.setNextState(nextState);

		// currnetly we use the same heuristic as greedybot
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
		
		//Debug.i(TAG, "Before " + state1[0] + " " + state1[1] + " After " + state2[0] + " " + state2[1]);

		return (float) (800 * kills - 1000 * deaths - 0.1 * sumDist);
	}

	private List<Shot> filterMoves(VirtualGameState currentState,
			Player currentPlayer, ArrayList<Shot> shots, int length) {
		ArrayList<Shot> filteredMoves = new ArrayList<Shot>();
		final float scoreThreshold = 0;
		for (Shot shot : shots) {
			if (evaluateShot(currentState, shot, currentPlayer) > scoreThreshold) {
				filteredMoves.add(shot);
			}
		}

		Debug.i(TAG, "Before filterMoves() " + shots.size() + " after "
				+ filteredMoves.size());

		Collections.sort(filteredMoves, Collections.reverseOrder());

		if(filteredMoves.size() < length) return (List<Shot>) filteredMoves;
		
		return filteredMoves.subList(0, length);
	}

	public static MiniMaxBot getInstance() {
		return INSTANCE;
	}
}
