package HPA;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;

public class HPANode {
	public int id = -1;
	public String name = "";
	public Set<String> prop = new HashSet<String>(); // labeling propositions
														// that are true at this
														// state
	public ArrayList<PATransition> out_transitions = new ArrayList<PATransition>();
	
	//Size of in_symbles = size of in_edges, they correspond to each other
	//an in_edge comes to this node on corresponding in_symble
	ArrayList<String> in_symbles = new ArrayList<String>();
	ArrayList<edge> in_edges = new ArrayList<edge>();
	
	public int level = -1;
	
//	Set<Integer> conflict_nodes = new HashSet<Integer>();
	
	
	public HPANode(int v){
		this.id = v;
	}
	
	/**
	 * Return the set of acceptable input symbols at this node
	 * */
	public Set<String> input(){
		Set<String> input = new HashSet<String>();
		for(PATransition t: this.out_transitions){
			if(t.isEmpty()) break;
			input.add(t.input);
		}
		return input;
	}
	
	/**
	 * Return the set of direct next states on any input
	 * */
	public Set<Integer> post_nodes(){
		Set<Integer> q1 = new HashSet<Integer>();
		for(PATransition t: this.out_transitions){
			if(t.isEmpty()) break;
			for(edge d:t.distribution){
				q1.add(d.node);
			}
		}
		return q1;
	}
	
	/**
	 * Return the set of direct next states on input a
	 * */
	public Set<Integer> post_nodes(String a){
		Set<Integer> q1 = new HashSet<Integer>();
		for(PATransition t: this.out_transitions){
			if(t.isEmpty()) break;
			if(t.input.equals(a)){
				for(edge d:t.distribution){
					q1.add(d.node);
				}
			}
		}
		return q1;
	}
	
	public Set<edge> pre_edges(String a){
		Set<edge> e = new HashSet<edge>();
		for(int i=0; i< this.in_edges.size(); i++){
			if(this.in_symbles.get(i).equals(a)){
				e.add(this.in_edges.get(i));
			}
		}
		return e;
	}
	
	public Set<Integer> pre_nodes(String a){
		Set<Integer> e = new HashSet<Integer>();
		for(int i=0; i< this.in_edges.size(); i++){
			if(this.in_symbles.get(i).equals(a)){
				e.add(this.in_edges.get(i).node);
			}
		}
		return e;
	}
	
	/**
	 * Return the probability distribution on input a
	 * */
	public ArrayList<edge> post_dist(String a){
		for(PATransition t: this.out_transitions){
			if(t.input.equals(a)){
				return t.distribution;
			}
		}
		return null;
	}
	
	/**
	 * Return the probability this state reads input a and goes to state q1
	 * */
	public Fraction post_pr(String a, int q1){
		for(PATransition t: this.out_transitions){
			if(t.input.equals(a)){
				for(edge d:t.distribution){
					if(d.node == q1)
						return d.pr;
				}
			}
		}
		return new Fraction(0);
	}
	
	/**
	 * Return the probability this state reads input a and goes to some state in W.
	 * Worst time O(mn)£¿O(m)
	 * */
	public Fraction post_pr(String a, Set<Integer> W){
		Fraction pr = Fraction.ZERO;
		for(PATransition t: this.out_transitions){
			if(t.input.equals(a)){
				for(edge d:t.distribution){
					if(W.contains(d.node))
						pr = pr.add(d.pr);
				}
			}
		}
		return pr;
	}

	/**
	 * Return the max. probability this state goes to a set of state
	 * */
	public Fraction post_pr(Set<Integer> W) {
		Set<Fraction> pr = new HashSet<Fraction>();
		for(String a:this.input()){
			pr.add(this.post_pr(a, W));
		}
		return Collections.max(pr);
	}

}
