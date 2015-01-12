package koda.tanks;

public class MyTimer {

	public float accum;
	public float max;
	public int count;
	public boolean ready;
	public boolean asAccum;
	
	public MyTimer(float max) {
		this.max = max;
		ready = false;
	}
	
	public void half() {
		accum = max / 2;
	}
	
	public float progress() {
		return accum / max;
	}
	
	public void asAccumulator() {
		asAccum = true;
	}
	
	public void asTimer() {
		asAccum = false;
	}
	
	public void update(float dt) {
		accum += dt;
		if (!asAccum && accum >= max) {
			accum = max;
			ready = true;
		}
	}
	
	public boolean poll() {
		if (!ready)
			return false;
		
		accum = 0;
		ready = false;
		count++;
		return true;
	}
}