package sigma.quant;

public class RationalCubic {
	private final static double DBL_EPSILON = 0.00000000001;
	private final static double DBL_MAX = Double.MAX_VALUE;
	private final static double DBL_MIN = Double.MIN_VALUE;
	
	static double minimum_rational_cubic_control_parameter_value = -(1 - Math.sqrt(DBL_EPSILON));
	static double maximum_rational_cubic_control_parameter_value = 2 / (DBL_EPSILON * DBL_EPSILON);
		  
	static boolean is_zero(double x){ return Math.abs(x) < DBL_MIN; }


	static double rational_cubic_interpolation(double x, double x_l, double x_r, double y_l, double y_r, double d_l, double d_r, double r) {
	   double h = (x_r - x_l);
	   if (Math.abs(h)<=0)
	      return 0.5 * (y_l + y_r);
	   // r should be greater than -1. We do not use  assert(r > -1)  here in order to allow values such as NaN to be propagated as they should.
	   double t = (x - x_l) / h;
	   
	   if ( ! (r >= maximum_rational_cubic_control_parameter_value) ) {
	      double t1 = (x - x_l) / h, omt = 1 - t, t2 = t * t, omt2 = omt * omt;
	      // Formula (2.4) divided by formula (2.5)
	      return (y_r * t2 * t1 + (r * y_r - h * d_r) * t2 * omt + (r * y_l + h * d_l) * t * omt2 + y_l * omt2 * omt) / (1 + (r - 3) * t1 * omt);
	   }
	   // Linear interpolation without over-or underflow.
	   return y_r * t + y_l * (1 - t);
	}

	static double rational_cubic_control_parameter_to_fit_second_derivative_at_left_side(double x_l, double x_r, double y_l, double y_r, double d_l, double d_r, double second_derivative_l) {
	   double h = (x_r-x_l), numerator = 0.5*h*second_derivative_l+(d_r-d_l);
	   if (is_zero(numerator))
	      return 0;
	   double denominator = (y_r-y_l)/h-d_l;
	   if (is_zero(denominator))
	      return numerator>0 ? maximum_rational_cubic_control_parameter_value : minimum_rational_cubic_control_parameter_value;
	   return numerator/denominator;
	}

	static double rational_cubic_control_parameter_to_fit_second_derivative_at_right_side(double x_l, double x_r, double y_l, double y_r, double d_l, double d_r, double second_derivative_r) {
	   double h = (x_r-x_l), numerator = 0.5*h*second_derivative_r+(d_r-d_l);
	   if (is_zero(numerator))
	      return 0;
	   double denominator = d_r-(y_r-y_l)/h;
	   if (is_zero(denominator))
	      return numerator>0 ? maximum_rational_cubic_control_parameter_value : minimum_rational_cubic_control_parameter_value;
	   return numerator/denominator;
	}

	static double minimum_rational_cubic_control_parameter(double d_l, double d_r, double s, boolean preferShapePreservationOverSmoothness) {
	   boolean monotonic = d_l * s >= 0 && d_r * s >= 0, convex = d_l <= s && s <= d_r, concave = d_l >= s && s >= d_r;
	   if (!monotonic && !convex && !concave) // If 3==r_non_shape_preserving_target, this means revert to standard cubic.
	      return minimum_rational_cubic_control_parameter_value;
	   double d_r_m_d_l = d_r - d_l, d_r_m_s = d_r - s, s_m_d_l = s - d_l;
	   double r1 = -DBL_MAX, r2 = r1;
	   // If monotonicity on this interval is possible, set r1 to satisfy the monotonicity condition (3.8).
	   if (monotonic){
	      if (!is_zero(s)) // (3.8), avoiding division by zero.
	         r1 = (d_r + d_l) / s; // (3.8)
	      else if (preferShapePreservationOverSmoothness) // If division by zero would occur, and shape preservation is preferred, set value to enforce linear interpolation.
	         r1 =  maximum_rational_cubic_control_parameter_value;  // This value enforces linear interpolation.
	   }
	   if (convex || concave) {
	      if (!(is_zero(s_m_d_l) || is_zero(d_r_m_s))) // (3.18), avoiding division by zero.
	         r2 = Math.max(Math.abs(d_r_m_d_l / d_r_m_s), Math.abs(d_r_m_d_l / s_m_d_l));
	      else if (preferShapePreservationOverSmoothness)
	         r2 = maximum_rational_cubic_control_parameter_value; // This value enforces linear interpolation.
	   } else if (monotonic && preferShapePreservationOverSmoothness)
	      r2 = maximum_rational_cubic_control_parameter_value; // This enforces linear interpolation along segments that are inconsistent with the slopes on the boundaries, e.g., a perfectly horizontal segment that has negative slopes on either edge.
	   return Math.max(minimum_rational_cubic_control_parameter_value, Math.max(r1, r2));
	}

	static double convex_rational_cubic_control_parameter_to_fit_second_derivative_at_left_side(double x_l, double x_r, double y_l, double y_r, double d_l, double d_r, double second_derivative_l, 
			boolean preferShapePreservationOverSmoothness) {
	   double r = rational_cubic_control_parameter_to_fit_second_derivative_at_left_side(x_l, x_r, y_l, y_r, d_l, d_r, second_derivative_l);
	   double r_min = minimum_rational_cubic_control_parameter(d_l, d_r, (y_r-y_l)/(x_r-x_l), preferShapePreservationOverSmoothness);
	   return Math.max(r,r_min);
	}

	static double convex_rational_cubic_control_parameter_to_fit_second_derivative_at_right_side(double x_l, double x_r, double y_l, double y_r, double d_l, double d_r, double second_derivative_r, 
			boolean preferShapePreservationOverSmoothness) {
	   double r = rational_cubic_control_parameter_to_fit_second_derivative_at_right_side(x_l, x_r, y_l, y_r, d_l, d_r, second_derivative_r);
	   double r_min = minimum_rational_cubic_control_parameter(d_l, d_r, (y_r-y_l)/(x_r-x_l), preferShapePreservationOverSmoothness);
	   return Math.max(r,r_min);
	}
}
