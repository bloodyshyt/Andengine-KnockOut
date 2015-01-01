package com.cslabs.knockout.AI;

import java.util.ArrayList;

import org.andengine.util.debug.Debug;

import com.cslabs.knockout.entity.Player;
import com.cslabs.knockout.entity.VirtualChecker;

public class VirtualGameState {
	public ArrayList<VirtualChecker> gameVirtualCheckers = new ArrayList<VirtualChecker>();
	public int nPlayer = 0, nOpponent = 0;

	public VirtualGameState(ArrayList<VirtualChecker> gameVirtualCheckers) {
		this.gameVirtualCheckers = gameVirtualCheckers;
	}

	public static void dumpinfo(VirtualGameState state) {
		for (VirtualChecker vc : state.gameVirtualCheckers) {
			Debug.i("VirtualGameState", "VC ID:" + vc.getID() + " \t x:" + vc.x
					+ "\t  y:" + vc.y + " \t alive? " + vc.isAlive());
		}
	}

	public int[] getNumOfPlayerAndOpponent(Player currentPlayer) {

		int[] lives = new int[] { 0, 0 };

		for (VirtualChecker vc : this.gameVirtualCheckers) {
			if (vc.isAlive()) {
				if (vc.getPlayer() == currentPlayer.playerNo) {
					lives[0]++;
				} else {
					lives[1]++;
				}
			}
		}

		return lives;
	}

	// doesnt seem to be called at all
	public boolean isGameOver(Player currentPlayer) {

		for (VirtualChecker vc : this.gameVirtualCheckers) {
			if (vc.isAlive() && vc.getPlayer() == currentPlayer.playerNo) 
				return false;
		}
		return true;
	}
}
