
public class LogMath {
	
	public static Double LOGZERO = Double.NaN;
	
	public static Double exp(Double x) {
		if (Double.isNaN(x)) {
			return 0.0;
		}
		
		return Math.pow(2, x);
	}
	
	public static Double log(Double x) {
		if (x == 0) {
			return Double.NaN;
		} else if (x > 0) {
			return log_2(x);
		} else {
			throw new IllegalArgumentException("log of negative number is imaginary");
		}
	}
	
	public static Double sum(Double... vals) {
		double sum = vals[0];
		for (int i = 1; i < vals.length; i++) {
			sum = sum_helper(sum, vals[i]);
		}
		return sum;
	}
	
	public static Double product(Double... vals) {
		double product = vals[0];
		for (int i = 1; i < vals.length; i++) {
			product = product_helper(product, vals[i]);
		}
		return product;
	}
	
	private static Double sum_helper(Double log_x, Double log_y) {
		if (Double.isNaN(log_x)) {
			return log_y;
		} else if (Double.isNaN(log_y)) {
			return log_x;
		}
		
		if (log_x > log_y) {
			return log_x + log(1 + exp(log_y - log_x));
		} else {
			return log_y + log(1 + exp(log_x - log_y)); 
		}
	}
	
	private static Double product_helper(Double log_x, Double log_y) {
		if (Double.isNaN(log_x) || Double.isNaN(log_y)) {
			return Double.NaN;
		}
		
		return log_x + log_y;
	}
	
	
	private static Double log_2(Double x) {
		return Math.log(x) / Math.log(2);
	}

}
