package framework.ds;

import java.util.List;

/**
 * Generalized KeySignature class for use navigating key
 * information. This class can be implemented but should
 * be used very rarely. Try to use DiatonicKeySignature
 * or another specialized KeySinature class because of their
 * highly increased functionality.
 * 
 * @author Sam
 * 
 * @version 2.2
 *
 */
public abstract class KeySignature {
	
	public static final int HALF_STEP = 1;

	public static final int WHOLE_STEP = 2;
	
	public static final int STEP_AND_A_HALF = 3;

	public static final int MAJOR = 0;

	public static final int MINOR = 1;

	/** 
	 * Number of sharps or flats in the key signature.
	 * This value is only useful for major and minor
	 * scales. For modal scales, write in the keySignature
	 * of C Major or A Minor(0)
	**/
	protected int sharpsOrFlats;

	/** 
	 * Whether the key is major (0) or minor (1).  
	 * Integer to match JMusic library
	**/
	protected int keyQuality;
	
	/** Name of the key **/
	protected String keyName;
	
	/** Pitches in the key's scale **/
	protected int[] scale;
	
	/**
	 * Returns whether the pitch is part of the scale in
	 * this key.
	 * @param pitch: the pitch to be tested against this
	 * 				 key's scale.
	 * @return true if the pitch is part of this key's
	 * 		   scale.
	 */
	public boolean matchesKey(int pitch) {
		for (int i = 0; i < scale.length; i++) {
			// offsets of 1 octave are still in-key
			if (scale[i] % 12 == pitch % 12) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns whether the pitches in the chord are part 
	 * of the scale in this key.
	 * @param chord: the list of integer pitches in the 
	 * 				 chord to be tested against this
	 * 				 key's scale.
	 * @return true if the chord fits this key's scale.
	 */
	public boolean chordMatchesKey(List<Integer> chord) {
		for (int i = 0; i < chord.size(); i++) {
			if (!matchesKey(chord.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns the number of sharps or flats in the key
	 * Positive numbers are the number of sharps, negative
	 * numbers are the number of flats.
	 * @return int sharps or flats.
	 */
	public int getNumSharpsOrFlats(){
		return sharpsOrFlats;
	}
	
	/**
	 * Returns whether the key is major or minor. Major keys
	 * return 0, minor keys return 1.
	 * @return int key quality.
	 */
	public int getQuality(){
		return keyQuality;
	}
	
	/**
	 * Returns the name of the key, in string format, for
	 * example, C Sharp Major would be returned as "C# Major",
	 * and B flat Minor would be returned as "Bb Minor".
	 * @note KeySignature never properly sets the name of the
	 *       key. Use DiatonicKeySignature or another extension
	 *       of KeySignature for full functionality.
	 * @return keyName
	 */
	public String getKeyName() {
		return keyName;
	}
	
	/**
	 * Returns the nth pitch in this key, with 0 returning the tonic
	 * @return nth pitch as a JMusic Pitch constant
	 */
	public int getNthPitch(int n) {
		return scale[n];
	}
	
	/**
	 * Returns the tonic of this key, the first pitch in its scale
	 * @return tonic pitch as a JMusic Pitch constant
	 */
	public int getTonic() {
		return getNthPitch(0);
	}
	
	/**
	 * Returns the number of notes per octave in this key
	 * @return tonic pitch as a JMusic Pitch constant
	 */
	public int getScaleSize() {
		return scale.length;
	}
}
