package koda.tanks;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.minlog.Log;

public class Level {

	public static Texture floorTex = new Texture(Gdx.files.internal("tanks_floor_tile.png"));
	public static Texture wallTex = new Texture(Gdx.files.internal("tanks_wall_tile.png"));
	public static final int FLOOR = 255; //green
	public static final int WALL = 200; //red
	public static final int SPAWN = 255; //red
	public static final int floor = 0;
	public static final int wall = 1;
	public static final int spawn = 2;
	public static final int CAMERA_TILES_IN_X = 8;
	public static final int CAMERA_TILES_IN_Y = 6;
	
	public float offX;
	public float offY;
	
	public int[][] level;
	public int[] pixels;
	public int width;
	public int height;
	
	public Array<Wall> walls = new Array<Wall>();
	public Array<Vector2> positions = new Array<Vector2>();
	public GameLogic game;
	
	public Level(GameLogic game) {
		this.game = game;
	}
	
	public void createLevel(String filename) {
		try {
			long now = System.currentTimeMillis();
			BufferedImage buff = ImageIO.read(new File("C:/Users/Sean/Desktop/libgdxdestination/MultiplayerTanks/core/assets/tanks_level1.png"));
			width = buff.getWidth();
			height = buff.getHeight();
			pixels = new int[width * height];
			buff.getRGB(0, 0, width, height, pixels, 0, width);
			
			flipY();
			
			int id = 1;
			level = new int[height][width];
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int px = getPixel(x, y);
					if ((px & 0x00FF00) >> 8 == FLOOR) {
						level[x][y] = floor;
					} else if ((px & 0xFF0000) >> 16 == WALL) {
						Wall w = new Wall(game, new Sprite(wallTex), x * Entity.TILESIZE + offX, y * Entity.TILESIZE + offY);
						w.id = id;
						id++;
						walls.add(w);
						level[x][y] = wall;
					} else if ((px & 0xFF0000) >> 16 == SPAWN) {
						positions.add(new Vector2(x * Entity.TILESIZE + offX, y * Entity.TILESIZE + offY));
						level[x][y] = spawn;
					}
				}
			}
			
			long elapsed = System.currentTimeMillis() - now;
			Log.info("Generated the map in " + elapsed / 1000.0 + " seconds");
			
			
			Entity.screen.x = offX;
			Entity.screen.y = offY;
			Entity.screen.width = width * Entity.TILESIZE;
			Entity.screen.height = height * Entity.TILESIZE;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void flipY() {
		int[] temp = new int[pixels.length];
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				temp[i + j * width] = pixels[i + (height - j - 1) * width];
			}
		}
		
		pixels = temp;
	}
	
	private int getPixel(int x, int y) {
		return pixels[x + y * width];
	}
	
	public void checkCameraPosition() {
		float ts = Entity.TILESIZE;
		float camLeft = offX + CAMERA_TILES_IN_X * ts;
		float camBot = offY + CAMERA_TILES_IN_Y * ts;
		float camRight = offX + width * ts - CAMERA_TILES_IN_X * ts;
		float camTop = offY + height * ts - CAMERA_TILES_IN_Y * ts;
		
		if (game.levelCam.position.x < camLeft)
			game.levelCam.position.x = camLeft;
		
		if (game.levelCam.position.x > camRight)
			game.levelCam.position.x = camRight;
		
		if (game.levelCam.position.y < camBot)
			game.levelCam.position.y = camBot;
		
		if (game.levelCam.position.y > camTop)
			game.levelCam.position.y = camTop;
		
		game.levelCam.update();
	}
	
//	public void createSampleLevel() {
//		level = new int[10][10];
//		level[0] = new int[] {w,w,w,w,w,w,w,w,w,w};
//		level[1] = new int[] {w,f,f,f,f,f,f,f,f,w};
//		level[2] = new int[] {w,f,w,w,f,f,w,w,f,w};
//		level[3] = new int[] {w,f,w,w,f,f,w,w,f,w};
//		level[4] = new int[] {w,f,f,f,f,f,f,f,f,w};
//		level[5] = new int[] {w,f,f,f,f,f,f,f,f,w};
//		level[6] = new int[] {w,f,w,w,f,f,w,w,f,w};
//		level[7] = new int[] {w,f,w,w,f,f,w,w,f,w};
//		level[8] = new int[] {w,f,f,f,f,f,f,f,f,w};
//		level[9] = new int[] {w,w,w,w,w,w,w,w,w,w};
//		createLevel(level);
//	}
	
//	public void createLevel(int[][] m) {
//		walls.clear();
//		level = m;
//		int id = 1;
//		for (int i = 0; i < level.length; i++) {
//			for (int j = 0; j < level[i].length; j++) {
//				if (level[i][j] == w) {
//					Wall wa = new Wall(game, new Sprite(wall), j * Entity.TILESIZE + offX, i * Entity.TILESIZE + offY);
//					wa.id = id;
//					walls.add(wa);
//					id++;
//				}
//			}
//		}
//	}
	
	public void render(SpriteBatch batch) {
		batch.begin();
		for (int i = 0; i < level.length; i++) {
			for (int j = 0; j < level[i].length; j++) {
				if (level[j][i] == floor || level[j][i] == spawn) {
					batch.draw(floorTex, j * Entity.TILESIZE + offX, i * Entity.TILESIZE + offY);
				}
			}
		}
		batch.end();
		
		for (Wall wall : walls) {
			wall.render(batch);
		}
		
		if (Entity.DEBUG) {
//			for (int i = 0; i < level.length; i++) {
//				for (int j = 0; j < level[i].length; j++) {
//					if (level[j][i] == spawn) {
//						game.sr.setProjectionMatrix(game.levelCam.combined);
//						game.sr.begin(ShapeType.Filled);
//						game.sr.circle(j * Entity.TILESIZE + offX + Entity.TILESIZE / 2, i * Entity.TILESIZE + offY + Entity.TILESIZE / 2, Entity.TILESIZE / 2);
//						game.sr.end();
//					}
//				}
//			}
			
			for (Vector2 pos : positions) {
				game.sr.setProjectionMatrix(game.levelCam.combined);
				game.sr.begin(ShapeType.Line);
				game.sr.circle(pos.x + Entity.TILESIZE / 2, pos.y + Entity.TILESIZE / 2, Entity.TILESIZE / 2);
				game.sr.end();
			}
			
			float ts = Entity.TILESIZE;
			float camLeft = offX + CAMERA_TILES_IN_X * ts;
			float camBot = offY + CAMERA_TILES_IN_Y * ts;
			float width = this.width * ts - 2 * CAMERA_TILES_IN_X * ts;
			float height = this.height * ts - 2 * CAMERA_TILES_IN_Y * ts;
			game.sr.begin(ShapeType.Line);
			game.sr.box(camLeft, camBot, 0, width, height, 0);
			game.sr.end();
		}
	}
}
