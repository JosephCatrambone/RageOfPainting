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
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.josephcatrambone.rageofpainting.Game;
import com.josephcatrambone.rageofpainting.entities.Button;
import com.josephcatrambone.rageofpainting.entities.Canvas;
import com.josephcatrambone.rageofpainting.entities.TextDisplay;
import com.josephcatrambone.rageofpainting.handlers.GameStateManager;
import com.josephcatrambone.rageofpainting.handlers.ImageToolkit;
import com.josephcatrambone.rageofpainting.handlers.InputManager;
import com.josephcatrambone.rageofpainting.handlers.TweenManager;

public class PlayState extends GameState {
	private static final String BUTTON_TEXTURE = "button_up.png";
	
	private BitmapFont font;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	
	// Level logic
	private boolean loaded = false;
	private String levelName; // What do we call this?
	private float passThreshold; // How close the images have to be for passing
	private float episodeDuration; // How long this stage lasts
	private float accumulatedTime; // How long we've been in this level
	private int lastScriptMarker; // CALCULATED!  The last script marker we encountered.
	private Float[] timeMarkers; // Values between 0 and 1 which indicate how far along we should be to trigger an episode event.
	private String[] hostEmotions; // What look will our host take
	private String[] hostComments; // What will our host say?
	
	// Demonstration pieces
	private boolean showHint = false;
	private Texture goalImage = null;
	private Texture teacherImage = null;
	private Texture teacherSprite = null;
	private Vector2 teacherSpriteLocation = new Vector2(16, 100);
	private Texture backdrop = null;
	private Pixmap teacherCanvas = null;
	private Vector2 teacherCanvasLocation = new Vector2(180, 90);
	private int[] pal = null;
	private int[][] steps = null;
	private TextDisplay textOut = null;
	
	// User pieces
	private Canvas userCanvas;
	private Button[] colorSelection;
	private Button[] controls;
	
	public PlayState(String scriptFilename) {
		super();
		final int TOOLBAR_HEIGHT = 32;
		Game.assetManager.load("backdrop.png", Texture.class);
		Game.assetManager.load(BUTTON_TEXTURE, Texture.class);
		Game.assetManager.load("PaintAndMisery.ogg", Music.class);
		Game.assetManager.finishLoading();
		
		Texture buttonTexture = Game.assetManager.get(BUTTON_TEXTURE, Texture.class);
		
		camera = Game.mainCamera;
		batch = Game.spriteBatch;
		font = Game.font;
		loaded = false;
		
		// Make sure our camera is on point.
		camera.setToOrtho(false, Game.VIRTUAL_WIDTH, Game.VIRTUAL_HEIGHT);
		camera.update();
		
		// Create our text output.
		textOut = new TextDisplay(0, buttonTexture.getHeight(), Game.VIRTUAL_WIDTH/2, Game.VIRTUAL_HEIGHT/6, font);
		lastScriptMarker = 0;
		
		// Convert the script filename into the actual script.
		scriptLoad(scriptFilename);
		
		// Create our palette buttons
		colorSelection = new Button[pal.length]; // All the colors plus brush enlarge and brush shrink.
		int buttonWidth = (Game.VIRTUAL_WIDTH/2)/colorSelection.length;
		for(int i=0; i < colorSelection.length; i++) {
			Pixmap pm = new Pixmap(buttonWidth, TOOLBAR_HEIGHT, Format.RGBA8888);
			pm.setColor(pal[i]);
			pm.fillRectangle(0, 0, buttonWidth, TOOLBAR_HEIGHT);
			Texture colorButtonTexture = new Texture(pm);
			final Integer color = pal[i];
			colorSelection[i] = new Button(colorButtonTexture, i*buttonWidth+(Game.VIRTUAL_WIDTH/2), 0, new Runnable() {
				public void run() {
					userCanvas.brushColor = color;
				}
			});
			pm.dispose();
		}
		
		// Create our brush and sequence control buttons
		controls = new Button[4];
		// Done
		controls[0] = new Button(buttonTexture, 0, 0, new Runnable() {
			public void run() {
				// Jump to half the remaining time.
				accumulatedTime += Math.max(0, (episodeDuration-accumulatedTime)*0.5);
			}
		});
		controls[0].setText(">>");
		// Preview completed
		controls[1] = new Button(buttonTexture, 1*buttonTexture.getWidth(), 0, new Runnable() {
			public void run() {
				showHint = !showHint;
			}
		});
		controls[1].setText("?");
		// Enlarge brush
		controls[2] = new Button(buttonTexture, 2*buttonTexture.getWidth(), 0, new Runnable() {
			public void run() {
				userCanvas.brushSize += 1;
			}
		});
		controls[2].setText("[+]");
		// Shrink brush
		controls[3] = new Button(buttonTexture, 3*buttonTexture.getWidth(), 0, new Runnable() {
			public void run() {
				userCanvas.brushSize = Math.max(userCanvas.brushSize-1, 1);
			}
		});
		controls[3].setText("[-]");
		
		textOut.setText(hostComments[0]);
		
		// Load the backdrop
		backdrop = Game.assetManager.get("backdrop.png", Texture.class);
		
		// Finally, play music
		// TODO: Make sure we stop other tracks which are playing.
		if(Game.activeMusicTrack != null && Game.activeMusicTrack.isPlaying()) { Game.activeMusicTrack.stop(); }
		Game.activeMusicTrack = Game.assetManager.get("PaintAndMisery.ogg", Music.class);
		Game.activeMusicTrack.play();
		Game.activeMusicTrack.setLooping(true);
	}
	
	private void scriptLoad(String scriptFilename) {
		try {
			InputStream fstream = Gdx.files.internal(scriptFilename).read();
			Scanner fin = new Scanner(fstream);
			Texture buttonTexture = Game.assetManager.get(BUTTON_TEXTURE, Texture.class);
			
			// The file format is detailed in ScriptTemplate.
			
			//JsonParser json = Json.createparser(new InputStream(new URL()));
			
			// Set level name
			levelName = fin.nextLine();
			
			// Set level image
			String imageFilename = fin.nextLine();
			System.out.println("Opening image.");
			Pixmap img = new Pixmap(Gdx.files.internal(imageFilename));
			teacherCanvas = new Pixmap(img.getWidth(), img.getHeight(), Format.RGBA8888);
			teacherImage = new Texture(teacherCanvas);
			userCanvas = new Canvas(Game.VIRTUAL_WIDTH*3/4 - img.getWidth()/2, Game.VIRTUAL_HEIGHT/2+buttonTexture.getHeight() - img.getHeight()/2, img.getWidth(), img.getHeight()); // TODO: Move over the canvas to fit the aspect ratio.
			userCanvas.brushSize = 3;
			userCanvas.INTERPOLATION_LEVEL = 10;
			teacherCanvasLocation.x = Integer.parseInt(fin.nextLine());
			teacherCanvasLocation.y = Integer.parseInt(fin.nextLine()); // TODO: This Integer parse is a holdover from when all we had was line data.  Use the scanner to its full potential with nextInt on rewrite.
			
			
			// Select the number of colors
			int numColors = Integer.parseInt(fin.nextLine());
			System.out.println("Selecting palette.");
			pal = ImageToolkit.getPalette(img, numColors, 100);
			
			// Now that we have the level image and number of colors, process it.
			System.out.println("Reducing colors.");
			ImageToolkit.reduceColors(img, pal, img);
			System.out.println("Finding steps.");
			steps = ImageToolkit.approximateImage(img, pal);
			System.out.println("Pushing texture from CPU mem to GPU mem.");
			goalImage = new Texture(img);
			System.out.println("Disposing of unused pixel map.");
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
			
			// Load all the emotion states
			for(String s : hostEmotions) {
				Game.assetManager.load(s, Texture.class);
			}
			Game.assetManager.finishLoading();
			teacherSprite = Game.assetManager.get(hostEmotions[0], Texture.class);

			//playButton = new Button(goalImage, 0, 0, new Runnable() { public void run() {}; });
			//TweenManager.addTween(playButton.position, "x", -1, 1, 10, TweenManager.makeTween(playButton.position, "x", 1, -1, 10, null));
			
			loaded = true;
			fin.close();
			fstream.close();
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

	@Override
	public void update(float dt) {
		if(!loaded) { return; }
		
		accumulatedTime += dt;
		float completionAmount = accumulatedTime/episodeDuration;
		
		textOut.update(dt);
		
		// Select the most recent trigger
		if(lastScriptMarker+1 < timeMarkers.length && accumulatedTime > timeMarkers[lastScriptMarker+1]) {
			lastScriptMarker++;
			
			// Set the text
			textOut.setText(hostComments[lastScriptMarker], 0.5f, 5f);
			
			// Set the emotion
			teacherSprite = Game.assetManager.get(hostEmotions[lastScriptMarker], Texture.class);
			//System.out.print(hostEmotions[latestTrigger] + ":" + hostComments[latestTrigger] + "\n");
		}
		
		// Check completion
		if(completionAmount >= 1.0) {
			Pixmap goal = new Pixmap(teacherCanvas.getWidth(), teacherCanvas.getHeight(), teacherCanvas.getFormat());
			goal.drawPixmap(teacherCanvas, 0, 0);
			Pixmap src = userCanvas.getCopyOfPixmap();
			GameState nextScreen = new ScoreState(levelName, passThreshold, goal, src);
			Game.stateManager.setState(nextScreen);
			this.dispose();
		}
		
		// TODO: Calculate steps and update the teacher image if we need to.
		ImageToolkit.drawPixelsToImage(teacherCanvas, steps, pal, completionAmount);
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
		
		// Handle controls
		for(Button b : controls) {
			b.update(dt);
		}
	}

	@Override
	public void render(float dt) {
		if(!loaded) { return; }
		
		Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		// Draw background
		batch.draw(backdrop, 0f, 0f);
		
		// Render teacher images
		batch.draw(teacherImage, teacherCanvasLocation.x, teacherCanvasLocation.y);
		batch.draw(teacherSprite, teacherSpriteLocation.x, teacherSpriteLocation.y);
		if(showHint) {
			batch.draw(goalImage, teacherCanvasLocation.x, teacherCanvasLocation.y);
		}
		
		// Render UI
		userCanvas.render(batch);
		for(Button b : colorSelection) {
			b.render(batch);
		}
		for(Button b : controls) {
			b.render(batch);
		}
		
		// Render teacher text
		textOut.render(batch);
		
		// Wrap up
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
