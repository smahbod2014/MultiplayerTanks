package koda.tanks;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ScoreEntry {

	public String name;
	public int score;
	
	public ScoreEntry(String name, int score) {
		this.name = name;
		this.score = score;
	}
	
	@Override
	public String toString() {
		return name + "    " + score;
	}
}
