package com.josephcatrambone.rageofpainting.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.josephcatrambone.rageofpainting.handlers.InputManager;

public class Canvas implements Updateable, Renderable{

	private Vector2 position;
	private Vector2 size;
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
			float x = InputManager.getPosition("HORIZONTAL");
			float y = InputManager.getPosition("VERTICAL");
			if(isInside(x, y)) {
				canvas.setColor(brushColor);
				// The canvas is actually 'upside down'.  <0,0> is the top left and y grows down.
				int xPrime = (int)x-(brushSize/2)-(int)position.x;
				int yPrime = canvas.getHeight()-((int)y-(brushSize/2)-(int)position.y);				
				canvas.fillRectangle(xPrime, yPrime, brushSize, brushSize);
			}
		}
	}
	
	private boolean isInside(float x, float y) {
		return (x > position.x && y > position.y && x < position.x + size.x && y < position.y + size.y);
	}
	
	public Pixmap getCopyOfPixmap() {
		Pixmap clone = new Pixmap(canvas.getWidth(), canvas.getHeight(), Format.RGBA8888);
		clone.drawPixmap(canvas, 0, 0);
		return clone;
	}

}
