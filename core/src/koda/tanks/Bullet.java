package koda.tanks;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Bullet extends Entity {

	public static final float SPEED = 300;
	public static final float REDUCTION = 15;
	
	public String shooter;
	
	public Bullet(GameLogic game, Sprite sprite, float x, float y, int angle, String shooter) {
		super(game, sprite, x, y);
		this.angle = angle;
		this.shooter = shooter;
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
	public void update(float dt) {
		x += dt * SPEED * MathUtils.cosDeg(angle);
		y += dt * SPEED * MathUtils.sinDeg(angle);
		
		if (outOfBounds())
			alive = false;
	}
}
