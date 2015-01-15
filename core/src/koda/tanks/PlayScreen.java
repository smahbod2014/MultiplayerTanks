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
	
	public PlayScreen(boolean isServer, String host, String name) {
		Gdx.app.setLogLevel(Log.LEVEL_DEBUG);
		this.host = host;
		this.isServer = isServer;
		this.name = name;
	}
	
	@Override
	public void render(float dt) {
		//input
		game.input(dt);
		
		//update
		game.update(dt);
		if (isServer)
			ts.game.update(dt);
		
		//render
		game.render(batch);
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
