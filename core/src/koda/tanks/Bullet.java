package koda.tanks;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Bullet extends Entity {

	public static final float SPEED = 300;
	
	public String shooter;
	
	public Bullet(GameLogic game, Sprite sprite, float x, float y, int dir, String shooter) {
		super(game, sprite, x, y);
		this.dir = dir;
		this.shooter = shooter;
		rotateSprite(dir);
	}

	@Override
	public void update(float dt) {
		super.update(dt);
		switch (dir) {
		case UP:
			y += dt * SPEED;
			break;
		case LEFT:
			x -= dt * SPEED;
			break;
		case DOWN:
			y -= dt * SPEED;
			break;
		case RIGHT:
			x += dt * SPEED;
			break;
		}
		
		if (outOfBounds())
			alive = false;
	}
}
