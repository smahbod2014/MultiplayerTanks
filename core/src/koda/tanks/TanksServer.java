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
			
			msg.pid = c.getID();
			msg.x = positions.get(0).x;
			msg.y = positions.get(0).y;
			msg.dir = (int) positions.get(0).z;
			msg.hp = Player.MAX_HP;
			positions.add(positions.removeIndex(0));
			game.onNewPlayer(msg);
			//tell everyone about this new player
			server.sendToAllExceptTCP(c.getID(), msg);
			
			LoginResponseMessage response = new LoginResponseMessage();
			response.x = msg.x;
			response.y = msg.y;
			response.dir = msg.dir;
			response.hp = msg.hp;
			server.sendToTCP(c.getID(), response);
			
		} else if (pkt instanceof PositionMessage) {
			PositionMessage msg = (PositionMessage) pkt;
//			Entity e = svEntities.get(c.getID());
//			e.x = msg.x;
//			e.y = msg.y;
//			if (e instanceof Player)
//				((Player) e).setDir(msg.dir);
			msg.pid = c.getID();
			game.onMovementUpdate(msg);
			server.sendToAllExceptUDP(c.getID(), msg);
		} else if (pkt instanceof BulletMessage) {
			final BulletMessage msg = (BulletMessage) pkt;
//			final Player p = (Player) svEntities.get(c.getID());
//			Gdx.app.postRunnable(new Runnable() {
//				public void run() {
//					p.addBullet(msg.x, msg.y, msg.dir, msg.name);
//					Log.info("Server added bullet for " + p.name);
//				}
//			});
			
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
