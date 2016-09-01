package HPA;

import org.apache.commons.math3.fraction.Fraction;
/**
 * @author Cindy Yue Ben
 * */
public class edge {
	public Fraction pr = new Fraction(0); //[0,1]
	public int node = -1;
	//public Set<String> prop = new HashSet<String>();

	public edge(Fraction pr, int node) throws Exception {
		this.pr = pr;
		this.node = node;
	}

}
