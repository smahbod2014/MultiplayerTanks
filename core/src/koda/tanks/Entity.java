package koda.tanks;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {

	public static final float TILESIZE = 30;
	public static final int UP = 90;
	public static final int LEFT = 180;
	public static final int DOWN = 270;
	public static final int RIGHT = 0;
	protected static final Rectangle screen = new Rectangle(0, 0, 0, 0);
	
	public static final boolean DEBUG = false;
	
	public float x;
	public float y;
	public float prevX;
	public float prevY;
	public int angle;
	public int prevAngle;
	public boolean alive;
	public Sprite sprite;
	public GameLogic game;
	//make TimerSystem static?
	public TimerSystem timers = new TimerSystem();
	protected final Rectangle bounds = new Rectangle(0, 0, TILESIZE, TILESIZE);
	
	
	protected Entity(GameLogic game, Sprite sprite, float x, float y) {
		this.game = game;
		this.x = x;
		this.y = y;
		this.prevX = x;
		this.prevY = y;
		this.alive = true;
		if (sprite != null) {
			this.sprite = new Sprite(sprite);
			this.sprite.setX(x);
			this.sprite.setY(y);
		}
	}
	
	public boolean changedPosAngle() {
		return changedPosition() || angle != prevAngle;
	}
	
	public boolean changedPosition() {
		return x != prevX || y != prevY;
	}
	
	public void cleanPosition() {
		prevX = x;
		prevY = y;
	}
	
	public void cleanPosAngle() {
		cleanPosition();
		prevAngle = angle;
	}
	
	protected Rectangle bounds() {
		bounds.x = x;
		bounds.y = y;
		return bounds;
	}
	
	public boolean collidesWith(Rectangle r) {
		return bounds().overlaps(r);
	}
	
	public boolean collidesWith(Entity e) {
		return bounds().overlaps(e.bounds()) || bounds().contains(e.bounds());
	}
	
	public boolean outOfBounds() {
		return !screen.contains(bounds());
	}
	
	public void setPosition(float x, float y, int angle) {
		this.x = x;
		this.y = y;
		this.angle = angle;
	}
	
	public void updateSprite() {
		if (sprite.getX() != x)
			sprite.setX(x);
		
		if (sprite.getY() != y)
			sprite.setY(y);
		
		if (sprite.getRotation() != angle - 90)
			sprite.setRotation(angle - 90);
	}
	
	public int correctedAngle() {
		int a = angle % 360;
		if (a < 0)
			a += 360;
		return a;
	}
	
	public boolean isWithin(float test, float against, float tolerance) {
		return test >= against - tolerance && test <= against + tolerance;
	}
	
	public void input(float dt) {}
	
	public void update(float dt) {}
	
	public void render(SpriteBatch batch) {
		batch.begin();
		updateSprite();
		sprite.draw(batch);
		batch.end();
		
		if (DEBUG) {
			game.sr.setProjectionMatrix(game.levelCam.combined);
			game.sr.begin(ShapeType.Line);
			game.sr.box(bounds().x, bounds().y, 0, bounds().width, bounds().height, 0);
			game.sr.box(screen.x, screen.y, 0, screen.width, screen.height, 0);
			game.sr.end();
		}
	}
	
	@Override
	public String toString() {
		return "Bounds: (" + bounds().x + ", " + bounds().y + "), width = " + bounds().width + ", height = " + bounds().height;
	}
}
