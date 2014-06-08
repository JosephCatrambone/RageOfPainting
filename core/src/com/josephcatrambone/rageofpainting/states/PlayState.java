package com.josephcatrambone.rageofpainting.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.josephcatrambone.rageofpainting.entities.Button;
import com.josephcatrambone.rageofpainting.handlers.GameStateManager;
import com.josephcatrambone.rageofpainting.handlers.ImageToolkit;
import com.josephcatrambone.rageofpainting.handlers.InputManager;

public class PlayState extends GameState {
	
	SpriteBatch batch;
	
	Texture goalImage;
	Texture teacherImage;
	Texture userImage;
	
	Button playButton;
	
	int[] pal;
	int[][] steps;
	int step = 0;
	boolean play = false;
	
	Pixmap teacherCanvas;
	Pixmap userCanvas;
	
	public PlayState(GameStateManager gsm) {
		super(gsm);
		
		batch = new SpriteBatch();
		
		System.out.println("Opening image.");
		Pixmap img = new Pixmap(Gdx.files.internal("shalinor.gif"));
		teacherCanvas = new Pixmap(img.getWidth(), img.getHeight(), Format.RGBA8888);
		userCanvas = new Pixmap(img.getWidth(), img.getHeight(), Format.RGBA8888);
		
		System.out.println("Selecting palette.");
		pal = ImageToolkit.getPalette(img, 6, 50);
		
		System.out.println("Reducing colors.");
		ImageToolkit.reduceColors(img, pal, img);
		
		System.out.println("Finding steps.");
		steps = ImageToolkit.approximateImage(img, pal);
		
		System.out.println("Pushing texture from CPU mem to GPU mem.");
		goalImage = new Texture(img);
		img.dispose();
		
		playButton = new Button(goalImage, 0, 0, new Runnable() {
			public void run() {
				System.out.println("Button pressed.");
				play = !play;
			}
		});
	}

	@Override
	public void update(float dt) {
		ImageToolkit.drawPixelsToImage(teacherCanvas, steps, pal, step);
		
		if(!Gdx.input.isKeyPressed(Keys.P) && play) {
			step = (step+1) % steps.length;
		}
		
		playButton.update(dt);
	}

	@Override
	public void render(float dt) {
		Gdx.gl.glClearColor(0, 0.5f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Texture t2 = new Texture(teacherCanvas);

		batch.begin();
		batch.draw(goalImage, 0f, 0f);
		batch.draw(t2, goalImage.getWidth(), 0);
		playButton.render(batch);
		batch.end();
		
		t2.dispose();
	}

	@Override
	public void dispose() {
		goalImage.dispose();
		batch.dispose();
	}

}
