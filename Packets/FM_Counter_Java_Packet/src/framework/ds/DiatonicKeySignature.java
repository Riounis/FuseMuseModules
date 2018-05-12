package framework.ds;

import jm.constants.Pitches;

/**
 * Highly specialized KeySignature class for diatonic
 * key signature representation. Has much more functionality
 * than KeySignature.
 * 
 * @author Sam
 * 
 * @version 1.2
 * 
 */
public class DiatonicKeySignature extends KeySignature{
	
	/** Distance in semitones between tonic and supertonic **/
	private int firstInterval;
	
	/** Distance in semitones between supertonic and mediant **/
	private int secondInterval;
		
	/** Distance in semitones between mediant and subdominant **/
	private int thirdInterval;
	
	/** Distance in semitones between subdominant and dominant **/
	private int fourthInterval;
	
	/** Distance in semitones between dominant and submediant **/
	private int fifthInterval;
	
	/** Distance in semitones between submediant and leading tone **/
	private int sixthInterval;
	
	/** Distance in semitones between leading tone and tonic **/
	private int seventhInterval;
	
	/** Flags if the key signature is to be written with sharps **/
	private boolean sharps;
	
	/** Flags if the key signature is to be written with flats **/
	private boolean flats;
	
	/**
	 * Constructs a key signature given its name, for example:
	 * "Ab Major" or "C Aeolian". Please ensure that the tonic
	 * note for your key is capitalized. Also note that we do
	 * not support theoretical keys (any key with a double sharp
	 * or flat is theoretical).
	 * @param name: the name of the key signature to construct
	 * @unfinished
	 */
	public DiatonicKeySignature(String name) {
		keyName = name;
		sharps = false;
		flats = false;
		String mode = "";
		int tonic = -1;
		switch(name.substring(0, 2)) {
		case "C ":
			tonic = Pitches.C3;
			mode = name.substring(2);
			if (mode.equals("Minor") || mode.equals("Aeolian")) {
				flats = true;
			}
			break;
		case "C#":
			tonic = Pitches.CS3;
			mode = name.substring(3);
			sharps = true;
			break;
		case "Db":
			tonic = Pitches.DF3;
			mode = name.substring(3);
			flats = true;
			break;
		case "D ":
			tonic = Pitches.D3;
			mode = name.substring(2);
			if (mode.equals("Major") || mode.equals("Ionian")) {
				sharps = true;
			}
			else if (mode.equals("Minor") || mode.equals("Aeolian")) {
				flats = true;
			}
			break;
		case "D#":
			tonic = Pitches.DS3;
			mode = name.substring(3);
			sharps = true;
			break;
		case "Eb":
			tonic = Pitches.EF3;
			mode = name.substring(3);
			flats = true;
			break;
		case "E ":
			tonic = Pitches.E3;
			mode = name.substring(2);
			sharps = true;
			break;
		case "E#":
			tonic = Pitches.F3;
			mode = name.substring(3);
			sharps = true;
			break;
		case "Fb":
			tonic = Pitches.E3;
			mode = name.substring(3);
			flats = true;
			break;
		case "F ":
			tonic = Pitches.F3;
			mode = name.substring(2);
			flats = true;
			break;
		case "F#":
			tonic = Pitches.FS3;
			mode = name.substring(3);
			sharps = true;
			break;
		case "Gb":
			tonic = Pitches.GF3;
			mode = name.substring(3);
			flats = true;
			break;
		case "G ":
			tonic = Pitches.G3;
			mode = name.substring(2);
			if (mode.equals("Major") || mode.equals("Ionian")) {
				sharps = true;
			}
			else if (mode.equals("Minor") || mode.equals("Aeolian")) {
				flats = true;
			}
			break;
		case "G#":
			tonic = Pitches.GS3;
			mode = name.substring(3);
			sharps = true;
			break;
		case "Ab":
			tonic = Pitches.AF3;
			mode = name.substring(3);
			flats = true;
			break;
		case "A ":
			tonic = Pitches.A3;
			mode = name.substring(2);
			if (mode.equals("Major") || mode.equals("Ionian")) {
				sharps = true;
			}
			break;
		case "A#":
			tonic = Pitches.AS3;
			mode = name.substring(3);
			sharps = true;
			break;
		case "Bb":
			tonic = Pitches.BF3;
			mode = name.substring(3);
			flats = true;
			break;
		case "B ":
			tonic = Pitches.B3;
			mode = name.substring(2);
			sharps = true;
			break;
		case "B#":
			tonic = Pitches.C4;
			mode = name.substring(3);
			sharps = true;
			break;
		case "Cb":
			tonic = Pitches.B3;
			mode = name.substring(3);
			flats = true;
			break;
		}
		identifyScale(mode, tonic);
	}

	/**
	 * Constructs a diatonic scale from a tonic note and seven
	 * intervals, which add up to 12 semitones.
	 * @param tonic: the tonic note of the key
	 * @param x1,x2,x3,x4,x5,x6,x7: seven note intervals, in semitones
	 * @throws Exception 
	 */
	public DiatonicKeySignature(int ton, int x1, int x2,
			int x3, int x4, int x5, int x6, int x7) throws Exception {
		int tonic = ton;
		if ( (x1 + x2 + x3 + x4 + x5 + x6 + x7) == 12 ) {
			keyName = "Custom Scale";
			sharpsOrFlats = 0;
			keyQuality = MAJOR;
			firstInterval = x1;
			secondInterval = x2;
			thirdInterval = x3;
			fourthInterval = x4;
			fifthInterval = x5;
			sixthInterval = x6;
			seventhInterval = x7;
		}
		else {
			throw new Exception("Intervals in custom scales must add to 12 semitones");
		}
		calculateScale(tonic);
	}
	
	/**
	 * Constructs a diatonic scale from a number of sharps (+) or flats (-) and
	 * a keyQuality; translates easily to JMusic KeySignature
	 * @param sof: number of sharps or flats in the key signature
	 * @param quality: whether the key is major (0) or minor (1)
	 */
	public DiatonicKeySignature(int sof, int quality) {
		keyQuality = quality;
		if (keyQuality == 0) {
			ionian();
		}
		else {
			aeolian();
		}
		sharpsOrFlats = sof;
		int tonic = -1;
		switch (sharpsOrFlats) {
		case 7:
			if (keyQuality == 0) {
				keyName = "C# Major";
				tonic = Pitches.CS3;
			}
			else {
				keyName = "A# Minor";
				tonic = Pitches.AS3;
			}
			break;
		case 6:
			if (keyQuality == 0) {
				keyName = "F# Major";
				tonic = Pitches.FS3;
			}
			else {
				keyName = "D# Minor";
				tonic = Pitches.DS3;
			}
			break;
		case 5:
			if (keyQuality == 0) {
				keyName = "B Major";
				tonic = Pitches.B3;
			}
			else {
				keyName = "G# Minor";
				tonic = Pitches.GS3;
			}
			break;
		case 4:
			if (keyQuality == 0) {
				keyName = "E Major";
				tonic = Pitches.E3;
			}
			else {
				keyName = "C# Minor";
				tonic = Pitches.CS3;
			}
			break;
		case 3:
			if (keyQuality == 0) {
				keyName = "A Major";
				tonic = Pitches.A3;
			}
			else {
				keyName = "F# Minor";
				tonic = Pitches.FS3;
			}
			break;
		case 2:
			if (keyQuality == 0) {
				keyName = "D Major";
				tonic = Pitches.D3;
			}
			else {
				keyName = "B Minor";
				tonic = Pitches.B3;
			}
			break;
		case 1:
			if (keyQuality == 0) {
				keyName = "G Major";
				tonic = Pitches.G3;
			}
			else {
				keyName = "E Minor";
				tonic = Pitches.E3;
			}
			break;
		case 0:
			if (keyQuality == 0) {
				keyName = "C Major";
				tonic = Pitches.C3;
			}
			else {
				keyName = "A Minor";
				tonic = Pitches.A3;
			}
			break;
		case -1:
			if (keyQuality == 0) {
				keyName = "F Major";
				tonic = Pitches.F3;
			}
			else {
				keyName = "D Minor";
				tonic = Pitches.D3;
			}
			break;
		case -2:
			if (keyQuality == 0) {
				keyName = "Bb Major";
				tonic = Pitches.BF3;
			}
			else {
				keyName = "G Minor";
				tonic = Pitches.G3;
			}
			break;
		case -3:
			if (keyQuality == 0) {
				keyName = "Eb Major";
				tonic = Pitches.EF3;
			}
			else {
				keyName = "C Minor";
				tonic = Pitches.C3;
			}
			break;
		case -4:
			if (keyQuality == 0) {
				keyName = "Ab Major";
				tonic = Pitches.AF3;
			}
			else {
				keyName = "F Minor";
				tonic = Pitches.F3;
			}
			break;
		case -5:
			if (keyQuality == 0) {
				keyName = "Db Major";
				tonic = Pitches.DF3;
			}
			else {
				keyName = "Bb Minor";
				tonic = Pitches.BF3;
			}
			break;
		case -6:
			if (keyQuality == 0) {
				keyName = "Gb Major";
				tonic = Pitches.GF3;
			}
			else {
				keyName = "Eb Minor";
				tonic = Pitches.EF3;
			}
			break;
		case -7:
			if (keyQuality == 0) {
				keyName = "Cb Major";
				tonic = Pitches.CF3;
			}
			else {
				keyName = "Ab Minor";
				tonic = Pitches.AF3;
			}
			break;
		}
		calculateScale(tonic);
	}
	
	/**
	 * Returns the JMusic pitch of the tonic note for 
	 * the key.
	 * @pre: calculateScale() must be called before this method
	 * @return int tonic note.
	 */
	public int tonic(){
		return scale[0];
	}
	
	/**
	 * Returns the JMusic pitch of the supertonic note for 
	 * the key.
	 * @pre: calculateScale() must be called before this method
	 * @return int tonic note.
	 */
	public int supertonic(){
		return scale[1];
	}
	
	/**
	 * Returns the JMusic pitch of the mediant note for 
	 * the key.
	 * @pre: calculateScale() must be called before this method
	 * @return int tonic note.
	 */
	public int mediant(){
		return scale[2];
	}
	
	/**
	 * Returns the JMusic pitch of the subdominant note for 
	 * the key.
	 * @pre: calculateScale() must be called before this method
	 * @return int tonic note.
	 */
	public int subdominant(){
		return scale[3];
	}
	
	/**
	 * Returns the JMusic pitch of the dominant note for 
	 * the key.
	 * @pre: calculateScale() must be called before this method
	 * @return int tonic note.
	 */
	public int dominant(){
		return scale[4];
	}
	
	/**
	 * Returns the JMusic pitch of the submediant note for 
	 * the key.
	 * @pre: calculateScale() must be called before this method
	 * @return int tonic note.
	 */
	public int submediant(){
		return scale[5];
	}
	
	/**
	 * Returns the JMusic pitch of the leading tone for 
	 * the key.
	 * @pre: calculateScale() must be called before this method
	 * @return int tonic note.
	 */
	public int leadingTone(){
		return scale[6];
	}
	
	/**
	 * Returns the first interval in the key.
	 * @return int first interval.
	 */
	public int firstInterval(){
		return firstInterval;
	}
	
	/**
	 * Returns the second interval in the key.
	 * @return int second interval.
	 */
	public int secondInterval(){
		return secondInterval;
	}
	
	/**
	 * Returns the third interval in the key.
	 * @return int third interval.
	 */
	public int thirdInterval(){
		return thirdInterval;
	}
	
	/**
	 * Returns the fourth interval in the key.
	 * @return int fourth interval.
	 */
	public int fourthInterval(){
		return fourthInterval;
	}
	
	/**
	 * Returns the fifth interval in the key.
	 * @return int fifth interval.
	 */
	public int fifthInterval(){
		return fifthInterval;
	}
	
	/**
	 * Returns the sixth interval in the key.
	 * @return int sixth interval.
	 */
	public int sixthInterval(){
		return sixthInterval;
	}
	
	/**
	 * Returns the seventh interval in the key.
	 * @return int seventh interval.
	 */
	public int seventhInterval(){
		return seventhInterval;
	}
	
	/**
	 * Helper method for setting intervals in ionian
	 * mode.
	 */
	private void ionian(){
		keyQuality = MAJOR;
		firstInterval = WHOLE_STEP;
		secondInterval = WHOLE_STEP;
		thirdInterval = HALF_STEP;
		fourthInterval = WHOLE_STEP;
		fifthInterval = WHOLE_STEP;
		sixthInterval = WHOLE_STEP;
		seventhInterval = HALF_STEP;
	}
	
	/**
	 * Helper method for setting intervals in dorian
	 * mode.
	 */
	private void dorian(){
		keyQuality = MINOR;
		sharpsOrFlats = 0;
		firstInterval = WHOLE_STEP;
		secondInterval = HALF_STEP;
		thirdInterval = WHOLE_STEP;
		fourthInterval = WHOLE_STEP;
		fifthInterval = WHOLE_STEP;
		sixthInterval = HALF_STEP;
		seventhInterval = WHOLE_STEP;
	}
	
	/**
	 * Helper method for setting intervals in phrygian
	 * mode.
	 */
	private void phrygian(){
		keyQuality = MINOR;
		sharpsOrFlats = 0;
		firstInterval = HALF_STEP;
		secondInterval = WHOLE_STEP;
		thirdInterval = WHOLE_STEP;
		fourthInterval = WHOLE_STEP;
		fifthInterval = HALF_STEP;
		sixthInterval = WHOLE_STEP;
		seventhInterval = WHOLE_STEP;
	}
	
	/**
	 * Helper method for setting intervals in lydian
	 * mode.
	 */
	private void lydian(){
		keyQuality = MAJOR;
		sharpsOrFlats = 0;
		firstInterval = WHOLE_STEP;
		secondInterval = WHOLE_STEP;
		thirdInterval = WHOLE_STEP;
		fourthInterval = HALF_STEP;
		fifthInterval = WHOLE_STEP;
		sixthInterval = WHOLE_STEP;
		seventhInterval = HALF_STEP;
	}
	
	/**
	 * Helper method for setting intervals in mixolydian
	 * mode.
	 */
	private void mixolydian(){
		keyQuality = MAJOR;
		sharpsOrFlats = 0;
		firstInterval = WHOLE_STEP;
		secondInterval = WHOLE_STEP;
		thirdInterval = HALF_STEP;
		fourthInterval = WHOLE_STEP;
		fifthInterval = WHOLE_STEP;
		sixthInterval = HALF_STEP;
		seventhInterval = WHOLE_STEP;
	}
	
	/**
	 * Helper method for setting intervals in aeolian
	 * mode.
	 */
	private void aeolian(){
		keyQuality = MINOR;
		firstInterval = WHOLE_STEP;
		secondInterval = HALF_STEP;
		thirdInterval = WHOLE_STEP;
		fourthInterval = WHOLE_STEP;
		fifthInterval = HALF_STEP;
		sixthInterval = WHOLE_STEP;
		seventhInterval = WHOLE_STEP;
	}
	
	/**
	 * Helper method for setting intervals in locrian
	 * mode.
	 */
	private void locrian(){
		keyQuality = MINOR;
		sharpsOrFlats = 0;
		firstInterval = HALF_STEP;
		secondInterval = WHOLE_STEP;
		thirdInterval = WHOLE_STEP;
		fourthInterval = HALF_STEP;
		fifthInterval = WHOLE_STEP;
		sixthInterval = WHOLE_STEP;
		seventhInterval = WHOLE_STEP;
	}
	
	/**
	 * Helper method for identifying the scale and determining
	 * number of sharps or flats in a key signature.
	 */
	private void identifyScale(String mode, int tonic){
		switch (mode) {
		case "Minor":
			aeolian();
			calculateScale(tonic);
			calculateNumSharpsOrFlats();
			break;
		case "Ionian":
			ionian();
			calculateScale(tonic);
			calculateNumSharpsOrFlats();
			break;
		case "Dorian":
			dorian();
			calculateScale(tonic);
			break;
		case "Phrygian":
			phrygian();
			calculateScale(tonic);
			break;
		case "Lydian":
			lydian();
			calculateScale(tonic);
			break;
		case "Mixolydian":
			mixolydian();
			calculateScale(tonic);
			break;
		case "Aeolian":
			aeolian();
			calculateScale(tonic);
			calculateNumSharpsOrFlats();
			break;
		case "Locrian":
			locrian();
			calculateScale(tonic);
			break;
		default:
			System.err.println("identifyScale failed to find{" + mode + "}, defaulting to Major");
		case "Major":
			ionian();
			calculateScale(tonic);
			calculateNumSharpsOrFlats();
		}
	}
	
	/**
	 * Helper method for calculating the scale in this
	 * key.
	 */
	private void calculateScale(int tonic){
		int supertonic = tonic + firstInterval;
		int mediant = supertonic + secondInterval;
		int subdominant = mediant + thirdInterval;
		int dominant = subdominant + fourthInterval;
		int submediant = dominant + fifthInterval;
		int leadingTone = submediant + sixthInterval;
		int[] newScale = {tonic, supertonic, mediant, subdominant, dominant, submediant, leadingTone};
		scale = newScale;
		/*System.out.println("calculateScale\n==========");
		for(int i : scale) {
			System.out.println(i);
		}
		System.out.println(firstInterval);
		System.out.println(secondInterval);
		System.out.println(thirdInterval);
		System.out.println(fourthInterval);
		System.out.println(fifthInterval);
		System.out.println(sixthInterval);*/
	}
	
	/**
	 * Helper method for calculating the number of sharps
	 * or flats in the scale.
	 * 
	 * @pre: calculateScale() must be called before this method
	 */
	private void calculateNumSharpsOrFlats(){
		boolean sharpFlag = false;
		boolean flatFlag = false;
		if (keyName.substring(1,2).equals("#")) {
			sharpFlag = true;
		}
		else if (keyName.substring(1,2).equals("b")) {
			flatFlag = true;
		}
		int absoluteVal = 0;
		int[] reducedScale = new int[7];
		reducedScale[0] = scale[0] % 12;
		reducedScale[1] = scale[1] % 12;
		reducedScale[2] = scale[2] % 12;
		reducedScale[3] = scale[3] % 12;
		reducedScale[4] = scale[4] % 12;
		reducedScale[5] = scale[5] % 12;
		reducedScale[6] = scale[6] % 12;
		// Checks to see if the scale contains any sharp or flat notes
		for (int i = 0; i < reducedScale.length; i++) {
			if (reducedScale[i] == 1 || reducedScale[i] == 3
					|| reducedScale[i] == 6 || reducedScale[i] == 8
					|| reducedScale[i] == 10 || (sharpFlag && reducedScale[i] == 0)
					|| (sharpFlag && reducedScale[i] == 5) 
					|| (flatFlag && reducedScale[i] == 4) 
					|| (flatFlag && reducedScale[i] == 11)) {
				absoluteVal++;
			}
		}
		if (sharps) {
			sharpsOrFlats = absoluteVal;
		}
		else if (flats) {
			sharpsOrFlats = -1 * absoluteVal;
		}
		else {
			sharpsOrFlats = 0;
		}
		
	}
	
	public String toString() {
		return keyName;
	}
}
