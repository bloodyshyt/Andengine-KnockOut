package com.cslabs.knockout.GameLevels;

import java.util.ArrayList;

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

	private static final LevelDef[] AvailableLevels = new LevelDef[] {
			new LevelDef(1, 1, new PlatformDef(new float[] { 80, 100, 0, 400,
					100, 0, 400, 700, 0, 80, 700, 0 }), new CheckerDef[] {
					new CheckerDef(140, 600, PlayerNo.P1),
					new CheckerDef(240, 600, PlayerNo.P1),
					new CheckerDef(340, 600, PlayerNo.P1),
					new CheckerDef(190, 500, PlayerNo.P1),
					new CheckerDef(290, 500, PlayerNo.P1) }, new CheckerDef[] {
					new CheckerDef(140, 200, PlayerNo.P2),
					new CheckerDef(240, 200, PlayerNo.P2),
					new CheckerDef(340, 200, PlayerNo.P2),
					new CheckerDef(190, 300, PlayerNo.P2),
					new CheckerDef(290, 300, PlayerNo.P2) }, null, null),

			new LevelDef(1, 2, new PlatformDef(new float[] { 10, 10, 0, 470,
					10, 0, 470, 790, 0, 10, 790, 0 }), new CheckerDef[] {
					new CheckerDef(30, 30, PlayerNo.P1),
					new CheckerDef(30, 60, PlayerNo.P1),
					new CheckerDef(60, 30, PlayerNo.P1),
					new CheckerDef(450, 30, PlayerNo.P1),
					new CheckerDef(450, 60, PlayerNo.P1),
					new CheckerDef(420, 30, PlayerNo.P1) }, new CheckerDef[] {
					new CheckerDef(30, 770, PlayerNo.P2),
					new CheckerDef(30, 740, PlayerNo.P2),
					new CheckerDef(60, 770, PlayerNo.P2),
					new CheckerDef(450, 770, PlayerNo.P2),
					new CheckerDef(450, 740, PlayerNo.P2),
					new CheckerDef(420, 770, PlayerNo.P2) }, null, null),

			new LevelDef(1, 3, new PlatformDef(new float[] { 240, 800, 0, 480,
					400, 0, 240, 0, 0, 0, 400, 0 }), new CheckerDef[] {
					new CheckerDef(240, 770, PlayerNo.P1),
					new CheckerDef(210, 700, PlayerNo.P1),
					new CheckerDef(270, 700, PlayerNo.P1),
					new CheckerDef(255, 650, PlayerNo.P1),
					new CheckerDef(255, 650, PlayerNo.P1) }, new CheckerDef[] {
					new CheckerDef(240, 30, PlayerNo.P2),
					new CheckerDef(210, 100, PlayerNo.P2),
					new CheckerDef(270, 100, PlayerNo.P2),
					new CheckerDef(255, 150, PlayerNo.P2),
					new CheckerDef(255, 150, PlayerNo.P2) }, null, null) };

	// ====================================================
	// METHODS
	// ============================================= =======
	public static final LevelDef getLevelDef(final int pWorldIndex,
			final int pLevelIndex) {
		for (LevelDef curLevelDef : AvailableLevels) {
			if (curLevelDef.doIndicesMatch(pWorldIndex, pLevelIndex))
				return curLevelDef;
		}
		return null;
	}
	
	public static final LevelDef[] getLevelDefofWorld(final int pWorldIndex) {
		ArrayList<LevelDef> levels = new ArrayList<Levels.LevelDef>();
		for (LevelDef curLevelDef : AvailableLevels) 
			if(curLevelDef.mWorldIndex == pWorldIndex) levels.add(curLevelDef);
		LevelDef[] a = new LevelDef[levels.size()];
		a = levels.toArray(a);
		return a;
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

		public LevelDef(final int pWorldIndex, final int pLevelIndex,
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

		public boolean doIndicesMatch(final int pWorldIndex,
				final int pLevelIndex) {
			if (mWorldIndex == pWorldIndex && mLevelIndex == pLevelIndex)
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

	/*
	 * new LevelDef(1, 1, new PlatformDef(new float[] { 80, 100, 0, 400, 100, 0,
	 * 400, 700, 0, 80, 700, 0 }), new CheckerDef[] { new CheckerDef(140, 600,
	 * PlayerNo.P1), new CheckerDef(240, 600, PlayerNo.P1), new CheckerDef(340,
	 * 600, PlayerNo.P1), new CheckerDef(190, 500, PlayerNo.P1), new
	 * CheckerDef(290, 500, PlayerNo.P1) }, new CheckerDef[] { new
	 * CheckerDef(140, 200, PlayerNo.P2), new CheckerDef(240, 200, PlayerNo.P2),
	 * new CheckerDef(340, 200, PlayerNo.P2), new CheckerDef(190, 300,
	 * PlayerNo.P2), new CheckerDef(290, 300, PlayerNo.P2) }, null, null),
	 * 
	 * new LevelDef(2, 1, new PlatformDef(new float[] { 240, 750, 0, 425, 700,
	 * 0, 425, 200, 0, 240, 50, 0, 65, 100, 0, 65, 700, 0 }), new CheckerDef[] {
	 * new CheckerDef(140, 500, PlayerNo.P1), new CheckerDef(240, 500,
	 * PlayerNo.P1), new CheckerDef(340, 500, PlayerNo.P1) }, new CheckerDef[] {
	 * new CheckerDef(140, 400, PlayerNo.P2), new CheckerDef(240, 400,
	 * PlayerNo.P2), new CheckerDef(340, 400, PlayerNo.P2) }, new CheckerDef[] {
	 * new CheckerDef(140, 300, PlayerNo.P3), new CheckerDef(240, 300,
	 * PlayerNo.P3), new CheckerDef(340, 300, PlayerNo.P3) }, null), new
	 * LevelDef(3, 1, new PlatformDef(new float[] { 80, 100, 0, 400, 100, 0,
	 * 400, 700, 0, 80, 700, 0 }), new CheckerDef[] { new CheckerDef(140, 600,
	 * PlayerNo.P1), new CheckerDef(240, 600, PlayerNo.P1), new CheckerDef(340,
	 * 600, PlayerNo.P1), new CheckerDef(190, 600, PlayerNo.P1), new
	 * CheckerDef(290, 600, PlayerNo.P1) }, new CheckerDef[] { new
	 * CheckerDef(140, 200, PlayerNo.P2), new CheckerDef(240, 200, PlayerNo.P2),
	 * new CheckerDef(340, 200, PlayerNo.P2), new CheckerDef(190, 200,
	 * PlayerNo.P2), new CheckerDef(290, 200, PlayerNo.P2) }, null, null), new
	 * LevelDef(4, 1, new PlatformDef(new float[] { 80, 100, 0, 400, 100, 0,
	 * 400, 700, 0, 80, 700, 0 }), new CheckerDef[] { new CheckerDef(382, 167,
	 * PlayerNo.P1), new CheckerDef(90, 192, PlayerNo.P1), new CheckerDef(210,
	 * 315, PlayerNo.P1), new CheckerDef(254, 158, PlayerNo.P1), new
	 * CheckerDef(234, 388, PlayerNo.P1) }, new CheckerDef[] { new
	 * CheckerDef(382, 257, PlayerNo.P2), new CheckerDef(302, 359, PlayerNo.P2),
	 * new CheckerDef(354, 268, PlayerNo.P2), new CheckerDef(246, 600,
	 * PlayerNo.P2), new CheckerDef(156, 580, PlayerNo.P2) }, null, null)
	 */
}
