package com.cslabs.knockout.Managers;

import java.io.IOException;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.bitmap.BitmapTextureFormat;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;

import com.cslabs.knockout.GameActivity;

import android.graphics.Typeface;

public class ResourceManager {

	// single instance is created only once
	private static final ResourceManager INSTANCE = new ResourceManager();

	// common objects
	public GameActivity activity;
	public Engine engine;
	public ZoomCamera camera;
	public VertexBufferObjectManager vbom;

	// game textures
	private BuildableBitmapTextureAtlas gameTextureAtlas;

	public ITextureRegion gameLogoTextureRegion;
	public ITextureRegion testPieceTextureRegion;
	public ITextureRegion arrowTextureRegion;
	public ITextureRegion checkerCenterRegion;
	public ITextureRegion player1TextureRegion;
	public ITextureRegion player2TextureRegion;

	// game font used
	public Font mFont;
	public Font menuFont;

	// sounds

	// music

	// splash graphics
	public ITextureRegion splashTextureRegion;
	private BitmapTextureAtlas splashTextureAtlas;

	// constructor is private so that nobody can call it from the outside
	private ResourceManager() {
	}

	public static ResourceManager getInstance() {
		return INSTANCE;
	}

	public void create(GameActivity activity, Engine engine, ZoomCamera camera,
			VertexBufferObjectManager vbom) {
		this.activity = activity;
		this.engine = engine;
		this.camera = camera;
		this.vbom = vbom;
	}

	public void loadGameGraphics() {

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		gameTextureAtlas = new BuildableBitmapTextureAtlas(
				activity.getTextureManager(), 1024, 512,
				BitmapTextureFormat.RGBA_8888,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		gameLogoTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameTextureAtlas, activity.getAssets(),
						"gameLogo.png");

		testPieceTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameTextureAtlas, activity.getAssets(),
						"marble.png");

		arrowTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameTextureAtlas, activity.getAssets(),
						"arrow.png");

		checkerCenterRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameTextureAtlas, activity.getAssets(),
						"center.png");
		player1TextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameTextureAtlas, activity.getAssets(),
						"bluechecker.png");
		player2TextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameTextureAtlas, activity.getAssets(),
						"greenchecker.png");
		
		try {
			gameTextureAtlas
					.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
							2, 0, 2));
			gameTextureAtlas.load();

		} catch (final TextureAtlasBuilderException e) {
			throw new RuntimeException("Error while loading game textures", e);
		}
	}

	public void loadGameAudio() {
		// NOTHING DOING HERE
	}

	public void loadFont() {
		// load font
		mFont = FontFactory.create(engine.getFontManager(),
				engine.getTextureManager(), 256, 256,
				Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), 32f, true);
		mFont.load();

		menuFont = FontFactory.createStroke(activity.getFontManager(),
				activity.getTextureManager(), 512, 256,
				Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD), 50, true,
				Color.WHITE_ABGR_PACKED_INT, 2, Color.BLACK_ABGR_PACKED_INT);
		menuFont.prepareLetters("01234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ.,!?"
				.toCharArray());
		menuFont.load();

	}

	public void loadSplashGraphics() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		splashTextureAtlas = new BitmapTextureAtlas(
				activity.getTextureManager(), 256, 256,
				BitmapTextureFormat.RGBA_8888,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		splashTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(splashTextureAtlas, activity.getAssets(),
						"badge.png", 0, 0);

		splashTextureAtlas.load();
	}

	public void unloadSplashGraphics() {

		splashTextureAtlas.unload();

	}
}
