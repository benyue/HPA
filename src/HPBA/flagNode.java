package HPBA;

class flagNode {
	int index = -1; // node index in size 1 wsnodes

	/**
	 * flag to denote encounter of final states. false=0 by default, true=1
	 */
	boolean hasFinal;

	/**
	 * Constructor. this.hasFinal = false unless otherwise updated.
	 */
	flagNode(int idx) {
		this.index = idx;
	}

	/**
	 * Constructor.
	 */
	flagNode(int idx, boolean flag) {
		this.index = idx;
		this.hasFinal = flag;
	}
	
	@Override
	public int hashCode() {
		return index;
	}
	
	@Override
	public boolean equals(Object o) {
		//same class, hashcode, and value
		if ((o instanceof flagNode) 
				&& (((flagNode) o).index == index)) {
			return true;
		}
		return false;
	}

}
