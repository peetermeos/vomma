package sigma.optimiser;

import java.util.ArrayList;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

import sigma.quant.Option;
import sigma.utils.Logger;

/**
 * Optimiser class
 * 
 * @author Peeter Meos
 * @version 0.1
 */
public class Optimiser {
	// Logger, perhaps its better to pass a reference or something 
	protected Logger logger;
	
	// Row counter for constraint matrix
	protected int row;
	
	protected glp_prob lp;
    protected SWIGTYPE_p_int ind;
    protected SWIGTYPE_p_double val;
	protected int ret;
	
	// Option data that is used for optimisation
	protected ArrayList<Option> data;

	/** 
	 * Simple constructor that initialises the logger
	 */
	public Optimiser() {
		logger = new Logger();
		row = 0;
		
        // Create problem
        lp = GLPK.glp_create_prob();
        logger.log("Problem created");
	}
	
	/**
	 * Loads data for optimisation
	 * 
	 * @param d Data structure
	 */
	public void loadData(ArrayList<Option> d) {    
		this.data = d;
		
        // Allocate memory
        ind = GLPK.new_intArray(data.size());
        val = GLPK.new_doubleArray(data.size());
	}
	
	/**
	 * Free memory on shutdown
	 */
	public void shutdown() {
		// Free memory
        GLPK.glp_delete_prob(lp);
	}
	
	/**
	 * Objective function for theta maximisation
	 * 
	 * @param maxPos maximum quantity for one contract
	 */
	public void objMaximiseTheta(int maxPos) {
        GLPK.glp_set_prob_name(lp, "maxTheta");

        // Define objective
        GLPK.glp_set_obj_name(lp, "z");
        GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
        GLPK.glp_set_obj_coef(lp, 0, 0.0);
        
        // Define columns one for every portfolio entry
        // Add objective function coefficients
        GLPK.glp_add_cols(lp, data.size());
        for(int i = 0; i < data.size(); i++) {
        	GLPK.glp_set_col_name(lp, i + 1, "x" + data.get(i).getId());
            GLPK.glp_set_col_kind(lp, i + 1, GLPKConstants.GLP_IV);
            GLPK.glp_set_col_bnds(lp, i + 1, GLPKConstants.GLP_DB, 0, maxPos);
            
            // Thetas to the objective function
            GLPK.glp_set_obj_coef(lp, i + 1, -data.get(i).theta()); 
        }
	}
	
	/**
	 * Objective function for gamma minimisation
	 * 
	 * @param Maximum quantity allowed for one contract
	 */
	public void objMinimiseGamma(int maxPos) {
        // Create problem
        lp = GLPK.glp_create_prob();
        //logger.log("Problem created");
        GLPK.glp_set_prob_name(lp, "minGamma");

        // Define objective
        GLPK.glp_set_obj_name(lp, "z");
        GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
        GLPK.glp_set_obj_coef(lp, 0, 1.);
      
        // Define columns one for every portfolio entry
        // Add objective function coefficients
        GLPK.glp_add_cols(lp, data.size());
        for(int i = 0; i < data.size(); i++) {
        	GLPK.glp_set_col_name(lp, i + 1, "x" + data.get(i).getId());
            GLPK.glp_set_col_kind(lp, i + 1, GLPKConstants.GLP_IV);
            GLPK.glp_set_col_bnds(lp, i + 1, GLPKConstants.GLP_DB, 0, maxPos);
            
            // Gammas to the objective function
            GLPK.glp_set_obj_coef(lp, i + 1, data.get(i).gamma());
        }
	}
	
	/**
	 * Adds limit to absolute cumulative delta allowed
	 * 
	 * @param limit
	 */
	protected void constrDelta(Double limit) {
		// Create rows
        GLPK.glp_add_rows(lp, 2);
        row++;
        
        // Bounded delta
        GLPK.glp_set_row_name(lp, row, "delta_up");
        GLPK.glp_set_row_name(lp, row + 1, "delta_dn");
        GLPK.glp_set_row_bnds(lp, row, GLPKConstants.GLP_UP, .0, limit);
        GLPK.glp_set_row_bnds(lp, row + 1, GLPKConstants.GLP_LO, -limit, .0);
        for (int i = 1; i <= data.size(); i++) {
        	GLPK.intArray_setitem(ind, i, i);
        	GLPK.doubleArray_setitem(val, i, -data.get(i - 1).delta());
        }           
        GLPK.glp_set_mat_row(lp, row, data.size(), ind, val);
        GLPK.glp_set_mat_row(lp, row + 1, data.size(), ind, val);
        
        row++;
	}
	
	/**
	 * Adds upper bound constraint to cumulative gamma
	 * 
	 * @param limit
	 */
	protected void constrGamma(double limit) {
		// Create rows
        GLPK.glp_add_rows(lp, 1);
        row++;
        
        // Bounded gamma
        GLPK.glp_set_row_name(lp, row, "gamma");
        GLPK.glp_set_row_bnds(lp, row, GLPKConstants.GLP_UP, 0.0 , limit);
        for (int i = 1; i <= data.size(); i++) {
        	GLPK.intArray_setitem(ind, i, i);
        	GLPK.doubleArray_setitem(val, i, data.get(i - 1).gamma());
        }
        GLPK.glp_set_mat_row(lp, row, data.size(), ind, val);
	}
	
	/**
	 * Adds constraint to bound theta. Minimum level allowed.
	 * @param limit
	 */
	protected void constrTheta(double limit) {
		// Create rows
        GLPK.glp_add_rows(lp, 1);
        row++;
        
        // Bounded theta
        GLPK.glp_set_row_name(lp, row, "theta");
        GLPK.glp_set_row_bnds(lp, row, GLPKConstants.GLP_LO, limit * 365, 0.0);
        for (int i = 1; i <= data.size(); i++) {
        	GLPK.intArray_setitem(ind, i, i);
        	GLPK.doubleArray_setitem(val, i, -data.get(i - 1).theta());
        }
        GLPK.glp_set_mat_row(lp, row, data.size(), ind, val);
	}
	
	/**
	 * Max allowed positions that can be opened.
	 * 
	 * @param maxSize
	 */
	protected void constrMaxPortfolio(int maxSize) {
		// Create rows
        GLPK.glp_add_rows(lp, 1);
        
        // Max positions open
        GLPK.glp_set_row_name(lp, row, "open positions");
        GLPK.glp_set_row_bnds(lp, row, GLPKConstants.GLP_UP, 0, maxSize);
        for (int i = 1; i <= data.size(); i++) {
        	GLPK.intArray_setitem(ind, i, i);
        	GLPK.doubleArray_setitem(val, i, 1.0);
        }           
        GLPK.glp_set_mat_row(lp, row, data.size(), ind, val);
	}
	
	/**
	 * Solves the problem
	 * 
	 * @param mip boolean MIP flag (solves LP relaxation if false)
	 */
	public void solve(Boolean mip) {
        // Free memory
        GLPK.delete_intArray(ind);
        GLPK.delete_doubleArray(val);
        
        if (!mip) {
            // Solve model as LP
            glp_smcp parm = new glp_smcp();
            parm.setMsg_lev(GLPKConstants.GLP_MSG_ALL);
            GLPK.glp_init_smcp(parm);
            ret = GLPK.glp_simplex(lp, parm);
        } else {
            // Solve model as MIP
            glp_iocp parm = new glp_iocp();
            GLPK.glp_init_iocp(parm);
            parm.setMsg_lev(GLPKConstants.GLP_MSG_ALL);
            parm.setPresolve(GLPKConstants.GLP_ON);
            GLPK.glp_write_lp(lp, null, "yi.lp");
            ret = GLPK.glp_intopt(lp, parm);
        }
	}
	
	/**
	 * Outputs LP solution
	 * 
	 * @param lp Solved LP problem
	 */
	public void writeLpSolution() {
        int i;
        int n;
        String name;
        double val;

        logger.log("LP solution output");
        
        name = GLPK.glp_get_obj_name(lp);
        val = GLPK.glp_get_obj_val(lp);
        logger.log(name + " = " + val);

        n = GLPK.glp_get_num_cols(lp);
        for (i = 1; i <= n; i++) {
            name = GLPK.glp_get_col_name(lp, i);
            val = GLPK.glp_get_col_prim(lp, i);
            logger.log(name + " = " + val);
        }
    }
	
	/**
	 * Returns solution data
	 * @return ArrayList consisting optimal option portfolio
	 */
	public ArrayList<Option> returnSolution() {
		int n;
		double val;
		
		val = GLPK.glp_get_obj_val(lp);
        n = GLPK.glp_get_num_cols(lp);
        for (int i = 1; i <= n; i++) {       	
        	data.get(i - 1).setPos(-val);
        }
        
		return(data);
	}
}
