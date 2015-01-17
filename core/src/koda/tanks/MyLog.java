package koda.tanks;

import java.util.ArrayList;

import com.esotericsoftware.minlog.Log;

public class MyLog {

	private static ArrayList<Integer> log = new ArrayList<Integer>();
	
	public static void logOnce(int index, String message) {
		if (!log.contains(index)) {
			Log.info(message);
			log.add(index);
		}
	}
}
