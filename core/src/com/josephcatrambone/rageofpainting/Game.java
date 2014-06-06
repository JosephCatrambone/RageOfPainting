package com.josephcatrambone.rageofpainting;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.josephcatrambone.rageofpainting.handlers.GameStateManager;
import com.josephcatrambone.rageofpainting.handlers.ImageToolkit;
import com.josephcatrambone.rageofpainting.handlers.InputManager;

public class Game extends ApplicationAdapter {
	
	public static int VIRTUAL_WIDTH = 640;
	public static int VIRTUAL_HEIGHT = 480;
	
	public static GameStateManager stateManager;
	public static AssetManager assetManager;
	public static InputManager inputManager;

	SpriteBatch batch;
	Texture texture, texture2;
	
	@Override
	public void create () {
		Game.assetManager = new AssetManager();
		Game.stateManager = new GameStateManager(this);
		Game.inputManager = new InputManager();
		
		Pixmap pm = new Pixmap(Gdx.files.internal("badlogic.jpg"));
		Pixmap pm2 = new Pixmap(pm.getWidth(), pm.getHeight(), Format.RGBA8888);
		int[] pal = ImageToolkit.getPalette(pm, 8, 10);
		ImageToolkit.reduceColors(pm, pal, pm2);
		texture = new Texture(pm);
		texture2 = new Texture(pm2);
		pm.dispose();
		pm2.dispose();
		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0.5f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		batch.draw(texture, 0f, 0f);
		batch.draw(texture2, texture.getWidth(), 0);
		batch.end();
	}
	
	@Override
	public void dispose() {
		texture.dispose();
		stateManager.dispose();
		assetManager.dispose();
	}
}
