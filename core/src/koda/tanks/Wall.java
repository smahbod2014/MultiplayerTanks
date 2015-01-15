package koda.tanks;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Wall extends Entity {

	public static final float REDUCTION = 4;
	
	BitmapFont f = new BitmapFont();
	public int id;
	
	public Wall(GameLogic game, Sprite sprite, float x, float y) {
		super(game, sprite, x, y);
		bounds.width = TILESIZE - REDUCTION;
		bounds.height = TILESIZE - REDUCTION;
	}
	
	@Override
	protected Rectangle bounds() {
		bounds.x = x + REDUCTION / 2;
		bounds.y = y + REDUCTION / 2;
		return bounds;
	}
	
	@Override
	public void render(SpriteBatch batch) {
		super.render(batch);
		if (DEBUG) {
			batch.begin();
			float tx = bounds.x + f.getBounds(Integer.toString(id)).width / 2;
			float ty = bounds.y + bounds.height - f.getBounds(Integer.toString(id)).height / 2;
			f.draw(batch, Integer.toString(id), tx, ty);
			batch.end();
		}
	}
	
	@Override
	public String toString() {
		return "Wall " + id + ": " + super.toString();
	}
}
