package com.cslabs.knockout.scene;

import org.andengine.entity.sprite.Sprite;

import com.cslabs.knockout.GameActivity;

public class SplashScene extends AbstractScene {

	@Override
	public void populate() {
		Sprite splashSprite = new Sprite(GameActivity.CAMERA_WIDTH / 2,
				GameActivity.CAMERA_HEIGHT / 2, res.splashTextureRegion, vbom);
		attachChild(splashSprite);
	}

	@Override
	public void onPause() {
	}

	@Override
	public void onResume() {
	}
}