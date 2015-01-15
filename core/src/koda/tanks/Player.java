package koda.tanks;

import koda.tanks.Network.BulletMessage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player extends Entity {
	
	public static final long DEAD_TIME = 1500;
	public static final int MAX_HP = 10;
	public static final float SPEED = 150;
	public static final float ROT_SPEED = 350;
	public static final float DISTANCE = TILESIZE;
	
	public String name;
	public Array<Bullet> bullets;
	public int hp;
	public boolean forward;
	public float lastX;
	public float lastY;
	public float targetX;
	public float targetY;
	public float distAccum;
	boolean inMotion;
	
	private long timeKilled;
	
	public Player(GameLogic game, Sprite sprite, float x, float y, String name) {
		super(game, sprite, x, y);
		this.name = name;
		this.prevAngle = -1;
		bullets = new Array<Bullet>();
		lastX = x;
		lastY = y;
	}
	
	public void addBullet(float x, float y, int dir, String shooter) {
		bullets.add(new Bullet(game, game.sprites.get("bullet"), x, y, dir, shooter));
	}
	
	public void hit(int damage) {
		hp -= damage;
		if (hp <= 0) {
			hp = 0;
			alive = false;
			timeKilled = System.currentTimeMillis();
		}
	}
	
	public boolean canRespawn() {
		return System.currentTimeMillis() >= timeKilled + DEAD_TIME;
	}
	
	/*
	public void checkCollisions(float dt) {

		if (forward) {
			for (int i = 0; i < game.allEntities.size; i++) {
				Entity e = game.allEntities.get(i);
				if (e == this || !e.alive)
					continue;
				
				if (this.collidesWith(e)) {
					System.out.println(name + " colliding with " + e);
					System.out.println("Reverting from ("+x+", "+y+") to ("+lastX+", "+lastY+")");
					if (x == lastX && y == lastY) {
						//attempt a reverse vector!
//						float a = MathUtils.atan2(e.bounds.y - this.bounds.y, e.bounds.x - this.bounds.x);
//						x -= dt * SPEED * MathUtils.cos(a);
//						y -= dt * SPEED * MathUtils.sin(a);
//						x -= dt * SPEED * MathUtils.cosDeg(angle);
//						y -= dt * SPEED * MathUtils.sinDeg(angle);
						fitAround(e);
						updateBounds();
						System.out.println("SHITTO");
						
					} else {
						x = lastX;
						y = lastY;
						updateBounds();
					}
					
//					angle = lastAngle;
					float cos = dt * SPEED * MathUtils.cosDeg(angle);
					float sin = dt * SPEED * MathUtils.sinDeg(angle);
					if (!done && this.collidesWith(e)) {
						System.out.println("Still colliding?!?!");
						done = true;
					}
					
					
					x += cos;
					updateBounds();
					if (this.collidesWith(e)) {
						x = lastX;
						updateBounds();
					}
					
					y += sin;
					updateBounds();
					if (this.collidesWith(e)) {
						y = lastY;
						updateBounds();
					}
				}
			}
			
			//collision should be fixed at this point. if not, wtf
			lastX = x;
			lastY = y;
			updateBounds();
		} else {
			for (int i = 0; i < game.allEntities.size; i++) {
				Entity e = game.allEntities.get(i);
				if (e == this || !e.alive)
					continue;
				
				if (this.collidesWith(e)) {
					System.out.println(name + " colliding with " + e);
					x = lastX;
					y = lastY;
					float cos = dt * SPEED * MathUtils.cosDeg(angle);
					float sin = dt * SPEED * MathUtils.sinDeg(angle);
					if (!done && this.collidesWith(e)) {
						System.out.println("Still colliding?!?!");
						done = true;
					}
					
					
					x += cos;
					if (this.collidesWith(e))
						x = lastX;
					
					y += sin;
					if (this.collidesWith(e))
						y = lastY;
				}
			}
			
			//collision should be fixed at this point. if not, wtf
			lastX = x;
			lastY = y;
		}
		
		setPosition(x, y, angle);
	}*/
	
	public boolean canMove() {
		float nx = bounds().x + TILESIZE * MathUtils.cosDeg(angle);
		float ny = bounds().y + TILESIZE * MathUtils.sinDeg(angle);
		Rectangle tmp = new Rectangle(nx, ny, bounds().width, bounds().height);
		for (Wall w : game.level.walls) {
			if (tmp.overlaps(w.bounds()))
				return false;
		}
		
		targetX = x + TILESIZE * MathUtils.cosDeg(angle);
		targetY = y + TILESIZE * MathUtils.sinDeg(angle);
		
		for (Player p : game.players.values()) {
			if (p == this || !p.alive)
				continue;
			
			if (targetX == p.x && targetY == p.y)
				return false;
		}
		
		return true;
	}
	
	public float normalize(float val) {
		val += TILESIZE / 2;
		int tmp = (int) (val / TILESIZE);
		return tmp * TILESIZE;
	}
	
	//not everything in input is efficient, such as the lastX/Y and targetX/Y
	@Override
	public void input(float dt) {
		//shooting
		if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			addBullet(x, y, angle, name);
			BulletMessage msg = new BulletMessage();
			game.tc.client.sendTCP(msg);
			//move this sound later, should be together with onBulletFired() in GameLogic
			if (!game.windowMinimized)
				game.sounds.get("shoot").play(game.volume);
		}
		
		if (Gdx.input.isKeyJustPressed(Keys.Z)) {
			System.out.println(this);
		}
		
		if (Gdx.input.isKeyJustPressed(Keys.X)) {
			setPosition(289, 164, angle);
		}
		
		//movement version 1
		//W - up, A - left, S - down, D - right
		if (!inMotion) {
			if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) {
				lastY = y;
				angle = UP;
				if (canMove())
					inMotion = true;
			}
			
			else if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
				lastX = x;
				angle = LEFT;
				if (canMove())
					inMotion = true;
			}
			
			else if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) {
				lastY = y;
				angle = DOWN;
				if (canMove())
					inMotion = true;
			}
			
			else if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
				lastX = x;
				angle = RIGHT;
				if (canMove())
					inMotion = true;
				//THIS SYSTEM ISN'T PERFECT. FIX IT
			}
		}
		
		setPosition(x, y, angle);
		
		//movement, version 2
		//W - forward, A/D - rotate left/right, S - backward
//		if (Gdx.input.isKeyPressed(Keys.W)) {
//			lastX = x;
//			lastY = y;
//			x += dt * SPEED * MathUtils.cosDeg(angle);
//			y += dt * SPEED * MathUtils.sinDeg(angle);
//			forward = true;
//		}
//		
//		if (Gdx.input.isKeyPressed(Keys.S)) {
//			lastX = x;
//			lastY = y;
//			x -= dt * SPEED * MathUtils.cosDeg(angle);
//			y -= dt * SPEED * MathUtils.sinDeg(angle);
//			forward = false;
//		}
//		
//		if (Gdx.input.isKeyPressed(Keys.A)) {
//			angle += dt * ROT_SPEED;
//		}
//		
//		if (Gdx.input.isKeyPressed(Keys.D)) {
//			angle -= dt * ROT_SPEED;
//		}
		
		
		
//		if (angle != prevAngle)
//			rotateSprite(angle);
	}

	@Override
	public void update(float dt) {
		if (inMotion) {
			float tx = x;
			float ty = y;
			x += dt * SPEED * MathUtils.cosDeg(angle);
			y += dt * SPEED * MathUtils.sinDeg(angle);
			distAccum += (float) Math.hypot(x - tx, y - ty);
			//temporary
			
			if (distAccum >= DISTANCE) {
				distAccum = 0;
				x = normalize(x);
				y = normalize(y);
				inMotion = false;
			}
			
			if (!game.isServer) {
				game.levelCam.position.set(x, y, 0);
				game.levelCam.update();
			}
		}
		
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
	
	@Override
	public String toString() {
		return "Player \"" + name + "\": (" + x + ", " + y + "), angle = " + correctedAngle();
	}
}
