package com.cslabs.knockout;

import org.andengine.util.debug.Debug;

import com.cslabs.knockout.scene.AbstractScene;
import com.cslabs.knockout.scene.CopyOfGameScene;
import com.cslabs.knockout.scene.GameScene;

public class SceneManager {

	private static final SceneManager INSTANCE = new SceneManager();

	private ResourceManager res = ResourceManager.getInstance();

	private AbstractScene currentScene;

	private SceneManager() {
	}

	public static SceneManager getInstance() {
		return INSTANCE;
	}

	public AbstractScene getCurrentScene() {
		return currentScene;
	}

	public void setCurrentScene(AbstractScene currentScene) {
		this.currentScene = currentScene;
		res.engine.setScene(currentScene);
		Debug.i("Current scene: " + currentScene.getClass().getName());
	}

	public void showGameScene() {
		// final AbstractScene previousScene = getCurrentScene();
		GameScene gameScene = new GameScene();
		gameScene.populate();
		// previousScene.destroy();

		setCurrentScene(gameScene);

	}

}
