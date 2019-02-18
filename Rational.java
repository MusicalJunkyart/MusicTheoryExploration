
/******************************************************************************
 *	Source: https://introcs.cs.princeton.edu/java/92symbolic/Rational.java.html
 *	With additions and improvements
 ******************************************************************************/

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public final class Rational implements Comparable<Rational> {

	// Constants
	static Rational ZERO = new Rational(0, 1);
	static Rational ONE  = new Rational(1, 1);

	// Variables
	private long numerator;
	private long denominator;

	// Constructors
	public Rational() {
		this(0, 1);
	}
	
	public Rational(long numerator) {		
		this(numerator, 1);
	}
	
	public Rational(Rational rational) {
		
		if (rational == null) {
			throw new NullPointerException("Must supply a non-null Object value");
		}
		
		numerator   = rational.numerator;
		denominator = rational.denominator;
	}
	
	public Rational(long numerator, long denominator) {
		
		if (denominator < 0 && numerator < 0) {
			denominator = -denominator;
			numerator   = -numerator;
		}
		
		this.denominator = denominator;
		this.numerator   = numerator;
		this.reduce();
	}
	
	// Reduces fraction to simple form
	private void reduce() {
		
		if (denominator == 0) {
			throw new IllegalArgumentException("Denominator cannot be 0");
		}
		
		if (denominator < 0 && numerator < 0) {
			denominator = -denominator;
			numerator =   -numerator;
		}
		
		long divisor = (numerator == 0) ? 1 : GCD(numerator, denominator);
		denominator = (numerator == 0) ? 1 : denominator / divisor;
		numerator /= divisor;
	}

	// Setters
	public void setNumerator(long n) {
		numerator = n;
		this.reduce();
	}

	public void setDenominator(long d) {
		denominator = d;
		this.reduce();
	}

	public void setRational(long n, long d) {
		numerator = n;
		denominator = d;
		this.reduce();
	}
 
	// Getters
	public long getNumerator() {
		return numerator;
	}

	public long getDenominator() {
		return denominator;
	}

	// Returns Double representation of this 
	public double toDouble() {
		return (double) numerator / denominator;
	}

	@Override
	// Returns String representation of this
	public String toString() {
		if (denominator == 1) return numerator + "";
		return numerator + "/" + denominator;
	}

	// Returns -1, 0, +1 
	public int compareTo(Rational that) {
		
		long a = this.numerator   * that.denominator;
		long b = this.denominator * that.numerator;
		
		if (a < b) return -1;
		if (a > b) return +1;
		return 0;
	}

	// Returns the Sign 
	public int signum() {
		return this.compareTo(ZERO);
	}
	
	// Returns absolute value of this
	public Rational abs() {
		
		if (numerator >= 0) return this;
		return new Rational(-numerator, denominator);
	}

	@Override
	// Return true if objects are equal
	public boolean equals(Object that) {
		
		if (that == null) {
			throw new NullPointerException("Must supply a non-null Object value");
		}
		
		if (that.getClass() != this.getClass()) {
			return false;
		}
		return (this.compareTo((Rational)that) == 0);
	}

	@Override
	// HashCode Consistent with equals() & compareTo()
	public int hashCode() {
		return this.toString().hashCode();
	}

	// Returns Greatest Common Divisor
	static long GCD(long m, long n) {
		
		if (m < 0) m = -m;
        if (n < 0) n = -n;
        if (0 == n) return m;
        return GCD(n, m % n);
	}
	
	static Rational GCD(Rational x, Rational y) {
		
		long m = x.numerator   * y.denominator;
		long n = y.numerator   * x.denominator;
		long d = x.denominator * y.denominator;
		return new Rational(GCD(m, n), d);
	}

	// Returns Least Common Multiplier
	static long LCM(long m, long n) {
	
		if (n < 0) n = -n;
		if (m < 0) m = -m;
		// Parentheses need to avoid overflow
		return m * (n / GCD(m, n)); 
	}
	
	static Rational LCM(Rational x, Rational y) {
		return x.multiply(y.divide(GCD(x, y)));
	}

	// Returns addition of this + that
	public Rational add(long value) {
		return this.add(new Rational(value));
	}
	
	public Rational add(Rational that) {

		// special cases
		if (this.compareTo(ZERO) == 0) return that;
		if (that.compareTo(ZERO) == 0) return this;

		// find gcd of numerators and denominators
		long n = GCD(this.numerator,   that.numerator);
		long d = GCD(this.denominator, that.denominator);

		// add cross-product terms for numerator
		Rational sum = new Rational(
				(this.numerator / n) * (that.denominator / d) + (that.numerator / n) * (this.denominator / d),
				 this.denominator    * (that.denominator / d));
		
		// multiply back in
		sum.numerator *= n;
		return sum;
	}
	
	// Returns a new Rational (r.num + s.num) / (r.den + s.den)
    public static Rational mediant(Rational r, Rational s) {
        return new Rational(r.numerator + s.numerator, r.denominator + s.denominator);
    }
	
	// Returns new Rational half way between this and that
	public Rational mean(Rational that) {
		return this.add(that).divide(2);
	}

	// Returns this - that
	public Rational subtract(int value) {
		return this.subtract(new Rational(value));
	}
	
	public Rational subtract(Rational that) {
		return this.add(new Rational(-that.numerator, this.denominator));
	}
	
	// Returns multiplication of this * that
	public Rational multiply(int value) {
		return this.multiply(new Rational(value));
	}

	public Rational multiply(Rational that) {

		// reduce p1/q2 & p2/q1, then multiply, where this = p1/q1 & that = p2/q2
		Rational right = new Rational(this.numerator, that.denominator);
		Rational left  = new Rational(that.numerator, this.denominator);
		// return right * left
		return new Rational(right.numerator * left.numerator, right.denominator * left.denominator);
	}

	// Returns this / that
	public Rational divide(long value) {
		return this.divide(new Rational(value));
	}
	
	public Rational divide(Rational that) {
		return this.multiply(that.invert());
	}

	// Returns 1 / this
	public Rational invert() {
		return new Rational(denominator, numerator);
	}
	
	// Returns nth power of Rational
	public Rational pow(int n) {
		
		Rational R = this;
		R.numerator   = (long)Math.pow(R.numerator,   Math.abs(n));
		R.denominator = (long)Math.pow(R.denominator, Math.abs(n));
		R = new Rational(R);
		
		if (n == 0) return ONE;
		return (n > 0) ? R : R.invert();
	}

	// Returns list of Coefficients for Continued Fraction Expansion
	static ArrayList<Long> CFE(Double v, int terms) {
		
		if (terms <= 0) {
			throw new IllegalArgumentException("Invalid terms input");
		}
		
		long decimal;
		BigDecimal fractional;
		BigDecimal error = new BigDecimal("0.00000001");
		BigDecimal value = new BigDecimal(v.toString());
		ArrayList<Long> coefficients = new ArrayList<Long>();

		while (terms-- != 0) {
			
			decimal = value.longValue();                   //decimal part
			fractional = value.remainder(BigDecimal.ONE);  //fractional part
			coefficients.add(decimal);

			if (fractional.compareTo(error) <= 0)  
				break;

			value = BigDecimal.ONE.divide(fractional, 40, RoundingMode.HALF_UP);  // 1 / fractional
																							
		}
		
		return coefficients;
	}

	// Returns nearest Rational Approximations of Double with CFE method
	static Rational approximateCFE(Double v, int term) {
		
		// calculate cfe coefficients
		ArrayList<Long> coefficients = CFE(v, term);
		
		// check out of bounds
		int size = coefficients.size();
		if(term < size) size = term;
		
		Rational R = new Rational(coefficients.get(size - 1));
		for (int i = size - 2; i >= 0; i--) {
			R = R.invert();
			R = R.add(new Rational(coefficients.get(i)));
		}
		
		return new Rational(R);
	}

	// Returns nearest Rational Approximations of Double with Stern-Broco tree
	static Rational approximateSBT(Double x, int term) {
		
		//in order to round errors from double
		double digits = Math.pow(10, 10);   
		x = Math.round(x * digits) / digits;
		
		
		int num = (int)Math.floor(x);
		Rational left  = new Rational(num, 1);
		Rational right = new Rational(num + 1, 1);
		Rational best = left;
		double bestError = Math.abs(best.toDouble() - x);
		
		// do Stern-Brocot binary search
		while(term-- > 0 && bestError != 0) {
			
			// check if best has changed or not
			boolean flag = true;
		
			while(flag) {

				// compute next possible rational approximation
			    Rational mediant = Rational.mediant(left, right);
			    if (x < mediant.toDouble())
			       right = mediant;        // go left
			    else
			       left  = mediant;        // go right
			
			    // check if better and update the best
			    double error = Math.abs(mediant.toDouble() - x); 
			    if (error < bestError) {
			       best = mediant;
			       bestError = error;  
			       flag = false;
			       
			    }
			}
		}
		return best;
	}
			
}