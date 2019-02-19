
// Description
/*
 * Traditionally in Western music, a musical tone is a steady periodic sound.
 * A musical tone is characterized by its duration, pitch, intensity (or loudness),
 * and timbre (or quality).The notes used in music can be more complex than musical tones,
 * as they may include aperiodic aspects, such as attack transients, vibrato, and envelope modulation.
 * 
 * Here our class is simplified and just describes a named simple or pure tone, 
 * which is just a sinusoidal waveform with given frequency without a specifed amplitude
 */

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Note implements Comparable<Note> {
	
	// Constants
	// this is our reference note
	static Note A4 = new Note("A4", 440.0);
	
	// these notes specify the range of the piano
	// and they correspond to low A0 & high C8 respectively
	static final Note MIN = new Note("A0", A4.getFrequency() / Math.pow(2, 4)); 						
	static final Note MAX = new Note("C8", A4.getFrequency() * Math.pow(2, 3) * Interval.m3.getRatio());
	
	// Variables 
	
	// Scientific pitch notation
	// name gives information about the abstract note name
	// the accidentals, the octave and the error deviation in cent 
	private String name;   // name is not unique
	private Double frequency;

	
	//Constructors
	public Note() {
		this(A4);
	}
	
	// copy constructor
	public Note(Note x) {
		this.name = new String(x.name);
		this.frequency = new Double(x.frequency);
	}
	
	// create new Note only with frequency, name will be created automaticaly
	public Note(Double frequency) {
		this(NameFrequency(frequency), frequency);
	}
	
	// create new Note only with name, frequency will be created automaticaly
	public Note(String name) {
		this(name, FrequencyFromName(name));
	}
	
	// create analyticaly new Note
	public Note(String name, Double frequency) {
				
		if (frequency <= 0) {
			throw new IllegalArgumentException("Frequency cannot be less or equal to 0");
		}
		this.name = name;
		this.frequency = frequency;
	}
	
	//Setters
	public void setName(String name) {
		this.name = name;
	}
	public void setFrequency(Double frequency) {
		this.frequency = frequency;
	}
	
	//Getters
	public String getName() {
		return name;
	}
	public Double getFrequency() {
		return frequency;
	}
	
	@Override
    public int compareTo(Note that) {
        Double dv = this.frequency - that.frequency;
        if (dv < 0) return -1;
        if (dv > 0) return +1;
        return 0;
    }
    
	@Override
    public boolean equals(Object that) {
		
    	if (that == null) {
			throw new NullPointerException("Must supply a non-null Object value");
		}
    	
        if (that.getClass() != this.getClass()) return false;
        return compareTo((Note)that) == 0;
    }
	
	@Override
	public String toString() {
		return this.name;
	}
	
	@Override
    public int hashCode() {
        return this.frequency.hashCode();
    }

	//Methods
	
	// Raturns this Note transposed a specified Interval up
	public Note up(Interval that) {
		
		Double frequency = this.frequency * that.getRatio();
		return new Note(frequency);
	}
	
	// Raturns this Note transposed a specified Interval down
	public Note down(Interval that) {
		
		Double frequency = this.frequency / that.getRatio();
		return new Note(frequency);
	}
	
	// Returns the nth undertone of this Note
	public Note undertone(int nth) {
		return overtone(-nth);
	}
	
	// Returns the nth overtone of this Note
	public Note overtone(int nth) {
		// 0th overtone is set as the fundamental frequency
		if (nth == 0) return this;
		if (nth <  0) return new Note(frequency / -(nth - 1));
		if (nth >  0) return new Note(frequency *  (nth + 1));
		return null;
	}
	
	// Conversions
	// Retruns the corresponding frequency from a given piano key
	static Double FrequencyFromKey(long key) {
		Double frequency = MIN.getFrequency() * Math.pow(Interval.H.getRatio(), key - 1);
		
		// in order to round errors from double
		// also this creates an upper limit of 9.223372036854776E8 Hz
		double precission = Math.pow(10, 10);   
		frequency = Math.round(frequency * precission) / precission;
		return frequency;
	}

	// Returns the correspoing piano key from a given frequency
	static long KeyFromFrequency(double frequency) {
		return (long)Math.round(12 * Math.log(frequency / MIN.frequency) / Math.log(2) + 1);
	}
	
	// Returs the octave range from a given frequency
	static Integer OctaveFromFrequency(double frequency) {
		int octave = 0;
		long key = KeyFromFrequency(frequency);
		if(frequency >= MIN.frequency)
			while((key -= 12) >= -8) octave++;
		else 
			while((key += 12) < 4) octave--;
		return octave;
	}

	// Assigns Note name to a given frequency
	static String NameFrequency(double frequency) {
		
		String[] notes = {
				"Ao", "A#o/Bbo", "Bo", "Co", "C#o/Dbo", "Do", 
				"D#o/Ebo", "Eo", "Fo", "F#o/Gbo", "Go", "G#o/Abo"
		};
		
		// calculate the nearest note name to the frequency
		int key = (int)(KeyFromFrequency(frequency) - 1) % 12;
		key = (key > 0) ? key : (12 + key) % 12;
		
		String name = notes[key];
		name = name.replace("o", OctaveFromFrequency(frequency).toString());
		

		// calculate the error in cent from the given frequency 
		Double error = frequency / FrequencyFromKey(KeyFromFrequency(frequency));				  
		int error_cent = (int)Math.round(Math.log(error) / Math.log(Interval.cent));  
		
		if (error < 1 / Interval.limit || error > Interval.limit) {
			
			String sign = (error_cent > 0) ? "+" : "-";
			name = name.concat(sign + Math.abs(error_cent) + "c");
		}
		return name;
	} 
	
	// Returns the frequecy from a given Note name
	static Double FrequencyFromName(String name) {
		
		// correct name template: <pitch class><accidental><octave><cent error> 
		// ex. <A><b><3><-20c> == Ab3-20c
		
		@SuppressWarnings("serial")
		HashMap<Character, Integer> noteToKey = new HashMap<Character, Integer>() {{			
			put('C', -8);
			put('D', -6);
			put('E', -4);
			put('F', -3);
			put('G', -1);
			put('A',  1);
			put('B',  3);
		}};
				
		
		// Find pitch class lowest piano key
		if (name.charAt(0) < 'A' || name.charAt(0) > 'G') {
			throw new IllegalArgumentException("You must include pitch class note information");
		}
		int key = noteToKey.get(name.charAt(0));
		
		
		// Find accidentals
		if (name.indexOf("X")  != -1) key += 2;
		if (name.indexOf("#")  != -1) key += 1;
		if (name.indexOf("b")  != -1) key -= 1;
		if (name.indexOf("bb") != -1) key -= 1;

		
		// Find octave range
		Pattern pattern = Pattern.compile("[-]?[0-9]+");
		Matcher matcher = pattern.matcher(name);
		
		if (! matcher.find()) {
			throw new IllegalArgumentException("You must include octave information");
		}
		int octave = Integer.parseInt(matcher.group());
		key += 12 * octave;
		
		
		// Find the error deviation in cents	
		double frequency = FrequencyFromKey(key);
		pattern = Pattern.compile("([+-][0-9]+)c");
		matcher = pattern.matcher(name);
		if(matcher.find()) {
			
			double error = Double.parseDouble(matcher.group(1));
			if(error > 0) frequency *=  error;
			if(error < 0) frequency /= -error;
		}
		return frequency;
	}

}
