package framework.packet;

import java.util.Collection;

import framework.ds.FMComposition;
import jm.music.data.Part;

/**
 * Interface to be used by Control Modules that are meant to be
 * compatible with FuseMuse's Shell.
 * 
 * @author Sam Rappl
 * @version 2.0
 */
public interface FMControlModule {
	
	/**
	 * Defines how the musical piece should be constructed
	 * and is allowed to revise existing parts.
	 * 
	 * @param composition a data structure containing all existing
	 *                    basslines, chord progressions,
	 *                    melodies, harmonies, and supporting
	 *                    parts.
	 *                    
	 * @param currentPart the most recently created part,
	 *                    so the Control Module has the ability
	 *                    to alter only the most recent
	 *                    contribution to the composition if
	 *                    it chooses.
	 */
	public void execute(FMComposition composition, Collection<Part> currentPart);

	/**
	 * A different version of execute that is called after
	 * all Packets have finished executing. This execute
	 * method should finalize the piece.
	 * 
	 * @param composition a data structure containing all existing
	 *                    basslines, chord progressions,
	 *                    melodies, harmonies, and supporting
	 *                    parts.
	 */
	public void execute(FMComposition composition);
	
}
