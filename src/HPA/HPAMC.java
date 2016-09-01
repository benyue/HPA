package HPA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.fraction.Fraction;

import HPBA.HPBA;
import Util.RobustMessage;
import Util.VerificationMessage;
import Util.message;
import Util.util;

/**
 * @author Cindy Yue Ben
 */
public class HPAMC {
	// for each witness set w: i => probk(w,i). k=1,2,...
	private ArrayList<HashMap<Long, Vali_Probk>> PROBK =
	        new ArrayList<HashMap<Long, Vali_Probk>>();
	public Fraction BKD_x = null;// max non-empty
	public Long BKD_k = 0L; // record when bkd terminates

	public ArrayList<Long> FWD = new ArrayList<Long>();
	public HashMap<String, Long> FWD_Val = new HashMap<String, Long>();
	public Long FWD_k = 0L; // record when FWD terminates

	/**
	 * FWD checking.
	 * 
	 * @param robustness
	 *            true if called by robustness alg, no need to trace back.
	 */
	public VerificationMessage fwdCheckX(PA g, Fraction x, Long L,
	        boolean robustness) throws Exception {
		this.resetFWD(g);// required for every new x
		VerificationMessage m = new VerificationMessage();
		m.A = alg.FWD;
		m.x = x;
		if (g.F.isEmpty()) {
			m.isEmpty = true;
			m.note = "Empty F";
			m.ws = g.WS0s.size() + g.WS1s.size();
			return m;
		}
		long t0 = util.getCPUTime() / 1000000;
		// boolean hasF0 = !g.obtainSetOnLevel(g.F, 0).isEmpty();
		// i starts from 0
		// Val values denotes the prob of reaching gws,
		// and there is still distance 1 from every state in the gws to final
		// states, so i+1 is the length of string accepted.
		long i = 0;
		while (i <= g.WS0s.size() + g.WS1s.size()) {// length
			// printFWDResult(g);
			String val_string = "";
			int countGT1 = 0;
			int repeatVal = 0;
			for (int w = 0; w < g.WS0s.size() + g.WS1s.size(); ++w) {
				WitnessSet WS = null;
				Fraction val;
				if (w < g.WS0s.size()) {// WS0
					WS = g.WS0s.get(w);
					val = this.Val_WS0Q0(g, (WitnessSet_0Q0) WS, x, i);
				} else {// WS1
					WS = g.WS1s.get(w - g.WS0s.size());
					val = this.Val_WS1Q0(g, (WitnessSet_1Q0) WS, x, i);
				}
				val_string += val.toString() + ",";
				if (val.compareTo(Fraction.ZERO) < 0
				        || WS.isSuperGoodWS1 && /// hasF0&& removed 07092016
				                val.compareTo(Fraction.ZERO) >= 0
				                && val.compareTo(Fraction.ONE) < 0) {
					FWD_k = i;
					m.isEmpty = false;
					m.length = i;
					m.note = "Val Termination";
					long t1 = util.getCPUTime() / 1000000;
					m.time = t1 - t0;
					m.ws = g.WS0s.size() + g.WS1s.size();

					if (!robustness)
						m.trace = this.fwdTracingBack(g, WS, i);
					// printFWDResult(g);
					return m;
				}
				if (val.compareTo(Fraction.ONE) >= 0)// (val >= 1)
					++countGT1;
				if (WS.repeat_WS_VAL(i) != null
				        && !WS.repeat_WS_VAL(i).isEmpty())
					++repeatVal;
			} // for each ws0 and each ws1
			if (countGT1 == g.WS0s.size() + g.WS1s.size()) {
				// val>=1 for every WS at the same i => empty
				m.note = "Val>1 for all W";
				m.length = i;
				break;
			} else if (repeatVal == g.WS0s.size() + g.WS1s.size()) {// TODO
				m.note = "Repeated WS_VAL";
				m.length = i;
				break;
			} else if (FWD_Convergent(val_string, i)) {
				m.note = "Forward Convergence";
				m.length = i - 1;
				break;
			}
			++i;
		} // for i
		if (i <= g.WS0s.size() + g.WS1s.size()) {
			m.isEmpty = true;
		} else {
			m.isEmpty = false;
			m.length = i;
			m.note = "L=|WS|";
		}
		FWD_k = m.length;
		long t1 = util.getCPUTime() / 1000000;
		m.time = t1 - t0;
		m.ws = g.WS0s.size() + g.WS1s.size();
		// printFWDResult(g);
		return m;
	}

	private void printFWDResult(PA g) {
		System.out.println();
		for (WitnessSet_0Q0 WS : g.WS0s) {
			System.out.println(
			        "WS0 id=" + WS.id + ",nodes=" + g.WS0nodes.get(WS.id));
			System.out.println("Waq:" + WS.Waq.toString());
			System.out.print("VALI:");
			WS.printVALI();
		}
		for (WitnessSet_1Q0 WS : g.WS1s) {
			System.out.println("WS1 id=" + WS.id + ",WS0id=" + WS.WS0id +
			        ",q0=" + WS.q0 +
			        ",WS0nodes=" + g.WS0nodes.get(WS.WS0id).toString());
			System.out.println("Waq:" + WS.Waq.toString());
			System.out.print("VALI:");
			WS.printVALI();
		}
		for (Entry<String, Long> e : this.FWD_Val.entrySet()) {
			System.out.println(e.getValue() + ":" + e.getKey());
		}
	}

	/**
	 * Reset FWD values.
	 */
	public void resetFWD(PA g) {
		this.FWD.clear();
		this.FWD_k = 0L;
		this.FWD_Val.clear();
		for (WitnessSet_1Q0 WS : g.WS1s) {
			WS.VALI.clear();
		}
		for (WitnessSet_0Q0 WS : g.WS0s) {
			WS.VALI.clear();
		}
	}

	/**
	 * Reset BKD values.
	 */
	public void resetBKD(PA g) {
		for (WitnessSet_1Q0 WS : g.WS1s) {
			WS.AS.clear();
			WS.ASidx.clear();
		}
	}

	/**
	 * Obtain one accepted run for FWD alg.
	 */
	private String fwdTracingBack(PA g, WitnessSet WS, long i) {
		Vali_Probk v = WS.VALI.get(i);
		StringBuffer u = new StringBuffer("[i=" + i + "]");
		int nextws = WS.id;
		if (WS.getClass() == WitnessSet_0Q0.class) {
			u.append(g.WS0nodes.get(WS.id));
			//u.append("WS0#" + nextws);
		} else {
			u.append(g.WS1nodes(nextws));
			//u.append("WS1#" + nextws);
		}
		while (i > 0) {
			u.append("<-" + v.symbols + "-");
			u.append("[i=" + (i - 1) + "]");
			/*for (int wspre : v.VALpreWSs_PROBKsuccessors) {
				//u.append("WS1#" + wspre);
				u.append(g.WS1nodes(wspre));
			}*/
			// |v.symbols| = |v.VALpreWSs_PROBKsuccessors|,
			// now one trace only
			try {
				nextws = v.VALpreWSs_PROBKsuccessors.get(0);//waq is WS1
				u.append(g.WS1nodes(nextws));
				//u.append("WS1#" + nextws);
				i--;
				v = g.WS1s.get(nextws).VALI.get(i);
				if (v == null)
					break;
			} catch (java.lang.IndexOutOfBoundsException e) {
				break;// v.VALpreWSs_PROBKsuccessors.get(0)error??
			}
		}
		return u.toString();
	}

	/**
	 * Return the Val
	 * 
	 * @param qs
	 *            is the single initial node of g
	 * @param x
	 *            is the given acceptance threshold
	 * @throws Exception
	 */
	private Fraction Val_WS1Q0(PA g, WitnessSet_1Q0 W, Fraction x, long i)
	        throws Exception {
		Vali_Probk vp = W.VALI.get(i);
		if (vp != null) { // /best time for the function
			return vp.pr;
		}

		Fraction val = Fraction.TWO; // (Double.POSITIVE_INFINITY);
		ArrayList<String> ts = new ArrayList<String>();
		ArrayList<Integer> as = new ArrayList<Integer>();

		if (i == 0) {
			if (W.q0 == g.q0) {
				val = x;
			}
		} else {// i>0
			val = W.VALI.get(i - 1).pr;// min of val(W,i-1) and ...
			int j = 0;
			for (j = 0; j < g.V.get(W.q0).in_edges.size(); j++) {
				int q = g.V.get(W.q0).in_edges.get(j).node;// Pr{q-a->qw}>0
				String a = g.V.get(W.q0).in_symbols.get(j);
				Fraction qa0 = g.V.get(W.q0).in_edges.get(j).pr;
				int waq = Waq(g, W, a, q);
				Fraction d1 = Val_WS1Q0(g, g.WS1s.get(waq), x, i - 1);
				if (d1.compareTo(Fraction.ONE) == 1) // d1>1
					continue;
				Fraction // qa1 = g.WS0s.get(W.WS0id).qa1.get(a + "#" + q);
				// if (qa1 == null) {
				qa1 = g.V.get(q).post_pr(a, g.WS0nodes.get(W.WS0id));
				// g.WS0s.get(W.WS0id).qa1.put(a + "#" + q, qa1);
				// }

				Fraction d_add = Fraction.ZERO;
				try {
					d_add = (d1.subtract(qa1)).divide(qa0);// (d1-qa1)/qa0
				} catch (MathArithmeticException e) {
					// approximate value
					d_add = new Fraction((d1.doubleValue() - qa1.doubleValue())
					        / qa0.doubleValue());
				}
				// val = (val < d_add)? val : d_add;
				if (val.compareTo(d_add) == 1) {// val > d_add){
					val = d_add;
					ts.clear();
					ts.add(a);
					as.clear();
					as.add(waq);
				}
			}
		}
		W.VALI.put(i, new Vali_Probk(val, -1, ts, as));
		return val;
	}

	/**
	 * Return the Val for Val_WS0Q0.
	 * 
	 * @param x
	 *            is the given acceptance threshold
	 * @throws Exception
	 */
	private Fraction Val_WS0Q0(PA g, WitnessSet_0Q0 W, Fraction x, long i)
	        throws Exception {
		Vali_Probk vp = W.VALI.get(i);
		if (vp != null) {
			return vp.pr;
		}

		Fraction val = Fraction.TWO; // (Double.POSITIVE_INFINITY);
		// ts and as are for tracing back purpose
		ArrayList<String> ts = new ArrayList<String>();
		ArrayList<Integer> as = new ArrayList<Integer>();

		// if i==0: val = + inf
		if (i > 0) {
			val = W.VALI.get(i - 1).pr;// min of val(W,i-1) and ...
			for (String a : g.symbols) {
				if (W.inWS0.get(a) == null)
					continue;
				int inWS0 = W.inWS0.get(a);
				if (g.WS0s.get(inWS0).superQ0.isEmpty() &&
				        g.WS0s.get(inWS0).goodQ0.isEmpty())
					continue;
				Set<Integer> preWaQ0 = util.union(g.WS0s.get(inWS0).superQ0,
				        g.WS0s.get(inWS0).goodQ0);
				for (int q : preWaQ0) {
					// \delta_a(q,W)>minval(W_{a,q},i-1)
					int waq = Waq(g, W, a, q);// Waq is a WS_1Q0
					Fraction d1 = Val_WS1Q0(g, g.WS1s.get(waq), x, i - 1);
					Fraction // qa1 = W.qa1.get(a + "#" + q);
					// if (qa1 == null) {
					qa1 = g.V.get(q).post_pr(a, g.WS0nodes.get(W.id));
					// W.qa1.put(a + "#" + q, qa1);
					// }
					if (d1.compareTo(qa1) < 0) {
						val = new Fraction(-2);// - \inf
					} else {// val i = val i-1
						if (val.compareTo(d1) == 1) {// val > d1){
							val = d1;
							ts.clear();
							ts.add(" ");
							as.clear();
							as.add(waq);
						}
					}
				}
			}
		}
		W.VALI.put(i, new Vali_Probk(val, -1, ts, as));
		return val;
	}

	/**
	 * Return the index of the Waq (a WitnessSet_1Q0) in WitnessSet_1Q0 for any
	 * WitnessSet. Since for any W\in X, a\in \Sigma, and q\in Q0, let
	 * W_{a,q} = (pre(W,a) \cap Q1) \cup {q}. valid Waq for WitnessSet_1Q0:
	 * \delta_a(q,q_W)>0.
	 * 
	 * @throws Exception
	 */
	private Integer Waq(PA g, WitnessSet W, String a, int q)
	        throws Exception {
		Integer waqid = W.Waq.get(a + "#" + q);
		if (waqid != null)
			return waqid;
		WitnessSet_0Q0 WS0 = null;
		if (W.getClass() == WitnessSet_0Q0.class) {
			WS0 = (WitnessSet_0Q0) W;
		} else {
			WitnessSet_1Q0 tempWS1 = (WitnessSet_1Q0) W;
			WS0 = g.WS0s.get(tempWS1.WS0id);
		}
		if (WS0.inWS0.get(a) == null) {
			return -2;// ERROR in TraverseBack()
		}
		int inWS0id = WS0.inWS0.get(a);
		// int oldWS1size = g.WS1s.size();
		waqid = g.addOrFindWS1(q, inWS0id);
		// if(oldWS1size<g.WS1s.size()){
		// System.out.println("new WS1 added as Waq.");
		// }
		W.Waq.put(a + "#" + q, waqid);
		return waqid;
	}

	/**
	 * Check whether forward algorithm converges, if not, update FWD and
	 * FWD_Val. Supports repeating convergence as well as fixed point
	 * convergence.
	 */
	private boolean FWD_Convergent(String val_string, long i) {
		if (FWD_Val.get(val_string) != null) {// same val for smaller i
			FWD.add(FWD_Val.get(val_string));
			return true;
		} else {
			FWD.add(i);
			FWD_Val.put(val_string, i);
		}
		return false;
	}

	/** print VALI values in the console */
	@SuppressWarnings("unused")
	private void printVALI(PA g) {
		System.out.println("WitnessSet_0Q0");
		for (WitnessSet ws : g.WS0s) {
			ws.printVALI();
		}
		System.out.println("WitnessSet_1Q0");
		for (WitnessSet ws : g.WS1s) {
			ws.printVALI();
		}
	}

	/**
	 * Update BKD_x and BKD_k for g
	 */
	public message bkd(PA g, long L) throws Exception {
		message m = new message();
		if (this.BKD_x != null && this.BKD_k <= L) {
			return m;
		}
		if (g.F.isEmpty()) {
			this.BKD_x = Fraction.ZERO;
			this.BKD_k = 0L;
			return m;
		}
		Fraction xmax = Fraction.MINUS_ONE;
		int q0WS1id = -1;
		for (long k = 1; k <= L; k++) {
			// bkd looks at max length of Q0 path, but there could be circle
			Fraction xt = Fraction.ZERO; // for each K
			int ws = 0;
			while (ws <= g.WS1s.size() - 1) {
				while (PROBK.size() < g.WS1s.size())// initialize PROBK(ws)
					PROBK.add(new HashMap<Long, Vali_Probk>());
				WitnessSet_1Q0 W = g.WS1s.get(ws);
				Fraction probk = Prob_k_W(g, W, k);// calculate and store probk
				if (probk == null) {
					this.BKD_x = null;
					this.BKD_k = null;
					m.ErrorMessage = "Exception: Integer overflow.";
					/// q0WS1id's max probk value, <=actual
					/// this.BKD_k = (long) PROBK.get(q0WS1id).size();
					/// this.BKD_x = PROBK.get(q0WS1id).get(BKD_k).pr;
					/// m.note = "BKD Exception. Approximate value.";
					// printBKDResult(g);
					return m;
				}
				if (W.q0 == g.q0 && g.WS0nodes.get(W.WS0id).isEmpty()) { // W={qs}
					q0WS1id = W.id;
					if (probk.compareTo(xt) >= 0) // probk >= xt) {
						xt = probk;// non-empty
				}
				++ws;
			} // for each WS
			if (xt.compareTo(xmax) > 0) {// xt > xmax
				xmax = xt;
			}

			// K++, if every probk of WSs remains unchanged, terminates early.
			// Fixed Point.
			if (k == 1)
				continue;
			int i = 0;
			while (i < g.WS1s.size()
			        && this.PROBK.get(g.WS1s.get(i).id).get(k).pr
			                .compareTo(this.PROBK.get(g.WS1s.get(i).id)
			                        .get(k - 1).pr) == 0) {
				i++;
			}
			if (i == g.WS1s.size()) {
				this.BKD_x = xmax;
				this.BKD_k = k;
				// printBKDResult(g);
				return m;
			}

		} // for each K
		this.BKD_x = xmax;
		this.BKD_k = L;
		// printBKDResult(g);
		return m;
	}

	public VerificationMessage bkdCheckX_wTrace(PA g, Fraction x, long L)
	        throws Exception {
		long t0 = util.getCPUTime() / 1000000;
		VerificationMessage m = new VerificationMessage();
		m.A = alg.BKD;
		m.x = x;
		if (g.F.isEmpty()) {
			m.isEmpty = true;
			m.note = "Empty F";
			m.ws = g.WS0s.size() + g.WS1s.size();
			return m;
		}
		if (PROBK.isEmpty()) {
			// 1st time call, initialize PROBK(ws)
			for (int ws = 0; ws <= g.WS1s.size() - 1; ws++) {
				PROBK.add(new HashMap<Long, Vali_Probk>());
			}
		}
		int q0WS1id = -1;
		for (long k = 1; k <= L; k++) { // k starts from 1
			for (WitnessSet_1Q0 W : g.WS1s) {
				Fraction probk = Prob_k_W(g, W, k);
				if (probk == null) {
					/// this.BKD_k = (long) PROBK.get(q0WS1id).size();
					/// this.BKD_x = PROBK.get(q0WS1id).get(BKD_k).pr;
					/// m.note = "Exception. Approximate value.";
					/// m.length = k - 1;
					this.BKD_x = null;
					this.BKD_k = null;
					m.ErrorMessage = "Exception: Integer overflow.";
					m.length = k;
					m.note = "Empty Non-Convergence?";
					long t1 = util.getCPUTime() / 1000000;
					m.time = t1 - t0;
					m.ws = g.WS0s.size() + g.WS1s.size();
					return m;
				}

				if (W.q0 == g.q0 && g.WS0nodes.get(W.WS0id).isEmpty()) { // {qs}
					q0WS1id = W.id;
					if (probk.compareTo(x) >= 0) {
						m.isEmpty = false;
						m.length = k;
						long t1 = util.getCPUTime() / 1000000;
						m.time = t1 - t0;
						m.ws = g.WS0s.size() + g.WS1s.size();
						// printBKDResult(g);
						m.trace = bkdTracingBack(g, W, k); // counter example
						return m;
					}
				}
			} // for each WS

			// K++, if every probk of WSs remains unchanged, terminates early.
			// Fixed Point.
			if (k == 1)
				continue;
			int i = 0;
			while (i < g.WS1s.size()
			        && this.PROBK.get(g.WS1s.get(i).id).get(k).pr
			                .compareTo(this.PROBK.get(g.WS1s.get(i).id)
			                        .get(k - 1).pr) == 0) {
				i++;
			}
			if (i == g.WS1s.size()) {
				m.isEmpty = true;
				m.length = k;
				m.note = "BKD Convergence";
				long t1 = util.getCPUTime() / 1000000;
				m.time = t1 - t0;
				m.ws = g.WS0s.size() + g.WS1s.size();
				// printBKDResult(g);
				return m;
			}
		} // for each K
		m.isEmpty = true;
		m.length = L;
		m.note = "Max L";
		long t1 = util.getCPUTime() / 1000000;
		m.time = t1 - t0;
		m.ws = g.WS0s.size() + g.WS1s.size();
		// printBKDResult(g);
		return m;
	}

	@SuppressWarnings("unused")
	private String bkdTracingBack(PA g, WitnessSet_1Q0 W, long k) {
		StringBuffer u = new StringBuffer();
		int curW = W.id;
		Vali_Probk prk = this.PROBK.get(W.id).get(k);
		u.append(g.WS1nodes(curW));
		//u.append("WS1#").append(curW);
		while (curW != -1 && k >= 0 && prk != null) {
			// get any counter example
			if (prk.symbols == null || prk.symbols.isEmpty())
				//prk.VALpreWSs_PROBKsuccessors.isEmpty()
				break;
			String a = prk.symbols.get(0);// 1st trace available
			u.append("-" + a + "->");
			curW = prk.VALpreWSs_PROBKsuccessors.get(0);
			Set<Integer> nodes;// = new HashSet<Integer>();
			if (curW >= g.WS1s.size()) {// WS0
				nodes = g.V.get(W.q0).post_nodes(a,
				        g.WS0nodes.get(curW - g.WS1s.size()));
			} else {// WS1
				Set<Integer> temp = g.WS0nodes.get(g.WS1s.get(curW).WS0id);// ref
				Set<Integer> pWa = g.postWS1(W, a);// deep copy
				nodes = util.intersection(pWa, temp);
				if (temp.contains(g.WS1s.get(curW).q0))
					nodes.add(g.WS1s.get(curW).q0);
			}
			u.append(nodes.toString());
			k--;
			prk = this.PROBK.get(W.id).get(k);
		}
		return u.toString();
	}

	/**
	 * Calculate and Store prob_k_W. max Aprob(a,W1),
	 * where W1 is an a-successor of W.
	 * 
	 * Return NULL if Exception in bkd, maybe empty non-convergence.
	 */
	private Fraction Prob_k_W(PA g, WitnessSet_1Q0 W, long k) throws Exception {
		if (PROBK.get(W.id).get(k) != null) {
			return PROBK.get(W.id).get(k).pr;
		}
		Fraction prob = k == 1 ? Fraction.ZERO : PROBK.get(W.id).get(k - 1).pr;
		ArrayList<String> ts = new ArrayList<String>();
		ArrayList<Integer> as = new ArrayList<Integer>();
		if (k == 1) {
			for (String a : g.symbols) {
				update_a_successor(g, W, a);// only needed at k = 1
				Set<Integer> pW1a = g.post(g.WS0nodes.get(W.WS0id), a);
				// for any super good witness set
				for (WitnessSet_1Q0 WS1 : g.WS1s) {
					if (!WS1.isSuperGoodWS1 ||
					        !util.isSubset(pW1a, g.WS0nodes.get(WS1.WS0id)))
						continue;
					Fraction p =
					        g.V.get(W.q0).post_pr(a, g.WS0nodes.get(WS1.WS0id));
					p = p.add(g.V.get(W.q0).post_pr(a, WS1.q0));
					if (p.compareTo(prob) > 0) {
						prob = p;
						ts.add(a);
						as.add(WS1.id);
					}
				}
				for (WitnessSet_0Q0 WS0 : g.WS0s) {// for WS0
					if (!WS0.superQ0.isEmpty()// in good WS1
					        || !util.isSubset(pW1a, g.WS0nodes.get(WS0.id)))
						continue;
					Fraction p =
					        g.V.get(W.q0).post_pr(a, g.WS0nodes.get(WS0.id));
					if (p.compareTo(prob) > 0) {
						prob = p;
						ts.add(a);
						as.add(WS0.id + g.WS1s.size());
					}
				}
			}
		} else if (W.ASidx != null && !W.ASidx.isEmpty()) {
			for (Entry<String, Integer> a : W.ASidx.entrySet()) {
				LinkedList<Integer> AS = W.AS.get(a.getValue());
				if (AS == null || AS.isEmpty()) {
					continue;
				}
				for (int v : AS) {
					WitnessSet_1Q0 U = g.WS1s.get(v);
					Fraction d1 = g.V.get(W.q0).post_pr(a.getKey(),
					        g.WS0nodes.get(U.WS0id));
					Fraction d2 = g.V.get(W.q0).post_pr(a.getKey(), U.q0);
					Fraction d3 = null;
					Fraction t = Fraction.ZERO;
					try {
						d3 = Prob_k_W(g, U, k - 1);
						if (d3 == null)
							return null;
						t = d1.add(d2.multiply(d3));// apr
					} catch (MathArithmeticException e) {
						// approximate value
						t = d1.add(new Fraction(
						        (d2.doubleValue() * d3.doubleValue())));
						return null;
					}
					if (t.compareTo(prob) > 0) {// t > prob
						prob = t;
						ts.clear();
						as.clear();
						ts.add(a.getKey());
						as.add(U.id);
					}
				}

			}
		}
		PROBK.get(W.id).put(k, new Vali_Probk(prob, (long) k, ts, as));
		return prob;
	}

	/**
	 * Calculate and store the a_successors of U on a.
	 * When >1 a_successor on the same a, there is a maximal one containing all
	 * others by debugging, but it's not necessarily the maximal one will lead
	 * to max probk, so keep all.
	 * a_successor must be a good WS: after reduce(), either Q0 alone, or Q0 and
	 * Q1 as super good.
	 */
	private void update_a_successor(PA g, WitnessSet_1Q0 U, String a) {
		if (U.ASidx != null && U.ASidx.get(a) != null)
			return;
		Set<Integer> pU1a = g.post(g.WS0nodes.get(U.WS0id), a);// post(U.Q1,a)
		int pU0a0 = -1;// post(U.q0,a) on Q0
		Set<Integer> pU0a1 = new HashSet<Integer>(); // post(U.q0,a) on Q1
		for (T t : g.V.get(U.q0).outT) {
			if (!t.input.equals(a))
				continue;
			for (edge d : t.dist) {
				if (g.V.get(d.node).level == 0)
					pU0a0 = d.node;
				else
					pU0a1.add(d.node);
			}
		}
		Set<Integer> pUa = new HashSet<Integer>();// post(U,a)
		pUa.addAll(pU1a);
		if (pU0a0 != -1)
			pUa.add(pU0a0);
		pUa.addAll(pU0a1);
		if (pUa.isEmpty())
			return;

		LinkedList<Integer> AS = new LinkedList<Integer>();

		// ATVA15: post(U \cap Q1 , a) \subseteq W1
		for (WitnessSet_1Q0 W1 : g.WS1s) {
			if (// !util.intersection(pUa, g.WS0nodes.get(W1.WS0id)).isEmpty()
			    // || W1.q0 == pU0a0)
			(util.isSubset(pU1a, g.WS0nodes.get(W1.WS0id))))
			    // && util.isSubset(g.WS0nodes.get(W1.WS0id), pUa)))
			    // if (pU0a0 != -1 && W1.q0 == pU0a0 || pU0a0 == -1)
			    // removed according to atva paper
			    AS.add(W1.id);
		}

		/*
		 * // 07/13 on v2.6
		 * // for each conflict group 0-1/2/3, at most one of 1,2,3 will be on
		 * Q0.
		 * // if there is one Q0 node,
		 * // any WS0 intersecting {1,2,3} shall make a WS1 with the Q0 node,
		 * // and this WS1 is an a-suc of {0} (HPAMC.update_a_successor()).
		 * if (g.conflictEnds != null && !g.conflictEnds.isEmpty()) {
		 * for (int i = 0; i < g.conflictEnds.size(); ++i) {
		 * if (g.conflictSource.get(i) != U.q0)// not a-suc of current WS1
		 * continue;
		 * ArrayList<Integer> todo = g.conflictEnds.get(i);
		 * int WS1q0 = -1;
		 * HashSet<Integer> Q1 = new HashSet<Integer>(todo.size());
		 * for (int n : todo) {
		 * if (g.V.get(n).level == 0) {
		 * WS1q0 = n;
		 * } else {
		 * Q1.add(n);
		 * }
		 * }
		 * if (WS1q0 == -1)// no WS1 from this conflict group
		 * continue;
		 * for (int j = 0; j < g.WS0s.size(); ++j) {
		 * // if (!util.intersection(Q1, g.WS0nodes.get(j)).isEmpty())
		 * // {
		 * if (util.isSubset(pU1a, g.WS0nodes.get(j)) &&
		 * util.isSubset(g.WS0nodes.get(j), pUa)) {
		 * int WS1id = g.addOrFindWS1(WS1q0, j);
		 * AS.add(WS1id);
		 * }
		 * }
		 * }
		 * }
		 */

		// v2.0.1
		// int max1 = 0;
		// for (WitnessSet_1Q0 V : g.WS1s) {
		// // if (!V.isSuperGoodWS)
		// //// wrong: no superGood if more Q1, CE: S2.hpa1
		// // continue;
		// if (g.WS0nodes.get(U.WS0id).isEmpty()) {
		// if (V.WS0id == U.WS0id && pU0a0 == V.q0 // V Q0-only
		// || util.isSubset(g.WS0nodes.get(V.WS0id),pUa)
		// /// !pU0a1.isEmpty() &&
		// /// util.isSubset(pU0a1,g.WS0nodes.get(V.WS0id))//CE:s2.hpa1
		// )
		// AS.add(V.id);
		// } else {// !pU1a.isEmpty()
		// if (g.WS0nodes.get(V.WS0id).size() > max1
		// && pU0a0 == V.q0
		// && util.isSubset(pU1a, g.WS0nodes.get(V.WS0id))) {
		// max1 = g.WS0nodes.get(V.WS0id).size();
		// AS.add(V.id);
		// }
		// }
		// }

		if (!AS.isEmpty()) {
			U.AS.add(AS);
			U.ASidx.put(a, U.AS.size() - 1);
		}
	}

	private void printBKDResult(PA g) {
		for (WitnessSet_1Q0 W1 : g.WS1s) {
			System.out.print("WS1 id=" + W1.id + ":" +
			        W1.q0 + "," + g.WS0nodes.get(W1.WS0id).toString() + ";");
			if (W1.ASidx != null && !W1.ASidx.isEmpty()) {
				for (Entry<String, Integer> e : W1.ASidx.entrySet()) {
					System.out.print(e.getKey() + ":");
					System.out.print(W1.AS.get(e.getValue()).toString());
				}
			}
			System.out.print("\n");
			for (Entry<Long, Vali_Probk> vp : this.PROBK.get(W1.id)
			        .entrySet()) {
				System.out.println(vp.getKey() + ":"
				        + vp.getValue().pr.toString()
				        + ",ts=" + vp.getValue().symbols.toString() + ",as="
				        + vp.getValue().VALpreWSs_PROBKsuccessors.toString());
			}
		}
	}

	/**
	 * Find MAX non-empty threshold, with defined precision and execution
	 * length. Assumption: L is large enough.
	 * 
	 * @param xprecision
	 */
	public RobustMessage robust(PA hpa, long L, Fraction xprecision) {
		RobustMessage rm = new RobustMessage();
		if (hpa == null) {
			rm.ErrorMessage = "ERROR in Robustness: load HPA first.";
			return rm;
		}
		// L=4rn8^n
		rm.L = L;
		HPAMC HPA = new HPAMC();// new for each new hpa
		try {
			long ut0 = util.getCPUTime() / 1000000;
			if (!(HPA.BKD_x != null && HPA.BKD_k <= L)) {
				rm.note = HPA.bkd(hpa, L).note;
				rm.BKDws = hpa.WS0s.size() + hpa.WS1s.size();
			}
			if (HPA.BKD_x != null) {
				rm.BKDr = Fraction.ONE.subtract(HPA.BKD_x);
				rm.lengthBKD = HPA.BKD_k;
				// return here if no FWD robustness.
			}
			long ut1 = util.getCPUTime() / 1000000;
			rm.timeBKD = ut1 - ut0;

			// using FWD to decide robustness
			ut0 = util.getCPUTime() / 1000000;
			Fraction ub = Fraction.ONE;// upper bound, empty
			Fraction lb = Fraction.ZERO;// lower bound, non-empty
			// robustness: min empty, x>=XX, empty; x<XX, non-empty
			Fraction x = ub.add(lb).divide((int) 2);
			while (ub.subtract(lb).compareTo(xprecision) >= 0) {
				// keep reducing distance between ub and lb until
				// (ub-lb)<=xprecision
				VerificationMessage m = HPA.fwdCheckX(hpa, x, L, true);
				// appendLog(m);
				if (!m.isEmpty) {
					lb = x;
					x = lb.add(ub).divide((int) 2);
				} else {
					ub = x;
					x = lb.add(ub).divide((int) 2);
				}
			}
			rm.FWDl = Fraction.ONE.subtract(ub);// (
			rm.FWDu = Fraction.ONE.subtract(lb);// ]
			rm.fwdPrecision = xprecision;
			rm.FWDws = hpa.WS0s.size() + hpa.WS1s.size();
			ut1 = util.getCPUTime() / 1000000;
			rm.timeFWD = ut1 - ut0;
		} catch (Exception e1) {
			rm.ErrorMessage = e1.getMessage();
			// e1.printStackTrace();
		}
		return rm;
	}

	/**
	 * Load PA file input stream, validate and assign levels. WSs not generated.
	 */
	public PA loadHPA(edu.princeton.cs.introcs.In in, String filetype,
	        boolean pfa, boolean moreQ1)
	        throws Exception {
		PA hpa = null;
		if (!(filetype.isEmpty() || filetype.equalsIgnoreCase("txt") || filetype
		        .equalsIgnoreCase("hpa"))) {
			throw new Exception(
			        "ERROR in loading HPA file: invalid file type.");
			// return null;
		}
		try {
			if (pfa)// PFA
				hpa = new PA(in, filetype);
			else
				hpa = new HPBA(in, filetype);// PBA
		} catch (Exception e1) {
			throw new Exception("ERROR in loading HPA:\n" + e1.getMessage());
			// return null;
		}
		in.close();

		message m = hpa.HPAinitial(moreQ1);
		// appendLog(message);
		if (m.ErrorMessage != null) {
			throw new Exception("ERROR in loading HPA:" + m.ErrorMessage);
			// return null;
		}
		/*
		 * // if called Determinize() in PA:
		 * // non-deterministic hpa, ask whether to save determinized
		 * if (message.contains("determinized")) {
		 * int sel = JOptionPane.showConfirmDialog(new JFrame(),
		 * "Given PA has been determinized. Do you want to save it?\n");
		 * if (sel == 0) {// YES=0, save PA in memory to a file
		 * JFileChooser fc = new JFileChooser();
		 * if (dir != null) {
		 * fc.setCurrentDirectory(dir);
		 * } else {
		 * fc.setCurrentDirectory(new java.io.File("."));
		 * }
		 * fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		 * fc.setDialogTitle("Output Determinized HPA");
		 * fc.setAcceptAllFileFilterUsed(true);
		 * if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
		 * try {
		 * File fileToSave = fc.getSelectedFile();
		 * hpa.writePA2HPA(fileToSave.getAbsolutePath() + ".hpa",
		 * null);
		 * appendLog("Determinized HPA saved to file "
		 * + fileToSave.getAbsolutePath()
		 * + ".hpa\n");
		 * } catch (IOException e1) {
		 * appendLog("Failed to savve determinized HPA "
		 * + e1.getMessage() + "\n");
		 * e1.printStackTrace();
		 * }
		 * }
		 * }
		 * }
		 */
		return hpa;
	}
}