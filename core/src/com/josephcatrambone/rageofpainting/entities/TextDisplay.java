package com.josephcatrambone.rageofpainting.entities;

import java.awt.Font;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TextDisplay implements Updateable, Renderable {
	// Specified
	public BitmapFont font;
	private String text;
	private float x, y, width, height;
	private float accumulatedTime;
	private float revealTime;
	private float holdTime;
	
	// Calculated
	private Texture background;
	private float percentComplete;
	
	public TextDisplay(int x, int y, int width, int height, BitmapFont font) {
		this.text = "";
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.revealTime = 1.0f;
		this.holdTime = -1f;
		this.font = font;
		accumulatedTime = 0;
		
		setTransparency(0.5f);
	}
	
	public void setTransparency(float transparency) {
		if(background != null) { background.dispose(); background = null; }
		
		Pixmap bg = new Pixmap((int)width, (int)height, Pixmap.Format.RGBA4444);
		bg.setColor(0f, 0f, 0f, 0.5f);
		bg.fillRectangle(0, 0, (int)width, (int)height);
		background = new Texture(bg);
		bg.dispose();
	}
	
	public void setText(String str) {
		setText(str, 0, -1);
	}
	
	public void setText(String str, float revealTime, float holdTime) {
		this.accumulatedTime = 0;
		this.revealTime = revealTime;
		this.holdTime = holdTime;
		this.text = str;
	}

	@Override
	public void render(SpriteBatch batch) {
		// Draw backdrop.
		batch.draw(background, x, y);
		
		if(text.equals("")) {
			return;
		}
		
		// Calculate length of the sequence (and size).
		CharSequence str = text.subSequence(0, (int)(text.length()*percentComplete));
		font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		font.drawWrapped(batch, str, x, y+height, width);
	}

	@Override
	public void update(float deltaTime) {
		accumulatedTime += deltaTime;
		
		// If the display time is negative or zero, just display it all from the start.
		if(revealTime <= 0) {
			percentComplete = 1.0f;
		} else {
			percentComplete = Math.min(1.0f, accumulatedTime/revealTime);
		}
		
		// If our hold time is negative one, clear it as soon as we're done displaying all of it.
		if(holdTime == -1) {
			// Do nothing.
		} else {
			if(accumulatedTime > revealTime + holdTime) {
				setText("");
			}
		}
	}
	
	public void dispose() {
		background.dispose();
	}
}
