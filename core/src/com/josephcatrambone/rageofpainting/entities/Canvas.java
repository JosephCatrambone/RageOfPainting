package com.josephcatrambone.rageofpainting.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.josephcatrambone.rageofpainting.handlers.InputManager;

public class Canvas implements Updateable, Renderable{
	public int INTERPOLATION_LEVEL = 1;
	
	private Vector2 position;
	private Vector2 size;
	private Vector2 lastBrushMark = null; // We use this to help interpolate on fast strokes.
	private Texture texture;
	private Pixmap canvas;
	public int brushColor;
	public int brushSize;
	
	public Canvas(float x, float y, int width, int height) {
		position = new Vector2(x, y);
		size = new Vector2(width, height);
		canvas = new Pixmap(width, height, Format.RGBA8888);
		texture = new Texture(canvas);
		
		// Clear canvas, set brush size and color to zero.
		canvas.setColor(Color.WHITE);
		canvas.fillRectangle(0, 0, width, height);
		brushSize = 0;
		brushColor = 0;
	}
	
	public void finalize() {
		// If we kill the game prior to the render call completing, we still need to deallocate the graphics memory.
		canvas.dispose();
		texture.dispose();
	}
	
	@Override
	public void render(SpriteBatch batch) {
		texture.draw(canvas, 0, 0);
		batch.draw(texture, position.x, position.y);
	}

	@Override
	public void update(float deltaTime) {
		if(InputManager.isMouseDown(0)) {
			// We're going to draw several marks between the start brush stroke and the dest.
			// OpenGL (or really the PixMap has no way to change line thicknes.
			// So we're just going to add a bunch of dots.
			Vector2 toMark = new Vector2(InputManager.getPosition("HORIZONTAL"), InputManager.getPosition("VERTICAL"));
			if(lastBrushMark == null) { lastBrushMark = toMark; }
			Vector2 fromMark = lastBrushMark;
			for(int i=0; i < INTERPOLATION_LEVEL; i++) {
				float x = (toMark.x - fromMark.x)*((float)i/(float)INTERPOLATION_LEVEL) + fromMark.x;
				float y = (toMark.y - fromMark.y)*((float)i/(float)INTERPOLATION_LEVEL) + fromMark.y;
				if(isInside(x, y)) { 
					canvas.setColor(brushColor);
					// The canvas is actually 'upside down'.  <0,0> is the top left and y grows down.
					int xPrime = (int)x-(brushSize/2)-(int)position.x;
					int yPrime = canvas.getHeight()-((int)y-(brushSize/2)-(int)position.y);				
					canvas.fillRectangle(xPrime, yPrime, brushSize, brushSize);
				}
			}
			lastBrushMark = toMark;
		} else {
			lastBrushMark = null;
		}
	}
	
	private boolean isInside(float x, float y) {
		System.out.println(x + ", " + y + " vs " + this.position.x + ", " + this.position.y + ", " + this.size.x + ", " + this.size.y);
		return (x > position.x && y > position.y && x < position.x + size.x && y < position.y + size.y);
	}
	
	public Pixmap getCopyOfPixmap() {
		Pixmap clone = new Pixmap(canvas.getWidth(), canvas.getHeight(), Format.RGBA8888);
		clone.drawPixmap(canvas, 0, 0);
		return clone;
	}

}
