package koda.tanks;

import java.io.IOException;

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
	}
	
	//uses random algorithm
	public synchronized Vector2 getStartingSpot() {
		if (game.players.values().size() > positions.size)
			return null;
		
//		int i = 1;
//		if (i == 1)
//			return positions.get(0);
		
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
			//have the server assign a corner to the player
			
			//this chunk of code is only invoked when a player is connecting for the first time
			Vector2 position = getStartingSpot();
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
