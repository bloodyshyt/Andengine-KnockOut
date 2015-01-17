package com.cslabs.knockout.Managers;

import java.io.IOException;

import org.andengine.util.debug.Debug;

import android.app.Activity;
import android.os.AsyncTask;

import com.cslabs.knockout.scene.AbstractScene;
import com.cslabs.knockout.scene.GameScene;
import com.cslabs.knockout.scene.LoadingScene;
import com.cslabs.knockout.scene.MenuSceneWrapper;
import com.cslabs.knockout.scene.PlayerSelectionMenu;
import com.cslabs.knockout.scene.SplashScene;

public class SceneManager {

	private static final String TAG = "SceneManager";

	private static final SceneManager INSTANCE = new SceneManager();
	public static final long SPLASH_DURATION = 200;

	private ResourceManager res = ResourceManager.getInstance();

	Activity activity = ResourceManager.getInstance().activity;

	private AbstractScene currentScene;
	private LoadingScene loadingScene = null;

	private SceneManager() {
	}

	public static SceneManager getInstance() {
		return INSTANCE;
	}

	public AbstractScene getCurrentScene() {
		return currentScene;
	}

	public AbstractScene showSplashAndMenuScene() {
		final SplashScene splashScene = new SplashScene();
		splashScene.populate();
		Debug.i(TAG, "splash scene populated");
		setCurrentScene(splashScene);
		Debug.i(TAG, "current scene is splash scene");

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				long timestamp = System.currentTimeMillis();
				res.loadFont();
				res.loadGameAudio();
				res.loadGameGraphics();

				loadingScene = new LoadingScene();
				loadingScene.populate();

				AbstractScene nextScene = new MenuSceneWrapper();

				if (System.currentTimeMillis() - timestamp < SPLASH_DURATION) {
					try {
						Thread.sleep(SPLASH_DURATION
								- (System.currentTimeMillis() - timestamp));
					} catch (InterruptedException e) {
						Debug.e("Interrupted", e);
					}
				}
				nextScene.populate();
				setCurrentScene(nextScene);
				splashScene.destroy();
				res.unloadSplashGraphics();
			}
		});

		return splashScene;
	}

	public void showMenuScene() {
		final AbstractScene previousScene = getCurrentScene();
		setCurrentScene(loadingScene);

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				MenuSceneWrapper menuSceneWrapper = new MenuSceneWrapper();
				menuSceneWrapper.populate();
				setCurrentScene(menuSceneWrapper);
				previousScene.destroy();
				return null;
			}

		}.execute();

	}
	
	public void showPlayerSelectionScene() {
		final AbstractScene previousScene = getCurrentScene();
		setCurrentScene(loadingScene);
		
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				res.loadPlayerSelectionGraphics();
				PlayerSelectionMenu playerSelectionMenu = new PlayerSelectionMenu();
				playerSelectionMenu.populate();
				setCurrentScene(playerSelectionMenu);
				previousScene.destroy();
				return null;
			}
			
		}.execute();
		
	}

	public void showGameScene() {
		final AbstractScene previousScene = getCurrentScene();
		setCurrentScene(loadingScene);

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Debug.e("Interrupted", e);
				}
				GameScene gameScene = new GameScene();
				gameScene.populate();
				previousScene.destroy();

				setCurrentScene(gameScene);
			}
		});
	}

	public void setCurrentScene(AbstractScene currentScene) {
		this.currentScene = currentScene;
		res.engine.setScene(currentScene);
		Debug.i("Current scene: " + currentScene.getClass().getName());
	}
}
