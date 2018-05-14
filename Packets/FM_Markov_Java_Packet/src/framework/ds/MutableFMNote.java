package framework.ds;

/**
 * Editable version of an FMNote, used for storing pitches for use by Packets and Driver Modules.
 */
public class MutableFMNote extends FMNote{

	/**
	 * Constructor
	 * @param ptch pitch of the note
	 * @param dur duration of the note
	 * @param tied whether the note is tied to the next note
	 */
	public MutableFMNote(int ptch, int dur, boolean tied) {
		super(ptch, dur, tied);
	}

	/**
	 * Constructor
	 * @param ptch pitch of the note
	 * @param dur duration of the note
	 */
	public MutableFMNote(int ptch, int dur) {
		this(ptch, dur, false);
	}
	
	/**
	 * Sets the pitch of the note.
	 * @param newPitch
	 */
	public void setPitch(int newPitch) { pitch = newPitch; }
	
	/**
	 * Sets the duration of the note.
	 * @param newDuration
	 */
	public void setDuration(int newDuration) { duration = newDuration; }
	
	/**
	 * Sets the status of whether the note is tied to the next note or not.
	 * @param newTied
	 */
	public void setTiedToNext(boolean newTied) { tiedToNext = newTied; }
}
