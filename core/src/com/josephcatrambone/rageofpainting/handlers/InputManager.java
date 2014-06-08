package com.josephcatrambone.rageofpainting.handlers;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.josephcatrambone.rageofpainting.Game;

public class InputManager extends InputAdapter {
	public static final int NUM_KEYS = 256;
	public static final int NUM_BUTTONS = 3;
	// We have two ways of handling input: Low-level names and high-level names. 
	// Low-level means directly accessing the KeyCodes.  High level means binding inputs to full names like 'Jump'.
	private static HashMap <String, Integer> keymap; // The 'jump' to 'space' mapping.
	private static boolean[] previousKeyboardState; // The keys that were pressed last update
	private static boolean[] currentKeyboardState; // The keys pressed this update
	private static Vector2 dragStart; // Will be null if there is no drag.
	private static Vector2 lastMouse; // Updated each update.  Used to calculate drag since last frame.
	private static Vector2 mouseDelta; // Track the mouse change before updating last mouse
	private static boolean[] previousMouseState;
	private static boolean[] currentMouseState;
	
	/*
	public static void init() {
		previousState = new boolean[256];
		currentState = new boolean[256];
		keymap = new HashMap<String, Integer>();
	}
	*/
	
	static {
		previousKeyboardState = new boolean[NUM_KEYS];
		currentKeyboardState = new boolean[NUM_KEYS];
		keymap = new HashMap<String, Integer>();
		previousMouseState = new boolean[NUM_BUTTONS]; // Also applies for pointers 1, 2, and 3
		currentMouseState = new boolean[NUM_BUTTONS]; // Also applies for pointers 1, 2, and 3
		lastMouse = new Vector2(0, 0);
	}
	
	public static void bindKey(String name, int key) {
		keymap.put(name,  key);
	}
	
	public static void unbindKey(String name) {
		keymap.remove(name);
	}
	
	public static int getKey(String name) {
		return keymap.get(name);
	}
	
	public static void update() {
		// Handle keyboard state changes
		for(int i=0; i < NUM_KEYS; i++) {
			previousKeyboardState[i] = currentKeyboardState[i];
			currentKeyboardState[i] = Gdx.input.isKeyPressed(i); // True == down
		}
		
		// Handle mouse state changes
		boolean digitDown = false;
		for(int i=0; i < NUM_BUTTONS; i++) {
			previousMouseState[i] = currentMouseState[i];
			if(Gdx.input.isButtonPressed(i) || Gdx.input.isTouched(i)) {
				digitDown = true;
				currentMouseState[i] = true;
			} else {
				currentMouseState[i] = false;
			}
		}
		// Handle the drag
		if(digitDown) {
			if(dragStart == null) { // Drag was not started.
				dragStart = new Vector2(Gdx.input.getX(), Gdx.input.getY());
			}
		} else {
			dragStart = null; // Clear the drag start.
		}
		// Calculate delta
		mouseDelta = new Vector2(lastMouse.x - Gdx.input.getX(), lastMouse.y - Gdx.input.getY());
		lastMouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
	}
	
	// KEYBOARD
	/*** isPressed
	 * Returns true if the specified key was pressed.
	 * @param name
	 * @return
	 */
	public static boolean isKeyPressed(String name) {
		int k = keymap.get(name); // TODO: Check for not found.
		return isKeyPressed(k);
	}
	
	public static boolean isKeyPressed(int key) {
		// TODO: Assert range.
		return !previousKeyboardState[key] && currentKeyboardState[key];
	}
	
	/*** isReleased
	 * Returns true if the specified key was released this last update.
	 * @param name
	 * @return
	 */
	public static boolean isKeyReleased(String name) {
		int k = keymap.get(name); // TODO: Check for not found.
		return isKeyReleased(k);
	}
	
	public static boolean isKeyReleased(int key) {
		return previousKeyboardState[key] && !currentKeyboardState[key];
	}
	
	public static boolean isKeyDown(String name) {
		return currentKeyboardState[keymap.get(name)];
	}
	
	public static boolean isKeyDown(int key) {
		return currentKeyboardState[key];
	}
	
	public static boolean isKeyUp(String name) {
		return !currentKeyboardState[keymap.get(name)];
	}
	
	public static boolean isKeyUp(int key) {
		return !currentKeyboardState[key];
	}
	
	// Required keyboard binds
	/*** keyDown
	 * Don't use this.
	 * Included so this can be added as an input handler to libGdx.
	 * If we decide to do so, add `Gdx.input.setInputProcessor(new KeyboardInputHandler());` to Game.
	 * If unused, we'll do input polling.
	 */
	public boolean keyDown(int k) {
		previousKeyboardState[k] = currentKeyboardState[k];
		currentKeyboardState[k] = true;
		return true;
	}
	
	/*** keyUp
	 * Don't use this.
	 * Included so this can be added as an input handler to libGdx.
	 */
	public boolean keyUp(int k) {
		previousKeyboardState[k] = currentKeyboardState[k];
		currentKeyboardState[k] = false;
		return true;
	}
	
	// Mouse
	// NOTE: These mouse functions assume the origin is in the bottom left.
	
	/*** getDrag
	 * @return Returns NULL if there is no mouse drag, otherwise returns the origin of the mouse drag.
	 */
	public static Vector2 getDragOrigin() {
		return dragStart;
	}
	
	public static Vector2 getMouseDelta() {
		return mouseDelta;
	}
	
	// TODO: Replace with camera unproject?
	public static float getAxis(String axis) {
		if("HORIZONTAL".equals(axis)) {
			return 2.0f*((float)Gdx.input.getX()/(Game.VIRTUAL_WIDTH)) - 1.0f;
		} else if("VERTICAL".equals(axis)) {
			return -2.0f*((float)Gdx.input.getY()/(Game.VIRTUAL_HEIGHT)) + 1.0f;
		} else {
			System.err.println("Unrecognized axis passed into getAxis function: " + axis);
			return 0;
		}
	}
	
	public static float getPosition(String axis) {
		if("HORIZONTAL".equals(axis)) {
			return (float)Gdx.input.getX();
		} else if("VERTICAL".equals(axis)) {
			return Game.VIRTUAL_HEIGHT-(float)Gdx.input.getY();
		} else {
			System.err.println("Unrecognized axis passed into getAxis function: " + axis);
			return 0;
		}
	}
	
	public static boolean isMousePressed(int button) {
		return !previousMouseState[button] && currentMouseState[button];
	}
	
	public static boolean isMouseDown(int button) {
		return currentMouseState[button];
	}
	
}