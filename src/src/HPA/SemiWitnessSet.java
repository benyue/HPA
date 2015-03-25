package HPA;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SemiWitnessSet {
	int id = -1;
	public int L1_id = -1;//index of set in SWS_Q1

	ArrayList<String> in_symbols = new ArrayList<String>();
	ArrayList<Integer> in_L1SetId = new ArrayList<Integer>();//set of SWS_Q1 indices

	public Set<Integer> super_L0_nodes = new HashSet<Integer>();

	// Stores the L0 nodes adding which to this sws will create a super witness
	// set

	public SemiWitnessSet(int id) {
		this.id = id;
	}


	public SemiWitnessSet(int id, Integer L1_id, Set<Integer> super_L0_nodes) {
		this.id = id;
		this.L1_id = L1_id;
		this.super_L0_nodes = (super_L0_nodes == null) ? this.super_L0_nodes : super_L0_nodes;
	}

	/**
	 * Decide whether two semi witness sets contain same level 1 nodes.
	 * */
	public boolean containsSameL1Nodes(SemiWitnessSet b) {
		return ((int)b.L1_id == (int)this.L1_id);
	}
}
