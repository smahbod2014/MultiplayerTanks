package koda.tanks;

public class MyTimer2 {

	public long duration;
	public long current;
	public long elapsed;
	public int count;
	public boolean checked;
	public boolean justStarted;
	
	public MyTimer2(long duration) {
		this.duration = duration;
	}
	
	public void reset() {
		current = System.currentTimeMillis();
		checked = false;
		justStarted = true;
	}
	
	public boolean finished() {
		long now = System.currentTimeMillis();
		if (now >= current + duration) {
			elapsed = now - current;
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
	
//	public int poll() {
//		if (finished()) {
//			reset();
//			count++;
//			return count;
//		}
//		
//		return -1;
//	}
}
