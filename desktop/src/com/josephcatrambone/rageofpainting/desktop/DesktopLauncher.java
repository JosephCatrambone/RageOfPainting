package com.josephcatrambone.rageofpainting.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.josephcatrambone.rageofpainting.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = Game.VIRTUAL_WIDTH*Game.SCREEN_SCALE;
		config.height = Game.VIRTUAL_HEIGHT*Game.SCREEN_SCALE;
		new LwjglApplication(new Game(), config);
	}
}
