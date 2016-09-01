package Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.math3.fraction.Fraction;

import HPA.HPAMC;
import HPA.PA;
import HPBA.HPBA;
import Util.RobustMessage;
import Util.VerificationMessage;
import Util.message;
import Util.util;
//import static org.junit.Assert.*;

public class testMC {

	public static void main(String[] args) throws Exception {
		testMC t = new testMC();
		File f = new File("./examples/list.txt");
		t.testEmptiness(f,true);//boolean pfa
		t.testRobust(f,true);//boolean pfa
	}

	public void testEmptiness(File f, boolean pfa) throws Exception {
		BufferedReader o = new BufferedReader(new FileReader(f));
		//In o = new In(f); // alternative
		File fw = new File("./examples/test_"
		        + f.getName() + "_"
		        + new SimpleDateFormat("yyyy.MM.dd.H.m.s").format(new Date())
		        + "_V.csv");
		FileWriter ow = new FileWriter(fw);
		// title row
		ow.append("TestCase,Leveling,");
		ow.append("x,");// threshold value
		ow.append("w_BKD,");
		ow.append("BKD,");// BKD result: empty, or non-empty
		ow.append("L_BKD,T_BKD,Note_BKD,");
		ow.append("w_FWD,");
		ow.append("FWD,");// FWD result: empty, or non-empty
		ow.append("L_FWD,T_FWD,Note_FWD\n");
		ow.flush();
		// collect data, one line for one test case
		String line = o.readLine();
		while (line != null) {
			if(line.isEmpty()){
				line = o.readLine();
				continue;
			}
			File fr = new File(line);
			PA g = this.loadHPAforTest(fr, pfa, false);// 0
			if (g != null) {
				long L = g.L();
				this.testV(ow, g, L, new Fraction(1, 10), fr, 0);
				this.testV(ow, g, L, new Fraction(3, 10), fr, 0);
				this.testV(ow, g, L, new Fraction(5, 10), fr, 0);
				this.testV(ow, g, L, new Fraction(7, 10), fr, 0);
				this.testV(ow, g, L, new Fraction(9, 10), fr, 0);
			}

			g = this.loadHPAforTest(fr, pfa, true);// 1
			if (g != null) {
				long L = g.L();
				this.testV(ow, g, L, new Fraction(1, 10), fr, 1);
				this.testV(ow, g, L, new Fraction(3, 10), fr, 1);
				this.testV(ow, g, L, new Fraction(5, 10), fr, 1);
				this.testV(ow, g, L, new Fraction(7, 10), fr, 1);
				this.testV(ow, g, L, new Fraction(9, 10), fr, 1);
			}
			line = o.readLine();
		}
		o.close();
		ow.close();
	}

	public void testRobust(File f, boolean pfa) throws Exception {
		BufferedReader o = new BufferedReader(new FileReader(f));
		File fw = new File("./examples/test_"
		        + f.getName() + "_"
		        + new SimpleDateFormat("yyyy.MM.dd.H.m.s").format(new Date())
		        + "_R.csv");
		FileWriter ow = new FileWriter(fw);
		// title row
		ow.append("TestCase,Leveling,");// leveling option
		ow.append("n,");// n
		ow.append("s,");// s
		ow.append("w_BKD,");
		ow.append("R_BKD,");
		ow.append("T_BKD,");
		ow.append("w_FWD,");
		ow.append("R_FWD,");
		ow.append("T_FWD,");
		ow.append("Note\n");
		ow.flush();
		// collect data, one line for one test case
		String line = o.readLine();
		while (line != null) {
			if(line.isEmpty()){
				line = o.readLine();
				continue;
			}
			File fr = new File(line);
			testR(fr, pfa, false, ow);
			testR(fr, pfa, true, ow);
			line = o.readLine();
		}
		o.close();
		ow.close();
		// assertEquals(null,p.BKD_x.floatValue(),p.fwdR.floatValue(),0.01);
	}

	private void testV(FileWriter ow, PA g, long L, Fraction x, File fr,
	        int i) throws Exception {
		HPAMC HPA = new HPAMC();
		VerificationMessage vm;// = new VerificationMessage();
		vm = HPA.bkdCheckX_wTrace(g, x, L);
		ow.append(fr.getName() + "," + i + ",");
		ow.append(x.floatValue() + ",");
		// ow.append(x.toString().replace(" / ", "/") + ",");
		// ow.append(vm.A.name() + ",");
		ow.append(vm.ws + ",");// BKD
		int ws = vm.ws;
		if (vm.isEmpty)
			ow.append("empty,");
		else
			ow.append("non-empty,");
		ow.append(vm.length + ",");
		ow.append(vm.time + ",");
		if (vm.note != null && !vm.note.isEmpty())
			ow.append(vm.note + ",");
		else
			ow.append(",");
		ow.flush();

		vm = HPA.fwdCheckX(g, x, L, false);
		// ow.append(vm.A.name() + ",");
		ow.append(vm.ws + ",");// FWD
		if (vm.ws > ws) {
			g.writeWS2file(fr.getPath() + i + "FWD", "//More WSs added in FWD");
			//reset WSs for next test: important!
			g.resetWS();
			g.witnessSets();
		}
		if (vm.isEmpty)
			ow.append("empty,");
		else
			ow.append("non-empty,");
		ow.append(vm.length + ",");
		ow.append(vm.time + ",");
		if (vm.note != null && !vm.note.isEmpty())
			ow.append(vm.note + "\n");
		else
			ow.append("\n");
		ow.flush();
	}

	/**
	 * Load a PA and test robustness.
	 * */
	private void testR(File fr, boolean pfa, boolean moreQ1, Writer ow)
	        throws Exception {
		PA g = this.loadHPAforTest(fr, pfa, moreQ1);
		if (g == null)
			return;
		int i = moreQ1 ? 1 : 0;
		ow.append(fr.getName() + "," + i + ",");
		ow.append(g.V.size() + ",");// n
		ow.append(g.symbols.size() + ",");// s
		HPAMC HPA = new HPAMC();
		RobustMessage rm = HPA.robust(g, g.L(), new Fraction(1, 100));
		ow.append(rm.BKDws + ",");
		if (rm.BKDr == null) {
			ow.append("Exception,");
		} else {
			ow.append(rm.BKDr.floatValue() + ",");
		}
		ow.append(rm.timeBKD + ",");
		ow.flush();
		ow.append(rm.FWDws + ",");
		if (rm.FWDws > rm.BKDws) {
			g.writeWS2file(fr.getPath() + i + "FWD", "//More WSs added in FWD");
		}
		if (rm.FWDu == null) {
			ow.append("Exception,");
		} else {
			ow.append(rm.FWDu.floatValue() + ",");
		}
		ow.append(rm.timeFWD + ",");
		if(rm.note!=null)
			ow.append(rm.note);
		else
			ow.append("ok");
		ow.append("\n");
		ow.flush();
	}

	/**
	 * @param f: hpa file
	 * @param pfa: is pfa (true) or pbfa (false)
	 * @param moreQ1: leveling option
	 */
	PA loadHPAforTest(File f, boolean pfa, boolean moreQ1)
	        throws Exception {
		System.out.println(f.getName());
		edu.princeton.cs.introcs.In in =
		        new edu.princeton.cs.introcs.In(f);
		String filetype = org.apache.commons.io.FilenameUtils
		        .getExtension(f.toString());
		// create HPA
		PA hpa = null;
		if (!(filetype.isEmpty() || filetype.equalsIgnoreCase("txt") 
				|| filetype.equalsIgnoreCase("hpa"))) {
			return null;// ("ERROR in loading HPA file: invalid file type.\n");
		}
		try {
			if (pfa)// PFA
				hpa = new PA(in, filetype);
			else
				hpa = new HPBA(in, filetype);// PBA
		} catch (Exception e1) {
			return null;// ("ERROR in loading HPA:\n" + e1.getMessage());
		}
		in.close();
		int n = hpa.V.size();
		message m = hpa.HPAinitial(moreQ1);
		if (m.ErrorMessage != null) {
			hpa = null;
			return null;
		}
		if (n > hpa.V.size())
			hpa.writePA2HPA(f.getPath() + "R", "Reduced");
		hpa.writePA2FAT(f.getPath(),null);
		//System.out.println(hpa.basicInfo());
		long t0 = util.getCPUTime() / 1000000;
		hpa.witnessSets();
		long t1 = util.getCPUTime() / 1000000;
		String m1 = "//CPU time spent: " + (t1 - t0) + " ms.";
		int l = (moreQ1) ? 1 : 0;
		hpa.writeWS2file(f.getPath() + l, m1);
		return hpa;
	}

}
