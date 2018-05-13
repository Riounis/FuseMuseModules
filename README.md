# FuseMuseModules
This repository contains source code and metadata for Packets, Driver Modules, and Control Modules for FuseMuse.

The FuseMuse procedural music generation engine is located at https://github.com/sigmaJ/FuseMuse

The FuseMuse music library is located at https://github.com/Riounis/libfm

### Modules

Packets, Driver Modules, and Control Modules (hereafter referred to as "modules") are shipped as .zip or .jar archives containing the necessary componenets for execution. Modules written in Python or composed of binaries are shipped in .zip files, and are extracted to a temporary directory before execution, while modules written in Java are executed as they are. This is because Java has a mechanism for running and accessing files within a jar, while Python and other binaries lack this capability.

Modules communicate using JSON data. The details of this communication are outlined in the sections "Packets", "Driver Modules", and "Control Modules" below.

Additionally, modules are required to contain some metadata for use by the FuseMuse application.  The details of this metadata are outlined in the sections “Packets”, “Driver Modules”, and “Control Modules” below.

We recommend use of the Music21 library in python or libfm in C++. Modules can be written in any language or using any library so long as they return the appropriate data.

#### Packets

Packets are passed several parameters, all via standard input, to guide their execution. Firstly, Packets receive the mode they will use. This mode will be a string with a value of “melodic”, “harmonic”, or “supporting”. Your Packet does not have to be capable of producing all three of these types of parts, and these capabilities should be reflected in the packet’s metadata.  Following this, the packet will receive via standard input a JSON string representing the entire composition thus far.  The structure of this JSON is outlined in the section “JSON” below. 

Packets should return a single JSON Part, which is a list of musical events.  See the JSON specification below for the meaning of a JSON Part.

##### Metadata

The metadata file necessary for Packets should have the same flags as the following metadata block.

```
can_play_melody: (true or false)
can_play_harmony: (true or false)
can_play_support: (true or false)
packet_name: (the name of the packet)
packet_description: (describe what instrument your packet represents, what style it emulates, what methods of music generation it uses, and all other relevant information for the user to know about it)
instrument: (general midi instrument number constant value)
exec_type: (.exe, .python, .java)
windows_bin: (only exists if exec_type is .exe, name of windows executable in zipfile)
linux_bin: (only exists if exec_type is .exe, name of windows executable in zipfile)
osx_bin: (only exists if exec_type is .exe, name of windows executable in zipfile)
```

#### Driver Modules

Driver modules are the starting point of the FuseMuse execution flow, and as such receive no input data.  The Driver Module should output a Composition with no Parts.

##### Metadata

The metadata file necessary for Driver Modules should have the same flags as the following metadata block.

```
driver_name: (the name of the driver module)
driver_description: (describe what musical style your driver module emulates, what structure of music it produces, what methods of music generation it uses, and all other relevant information for the user to know about it)
exec_type: (one of exe, python, or java.  Tells FuseMuse how to execute the driver module)
windows_bin: (only exists if exec_type is .exe, name of windows executable in zipfile)
linux_bin: (only exists if exec_type is .exe, name of windows executable in zipfile)
osx_bin: (only exists if exec_type is .exe, name of windows executable in zipfile)
```

#### Control Modules

A control module exacts complete control of the piece, and runs both after each Packet, and after the whole composition is complete.  In order to allow each mode to operate differently, the control module first receives either “control” or “finalcontrol”, followed by a JSON Composition.  The control module should make any changes it sees fit to make before printing the modified JSON Composition to standard output.

##### Metadata

The metadata file necessary for Control Modules should have the same flags as the following metadata block.

```
control_name: (the name of the control module)
control_description: (describe what musical style your control module emulates, what boundaries it imposes, what methods of music generation it uses, and all other relevant information for the user to know about it)
exec_type: (one of exe, python, or java.  Tells FuseMuse how to execute the control module)
windows_bin: (only exists if exec_type is .exe, name of windows executable in zipfile)
linux_bin: (only exists if exec_type is .exe, name of windows executable in zipfile)
osx_bin: (only exists if exec_type is .exe, name of windows executable in zipfile)
```

### JSON

For descriptions of the fields in the JSON below, please see the libfm source code (located at https://github.com/Riounis/libfm) for java style documentation.

#### Note

```
{
	pitch: 			        number,
	duration:		        number,
	triplet: 			      boolean (optional),
	dotted: 		        boolean (optional),
	double_dotted: 	    boolean (optional),
	staccato:  		      boolean (optional),
	tenuto:  		        boolean (optional),
	accent:  		        boolean (optional),
	fermata:  		      boolean (optional),
	tied: 			        boolean (optional),
	slurred:  		      boolean (optional),
	type: 			        String (value should always be “note”)
}
```

#### Chord

```
{
	pitches: 		        [number],
	duration:		        number,
	triplet: 			      boolean (optional),
	dotted: 		        boolean (optional),
	double_dotted: 	    boolean (optional),
	staccato:  		      boolean (optional),
	tenuto:  		        boolean (optional),
	accent:  		        boolean (optional),
	fermata:  		      boolean (optional),
	tied: 			        boolean (optional),
	slurred:  		      boolean (optional),
	type: 			        String (value should always be “chord”)
}
```

#### Dynamics

```
{
	volume: 		        number,
	cresc: 			        boolean (optional),
	decresc: 		        boolean (optional),
	type: 			        String (value should always be “dynamics”)
}

```

#### Key

```
{
	tonic: 			        number,
	intervals: 		      [number]
}
```

#### Composition Metrics

```
{
	num: 			          number,
	denom:	 	          number,
	tempo: 		          number,
	key: 			          Key,
	position: 		      number
}
```

#### Part

```
{
	name: 			        String,
	events: 		        [{Event}] (Events are Notes, Chords, and Dynamics),
	length: 		        number
}
```

#### Pattern Segment

```
{
	name: 			        String,
	duration: 		      number,
	chord_progression: 	Part
}
```

#### Packet Part

```
{
	children: 		      [PacketPart],
	part: 			        Part,
	packet_path: 		    String,
	mode: 			        String,
	executed: 		      boolean,
	is_active: 		      boolean
}
```

#### Composition

```
{
	metrics: 		        [CompositionMetrics],
	parts: 			        [Part],
	pattern_segments: 	[PatternSegment],
	pattern: 		        [String],
	packet_tree_root: 	PacketPart
}
```

### Getting the Most out of the FuseMuse Architecture

FuseMuse provides a unique architecture for developers to use. In addition to a simple and functional music library, we also offer tools for algorithmic composers to structure their music more rigidly. FuseMuse allows composers to mimic specific styles of music by encouraging pattern-based music generation. The PatternSegment class is at the forefront of this innovation. At its core, the PatternSegment simply stores a chord progression; however, the Composition class also contains a pattern field, which is a list of names of PatternSegments. Additonally, FuseMuse allows CompositionMetric changes at any point in time during the composition. 
The PatternSegment also stores its own length. This allows modules to align PatternSegment changes with CompositionMetric changes at the same time in the music for maximum cohesiveness. An example of a good use of this architecture is a classical piece written in movements. The first movement is fast and has a high tempo. It is in a major key. The second movement is slow and is minor, sounding morose. The PatternSegment facilitates the generation of sectionalized pieces of music like this (or any music with a verse-chorus, call & response, or other pattern-based structure) by allowing the Driver Module to deliver information on the timing of music feel changes to the Packets.

Of course, not all music needs this much structure, and you are free to entirely ignore the PatternSegment. Simply construct a single PatternSegment and register it with the Composition within the Driver Module. Add your chord progression to this PatternSegment and add its name to the pattern in the Compositon once.

Code for example modules can be found in the Packets, Driver Modules, or Control Modules directories in this repository
