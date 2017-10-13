package sigma.trading;

import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.TickAttr;
import com.ib.client.Types.Action;

/**
 * The idea is to trade when bid/ask is way off 
 * balance.
 * 
 * @author Peeter Meos
 * @version 0.1
 *
 */
public class DeltaTrader extends Connector {
	
	protected Instrument inst;
	protected int threshold = 150;
	protected int q = 1;
	
	protected boolean active = false;

	public DeltaTrader() {
		super();
		
		inst = new Instrument("CL", "NYMEX", "FUT", "201711");
	}
	
	public void run() {
		int pos = 0;
		Order o;
		
		// Request data
		this.getClient().reqMktData(this.getValidId(), inst.getContract(), "", false, false, null);	
		
		// Endless loop
		while(Math.abs(q) < 10) {
			if ((inst.getAskSize() - inst.getBidSize() > threshold) &&
					(inst.getBid() > 0) &&
					(pos == 0)) {
				// go short
				pos = pos - q;
				
				o = new Order();
				o.action(Action.SELL);
				o.totalQuantity(q);
				o.orderType(OrderType.LMT);
				o.lmtPrice(inst.getBid());
				
				logger.log("Placing sell order");
				if (active)
					this.getClient().placeOrder(getValidId(), inst.getContract(), o);
			}
			
			if ((inst.getAskSize() - inst.getBidSize() < -threshold) &&
					(inst.getAsk() > 0) &&
					(pos == 0)) {
				// go long
				pos = pos + q;
				
				o = new Order();
				o.action(Action.BUY);
				o.totalQuantity(q);
				o.orderType(OrderType.LMT);
				o.lmtPrice(inst.getAsk());
				
				logger.log("Placing buy order");
				if (active) 
					this.getClient().placeOrder(getValidId(), inst.getContract(), o);
			}

		}
	}
	
	/**
	 * Tick price processing needs to be overriden
	 */
	@Override
	public void tickPrice(int tickerId, int field, double price, TickAttr attribs) {
		logger.log("Tick Price. Ticker Id:" + tickerId + ", Field: " + field + 
				", Price: " + price + ", CanAutoExecute: " +  attribs.canAutoExecute() +
                ", pastLimit: " + attribs.pastLimit() + ", pre-open: " + attribs.preOpen());
			
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
	
	public static void main(String[] args) {
		DeltaTrader trader;
		
		trader = new DeltaTrader();
		trader.twsConnect();
		trader.run();
		trader.twsDisconnect();

	}

}
