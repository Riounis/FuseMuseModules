package framework.ds;

/**
 * Holds the numerator and denominator of a
 * time signature. Simplifies passing data around.
 * 
 * @author Sam
 * 
 * @version 1.0
 */
public class TimeSignature {

	/** beats per measure **/
	private int top;
	
	/** note that gets a beat **/
	private int bot;
	
	/**
	 * Constructs a time signature object
	 * @param num: the number of beats per measure
	 * @param denom: the note that gets a beat
	 * @throws Exception 
	 */
	public TimeSignature(int num, int denom) throws Exception {
		if (denom != 1 && denom != 2 && denom != 4 && denom != 8
				&& denom != 16 && denom != 32 && denom != 64) {
			throw new Exception("Invalid Time Signature");
		}
		top = num;
		bot = denom;
	}
	
	/**
	 * Constructs a time signature object from
	 * a string. Ex: "4/4".
	 * @param sig: the time signature, in string form
	 * @throws Exception 
	 */
	public TimeSignature(String sig) throws Exception {
		int slash = sig.indexOf("/");
		if (slash < 1) {
			throw new Exception("Improperly Formatted Time"
					+ "Signature");
		}
		String num = sig.substring(0, slash);
		String denom = sig.substring(slash + 1);
		int nume = Integer.parseInt(num);
		int den = Integer.parseInt(denom);
		if (den != 1 && den != 2 && den != 4 && den != 8
				&& den != 16 && den != 32 && den != 64) {
			throw new Exception("Invalid Time Signature");
		}
		top = nume;
		bot = den;
	}
	
	/**
	 * Returns the number of beats per measure.
	 * @return beats per measure
	 */
	public int getBeatsPerMeasure() {
		return top;
	}
	
	/**
	 * Returns the note which gets a beat.
	 * @return note that gets a beat
	 */
	public int getNoteGetsBeat() {
		return bot;
	}
	
	/**
	 * Returns the time signature as a string.
	 * @return timeSig
	 */
	public String toString() {
		return top+"/"+bot;
	}
}
