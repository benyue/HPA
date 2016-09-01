package HPA;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;

/**
 * @author Cindy Yue Ben
 * 
 *         A witness set is a subset of the state space Q, with at most one Q0
 *         state.
 */
public class WitnessSet {
	public int id = -1;
	public boolean isSuperGoodWS1 = false;

	public WitnessSet() {
	}

	public WitnessSet(int id) {
		this.id = id;
	}

	// for forward algorithm, WS has at most 1 Q0 state.
	Hashtable<String, Integer> Waq = new Hashtable<String, Integer>();
	// a#q => index of Waq in WitnessSet_1Q0, unique for fixed a and q
	Hashtable<Long, Vali_Probk> VALI = new Hashtable<Long, Vali_Probk>();// i=0,1,...
	// at each i, a unique key WS decides the Val value, either itself or some
	// Waq at some a;
	// could be more than one Waq giving the same Val.
	// i => pr=Val_value; k=-1;
	// symbols={a,b,...}; VALpreWSs_PROBKsuccessors={Waq,Wbq,...}
	// symbols and VALpreWSs_PROBKsuccessors correspond to each other

	/**
	 * Compare Vali_i with each Vali_j with j less than i, if for any j, the
	 * predecessor that decides Val repeats, stop. Return the repeated
	 * WS_VAL(s), with last element of Alpha_1, i.e. j, when for the first time
	 * this WS_VAL_i = WS_VAL_j;
	 */
	public ArrayList<Integer> repeat_WS_VAL(long i) {
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		for (int j = 0; j < i; j++) {
			try {
				Vali_Probk vp = VALI.get((long) j);
				ArrayList<Integer> preWSs = vp.VALpreWSs_PROBKsuccessors;
				ArrayList<String> preSymbols = vp.symbols;
				for (int k = 0; k < preWSs.size(); k++) {
					Integer prews = preWSs.get(k);
					if (preWSs.contains(prews)
					        && !preSymbols.get(k).isEmpty()
					        && VALI.get((long) j).pr.compareTo(VALI
					                .get((long) i).pr) > 0) {
						tmp.add(prews);
					}
				}
				if (!tmp.isEmpty()) {
					tmp.add(j);
					return tmp;
				}
			} catch (java.lang.NullPointerException e) {
				return null;
			}
		}
		return null;
	}

	void printVALI() {
		//System.out.println("WS id=" + id);
		for (Entry<Long, Vali_Probk> e : VALI.entrySet()) {
			System.out.println(
			        "i:" + e.getKey() + ",val=" + e.getValue().pr + "."
			                + "input:" + e.getValue().symbols
			                + ",preWSs:"
			                + e.getValue().VALpreWSs_PROBKsuccessors);
		}

	}

}
