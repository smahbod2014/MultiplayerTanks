package koda.tanks;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.swing.JOptionPane;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.esotericsoftware.kryonet.Client;

public class MenuScreen implements Screen {

	Tanks game;
	BitmapFont test;
	String[] names = {"Bob", "Andy", "Charlie", "Roger", "Jack"};
	String instructions = "SPACE to create server, M to join";
	Stage stage;
	Skin skin;
	TextField tf;
	int nameIndex = 0;
	String title = "Multiplayer Tanks - " + names[nameIndex];
	
	public MenuScreen(Tanks game) {
		this.game = game;
		Gdx.graphics.setTitle(title);
//		stage = new Stage();
//		skin = new Skin(new TextureAtlas(Gdx.files.internal("tanks_pack.pack")));
//		tf = new TextField("localhost", skin);
//		stage.addActor(tf);
	}
	
	@Override
	public void render(float delta) {
		int width = Gdx.graphics.getWidth();
		game.batch.begin();
		test.draw(game.batch, instructions, width / 2 - test.getBounds(instructions).width / 2, Gdx.graphics.getHeight() / 2 - 50);
		test.draw(game.batch, "Name is: " + names[nameIndex], 0, Gdx.graphics.getHeight() / 2 - 100);
		game.batch.end();
		
		if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
//			Gdx.graphics.setTitle("Multiplayer Tanks - " + names[nameIndex] + " (Server)");
			
			try {
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					NetworkInterface iface = interfaces.nextElement();
					if (iface.isLoopback() || !iface.isUp())
						continue;
					
					Enumeration<InetAddress> addresses = iface.getInetAddresses();
					while (addresses.hasMoreElements()) {
						String ip = addresses.nextElement().getHostAddress();
						if (ip.indexOf("128") == 0) {
							title = "Multiplayer Tanks - " + names[nameIndex] + " (" + ip + ")";
							Gdx.graphics.setTitle(title);
						}
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
			
			game.setScreen(new PlayScreen(true, "localhost", names[nameIndex], title));
		}
		
		if (Gdx.input.isKeyJustPressed(Keys.M)) {
			System.out.println("Attempting automatic lookup...");
			find();
		}
		
		if (Gdx.input.isKeyJustPressed(Keys.L)) {
			nameIndex = (nameIndex + 1) % names.length;
			title = "Multiplayer Tanks - " + names[nameIndex];
			Gdx.graphics.setTitle(title);
		}
		
//		stage.draw();
	}
	
	private void find() {
		Client client = new Client();
		client.start();
		InetAddress found = client.discoverHost(Network.udpPort, 5000);
		if (found == null) {
			System.out.println("No server found. Enter an ip.");
//			game.setScreen(new PlayScreen(true, "localhost", names[nameIndex]));
			String ip = JOptionPane.showInputDialog(null, "Input ip:", "Join server", JOptionPane.INFORMATION_MESSAGE);
			if (ip == null) {
				client.close();
				client.stop();
				return;
			}
			game.setScreen(new PlayScreen(false, ip, names[nameIndex], title));
		} else {
			System.out.println("Server found! Connecting as a client");
			game.setScreen(new PlayScreen(false, found.getHostAddress(), names[nameIndex], title));
		}
		client.close();
		client.stop();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		test = new BitmapFont(Gdx.files.internal("comicsans.fnt"), false);
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		test.dispose();
	}

}
