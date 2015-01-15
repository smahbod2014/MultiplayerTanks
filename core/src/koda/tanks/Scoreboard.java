package koda.tanks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Scoreboard {

	public static final float HEIGHT = 25;
	
	Array<ScoreEntry> entries;
	BitmapFont font;
	BitmapFont localFont;
	float offX;
	float offY;
	String localPlayerName;
	
	public Scoreboard(BitmapFont font, BitmapFont localFont) {
		entries = new Array<ScoreEntry>();
		this.font = font;
		this.localFont = localFont;
//		this.localFont.setColor(89f/255, 205f/255, 121f/255, 1);
		this.localFont.setColor(Color.RED);
	}
	
	public void addEntry(String name) {
		entries.add(new ScoreEntry(name, 0));
	}
	
	public void updateEntryAdd(String name, int score) {
		for (ScoreEntry s : entries) {
			if (s.name.equals(name)) {
				s.score += score;
			}
		}
	}
	
	public void updateEntrySet(String name, int score) {
		for (ScoreEntry s : entries) {
			if (s.name.equals(name)) {
				s.score = score;
			}
		}
	}
	
	public void removeEntry(String name) {
		for (int i = entries.size - 1; i >= 0; i--) {
			if (entries.get(i).name.equals(name))
				entries.removeIndex(i);
		}
	}
	
	public void render(SpriteBatch batch) {
		float y = Gdx.graphics.getHeight() - offY;
		batch.begin();
		font.draw(batch, "Name    Score", offX, y);
		y -= HEIGHT;
		for (int i = 0; i < entries.size; i++) {
			ScoreEntry s = entries.get(i);
			if (s.name.equals(localPlayerName))
				localFont.draw(batch, s.toString(), offX, y);
			else
				font.draw(batch, s.toString(), offX, y);
			y -= HEIGHT;
		}
		batch.end();
	}
}
