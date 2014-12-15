package com.cslabs.knockout.AI;

import java.util.ArrayList;
import java.util.Collections;

import org.andengine.util.debug.Debug;

import com.badlogic.gdx.physics.box2d.Body;
import com.cslabs.knockout.entity.PlayerNo;
import com.cslabs.knockout.entity.VirtualChecker;

public class GreedyBot extends AbstractAIBot {

	private static final String TAG = "GreedyBot";

	// single instance is created only once
	private static final GreedyBot INSTANCE = new GreedyBot();

	private PlayerNo player, opponent;

	public static GreedyBot getInstance() {
		return INSTANCE;
	}

	@Override
	public Shot findBestShot(GameState state, PlayerNo currentPlayer) {

		int depth = 1;

		// get the current player
		player = currentPlayer; // maximizing player
		opponent = (player == PlayerNo.P1) ? PlayerNo.P2 : PlayerNo.P1; // minimizing
																		// player
		return minimax(state, depth, player);
	}

	public Shot minimax(GameState state, int depth, PlayerNo currentPlayer) {
		
		Debug.i(TAG, "minmax at depth " + depth + " for player " + currentPlayer);

		if (depth == 0 || state.gameOver(currentPlayer))
			return evaluate(state, currentPlayer);

		Shot bestShot = null;
		float bestScore = (currentPlayer == player) ? Float.MIN_VALUE
				: Float.MAX_VALUE;

		ArrayList<Shot> shots = state.generateShots(currentPlayer);
		if(shots.size() == 1) return shots.get(0); 
		for (Shot shot : shots) {
			Debug.i(TAG, "going down!");
			GameState nextState = state.simulate(shot);
			float score = (currentPlayer == player) ? minimax(nextState,
					depth - 1, opponent).getScore() : minimax(nextState,
					depth - 1, opponent).getScore();
			if (currentPlayer == player) {
				if (score > bestScore) {
					bestScore = score;
					bestShot = shot;
				}
			} else if (currentPlayer == opponent) {
				bestScore = score;
				bestShot = shot;
			}
		}

		return bestShot;
	}

	@Override
	public Shot evaluate(GameState state, PlayerNo currentPlayer) {
		Debug.i(TAG,
				"Calling evaluating state and dumping state info for player "
						+ currentPlayer);
		ArrayList<Shot> shots = state.generateShots(currentPlayer);
		if(shots.size() == 0) {
			Debug.i("Game over!!!!!!!!!!!!!!!!!!");
			Shot fuck =  new Shot(0, null);
			fuck.setScore(Float.MIN_VALUE);
			return fuck;
		}

		Shot bestShot = shots.get(0);
		float bestScore = Float.MIN_VALUE;
		for (Shot shot : shots) {
			float score = evaluateShot(state, shot, currentPlayer);
			if (score > bestScore) {
				bestScore = score;
				bestShot = shot;
			}
		}
		return bestShot;
	}

	public float evaluateShot(GameState state, Shot shot,
			PlayerNo currentPlayer) {

		int[] state1, state2;
		float x1, x2, y1, y2;
		double distance; // buffer variable to store current distance
		double sumOfdist = 0;

		GameState nextState = state.simulate(shot);

		state1 = state.returnNumOfPlayerCheckers(currentPlayer);
		state2 = nextState.returnNumOfPlayerCheckers(currentPlayer);
		
		//state.dumpInfo("GameScene");
		//nextState.dumpInfo("GameScene");
		//Debug.i(state1[0] + " " + state1[1] + " " + state2[0] +  " " + state2[1]);

		int kills = state1[1] - state2[1];
		int deaths = state1[0] - state2[0];

		// calculate the sum of distances between each player checker and all
		// enemy checkers
		for (VirtualChecker pvc : nextState.playerCheckers) {
			for (VirtualChecker ovc : nextState.opponentCheckers) {
				Body bp = pvc.getBody();
				Body bo = ovc.getBody();

				x1 = bp.getPosition().x;
				y1 = bp.getPosition().y;
				x2 = bo.getPosition().x;
				y2 = bo.getPosition().y;
				distance = Math.sqrt(Math.pow((x1 - x2), 2)
						+ Math.pow((y1 - y2), 2));
				sumOfdist += distance;
			}
		}
		float score = (float) (800 * kills - 1000 * deaths - 0.1 * sumOfdist);

		shot.data[0] = state1[0];
		shot.data[1] = state1[1];
		shot.data[2] = state2[0];
		shot.data[3] = state2[1];
		shot.data[4] = kills;
		shot.data[5] = deaths;
		shot.data[6] = (float) sumOfdist;
		shot.data[7] = score;

		shot.setScore(score);
		return score;

	}

	public void dumpInfo(Shot shot) {
		Debug.i(TAG, +shot.data[0] + " " + shot.data[1] + " " + shot.data[2]
				+ " " + shot.data[3] + " " + shot.data[4] + " " + shot.data[5]
				+ " " + shot.data[6] + " " + shot.data[7]);
	}

}
