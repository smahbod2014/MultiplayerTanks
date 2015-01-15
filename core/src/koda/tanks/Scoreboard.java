package koda.tanks;

import java.util.ArrayList;
import java.util.Collections;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Scoreboard {

	public static final float HEIGHT = 25;
	
//	Array<ScoreEntry> entries = new Array<ScoreEntry>();
	ArrayList<ScoreEntry> entries = new ArrayList<ScoreEntry>();
	BitmapFont font;
	BitmapFont localFont;
	float offX;
	float offY;
	String localPlayerName;
	
	public Scoreboard(BitmapFont font, BitmapFont localFont) {
		this.font = font;
		this.localFont = localFont;
//		this.localFont.setColor(89f/255, 205f/255, 121f/255, 1);
		this.localFont.setColor(Color.RED);
	}
	
	public Scoreboard() {}
	
	public void addEntry(String name, int score) {
		entries.add(new ScoreEntry(name, score));
		Collections.sort(entries);
	}
	
	public void updateEntryAdd(String name, int score) {
		for (ScoreEntry s : entries) {
			if (s.name.equals(name)) {
				s.score += score;
				Collections.sort(entries);
			}
		}
	}
	
	public void updateEntrySet(String name, int score) {
		for (ScoreEntry s : entries) {
			if (s.name.equals(name)) {
				s.score = score;
				Collections.sort(entries);
			}
		}
	}
	
	public void removeEntry(String name) {
		for (int i = entries.size() - 1; i >= 0; i--) {
			if (entries.get(i).name.equals(name))
				entries.remove(i);
		}
	}
	
	public int getScore(String name) {
		for (int i = entries.size() - 1; i >= 0; i--) {
			if (entries.get(i).name.equals(name))
				return entries.get(i).score;
		}
		
		return -1;
	}
	
	public void render(SpriteBatch batch) {
		if (font == null)
			return;
		
		float y = Gdx.graphics.getHeight() - offY;
		batch.begin();
		font.draw(batch, "Name    Score", offX, y);
		y -= HEIGHT;
		for (int i = 0; i < entries.size(); i++) {
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
