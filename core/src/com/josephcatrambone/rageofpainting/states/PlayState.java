package com.josephcatrambone.rageofpainting.states;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

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
	
	// Level logic
	private boolean loaded = false;
	private float passThreshold; // How close the images have to be for passing
	private float episodeDuration; // How long this stage lasts
	private float accumulatedTime; // How long we've been in this level
	private Float[] timeMarkers; // Values between 0 and 1 which indicate how far along we should be to trigger an episode event.
	private String[] hostEmotions; // What look will our host take
	private String[] hostComments; // What will our host say?
	
	// Demonstration pieces
	private Texture goalImage = null;
	private Texture teacherImage = null;
	private Pixmap teacherCanvas = null;
	private int[] pal = null;
	private int[][] steps = null;
	private int step = 0;
	private boolean play = false;
	
	// User pieces
	private Canvas userCanvas;
	private Button[] colorSelection;
	
	public PlayState(String scriptFilename) {
		super();
		camera = Game.mainCamera;
		batch = Game.spriteBatch;
		font = Game.font;
		loaded = false;
		
		// Convert the script filename into the actual script.
		BufferedReader fin;
		try {
			parseScript(Gdx.files.internal(scriptFilename).read());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			loaded = false;
		} catch(IOException ioe) {
			// TODO Handle io exception
			ioe.printStackTrace();
			loaded = false;
		}
	}

	public void parseScript(InputStream fstream) throws IOException {
		final int PALETTE_SELECTION_HEIGHT = 32;
		Scanner fin = new Scanner(fstream);
		
		// The file format:
		//0 EpisodeTemplate // Level Name
		//1 ImageFilename.png // Image to be loaded
		//2 number_of_colors // Number of colors on the palette
		//3 time_limit // How long the user has to complete it
		//4 0.7 // This is the similarity that the images must have for this level to be passed
		//5.0 0.0 // This is the percent of the way done we are
		//5.1 thestate_for_the_painter_to_have.png Or Blank for no change.
		//5.2 This is a line which the robot will say. 
		//Repeat 5
		
		//JsonParser json = Json.createparser(new InputStream(new URL()));
		
		// Set level name
		String levelName = fin.nextLine();
		
		// Set level image
		String imageFilename = fin.nextLine();
		System.out.println("Opening image.");
		Pixmap img = new Pixmap(Gdx.files.internal(imageFilename));
		teacherCanvas = new Pixmap(img.getWidth(), img.getHeight(), Format.RGBA8888);
		userCanvas = new Canvas(Game.VIRTUAL_WIDTH/2, PALETTE_SELECTION_HEIGHT, img.getWidth(), img.getHeight()); // TODO: Move over the canvas to fit the aspect ratio.
		userCanvas.brushSize = 3;
		userCanvas.INTERPOLATION_LEVEL = 10;
		
		// Select the number of colors
		int numColors = Integer.parseInt(fin.nextLine());
		System.out.println("Selecting palette.");
		pal = ImageToolkit.getPalette(img, numColors, 100);
		setupToolbox(Game.VIRTUAL_WIDTH/2, 0, Game.VIRTUAL_WIDTH/2, PALETTE_SELECTION_HEIGHT);
		
		// Now that we have the level image and number of colors, process it.
		System.out.println("Reducing colors.");
		ImageToolkit.reduceColors(img, pal, img);
		System.out.println("Finding steps.");
		steps = ImageToolkit.approximateImage(img, pal);
		System.out.println("Pushing texture from CPU mem to GPU mem.");
		goalImage = new Texture(img);
		img.dispose();
		
		// Set the time limit
		episodeDuration = Float.parseFloat(fin.nextLine());
		accumulatedTime = 0;
		
		// Set the passing threshold
		passThreshold = Float.parseFloat(fin.nextLine());
		
		// Finish processing.
		ArrayList <Float> triggers = new ArrayList<Float>();
		ArrayList <String> emotions = new ArrayList<String>();
		ArrayList <String> comments = new ArrayList<String>();
		while(fin.hasNextLine()) {
			triggers.add(Float.parseFloat(fin.nextLine()));
			emotions.add(fin.nextLine());
			comments.add(fin.nextLine());
		}
		timeMarkers = new Float[triggers.size()];
		triggers.toArray(timeMarkers);
		hostEmotions = new String[emotions.size()];
		emotions.toArray(hostEmotions);
		hostComments = new String[comments.size()];
		comments.toArray(hostComments);

		//playButton = new Button(goalImage, 0, 0, new Runnable() { public void run() {}; });
		//TweenManager.addTween(playButton.position, "x", -1, 1, 10, TweenManager.makeTween(playButton.position, "x", 1, -1, 10, null));
		
		loaded = true;
		fin.close();
		fstream.close();
	}

	@Override
	public void update(float dt) {
		if(!loaded) { return; }
		
		accumulatedTime += dt;
		float completionAmount = accumulatedTime/episodeDuration;
		
		System.out.print(accumulatedTime + ", " + episodeDuration + ", " + completionAmount);
		
		// Select the most recent trigger
		int latestTrigger = 0;
		while(timeMarkers.length < latestTrigger+1 && timeMarkers[latestTrigger+1] < completionAmount) {
			latestTrigger++; // TODO: We don't have to scan here.
		}
		
		// Set the emotion
		// Set the text
		System.out.print(hostEmotions[latestTrigger] + ":" + hostComments[latestTrigger] + "\n");
		
		// Check completion
		if(completionAmount >= 1.0) {
			Game.stateManager.popState();
		}
		
		// TODO: Calculate steps and update the teacher image if we need to.
		step = (int)(completionAmount*steps.length);
		ImageToolkit.drawPixelsToImage(teacherCanvas, steps, pal, step);
		// if(advanceToNextStep)
		if(teacherImage != null) { teacherImage.dispose(); }
		teacherImage = new Texture(teacherCanvas);
		
		// Handle keyboard shortcuts for the bruashes
		if(Gdx.input.isKeyPressed(Keys.NUM_1)) {
			userCanvas.brushColor = pal[0];
		}
		
		if(Gdx.input.isKeyPressed(Keys.NUM_2)) {
			userCanvas.brushColor = pal[1];
		}
		
		// Handle user canvas painting
		userCanvas.update(dt);
		
		// Handle the color selection
		for(Button b : colorSelection) {
			b.update(dt);
		}
	}

	@Override
	public void render(float dt) {
		if(!loaded) { return; }
		
		Gdx.gl.glClearColor(0, 0.5f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(goalImage, 0f, 0f);
		if(teacherImage != null) { batch.draw(teacherImage, goalImage.getWidth(), 0); } //We only update the teacher image once in a while, so don't regernerate it every time.
		userCanvas.render(batch);
		for(Button b : colorSelection) {
			b.render(batch);
		}
		batch.end();
	}

	@Override
	public void dispose() {
		if(teacherImage != null) { teacherImage.dispose(); teacherImage = null; }
		goalImage.dispose();
		// batch.dispose();
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
