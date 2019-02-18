import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Chord {

	// Variables
	private Note root;
	private Structure structure;
	private ArrayList<Note> notes;

	// Constructors
	public Chord() {
		this("A4", "1");
	}
	
	public Chord(Chord c) {
		this.root = c.notes.get(0);
		this.notes = new ArrayList<Note>(c.getNotes());
		this.structure = new Structure(c.getStructure());
	}
	
	public Chord(ArrayList<Note> notes) {
		notes.sort(null);
		this.root = notes.get(0);
		this.notes = new ArrayList<Note>(notes);
		this.structure = StructureFromNotes(notes);
	}

	public Chord(String root, String pattern) {

		if (root == null) {
			throw new NullPointerException("Must supply a non-null Note value");
		}
		this.root = new Note(root);
		this.structure = new Structure(pattern);
		this.notes = new ArrayList<Note>(structure.getIntervals().size());
		
		for (int i = 0; i < structure.getIntervals().size(); i++) {

			notes.add(this.root.up(structure.getIntervals().get(i)));
		}
		this.notes.sort(null);
	}

	public Chord(Note root, Structure structure) {
		
		if (root == null || structure == null) {
			throw new NullPointerException("Must supply non-null Object values");
		}
		this.root = root;
		this.structure = structure;

		this.notes = new ArrayList<Note>(structure.getIntervals().size());
		for (int i = 0; i < structure.getIntervals().size(); i++) {

			notes.add(root.up(structure.getIntervals().get(i)));
		}
		this.notes.sort(null);
	}

	// Setters
	public void setRoot(Note root) {

		this.root = root;
		this.notes = new ArrayList<Note>(structure.getIntervals().size());
		for (int i = 0; i < structure.getIntervals().size(); i++) {

			notes.add(root.up(structure.getIntervals().get(i)));
		}
	}

	public void setStructure(String pattern) {

		this.structure = new Structure(pattern);
		this.notes = new ArrayList<Note>(structure.getIntervals().size());
		for (int i = 0; i < structure.getIntervals().size(); i++) {

			notes.add(root.up(structure.getIntervals().get(i)));
		}
		this.notes.sort(null);
	}

	// Getters
	public Note getRoot() {
		return root;
	}

	public Structure getStructure() {
		return structure;
	}

	public ArrayList<Note> getNotes() {
		return notes;
	}
	
	@Override
    public boolean equals(Object that) {
		
    	if (that == null) {
			throw new NullPointerException("Must supply a non-null Object value");
		}
    	
        if (that.getClass() != this.getClass()) return false;
        return this.notes.equals(((Chord)that).notes);
    }
	
	@Override
	public String toString() {
		return this.notes.toString();
	}
	
	@Override
    public int hashCode() {
        return this.toString().hashCode();
    }

	
	// Methods
	
	public TreeMap<Note, Integer> solutions() {
		
		TreeMap<Note, Integer> solutions = new TreeMap<Note, Integer>();
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		ArrayList<Chord> subChords = this.subChords();
		
		// we find the solutions
		for (int i = 0; i < subChords.size(); i++) {
			
			String sol1 = subChords.get(i).GCU().getName();	
			String sol2 = subChords.get(i).LCO().getName();
			int counter1 = subChords.get(i).getNotes().size();
			int counter2 = counter1;
			
			if(map.containsKey(sol1)) {
				counter1 += map.get(sol1);
			}
			if(map.containsKey(sol2)) {
				counter2 += map.get(sol2);
			}
			map.put(sol1, counter1);
			map.put(sol2, counter2);
		}
		
		for (String s : map.keySet()) {
			solutions.put(new Note(s), map.get(s));
		}
		return solutions;
	}
	
	public ArrayList<Chord> subChords() {
		
		// set to store all the subsequences 
	    ArrayList<Chord> list = new ArrayList<Chord>();
	    
	    // iterate over the entire chord notes
	    for (int i = 0; i < notes.size(); i++) {
	    	
	    	// iterate from the end of chord notes 
            // to generate subchords
            for (int j = notes.size(); j > i + 1; j--) {
            	
            	ArrayList<Note> sub_notes = new ArrayList<Note>();
            	sub_notes.addAll(notes.subList(i, j));
            	Chord sub_chord = new Chord(sub_notes);
            
            	// check is subchord already exists
            	if(!list.contains(sub_chord)) 
            		list.add(sub_chord);
            	
            	// drop kth character in the substring 
                // and if its not in the set then recur 
                for (int k = 1; k < sub_chord.notes.size() - 1; k++) { 
                    Chord temp = new Chord(sub_chord); 
                     
                    // drop character from the string 
                    temp.getNotes().remove(k);
                    temp = new Chord(temp.getNotes());                    
                    if(!list.contains(temp)) 
                		list.add(temp);
                    
                   // recursion 
                   ArrayList<Chord> temp_list = temp.subChords(); 
                   for(Chord c : temp_list) {
                	   if(!list.contains(c))
                		   list.add(c);
                	   
                   }
                } 
            }
	    }
	    return list;
	}
	
	// Returns the Greatest Common Undertone of chord notes
	public Note GCU() {
		
		if(notes.size() == 1) {
			return notes.get(0);
		}
		
		Rational[] intervals = new Rational[structure.getIntervals().size()];
		for(int i = 0; i < intervals.length; i++) {
			intervals[i] = structure.getIntervals().get(i).rationalize();
		}
				
		Rational gcd = Rational.GCD(intervals[0], intervals[1]);
		for(int i = 2; i < intervals.length; i++) {
			
			gcd = Rational.GCD(gcd, intervals[i]);
		}
		Interval i = new Interval(gcd.invert().toDouble());
		return new Note(notes.get(0).down(i));
	}
	
	// Returns the Least Common Overtone of chord notes 
	public Note LCO() {
		
		if(notes.size() == 1) {
			return notes.get(0);
		}
		
		Rational[] intervals = new Rational[structure.getIntervals().size()];
		for(int i = 0; i < intervals.length; i++) {
			intervals[i] = structure.getIntervals().get(i).rationalize();
			//System.out.println(intervals[i]);
		}
				
		Rational lcm = Rational.LCM(intervals[0], intervals[1]);
		for(int i = 2; i < intervals.length; i++) {
			
			lcm = Rational.LCM(lcm, intervals[i]);
		}
		Interval i = new Interval(lcm.toDouble());
		return new Note(notes.get(0).up(i));
	}
	
	// Returns a measure of dissonance
	public Double complexity() {
		return this.getStructure().complexity();
	}
	
	// Returns a normalized measure of dissonance
	public Double logComplexity() {
		return this.getStructure().logComplexity();
	}
	
	// Returns the nth inversion of the chord
	public Chord inversion(int num) {
		
		if (num < 0 || num >= this.notes.size()) {
			System.out.println("This Inversion is not Supported");
			return this;
		}
		Note newRoot = this.notes.get(num);
		Structure structure = this.structure.inversion(num);
		return new Chord(newRoot, structure);
	}
	
	// Returns the Note pattern 
	private Structure StructureFromNotes(ArrayList<Note> notes) {
		Structure s = new Structure();
		notes.sort(null);
		
		for (int i = 0; i < notes.size(); i++) {
			s.addInterval(new Interval(notes.get(0), notes.get(i)));
		}
		return s;
	}

}
