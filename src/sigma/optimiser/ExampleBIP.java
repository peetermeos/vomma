/**
 * 
 */
package sigma.optimiser;

import com.joptimizer.exception.JOptimizerException;
import com.joptimizer.optimizers.BIPLokbaTableMethod;
import com.joptimizer.optimizers.BIPOptimizationRequest;

import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

/**
 * Test of optimisation framework 
 * 
 * @author Peeter Meos
 * @version 1.0
 *
 */
public class ExampleBIP {

	
	public void run() {
		DoubleFactory1D F1 = DoubleFactory1D.dense;
		DoubleFactory2D F2 = DoubleFactory2D.dense;
		
		DoubleMatrix1D c = F1.make(new double[] { 1, 4, 0, 4, 0, 0, 8, 6, 0, 4, 1, 4, 0, 4, 0, 0, 8, 6, 0, 4 });
		DoubleMatrix2D G = F2.make(new double[][] { 
				{ -3, -1, -4, -4, -1, -5, -4, -4, -1, -1, -3, -1, -4, -4, -1, -5, -4, -4, -1, -1 },
				{  0,  0, -3, -1, -5, -5, -5, -1,  0, 0, 0,  0, -3, -1, -5, -5, -5, -1,  0, 0 }, 
				{ -4, -1, -5, -2, -4, -3, -2, -4, -4, 0, -4, -1, -5, -2, -4, -3, -2, -4, -4, 0 },
				{ -3, -4, -3, -5, -3, -1, -4, -5, -1, -4, -3, -4, -3, -5, -3, -1, -4, -5, -1, -4 } });
		
		DoubleMatrix1D h = F1.make(new double[] { 2, -3, -4, -8 });
		
		BIPOptimizationRequest or = new BIPOptimizationRequest();
		or.setC(c);
		or.setG(G);
		or.setH(h);
		or.setDumpProblem(true);
		
		//optimization
		BIPLokbaTableMethod opt = new BIPLokbaTableMethod();
		opt.setBIPOptimizationRequest(or);
		
		try {
			opt.optimize();
		} catch (JOptimizerException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExampleBIP opt;
		
		opt = new ExampleBIP();
		opt.run();

	}

}
