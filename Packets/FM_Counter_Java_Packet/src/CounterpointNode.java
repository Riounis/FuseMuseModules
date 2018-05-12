import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import framework.ds.FMCompositionSegment;
import framework.ds.KeySignature;

public class CounterpointNode {
    public CounterpointNode next;
    public CounterpointNode prev;
    
    AcceptanceLevel[] leveledOptions = new AcceptanceLevel[5];
    private LocatedNote selection;
    
    public CounterpointNode(ArrayList<LocatedNote> cantusFirmus, FMCompositionSegment compositionSegment) {
        initLists();
        //Set up lists for first note
        HashMap<LocatedNote, Integer> levels = createLevelMap(cantusFirmus, 0);
        adjustNotes(cantusFirmus, 0, levels, compositionSegment, this);
        for(LocatedNote n : levels.keySet()) {
            leveledOptions[Math.min(levels.get(n), leveledOptions.length - 1)].add(n);
        }
        
    }
    
    private CounterpointNode() {
        //Default constructor - does no setup, which is done in the expand methods
        initLists();
    }
    
    private void initLists() {
        for(int i = 0; i < leveledOptions.length; i++) {
            leveledOptions[i] = new AcceptanceLevel();
        }
    }
    
    /**
     * 
     * @param cantusFirmus
     * @param index: The index of the current note in the cantus firmus
     * @param tolerance
     * @param compositionSegment: The current composition segment
     * @return
     */
    public boolean expand(ArrayList<LocatedNote> cantusFirmus, int index, int tolerance, FMCompositionSegment compositionSegment) {
        //Pick a note to play
    	if(!selectNote(tolerance)) {
            return false;
        }
    	//Create and link a new, empty node
    	linkNewNote();
    	//set up lists for it
    	HashMap<LocatedNote, Integer> levels = createLevelMap(cantusFirmus, index+1);
    	adjustNotes(cantusFirmus, index+1, levels, compositionSegment, next);
    	for(LocatedNote n : levels.keySet()) {
            next.leveledOptions[Math.min(levels.get(n), leveledOptions.length - 1)].add(n);
        }
    	return true;
    }
    
    private void adjustNotes(ArrayList<LocatedNote> cantusFirmus, int index, HashMap<LocatedNote, Integer> levels,
            FMCompositionSegment compositionSegment, CounterpointNode nextNode) {
        for(LocatedNote n : levels.keySet()) {
            levels.put(n, nextNode.getNoteProblemValue(cantusFirmus, index, compositionSegment, n));
        }
        
    }

    private int getNoteProblemValue(ArrayList<LocatedNote> cantusFirmus, int index,
            FMCompositionSegment compositionSegment, LocatedNote n) {
    	int problems = 0;
        
        /* ============================== *
         * Forbiddance rules: Problems +3 *
         * ============================== */
        
        //Tonic must be approached by step
        if(index != 0) {
            if(compositionSegment.getKeySignature().getTonic() == n.getPitch()) {
                if(Math.abs(prev.selection.getPitch() - n.getPitch()) > 2) {
                    problems += 3;
                }
            }
        }
        
        //Permitted melodic intervals: perfect 4, 5, octave, major and minor 2 and 3, ascending minor 6th
        //In semitones: 1 2 3 4 5 7 8^ 12
        if(index != 0) {
            int melodicInterval = n.getPitch() - prev.selection.getPitch();
            switch(melodicInterval) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 7:
            case 12:
            case -1:
            case -2:
            case -3:
            case -4:
            case -5:
            case -7:
            case -12:
            case 8:
                break; //permitted
            default:
                problems += 3;
            }
        }
        
        //do not ascend after ascending minor 6th
        if(index >= 2) {
            int prevMelodicInterval = prev.selection.getPitch() - prev.prev.selection.getPitch();
            int melodicInterval = n.getPitch() - prev.selection.getPitch();
            if(prevMelodicInterval == 8 && melodicInterval > 0) {
                problems += 3;
            }
        }
        
      //after skip, do not skip larger or same size
        if(index >= 2) {
            int prevMelodicInterval = Math.abs(prev.selection.getPitch() - prev.prev.selection.getPitch());
            int melodicInterval = Math.abs(n.getPitch() - prev.selection.getPitch());
            if(prevMelodicInterval > 2 && melodicInterval >= prevMelodicInterval) {
                problems += 3;
            }
        }
      
        //2 similar skips forming dissonance across them
        if(index >= 2) {
            int prevMelodicInterval = Math.abs(prev.selection.getPitch() - prev.prev.selection.getPitch());
            int melodicInterval = Math.abs(n.getPitch() - prev.selection.getPitch());
            if(prevMelodicInterval > 2 && melodicInterval > 2) {
                int twoSkipInterval = prevMelodicInterval + melodicInterval;
                switch(twoSkipInterval) {
                case 7:
                case 8:
                case 9:
                case 12:
                    break; //consonant
                default:
                    problems += 3;
                }
            }
        }
        
        //Disabled due to the way we're choosing notes - this will too-often force tolerance immediately to 3
        /*
        //Forbidden: Begin or end on anything but 7, 12, 0 with cantus firmus.  No 7 if below.
        if(index == 0 || index == cantusFirmus.size() - 1) {
            int pitchInterval = n.getPitch() - cantusFirmus.get(index).getPitch();
            switch (pitchInterval) {
            case 0:
            case 7:
            case 12:
            case -12:
                break; // permitted
            default:
                problems += 3;
            }
        }
        */
        
        //Forbidden: 7, 12 in same direction as previous motion
        if(index >= 2) {
            int prevMelodicInterval = prev.selection.getPitch() - prev.prev.selection.getPitch();
            int melodicInterval = n.getPitch() - prev.selection.getPitch();
            if(prevMelodicInterval * melodicInterval > 0) { //same sign 
                switch (melodicInterval) {
                case 7:
                case 12:
                case -7:
                case -12:
                    problems += 3;
                }
            }
        }
        

        /* ============================ *
         * Avoidance rules: Problems +1 *
         * ============================ */
        
        //Avoid 2 skips in same direction
        //Avoid skip followed by similar motion
        if(index >= 2) {
            int prevMelodicInterval = prev.selection.getPitch() - prev.prev.selection.getPitch();
            int melodicInterval = n.getPitch() - prev.selection.getPitch();
            if(prevMelodicInterval * melodicInterval > 0) { //same sign
                if(prevMelodicInterval > 2 || prevMelodicInterval < -2) { //prior was skip
                    problems += 1;
                }
            }
        }
        
        //Avoid 2 skips forming a non-triad
        if(index >= 2) {
            ArrayList<Integer> pitches = new ArrayList<>(3);
            pitches.add(prev.prev.selection.getPitch());
            pitches.add(prev.selection.getPitch());
            pitches.add(n.getPitch());
            Collections.sort(pitches);
            int ival1 = pitches.get(1) - pitches.get(0);
            int ival2 = pitches.get(2) - pitches.get(1);
            if((ival1 != 3 && ival1 != 4) || (ival2 != 3 && ival2 != 4)) {
                problems += 1;
            }
        }
        
        //Avoid 2 skips spanning over an octave
        if(index >= 2) {
            int twoSkipMelodicInterval = Math.abs(n.getPitch() - prev.prev.selection.getPitch());
            if(twoSkipMelodicInterval > 12) {
                problems += 1;
            }
        }
        
        //Avoid similar motion where sum is 6 in 3 notes
        if(index >= 2) {
            int prevMelodicInterval = prev.selection.getPitch() - prev.prev.selection.getPitch();
            int melodicInterval = n.getPitch() - prev.selection.getPitch();
            if(prevMelodicInterval * melodicInterval > 0) { //same sign
                switch(melodicInterval + prevMelodicInterval) {
                case 6:
                case -6:
                    problems += 1;
                }
            }
        }

        //Avoid similar motion where sum is 10, 11
        if(index >= 2) {
            //Start at current note
            //Check direction and interval for last 3
            int prevMelodicInterval = prev.selection.getPitch() - prev.prev.selection.getPitch();
            int melodicInterval = n.getPitch() - prev.selection.getPitch();
            //If all same and total < 10:
            if(melodicInterval * prevMelodicInterval > 0 && melodicInterval + prevMelodicInterval < 10) {
                int intervalSum = melodicInterval + prevMelodicInterval;
                //Until we hit the start
                CounterpointNode cur = prev;
                while(cur.prev.prev != null) {
                    melodicInterval = cur.selection.getPitch() - cur.prev.selection.getPitch();
                    prevMelodicInterval = cur.prev.selection.getPitch() - cur.prev.prev.selection.getPitch();
                    //Add to total
                    intervalSum += prevMelodicInterval;
                    cur = cur.prev;
                    //If direction differs, break
                    //If total is >12, break
                    if(melodicInterval * prevMelodicInterval > 0 || intervalSum > 11) {
                        break;
                    }
                    //If total is 10-11, problem
                    if(intervalSum == 10 || intervalSum == 11 || intervalSum == -10 || intervalSum == -11) {
                        problems += 1;
                        break;
                    }
                }
            }
        }
        
        //Avoid similar motion
        if(index >= 2) {
            int prevMelodicInterval = prev.selection.getPitch() - prev.prev.selection.getPitch();
            int melodicInterval = n.getPitch() - prev.selection.getPitch();
            if(melodicInterval * prevMelodicInterval > 0) {
                problems += 1;
            }
        }

        //Avoid interval > 16 between parts 
        {
            int interval = Math.abs(n.getPitch() - cantusFirmus.get(index).getPitch());
            if(interval > 16) {
                problems += 1;
            }
        }
        
        //Avoid crossing over the melody track
        if(index != 0) {
            CounterpointNode root = prev;
            while(root.prev != null) root = root.prev;
            int initialInterval = Math.abs(root.selection.getPitch() - cantusFirmus.get(0).getPitch());
            int interval = Math.abs(n.getPitch() - cantusFirmus.get(index).getPitch());
            if(initialInterval * interval < 0) { //different sign
                problems += 1;
            }
        }
        
        //Avoid parallel 5, 7, 12 (fourth, fifth, octave)
        if(index != 0) {
            int melodicInterval = n.getPitch() - prev.selection.getPitch();
            int prevInterval = Math.abs(prev.selection.getPitch() - cantusFirmus.get(index - 1).getPitch());
            int interval = Math.abs(n.getPitch() - cantusFirmus.get(index).getPitch());
            if(melodicInterval == 5 || melodicInterval == 7 || melodicInterval == 12) {
                if(prevInterval == interval) {
                    problems += 1;
                }
            }
        }
        
        //Avoid repeated parallel 3,4,8,9 (major and minor 3rd, 6th)
        if(index >= 2) {
            int melodicInterval = Math.abs(n.getPitch() - prev.selection.getPitch());
            int prevMelodicInterval = Math.abs(prev.selection.getPitch() - prev.prev.selection.getPitch());
            int prevPrevInterval = Math.abs(prev.prev.selection.getPitch() - cantusFirmus.get(index - 2).getPitch());
            int prevInterval = Math.abs(prev.selection.getPitch() - cantusFirmus.get(index - 1).getPitch());
            int interval = Math.abs(n.getPitch() - cantusFirmus.get(index).getPitch());
            if(interval == prevInterval && prevInterval == prevPrevInterval) {
                if(melodicInterval == prevMelodicInterval) {
                    switch(melodicInterval) {
                    case 3:
                    case 4:
                    case 8:
                    case 9:
                        problems += 1;
                    }
                }
            }
        }
        
        //Avoid 2 parts making similar skips
        if(index != 0) {
            int melodicInterval = n.getPitch() - prev.selection.getPitch();
            int cantusMelodicInterval = cantusFirmus.get(index).getPitch() - cantusFirmus.get(index - 1).getPitch();
            if(melodicInterval > 2 && cantusMelodicInterval > 2 && melodicInterval*cantusMelodicInterval > 0) {
                problems += 1;
            }
        }
        
        //Avoid dissonant interval between parts (second, seventh, fourth)
        {
            int interval = Math.abs(n.getPitch() - cantusFirmus.get(index).getPitch()) % 12;
            int[] nondissonances = new int[4];
            KeySignature k = compositionSegment.getKeySignature();
            //unison
        	nondissonances[0] = 0;
            //third
            if(k.getScaleSize() >= 3)
            	nondissonances[1] = k.getNthPitch(2) - k.getNthPitch(0);
            //fifth
            if(k.getScaleSize() >= 5)
            	nondissonances[2] = k.getNthPitch(4) - k.getNthPitch(0);
            //sixth
            if(k.getScaleSize() >= 6)
            	nondissonances[3] = k.getNthPitch(5) - k.getNthPitch(0);
            
            if(!contains(nondissonances, interval))
        		problems++;
        }
        
        //Prefer to stick to the key
        if(! compositionSegment.getKeySignature().matchesKey(n.getPitch())) {
        	problems += 1;
        }
        
        return problems;
    }

    private HashMap<LocatedNote, Integer> createLevelMap(ArrayList<LocatedNote> cantusFirmus, int cfindex) {
        ArrayList<LocatedNote> notes = new ArrayList<>(cantusFirmus);
        notes.sort(new Comparator<LocatedNote>() {

            @Override
            public int compare(LocatedNote o1, LocatedNote o2) {
                return new Integer(o1.getPitch()).compareTo(new Integer(o2.getPitch()));
            }
        });
        
        int thirdPitchQuartile = notes.get(notes.size() * 3 / 4).getPitch();
        
        HashMap<LocatedNote, Integer> levelMap = new HashMap<>(24);
        
        for(int i = 0; i < 24; i++) {
            levelMap.put(new LocatedNote(thirdPitchQuartile+i, cantusFirmus.get(cfindex).getRhythmValue(), cantusFirmus.get(cfindex).getTime()), 0);
        }
        
        return levelMap;
    }

    public boolean selectTerminalNote(int tolerance) {
        return selectNote(tolerance);
    }
    
    //links this note to a new one
    private void linkNewNote() {
        this.next = new CounterpointNode();
        next.prev = this;
    }
    
    private boolean selectNote(int tolerance) {
        System.err.println("Selecting Note with t=" + tolerance + ", len0=" + leveledOptions[0].size() + ", len1=" + leveledOptions[1].size() + ", len2=" + leveledOptions[2].size() + ", len3=" + leveledOptions[3].size() + ", len4=" + leveledOptions[4].size() + " as " + this.toString());
        //Pick a note to play from minimum acceptance level, up to tolerance
        for(int i = 0; i <= tolerance; i++) {
            if(leveledOptions[i].isEmpty()) {
                continue;
            }
            Random rng = new Random();
            int index = rng.nextInt(leveledOptions[i].size());
            //move selected note to the next level
            selection = leveledOptions[i].remove(index);
            leveledOptions[Math.min(i+1, leveledOptions.length-1)].add(selection);
            return true;
        }
        // failed to find
        return false;
    }
    
    public LocatedNote getNote() { return selection; }
    
    private static boolean contains(int[] arr, int targ) {
    	for(int i = 0; i < arr.length; i++) {
    		if(arr[i] == targ) return true;
    	}
    	return false;
    }
}
