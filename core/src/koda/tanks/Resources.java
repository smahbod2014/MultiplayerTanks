package koda.tanks;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Resources {

	private TextureAtlas atlas;
	private HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();
	private HashMap<String, Sound> sounds = new HashMap<String, Sound>();
	private HashMap<String, BitmapFont> fonts = new HashMap<String, BitmapFont>();
	private GameLogic game;
	
	public Resources(GameLogic game) {
		this.game = game;
	}
	
	public void loadSound(String alias, String path) {
		sounds.put(alias, Gdx.audio.newSound(Gdx.files.internal(path)));
	}
	
	public void loadAtlas(String path) {
		atlas = new TextureAtlas(Gdx.files.internal(path));
	}
	
	public void loadSpriteFromAtlas(String alias, String name) {
		sprites.put(alias, atlas.createSprite(name));
	}
	
	public void loadSprite(String alias, String path) {
		sprites.put(alias, new Sprite(new Texture(Gdx.files.internal(path))));
	}
	
	public void loadFont(String alias, String path) {
		fonts.put(alias, new BitmapFont(Gdx.files.internal(path)));
	}
	
	public void playSound(String name, float volume) {
		if (!game.windowMinimized)
			sounds.get(name).play(volume);
	}
	
	public Sprite getSprite(String name) {
		return sprites.get(name);
	}
	
	public BitmapFont getFont(String name) {
		return fonts.get(name);
	}
}
