package HPA;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;

public class PATransition {
	public String input = "";
	public ArrayList<edge> distribution = new ArrayList<edge>();

	public PATransition(String input2, ArrayList<edge> distribution)
			throws Exception {
		if(input2.isEmpty() ||
				distribution.isEmpty() || distribution == null){
			return;
		}
		Fraction sum = new Fraction(0);
		Set<Integer> q_next = new HashSet<Integer>();
		for (edge d : distribution) {
			sum =sum.add(d.pr);
			if(q_next.contains(d.node)){
				throw new Exception("ERROR: redundant edges discovered.");
			}else{
				q_next.add(d.node);
			}
		}
		if (sum.compareTo(new Fraction(1)) != 0)
			throw new Exception(
					"ERROR: sum of probabilities in a transition distribution should be exactly 1.0");

		this.input = input2;
		this.distribution = distribution;
	}
	
	public boolean isEmpty(){
		return this.distribution.isEmpty();
	}
	
	
	public Set<Integer> end_nodes(){
		Set<Integer> ends = new HashSet<Integer>();
		for(edge d:this.distribution){
			if(d.node != -1) ends.add(d.node);
		}
		return ends;
	}
	
	public boolean isConflictEnds(int a, int b){
		Set<Integer> ends = this.end_nodes();
		if(ends.contains(a) && ends.contains(b)) return true;
		return false;
	}

	public static void main(String[] args) {

	}

}
