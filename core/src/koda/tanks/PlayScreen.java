package koda.tanks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.esotericsoftware.minlog.Log;

public class PlayScreen implements Screen {

	TanksServer ts;
	TanksClient tc;
	final String host;
	final boolean isServer;
	final String name;
	SpriteBatch batch;
	GameLogic game;
	String title;
	
	public PlayScreen(boolean isServer, String host, String name, String title) {
		this.host = host;
		this.isServer = isServer;
		this.name = name;
		this.title = title;
	}
	
	@Override
	public void render(float dt) {
		//input
		game.input(dt);
		if (isServer)
			ts.game.botInput(dt);
		
		//update
		game.update(dt);
		if (isServer)
			ts.game.update(dt);
		
		//render
		game.render(batch);
		
		Gdx.graphics.setTitle("FPS: " + Gdx.graphics.getFramesPerSecond() + " - " + title);
	}

	@Override
	public void resize(int width, int height) {
		game.resize(width, height);
	}

	@Override
	public void show() {
		tc = new TanksClient(name);
		if (isServer) {
			ts = new TanksServer();
		}
		
		tc.connect(host);
		
		batch = new SpriteBatch();
		game = tc.game;
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void pause() {
		game.windowMinimized = true;
	}

	@Override
	public void resume() {
		game.windowMinimized = false;
	}

	@Override
	public void dispose() {
		if (isServer) {
			ts.shutdown();
		}
		
		tc.shutdown();
	}
}
