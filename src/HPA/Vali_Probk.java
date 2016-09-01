package HPA;

import java.util.ArrayList;

import org.apache.commons.math3.fraction.Fraction;
/**
 * @author Cindy Yue Ben
 */
public class Vali_Probk {
	public Fraction pr = Fraction.ZERO;
	public long k = -1; // val_i starts with i=0, prob_k starts with k=1
	ArrayList<String> symbols = null;
	// vali: the set of symbols is empty for k<=0
	ArrayList<Integer> VALpreWSs_PROBKsuccessors = null;
	// vali: empty for k<=0;//probk: -1 implies F

	public Vali_Probk(Fraction pr){
		if (pr != null)
			this.pr = pr;
	}

	public Vali_Probk(Fraction pr, long i) {
		if (pr != null)
			this.pr = pr;
		this.k = i;
	}

	public Vali_Probk(Fraction pr, long k2, ArrayList<String> ts,
	        ArrayList<Integer> as) {
		this.pr = pr;
		this.k = k2;
		if (ts != null)
			this.symbols = ts;
		if (as != null)
			this.VALpreWSs_PROBKsuccessors = as;
	}

	public static void main(String[] args) {

	}

}
