// Description
/*
 * In music theory, an interval is the difference in pitch between two sounds
 * In physical terms, an interval is the ratio between two sonic frequencies
 */

import java.util.ArrayList;
import java.util.Arrays;

public class Interval implements Comparable<Interval> {

	// Variables
	
	// name gives information about the quality of Interval 
	// the size & the error deviation from that quality in cent
	// name is not unique
	private String name;  
	// we alow ration to be strictly greater or equal to 1
	private Double ratio;

	// Constructors
	public Interval() {
		this(U1);
	}
	
	// create new Interval only with ratio, name will be created automaticaly
	public Interval(double ratio) {
		this(NameRatio(ratio), ratio);
	}

	// copy contructor
	public Interval(Interval interval) {
		
		if (interval == null) {
			throw new NullPointerException("Must supply a non-null Object value");
		}
		this.name  = new String(interval.name);
		this.ratio = new Double(interval.ratio);
	}
	
	// create analyticaly new Interval
	public Interval(String name, double ratio) {
		
		if (ratio <= 0) {
			throw new IllegalArgumentException("Ratio cannot be less or equal to 0");
		}
		this.ratio = (ratio < 1) ? 1 / ratio : ratio; 
		this.name  = name;
	}
	
	// returns Interval detween two Notes
	public Interval(Note N, Note M) {
		
		if (N == null || M == null) {
			throw new NullPointerException("Must supply non-null Notes");
		}
		ratio = N.getFrequency() / M.getFrequency();
		if (ratio < 1) ratio = 1 / ratio;	
		name = NameRatio(ratio);
	}
	
	// Setters
	public void setName(String n) {
		name = n;
	}

	public void setRatio(Double r) {
		ratio = r;
	}

	// Getters
	public String getName() {
		return name;
	}

	public Double getRatio() {
		return ratio;
	}

	@Override
	// Returns -1, 0, +1 
	public int compareTo(Interval that) {
		Double dv = this.ratio - that.ratio;
		if (dv < 0) return -1;
		if (dv > 0) return +1;
		return 0;
	}

	@Override
	// Returns true if objects are equal
	public boolean equals(Object that) {
		
		if (that == null) {
			throw new NullPointerException("Must supply a non-null Object value");
		}
		
		if (that.getClass() != this.getClass()) {
			return false;
		}
		return (this.compareTo((Interval) that) == 0);
	}

	@Override
	// Returns String representation of this
	public String toString() {
		return this.name;
	}
	
	@Override
	// HashCode Consistent with equals() & compareTo()
	public int hashCode() {
		return this.ratio.hashCode();
	}

	// Methods
	
	// Returns resulting Interval from going this an Interval that up
	public Interval up(Interval that) {
		
		Double r = this.ratio * that.ratio;
		return new Interval(NameRatio(r), r);
	}
	
	// Returns resulting Interval from going this an Interval that down
	public Interval down(Interval that) {
		
		Double r = this.ratio / that.ratio;
		if (r < 1) r = 1 / r;		
		return new Interval(NameRatio(r), r);
	}
	
	// Returns repeating this Interval n times
	public Interval times(double n) {
		
		if (n < 0) {
			throw new IllegalArgumentException("Value cannot be negative");
		}
		Double r = Math.pow(this.ratio, n);
		return new Interval(NameRatio(r), r);
	}
	
	// Returns Rational approximation of this Interval ratio within 
	// the specified error (in cent) using Stern-Brocot tree method (see class Rational)
 	public Rational approxRatio(double errorInCent) {
 		
 		errorInCent = Math.abs(errorInCent);
		Rational rational = Rational.ONE;
		Double error = errorInCent + 1;
		
		for (int term = 1; error > errorInCent; term++) { 
			
			rational = Rational.approxWithSBT(this.ratio, term);
			error = Math.abs(Math.log(this.ratio / rational.toDouble()) / Math.log(cent));  
		}
		return rational;
	}
 	
 	// the default error in cent is set 16 in order to obtain simple Rational forms
 	public Rational approxRatio() {
 		return this.approxRatio(16);
 	}

 	/*
 	 * UNDER CONSTRUCTION
 	 * Need to generalize this in order to differentiate augmented, diminished qualities
 	 */
	// Assignes an Iterval name to the corresponding Interval ratio you give
	public static String NameRatio(double ratio) { 

		if (ratio <= 0) 
			throw new IllegalArgumentException("Ratio cannot be less or equal to 0");
		if (ratio < 1) ratio = 1 / ratio;		
		
		String[] intervals = { 
				"O1", "m2", "M2", "m3", "M3", "P4", "TT", "P5", "m6", "M6", "m7", "M7" 
		};
		
		// calculate the nearest interval name
		int num  = (int)Math.round(12 * Math.log(ratio) / Math.log(2));
		String name = intervals[num % 12];
		
		if (num == 0) //special case for U1
			name = "U1";
		else if (num >= 12) {
			
			Integer note;
			if(num % 12 == 6) {  //special case for TT
				note = 1 + 7 * (num / 12);
				name += note.toString();
			}
			else {
				note = name.charAt(1) - '0' + 7 * num / 12;
				name = name.charAt(0) + note.toString();
			}
		}
		
		// calculate and print the error 
		Double error  = ratio / Math.pow(2, num / 12.0);
		int error_cent = (int)Math.round(Math.log(error) / Math.log(cent));
		
		if (error < 1 / limit || error > limit) {
			String sign = (error_cent > 0) ? "+" : "-";
			name = name.concat(sign + Math.abs(error_cent) + "c");
		}
		return name;
	}

	// The standard system for comparing interval sizes is with cents
	static final Double cent  = Math.pow(2, 1.0 / 1200);
	static final Double limit = Math.pow(cent, 6);  // Humans can identify differences between 5 to 6 cents
	
	// Main intervals
	static final Interval H = new Interval("Half  Tone", Math.pow(cent, 100));
	static final Interval W = new Interval("Whole Tone", Math.pow(H.ratio, 2));
	static final Interval U1 = new Interval("U1", Math.pow(2, 0.0  / 12));
	static final Interval m2 = new Interval("m2", Math.pow(2, 1.0  / 12));
	static final Interval M2 = new Interval("M2", Math.pow(2, 2.0  / 12));
	static final Interval m3 = new Interval("m3", Math.pow(2, 3.0  / 12));
	static final Interval M3 = new Interval("M3", Math.pow(2, 4.0  / 12));
	static final Interval P4 = new Interval("P4", Math.pow(2, 5.0  / 12));
	static final Interval TT = new Interval("TT", Math.pow(2, 6.0  / 12));
	static final Interval P5 = new Interval("P5", Math.pow(2, 7.0  / 12));
	static final Interval m6 = new Interval("m6", Math.pow(2, 8.0  / 12));
	static final Interval M6 = new Interval("M6", Math.pow(2, 9.0  / 12));
	static final Interval m7 = new Interval("m7", Math.pow(2, 10.0 / 12));
	static final Interval M7 = new Interval("M7", Math.pow(2, 11.0 / 12));
	static final Interval O8 = new Interval("O8", Math.pow(2, 12.0 / 12));
	
	// List of Main Intervals
	static final ArrayList<Interval> list = new ArrayList<Interval>(
			Arrays.asList(U1, m2, M2, m3, M3, P4, TT, P5, m6, M6, m7, M7));
	
}
