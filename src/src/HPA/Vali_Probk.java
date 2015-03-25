package HPA;

import java.util.ArrayList;

import org.apache.commons.math3.fraction.Fraction;

public class Vali_Probk {
	public Fraction pr = new Fraction(0);
	public long k = -1; //val_i starts with i=0, prob_k starts with k=1
	ArrayList<String> symbols = null;
	// vali: the set of symbols is empty for k<=0
	ArrayList<Integer> VALpreWSs_PROBKsuccessors = null;
	// vali: empty for k<=0;//probk: -1 implies F

	public Vali_Probk(Fraction pr) throws Exception {
		this.pr = pr;
	}
	
	public Vali_Probk(Fraction pr, long i) throws Exception {
		this.pr = pr;
		this.k = i;
	}
	
	public Vali_Probk(Fraction pr, long k2, ArrayList<String> ts,
			ArrayList<Integer> as) throws Exception {
		this.pr = pr;
		this.k = k2;
		this.symbols = ts;
		this.VALpreWSs_PROBKsuccessors = as;
	}

	public static void main(String[] args) {

	}

}
