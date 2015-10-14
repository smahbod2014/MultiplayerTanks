package koda.tanks;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ChatLog {

	private static final long CHAT_MESSAGE_DURATION = 4000;
	private static ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
	private static BitmapFont messageFont = new BitmapFont();
	private static float textHeight = messageFont.getBounds("A").height;
	
	public static boolean typing = false;
	
	public static void render(SpriteBatch batch) {
		batch.begin();
		float x = 2;
		float y = 25;
		for (int i = 0; i < messages.size(); i++) {
			ChatMessage msg = messages.get(i);
			messageFont.draw(batch, msg.sender + ": " + msg.message, x, y);
			y += textHeight;
			if (msg.shouldRemove()) {
				messages.remove(i);
				i--;
			}
		}
		batch.end();
	}
	
	public static void addMessage(String sender, String message) {
		messages.add(0, new ChatMessage(sender, message));
	}
	
	public static void input() {
		if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			typing = !typing;
		}
		
//		if (typing) {
//			if (Gdx.input.isKeyJustPressed)
//				if (Gdx.input.cap)
//		}
	}
	
	private static class ChatMessage {
		private TimerSystem ts = new TimerSystem();
		private String sender;
		private String message;
		
		public ChatMessage(String sender, String message) {
			this.ts = new TimerSystem();
			this.sender = sender;
			this.message = message;
			ts.addTimer("message", CHAT_MESSAGE_DURATION);
			ts.resetWithDelay("message");
		}
		
		public boolean shouldRemove() {
			return ts.finished("messsage");
		}
	}
}
