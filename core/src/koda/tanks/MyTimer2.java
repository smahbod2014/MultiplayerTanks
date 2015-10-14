package koda.tanks;

public class MyTimer2 {

	public long duration;
	public long current;
	public long elapsed;
	public int count;
	public boolean checked;
	public boolean justStarted;
	public boolean ready;
	
	public MyTimer2(long duration) {
		this.duration = duration;
	}
	
	public void resetWithDelay() {
		current = System.currentTimeMillis();
		checked = false;
		justStarted = true;
	}
	
	public void resetWithoutDelay() {
		resetWithDelay();
		ready = true;
	}
	
	public boolean finished() {
		long now = System.currentTimeMillis();
		if (ready || now >= current + duration) {
			elapsed = now - current;
			ready = false;
			return true;
		}
		return false;
//		return System.currentTimeMillis() >= current + duration;
	}
	
	public boolean justFinished() {
		if (!checked && finished()) {
			checked = true;
			return true;
		}

		return false;
	}
	
	public boolean poll() {
		if (justFinished()) {
			count++;
			return true;
		}
		
		return false;
	}
	
	public boolean justStarted() {
		if (justStarted) {
			justStarted = false;
			return true;
		}
		
		return false;
	}
}
