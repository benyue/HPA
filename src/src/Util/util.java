package Util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.fraction.Fraction;

public class util {

	/**
	 * Decide if a given set S1 is subset of S2
	 * */
	public static boolean isSubsetOf(Set<Integer> S1, Set<Integer> S2) {
		return S2.containsAll(S1);
	}

	public static <T> Set<T> union(Set<T> setA, Set<T> setB) {
		if(setA==null || setB==null) return null;
		Set<T> tmp = new TreeSet<T>(setA);
		tmp.addAll(setB);
		return tmp;
	}

	public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
		if(setA==null || setB==null) return null;
		Set<T> tmp = new TreeSet<T>();
		for (T x : setA)
			if (setB.contains(x))
				tmp.add(x);
		return tmp;
	}

	public static <T> ArrayList<T> intersection(
			ArrayList<T> L1,
			ArrayList<T> L2) {
		if(L1==null || L2==null) return null;
		ArrayList<T> tmp = new ArrayList<T>();
		for (T x : L1)
			if (L2.contains(x))
				tmp.add(x);
		return tmp;
	}

	public static <T> Set<T> difference(Set<T> setA, Set<T> setB) {
		if(setA==null) return null;
		if(setB==null) return setA;
		Set<T> tmp = new TreeSet<T>(setA);
		tmp.removeAll(setB);
		return tmp;
	}

	public static <T> Set<T> symDifference(Set<T> setA, Set<T> setB) {
		Set<T> tmpA;
		Set<T> tmpB;

		tmpA = union(setA, setB);
		tmpB = intersection(setA, setB);
		return difference(tmpA, tmpB);
	}

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
	 * Return the smallest non-negative value of an int array; OR the 1st value
	 * if all negative
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
	 * Return the indices of an array with the smallest non-negative value
	 * 
	 * @param nums
	 *            has all non-negative values
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

	public static int max(int[] num) {
		int md = num[0];
		for (int i = 1; i < num.length; i++) {
			if (num[i] > md)
				md = num[i];
		}
		return md;
	}

	public static double max(double[] num) {
		double md = num[0];
		for (int i = 1; i < num.length; i++) {
			if (num[i] > md)
				md = num[i];
		}
		return md;
	}

	public static double min(double[] num) {
		double md = num[0];
		for (int i = 1; i < num.length; i++) {
			if (num[i] < md)
				md = num[i];
		}
		return md;
	}

	public static double max(double[][] A) {
		double md = A[0][0];
		for (int i = 1; i < A.length; i++) {
			if (max(A[i]) > md)
				md = max(A[i]);
		}
		return md;
	}

	/** Read double numbers from a string */
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

	/** Read BigDecimal numbers from a string */
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

	/** Read non-negative int numbers from a string */
	public static List<Integer> parsePosIntegers(String s) {
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
	 * Change all a values in nums to b
	 * */
	public static int[] changeSpecificValues(int[] nums, int a, int b) {
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] == a)
				nums[i] = b;
		}
		return nums;
	}

	public static void main(String[] args) {

	}

	public static ArrayList<String> rename(String prefix, String surfix,
			ArrayList<String> S) {
		ArrayList<String> S1 = new ArrayList<String>();
		for (int i = 0; i < S.size(); i++) {
			S1.add(prefix + S.get(i) + surfix);
		}
		return S1;
	}

	/**
	 * Obtain the probability value as a fraction from a string. Allowed input:
	 * integer, decimal, or fraction defined as "a/b". Verify the probability
	 * value at the end, i.e. 0<=pr<=1. Return NULL for invalid probability if
	 * pr<0 or pr>1, or invalid input string such as "0.01.01".
	 * 
	 * */
	public static Fraction parseFractionPr(String s) {
		if (s.isEmpty() | s.equals(null))
			return null;

		s = s.replaceAll(" ", "");// remove white spaces
		// obtain probability
		Fraction pr = Fraction.ZERO;
		if (s.contains(".")) { // decimal
			// pr = new Fraction (Double.parseDouble(s));//precision concern
			String[] snew = s.split("\\.");
			if(snew.length != 2) return null;
			int pr1 = Integer.parseInt(snew[0]);//den
			int den = Integer.parseInt(snew[1]);//nom
			int nom = 1;
			for (int i = 0; i < String.valueOf(snew[1]).length(); i++) {
				nom = nom * 10;
			}
			Fraction prd = new Fraction(den, nom);
			pr = new Fraction(pr1).add(prd);
		} else if (s.contains("/")) { // fraction
			int nom = Integer.parseInt(s.split("/")[0]);
			int den = 1;
			if (s.split("/").length > 1) {
				den = Integer.parseInt(s.split("/")[1]);
			}
			pr = new Fraction(nom, den);
		} else { // int, or invalid
			try{
				pr = new Fraction(Integer.parseInt(s));
			}catch (Exception e){
				return null;
			}
			
		}

		if (pr.compareTo(Fraction.ZERO) == -1 // pr<0
				| pr.compareTo(Fraction.ONE) == 1)// pr>1
			return null;

		return pr;
	}

	/** Get CPU time in nanoseconds. */
	public static long getCPUTime() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported() ? bean
				.getCurrentThreadCpuTime() : 0L;
	}

	/** Get user time in nanoseconds. */
	public static long getUserTime() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported() ? bean
				.getCurrentThreadUserTime() : 0L;
	}

	/** Get system time in nanoseconds. */
	public static long getSystemTime() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported() ? (bean
				.getCurrentThreadCpuTime() - bean.getCurrentThreadUserTime())
				: 0L;
	}
	
	/**
	 * Count the number of unique elements in an ArrayList
	 * */
	public static int countUniqueElements(ArrayList<String> failedInputs){
		Set<Object> B = new HashSet<Object>();
		B.addAll(failedInputs);
		return B.size();
	}
}
