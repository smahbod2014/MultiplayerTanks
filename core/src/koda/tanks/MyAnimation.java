package koda.tanks;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MyAnimation {

	public enum Mode {REGULAR, PINGPONG, REVERSED, ONLY_ONCE};
	
	private TextureRegion[] frames;
	private float frameTime;
	private float accum;
	private int currentFrame;
	private Mode mode;
	private Mode pingPongSubMode;
	private boolean running;
	private boolean done;
	
	public MyAnimation(float frameTime, TextureRegion[] frames) {
		this.frameTime = frameTime;
		this.frames = frames;
	}
	
	public void update(float dt) {
		if (!running)
			return;
		
		accum += dt;
		if (accum >= frameTime) {
			accum = 0;
			nextFrame();
		}
	}
	
	private void nextFrame() {
		switch (mode) {
		case REGULAR:
			currentFrame = (currentFrame + 1) % frames.length;
			break;
		case REVERSED:
			currentFrame = (currentFrame - 1) % frames.length;
			if (currentFrame < 0)
				currentFrame += frames.length;
			break;
		case PINGPONG:
			switch (pingPongSubMode) {
			case REGULAR:
				currentFrame++;
				if (currentFrame == frames.length - 1) {
					pingPongSubMode = Mode.REVERSED;
				}
				break;
			case REVERSED:
				currentFrame--;
				if (currentFrame == 0) {
					pingPongSubMode = Mode.REGULAR;
				}
				break;
			}
			break;
		case ONLY_ONCE:
			currentFrame++;
			if (currentFrame == frames.length) {
				currentFrame--;
				done = true;
				pause();
			}
		}
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
		if (mode == Mode.PINGPONG) {
			pingPongSubMode = Mode.REGULAR;
		}
	}
	
	public void play() {
		running = true;
	}
	
	public void pause() {
		running = false;
	}
	
	public void stop() {
		running = false;
		currentFrame = 0;
		if (mode == Mode.PINGPONG)
			pingPongSubMode = Mode.REGULAR;
	}
	
	public void reset() {
		currentFrame = 0;
		done = false;
	}
	
	public boolean isDone() {
		return done;
	}
	
	public float frameWidth() {
		return frames[0].getRegionWidth();
	}
	
	public float frameTime() {
		return frameTime;
	}
	
	public TextureRegion getCurrentFrame() {
		return frames[currentFrame];
	}
	
	public void setFrame(int frame) {
		currentFrame = frame;
	}
	
	public int frame() {
		return currentFrame;
	}
}
