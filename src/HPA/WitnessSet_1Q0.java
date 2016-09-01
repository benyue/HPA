package HPA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Cindy Yue Ben
 * 
 *         Witness Set with exactly 1 Q0 state. WitnessSet_1Q0 is one
 *         WitnessSet_0Q0 plus one Q0 state.
 */
public class WitnessSet_1Q0 extends WitnessSet {
	public Integer q0 = -1; // the only node in the witness set on level 0
	public int WS0id = -1; // WitnessSet_0Q0 sws; on Q1

	// a => WS1 a_successor for BKD (index in AS)
	public HashMap<String, Integer> ASidx =
	        new HashMap<String, Integer>();
	public ArrayList<LinkedList<Integer>> AS =
	        new ArrayList<LinkedList<Integer>>();

	/**
	 * @param id
	 * @param q0
	 *            is the only L0-node of this Witness Set
	 * @param sws_id
	 * @param bool
	 *            is true iff this WS is part of a super good witness set
	 */
	public WitnessSet_1Q0(int id, int q0, int WS0id, boolean bool) {
		this.id = id;
		this.q0 = q0;
		this.WS0id = WS0id;
		isSuperGoodWS1 = bool;
	}

	public WitnessSet_1Q0(int id, int q0) {
		this.id = id;
		this.q0 = q0;
	}

	public WitnessSet_1Q0(int id) {
		this.id = id;
	}
}
