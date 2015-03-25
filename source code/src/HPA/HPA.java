package HPA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;

import Util.TarjanSCC;
import Util.util;

public class HPA {

	/**
	 * Return true if the given graph is HPA. If there exits 1-a->2 and 1-a->3,
	 * at most one of 2 and 3 can be strongly connected with 1 in a HPA.
	 * 
	 * @param cCSS
	 *            = g.conflict_node_group
	 * @param id
	 *            was obtained from calling TarjanSCC_q0 for g.
	 * */
	public boolean isHPA(String cCSS, int[] id) {
		if (cCSS.isEmpty())
			return true;

		for (String s : cCSS.split(";")) {
			if (s.isEmpty())
				continue;
			boolean sc = false; // record if there is already one end on the
								// same level w/ source
			List<Integer> nodes = util.parsePosIntegers(s);
			int v = nodes.get(0);
			for (int i = 1; i < nodes.size(); i++) {
				int w = nodes.get(i);
				if (v == w)
					continue;
				if (id[v] == id[w]) {
					if (sc)
						return false;
					sc = true;
				}
			}
		}
		return true;
	}

	/**
	 * Return the in-degrees of SCCs index corresponds to that of sccs.
	 * Self-loops are NOT counted towards in-degrees
	 * */
	private int[] inDegreeOfSCC(PA g, TarjanSCC tjscc) {
		int M = tjscc.count();
		ArrayList<Integer>[] sccs = tjscc.sccs(); // size M
		int[] sccHead = tjscc.head(); // size V

		int[] inDegree = new int[M];
		for (int i = 0; i < M; i++) {
			ArrayList<Integer> scc = sccs[i];
			ArrayList<Integer> inSCCs = new ArrayList<Integer>();
			for (int n1 : scc) {
				for (int j = 0; j < g.adj_in_int[n1].size(); j++) {
					int n2 = g.adj_in_int[n1].get(j);
					if (!inSCCs.contains(sccHead[n2])
							&& sccHead[n1] != sccHead[n2]// exclude self-loops
					) {
						inDegree[i]++;
						inSCCs.add(sccHead[n2]);
					}
				}
			}

		}
		return inDegree;

	}

	/**
	 * Return the neighboring next SCCs of each SCC index corresponds to that of
	 * sccs; outSCCs excludes SCC itself even if it contains self-loop
	 * */
	@SuppressWarnings("unchecked")
	private ArrayList<Integer>[] outSCCsOfSCC(PA g, TarjanSCC tjscc) {
		int M = tjscc.count();
		ArrayList<Integer>[] outSCCs = (ArrayList<Integer>[]) new ArrayList[M];
		for (int i = 0; i < M; i++) {
			outSCCs[i] = this.outSCCofSCCi(g, tjscc, i);
		}
		return outSCCs;
	}

	private ArrayList<Integer> outSCCofSCCi(PA g, TarjanSCC tjscc, int i) {
		ArrayList<Integer> outSCC = new ArrayList<Integer>();
		int[] id = tjscc.id(); // size V
		ArrayList<Integer>[] sccs = tjscc.sccs(); // size M
		ArrayList<Integer> scc = sccs[i];

		for (int n1 : scc) {
			for (int j = 0; j < g.adj_out_int[n1].size(); j++) {
				int n2 = g.adj_out_int[n1].get(j);
				if (!outSCC.contains(id[n2]) && id[n1] != id[n2] // excludes
																	// self-loops
				) {
					outSCC.add(id[n2]);
				}
			}
		}
		return outSCC;
	}

	/**
	 * Return the neighboring last SCCs of each SCC index corresponds to that of
	 * SCCs; inSCCs excludes SCC itself even if it contains self-loop
	 * */
	@SuppressWarnings("unchecked")
	private ArrayList<Integer>[] inSCCsOfSCC(PA g, TarjanSCC tjscc) {
		int M = tjscc.count();
		ArrayList<Integer>[] inSCCs = (ArrayList<Integer>[]) new ArrayList[M];
		for (int i = 0; i < M; i++) {
			inSCCs[i] = this.inSCCofSCCi(g, tjscc, i);
		}
		return inSCCs;
	}

	private ArrayList<Integer> inSCCofSCCi(PA g, TarjanSCC tjscc, int i) {
		ArrayList<Integer> inSCC = new ArrayList<Integer>();
		int[] id = tjscc.id(); // size V
		ArrayList<Integer>[] sccs = tjscc.sccs(); // size M
		ArrayList<Integer> scc = sccs[i];

		for (int n1 : scc) {
			for (int j = 0; j < g.adj_in_int[n1].size(); j++) {
				int n2 = g.adj_in_int[n1].get(j);
				if (!inSCC.contains(id[n2]) && id[n1] != id[n2] // excludes
																// self-loops
				) {
					inSCC.add(id[n2]);
				}
			}
		}
		return inSCC;
	}

	/**
	 * Assign levels to SCCs, indexing corresponds to that of sccs, as well as
	 * inDegree, and outSCCs TODO
	 * */
	public int[] levelSCCs(PA g, TarjanSCC tjscc) {
		ArrayList<Integer>[] outSCCs = this.outSCCsOfSCC(g, tjscc);
		ArrayList<Integer>[] inSCCs = this.inSCCsOfSCC(g, tjscc);
		int[] ind = this.inDegreeOfSCC(g, tjscc);
		int[] level = new int[ind.length];
		for (int i = 0; i < ind.length; i++)
			level[i] = -1;
		// assign level 0
		for (int idx : util.minNonNeg_idx(ind)) {
			level[idx] = 0;
			ind[idx]--;
			for (int i = 0; i < outSCCs[idx].size(); i++) {
				ind[outSCCs[idx].get(i)]--;
			}
		}
		// assign levels to remaining SCCs until each SCC is assigned a level
		int m = 0;
		while (m >= 0) {
			ArrayList<Integer> minind_scc_idx = util.minNonNeg_idx(ind);
			for (int idx : minind_scc_idx) {
				level[idx] = this.LevelSCC(idx, inSCCs[idx], level,
						g.conflict_node_group, tjscc);
				ind[idx]--;
				for (int i = 0; i < outSCCs[idx].size(); i++) {
					ind[outSCCs[idx].get(i)]--;
				}
			}
			m = util.minNonNegtive(ind);
		}
		return level;
	}

	/**
	 * Assign level to each node according to the level of the SCC it is in
	 * 
	 * @param levelSCC
	 *            the level of each SCC
	 * @param g
	 * */
	public void levelNodes(PA g, TarjanSCC tjscc, int[] levelSCC) {
		// int M = levelSCC.length;
		for (HPANode node : g.V) {
			node.level = levelSCC[tjscc.id[node.id]];
		}
	}

	/**
	 * Decide the level of a SCC according to levels of its inSCCs and conflict
	 * SCCs
	 * */
	private int LevelSCC(int SCCidx, ArrayList<Integer> inSCC, int[] level,
			String cSCC, TarjanSCC tjscc) {
		Set<Integer> ll = new HashSet<Integer>();
		for (int node : util.parsePosIntegers(cSCC)) {
			cSCC = cSCC.replaceAll(";" + node + ",", ";" + tjscc.id(node)
					+ "*,");
			cSCC = cSCC.replaceAll("," + node + ",", "," + tjscc.id(node)
					+ "*,");
		}
		for (String s : cSCC.split(";")) {// for each conflict group
			if (s.isEmpty())
				continue;

			List<Integer> nodes = util.parsePosIntegers(s);
			if (!nodes.contains(SCCidx))
				continue;
			if (nodes.get(0) == SCCidx)
				continue;
			if (Collections.frequency(nodes, SCCidx) > 1) {// &nodes.get(0)!=SCCidx
				ll.add(level[nodes.get(0)] + 1);
			}
			for (int i = 1; i < nodes.size(); i++) {
				if (nodes.get(i) == SCCidx)
					continue;
				if (level[nodes.get(0)] == level[nodes.get(i)]) { // 有其它SCC和sourceSCC同层
					ll.add(level[nodes.get(0)] + 1);
					break;
				}
			}
		}
		ll.add(0);
		for (int j = 0; j < level.length; j++) {
			if (j == SCCidx)
				continue;
			if (inSCC.contains(j))
				ll.add(level[j]);
		}
		return Collections.max(ll);
	}

	public static void main(String[] args) throws Exception {

	}

	public String fwdCheckX(PA g, int qs, Fraction x, Long L) throws Exception {
		g.FWD.clear();
		g.clear_WS_VALI();
		g.FWD_k = (long) 0;
		g.FWD_Val.clear(); // IMPORTANT!!!

		if (g.F.isEmpty()) {
			return "Using forward alg, L(A) is empty with threshold at L=0 [Empty Final States]\n";
		}

		String message = "";
		boolean F0 = !g.obtainSetOnLevel(g.F, 0).isEmpty(); // true if F0 !=
															// empty
		for (long i = 0; i <= g.WSs.size(); i++) {// i = length of input
													// strings, starts from 0
			int count = 0;
			String val_string = "";
			// System.out.println("VAL string:"+g.FWD.toString()+", and\n"+g.FWD_Val.toString()+"\n");
			for (int ws = 0; ws <= g.WSs.size() - 1; ws++) {
				WitnessSet WS = g.WSs.get(ws);
				Fraction val = g.Val(ws, qs, x, i);
				val_string += val.toString() + ";";
				Vali_Probk v = WS.VALI.get(i);
				/*
				 * if(i==1){
				 * System.out.println(ws+" has Waq "+g.WSs.get(ws).Waq.
				 * toString()); }
				 */
				if (val.compareTo(Fraction.ZERO) < 0
						|| (F0 && WS.isSuperGoodWS
						&& val.compareTo(Fraction.ZERO) >= 0
						&& val.compareTo(Fraction.ONE) < 0)) {
					message = "Using forward alg, L(A) is non-empty with threshold x="
							+ x + " at L=" + i + ". [Val Termination]\n";// +" at val= "+val.toString()+"\n";
					g.FWD_k = i;
					// message +=
					// ("VAL string:"+g.FWD.toString()+", and\n"+g.FWD_Val.toString()+"\n");
					// tracing back
					String u = "[i="+i+"]";
					int nextws = WS.id;
					u += g.WSNodes(nextws);
					while (i > 0) {
						u += "<-" + v.symbols + "<-";
						u += "[i="+(i-1)+"]";
						for (int wspre : v.VALpreWSs_PROBKsuccessors) {
							u += g.WSNodes(wspre);
						}
						//|v.symbols.size()| = |v.VALpreWSs_PROBKsuccessors|, 
						nextws = v.VALpreWSs_PROBKsuccessors.size() > 1 ? v.VALpreWSs_PROBKsuccessors
								.get(1) : v.VALpreWSs_PROBKsuccessors.get(0);//TODO, one trace only
						i--;
						v = g.WSs.get(nextws).VALI.get(i);
						if (v == null)
							break;
					}
					message += "Accepted sequence and runs: " + u + "\n";
					return message;
				}

				// System.out.println();
				// for(WitnessSet WS1:g.WSs){
				// System.out.println("WS id="+WS1.id);
				// for(Long key:WS1.VALI.keySet()){
				// System.out.println(key+":"+WS1.VALI.get(key).symbols +"::"+
				// WS1.VALI.get(key).VALpreWSs_PROBKsuccessors);
				// }
				// }
				ArrayList<Integer> A = WS.repeat_WS_VAL(i);//return j+"#"+tmp.toString();
				if ( A!=null) {					
					int j = A.get(A.size()-1);
					
					message = "Using forward alg, L(A) is non-empty with threshold x="
							+ x + " at L=" + i + ".[Repeated WS_VAL]\n";// +" at val= "+val.toString()+"\n";
					g.FWD_k = i;
					// message +=
					// ("VAL string:"+g.FWD.toString()+", and\n"+g.FWD_Val.toString()+"\n");
					message += "Accepted sequence and runs: (Keep repeating sequence between i="+j+" and i="+i+").\n";
					// tracing back
					String u = "[i="+i+"]";
					int nextws = WS.id;
					u += g.WSNodes(nextws);
					while (i > 0) {
						u += "<-" + v.symbols + "<-";
						u += "[i="+(i-1)+"]";
						for (int wspre : v.VALpreWSs_PROBKsuccessors) {
							u += g.WSNodes(wspre);
						}
						//|v.symbols.size()| = |v.VALpreWSs_PROBKsuccessors|, 
						if(i!=j){
							nextws = v.VALpreWSs_PROBKsuccessors.size() > 0 ?v.VALpreWSs_PROBKsuccessors.get(0):
								nextws;
						}else{
							for(int t=0;t<A.size()-1;t++){
								if(v.VALpreWSs_PROBKsuccessors.contains(A.get(t))&&A.get(t)!=WS.id){
									nextws = A.get(t);
								}
							}
						}
						i--;
						v = g.WSs.get(nextws).VALI.get(i);
						if (v == null)
							break;
					}
					message += u +".\n";
					return message;
				}
				if (val.compareTo(Fraction.ONE) >= 0)// (val >= 1)
					count++;
			}// for ws
			if (count == g.WSs.size()) {
				// val>=1 for every WS at the same i => empty
				message = "Using forward alg, L(A) is empty with threshold x="
						+ x + " at L=" + i + ". [W Limitation]\n";
				// message +=
				// ("VAL string:"+g.FWD.toString()+", and\n"+g.FWD_Val.toString()+"\n");

				g.FWD_k = i;
				return message;
			}

			if (g.FWD_Convergent(val_string, i)) {
				// message =
				// ("VAL string:"+g.FWD.toString()+", and\n"+g.FWD_Val.toString()+"\n");
				message += "Using forward alg, L(A) is empty with threshold x="
						+ x + " at L=" + (i - 1) + ". [Forward Convergence]\n";
				g.FWD_k = i;
				return message;
			}
		}// for i

		// i = g.WSs.size() + 1
		message += "Using forward alg, L(A) is non-empty with threshold x=" + x
				+ " after reaching L=ws=" + g.WSs.size() + " \n";
		// message +=
		// ("VAL string:"+g.FWD.toString()+", and\n"+g.FWD_Val.toString()+"\n");
		g.FWD_k = (long) g.WSs.size();
		return message;
	}

	public String bkdCheckX_withTracingBack(PA g, int qs, Fraction x, long L)
			throws Exception {
		if (g.F.isEmpty()) {
			return "Using backward alg, L(A) is empty with threshold at L=0 [Empty Final States]\n";
		}
		String message = "";
		for (long k = 1; k <= L; k++) { // k starts from 1
			for (int ws = 0; ws <= g.WSs.size() - 1; ws++) {
				WitnessSet W = g.WSs.get(ws);
				Fraction probk = g.Prob_k_W(W, k);// calculate and store probk
				if (probk == null) {// Error in bkd, maybe empty non-convergence
					g.BKD_x = null;
					g.BKD_k = null;
					message = "Exception in backward alg: Integer overflow at L="
							+ k + ", consider Empty Non-Convergence. \n";
					/*
					 * for(WitnessSet Wo: g.WSs){ message +=
					 * Wo.a_successor.get("a").toString()+";"; } for(String
					 * s:g.PROBK.keySet()){ message +=
					 * s+"="+g.PROBK.get(s).pr+";"; } message += "\n";
					 */
					return message;
				}
				Vali_Probk prk = g.PROBK.get(ws + "#" + k);

				if (W.q0 == qs && g.WSL1Nodes(ws).isEmpty() // W={qs}
						&& probk.compareTo(x) == 1) {// probk > x) {
					message = "Using backward alg, L(A) is non-empty with threshold x="
							+ x + " at L=" + k + ".\n";
					// tracing back
					String u = "";
					while (ws != -1 & k > 0) {
						Set<Integer> nodes = g.WSNodes(ws);
						if (!nodes.isEmpty())
							u += nodes;
						if (!prk.symbols.isEmpty())
							u += "->" + prk.symbols + "->";
						ws = prk.VALpreWSs_PROBKsuccessors.isEmpty() ? -1
								: prk.VALpreWSs_PROBKsuccessors.get(0);// any
																		// one
																		// will
																		// lead
																		// to
																		// ws=-1
						if (ws == -1) {
							Set<Integer> nodesF = new HashSet<Integer>();
							for (String a : prk.symbols) {
								nodesF.addAll(util.intersection(
										g.post(nodes, a), g.F));
							}
							if (!nodesF.isEmpty())
								u += nodesF;
							break;
						}
						k--;
						W = g.WSs.get(ws);
						prk = g.PROBK.get(ws + "#" + k);
						if (prk == null)
							break;
					}
					message += "Accepted sequence and runs: " + u + " \n";
					return message;
				}
			}// for each WS

			// K++, if every probk of WSs remains unchanged, terminates early.
			// Fixed Point.
			if (k == 1)
				continue;
			int conv = 0;
			for (WitnessSet WS : g.WSs) {
				if (g.PROBK.get(WS.id + "#" + k).pr.compareTo(g.PROBK.get(WS.id
						+ "#" + (k - 1)).pr) != 0)
					continue;
				else
					conv++;
			}
			if (conv == g.WSs.size()) {
				message = "Using bakcward alg, L(A) is empty with threshold x="
						+ x + " within L=" + k + ". [Backward Convergence]\n";
				/*
				 * for(String s:g.PROBK.keySet()){ message +=
				 * s+"="+g.PROBK.get(s).pr+";"; } message += "\n";
				 */
				return message;
			}
		}// for each K
		message = "Using bakcward alg, L(A) is empty with threshold x=" + x
				+ " within L=" + L + " \n";
		return message;
	}

	/**
	 * Use the result of bkd(PA g, int qs, long K) to decide emptiness
	 * */

	public String bkdCheckX(PA g, int qs, Fraction x, long K) throws Exception {
		String message = "";
		if (g.BKD_k == null && g.BKD_x == null) {
			// Error in bkd, maybe empty non-convergence
			message = "Exception in backward alg: Integer overflow, maybe Empty Non-Convergence. \n";
			for (WitnessSet Wo : g.WSs) {
				message += Wo.a_successor.get("a").toString() + ";";
			}
			for (String s : g.PROBK.keySet()) {
				message += s + "=" + g.PROBK.get(s).pr + ";";
			}
			message += "\n";
			return message;
		}

		if (g.BKD_x == null) {
			bkd(g, qs, K);
		}
		if (g.BKD_x.compareTo(x) > 0) {// non-empty
			message = "Using backward alg, L(A) is non-empty with threshold x="
					+ x + " at L=" + g.BKD_k + " \n";
			// pity: no tracing back
			return message;
		} else {
			message = "Using bakcward alg, L(A) is empty with threshold x=" + x
					+ " at L=" + g.BKD_k + " \n";
			return message;
		}

	}

	/**
	 * Update BKD_x and BKD_k for g
	 * */
	public void bkd(PA g, int qs, long L) throws Exception {
		if (g.BKD_x != null && g.BKD_k <= L) {
			return;
		}
		if (g.F.isEmpty()) {
			g.BKD_x = Fraction.ZERO;
			g.BKD_k = (long) 0;
			return;
		}

		Fraction xmax = Fraction.MINUS_ONE;
		long k = g.BKD_k != null && g.BKD_k > 1 ? g.BKD_k : 1;
		for (; k <= L; k++) {
			Fraction xt = Fraction.ZERO; // for each K
			for (int ws = 0; ws <= g.WSs.size() - 1; ws++) {
				WitnessSet W = g.WSs.get(ws);
				Fraction probk = g.Prob_k_W(W, k);// calculate and store probk
				if (probk == null) {// Error in bkd, maybe empty non-convergence
					g.BKD_x = null;
					g.BKD_k = null;
					return;
				}
				if (W.q0 == qs && g.WSL1Nodes(ws).isEmpty() // W={qs}, non-empty
						&& probk.compareTo(xt) == 1) {// probk > xt) {
					xt = probk;
				}
			}// for each WS
			if (xt.compareTo(xmax) > 0) {// xt > xmax
				xmax = xt;
				// System.out.println("bkd: xmax updated to "+xmax+" at k = "+k);
			}

			// K++, if every probk of WSs remains unchanged, terminates early.
			// Fixed Point.
			if (k == 1)
				continue;
			int conv = 0;
			for (WitnessSet WS : g.WSs) {
				if (g.PROBK.get(WS.id + "#" + k).pr.compareTo(g.PROBK.get(WS.id
						+ "#" + (k - 1)).pr) != 0)
					continue;
				else
					conv++;
			}
			if (conv == g.WSs.size()) {
				// g.print_WS_indicator_values("p");
				g.BKD_x = xmax;
				g.BKD_k = k;
				return;
			}

		}// for each K
			// g.print_WS_indicator_values("p");
		g.BKD_x = xmax;
		g.BKD_k = L;
		return;
	}
}