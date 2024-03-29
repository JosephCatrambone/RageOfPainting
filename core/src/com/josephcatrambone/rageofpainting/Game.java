package com.josephcatrambone.rageofpainting;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.josephcatrambone.rageofpainting.handlers.GameStateManager;
import com.josephcatrambone.rageofpainting.handlers.InputManager;
import com.josephcatrambone.rageofpainting.handlers.TweenManager;
import com.josephcatrambone.rageofpainting.states.GameState;
import com.josephcatrambone.rageofpainting.states.MainMenuState;
import com.josephcatrambone.rageofpainting.states.PlayState;
import com.josephcatrambone.rageofpainting.states.LoadingState;
import com.josephcatrambone.rageofpainting.states.TitleState;

public class Game extends ApplicationAdapter {
	
	public static int VIRTUAL_WIDTH = 480;
	public static int VIRTUAL_HEIGHT = 320;
	public static int SCREEN_SCALE = 2;
	
	public static GameStateManager stateManager;
	public static AssetManager assetManager;
	public static InputManager inputManager;
	public static OrthographicCamera mainCamera;
	public static SpriteBatch spriteBatch;
	public static BitmapFont font;
	public static Music activeMusicTrack = null;
	
	private int frameCount;
	private float timeAccumulator;
	
	@Override
	public void create () {
		Game.assetManager = new AssetManager();
		Game.stateManager = new GameStateManager(this);
		//Game.inputManager = new InputManager();
		//Game.tweenManager = new TweenManager();
		Game.mainCamera = new OrthographicCamera(Game.VIRTUAL_WIDTH, Game.VIRTUAL_HEIGHT);
		Game.mainCamera.setToOrtho(false, Game.VIRTUAL_WIDTH, Game.VIRTUAL_HEIGHT);
		Game.spriteBatch = new SpriteBatch();
		
		// Load the font.
		Game.assetManager.load("font_visitor.png", Texture.class);
		Game.assetManager.finishLoading();
		Game.font = new BitmapFont(Gdx.files.internal("font_visitor.fnt"), new TextureRegion(Game.assetManager.get("font_visitor.png", Texture.class)));

		stateManager.pushState(new TitleState());
	}

	@Override
	public void render () {
		float dt = Gdx.graphics.getDeltaTime();
		InputManager.update();
		TweenManager.update(dt);
		stateManager.update(dt);
		stateManager.render(dt);
		//calculateFPS(dt);
	}
	
	@Override
	public void dispose() {
		stateManager.dispose();
		assetManager.dispose();
	}
	
	private void calculateFPS(float dt) {
		timeAccumulator += dt;
		frameCount += 1;
		if(timeAccumulator > 1) {
			System.out.println(frameCount + " FPS");
			timeAccumulator = 0;
			frameCount = 0;
		}
	}
}
