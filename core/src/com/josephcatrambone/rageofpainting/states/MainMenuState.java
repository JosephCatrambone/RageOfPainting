package com.josephcatrambone.rageofpainting.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.josephcatrambone.rageofpainting.Game;
import com.josephcatrambone.rageofpainting.entities.Button;
import com.josephcatrambone.rageofpainting.entities.Canvas;
import com.josephcatrambone.rageofpainting.handlers.GameStateManager;
import com.josephcatrambone.rageofpainting.handlers.ImageToolkit;
import com.josephcatrambone.rageofpainting.handlers.InputManager;
import com.josephcatrambone.rageofpainting.handlers.TweenManager;

public class MainMenuState extends GameState {

	private SpriteBatch batch;
	private OrthographicCamera camera;
	private BitmapFont font;
	private Button[] levelSelect;
	
	public MainMenuState(GameStateManager gsm) {
		super(gsm);
		
		batch = new SpriteBatch();
		camera = new OrthographicCamera(Game.VIRTUAL_WIDTH, Game.VIRTUAL_HEIGHT);
		camera.setToOrtho(false, Game.VIRTUAL_WIDTH, Game.VIRTUAL_HEIGHT);
		font = new BitmapFont();
		
	}

	@Override
	public void update(float dt) {
	}

	@Override
	public void render(float dt) {
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		
		
		batch.end();
	}

	@Override
	public void dispose() {
	}

}
