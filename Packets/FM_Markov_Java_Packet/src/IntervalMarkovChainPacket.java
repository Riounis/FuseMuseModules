import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import framework.packet.FMPacket;
import framework.ds.FMComposition;
import framework.ds.FMCompositionSegment;
import framework.ds.KeySignature;
import jm.constants.Pitches;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;

/**
 * Provides an executor for an interval-based Markov chain of arbitrary order.
 * For additional information, see Implementation Details in Papers/MarkovChains.pdf
 * 
 * @author Jake
 *
 */
public class IntervalMarkovChainPacket implements FMPacket{

	/** The name of the markov chain data file, including the extension if any */
	private static final String MARKOV_CHAIN_FILE = "markovChainData";

	/** Weight change to be added to chords that don't fit the piece's chord progression. */
	private static final float CHORD_FITTING_WEIGHT = 1;
	
	/** Order of the markov chain data, detected during execution */
	private int order = -1;
	
	private FMComposition compo;
	
	/** true when a triplet is being executed */
	private boolean inTriplet = false;
	
	/** notes left in triplet */
	private int remainingTriplet = 0;
	
	/** triplet note duration */
	private double dur = 0;
	
	/** Random number generator */
	Random rng;
	
	/**
	 * Executor method for the packet.  Gathers required information and executes the Markov chain.
	 */
	@Override
	public Collection<Part> executeMelody(FMComposition composition, DefaultMutableTreeNode node) {
		
		String seed = JOptionPane.showInputDialog("Please Enter a seed, or leave blank for a random seed.");
		if(seed.equals("")) {
			//System.out.println("No seed entered");
			rng = new Random();
		} else {
			rng = new Random(seed.hashCode());
		}
		
		compo = composition;
		
		Collection<Part> allparts = new LinkedList<>();
		
		// markovTable contains the markov chain structure.
		// Each String key is a state.  Outer table keys are 'from' states, Inner table keys are 'to' states, values are probabilities
		Map<String, Map<String, Float>> markovTable = loadMarkovChainData(MARKOV_CHAIN_FILE);
		setOrder(markovTable);
		
		//Execute the Markov Chain
		execute(allparts, markovTable, composition);
		
		//Tell the shell which part we just added
		return allparts;
	}
	
	/**
	 * Determines the order of the markov chain from the table
	 * This is only used to create the 'start' state string, and so could probably be more efficiently done
	 * @param markovTable The table structure containing the Markov chain
	 */
	private void setOrder(Map<String, Map<String, Float>> markovTable) {
		String anyState = markovTable.keySet().iterator().next();
		order = 1; //start at one to account for fencepost problem
		for(char c : anyState.toCharArray()) {
			if(c == '|') {
				order++;
			}
		}
	}

	/**
	 * Executes the markov chain along the length of the rhythm
	 * @param allparts 
	 * @param markovTable: Transition table
	 * @param chordProgression: list of chords, looped if necessary
	 * @param rhythm: Note lengths
	 * @return
	 */
	private Collection<Part> execute(Collection<Part> allparts, Map<String, Map<String, Float>> markovTable, FMComposition composition) {
		List<FMCompositionSegment> parts = composition.getCompositionSegments();
		//System.err.println(parts.toString());
		Map<FMCompositionSegment, Part> segmentPhrases = new HashMap<>();
		double time = 0;
		for(FMCompositionSegment fmcs : parts) {
			if(segmentPhrases.get(fmcs) == null) {
				Phrase phrase = new Phrase();
				phrase.setTempo(fmcs.getTempo());
				phrase.setDenominator(fmcs.getTimeSignatureDenominator());
				phrase.setNumerator(fmcs.getTimeSignatureNumerator());
				String lastNote = selectNote(markovTable, String.join("|", Collections.nCopies(order, "~")), composition.getKeySignatureAtPosition(0));
				
				double positionInPhrase = 0;
				//System.out.println(lastNote);
				
				while(positionInPhrase < fmcs.getDuration()) {
					//System.out.println(positionInPhrase + ", " + fmcs.getDuration());
					lastNote = selectNote(markovTable, lastNote, fmcs.getKeySignature());
					Note added = addNote(phrase, 0, lastNote);
					positionInPhrase += FMComposition.getFMRhythmValue(added.getRhythmValue());
					//System.out.println(lastNote);
				}
				Part part = new Part(phrase);
				part.setTempo(fmcs.getTempo());
				//System.out.println("FMCS TEMPO: " + fmcs.getTempo());
				part.setNumerator(fmcs.getTimeSignatureNumerator());
				part.setDenominator(fmcs.getTimeSignatureDenominator());
				part.setKeySignature(fmcs.getKeySignature().getNumSharpsOrFlats());
				part.setKeyQuality(fmcs.getKeySignature().getQuality());
				part.setInstrument(110);
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

	/**
	 * Adds a note to the given phrase
	 * @param phrase: The phrase to add the note to
	 * @param state: The current state, from which the pitch is extracted
	 * @param duration: The duration of the note to add
	 * @return Note: the note added to the phrase
	 */
	private Note addNote(Phrase phrase, int position, String state) {
		//Get the note to play from the tonic and interval.
		String note = state.substring(state.lastIndexOf('|')+1);
		String pitchOffsetStr = note.substring(0, note.lastIndexOf('X'));
		String durationStr = note.substring(note.lastIndexOf('X')+1);
		
		int lastNote = compo.getKeySignatureAtPosition(position).getTonic();
		double duration = 0;
		try{
			int lastNoteInterval = Integer.parseInt(pitchOffsetStr);
			duration = Double.parseDouble(durationStr);
			double tripletCheck = duration / 0.0833333333330;
			int tripletC = (int) Math.round(tripletCheck);
			//System.out.println("tripletc: "+tripletC);
			if (inTriplet && remainingTriplet > 0) {
				//System.out.println("We are in a triplet: "+remainingTriplet+" more notes");
				duration = dur;
				//System.out.println("Set duration to: "+dur);
				remainingTriplet--;
				if (remainingTriplet == 0) {
					inTriplet = false;
					//System.out.println("We have exited the triplet");
				}
			}
			else if (tripletC == 1 || tripletC == 2 || tripletC == 4 || tripletC == 8) {
				double threeTriplet = 3 * duration;
				int fmThreeTriplet = FMComposition.getFMRhythmValue(threeTriplet);
				int fmMeasureRemainingDuration = compo.getRemainingMeasureDurationAtPosition(position);
				if (fmThreeTriplet >= fmMeasureRemainingDuration) {
					//System.out.println("Encountered a triplet note, but playing an eighth note, triplet will not fit");
					duration = .5;
				}
				else {
					//System.out.println("Entering triplet");
					inTriplet = true;
					remainingTriplet = 2;
					dur = duration;
				}
			}
			//System.out.println("Note duration: "+duration);
			//Rests require special handling, as they are a special value
			if(lastNoteInterval == Pitches.REST) {
				lastNote = Pitches.REST;
			} else {
				lastNote += lastNoteInterval;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.err.println("Markov chain format failure: Attempted to play from state " + state);
		}
		Note newNote = new Note(lastNote, duration);
		phrase.add(newNote);
		return newNote;
	}

	/**
	 * Selects the next note, low-weighting off-chord pitches
	 * @param markovMap The markov chain table
	 * @param lastNote The state we're going away from
	 * @param key The key to attempt to match
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String selectNote(Map<String, Map<String, Float>> markovMap, String lastNote, KeySignature key) {
		double r = rng.nextDouble();
		String retVal = null;
		Map<String, Float> map = markovMap.get(lastNote);
		if(map == null) {
			//If there's no data for this state
			System.err.println("Markov chain entered impossible state: " + lastNote);
			//Select random state to skip to
			Set<Entry<String, Map<String, Float>>> mEntries = markovMap.entrySet();
			while(retVal == null || retVal.contains("~")) {
				Object[] entryArr = mEntries.toArray();
				retVal = ((Entry<String, Map<String, Float>>)entryArr[rng.nextInt(mEntries.size())]).getKey();
			}
		} else {
			Set<Entry<String, Float>> adjustedEntries = adjustToPreferKey(map, key).entrySet();
			//Select random weighted
			for(Entry<String, Float> e : adjustedEntries) {
				if(r <= e.getValue()) {
					retVal = e.getKey();
					break;
				} else {
					r -= e.getValue();
				}
			}
		}
		return retVal;
	}

	/**
	 * Returns a copy of a Markov chain data row adjusted to prefer on-key pitches
	 * @param map Markov chain data row - maps states to priorities of going to them
	 * @param key The key to attempt to match
	 * @return A copy of the data row with values adjusted to prefer on-key pitches
	 */
	private Map<String, Float> adjustToPreferKey(Map<String, Float> map, KeySignature key) {
		Map<String, Float> ret = new HashMap<String, Float>();
		//For normalization
		float totalWeight = 0;
		//create map
		for(Entry<String, Float> e : map.entrySet()) {
			float r = e.getValue();
			int interval = Integer.parseInt(e.getKey().substring(e.getKey().lastIndexOf('|')+1, e.getKey().lastIndexOf('X')));
			if(interval != Pitches.REST && key.matchesKey(interval + key.getTonic()))
				r += CHORD_FITTING_WEIGHT;
			ret.put(e.getKey(), r);
			totalWeight += r;
		}
		//normalize
		for(Entry<String, Float> e : ret.entrySet()) {
			ret.replace(e.getKey(), e.getValue()/totalWeight);
		}
		
		return ret;
	}

	/**
	 * Reads every row of the markov chain data file into a usable data structure
	 * @param markovChainFileName Filename
	 * @return The map used in the packet's execution
	 */
	private Map<String, Map<String, Float>> loadMarkovChainData(String markovChainFileName) {
		Map<String, Map<String, Float>> markovTable = new HashMap<>();
		
		Scanner inputFile = new Scanner(getClass().getResourceAsStream(markovChainFileName));
		inputFile.nextLine();
		while(inputFile.hasNextLine()) {
			readIntoTable(markovTable, inputFile.nextLine());
		}
		inputFile.close();
		return markovTable;
	}

	/**
	 * Reads a row of the markov chain data file into the data structure
	 * @param markovTable The table to read into
	 * @param nextLine The line of the data file to read as a string 
	 */
	private void readIntoTable(Map<String, Map<String, Float>> markovTable, String nextLine) {
		Scanner lineReader = new Scanner(nextLine);
		lineReader.useDelimiter(", ");
		if(!lineReader.hasNext()) { lineReader.close(); return; }
		String fromState = lineReader.next();
		if(!lineReader.hasNext()) { lineReader.close(); return; }
		String toState = fromState.substring(fromState.indexOf('|')+1) + "|" + lineReader.next();
		if(!lineReader.hasNext()) { lineReader.close(); return; }
		Float  probability = lineReader.nextFloat();
		
		Map<String, Float> markovTableRow = markovTable.get(fromState);
		if(markovTableRow == null) {
			markovTableRow = new HashMap<String, Float>();
			markovTable.put(fromState, markovTableRow);
		}
		if(markovTableRow.containsKey(toState)) {
			System.err.println("warning: Markov Chain Data contains duplicate entries for " + fromState + " -> " + toState);
		}
		markovTableRow.put(toState, probability);
		lineReader.close();
	}

	/**
	 * This method should never be called - this is a melodic packet.
	 */
	@Override
	public Collection<Part> executeHarmony(FMComposition arg0, DefaultMutableTreeNode arg1) {
		throw new UnsupportedOperationException("Simple Markov Chains cannot play harmony");
	}

	/**
	 * This method should never be called - this is a melodic packet.
	 */
	@Override
	public Collection<Part> executeSupport(FMComposition arg0, DefaultMutableTreeNode arg1) {
		throw new UnsupportedOperationException("Simple Markov Chains cannot play support");
	}

}
