package HPA;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.commons.math3.fraction.Fraction;

import Util.util;

public class WitnessSet {
	int id = -1;
	int q0 = -1; // the only node in the witness set on level 0
	int sws_id = -1; // SemiWitnessSet sws;
	public boolean isSuperGoodWS = false;

	// for forward algorithm
	Hashtable<String, Fraction> qa1 = new Hashtable<String, Fraction>();
	// a#q => Fraction 1 to be used in val formula
	// (val - qa1) / qa0
	Hashtable<String, Integer> Waq = new Hashtable<String, Integer>();
	// a#q => index of Waq, unique for fixed a and q

	// for forward algorithm
	public Hashtable<Long, Vali_Probk> VALI = new Hashtable<Long, Vali_Probk>();// i
																				// starts
																				// from
																				// 0
	// at each i, a unique key WS decides the Val value, either itself or some
	// Waq at some a;
	// could be more than one Waq giving the same Val.
	// i => pr=Val_value; k=-1;
	// symbols={a,b,...}; VALpreWSs_PROBKsuccessors={Waq,Wbq,...}
	// symbols and VALpreWSs_PROBKsuccessors correspond to each other

	// for backward algorithm
	Hashtable<String, HashSet<Integer>> a_successor = new Hashtable<String, HashSet<Integer>>();

	// a => set of a_successor

	/**
	 * @param id
	 * @param q0
	 *            is the only L0-node of this Witness Set
	 * @param sws_id
	 * @param b
	 *            is true iff this WS is part of a super good witness set
	 * */
	public WitnessSet(int id, int q0, int sws_id, boolean bool) {
		this.id = id;
		this.q0 = q0;
		this.sws_id = sws_id;
		this.isSuperGoodWS = bool;
	}

	public WitnessSet(int q0) {
		this.q0 = q0;
	}

	public WitnessSet() {
	}

	public static void main(String[] args) {
	}

	/**
	 * Compare Vali_i with each Vali_j and j<i, if for any j, the predecessor
	 * that decides Val repeats, stop. Return the repeated WS_VAL(s), with last
	 * element of Alpha_1, i.e. j, when for the first time this WS_VAL_i =
	 * WS_VAL_j;
	 * */
	public ArrayList<Integer> repeat_WS_VAL(long i) {
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		for (int j = 0; j < i; j++) {
			try {
				Vali_Probk vp = this.VALI.get((long)j);
				ArrayList<Integer> preWSs = vp.VALpreWSs_PROBKsuccessors;
				ArrayList<String> preSymbols = vp.symbols;
				for (int k = 0; k < preWSs.size(); k++) {
					Integer prews = preWSs.get(k);
					if (preWSs.contains(prews)
							& !preSymbols.get(k).isEmpty()
							& this.VALI.get((long)j).pr.compareTo(this.VALI.get((long)i).pr)>0) {
						tmp.add(prews);
					}
				}
				if(!tmp.isEmpty()){
					tmp.add(j);
					return tmp;
				}
			} catch (java.lang.NullPointerException e) {
				return null;
			}
		}
		return null;
	}

}
