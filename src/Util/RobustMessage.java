package Util;

import org.apache.commons.math3.fraction.Fraction;

public class RobustMessage extends message {
	public long L;
	public Fraction BKDr;
	public long timeBKD = 0L;
	public Long lengthBKD;
	public int BKDws;
	public Fraction fwdPrecision;
	public Fraction FWDl;
	public Fraction FWDu;
	public long timeFWD = 0L;
	public int FWDws;

	public StringBuffer toStringBuffer() {
		StringBuffer s = new StringBuffer();
		s.append("*Robustness Result:\n");
		s.append("L_upperBound = ").append(L).append("\n");
		if (BKDr != null)
			s.append("BKD: R = ").append(BKDr).append(" at L = ")
			        .append(lengthBKD).append(", time = ")
			        .append(timeBKD).append(" ms;\n");
		if (FWDu != null)
			s.append("FWD: R = (").append(FWDl).append(",").append(FWDu)
			        .append("] at precision ").append(fwdPrecision)
			        .append(", time = ").append(timeFWD).append(" ms;\n");
		s.append(super.toStringBuffer());
		return s;
	}
}
