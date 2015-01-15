package koda.tanks;

import koda.tanks.Network.BulletMessage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player extends Entity {
	
	public static final long DEAD_TIME = 1500;
	public static final long SHOOT_TIME = 250;
	public static final long RESPAWN_INVULN_TIME = 2000;
	public static final long FLASHING_TIME = 125;
	public static final long FLASHING_TIME2 = 62;
	public static final long ATTACKED_FLASH_TIME = 125;
	public static final int MAX_HP = 10;
	public static final float SPEED = 150;
	public static final float ROT_SPEED = 350;
	public static final float DISTANCE_TO_MOVE = TILESIZE;
	public static final long ATTACKED_FLASH_NUM = ATTACKED_FLASH_TIME / FLASHING_TIME2;
	public static final long RESPAWNING_FLASH_NUM = RESPAWN_INVULN_TIME / FLASHING_TIME;
	
	public String name;
	public Array<Bullet> bullets;
	public int hp;
	public boolean forward;
	public float lastX;
	public float lastY;
	public float targetX;
	public float targetY;
	public float distAccum;
	public boolean inMotion;
	public boolean invulnerable;
	public boolean gotHit;
	
	public SpriteBatch psb;
	public ShaderProgram shader;
	
	public Player(GameLogic game, Sprite sprite, float x, float y, String name) {
		super(game, sprite, x, y);
		this.name = name;
		bullets = new Array<Bullet>();
		timers.addTimer("shooting", SHOOT_TIME);
		timers.addTimer("dead", DEAD_TIME);
		timers.addTimer("flashing", FLASHING_TIME);
		timers.addTimer("flashing2", FLASHING_TIME2);
		timers.addTimer("respawning", RESPAWN_INVULN_TIME);
		
		if (!game.isServer) {
			psb = new SpriteBatch();
			ShaderProgram.pedantic = false;
			shader = new ShaderProgram(Gdx.files.internal("shaders/flash.vsh"), Gdx.files.internal("shaders/flash.fsh"));
			psb.setShader(shader);
		}
	}
	
	public void addBullet(float x, float y, int dir, String shooter) {
		Sprite spr = game.res == null ? null : game.res.getSprite("bullet");
		bullets.add(new Bullet(game, spr, x, y, dir, shooter));
	}
	
	public void hit(int damage) {
		if (invulnerable)
			return;
		
		hp -= damage;
		timers.reset("flashing2");
		timers.setCount(0);
		gotHit = true;
		if (hp <= 0) {
			hp = 0;
			alive = false;
			timers.reset("dead");
		}
	}
	
	public boolean canRespawn() {
		return timers.finished("dead");
	}
	
	public boolean canMove() {
		float nx = bounds().x + TILESIZE * MathUtils.cosDeg(angle);
		float ny = bounds().y + TILESIZE * MathUtils.sinDeg(angle);
		Rectangle tmp = new Rectangle(nx, ny, bounds().width, bounds().height);
		for (Wall w : game.level.walls) {
			if (tmp.overlaps(w.bounds()))
				return false;
		}
		
		nx = x + TILESIZE * MathUtils.cosDeg(angle);
		ny = y + TILESIZE * MathUtils.sinDeg(angle);
		
		for (Player p : game.players.values()) {
			if (p == this || !p.alive)
				continue;
			
			if (nx == p.x && ny == p.y)
				return false;
			
			if (nx == p.lastX && ny == p.lastY)
				return false;
		}
		
		targetX = nx;
		targetY = ny;
		
		return true;
	}
	
	public float normalize(float val) {
		val += TILESIZE / 2;
		int tmp = (int) (val / TILESIZE);
		return tmp * TILESIZE;
	}
	
	public void respawn(float x, float y, int angle) {
		alive = true;
		this.x = x;
		this.y = y;
		this.angle = angle;
		hp = MAX_HP;
		lastX = x;
		lastY = y;
		timers.reset("respawning");
		timers.setCount(0);
		invulnerable = true;
	}
	
	private void flash(final float f) {
		if (game.isServer)
			return;
		
		shader.begin();
		shader.setUniformf("u_flasher", f, f, f, 1);
		shader.end();
	}
	
	@Override
	public void input(float dt) {
		//shooting
		if (alive && timers.finished("shooting") && Gdx.input.isKeyPressed(Keys.SPACE)) {
			timers.reset("shooting");
			addBullet(x, y, angle, name);
			BulletMessage msg = new BulletMessage();
			game.tc.client.sendTCP(msg);
			//move this sound later, should be together with onBulletFired() in GameLogic
			game.res.playSound("shoot", game.volume);
		}
		
		if (Gdx.input.isKeyJustPressed(Keys.Z)) {
			System.out.println(this);
		}
		
		//movement version 1
		//W - up, A - left, S - down, D - right
		if (alive && !inMotion) {
			if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) {
				//setting lastX/Y here likely useless, see canMove() for why
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
	}
	
	private void flashing() {
		if (game.isServer && gotHit)
			gotHit = false;
		
		if (gotHit) {
			if (timers.justStarted("flashing2") && timers.count("flashing2") == 0)
				flash(.7f);
			
			if (timers.poll("flashing2")) {
				timers.reset("flashing2");
				if (timers.count("flashing2") % 2 == 0) {
					flash(.7f);
				} else {
					flash(0);
				}
				
				if (timers.count("flashing2") == ATTACKED_FLASH_NUM - 1) {
					timers.setCount("flashing2", 0);
					gotHit = false;
				}
			}
		}
		
		if (invulnerable) {
			if (timers.justStarted("flashing") && timers.count("flashing") == 0)
				flash(.7f);
			
			if (timers.poll("flashing")) {
				timers.reset("flashing");
				if (timers.count("flashing") % 2 == 0) {
					flash(.7f);
				} else {
					flash(0);
				}
				
				if (timers.count("flashing") == RESPAWNING_FLASH_NUM - 1) {
					timers.setCount("flashing", 0);
					invulnerable = false;
				}
			}
		}
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
			
			if (distAccum >= DISTANCE_TO_MOVE) {
				distAccum = 0;
				x = normalize(x);
				y = normalize(y);
				lastX = x;
				lastY = y;
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
		
		flashing();
	}
	
	@Override
	public void render(SpriteBatch batch) {
		psb.setProjectionMatrix(game.levelCam.combined);
		super.render(psb);
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
