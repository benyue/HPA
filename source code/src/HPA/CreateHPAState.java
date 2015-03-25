package HPA;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CreateHPAState {
	public int id = -1; // as id
	public String name = ""; // unique, no ",", no "-", no ";", no "#" in a state
						// value in the data file
	public ArrayList<Integer> outTransID = new ArrayList<Integer>();
	public Set<String> inputs = new HashSet<String>();
	public Set<String> prop = new HashSet<String>(); //treated as pre-satisfied

	public CreateHPAState(int i, String v) {
		this.id = i;
		this.name = v;
	}

	public CreateHPAState(int id2, String value2, ArrayList<Integer> ot,
			HashSet<String> i) {
		this.id = id2;
		this.name = value2;
		this.outTransID = ot;
		this.inputs = i;
	}

	/**
	 * Return whether a specific proposition is defined on this state 
	 * */
	public boolean isPropDefined(String p) {
		if (this.prop.contains(p))
			return true;
		return false;
	}

	/**
	 * Add a proposition to the state.
	 * If the prop is already defined on this state, return false; else, return true.
	 * */
	public boolean addProp(String p) {
		if (this.prop.contains(p))
			return false;
		this.prop.add(p);
		return true;
	}
	
	public boolean removeProp(String p){
		if(this.prop.contains(p)){
			this.prop.remove(p);
			return true;
		}
		return false;
	}
	
	public boolean replaceProp(String p, String pnew){
		if(this.prop.contains(p)){
			this.prop.remove(p);
			this.prop.add(pnew);
			return true;
		}
		return false;
	}

}
