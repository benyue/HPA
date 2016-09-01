package HPA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import SCC.ComponentGraph;
import SCC.TarjanSCC;
import Util.io;
import Util.message;
import Util.util;
import edu.princeton.cs.introcs.In;

/**
 * @author Cindy Yue Ben
 */
public class PA {
	public int k = -1; // k-HPA
	public ArrayList<V> V; // list of nodes; nodes are automatically
	// assigned id/value 0,1,...,V-1

	/** initial state */
	public int q0 = -1;
	/** final states, read from data file */
	public HashSet<Integer> F = new HashSet<Integer>();
	/** adj list, out going */
	public HashSet<Integer>[] adj_out_int;
	/** adj list, incoming */
	public HashSet<Integer>[] adj_in_int;
	protected TarjanSCC tjscc = null;

	/**
	 * e.g. for "0 a 2 0.5 3 0.5" record ";0,2,3,",while source is 0 and ends is
	 * {2,3}. source-> conflictEnds for set of ends
	 */
	public ArrayList<Integer> conflictSource = null;
	public ArrayList<ArrayList<Integer>> conflictEnds = null;

	public Set<String> symbols = new HashSet<String>();

	// WS1s stores all finite good WitnessSet_1Q0
	public ArrayList<WitnessSet_1Q0> WS1s = new ArrayList<WitnessSet_1Q0>();
	// WS0s and WS0nodes stores all finite good WitnessSet_0Q0.
	public ArrayList<WitnessSet_0Q0> WS0s =
	        new ArrayList<WitnessSet_0Q0>();
	public ArrayList<HashSet<Integer>> WS0nodes =
	        new ArrayList<HashSet<Integer>>();
	public HashMap<HashSet<Integer>, Integer> indexInWS0nodes =
	        new HashMap<HashSet<Integer>, Integer>();

	/**
	 * Initializes a PA from an input file.
	 * 
	 * @throws Exception
	 */
	public PA(In in, String filetype) throws Exception {
		if (filetype.isEmpty() || filetype.equalsIgnoreCase("txt")
		        || filetype.equalsIgnoreCase("hpa")) {
			PA_constructor_HPA(in);
		} else if (filetype.equalsIgnoreCase("tra")) {
			PA_constructor_PRISM_tra_rows(in);
		} else if (filetype.equalsIgnoreCase("lab")) {
			updatePAusingPRISMlabel(in);
		} else if (filetype.equalsIgnoreCase("sta")) {
			updatePAusingPRISMStates(in);
		}
	}

	/**
	 * Initial a HPA from default format, stored in a plain file or txt file.
	 */
	@SuppressWarnings("unchecked")
	protected void PA_constructor_HPA(In in) throws Exception {
		try {
			String s = io.readNextNonemptyLine(in);
			if (s == null) {// empty file
				throw new Exception("ERROR: empty input file.");
			}
			int NV = Integer.parseInt(s); // V.size()
			if (NV < 0)
				throw new IllegalArgumentException("ERROR: # of vertices < 0");
			V = new ArrayList<V>(NV);
			// NV+1 in case for adding ERROR in determinization
			adj_out_int = (HashSet<Integer>[]) new HashSet[NV];
			adj_in_int = (HashSet<Integer>[]) new HashSet[NV];
			// node line: name and then #propositions
			for (int v = 0; v < NV; v++) {
				// e.g. #FINAL 5 5 is equal to #FINAL 5
				s = io.readNextNonemptyLine(in);
				V n = new V(); // n's id = index in this.V
				if (s.contains("#")) { // separator of propositions
					StringBuffer sb = new StringBuffer(s);
					while (sb.charAt(0) != '#') {
						sb.deleteCharAt(0); // remove name part
					}
					String sp = sb.toString();
					if (sp.contains("#INITIAL") || sp.contains("#initial")
					        || sp.contains("#init") || sp.contains("#INIT")) {
						if (q0 > -1) {
							throw new Exception("ERROR of data file: "
							        + ">1 initial state.");
						}
						q0 = v;
					}
					if (sp.contains("#FINAL") || sp.contains("#final")) {
						F.add(v);
					}
					for (String t : sp.split("#")) {
						if (t.isEmpty())
							continue;
						n.prop.add(t.trim());
					}
				}
				n.name = s.split("#")[0].trim();// warning: redundant names OK
				V.add(n);
				adj_out_int[v] = new HashSet<Integer>();
				adj_in_int[v] = new HashSet<Integer>();
			}
			if (q0 == -1)
				throw new Exception("ERROR of data file: "
				        + "no initial state.");

			// edge line: <int>source id <String>input <int>end <double>pr
			// <int>end
			// <double>pr ...
			// e.g. 0 a 2 2/5 3 3/6
			while (true) {
				s = io.readNextNonemptyLine(in);
				if (s == null) {
					break;
				} // end of file
				int source = -1;
				String input = "";
				int end = -1;
				int valid_i = 0; // the number of valid elements in the line
				Fraction pr_sum = Fraction.ZERO;
				ArrayList<edge> trans = new ArrayList<edge>();
				String[] ssplit = s.split(" ");
				for (String s1 : ssplit) {
					s1 = s1.replaceAll(" ", "");
					if (s1.equals("")) {
						continue;
					}
					valid_i++;
					if (valid_i == 1) {
						source = Integer.parseInt(s1);
						if (source < 0 || source >= NV)
							throw new IndexOutOfBoundsException("ERROR: node "
							        + source + " is not between 0 and "
							        + (NV - 1));
					} else if (valid_i == 2) {
						input = s1;
						if (!V.get(source).input().isEmpty()
						        && V.get(source).input().contains(input)) {
							throw new Exception("ERROR of data file in line "
							        + s
							        + ": all edges extending from the same source node "
							        + "and input should be written in one line");
						}
					} else if (valid_i % 2 == 1) {
						end = Integer.parseInt(s1);
						if (end < 0 || end >= NV)
							throw new IndexOutOfBoundsException("ERROR: node "
							        + end + " is not between 0 and "
							        + (NV - 1));
					} else {
						// obtain probability
						Fraction pr = util.parseFractionPr(s1);
						if (pr == null || pr.compareTo(Fraction.ZERO) <= 0
						        || pr.compareTo(Fraction.ONE) > 0) {
							throw new Exception("ERROR of data file in line "
							        + s
							        + ": invalid pr.");
						}
						pr_sum = pr_sum.add(pr);
						trans.add(new edge(pr, end));
					}
					if (pr_sum.compareTo(Fraction.ONE) == 1) {// >1
						throw new Exception("ERROR in data file " + s
						        + ": sum of pr >1.");

					}
					if (valid_i >= 4// ) {//record at line end if reduced
					        && pr_sum.compareTo(Fraction.ONE) == 0) {
						symbols.add(input);
						T t = new T(input, trans);
						V.get(source).outT.add(t);

						for (edge e : trans) {
							adj_out_int[source].add(e.node);
							adj_in_int[e.node].add(source);
							V.get(e.node).in_symbols.add(input);
							V.get(e.node).in_edges.add(new edge(e.pr, source));
						}

						if (trans.size() > 1) {
							if (this.conflictSource == null)
								this.conflictSource =
								        new ArrayList<Integer>();
							this.conflictSource.add(source);
							if (this.conflictEnds == null)
								this.conflictEnds =
								        new ArrayList<ArrayList<Integer>>();
							this.conflictEnds.add(t.end_nodes());
							// conflict_node_group += ";" + source + ",";
							// for (int e : t.end_nodes()) {
							// conflict_node_group += e + ",";
							// }
						}
					}
				} // for each edge line
				  // if (pr_sum.compareTo(Fraction.ONE) == -1) {// pr_sum < 1
				  // throw new Exception("ERROR in data file " + s
				  // + ": sum of probabilities is less than 1.");
				  // }
			} // end while
		} catch (NoSuchElementException e) {
			throw new Exception("ERROR of data file: Invalid input format.");
		}
		in.close();
		// / Q0 = init_Node();//already done using #propositions
	}

	/**
	 * Initializes a HPA from an .tra file, which is transition matrices output
	 * from PRISM for the model of MDP with Actions.
	 * http://www.prismmodelchecker.org/manual/Appendices/ExplicitModelFiles
	 * 
	 * Command line for trans matrix output in PRISM: "prism #ModelFile
	 * -exportmodel .tra:rows". Format: # of states (n), # of choices (c) = # of
	 * lines, and # of transitions (m) 16 64 72 \newline 0 1:4 a1 \newline 1
	 * 0.666666666667:0 0.333333333333:2 b2
	 */
	@SuppressWarnings("unchecked")
	protected void PA_constructor_PRISM_tra_rows(In in) throws Exception {
		// : rows
		String s = io.readNextNonemptyLine(in);
		if (s == null) {// empty file
			throw new Exception("ERROR: empty .tra file.");
		}
		List<Integer> NCM = util.parsePosIntegers2list(s);
		int NV = NCM.get(0);
		V = new ArrayList<V>(NV);
		// NV+1 in case for adding ERROR in determinization
		adj_out_int = (HashSet<Integer>[]) new HashSet[NV];
		adj_in_int = (HashSet<Integer>[]) new HashSet[NV];
		for (int v = 0; v < NV; v++) {// states:0~NV-1
			V n = new V();// n.id = v
			n.name = String.valueOf(v);
			V.add(n);
			adj_out_int[v] = new HashSet<Integer>();
			adj_in_int[v] = new HashSet<Integer>();
		}
		// edge line: <int>source <double>pr:<int>end <double>pr:<int>end
		// ... <String>input
		// e.g. 0 1:4 a1
		// e.g. 13 0.666666666667:12 0.333333333333:14 b2
		// e.g. 3 1:3 //deadlock
		while (true) {
			s = io.readNextNonemptyLine(in);
			if (s == null) {
				break;
			} // end of file
			if (!s.contains(":")) {
				continue;
			}
			int source = -1;
			String input = "";
			int end = -1;
			int valid_i = 0;
			Fraction pr_sum = Fraction.ZERO;
			ArrayList<edge> trans = new ArrayList<edge>();
			for (String s1 : s.split(" ")) {
				s1 = s1.replaceAll(" ", "");
				if (s1.equals("")) {
					continue;
				}
				valid_i++;
				if (valid_i == 1) {// source
					source = Integer.parseInt(s1);
				} else if (s1.contains(":")) {// pr and end
					Fraction pr = util.parseFractionPr(s1.split(":")[0]);
					if (pr == null || pr.compareTo(Fraction.ZERO) <= 0
					        || pr.compareTo(Fraction.ONE) > 0) {
						throw new Exception("Error pr in line " + s + "\n");
					}
					end = Integer.parseInt(s1.split(":")[1]);
					pr_sum = pr_sum.add(pr);
					trans.add(new edge(pr, end));
				} else {// input, or action
					input = s1;
				}
			}
			if (pr_sum.compareTo(Fraction.ONE) != 0) {
				throw new Exception("Error in pr_sum in line " + s + "\n");
			}
			if (input == null || input.isEmpty()) {// dead end
				continue;
			}
			// duplicate edge generated in PRISM
			if (V.get(source).input().contains(input)) {
				continue;
			}
			symbols.add(input);
			T t = new T(input, trans);
			V.get(source).outT.add(t);
			for (edge e : trans) {
				adj_out_int[source].add(e.node);
				adj_in_int[e.node].add(source);
				V.get(e.node).in_symbols.add(input);
				V.get(e.node).in_edges.add(new edge(e.pr, source));
			}

			if (trans.size() > 1) {
				if (this.conflictSource == null)
					this.conflictSource =
					        new ArrayList<Integer>();
				this.conflictSource.add(source);
				if (this.conflictEnds == null)
					this.conflictEnds =
					        new ArrayList<ArrayList<Integer>>();
				this.conflictEnds.add(t.end_nodes());
			}
		}
		in.close();
		// init_Node();//if commentedd out, updatePAusingPRISMlabel is necessary
		// to identify q0 using prop
	}

	/**
	 * Update PA using PRISM label file in the format of .lab files: add state
	 * propositions; update initial and final.
	 * 
	 * @param in
	 */
	public boolean updatePAusingPRISMlabel(In in) throws Exception {
		if (V == null || V.size() <= 0) {
			throw new Exception(
			        "WARNING: empty PA, nothing updated."
			                + " Please load valid PA first.");
			// return false;
		}
		String s = io.readNextNonemptyLine(in);
		if (s == null | !s.contains("=\"")) {// empty file
			throw new Exception("WARNING: empty lable file, nothing updated.");
			// return false;
		}
		/* obtain labels */
		// e.g. 0="init" 1="deadlock" 2="final"
		// Integer="String"
		HashMap<Integer, String> labels = new HashMap<Integer, String>();
		int key = -1;
		String value = null;// wrapped in quotation marks ("...")
		while (s.length() > 0) {
			while (s.charAt(0) == ' ') {
				s = s.substring(1, s.length());
			}
			int i = s.indexOf("=");
			try {
				key = Integer.parseInt(s.substring(0, i));
			} catch (NumberFormatException e) {
				return false;
			}
			if (i + 2 < s.length())
				s = s.substring(i + 2, s.length());
			else
				return false;
			i = s.indexOf("\"");// right \"
			value = s.substring(0, i);
			labels.put(key, value);
			if (i + 1 <= s.length() - 1)
				s = s.substring(i + 1, s.length());
			else
				break;
		}
		/* assign labels */
		// e.g. 0: 0 \newline 3: 2 \newline 13: 2 5 6
		s = io.readNextNonemptyLine(in);
		while (s != null) {
			int sid = Integer.parseInt(s.split(":")[0]);
			if (sid < V.size()) {
				List<Integer> ps = util.parsePosIntegers2list(s.split(":")[1]);
				for (Integer p : ps) {
					String prop = labels.get(p);
					if (prop == null) {
						s = io.readNextNonemptyLine(in);
						continue;
					}
					V.get(sid).prop.add(prop);
					if (prop.equalsIgnoreCase("INITIAL")
					        | prop.equalsIgnoreCase("INIT")) {
						if (q0 > -1) {
							throw new Exception("ERROR of data file: "
							        + ">1 initial state.");
						}
						q0 = sid;
					} else if (prop.equalsIgnoreCase("FINAL")) {
						F.add(sid);
					}
				}
			}
			s = io.readNextNonemptyLine(in);
		}
		// update final states: final states' outgoing states are final states
		if (q0 == -1)
			throw new Exception("ERROR of data file: "
			        + "no initial state.");
		return true;
	}

	/**
	 * Update PA using PRISM states file in the format of .sta files: update
	 * states' names.
	 * 
	 * @throws Exception
	 */
	public boolean updatePAusingPRISMStates(In in) throws Exception {
		if (V == null || V.size() <= 0) {
			throw new Exception(
			        "WARNING: empty PA, nothing updated."
			                + " Please load valid PA first.");
			// return false;
		}

		/* read and rename states */
		/* e.g. (x1,f1,x2,x1p) \n 0:(0,false,0,0) */
		String s = io.readNextNonemptyLine(in);
		while (s != null) {
			if (s.contains(":")) {
				int sid = Integer.parseInt(s.split(":")[0]);
				if (sid < V.size()) {
					String name = s.split(":")[1];
					if (name != null) {
						V.get(sid).name = name;
					}
				}
			}
			s = io.readNextNonemptyLine(in);
		}
		return true;
	}

	/**
	 * Do validation, reduction and leveling for the PA. Return message.
	 * 
	 * @throws Exception
	 */
	public message HPAinitial(boolean moreQ1) throws Exception {
		message m = new message();
		if (q0 == -1) {
			m.ErrorMessage = "ERROR: no q0";
			return m;
		}
		if (F.isEmpty()) {
			m.ErrorMessage = ("ERROR: no F. Robustness=1.");
			return m;
		}

		/* Compute SCCs in the DG */
		tjscc = new TarjanSCC(V.size());
		int visited = tjscc.dfs(adj_out_int, q0);
		if (visited < this.V.size()) {
			m.ErrorMessage = ("ERROR: reachability unsatisfied.");
			// unreachable states has default id[] value of 0,
			// error in leveling if not stop here
			return m;
		}
		tjscc.orderId();// O(n), easier to debug

		/* Decide if a DG is HPA */
		try {
			int isHPA = isHPA(tjscc.id);
			if (isHPA != -1) {
				m.ErrorMessage = ("ERROR: not HPA, conflict at "
				        + this.conflictSource.get(isHPA) + ":"
				        + this.conflictEnds.get(isHPA).toString()
				        + "(source:end1,end2,...).\n");
				return m;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		/* assign level to the SCCS, then each node */
		ComponentGraph CG = new ComponentGraph(this, tjscc);// size M
		int k = CG.leveling(tjscc.id, this, moreQ1);
		this.k = k;
		if (k > 1) {
			m.ErrorMessage = ("ERROR: " + k + "-HPA, undecidable.\n");
			return m;
		}

		/*
		 * reduce();
		 * if (F.isEmpty()) {
		 * m.ErrorMessage = ("ERROR: no F after reduction. Robustness=1.\n");
		 * return m;
		 * }
		 * if (q0 == -1) {
		 * m.ErrorMessage = ("ERROR: no q0 after reduction.\n");
		 * return m;
		 * }
		 */
		return m;
	}
	/**
	 * Reduce PA. TODO: reduce must be done after leveling. CE: test 8 more Q1.
	 * need to fix loading reduced hpa, identify when a T line ends
	 * conflict with if (valid_i >= 4//) {//record after reading the end
	 * && pr_sum.compareTo(Fraction.ONE) == 0) {
	 * 
	 * @param tjscc
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected void reduce() throws Exception {
		HashSet<Integer> idleV = new HashSet<Integer>();// V to remove
		// unreachable from q0 //done in HPAinitial
		// not on a path between q0 and a final state
		TarjanSCC tj = new TarjanSCC(V.size());
		tj.dfs_set(adj_in_int, F);
		for (int i = 0; i < V.size(); ++i) {
			if (tj.marked[i] || idleV.contains(i))
				continue;
			idleV.add(i);
		}
		F.removeAll(idleV);
		if (F.isEmpty())
			return;
		if (idleV.contains(q0)) {
			q0 = -1;
			return;
		}
		if (idleV.size() == 0 || idleV.size() == V.size())
			return;

		int NV = V.size() - idleV.size();
		HashMap<Integer, Integer> old2new = new HashMap<Integer, Integer>(NV);
		int skip = 0;
		for (int i = 0; i < V.size(); ++i) {
			if (idleV.contains(i)) {
				++skip;
				continue;
			}
			old2new.put(i, i - skip);
			V.set(i - skip, V.get(i));
			adj_out_int[i - skip] = adj_out_int[i];
			adj_in_int[i - skip] = adj_in_int[i];
		}
		if (old2new.get(q0) != null)
			q0 = old2new.get(q0);
		HashSet<Integer> newF = new HashSet<Integer>(F.size());
		for (Integer f : F) {
			newF.add(old2new.get(f));
		}
		F = newF;
		int oldNV = V.size();
		for (int i = oldNV - 1; i >= NV; --i) {
			V.remove(i);
		}
		HashSet<Integer>[] adj_out_new =
		        (HashSet<Integer>[]) new HashSet[NV];
		HashSet<Integer>[] adj_in_new =
		        (HashSet<Integer>[]) new HashSet[NV];
		for (int i = 0; i < NV; ++i) {
			V.get(i).in_edges.clear();
			V.get(i).in_symbols.clear();
			adj_out_new[i] = new HashSet<Integer>();
			for (int j : adj_out_int[i]) {
				if (old2new.get(j) == null)
					continue;
				adj_out_new[i].add(old2new.get(j));
			}
			adj_in_new[i] = new HashSet<Integer>();
			for (int j : adj_in_int[i]) {
				if (old2new.get(j) == null)
					continue;
				adj_in_new[i].add(old2new.get(j));
			}
		}
		adj_out_int = adj_out_new;
		adj_in_int = adj_in_new;

		this.conflictSource = null;
		this.conflictEnds = null;
		this.symbols.clear();
		for (int i = 0; i < V.size(); ++i) {// V.size() = NV
			V n = V.get(i);
			Iterator<T> itT = n.outT.iterator();
			while (itT.hasNext()) {
				T t = itT.next();
				Iterator<edge> it = t.dist.iterator();
				while (it.hasNext()) {
					edge e = it.next();
					if (old2new.get(e.node) == null) {
						it.remove();
					} else {
						e.node = old2new.get(e.node);
						V.get(e.node).in_symbols.add(t.input);
						V.get(e.node).in_edges.add(new edge(e.pr, i));
						symbols.add(t.input);
					}
				}
				if (t.dist.size() > 1) {// conflict group e.g.;0,2,3,
					if (this.conflictSource == null)
						this.conflictSource = new ArrayList<Integer>();
					this.conflictSource.add(i);
					if (this.conflictEnds == null)
						this.conflictEnds = new ArrayList<ArrayList<Integer>>();
					this.conflictEnds.add(t.end_nodes());
				} else if (t.dist.size() == 0) {
					itT.remove();
				}
			}
		}
	}

	/**
	 * Return the initial state of G, decided by in-degrees of each state. If a
	 * state has no incoming edges, it's the initial state. If no q0 or >1 q0
	 * defined, assign no q0.
	 */
	@SuppressWarnings("unused")
	private void init_Node() {
		ArrayList<Integer> inis = new ArrayList<Integer>(V.size());
		for (int i = 0; i < adj_in_int.length; i++) {
			if (adj_in_int[i].size() == 0) {
				inis.add(i);
			}
		}
		if (inis.size() == 1)
			q0 = inis.get(0);
	}

	/**
	 * Determinize a PA if it's not.
	 * Add an ERROR state, and then for each state and undefined input, add an
	 * edge to ERROR state.
	 */
	@SuppressWarnings("unused")
	private boolean isDeterministic() throws Exception {
		int n = V.size();
		for (int i = 0; i < n; ++i) {
			V s = V.get(i);
			if (s.outT.size() == symbols.size()) // deterministic
				continue;
			return false;
		}
		return true;
	}

	/**
	 * Determinize a PA if it's not.
	 * Add an ERROR state, and then for each state and undefined input, add an
	 * edge to ERROR state.
	 */
	@SuppressWarnings("unused")
	private boolean Determinize() throws Exception {
		int ie = -1; // index of ERROR state
		for (int i = 0; i < V.size(); ++i) {
			V n = V.get(i);
			if (n.name.equalsIgnoreCase("ERROR")
			        || n.prop.contains("deadlock")) {
				ie = i;
				break;
			}
		}
		V ne = null;
		boolean res = true; // true if deterministic
		int n = V.size();
		for (int i = 0; i < n; ++i) {
			V s = V.get(i);
			if (s.outT.size() == symbols.size()) // deterministic
				continue;
			// non-deterministic
			if (res) {
				res = false;
				if (ie == -1) {
					ie = V.size();
					ne = new V("ERROR");
					ne.prop.add("ERROR");
					ne.prop.add("deadlock");
					adj_out_int[ie] = new HashSet<Integer>();
					adj_in_int[ie] = new HashSet<Integer>();
					V.add(ne);
					++n;
				} else {
					ne = this.V.get(ie);
				}
			}
			for (String input : util.difference(symbols, s.input())) {
				edge e = new edge(Fraction.ONE, ie);
				ArrayList<edge> dist = new ArrayList<edge>(1);
				dist.add(e);
				T t = new T(input, dist);
				s.outT.add(t);
				adj_out_int[i].add(ie);
				adj_in_int[ie].add(i);
				ne.in_symbols.add(input);
				ne.in_edges.add(new edge(Fraction.ONE, i));
			}
		}
		return res;
	}

	/**
	 * Return the set of level level states of the graph
	 */
	public Set<Integer> obtainLevel(int level) {
		Set<Integer> L = new HashSet<Integer>();
		for (int i = 0; i < V.size(); i++) {
			if (V.get(i).level == level) {
				L.add(i);
			}
		}
		return L;
	}

	/**
	 * Return the set of states in set S from a specified level as a new set.
	 * 
	 * @param level
	 *            : the specified level
	 * @param S
	 *            : the data source as a set
	 */
	public HashSet<Integer> obtainSetOnLevel(Set<Integer> S, int level) {
		HashSet<Integer> L = new HashSet<Integer>();
		for (int i : S) {
			V v = V.get(i);
			if (v.level == level) {
				L.add(i);
			}
		}
		return L;
	}

	/**
	 * Return the set of states reached from state q with input symbol a with
	 * probabilities >0.
	 */
	Set<Integer> postQ(int q, String a) {
		return V.get(q).post_nodes(a);
	}

	/**
	 * Return the set of states reached from any state in set W on input a with
	 * probabilities >0.
	 */
	Set<Integer> post(Set<Integer> S, String a) {
		Set<Integer> s = new HashSet<Integer>();
		for (Integer q : S) {
			s.addAll(postQ(q, a));
		}
		return s;
	}

	/**
	 * Return the set of states reached from any state in a WS1 on input a with
	 * probabilities >0.
	 */
	Set<Integer> postWS1(WitnessSet_1Q0 WS1, String a) {
		return postWS1(WS1.id, a);
	}

	/**
	 * Return the set of states reached from any state in a WS1 on input a as a
	 * new set.
	 */
	Set<Integer> postWS1(int WS1id, String a) {
		Set<Integer> s = new HashSet<Integer>();
		for (int q : WS0nodes.get(WS1s.get(WS1id).WS0id)) {
			s.addAll(postQ(q, a));
		}
		s.addAll(postQ(WS1s.get(WS1id).q0, a));
		return s;
	}

	/**
	 * Return the set of states reached from state q with input string u with
	 * probabilities >0.
	 */
	Set<Integer> post(int q, ArrayList<String> u) {
		Set<Integer> p = new HashSet<Integer>();
		RealMatrix U = u_matrix(u);
		if (U == null)
			return null;
		for (int j = 0; j < V.size(); j++) {
			if (U.getEntry(q, j) > 0)
				p.add(j);
		}
		return p;
	}

	/**
	 * Return the next state distribution from state q with input symbol a
	 */
	ArrayList<edge> post_dist(int q, String a) {
		return V.get(q).post_dist(a);
	}

	/**
	 * Return the probability that starting from q, after reading u, the final
	 * state is in Q.
	 */
	public double probQ(int q, ArrayList<String> u, Set<Integer> Q) {
		double pr = 0;
		double[] q_u = u_matrix(u).getRow(q);
		for (int i = 0; i < q_u.length; i++) {
			if (Q.contains(i))
				pr = pr + q_u[i];
		}
		return pr;
	}

	/**
	 * Return the pr. {starting from q, after reading a, the next state is in
	 * Q}.
	 */
	public double probQa(int q, String a, Set<Integer> Q) {
		double pr = 0;
		double[] q_a = a_matrix(a).getRow(q); // /O(m)
		for (int i = 0; i < q_a.length; i++) { // /O(n)
			if (Q.contains(i))
				pr = pr + q_a[i];
		}
		return pr;
	}

	/**
	 * Return the pr. {starting from q, after reading a, the next state is in
	 * Q}.
	 */
	public double probQa(int q, RealMatrix A, Set<Integer> Q) {
		double pr = 0;
		double[] q_a = A.getRow(q); // /O(m)
		for (int i = 0; i < q_a.length; i++) { // /O(n)
			if (Q.contains(i))
				pr = pr + q_a[i];
		}
		return pr;
	}

	/**
	 * Return True if there is at least a run of G accepting u with probability
	 * greater than x, Starting from the initial node
	 */
	public boolean acceptWord(ArrayList<String> u, double x) {
		return probQ(q0, u, F) >= x;
	}

	/**
	 * Return the one-step transition matrix of G on input symbol a; Return null
	 * if G is empty, or a is not acceptable to this G. Time: O(number of
	 * transitions in G) = O(m)
	 */
	public RealMatrix a_matrix(String a) {
		int NV = V.size();
		if (NV <= 0)
			return null;
		if (!symbols.contains(a))
			return null;
		RealMatrix A = MatrixUtils.createRealMatrix(NV, NV);
		for (int i = 0; i < V.size(); ++i) {
			V n = V.get(i);
			if (n.post_dist(a) == null) { // n does not accept a
				continue;
			}
			for (edge d : n.post_dist(a)) {
				A.setEntry(i, d.node, d.pr.doubleValue());
			}
		}
		return A;
	}

	/**
	 * Return the transition matrix of G on input string u; Return null if u is
	 * empty.
	 */
	public RealMatrix u_matrix(ArrayList<String> u) {
		if (u.isEmpty())
			return null;
		RealMatrix U = a_matrix(u.get(0));
		for (int i = 1; i < u.size(); i++) {
			RealMatrix A = a_matrix(u.get(i));
			U = U.multiply(A);
		}
		return U;
	}

	/**
	 * Detect if the given graph is HPA. If there exits transitions 1-a-2 and
	 * 1-a-3, at most one of 2 and 3 can be strongly connected with 1 in a HPA.
	 * 
	 * @param id: obtained from calling TarjanSCC_q0 for g.
	 * @return Return -1 (true) if given PA is HPA; else return the
	 *         conflict group index.
	 */
	protected int isHPA(int[] id) {
		if (this.conflictSource == null || this.conflictSource.isEmpty())
			return -1;// true

		for (int i = 0; i < this.conflictSource.size(); ++i) {
			boolean sc = false; // record if there is already one end on the
			                    // same level w/ source
			int source = this.conflictSource.get(i);
			for (int end : this.conflictEnds.get(i)) {
				if (id[source] == id[end]) {
					if (sc)
						return i;// false
					sc = true;
				}
			}
		}
		return -1;// true
	}

	/**
	 * Obtain finite good WS0s by traversing backward from a known good witness
	 * set S on level 1.
	 */
	protected void obtainWS0sByTraversingBack() throws Exception {
		int idx = 0;
		while (idx < WS0s.size()) {
			traverseBackWS0(idx);
			++idx;
		}
	}

	/**
	 * Traverse back a WS0 to get other WS0s.
	 * 
	 * @param idx: index in WS0s and WS0nodes.
	 */
	private void traverseBackWS0(int idx) {
		WitnessSet_0Q0 SW = WS0s.get(idx);
		for (String a : symbols) {
			ArrayList<HashSet<Integer>> t = pre_a(WS0nodes.get(idx), a);
			if (!SW.superQ0.isEmpty()) {
				ArrayList<HashSet<Integer>> t0 = pre_a(SW.superQ0, a);
				t.get(0).addAll(t0.get(0));// pr=1 Q0, super good WS1
				t.get(2).addAll(t0.get(2));// pr<1 Q0, good WS1
			}
			if (!SW.goodQ0.isEmpty()) {
				ArrayList<HashSet<Integer>> t0 = pre_a(SW.goodQ0, a);
				// from goodQ0, get only goodQ0
				t.get(2).addAll(t0.get(0));
				t.get(2).addAll(t0.get(2));
			}
			int in = addOrFindWS0(t.get(1));// pr=1 Q1, (super) good WS0
			WS0s.get(idx).inWS0.put(a, in);// for waq for FWD

			t.get(0).removeAll(WS0s.get(in).superQ0);
			t.get(2).removeAll(WS0s.get(in).goodQ0);
			if (!t.get(0).isEmpty() || !t.get(2).isEmpty()) {
				WS0s.get(in).superQ0.addAll(t.get(0));
				WS0s.get(in).goodQ0.addAll(t.get(2));
				if (in <= idx) {
					traverseBackWS0(in);// scanned WS0 changed, rescan
				}
			}
		} // for a
	}

	/**
	 * Return three sets of states which are a-predecessor of the set S of
	 * states: set 1 on Q0 with pr=1; set 2 on Q1 with pr=1; set 3 on Q0 with
	 * 0<pr<1. Note pr-1 super good; pr<1 Q0 and pr-1 Q1, still good.
	 */
	private ArrayList<HashSet<Integer>> pre_a(Set<Integer> S, String a) {
		ArrayList<HashSet<Integer>> res = new ArrayList<HashSet<Integer>>(2);
		res.add(new HashSet<Integer>());// pre on level 0 with pr=1
		res.add(new HashSet<Integer>());// pre on level 1 with pr=1
		res.add(new HashSet<Integer>());// pre on level 0 with pr<1
		for (int s : S) {
			V n = V.get(s);
			for (int i = 0; i < n.in_edges.size(); i++) {
				if (!n.in_symbols.get(i).equals(a))
					continue;
				edge e = n.in_edges.get(i);
				if (e.pr.compareTo(Fraction.ONE) == 0) {// pr = 1
					int k = V.get(e.node).level;
					res.get(k).add(e.node);
				} else if (e.pr.compareTo(Fraction.ZERO) > 0) {// 0<pr<1
					if (V.get(e.node).level == 0)// Q0 only
						res.get(2).add(e.node);
				}
			}
		}
		return res;
	}

	/**
	 * Add a WS0 if WS0nodes is not already recorded. If it exists, add all
	 * new superQ0 nodes. Return its index.
	 * 
	 * @param Q1: a set of Q1 nodes constructing a good WS0
	 * @param superQ0: each superQ0 plus Q1 is a super good WS1
	 */
	public int addOrFindWS0(HashSet<Integer> Q1, Set<Integer> superQ0) {
		Integer id = indexInWS0nodes.get(Q1);
		if (id != null) { // existing ws
			if (superQ0 != null && !superQ0.isEmpty()) {
				WS0s.get(id).superQ0.addAll(superQ0);
			}
			return id;
		}
		id = WS0s.size();
		WS0nodes.add(Q1);
		WS0s.add(new WitnessSet_0Q0(id, superQ0));
		indexInWS0nodes.put(Q1, id);
		return id;
	}

	/**
	 * Add a WS0 if WS0nodes not already recorded. Return its index. superQ0
	 * nodes are not taken care of.
	 */
	public int addOrFindWS0(HashSet<Integer> Q1) {
		Integer id = indexInWS0nodes.get(Q1);
		if (id != null) { // existing ws
			return id;
		}
		id = WS0s.size();
		WS0nodes.add(Q1);
		WS0s.add(new WitnessSet_0Q0(id));
		indexInWS0nodes.put(Q1, id);
		return id;
	}

	/**
	 * Obtain finite Good WS1Q0s by adding each Q0 state to each good WS0Q0s.
	 */
	protected void obtainWS1s() throws Exception {
		for (int WS0id = 0; WS0id < WS0s.size(); ++WS0id) {
			for (Integer q : WS0s.get(WS0id).superQ0) {
				/// obtainLevel(0)) {//alternative: good WS0 plus each Q0
				this.addWS1(q, WS0id, true);// super good only
			}
			for (Integer q : WS0s.get(WS0id).goodQ0) {
				this.addWS1(q, WS0id, false);// good on Q1
			}
			// ensure {q0} is a good witness set
			if (this.WS0nodes.get(WS0id).isEmpty()
			        && !WS0s.get(WS0id).superQ0.contains(this.q0)
			        && !WS0s.get(WS0id).goodQ0.contains(this.q0)) {
				this.addWS1(this.q0, WS0id);
			}
		}
	}

	/**
	 * Find a WS1 or add one if it's not existing. Return its index.
	 */
	public int addOrFindWS1(Integer q0, int WS0id) {
		for (WitnessSet_1Q0 WS1 : this.WS1s) {
			if (WS1.q0 == q0 && WS1.WS0id == WS0id)
				return WS1.id;
		}
		this.addWS1(q0, WS0id);
		return this.WS1s.size() - 1;
	}

	/**
	 * Force add a WS1 without checking if it already exists.
	 */
	private void addWS1(Integer q0, int WS0id, boolean isSuperGood) {
		int i = WS1s.size();
		WS1s.add(new WitnessSet_1Q0(i, q0, WS0id, isSuperGood));
	}

	/**
	 * Force add a WS1 without checking if it already exists.
	 */
	private void addWS1(Integer q0, int WS0id) {
		int i = WS1s.size();
		if (WS0s.get(WS0id).superQ0.contains(q0))
			WS1s.add(new WitnessSet_1Q0(i, q0, WS0id, true));
		else
			WS1s.add(new WitnessSet_1Q0(i, q0, WS0id, false));
	}

	/**
	 * L = 4rn8^n
	 */
	public long L() {
		long n = V.size();
		long R = 1;
		for (V s : V) {
			for (T t : s.outT) {
				for (edge e : t.dist) {
					// numerator <= denominator for a valid Pr
					R = e.pr.getNumerator() > R ? e.pr.getNumerator() : R;
					R = e.pr.getDenominator() > R ? e.pr.getDenominator() : R;
				}
			}
		}
		long r = (int) (Math.log(R) / Math.log(2) + 1e-10);
		return (long) (4 * r * n * Math.pow(8, n));
	}

	/**
	 * Obtain good finite witness sets for 1-HPA
	 * 
	 * @throws Exception
	 */
	public void witnessSets() throws Exception {
		addOrFindWS0(new HashSet<Integer>(), obtainSetOnLevel(F, 0)); // empty
		// in traverse back, bkd-accessible from F0 w pr=1 will add to F0
		HashSet<Integer> F1 = new HashSet<Integer>(F.size()),
		        F0 = new HashSet<Integer>(F.size());
		for (int i : F) {
			V v = V.get(i);
			if (v.level == 1) {
				F1.add(i);
			} else if (v.level == 0) {
				F0.add(i);
			}
		}
		addOrFindWS0(F1, F0);// add F
		obtainWS0sByTraversingBack();
		obtainWS1s();
	}

	/**
	 * Clear/Reset all witness sets nodes and pointers.
	 */
	public void resetWS() {
		this.WS0s.clear();
		this.WS1s.clear();
		this.WS0nodes.clear();
		this.indexInWS0nodes.clear();
	}

	/**
	 * Write the PA to file, in standard HPA format.
	 * 
	 * @param file
	 *            Specified destination file.
	 * @param comment
	 *            Will be written as comment in output file.
	 */
	public void writePA2HPA(String filename, String comment)
	        throws IOException {
		File file = new File(filename + ".hpa");
		BufferedWriter o = new BufferedWriter(new FileWriter(file));
		if (comment != null && !comment.isEmpty())
			o.write("//" + comment + "\n");
		o.write(V.size() + "\n");
		// states
		o.write("//States.size()=" + V.size() + "\n");
		o.write("//state line = state.name + state.propositions\n");
		for (V s : V) {
			o.write(s.name + " ");
			for (String p : s.prop) {
				o.write("#" + p + " ");
			}
			o.write("\n");
		}
		// transitions
		o.write("//note transition line is using state ids (not names)\n");
		int TSize = 0;
		for (int i = 0; i < V.size(); ++i) {
			V s = V.get(i);
			for (T t : s.outT) {
				o.write(i + " ");
				String input = t.input;
				o.write(input + " ");
				for (edge e : t.dist) {
					o.write(e.node + " "
					        + e.pr.toString().replaceAll(" ", "") + " ");
					TSize++;
				}
				o.write("\n");
			}

		}
		o.write("//Transitions.size()=" + TSize + "\n");

		o.close();

	}

	/**
	 * Write the PA to file, in FAT tool format.
	 * http://cl-informatik.uibk.ac.at/software/fat/grammar.php
	 * 
	 * @throws IOException
	 * 
	 * @param filename
	 *            Specified file name.
	 * @param comment
	 *            Will be written as comment in output file.
	 */
	public void writePA2FAT(String filename, String comment)
	        throws IOException {
		File file = new File(filename + ".fat");
		BufferedWriter o = new BufferedWriter(new FileWriter(file));
		if (comment != null && !comment.isEmpty())
			o.write("//" + comment + "\n");
		o.write("NFA hpa = {\n");
		// states
		o.write("states = {");
		for (int i = 0; i < V.size(); ++i) {
			// V s = this.V.get(i);
			// o.write(s.name + " ");
			o.write("q" + i + " ");// add "q" to avoid bug in FAT tool
		}
		o.write("}\n");
		o.write("//States.size()=" + V.size() + "\n");

		HashSet<String> alphabet = new HashSet<String>();
		// transitions
		o.write("transitions = {\n");
		int TSize = 0;
		for (int i = 0; i < V.size(); ++i) {
			V s = this.V.get(i);
			for (T t : s.outT) {
				for (edge e : t.dist) {
					// String in = t.input;
					String in = !e.pr.equals(Fraction.ONE)
					        ? t.input + "000" + e.pr.getNumerator()
					                + "d" + e.pr.getDenominator()
					        : t.input;
					alphabet.add(in);
					o.write("q" + i + "-" + in + "->" + "q" + e.node);
					TSize++;
					o.write("\n");
				}
			}
		}
		o.write("}\n");
		o.write("//Transitions.size()=" + TSize + "\n");
		// alphabet
		o.write("alphabet = {");
		for (String s : alphabet) {
			o.write(s + " ");
		}
		o.write("}\n");
		// initial state
		o.write("initial state = ");
		o.write("q" + q0);// (V.get(q0).name);
		o.write("\n");
		// final states
		o.write("final states = {");
		for (int i : F) {
			o.write("q" + i + " ");
			// o.write(V.get(i).name + " ");
		}
		o.write("}\n");

		o.write("}\n");
		o.close();
		// System.out.println(name + " written to file.");
	}

	/**
	 * Output the whole PA to plain file.
	 * 
	 * @throws IOException
	 * 
	 * @param filename
	 *            Specified file name.
	 * @param comment
	 *            Will be written as comment in output file.
	 */
	public void writeHPA2file(String filename, String comment)
	        throws IOException {
		File file = new File(filename + "_plain");
		BufferedWriter o = new BufferedWriter(new FileWriter(file));
		if (comment != null && !comment.isEmpty())
			o.write("//" + comment + "\n");
		o.write(V.size() + "\n");
		o.write("Initial state: " + q0 + ";\n" + "Final states: "
		        + F.toString() + "\n");
		o.write("Symbols (" + symbols.size() + " totally):\n" + symbols + "\n");
		o.write("States (" + V.size() + " totally):\n");

		// states
		for (int i = 0; i < V.size(); ++i) {
			V s = V.get(i);
			o.write("State id=" + i + ": name=" + s.name + "; in-states="
			        + adj_in_int[i] + "; out-states=" + adj_out_int[i]
			        + "; Props=");
			for (String p : s.prop) {
				o.write("#" + p + " ");
			}
			o.write("\n");
		}
		// transitions
		o.write("Transitions:\n");
		int TSize = 0;
		for (int i = 0; i < V.size(); ++i) {
			V s = V.get(i);
			for (T t : s.outT) {
				o.write(i + " ");
				String input = t.input;
				o.write(input + " ");
				for (edge e : t.dist) {
					o.write(e.node + " "
					        + e.pr.toString().replaceAll(" ", "") + " ");
					TSize++;
				}
				o.write("\n");
			}

		}
		o.write("Transitions (" + TSize + " totally).\n");
		o.close();
		// System.out.println(name + " written to file.");
	}

	public void writeWS2file(String filename, StringBuilder m)
	        throws IOException {
		this.writeWS2file(filename, m.toString());
	}

	/**
	 * Output an 1-HPA's good witness sets to .ws file.
	 * 
	 * @throws IOException
	 * 
	 * @param filename: Specified file name.
	 * @param comment: as comment in output file.
	 */
	@SuppressWarnings("rawtypes")
	public void writeWS2file(String filename, String comment)
	        throws IOException {
		File file = new File(filename + ".ws");
		BufferedWriter o = new BufferedWriter(new FileWriter(file));
		if (comment != null && !comment.isEmpty())
			o.write("//" + comment + "\n");
		o.write("//WS0 size, WS1 size\n");
		o.write(WS0s.size() + "," + WS1s.size() + "\n");
		o.write("//WS0: [nodes][superQ0][goodQ0]in_symbol:in_WS0\n");
		for (WitnessSet_0Q0 w : WS0s) {
			o.write("" + WS0nodes.get(w.id) + w.superQ0 + w.goodQ0);
			for (Map.Entry e : w.inWS0.entrySet()) {
				o.write(e.getKey() + ":" + e.getValue() + ",");
			}
			o.write("\n");
		}
		o.write("//WS1: q0,WS0id,isSuperGood(1 for true and 0 for false)\n");
		for (WitnessSet_1Q0 w : WS1s) {
			o.write(w.q0 + "," + w.WS0id + ",");
			if (w.isSuperGoodWS1)
				o.write('1');
			else
				o.write('0');
			o.write("\n");
		}
		o.close();
	}

	/**
	 * Return the basic info of the 0/1-HPA as a StringBuffer.
	 */
	public StringBuffer basicInfo() {
		StringBuffer m = new StringBuffer();
		m.append("PA is a " + k + "-HPA with " + V.size()
		        + " states.\n");
		m.append("q0 = " + q0 + "\n");
		m.append("F(size " + F.size() + ")= " + F + "\n");
		m.append("Alphabet(size " + symbols.size() + ")= " + symbols
		        + "\n");
		HashSet<Integer> F0 = obtainSetOnLevel(F, 0),
		        F1 = obtainSetOnLevel(F, 1);
		m.append(" F0(size ").append(F0.size()).append(")= ")
		        .append(F0.toString()).append(";\n").append(" F1(size ")
		        .append(F1.size()).append(")= F-F0.\n");
		int remainingNodes = V.size();
		for (int i = 0; i < k; i++) {
			Set<Integer> temp = obtainLevel(i);
			m.append("Q" + i + "(" + temp.size() + ")= " + temp + "\n");
			remainingNodes -= temp.size();
		}
		m.append("Q").append(k).append("(size ").append(remainingNodes)
		        .append(")= all others.\n");
		return m;
	}

	StringBuffer WS1nodes(int id) {
		if (id < 0 || id >= this.WS1s.size())
			return null;
		StringBuffer sb = new StringBuffer();
		sb.append(WS1s.get(id).q0);
		if (!WS0nodes.get(WS1s.get(id).WS0id).isEmpty()) {
			sb.append(",");
			sb.append(WS0nodes.get(WS1s.get(id).WS0id).toString());
		}
		return sb;
	}

}