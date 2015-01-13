package koda.tanks;


public class ScoreEntry {

	public static final int CHARS = 10;
	String formatted;
	public String name;
	public int score;
	
	public ScoreEntry(String name, int score) {
		this.name = name;
		this.score = score;
	}
	
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
}
