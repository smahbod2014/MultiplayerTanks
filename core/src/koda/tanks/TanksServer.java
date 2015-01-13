package koda.tanks;

import java.io.IOException;

import koda.tanks.Network.BulletMessage;
import koda.tanks.Network.LeaveMessage;
import koda.tanks.Network.LoginResponseMessage;
import koda.tanks.Network.NewPlayerMessage;
import koda.tanks.Network.PositionMessage;
import koda.tanks.Network.ShuttingDownMessage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

public class TanksServer extends Listener {

	Server server;
	GameLogic game;
	Array<Vector3> positions = new Array<Vector3>();
	
	public TanksServer() {
		game = new GameLogic(this);
		server = new Server() {
//			@Override
//			protected Connection newConnection() {
//				return new TanksConnection();
//			}
		};
		
		Network.registerPackets(server);
		try {
			server.bind(Network.tcpPort, Network.udpPort);
		} catch (IOException e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
		
		server.addListener(this);
		server.start();
		
		positions.add(new Vector3(game.level.offX + Entity.TILESIZE, game.level.offY + Entity.TILESIZE, Entity.RIGHT));
		positions.add(new Vector3(game.level.offX + Entity.TILESIZE, game.level.offY + (game.level.level.length - 2) * Entity.TILESIZE, Entity.RIGHT));
		positions.add(new Vector3(game.level.offX + (game.level.level[0].length - 2) * Entity.TILESIZE, game.level.offY + (game.level.level.length - 2) * Entity.TILESIZE, Entity.LEFT));
		positions.add(new Vector3(game.level.offX + (game.level.level[0].length - 2) * Entity.TILESIZE, game.level.offY + Entity.TILESIZE, Entity.LEFT));
	}
	
	public Vector3 getStartingSpot() {
		int index = 0;
		for (Player p : game.players.values()) {
			if (!p.alive)
				continue;
			
			float posx = p.x;
			float posy = p.y;
			float checkx = positions.get(index).x;
			float checky = positions.get(index).y;
			System.out.println(p.name + " at: (" + posx + ", " + posy + "), spawn is (" + checkx + ", " + checky + ")");
			if ((int) p.x == (int) positions.get(index).x && (int) p.y == (int) positions.get(index).y) {
				index++;
//				System.out.println("Incrementing index, is now" + index);
				if (index == positions.size) {
					//figure something out for this later
					return positions.get(0);
				}
			} else {
//				System.out.println("Returning index " + index);
				return positions.get(index);
			}
		}
		
		return positions.get(index);
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
			
			Vector3 position = getStartingSpot();
			msg.pid = c.getID();
			msg.x = position.x;
			msg.y = position.y;
			msg.dir = (int) position.z;
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
