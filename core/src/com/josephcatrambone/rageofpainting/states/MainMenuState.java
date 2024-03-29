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
		
		levelButtons = new Button[4];
		
		levelButtons[0] = new Button(Game.assetManager.get(BUTTON_TEXTURE, Texture.class), 50f, 100f, "Episode 1", new Runnable() {
			public void run() {
				System.out.println("Loading Level 1");
				Game.stateManager.pushState(new LoadingState());
				Game.stateManager.setState(new PlayState("Level1.txt"));
			}
		});
		
		levelButtons[1] = new Button(Game.assetManager.get(BUTTON_TEXTURE, Texture.class), 150f, 100f, "Episode 2", new Runnable() {
			public void run() {
				System.out.println("Loading Level 2");
				Game.stateManager.pushState(new LoadingState());
				Game.stateManager.setState(new PlayState("Level2.txt"));
			}
		});
		
		levelButtons[2] = new Button(Game.assetManager.get(BUTTON_TEXTURE, Texture.class), 250f, 100f, "Episode 3", new Runnable() {
			public void run() {
				System.out.println("Loading Level 3");
				Game.stateManager.pushState(new LoadingState());
				Game.stateManager.setState(new PlayState("Level3.txt"));
			}
		});
		
		levelButtons[3] = new Button(Game.assetManager.get(BUTTON_TEXTURE, Texture.class), 350f, 100f, "Episode 4", new Runnable() {
			public void run() {
				System.out.println("Loading Level 4");
				Game.stateManager.pushState(new LoadingState());
				Game.stateManager.setState(new PlayState("Level4.txt"));
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
