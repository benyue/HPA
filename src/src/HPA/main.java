package HPA;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.apache.commons.math3.fraction.Fraction;

import UI.UI;
import Util.TarjanSCC;
import Util.io;
import Util.util;

public class main {
	String ProcessFileName = "";
	

	public static void main(String[] args) throws Exception {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UI window = new UI();
			}
		});
		
		/*Create HPA*/
		File f = new File("data/s1");
		if (!f.exists()) {
			throw new FileNotFoundException(f.getName());
		}
		System.out.println("Data File name: " + f.getName());
		edu.princeton.cs.introcs.In in = new edu.princeton.cs.introcs.In(f);
		CreateHPA g0 = new CreateHPA(in);
		g0.RemoveUnreachable();
		in.close();
		g0.name = "g0";
		if (!g0.is0HPA)
			throw new Exception("g0 is not 1HPA.");
		String message = g0.checkT();
		if (message != null) {
			throw new Exception("\n" + g0.name + " violating checkT, at state "
					+ message);
		}

		io.writePA2file(g0, "data/file_" + g0.name, "");
		io.writeNFA2FAT(g0, "data/FAT_g0", "");
		CreateHPA g1 = new CreateHPA(g0), g2 = new CreateHPA(g0);// a deep copy

		g1.name = "g1";
		g1.prefix = "1S";
		g1.renameStates(g1.prefix, "");
		g1.renameStateProp(g1.prefix, "");
		g2.name = "g2";
		g2.prefix = "2S";
		g2.renameStates(g2.prefix, "");
		g2.renameStateProp(g2.prefix, "");

		Set<String> com = new HashSet<String>();
		com.add("T");
		com = util.intersection(com, g0.symbols);

		System.out
				.println("\nShared/Common symbols (synced):" + com.toString());

		g1.renameSymbolsExceptCom(g1.prefix, "", com);
		g2.renameSymbolsExceptCom(g2.prefix, "", com);

		CreateHPA g = CreateHPA.PSCompPOR(g1, g2);
		io.writePA2file(g, "data/file_" + g.name, "");
		io.writeNFA2FAT(g, "data/FAT_g1g2", "");

		// Define Failures. Two ways: failed states specified or not.
		CreateHPA gf = new CreateHPA(g); // deep copy
		String failedserver = g1.prefix; // Note only one server can fail!
		ArrayList<Integer> failedStateIDs = new ArrayList<Integer>();
		ArrayList<String> failedInputs = new ArrayList<String>();
		ArrayList<Fraction> r = new ArrayList<Fraction>(); // failure prob
		if (!(failedStateIDs.contains(6) && failedInputs.contains(failedserver
				+ "IUD"))) {
			failedStateIDs.add(6);
			failedInputs.add(failedserver + "IUD");
			r.add(new Fraction(4, 10));
		}
		if (!(failedStateIDs.contains(0) && failedInputs.contains(failedserver
				+ "Search"))) {
			failedStateIDs.add(0);
			failedInputs.add(failedserver + "Search");
			r.add(new Fraction(1, 10));
		}

		if (failedInputs.size() != r.size()) {
			throw new Exception("\n" + "Error defining falure probabilities.");
		}
		if (!failedStateIDs.isEmpty() && failedStateIDs.size() != r.size()
				|| failedInputs.size() != r.size()) {
			throw new Exception(
					"\n"
							+ "Error defining falure probabilities w.r.t. failedStateIDs.");
		} else {
			gf.addFailure(g2, g1, failedserver, failedStateIDs, failedInputs,
					r, "f", false);
		}

		String comment = ": g with failure " + failedserver + failedStateIDs
				+ failedInputs.toString() + r.toString();
		io.writePA2file(gf, "data/file_" + gf.name, comment);
		io.writeNFA2FAT(gf, "data/FAT_" + gf.name, comment);
		message = gf.checkT();
		if (message != null) {
			throw new Exception("\n" + gf.name + " violating checkT, at state "
					+ message);
		}

		f = new File("data/s1_property");
		if (!f.exists()) {
			throw new FileNotFoundException(f.getName());
		}
		System.out.println("\nProperty Data File name: " + f.getName());
		in = new edu.princeton.cs.introcs.In(f);
		CreateHPA gp = new CreateHPA(in);
		String gpSession = g1.prefix;// on which session is gp defined.
		in.close();
		gp.name = "gp" + gpSession;
		gp.prefix = "p" + gpSession;
		gp.renameStates(gp.prefix, "");
		gp.renameStateProp(gp.prefix, "");
		gp.renameSymbolsExceptCom(gpSession, "", com);

		// add ERROR state with ERROR proposition:
		// checkT() is false
		io.writePA2file(gp, "data/file_" + gp.name, "");
		io.writeNFA2FAT(gp, "data/FAT_" + gp.name, "Property automata");

		gp.DeterminePropertyAutomata(g.symbols, com, gpSession);
		io.writePA2file(gp, "data/file_" + gp.name + "_d", "");
		io.writeNFA2FAT(gp, "data/FAT_" + gp.name + "_d",
				"Property automata determinized");

		CreateHPA gfp = CreateHPA.PSCompMergeERROR(gf, gp, gpSession);
		io.writePA2file(gfp, "data/file_" + gfp.name, "");
		io.writeNFA2FAT(gfp, "data/FAT_" + gfp.name, "");
		io.write2HPA(gfp, "data/HPA_" + gfp.name, ""); // 1-HPA

		// elimitate T transitions.
		// gf.checkT()->gfp.checkT(), and gp.checkT() not required
		message = gfp.checkT();
		if (message != null)
			throw new Exception("\nWhat happened to gfp?" + "@" + message);
		message = gfp.hasAbeforeT();
		if (message != null) {
			// ensure every T-transition has an external input before T
			throw new Exception(
					"\nError: not every T-state comes after input-state, such as state "
							+ message);
		}
		gfp.removeTTransitions();
		io.writePA2file(gfp, "data/file_" + gfp.name + "_no_T", "");
		io.writeNFA2FAT(gfp, "data/FAT_"+gfp.name+"_no_T", "");
		io.write2HPA(gfp, "data/HPA_"+gfp.name+"_no_T", ""); // 2-HPA

		// remove unreachable
		// gfp.RemoveUnreachable();
		// gfp.printPA(gfp.name + "_no_T_reachable");
		// gfp.writeNFA2FAT("data/FAT_gfp_no_T_reachable", "");
		// gfp.write2HPA("data/HPA_hpa0.91", ""); //2-HPA

		// deterministic
		gfp.Determine();
		io.writePA2file(gfp, "data/file_hpa", "");
		io.writeNFA2FAT(gfp, "data/FAT_hpa", "");
		io.write2HPA(gfp, "data/HPA_hpa", "");
		
		/*HPA*/
		long SystemTimeNano0 = util.getSystemTime( );
		long UserTimeNano0   = util.getUserTime( );
		System.out.println("Current system time: "+SystemTimeNano0+"; user time: "+ UserTimeNano0
				+".\n"
				+ "Note timing is using nanoseconds.");
		
		HPA hpa = new HPA();
		f = new File("data/HPA_hpa");// simple_test.txt //HPA_hpa");///
		if(!f.exists()){
			throw new FileNotFoundException(f.getName());
		}
		in = new edu.princeton.cs.introcs.In(f);
		PA g11 = new PA(in);
		in.close();
		System.out.println("Data File name: "+ f.getName());
		if (g11.Q0.size() > 1) {
			throw new Exception("\nERROR: There is more than one inisital state.");
		}
		
		long SystemTimeNano1 = util.getSystemTime( );
		long UserTimeNano1   = util.getUserTime( );
		System.out.println("Time spent to read data: system time="+(SystemTimeNano1-SystemTimeNano0)+";"
				+ " user time="+ (UserTimeNano1-UserTimeNano0));
/*
		// Validate the PA for determination
		if(g.isDeterministic() != -1){
			throw new Exception("\nERROR: g is non-deterministic.");
		}
		
		SystemTimeNano0 = util.getSystemTime( );
		UserTimeNano0   = util.getUserTime( );
		System.out.println("Time spent to validate determination:"
				+ " system time="+(SystemTimeNano0-SystemTimeNano1)+";"
				+ " user time="+ (UserTimeNano0-UserTimeNano1));
*/
		/* Compute SCCs in the DG */
		TarjanSCC tjscc = new TarjanSCC(g11.adj_out_int);
		// Check for reachability of the states
		if (tjscc.unReachableState != -1) {
			throw new Exception("\nERROR: Not all states in the given graph are reachable from initial state, e.g. state "
							+ tjscc.unReachableState);
		}

		/* Decide if a DG is HPA */
		if (!hpa.isHPA(g11.conflict_node_group, tjscc.id())) {
			throw new Exception("\nThe given PA is not an HPA.");
		}
		//int M = tjscc.count();//M < V -> there is/are directed cycle(s)
		//ArrayList<Integer>[] sccs = tjscc.sccs(); // # of SCCs, size M
		//int[] sccHead = tjscc.head(); // size V

		/* assign level to the SCCS, then each node */
		// assign level to scc
		int[] levelSCC = hpa.levelSCCs(g11, tjscc);// size M
		// assign level to nodes
		hpa.levelNodes(g11,tjscc, levelSCC);

		System.out.println("The given PA is a " + util.max(levelSCC)
				+ "-level HPA.");
		System.out.println("Initial states: " + g11.Q0);
		System.out.println("Final states: " + g11.F);
		for (int i = 0; i <= util.max(levelSCC); i++) {
			System.out.println("Level " + i + " nodes: " + g11.obtainLevel(i));
		}

		if(util.max(levelSCC) > 1){
			throw new Exception("\nThis HPA has more than two levels.");
		}

		// after assigning levels, obtain witness sets now
		// Note the order of the functions cannot be exchanged
		g11.obtainFiniteGoodSWSs();
		g11.WSs = g11.obtainFiniteGoodWSs(g11.SWSs);

		for (int i = 0; i < g11.SWSs.size(); i++) {
			System.out.println("SemiWitnessSet " + i + ": L1Nodes= "
					+ g11.SWSs.get(i).L1_nodes + "; superL0Nodes="
					+ g11.SWSs.get(i).super_L0_nodes);
		}

		for (int i = 0; i < g11.WSs.size(); i++) {
			System.out.println("WitnessSet " + i + ": " + g11.WSNodes(i)
					+ "is superWS: " + g11.WSs.get(i).isSuperGoodWS);
		}
		
		SystemTimeNano1 = util.getSystemTime( );
		UserTimeNano1   = util.getUserTime( );
		System.out.println("Time spent to obtain levels and witness sets:"
				+ " system time="+(SystemTimeNano1-SystemTimeNano0)+";"
				+ " user time="+ (UserTimeNano1-UserTimeNano0));

		int qs = 0;
		Fraction x = new Fraction(40,100);//Fraction(int num, int den)
		long L = 60;
		hpa.fwdCheckX(g11, qs, x, L);
		
		SystemTimeNano0 = util.getSystemTime( );
		UserTimeNano0   = util.getUserTime( );
		System.out.println("Time spent on forward algorithm:"
				+ " system time="+(SystemTimeNano0-SystemTimeNano1)+";"
				+ " user time="+ (UserTimeNano0-UserTimeNano1));
		
		hpa.bkdCheckX(g11, qs, x, L);
		SystemTimeNano1 = util.getSystemTime( );
		UserTimeNano1   = util.getUserTime( );
		System.out.println("Time spent on backward algorithm:"
				+ " system time="+(SystemTimeNano1-SystemTimeNano0)+";"
				+ " user time="+ (UserTimeNano1-UserTimeNano0));

	}

}
