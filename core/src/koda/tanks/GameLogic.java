package koda.tanks;

import java.util.HashMap;
import java.util.Map;

import koda.tanks.Network.BulletMessage;
import koda.tanks.Network.LeaveMessage;
import koda.tanks.Network.LoginResponseMessage;
import koda.tanks.Network.NewPlayerMessage;
import koda.tanks.Network.PlayerHitMessage;
import koda.tanks.Network.PlayerRevivedMessage;
import koda.tanks.Network.PositionMessage;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;

public class GameLogic {
	
	TextureAtlas spriteAtlas;
	HashMap<Integer, Player> players = new HashMap<Integer, Player>();
	String[] spriteAliases = {"tank", "bullet", "testbullet"};
	String[] spriteNames = {"tanks_tank", "tanks_bullet", "testmissile"};
	String[] soundAliases = {"shoot", "hit", "bcollide", "death"};
	String[] soundNames = {"tanks_shoot.wav", "tanks_hit.wav", "tanks_bullet_collide.wav", "tanks_death.wav"};
	String fontName = "comicsans.fnt";
	String[] fontAliases = {"nametag", "hptag", "text"};
	boolean isServer;
	TanksServer ts;
	TanksClient tc;
	Level level;
	Scoreboard scores;
	ShapeRenderer sr;
	OrthographicCamera levelCam;
	OrthographicCamera hudCam;
	Resources res;
	
	Player localPlayer;
	float volume = .375f;
	boolean windowMinimized;
	long hitMessageDuration = 2000;
	
	public GameLogic(TanksClient tc) {
		this.tc = tc;
		isServer = false;
		scores = new Scoreboard(new BitmapFont(Gdx.files.internal("comicsans.fnt")), new BitmapFont(Gdx.files.internal("comicsans.fnt")));
		scores.offX = 2;
		scores.offY = 2;
		
		
		sr = new ShapeRenderer();
		
		res = new Resources(this);
		for (int i = 0; i < soundAliases.length; i++) {
			res.loadSound(soundAliases[i], soundNames[i]);
		}
		
		res.loadAtlas("tanks_pack.pack");
		for (int i = 0; i < spriteAliases.length; i++) {
			res.loadSpriteFromAtlas(spriteAliases[i], spriteNames[i]);
		}
		
		for (int i = 0; i < fontAliases.length; i++) {
			res.loadFont(fontAliases[i], fontName);
		}
		
		res.getFont("hptag").setScale(.5f);
		
		levelCam = new OrthographicCamera();
		hudCam = new OrthographicCamera();
		
		localPlayer = new Player(this, res.getSprite("tank"), 0, 0, tc.name);
		
		initCommon();
	}
	
	public GameLogic(TanksServer ts) {
		this.ts = ts;
		isServer = true;
		scores = new Scoreboard();
		initCommon();
	}
	
	private void initCommon() {
		level = new Level(this);
		level.offX = 5 * Entity.TILESIZE;
		level.offY = Entity.TILESIZE;
		level.createLevel("tanks_level1.png");
	}
	
	public synchronized void input(float dt) {
		//client not ready?
		if (localPlayer == null)
			return;
		
		if (Gdx.input.isKeyJustPressed(Keys.C)) {
			levelCam.zoom += 1 / 25f;
			levelCam.update();
		}
		
		if (Gdx.input.isKeyJustPressed(Keys.V)) {
			levelCam.zoom -= 1 / 25f;
			levelCam.update();
		}
		
		localPlayer.input(dt);
	}
	
	public synchronized void update(float dt) {
		for (Player p : players.values()) {
			p.update(dt);
		}
		
		//check collisions (bullet to player, bullet to bullet, bullet to wall)
		for (Player p : players.values()) {
			for (Bullet b : p.bullets) {
				if (!b.alive)
					continue;
				
				//bullet to player
				for (Player victim : players.values()) {
					if (p == victim)
						continue;
					
					if (victim.alive && b.collidesWith(victim)) {
						b.alive = false;
						if (isServer) {
							PlayerHitMessage msg = new PlayerHitMessage();
							msg.shooterName = p.name;
							msg.victimName = victim.name;
							ts.server.sendToAllTCP(msg);
							onPlayerHit(msg);
						}
					}
					
					//bullet to bullet
					for (Bullet b2 : victim.bullets) {
						if (!b.alive || !b2.alive)
							continue;
						
						if (b.collidesWith(b2)) {
							b.alive = false;
							b2.alive = false;
							if (!isServer)
								res.playSound("bcollide", volume);
						}
					}
				}
				
				//bullet to wall
				for (Wall w : level.walls) {
					if (b.collidesWith(w)) {
						b.alive = false;
						if (!isServer)
							res.playSound("bcollide", volume);
					}	
				}
			}
		}
		
		//revive dead players
		if (isServer) {
			for (Map.Entry<Integer, Player> e : players.entrySet()) {
				Player p = e.getValue();
				if (!p.alive && p.canRespawn()) {
					Vector2 pos = ts.getStartingSpot();
					PlayerRevivedMessage msg = new PlayerRevivedMessage();
					msg.pid = e.getKey();
					msg.angle = Entity.RIGHT; //(int) pos.z;
					msg.x = pos.x;
					msg.y = pos.y;
					ts.server.sendToAllTCP(msg);
					//for code re-use
					onPlayerRevived(msg);
				}
			}
		}
		
		//send positional data about self to server
		//the !isServer check is not necessary, but makes things explicit
		if (!isServer && localPlayer != null) {
			if (localPlayer.changedPosAngle()) {
				PositionMessage msg = new PositionMessage();
				msg.x = localPlayer.x;
				msg.y = localPlayer.y;
				msg.angle = localPlayer.angle;
				msg.lastX = localPlayer.lastX;
				msg.lastY = localPlayer.lastY;
				msg.targetX = localPlayer.targetX;
				msg.targetY = localPlayer.targetY;
				localPlayer.cleanPosAngle();
				tc.client.sendUDP(msg);
			}
		}
	}
	
	/**
	 * Called by the client on first connection
	 */
	public synchronized void onConnect() {
		//this method should never be called by the server
		NewPlayerMessage msg = new NewPlayerMessage();
		msg.name = tc.name;
		tc.client.sendTCP(msg);
		Log.info("Client " + tc.name + " is online");
	}
	
	/**
	 * Called by both server and client
	 * @param msg
	 */
	public synchronized void onPlayerHit(PlayerHitMessage msg) {
		Player victim = playerByName(msg.victimName);
		victim.hit(1);
		
		if (!isServer) {
			if (victim.alive) {
				tc.specialText = "";
				res.playSound("hit", volume);
			}
			else {
				tc.specialText = msg.shooterName + " killed " + victim.name + "!";
				res.playSound("death", volume);
			}
			tc.timeSpecialTextSet = System.currentTimeMillis();
		}
		
		if (!victim.alive)
			scores.updateEntryAdd(msg.shooterName, 1);
	}
	
	/**
	 * Called by both server and client, but not by the client who fired the bullet. Should fix that later.
	 * @param msg
	 */
	public synchronized void onBulletFired(BulletMessage msg) {
		Player shooter = players.get(msg.pid);
		shooter.addBullet(shooter.x, shooter.y, shooter.angle, shooter.name);
		
		if (!isServer)
			res.playSound("shoot", volume);
	}
	
	/**
	 * Called by both server and client
	 * @param msg
	 */
	public synchronized void onMovementUpdate(PositionMessage msg) {
		Player current = players.get(msg.pid);
		//temporary fix
		if (current == null)
			return;
		current.x = msg.x;
		current.y = msg.y;
		current.angle = msg.angle;
		current.lastX = msg.lastX;
		current.lastY = msg.lastY;
		current.targetX = msg.targetX;
		current.targetY = msg.targetY;
	}
	
	/**
	 * Called by both server and client
	 * @param msg
	 */
	public synchronized void onPlayerLeaves(LeaveMessage msg) {
		//this will need synchronization and cleanup later
		Player p = players.get(msg.pid);
		//temporary fix
		if (p == null)
			return;
//		info("Removing " + p.name + " (" + msg.pid + ")");
		players.remove(msg.pid);
		scores.removeEntry(p.name);
	}
	
	/**
	 * Called by both server and client
	 * @param msg
	 */
	public synchronized void onNewPlayer(final NewPlayerMessage msg) {
		//there's already someone with this name
		if (isServer) {
			if (playerByName(msg.name) != null) {
				LoginResponseMessage response = new LoginResponseMessage();
				response.success = false;
				ts.server.sendToTCP(msg.pid, response);
				return;
			} else {
				LoginResponseMessage response = new LoginResponseMessage();
				response.x = msg.x;
				response.y = msg.y;
				response.angle = msg.angle;
				response.hp = msg.hp;
				response.success = true;
				ts.server.sendToTCP(msg.pid, response);
			}
		}
		
		final GameLogic g = this;
		Gdx.app.postRunnable(new Runnable() {
			public void run() {
				//otherwise we're good
				Sprite spr = res == null ? null : res.getSprite("tank");
				Player newPlayer = new Player(g, spr, msg.x, msg.y, msg.name);
				//probably make dir a setter
				newPlayer.angle = msg.angle;
				newPlayer.hp = msg.hp;
				players.put(msg.pid, newPlayer);
				
				scores.addEntry(msg.name, msg.score);
				
//				if (isServer)
//					Log.info("Server added player " + newPlayer.name);
//				else
//					Log.info(tc.name + " added player " + newPlayer.name);
			}
		});
		
		
		
		
		if (isServer) {
			//tell everyone about this new player
			ts.server.sendToAllExceptTCP(msg.pid, msg);
			
			//tell this new player about everyone else
			//alternatively, just loop through the players map...instead of getting connections
			for (Connection con : ts.server.getConnections()) {
				if (con.getID() != msg.pid) {
					NewPlayerMessage previousPlayer = new NewPlayerMessage();
					Player e = players.get(con.getID());
					previousPlayer.name = e.name;
					previousPlayer.pid = con.getID();
					previousPlayer.x = e.x; 
					previousPlayer.y = e.y;
					previousPlayer.angle = e.angle;
					previousPlayer.hp = e.hp;
					previousPlayer.score = scores.getScore(e.name);
					ts.server.sendToTCP(msg.pid, previousPlayer);
				}
			}
		}
	}
	
	/**
	 * Called only by client. Invoked when the server recognizes the client for the first time, so
	 * all the code in here should be initialization-style code for the client, such as allocating
	 * a ScoreEntry with an initial score of 0
	 * @param msg
	 */
	public synchronized void onLoginResponse(LoginResponseMessage msg) {
		if (!msg.success) {
			Log.info("There is already a user with the name " + tc.name);
			Gdx.app.postRunnable(new Runnable() {
				public void run() {
					Game g = (Game) Gdx.app.getApplicationListener();
					g.setScreen(new MenuScreen((Tanks) g));
				}
			});
			
			return;
		}
		
//		localPlayer = new Player(this, sprites.get("tank"), msg.x, msg.y, tc.name);
		localPlayer.angle = msg.angle;
		localPlayer.hp = msg.hp;
		localPlayer.x = msg.x;
		localPlayer.y = msg.y;
		players.put(tc.id, localPlayer);
		scores.addEntry(tc.name, 0);
		scores.localPlayerName = tc.name;
		levelCam.position.set(msg.x, msg.y, 0);
		levelCam.update();
		Log.info(tc.name + " has been instantiated");
	}
	
	
	public synchronized void onPlayerRevived(PlayerRevivedMessage msg) {
		Player p = players.get(msg.pid);
		p.respawn(msg.x, msg.y, msg.angle);
		
		//client only
		if (p == localPlayer) {
			levelCam.position.set(p.x, p.y, 0);
			levelCam.update();
		}
	}
	
	public synchronized void render(SpriteBatch batch) {
		level.checkCameraPosition();
		
		batch.setProjectionMatrix(levelCam.combined);
		
		level.render(batch);
		
		for (Player p : players.values()) {
			if (p.alive)
				p.render(batch);
		}
		
		//to keep the name tags on top of the players
		for (Player p : players.values()) {
			if (p.alive)
				p.drawTags(res.getFont("nametag"), res.getFont("hptag"), batch);
		}
		
		batch.setProjectionMatrix(hudCam.combined);
		scores.render(batch);
		
		if (tc.alive) {
			batch.begin();
			res.getFont("text").draw(batch, "Connected", 2, 25);
			batch.end();
		} else {
			batch.begin();
			res.getFont("text").draw(batch, "No connection!", 2, 25);
			batch.end();
		}
		
		if (System.currentTimeMillis() < tc.timeSpecialTextSet + hitMessageDuration) {
			batch.begin();
			res.getFont("text").draw(batch, tc.specialText, 2, 50);
			batch.end();
		}
	}
	
	public Player playerByName(String name) {
		for (Player p : players.values()) {
			if (p.name.equals(name))
				return p;
		}
		return null;
	}
	
	public synchronized void resize(int width, int height) {
		levelCam.viewportWidth = width;
		levelCam.viewportHeight = height;
		if (localPlayer == null) {
			levelCam.position.setZero();
			levelCam.translate(width / 2, height / 2);
		} else {
			levelCam.position.set(localPlayer.x, localPlayer.y, 0);
		}
		levelCam.update();
		
		hudCam.viewportWidth = width;
		hudCam.viewportHeight = height;
		hudCam.position.setZero();
		hudCam.translate(width / 2, height / 2);
		hudCam.update();
		
//		Entity.screen.width = width;
//		Entity.screen.height = height;
	}
	
	public void info(String msg) {
		if (isServer)
			Log.info("Server", msg);
		else
			Log.info(tc.name, msg);
	}
}
