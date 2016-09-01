package HPA;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;
/**
 * @author Cindy Yue Ben
 */
public class V {
	public String name = "";
	/** prop stores labeling propositions that are true at this state */
	public Set<String> prop = new HashSet<String>();
	public ArrayList<T> outT = new ArrayList<T>();

	// |in_symbles| = |in_edges|, they correspond to each other
	// an in_edge comes to this node on corresponding in_symble
	// for use in FWD alg.
	// Note there can be multiple edges coming on the same input
	ArrayList<String> in_symbols = new ArrayList<String>();
	ArrayList<edge> in_edges = new ArrayList<edge>();

	public int level = -1;

	public V() {
	}

	public V(String name) {
		if (name != null)
			this.name = name;
	}

	/**
	 * Return the set of acceptable input symbols at this node
	 */
	Set<String> input() {
		Set<String> input = new HashSet<String>();
		for (T t : outT) {
			if (t.isEmpty())
				break;
			input.add(t.input);
		}
		return input;
	}

	/**
	 * Return the set of direct next states on any input
	 */
	Set<Integer> post_nodes() {
		Set<Integer> q1 = new HashSet<Integer>();
		for (T t : outT) {
			if (t.isEmpty())
				break;
			for (edge d : t.dist) {
				q1.add(d.node);
			}
		}
		return q1;
	}

	/**
	 * Return the set of direct next states from current state on input a which
	 * are also in set S.
	 */
	Set<Integer> post_nodes(String a, Set<Integer> S) {
		Set<Integer> q1 = new HashSet<Integer>();
		for (T t : outT) {
			if (!t.input.equals(a))
				continue;
			for (edge d : t.dist) {
				if (S.contains(d.node))
					q1.add(d.node);
			}
		}
		return q1;
	}

	/**
	 * Return the set of direct next states on input a
	 */
	Set<Integer> post_nodes(String a) {
		Set<Integer> q1 = new HashSet<Integer>();
		for (T t : outT) {
			if (t.input.equals(a)) {
				for (edge d : t.dist) {
					q1.add(d.node);
				}
			}
		}
		return q1;
	}

	Set<edge> pre_edges(String a) {
		Set<edge> e = new HashSet<edge>();
		for (int i = 0; i < in_edges.size(); i++) {
			if (in_symbols.get(i).equals(a)) {
				e.add(in_edges.get(i));
			}
		}
		return e;
	}

	/**
	 * Return the probability distribution on input a
	 */
	ArrayList<edge> post_dist(String a) {
		for (T t : outT) {
			if (t.input.equals(a)) {
				return t.dist;
			}
		}
		return null;
	}

	/**
	 * Return the probability this state reads input a and goes to state q1
	 */
	Fraction post_pr(String a, int q1) {
		for (T t : outT) {
			if (t.input.equals(a)) {
				for (edge d : t.dist) {
					if (d.node == q1)
						return d.pr;
				}
			}
		}
		return Fraction.ZERO;
	}

	/**
	 * Return the probability this state reads input a and goes to some state in
	 * W.
	 */
	Fraction post_pr(String a, Set<Integer> W) {
		if (a == null || a.isEmpty() || W == null || W.isEmpty())
			return Fraction.ZERO;
		Fraction pr = Fraction.ZERO;
		for (T t : outT) {
			if (t.input.equals(a)) {
				for (edge d : t.dist) {
					if (W.contains(d.node))
						pr = pr.add(d.pr);
				}
			}
		}
		return pr;
	}

	/**
	 * Return the max. probability this state goes to a set of state
	 */
	Fraction post_pr(Set<Integer> W) {
		if (W == null || W.isEmpty())
			return Fraction.ZERO;
		Set<Fraction> pr = new HashSet<Fraction>();
		for (String a : input()) {
			pr.add(post_pr(a, W));
		}
		return Collections.max(pr);
	}

}
