package com.josephcatrambone.rageofpainting.handlers;

import java.util.Stack;

import com.josephcatrambone.rageofpainting.Game;
import com.josephcatrambone.rageofpainting.states.*;

public class GameStateManager {
	private Game game;
	private Stack<GameState> states;
	
	public GameStateManager(Game game) {
		this.game = game;
		this.states = new Stack<GameState>();
	}
	
	public Game getGame() {
		return game;
	}
	
	public GameState getState() {
		return states.peek();
	}
	
	public void pushState(GameState state) {
		states.push(state);
	}
	
	public void popState() {
		GameState g = states.pop();
		g.dispose();
	}
	
	public void setState(GameState state) {
		popState();
		pushState(state);
	}
	
	public void update(float dt) {
		states.peek().update(dt);
	}
	
	public void render(float dt) {
		states.peek().render(dt);
	}
	
	public void pause() {
		// TODO: Pause
	}
	
	public void resume() {
		// TODO: Resume
	}
	
	public void dispose() {
		// TODO: Dispose
	}
}
