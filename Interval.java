import java.util.ArrayList;
import java.util.Arrays;

public class Interval implements Comparable<Interval> {

	// Variables
	private Double value;
	private String name;

	// Constructors
	public Interval() {
		this(U1);
	}
	
	public Interval(double value) {
		this(NameValue(value), value);
	}

	public Interval(Interval interval) {
		
		if (interval == null) {
			throw new NullPointerException("Must supply a non-null Object value");
		}
		this.name  = interval.name;
		this.value = interval.value;
	}
	
	public Interval(String name, double value) {
		
		if (value <= 0) {
			throw new IllegalArgumentException("Value cannot be less or equal to 0");
		}
		this.value = (value < 1) ? 1 / value : value; 
		this.name  = name;
	}
	
	// Returns Interval detween two Notes
	public Interval(Note N, Note M) {
		
		if (N == null || M == null) {
			throw new NullPointerException("Must supply non-null Notes");
		}
		value = N.getFrequency() / M.getFrequency();
		if (value < 1) value = 1 / value;	
		name = NameValue(value);
	}
	
	// Setters
	public void setName(String n) {
		name = n;
	}

	public void setValue(Double v) {
		value = v;
	}

	// Getters
	public String getName() {
		return name;
	}

	public Double getValue() {
		return value;
	}

	@Override
	// Returns -1, 0, +1 
	public int compareTo(Interval that) {
		Double dv = this.value - that.value;
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
		return this.value.hashCode();
	}

	// Methods
	
	// Returns resulting Interval from going this an Interval that up
	public Interval up(Interval that) {
		
		Double v = this.value * that.value;
		return new Interval(NameValue(v), v);
	}
	
	// Returns resulting Interval from going this an Interval that down
	public Interval down(Interval that) {
		
		Double v = this.value / that.value;
		if (v < 1) v = 1 / v;		
		return new Interval(NameValue(v), v);
	}
	
	// Returns repeating this Interval n times
	public Interval times(double n) {
		
		if (n < 0) {
			throw new IllegalArgumentException("Value cannot be negative");
		}
		Double v = Math.pow(this.value, n);
		return new Interval(NameValue(v), v);
	}
	
	// Returns a Rational approximation of Interval value
	public Rational rationalize() {
		return rationalize(this);
	}

	static Rational rationalize(Interval x) {
		Rational rational = Rational.ONE;
		Double error = 20.0;
		
		for (int term = 1; error > 16; term++) {  // error percived by humans in 6 cent and more
			
			rational = Rational.approximateSBT(x.value, term);
			error = Math.abs(Math.log(x.value / rational.toDouble()) / Math.log(cent));
		}
		return rational;
	}

	// Returns the Iterval name of the corresponding value
	public static String NameValue(double value) {

		if (value <= 0) {
			throw new IllegalArgumentException("Value cannot be less or equal to 0");
		}
		if (value < 1) value = 1 / value;		
		
		String[] intervals = { 
				"O1", "m2", "M2", "m3", "M3", "P4", "TT", "P5", "m6", "M6", "m7", "M7" 
		};
		
		// calculate the nearest interval name
		int num  = (int)Math.round(12 * Math.log(value) / Math.log(2));
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
		Double error  = value / Math.pow(2, num / 12.0);
		int error_cent = (int)Math.round(Math.log(error) / Math.log(cent));
		
		if (error < 1 / limit || error > limit) {
			String sign = (error_cent > 0) ? "+" : "-";
			name = name.concat(sign + Math.abs(error_cent) + "c");
		}
		return name;
	}

	// Interval units
	static final Double cent  = Math.pow(2, 1.0 / 1200);
	static final Double limit = Math.pow(cent, 6);  // Humans can identify differences of 5~6 cents
	
	// Common Intervals
	static final Interval H = new Interval("Half  Tone", Math.pow(cent, 100));
	static final Interval W = new Interval("Whole Tone", Math.pow(H.value, 2));
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
	static final ArrayList<Interval> list = new ArrayList<Interval>(
			Arrays.asList(U1, m2, M2, m3, M3, P4, TT, P5, m6, M6, m7, M7));
	
}
