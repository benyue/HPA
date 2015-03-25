package HPA;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import Util.util;
import edu.princeton.cs.introcs.In;

public class PA {

	public ArrayList<HPANode> V; // list of nodes; nodes are automatically
	// assigned id/value 0,1,...,V-1
	public Set<Integer> Q0 = new HashSet<Integer>(); // initial states;
														// automatically
	// recognized
	public Set<Integer> F = new HashSet<Integer>(); // accepting states; read
													// from data
	// file
	public ArrayList<Integer>[] adj_out_int; // store for each node a list of
												// its
	// outgoing states
	public ArrayList<Integer>[] adj_in_int;
	public String conflict_node_group = "";
	// e.g. for "0 a 2 0.5 3 0.5" record ";0,2,3,"
	// while the 1st node is the source node

	public Set<String> symbols = new HashSet<String>();

	//public Trie SWS_trie = new PatriciaTrie<Integer>();
	public ArrayList<SortedSet<Integer>> SWS_Q1 = new ArrayList<SortedSet<Integer>>();
	public ArrayList<SemiWitnessSet> SWSs = new ArrayList<SemiWitnessSet>();
	Hashtable<String,Integer> SWS_pre_a_SWS = new Hashtable<String, Integer>();
	//idx#a => pre_a sws idx // level 1 only
	
	public ArrayList<WitnessSet> WSs = new ArrayList<WitnessSet>();
	// ArrayList<SuperWitnessSet> superWSs = new ArrayList<SuperWitnessSet>();
	

	
	public Hashtable<String, Vali_Probk> PROBK = new Hashtable<String, Vali_Probk>(); //k starts from 1
	//witness set id # i => probk(w,i)
	public Fraction BKD_x = null; //used by bkd(...) only
	public Long BKD_k = (long) 0; //used by bkd(...) only, record when bkd terminates
	
	public ArrayList<Long> FWD = new ArrayList<Long>();
	public Hashtable<String, Long> FWD_Val = new Hashtable<String, Long>();
	public Long FWD_k = (long) 0; //record when fwd terminates
	//whole val vector of every witness set => i
	
	//public 
	/**
	 * Initializes a DGraph from an input file.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public PA(In in) throws Exception {
		try {
			String s = in.readLine().split("//")[0].trim();
			while(s == null || s.equals("") || s.replaceAll(" ", "").isEmpty()) {
				s = in.readLine().split("//")[0].trim();
			}
			int NV = Integer.parseInt(s); // V.size()
			if (NV < 0)
				throw new IllegalArgumentException("ERROR: # of vertices < 0");
			V = new ArrayList<HPANode>(NV);
			adj_out_int = (ArrayList<Integer>[]) new ArrayList[NV];
			adj_in_int = (ArrayList<Integer>[]) new ArrayList[NV];
			// node line: name and then #propositions
			for (int v = 0; v < NV; v++) {
				// e.g. #FINAL 5 5 is equal to #FINAL 5, while #FINAL->final
				// states
				s = in.readLine().split("//")[0].trim();
				while(s == null || s.equals("") || s.replaceAll(" ", "").isEmpty()) {
					s = in.readLine().split("//")[0].trim();
				}
				HPANode n = new HPANode(v); // n.value = v
				if (s.contains("#")) { // separator of propositions
					if (s.contains("#INITIAL")) {//TODO: ignore case
						this.Q0.add(v);
					}
					if (s.contains("#FINAL")) {
						this.F.add(v);
					}
					for (String t : s.split("#")) {
						if (t.isEmpty())
							continue;
						n.prop.add(t.trim());
					}
				}
				n.name = s.split("#")[0].trim();
				V.add(n);
				adj_out_int[v] = new ArrayList<Integer>();
				adj_in_int[v] = new ArrayList<Integer>();
			}

			// edge line: <int>source <String>input <int>end <double>pr <int>end
			// <double>pr ...
			// e.g. 0 a 2 2/5 3 3/6
			while (true) {
				s = in.readLine();
				if (s == null) {
					break;
				} // end of file; NULL ! = blank line
				s = s.split("//")[0].trim();
				if (s.equals("")) {
					continue;
				}
				int source = -1;
				String input = "";
				int end = -1;
				int valid_i = 0; // the number of valid elements in the line
				Fraction pr_sum = Fraction.ZERO;
				ArrayList<edge> trans = new ArrayList<edge>();
				for (String s1 : s.split(" ")) {
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
							throw new Exception(
									"ERROR of data file in line "
											+ s
											+ ": all edges extending from the same source node and input should be written in one line");
						}
					} else if (valid_i % 2 == 1) {
						end = Integer.parseInt(s1);
						if (end < 0 || end >= NV)
							throw new IndexOutOfBoundsException("ERROR: node "
									+ end + " is not between 0 and " + (NV - 1));
					} else {
						// obtain probability
						Fraction pr = util.parseFractionPr(s1);
						if(pr == null){//not a valid pr.
							throw new Exception(
									"ERROR of data file in line "
											+ s + ": invalid probability value defined.");
						}
						pr_sum = pr_sum.add(pr);
						trans.add(new edge(pr, end));
					}
					if (pr_sum.compareTo(Fraction.ONE) == 1) {// >1
						throw new Exception("ERROR in data file " + s
								+ ": sum of probabilities exceeds 1.");

					}
					if (valid_i >= 4 && pr_sum.compareTo(Fraction.ONE) == 0) {
						symbols.add(input);
						PATransition t = new PATransition(input, trans);
						V.get(source).out_transitions.add(t);

						for (edge e : trans) {
							adj_out_int[source].add(e.node);
							adj_in_int[e.node].add(source);
							V.get(e.node).in_symbles.add(input);
							V.get(e.node).in_edges.add(new edge(e.pr, source));
						}

						if (trans.size() > 1) {
							conflict_node_group += ";" + source + ",";
							for (int e : t.end_nodes()) {
								conflict_node_group += e + ",";
							}
						}
					}
				}// for each edge line
				if (pr_sum.compareTo(Fraction.ONE) == -1) {// pr_sum < 1
					throw new Exception("ERROR in data file " + s
							+ ": sum of probabilities is less than 1.");
				}
			}// end while

		} catch (NoSuchElementException e) {
			throw new Exception(
					"ERROR of data file: Invalid input format");
		}

		in.close();

		// Q0 = init_Node();//already done using #propositions
		if (F.isEmpty()) { // implies no incorrect runs
			// TODO
			// throw new Exception("ERROR: no final states defined.");
		}
	}

	/**
	 * Return the set of initial states of G. Initial states decided by
	 * in-degrees of each state. It a state has no incoming edges, it's an
	 * initial state.
	 * 
	 * */
	@SuppressWarnings("unused")
	private Set<Integer> init_Node() {
		Set<Integer> ini = new HashSet<Integer>();
		for (int i = 0; i < adj_in_int.length; i++) {
			if (adj_in_int[i].size() == 0) {
				ini.add(i);
			}
		}
		if (ini.isEmpty())
			ini.add(0);// node 0 is by default the initial node
		return ini;
	}

	/**
	 * Check whether this PA is determined on each symbol at each state. Return
	 * the state id if non-determinism found at the state, or -1.
	 * */
	protected int isDeterministic() {
		for (HPANode n : this.V) {
			if (n.input().size() != this.symbols.size()) {
				return n.id;
			}
		}
		return -1;
	}

	/**
	 * Return the conflict node group containing node n while the first node in
	 * the ArrayList is the source node, and the other nodes are end nodes.
	 * Note: n is included is in the returned ArrayList
	 * */
	public ArrayList<Integer> conflictNodes(int n) {
		ArrayList<Integer> C = new ArrayList<Integer>();
		for (String s : conflict_node_group.split(";")) {
			List<Integer> nodes = util.parsePosIntegers(s);
			if (!nodes.contains(n))
				continue;
			for (int c : nodes) {
				C.add(c);
			}
		}
		return C;
	}

	/**
	 * Decide if two nodes are conflict w.r.t. a third source node, i.e. they
	 * are both possible successors of that source node on the same input.
	 * Return the source node if they are conflict, else return -1;
	 * */
	public int isConflict(int f, int t) {
		// ;2,3,5,
		for (String s : conflict_node_group.split(";")) {// 2,3,5,
			if (s.contains("," + f + ",") && s.contains("," + t + ",")) {
				return util.parsePosIntegers(s).get(0);
			}
		}
		return -1;
	}

	public static void main(String[] args) {
	}

	/**
	 * Return the set of level-l states of the graph
	 * */
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
	 * Return the set of states in set S from a specified level
	 * 
	 * @param level
	 *            : the specified level
	 * @param S
	 *            : the data source as a set
	 * */
	public SortedSet<Integer> obtainSetOnLevel(Set<Integer> S, int level) {
		SortedSet<Integer> L = new TreeSet<Integer>();
		for (int i : S) {
			if (V.get(i).level == level) {
				L.add(i);
			}
		}
		return L;
	}

	/**
	 * Return the set of states reached from state q with input symbol a
	 * */
	public Set<Integer> postQ(int q, String a) {
		return V.get(q).post_nodes(a);
	}

	/**
	 * Return the set of states reached from any state in set W on input a
	 * */
	public Set<Integer> post(Set<Integer> W, String a) {
		Set<Integer> s = new HashSet<Integer>();
		for (Integer q : W) {
			s.addAll(V.get(q).post_nodes(a));
		}
		return s;
	}

	/**
	 * Return the set of states reached from state q with input string u
	 * */
	public Set<Integer> post(int q, ArrayList<String> u) {
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
	 * */
	public ArrayList<edge> post_dist(int q, String a) {
		return V.get(q).post_dist(a);
	}

	/**
	 * Return the pr. starting from q, after reading u, the final state is in Q
	 * */
	public double probQ(int q, ArrayList<String> u, Set<Integer> Q) {
		double pr = 0;
		double[] q_u = this.u_matrix(u).getRow(q);
		for (int i = 0; i < q_u.length; i++) {
			if (Q.contains(i))
				pr = pr + q_u[i];
		}
		return pr;
	}
	
	/**
	 * Return the pr. {starting from q, after reading a, the next state is in Q}.
	 * Time: O(m+n)
	 * */
	public double probQa(int q, String a, Set<Integer> Q) {
		double pr = 0;
		double[] q_a = this.a_matrix(a).getRow(q); ///O(m)
		for (int i = 0; i < q_a.length; i++) { ///O(n)
			if (Q.contains(i))
				pr = pr + q_a[i];
		}
		return pr;
	}

	/**
	 * Return True if there is at least a run of G accepting u with probability
	 * greater than x, Starting from specific node qs
	 * */
	public boolean acceptWord(int qs, ArrayList<String> u, double x) {
		if (probQ(qs, u, F) >= x) {
			return true;
		}
		return false;
	}

	/**
	 * Return True if there is at least a run of G accepting u with probability
	 * greater than x, Starting from any initial node
	 * */
	public boolean acceptWord(ArrayList<String> u, double x) {
		for (int qs : Q0) {
			if (acceptWord(qs, u, x)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the one-step transition matrix of G on input symbol a; Return null
	 * if G is empty, or a is not acceptable to this G.
	 * Time: O(number of transitions in G) = O(m)
	 * */
	public RealMatrix a_matrix(String a) {
		int NV = V.size();
		if (NV <= 0)
			return null;
		if (!symbols.contains(a))
			return null;
		RealMatrix A = MatrixUtils.createRealMatrix(NV, NV);
		for (HPANode n : V) {
			if (n.post_dist(a) == null) { // n does not accept a
				continue;
			}
			for (edge d : n.post_dist(a)) {
				A.setEntry(n.id, d.node, d.pr.doubleValue());
			}
		}
		return A;
	}

	/**
	 * Return the transition matrix of G on input string u; Return null if u is
	 * empty.
	 * */
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
	 * Calculate and store a_successors of U on a.
	 * */
	private void update_a_successor(WitnessSet U, String a){
		if(U.a_successor.get(a)!=null){ //already calculated for WS and a
			return;
		}else{
			HashSet<Integer> Vs= new HashSet<Integer>();
			for (WitnessSet V : WSs) {///w *
				if (util.isSubset(WSNodes(V.id), post(WSNodes(U.id), a))///(m+n)
						&& util.isSubset(post(WSL1Nodes(U.id), a), WSNodes(V.id))) {///(m+n)
					Vs.add(V.id);
				}
			}
			U.a_successor.put(a, Vs);
		}
	}

	/**
	 * Calculate and Store prob_k_W. max Aprob(a,W1), where W1 is an a-successor
	 * of W.
	 * 
	 * Return NULL if org.apache.commons.math3.exception.MathArithmeticException.
	 * Error in bkd, maybe empty non-convergence
	 * @throws Exception
	 * */
	public Fraction Prob_k_W(WitnessSet W, long k) throws Exception {
		Vali_Probk vp = this.PROBK.get(W.id+"#"+k);
		if (vp!= null) {
			return vp.pr;
		}

		Fraction prob = Fraction.ZERO;
		ArrayList<String> ts = new ArrayList<String>();
		ArrayList<Integer> as = new ArrayList<Integer>();

		if (k == 1) {
			for (String a : symbols) {
				this.update_a_successor(W, a);//only needed at k = 1
				if (F.containsAll(post(WSL1Nodes(W.id), a))) {
					Fraction p = V.get(W.q0).post_pr(a, F);
					if (p.compareTo(prob) == 1) {// p > prob
						prob = p;
						ts.clear();
						ts.add(a);
						as.clear();
						as.add(-1);// -1 implies F
					} else if (p.compareTo(prob) == 0) {// p = prob
						ts.add(a);
						as.add(-1);// -1 implies F
					}
				}
			}
		} else {
			for (String a : symbols) {
				HashSet<Integer> Vs = W.a_successor.get(a);
				if(Vs==null || Vs.isEmpty()){ // no a_succesor
					continue; //for a 
				}
				for (int v : Vs) {
					WitnessSet W1 = this.WSs.get(v);
					Fraction d1 = V.get(W.q0).post_pr(a, WSL1Nodes(W1.id));
					Fraction d2 = V.get(W.q0).post_pr(a, W1.q0);
					Fraction d3 = Prob_k_W(W1, k - 1);
					if(d3 == null) return null; //Error in bkd, maybe empty non-convergence
					Fraction t = Fraction.ZERO;
					try{
						t = d1.add(d2.multiply(d3));// apr
					}catch(org.apache.commons.math3.exception.MathArithmeticException e){
						//Error in bkd, maybe empty non-convergence
						return null;
					}
					if (t.compareTo(prob) == 1) {// t > prob
						prob = t;
						ts.clear();
						ts.add(a);
						as.clear();
						as.add(W1.id);
					} else if (t.compareTo(prob) == 0) {// t = prob
						ts.add(a);
						as.add(W1.id);
					}
				}
			}
		}
		this.PROBK.put(W.id+"#"+k,new Vali_Probk(prob,(long)k,ts,as));
		return prob;
	}

	public void obtainFiniteGoodSWSs() throws Exception {
		SortedSet<Integer> F1 = obtainSetOnLevel(F, 1);
		Set<Integer> F0 = obtainSetOnLevel(F, 0);
		// if (F1.isEmpty())
		// throw new Exception("ERROR: no final states on level 1");
		this.SWS_Q1.add(F1);
		this.SWSs.add(new SemiWitnessSet(0, 0, F0));
		//SortedSet<Integer> L = new TreeSet<Integer>();
		int id = 0; // latest id in the SemiWitnessSets
		int idx = 0;
		boolean hasEmptySWS = false;

		while (idx < this.SWSs.size()) {
			SemiWitnessSet sw = this.SWSs.get(idx);
			if(this.SWS_Q1.get(sw.L1_id).isEmpty()){
				hasEmptySWS = true;
			}else{
				for (String a : symbols) {
					Set<Integer> t = pre_a_pr1(this.ObtainSWSNodes(sw), a);
					SortedSet<Integer> t1 = this.obtainSetOnLevel(t, 1);
					Set<Integer> t0 = this.obtainSetOnLevel(t, 0);
					if (t1.isEmpty() && t0.isEmpty())
						continue;
					if (t1.isEmpty())
						hasEmptySWS = true;
					int t1id = this.SWS_Q1.indexOf(t1);
					if(t1id != -1){//existing L1 node set and sws
						this.SWSs.get(idx).in_L1SetId.add(t1id);
						this.SWSs.get(idx).super_L0_nodes.addAll(t0);
					}else{
						this.SWS_Q1.add(t1);
						t1id = this.SWS_Q1.size()-1;
						SWS_pre_a_SWS.put(idx+"#"+a, t1id);
						SemiWitnessSet temp = new SemiWitnessSet(this.SWSs.size(), t1id, t0);
						this.SWSs.get(idx).in_symbols.add(a);
						this.SWSs.get(idx).in_L1SetId.add(t1id);
						this.SWSs.add(temp);
					}
				}//for a
			}//else
			idx++;
			//System.out.println(SWS_pre_a_SWS.toString());
		}
		if (!hasEmptySWS){ // add empty set
			this.SWS_Q1.add(new TreeSet<Integer>());
			int t1id = this.SWS_Q1.size()-1;
			this.SWSs.add(new SemiWitnessSet(id + 1, t1id, null));
		}
	}

	private Set<Integer> pre_a_pr1(Set<Integer> S, String a) {
		Set<Integer> pre = new HashSet<Integer>();
		for (int i : S) {
			HPANode n = V.get(i);
			for (int p : n.pre_nodes(a)) {
				if (V.get(p).post_pr(a, i).compareTo(Fraction.ONE) == 0) {
					pre.add(p);
				}
			}
		}
		return pre;
	}

	public void obtainFiniteGoodWSs() throws Exception {
		for (int sws_id = 0; sws_id < this.SWSs.size(); sws_id++) {
			this.addWSs(sws_id);
		}
	}

	/**
	 * Construct WSs for a newly added sws in SemiWitnessSets
	 * 
	 * @param wS
	 * @return
	 * */
	public void addWSs(int sws_id) {
		for (Integer q0 : obtainLevel(0)) {
			int i = this.WSs.size();
			if (this.SWSs.get(sws_id).super_L0_nodes.contains(q0))
				this.WSs.add(new WitnessSet(i, q0, sws_id, true));
			else
				this.WSs.add(new WitnessSet(i, q0, sws_id, false));
		}
	}

	/**
	 * Return the Val
	 * 
	 * @param qs
	 *            is the single initial node of g
	 * @param x
	 *            is the given acceptance threshold
	 * @throws Exception
	 * */
	public Fraction Val(int ws, int qs, Fraction x, long i) throws Exception {
		if(ws == -1){
			return null;
		}
		WitnessSet W= WSs.get(ws);
		Vali_Probk vp = W.VALI.get(i);
		if (vp != null) { // /best time for the function
			return vp.pr;
		}

		Fraction val = Fraction.TWO; // (Double.POSITIVE_INFINITY);
		ArrayList<String> ts = new ArrayList<String>();
		ArrayList<Integer> as = new ArrayList<Integer>();
		//ts.add("");
		//as.add(ws);
		
		if (i == 0) {
			if (W.q0 == qs){
				val = x;
			}
		} else {// i>0
			val = W.VALI.get(i-1).pr;//min of val(W,i-1) and ...
			int j = 0;
			for (j = 0; j < V.get(W.q0).in_edges.size(); j++) {
				int q = V.get(W.q0).in_edges.get(j).node;
				String a = V.get(W.q0).in_symbles.get(j);
				Fraction qa0 = V.get(W.q0).in_edges.get(j).pr;
				int waq = Waq(ws, a, q);
				Fraction d1 = Val(waq, qs, x, i - 1);
				if (d1.compareTo(Fraction.ONE) == 1) // d1>1
					continue;
				Fraction qa1 = W.qa1.get(a+"#"+q);
				if(qa1 ==null){
					qa1 = V.get(q).post_pr(a, WSL1Nodes(ws));
					W.qa1.put(a+"#"+q, qa1);
				}
				 
				Fraction d_add = Fraction.ZERO;
				d_add = (d1.subtract(qa1)).divide(qa0);// (d1-qa1)/qa0
				// val = (val < d_add)? val : d_add;
				if (val.compareTo(d_add) == 1) {// val > d_add){
					val = d_add;
					ts.clear();
					ts.add(a);
					as.clear();
					as.add(waq);
				}
				else if (val.compareTo(d_add) == 0) {// val == d_add){
					ts.add(a);
					as.add(waq);
				}
			}
			
		}
		W.VALI.put(i, new Vali_Probk(val, -1, ts, as));
		return val;
	}

	/**
	 * Return the index of the WitnessSet Waq in WitnessSets
	 * @throws Exception 
	 * */
	public Integer Waq(int ws, String a, int q) throws Exception {
		WitnessSet W = WSs.get(ws);
		Integer waqid = W.Waq.get(a+"#"+q);
		if(waqid != null){
			return waqid;
		}
		
		Set<Integer> pre_a = this.pre_a_pr1(WSNodes(ws), a);
		Set<Integer> pre_a1 = this.obtainSetOnLevel(pre_a, 1);
		int l1id = this.SWS_Q1.indexOf(pre_a1);
		if (l1id == -1) {//Theoretically, this will not occur.
			throw new Exception("Error found in Waq, must be error in generating all swss.\n"
					+ "ws="+ws+", input="+a+",q at Q0 is "+q+".\n");
		} else {
			for (int i = 0; i < WSs.size(); i++) {
				if (WSs.get(i).q0 == q && this.SWSs.get(WSs.get(i).sws_id).L1_id == l1id) {
					W.Waq.put(a+"#"+q,i);
					return i;
				}
			}
		}
		throw new Exception("Error found in Waq, why Waq not found?\n"
				+ "ws="+ws+", input="+a+",q at Q0 is "+q+".\n");
		//W.Waq.put(a+"#"+q, -1);//empty set
		//return -1;
	}

	/**
	 * Obtain the set of nodes in specific witness set.
	 * Time: O(|Q1|)<O(n)
	 * */
	public Set<Integer> WSNodes(int ws) {
		Set<Integer> new_nodes = new HashSet<Integer>(this.WSL1Nodes(ws));// copy-constructor
		new_nodes.add(WSs.get(ws).q0);
		return new_nodes;
	}

	/**
	 * Obtain the set of level 1 nodes in specific witness set
	 * */
	public Set<Integer> WSL1Nodes(int ws) {
		return this.SWS_Q1.get(this.SWSs.get(this.WSs.get(ws).sws_id).L1_id);
	}

	/**
	 * TODO
	 * */
	public ArrayList<WitnessSet> obtainInfGoodWS(ArrayList<WitnessSet> WS) {
		ArrayList<WitnessSet> infWS = new ArrayList<WitnessSet>();

		return infWS;
	}

	/**
	 * L = 4rn8^n
	 * */
	public long L() {
		long n = this.V.size();
		long R = 1;
		for (HPANode s : this.V) {
			for (PATransition t : s.out_transitions) {
				for (edge e : t.distribution) {
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
	 * Check whether forward algorithm converges, if not, update FWD and FWD_Val.
	 * Supports repeating convergence as well as fixed point convergence.
	 * */
	public boolean FWD_Convergent(String val_string, long i) {
		Long n = this.FWD_Val.get(val_string);
		if (n != null) {//same val for smaller i
			this.FWD.add(n);
			return true;
		} else {
			this.FWD.add(i);
			this.FWD_Val.put(val_string, i);
		}
		return false;
	}

	public Set<Integer> ObtainSWSNodes(SemiWitnessSet sws) {
		Set<Integer> nodes = new HashSet<Integer>();
		nodes.addAll(this.SWS_Q1.get(sws.L1_id));
		nodes.addAll(sws.super_L0_nodes);
		return nodes;
	}
	
	public void clear_WS_VALI (){
		for(WitnessSet WS:this.WSs){
			WS.VALI.clear();
		}
	}

}