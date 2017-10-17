package sigma.optimiser;

import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;

import com.joptimizer.exception.JOptimizerException;
import com.joptimizer.optimizers.BIPLokbaTableMethod;
import com.joptimizer.optimizers.BIPOptimizationRequest;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;

/**
 * General BIP minimisation
 * 
 * @author Peeter Meos
 * @version 0.1
 *
 */
public class MinimiseBIP {

	Double[] c;
	ArrayList<Double[]> A;
	ArrayList<Double[]> G;
	ArrayList<Double> b;
	ArrayList<Double> h;
	
	BIPLokbaTableMethod opt;
	BIPOptimizationRequest or;
	
	public MinimiseBIP() {
		A = new ArrayList<>();
		G = new ArrayList<>();
		b = new ArrayList<>();
		h = new ArrayList<>();
		
		or = new BIPOptimizationRequest();
	}
	
	/**
	 * Creates coefficient vector for the objective function
	 * 
	 * @param c Double[] vector of the objective function coefficients
	 */
	public void addObjCoefficients(Double[] c) {
		this.c = new Double[c.length];
		
		this.c = c;
	}
	
	/**
	 * Adds equality constraint with the respective RHS value
	 * 
	 * @param A vector of constraint coefficients
	 * @param b RHS value
	 */
	public void addEqConstraint(Double[] A, Double b) {
		this.A.add(A);
		this.b.add(b);
	}
	
	/**
	 * Adds inequality constraint with the respective RHS value
	 * The constraint is in form of G <= h
	 * Therefore pay attention to the signs!
	 * 
	 * @param G vector of constraint coefficients
	 * @param h RHS value
	 */
	public void addIneqConstraint(Double[] G, Double h) {
		this.G.add(G);
		this.h.add(h);
	}
	
	/**
	 * Generate optimisation formulation and solve
	 */
	public void optimise() {	
		DoubleFactory1D F1 = DoubleFactory1D.dense;
		DoubleFactory2D F2 = DoubleFactory2D.dense;
		
		BIPOptimizationRequest or = new BIPOptimizationRequest();
		or.setC(F1.make(ArrayUtils.toPrimitive(this.c)));
		
		// TODO This double[][] conversion will probably not work!
		or.setG(F2.make((double[][]) ArrayUtils.toPrimitive(G.toArray(new Double[this.c.length][G.size()]))));
		or.setH(F1.make(ArrayUtils.toPrimitive(h.toArray(new Double[h.size()]))));
		
		// TODO This double[][] conversion will probably not work!
		or.setA(F2.make((double[][]) ArrayUtils.toPrimitive(A.toArray(new Double[this.c.length][A.size()]))));
		or.setB(F1.make(ArrayUtils.toPrimitive(b.toArray(new Double[b.size()]))));

		
		or.setDumpProblem(true);
		
		//	optimization
		opt = new BIPLokbaTableMethod();
		opt.setBIPOptimizationRequest(or);
		try {
			opt.optimize();
		} catch (JOptimizerException e) {
			e.printStackTrace();
		}
	}
}
