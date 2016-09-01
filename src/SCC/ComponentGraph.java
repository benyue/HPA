package SCC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import HPA.PA;
import Util.util;

/**
 * @author Cindy Yue Ben
 */
public class ComponentGraph {
	ArrayList<Integer>[] SCCs = null;// which SCC contains which node(s)
	HashSet<Integer>[] adj_in_SCC = null;
	HashSet<Integer>[] adj_in_SCC_SelfExclusive = null;
	HashSet<Integer>[] adj_out_SCC = null;
	HashSet<Integer>[] adj_out_SCC_SelfExclusive = null;
	private ArrayList<Integer> CSCCSource = null;// conflict SCC source
	private ArrayList<ArrayList<Integer>> CSCCEnds = null;// conflict SCC ends
	int[] levelSCC = null;

	@SuppressWarnings("unchecked")
	public ComponentGraph(PA g, TarjanSCC tjscc) {
		int N = tjscc.count();
		SCCs = tjscc.sccs();
		adj_in_SCC = (HashSet<Integer>[]) new HashSet[N];
		adj_in_SCC_SelfExclusive = (HashSet<Integer>[]) new HashSet[N];
		adj_out_SCC = (HashSet<Integer>[]) new HashSet[N];
		adj_out_SCC_SelfExclusive = (HashSet<Integer>[]) new HashSet[N];
		levelSCC = new int[N];

		int[] id = tjscc.id; // size V
		obtain_conflict_SCC(g.conflictSource, g.conflictEnds, id);

		// Initialize for each SCC, exclude self-loops
		for (int i = 0; i <= N - 1; i++) {
			adj_in_SCC[i] = new HashSet<Integer>();
			adj_in_SCC_SelfExclusive[i] = new HashSet<Integer>();
			adj_out_SCC[i] = new HashSet<Integer>();
			adj_out_SCC_SelfExclusive[i] = new HashSet<Integer>();
			for (int n1 : SCCs[i]) {
				// inSCC, and inDegree
				for (int n2 : g.adj_in_int[n1]) {
					if (!adj_in_SCC[i].contains(id[n2])) {
						adj_in_SCC[i].add(id[n2]);
						if (id[n1] != id[n2]) {// not counting self loop
							adj_in_SCC_SelfExclusive[i].add(id[n2]);
						}
					}
				}
				// outSCC, and outDegree
				for (int n2 : g.adj_out_int[n1]) {
					if (!adj_out_SCC[i].contains(id[n2])) {
						adj_out_SCC[i].add(id[n2]);
						if (id[n1] != id[n2]) {
							adj_out_SCC_SelfExclusive[i].add(id[n2]);
						}
					}
				}
			}

			levelSCC[i] = -1;// init
		}

	}

	/**
	 * Obtain conflict SCCs using PA's conflict node group
	 */
	private void obtain_conflict_SCC(ArrayList<Integer> conflictSource,
	        ArrayList<ArrayList<Integer>> conflictEnds, int[] id) {
		if (conflictSource == null || conflictSource.isEmpty())
			return;
		this.CSCCSource = new ArrayList<Integer>();
		this.CSCCEnds = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < conflictSource.size(); ++i) {
			CSCCSource.add(id[conflictSource.get(i)]);
			ArrayList<Integer> ends =
			        new ArrayList<Integer>(conflictEnds.get(i).size());
			for (int end : conflictEnds.get(i)) {
				ends.add(id[end]);
			}
			CSCCEnds.add(ends);
		}
	}

	/**
	 * Calculate outDegree for each SCC from adj_out_SCC_SelfExclusive.
	 */
	private int[] outDegree() {
		int N = SCCs.length;
		int[] D = new int[N];
		for (int i = 0; i <= N - 1; i++) {
			D[i] = adj_out_SCC_SelfExclusive[i].size();
		}
		return D;
	}

	/**
	 * Calculate inDegree for each SCC from adj_in_SCC_SelfExclusive.
	 */
	private int[] InDegree() {
		int N = SCCs.length;
		int[] D = new int[N];
		for (int i = 0; i <= N - 1; i++) {
			D[i] = adj_in_SCC_SelfExclusive[i].size();
		}
		return D;
	}

	/**
	 * Assign levels to SCCs, indexing corresponds to that of sccs, as well as
	 * inDegree, and outSCCs. Return k s.t. k-HPA.
	 */
	public int leveling(int[] id, PA g, boolean moreQ1) {
		int[] outDegree = outDegree();
		/* Going Backward, decide max level */
		// assign level MAX
		int[] maxL = new int[SCCs.length];// smaller maxL implies lower level
		for (int i = 0; i < SCCs.length; i++) {// init
			maxL[i] = -1;
		}
		int kMax = SCCs.length - 1;
		int maxL0 = kMax;// smallest non-negative maxL
		for (int idx : util.minNonNeg_idx(outDegree)) {
			maxL[idx] = kMax;
			outDegree[idx]--;
			for (int i : adj_in_SCC_SelfExclusive[idx]) {
				outDegree[i]--;
			}
		}
		// assign max to each remaining SCC
		int m = 0;
		while (m >= 0) {
			ArrayList<Integer> min_od_scc_idx = util.minNonNeg_idx(outDegree);
			for (int idx : min_od_scc_idx) {
				maxL[idx] = max_Level_TD_SCC(idx, id, maxL, kMax);
				if (maxL[idx] < maxL0)
					maxL0 = maxL[idx];
				outDegree[idx]--;
				for (int i : adj_in_SCC_SelfExclusive[idx]) {
					outDegree[i]--;
				}
			}
			m = util.minNonNegtive(outDegree);
		}

		// if (kMax - maxL0 > 1)// k = kMax - maxL0
		// return (kMax - maxL0);
		/*
		 * Assign level to each SCC.
		 */
		if (moreQ1)
			SCCLevelMax(maxL, maxL0); // more Q1, simple
		else
			SCCLevelMin(kMax, maxL, id);// more Q0, complex

		/*
		 * Assign level to each node
		 */
		for (int i = 0; i < g.V.size(); ++i) {
			g.V.get(i).level = levelSCC[id[i]];
		}
		return kMax - maxL0;
	}

	/**
	 * Going Forward, assign max levels to SCCs, by decreasing maxL by maxL0 as
	 * level.
	 */
	private void SCCLevelMax(int[] maxL, int maxL0) {
		for (int i = 0; i < this.levelSCC.length; ++i)
			levelSCC[i] = maxL[i] - maxL0;
	}

	/**
	 * Going Forward, assign min levels to SCCs.
	 */
	private void SCCLevelMin(int kMax, int[] maxL, int[] id) {
		int[] inDegree = InDegree();
		// assign level 0
		for (int idx : util.minNonNeg_idx(inDegree)) {
			levelSCC[idx] = 0;
			inDegree[idx]--;
			for (int i : adj_out_SCC_SelfExclusive[idx]) {
				inDegree[i]--;
			}
		}
		// assign levels to remaining SCCs
		int m = 0;
		while (m >= 0) {
			ArrayList<Integer> minind_scc_idx = util.minNonNeg_idx(inDegree);
			if (minind_scc_idx.size() > 1 &&
			        this.CSCCSource != null && !this.CSCCSource.isEmpty()) {
				for (int j = 0; j < this.CSCCSource.size(); ++j) {
					ArrayList<Integer> nodes = this.CSCCEnds.get(j);
					ArrayList<Integer> todo =
					        util.intersection(nodes, minind_scc_idx);
					if (todo.size() < 2)
						continue;
					int updated = 0;
					while (updated < todo.size()) {
						// sort todo by ascending maxL();
						int min_maxL = kMax;
						for (int idx : todo) {
							if (min_maxL > maxL[idx] && levelSCC[idx] == -1) {
								min_maxL = maxL[idx];
							}
						}
						for (int idx : todo) {
							if (levelSCC[idx] > -1) {
								++updated;
								continue;
							}
							if (maxL[idx] > min_maxL)
								continue;
							levelSCC[idx] = LevelSCC(idx, id);
							inDegree[idx]--;
							for (int i : adj_out_SCC_SelfExclusive[idx]) {
								inDegree[i]--;
							}
							++updated;
						}
					}

				}
			}

			for (int idx : minind_scc_idx) {
				if (levelSCC[idx] > -1) {
					continue;
				}
				levelSCC[idx] = LevelSCC(idx, id);
				inDegree[idx]--;
				for (int i : adj_out_SCC_SelfExclusive[idx]) {
					inDegree[i]--;
				}
			}
			m = util.minNonNegtive(inDegree);
		}

	}

	/**
	 * Decide the max level of a TD SCC according to levels of its outSCCs.
	 * 
	 * @param maxL
	 *            calculated max levels.
	 */
	private int max_Level_TD_SCC(int SCCidx, int[] id, int[] maxL, int kMax) {
		if (maxL[SCCidx] != -1)
			return maxL[SCCidx];
		int ll = kMax;// find min
		// part 1: outSCC
		for (int no : adj_out_SCC_SelfExclusive[SCCidx]) {
			if (maxL[no] > -1 && ll > maxL[no])
				ll = maxL[no];
		}
		// part 2: conflicting SCC
		if (this.CSCCSource != null && !this.CSCCSource.isEmpty()) {
			for (int j = 0; j < this.CSCCSource.size(); ++j) {
				int source = this.CSCCSource.get(j);
				if (source != SCCidx)
					continue;
				ArrayList<Integer> nodes = this.CSCCEnds.get(j);
				if (nodes.contains(SCCidx)) {// self-loop
					for (int no : nodes) {
						if (maxL[no] > -1 && ll > maxL[no] - 1)
							ll = maxL[no] - 1;
					}
				} else {
					// e.g. 0,1,1,2: 0 on 0; 0,1,2,2: 0 on 1.
					int min = -1;
					int min_count = 0;
					for (int no : nodes) {
						if (maxL[no] <= -1)
							continue;
						if (min < 0) {
							min = maxL[no];
							++min_count;
							continue;
						}
						if (min > maxL[no]) {
							min_count = 1;
							min = maxL[no];
						} else if (min == maxL[no]) {
							++min_count;
						}
					} // for
					if (min_count >= 2) {
						if (ll > min - 1)
							ll = min - 1;
					} else {
						if (ll > min)
							ll = min;
					}

				}
			}
		}
		return ll;
	}

	/**
	 * Decide the level of a SCC according to levels of its inSCCs and conflict
	 * SCCs.
	 */
	private int LevelSCC(int SCCidx, int[] id) {
		int lm = 0; // max
		// part 1: inSCC
		for (int j = 0; j < levelSCC.length; j++) {
			if (j != SCCidx && adj_in_SCC_SelfExclusive[SCCidx].contains(j))
				if (levelSCC[j] > lm)
					lm = levelSCC[j];
		}
		// part 2: conflicting SCC
		if (this.CSCCSource != null && !this.CSCCSource.isEmpty()) {
			for (int j = 0; j < this.CSCCSource.size(); ++j) {
				int source = this.CSCCSource.get(j);
				int count = 0;// count SCCidx, e.g. 0,1,1,1 1 is higher than 0
				if(!this.CSCCEnds.get(j).contains(SCCidx)
						||source == SCCidx)//ends must not be leveled yet
					continue;
				for (int end : this.CSCCEnds.get(j)) {
					if (end == SCCidx) {// itself
						count++;
					} else if (levelSCC[end] == levelSCC[source]) {
						// including 0,0,1: 1 higher than 0
						if (levelSCC[source] + 1 > lm)
							lm = levelSCC[source] + 1;
					}
				}
				if (count > 1//SCCidx is higher than source SCC
						&& levelSCC[source] + 1 > lm)
					lm = levelSCC[source] + 1;
			}
		}
		return lm;
	}
}
