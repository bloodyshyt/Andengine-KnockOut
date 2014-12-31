package com.cslabs.knockout.AI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import org.andengine.util.debug.Debug;

import com.badlogic.gdx.physics.box2d.Body;
import com.cslabs.knockout.entity.Player;
import com.cslabs.knockout.entity.PlayerNo;
import com.cslabs.knockout.entity.Utils;
import com.cslabs.knockout.entity.VirtualChecker;

public class MiniMaxBot extends AbstractAIBot {

	private static final String TAG = "MiniMaxBot";

	// single instance is created only once
	private static final MiniMaxBot INSTANCE = new MiniMaxBot();

	private Player maxPlayer;

	public static MiniMaxBot getInstance() {
		return INSTANCE;
	}

	@Override
	public Shot findBestShot2Player(GameState state, Player currentPlayer,
			final int pDepth, final int pAccuracy) {

		// generate shots
		ArrayList<Shot> shots = state.generateShots(currentPlayer,
				(AbstractAIBot) this);
		Shot bestShot = null;
		float bestScore = Float.MIN_VALUE, score;
		for (Shot shot : shots) {
			//score = minimax(state, currentPlayer, pDepth);
			score = ab(state, currentPlayer, pDepth, Float.MIN_VALUE, Float.MAX_VALUE);
			if (score > bestScore) {
				bestScore = score;
				bestShot = shot;
			}
		}
		return bestShot;

		/*
		 * int depth = pDepth; maxPlayer = currentPlayer;
		 * 
		 * Shot a = new Shot(0, null); a.setScore(Float.MIN_VALUE);
		 * 
		 * Shot b = new Shot(0, null); b.setScore(Float.MAX_VALUE);
		 * 
		 * Shot best = minimax(state, depth, currentPlayer, a, b); if (best ==
		 * null) Debug.i(TAG, "FUCK SHOT IS EMPTY"); //
		 * best.dumpInfo("GreedyBot");
		 * 
		 * // perturb shot // TODO migrate perturbShot code to shot class itself
		 * Utils.perturbShot(best, pAccuracy); return best;
		 */
	}

	public Shot minimax(GameState state, int depth, Player currentPlayer,
			Shot alpha, Shot beta) {

		Shot fuck;

		// Debug.i(TAG,
		// "minmax at depth " + depth + " for player " + currentPlayer
		// + " a:" + alpha.getScore() + " b:" + beta.getScore());

		if (depth == 0)
			return evaluate(state, currentPlayer);

		ArrayList<Shot> shots = state.generateShots(currentPlayer,
				(AbstractAIBot) this);
		if (shots.size() == 1)
			return shots.get(0);

		if (currentPlayer == maxPlayer) {
			// find max and store in alpha
			for (Shot shot : shots) {
				GameState nextState = state.simulate(shot);
				fuck = minimax(nextState, depth - 1,
						currentPlayer.getNextPlayer(), alpha, beta);
				float score = fuck.getScore();
				// Debug.i(TAG,"MiniMax shot data:");
				fuck.dumpInfo(TAG);
				if (score > alpha.getScore()) {
					alpha = fuck;
					// Debug.i(TAG, "Assigned new alpha of score:" +
					// alpha.getScore());
				}
				if (alpha.getScore() >= beta.getScore())
					return alpha; // cut off
			}
			return alpha;
		} else if (currentPlayer != maxPlayer) {
			// find min and store in beta
			for (Shot shot : shots) {
				GameState nextState = state.simulate(shot);
				fuck = minimax(nextState, depth - 1,
						currentPlayer.getNextPlayer(), alpha, beta);
				float score = fuck.getScore();
				if (score < beta.getScore()) {
					beta = fuck;
					// Debug.i(TAG, "Assigned new beta of score:" +
					// beta.getScore());
				}
				if (alpha.getScore() >= beta.getScore())
					return beta; // cut off
			}
			return alpha;
		}

		return null;
	}

	@Override
	public Shot evaluate(GameState state, Player currentPlayer) {
		ArrayList<Shot> shots = state.generateShots(currentPlayer,
				(AbstractAIBot) this);
		if (shots.size() == 0) {
			Debug.i("Game over!!!!!!!!!!!!!!!!!!");
			Shot fuck = new Shot(0, null);
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
		// Debug.i(TAG, "evaluating state for " + currentPlayer + " score of "
		// + bestShot.getScore());
		return bestShot;

	}

	public float evaluateShot(GameState state, Shot shot, Player currentPlayer) {

		int[] state1, state2;
		float x1, x2, y1, y2;
		double distance; // buffer variable to store current distance
		double sumOfdist = 0;

		GameState nextState = state.simulate(shot);
		shot.setNextGameState(nextState);

		state1 = state.getNumOfPlayerAndOpponentCheckers(currentPlayer);
		state2 = nextState.getNumOfPlayerAndOpponentCheckers(currentPlayer);

		int kills = state1[1] - state2[1];
		int deaths = state1[0] - state2[0];

		// calculate the sum of distances between each player checker and all
		// enemy checkers

		List<Body> listOfBodies = Utils.copyIterator(nextState.getBodies());
		for (int i = 0; i < listOfBodies.size(); i++) {
			VirtualChecker vc_i = null;
			if (!(listOfBodies.get(i).getUserData() instanceof VirtualChecker))
				continue;
			vc_i = (VirtualChecker) listOfBodies.get(i).getUserData();
			if (vc_i.getPlayer() != currentPlayer.playerNo)
				continue;
			for (int j = 0; j < listOfBodies.size(); j++) {
				VirtualChecker vc_j = null;
				if (!(listOfBodies.get(j).getUserData() instanceof VirtualChecker))
					continue;
				vc_j = (VirtualChecker) listOfBodies.get(j).getUserData();
				if (vc_j.getPlayer() == currentPlayer.playerNo)
					continue;
				Body bp = listOfBodies.get(i);
				Body bo = listOfBodies.get(j);

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
		// Debug.i(TAG, "shot has score of " + score);
		return score;
	}

	public void dumpInfo(Shot shot) {
		Debug.i(TAG, +shot.data[0] + " " + shot.data[1] + " " + shot.data[2]
				+ " " + shot.data[3] + " " + shot.data[4] + " " + shot.data[5]
				+ " " + shot.data[6] + " " + shot.data[7]);
	}

	@Override
	public boolean isShotSuccessful(GameState state1, Player currentPlayer,
			Shot shot) {
		float score = evaluateShot(state1, shot, currentPlayer);
		if (score > 0) {

			return true;
		}
		shot.setNextGameState(null);
		return false;
	}

	@Override
	public Shot findBestShotMultiPlayer(GameState state, Player currentPlayer,
			int depth, int pAccuracy) {
		// TODO Auto-generated method stub
		return null;
	}

	private float minimax(GameState state, Player currentPlayer, int depth) {
		if (depth == 1)
			return evaluate(state, currentPlayer).getScore();

		float max = Float.MIN_VALUE, min = Float.MAX_VALUE;

		ArrayList<Shot> shots = state.generateShots(currentPlayer,
				(AbstractAIBot) this);

		if (currentPlayer == maxPlayer) {
			for (Shot shot : shots)
				max = Math.max(
						max,
						minimax(shot.getNextGameState(),
								currentPlayer.getNextPlayer(), depth - 1));
			return max;
		} else {
			for (Shot shot : shots)
				min = Math.min(
						min,
						minimax(shot.getNextGameState(),
								currentPlayer.getNextPlayer(), depth - 1));
			return min;
		}
	}

	private float ab(GameState state, Player currentPlayer, int depth,
			float alpha, float beta) {
		if (depth == 1)
			return evaluate(state, currentPlayer).getScore();

		ArrayList<Shot> shots = state.generateShots(currentPlayer,
				(AbstractAIBot) this);

		Collections.sort(shots, Collections.reverseOrder());
		shots = (ArrayList<Shot>) shots.subList(0, 2);

		if (currentPlayer == maxPlayer) {
			for (Shot shot : shots) {
				alpha = Math.max(
						alpha,
						ab(shot.getNextGameState(),
								currentPlayer.getNextPlayer(), depth - 1,
								alpha, beta));
				if (alpha > beta)
					return alpha;
			}

			return alpha;
		}

		else {
			for (Shot shot : shots) {
				beta = Math.min(
						beta,
						ab(shot.getNextGameState(),
								currentPlayer.getNextPlayer(), depth - 1,
								alpha, beta));
				if (alpha > beta)
					return beta;
			}

			return beta;
		}

	}
}
