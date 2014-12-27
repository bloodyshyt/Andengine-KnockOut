package com.cslabs.knockout;

import java.io.IOException;

import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.IResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.debug.Debug;

import android.view.KeyEvent;

import com.cslabs.knockout.Managers.ResourceManager;
import com.cslabs.knockout.Managers.SceneManager;

public class GameActivity extends BaseGameActivity {

	public static final int CAMERA_WIDTH = 480;
	public static final int CAMERA_HEIGHT = 800;
	
	//private Handler handler;

	public Scene mScene;
	
	private SmoothCamera camera;

	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 1000, 1000, 1);
		IResolutionPolicy resolutionPolicy = new FillResolutionPolicy();
		EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.PORTRAIT_FIXED, resolutionPolicy, camera);
		engineOptions.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
		engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
		engineOptions.getRenderOptions().setDithering(true);

		Debug.i("Engine configured");
		return engineOptions;
	}
	
	@Override
	public synchronized void onResumeGame() {
		super.onResumeGame();
		SceneManager.getInstance().getCurrentScene().onResume();
	}

	@Override
	public synchronized void onPauseGame() {
		super.onPauseGame();
		SceneManager.getInstance().getCurrentScene().onPause();
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws IOException {
		ResourceManager.getInstance().create(this, getEngine(),
				(ZoomCamera) getEngine().getCamera(), getVertexBufferObjectManager());
		//ResourceManager.getInstance().loadGameGraphics();
		ResourceManager.getInstance().loadSplashGraphics();
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws IOException {
		pOnCreateSceneCallback.onCreateSceneFinished(null);
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback)
			throws IOException {

		//SceneManager.getInstance().showGameScene();
		SceneManager.getInstance().showSplashAndMenuScene();
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
