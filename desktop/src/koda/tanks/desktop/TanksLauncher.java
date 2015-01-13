package koda.tanks.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import koda.tanks.Tanks;

public class TanksLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 480;
		config.height = 360;
		config.title = "Multiplayer Tanks";
//		config.y = 30;
		config.resizable = false;
		new LwjglApplication(new Tanks(), config);
	}
}
