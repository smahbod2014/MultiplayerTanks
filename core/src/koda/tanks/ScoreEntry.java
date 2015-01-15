package koda.tanks;


public class ScoreEntry implements Comparable<ScoreEntry> {

	public static final int CHARS = 10;
	
	public String name;
	public int score;
	
	public ScoreEntry(String name, int score) {
		this.name = name;
		this.score = score;
	}
	
	public ScoreEntry() {}
	
	private String format() {
		String s = name;
		for (int i = 0; i < CHARS - name.length(); i++) {
			s += " ";
		}
		s += score;
		return s;
	}
	
	@Override
	public String toString() {
		return format();
	}

	@Override
	public int compareTo(ScoreEntry s) {
		return s.score - score;
	}
}
