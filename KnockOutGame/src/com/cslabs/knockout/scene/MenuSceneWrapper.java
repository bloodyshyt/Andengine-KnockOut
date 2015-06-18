package com.cslabs.knockout.scene;

import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.TextMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ColorMenuItemDecorator;
import org.andengine.entity.sprite.Sprite;
import org.andengine.util.adt.color.Color;

import com.cslabs.knockout.AI.AIBotWrapper;
import com.cslabs.knockout.GameLevels.Levels;
import com.cslabs.knockout.Managers.SceneManager;

public class MenuSceneWrapper extends AbstractScene implements
		IOnMenuItemClickListener {

	private IMenuItem playMenuItem;
	private IMenuItem optionsMenuItem;
	private IMenuItem helpMenuItem;
	private IMenuItem aboutMenuItem;

	@Override
	public void populate() {
		MenuScene menuScene = new MenuScene(camera);
		menuScene.getBackground().setColor(0.82f, 0.96f, 0.97f);

		playMenuItem = new ColorMenuItemDecorator(new TextMenuItem(0,
				res.menuFont, "PLAY", vbom), Color.CYAN, Color.WHITE);
		optionsMenuItem = new ColorMenuItemDecorator(new TextMenuItem(1,
				res.menuFont, "OPTIONS", vbom), Color.CYAN, Color.WHITE);
		helpMenuItem = new ColorMenuItemDecorator(new TextMenuItem(2,
				res.menuFont, "HELP", vbom), Color.CYAN, Color.WHITE);
		aboutMenuItem = new ColorMenuItemDecorator(new TextMenuItem(3,
				res.menuFont, "ABOUT", vbom), Color.CYAN, Color.WHITE);
	
		menuScene.addMenuItem(playMenuItem);
		menuScene.addMenuItem(optionsMenuItem);
		menuScene.addMenuItem(helpMenuItem);
		menuScene.addMenuItem(aboutMenuItem);
		
		menuScene.buildAnimations();
		menuScene.setBackgroundEnabled(true);
		
		menuScene.setOnMenuItemClickListener(this);
		
		Sprite gameLogo = new Sprite(240, 600, res.gameLogoTextureRegion, vbom);
		menuScene.attachChild(gameLogo);
		
		// TODO add entity modifiers for sprites and menu items
		
		setChildScene(menuScene);
		
	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
			float pMenuItemLocalX, float pMenuItemLocalY) {
		switch(pMenuItem.getID()) {
		case 0: 
			//SceneManager.getInstance().showLevelSelectionScene();
			SceneManager.getInstance().showGameScene(Levels.getLevelDef(1, 1), new AIBotWrapper[] {null, null});
			return true;
		case 1:
			// options
			//SceneManager.getInstance().showPlayerSelectionScene();
			return true;
		case 2: 
			// help
			return true;
		case 3: 
			// about
			return true;
		default:
			return true;
		}
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void onBackKeyPressed() {
		activity.finish();
	}

}
