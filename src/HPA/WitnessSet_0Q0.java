package HPA;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
/**
 * @author Cindy Yue Ben
 * Witness Set with no Q0 state.
 * */
public class WitnessSet_0Q0 extends WitnessSet{
	public HashMap<String, Integer> inWS0 = new HashMap<String, Integer>();
	/**Add a node from superQ0 or good Q0 to a good WS0 will create a WS*/
	public Set<Integer> superQ0 = new HashSet<Integer>();//W is super good
	public Set<Integer> goodQ0 = new HashSet<Integer>();//W on Q1 is super good

	/**
	 * WitnessSet_0Q0 constructor. 
	 * @param id: unique id in PA.WS0s
	 * @param super_L0_nodes: super level 0 nodes
	 * */
	public WitnessSet_0Q0(int id) {
		this.id = id;
	}
	
	public WitnessSet_0Q0(int id,Set<Integer> super_L0_nodes) {
		this.id = id;
		if(super_L0_nodes != null && !super_L0_nodes.isEmpty()) {
			this.superQ0 = super_L0_nodes;
		}
	}
}
