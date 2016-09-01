package Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import HPA.PA;
import edu.princeton.cs.introcs.In;

public class testPrismHPA {

	public static void main(String[] args) throws Exception {
		testPrismHPA t = new testPrismHPA();
		File f = new File("./examples/3PRISM/prism/list.txt");
		In o = new In(f);
		// one line for one test case
		String line = o.readLine();
		while (line != null) {
			if (!line.isEmpty()) {
				t.testPRISMGen(line);//path and name only, no extension
			}
			line = o.readLine();
		}
		o.close();
	}

	private void testPRISMGen(String f) throws Exception{
		In in;
		PA hpa;
		try {
			in = new In(f+".tra");
			hpa = new PA(in,"tra");
		} catch (Exception e) {//non-existing file
			return;
		}
		in.close();
		in = new In(f+".lab");
		hpa.updatePAusingPRISMlabel(in);
		in.close();
		in = new In(f+".sta");
		hpa.updatePAusingPRISMStates(in);
		in.close();
		hpa.writePA2HPA(f, "");
	}

}
