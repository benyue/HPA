package HPA;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;

public class edge {
	public Fraction pr = new Fraction(0); //[0,1]
	public int node = -1;
	public Set<String> prop = new HashSet<String>();

	public edge(Fraction pr, int next) throws Exception {
		this.pr = pr;
		this.node = next;
	}

	public static void main(String[] args) {

	}

}
