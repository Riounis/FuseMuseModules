package framework.packet;

import java.util.ArrayList;
import java.util.Collection;


import jm.music.data.Part;

/**
 * A container for FuseMuse Packets that allows the Shell to
 * order them in a hierarchy. The PacketPart container also
 * provides a mechanism for the FuseMuse interface to display
 * to users whether each Packet is capable of generating a
 * melody, a harmony, or a supporting part.
 * 
 * @author Sam Rappl
 * @version 2.0
 */
public class PacketPart {

	/**
	 * Defines whether a Packet will fill the role of Melody,
	 * Harmony, or Support.
	 */
	public enum Packet_Enum {
		Melody, Harmony, Support
	}
	
	/**
	 * Contains data on the chosen mode of the Packet.
	 */
	private Packet_Enum mode;
	
	/**
	 * Contains the part that is generated using the wrapped
	 * Packet.
	 */
	private Collection<Part> track;
	
	/**
	 * Represents the instrument that the part should be played on
	 * using JMusic's instrument constants
	 */
	private int instrument;
	
	/**
	 * Constructs a PacketPart object to hold the given Packet
	 * with the specified mode.
	 * 
	 * @param pi packet to be wrapped by the PacketPart class.
	 * 
	 * @param m mode of the given Packet.
	 */
	public PacketPart(Packet_Enum m){
		mode = m;
		track = new ArrayList<Part>();
		setInstrument(-1);
	}
	
	/**
	 * Returns the parts generated by the Packet.
	 * 
	 * @return Parts generated by the Packet.
	 */
	public Collection<Part> getPart(){
		return track;
	}
	
	/**
	 * Returns the mode chosen for the Packet.
	 * 
	 * @return Packet_Enum mode
	 */
	public Packet_Enum getMode(){
		return mode;
	}
	
	/**
	 * Returns the JMusic instrument constant for the track
	 * @return the JMusic instrument constant for the track
	 */
	public int getInstrument() {
		return instrument;
	}

	/**
	 * Sets the instrument
	 * @param instrument: A JMusic Insrument constant
	 */
	public void setInstrument(int instrument) {
		this.instrument = instrument;
	}
}
