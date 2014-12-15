package com.cslabs.knockout;

import java.io.IOException;
import java.util.ArrayList;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.IResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.debug.Debug;
import org.xml.sax.SAXException;

import android.content.Context;

import com.cslabs.knockout.entity.Checker;
import com.cslabs.knockout.factory.CheckerFactory;
import com.cslabs.knockout.scene.GameScene;

public class GameActivity extends BaseGameActivity {

	public static final int CAMERA_WIDTH = 480;
	public static final int CAMERA_HEIGHT = 800;

	public Scene mScene;

	//public Context context;

	private ArrayList<Checker> pieces = new ArrayList<Checker>();

	private int[][] coordinates = { { 100, 600 }, { 200, 600 }, { 300, 600 },
			{ 100, 200 }, { 200, 200 }, { 300, 200 }, };

	@Override
	public EngineOptions onCreateEngineOptions() {
		Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
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
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws IOException {
		ResourceManager.getInstance().create(this, getEngine(),
				getEngine().getCamera(), getVertexBufferObjectManager());
		ResourceManager.getInstance().loadGameGraphics();
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws IOException {
		mScene = new GameScene();
		pOnCreateSceneCallback.onCreateSceneFinished(null);
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback)
			throws IOException {

		SceneManager.getInstance().showGameScene();
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

}