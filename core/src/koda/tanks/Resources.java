package koda.tanks;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Resources {

	private TextureAtlas atlas;
	private HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();
	private HashMap<String, Sound> sounds = new HashMap<String, Sound>();
	private HashMap<String, BitmapFont> fonts = new HashMap<String, BitmapFont>();
	private ArrayList<SpriteBatch> psbs = new ArrayList<SpriteBatch>();
	private ArrayList<ShaderProgram> shaders = new ArrayList<ShaderProgram>();
	private GameLogic game;
	
	public Resources(GameLogic game) {
		this.game = game;
		ShaderProgram.pedantic = false;
		for (int i = 0; i < 5; i++) {
			SpriteBatch psb = new SpriteBatch();
			ShaderProgram shader = new ShaderProgram(Gdx.files.internal("shaders/flash.vsh"), Gdx.files.internal("shaders/flash.fsh"));
			psb.setShader(shader);
			psbs.add(psb);
			shaders.add(shader);
		}
	}
	
	public void loadSound(String alias, String path) {
		sounds.put(alias, Gdx.audio.newSound(Gdx.files.internal("sounds/" + path)));
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
	
	public SpriteBatch getFlashSpriteBatch() {
		return psbs.remove(0);
	}
	
	public ShaderProgram getFlashShader() {
		return shaders.remove(0);
	}
	
	public void replenishFlashSpriteBatch(SpriteBatch batch) {
		psbs.add(batch);
	}
	
	public void replenishFlashShader(ShaderProgram shader) {
		shaders.add(shader);
	}
}
