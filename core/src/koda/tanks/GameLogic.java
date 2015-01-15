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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;

public class GameLogic {
	
	TextureAtlas spriteAtlas;
	HashMap<Integer, Player> players = new HashMap<Integer, Player>();
	//to make this non-static, pass in an instance of the game to the player/other entities
	//actually, going to pre-initialize the sprite map later
	HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();
	HashMap<String, Sound> sounds;
	String[] spriteAliases = {"tank", "bullet", "testbullet"};
	String[] spriteNames = {"tanks_tank", "tanks_bullet", "testmissile"};
	String[] soundAliases = {"shoot", "hit", "death"};
	String[] soundNames = {"tanks_shoot.wav", "tanks_hit.wav", "tanks_death.wav"};
	boolean isServer;
	TanksServer ts;
	TanksClient tc;
	BitmapFont nameTag;
	BitmapFont hpTag;
	BitmapFont text;
	Level level;
	Scoreboard scores;
	ShapeRenderer sr;
	OrthographicCamera levelCam;
	OrthographicCamera hudCam;
	
	Player localPlayer;
	Array<Integer> lastToMove;
	Array<Entity> allEntities;
	float volume = .375f;
	boolean windowMinimized;
	long hitMessageDuration = 2000;
	
	public GameLogic(TanksClient tc) {
		this.tc = tc;
		isServer = false;
		initCommon();
		
		sr = new ShapeRenderer();
		sounds = new HashMap<String, Sound>();
		for (int i = 0; i < soundAliases.length; i++) {
			sounds.put(soundAliases[i], Gdx.audio.newSound(Gdx.files.internal(soundNames[i])));
		}
		
		text = new BitmapFont(Gdx.files.internal("comicsans.fnt"));
		nameTag = new BitmapFont(Gdx.files.internal("comicsans.fnt"));
		hpTag = new BitmapFont(Gdx.files.internal("comicsans.fnt"));
		hpTag.setScale(.5f);
		
		scores = new Scoreboard(new BitmapFont(Gdx.files.internal("comicsans.fnt")), new BitmapFont(Gdx.files.internal("comicsans.fnt")));
		scores.offX = 2;
		scores.offY = 2;
		
		levelCam = new OrthographicCamera();
		hudCam = new OrthographicCamera();
	}
	
	public GameLogic(TanksServer ts) {
		this.ts = ts;
		isServer = true;
		initCommon();
	}
	
	private void initCommon() {
		//sprite initialization here is temporary
		//just make it so that the server never loads textures since it will never render anything
		spriteAtlas = new TextureAtlas(Gdx.files.internal("tanks_pack.pack"));
		for (int i = 0; i < spriteAliases.length; i++) {
			if (sprites.get(spriteAliases[i]) != null) {
				continue;
			}
			sprites.put(spriteAliases[i], spriteAtlas.createSprite(spriteNames[i]));
		}
				
		lastToMove = new Array<Integer>();
		allEntities = new Array<Entity>();
		
		level = new Level(this);
		level.offX = 5 * Entity.TILESIZE;
		level.offY = Entity.TILESIZE;
		level.createLevel("tanks_level1.png");
		allEntities.addAll(level.walls);
	}
	
	public void input(float dt) {
		//client not ready
		//useless statement?
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
	
	public void update(float dt) {
		for (Player p : players.values()) {
			p.update(dt);
		}
		
		//check collisions (bullet to player)
		for (Map.Entry<Integer, Player> entry : players.entrySet()) {
			Player curr = entry.getValue();
			for (Bullet b : curr.bullets) {
				if (!b.alive)
					continue;
				for (Map.Entry<Integer, Player> entry2 : players.entrySet()) {
					Player victim = entry2.getValue();
					if (curr == victim || !victim.alive)
						continue;
					
					if (b.collidesWith(victim)) {
						b.alive = false;
						if (isServer) {
							PlayerHitMessage msg = new PlayerHitMessage();
							msg.shooterName = curr.name;
							msg.victimName = victim.name;
							ts.server.sendToAllTCP(msg);
							onPlayerHit(msg);
						}
					}
				}
			}
		}
		
		//check collisions (bullet to wall)
		for (Player p : players.values()) {
			for (Bullet b : p.bullets) {
				for (Wall w : level.walls) {
					if (b.collidesWith(w)) {
						b.alive = false;
					}
				}
			}
		}
		
		//check collisions (player to player)
//		for (Player p1 : players.values()) {
//			if (!p1.alive)
//				continue;
//			
//			for (Player p2 : players.values()) {
//				if (p2.alive && p1 != p2 && p1.collidesWith(p2)) {
//					p1.setPosition(p1.lastX, p1.lastY, p1.angle);
//					p2.setPosition(p2.lastX, p2.lastY, p2.angle);
//				}
//			}
//		}
		
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
		
		//clean up at end of update loop
		for (Player p : players.values()) {
			if (p != localPlayer)
				p.cleanPosAngle();
		}
		
		//send positional data about self to server
		//the !isServer check is not necessary, but makes things explicit
		if (!isServer && localPlayer != null) {
			if (localPlayer.changedPosAngle()) {
				PositionMessage msg = new PositionMessage();
				msg.x = localPlayer.x;
				msg.y = localPlayer.y;
				msg.lastX = localPlayer.lastX;
				msg.lastY = localPlayer.lastY;
				msg.angle = localPlayer.angle;
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
	public void onConnect() {
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
	public void onPlayerHit(PlayerHitMessage msg) {
		Player victim = playerByName(msg.victimName);
		victim.hit(1);
		
		if (!isServer) {
			if (victim.alive) {
				tc.specialText = msg.shooterName + " hit " + victim.name + "!";
				if (!windowMinimized)
					sounds.get("hit").play(volume);
			}
			else {
				scores.updateEntryAdd(msg.shooterName, 1);
				tc.specialText = msg.shooterName + " killed " + victim.name + "!";
				if (!windowMinimized)
					sounds.get("death").play(volume);
			}
			tc.timeSpecialTextSet = System.currentTimeMillis();
		}
	}
	
	/**
	 * Called by both server and client, but not by the client who fired the bullet. Should fix that later.
	 * @param msg
	 */
	public void onBulletFired(BulletMessage msg) {
		Player shooter = players.get(msg.pid);
		shooter.addBullet(shooter.x, shooter.y, shooter.angle, shooter.name);
		
		if (!isServer && !windowMinimized)
			sounds.get("shoot").play(volume);
	}
	
	/**
	 * Called by both server and client
	 * @param msg
	 */
	public void onMovementUpdate(PositionMessage msg) {
		Player current = players.get(msg.pid);
		current.setPosition(msg.x, msg.y, msg.angle);
		current.lastX = msg.lastX;
		current.lastY = msg.lastY;
		current.targetX = msg.targetX;
		current.targetY = msg.targetY;
	}
	
	/**
	 * Called by both server and client
	 * @param msg
	 */
	public void onPlayerLeaves(LeaveMessage msg) {
		//this will need synchronization and cleanup later
		Player p = players.get(msg.pid);
		allEntities.removeValue(p, true);
		players.remove(msg.pid);
	}
	
	/**
	 * Called by both server and client
	 * @param msg
	 */
	public void onNewPlayer(NewPlayerMessage msg) {
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
		
		//otherwise we're good
		Player newPlayer = new Player(this, sprites.get("tank"), msg.x, msg.y, msg.name);
		//probably make dir a setter
		newPlayer.angle = msg.angle;
		newPlayer.hp = msg.hp;
		players.put(msg.pid, newPlayer);
		allEntities.add(newPlayer);
		
		if (!isServer)
			scores.addEntry(msg.name);
		
		if (isServer)
			Log.info("Server added player " + newPlayer.name);
		else
			Log.info(tc.name + " added player " + newPlayer.name);
		
		
		
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
					ts.server.sendToTCP(msg.pid, previousPlayer);
				}
			}
		}
	}
	
	/**
	 * Called only by client
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
		
		localPlayer = new Player(this, sprites.get("tank"), msg.x, msg.y, tc.name);
		localPlayer.angle = msg.angle;
		localPlayer.hp = msg.hp;
		players.put(tc.id, localPlayer);
		allEntities.add(localPlayer);
		scores.addEntry(tc.name);
		scores.localPlayerName = tc.name;
		levelCam.position.set(msg.x, msg.y, 0);
		levelCam.update();
		Log.info(tc.name + " has been instantiated");
	}
	
	public void onPlayerRevived(PlayerRevivedMessage msg) {
		Player p = players.get(msg.pid);
		p.alive = true;
		p.hp = Player.MAX_HP;
		p.setPosition(msg.x, msg.y, msg.angle);
		p.lastX = p.x;
		p.lastY = p.y;
		p.cleanPosAngle();
		
		//client only
		if (p == localPlayer) {
			levelCam.position.set(p.x, p.y, 0);
			levelCam.update();
		}
	}
	
	public void render(SpriteBatch batch) {
		level.checkCameraPosition();
		
		batch.setProjectionMatrix(levelCam.combined);
		
		level.render(batch);
		
		for (Player p : players.values()) {
			if (!p.alive)
				continue;
			
			p.render(batch);
			p.drawTags(nameTag, hpTag, batch);
		}
		
		batch.setProjectionMatrix(hudCam.combined);
		scores.render(batch);
		
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
}
