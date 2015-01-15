package koda.tanks.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import koda.tanks.Tanks;

public class TanksLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 600;
		config.height = 450;
		config.title = "Multiplayer Tanks";
		config.y = 30;
		config.resizable = false;
		new LwjglApplication(new Tanks(), config);
	}
}
