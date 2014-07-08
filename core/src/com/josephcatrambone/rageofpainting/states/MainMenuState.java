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
	private static final String BUTTON_TEXTURE = "button_up.png";

	private SpriteBatch batch;
	private OrthographicCamera camera;
	private BitmapFont font;
	private Button[] levelButtons;
	
	public MainMenuState() {
		super();
		
		batch = Game.spriteBatch;
		camera = Game.mainCamera;
		font = Game.font;
		
		Game.assetManager.load(BUTTON_TEXTURE, Texture.class);
		Game.assetManager.finishLoading();
		
		final GameStateManager gsmRef = Game.stateManager; 
		levelButtons = new Button[1];
		levelButtons[0] = new Button(Game.assetManager.get(BUTTON_TEXTURE, Texture.class), 0f, 0f, "Episode 1", new Runnable() {
			public void run() {
				System.out.println("Loading Level 1");
				Game.stateManager.pushState(new LoadingState());
				Game.stateManager.setState(new PlayState("Level1.txt"));
			}
		});
	}

	@Override
	public void update(float dt) {
		for(Button b : levelButtons) {
			b.update(dt);
		}
	}

	@Override
	public void render(float dt) {
		Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT | Gdx.gl.GL_DEPTH_BUFFER_BIT);
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		for(Button b : levelButtons) {
			b.render(batch);
		}
		
		font.draw(batch, "Level Select", 0, Game.VIRTUAL_HEIGHT-1*font.getLineHeight());
		
		batch.end();
	}

	@Override
	public void dispose() {
	}

}
