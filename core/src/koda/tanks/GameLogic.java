package koda.tanks;

import java.util.HashMap;
import java.util.Map;

import koda.tanks.Network.BulletMessage;
import koda.tanks.Network.LeaveMessage;
import koda.tanks.Network.LoginResponseMessage;
import koda.tanks.Network.NewPlayerMessage;
import koda.tanks.Network.PlayerHitMessage;
import koda.tanks.Network.PositionMessage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;

public class GameLogic {

	TextureAtlas spriteAtlas;
	HashMap<Integer, Player> players = new HashMap<Integer, Player>();
	//to make this non-static, pass in an instance of the game to the player/other entities
	//actually, going to pre-initialize the sprite map later
	HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();
	String[] spriteAliases = {"tank", "bullet", "testbullet"};
	String[] spriteNames = {"tanks_tank", "tanks_bullet", "testmissile"};
	boolean isServer;
	TanksServer ts;
	TanksClient tc;
	BitmapFont nameTag;
	BitmapFont hpTag;
	Level level;
	
	Player localPlayer;
	
	public GameLogic(TanksClient tc) {
		this.tc = tc;
		isServer = false;
		initCommon();
	}
	
	public GameLogic(TanksServer ts) {
		this.ts = ts;
		isServer = true;
		initCommon();
	}
	
	private void initCommon() {
		//sprite initialization here is temporary
		spriteAtlas = new TextureAtlas(Gdx.files.internal("tanks_pack.pack"));
		for (int i = 0; i < spriteAliases.length; i++) {
			if (sprites.get(spriteAliases[i]) != null) {
				continue;
			}
			sprites.put(spriteAliases[i], spriteAtlas.createSprite(spriteNames[i]));
		}
		
		
		nameTag = new BitmapFont(Gdx.files.internal("comicsans.fnt"));
		hpTag = new BitmapFont(Gdx.files.internal("comicsans.fnt"));
		hpTag.setScale(.5f);
		
		level = new Level(this);
		level.offX = 5 * Entity.TILESIZE;
		level.offY = Entity.TILESIZE;
		level.createSampleLevel();
	}
	
	public void input() {
		if (!isServer)
			localPlayer.input();
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
					if (curr == victim)
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
		
		//check collisions (player to wall)
		for (Player p : players.values()) {
			for (Wall w : level.walls) {
				if (p.collidesWith(w)) {
					p.setPosition(p.prevX, p.prevY, p.dir);
				}
			}
		}
		
		//check collisions (player to player)
		for (Player p1 : players.values()) {
			for (Player p2 : players.values()) {
				if (p1 != p2 && p1.collidesWith(p2)) {
					p1.setPosition(p1.prevX, p1.prevY, p1.dir);
					p2.setPosition(p2.prevX, p2.prevY, p2.dir);
				}
			}
		}
		
		//clean up at end of update loop
		for (Player p : players.values()) {
			if (p != localPlayer)
				p.clean();
		}
		
		if (!isServer) {
			if (localPlayer.changed()) {
				PositionMessage msg = new PositionMessage();
				msg.x = localPlayer.x;
				msg.y = localPlayer.y;
				msg.dir = localPlayer.dir;
				tc.client.sendUDP(msg);
			}
		}
	}
	
	public void onConnect() {
		//this method should never be called by the server
		NewPlayerMessage msg = new NewPlayerMessage();
		msg.name = tc.name;
		tc.client.sendTCP(msg);
		players.put(tc.id, localPlayer);
		Log.info("Client " + tc.name + " is online");
	}
	
	public void onPlayerHit(PlayerHitMessage msg) {
		//shooter is not needed. maybe for other features later?
//		Player shooter = playerByName(msg.shooterName);
		
		Player victim = playerByName(msg.victimName);
		victim.hit(1);
		
		if (!isServer) {
			tc.specialText = msg.shooterName + " hit " + victim.name + "!";
			tc.timeSpecialTextSet = System.currentTimeMillis();
		}
	}
	
	public void onBulletFired(BulletMessage msg) {
		Player shooter = players.get(msg.pid);
		//unneeded message attributes. just need the pid
		shooter.addBullet(shooter.x, shooter.y, shooter.dir, shooter.name);
	}
	
	public void onMovementUpdate(PositionMessage msg) {
		Player current = players.get(msg.pid);
		current.setPosition(msg.x, msg.y, msg.dir);
	}
	
	public void onPlayerLeaves(LeaveMessage msg) {
		//this will need synchronization and cleanup later
		players.remove(msg.pid);
	}
	
	public void onNewPlayer(NewPlayerMessage msg) {
		Player newPlayer = new Player(this, sprites.get("tank"), msg.x, msg.y, msg.name);
		//probably make dir a setter
		newPlayer.dir = msg.dir;
		newPlayer.hp = msg.hp;
		newPlayer.rotateSprite(newPlayer.dir);
		players.put(msg.pid, newPlayer);
		
		if (isServer)
			Log.info("Server added player " + newPlayer.name);
		else
			Log.info(tc.name + " added player " + newPlayer.name);
		
		if (isServer) {
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
					previousPlayer.dir = e.dir;
					previousPlayer.hp = e.hp;
					ts.server.sendToTCP(msg.pid, previousPlayer);
				}
			}
		}
	}
	
	public void onLoginResponse(LoginResponseMessage msg) {
		localPlayer = new Player(this, sprites.get("tank"), msg.x, msg.y, tc.name);
		localPlayer.dir = msg.dir;
		localPlayer.hp = msg.hp;
		localPlayer.rotateSprite(localPlayer.dir);
		players.put(tc.id, localPlayer);
		Log.info(tc.name + " has been instantiated");
	}
	
	public void render(SpriteBatch batch) {
		batch.setProjectionMatrix(PlayScreen.camera.combined);
		
		level.render(batch);
		
		for (Player p : players.values()) {
			p.render(batch);
			p.drawTags(nameTag, hpTag, batch);
		}
	}
	
	public Player playerByName(String name) {
		for (Player p : players.values()) {
			if (p.name.equals(name))
				return p;
		}
		return null;
	}
}
