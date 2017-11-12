package sigma.optimiser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EWrapperMsgGenerator;
import com.ib.client.TickAttr;

import sigma.quant.OptSide;
import sigma.quant.Option;
import sigma.trading.Connector;
import sigma.trading.Instrument;

/**
 * Option portfolio optimisation class
 * Main sequence
 * - connect to tws
 * - get current portfolio
 * - get market data for the underlyings
 * - get market data for the option chain
 * - calculate greeks
 * - populate optimisation model
 * - optimise
 * - output results
 * - optionally create orders to balance portfolio
 * - disconnect from the tws 
 *  
 * @author Peeter Meos
 * @version 0.1
 *
 */
public class OptimisePortfolio extends Connector {
	
	protected Instrument inst;
	protected ArrayList<Option> portfolio;
	
	private Boolean done;
		
	/**
	 * Standard constructor
	 */
	public OptimisePortfolio() {
		super();
		
		Double[] strikeArray = {45.0 ,50.0, 55.0};
		String[] expiryArray = {"201801", "201802"};
		
		portfolio = new ArrayList<>();
		
		// Add placeholders for options
		for(Double k: strikeArray)
			for(String exp: expiryArray) {
				portfolio.add(new Option("CL", "NYMEX", OptSide.CALL, k, exp));
				portfolio.add(new Option("CL", "NYMEX", OptSide.PUT, k, exp));
			}
	}
	
	/**
	 * Default contract details handling must be overriden 
	 */
	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
        logger.verbose(EWrapperMsgGenerator.contractDetails(reqId, contractDetails));
        // Find matching entry in option surface
        // and populate local symbol and last trading date
        if(reqId < 1000) {
        	portfolio.get(reqId).setExpiry(contractDetails.contract().lastTradeDateOrContractMonth());
        }
	}
	
	/**
	 * Request and receive option surface
	 */
	public void getSurface() {
		Boolean allDone = false;
		
		if(this.getClient().isConnected() == false) {
			this.logger.error("Not connected");
			return;
		}
		
		// Loop through portfolio entries and request contract info
		logger.log("Requesting option surface");
		for(int i=0; i < portfolio.size(); i++) {
			portfolio.get(i).setId(i);
			this.getClient().reqContractDetails(i, portfolio.get(i).getContract());
		}
		
		// Wait until we have the data for the entire surface
		/*
		while(!allDone) {
			allDone = true;
			for(Option o: portfolio) {
				if(o.getExpiry() == "") {
					allDone = false;
				}
			}
		}
		*/
		
		// Request market data for options
		logger.log("Request market data for options");
		for(int i = 0; i < portfolio.size(); i++) {
			this.getClient().reqMktData(1000 + i,  portfolio.get(i).getContract(), "", true, false, null);
		}
		
		// Done with options, now underlyings
		logger.log("Done with options, now underlyings");
		ArrayList<String> used = new ArrayList<>();
		Contract c;
		int ulId = 2000;
		
		for(int i=0; i < portfolio.size(); i++) {
			allDone = false;
			c = portfolio.get(i).getUnderlying();
			
			// Find if we have already requested data for that instrument
			for(int s=0; s<used.size(); s++) {
				if(used.get(s).compareTo(c.lastTradeDateOrContractMonth()) == 0) {
					allDone = true;
					portfolio.get(i).getUl().setId(2000 + s);
				}
			}
			
			// If we have not requested data for that one
			if (!allDone) {
				used.add(portfolio.get(i).getUnderlying().lastTradeDateOrContractMonth());
				portfolio.get(i).getUl().setId(ulId);
				this.getClient().reqMktData(ulId, c, "", true, false, null);
				ulId++;
			}
		}
	}
	
	/**
	 * Prints out the option surface
	 */
	public void printSurface() {
		for (Option o: portfolio) {
			logger.log(o.getSymbol() + " " + 
		               o.getExpiry() + " " + 
		               o.getStrike() + " " +
					   o.getSide().toString() + 
					   " price: " + o.getPrice() +
					   " ul: " + o.getUl().getPrice() +
					   " vol: " + o.getSigma()
		               );
		}
	}
	
	/**
	 * Retrieves portfolio from TWS
	 */
	public void getCurrentPortfolio() {
		this.done = false;
		
		// Request positions
		logger.log("Getting positions");
		this.getClient().reqPositions();
		
		// Wait until all is received
		while(! this.done) {}
		
		// And we're done
		logger.log("Done retrieving portfolio");
		this.getClient().cancelPositions();
	}
	
	/**
	 * Position reporting processing
	 */
	@Override
	public void position(String account, Contract contract, double pos, double avgCost) {
		Instrument i;
		
		logger.log("Position. " + account + 
				" - Symbol: " + contract.symbol() + 
				", SecType: " + contract.getSecType() + 
				", Currency: " + contract.currency() +
				", Position: " + pos + 
				", Avg cost: " + avgCost);
		
		i = new Instrument(contract.symbol(), 
				contract.exchange(), 
				contract.secType().toString(), 
				contract.lastTradeDateOrContractMonth());
		
		i.setPos(Math.round(pos));
		//portfolio.add(i);
	}
	
	/**
	 * Tick price processing override
	 */
	@Override
	public void tickPrice(int tickerId, int field, double price, TickAttr attribs) {
		logger.log("Tick price. Ticker Id:" + tickerId + ", Field: " + field + 
				", Price: " + price + ", CanAutoExecute: " +  attribs.canAutoExecute() +
                ", pastLimit: " + attribs.pastLimit() + ", pre-open: " + attribs.preOpen());
		
		// Process option prices
		if((tickerId >= 1000) && (tickerId < 2000)) {
			switch(field) {
			case 1: // bid
				portfolio.get(tickerId - 1000).setBid(price);
				break;
			case 2: // ask
				portfolio.get(tickerId - 1000).setAsk(price);
				break;
			case 4: // last
			case 9: // close	
				portfolio.get(tickerId - 1000).setPrice(price);
				break;
			default:
				break;
			}
		}
		
		// Process underlying prices
		if(tickerId >= 2000) {
			switch(field) {
			case 1: // bid
				for(Option o: portfolio)
					if(o.getUl().getId() == tickerId)
						o.getUl().setBid(price);
				break;
			case 2: // ask
				for(Option o: portfolio)
					if(o.getUl().getId() == tickerId)
						o.getUl().setAsk(price);
				break;
			case 4: // last
			case 9: // close
				for(Option o: portfolio)
					if(o.getUl().getId() == tickerId)
						o.getUl().setPrice(price);
				break;
			default:
				break;
			}			
		}
	}

	/**
	 * End of position reporting processing
	 */
	@Override
	public void positionEnd() {
		this.done = true;	
	}
	
	/**
	 * Calculates option chain volatilities
	 */
	public void calcVol() {
		for (Option o: portfolio) {
			
			logger.log("Calculating implied volatility for instrument " + o.getId());
			o.calcVol();
		}
	}
	
	/** 
	 * Main optimisation routine
	 */
	public void optimise() {
        glp_prob lp;
        glp_iocp parm;
        //glp_smcp parm;
        SWIGTYPE_p_int ind;
        SWIGTYPE_p_double val;
        int ret;
        
        try {
            // Create problem
            lp = GLPK.glp_create_prob();
            System.out.println("Problem created");
            GLPK.glp_set_prob_name(lp, "myProblem");

            // Define columns
            GLPK.glp_add_cols(lp, 3);
            GLPK.glp_set_col_name(lp, 1, "x1");
            GLPK.glp_set_col_kind(lp, 1, GLPKConstants.GLP_IV);
            GLPK.glp_set_col_bnds(lp, 1, GLPKConstants.GLP_DB, 0, 1);
            GLPK.glp_set_col_name(lp, 2, "x2");
            GLPK.glp_set_col_kind(lp, 2, GLPKConstants.GLP_CV);
            GLPK.glp_set_col_bnds(lp, 2, GLPKConstants.GLP_DB, 0, 3);
            GLPK.glp_set_col_name(lp, 3, "x3");
            GLPK.glp_set_col_kind(lp, 3, GLPKConstants.GLP_CV);
            GLPK.glp_set_col_bnds(lp, 3, GLPKConstants.GLP_DB, 0, 1);

            // Create constraints

            // Allocate memory
            ind = GLPK.new_intArray(3);
            val = GLPK.new_doubleArray(3);

            // Create rows
            GLPK.glp_add_rows(lp, 2);

            // Set row details
            GLPK.glp_set_row_name(lp, 1, "c1");
            GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_DB, 0, 0.2);
            GLPK.intArray_setitem(ind, 1, 1);
            GLPK.intArray_setitem(ind, 2, 2);
            GLPK.doubleArray_setitem(val, 1, 1.);
            GLPK.doubleArray_setitem(val, 2, -.5);
            GLPK.glp_set_mat_row(lp, 1, 2, ind, val);

            GLPK.glp_set_row_name(lp, 2, "c2");
            GLPK.glp_set_row_bnds(lp, 2, GLPKConstants.GLP_UP, 0, 0.4);
            GLPK.intArray_setitem(ind, 1, 2);
            GLPK.intArray_setitem(ind, 2, 3);
            GLPK.doubleArray_setitem(val, 1, -1.);
            GLPK.doubleArray_setitem(val, 2, 1.);
            GLPK.glp_set_mat_row(lp, 2, 2, ind, val);

            // Free memory
            GLPK.delete_intArray(ind);
            GLPK.delete_doubleArray(val);

            // Define objective
            GLPK.glp_set_obj_name(lp, "z");
            GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
            GLPK.glp_set_obj_coef(lp, 0, 1.);
            GLPK.glp_set_obj_coef(lp, 1, -.5);
            GLPK.glp_set_obj_coef(lp, 2, .5);
            GLPK.glp_set_obj_coef(lp, 3, -1);

            // Write model to file
            // GLPK.glp_write_lp(lp, null, "lp.lp");

            // Solve model
            //parm = new glp_smcp();
            //GLPK.glp_init_smcp(parm);
            //ret = GLPK.glp_simplex(lp, parm);
            
            parm = new glp_iocp();
            GLPK.glp_init_iocp(parm);
            parm.setPresolve(GLPKConstants.GLP_ON);
            //  GLPK.glp_write_lp(lp, null, "yi.lp");
            ret = GLPK.glp_intopt(lp, parm);

            // Retrieve solution
            if (ret == 0) {
               writeLpSolution(lp);
            } else {
                logger.error("The problem could not be solved");
            }

            // Free memory
            GLPK.glp_delete_prob(lp);
        } catch (GlpkException e) {
            logger.error(e.toString());
        }
	}
	
	/**
	 * Prints out the LP solution
	 *  
	 * @param lp
	 */
	public void writeLpSolution(glp_prob lp) {
        int i;
        int n;
        String name;
        double val;

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
	 * Saves a portfolio snapshot to a file
	 * 
	 * @param fname Filename string
	 * @throws IOException
	 */
	public void savePortfolio(String fname) throws IOException {
		FileOutputStream fos;
		ObjectOutputStream oos;

		logger.log("Saving portfolio to " + fname);
		fos = new FileOutputStream(fname);
		oos = new ObjectOutputStream(fos);
		oos.writeObject(this.portfolio);
		oos.close();
		fos.close();
	}
	
	/**
	 * Loads saved portfolio from a file
	 * 
	 * @param fname Filename string
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public void loadPortfolio(String fname) throws ClassNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(fname);
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		if (ois.readObject() instanceof ArrayList) {
			portfolio = (ArrayList<Option>) ois.readObject();	
		}
		
		ois.close();
		fis.close();
	}
	
	/**
	 * Main entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		OptimisePortfolio o;
		
		o = new OptimisePortfolio();
		
		o.twsConnect();
		o.getSurface();
		try {
			while(System.in.available() == 0) {}
		} catch (IOException e) {
			o.logger.error(e.toString());
		}
		
		// Calculate volatilities
		o.calcVol();
		
		// For debugging print out the surface
		o.printSurface();
		
		// Create optimisation problem and optimise
		o.optimise();
		
		// And we are done with TWS
		o.twsDisconnect();
	}

}
