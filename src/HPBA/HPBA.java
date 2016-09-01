package HPBA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math3.fraction.Fraction;

import HPA.T;
import HPA.V;
import HPA.edge;
import SCC.TarjanSCC;
import edu.princeton.cs.introcs.In;

/**
 * @author Cindy Yue Ben
 */
public class HPBA extends HPA.PA {
	/**
	 * finalSCC1nodes stores all HPANodes in final SCCs.
	 */
	ArrayList<Integer> finalSCCnodes = new ArrayList<Integer>();

	/**
	 * for each size1wsnodes.get(i), fscc_out_input.get(i) stores inputs for
	 * transitions with pr 1 and leading to the same final SCCs. |size1wsnodes|
	 * = fscc_out_input.size()
	 */
	// ArrayList<ArrayList<String>> fscc_out_input = null;
	/**
	 * for each size1wsnodes.get(i), fscc_out_input2node stores input->end node
	 * index in size1wsnodes
	 */
	private ArrayList<HashMap<String, Integer>> fscc_out_input2node = null;
	/**
	 * for each size1wsnodes.get(i), fscc_mergablenodes stores a set of its
	 * mergable nodes (indexes in size1wsnodes) excluding itself.
	 */
	private ArrayList<HashSet<Integer>> fscc_mergablenodes = null;

	public HPBA(In in, String filetype) throws Exception {
		super(in, filetype);
	}

	/** Obtain buchi good ws for 1-HPA */
	public void witnessSets() {
		try {
			obtainBuchiGoodSWSs();
			super.obtainWS1s();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void obtainBuchiGoodSWSs() throws Exception {
		// int nf = F.size();
		HashSet<Integer>[] adj_out_int_pr1// all pr=1
		        = (HashSet<Integer>[]) new HashSet[V.size()];
		for (int i = 0; i < V.size(); ++i) {
			adj_out_int_pr1[i] = new HashSet<Integer>(V.size());
			V v = V.get(i);
			for (T t : v.outT) {
				if (t.dist.size() != 1) // pr<1
					continue;
				edge e = t.dist.get(0);// the only edge
				if (e.pr.compareTo(Fraction.ONE) == 0) {
					adj_out_int_pr1[i].add(e.node);
				}
			}
		}
		TarjanSCC tj = new TarjanSCC(V.size());
		tj.dfsAll(adj_out_int_pr1);
		ArrayList<Integer>[] sccs = tj.sccs();
		HashSet<Integer> finalSCCs = getFinalSCCs_updateF(tj, sccs);
		/*
		 * if (nf > F.size()) {
		 * reduce();
		 * if (F.isEmpty()) {
		 * m.ErrorMessage = ("ERROR: no F after reduction. Robustness=1.\n");
		 * return m;
		 * }
		 * if (q0 == -1) {
		 * m.ErrorMessage = ("ERROR: no q0 after reduction.\n");
		 * return m;
		 * }
		 * tjscc = new TarjanSCC(V.size());
		 * tjscc.dfs(adj_out_int, q0);
		 * }
		 */

		if (finalSCCs == null || finalSCCs.isEmpty()) {
			// no final SCCs, always empty
			return;
		}
		// add empty set at index 0
		addOrFindWS0(new HashSet<Integer>(), obtainSetOnLevel(F, 0));
		// pick out all size 1 ws nodes, those in FSCCs
		for (int i = 0; i < sccs.length; ++i) {
			if (!finalSCCs.contains(i))
				continue;
			for (int q : sccs[i]) {
				finalSCCnodes.add(q);
				if (V.get(q).level == 1) {
					HashSet<Integer> ws = new HashSet<Integer>(1);
					ws.add(q);
					this.addOrFindWS0(ws, null);
				}else{//level = 0
					this.WS0s.get(0).superQ0.add(q);//superQ0 of empty set
				}
			}
		}
		/*
		 * obtain big sizes: flag construction, traverse back
		 */
		// pick out pr 1 transitions leading to same final SCC
		// fscc_out_input = new ArrayList<ArrayList<String>>();
		fscc_out_input2node = new ArrayList<HashMap<String, Integer>>();
		fscc_mergablenodes = new ArrayList<HashSet<Integer>>();
		for (int i = 0; i < finalSCCnodes.size(); i++) {
			ini1ws(i);
		}
		this.FlagConstructionTopDown();
		// this.FlagConstructionBottomUp();//TODO

		// traverse backward
		obtainWS0sByTraversingBack();
	}

	/**
	 * Obtain the final SCCs of the HPBA, which are non-trivial pr-1 SCCs
	 * containing final states. Then update final states to include only
	 * final states in final SCCs.
	 * 
	 * @param tj
	 */
	private HashSet<Integer> getFinalSCCs_updateF(TarjanSCC tj,
	        ArrayList<Integer>[] sccs) {
		if (F.isEmpty()) {
			return null;
		}
		HashSet<Integer> finalSCCs = new HashSet<Integer>();
		for (int f : F) {
			int sccid = tj.id(f);
			// int level = V.get(f).level;
			if (finalSCCs.contains(sccid))
				continue;
			if (sccs[sccid].size() > 1) {
				finalSCCs.add(sccid);
			} else {// detect whether there is a pr=1 self loop on f
				V n = V.get(f);
				for (int i = 0; i < n.outT.size(); ++i) {
					T t = n.outT.get(i);
					if (t.dist.size() != 1) // pr<1
						continue;
					edge e = t.dist.get(0);
					if (e.node == f && e.pr.compareTo(Fraction.ONE) == 0) {
						finalSCCs.add(sccid);
						break;
					}
				}
			}
		}

		// update final states to contain only final states in final SCCs
		Iterator<Integer> fi = F.iterator();
		while (fi.hasNext()) {
			int q = fi.next();
			if (!finalSCCs.contains(tj.id(q))) {
				fi.remove();
			}
		}
		return finalSCCs;
	}

	/**
	 * For each node in final SCC, generate the list of pr-1 Ts back to the same
	 * SCC.
	 * 
	 * @param i is the index of the size 1 ws node in size1wsnodes.
	 */
	private void ini1ws(int i) {
		int vid = finalSCCnodes.get(i);
		V v = V.get(vid);
		// ArrayList<String> inputs = new ArrayList<String>();
		// fscc_out_input.add(inputs);
		HashMap<String, Integer> input2node = new HashMap<String, Integer>();
		fscc_out_input2node.add(input2node);
		HashSet<Integer> mergeablenodes = new HashSet<Integer>();
		fscc_mergablenodes.add(mergeablenodes);
		for (T t : v.outT) {
			if (t.dist.size() == 1) {
				String a = t.input;
				edge e = t.dist.get(0);
				if (e.pr.compareTo(Fraction.ONE) == 0//pr = 1
				        && tjscc.id(vid) == tjscc.id(e.node)) {
					// inputs.add(a);
					int i1 = finalSCCnodes.indexOf(e.node);
					if (i1 == -1) {
						try {
							throw new Exception(
							        "Error: i-a:1->i1, i1 not in size1wsnodes?!");
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					input2node.put(a, i1);
				}
			}
		}
	}

	private void FlagConstructionTopDown() {
		if (finalSCCnodes == null || finalSCCnodes.size() <= 1)
			return;
		/** trace maintains a list of all traceNodes */
		ArrayList<HashSet<Integer>> trace = new ArrayList<HashSet<Integer>>();
		/**
		 * traceV -> index in trace. Improve searching efficiency.
		 */
		HashMap<HashSet<Integer>, Integer> map =
		        new HashMap<HashSet<Integer>, Integer>();
		/**
		 * Graph structure using for each traceNode, outT maintains a list of
		 * its out transitions. size of trace = size of outT
		 */
		HashSet<Integer> V0 = new HashSet<Integer>(finalSCCnodes.size());
		ArrayList<HashSet<traceT>> outT = new ArrayList<HashSet<traceT>>();
		for (int i = 0; i < this.finalSCCnodes.size(); ++i) {
			V0.add(i);// index in finalSCCnodes
		}
		trace.add(V0);
		map.put(V0, 0);
		outT.add(new HashSet<traceT>());

		HashSet<Integer> next = new HashSet<Integer>();
		next.add(0);
		while (!next.isEmpty()) {// BFS, next level of trace nodes
			HashSet<Integer> next1 = new HashSet<Integer>();
			for (int i : next) {// for each trace node on the current level
				HashSet<Integer> node = trace.get(i);
				for (String input : this.symbols) {
					HashSet<Integer> nodeNext =
					        new HashSet<Integer>(node.size());
					for (int n : node) {
						Integer n1 = this.fscc_out_input2node.get(n).get(input);
						if (n1 == null)
							continue;
						nodeNext.add(n1);
					}
					if (nodeNext.size() <= 1)
						continue;
					Integer nodeNextId = map.get(nodeNext);
					if (nodeNextId == null) {
						trace.add(nodeNext);
						nodeNextId = trace.size() - 1;
						map.put(nodeNext, nodeNextId);
						outT.add(new HashSet<traceT>());
						next1.add(nodeNextId);
					}
					if (nodeNext.size() == node.size())
					    // or else will not contribute to SCC
					    outT.get(i).add(new traceT(input, nodeNextId));
				}
			}
			next = next1;
		}

		// do SCC on trace and outT to obtain strict gbws
		@SuppressWarnings("unchecked")
		HashSet<Integer>[] trace_adj_out =
		        (HashSet<Integer>[]) new HashSet[trace.size()];
		for (int l = 0; l < trace.size(); l++) {
			trace_adj_out[l] = new HashSet<Integer>();
			for (traceT t : outT.get(l)) {
				trace_adj_out[l].add(t.nextNodeIdx);
			}
		}
		TarjanSCC tjscct = new TarjanSCC(trace_adj_out.length);
		tjscct.dfsAll(trace_adj_out);
		for (ArrayList<Integer> scct : tjscct.sccs()) {
			if (scct.size() < 1) {
				continue;
			}
			if (scct.size() == 1) {
				int n = scct.get(0);
				if (!containsT(outT.get(n), n)) {
					// no self loop, not a good ws
					continue;
				}
			}
			// scct.size() >= 1
			int w = trace.get(scct.get(0)).size();
			HashSet<Integer> finalColors = new HashSet<Integer>(w);
			/**
			 * match color c1->c2. c1>c2, update c2 to c3 if c1->c3 and c3<c2
			 */
			HashMap<Integer, Integer> colorMatching =
			        new HashMap<Integer, Integer>(w);
			HashMap<Integer, HashMap<Integer, Integer>> coloredNodes =
			        new HashMap<Integer, HashMap<Integer, Integer>>(
			                scct.size());// index in trace&outT -> colored node
			/*
			 * BFS within this SCC, pick any one node to start, keep exploring
			 * until all nodes in the SCC visited.
			 */
			ArrayList<Integer> todo = new ArrayList<Integer>(scct.size());
			todo.add(scct.get(0));// index in trace&outT
			HashMap<Integer, Integer> v0 = new HashMap<Integer, Integer>(w);
			int c = 0;
			for (int i : trace.get(scct.get(0))) {
				v0.put(i, c);
				if (F.contains(finalSCCnodes.get(i))) {
					finalColors.add(c);
				}
				c++;
			}
			coloredNodes.put(scct.get(0), v0);

			HashSet<Integer> visited = new HashSet<Integer>(scct.size());
			while (!todo.isEmpty() && finalColors.size() < w) {
				ArrayList<Integer> todo1 = new ArrayList<Integer>();
				for (int i = 0; i < todo.size()
				        && finalColors.size() < w; ++i) {// BFS
					v0 = coloredNodes.get(todo.get(i));
					for (traceT t : outT.get(todo.get(i))) {
						if (tjscct.id(todo.get(i)) == tjscct.id(t.nextNodeIdx)
						        && !visited.contains(t.nextNodeIdx)) {
							todo1.add(t.nextNodeIdx);
							HashMap<Integer, Integer> v1 =
							        coloredNodes.get(t.nextNodeIdx);
							if (v1 == null) {
								v1 = new HashMap<Integer, Integer>(w);
							}
							for (int n : v0.keySet()) {
								if (fscc_out_input2node.get(n) == null)
									continue;
								Integer n1 =
								        fscc_out_input2node.get(n).get(t.input);
								if (n1 == null)
									continue;
								if (colorMatching.get(v0.get(n)) != null
								        && colorMatching.get(v0.get(n)) < v0
								                .get(n)) {
									v0.put(n, colorMatching.get(v0.get(n)));// useless?
								}
								if (v1.get(n1) == null) {
									v1.put(n1, v0.get(n));
									if (F.contains(finalSCCnodes.get(n1))) {
										finalColors.add(v0.get(n));
									}
								} else { // do color matching
									// if (F.contains(finalSCC1nodes.get(n1))) {
									// finalColors.contains(v1.get(n1));
									// }
									if (colorMatching.get(v1.get(n1)) != null
									        && colorMatching.get(
									                v1.get(n1)) < v1.get(n1)) {
										v1.put(n1,
										        colorMatching.get(v1.get(n1)));// useless?
									}
									if (F.contains(finalSCCnodes.get(n1))) {
										finalColors.add(v0.get(n));
										finalColors.add(v1.get(n1));// matched c
									}
									if (v0.get(n) > v1.get(n1)) {
										colorMatching.put(v0.get(n),
										        v1.get(n1));
										v0.put(n, v1.get(n1));
									} else if (v0.get(n) < v1.get(n1)) {
										colorMatching.put(v1.get(n1),
										        v0.get(n));
										v1.put(n1, v0.get(n));
									}
								}
							}
							coloredNodes.put(t.nextNodeIdx, v1);
						}
					}
					visited.add(todo.get(i));// index in trace&outT
				}
				todo = todo1;
			}

			// obtain good ws using finalColors
			if (finalColors.size() > 1) {
				for (HashMap<Integer, Integer> m : coloredNodes.values()) {
					HashSet<Integer> ws = new HashSet<Integer>(w);
					HashSet<Integer> superQ0 = new HashSet<Integer>(w);
					for (@SuppressWarnings("rawtypes")
					Map.Entry e : m.entrySet()) {
						if (!finalColors.contains(e.getValue()))
							continue;
						int node = this.finalSCCnodes.get((int) e.getKey());
						if (this.V.get(node).level == 1)
							ws.add(node);
						else
							superQ0.add(node);
					}
					addOrFindWS0(ws, superQ0);
				}
			}
		}
	}

	private void FlagConstructionBottomUp() {
		FlagConstructionSize1_vector();
		// FlagConstructionSize1_set();

		// size >=3
		/** trace maintains a list of all traceNodes */
		ArrayList<traceV> trace = new ArrayList<traceV>();
		/**
		 * Map each traceV to its index in trace. Improve searching efficiency.
		 */
		HashMap<traceVset, Integer> traceVSearchMap =
		        new HashMap<traceVset, Integer>();
		/**
		 * for each traceNode, outT maintains a list of its out transitions.
		 * size of trace = size of outT
		 */
		ArrayList<HashSet<traceT>> outT = new ArrayList<HashSet<traceT>>();
		// TODO

	}

	/**
	 * Do flag construction for size 1 wss. Obtain all STRONG gbwss.
	 * TODO: uncompleted
	 */
	@SuppressWarnings({"unchecked"})
	private void FlagConstructionSize1_set() {
		/** trace maintains a list of all traceNodes */
		ArrayList<traceVset> trace = new ArrayList<traceVset>();
		/**
		 * Map each traceV to its index in trace. Improve searching efficiency.
		 */
		HashMap<traceVset, Integer> traceVSearchMap =
		        new HashMap<traceVset, Integer>();
		/**
		 * for each traceNode, outT maintains a list of its out transitions.
		 * size of trace = size of outT
		 */
		ArrayList<HashSet<traceT>> outT = new ArrayList<HashSet<traceT>>();

		for (int i = 0; i < finalSCCnodes.size(); i++) {
			for (int j = i + 1; j < finalSCCnodes.size(); j++) {
				traceVset vn = iniTraceVset(i, j);// i!=j
				int vid = traceVSearchMap.get(vn);// trace.indexOf(vn);
				if (vid == -1) {
					vid = trace.size();
					addFCnodeSet(trace, traceVSearchMap, outT, vn);
				}

				for (String a : this.symbols) {
					Integer i1 = fscc_out_input2node.get(i).get(a);
					Integer j1 = fscc_out_input2node.get(j).get(a);
					if (i1 == null || j1 == null)
						continue;
					if (i1.equals(j1)) { // good ws
						// will be obtained later by traversing backward
						continue;
					}
					traceVset v1n = this.iniTraceVset(i1, j1);
					int v1id = traceVSearchMap.get(v1n);
					if (v1id == -1) {
						v1id = trace.size();
						addFCnodeSet(trace, traceVSearchMap, outT, v1n);
					}
					// the trans cannot exist since 1st dealing with <i,j>'s out
					outT.get(vid).add(new traceT(a, v1id));
				}
			}
		}
		// do SCC on trace and outT, find all size 2 and record size 1's
		// mergable.
		HashSet<Integer>[] trace_adj_out =
		        (HashSet<Integer>[]) new HashSet[trace.size()];
		for (int l = 0; l < trace.size(); l++) {
			for (traceT t : outT.get(l)) {
				trace_adj_out[l].add(t.nextNodeIdx);
			}
		}
		TarjanSCC tjscct = new TarjanSCC(trace.size());
		tjscct.dfsAll(trace_adj_out);
		ArrayList<Integer>[] sccst = tjscct.sccs();

		for (ArrayList<Integer> scct : sccst) {
			// TODO
		}

	}

	/**
	 * Create a traceVset node using indices in finalSCCnodes.
	 */
	private traceVset iniTraceVset(int i, int j) {
		if (i == j)
			return null;
		int ws1 = finalSCCnodes.get(i);
		flagNode n1 = new flagNode(i);
		if (this.F.contains(ws1))
			n1.hasFinal = true;
		int ws2 = finalSCCnodes.get(j);// ws1 != ws2
		flagNode n2 = new flagNode(j);
		if (this.F.contains(ws2))
			n2.hasFinal = true;
		HashSet<flagNode> v = new HashSet<flagNode>(2);
		v.add(n1);
		v.add(n2);
		return new traceVset(v);
	}

	/**
	 * Create a traceVset node using indices in this.V.
	 */
	private traceVset createTraceVset(HashSet<Integer> set) {
		if (set == null || set.isEmpty())
			return null;
		HashSet<flagNode> fn = new HashSet<flagNode>(set.size());
		for (int i : set) {
			flagNode n = new flagNode(i);
			if (F.contains(i))
				n.hasFinal = true;
			fn.add(n);
		}
		return new traceVset(fn);
	}

	/**
	 * Do flag construction for size 1 wss. Obtain all STRONG gbwss. Construct
	 * the whole graph first, then do SCC.
	 */
	private void FlagConstructionSize1_vector() {
		/** trace maintains a list of all traceNodes */
		ArrayList<traceV> trace = new ArrayList<traceV>();
		/**
		 * Map each traceV to its index in trace. Improve searching efficiency.
		 */
		HashMap<ArrayList<Integer>, Integer> traceVSearchMap =
		        new HashMap<ArrayList<Integer>, Integer>();

		/**
		 * for each traceNode, outT maintains a list of its out transitions.
		 * size of trace = size of outT
		 */
		ArrayList<HashSet<traceT>> outT = new ArrayList<HashSet<traceT>>();
		for (int i = 0; i < finalSCCnodes.size(); i++) {
			for (int j = 0; j < finalSCCnodes.size(); j++) {
				if (i == j)
					continue;
				// Integer ws1 = finalSCC1nodes.get(i);
				// Integer ws2 = finalSCC1nodes.get(j);// ws1 != ws2
				ArrayList<Integer> v = new ArrayList<Integer>(2);
				v.add(i);
				v.add(j);
				traceV vn = new traceV(v);
				Integer vid = traceVSearchMap.get(v);// trace.indexOf(vn);
				if (vid == null || vid == -1) {
					vid = trace.size();
					addFCnodeVector(trace, traceVSearchMap, outT, vn);
				}
				for (String a : this.symbols) {
					Integer i1 = fscc_out_input2node.get(i).get(a);
					Integer j1 = fscc_out_input2node.get(j).get(a);
					if (i1 == null || j1 == null)
						continue;
					if (i1.equals(j1)) { // good ws
						// will be obtained later by traversing backward
						// recordMergable(i, j);
						// HashSet<Integer> ws = new HashSet<Integer>();
						// ws.add(finalSCC1nodes.get(i));
						// ws.add(finalSCC1nodes.get(j));
						// this.addWS0(ws, WS0s.size(), null);
						continue;
					}
					ArrayList<Integer> v1 = new ArrayList<Integer>(2);
					v1.add(i1);
					v1.add(j1);
					traceV v1n = new traceV(v1);
					int v1id = trace.indexOf(v1n);
					if (v1id == -1) {
						v1id = trace.size();
						addFCnodeVector(trace, traceVSearchMap, outT, v1n);
					}
					// the trans cannot exist since 1st dealing with <i,j>'s out
					outT.get(vid).add(new traceT(a, v1id));
				}
			}
		}
		// do SCC on trace and outT, find all size 2 and record size 1's
		// mergable.
		@SuppressWarnings("unchecked")
		HashSet<Integer>[] trace_adj_out =
		        (HashSet<Integer>[]) new HashSet[trace.size()];
		for (int l = 0; l < trace.size(); l++) {
			for (traceT t : outT.get(l)) {
				trace_adj_out[l].add(t.nextNodeIdx);
			}
		}
		TarjanSCC tjscct = new TarjanSCC(trace.size());
		tjscct.dfsAll(trace_adj_out);
		ArrayList<Integer>[] sccst = tjscct.sccs();
		for (ArrayList<Integer> scct : sccst) {
			if (scct.size() < 1)
				continue;
			if (scct.size() == 1) {
				int n = scct.get(0);
				if (!containsT(outT.get(n), n)) {
					// no self loop, not a good ws
					continue;
				}
				traceV tn = trace.get(n);
				if (tn.V.size() == 1) {
					// size 1 ws, has been taken care of
					continue;
				}
				boolean allF = true;
				for (int nvid : tn.V) {
					if (!F.contains(finalSCCnodes.get(nvid))) {
						// not every "position" in the vector has final,
						// this is not a good ws
						allF = false;
						break;
					}
				}
				if (allF) {// size 2 ws obtained
					recordMergable(tn);
					addTraceNode2WS0(tn);
				}
				continue;
			}
			// scct.size() > 1
			int width = trace.get(0).V.size();
			// here every vector has same width
			boolean[] hasF = new boolean[width];
			boolean hasf = false;
			for (int i = 0; i < scct.size(); ++i) {
				traceV tn = trace.get(scct.get(i));
				for (int l = 0; l < width; l++) {
					if (hasF[l])
						continue;
					if (F.contains(finalSCCnodes.get(tn.V.get(l)))) {
						hasF[l] = true;
						if (Util.util.count(hasF, true) == width) {
							hasf = true;
							i = scct.size();
							break;
						}
					}
				}
			}
			if (hasf) { // every vector in this scct denotes a new good ws.
				for (int tnid : scct) {
					traceV tn = trace.get(tnid);
					recordMergable(tn);
					addTraceNode2WS0(tn);
				}
			}
		}
	}

	/**
	 * Record a WS0 from a traceV node.
	 */
	private int addTraceNode2WS0(traceV tn) {
		HashSet<Integer> ws = new HashSet<Integer>(tn.width());
		HashSet<Integer> superQ0 = new HashSet<Integer>(tn.width());
		for (int n : tn.V) {
			int node = finalSCCnodes.get(n);
			if (this.V.get(node).level == 1)
				ws.add(node);
			else
				superQ0.add(node);
		}
		return addOrFindWS0(ws, superQ0);
	}

	/**
	 * Record a WS0 from a traceV node.
	 */
	private int addTraceNode2WS0(traceVset tn) {
		HashSet<Integer> ws = new HashSet<Integer>(tn.width());
		HashSet<Integer> superQ0 = new HashSet<Integer>(tn.width());
		for (flagNode n : tn.V) {
			int node = finalSCCnodes.get(n.index);
			if (this.V.get(node).level == 1)
				ws.add(node);
			else
				superQ0.add(node);
		}
		return addOrFindWS0(ws, superQ0);
	}

	/**
	 * Called in size 1 flag construction to record for each size 1 wsnode its
	 * mergable wsnodes.
	 */
	private void recordMergable(traceV tn) {
		for (int i : tn.V) {
			for (int j : tn.V) {
				if (i != j) {
					fscc_mergablenodes.get(i).add(j);
				}
			}
		}
	}

	private void recordMergable(traceVset tn) {
		for (flagNode i : tn.V) {
			for (flagNode j : tn.V) {
				if (i.index != j.index) {
					fscc_mergablenodes.get(i.index).add(j.index);
				}
			}
		}
	}

	private void recordMergable(int i, int j) {
		if (i != j) {
			fscc_mergablenodes.get(i).add(j);
			fscc_mergablenodes.get(j).add(i);
		}
	}

	/** Decide if n is one of the end nodes in trans in tlist */
	private boolean containsT(HashSet<traceT> tlist, int n) {
		for (traceT t : tlist) {
			if (t.nextNodeIdx == n)
				return true;
		}
		return false;
	}

	private boolean addFCnodeSet(ArrayList<traceVset> trace,
	        HashMap<traceVset, Integer> traceVSearchMap,
	        ArrayList<HashSet<traceT>> outT, traceVset q) {
		if (traceVSearchMap.get(q) != null)
			return false;
		if (q == null || q.V == null || q.V.isEmpty())
			return false;
		trace.add(q);
		traceVSearchMap.put(q, trace.size() - 1);
		outT.add(new HashSet<traceT>());
		return true;
	}

	/**
	 * add a new node to the trace, initialize its out trans list. return false
	 * if q is already in the trace or it's invalid.
	 */
	private boolean addFCnodeVector(ArrayList<traceV> trace,
	        HashMap<ArrayList<Integer>, Integer> traceVSearchMap,
	        ArrayList<HashSet<traceT>> outT, traceV q) {
		if (traceVSearchMap.get(q) != null)
			return false;
		if (q == null || q.V == null || q.V.isEmpty())
			return false;
		trace.add(q);
		traceVSearchMap.put(q.V, trace.size() - 1);
		outT.add(new HashSet<traceT>());
		return true;
	}
}
