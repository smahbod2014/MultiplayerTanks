package koda.tanks;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Tanks extends Game {
	
	SpriteBatch batch;
	MenuScreen menu;
	
	@Override
	public void create() {
		batch = new SpriteBatch();
		menu = new MenuScreen(this);
		setScreen(menu);
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render();
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		getScreen().dispose();
	}
}
