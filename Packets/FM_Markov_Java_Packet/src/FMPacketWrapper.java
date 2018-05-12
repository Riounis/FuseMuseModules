import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.tree.DefaultMutableTreeNode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import framework.ds.DiatonicKeySignature;
import framework.ds.FMComposition;
import framework.ds.FMCompositionSegment;
import framework.ds.FMMeasure;
import framework.ds.FMNote;
import framework.packet.FMPacket;
import framework.packet.PacketPart;
import framework.packet.PacketPart.Packet_Enum;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jsoneq.JSONNote;
import jsoneq.JSONPart;

public class FMPacketWrapper {
	private static DefaultMutableTreeNode activeElement;

	public static void main(String[] args) {

		String json = "";
		String packetMode = "";
		int v = 0;
		while(v != -1) {
			try {
				v = System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(v > 31) {
				json += ("" + (char)v);
			}
		}

		packetMode = json.substring(0, json.indexOf(' ')).trim();
		json = json.substring(json.indexOf(' ')).trim();
		
		System.err.println(packetMode);
		System.err.println("\n\n\n");
		System.err.println(json);
		
		Properties packetProperties = loadProps();
		
		Class<?> c;
		FMPacket packet = null;
		try {
			c = Class.forName(packetProperties.getProperty("FMPacket_class_name"));
			//System.out.println(packetProperties.getProperty("FMPacket_class_name"));
			packet = (FMPacket) c.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		Collection<Part> result = null;
		
		JsonParser parser = new JsonParser();
        JsonObject jsonRoot = parser.parse(json).getAsJsonObject();
        		
		FMComposition composition = deserializeComposition(jsonRoot);
		
		DefaultMutableTreeNode currentExecutionNode = deserializeTree(jsonRoot.get("packetTreeRoot"), composition);

		switch(packetMode) {
		case "melodic":
			result = packet.executeMelody(composition, activeElement);
			break;
		case "harmonic":
			result = packet.executeHarmony(composition, activeElement);
			break;
		case "supporting":
			result = packet.executeSupport(composition, activeElement);
			break;
		default:
		}
		
		String newParts = serializeParts(result, packetProperties);
		
		System.out.println(newParts);
	}

	private static String serializeParts(Collection<Part> result, Properties packetProperties) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        JSONPart pp = new JSONPart();
        for(Part p : result) {
            pp.name = "Part from " + packetProperties.getProperty("FMPacket_class_name");
            for(Object phrase : p.getPhraseList()) {
                for(Object n : ((Phrase)phrase).getNoteList()) {
                    JSONNote e = new JSONNote();
                    e.pitch = ((Note)n).getPitch();
                    e.duration = FMComposition.getFMRhythmValue(((Note)n).getRhythmValue());
                    pp.events.add(e);
                }
            }
        }
        return gson.toJson(pp);
	}

	private static DefaultMutableTreeNode deserializeTree(JsonElement jsonElement, FMComposition composition) {
		if(jsonElement == null) {
			return null;
		}
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		PacketPart.Packet_Enum mode = Packet_Enum.Melody;
        if(jsonElement.getAsJsonObject().get("mode").getAsString().equalsIgnoreCase("harmony")) {
            mode = Packet_Enum.Harmony;
        }
        if(jsonElement.getAsJsonObject().get("mode").getAsString().equalsIgnoreCase("support")) {
            mode = Packet_Enum.Support;
        }
		PacketPart pp = new PacketPart(mode);
		root.setUserObject(pp);
		//p.setInstrument(jsonElement.getAsJsonObject().get("instrumnet").getAsInt());
		JsonObject partJson = jsonElement.getAsJsonObject().get("part").getAsJsonObject();
        Part p = new Part();
        pp.getPart().add(p);
        JsonArray notesJson = partJson.getAsJsonObject().get("events").getAsJsonArray();
        for(JsonElement e : notesJson) {
            int pitch = 0;
            if(e.getAsJsonObject().has("pitch")) {
                pitch = e.getAsJsonObject().get("pitch").getAsInt();
            }
            if(e.getAsJsonObject().has("pitches")) {
                //We don't support chords in parts because JMusic supports them like wet tissue paper supports a brick
                pitch = e.getAsJsonObject().get("pitches").getAsJsonArray().get(0).getAsInt();
            }
            double dur = e.getAsJsonObject().get("duration").getAsDouble() / 96.0;
            Phrase ppp = new Phrase();
            ppp.add(new Note(pitch, dur));
            p.add(ppp);
        }
		
		for(JsonElement e : jsonElement.getAsJsonObject().get("children").getAsJsonArray()) {
			DefaultMutableTreeNode newval = deserializeTree(e, composition);
            root.add(newval);
        }
        if(jsonElement.getAsJsonObject().get("isActive").getAsBoolean()) {
			FMPacketWrapper.activeElement = root;
        }
        return root;
        
	}
	

    private static FMComposition deserializeComposition(JsonElement compJsonElement) {
        
        //
        // BASIC BULLSHIT
        //
		JsonObject met = compJsonElement.getAsJsonObject().get("metrics").getAsJsonArray().get(0).getAsJsonObject();
        int timeSigNum = met.get("timeSignature").getAsJsonObject().get("num").getAsInt();
        int timeSigDenom = met.get("timeSignature").getAsJsonObject().get("denom").getAsInt();
        int tempo = met.get("tempo").getAsInt();
        int tonic = met.get("key").getAsJsonObject().get("tonic").getAsInt();
        JsonArray intervalsJSON = met.get("key").getAsJsonObject().get("intervals").getAsJsonArray();
        ArrayList<Integer> intervalsAL = new ArrayList<>();
        for(JsonElement e : intervalsJSON) {
            intervalsAL.add(e.getAsInt());
        }
        FMComposition compo = new FMComposition();
        compo.getScore().setNumerator(timeSigNum);
        compo.getScore().setDenominator(timeSigDenom);
        compo.getScore().setTempo(tempo);
        compo.getScore().setKeyQuality(intervalsAL.get(1) == 1 ? 1 : 0); //1 for minor, 0 major.  Cheap heuristic
        
        //
        // SEGMENT CONTENT
        //
        JsonArray patternSegmentsJson = compJsonElement.getAsJsonObject().getAsJsonArray("patternSegments");
        for(JsonElement e : patternSegmentsJson) {
            System.err.println("A");
            FMCompositionSegment newSeg = new FMCompositionSegment(new DiatonicKeySignature(0, compo.getScore().getKeyQuality()), (int)compo.getScore().getTempo());
            
            String name = e.getAsJsonObject().get("name").getAsString();
            JsonArray notesJson = e.getAsJsonObject().get("chordProgression").getAsJsonObject().get("events").getAsJsonArray();
            
            FMMeasure newMeasure = new FMMeasure(compo.getScore().getNumerator(), compo.getScore().getDenominator());
            int pos = 0;
            
            for(JsonElement nn : notesJson) {
                System.err.println("B");
                JsonArray pitchesJson = nn.getAsJsonObject().get("pitches").getAsJsonArray();
                int duration = nn.getAsJsonObject().get("duration").getAsInt();
                for(JsonElement d : pitchesJson) {
                    System.err.println("C");
                    FMNote n = new FMNote(d.getAsInt(), duration);
                    newMeasure.addNote(n, pos);
                }
                pos += duration;
                System.err.println("D");
                System.err.println(pos);
                System.err.println(newMeasure.getMeasureLength());
                if(pos == newMeasure.getMeasureLength()) {
                    System.err.println("E");
                    pos = 0;
                    newSeg.addMeasure(newMeasure);
                    newMeasure = new FMMeasure(compo.getScore().getNumerator(), compo.getScore().getDenominator());
                }
            }
            compo.registerCompositionSegment(name, newSeg);
            System.err.println("F");
        }
        
        //
        // SEGMENTS ORDER
        //
        JsonArray compositionSegmentNamesJson = compJsonElement.getAsJsonObject().get("pattern").getAsJsonArray();
        for(JsonElement e : compositionSegmentNamesJson) {
            compo.addCompositionSegment(e.getAsString());
            System.err.println("G");
        }
        
        //PARTS
        JsonArray partsJson = compJsonElement.getAsJsonObject().get("parts").getAsJsonArray();
        for(JsonElement partJson : partsJson) {
	        Part p = new Part();
	        compo.getScore().addPart(p);
	        JsonArray notesJson = partJson.getAsJsonObject().get("events").getAsJsonArray();
	        for(JsonElement e : notesJson) {
	            int pitch = 0;
	            if(e.getAsJsonObject().has("pitch")) {
	                pitch = e.getAsJsonObject().get("pitch").getAsInt();
	            }
	            if(e.getAsJsonObject().has("pitches")) {
	                //We don't support chords in parts because JMusic supports them like wet tissue paper supports a brick
	                pitch = e.getAsJsonObject().get("pitches").getAsJsonArray().get(0).getAsInt();
	            }
	            double dur = e.getAsJsonObject().get("duration").getAsDouble() / 96.0;
	            Phrase ppp = new Phrase();
	            ppp.add(new Note(pitch, dur));
	            p.add(ppp);
	        }
        }
        return compo;
	}

	private static Properties loadProps() {
		InputStream in = FMPacketWrapper.class.getResourceAsStream("/packetdata"); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		Properties packetProperties = new Properties();
		try {
			packetProperties.load(reader);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return packetProperties;
	}
}
