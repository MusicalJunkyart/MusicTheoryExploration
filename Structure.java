
// Description
/*
 * Here as Music Structure we specify the abstract 
 * collection of intervals that will constitude the backbone
 * in which other classes will be built. These classes might be
 * chords, scales, extensions etc. Also I provide some functionality
 * and methods to extract musical infromation about the Structure
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Structure {

	// Variables
	
	// Pattern is a string that conveys information 
	// about the intervals within the structure
	// ex. 1 3b 5# 7 9b
	private String pattern;  // pattern is not unique
	private ArrayList<Interval> intervals;

	// Constructors
	public Structure() {
		this(0);
	}
	
	// copy constructor
	public Structure(Structure s) {
		this.intervals = new ArrayList<Interval>(s.getIntervals());
		this.pattern = new String(s.pattern);
	}
	
	// create new Structure only with pattern, intervals will be created automaticaly
	public Structure(String pattern) {
		this.intervals = IntervalsFromPattern(pattern);
		this.pattern = new String(pattern);
	}
	
	// create new Structure with intervals, pattern will be created automaticaly
	public Structure(ArrayList<Interval> intervals) {
		this.intervals = new ArrayList<Interval>(intervals);
		this.pattern = PatternFromIntervals(intervals);
	}
		
	// initialize an emty structure 
	public Structure(int size) {
		intervals = new ArrayList<Interval>(size);
		pattern = "";
	}
	
	
	// Setters
	public void setIntervals(ArrayList<Interval> intervals) {
		this.intervals = new ArrayList<Interval>(intervals);
		this.pattern = PatternFromIntervals(intervals);
	}
	
	public void setPattern(String pattern) {
		this.pattern = new String(pattern);
		this.intervals = IntervalsFromPattern(pattern);
	}
	
	// Getters
	public ArrayList<Interval> getIntervals() {
		return intervals;
	}
	
	public String getPattern() {
		return pattern;
	}
	
    
	@Override
    public boolean equals(Object that) {
		
    	if (that == null) {
			throw new NullPointerException("Must supply a non-null Object value");
		}
    	
        if (that.getClass() != this.getClass()) return false;
        return this.intervals.equals(((Structure)that).intervals);
    }
	
	@Override
	public String toString() {
		return this.intervals.toString();
	}
	
	@Override
    public int hashCode() {
        return this.toString().hashCode();
    }

	// Methods
	
	// adds an extra interval inside the Structure 
	public void addInterval(Interval i) {
		intervals.add(i);
		intervals.sort(null);
		pattern = PatternFromIntervals(intervals);
	}
	
	// Returns a list of all possible intervals located inside this Structure
	public ArrayList<Integer> intervalVector() {
		
		@SuppressWarnings("serial")
		TreeMap<String, Integer> counter = new TreeMap<String, Integer>() {{
			put("m2", 0);
			put("M2", 0);
			put("m3", 0);
			put("M3", 0);
			put("P4", 0);
			put("TT", 0);
		}};
		
		Structure s = this;
		int size = this.intervals.size();
		
		for (int gap = 1; gap < size; gap++) {
			for (int i = 0; i < size - gap; i++) {
				
				Interval temp = s.getIntervals().get(i).down(s.getIntervals().get(i + gap));
				if (temp.compareTo(Interval.O8) > 0) {
					temp = temp.down(Interval.O8);
				}
				if (temp.compareTo(Interval.TT) > 0) {
					temp = Interval.O8.down(temp);
				}
				String key = temp.getName();
				if(counter.containsKey(key)) {
					Integer n = counter.get(key);
					counter.replace(key, n + 1);
				}				
			}
		}
		int index = 0;
		ArrayList<Integer> vector = new ArrayList<Integer>(counter.size());
		for(Interval key = Interval.m2; key.compareTo(Interval.TT) <= 0; key = key.up(Interval.H)) {
			
			vector.add(counter.get(key.getName()));
			System.out.println(key + ": " + vector.get(index));
			index++;
		}
		return vector;
	}
	
	// Retruns a supported inversion of the Structure
	public Structure inversion(int num) {
		
		if (num < 0 || num >= this.intervals.size()) {
			System.out.println("Num of Inversion is not Supported");
			return this;
		}
		
		// invert the first num intervals up an octave
		Structure s = new Structure(this);
		for (int i = 0; i < num; i++) {
			
			Interval temp = s.intervals.get(i).up(Interval.O8);
			s.intervals.set(i, temp);
		}
		s.intervals.sort(null);
		
		// normalize the Intervals with relation to root
		Interval root = s.intervals.get(0);
		for (int i = 0; i < s.intervals.size(); i++) {
			
			Interval temp = s.intervals.get(i).down(root);
			s.intervals.set(i, temp);
		}
		
		return s;
	}
	
	// this can be defined as a degree of dissonance 
	// and it's acctualy a measure of how far we
	// find this Structure inside the overtone Series
	public Double complexity() {
		
		if(intervals.size() == 1) {
			return 1.0;
		}
		
		Rational[] rationals = new Rational[intervals.size()];
		for(int i = 0; i < intervals.size(); i++) {
			
			rationals[i] = intervals.get(i).approxRatio();
		}
		
		Rational gcd = Rational.GCD(rationals[0], rationals[1]);
		Rational lcm = Rational.LCM(rationals[0], rationals[1]);
		
		for(int i = 2; i < rationals.length; i++) {
			
			gcd = Rational.GCD(gcd, rationals[i]);
			lcm = Rational.LCM(lcm, rationals[i]);
		}
		return lcm.divide(gcd).toDouble();
	}
	
	// Returns the above measure normalized including 
	// the size of the Structure as information 
	public Double normComplexity() {
		return Math.log(this.complexity()) / this.intervals.size();
	}
	
	// Creates a list of all possible Structures with a given
	// size, sorted in ascending order based on their complexity
	static ArrayList<Structure> allCombinations(int size) {
		
		if (size <= 0) {
			throw new IllegalArgumentException("Size cannot be less or equal to 0");
		}
		
		// the initial seed must be "1" to start properly the recurssion
		ArrayList<Structure> all = allCombinations(new Structure("1"), size, size);
		all.sort((o1, o2) -> o1.complexity().compareTo(o2.complexity()));
		 
		// in order to keep unique Stractures, we must remove their inversions
		for(int i = all.size() - 1; i >= 0; i--) {
			Structure temp = new Structure(all.get(i));
			
			for (int j = 1; j < temp.getIntervals().size(); j++) {
				if (all.contains(temp.inversion(j))) {
					all.remove(i);
					break;
				}
			}
		}
		return all;
	}
	
	// in order to calculate all possible combinations of Structures 
	// with a specified size we need to use recurssion 
	// In each step we build the seed adding to it different
	// intervals until we reach Structures with the desired size
	private static ArrayList<Structure> allCombinations(Structure seed, int counter, int size) {
	
		if (size == 1)
			return new ArrayList<Structure>(Collections.singleton(new Structure("1")));
		if(counter == 1) 
			return new ArrayList<Structure>(0);
			
		// we start from the U1 and then step by step we add more intervals
		ArrayList<Structure> all = new ArrayList<Structure>();		
		int index = Interval.list.indexOf(seed.intervals.get(seed.intervals.size() - 1));
		
		for(int i = index + 1; i < Interval.list.size(); i++) {
			
			Structure temp = new Structure(seed);
			temp.addInterval(Interval.list.get(i));
			
			if (temp.intervals.size() == size) {
				all.add(temp);
			}
			all.addAll(allCombinations(temp, counter - 1, size));
		}
		return all;
	}
	
	// Conversions
	
	/*
	 * UNDER CONSTRUCTION
	 * first I need to include Augmented & Diminished Intervals
	 */
	// creates a possible String pattern for a given Interval list
	static String PatternFromIntervals(ArrayList<Interval> intervals) {
		String pattern = "";
		return pattern;
	}
	
	// converts a given string pattern to a list of Intervals
	static ArrayList<Interval> IntervalsFromPattern(String pattern) {
		@SuppressWarnings("serial")
		HashMap<Integer, String> keyToNote = new HashMap<Integer, String>() {{			
			put(1, "C");
			put(2, "D");
			put(3, "E");
			put(4, "F");
			put(5, "G");
			put(6, "A");
			put(7, "B");
			
		}};		
		
		// Find the information inside pattern
		Matcher matcher = Pattern.compile("\\d+[X#b]*").matcher(pattern);
		ArrayList<String> allMatches = new ArrayList<String>();
		while (matcher.find()) {
			allMatches.add(matcher.group());
		}
	
		// Translate information to Notes
		ArrayList<Note> notes = new ArrayList<Note>();
		for (String s : allMatches) {
			int octave = 1;
			int noteNumber = 1;
			
			// retrive note number from string
			matcher = Pattern.compile("\\d+").matcher(s);
			if (matcher.find()) {
				noteNumber = Integer.parseInt(matcher.group());
			}
			// find the note octave range
			while (noteNumber > 7) {
				noteNumber -= 7;
				octave++;
			}
			// name the note 
			s = s.replaceAll("\\d+", keyToNote.get(noteNumber)).concat(octave + "");
			notes.add(new Note(s));
		}
		
		// Translate Notes to Intervals
		ArrayList<Interval> intervals = new ArrayList<Interval>();
		HashSet<Note> set = new HashSet<>(notes);
		notes.clear();
		notes.addAll(set);  //remove duplicates
		notes.sort(null);   //order the notes
	
		for (Note n : notes) {
			intervals.add(new Interval(notes.get(0), n));
		}
		return intervals;
	}
	
	
}