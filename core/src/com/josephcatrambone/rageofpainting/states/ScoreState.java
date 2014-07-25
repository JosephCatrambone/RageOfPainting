package com.josephcatrambone.rageofpainting.states;

import java.awt.image.DirectColorModel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.josephcatrambone.rageofpainting.Game;
import com.josephcatrambone.rageofpainting.handlers.InputManager;
import com.josephcatrambone.rageofpainting.handlers.TweenManager;

public class ScoreState extends GameState {
	private static final float padding = 30f;
	private OrthographicCamera camera = null;
	private SpriteBatch batch = null;
	private BitmapFont font = null;
	private Texture objective;
	private Texture attempt;
	
	private String[] judgeSayings = {"", "", ""};
	private String result = "";
	private float[] distances = {-1, -1, -1};
	
	private float maxDistance;
	private Thread calculator;
	
	public ScoreState(String levelName, float successThreshold, Pixmap objective, Pixmap attempt) {
		camera = Game.mainCamera;
		batch = Game.spriteBatch;
		font = Game.font;
		
		this.objective = new Texture(objective);
		this.attempt = new Texture(attempt);
		
		this.maxDistance = successThreshold;
		
		final Pixmap obj = objective;
		final Pixmap att = attempt;
		calculator = new Thread() {
			public void run() {
				float maxDist = getMaxDistance();
				distances[0] = getDistance(getBasicHash(obj), getBasicHash(att)) / maxDist;
				distances[1] = getDistance(getSubsampleHash(obj), getSubsampleHash(att)) / maxDist;
				distances[2] = getDistance(getPHash(obj), getPHash(att)) / maxDist;
			}
		};
		calculator.start();
	}

	@Override
	public void update(float dt) {
		result = "Result: Pending...";
		int judgePasses = 0;
		int judgesPending = 3;
		
		// Calculate judge sayings
		for(int i=0; i < 3; i++) {
			if(distances[i] == -1) {
				judgeSayings[i] = "Judge " + i + ": Deciding...";
			} else {
				judgesPending--;
				judgeSayings[i] = "Judge " + i + ": " + (int)(10*(1.0-distances[i])) + "/10";
				if(distances[i] < maxDistance) {
					judgePasses++;
				}
			}
		}
		
		// And the verdict
		if(judgesPending < 1) {
			if(judgePasses > 2) {
				result = "Result: Pass! (Click anywhere to return to the main menu.)";
			} else {
				result = "Result: Failure! (Click anywhere to return to the main menu.)";
			}
		}
		
		if(InputManager.isMouseDown(0) && judgesPending < 1) { // TODO: Make this just wait for the mouse to be up, then allow any press.
		//if(InputManager.isKeyDown(Input.Keys.K)) {
			Game.stateManager.popState();
		}
	}

	@Override
	public void render(float dt) {
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		batch.draw(objective, padding, padding);
		batch.draw(attempt, Game.VIRTUAL_WIDTH-attempt.getWidth()-padding, padding);
		
		font.draw(batch, judgeSayings[0], padding, Game.VIRTUAL_HEIGHT-2*padding);
		font.draw(batch, judgeSayings[1], padding, Game.VIRTUAL_HEIGHT-3*padding);
		font.draw(batch, judgeSayings[2], padding, Game.VIRTUAL_HEIGHT-4*padding);
		
		font.draw(batch, result, Game.VIRTUAL_WIDTH/2 - font.getBounds(result).width/2, padding/2);
		
		batch.end();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}
	
	public byte[] getHash(Pixmap img) {
		return getSubsampleHash(img);
	}
	
	public byte[] getBasicHash(Pixmap img) {
		byte[] output = new byte[8];
		
		float mean = 0.0f;
		float sum = 0.0f;
		
		Pixmap tempImg = new Pixmap(8, 8, Format.RGBA8888);
		tempImg.drawPixmap(img, 0, 0, img.getWidth(), img.getHeight(), 0, 0, 8, 8);
		
		DirectColorModel cm = new DirectColorModel(32, 0xFF000000, 0x00FF0000, 0x0000FF00, 0x000000FF);
		for(int j=0; j < tempImg.getHeight(); j++) {
			for(int i=0; i < tempImg.getWidth(); i++) {
				int pixel = tempImg.getPixel(i, j);
				float r = cm.getRed(pixel);
				float g = cm.getGreen(pixel);
				float b = cm.getBlue(pixel);
				sum += (r/255f + g/255f + b/255f)/3f;
			}
		}
		
		mean = sum/(float)(tempImg.getWidth()*tempImg.getHeight());
		
		for(int j=0; j < 8; j++) {
			int newByte = 0;
			for(int i=0; i < 8; i++) {
				int pixel = img.getPixel(i, j);
				float r = cm.getRed(pixel);
				float g = cm.getGreen(pixel);
				float b = cm.getBlue(pixel);
				if((r/255f + g/255f + b/255f)/3f > mean) {
					newByte += 1;
				}
				newByte = newByte << 1;
			}
			output[j] = (byte)newByte;
		}
		
		return output;
	}
	
	public byte[] getSubsampleHash(Pixmap img) {
		byte[] output = new byte[8];
		
		float mean = 0.0f;
		float sum = 0.0f;
		
		DirectColorModel cm = new DirectColorModel(32, 0xFF000000, 0x00FF0000, 0x0000FF00, 0x000000FF);
		for(int j=0; j < img.getHeight(); j++) {
			for(int i=0; i < img.getWidth(); i++) {
				int pixel = img.getPixel(i, j);
				float r = cm.getRed(pixel);
				float g = cm.getGreen(pixel);
				float b = cm.getBlue(pixel);
				sum += (r/255f + g/255f + b/255f)/3f;
			}
		}
		
		mean = sum/(float)(img.getWidth()*img.getHeight());
		
		for(int j=0; j < 8; j++) {
			int newByte = 0;
			for(int i=0; i < 8; i++) {
				int pixel = img.getPixel(i*(img.getWidth()/8), j*(img.getHeight()/8));
				float r = cm.getRed(pixel);
				float g = cm.getGreen(pixel);
				float b = cm.getBlue(pixel);
				if((r/255f + g/255f + b/255f)/3f > mean) {
					newByte += 1;
				}
				newByte = newByte << 1;
			}
			output[j] = (byte)newByte;
		}
		
		return output;
	}

	public byte[] getPHash(Pixmap img) {
		// Perform a DCT hash of the specified image.
		byte[] output = new byte[8];
		double[] result = new double[32*32];
		
		// Scale the image to 32x32 and make it greyscale
		//Pixmap tempImg = new Pixmap(32, 32, Format.RGBA8888);
		//tempImg.drawPixmap(img, 0, 0, 0, 0, img.getWidth(), img.getHeight());

		// Compute the DCT
		/*
		for(int y=0; y < tempImg.getHeight(); y++) {
			for(int x=0; x < tempImg.getWidth(); x++) {
				result[y * tempImg.getWidth() + x] = 0;
				for(int v=0; v < tempImg.getHeight(); v++) {
					for(int u=0; u < tempImg.getWidth(); u++) {
						result[y*tempImg.getWidth()+x] += alpha(u)*alpha(v)*tempImg.getRGB(u, v)*altcos(u,x)*altcos(v,y);
					}
				}
			}
		}
		*/
		DirectColorModel cm = new DirectColorModel(32, 0xFF000000, 0x00FF0000, 0x0000FF00, 0x000000FF);
		float lastPixelValue = -1;
		for(int y=0; y < img.getHeight()/8; y++) {
			for(int x=0; x < img.getWidth()/8; x++) {
				result[y*(img.getWidth()/8) + x] = 0;
				for(int v=0; v < img.getHeight()/8; v++) {
					for(int u=0; u < img.getWidth()/8; u++) {
						float rValue = cm.getRed(img.getPixel(u*8,v*8))/255.0f;
						float gValue = cm.getGreen(img.getPixel(u*8,v*8))/255.0f;
						float bValue = cm.getBlue(img.getPixel(u*8,v*8))/255.0f;
						float pixelValue = (rValue+gValue+bValue)/3f;
						if(pixelValue != lastPixelValue) {
							System.out.println("tempImg.getPixel(" + u + "," + v + "): " + pixelValue);
							lastPixelValue = pixelValue;
						}
						
						result[y*(img.getWidth()/8)+x] += pixelValue * Math.cos(Math.PI/((float)(img.getHeight()/8f))*(v+1.0/2.0)*y) * Math.cos(Math.PI/((float)(img.getWidth()/8f))*(u+1.0/2.0)*x);
					}
				}
			}
		}

		// Calculate the mean
		double mean = 0;
		for(int i=0; i < result.length; i++) {
			mean += result[i];
		}
		mean /= result.length;

		// Produce the actual hash.
		for(int i=0; i < 8; i++) { // 8 bits in a byte, last time I checked.
			byte val = 0;
			for(int j=0; j < 8; j++) { // And we only care about the top 8x8 space.
				if(result[i + j*img.getWidth()/8] > mean) { 
					val |= 0x01;
				}
				val = (byte)(val << 1);
			}
			output[i] = val;
		}

		return output;
	}

	public int getDistance(byte[] hashA, byte[] hashB) {
		int distance = 0;
		for(int i=0; i < hashA.length; i++) {
			distance += Integer.bitCount((0xFF&((0xFF&hashA[i])^(0xFF&hashB[i]))));
		}
		System.out.println("Distance: " + distance);
		return distance;
	}

	public float getMaxDistance() {
		return 64f;
	}

	private double alpha(float d) {
		if(d == 0) {
			return Math.sqrt(2)*0.25; // sqrt(2)/2/2;
		}
		return 0.5;
	}

	private double altcos(float a, float b) {
		return Math.cos(Math.PI * a * (2.0*b+1) * (1.0/16));
	}

	public long hashToInteger(byte[] hash) {
		long i = 0;
		for(int a = 0; a < hash.length*8; a++) {
			int byteIndex = a/8;
			int bitIndex = a%8;
			if((hash[byteIndex] & (0x01 << bitIndex)) > 0) {
				i |= 0x01;
			}
			i = i << 1;
		}
		return i;
	}
	
	public String hashToString(byte[] hash) {
		String s = "[";
		for(int i=0; i < hash.length; i++) {
			Byte b = hash[i];
			for(int j = 7; j >= 0; j--) {
				s += ((b>>j)&0x01);
			}
		}
		s += "]";
		return s;
	}
}
