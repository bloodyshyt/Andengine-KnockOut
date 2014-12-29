package com.cslabs.knockout.GameLevels;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import com.cslabs.knockout.Managers.SceneManager;
import com.cslabs.knockout.GameLevels.Levels.CheckerDef;
import com.cslabs.knockout.GameLevels.Levels.LevelDef;
import com.cslabs.knockout.GameLevels.Levels.PlatformDef;
import com.cslabs.knockout.entity.Checker;
import com.cslabs.knockout.entity.Platform;
import com.cslabs.knockout.entity.PlayerNo;
import com.cslabs.knockout.factory.CheckerFactory;
import com.cslabs.knockout.factory.PlatformFactory;
import com.cslabs.knockout.scene.GameScene;

public class LevelLoader {
//
//
//	// variables
//	private GameScene gameScene = GameScene.getInstance();
//	private FixedStepPhysicsWorld physicsWorld;
//	private VertexBufferObjectManager vbom;
//	private int p1index, p2index, p3index, p4index;
//
//	public void loadLevel(final int pLevelIndex,
//			FixedStepPhysicsWorld pPhysicsWorld, VertexBufferObjectManager pVbom) {
//		physicsWorld = pPhysicsWorld;
//		vbom = pVbom;
//
//		p1index = 100;
//		p2index = 200;
//		p3index = 300;
//		p4index = 400;
//
//		CheckerFactory.getInstance().create(physicsWorld, vbom);
//		PlatformFactory.getInstance().create(physicsWorld, vbom);
//
//		// get LevelDef object
//		LevelDef currentLevel = Levels.getLevelDef(pLevelIndex);
//
//		// load player checkers
//		if (currentLevel.mP1Checkers != null) {
//			for (CheckerDef cDef : currentLevel.mP1Checkers) {
//				addChecker(cDef.mX, cDef.mY, PlayerNo.P1, p1index++);
//			}
//		}
//
//		if (currentLevel.mP2Checkers != null) {
//			for (CheckerDef cDef : currentLevel.mP2Checkers) {
//				addChecker(cDef.mX, cDef.mY, PlayerNo.P2, p2index++);
//			}
//		}
//
//		if (currentLevel.mP3Checkers != null) {
//			for (CheckerDef cDef : currentLevel.mP3Checkers) {
//				addChecker(cDef.mX, cDef.mY, PlayerNo.P3, p3index++);
//			}
//		}
//
//		if (currentLevel.mP4Checkers != null) {
//			for (CheckerDef cDef : currentLevel.mP4Checkers) {
//				addChecker(cDef.mX, cDef.mY, PlayerNo.P4, p4index++);
//			}
//		}
//
//		// load platform
//		PlatformDef currPlatform = currentLevel.mPlatformDef;
//		addPlatform(currPlatform.coords);
//	}
//
//	// helper methods
//	private void addChecker(float x, float y, PlayerNo playerNo, int ID) {
//		Checker piece = CheckerFactory.getInstance().createPlayingPiece(x, y,
//				playerNo, ID);
//		piece.setVisible(true);
//		//GameScene.getInstance().registerTouchArea(piece);
//		//SceneManager.getInstance().getCurrentScene().attachChild(piece);
//		GameScene.getInstance().gameCheckers.add(piece);
//		Rectangle rect = new Rectangle(100, 100, 50, 50, vbom);
//		rect.setColor(Color.BLACK);
//		Debug.i("TEST", SceneManager.getInstance().getCurrentScene() + " and " + gameScene);
//	}
//
//	private Platform addPlatform(float[] platform_cords2) {
//		Platform platform = PlatformFactory.getInstance().createPlatform(
//				platform_cords2);
//		gameScene.attachChild(platform);
//		return platform;
//	}

}
