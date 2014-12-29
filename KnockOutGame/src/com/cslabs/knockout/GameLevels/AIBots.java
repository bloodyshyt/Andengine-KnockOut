package com.cslabs.knockout.GameLevels;

/**
 * This class holds an array of all of the bot definitions that can be played in
 * the game as well as helper methods to retrieve specific bots.
 * 
 *** @author Raynold Ng - cs labs
 **/
public class AIBots {

	// ====================================================
	// CONSTANTS
	// ====================================================

	private static final AIBotDef[] AvailableAIBots = new AIBotDef[] {
		new AIBotDef("Sally", AIBotTypes.GREEDYBOT, 3, 2),
		new AIBotDef("Bob", AIBotTypes.GREEDYBOT, 5, 1),
		new AIBotDef("Dick", AIBotTypes.GREEDYBOT, 4, 3)
	};

	// ====================================================
	// METHODS
	// ====================================================
	public static final AIBotDef getAIBotDef(final String pBotName) {
		for (AIBotDef curLevelDef : AvailableAIBots) {
			if (curLevelDef.doNamesMatch(pBotName))
				return curLevelDef;
		}
		return null;
	}

	// ====================================================
	// CLASSES
	// ====================================================
	public static class AIBotDef {
		public final String mBotName;
		public final AIBotTypes mAIBot;
		public final int mAccuracy;
		public final int mIntelligence;

		public AIBotDef(final String pBotName, final AIBotTypes pAIBot,
				final int pAccuracy, final int pIntelligence) {
			mBotName = pBotName;
			mAIBot = pAIBot;
			mAccuracy = pAccuracy;
			mIntelligence = pIntelligence;
		}

		public boolean doNamesMatch(final String pBotName) {
			if (mBotName == pBotName)
				return true;
			return false;
		}
	}
	
	public enum AIBotTypes {
		MINIMAX, GREEDYBOT, MONTE_CARLO, YOLO;
	}
}
