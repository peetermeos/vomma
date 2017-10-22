package sigma.trading;

import java.io.IOException;

import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.TickAttr;
import com.ib.client.Types.Action;

import sigma.utils.LogLevel;

/**
 * The idea is to trade when bid/ask is way off 
 * balance. Extends TWS connector
 * 
 * @author Peeter Meos
 * @version 0.1
 *
 */
public class DeltaTrader extends Connector {
	
	protected Portfolio port;
	protected Instrument inst;
	protected int threshold = 40;
	protected int q = 1;
	protected int pos = 0;
	
	protected boolean active = false;

	/**
	 * Standard constructor
	 */
	public DeltaTrader() {
		super("Delta Trader");
		
		inst = new Instrument("CL", "NYMEX", "FUT", "201711");
		port = new Portfolio();
		port.addInstrument(inst);
	}
	
	/**
	 * Main processing loop for the trader
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {
		Order o;
		Order pt;
		Order sl;
		
		Double tgt = 0.02;
		
		// Request data
		logger.log("Requesting instrument data");
		this.getClient().reqMktData(this.getValidId(), inst.getContract(), "", false, false, null);	
		
		logger.log("Entering main loop");
		// Endless loop while key is pressed
		while(System.in.available() == 0) {
			if ((inst.getAskSize() - inst.getBidSize() > threshold) &&
					(inst.getBid() > 0) &&
					(pos == 0)) {
				// go short
				
				this.getClient().reqIds(-1);
				
				inst.setId(this.getValidId());
				
				o = new Order();
				o.orderId(this.getValidId());
				o.action(Action.SELL);
				o.totalQuantity(q);
				o.orderType(OrderType.LMT);
				o.lmtPrice(inst.getBid());
		        o.transmit(false);
		        
		        pt = new Order();
		        pt.orderId(o.orderId() + 1);
		        pt.action("BUY");
		        pt.orderType("LMT");
		        pt.totalQuantity(q);
		        pt.lmtPrice(inst.getBid() - tgt);
		        pt.parentId(o.orderId());
		        pt.ocaGroup(this.name);
		        pt.transmit(false);
		        
		        sl = new Order();
		        sl.orderId(o.orderId() + 2);
		        sl.action("BUY");
		        sl.orderType(OrderType.STP_LMT);
		        sl.totalQuantity(q);
		        sl.lmtPrice(inst.getBid() + tgt);
		        sl.auxPrice(inst.getBid() + tgt);
		        sl.parentId(o.orderId());
		        sl.ocaGroup(this.name);
		        sl.transmit(true);
				
				
				logger.log("Placing sell order @" + inst.getBid());
				pos = pos - q;
				if (active) {
				//	this.getClient().placeOrder(getValidId(), inst.getContract(), o);
				//	this.getClient().placeOrder(getValidId() + 1, inst.getContract(), pt);
				//	this.getClient().placeOrder(getValidId() + 2, inst.getContract(), sl);
				}
			}
			
			if ((inst.getAskSize() - inst.getBidSize() < -threshold) &&
					(inst.getAsk() > 0) &&
					(pos == 0)) {
				// go long
				
				o = new Order();
				o.action(Action.BUY);
				o.totalQuantity(q);
				o.orderType(OrderType.LMT);
				o.lmtPrice(inst.getAsk());
		        o.transmit(false);
				
		        pt = new Order();
		        pt.orderId(o.orderId() + 1);
		        pt.action("SELL");
		        pt.orderType("LMT");
		        pt.totalQuantity(q);
		        pt.lmtPrice(inst.getAsk() + tgt);
		        pt.parentId(o.orderId());
		        pt.ocaGroup(this.name);
		        pt.transmit(false);
		        
		        sl = new Order();
		        sl.orderId(o.orderId() + 2);
		        sl.action("SELL");
		        sl.orderType(OrderType.STP_LMT);
		        sl.totalQuantity(q);
		        sl.lmtPrice(inst.getAsk() - tgt);
		        sl.auxPrice(inst.getAsk() - tgt);
		        sl.parentId(o.orderId());
		        sl.ocaGroup(this.name);
		        sl.transmit(true);
				
				
				logger.log("Placing buy order @" + inst.getAsk());
				pos = pos + q;
				if (active) {
				//	this.getClient().placeOrder(getValidId(), inst.getContract(), o);
				//	this.getClient().placeOrder(getValidId() + 1, inst.getContract(), pt);
				//	this.getClient().placeOrder(getValidId() + 2, inst.getContract(), sl);
				}
			}

		}
		logger.log("Done with trading, shutting down.");
	}
	
	/**
	 * Tick price processing needs to be overriden
	 */
	@Override
	public void tickPrice(int tickerId, int field, double price, TickAttr attribs) {
		logger.log("Tick Price. Ticker Id:" + tickerId + ", Field: " + field + 
				", Price: " + price);
			
		switch(field) {
		case 1: //bid
			inst.setBid(price);
			break;
		case 2: // ask
			inst.setAsk(price);
			break;
		case 4: //last
			inst.setSpot(price);
			break;
		default:
			break;
		}		
	}
	
	/**
	 * Tick size processing needs to be overriden
	 */
	@Override
	public void tickSize(int tickerId, int field, int size) {
		logger.verbose("Tick Size. Ticker Id:" + tickerId + ", Field: " + field + ", Size: " + size);		
		
		switch(field) {
		case 0: //bid
			inst.setBidSize(size);
			break;
		case 3: // ask
			inst.setAskSize(size);
			break;
		case 5: // last
			inst.setSpotSize(size);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Order execution details handling must be overriden
	 */
    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {

        logger.log("ExecDetails. " + reqId + " - [" + contract.symbol() + "], [" +
        		contract.secType() + "], [" + contract.currency() + "], [" +
        		execution.execId() + "], [" + execution.orderId() + "], [" + execution.shares() + "]");
        
        if ((execution.orderId() == inst.getId()) && (contract.symbol() == inst.getSymbol())) {
        	// Open position
        	logger.log("Position opened");
        	pos = execution.side() == "BUY" ? pos + q : pos - q;
        }
        
        if ((execution.orderId() == inst.getId() + 1) && (contract.symbol() == inst.getSymbol())) {
        	// Target reached
        	logger.log("Target reached");
        	pos = execution.side() == "BUY" ? pos + q : pos - q;
        	
        }
        
        if ((execution.orderId() == inst.getId() + 2) && (contract.symbol() == inst.getSymbol())) {
        	// Stop loss fired
        	logger.log("Stop loss fired");
        	pos = execution.side() == "BUY" ? pos + q : pos - q;
        }
    }

	
	/**
	 * Main entry point
	 * 
	 * @param args command line parameters
	 */
	public static void main(String[] args) {
		DeltaTrader trader;
		
		trader = new DeltaTrader();
		
		// Parse the arguments
		for(int i = 0; i < args.length; i++) {
			// Help
			if(args[i].compareTo("-h") == 0) {
				System.out.println("Help text.");
				System.exit(0);
			}
			
			// Verbose loglevel
			if(args[i].compareTo("-v") == 0) {
				trader.logger.setMyLogLevel(LogLevel.VERBOSE);
			}
		}
				
		trader.twsConnect();
		try {
			trader.run();
		} catch (IOException e) {
			trader.logger.error(e.toString());
		}
		trader.twsDisconnect();

	}

}
