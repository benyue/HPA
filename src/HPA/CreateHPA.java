package HPA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;

import SCC.TarjanSCC;
import Util.util;
import edu.princeton.cs.introcs.In;
/**
 * @author Cindy Yue Ben
 */
public class CreateHPA {
	public String name = "";
	public String prefix = ""; // prefer unique among different sessions

	public ArrayList<CreateHPAState> V = new ArrayList<CreateHPAState>();;
	public ArrayList<CreateHPATransition> T =
	        new ArrayList<CreateHPATransition>();

	public Integer q0 = -1; // initial state, can be automatically updated
	public Set<Integer> F = new HashSet<Integer>(); // accepting states; read
	                                                // from data
	                                                // file
	public Set<String> symbols = new HashSet<String>();
	public boolean is0HPA = true;

	/**
	 * Initializes a DGraph from an input file.
	 * 
	 * @throws Exception
	 */
	public CreateHPA(In in) throws Exception {
		try {
			String s = in.readLine().split("//")[0].trim();
			while (s == null || s.equals("")
			        || s.replaceAll(" ", "").isEmpty()) {
				s = in.readLine().split("//")[0].trim();
			}
			int NV = 0;
			try {
				NV = Integer.parseInt(s); // V.size()
			} catch (java.lang.NumberFormatException ne) {
				throw new NumberFormatException("ERROR: invalid file format");
			}
			if (NV <= 0)
				throw new IllegalArgumentException("ERROR: # of vertices <= 0");
			String tempV = "";
			for (int v = 0; v < NV; v++) {
				s = in.readLine().split("//")[0].trim();
				while (s == null || s.equals("")
				        || s.replaceAll(" ", "").isEmpty()) {
					s = in.readLine().split("//")[0].trim();
				}
				String value = s.split("#")[0].trim();
				if (tempV.contains(";" + value + ";")) {
					throw new IllegalArgumentException(
					        "ERROR: duplicate state with value " + value);
				}
				CreateHPAState n = new CreateHPAState(v, value);
				if (s.contains("#")) {// separator of propositions
					if (s.contains("#INITIAL")) {
						q0 = v;
					}
					if (s.contains("#FINAL")) {
						F.add(v);
					}
					for (String t : s.split("#")[1].split("#")) {
						if (t.isEmpty())
							continue;
						n.prop.add(t.trim());
					}
				}
				V.add(n);
				tempV = tempV + (";" + value + ";");
			}

			while (true) { // obtain transitions
				s = in.readLine();
				if (s == null) {
					break;
				} // end of file; NULL ! = blank line
				s = s.split("//")[0].trim();
				if (s.equals("") || s.replaceAll(" ", "").isEmpty()) {
					continue;
				}
				int source = -1;
				String input = "";
				int end = -1;
				// s2-a->s1
				// s0-a->s1,0.8 #SYNC;2,0.2
				if (s.contains(";")) {
					source = findStateIDbyValue(s.split("-")[0]
					        .replaceAll(" ", ""));
					input = s.split("-")[1].replaceAll(" ", "");
					if (source < 0)
						throw new IllegalArgumentException("ERROR: line " + s
						        + " is invalid with undefined source state");
					if (input.isEmpty())
						throw new IllegalArgumentException("ERROR: line " + s
						        + " is invalid with no input");
					if (V.get(source).inputs.contains(input)) {
						throw new IllegalArgumentException("ERROR: line " + s
						        + " is invalid with duplicate input");
					}
					ArrayList<CreateHPATransition> tl =
					        new ArrayList<CreateHPATransition>();
					s = s.split("->")[1];// s1,0.8 #SYNC;2,0.2
					Fraction pr_sum = Fraction.ZERO;
					int nt = T.size();
					for (String s1 : s.split(";")) {// s1,0.8 #SYNC //2,0.2
						if (!s1.contains(","))
							continue;
						end = findStateIDbyValue(s1.split(",")[0].trim());
						if (end < 0)
							throw new IllegalArgumentException(
							        "ERROR: invalid line containing " + s1);
						Fraction p = util.parseFractionPr(s1.split(",")[1]
						        .split("#")[0].replaceAll(" ", ""));
						pr_sum = pr_sum.add(p);
						CreateHPATransition t = new CreateHPATransition(nt,
						        source, input, end, p);
						if (s1.contains("#")) {
							for (String prop : s1.split("#")[1].split("#")) {
								prop = prop.trim();
								if (!prop.isEmpty()) {
									t.propRequired.add(prop);
								}
							}
						}
						tl.add(t);
						nt++;
					}
					if (pr_sum.compareTo(Fraction.ONE) == 0) {
						V.get(source).inputs.add(input);
						for (CreateHPATransition t : tl) {
							T.add(t);
							symbols.add(input);
							V.get(source).outTransID
							        .add(T.size() - 1);
						}
						is0HPA = false;
					}

				} else {
					try {
						source = this
						        .findStateIDbyValue(s.split("-")[0].trim());
						input = s.split("-")[1].trim();
						end = findStateIDbyValue(s.split("->")[1]
						        .split("#")[0].trim());
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println(s);
					}
					if (source < 0) {
						throw new IllegalArgumentException(
						        "ERROR: line "
						                + s
						                + " is an invalid transition with undefined source");
					}
					if (end < 0) {
						throw new IllegalArgumentException(
						        "ERROR: line "
						                + s
						                + " is an invalid transition with undefined end");
					}
					if (input.isEmpty()) {
						throw new IllegalArgumentException("ERROR: line " + s
						        + " is an invalid transition with no input");
					}
					if (V.get(source).inputs.contains(input)) {
						throw new IllegalArgumentException("ERROR: line " + s
						        + " is invalid with duplicate input");
					}
					CreateHPATransition t = new CreateHPATransition(
					        T.size(), source, input, end);
					if (s.contains("#")) {
						for (String prop : s.split("#")[1].split("#")) {
							prop = prop.trim();
							if (!prop.isEmpty()) {
								t.propRequired.add(prop);
							}
						}
					}
					T.add(t);
					symbols.add(input);
					V.get(source).outTransID.add(T.size() - 1);
					V.get(source).inputs.add(input);
				}
			} // end while(true)

		} catch (NoSuchElementException e) {
			throw new InputMismatchException("ERROR: Invalid input format");
		}

		in.close();

		if (q0 == -1)
			throw new Exception("ERROR: no initial state.");
		if (F.isEmpty())
			throw new Exception("ERROR: no final states.");
	}

	/**
	 * Construct a new PA instance by creating a deep copy of an existing one;
	 */
	public CreateHPA(CreateHPA g) {
		// T= new ArrayList<Transition> (g.T);//Wrong! shallow copy
		// T= new ArrayList<Transition> ();//duplicate
		for (CreateHPATransition t : g.T) {
			CreateHPATransition t1 = new CreateHPATransition(t);
			T.add(t1);
		}
		// V= new ArrayList<State> (g.V);//Wrong! not a deep copy
		for (CreateHPAState s : g.V) {
			CreateHPAState s1 = new CreateHPAState(s.id, s.name,
			        new ArrayList<Integer>(s.outTransID), new HashSet<String>(
			                s.inputs));
			V.add(s1);
			s1.prop.addAll(s.prop);
		}

		q0 = g.q0;
		F = new HashSet<Integer>(g.F);
		symbols = new HashSet<String>(g.symbols);

		name = g.name;
		prefix = g.prefix;
		is0HPA = g.is0HPA;
	}

	public CreateHPA() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {
	}

	/**
	 * Ensure every state with T as input has no external input. Return the
	 * sate's ID and name if violated; else return null.
	 */
	public String checkT() {
		for (CreateHPAState s : V) {
			if (s.inputs.contains("T") && s.inputs.size() > 1)
				return (s.name);
		}
		return null;

	}

	/**
	 * 
	 * Partially synchronized composition with partial order reduction,
	 * automatically sync-ed on shared/common symbols, which should be
	 * consistent with com.
	 * 
	 * All states and transitions generated in this function will be reachable.
	 * 
	 * @param g1
	 * @param g2
	 *            the two threads to be composed; note g1 ALWAYS has priority
	 *            over g2 in partial order reduction.
	 */
	public static CreateHPA PSCompPOR(CreateHPA g1, CreateHPA g2)
	        throws Exception {
		Set<String> com = util.intersection(g1.symbols, g2.symbols);
		CreateHPA g = new CreateHPA();
		g.name = g1.name + g2.name;
		g.prefix = g1.prefix + g2.prefix;
		g.symbols = util.union(g1.symbols, g2.symbols);

		Queue<Integer> Q = new LinkedList<Integer>();
		// q1 and q2 record v1 and v2 in each q in Q
		Queue<Integer> q1 = new LinkedList<Integer>();
		Queue<Integer> q2 = new LinkedList<Integer>();
		// g.q0
		String v = g1.V.get(g1.q0).name + "," + g2.V.get(g2.q0).name;
		CreateHPAState s0 = new CreateHPAState(g.V.size(), v);
		s0.prop.addAll(g1.V.get(g1.q0).prop);
		s0.prop.addAll(g2.V.get(g2.q0).prop);
		s0.prop.add(g.prefix + "INITIAL");
		g.q0 = s0.id;
		g.V.add(s0);
		Q.add(s0.id);
		q1.add(g1.q0);
		q2.add(g2.q0);

		int v1 = 0, v2 = 0;
		while (Q.size() > 0) {
			int sid = Q.poll();
			v1 = q1.poll();
			v2 = q2.poll();

			boolean v1_stuck = true;
			for (int i1 = 0; i1 < g1.V.get(v1).outTransID.size(); i1++) {
				int c1 = g1.V.get(v1).outTransID.get(i1);
				CreateHPATransition t1 = g1.T.get(c1);

				for (int i2 = 0; i2 < g2.V.get(v2).outTransID.size(); i2++) {
					int c2 = g2.V.get(v2).outTransID.get(i2);
					CreateHPATransition t2 = g2.T.get(c2);

					if (com.contains(t1.input) && t1.input.equals(t2.input)) {// sync
						v1_stuck = false;
						String end = g1.V.get(t1.endID).name + ","
						        + g2.V.get(t2.endID).name;
						String a = t1.input;
						g.V.get(sid).inputs.add(a);
						int eid = g.findStateIDbyValue(end);
						if (eid != -1) {
							if (g.findTransitionIdBySourceEndInput(sid, eid,
							        a) != -1)
								continue;
						} else { // if (eid == -1) {
							CreateHPAState s = new CreateHPAState(g.V.size(),
							        end);
							s.prop.addAll(g1.V.get(t1.endID).prop);
							s.prop.addAll(g2.V.get(t2.endID).prop);
							g.V.add(s);
							eid = g.V.size() - 1;
							if (g1.F.contains(t1.endID)
							        && g2.F.contains(t2.endID)) {
								s.prop.add(g.prefix + "FINAL");
								g.F.add(eid);
							}
							Q.add(eid);
							q1.add(t1.endID);
							q2.add(t2.endID);
						}
						CreateHPATransition t = new CreateHPATransition(
						        g.T.size(), sid, a, eid,
						        (t1.pr.multiply(t2.pr)));
						t.propSatisfied.addAll(t1.propSatisfied);
						t.propSatisfied.addAll(t2.propSatisfied);
						g.T.add(t);
						g.V.get(sid).outTransID.add(g.T.size() - 1);
					} // end sync
					else {
						if (com.contains(t1.input) && !g2.F.contains(v2))
							continue;

						if (g1.F.contains(v1) && com.contains(t2.input))
							continue;// so g1 will not start a new session while
							         // g2 executing T-transitions

						v1_stuck = false;
						String end = g1.V.get(t1.endID).name + ","
						        + g2.V.get(v2).name;
						String a = t1.input;
						g.V.get(sid).inputs.add(a);

						int eid = g.findStateIDbyValue(end);
						if (eid != -1) {
							if (g.findTransitionIdBySourceEndInput(sid, eid,
							        a) != -1)
								continue;
						} else {// if (eid == -1) {
							CreateHPAState s = new CreateHPAState(g.V.size(),
							        end);
							s.prop.addAll(g1.V.get(t1.endID).prop);
							s.prop.addAll(g2.V.get(v2).prop);
							g.V.add(s);
							eid = g.V.size() - 1;
							if (g1.F.contains(t1.endID) && g2.F.contains(v2)) {
								s.prop.add(g.prefix + "FINAL");
								g.F.add(eid);
							}
							Q.add(eid);
							q1.add(t1.endID);
							q2.add(v2);
						}
						CreateHPATransition t = new CreateHPATransition(
						        g.T.size(), sid, a, eid, (t1.pr));
						t.propSatisfied.addAll(t1.propSatisfied);
						g.T.add(t);
						g.V.get(sid).outTransID.add(g.T.size() - 1);
					}

					// g.printPA("temp");
				} // for g2.V.get(v2).outTransID
			} // for g1.V.get(v1).outTransID

			if (g1.F.contains(v1) || g1.V.get(v1).outTransID.size() == 0
			        || v1_stuck) {
				for (int i2 = 0; i2 < g2.V.get(v2).outTransID.size(); i2++) {
					int c2 = g2.V.get(v2).outTransID.get(i2);
					CreateHPATransition t2 = g2.T.get(c2);
					if (com.contains(t2.input) && !g1.F.contains(v1))
						continue;
					String end = g1.V.get(v1).name + ","
					        + g2.V.get(t2.endID).name;
					String a = t2.input;
					g.V.get(sid).inputs.add(a);
					int eid = g.findStateIDbyValue(end);
					if (eid != -1) {
						if (g.findTransitionIdBySourceEndInput(sid, eid,
						        a) != -1)
							continue;
					} else {// if (eid == -1) {
						CreateHPAState s = new CreateHPAState(g.V.size(), end);
						s.prop.addAll(g1.V.get(v1).prop);
						s.prop.addAll(g2.V.get(t2.endID).prop);
						g.V.add(s);
						eid = g.V.size() - 1;
						if (g1.F.contains(v1) && g2.F.contains(t2.endID)) {
							s.prop.add(g.prefix + "FINAL");
							g.F.add(eid);
						}
						Q.add(eid);
						q1.add(v1);
						q2.add(t2.endID);
					}
					CreateHPATransition t = new CreateHPATransition(g.T.size(),
					        sid, a, eid, (t2.pr));
					t.propSatisfied.addAll(t2.propSatisfied);
					g.T.add(t);
					g.V.get(sid).outTransID.add(g.T.size() - 1);
				}
			} // end of adding server 2

			// g.printPA("Temp g with Q, q1 and q2 as: " + Q + ";" + q1 + ";" +
			// q2);
		} // end of while (Q.size() > 0)

		return g;
	}

	/**
	 * Partially synchronized composition, automatically sync-ed on
	 * shared/common symbols, which should be consistent with com. All ERROR
	 * states will be merged into one during composition. All states and
	 * transitions generated in this function will be reachable. Note gfp's
	 * final states are defined ALS g1FINAL (suppose p is defeined on g1) and
	 * gpFINAL. Also as for situations "g1Final", but either not "pfinal" or not
	 * "g1failed", go to ERROR state. <br>
	 * Pre-condition: no ERROR state in the initial states nor final states;
	 * only one ERROR state in gp.
	 * 
	 * @param gf
	 * @param gp
	 *            the two threads to be composed; one is the thread, the other
	 *            is the property automata.
	 * @param failedServer
	 *            Specify one session's prefix to clarify on which session is gp
	 *            defined.
	 */
	public static CreateHPA PSCompMergeERROR(CreateHPA gf, CreateHPA gp,
	        String failedServer) throws Exception {
		if (!util.equals(gf.symbols, gp.symbols)) {
			throw new Exception(
			        "Error in determinizing gp s.t. gf.symbols != gp.symbols.\n");
		}
		Set<String> com = util.intersection(gf.symbols, gp.symbols);
		// com = gf.symbols = gp.symbols

		CreateHPA g = new CreateHPA();
		g.symbols = gp.symbols; // util.union(gf.symbols, gp.symbols);
		g.name = gf.name + gp.name;
		g.prefix = gf.prefix + gp.prefix;

		Queue<Integer> Q = new LinkedList<Integer>();
		Queue<Integer> q1 = new LinkedList<Integer>();
		Queue<Integer> q2 = new LinkedList<Integer>();
		// g.q0
		String v = gf.V.get(gf.q0).name + "," + gp.V.get(gp.q0).name;
		CreateHPAState s0 = new CreateHPAState(g.V.size(), v);
		s0.prop.addAll(gf.V.get(gf.q0).prop);
		s0.prop.addAll(gp.V.get(gp.q0).prop);
		s0.prop.add("INITIAL");// to be detected in HPA program
		s0.prop.add(g.prefix + "INITIAL");// for use later in removeT and
		                                  // reAssignID
		g.q0 = s0.id;
		g.V.add(s0);
		Q.add(s0.id);
		q1.add(gf.q0);
		q2.add(gp.q0);

		// find the ERROR state in gp
		int v2error = gp.findStateIDbyValue("ERROR");
		if (v2error == -1) {
			throw new Exception(
			        "/n ERROR: why no ERROR state in gp? what's that defined in determinaztion of gp?");
		}
		// ensure ERROR is not "pfinal"
		if (gp.F.contains(v2error)) {
			throw new Exception(
			        "/nERROR: ERROR state should not be final state in gp.");
		}

		CreateHPAState es = new CreateHPAState(g.V.size(), "ERROR");
		es.prop.add("ERROR");
		g.V.add(es);

		int v1 = 0, v2 = 0;// g and gp
		while (Q.size() > 0) {
			int sid = Q.poll();
			v1 = q1.poll();
			v2 = q2.poll();

			for (int i1 = 0; i1 <= gf.V.get(v1).outTransID.size(); i1++) {
				int c1 = -1;
				CreateHPATransition t1 = new CreateHPATransition();
				if (gf.V.get(v1).outTransID.size() > 0
				        & gf.V.get(v1).outTransID.size() > i1) {
					c1 = gf.V.get(v1).outTransID.get(i1);
					t1 = gf.T.get(c1);
				} else {
					t1.input = "";
				}

				for (int i2 = 0; i2 < gp.V.get(v2).outTransID.size(); i2++) {
					int c2 = gp.V.get(v2).outTransID.get(i2);
					CreateHPATransition t2 = gp.T.get(c2);

					if (gf.V.get(v1).outTransID.size() > 0
					        && gf.V.get(v1).outTransID.size() > i1
					        && com.contains(t1.input)
					        && t1.input.equals(t2.input)) {// sync
						@SuppressWarnings("unused")
						String source = g.V.get(sid).name;
						String end = gf.V.get(t1.endID).name + ","
						        + gp.V.get(t2.endID).name;

						if (t2.endID == v2error) {// merge error state
							end = es.name;
						}
						if (gf.V.get(t1.endID).prop.contains(failedServer
						        + "FINAL")
						        && !gp.F.contains(t2.endID)) {
							// if not forcing T-transitions to execute first
							// after failure,
							// then once g1Final but not gpFinal, go to ERROR
							// state,
							// or else will introduce 2-HPA error.
							end = es.name;
						}

						if (!t1.propSatisfied.containsAll(t2.propRequired)) {
							end = es.name;
						}

						Fraction pr = t1.pr.multiply(t2.pr);
						String a = t1.input;
						g.V.get(sid).inputs.add(a);
						int eid = g.findStateIDbyValue(end);
						if (eid != -1) {
							int tid = g.findTransitionIdBySourceEndInput(sid,
							        eid, a);
							if (tid != -1) {
								if (g.T.get(tid).pr
								        .compareTo(Fraction.ONE) < 0) {// pr
								                                       // <
								                                       // 1
									Fraction prtemp = g.T.get(tid).pr.add(pr);
									if (prtemp.compareTo(Fraction.ONE) <= 0)
										g.T.get(tid).pr = prtemp;
									// System.out.println("[Transition "+tid+"
									// new pr = ]"+g.T.get(tid).pr);
								}
								continue;
							}
						} else {// if (eid == -1) {
							CreateHPAState s = new CreateHPAState(g.V.size(),
							        end);
							s.prop.addAll(gf.V.get(t1.endID).prop);
							s.prop.addAll(gp.V.get(t2.endID).prop);
							if (s.prop.contains(failedServer + "FINAL")
							        && gp.F.contains(t2.endID)) {
								s.prop.add("FINAL");
								s.prop.add(g.prefix + "FINAL");
								g.F.add(s.id);
							}
							g.V.add(s);
							eid = s.id;
							Q.add(eid);
							q1.add(t1.endID);
							q2.add(t2.endID);
						}
						CreateHPATransition t = new CreateHPATransition(
						        g.T.size(), sid, a, eid, pr);
						t.propSatisfied.addAll(t1.propSatisfied);
						t.propSatisfied.addAll(t2.propSatisfied);
						g.T.add(t);
						g.V.get(sid).outTransID.add(g.T.size() - 1);
						// System.out.println("[Transition
						// "+t.id+"]"+source+"-"+a+"->"+end+": "+t.pr);
					} // sync ends
					else {
						String end = "", a = "";
						int eid = -1;
						// add s1 transitions
						if (gf.V.get(v1).outTransID.size() > 0
						        & gf.V.get(v1).outTransID.size() > i1
						        & !com.contains(t1.input)) {
							end = (v2 == v2error)
							        ? es.name
							        : (gf.V.get(t1.endID).name + "," + gp.V
							                .get(v2).name);
							if (gf.V.get(t1.endID).prop.contains(failedServer
							        + "FINAL")
							        && !gp.F.contains(v2)) {
								end = es.name;
							}
							Fraction pr = t1.pr;
							a = t1.input;
							g.V.get(sid).inputs.add(a);

							eid = g.findStateIDbyValue(end);
							if (eid != -1) {
								int tid = g.findTransitionIdBySourceEndInput(
								        sid, eid, a);
								if (tid != -1) {
									if (g.T.get(tid).pr
									        .compareTo(Fraction.ONE) < 0) {// pr
									                                       // <
									                                       // 1
										Fraction prtemp = g.T.get(tid).pr
										        .add(pr);
										if (prtemp.compareTo(Fraction.ONE) <= 0)
											g.T.get(tid).pr = prtemp;
										// System.out.println("[Transition
										// "+tid+" new pr = ]"+g.T.get(tid).pr);
									}
									continue;
								}
							} else {
								CreateHPAState s = new CreateHPAState(
								        g.V.size(), end);
								s.prop.addAll(gf.V.get(t1.endID).prop);
								s.prop.addAll(gp.V.get(v2).prop);
								if (s.prop.contains(failedServer + "FINAL")
								        && gp.F.contains(v2)) {
									s.prop.add("FINAL");
									s.prop.add(g.prefix + "FINAL");
									g.F.add(s.id);
								}
								g.V.add(s);
								eid = s.id;
								Q.add(eid);
								q1.add(t1.endID);
								q2.add(v2);
							}
							CreateHPATransition t = new CreateHPATransition(
							        g.T.size(), sid, a, eid, pr);
							t.propSatisfied.addAll(t1.propSatisfied);
							g.T.add(t);
							g.V.get(sid).outTransID.add(g.T.size() - 1);
							// System.out.println("[Transition
							// "+t.id+"]"+g.V.get(t.sourceID).name+"-"+a+"->"+end+":
							// "+t.pr);
						}
						// add s2 transitions
						if (com.contains(t2.input))
							continue;
						end = (t2.endID == v2error)
						        ? es.name
						        : (gf.V.get(v1).name + ","
						                + gp.V.get(t2.endID).name);
						if (gf.V.get(v1).prop.contains(failedServer + "FINAL")
						        && !gp.F.contains(t2.endID)) {
							end = es.name;
						}
						Fraction pr = t2.pr;
						a = t2.input;
						g.V.get(sid).inputs.add(a);
						eid = g.findStateIDbyValue(end);
						if (eid != -1) {
							int tid = g.findTransitionIdBySourceEndInput(sid,
							        eid, a);
							if (tid != -1) {
								if (g.T.get(tid).pr
								        .compareTo(Fraction.ONE) < 0) {// pr
								                                       // <
								                                       // 1
									Fraction prtemp = g.T.get(tid).pr.add(pr);
									if (prtemp.compareTo(Fraction.ONE) <= 0)
										g.T.get(tid).pr = prtemp;
									// System.out.println("[Transition "+tid+"
									// new pr = ]"+g.T.get(tid).pr);
								}
								continue;
							}
						} else {
							CreateHPAState s = new CreateHPAState(g.V.size(),
							        end);
							s.prop.addAll(gf.V.get(v1).prop);
							s.prop.addAll(gp.V.get(t2.endID).prop);
							if (s.prop.contains(failedServer + "FINAL")
							        && gp.F.contains(t2.endID)) {
								s.prop.add("FINAL");
								s.prop.add(g.prefix + "FINAL");
								g.F.add(s.id);
							}
							g.V.add(s);
							eid = s.id;
							Q.add(eid);
							q1.add(v1);
							q2.add(t2.endID);
						}
						CreateHPATransition t = new CreateHPATransition(
						        g.T.size(), sid, a, eid, pr);
						t.propSatisfied.addAll(t2.propSatisfied);
						g.T.add(t);
						g.V.get(sid).outTransID.add(g.T.size() - 1);
						// System.out.println("[Transition
						// "+t.id+"]"+g.V.get(t.sourceID).name+"-"+a+"->"+end+":
						// "+t.pr);
					}
				} // for g2.V.get(v2).outTransID
			} // for g1.V.get(v1).outTransID
		} // end of while (Q.size() > 0)

		return g;
	}

	/**
	 * Extend the model with failures. Since there is only one server working
	 * after the other failed, 1) if (isInterLeaving == false), the server will
	 * first complete the g1 session, then g2 session; 2) if (isInterLeaving ==
	 * true), the server will first execute g1 until its next input is not T,
	 * saying g1 has completed current external input, then execute g2 until its
	 * next input is not T, and so on and so forth, interleavingly execute g1
	 * and g2 until they both end; 3)then start a new session on the server that
	 * didn't fail. Failure probabilities are defined for specific states
	 * [optional] and symbols.
	 * 
	 * @param g1
	 * @param g2
	 *            g1 has priority over g2 after failure. But note this g1 and g2
	 *            may not be the same order with function PSCompPOR.
	 * @param failedserver
	 *            a single string denoting the prefix of failed server
	 * @param failedStateIDs
	 *            size X or 0, specifying states where failure is introduced.
	 *            It's defined for the specific process which failed.
	 * @param failedInputs
	 *            size X, specifying symbols on which failure occurs
	 * @param r
	 *            size X, specifying the probability of the failure is
	 *            introduced
	 * @param failmark
	 *            the string to be added to the name of the failed states.
	 * @param isInterLeaving
	 *            if True, after failure, each session execute an external input
	 *            interleavingly
	 */
	public void addFailure(CreateHPA g1, CreateHPA g2, String failedserver,
	        ArrayList<Integer> failedStateIDs, ArrayList<String> failedInputs,
	        ArrayList<Fraction> r, String failmark, boolean isInterLeaving)
	        throws Exception {
		if (failedStateIDs.isEmpty()
		        && util.countUniqueElements(failedInputs) != failedInputs
		                .size()) {
			throw new Exception(
			        "ERROR: duplicate failure definitions addressed./n");
		}

		int fs = g1.prefix.equalsIgnoreCase(failedserver) ? 1 : 2; // 1 denotes
		                                                           // g1, 2
		                                                           // denotes
		                                                           // g2
		boolean g1g2 = true;
		// true iff same priority as defined doing composition before failure
		// i.e. gf's state's name is like "g1prefix,g2prefix"
		if (V.get(0).name.split(",")[0].contains(g1.prefix)) {
			g1g2 = true; // g1 = session 1, g2 = session 2
			if (fs == 1) {
				name = g1.name + "f" + g2.name;// "g1fg2"
				prefix = g1.prefix + "f" + g2.prefix;
			} else {
				name = g1.name + g2.name + "f";// "g1g2f"
				prefix = g1.prefix + g2.prefix + "f";
			}
		} else {
			g1g2 = false; // g2 = session 1, g1 = session 2
			if (fs == 2) {
				name = g1.name + g2.name + "f";// "g2g1f"
				prefix = g1.prefix + g2.prefix + "f";
			} else {
				name = g1.name + "f" + g2.name;// "g2fg1"
				prefix = g1.prefix + "f" + g2.prefix;
			}
		}
		if (isInterLeaving) {
			prefix = prefix + "i";
			name = name + "i";
		}

		/* add failure points */
		Queue<Integer> Q = new LinkedList<Integer>(); // failure points id;
		Queue<Integer> Q1 = new LinkedList<Integer>();// g1
		Queue<Integer> Q2 = new LinkedList<Integer>();// g2
		Queue<Integer> NS = new LinkedList<Integer>();
		// NS stores which session to execute next in isInterleaving situation
		// nsQ and nsNS store NS for added states in isInterleaving situation
		ArrayList<Integer> nsQ = new ArrayList<Integer>();
		ArrayList<Integer> nsNS = new ArrayList<Integer>();

		// int nf = FinalID.size();
		int TN = T.size();
		for (int j = 0; j < failedInputs.size(); j++) {
			String symbol = failedInputs.get(j);
			Fraction pr = r.get(j);
			int failedStateID = failedStateIDs.isEmpty() ? -1 : failedStateIDs
			        .get(j);

			for (int i = 0; i < TN; i++) {
				CreateHPATransition t = T.get(i);
				if (!t.input.equals(symbol))
					continue;

				if (!failedStateIDs.isEmpty()) {// failure defined on states
					int sfid = -1;
					if (g1g2) {
						if (failedserver.equals(g1.prefix))
							sfid = g1
							        .findStateIDbyValue(
							                V.get(t.sourceID).name
							                        .split(",")[0]);
						else
							sfid = g2
							        .findStateIDbyValue(
							                V.get(t.sourceID).name
							                        .split(",")[1]);
					} else {
						if (failedserver.equals(g1.prefix))
							sfid = g1
							        .findStateIDbyValue(
							                V.get(t.sourceID).name
							                        .split(",")[1]);
						else
							sfid = g2
							        .findStateIDbyValue(
							                V.get(t.sourceID).name
							                        .split(",")[0]);
					}
					if (sfid != failedStateID) {// && failedStateID!=-1) {
						// t.sourceID at failed server != failedStateID
						continue;
					}
				}

				String value = V.get(t.endID).name.replaceFirst(
				        failedserver, failedserver + failmark);
				int sid = findStateIDbyValue(value);
				if (sid != -1) {
					continue;
				}

				CreateHPAState s = new CreateHPAState(V.size(), value);
				s.prop.add(prefix + "FAILED");
				s.prop.addAll(V.get(t.endID).prop);
				V.add(s);// id,value
				if (s.prop.contains(prefix + "INITIAL"))
					q0 = s.id;
				if (s.prop.contains(prefix + "FINAL"))
					F.add(s.id);
				sid = s.id;

				CreateHPATransition t2add = new CreateHPATransition(
				        T.size(), t.sourceID, t.input, sid,
				        t.pr.multiply(pr));
				t2add.propSatisfied.add(prefix + "FAILED");
				t2add.propSatisfied.add(failedserver);
				T.add(t2add);
				t.pr = t.pr.multiply(Fraction.ONE.subtract(pr));
				V.get(t.sourceID).outTransID.add(T.size() - 1);
				Q.add(sid);
				NS.add(1);// session to execute after failure is g1, but yield
				          // to T constraints
				nsQ.add(sid);
				nsNS.add(1);

				if (g1g2) {
					int q1 = g1.findStateIDbyValue(V.get(t.endID).name
					        .split(",")[0]);
					int q2 = g2.findStateIDbyValue(V.get(t.endID).name
					        .split(",")[1]);
					if (q1 < 0 || q2 < 0) {
						throw new Exception(
						        "Wrongly scanning failed transtions that're just added.");
					}
					Q1.add(q1);
					Q2.add(q2);
				} else {
					int q1 = g1.findStateIDbyValue(V.get(t.endID).name
					        .split(",")[1]);
					int q2 = g2.findStateIDbyValue(V.get(t.endID).name
					        .split(",")[0]);
					if (q1 < 0 || q2 < 0) {
						throw new Exception(
						        "Wrongly scanning failed transtions that're just added.");
					}
					Q1.add(q1);
					Q2.add(q2);
				}

			}
		}
		// io.printPA2console(this,"initial failure points added:");

		/*
		 * extend failure points. no synchronization after failure.
		 * 
		 * if both T next or no T next, the one with priority first;if one T
		 * next, execute it ignoring priority; *
		 */
		while (Q.size() > 0) {
			int sid = Q.poll();
			int v1 = Q1.poll(), v2 = Q2.poll();
			int ns = NS.poll();

			for (int i = 0; i < g1.V.get(v1).outTransID.size(); i++) {
				if (g1.F.contains(v1) && (!g2.F.contains(v2) || fs == 1)) {
					// g1 will not start a new session until g2 ends.
					// after they both end, only g1 will start a new session.
					break;
				}

				if (!g1.V.get(v1).inputs.contains("T")
				        && g2.V.get(v2).inputs.contains("T")) {
					// g1 next is not T, while g2 next is T.
					// ensured even not interleaving,
					// so update it here rather than initial failure points
					ns = 2;
					break;
				}
				if (isInterLeaving && ns != 1)
					break;

				int c = g1.V.get(v1).outTransID.get(i);
				CreateHPATransition t1 = g1.T.get(c);
				String end = "";
				if (g1g2) {
					end = g1.V.get(t1.endID).name + "," + g2.V.get(v2).name;
				} else {
					end = g2.V.get(v2).name + "," + g1.V.get(t1.endID).name;
				}

				end = end.replaceFirst(failedserver, failedserver + failmark);
				String a = t1.input;
				V.get(sid).inputs.add(a);

				int ns1 = 2; // decide which session to execute next
				if (g1.V.get(t1.endID).inputs.contains("T")) {
					// && g1.V.get(t1.endID).inputs.size() == 1
					ns1 = 1;
				} else if (!g1.F.contains(t1.endID) && g2.F.contains(v2)) {
					ns1 = 1;
				} else if (g1.F.contains(t1.endID) && g2.F.contains(v2)
				        && fs == 2) {
					ns1 = 1;
				}

				int eid = findStateIDbyValue(end);
				if (eid != -1) {// existing state
					// 1) if not interleaving (always priority takes over),
					// merge the same eid, and do not need to add eid into Q,
					// also add transitions if it doesn't exist.
					// 2) if interleaving,
					// check stored "ns" information with existing eid,
					// if same "ns", treat same as not interleaving,
					// else create new state instead.
					// if on the new state the updated eid' still exists,
					// treat same as not interleaving.
					if (!isInterLeaving
					        && findTransitionIdBySourceEndInput(sid, eid,
					                a) != -1)
						continue;// for g1.V.get(v1).outTransID
					if (isInterLeaving) {
						if (ns1 == nsNS.get(nsQ.indexOf(eid))) {
							if (findTransitionIdBySourceEndInput(sid, eid,
							        a) != -1)
								continue;
						} else {// new state, with sid in name
							end = "sid" + sid + "i_" + end;
							eid = findStateIDbyValue(end);
							if (eid != -1
							        && findTransitionIdBySourceEndInput(
							                sid, eid, a) != -1) {
								continue;
							}
						}
					}
				}
				if (eid == -1) { // new state
					// note this if statement cannot change to else for
					// isInterLeaving situation
					CreateHPAState s = new CreateHPAState(V.size(), end);
					s.prop.add(prefix + "FAILED");
					s.prop.addAll(g1.V.get(t1.endID).prop);
					s.prop.addAll(g2.V.get(v2).prop);
					if (g1.F.contains(t1.endID) && g2.F.contains(v2)) {
						s.prop.add(prefix + "FINAL");
						F.add(s.id);
					}
					V.add(s);
					eid = s.id;
					// update queues, !Q.contains(eid) since eid=-1 before
					Q.add(eid);
					nsQ.add(eid);
					Q1.add(t1.endID);
					Q2.add(v2);
					NS.add(ns1);
					nsNS.add(ns1);
				}
				// add new transition if eid==-1, new state;
				// or eid!=-1, but the transition doesn't exist
				CreateHPATransition t = new CreateHPATransition(T.size(),
				        sid, a, eid, t1.pr);
				t.propSatisfied.add(prefix + "FAILED");
				t.propSatisfied.addAll(t1.propSatisfied);
				T.add(t);
				V.get(sid).outTransID.add(t.id);

			} // finish extending g1 branch

			if (g1.V.get(v1).outTransID.size() == 0
			        || (g1.F.contains(v1) && !g2.F.contains(v2))
			        || (g1.F.contains(v1) && g2.F.contains(v2) && fs == 1)
			        || (isInterLeaving && ns == 2)
			        || (!g1.V.get(v1).inputs.contains("T") // ensure T >
			                                               // priority
			                && g2.V.get(v2).inputs.contains("T"))) {

				for (int i = 0; i < g2.V.get(v2).outTransID.size(); i++) {
					int c = g2.V.get(v2).outTransID.get(i);
					CreateHPATransition t2 = g2.T.get(c);
					String end = "";
					if (g1g2) {
						end = g1.V.get(v1).name + "," + g2.V.get(t2.endID).name;
					} else {
						end = g2.V.get(t2.endID).name + "," + g1.V.get(v1).name;
					}
					end = end.replaceFirst(failedserver, failedserver
					        + failmark);
					String a = t2.input;
					V.get(sid).inputs.add(a);

					int ns1 = 1; // decide which session to execute next
					if (g2.V.get(t2.endID).inputs.contains("T")) {
						// && g2.V.get(t2.endID).inputs.size() == 1
						ns1 = 2;
					} else if (!g2.F.contains(t2.endID) && g1.F.contains(v1)) {
						ns1 = 2;
					} else if (g2.F.contains(t2.endID) && g1.F.contains(v1)
					        && fs == 1) {
						ns1 = 2;
					}
					int eid = findStateIDbyValue(end);
					if (eid != -1) {
						if (!isInterLeaving
						        && findTransitionIdBySourceEndInput(sid,
						                eid, a) != -1)
							continue;// for g2.V.get(v2).outTransID
						if (isInterLeaving) {
							if (ns1 == nsNS.get(nsQ.indexOf(eid))) {
								if (findTransitionIdBySourceEndInput(sid,
								        eid, a) != -1)
									continue;
							} else {// new state, with sid in name
								end = "sid" + sid + "i_" + end;
								eid = findStateIDbyValue(end);
								if (eid != -1
								        && findTransitionIdBySourceEndInput(
								                sid, eid, a) != -1) {
									continue;
								}
							}
						}
					}
					if (eid == -1) {
						CreateHPAState s = new CreateHPAState(V.size(),
						        end);
						s.prop.add(prefix + "FAILED");
						s.prop.addAll(g1.V.get(v1).prop);
						s.prop.addAll(g2.V.get(t2.endID).prop);
						if (g1.F.contains(v1) && g2.F.contains(t2.endID)) {
							s.prop.add(prefix + "FINAL");
							F.add(s.id);
						}
						V.add(s);
						eid = s.id;
						// update queues, !Q.contains(eid) since eid=-1 before
						Q.add(eid);
						nsQ.add(eid);
						Q1.add(v1);
						Q2.add(t2.endID);
						NS.add(ns1);
						nsNS.add(ns1);
					}
					CreateHPATransition t = new CreateHPATransition(
					        T.size(), sid, a, eid, t2.pr);
					t.propSatisfied.add(prefix + "FAILED");
					t.propSatisfied.addAll(t2.propSatisfied);
					T.add(t);
					V.get(sid).outTransID.add(t.id);
				}
			} // finish adding g2 transitions
		} // while (Q.size() > 0)
	}// AddFailure

	/**
	 * Rename (i.e. update values) the states in the graph, adding prefix and/or
	 * surfix.
	 */
	public void renameStates(String prefix, String surfix) {
		for (int i = 0; i < V.size(); i++) {
			V.get(i).name = prefix + V.get(i).name + surfix;
		}
	}

	/**
	 * Rename state props
	 */
	public void renameStateProp(String prefix, String surfix) {
		for (CreateHPAState s : V) {
			for (String p : s.prop) {
				s.replaceProp(p, prefix + p + surfix);
			}
		}
	}

	/**
	 * Rename specified props
	 */
	public void renameStateProp(String prefix, String surfix,
	        Set<String> props) {
		for (int i = 0; i < V.size(); i++) {
			CreateHPAState s = V.get(i);
			Set<String> P = util.intersection(props, s.prop);
			if (P.isEmpty())
				continue;
			for (String p : P) {
				s.replaceProp(p, prefix + p + surfix);
			}
		}
	}

	/**
	 * Initiate transition props with prefix of the graph
	 */
	public void iniTransPropSatisfied() {
		for (CreateHPATransition t : T) {
			t.propSatisfied.clear(); // initialize
			t.propSatisfied.add(prefix);
		}
	}

	/**
	 * Initiate transition props with specified prop
	 */
	public void iniTransPropSatisfied(String p) {
		for (CreateHPATransition t : T) {
			t.propSatisfied.clear(); // initialize
			t.propRequired.add(p);
		}
	}

	/**
	 * Add specified prop to each transition
	 */
	public void addTransPropRequired(String p) {
		for (CreateHPATransition t : T) {
			t.propRequired.add(p);
		}
	}

	/**
	 * Rename transition props
	 */
	public void renameTransProp(String prefix, String surfix) {
		for (CreateHPATransition t : T) {
			for (String p : t.propSatisfied) {
				t.replaceProp(p, prefix + p + surfix);
			}
		}
	}

	/**
	 * Rename the symbols in the graph, adding prefix and/or surfix. Symbols in
	 * the set "com" will not be renamed
	 */
	public void renameSymbolsExceptCom(String prefix, String surfix,
	        Set<String> com) {
		symbols.clear();
		for (CreateHPAState s : V) {
			s.inputs.clear();
			for (int j = 0; j < s.outTransID.size(); j++) {
				String temp = T.get(s.outTransID.get(j)).input;
				if (com.contains(temp)) {
					s.inputs.add(temp);
					symbols.add(temp);
					continue;
				}
				temp = prefix + temp + surfix;
				T.get(s.outTransID.get(j)).input = temp;
				s.inputs.add(temp);
				symbols.add(temp);
			}
		}

	}

	/**
	 * Rename the symbols in the graph, adding prefix and/or surfix. ONLY
	 * Symbols in the set "com" will be renamed.
	 */
	public void renameSymbolsInCom(String prefix, String surfix,
	        Set<String> com) {
		symbols.removeAll(com);
		for (CreateHPAState s : V) {
			s.inputs.removeAll(com);
			for (int j = 0; j < s.outTransID.size(); j++) {
				String temp = T.get(s.outTransID.get(j)).input;
				if (!com.contains(temp))
					continue;

				temp = prefix + temp + surfix;
				T.get(s.outTransID.get(j)).input = temp;
				s.inputs.add(temp);
				symbols.add(temp);
			}
		}

	}

	public int findStateIDbyValue(String value) {
		for (CreateHPAState s : V) {
			if (s.name.equals(value))
				return s.id;
		}
		return -1;
	}

	public int findStateIdxByID(int id) {
		for (int i = 0; i < V.size(); i++) {
			if (V.get(i).id == id)
				return i;
		}
		return -1;
	}

	public int findTransitionIdxByID(int id) {
		for (int i = 0; i < T.size(); i++) {
			if (T.get(i).id == id)
				return i;
		}
		return -1;
	}

	/**
	 * Find all transitions with the same sourceID and endID as specified
	 */

	public Set<Integer> findTransitionIdBySourceEnd(int s, int e) {
		Set<Integer> IDs = new HashSet<Integer>();
		for (int i : V.get(s).outTransID) {
			if (T.get(i).endID == e)
				IDs.add(i);
		}
		return IDs;
	}

	/**
	 * Find the transition with specified sourceID, endID, and input
	 */
	public int findTransitionIdBySourceEndInput(int s, int e, String input) {
		for (int i : V.get(s).outTransID) {
			if (T.get(i).endID == e && T.get(i).input.equals(input))
				return i;
		}
		return -1;
	}

	public Set<Integer> findTransitionIDsOnA(String a) {
		Set<Integer> I = new HashSet<Integer>();
		for (CreateHPATransition t : T) {
			if (t.input.equals(a)) {
				I.add(t.id);
			}
		}
		return I;
	}

	/**
	 * Obtain the adjacent out list of the graph
	 */
	@SuppressWarnings("unchecked")
	public HashSet<Integer>[] adj_out_int() {
		int NV = V.size();
		HashSet<Integer>[] adj_out_int =
		        (HashSet<Integer>[]) new HashSet[NV];
		for (int v = 0; v < NV; v++) { // Nodes values: 0,...,NV-1
			adj_out_int[v] = out_state_ids(v);
		}
		return adj_out_int;
	}

	/**
	 * Return a set of integer state IDs that are next neighbors of specified
	 * state.
	 */
	protected HashSet<Integer> out_state_ids(int stateID) {
		HashSet<Integer> O = new HashSet<Integer>();
		for (int i : V.get(stateID).outTransID) {
			O.add(T.get(i).endID);
		}
		return O;
	}

	/**
	 * Return a set of integer transitions IDs that are incoming edges of
	 * specified state.
	 */
	protected ArrayList<Integer> in_trans_ids(int stateID) {
		ArrayList<Integer> I = new ArrayList<Integer>();
		for (CreateHPATransition t : T) {
			if (t.endID == stateID) {
				I.add(t.id);
			}
		}
		return I;
	}

	/**
	 * Remove unreachable states and transitions from current PA. ReassignID()
	 * and updateSymbols() called at the end.
	 */
	public void RemoveUnreachable() {
		HashSet<Integer>[] adj_out = adj_out_int();
		TarjanSCC tjscc = new TarjanSCC(V.size());
		int visited = tjscc.dfs(adj_out, q0);
		if (visited < V.size()) {
			// Remove all unreachable states and their out transitions
			for (int id = 0; id < tjscc.marked.length; id++) {
				if (!tjscc.marked[id]) {// the state is unreachable
					for (int tid : V
					        .get(findStateIdxByID(id)).outTransID) {
						T.remove(findTransitionIdxByID(tid));
					}
					V.remove(findStateIdxByID(id));
				}
			}
			// update IDs and symbols
			reAssignID();
			updateSymbols();
		}
	}

	/**
	 * Eliminate all T-transitions and then removeUnrechable(). Pre-conditions:
	 * checkT() and hasAbeforeT() is true.
	 * 
	 * @throws Exception
	 *             if the graph is not well formed.
	 */
	public void removeTTransitions() throws Exception {
		if (!symbols.contains("T"))
			return;
		boolean hasT = true;
		while (hasT) {
			hasT = false;
			for (CreateHPAState s : V) {
				if (s.inputs.contains("T") || s.outTransID.isEmpty())
					continue;
				int outTransSize = s.outTransID.size();
				for (int index = 0; index < outTransSize; index++) {
					int tid = s.outTransID.get(index);
					CreateHPATransition ot = T.get(tid);
					CreateHPAState nexts = V.get(ot.endID);
					if (!nexts.inputs.contains("T"))
						continue;
					// else, nexts.inputs = {T} according to checkT()
					hasT = true;
					ot.endID = T.get(nexts.outTransID.get(0)).endID;
					if (nexts.outTransID.size() > 1)
						throw new Exception("\nPoorly defined " + nexts.name
						        + "'s out transitions.");
				}
			}
		}
		RemoveUnreachable();

		// mergeRedundantTransitions();
	}

	/**
	 * Deal with cases such as "6 1Sa 1 9/10 1 1/10", merge redundant
	 * transitions. NO LONGER USED because pr. are added in PSCompMergeERROR.
	 */
	@SuppressWarnings("unused")
	private void mergeRedundantTransitions() {
		Set<Integer> t2remove = new HashSet<Integer>();
		for (CreateHPAState s : V) {
			HashMap<String, Integer> next2trans =
			        new HashMap<String, Integer>();
			// nextState # input => transition id
			for (int i = s.outTransID.size() - 1; i >= 0; i--) {
				int tid = s.outTransID.get(i);
				int nextState = T.get(tid).endID;
				String input = T.get(tid).input;
				Fraction pr = T.get(tid).pr;
				Integer tid0 = next2trans.get(nextState + "###" + input);
				if (tid0 != null) { // to merge
					T.get(tid0).pr = T.get(tid0).pr.add(pr);
					t2remove.add(tid);
					s.outTransID.remove(i);
				} else {
					next2trans.put(nextState + "###" + input, tid);
				}
			}
		} // for state s
		for (Integer i : t2remove) {
			T.remove(i);
		}
		reAssignID();
	}

	/**
	 * ensure every T-transition has an external input before T. Return null if
	 * okay, or the state's name if violated.
	 */
	public String hasAbeforeT() {
		for (CreateHPAState s : V) {
			if (!s.inputs.contains("T")) {
				continue;
			}
			if (in_trans_ids(s.id).isEmpty())
				return s.name;

			for (int itid : in_trans_ids(s.id)) {
				if (T.get(itid).input.contains("new")) {
					return s.name;
				}
			}
		}
		return null;
	}

	/**
	 * Re-assign id for states and transitions, s.t. id = idx.
	 * 
	 * Update initial states and final states at the end.
	 */
	private void reAssignID() {
		int NV = V.size();
		int NT = T.size();

		for (CreateHPAState v : V) {
			int N = v.outTransID.size();
			ArrayList<Integer> ot = new ArrayList<Integer>(N);
			for (int i = 0; i < N; i++) {
				ot.add(findTransitionIdxByID(v.outTransID.get(i)));
			}
			v.outTransID.clear();
			v.outTransID = ot;
		}

		for (CreateHPATransition t : T) {
			t.sourceID = findStateIdxByID(t.sourceID);
			t.endID = findStateIdxByID(t.endID);
		}

		for (int i = 0; i < NV; i++) {
			V.get(i).id = i;
		}

		for (int i = 0; i < NT; i++) {
			T.get(i).id = i;
		}

		updateInitialStates();
		updateFinalStates();
	}

	/**
	 * Update symbols according to transition' input field
	 */
	private void updateSymbols() {
		symbols.clear();
		for (CreateHPATransition t : T) {
			symbols.add(t.input);
		}
	}

	/**
	 * Update final states according to prefix+FINAL proposition
	 */
	private void updateFinalStates() {
		F.clear();
		for (CreateHPAState s : V) {
			if (s.prop.contains(prefix + "FINAL")) {
				F.add(s.id);
			}
		}
	}

	/**
	 * Update initial states according to prefix+INITIAL proposition.
	 * 
	 * @return True if exactly 1 initial state; False else.
	 */
	private boolean updateInitialStates() {
		q0 = -1;
		int q0Count = 0;
		for (CreateHPAState s : V) {
			if (s.prop.contains(prefix + "INITIAL")) {
				q0 = s.id;
				q0Count++;
			}
		}
		if (q0Count == 1)
			return true;
		else
			return false;
	}

	/**
	 * Determines a PA. Add an ERROR state, and then for each state and
	 * undefined inputs, add an edge to ERROR state (on common inputs or inputs
	 * from the same server that the property is defined on), or a self-loop (on
	 * inputs from other server).
	 * 
	 * @param symbols
	 *            need to be determinised for all symbols in "symbols"
	 * @param com
	 * 
	 * @throws Exception
	 */
	public void DeterminePropertyAutomata(Set<String> symbols, Set<String> com,
	        String pre) throws Exception {
		symbols.addAll(symbols);
		String value = "ERROR";
		int sid = findStateIDbyValue(value);
		if (sid == -1) {
			sid = V.size();
			CreateHPAState s = new CreateHPAState(sid, value);
			s.prop.add("ERROR");
			V.add(s);
		} else {
			throw new Exception("Error in function Determine of PA "
			        + name + ": ERROR states occupied");
		}

		for (CreateHPAState s : V) {
			for (String input : symbols) {
				if (!s.inputs.contains(input)) {// add an edge
					if (input.contains(pre) || com.contains(input)) { // "T"
						// symbols from the same server which gp is defined on;
						// goes to ERROR state
						CreateHPATransition t = new CreateHPATransition(
						        T.size(), s.id, input, sid);

						T.add(t);
						s.outTransID.add(t.id);
					} else {// stays
						CreateHPATransition t = new CreateHPATransition(
						        T.size(), s.id, input, s.id);
						T.add(t);
						s.outTransID.add(t.id);
					}
					s.inputs.add(input);
				}
			}
		}
	}

	/**
	 * Determines a PA. Add an ERROR state, and then for each state and
	 * undefined inputs, add an edge to ERROR state.
	 * 
	 * @throws Exception
	 */
	public void Determine() throws Exception {
		symbols.addAll(symbols);
		String value = "ERROR";
		int sid = findStateIDbyValue(value);
		if (sid == -1) {
			sid = V.size();
			CreateHPAState s = new CreateHPAState(sid, value);
			s.prop.add("ERROR");
			V.add(s);
		}

		for (CreateHPAState s : V) {
			for (String input : symbols) {
				if (!s.inputs.contains(input)) {// add an edge leading to error
				                                // state
					CreateHPATransition t = new CreateHPATransition(
					        T.size(), s.id, input, sid);
					T.add(t);
					// t.prop.add("ERROR");
					s.outTransID.add(t.id);
					s.inputs.add(input);
				}
			}
		}
	}

	/**
	 * Print out a whole CreateHPA to console.
	 */
	public void printPA2console(String message) {
		if (message != null)
			System.out.println("\n" + message + "\n");
		System.out.println(name + " has " + V.size() + " states, " + "and "
		        + T.size() + " transitions.");
		System.out.println("Initial state: " + q0 + ";\n"
		        + "Final states: " + F.toString());
		System.out.println("Symbols: " + symbols);
		System.out.println("States (" + V.size() + " totally):");
		for (CreateHPAState s : V) {
			// printState(s);
			System.out.println("State id=" + s.id + ": value=" + s.name
			        + " out-trans=" + s.outTransID.toString() + " inputs="
			        + s.inputs + " Prop=" + s.prop);
		}
		System.out.println("Transitions (" + T.size() + " totally):");
		for (CreateHPATransition t : T) {
			// printTransition(t);
			System.out.println("Tran id=" + t.id + ": "
			        + V.get(t.sourceID).name + " - " + t.input + " -> "
			        + V.get(t.endID).name + " w/ pr="
			        + t.pr.toString().replaceAll(" ", "") + " Prop="
			        + t.propSatisfied);
		}
	}

	/**
	 * Output the whole CreateHPA into a plain file, for record/debugging use
	 * only.
	 * 
	 * @param filename
	 *            Specified file name.
	 * @param comment
	 *            Will be written as comment in output file.
	 * @throws IOException
	 */
	public void writeCreateHPA2file(String filename,
	        String comment) throws IOException {
		File file = new File(filename + "_plain");
		BufferedWriter o = new BufferedWriter(new FileWriter(file));
		if (comment != null && !comment.isEmpty())
			o.write("//" + comment + "\n");
		o.write(V.size() + "\n");
		o.write(name + " w/ prefix " + prefix + " has " + V.size()
		        + " states, " + T.size() + " transitions, and "
		        + symbols.size() + " symbols.\n");
		o.write("Initial state: " + q0 + ";\n" + "Final states: "
		        + F.toString() + "\n");
		o.write("Symbols (" + symbols.size() + " totally):\n" + symbols
		        + "\n");
		o.write("States (" + V.size() + " totally):\n");
		for (CreateHPAState s : V) {
			o.write("State id=" + s.id + ": value=" + s.name + " out-trans="
			        + s.outTransID.toString() + " inputs=" + s.inputs
			        + " Prop=" + s.prop + "\n");
		}
		o.write("Transitions (" + T.size() + " totally):\n");
		for (CreateHPATransition t : T) {
			o.write("Tran id=" + t.id + ": " + V.get(t.sourceID).name + " - "
			        + t.input + " -> " + V.get(t.endID).name + " w/ pr="
			        + t.pr.toString().replaceAll(" ", "") + " PropSatisfied="
			        + t.propSatisfied + " PropRequired=" + t.propRequired
			        + "\n");
		}
		o.close();
	}

	/**
	 * Output CreateHPA to standard HPA tool formatted file, ready as input for
	 * HPA Verification.
	 * 
	 * @param filename
	 *            Specified file name.
	 * @param comment
	 *            Will be written as comment in output file.
	 * @throws IOException
	 */
	public void writeCreateHPA2HPA(String filename,
	        String comment) throws IOException {
		File file = new File(filename + ".hpa");
		BufferedWriter o = new BufferedWriter(new FileWriter(file));
		if (comment != null && !comment.isEmpty())
			o.write("//" + comment + "\n");
		o.write(V.size() + "\n");
		// states
		o.write("//States.size()=" + V.size() + "\n");
		for (CreateHPAState s : V) {
			o.write(s.name + " ");
			for (String p : s.prop) {
				o.write("#" + p + " ");
			}
			o.write("\n");
		}
		// alphabet
		// transitions
		o.write("//Transitions.size()=" + T.size() + "\n");
		for (CreateHPAState s : V) {
			for (String in : s.inputs) {
				o.write(s.id + " " + in + " ");
				for (int oid : s.outTransID) {
					CreateHPATransition t = T.get(oid);
					if (t.input.equals(in)) {
						o.write(t.endID + " "
						        + t.pr.toString().replaceAll(" ", "") + " ");
					}
				}
				o.write("\n");
			}
		}
		// initial state
		// final states

		o.close();
		// System.out.println(name + " written to file.");
	}

	/**
	 * Output the CreateHPA to file in FAT tool format.
	 * http://cl-informatik.uibk.ac.at/software/fat/grammar.php
	 * 
	 * @param filename
	 *            Specified file name.
	 * @param comment
	 *            Will be written as comment in output file.
	 * @throws IOException
	 */
	public void writeCreateHPA2FAT(String filename,
	        String comment) throws IOException {
		File file = new File(filename + "_fat");
		BufferedWriter o = new BufferedWriter(new FileWriter(file));
		Set<String> symbols = new HashSet<String>();
		if (comment != null && !comment.isEmpty())
			o.write("//" + comment + "\n");
		o.write("NFA " + name + " = {\n");
		// states
		o.write("states = {");
		for (CreateHPAState s : V) {
			// o.write(s.id + "_" + s.name + " ");
			o.write(s.name + " ");
		}
		o.write("}\n");
		o.write("//States.size()=" + V.size() + "\n");

		// transitions
		o.write("transitions = {\n");
		o.write("//Transitions.size()=" + T.size() + "\n");
		for (CreateHPATransition t : T) {
			// e. q2-a->q2
			// e. q1-b->{q2 q0}
			String input = // t.id+"000"+t.input + "" +
			               // t.pr.toString().replace(" / ", "00");
			        t.input;
			/*
			 * o.write(V.get(t.sourceID).id + "_" + V.get(t.sourceID).name +
			 * "-" + input + "->" + V.get(t.endID).id + "_" +
			 * V.get(t.endID).name);
			 */
			o.write(V.get(t.sourceID).name + "-" + input + "->"
			        + V.get(t.endID).name);
			symbols.add(input);
			o.write("\n");
		}
		o.write("}\n");
		// alphabet
		o.write("alphabet = {");
		for (String s : symbols) {
			o.write(s + " ");
		}
		o.write("}\n");
		// initial state
		o.write("initial state = " + V.get(q0).name);
		o.write("\n");
		// final states
		o.write("final states = {");
		for (int i : F) {
			// o.write(V.get(i).id + "_" + V.get(i).name + " ");
			o.write(V.get(i).name + " ");
		}
		o.write("}\n");

		o.write("}\n");
		o.close();
		// System.out.println(name + " written to file.");
	}
}