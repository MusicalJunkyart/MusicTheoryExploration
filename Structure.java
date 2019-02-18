import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Structure {

	// Variables
	private ArrayList<Interval> intervals;
	
	// Constructors
	public Structure() {
		this(0);
	}
	
	public Structure(Structure s) {
		intervals = new ArrayList<Interval>(s.getIntervals());
	}
	
	// ex. 1 3b 5# 7 9b
	public Structure(String pattern) {
		intervals = IntervalsFromPattern(pattern);
	}
	
	public Structure(int size) {
		intervals = new ArrayList<Interval>(size);
	}
	
	// Setters
	public void addInterval(Interval i) {
		intervals.add(i);
		intervals.sort(null);
	}
	
	// Getters
	public ArrayList<Interval> getIntervals() {
		return intervals;
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
	// Returns a list of how many intervals there are inside this Structure
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
		
		// Invert the first num intervals up an octave
		Structure s = new Structure(this);
		for (int i = 0; i < num; i++) {
			
			Interval temp = s.intervals.get(i).up(Interval.O8);
			s.intervals.set(i, temp);
		}
		s.intervals.sort(null);
		
		// Normalize the Intervals with relation to root
		Interval root = s.intervals.get(0);
		for (int i = 0; i < s.intervals.size(); i++) {
			
			Interval temp = s.intervals.get(i).down(root);
			s.intervals.set(i, temp);
		}
		
		return s;
	}
	
	// Returns a measure of dissonance
	public Double complexity() {
		
		if(intervals.size() == 1) {
			return 1.0;
		}
		
		Rational[] rationals = new Rational[intervals.size()];
		for(int i = 0; i < intervals.size(); i++) {
			
			rationals[i] = intervals.get(i).rationalize();
		}
		
		Rational gcd = Rational.GCD(rationals[0], rationals[1]);
		Rational lcm = Rational.LCM(rationals[0], rationals[1]);
		
		for(int i = 2; i < rationals.length; i++) {
			
			gcd = Rational.GCD(gcd, rationals[i]);
			lcm = Rational.LCM(lcm, rationals[i]);
		}
		return lcm.divide(gcd).toDouble();
	}
	
	// Returns a normalized measure of dissonance
	public Double logComplexity() {
		return Math.log(this.complexity()) / this.intervals.size();
	}
	
	static ArrayList<Structure> allStructures(int size) {
		
		ArrayList<Structure> all = allStructures(new Structure("1"), size);
		all.sort((o1, o2) -> o1.complexity().compareTo(o2.complexity()));
		 
		for(int i = all.size() - 1; i >= 0; i--) {
			
			Structure temp = new Structure(all.get(i));
			boolean flag = false;
			
			for (int j = 1; j < temp.getIntervals().size(); j++) {
				
				if (all.contains(temp.inversion(j))) {
					flag = true;
					break;
				}
			}
			if(flag == true) all.remove(i);
		 }
		return all;
	}
	
	private static ArrayList<Structure> allStructures(Structure seed, int size) {
		
		if(size == 1) 
			return new ArrayList<Structure>(0);
		ArrayList<Structure> all = new ArrayList<Structure>();		
		
		int index = Interval.list.indexOf(seed.intervals.get(seed.intervals.size() - 1));
		for(int i = index + 1; i < Interval.list.size(); i++) {
			
			Structure temp = new Structure(seed);
			temp.addInterval(Interval.list.get(i));
			
			all.add(temp);
			all.addAll(allStructures(temp, size - 1));
		}
		return all;
	}
	
	// Conversions
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