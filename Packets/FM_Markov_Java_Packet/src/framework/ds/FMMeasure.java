package framework.ds;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import framework.ds.FMMeasure.PositionedFMNote;

/**
 * Represents a measure which holds notes. For use by Packets and Driver Modules.
 * 
 * See FMNote for documentation on rhythm values.
 */
public class FMMeasure {
	
	// beats per measure
	private int timeSigNumerator;
	
	// note that gets the beat
	private int timeSigDenominator;
	
	// length of a measure in FMNote rhythm value units
	protected int measureLength;
	
	// collection of notes in the measure
	protected Collection<PositionedFMNote> notes;
	
	/**
	 * Constructor
	 * @param timeSigNum number of beats in this measure
	 * @param timeSigDenom note that receives a beat in this measure
	 */
	public FMMeasure(int timeSigNum, int timeSigDenom) {
		timeSigNumerator = timeSigNum;
		timeSigDenominator = timeSigDenom;
		
		measureLength = timeSigNum * 384/timeSigDenom;
		
		notes = new LinkedList<PositionedFMNote>();
	}
	
	/**
	 * Constructor
	 * constructs a simple measure in 4/4 time
	 */
	public FMMeasure() {
		this(4, 4);
	}

	/**
	 * Returns the number of beats in this measure.
	 * @return beats per measure
	 */
	public int getTimeSignatureNumerator() { return timeSigNumerator; }
	
	/**
	 * Returns the bottom number of the time signature for this measure.
	 * @return note gets beat
	 */
	public int getTimeSignatureDenominator() { return timeSigDenominator; }
	
	/**
	 * Returns the length of the measure in FMNote rhythm value units.
	 * @return length of measure
	 */
	public int getMeasureLength() { return measureLength; }
	
	/**
	 * Returns the pitches of the chord being played at the position requested.
	 * @param position in FMNote rhythm value units
	 * @return pitches being played at the chosen position
	 */
	public Collection<Integer> getPitchesAtPosition(int position) {
		if(position < 0 || position > measureLength) return null;
		
		Collection<Integer> res = new HashSet<>();
		for(PositionedFMNote n : notes) {
			if(n.startPosition <= position && 
					n.startPosition + n.note.getDuration() > position) {
				res.add(n.note.pitch);
			}
		}
		return res;
	}
	
	/**
	 * Returns a map containing the start time and duration of all notes in the measure,
	 * using FMNote rhythm value units to describe both start time and duration.
	 * @return position and duration of notes
	 */
	public SortedMap<Integer, Integer> getRhythm() {
		SortedMap<Integer, Integer> res = new TreeMap<>();
		for(PositionedFMNote n : notes) {
			res.put(n.startPosition, n.note.getDuration());
		}
		return res;
	}
		
	/**
	 * A note that has been positioned within a measure. For use encapsulating notes to add to
	 * the measure.
	 */
	protected static class PositionedFMNote {
		public FMNote note;
		public int startPosition;
		
		public PositionedFMNote(FMNote n, int sp) {
			note = n;
			startPosition = sp;
		}
	}
	
	/**
	 * Adds a note to the measure at the given start position.
	 * @param note
	 * @param startPosition
	 * @return whether the note is added
	 */
	public boolean addNote(FMNote note, int startPosition) {
		for(PositionedFMNote n : notes) {
			if(n.note.equals(note) && n.startPosition == startPosition) {
				// Cannot place the same note at the same position twice
				return false;
			}
		}
		
		if(startPosition < 0) {
			// cannot exist before measure
			return false;
		}
		
		if((startPosition + note.getDuration()) > measureLength) {
			// Note cannot extend beyond end of measure
			return false;
		}
		
		return notes.add(new PositionedFMNote(note, startPosition));
		
	}
}
