package com.josephcatrambone.rageofpainting.states;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.josephcatrambone.rageofpainting.Game;
import com.josephcatrambone.rageofpainting.handlers.GameStateManager;
import com.josephcatrambone.rageofpainting.handlers.InputManager;
import com.josephcatrambone.rageofpainting.handlers.TweenManager;

public class TitleState extends GameState {
	
	private static final float SPLASH_DELAY = 2.0f;
	private static final float SMOOTHING = 0.1f;

	private BitmapFont font;
	private Texture texture;
	private Vector2 position;
	private float timeAccumulator;
	private float targetLogoY;
	
	public TitleState() {
		super();
		font = new BitmapFont();
		position = new Vector2(0, -200);
		
		Game.assetManager.load("splash.png", Texture.class);
		Game.assetManager.finishLoading();
		texture = Game.assetManager.get("splash.png", Texture.class);
		
		position.x = Game.VIRTUAL_WIDTH/2 - texture.getWidth()/2;
		targetLogoY = (Game.VIRTUAL_HEIGHT - texture.getHeight())/2.0f;
		
		//TweenManager.addTween((Object)position, "y", -200f, Game.VIRTUAL_HEIGHT/2 - texture.getHeight()/2, SPLASH_DELAY);
		Game.assetManager.load("title.png", Texture.class);
		Game.assetManager.load("PaintAndMisery.wav", Music.class);
	}
	
	@Override
	public void update(float dt) {
		timeAccumulator += dt;
		
		// If we're done loading the main logo and the music, set the logo and play the music.
		if(timeAccumulator > SPLASH_DELAY && Game.assetManager.update()) {
			texture = Game.assetManager.get("title.png", Texture.class);
			position.x = 0;
			position.y = 0;
			
			if(Game.activeMusicTrack == null) {
				Game.activeMusicTrack = Game.assetManager.get("PaintAndMisery.wav", Music.class);
				Game.activeMusicTrack.play();
				Game.activeMusicTrack.setLooping(true);
			}
		} else {
			position.y = SMOOTHING*(targetLogoY - position.y) + position.y;
		}
		
		if(InputManager.isMousePressed(0)) {
			Game.stateManager.setState(new MainMenuState());
		}
	}

	@Override
	public void render(float dt) {
		Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
		
		Game.mainCamera.update();
		Game.spriteBatch.setProjectionMatrix(Game.mainCamera.combined);
		Game.spriteBatch.begin();
		
		Game.spriteBatch.draw(texture, position.x, position.y);
		
		//font.draw(Game.spriteBatch, "Loading...", 0f, 0f);
		Game.spriteBatch.end();
	}

	@Override
	public void dispose() {
		texture.dispose();
	}

}
