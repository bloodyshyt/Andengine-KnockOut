package com.cslabs.knockout.GameLevels;

import com.cslabs.knockout.entity.PlayerNo;

/**
 * This class holds an array of all of the levels that can be played in the game
 * as well as helper methods to retrieve specific levels.
 * 
 *** @author Raynold Ng - cs labs
 **/
public class Levels {
	// ====================================================
	// CONSTANTS
	// ====================================================

	private static final LevelDef[] AvailableLevels = new LevelDef[] { new LevelDef(
			1, 1, new PlatformDef(new float[] { 80, 100, 0, 400, 100, 0, 400,
					700, 0, 80, 700, 0}), new CheckerDef[] {
					new CheckerDef(140, 500, PlayerNo.P1),
					new CheckerDef(240, 500, PlayerNo.P1),
					new CheckerDef(340, 500, PlayerNo.P1) }, new CheckerDef[] {
					new CheckerDef(140, 300, PlayerNo.P2),
					new CheckerDef(240, 300, PlayerNo.P2),
					new CheckerDef(340, 300, PlayerNo.P2) }, null, null) };

	// ====================================================
	// METHODS
	// ============================================= =======
	public static final LevelDef getLevelDef(final int pLevelIndex) {
		for (LevelDef curLevelDef : AvailableLevels) {
			if (curLevelDef.doIndicesMatch(pLevelIndex))
				return curLevelDef;
		}
		return null;
	}

	// ====================================================
	// CLASSES
	// ====================================================
	public static class LevelDef {
		public final int mLevelIndex, mWorldIndex;
		public final PlatformDef mPlatformDef;
		public final CheckerDef[] mP1Checkers;
		public final CheckerDef[] mP2Checkers;
		public final CheckerDef[] mP3Checkers;
		public final CheckerDef[] mP4Checkers;

		public LevelDef(final int pLevelIndex, final int pWorldIndex,
				PlatformDef platformDef, CheckerDef[] P1, CheckerDef[] P2,
				CheckerDef[] P3, CheckerDef[] P4) {
			mLevelIndex = pLevelIndex;
			mWorldIndex = pWorldIndex;
			mPlatformDef = platformDef;
			mP1Checkers = P1;
			mP2Checkers = P2;
			mP3Checkers = P3;
			mP4Checkers = P4;
		}

		public boolean doIndicesMatch(final int pLevelIndex) {
			if (mLevelIndex == pLevelIndex)
				return true;
			return false;
		}
	}

	public static class CheckerDef {

		public final float mX, mY;
		public final PlayerNo playerNo;

		public CheckerDef(final float pX, final float pY, final PlayerNo player) {
			mX = pX;
			mY = pY;
			playerNo = player;
		}
	}

	public static class PlatformDef {
		public final float[] coords;

		public PlatformDef(final float[] coords) {
			this.coords = coords;
		}
	}
}
