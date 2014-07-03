package com.josephcatrambone.rageofpainting.entities;

import java.util.concurrent.Callable;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.josephcatrambone.rageofpainting.handlers.InputManager;

public class Button implements Renderable, Updateable {
	private static BitmapFont font = null;
	public Texture releasedTexture = null;
	public Texture pushedTexture = null; // If null, will use releasedTexture for all drawing.
	public String text = "";
	public int shortcut = -1;
	public Runnable onRelease = null;
	public Vector2 position = null;
	public Vector2 textOffset = null;
	
	private boolean wasPressed;
	
	public Button(Texture texture, float x, float y, Runnable onRelease) {
		if(Button.font == null) { font = new BitmapFont(); }
		this.releasedTexture = texture;
		this.position = new Vector2(x, y);
		this.onRelease = onRelease;
		this.textOffset = new Vector2(0,0);
	}
	
	public Button(Texture texture, float x, float y, String txt, Runnable onRelease) {
		if(Button.font == null) { font = new BitmapFont(); }
		this.releasedTexture = texture;
		this.position = new Vector2(x, y);
		this.onRelease = onRelease;
		this.setText(txt);
	}
	
	public void render(SpriteBatch sb) {
		if(!wasPressed || pushedTexture == null) {
			sb.draw(releasedTexture, position.x, position.y);
		} else {
			sb.draw(pushedTexture, position.x, position.y);
		}
		font.draw(sb, text, position.x + textOffset.x, position.y + textOffset.y);
	}
	
	public void update(float dt) {
		float x = InputManager.getPosition("HORIZONTAL");
		float y = InputManager.getPosition("VERTICAL");
		
		// Check to see if the button was pressed.
		if(InputManager.isMouseDown(0) && isInside(x, y)) {
			wasPressed = true;
		}
		
		if(wasPressed && !InputManager.isMouseDown(0) && !isInside(x, y)) {
			wasPressed = false; // Released outside.
		}
		
		if((wasPressed && !InputManager.isMouseDown(0) && isInside(x, y)) || (shortcut != -1 && InputManager.isKeyReleased(shortcut))) {
			onRelease.run();
			wasPressed = false; // Released.
		}
	}
	
	private boolean isInside(float x, float y) {
		return (x > position.x && y > position.y && x < position.x+releasedTexture.getWidth() && y < position.y+releasedTexture.getHeight());
	}
	
	public void setText(String txt) {
		this.text = txt;
		textOffset = new Vector2(
				releasedTexture.getWidth()/2 - font.getMultiLineBounds(txt).width/2,
				releasedTexture.getHeight()/2 - font.getMultiLineBounds(txt).height/2
		);
	}
}
