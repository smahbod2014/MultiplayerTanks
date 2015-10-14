package koda.tanks;

import java.util.HashMap;

public class TimerSystem {

	private HashMap<String, MyTimer2> timers = new HashMap<String, MyTimer2>();
	private String lastTimer;
	
	public void setLast(String name) {
		this.lastTimer = name;
	}
	
	public void addTimer(String name, long duration) {
		MyTimer2 t = new MyTimer2(duration);
		timers.put(name, t);
	}
	
	public void resetWithDelay(String name) {
		lastTimer = name;
		timers.get(name).resetWithDelay();
	}
	
	public void resetWithDelay() {
		resetWithDelay(lastTimer);
	}
	
	public void resetWithoutDelay(String name) {
		lastTimer = name;
		timers.get(name).resetWithoutDelay();
	}
	
	public void resetWithoutDelay() {
		resetWithoutDelay(lastTimer);
	}
	
	public boolean finished(String name) {
		lastTimer = name;
		return timers.get(name).finished();
	}
	
	public boolean finished() {
		return finished(lastTimer);
	}
	
	public boolean justFinished(String name) {
		lastTimer = name;
		return timers.get(name).justFinished();
	}
	
	public boolean justFinished() {
		return justFinished(lastTimer);
	}
	
	public boolean poll(String name) {
		lastTimer = name;
		return timers.get(name).poll();
	}
	
	public boolean poll() {
		return poll(lastTimer);
	}
	
	public long elapsed(String name) {
		lastTimer = name;
		return timers.get(name).elapsed;
	}
	
	public long elapsed() {
		return elapsed(lastTimer);
	}
	
	public int count(String name) {
		lastTimer = name;
		return timers.get(name).count;
	}
	
	public int count() {
		return count(lastTimer);
	}
	
	public void setCount(String name, int count) {
		lastTimer = name;
		timers.get(name).count = count;
	}
	
	public void setCount(int count) {
		setCount(lastTimer, count);
	}
	
	public boolean justStarted(String name) {
		lastTimer = name;
		return timers.get(name).justStarted();
	}
	
	public boolean justStarted() {
		return justStarted(lastTimer);
	}
}
