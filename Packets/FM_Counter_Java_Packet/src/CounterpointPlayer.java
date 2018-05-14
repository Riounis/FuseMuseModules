import java.util.ArrayList;
import java.util.Collection;

import javax.swing.tree.DefaultMutableTreeNode;

import framework.ds.FMComposition;
import framework.ds.FMCompositionSegment;
import framework.packet.FMPacket;
import framework.packet.PacketPart;
import jm.music.data.Part;
import jm.music.data.Phrase;

public class CounterpointPlayer implements FMPacket {

    private static final int FAILURE_THRESHOLD = 30;

    @Override
    public Collection<Part> executeMelody(FMComposition composition, DefaultMutableTreeNode currentExecutionNode) {
        throw new UnsupportedOperationException("Counterpoint Packet only supports Harmony mode of play");
    }

    @Override
    public Collection<Part> executeSupport(FMComposition composition, DefaultMutableTreeNode currentExecutionNode) {
        throw new UnsupportedOperationException("Counterpoint Packet only supports Harmony mode of play");
    }

    @Override
    public Collection<Part> executeHarmony(FMComposition composition, DefaultMutableTreeNode currentExecutionNode) {
        //Get the parent packet's part list
    	System.err.println(currentExecutionNode.getChildCount());
    	System.err.println(currentExecutionNode);
    	DefaultMutableTreeNode parent = (DefaultMutableTreeNode)currentExecutionNode.getParent();
    	System.err.println(parent);
    	System.err.println(parent.getUserObject());
    	PacketPart ppp = (PacketPart)(parent).getUserObject();
        Collection<Part> parentParts = (ppp).getPart();
        //Get a melodic line
        MelodicLineExtractor cantusFirmus = new MelodicLineExtractor(parentParts, false); //false: do not smooth parent, as doing so makes bad intervals happen often
        //Start at level 0
        int toleranceLevel = 0;
        //use the composition segment at 0 because I just don't care enough to put up with it
        FMCompositionSegment seg = composition.getCompositionSegmentAtPosition(0);
        //Start counterpoint string
        CounterpointNode root = new CounterpointNode(cantusFirmus, seg);
        CounterpointNode currentNode = root;
        int lineIndex = 0;
        int faultCounter = 0;
        double jmTime = 0;
        System.err.println("Cantus Firmus size: " + cantusFirmus.size());
        //Until we're done
        while(lineIndex < cantusFirmus.size()) {
            System.err.println("index: " + lineIndex + ", time: " + jmTime);
            //Progressv
            boolean success;
            if (lineIndex == cantusFirmus.size() - 1) {
                success = currentNode.selectTerminalNote(toleranceLevel);
                System.err.println("Expanding (Terminal): " + lineIndex);
            } else {
                success = currentNode.expand(cantusFirmus, lineIndex, toleranceLevel, seg);
                System.err.println("Expanding (General):" + lineIndex);
            }
            
            if(success) {
                currentNode = currentNode.next;
                jmTime += cantusFirmus.get(lineIndex).getTime();
                lineIndex++;
            //If a failure occurs
            } else {
                //Increment fault counter
                faultCounter++;
                //If we have failed FAILURE_THRESHOLD times
                if(faultCounter >= FAILURE_THRESHOLD) {
                    //Increase tolerance level (Will always succeed: Tolerance 4 will always have notes available
                    toleranceLevel++;
                    faultCounter = 0;
                }
                //Back up one note
                if(lineIndex > 0) {
                    currentNode = currentNode.prev;
                    jmTime -= cantusFirmus.get(lineIndex).getTime();
                    lineIndex--;
                }
            }
        }
        Phrase phrase = new Phrase();
        Part part = new Part(phrase, "Counterpoint");
        phrase.setInstrument(110);
        Collection<Part> parts = new ArrayList<>(1);
        parts.add(part);
        currentNode = root;
        while(currentNode != null) {
            phrase.add(currentNode.getNote());
            currentNode = currentNode.next;
        }
        System.err.println("Cantus Firmus Size: " + cantusFirmus.size());
        System.err.println("Part size: " + phrase.size());
        return parts;
    }
}
