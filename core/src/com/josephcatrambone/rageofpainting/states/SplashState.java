package com.josephcatrambone.rageofpainting.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.josephcatrambone.rageofpainting.Game;
import com.josephcatrambone.rageofpainting.handlers.GameStateManager;
import com.josephcatrambone.rageofpainting.handlers.TweenManager;

public class SplashState extends GameState {

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture texture;
	private Vector2 position;
	private float deltaTime = 0;
	private boolean doneLoading = false;
	private MainMenuState mms = null;
	
	public SplashState(GameStateManager gsm) {
		super(gsm);
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Game.VIRTUAL_WIDTH, Game.VIRTUAL_HEIGHT);
		position = new Vector2(0, -200);
		batch = new SpriteBatch();
		Game.assetManager.load("splash.png", Texture.class);
		Game.assetManager.finishLoading();
		texture = Game.assetManager.get("splash.png", Texture.class);
		TweenManager.addTween((Object)position, "y", -200f, Game.VIRTUAL_HEIGHT/2 - texture.getHeight()/2, 2.0f);
	}
	
	@Override
	public void update(float dt) {
		TweenManager.update(dt);
		deltaTime += dt;
	}

	@Override
	public void render(float dt) {
		Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(texture, position.x, position.y);
		batch.end();
	}

	@Override
	public void dispose() {
		texture.dispose();
	}

}
