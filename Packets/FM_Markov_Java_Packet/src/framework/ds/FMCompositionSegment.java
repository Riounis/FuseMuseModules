package framework.ds;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Data structure that corresponds to an entire section of the pattern of a composition
 * as determined by the Driver Module. Within an FMCompositionSegment, all measures will
 * be in the same key, have the same time signature, and define a rhythm and chord progression
 * that is unique to this composition segment.
 */
public class FMCompositionSegment implements Iterable<FMMeasure> {
	
	// key of the segment.
	private KeySignature keySig;
	
	// tempo of the composition segment, in beats per minute
	private int tempo;
	
	// time signature
	private TimeSignature timeSig;
	
	// measures
	private List<FMMeasure> measures;
	
	/**
	 * Constructs a composition segment from a KeySignature
	 * and a tempo.
	 * @param ks: the key signature of the segment
	 * @param temp: tempo in beats per minute
	 */
	public FMCompositionSegment(KeySignature ks, int temp) {
		keySig = ks;
		tempo = temp;
		try {
			timeSig = new TimeSignature(4, 4);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		measures = new LinkedList<>();
	}
	
	/**
	 * Returns the tempo of the composition in beats per minute
	 * @return tempo
	 */
	public int getTempo() { return tempo; }
	
	/**
	 * Adds a measure to the composition segment
	 * @param newMeasure
	 * @return whether the measure is added
	 */
	public boolean addMeasure(FMMeasure newMeasure) {
		// no elements yet, safe to add
		if (measures.size() == 0) {
			int timeSigNum = newMeasure.getTimeSignatureNumerator();
			int timeSigDenom = newMeasure.getTimeSignatureDenominator();
			try {
				timeSig = new TimeSignature(timeSigNum, timeSigDenom);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return measures.add(newMeasure);
		} else {
			if(timeSig.getBeatsPerMeasure() != newMeasure.getTimeSignatureNumerator() ||
					timeSig.getNoteGetsBeat() != newMeasure.getTimeSignatureDenominator()) {
				throw new IllegalArgumentException("Time signature must be consistent"
						+ "throughout the composition segment.");
			} else {
				return measures.add(newMeasure);
			}
		}
	}
	
	/**
	 * Returns the KeySignature object of the composition segment.
	 * @return key
	 */
	public KeySignature getKeySignature() { return keySig; }

	/**
	 * Returns the number of beats per measure for the composition segment.
	 * @return beats per measure
	 */
	public int getTimeSignatureNumerator() {
		return timeSig.getBeatsPerMeasure();
	}
	
	/**
	 * Returns the note that gets the beat for each measure in the composition segment.
	 * @return note gets beat
	 */
	public int getTimeSignatureDenominator() {
		return timeSig.getNoteGetsBeat();
	}

	/**
	 * Returns the time signature
	 * @return time signature
	 */
	public TimeSignature getTimeSignature() {
		return timeSig;
	}
	
	/**
	 * Returns the duration of the composition segment in FMNote rhythm value units.
	 * @return duration
	 */
	public int getDuration() {
		if(measures.isEmpty()) return -1;
		return measures.get(0).getMeasureLength() * measures.size();
	}
	
	/**
	 * Returns a list of the measures in this composition segment.
	 * @return measureList
	 */
	public List<FMMeasure> getMeasures() {
		return Collections.unmodifiableList(measures);
	}
	
	/**
	 * Returns the number of measures in this composition segment.
	 * @return numMeasures
	 */
	public int getNumMeasures() {
		return measures.size();
	}
	
	/**
	 * Returns a map of the starting position and duration of all notes in the composition segment,
	 * in FMNote rhythm value units.
	 * @return map of rhythm
	 */
	public SortedMap<Integer, Integer> getRhythm() {
		SortedMap<Integer, Integer> res = new TreeMap<>();
		int currentMeasureStartPosition = 0;
		for(FMMeasure fmm : measures) {
			SortedMap<Integer, Integer> measureRhythm = fmm.getRhythm();
			for(Integer startInMeasure : measureRhythm.keySet()) {
				res.put(startInMeasure + currentMeasureStartPosition,
						measureRhythm.get(startInMeasure));
			}
			currentMeasureStartPosition += fmm.getMeasureLength();
		}
		return res;
	}
	
	/**
	 * Returns the chord at the position provided in FMNote rhythm value units.
	 * @param position
	 * @return collection of pitches
	 */
	public Collection<Integer> getPitchesAtPosition(int position) {
		
		// if no measures or negative time, no pitches
		if(measures.isEmpty() || position < 0) return Collections.emptySet();
		
		int measureLength = measures.get(0).getMeasureLength();
		int whichMeasure = position / measureLength;
		
		//if off tail end, no pitches
		if(whichMeasure >= measures.size()) return Collections.emptySet();
		
		int positionInMeasure = position % measureLength;
		return measures.get(whichMeasure).getPitchesAtPosition(positionInMeasure);
	}
	
	/**
	 * Returns the remaining duration in FMRhythmValue units in the measure at the
	 * given position.
	 * @param position
	 * @return duration
	 */
	public int getRemainingMeasureDurationAtPosition(int position) {
		List<FMMeasure> measures = getMeasures();
		int currentMeasureStartPosition = 0;
		for(FMMeasure meas: measures) {
			if (position >= currentMeasureStartPosition &&
					position < currentMeasureStartPosition + meas.getMeasureLength()) {
				int posInMeasure = position - currentMeasureStartPosition;
				return meas.getMeasureLength() - posInMeasure;
			}
			currentMeasureStartPosition += meas.getMeasureLength();
		}
		return -1;
	}

	@Override
	public Iterator<FMMeasure> iterator() {
		return getMeasures().iterator();
	}
}
