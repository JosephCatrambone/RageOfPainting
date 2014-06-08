package com.josephcatrambone.rageofpainting;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.josephcatrambone.rageofpainting.handlers.GameStateManager;
import com.josephcatrambone.rageofpainting.handlers.ImageToolkit;
import com.josephcatrambone.rageofpainting.handlers.InputManager;
import com.josephcatrambone.rageofpainting.states.PlayState;

public class Game extends ApplicationAdapter {
	
	public static int VIRTUAL_WIDTH = 640;
	public static int VIRTUAL_HEIGHT = 480;
	
	public static GameStateManager stateManager;
	public static AssetManager assetManager;
	public static InputManager inputManager;
	
	@Override
	public void create () {
		Game.assetManager = new AssetManager();
		Game.stateManager = new GameStateManager(this);
		//Game.inputManager = new InputManager();
		
		PlayState ps = new PlayState(Game.stateManager);
		stateManager.pushState(ps);
	}

	@Override
	public void render () {
		float dt = Gdx.graphics.getDeltaTime();
		InputManager.update();
		stateManager.update(dt);
		stateManager.render(dt);
	}
	
	@Override
	public void dispose() {
		stateManager.dispose();
		assetManager.dispose();
	}
}
