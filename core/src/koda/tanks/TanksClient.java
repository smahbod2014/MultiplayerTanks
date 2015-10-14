package koda.tanks;

import java.io.IOException;

import koda.tanks.Network.BulletMessage;
import koda.tanks.Network.ChatMessage;
import koda.tanks.Network.LeaveMessage;
import koda.tanks.Network.LoginResponseMessage;
import koda.tanks.Network.NewPlayerMessage;
import koda.tanks.Network.PlayerHitMessage;
import koda.tanks.Network.PlayerRevivedMessage;
import koda.tanks.Network.PositionMessage;
import koda.tanks.Network.ShuttingDownMessage;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class TanksClient extends Listener {

	Client client;
	String name;
	String specialText = "";
	long timeSpecialTextSet;
	GameLogic game;
	boolean alive;
	int id;
	
	public TanksClient(String name) {
		this.name = name;
		game = new GameLogic(this);
		client = new Client();
		Network.registerPackets(client);
		client.addListener(this);
		client.start();
		alive = true;
	}
	
	public void connectLocal() {
		connect("localhost");
	}
	
	public void connect(String host) {
		try {
			client.connect(5000, host, Network.tcpPort, Network.udpPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		Log.info("Client shutting down");
		client.close();
		client.stop();
	}
	
	@Override
	public void connected(Connection c) {
		id = c.getID();
		game.onConnect();
	}
	
	@Override
	public void received(Connection c, Object pkt) {
		if (pkt instanceof NewPlayerMessage) {
			final NewPlayerMessage msg = (NewPlayerMessage) pkt;

			
			game.onNewPlayer(msg);
			
		} else if (pkt instanceof PositionMessage) {
			PositionMessage msg = (PositionMessage) pkt;
			game.onMovementUpdate(msg);
		} else if (pkt instanceof LeaveMessage) {
			LeaveMessage msg = (LeaveMessage) pkt;
			game.onPlayerLeaves(msg);
		} else if (pkt instanceof ShuttingDownMessage) {
			alive = false;
		} else if (pkt instanceof BulletMessage) {
			BulletMessage msg = (BulletMessage) pkt;
			game.onBulletFired(msg);
		} else if (pkt instanceof PlayerHitMessage) {
			PlayerHitMessage msg = (PlayerHitMessage) pkt;
			game.onPlayerHit(msg);
		} else if (pkt instanceof LoginResponseMessage) {
			LoginResponseMessage msg = (LoginResponseMessage) pkt;
			game.onLoginResponse(msg);
		} else if (pkt instanceof PlayerRevivedMessage) {
			PlayerRevivedMessage msg = (PlayerRevivedMessage) pkt;
			game.onPlayerRevived(msg);
		} else if (pkt instanceof ChatMessage) {
			ChatMessage msg = (ChatMessage) pkt;
			game.onChatMessage(msg);
		}
	}
	
	@Override
	public void disconnected(Connection c) {
		
	}
	
	public Player playerByName(String name) {
		for (Player p : game.players.values()) {
			if (p.name.equals(name))
				return p;
		}
		return null;
	}
}
