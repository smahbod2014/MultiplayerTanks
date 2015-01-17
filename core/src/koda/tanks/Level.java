package koda.tanks;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.imageio.ImageIO;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
	
	BitmapFont f = new BitmapFont();
	{
		f.setScale(.75f);
	}
	
	public float offX;
	public float offY;
	
	public int[][] level;
	public int[] pixels;
	public int width;
	public int height;
	
	public Node[][] nodes;
	
	public Array<Wall> walls = new Array<Wall>();
	public Array<Vector2> positions = new Array<Vector2>();
	public ArrayList<Vector2> graphPath = new ArrayList<Vector2>();
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
//						Log.info("Adding wall at x = " + x + ", y = " + y);
					} else if ((px & 0xFF0000) >> 16 == SPAWN) {
						positions.add(new Vector2(x * Entity.TILESIZE + offX, y * Entity.TILESIZE + offY));
						level[x][y] = spawn;
					}
				}
			}
			
			createNodeGraph();
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
	
	private void createNodeGraph() {
		int index = 0;
		nodes = new Node[level.length][level[0].length];
		for (int row = 0; row < level.length; row++) {
			for (int col = 0; col < level[row].length; col++) {
				//this line is the most annoying shit and I hate it...
				nodes[row][col] = new Node(level[col][row], row, col, index++);
			}
		}
		
		int added = 0;
		for (int row = 0; row < nodes.length; row++) {
			for (int col = 0; col < nodes[row].length; col++) {
				Node n = nodes[row][col];
				for (int y = -1; y <= 1; y++) {
					for (int x = -1; x <= 1; x++) {
						if (Math.abs(x) == Math.abs(y))
							continue;
						
						try {
							Node nbr = nodes[row + y][col + x];
							if (nbr.type != wall) {
								n.addNeighbor(nbr);
								added++;
							}
						} catch (IndexOutOfBoundsException e) {}
					}
				}
			}
		}
		
		Log.info("Level created " + nodes.length * nodes[0].length + " nodes and added a total of " + added + " neighbors");
	}
	
	public Vector2 calculateShortestPath(Player chaser, Player target) {
		int cx = (int) ((chaser.x + 1 - offX) / Entity.TILESIZE);
		int cy = (int) ((chaser.y + 1 - offY) / Entity.TILESIZE);
		int tx = (int) ((target.x + 1 - offX) / Entity.TILESIZE);
		int ty = (int) ((target.y + 1 - offY) / Entity.TILESIZE);
		
//		Log.info("cx = " + cx + ", cy = " + cy + ", tx = " + tx + ", ty = " + ty);
		
		Node start = nodes[cy][cx];
		Node goal = nodes[ty][tx];
		Queue<Node> frontier = new PriorityQueue<Node>();
		frontier.add(start);

		Node[] cf = new Node[nodes.length * nodes[0].length];
		cf[start.index] = null;
		int[] costs = new int[nodes.length * nodes[0].length];
		for (int i = 0; i < costs.length; i++)
			costs[i] = -1;
		costs[start.index] = 0;
		
//		Log.info("Starting algorithm");
		while (!frontier.isEmpty()) {
			Node curr = frontier.poll();
			if (curr == goal) {
//				Log.info("Algorithm found victim at " + curr);
				break;
			}
			
			for (Node neighbor : curr.neighbors) {
				int newCost = costs[curr.index] + 1;
				if (costs[neighbor.index] == -1 || newCost < costs[neighbor.index]) {
					costs[neighbor.index] = newCost;
					int priority = newCost + heuristic(goal, neighbor);
					neighbor.priority = priority;
					frontier.add(neighbor);
					cf[neighbor.index] = curr;
				}
			}
		}
		
		ArrayList<Node> path = new ArrayList<Node>();
		
		Node current = goal;
		path.add(current);
		while (current != start) {
			current = cf[current.index];
			if (current == null) {
				Log.info("current was null. Path size was: " + path.size());
				break;
			}
			path.add(current);
		}
		
		//the last node in path is the chaser
		//the first node in path is the victim
		
//		path.remove(path.size() - 1);
		
		graphPath.clear();
		for (Node n : path) {
			graphPath.add(new Vector2(n.col * Entity.TILESIZE + offX, n.row * Entity.TILESIZE + offY));
		}
		
//		Log.info("Done filling graphPath. Path size is " + graphPath.size());
		
		if (path.size() == 1)
			return new Vector2(cx * Entity.TILESIZE + offX, cy * Entity.TILESIZE + offY);
		
		Node result = path.get(path.size() - 2);
		return new Vector2(result.col * Entity.TILESIZE + offX, result.row * Entity.TILESIZE + offY);
	}
	
	public int heuristic(Node a, Node b) {
		return Math.abs(a.col - b.col) + Math.abs(a.row - b.row);
	}
	
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
		
//		if (Player.BOT_DEBUG) {
//			batch.begin();
//			for (int row = 0; row < nodes.length; row++) {
//				for (int col = 0; col < nodes[row].length; col++) {
//					Node n = nodes[row][col];
//					if (n.type == wall)
//						continue;
//					
//					float ts = Entity.TILESIZE;
//					float tx = n.col * ts + offX + f.getBounds(Integer.toString(n.index)).width / 2;
//					float ty = n.row * ts + offY + ts - f.getBounds(Integer.toString(n.index)).height / 2;
//					f.draw(batch, Integer.toString(n.index), tx, ty);
//				}
//			}
//			batch.end();
//		}
		
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
			game.sr.setProjectionMatrix(game.levelCam.combined);
			game.sr.begin(ShapeType.Line);
			game.sr.box(camLeft, camBot, 0, width, height, 0);
			game.sr.end();
		}
	}
}
