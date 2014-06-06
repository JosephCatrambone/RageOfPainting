package com.josephcatrambone.rageofpainting.states;

import com.josephcatrambone.rageofpainting.Game;
import com.josephcatrambone.rageofpainting.handlers.GameStateManager;

public abstract class GameState {
	protected GameStateManager stateManager;
	protected Game game;
	
	public GameState(GameStateManager gsm) {
		stateManager = gsm;
		game = stateManager.getGame();
	}
	
	public abstract void update(float dt);
	public abstract void render(float dt);
	public abstract void dispose();
}