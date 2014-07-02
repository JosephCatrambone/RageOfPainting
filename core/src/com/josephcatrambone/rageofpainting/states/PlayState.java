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

public class PlayState extends GameState {
	
	private BitmapFont font;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	
	// Demonstration pieces
	private Texture goalImage;
	private Texture teacherImage;
	private Pixmap teacherCanvas;
	private int[] pal;
	private int[][] steps;
	private int step = 0;
	private boolean play = false;
	
	// User pieces
	private Canvas userCanvas;
	private Button[] colorSelection;
	
	public PlayState(GameStateManager gsm) {
		super(gsm);
		camera = new OrthographicCamera(game.VIRTUAL_WIDTH, game.VIRTUAL_HEIGHT);
		camera.setToOrtho(false, game.VIRTUAL_WIDTH, game.VIRTUAL_HEIGHT);
		
		final int PALETTE_SELECTION_HEIGHT = 32;
		
		batch = new SpriteBatch();
		
		font = new BitmapFont();
		
		System.out.println("Opening image.");
		Pixmap img = new Pixmap(Gdx.files.internal("shalinor.gif"));
		teacherCanvas = new Pixmap(img.getWidth(), img.getHeight(), Format.RGBA8888);
		userCanvas = new Canvas(Game.VIRTUAL_WIDTH/2, PALETTE_SELECTION_HEIGHT, img.getWidth(), img.getHeight());
		userCanvas.brushSize = 3;
		userCanvas.INTERPOLATION_LEVEL = 10;
		
		System.out.println("Selecting palette.");
		pal = ImageToolkit.getPalette(img, 8, 100);
		setupToolbox(Game.VIRTUAL_WIDTH/2, 0, Game.VIRTUAL_WIDTH/2, PALETTE_SELECTION_HEIGHT);
		
		System.out.println("Reducing colors.");
		ImageToolkit.reduceColors(img, pal, img);
		
		System.out.println("Finding steps.");
		steps = ImageToolkit.approximateImage(img, pal);
		
		System.out.println("Pushing texture from CPU mem to GPU mem.");
		goalImage = new Texture(img);
		img.dispose();

		//playButton = new Button(goalImage, 0, 0, new Runnable() { public void run() {}; });
		//TweenManager.addTween(playButton.position, "x", -1, 1, 10, TweenManager.makeTween(playButton.position, "x", 1, -1, 10, null));
		
	}

	@Override
	public void update(float dt) {
		ImageToolkit.drawPixelsToImage(teacherCanvas, steps, pal, step);
		
		if(!Gdx.input.isKeyPressed(Keys.P) && play) {
			step = (step+1) % steps.length;
		}
		
		if(Gdx.input.isKeyPressed(Keys.NUM_1)) {
			userCanvas.brushColor = pal[0];
		}
		
		if(Gdx.input.isKeyPressed(Keys.NUM_2)) {
			userCanvas.brushColor = pal[1];
		}
		
		userCanvas.update(dt);
		
		for(Button b : colorSelection) {
			b.update(dt);
		}
	}

	@Override
	public void render(float dt) {
		Gdx.gl.glClearColor(0, 0.5f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Texture t2 = new Texture(teacherCanvas);

		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(goalImage, 0f, 0f);
		batch.draw(t2, goalImage.getWidth(), 0);
		userCanvas.render(batch);
		for(Button b : colorSelection) {
			b.render(batch);
		}
		batch.end();
		
		t2.dispose();
	}

	@Override
	public void dispose() {
		goalImage.dispose();
		batch.dispose();
	}
	
	private void setupToolbox(int x, int y, int width, int height) {
		colorSelection = new Button[pal.length]; // All the colors plus brush enlarge and brush shrink.
		int buttonWidth = width/colorSelection.length;
		for(int i=0; i < colorSelection.length; i++) {
			Pixmap pm = new Pixmap(buttonWidth, height, Format.RGBA8888);
			pm.setColor(pal[i]);
			pm.fillRectangle(0, 0, buttonWidth, height);
			Texture buttonTexture = new Texture(pm);
			final Integer color = pal[i];
			colorSelection[i] = new Button(buttonTexture, i*buttonWidth+x, y, new Runnable() {
				public void run() {
					userCanvas.brushColor = color;
				}
			});
			pm.dispose();
		}
	}

}
