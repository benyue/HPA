package HPA;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
/**
 * Transition definition: a state maintains one PATransition for each input.
 * 
 * @author Cindy Yue Ben
 */
public class T {
	public String input = "";
	public ArrayList<edge> dist = new ArrayList<edge>();

	public T(String input, ArrayList<edge> distribution)
	        throws Exception {
		if (input.isEmpty() ||
		        distribution.isEmpty() || distribution == null) {
			return;
		}
		// Fraction sum = new Fraction(0);
		Set<Integer> q_next = new HashSet<Integer>();
		for (edge d : distribution) {
			// sum = sum.add(d.pr); //0<sum<=1
			if (q_next.contains(d.node)) {
				throw new Exception("ERROR: redundant edges discovered.");
			} else {
				q_next.add(d.node);
			}
		}
		/*
		 * if (sum.compareTo(new Fraction(1)) != 0)//checked on higher level
		 * throw new Exception(
		 * "ERROR: sum of probabilities in a transition distribution"
		 * + " should be exactly 1.0");
		 */

		this.input = input;
		this.dist = distribution;
	}

	public boolean isEmpty() {
		return this.dist.isEmpty();
	}

	/**
	 * Return a new set containing all end nodes.
	 */
	public ArrayList<Integer> end_nodes() {
		ArrayList<Integer> nodes = new ArrayList<Integer>(this.dist.size());
		for (edge d : this.dist) {
			if (d.node != -1)
				nodes.add(d.node);
		}
		return nodes;
	}
}
