package koda.tanks;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.kryonet.Client;

public class MenuScreen implements Screen {

	Pattern ipPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");
	Pattern usernamePattern = Pattern.compile("[a-zA-Z0-9]{3,}");
	
	Tanks game;
	BitmapFont test;
	String[] names = {"Bob", "Andy", "Charlie", "Roger", "Jack"};
	String instructions = "SPACE to create server, M to join";
	Stage stage;
	Skin skin;
	Table table;
	TextField tfUsername;
	TextField tfIP;
	Label lblUsername;
	Label lblIP;
	TextButton btnConnect;
	TextButton btnSearch;
	TextButton btnHost;
	int nameIndex = 0;
	String title = "Multiplayer Tanks - ";
	boolean gameStarting = false;
	boolean isHost = false;
	String gameIP = "";
	String username;
	
	public MenuScreen(Tanks game) {
		this.game = game;
		Gdx.graphics.setTitle(title);
		stage = new Stage();
		//Gdx.input.setCursorImage(null, 1, 1);
//		skin = new Skin(new TextureAtlas(Gdx.files.internal("tanks_pack.pack")));
//		tf = new TextField("localhost", skin);
//		stage.addActor(tf);
	}
	
	@Override
	public void render(float delta) {
		int width = Gdx.graphics.getWidth();
		/*game.batch.begin();
		test.draw(game.batch, instructions, width / 2 - test.getBounds(instructions).width / 2, Gdx.graphics.getHeight() / 2 - 50);
		test.draw(game.batch, "Name is: " + names[nameIndex], 0, Gdx.graphics.getHeight() / 2 - 100);
		game.batch.end();*/
		
		/*if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
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
		}*/
		
		/*if (Gdx.input.isKeyJustPressed(Keys.M)) {
			System.out.println("Attempting automatic lookup...");
			find();
		}*/
		
		/*if (Gdx.input.isKeyJustPressed(Keys.L)) {
			nameIndex = (nameIndex + 1) % names.length;
			title = "Multiplayer Tanks - " + names[nameIndex];
			Gdx.graphics.setTitle(title);
		}*/
		
		stage.act(delta);
		stage.draw();
		
		if (gameStarting) {
			if (isHost) {
				game.setScreen(new PlayScreen(true, gameIP, username, title));
			} else {
				game.setScreen(new PlayScreen(false, gameIP, username, title));
			}
		}	
	}
	
	private String find() {
		Client client = new Client();
		client.start();
		InetAddress found = client.discoverHost(Network.udpPort, 5000);
		if (found == null) {
//			System.out.println("No server found. Enter an ip.");
//			String ip = JOptionPane.showInputDialog(null, "Input ip:", "Join server", JOptionPane.INFORMATION_MESSAGE);
//			if (ip == null) {
//				client.close();
//				client.stop();
//				return;
//			}
//			game.setScreen(new PlayScreen(false, ip, names[nameIndex], title));
		} else {
			//System.out.println("Server found! Connecting as a client");
			//game.setScreen(new PlayScreen(false, found.getHostAddress(), names[nameIndex], title));
			String ip = found.getHostAddress();
			if (ip.indexOf(":") != -1) {
				ip = ip.substring(0, ip.indexOf(":"));
			}
			return ip;
		}
		client.close();
		client.stop();
		return "none";
	}

	@Override
	public void resize(int width, int height) {
		//stage.setViewport(new ScalingViewport(Scaling.fit, width, height));
		table.invalidateHierarchy();
	}

	@Override
	public void show() {
		test = new BitmapFont(Gdx.files.internal("comicsans.fnt"), false);
		skin = new Skin(Gdx.files.internal("uiskin.json"));
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		table = new Table();
		//table.setBounds(Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2, 100, Gdx.graphics.getHeight() / 2);
		
		lblUsername = new Label("Enter username", skin);
		lblIP = new Label("Enter IP", skin);
		tfUsername = new TextField("", skin);
		tfIP = new TextField("", skin);
		btnConnect = new TextButton("Connect", skin);
		btnSearch = new TextButton("Search", skin);
		btnHost = new TextButton("Host", skin);
		//btnConnect.setDisabled(true);
		btnConnect.setTouchable(Touchable.disabled);
		btnHost.setTouchable(Touchable.disabled);
		
		table.setFillParent(true);
		table.center();
		table.add(lblUsername).colspan(3).center().padRight(10);
		table.add(tfUsername).colspan(3).center().padBottom(5).row();
		table.add(lblIP).colspan(3).center();
		table.add(tfIP).colspan(3).center().padBottom(5).row();
		table.add(btnConnect).colspan(2).padRight(5).fill();
		table.add(btnSearch).colspan(2).padRight(5).fill();
		table.add(btnHost).colspan(3).fill();
		
		stage.addActor(table);
		
		btnConnect.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				gameStarting = true;
				isHost = false;
				username = tfUsername.getText();
				gameIP = tfIP.getText();
				title = "Multiplayer Tanks - " + names[nameIndex];
				Gdx.graphics.setTitle(title);
			}
		});
		
		tfUsername.addListener(new InputListener() {
			@Override
			public boolean keyTyped(InputEvent event, char character) {
				checkInput();
				return true;
			}
		});
		
		tfIP.addListener(new InputListener() {
			@Override
			public boolean keyTyped(InputEvent event, char character) {
				checkInput();
				return true;
			}
		});
		
		btnSearch.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				tfUsername.setDisabled(true);
				tfIP.setDisabled(true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						System.out.println("Starting");
						String ip = find();
						System.out.println("Finished");
						tfUsername.setDisabled(false);
						tfIP.setDisabled(false);
						if (!ip.equals("none")) {
							tfIP.setText(ip);
						}
					}
				}).start();
			}
		});
		
		btnHost.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				gameStarting = true;
				isHost = true;
				username = tfUsername.getText();
				gameIP = "localhost";
				displayIP();
			}
		});
	}
	
	private void checkInput() {
		Matcher mIP = ipPattern.matcher(tfIP.getText());
		Matcher mUsername = usernamePattern.matcher(tfUsername.getText());
		if (mUsername.matches() && (mIP.matches() || tfIP.getText().equals("localhost"))) {
			btnConnect.setTouchable(Touchable.enabled);
		} else {
			btnConnect.setTouchable(Touchable.disabled);
		}
		
		if (mUsername.matches()) {
			btnHost.setTouchable(Touchable.enabled);
		} else {
			btnHost.setTouchable(Touchable.disabled);
		}
	}
	
	private void displayIP() {
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
						title = "Multiplayer Tanks - " + username + " (" + ip + ")";
						Gdx.graphics.setTitle(title);
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
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
		stage.dispose();
	}
}
