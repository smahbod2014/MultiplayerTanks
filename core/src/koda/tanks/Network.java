package koda.tanks;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

public class Network {

	public static int tcpPort = 27960;
	public static int udpPort = 27961;
	
	public static void registerPackets(EndPoint ep) {
		Kryo k = ep.getKryo();
		k.register(LoginResponseMessage.class);
		k.register(PositionMessage.class);
		k.register(NewPlayerMessage.class);
		k.register(LeaveMessage.class);
		k.register(ShuttingDownMessage.class);
		k.register(BulletMessage.class);
		k.register(PlayerHitMessage.class);
	}
	
	public static class ShuttingDownMessage {}
	
	public static class NewPlayerMessage {
		int pid;
		String name;
		float x;
		float y;
		int dir;
		int hp;
	}
	
	public static class PositionMessage {
		int pid;
		float x;
		float y;
		int dir;
	}
	
	public static class LeaveMessage {
		int pid;
	}
	
	public static class BulletMessage {
		int pid;
		String name;
		boolean newBullet;
		float x;
		float y;
		int dir;
	}
	
	public static class PlayerHitMessage {
		int bIndex;
		String shooterName;
		String victimName;
	}
	
	public static class LoginResponseMessage {
		float x;
		float y;
		int dir;
		int hp;
	}
}
