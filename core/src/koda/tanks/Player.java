package koda.tanks;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

import koda.tanks.Network.BulletMessage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Player extends Entity {
	
	public static final long DEAD_TIME = 1500;
	public static final long SHOOT_TIME = 250;
	public static final long BOT_SHOOT_TIME = SHOOT_TIME * 2;
	public static final long RESPAWN_INVULN_TIME = 2000;
	public static final long FLASHING_TIME = 125;
	public static final long FLASHING_TIME2 = 62;
	public static final long ATTACKED_FLASH_TIME = 125;
	public static final int MAX_HP = 10;
	public static final int BOT_SHOOT_DISTANCE = 10;
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
	public boolean isBot;
	
	public SpriteBatch psb;
	public ShaderProgram shader;
	
	public ArrayList<Vector2> path = new ArrayList<Vector2>();
	public static final boolean BOT_DEBUG = false;
	
	public Player(GameLogic game, Sprite sprite, float x, float y, String name, boolean isBot) {
		super(game, sprite, x, y);
		this.name = name;
		targetX = x;
		targetY = y;
		lastX = x;
		lastY = y;
		this.isBot = isBot;
		bullets = new Array<Bullet>();
		if (!isBot)
			timers.addTimer("shooting", SHOOT_TIME);
		else
			timers.addTimer("shooting", BOT_SHOOT_TIME);
		timers.addTimer("dead", DEAD_TIME);
		timers.addTimer("flashing", FLASHING_TIME);
		timers.addTimer("flashing2", FLASHING_TIME2);
		timers.addTimer("respawning", RESPAWN_INVULN_TIME);
		
		if (!game.isServer) {
//			psb = new SpriteBatch();
//			ShaderProgram.pedantic = false;
//			shader = new ShaderProgram(Gdx.files.internal("shaders/flash.vsh"), Gdx.files.internal("shaders/flash.fsh"));
//			psb.setShader(shader);
			psb = game.res.getFlashSpriteBatch();
			shader = game.res.getFlashShader();
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
		timers.resetWithDelay("flashing2");
		timers.setCount(0);
		gotHit = true;
		if (hp <= 0) {
			hp = 0;
			alive = false;
			timers.resetWithDelay("dead");
		}
	}
	
	public boolean canRespawn() {
		return timers.finished("dead");
	}
	
	public boolean canMove() {
//		if (isBot) return false;
		
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
			
			if (nx == p.targetX && ny == p.targetY)
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
		targetX = x;
		targetY = y;
		timers.resetWithDelay("respawning");
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
		boolean signalSpace = Gdx.input.isKeyPressed(Keys.SPACE);
		boolean signalUp = Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP);
		boolean signalLeft = Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT);
		boolean signalDown = Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN);
		boolean signalRight = Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT);
		signalSpace &= !isBot;
		signalUp &= !isBot;
		signalLeft &= !isBot;
		signalDown &= !isBot;
		signalRight &= !isBot;
		
		if (isBot) {
			//use leastDist for some interesting results
//			float leastDist = Float.MAX_VALUE;
			int bestSize = -1;
			Vector2 next = null;
			Player target = null;
			ArrayList<Vector2> path = null;
			for (Player p : game.players.values()) {
				if (p == this || !p.alive)
					continue;
				
//				float dist = (float) Math.hypot(p.x - x, p.y - y);
				if (target == null) {
					next = game.level.calculateShortestPath(this, p);
					bestSize = game.level.graphPath.size() - 1;
					target = p;
					path = new ArrayList<Vector2>();
					for (Vector2 v : game.level.graphPath) {
						path.add(new Vector2(v));
					}
				} else {
					Vector2 maybenext = game.level.calculateShortestPath(this, p);
					int size = game.level.graphPath.size() - 1;
					if (size < bestSize) {
						bestSize = size;
						next = maybenext;
						target = p;
						path.clear();
						for (Vector2 v : game.level.graphPath) {
							path.add(new Vector2(v));
						}
					}
				}
			}
			
//			target = game.playerByName("Bob");

			
			if (target != null && target.alive) {
//				Log.info("Bot is calculating...");
//				if (game.level.distanceAway(this, target) > 1)
//				Vector2 next = game.level.calculateShortestPath(this, target);
//				path = game.level.graphPath;
				boolean allX = true;
				for (Vector2 v : path) {
					for (Vector2 v2 : path) {
						if (v != v2 && v.x != v2.x) {
							allX = false;
							break;
						}
					}
				}
				
				boolean allY = true;
				for (Vector2 v : path) {
					for (Vector2 v2 : path) {
						if (v != v2 && v.y != v2.y) {
							allY = false;
							break;
						}
					}
				}
				
				if (allX) {
//					Log.info("All x!");
					if (bestSize <= BOT_SHOOT_DISTANCE) {
						signalSpace = true;
						signalUp = y < next.y && angle != UP;
						signalDown = y > next.y && angle != DOWN;
						
					}
				}
				
				if (allY) {
//					Log.info("All y!");
					if (bestSize <= BOT_SHOOT_DISTANCE) {
						signalSpace = true;
						signalLeft = x > next.x && angle != LEFT;
						signalRight = x < next.x && angle != RIGHT;
					}
				}
				
				if (bestSize > BOT_SHOOT_DISTANCE || (!allX && !allY)) {
					signalUp = y < next.y;
					signalLeft = x > next.x;
					signalDown = y > next.y;
					signalRight = x < next.x;
				}
			}
		}
		
		
		
		if (!isBot && Gdx.input.isKeyJustPressed(Keys.Z)) {
			System.out.println(this);
		}
		
		//movement version 1
		//W - up, A - left, S - down, D - right
		if (alive && !inMotion) {
			if (signalUp) {
				//setting lastX/Y here likely useless, see canMove() for why
				lastY = y;
				angle = UP;
				if (canMove())
					inMotion = true;
			}
			
			else if (signalLeft) {
				lastX = x;
				angle = LEFT;
				if (canMove())
					inMotion = true;
			}
			
			else if (signalDown) {
				lastY = y;
				angle = DOWN;
				if (canMove())
					inMotion = true;
			}
			
			else if (signalRight) {
				lastX = x;
				angle = RIGHT;
				if (canMove())
					inMotion = true;
				//THIS SYSTEM ISN'T PERFECT. FIX IT
			}
		}
		
		//shooting
		if (alive && timers.finished("shooting") && signalSpace) {
			timers.resetWithDelay("shooting");
			addBullet(x, y, angle, name);
			BulletMessage msg = new BulletMessage();
			if (!game.isServer) {
				game.tc.client.sendTCP(msg);
				game.res.playSound("shoot", game.volume);
			} else {
				msg.pid = game.ts.bots.get(name);
				game.ts.server.sendToAllTCP(msg);
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
				timers.resetWithDelay("flashing2");
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
				timers.resetWithDelay("flashing");
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
			
			if (distAccum >= DISTANCE_TO_MOVE) {
				distAccum = 0;
//				x = normalize(x);
//				y = normalize(y);
				x = lastX + MathUtils.cosDeg(angle) * DISTANCE_TO_MOVE;
				y = lastY + MathUtils.sinDeg(angle) * DISTANCE_TO_MOVE;
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
		
//		if (BOT_DEBUG) {
//			int cx = (int) ((x + 1 - game.level.offX) / Entity.TILESIZE);
//			int cy = (int) ((y + 1 - game.level.offY) / Entity.TILESIZE);
//			String coords = "(" + cx + ", " + cy + ")";
//			float coordX = x + TILESIZE / 2 - hpTag.getBounds(coords).width / 2;
//			float coordY = y + 3f * TILESIZE;
//			batch.begin();
//			hpTag.draw(batch, coords, coordX, coordY);
//			batch.end();
//		}
	}
	
	@Override
	public String toString() {
		return "Player \"" + name + "\": (" + x + ", " + y + "), angle = " + correctedAngle();
	}
}
