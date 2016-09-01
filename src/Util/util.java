package Util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.fraction.Fraction;

/**
 * The Util.util class provides static utility functions.
 * 
 * @author Cindy
 * */
public class util {

	/**
	 * Return True if setA is a subset of setB
	 * */
	public static <T> boolean isSubset(Set<T> setA, Set<T> setB) {
		return setB.containsAll(setA);
	}

	/**
	 * Return True iff setA and setB have same elements
	 * */
	public static <T> boolean equals(Set<T> setA, Set<T> setB) {
		return (setB.containsAll(setA) && setA.containsAll(setB));
	}

	/**
	 * Calculate the union of setA and setB.
	 * */
	public static <T> Set<T> union(Set<T> setA, Set<T> setB) {
		if (setA == null || setB == null)
			return null;
		Set<T> tmp = new HashSet<T>();
		tmp.addAll(setA);
		tmp.addAll(setB);
		return tmp;
	}

	/**
	 * Return the intersection of setA and setB as a new set.
	 * */
	public static <T> HashSet<T> intersection(Set<T> setA, Set<T> setB) {
		if (setA == null || setB == null)
			return null;
		HashSet<T> tmp = new HashSet<T>();
		for (T x : setA)
			if (setB.contains(x))
				tmp.add(x);
		return tmp;
	}

	/**
	 * Calculate the intersection of ArrayList L1 and ArrayList L2.
	 * Return null if L1 or L2 is null, else at least return an empty list.
	 * */
	public static <T> ArrayList<T> intersection(ArrayList<T> L1, ArrayList<T> L2) {
		if (L1 == null || L2 == null)
			return null;
		ArrayList<T> tmp = new ArrayList<T>();
		for (T x : L1)
			if (L2.contains(x))
				tmp.add(x);
		return tmp;
	}

	/**
	 * Calculate the difference of setA and setB, that is, setA-setB.
	 * */
	public static <T> Set<T> difference(Set<T> setA, Set<T> setB) {
		if (setA == null)
			return null;
		if (setB == null)
			return setA;
		Set<T> tmp = new HashSet<T>(setA);
		tmp.removeAll(setB);
		return tmp;
	}

	/**
	 * Calculate union(setA,setB)-intersection(setA,setB).
	 * */
	public static <T> Set<T> symDifference(Set<T> setA, Set<T> setB) {
		Set<T> tmpA;
		Set<T> tmpB;

		tmpA = union(setA, setB);
		tmpB = intersection(setA, setB);
		return difference(tmpA, tmpB);
	}

	/**
	 * Return the smallest non-negative value of an int array; OR the 1st value
	 * if all negative.
	 */
	public static int minNonNegtive(int[] num) {
		int md = num[0];
		for (int i = 0; i < num.length; i++) {
			if (num[i] < 0)
				continue;
			// num[i]>=0
			if (md < 0 || num[i] < md)
				md = num[i];
		}

		return md;
	}

	/**
	 * Return the indices of an array with the smallest non-negative value.
	 * */
	public static ArrayList<Integer> minNonNeg_idx(int[] nums) {
		ArrayList<Integer> m = new ArrayList<Integer>();
		int md = util.minNonNegtive(nums);
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] == md)
				m.add(i);
		}
		return m;
	}

	/**
	 * Return the max value in an int array. <br>
	 * Time efficiency O(n).
	 * */
	public static int max(int[] num) {
		int md = num[0];
		for (int i = 1; i < num.length; i++) {
			if (num[i] > md)
				md = num[i];
		}
		return md;
	}

	/**
	 * Return the max value in a double array. <br>
	 * Time efficiency O(n).
	 * */
	public static double max(double[] num) {
		double md = num[0];
		for (int i = 1; i < num.length; i++) {
			if (num[i] > md)
				md = num[i];
		}
		return md;
	}

	/**
	 * Return the min value in a double array. <br>
	 * Time efficiency O(n).
	 * */
	public static double min(double[] num) {
		double md = num[0];
		for (int i = 1; i < num.length; i++) {
			if (num[i] < md)
				md = num[i];
		}
		return md;
	}

	/**
	 * Return the max value in a double 2-level array. <br>
	 * Time efficiency O(n^2).
	 * */
	public static double max(double[][] A) {
		double md = A[0][0];
		for (int i = 1; i < A.length; i++) {
			if (max(A[i]) > md)
				md = max(A[i]);
		}
		return md;
	}

	/**
	 * Return a List of the double numbers in a string.
	 * */
	public static List<Double> parseDoubles(String s) {
		s = s.split("//")[0].trim();
		List<Double> nums = new ArrayList<Double>();
		Pattern p = Pattern.compile("[0-9]?\\.?[0-9]*");
		Matcher m = p.matcher(s);
		while (m.find()) {

			double temp = Double.parseDouble(m.group());
			nums.add(temp);
		}
		return nums;
	}

	/**
	 * Return a List of the BigDecimal numbers in a string.
	 * */
	public static List<BigDecimal> parseBigDecimals(String s) {
		s = s.split("//")[0].trim();
		List<BigDecimal> nums = new ArrayList<BigDecimal>();
		Pattern p = Pattern.compile("[0-9]?\\.?[0-9]*");
		Matcher m = p.matcher(s);
		while (m.find()) {
			BigDecimal temp = new BigDecimal(m.group());
			nums.add(temp);
		}
		return nums;
	}

	/**
	 * Return a new List of the non-negative int numbers in a string.
	 * */
	public static List<Integer> parsePosIntegers2list(String s) {
		s = s.split("//")[0].trim();
		List<Integer> nums = new ArrayList<Integer>();
		Pattern p = Pattern.compile("[0-9]+");// "-?[0-9]+"
		Matcher m = p.matcher(s);
		while (m.find()) {
			int temp = Integer.parseInt(m.group());
			nums.add(temp);
		}
		return nums;
	}
	
	/**
	 * Return a new set of the non-negative int numbers in a string.
	 * */
	public static Set<Integer> parsePosIntegers2set(String s) {
		s = s.split("//")[0].trim();
		Set<Integer> nums = new HashSet<Integer>();
		Pattern p = Pattern.compile("[0-9]+");// "-?[0-9]+"
		Matcher m = p.matcher(s);
		while (m.find()) {
			int temp = Integer.parseInt(m.group());
			nums.add(temp);
		}
		return nums;
	}

	/**
	 * Change all "a" values in an int array to "b".
	 * */
	public static int[] changeSpecificValues(int[] nums, int a, int b) {
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] == a)
				nums[i] = b;
		}
		return nums;
	}

	/**
	 * Rename all strings in an ArrayList, add prefix and surfix.
	 * */
	public static ArrayList<String> rename(String prefix, String surfix,
			ArrayList<String> S) {
		ArrayList<String> S1 = new ArrayList<String>();
		for (int i = 0; i < S.size(); i++) {
			S1.add(prefix + S.get(i) + surfix);
		}
		return S1;
	}

	/**
	 * Obtain the probability value as a fraction from a string.<br>
	 * Legal input: integer, decimal, or fraction defined as "a/b".
	 * 
	 * @return a valid fraction, or NULL for invalid input.
	 * */
	public static Fraction parseFractionPr(String s){
		if (s.isEmpty() | s.equals(null))
			return null;
		s = s.replaceAll(" ", "");// remove white spaces
		// obtain probability
		Fraction pr = Fraction.ZERO;
		if (s.contains("/") && s.contains(".")) {//double/double
			if (s.split("/").length != 2)
				return null;
			try{
				double nom = Double.parseDouble(s.split("/")[0]);
				double den = Double.parseDouble(s.split("/")[1]);
				pr = new Fraction(nom/den);
			}catch (Exception e) {
				return null;
			}
			
		} else if (s.contains(".")) { // decimal
			// pr = new Fraction (Double.parseDouble(s));//precision concern
			String[] snew = s.split("\\.");
			if (snew.length != 2) // e.g.0.1.01.01
				return null;
			try {
				int pr1 = Integer.parseInt(snew[0]);// den
				int den = Integer.parseInt(snew[1]);// nom
				int nom = 1;
				for (int i = 0; i < String.valueOf(snew[1]).length(); i++) {
					nom = nom * 10;
				}
				Fraction prd = new Fraction(den, nom);
				pr = new Fraction(pr1).add(prd);
			} catch (Exception e) {
				pr = new Fraction(Double.parseDouble(s));
			}
		} else if (s.contains("/")) { // fraction
			if (s.split("/").length != 2)
				return null;
			int nom = Integer.parseInt(s.split("/")[0]);
			int den = Integer.parseInt(s.split("/")[1]);
			pr = new Fraction(nom, den);
		} else { // int, or invalid
			try {
				pr = new Fraction(Integer.parseInt(s));
			} catch (Exception e) {
				return null;
			}
		}

		if (pr.compareTo(Fraction.ZERO) == -1 // pr<0
				| pr.compareTo(Fraction.ONE) == 1)// pr>1
			return null;

		return pr;
	}

	/**
	 * Get CPU time in nanoseconds.
	 * */
	public static long getCPUTime() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported() ? bean
				.getCurrentThreadCpuTime() : 0L;
	}

	/**
	 * Get user time in nanoseconds.
	 * */
	public static long getUserTime() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported() ? bean
				.getCurrentThreadUserTime() : 0L;
	}

	/**
	 * Get system time in nanoseconds.
	 * */
	public static long getSystemTime() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported() ? (bean
				.getCurrentThreadCpuTime() - bean.getCurrentThreadUserTime())
				: 0L;
	}

	/**
	 * Count the number of unique elements in an ArrayList
	 * */
	public static int countUniqueElements(ArrayList<String> failedInputs) {
		Set<Object> B = new HashSet<Object>();
		B.addAll(failedInputs);
		return B.size();
	}

	/**
	 * Count the number of specific value in array
	 * */
	public static int count(boolean[] A, boolean a) {
		int count = 0;
		for(boolean v:A){
			if(v==a)
				count++;
		}
		return count;
	}
}
