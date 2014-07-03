package com.josephcatrambone.rageofpainting.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.josephcatrambone.rageofpainting.Game;
import com.josephcatrambone.rageofpainting.handlers.GameStateManager;
import com.josephcatrambone.rageofpainting.handlers.TweenManager;

public class LoadingState extends GameState {
	
	private static final float SPLASH_DELAY = 2.0f; 
	private static final float MOVE_SPEED = 1.0f;

	private BitmapFont font;
	private Texture texture;
	private Vector2 position;
	public boolean doneLoading = false;
	public GameState nextState;
	
	public LoadingState() {
		super();
		font = new BitmapFont();
		position = new Vector2(0, -200);
		Game.assetManager.load("splash.png", Texture.class);
		Game.assetManager.finishLoading();
		texture = Game.assetManager.get("splash.png", Texture.class);
		position.x = Game.VIRTUAL_WIDTH/2 - texture.getWidth()/2;
		//TweenManager.addTween((Object)position, "y", -200f, Game.VIRTUAL_HEIGHT/2 - texture.getHeight()/2, SPLASH_DELAY);
		position.y = -200;
	}
	
	@Override
	public void update(float dt) {
		TweenManager.update(dt);
		
		if(doneLoading) {
			Game.stateManager.setState(nextState);
		}
		
		position.y += MOVE_SPEED * dt;
		if(position.y > Game.VIRTUAL_HEIGHT + texture.getHeight()) {
			position.y = -texture.getHeight();
		}
	}

	@Override
	public void render(float dt) {
		Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
		
		Game.mainCamera.update();
		Game.spriteBatch.setProjectionMatrix(Game.mainCamera.combined);
		Game.spriteBatch.begin();
		Game.spriteBatch.draw(texture, position.x, position.y);
		font.draw(Game.spriteBatch, "Loading...", 0f, 0f);
		Game.spriteBatch.end();
	}

	@Override
	public void dispose() {
		texture.dispose();
	}

}
