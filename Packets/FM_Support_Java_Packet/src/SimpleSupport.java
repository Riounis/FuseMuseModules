import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;

import framework.packet.FMPacket;
import framework.ds.FMComposition;
import framework.ds.FMCompositionSegment;
import framework.ds.FMNote;
import jm.music.data.Part;
import jm.music.data.Phrase;

/**
 * Provides an executor for an interval-based Markov chain of arbitrary order.
 * For additional information, see Implementation Details in Papers/MarkovChains.pdf
 * 
 * @author Jake
 *
 */
public class SimpleSupport implements FMPacket{
	
	/**
	 * Control the packet's behavior
	 * Pitch modes:
	 * 	Basic: Play all pitches in chord progression
	 * 	Low: Plays lowest pitch in chord progression
	 * 	Low+Alt: Plays lowest pitch and alternates between other 2
	 * Rhythm modes:
	 * 	Basic: Plays as defined
	 * 	Beat: Plays on beat (Quarter notes in 4/4, for example)
	 */
	private String PITCH_MODE;
	private String RHYTHM_MODE;

	private static final String BASIC_PITCH_MODE = "Basic";
	private static final String LOW_PITCH_MODE = "Low";
	private static final String LOW_ALT_PITCH_MODE = "Low+Alt";
	private boolean pitchAlternator = false;
	
	private static final String BASIC_RHYTHM_MODE = "Basic";
	private static final String BEAT_RHYTHM_MODE = "Beat";

	/**
	 * Executor method for the packet.
	 */
	@Override
	public Collection<Part> executeSupport(FMComposition composition, DefaultMutableTreeNode node) {
		
		loadOptions();
		
		Collection<Part> allparts = new LinkedList<>();
		
		execute(allparts, composition);
		
		//Tell the shell which parts we just added
		return allparts;
	}

	/**
	 * Executes the packet
	 */
	private Collection<Part> execute(Collection<Part> allparts, FMComposition composition) {
		List<FMCompositionSegment> parts = composition.getCompositionSegments();
		//System.err.println(parts.toString());
		Map<FMCompositionSegment, Part> segmentPhrases = new HashMap<>();
		double time = 0;
		for(FMCompositionSegment fmcs : parts) {
			if(segmentPhrases.get(fmcs) == null) {
				Part part = createPart(fmcs);
				segmentPhrases.put(fmcs, part);
			}
			Part addingPart = segmentPhrases.get(fmcs);
			Phrase addingPhrase = new Phrase(addingPart.getPhrase(0).getNoteArray());
			double phraselen = addingPhrase.getEndTime();
			addingPhrase.setStartTime(time);
			time += phraselen;
			Part newPart = new Part(addingPhrase);
			allparts.add(newPart);
		}
		return allparts;
	}

	private Part createPart(FMCompositionSegment fmcs) {
		Phrase phrase = new Phrase();
		phrase.setTempo(fmcs.getTempo());
		phrase.setDenominator(fmcs.getTimeSignatureDenominator());
		phrase.setNumerator(fmcs.getTimeSignatureNumerator());

		SortedMap<Integer, Integer> notes;
		if(RHYTHM_MODE.equals(BEAT_RHYTHM_MODE)) {
			notes = new TreeMap<Integer, Integer>();
			int segmentDuration = fmcs.getDuration();
			int beat = FMNote.WHOLE_NOTE / fmcs.getTimeSignatureDenominator();
			for(int time = 0; time < segmentDuration; time += beat) {
				notes.put(time, beat);
			}
		} else {
			notes = fmcs.getRhythm();
		}
		
		for(Integer noteTime : notes.keySet()) {
			int[] chord = collectionToIntArray(fmcs.getPitchesAtPosition(noteTime));
			System.err.print("Chord: ");
			for(int pit : chord) System.err.print(pit + " ");
			System.err.println();
			intArraySubtract(chord, 12);
			double rhythmVal = FMComposition.getJMRhythmValue(notes.get(noteTime));
			switch(PITCH_MODE) {
			default:
			case BASIC_PITCH_MODE:
				addPitchesBasic(fmcs, phrase, noteTime, chord, rhythmVal);
				break;
			case LOW_PITCH_MODE:
				addPitchesLow(fmcs, phrase, noteTime, chord, rhythmVal);
				break;
			case LOW_ALT_PITCH_MODE:
				addPitchesLowAlt(fmcs, phrase, noteTime, chord, rhythmVal);
				break;
			}
		}
		
		
		Part part = new Part(phrase);
		part.setTempo(fmcs.getTempo());
		//System.err.println("FMCS TEMPO: " + fmcs.getTempo());
		part.setNumerator(fmcs.getTimeSignatureNumerator());
		part.setDenominator(fmcs.getTimeSignatureDenominator());
		part.setKeySignature(fmcs.getKeySignature().getNumSharpsOrFlats());
		part.setKeyQuality(fmcs.getKeySignature().getQuality());
		return part;
	}

	private void addPitchesBasic(FMCompositionSegment fmcs, Phrase phrase, Integer noteTime, int[] chord, double rhythmVal) {
		phrase.addChord(chord, rhythmVal);
	}

	private void addPitchesLow(FMCompositionSegment fmcs, Phrase phrase, Integer noteTime, int[] chord, double rhythmVal) {
		Arrays.sort(chord);
		int[] low = {chord[0]};
		phrase.addChord(low, rhythmVal);
	}

	/**
	 * @param chord array of 3 pitches (ints)
	 */
	private void addPitchesLowAlt(FMCompositionSegment fmcs, Phrase phrase, Integer noteTime, int[] chord, double rhythmVal) {
		Arrays.sort(chord);
		int[] low = {chord[0], 
				pitchAlternator ? chord[1] : chord[2]};
		phrase.addChord(low, rhythmVal);
		pitchAlternator = !pitchAlternator;
	}

	private int[] collectionToIntArray(Collection<Integer> c) {
		ArrayList<Integer> a = new ArrayList<>(c);
		int[] ia = new int[a.size()];
		for(int i = 0; i < ia.length; i++) ia[i] = a.get(i);
		return ia;
	}
	
	private void intArraySubtract(int[] a, int i) {
		for(int j = 0; j < a.length; j++) {
			a[j] -= i;
		}
	}

	/**
	 * This method should never be called - this is a Support packet.
	 */
	@Override
	public Collection<Part> executeHarmony(FMComposition arg0, DefaultMutableTreeNode arg1) {
		throw new UnsupportedOperationException("Simple Support cannot play harmony");
	}

	/**
	 * This method should never be called - this is a Support packet.
	 */
	@Override
	public Collection<Part> executeMelody(FMComposition arg0, DefaultMutableTreeNode arg1) {
		throw new UnsupportedOperationException("Simple Support cannot play melody");
	}
	
	private void loadOptions() {
		Properties props = new Properties();
		try {
			props.load(getClass().getResourceAsStream("options"));
			PITCH_MODE = props.getProperty("PitchMode");
			RHYTHM_MODE = props.getProperty("RhythmMode");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
