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

		ArrayList<Shot> possibleMoves = world.generateShots(currentState,
				currentPlayer);
		List<Shot> sortedMoves = filterMoves(currentState, currentPlayer,
				possibleMoves, 3);
		int[] lives = currentState.getNumOfPlayerAndOpponent(currentPlayer);
		 
		float score = 0.0f;

		for (Shot shot : sortedMoves) {
			score += evaluateShot(currentState, shot, currentPlayer);
		}

		
		score += 1000 * lives[0] - 600 * lives[1];
		
		Debug.i(TAG, "Evaluating state returned score of " + score + " with life of " + lives[0] + " " + lives[1]);
		
		return  score;

	}

	@Override
	public Shot findBestShot2Player(TestPhysicsWorld state,
			Player currentPlayer, int depth, int pAccuracy) {

		world = state;
		initialState = world.extractState();
		this.maxPlayer = currentPlayer;

		ArrayList<Shot> shots = world.generateShots(currentPlayer);
		List<Shot> filteredShots = filterMoves(initialState, currentPlayer,
				shots, 3);

		Shot bestShot = null;
		float bestScore = Float.MIN_VALUE;
		float score = Float.MIN_VALUE;

		Debug.i(TAG, "Expanding on " + filteredShots.size() + " shots");

		for (Shot shot : filteredShots) {
			// for each shot create a minimax tree
			score = minimax(world.simulate(initialState, shot), currentPlayer.getNextPlayer(), depth - 1);

			//Debug.i(TAG, "Score of " + score);

			if (score >= bestScore) {
				bestScore = score;
				bestShot = shot;
				Debug.i(TAG, "Found a better shot");
			}
		}

		if (bestShot == null) 
			Debug.i(TAG, "WTF findbestshot returned nothing!");
		Utils.perturbShot(bestShot, pAccuracy);
		return bestShot;
	}

	private float minimax(VirtualGameState currentState, Player currentPlayer,
			int depth) {

		int lives[] = currentState.getNumOfPlayerAndOpponent(currentPlayer);
		
		Debug.i(TAG, "In minimax depth " + depth + " player " +
		currentPlayer.playerNo + " with " + lives[0] + " and opponent " + lives[1]);

		if(lives[0] == 0) return (currentPlayer == maxPlayer) ? Float.MIN_VALUE : Float.MAX_VALUE;
		if (depth == 1)
			return evaluate(world, currentState, maxPlayer);

		float max = Float.MIN_VALUE, min = Float.MAX_VALUE;

		ArrayList<Shot> shots = world
				.generateShots(currentState, currentPlayer);
		List<Shot> filterdShots = filterMoves(currentState, currentPlayer,
				shots, 3);
			
		if (currentPlayer == maxPlayer) {
			for (Shot shot : filterdShots) {

				max = Math.max(
						max,
						minimax(shot.getNextState(),
								currentPlayer.getNextPlayer(), depth - 1));
			}
			return max;
		} else {
			for (Shot shot : filterdShots) {

				if (shot.getNextState().isGameOver(
						currentPlayer.getNextPlayer())) {
					// this move kills of the player's checkers, winning shot
					Debug.i("Opponent has found the winning shot!!!!!!!!!!!!!");
					return Float.MIN_VALUE;
				}
				min = Math.min(
						min,
						minimax(shot.getNextState(),
								currentPlayer.getNextPlayer(), depth - 1));
			}
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
		int[] state1 = currentState.getNumOfPlayerAndOpponent(currentPlayer);
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

		// Debug.i(TAG, "Before " + state1[0] + " " + state1[1] + " After " +
		// state2[0] + " " + state2[1]);
		
		float score = (float) (800 * kills - 1000 * deaths + 0.1 * sumDist);
		shot.setScore(score);
		
		//Debug.i(TAG, "score of " + score);
		return score;
	}

	private List<Shot> filterMoves(VirtualGameState currentState,
			Player currentPlayer, ArrayList<Shot> shots, int length) {

		for (Shot shot : shots) 
			evaluateShot(currentState, shot, currentPlayer); 


		Collections.sort(shots, Collections.reverseOrder());

		if (shots.size() < length)
			return (List<Shot>) shots;

		return shots.subList(0, length);
	}

	public static MiniMaxBot getInstance() {
		return INSTANCE;
	}
}
