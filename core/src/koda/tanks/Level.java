package koda.tanks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Level {

	public static Texture floor = new Texture(Gdx.files.internal("tanks_floor_tile.png"));
	public static Texture wall = new Texture(Gdx.files.internal("tanks_wall_tile.png"));
	public static final int f = 0;
	public static final int w = 1;
	
	public float offX;
	public float offY;
	
	public int[][] level;
	
	public Array<Wall> walls = new Array<Wall>();
	public GameLogic game;
	
	public Level(GameLogic game) {
		this.game = game;
	}
	
	public void createSampleLevel() {
		level = new int[10][10];
		level[0] = new int[] {w,w,w,w,w,w,w,w,w,w};
		level[1] = new int[] {w,f,f,f,f,f,f,f,f,w};
		level[2] = new int[] {w,f,w,w,f,f,w,w,f,w};
		level[3] = new int[] {w,f,w,w,f,f,w,w,f,w};
		level[4] = new int[] {w,f,f,f,f,f,f,f,f,w};
		level[5] = new int[] {w,f,f,f,f,f,f,f,f,w};
		level[6] = new int[] {w,f,w,w,f,f,w,w,f,w};
		level[7] = new int[] {w,f,w,w,f,f,w,w,f,w};
		level[8] = new int[] {w,f,f,f,f,f,f,f,f,w};
		level[9] = new int[] {w,w,w,w,w,w,w,w,w,w};
		createLevel(level);
	}
	
	public void createLevel(int[][] m) {
		walls.clear();
		level = m;
		for (int i = 0; i < level.length; i++) {
			for (int j = 0; j < level[i].length; j++) {
				if (level[i][j] == w) {
					walls.add(new Wall(game, new Sprite(wall), j * Entity.TILESIZE + offX, i * Entity.TILESIZE + offY));
				}
			}
		}
	}
	
	public void render(SpriteBatch batch) {
		batch.begin();
		for (int i = 0; i < level.length; i++) {
			for (int j = 0; j < level[i].length; j++) {
				if (level[i][j] == f) {
					batch.draw(floor, j * Entity.TILESIZE + offX, i * Entity.TILESIZE + offY);
				}
			}
		}
		batch.end();
		
		for (Wall wall : walls) {
			wall.render(batch);
		}
	}
}
