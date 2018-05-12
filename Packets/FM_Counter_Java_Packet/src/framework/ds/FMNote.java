package framework.ds;

/**
 * Represents a musical note which contains information about its pitch, duration, and whether it is tied to the
 * next note.
 * 
 * Example of Rhythm Values in FMNote:
 * In a 4/4 measure, there are 4 beats in the measure and the quarter note receives 1 beat. If a note in a 4/4 measure
 * has a duration of 48 and starts at position 96 in the measure, it is an eighth note that begins on the second beat
 * of the measure.
 */
public class FMNote {
	// JMusic constant-based pitch
	protected int pitch;
	
	// Duration as multiple of 128th note = 3
	protected int duration;
	
	// If the note is tied to the next note
	protected boolean tiedToNext;
	
	/**
	 * Constructor
	 * @param ptch pitch of the note
	 * @param dur duration of the note
	 * @param tied whether the note is tied to the next note
	 */
	public FMNote(int ptch, int dur, boolean tied) {
		if(ptch < 0 && ptch != jm.constants.Pitches.REST) {
			throw new IllegalArgumentException("Pitch must be >= 0 or Rest (jm.constants.Pitches.REST).");
		}
		pitch = ptch;
		duration = dur;
	}
	
	/**
	 * Constructor
	 * @param ptch pitch of the note
	 * @param dur duration of the note
	 */
	public FMNote(int ptch, int dur) {
		this(ptch, dur, false);
	}

	/**
	 * Returns the pitch of the note.
	 * @return note's pitch
	 */
	public int getPitch() { return pitch; }
	
	/**
	 * Returns the duration of the note.
	 * @return note's duration
	 */
	public int getDuration() { return duration; }
	
	/**
	 * Returns whether the note is tied to the next note.
	 * @return tied
	 */
	public boolean getTiedToNext() { return tiedToNext; }
	
	/** Rhythm value of 3 */
	public static final int ONE_TWENTY_EIGHTH_NOTE = 3;
	/** Rhythm value of 2 */
	public static final int ONE_TWENTY_EIGHTH_NOTE_TRIPLET = 2;
	/** Rhythm value of 6 */
	public static final int SIXTY_FOURTH_NOTE = 6;
	/** Rhythm value of 4 */
	public static final int SIXTY_FOURTH_NOTE_TRIPLET = 4;
	/** Rhythm value of 9 */
	public static final int DOTTED_SIXTY_FOURTH_NOTE = 9;
	/** Rhythm value of 12 */
	public static final int THIRTY_SECOND_NOTE = 12;
	/** Rhythm value of 8 */
	public static final int THIRTY_SECOND_NOTE_TRIPLET = 8;
	/** Rhythm value of 18 */
	public static final int DOTTED_THIRTY_SECOND_NOTE = 18;
	/** Rhythm value of 24 */
	public static final int SIXTEENTH_NOTE = 24;
	/** Rhythm value of 12 */
	public static final int SIXTEENTH_NOTE_TRIPLET = 12;
	/** Rhythm value of 36 */
	public static final int DOTTED_SIXTEENTH_NOTE = 36;
	/** Rhythm value of 48 */
	public static final int EIGHTH_NOTE = 48;
	/** Rhythm value of 32 */
	public static final int EIGHTH_NOTE_TRIPLET = 32;
	/** Rhythm value of 72 */
	public static final int DOTTED_EIGHTH_NOTE = 72;
	/** Rhythm value of 96 */
	public static final int QUARTER_NOTE = 96;
	/** Rhythm value of 64 */
	public static final int QUARTER_NOTE_TRIPLET = 64;
	/** Rhythm value of 144 */
	public static final int DOTTED_QUARTER_NOTE = 144;
	/** Rhythm value of 192 */
	public static final int HALF_NOTE = 192;
	/** Rhythm value of 128 */
	public static final int HALF_NOTE_TRIPLET = 128;
	/** Rhythm value of 288 */
	public static final int DOTTED_HALF_NOTE = 288;
	/** Rhythm value of 384 */
	public static final int WHOLE_NOTE = 384;
}
