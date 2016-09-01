package Util;

public class message {
	//public String testCase;
	public String ErrorMessage = null;// "null" implies no error
	public String note = null;// e.g. warning message

	public StringBuffer toStringBuffer() {
		StringBuffer s = new StringBuffer();
		//if (this.testCase != null)
		//	s.append(this.testCase);
		if (this.ErrorMessage != null)
			s.append("[ERROR]").append(this.ErrorMessage).append("\n");
		if (this.note != null)
			s.append("Note: ").append(this.note).append("\n");
		return s;
	}
}
