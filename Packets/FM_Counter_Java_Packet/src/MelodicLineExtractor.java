import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;

/**
 * Extracts a single melodic line from a JMusic Part - only one note exists at any start time
 * Does not account for overlapping notes.
 * @author Jake
 *
 */
@SuppressWarnings("serial") //we will not be serializing these differently
public class MelodicLineExtractor extends ArrayList<LocatedNote> {
    public MelodicLineExtractor(Collection<Part> parts, boolean smooth) {
        super();
        //add all notes to us
        for(Part part : parts) {
            extractNotesFromPart(part, smooth);
        }
        removeDuplicateNotes();
    }
    
    public MelodicLineExtractor(Part part, boolean smooth) {
        super();
        //add all notes to us
        extractNotesFromPart(part, smooth);
        removeDuplicateNotes();
    }

    private void extractNotesFromPart(Part part, boolean smooth) {
        for(Phrase phrase : part.getPhraseArray()) {
            Note[] notes = phrase.getNoteArray();
            for(int noteIdx = 0; noteIdx < notes.length; noteIdx++) {
            	int pitch = notes[noteIdx].getPitch();
            	if(smooth) {
            		int n = 1;
            		if(notes.length != noteIdx+1) {
            			pitch += notes[noteIdx+1].getPitch();
            			n++;
            		}
            		if(0 != noteIdx) {
            			pitch += notes[noteIdx-1].getPitch();
            			n++;
            		}
            		pitch /= n;
            	}
                add(new LocatedNote(pitch,
                                    notes[noteIdx].getRhythmValue(),
                                    //FIXME: Jmusic's docs are bad.  This may require fixes.
                                    phrase.getNoteStartTime(noteIdx)));
            }
        }
    }

    private void removeDuplicateNotes() {
        //remove excess notes
        Collections.sort(this);
        Random rng = new Random();
        //for all notes in us
        for(int i = 0; i < size(); i++) {
            double startTime = get(i).getTime();
            //make a list of notes with the same time
            ArrayList<LocatedNote> sameStartTimeNotes = new ArrayList<>();
            //while we're not off the end and the note at i has the same start time... 
            while(i < size() && get(i).getTime() == startTime) {
                //take it out and put it in a list
                sameStartTimeNotes.add(remove(i));
            }
            //pick a random note from that list
            LocatedNote pick = sameStartTimeNotes.get(rng.nextInt(sameStartTimeNotes.size()));
            //put that one back
            add(i, pick);
        }
    }
}
