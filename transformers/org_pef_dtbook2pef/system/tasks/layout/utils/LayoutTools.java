package org_pef_dtbook2pef.system.tasks.layout.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;


public class LayoutTools {

	public enum DistributeMode {
		EQUAL_SPACING,
		UNISIZE_TABLE_CELL};
	
	public static int length(String str) {
		return str.codePointCount(0, str.length());
	}

	public static String fill(char c, int amount) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<amount; i++) {
			sb.append(c);
		}
		return sb.toString();
	}
	
	public static String fill(String s, int amount) {
		if (amount<1) {
			return "";
		}
		if (s.length()==0) {
			throw new IllegalArgumentException("Cannot fill using an empty string.");
		}
		StringBuilder sb = new StringBuilder();
		while (sb.codePointCount(0, sb.length())<amount) {
			sb.append(s);
		}
		return sb.subSequence(0, amount).toString();
	}

	private static String distributeEqualSpacing(ArrayList<String> units, int width, String padding) {
		if (units.size()==1) {
			return units.get(0);
		}
		int chunksLength = 0;
		for (String s : units) {
			chunksLength += s.codePointCount(0, s.length());
		}
		int totalSpace = (width-chunksLength);
		int parts = units.size()-1;
		double target = totalSpace/(double)parts;
		int used = 0;
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<units.size(); i++) {
			if (i>0) {
				int spacing = (int)Math.round(i * target) - used;
				used += spacing;
				sb.append(fill(padding, spacing));
			}
			sb.append(units.get(i));
		}
		assert sb.length()==width;
		return sb.toString();
	}
	
	private static String distributeTable(ArrayList<String> units, int width, String padding) throws LayoutToolsException {
		double target = width/(double)units.size();
		StringBuffer sb = new StringBuffer();
		int used = 0;
		for (int i=0; i<units.size(); i++) {
			int spacing = (int)Math.round((i+1) * target) - used;
			String cell = units.get(i);
			used += spacing;
			spacing -= cell.codePointCount(0, cell.length());
			if (spacing<0) {
				throw new LayoutToolsException("Text does not fit within cell: " + cell);
			}
			sb.append(cell);
			if (i<units.size()-1) {
				sb.append(fill(padding, spacing));
			}
		}
		return sb.toString();
	}
	
	/**
	 * Distribute <tt>units</tt> of text over <tt>width</tt> chars, separated by <tt>padding</tt> pattern
	 * using distribution mode <tt>mode</tt>.
	 * @param units the units of text to distribute
	 * @param width the width of the resulting string
	 * @param padding the padding pattern to use as separator
	 * @param mode the distribution mode to use
	 * @return returns a string of <tt>width</tt> chars 
	 */
	public static String distribute(ArrayList<String> units, int width, String padding, DistributeMode mode) throws LayoutToolsException {
		switch (mode) {
			case EQUAL_SPACING:
				return distributeEqualSpacing(units, width, padding);
			case UNISIZE_TABLE_CELL:
				return distributeTable(units, width, padding);
		}
		// Cannot happen
		return null;
	}
	
	public static String distribute(Collection<TabStopString> units) {
		TreeSet<TabStopString> sortedUnits = new TreeSet<TabStopString>();
		sortedUnits.addAll(units);
		StringBuffer sb = new StringBuffer();
		int used = 0;
		for (TabStopString t : sortedUnits) {
			used = sb.codePointCount(0, sb.length());
			if (used > t.getPosition()) {
				throw new RuntimeException("Cannot layout cell.");
			}
			int amount = t.getPosition()-used;
			switch (t.getAlignment()) {
				case LEFT:
					//ok
					break;
				case CENTER:
					amount -= t.length() / 2;
					break;
				case RIGHT:
					amount -= t.length();
					break;
			}
			sb.append(fill(t.getPattern(), amount));
			sb.append(t.getText());
		}
		return sb.toString();
	}

}