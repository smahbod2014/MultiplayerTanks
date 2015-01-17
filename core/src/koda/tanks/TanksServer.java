package koda.tanks;

import java.io.IOException;
import java.util.HashMap;

import koda.tanks.Network.BulletMessage;
import koda.tanks.Network.LeaveMessage;
import koda.tanks.Network.NewPlayerMessage;
import koda.tanks.Network.PositionMessage;
import koda.tanks.Network.ShuttingDownMessage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

public class TanksServer extends Listener {

	Server server;
	GameLogic game;
	Array<Vector2> positions = new Array<Vector2>();
	HashMap<String, Integer> bots = new HashMap<String, Integer>();
	String[] botNames = {"Max Bot", "Harry Bot", "Danny Bot", "Alex Bot", "Sam Bot", "Mike Bot", "Chris Bot"};
	int[] botIds = {-1, -2, -3, -4, -5, -6, -7};
	int nameId = 0;
	
	public TanksServer() {
		game = new GameLogic(this);
		positions = game.level.positions;
		server = new Server();
		
		Network.registerPackets(server);
		try {
			server.bind(Network.tcpPort, Network.udpPort);
		} catch (IOException e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
		
		server.addListener(this);
		server.start();
		
		createBot();
		createBot();
		createBot();
		createBot();
		createBot();
		createBot();
		createBot();
	}
	
	//uses random algorithm
	public synchronized Vector2 getStartingSpot() {
		if (game.players.values().size() > positions.size)
			return null;
		
		boolean found = false;
		int index = 0;
		while (!found) {
			index = (int) (Math.random() * positions.size);
			
			if (game.players.values().size() == 0)
				return positions.get(index);
			
			boolean playerOnSpot = false;
			for (Player p : game.players.values()) {	
				if (p.alive && isAtPosition(p, index)) {
					playerOnSpot = true;
					break;
				}
			}
			
			if (!playerOnSpot)
				return positions.get(index);
		}
		
		return null;
	}
	
	private boolean isAtPosition(Player p, int index) {
		Rectangle rect = new Rectangle(positions.get(index).x, positions.get(index).y, Entity.TILESIZE, Entity.TILESIZE);
		return p.collidesWith(rect);
	}
	
	public void createBot() {
		String name = null;
		for (String s : botNames) {
			if (game.playerByName(s) == null) {
				name = s;
				break;
			}
		}
		
//		String name = botNames[nameId];
//		
//		int id = botIds[nameId];
		int id = 0;
		boolean idSuccess = false;
		for (int i = 0; i < botIds.length; i++) {
			boolean canAssign = true;
			for (Integer key : game.players.keySet()) {
				if (botIds[i] == key) {
					canAssign = false;
					break;
				}
			}
			
			if (canAssign) {
				id = botIds[i];
				idSuccess = true;
				break;
			}
		}
		
		if (!idSuccess) {
			Log.info("Server could not assign a valid id to the bot");
			return;
		}
		
		//can't add this bot, all names are taken
		if (name == null) {
			Log.info("Server failed to create bot. All bot names taken");
			return;
		}
		
		Vector2 pos = getStartingSpot();
//		Vector2 pos = null;
//		if (name.equals("Harry Bot")) {
//			for (Vector2 v : positions)
//				if (v.x == 270 && v.y == 450)
//					pos = v;
//		} else {
//			pos = positions.get(nameId);
//		}
//		nameId++;
		Log.info(name + " spawned at " + pos);
		if (pos == null) {
			//this null check is useless; getStartingSpot() will infinitely loop
			Log.info("Server failed to assign a starting position. All positions taken");
			return;
		}
		
		
//		Log.info("Server creating bot " + name);
		bots.put(name, id);
		NewPlayerMessage msg = new NewPlayerMessage();
		msg.pid = id;
		msg.name = name;
		msg.x = pos.x;
		msg.y = pos.y;
		msg.angle = Entity.RIGHT;
		msg.hp = Player.MAX_HP;
		msg.isBot = true;
		game.onNewPlayer(msg);
	}
	
	public void shutdown() {
		Log.info("Server shutting down");
		server.sendToAllTCP(new ShuttingDownMessage());
		server.close();
		server.stop();
	}
	
	@Override
	public void connected(Connection c) {
		
	}
	
	@Override
	public void received(Connection c, Object pkt) {
		
		if (pkt instanceof NewPlayerMessage) {
			final NewPlayerMessage msg = (NewPlayerMessage) pkt;
			
			//this chunk of code is only invoked when a player is connecting for the first time
			Vector2 position = getStartingSpot();
			if (position == null) {
				//this null check is useless; getStartingSpot() will infinitely loop
				Log.info("Server failed to assign a starting position. All positions taken");
				return;
			}
			msg.pid = c.getID();
			msg.x = position.x;
			msg.y = position.y;
			msg.angle = Entity.RIGHT; //(int) position.z;
			msg.hp = Player.MAX_HP;
			game.onNewPlayer(msg);
			
		} else if (pkt instanceof PositionMessage) {
			PositionMessage msg = (PositionMessage) pkt;
			msg.pid = c.getID();
			game.onMovementUpdate(msg);
			server.sendToAllExceptUDP(c.getID(), msg);
		} else if (pkt instanceof BulletMessage) {
			BulletMessage msg = (BulletMessage) pkt;
			msg.pid = c.getID();
			game.onBulletFired(msg);
			//for now, bullet fired is always valid
			server.sendToAllExceptTCP(c.getID(), msg);
		}
	}
	
	@Override
	public void disconnected(Connection c) {
		LeaveMessage msg = new LeaveMessage();
		msg.pid = c.getID();
		game.onPlayerLeaves(msg);
		server.sendToAllExceptTCP(c.getID(), msg);
	}
	
	public static class TanksConnection extends Connection {
		public String name;
	}
}
