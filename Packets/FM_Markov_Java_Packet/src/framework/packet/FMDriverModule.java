package framework.packet;

import framework.ds.FMComposition;

/**
 * Interface to be used by Driver Modules that are meant to be
 * compatible with FuseMuse's Shell.
 * 
 * @author Sam Rappl
 * @version 2.0
 */
public interface FMDriverModule {
	
	/**
	 * Generates a rhythmic baseline, a chord 
	 * progression, a key, a time signature,
	 * and a tempo, and the composition length 
	 * of the piece for the Shell.
	 * 
	 * @return {@link FMComposition} a data structure containing the aforementioned data 
	 * 
	 */
	public FMComposition execute();
	
}
