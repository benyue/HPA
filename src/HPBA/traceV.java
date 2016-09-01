package HPBA;

import java.util.ArrayList;

class traceV{
	/**
	 * tracenode stores nodes indecies in size 1 wsnodes
	 */
	ArrayList<Integer> V = null;//vector
	/**
	 * com_inputs stores common inputs of all nodes in the vector.
	 */
	//HashSet<String> com_inputs = null;

	traceV(ArrayList<Integer> node) {
		V = node;
	}

	/**
	 * Return the number of HPANodes in the tracenode. Return 0 if tracenode
	 * == null.
	 */
	Integer width() {
		if (V != null)
			return V.size();
		else
			return 0;
	}

	/**
	 * Count the number of same elements (same value at same index in the
	 * vector) in two vectors. Return 0 if the two vectors have different
	 * sizes.
	 */
	int sameElementsCount(traceV o) {
		if (V == null || o.V == null)
			return 0;
		int qsize = V.size();
		int osize = o.V.size();
		if (qsize != osize)
			return 0;
		int count = 0;
		for (int i = 0; i < qsize; i++) {
			try {
				if (V.get(i) == o.V.get(i))
					count++;
			} catch (Exception e) {
				// null elements
				continue;
			}
		}
		return count;
	}


	public int compareTo(traceV o) {
		int qsize = V.size();
		if (o.V == null || o.V.size() > qsize) return 1;
		if(o.V.size() < qsize) return -1;
		int j = 0;
		while (j < qsize) {
			if (V.get(j) != o.V.get(j))
				break;
			++j;
		}
		if (j == qsize) {
			return 0;
		}
		return -1;
	}

}
