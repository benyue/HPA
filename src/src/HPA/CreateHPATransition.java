package HPA;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;

public class CreateHPATransition {
	public int id = -1;
	public int sourceID = -1;
	public String input = "";
	public int endID = -1;
	public Fraction pr = Fraction.ONE;
	public Set<String> propRequired = new HashSet<String>();//required props to trigger the transition, defined in graph file
	public Set<String> propSatisfied = new HashSet<String>();

	public CreateHPATransition(int id, int source, String input, int end, Fraction p)
			throws Exception {
		if(id<0|source<0|end<0 | input.isEmpty()
				|p.compareTo(Fraction.ONE)>0 | p.compareTo(Fraction.ZERO)<0){
			return;
		}
		this.id = id;
		this.sourceID = source;
		this.input = input;
		this.endID = end;
		this.pr = p;
	}
	
	/**
	 * Initialize a transition by deep copying an existing transition
	 * */
	public CreateHPATransition (CreateHPATransition t){
		this.id = t.id;
		this.sourceID = t.sourceID;
		this.input = t.input;
		this.endID = t.endID;
		this.pr = t.pr;
		this.propRequired.clear();
		this.propRequired.addAll(t.propRequired);
		this.propSatisfied.clear();
		this.propSatisfied.addAll(t.propSatisfied);
	}
	
	/**
	 * specify id, source, input, end; while pr equals default value
	 * */
	public CreateHPATransition(int id, int source, String input, int end)
			throws Exception {
		if(id<0|source<0|end<0 | input.isEmpty()){
			return;
		}
		this.id = id;
		this.sourceID = source;
		this.input = input;
		this.endID = end;
	}
	
	public CreateHPATransition() {
	}

	/**
	 * Decide whether this transition requires a specific proposition to be true
	 * */
	public boolean isPropRequired(String p){
		if(this.propRequired.contains(p)) return true;
		return false;
	}
	
	/**
	 * Decide whether this transition satisfies a specific proposition
	 * */
	public boolean isPropSatisfied(String p){
		if(this.propSatisfied.contains(p)) return true;
		return false;
	}
	
	/**
	 * Add a proposition to the transition.
	 * If the prop is already defined, return false; else, return true.
	 * */
	public boolean addSatisfiedProp(String p) {
		if (this.propSatisfied.contains(p)){
			return false;
		}
		this.propSatisfied.add(p);
		return true;
	}
	
	/**
	 * Remove a proposition to the transition.
	 * If the prop is not defined, return false; else, return true.
	 * */
	public boolean removeSatisfiedProp(String p) {
		if (!this.propSatisfied.contains(p)){
			return false;
		}
		this.propSatisfied.remove(p);
		return true;
	}
	/**
	 * Replace a prop p with another, which is pnew.
	 * if pnew already exists, do nothing and return false;
	 * if p doesn't exist, do nothing and return false;
	 * if p exists and pnew doesn't exist, replace p with pnew, return true.
	 * */
	public boolean replaceProp(String p, String pnew){
		if(this.propSatisfied.contains(pnew)){
			return false;
		}
		if(this.removeSatisfiedProp(p)){
			this.propSatisfied.add(pnew);
			return true;
		}else{
			return false;
		}
	}


	public static void main(String[] args) {

	}

}
