package com.josephcatrambone.rageofpainting.states;

import com.josephcatrambone.rageofpainting.Game;
import com.josephcatrambone.rageofpainting.handlers.GameStateManager;

public abstract class GameState {
	public GameState() {}
	
	public abstract void update(float dt);
	public abstract void render(float dt);
	public abstract void dispose();
}