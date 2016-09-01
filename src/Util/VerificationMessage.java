package Util;

import org.apache.commons.math3.fraction.Fraction;

import HPA.alg;

public class VerificationMessage extends message {
	public alg A;// BKD, FWD
	public Fraction x;
	public boolean isEmpty;
	public long length;
	public long time;
	public String trace;
	public int ws;// number of good witness sets

	public StringBuffer toStringBuffer() {
		StringBuffer s = new StringBuffer();
		s.append("*Language Emptiness Result:\n");
		if (this.A != null)
			s.append(A.toString()).append(": ");
		s.append(isEmpty ? "empty" : "non-empty");
		if (this.x != null)
			s.append(" for x=").append(x.toString());
		s.append(" at L=").append(this.length).append("\n");
		if (trace != null)
			s.append("Trace Example: ").append(this.trace).append("\n");
		s.append("CPU Time: ").append(this.time).append(" ms.\n");
		s.append(super.toStringBuffer());
		return s;
	}

	public VerificationMessage(String error) {
		this.ErrorMessage = error;
	}

	public VerificationMessage(Fraction x, alg A, boolean isEmpty, long length,
	        long time) {
		this.x = x;
		this.A = A;
		this.isEmpty = isEmpty;
		this.length = length;
		this.time = time;
	}

	public VerificationMessage(Fraction x, alg A, boolean isEmpty, long length,
	        long time, String trace) {
		this.x = x;
		this.A = A;
		this.isEmpty = isEmpty;
		this.length = length;
		this.time = time;
		this.trace = trace;
	}

	public VerificationMessage() {

	}
}
