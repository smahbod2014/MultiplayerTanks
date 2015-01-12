package koda.tanks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PlayScreen implements Screen {

	static TanksServer ts;
	static TanksClient tc;
	final String host;
	final boolean isServer;
	final String name;
	SpriteBatch batch;
	static OrthographicCamera camera;
	GameLogic game;
	long hitMessageDuration = 2000;
	
	Texture test = new Texture(Gdx.files.internal("images/missile.png"));
	BitmapFont text = new BitmapFont(Gdx.files.internal("comicsans.fnt"));
	
	public PlayScreen(boolean isServer, String host, String name) {
		this.host = host;
		this.isServer = isServer;
		this.name = name;
	}
	
	@Override
	public void render(float dt) {
		//input
		game.input();
		
		//update
		game.update(dt);
		if (isServer)
			ts.game.update(dt);
		
		//render
		game.render(batch);
		
		if (tc.alive) {
			batch.begin();
			text.draw(batch, "Connected", 2, 25);
			batch.end();
		} else {
			batch.begin();
			text.draw(batch, "No connection!", 2, 25);
			batch.end();
		}
		
		if (System.currentTimeMillis() < tc.timeSpecialTextSet + hitMessageDuration) {
			batch.begin();
			text.draw(batch, tc.specialText, 2, 50);
			batch.end();
		}
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.position.setZero();
		camera.translate(width / 2, height / 2);
		camera.update();
		
		Entity.screen.width = width;
		Entity.screen.height = height;
	}

	@Override
	public void show() {
		tc = new TanksClient(name);
		if (isServer) {
			ts = new TanksServer();
		}
		
		tc.connect(host);
		
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		game = tc.game;
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		if (isServer) {
			ts.shutdown();
		}
		
		tc.shutdown();
	}
}
