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

public class Game extends ApplicationAdapter {
	
	public static int VIRTUAL_WIDTH = 640;
	public static int VIRTUAL_HEIGHT = 480;
	
	public static GameStateManager stateManager;
	public static AssetManager assetManager;
	public static InputManager inputManager;

	SpriteBatch batch;
	Texture texture;
	int[] pal;
	int[][] rects;
	int step = 0;
	Pixmap userCanvas;
	boolean aWasDown = false;
	
	@Override
	public void create () {
		Game.assetManager = new AssetManager();
		Game.stateManager = new GameStateManager(this);
		Game.inputManager = new InputManager();
		
		Pixmap pm = new Pixmap(Gdx.files.internal("badlogic.jpg"));
		userCanvas = new Pixmap(pm.getWidth(), pm.getHeight(), Format.RGBA8888);
		pal = ImageToolkit.getPalette(pm, 32, 100);
		rects = ImageToolkit.approximateImage(pm, pal, 64, 100, 200);
		texture = new Texture(pm);
		pm.dispose();
		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0.5f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		ImageToolkit.drawRectanglesToImage(userCanvas, rects, pal, step);
		
		boolean aWasReleased = false;
		if(aWasDown && !Gdx.input.isKeyPressed(Keys.A)) {
			aWasReleased = true;
		}
		aWasDown = Gdx.input.isKeyPressed(Keys.A);
		
		if(aWasReleased) {
			step = (step+1) % rects.length;
		}
		
		Texture t2 = new Texture(userCanvas);

		batch.begin();
		batch.draw(texture, 0f, 0f);
		batch.draw(t2, texture.getWidth(), 0);
		batch.end();
		
		t2.dispose();
	}
	
	@Override
	public void dispose() {
		texture.dispose();
		stateManager.dispose();
		assetManager.dispose();
	}
}
