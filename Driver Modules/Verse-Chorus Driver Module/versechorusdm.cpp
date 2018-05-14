#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <nlohmann/json.hpp>
#include "../../libfm/composition.h"
#include "../../libfm/utilities.h"

int main() {
	Composition comp;
	srand(time(0));
	// Some notes:
	// Most songs don't switch scales even if they switch keys
	// Most songs don't switch tempos
	// Most songs don't switch time signatures, and if they do,
	// 		it's usually as part of a phrase rather than part of a
	// 		composition segment change.
	// When songs switch keys, they usually go up a half step, a
	// 		whole step, or to the third or fifth of their previous key
	// Usually, either the intro will be a different key and chord
	// 		progression from the rest of the song, and the rest of the
	// 		song won't change, or the intro will be the same as the verse
	// 		and the chorus or bridge may change
	
	// Generate benchmark metrics
	CompositionMetrics *mets = new CompositionMetrics();
	int ton = rand() % 12;
	ton += 36;
	int scale = rand() % 100;
	if (scale < 50) {
		Key key(ton, major_intervals);
		mets->key = key;
	}
	else if (scale < 90) {
		Key key(ton, minor_intervals);
		mets->key = key;
	}
	else {
		Key key(ton, dorian_intervals);
		mets->key = key;
	}
	int time = rand() % 100;
	if (time < 75) {
		TimeSignature timeSig(4, 4);
		mets->time_signature = timeSig;
	}
	else if (time < 95) {
		TimeSignature timeSig(3, 4);
		mets->time_signature = timeSig;
	}
	else {
		TimeSignature timeSig(6, 8);
		mets->time_signature = timeSig;
	}
	int temp = 60;
	temp += rand() % 180;
	mets->tempo = temp;
	mets->position = 0;
	comp.set_initial_composition_metrics(mets);
	//printf("metrics generated\n");
	
	// Generate generic chord progression
	std::vector<int> chords;
	int prev = 1;
	int curr = rand() % 4 + 3;
	chords.push_back(prev);
	chords.push_back(curr);
	for (int i = 0; i < 2; i++) {
		switch(curr) {
			case 1:
				prev = curr;
				curr = rand() % 4 + 3;
				break;
			case 2:
				prev = curr;
				curr = rand() % 2 + 5;
				break;
			case 3:
				if (prev == 6) {
					prev = curr;
					curr = rand() % 3 + 4;
				}
				else {
					prev = curr;
					if (rand() % 5 == 0) {
						curr = 6;
					}
					else {
						curr = 2;
					}
				}
				break;
			case 4:
				if (prev == 1) {
					prev = curr;
					curr = (rand() % 2 + 1) + (rand() % 2 * 4);
				}
				else {
					prev = curr;
					if (rand() % 5 == 0) {
						curr = 1;
					}
					else {
						curr = 5;
					}
				}
				break;
			case 5:
				prev = curr;
				curr = 1 + (rand() % 2 * (3 + rand() % 2 * 2));
				break;
			case 6:
				prev = curr;
				curr = rand() % 5 + 1;
				break;
		}
		chords.push_back(curr);
	}
	//printf("chord prog generated\n");
	
	
	// Generate Intro: can be 4 or 8 measures
	/*CompositionMetrics introMets;
	Key introKey;
	TimeSignature introTimeSig;
	int introTempo;*/
	int introLength = (rand() % 2 + 1) * 4;
	PatternSegment *intro = new PatternSegment("intro", introLength * mets->time_signature.duration_of_measure());
	Part *introPart = new Part();
	for (int i = 0; i < introLength / 4; i++) {
		Chord *c = new Chord(mets->key.get_chord(chords[0]), mets->time_signature.duration_of_measure());
		Chord *c1 = new Chord(mets->key.get_chord(chords[1]), mets->time_signature.duration_of_measure());
		Chord *c2 = new Chord(mets->key.get_chord(chords[2]), mets->time_signature.duration_of_measure());
		Chord *c3 = new Chord(mets->key.get_chord(chords[3]), mets->time_signature.duration_of_measure());
		introPart->append_chord(c);
		introPart->append_chord(c1);
		introPart->append_chord(c2);
		introPart->append_chord(c3);
	}
	intro->set_chord_progression(introPart);
	//printf("intro generated\n");
	
	// Generate Verse: can be 8, 12, 16, or 20 measures
	/*CompositionMetrics verseMets;
	Key verseKey;
	TimeSignature verseTimeSig;
	int verseTempo;*/
	int verseLength = (rand() % 4 + 2) * 4;
	PatternSegment *verse = new PatternSegment("verse", verseLength * mets->time_signature.duration_of_measure());
	Part *versePart = new Part();
	for (int i = 0; i < verseLength / 4; i++) {
		Chord *c = new Chord(mets->key.get_chord(chords[0]), mets->time_signature.duration_of_measure());
		Chord *c1 = new Chord(mets->key.get_chord(chords[1]), mets->time_signature.duration_of_measure());
		Chord *c2 = new Chord(mets->key.get_chord(chords[2]), mets->time_signature.duration_of_measure());
		Chord *c3 = new Chord(mets->key.get_chord(chords[3]), mets->time_signature.duration_of_measure());
		versePart->append_chord(c);
		versePart->append_chord(c1);
		versePart->append_chord(c2);
		versePart->append_chord(c3);
	}
	verse->set_chord_progression(versePart);
	//printf("verse generated\n");
	
	// Generate Pre-Chorus: can be 4 measures
	/*CompositionMetrics prechorusMets;
	Key prechorusKey;
	TimeSignature prechorusTimeSig;
	int prechorusTempo;*/
	int prechorusLength = 4;
	PatternSegment* prechorus = new PatternSegment("prechorus", prechorusLength * mets->time_signature.duration_of_measure());
	Part *prechorusPart = new Part();
	for (int i = 0; i < prechorusLength / 4; i++) {
		Chord *c = new Chord(mets->key.get_chord(chords[0]), mets->time_signature.duration_of_measure());
		Chord *c1 = new Chord(mets->key.get_chord(chords[1]), mets->time_signature.duration_of_measure());
		Chord *c2 = new Chord(mets->key.get_chord(chords[2]), mets->time_signature.duration_of_measure());
		Chord *c3 = new Chord(mets->key.get_chord(chords[3]), mets->time_signature.duration_of_measure());
		prechorusPart->append_chord(c);
		prechorusPart->append_chord(c1);
		prechorusPart->append_chord(c2);
		prechorusPart->append_chord(c3);
	}
	prechorus->set_chord_progression(prechorusPart);
	//printf("prechorus generated\n");
	
	// Generate Chorus: can be 4, 8, or 16 measures
	/*CompositionMetrics chorusMets;
	Key chorusKey;
	TimeSignature chorusTimeSig;
	int chorusTempo;*/
	int chorusLength = pow(2, rand() % 3 + 2);
	PatternSegment *chorus = new PatternSegment("chorus", chorusLength * mets->time_signature.duration_of_measure());
	Part *chorusPart = new Part();
	for (int i = 0; i < chorusLength / 4; i++) {
		Chord *c = new Chord(mets->key.get_chord(chords[0]), mets->time_signature.duration_of_measure());
		Chord *c1 = new Chord(mets->key.get_chord(chords[1]), mets->time_signature.duration_of_measure());
		Chord *c2 = new Chord(mets->key.get_chord(chords[2]), mets->time_signature.duration_of_measure());
		Chord *c3 = new Chord(mets->key.get_chord(chords[3]), mets->time_signature.duration_of_measure());
		chorusPart->append_chord(c);
		chorusPart->append_chord(c1);
		chorusPart->append_chord(c2);
		chorusPart->append_chord(c3);
	}
	chorus->set_chord_progression(chorusPart);
	//printf("chorus generated\n");
	
	// Generate Bridge: can be 8 or 16 measures
	/*CompositionMetrics bridgeMets;
	Key bridgeKey;
	TimeSignature bridgeTimeSig;
	int bridgeTempo;*/
	int bridgeLength = (rand() % 2 + 1) * 8;
	PatternSegment *bridge = new PatternSegment("bridge", bridgeLength * mets->time_signature.duration_of_measure());
	Part *bridgePart = new Part;
	for (int i = 0; i < bridgeLength / 4; i++) {
		Chord *c = new Chord(mets->key.get_chord(chords[0]), mets->time_signature.duration_of_measure());
		Chord *c1 = new Chord(mets->key.get_chord(chords[1]), mets->time_signature.duration_of_measure());
		Chord *c2 = new Chord(mets->key.get_chord(chords[2]), mets->time_signature.duration_of_measure());
		Chord *c3 = new Chord(mets->key.get_chord(chords[3]), mets->time_signature.duration_of_measure());
		bridgePart->append_chord(c);
		bridgePart->append_chord(c1);
		bridgePart->append_chord(c2);
		bridgePart->append_chord(c3);
	}
	bridge->set_chord_progression(bridgePart);
	//printf("bridge generated\n");
	
	// Register pattern segments
	comp.register_pattern_segment(intro);
	comp.register_pattern_segment(verse);
	comp.register_pattern_segment(prechorus);
	comp.register_pattern_segment(chorus);
	comp.register_pattern_segment(bridge);
	//printf("pattern segments registered\n");
	
	// generate pattern
	if (rand() % 2 == 0) {
		comp.add_to_pattern("intro");
	}
	int pchorus = rand() % 2;
	int pat = rand() % 5;
	if (pat == 0) {
		comp.add_to_pattern("verse");
		if (pchorus == 0) {
			comp.add_to_pattern("prechorus");
		}
		comp.add_to_pattern("chorus");
		comp.add_to_pattern("verse");
		if (pchorus == 0) {
			comp.add_to_pattern("prechorus");
		}
		comp.add_to_pattern("chorus");
		comp.add_to_pattern("bridge");
		comp.add_to_pattern("chorus");
	}
	else if (pat == 1) {
		comp.add_to_pattern("verse");
		comp.add_to_pattern("verse");
		comp.add_to_pattern("bridge");
		comp.add_to_pattern("verse");
	}
	else if (pat == 2) {
		comp.add_to_pattern("verse");
		if (pchorus == 0) { 
			comp.add_to_pattern("prechorus");
		}
		comp.add_to_pattern("chorus");
		comp.add_to_pattern("verse");
		if (pchorus == 0) { 
			comp.add_to_pattern("prechorus");
		}
		comp.add_to_pattern("chorus");
		comp.add_to_pattern("verse");
		comp.add_to_pattern("chorus");
	}
	else if (pat == 3) {
		comp.add_to_pattern("verse");
		comp.add_to_pattern("verse");
		if (pchorus == 0) { 
			comp.add_to_pattern("prechorus");
		}
		comp.add_to_pattern("chorus");
		comp.add_to_pattern("verse");
		if (pchorus == 0) { 
			comp.add_to_pattern("prechorus");
		}
		comp.add_to_pattern("chorus");
	}
	else {
		comp.add_to_pattern("verse");
		comp.add_to_pattern("verse");
		if (pchorus == 0) { 
			comp.add_to_pattern("prechorus");
		}
		comp.add_to_pattern("chorus");
		comp.add_to_pattern("bridge");
		comp.add_to_pattern("chorus");
	}
	//printf("patterns generated\n");
	
	nlohmann::json j;
	to_json(j, comp);
	//printf("converted to json\n");
	std::cout << j.dump() << std::endl;
}