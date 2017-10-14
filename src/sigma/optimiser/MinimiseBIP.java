package sigma.optimiser;

import com.joptimizer.exception.JOptimizerException;
import com.joptimizer.optimizers.BIPLokbaTableMethod;
import com.joptimizer.optimizers.BIPOptimizationRequest;

/**
 * General BIP minimisation
 * 
 * @author Peeter Meos
 * @version 0.2
 *
 */
public class MinimiseBIP {

	BIPLokbaTableMethod opt;
	BIPOptimizationRequest or;
	
	
	public void setup() {
		
	}
	
	public void optimise() {
		or = new BIPOptimizationRequest();
		
		//	optimization
		opt = new BIPLokbaTableMethod();
		opt.setBIPOptimizationRequest(or);
		try {
			opt.optimize();
		} catch (JOptimizerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void results() {
		
	}
}
