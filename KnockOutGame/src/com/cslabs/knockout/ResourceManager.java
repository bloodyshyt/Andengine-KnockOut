package com.cslabs.knockout;

import java.io.IOException;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
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
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObject;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;

import android.graphics.Typeface;

public class ResourceManager {

	// single instance is created only once
	private static final ResourceManager INSTANCE = new ResourceManager();

	// common objects
	public GameActivity activity; 
	public Engine engine;
	public Camera camera;
	public VertexBufferObjectManager vbom;

	// game textures
	public ITexture player1Texture;
	public ITiledTextureRegion player1TextureRegion;
	public ITexture player2Texture;
	public ITiledTextureRegion player2TextureRegion;

	private BuildableBitmapTextureAtlas gameTextureAtlas;

	//public ITextureRegion testPieceTextureRegion;

	public ITextureRegion testPieceTextureRegion;
	public ITextureRegion arrowTextureRegion;
	public ITextureRegion checkerCenterRegion;
	
	// game font used
	public Font mFont;

	// constructor is private so that nobody can call it from the outside
	private ResourceManager() {
	}

	public static ResourceManager getInstance() {
		return INSTANCE;
	}

	public void create(GameActivity activity, Engine engine, Camera camera,
			VertexBufferObjectManager vbom) {
		this.activity = activity;
		this.engine = engine;
		this.camera = camera;
		this.vbom = vbom;
	}

	public void loadGameGraphics() throws IOException {

		player1Texture = new AssetBitmapTexture(
				activity.getTextureManager(), activity.getAssets(),
				"gfx/player1.png");
		player1TextureRegion = TextureRegionFactory
				.extractTiledFromTexture(player1Texture, 2, 1);
		player1Texture.load();
		
		player2Texture = new AssetBitmapTexture(
				activity.getTextureManager(), activity.getAssets(),
				"gfx/player2.png");
		player2TextureRegion = TextureRegionFactory
				.extractTiledFromTexture(player2Texture, 2, 1);
		player2Texture.load();

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		gameTextureAtlas = new BuildableBitmapTextureAtlas(
				activity.getTextureManager(), 1024, 512,
				BitmapTextureFormat.RGBA_8888,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		testPieceTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameTextureAtlas, activity.getAssets(),
						"marble.png");
		
		arrowTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameTextureAtlas, activity.getAssets(),
						"arrow.png");
		
		checkerCenterRegion= BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(gameTextureAtlas, activity.getAssets(),
						"center.png");
		
		try {
			gameTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(2, 0, 2));
			gameTextureAtlas.load();
			
		} catch (final TextureAtlasBuilderException e) {
			throw new RuntimeException("Error while loading game textures", e);
		}
		
		// load font 
		mFont = FontFactory.create(engine.getFontManager(),
				engine.getTextureManager(), 256, 256,
				Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), 32f, 
				true);
				mFont.load();

		// # This method does not seem to work (from Learning Andengine
		// BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		// gameTextureAtlas = new BuildableBitmapTextureAtlas(
		// activity.getTextureManager(), 1024, 512,
		// BitmapTextureFormat.RGBA_8888,
		// TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		//
		// mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory
		// .createTiledFromAsset(gameTextureAtlas, activity.getAssets(),
		// "face_circle_tiled.png", 2, 1);
		// try {
		// gameTextureAtlas
		// .build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
		// BitmapTextureAtlas>(
		// 2, 0, 2));
		// gameTextureAtlas.load();
		//
		// } catch (final TextureAtlasBuilderException e) {
		// throw new RuntimeException("Error while loading game textures", e);
		// }

	}
}
