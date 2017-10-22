package sigma.quant;

public class NormalDistribution {
	private final static double DBL_EPSILON = 0.00000000001;
	private final static double DBL_MAX = Double.MAX_VALUE;
	
	private final static double norm_cdf_asymptotic_expansion_first_threshold = -10.0;
	private final static double norm_cdf_asymptotic_expansion_second_threshold = -1/Math.sqrt(DBL_EPSILON);

	
	static final double ONE_OVER_SQRT_TWO     = 0.7071067811865475244008443621048490392848359376887;
	static final double ONE_OVER_SQRT_TWO_PI  = 0.3989422804014326779399460599343818684758586311649;
	static final double SQRT_TWO_PI           = 2.506628274631000502415765284811045253006986740610;
	
	static double norm_pdf(double x){ return ONE_OVER_SQRT_TWO_PI*Math.exp(-.5*x*x); }
	
	static double norm_cdf(double z){
		   if (z <= norm_cdf_asymptotic_expansion_first_threshold) {
		      // Asymptotic expansion for very negative z following (26.2.12) on page 408
		      // in M. Abramowitz and A. Stegun, Pocketbook of Mathematical Functions, ISBN 3-87144818-4.
			   
		      double sum = 1;
		      if (z >= norm_cdf_asymptotic_expansion_second_threshold) {
		         double zsqr = z * z, i = 1, g = 1, x, y, a = DBL_MAX, lasta;
		         do {
		            lasta = a;
		            x = (4 * i - 3) / zsqr;
		            y = x * ((4 * i - 1) / zsqr);
		            a = g * (x - y);
		            sum -= a;
		            g *= y;
		            ++i;
		            a = Math.abs(a);
		         } while (lasta > a && a >= Math.abs(sum * DBL_EPSILON));
		      }
		      return -norm_pdf(z) * sum / z;
		   }
		   return 0.5* ErfCody.erfc_cody( -z* ONE_OVER_SQRT_TWO );
		}
	
	static double inverse_norm_cdf(double u){
		   //
		   // ALGORITHM AS241  APPL. STATIST. (1988) VOL. 37, NO. 3
		   //
		   // Produces the normal deviate Z corresponding to a given lower
		   // tail area of u; Z is accurate to about 1 part in 10**16.
		   // see http://lib.stat.cmu.edu/apstat/241
		   //
		   double split1 = 0.425;
		   double split2 = 5.0;
		   double const1 = 0.180625;
		   double const2 = 1.6;

		   // Coefficients for P close to 0.5
		   double A0 = 3.3871328727963666080E0;
		   double A1 = 1.3314166789178437745E+2;
		   double A2 = 1.9715909503065514427E+3;
		   double A3 = 1.3731693765509461125E+4;
		   double A4 = 4.5921953931549871457E+4;
		   double A5 = 6.7265770927008700853E+4;
		   double A6 = 3.3430575583588128105E+4;
		   double A7 = 2.5090809287301226727E+3;
		   double B1 = 4.2313330701600911252E+1;
		   double B2 = 6.8718700749205790830E+2;
		   double B3 = 5.3941960214247511077E+3;
		   double B4 = 2.1213794301586595867E+4;
		   double B5 = 3.9307895800092710610E+4;
		   double B6 = 2.8729085735721942674E+4;
		   double B7 = 5.2264952788528545610E+3;
		   // Coefficients for P not close to 0, 0.5 or 1.
		   double C0 = 1.42343711074968357734E0;
		   double C1 = 4.63033784615654529590E0;
		   double C2 = 5.76949722146069140550E0;
		   double C3 = 3.64784832476320460504E0;
		   double C4 = 1.27045825245236838258E0;
		   double C5 = 2.41780725177450611770E-1;
		   double C6 = 2.27238449892691845833E-2;
		   double C7 = 7.74545014278341407640E-4;
		   double D1 = 2.05319162663775882187E0;
		   double D2 = 1.67638483018380384940E0;
		   double D3 = 6.89767334985100004550E-1;
		   double D4 = 1.48103976427480074590E-1;
		   double D5 = 1.51986665636164571966E-2;
		   double D6 = 5.47593808499534494600E-4;
		   double D7 = 1.05075007164441684324E-9;
		   // Coefficients for P very close to 0 or 1
		   double E0 = 6.65790464350110377720E0;
		   double E1 = 5.46378491116411436990E0;
		   double E2 = 1.78482653991729133580E0;
		   double E3 = 2.96560571828504891230E-1;
		   double E4 = 2.65321895265761230930E-2;
		   double E5 = 1.24266094738807843860E-3;
		   double E6 = 2.71155556874348757815E-5;
		   double E7 = 2.01033439929228813265E-7;
		   double F1 = 5.99832206555887937690E-1;
		   double F2 = 1.36929880922735805310E-1;
		   double F3 = 1.48753612908506148525E-2;
		   double F4 = 7.86869131145613259100E-4;
		   double F5 = 1.84631831751005468180E-5;
		   double F6 = 1.42151175831644588870E-7;
		   double F7 = 2.04426310338993978564E-15;

		   if (u<=0)
		      return Math.log(u);
		   if (u>=1)
		      return Math.log(1-u);

		   double q = u-0.5;
		   if (Math.abs(q) <= split1)
		   {
		      double r = const1 - q*q;
		      return q * (((((((A7 * r + A6) * r + A5) * r + A4) * r + A3) * r + A2) * r + A1) * r + A0) /
		         (((((((B7 * r + B6) * r + B5) * r + B4) * r + B3) * r + B2) * r + B1) * r + 1.0);
		   }
		   else
		   {
		      double r = q<0.0 ? u : 1.0-u;
		      r = Math.sqrt(-Math.log(r));
		      double ret;
		      if (r < split2)
		      {
		         r = r - const2;
		         ret = (((((((C7 * r + C6) * r + C5) * r + C4) * r + C3) * r + C2) * r + C1) * r + C0) /
		            (((((((D7 * r + D6) * r + D5) * r + D4) * r + D3) * r + D2) * r + D1) * r + 1.0);
		      }
		      else
		      {
		         r = r - split2;
		         ret = (((((((E7 * r + E6) * r + E5) * r + E4) * r + E3) * r + E2) * r + E1) * r + E0) /
		            (((((((F7 * r + F6) * r + F5) * r + F4) * r + F3) * r + F2) * r + F1) * r + 1.0);
		      }
		      return q<0.0 ? -ret : ret;
		   }
		}
	
}
