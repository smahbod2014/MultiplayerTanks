package koda.tanks;

import koda.tanks.Network.BulletMessage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Player extends Entity {
	
	public static final int MAX_HP = 10;
	public String name;
	public Array<Bullet> bullets;
	public int hp;
	
	public Player(GameLogic game, Sprite sprite, float x, float y, String name) {
		super(game, sprite, x, y);
		this.name = name;
		this.prevDir = -1;
		bullets = new Array<Bullet>();
	}
	
//	public int getDir() {
//		return tankAnim.frame();
//	}
//	
//	public void setDir(int frame) {
//		tankAnim.setFrame(frame);
//	}
	
	@Override
	public boolean changed() {
		if (x != prevX || y != prevY || dir != prevDir) {
			clean();
			return true;
		}
		
		return false;
	}
	
	@Override
	protected void clean() {
		prevX = x;
		prevY = y;
		prevDir = dir;
	}
	
	public void addBullet(float x, float y, int dir, String shooter) {
		bullets.add(new Bullet(game, game.sprites.get("bullet"), x, y, dir, shooter));
	}
	
	public void hit(int damage) {
		hp -= damage;
	}
	
	@Override
	public void input() {
		//shooting
		if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			addBullet(x, y, dir, name);
			BulletMessage msg = new BulletMessage();
			msg.x = x;
			msg.y = y;
			msg.name = name;
			msg.dir = dir;
			msg.newBullet = true;
			game.tc.client.sendTCP(msg);
		}
		
		if (Gdx.input.isKeyJustPressed(Keys.Z)) {
			System.out.println("Our x is " + x);
			System.out.println("Sprite's x is " + sprite.getX());
		}
		
		//movement
		if (Gdx.input.isKeyJustPressed(Keys.W)) {
			if (dir == UP)
				y += TILESIZE;
			else
				dir = UP;
		}
		
		if (Gdx.input.isKeyJustPressed(Keys.A)) {
			if (dir == LEFT)
				x -= TILESIZE;
			else
				dir = LEFT;
		}
		
		if (Gdx.input.isKeyJustPressed(Keys.S)) {
			if (dir == DOWN)
				y -= TILESIZE;
			else
				dir = DOWN;
		}
		
		if (Gdx.input.isKeyJustPressed(Keys.D)) {
			if (dir == RIGHT)
				x += TILESIZE;
			else
				dir = RIGHT;
		}
		
		rotateSprite(dir);
	}

	@Override
	public void update(float dt) {
		super.update(dt);
		for (int i = bullets.size - 1; i >= 0; i--) {
			Bullet b = bullets.get(i);
			b.update(dt);
			if (!b.alive) {
				bullets.removeIndex(i);
			}
		}
	}
	
	@Override
	public void render(SpriteBatch batch) {
		super.render(batch);
		for (Bullet b : bullets)
			b.render(batch);
	}
	
	/**
	 * Do not call batch.begin() before calling this method
	 * @param nameTag
	 * @param hpTag
	 * @param batch
	 */
	public void drawTags(BitmapFont nameTag, BitmapFont hpTag, SpriteBatch batch) {
		String hpString = hp + "/" + MAX_HP;
		float nameX = x + TILESIZE / 2 - nameTag.getBounds(name).width / 2;
		float nameY = y + 2.5f * TILESIZE;
		float hpX = x + TILESIZE / 2 - hpTag.getBounds(hpString).width / 2;
		float hpY = y + 1.5f * TILESIZE;
		batch.begin();
		nameTag.draw(batch, name, nameX, nameY);
		hpTag.draw(batch, hpString, hpX, hpY);
		batch.end();
	}
}
