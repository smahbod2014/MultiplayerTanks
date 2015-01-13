package koda.tanks;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {

	public static final float TILESIZE = 30;
	public static final int UP = 0;
	public static final int LEFT = 1;
	public static final int DOWN = 2;
	public static final int RIGHT = 3;
	protected static final Rectangle screen = new Rectangle(0, 0, 0, 0);
	
	public float x;
	public float y;
	public float prevX;
	public float prevY;
	public int dir;
	public int prevDir;
	public boolean alive;
	public Sprite sprite;
	public GameLogic game;
	protected final Rectangle bounds = new Rectangle(0, 0, (int) TILESIZE, (int) TILESIZE);
	
	
	protected Entity(GameLogic game, Sprite sprite, float x, float y) {
		this.game = game;
		this.sprite = new Sprite(sprite);
//		this.sprite = sprite;
		this.x = x;
		this.y = y;
		this.prevX = x - 1;
		this.prevY = y - 1;
		this.sprite.setX(x);
		this.sprite.setY(y);
		this.alive = true;
	}
	
	public boolean changed() {
		return true;
	}
	
	protected void clean() {}
	
	protected void updateBounds() {
		bounds.x = (int) x;
		bounds.y = (int) y;
	}
	
	public boolean collidesWith(Entity e) {
		updateBounds();
		e.updateBounds();
		return bounds.overlaps(e.bounds) || bounds.contains(e.bounds);
	}
	
	public boolean outOfBounds() {
		updateBounds();
		return !screen.contains(bounds);
	}
	
	public void setPosition(float x, float y, int dir) {
		this.x = x;
		this.y = y;
		sprite.setX(x);
		sprite.setY(y);
		rotateSprite(dir);
	}
	
	protected void rotateSprite(int dir) {
		this.dir = dir;
		switch (dir) {
		case UP:
			sprite.setRotation(0);
			break;
		case LEFT:
			sprite.setRotation(90);
			break;
		case DOWN:
			sprite.setRotation(180);
			break;
		case RIGHT:
			sprite.setRotation(270);
			break;
		}
	}
	
	public void input() {}
	
	public void update(float dt) {
//		System.out.println("Setting sprite x to " + x + ". Old x is " + sprite.getX());
		sprite.setX(x);
		sprite.setY(y);
//		System.out.println("New x is " + sprite.getX());
	}
	
	public void render(SpriteBatch batch) {
		batch.begin();
//		System.out.println("Sprite x is " + sprite.getX());
		sprite.draw(batch);
		batch.end();
	}
}
