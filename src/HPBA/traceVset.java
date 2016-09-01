package HPBA;

import java.util.HashSet;

class traceVset {

	HashSet<flagNode> V = null;

	/**
	 * com_inputs stores common inputs of all HPA nodes in this trace node.
	 */
	// HashSet<String> com_inputs = null;

	traceVset() {
	}

	traceVset(HashSet<flagNode> node) {
		V = node;
	}

	/**
	 * Return the number of HPANodes in the tracenode. Return 0 if tracenode ==
	 * null.
	 */
	Integer width() {
		if (V != null)
			return V.size();
		else
			return 0;
	}

	@Override
	public int hashCode() {
		return V.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof traceVset 
				&& ((traceVset) o).hashCode() == this.hashCode()
				&& this.V.equals(((traceVset) o).V)) {
			return true;
		}
		return false;
	}

	/**
	 * Retrieve the integer values from the flagNode in this trace node.
	 */
	/*public HashSet<Integer> Vset() {
		if (this.V == null)
			return null;
		HashSet<Integer> res = new HashSet<Integer>(this.V.size());
		for (flagNode fn : this.V) {
			if (fn == null)
				continue;
			res.add(fn.index);
		}
		return res;
	}*/

}
