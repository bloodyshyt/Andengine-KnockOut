package com.cslabs.knockout.AI;

import java.util.ArrayList;

import org.andengine.util.debug.Debug;

import com.badlogic.gdx.physics.box2d.Body;
import com.cslabs.knockout.entity.PlayerNo;
import com.cslabs.knockout.entity.VirtualChecker;
import com.cslabs.knockout.scene.GameScene;
import com.cslabs.knockout.scene.GameScene.WorldState;
import com.cslabs.knockout.utils.Stopwatch;

public class GreedyBotDepth2 extends AbstractAIBot {


	/*
	// original 

	private static final String TAG = "GreedyBot";

	// single instance is created only once
	private static final GreedyBotDepth2 INSTANCE = new GreedyBotDepth2();

	public static GreedyBotDepth2 getInstance() {
		return INSTANCE;
	}

	@Override
	public Shot findBestShot(GameState state, int depth) {
		Stopwatch timer = new Stopwatch();

		ArrayList<Shot> shots = state.GenerateShotsForPlayer(state);

		// for now only depth 1 simulation
		Shot bestShot = shots.get(0);
		float bestScore = bestShot.getScore();
		// generate shots
		for (Shot shot : shots) {
			float score = evaluate(state, shot);
			if (score > bestScore) {
				bestScore = score;
				bestShot = shot;
			}
		}

		bestShot.dumpInfo(TAG);

		Debug.i(TAG, state.player + " best shot from " + shots.size()
				+ " with score: " + bestScore + " in " + timer.elapsedTime() + "");
		Debug.i(TAG, "Shot data: " + bestShot.data[0] + " " + bestShot.data[1]
				+ " " + bestShot.data[2] + " " + bestShot.data[3] + " "
				+ bestShot.data[4] + " " + bestShot.data[5] + " "
				+ bestShot.data[6] + " " + bestShot.data[7]);

		return bestShot;
	}
	

	@Override
	public float evaluate(GameState state, Shot shot) {

		int p1, p2, o1, o2; // no of player and enemy pieces

		float x1, x2, y1, y2;
		double distance;

		double sumOfdist = 0;
		state.updateNoOfCheckers();
		p1 = state.numOfPlayerCheckers;
		o1 = state.numOfOpponentCheckers;

		// run the simulation
		GameState nextState = state.simulate(shot);
		nextState.updateNoOfCheckers();
		p2 = nextState.numOfPlayerCheckers;
		o2 = nextState.numOfOpponentCheckers;

		// calculate the sum of distances between each player checker and all
		// enemy checkers
		for (Body bp : nextState.playerCheckers) {
			for (Body bo : nextState.opponentCheckers) {
				x1 = bp.getPosition().x;
				y1 = bp.getPosition().y;
				x2 = bo.getPosition().x;
				y2 = bo.getPosition().y;
				distance = Math.sqrt(Math.pow((x1 - x2), 2)
						+ Math.pow((y1 - y2), 2));
				sumOfdist += distance;
			}
		}
		int kills = o1 - o2;
		int deaths = p1 - p2;
		float score = (float) (800 * kills - 1000 * deaths - 0.1 * sumOfdist);

		shot.data[0] = p1;
		shot.data[1] = p2;
		shot.data[2] = o1;
		shot.data[3] = o2;
		shot.data[4] = kills;
		shot.data[5] = deaths;
		shot.data[6] = (float) sumOfdist;
		shot.data[7] = score;
		
		shot.setScore(score);

		return score;
	}
*/

	@Override
	public Shot evaluate(GameState state, PlayerNo currentPlayer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Shot findBestShot(GameState state, PlayerNo currentPlayer) {
		// TODO Auto-generated method stub
		return null;
	}}
