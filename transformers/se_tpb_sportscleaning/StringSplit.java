package se_tpb_sportscleaning;

import java.awt.Point;

public class StringSplit extends Point {
    boolean isRegexMatch;

	public boolean isRegexMatch() {
		return isRegexMatch;
	}

	public void setRegexMatch(boolean isRegexMatch) {
		this.isRegexMatch = isRegexMatch;
	}

	/**
	 * @param x
	 * @param y
	 * @param isRegexMatch
	 */
	public StringSplit(int start, int end, boolean isRegexMatch) {
		super(start, end);
		this.isRegexMatch = isRegexMatch;
	}
 
	
	public String toString() {
		String s = "[" +x +","+y+","+isRegexMatch+"]";
		return s;
	}
}

