package Util;

import edu.princeton.cs.introcs.In;

/**
 * The Util.io class provides static IO functions, used for outputting automata
 * to console or files.
 * 
 * @author Cindy Yue Ben
 * */
public class io {

	/**
	 * Read next valid line from input stream, excluding empty line, null, and
	 * comments after "//".
	 * 
	 * @return null if no valid non-empty line.
	 * */
	public static String readNextNonemptyLine(In in) {
		if (!in.hasNextLine())
			return null;
		String s0 = in.readLine();
		String s = s0.split("//", 2)[0].trim();
		while (s == null || s.equals("") || s.replaceAll(" ", "").isEmpty()) {
			if (in.hasNextLine()) {
				s0 = in.readLine();
				s = s0.split("//", 2)[0].trim();
			} else {
				return null;
			}
		}
		return s;
	}

}
