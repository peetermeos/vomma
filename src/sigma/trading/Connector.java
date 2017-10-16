package sigma.trading;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;

import com.ib.client.Bar;
import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDescription;
import com.ib.client.ContractDetails;
import com.ib.client.DeltaNeutralContract;
import com.ib.client.DepthMktDataDescription;
import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;
import com.ib.client.EWrapper;
import com.ib.client.EWrapperMsgGenerator;
import com.ib.client.Execution;
import com.ib.client.FamilyCode;
import com.ib.client.HistogramEntry;
import com.ib.client.HistoricalTick;
import com.ib.client.HistoricalTickBidAsk;
import com.ib.client.HistoricalTickLast;
import com.ib.client.NewsProvider;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.PriceIncrement;
import com.ib.client.SoftDollarTier;
import com.ib.client.TickAttr;
import com.ib.client.TickType;

import sigma.utils.LogLevel;
import sigma.utils.Logger;

/**
 * Extension and implementation of TWS API for Sigma purposes
 * 
 * @author Peeter Meos
 * @version 0.1
 *
 */
public class Connector implements EWrapper {
	
	protected Logger logger;
	protected String name;
	
	// TWS stuff
	private EReaderSignal signal;
	private EClientSocket client;
	private EReader reader;
	private ProcessThread t;
	
	private int validId;
	
	/**
	 * Message processor class
	 * @author Peeter Meos
	 * @version 1.0
	 *
	 */
	
	private class ProcessThread extends Thread {
		public void run() {
    		while (getClient().isConnected()) { 
    			signal.waitForSignal(); 
    			try {
					reader.processMsgs();
				} catch (IOException e) {
					logger.error(e.toString());
				}
    		}
		}
	}
	
	/**
	 * Default constructor
	 */
	public Connector() {
		this("Sigma Trader");
	}
	
	/**
	 * Constructor with strategy name
	 * 
	 * @param name Strategy name
	 */
	public Connector(String name) {
		logger = new Logger(LogLevel.INFO, name);
		this.name = name;
		
		validId = -1;
		
		signal = new EJavaSignal();
		client = new EClientSocket(this, signal);

	};
	
	/**
	 * Default connection to TWS
	 * Uses localhost and port 4001
	 */
	public void twsConnect() {
		this.twsConnect("localhost", 4001);
	}
	
	/**
	 * Connection to TWS API at specified host and port
	 * 
	 * @param host hostname or IP
	 * @param port API port
	 */
	public void twsConnect(String host, int port) {
		// For getting random connection IDs
		Random rand = new Random();
		
		logger.log("Connecting to TWS API at " + host + ":" + port);
		getClient().eConnect(host, port, rand.nextInt(1000) + 1);
		
		while(! getClient().isConnected()) {}
		logger.log("Connection established");
		
		//Create a reader to consume messages from the TWS. The EReader will consume the incoming messages and put them in a queue
        reader = new EReader(getClient(), signal);
        reader.start();
        
        //Once the messages are in the queue, an additional thread can be created to fetch them
        t = new ProcessThread(); 
        t.start();		
	}
	
	/**
	 * Disconnects from the TWS
	 */
	public void twsDisconnect() {
		logger.log("Disconnecting from TWS");
		getClient().eDisconnect();
		t.interrupt();		
	}

	/**
	 * Account download end message processing.
	 */
	@Override
	public void accountDownloadEnd(String account) {
		logger.log("Account download finished: " + account + "\n");
	}

	/**
	 * Account summary processing
	 */
	@Override
	public void accountSummary(int reqId, String account, String tag, String value, String currency) {
		logger.log("Acct Summary. ReqId: " + reqId + ", Acct: " + account + ", Tag: " + tag + 
				", Value: " + value + ", Currency: " + currency);		
	}

	/**
	 * End of account summary message processing
	 */
	@Override
	public void accountSummaryEnd(int reqId) {
		logger.verbose("AccountSummaryEnd. Req Id: " + reqId + "\n");
		
	}

	@Override
	public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value, String currency) {
		logger.log("Account Update Multi. Request: " + reqId + ", Account: " + account + 
				", ModelCode: " + modelCode + ", Key: " + key + ", Value: " + value + ", Currency: " + currency );	
	}

	/**
	 * End of multi-account update processing
	 */
	@Override
	public void accountUpdateMultiEnd(int reqId) {
		logger.verbose("Account Update Multi End. Request: " + reqId + "\n");		
	}

	/**
	 * Bond contract details implementation.
	 */
	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		logger.log(EWrapperMsgGenerator.contractDetails(reqId, contractDetails));		
	}

	/**
	 * Commission report handling
	 */
    @Override
    public void commissionReport(CommissionReport commissionReport) {
        logger.verbose("CommissionReport. [" + commissionReport.m_execId + "] - [" + commissionReport.m_commission + 
        		"] [" + commissionReport.m_currency + "] RPNL [" + commissionReport.m_realizedPNL + "]");
    }

	/** 
	 * Connection acknowledgement
	 */
	@Override
	public void connectAck() {
		logger.verbose("Connection request received by TWS");
	}

	/** 
	 * Connection closure acknowledgement
	 */
	@Override
	public void connectionClosed() {
		logger.log("Connection closed by TWS");
		
	}

	/**
	 * Default contract details handling. 
	 */
	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
        logger.log(EWrapperMsgGenerator.contractDetails(reqId, contractDetails));
	}

	/**
	 * End of contract details request message
	 */
	@Override
	public void contractDetailsEnd(int reqId) {
		logger.verbose("End of contract details for request " + reqId);
		
	}

	/**
	 * Current time processing
	 * 
	 * @param time long timestamp
	 */
	@Override
	public void currentTime(long time) {
		logger.log("Current time is :" + time);	
	}

	/**
	 * Delta neutral validation processing
	 */
	@Override
	public void deltaNeutralValidation(int reqId, DeltaNeutralContract contract) {
		logger.verbose("Delta neutral validation request "  + reqId  + " response " + contract.toString());
	}

	/**
	 * A one-time response to querying the display groups.
	 */
	@Override
	public void displayGroupList(int reqId, String groups) {
		logger.verbose("Display groups for reqId " + reqId + ": " + groups);
	}

	/**
	 * Call triggered once after receiving the subscription request, 
	 * and will be sent again if the selected contract in the subscribed * 
	 * display group has changed.
	 */
	 @Override
	public void displayGroupUpdated(int reqId, String contractInfo ) {
		logger.verbose("Display group updated for request " + reqId + " contract " + contractInfo);
	}

	/**
	 * Error logging (exception)
	 * 
	 * @param e Exception to be logged
	 */
	@Override
	public void error(Exception e) {
		logger.error(e.toString());
	}

	/**
	 * Error logging (string)
	 * 
	 * @param e String containing error text
	 */
	@Override
	public void error(String e) {
		logger.error(e);		
	}

	/**
	 * Detailed error logging
	 * 
	 * @param id error ID
	 * @param errorCode error Code
	 * @param errorMsg String containing error text
	 */
	@Override
	public void error(int id, int errorCode, String errorMsg) {
		logger.error("Id: " + id + ", Code: " + errorCode + ", Msg: " + errorMsg);		
	}

	/**
	 * Order execution details handling
	 */
    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        logger.verbose("ExecDetails. " + reqId + " - [" + contract.symbol() + "], [" +
        		contract.secType() + "], [" + contract.currency() + "], [" +
        		execution.execId() + "], [" + execution.orderId() + "], [" + execution.shares() + "]");
    }

	/**
	 * End of execution details request.
	 */
	@Override
	public void execDetailsEnd(int reqId) {
		logger.verbose("End of execution details for request " + reqId);
		
	}

	/**
	 * Returns array of family codes
	 */
	@Override
	public void familyCodes(FamilyCode[] familyCodes) {
		for(FamilyCode i: familyCodes) {
			logger.verbose("Family code " + i.toString());
		}
	}

	/**
	 * Fundamental data handling
	 */
    @Override
    public void fundamentalData(int reqId, String data) {
        logger.verbose("FundamentalData. ReqId: [" + reqId + "] - Data: [" + data + "]");
    }

	/**
	 * Head timestamp processing
	 */
	@Override
	public void headTimestamp(int reqId, String headTimestamp) {
		logger.verbose("Head timestamp. Req Id: " + reqId + ", headTimestamp: " + headTimestamp);
	}

	/**
	 * Returns data histogram
	 */
	@Override
	public void histogramData(int reqId, List<HistogramEntry> data) {
		for(HistogramEntry i: data) {
			logger.log("Histogram entry for request " + reqId + " data " + i.toString());
		}
	}

	/**
	 * Historical data handling
	 * 
	 * @param reqId request ID
	 * @param bar Bar class for bar data
	 */
	@Override
	public void historicalData(int reqId, Bar bar) {
		logger.verbose("HistoricalData. " + reqId + " - Date: " + bar.time() + ", Open: " + bar.open() +
				", High: " + bar.high() + ", Low: " + bar.low() + ", Close: " + bar.close() +
				", Volume: " + bar.volume() + ", Count: " + bar.count() + ", WAP: " + bar.wap());
		
	}

	/**
	 * End of historical data message
	 */
	@Override
	public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {
		logger.verbose("HistoricalDataEnd. " + reqId + " - Start Date: " + startDateStr + ", End Date: " + endDateStr);
	}

	/**
	 * Update of historical data message
	 */
	@Override
	public void historicalDataUpdate(int reqId, Bar bar) {
		logger.verbose("HistoricalDataUpdate. " + reqId + " - Date: " + bar.time() + ", Open: " + bar.open() +
				", High: " + bar.high() + ", Low: " + bar.low() + ", Close: " + bar.close() + ", Volume: " + bar.volume() +
				", Count: " + bar.count() + ", WAP: " + bar.wap());	
	}

	/**
	 * Returns news headline
	 */
	@Override
	public void historicalNews(int reqId, String time, String providerCode, String articleId, String headline) {
		logger.verbose("News request " + reqId + " at time " + time +
				" provider " + providerCode + " article " + articleId +
				" headline " + headline);
	}

	/**
	 * Returns news headlines end marker
	 */
	@Override
	public void historicalNewsEnd(int reqId, boolean hasMore ) {
		logger.verbose("Historical news end for request " + reqId + " has more: " + hasMore);
	}

	/**
	 * Processing of historical tick data
	 */
	@Override
	public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done2) {
		for (HistoricalTick tick : ticks) {
            logger.verbose(EWrapperMsgGenerator.historicalTick(reqId, tick.time(), tick.price(), tick.size()));
		}
		
	}

	/**
	 * Processing of historical bid ask ticks
	 */
	@Override
	public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {
        for (HistoricalTickBidAsk tick : ticks) {
            logger.verbose(EWrapperMsgGenerator.historicalTickBidAsk(reqId, tick.time(), tick.mask(), tick.priceBid(), tick.priceAsk(), tick.sizeBid(),
                    tick.sizeAsk()));
        }		
	}

	/**
	 * Processing of historical last ticks
	 */
	@Override
	public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {
        for (HistoricalTickLast tick : ticks) {
            logger.verbose(EWrapperMsgGenerator.historicalTickLast(reqId, tick.time(), tick.mask(), tick.price(), tick.size(), tick.exchange(), 
                tick.specialConditions()));
        }
	}

	/**
	 * Receives a comma-separated string with the managed account IDs. 
	 * Occurs automatically on initial API client connection.
	 */
	@Override
	public void managedAccounts(String accountsList) {
		logger.verbose("Managed accounts " + accountsList);
	}

	/**
	 * Returns the market data type (real-time, frozen, delayed, delayed-frozen) of ticker 
	 * sent by EClientSocket::reqMktData when TWS switches from real-time to frozen and back 
	 * and from delayed to delayed-frozen and back.
	 */
	@Override
	public void marketDataType(int reqId, int marketDataType ) {
		logger.verbose("Request " + reqId + " market data type " + marketDataType);
	}

	/**
	 * Returns minimum price increment structure for a particular market rule ID 
	 * market rule IDs for an instrument on valid exchanges can be obtained from the 
	 * contractDetails object for that contract
	 */
	@Override
	public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {
		for(PriceIncrement i: priceIncrements) {
			logger.verbose("Market rule id " + marketRuleId +
					" price increment " + i.toString());
		}
	}

	/**
	 * Called when receives Depth Market Data Descriptions
	 */
	@Override
	public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {
		for(DepthMktDataDescription i: depthMktDataDescriptions)
			logger.verbose("Depth market data description " + i.toString());
	}

	/**
	 * News article retrieval
	 */
	@Override
	public void newsArticle(int reqId, int articleType, String articleText) {
		logger.verbose("News article request " + reqId + " for article type " + articleType +
				" containing " + articleText);
	}

	/**
	 * News provider listing.
	 */
	@Override
	public void newsProviders(NewsProvider[] newsProviders)		
    {
		logger.verbose("News providers:");
		for(NewsProvider i : newsProviders) {
			logger.verbose(i.toString());
		}
	}

	/**
	 * Update next valid ID
	 * 
	 * @param nextId containing next valid ID
	 */
	@Override
	public void nextValidId(int nextId) {
		logger.verbose("Next valid ID received");
		this.validId = nextId;	
	}

	/**
	 * Open order processing
	 */
  	@Override
    public void openOrder(int orderId, Contract contract, Order order,
            OrderState orderState) {
        logger.verbose("OpenOrder. ID: " + orderId + ", " + contract.symbol() + ", "+contract.secType() +
        		" @ " + contract.exchange() + ": " +
        		order.action() + ", " + order.orderType() + " " + order.totalQuantity() +
        		", " + orderState.status());
    }

	/**
	 * Open orders listing end.
	 */
	@Override
	public void openOrderEnd() {
		logger.verbose("End of open orders.");
		
	}

	/**
	 * Order status processing
	 */
    @Override
    public void orderStatus(int orderId, String status, double filled,
            double remaining, double avgFillPrice, int permId, int parentId,
            double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
        logger.verbose("OrderStatus. Id: " + orderId + ", Status: " + status + ", Filled" + filled +
        		", Remaining: " + remaining + ", AvgFillPrice: " + avgFillPrice + ", PermId: " + permId +
        		", ParentId: " + parentId + ", LastFillPrice: " + lastFillPrice +
                ", ClientId: " + clientId + ", WhyHeld: " + whyHeld + ", MktCapPrice: " + mktCapPrice);
    }

    /**
     * receives PnL updates in real time for the daily PnL and the total unrealized PnL for an account
     */
	@Override
	public void pnl(int reqId, double dailyPnL, double unrealizedPnL) {
		logger.verbose("Pnl for request " + reqId + " daily PnL " + dailyPnL +
				" unrealised PnL " + unrealizedPnL);
		
	}

	/**
	 * Receives real time updates for single position daily PnL values
	 */
	@Override
	public void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double value) {
		logger.verbose("Pnl for position " + pos + " : daily PnL " + dailyPnL +
				" unrealised PnL " + unrealizedPnL + " value " + value);	
	}

	/**
	 * Position reporting processing
	 */
	@Override
	public void position(String account, Contract contract, double pos, double avgCost) {
		logger.log("Position. " + account + " - Symbol: " + contract.symbol() + 
				", SecType: " + contract.getSecType() + ", Currency: " + contract.currency() +
				", Position: " + pos + ", Avg cost: " + avgCost);		
	}

	/**
	 * End of position reporting processing
	 */
	@Override
	public void positionEnd() {
		logger.log("PositionEnd");		
	}

	/**
	 * Multi position reporting.
	 */
	@Override
	public void positionMulti(int reqId, String account, String modelCode,
            Contract contract, double pos, double avgCost) {
		logger.log("Position Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " + modelCode + ", Symbol: " + contract.symbol() + 
				", SecType: " + contract.secType() + ", Currency: " + contract.currency() + ", Position: " + pos + ", Avg cost: " + avgCost);		
	}

	/**
	 * End of multi position reporting
	 */
	@Override
	public void positionMultiEnd(int reqId) {
		logger.log("Position Multi End. Request: " + reqId);		
	}

	/**
	 * Real time bar handling
	 */
    @Override
    public void realtimeBar(int reqId, long time, double open, double high,
            double low, double close, long volume, double wap, int count) {
        logger.verbose("RealTimeBars. " + reqId + " - Time: " + time + ", Open: " + open + ", High: " + high + 
        		", Low: " + low + ", Close: " + close + ", Volume: " + volume + 
        		", Count: " + count + ", WAP: " + wap);
    }

    /**
     * Financial Advisor message handling
     */
    @Override
    public void receiveFA(int faDataType, String xml) {
        logger.verbose("Receiving FA: " + faDataType + " - " + xml);
    }

    /**
     * Returns conId and exchange for CFD market data request re-route
     */
	@Override
	public void rerouteMktDataReq(int reqId, int conId, String exchange) {
		logger.verbose("Reroute request " + reqId + " for connection " + conId + " exchange " + exchange );		
	}

	/**
	 * Returns the conId and exchange for an underlying contract when a request is made for level 2 
	 * data for an instrument which does not have data in IB's database. 
	 * For example stock CFDs and index CFDs.
	 */
	@Override
	public void rerouteMktDepthReq(int reqId, int conId, String exchange) {
		logger.verbose("Reroute mkt depth request " + reqId + " for connection " + conId + " exchange " + exchange);
	}

	/**
	 * Scanner data processing
	 */
    @Override
    public void scannerData(int reqId, int rank,
            ContractDetails contractDetails, String distance, String benchmark,
            String projection, String legsStr) {
        logger.verbose("ScannerData. " + reqId + " - Rank: " + rank + ", Symbol: " + contractDetails.contract().symbol() + 
        		", SecType: " + contractDetails.contract().secType() + ", Currency: " + contractDetails.contract().currency() +
                ", Distance: " + distance + ", Benchmark: " + benchmark + ", Projection: " + projection + ", Legs String: " + legsStr);
    }

	/**
	 * End of scanner data message.
	 */
	@Override
	public void scannerDataEnd(int reqId) {
		logger.verbose("End of scanner data for reqId " + reqId);
		
	}

	/**
	 * Scanner parameters processing
	 */
	@Override
	public void scannerParameters(String xml) {
		logger.verbose("Scanner parameters " + xml);
	}

	/**
	 * Additional security parameter processing.
	 * Useful for option chains.
	 */
	@Override
	public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId, String tradingClass, String multiplier,
			Set<String> expirations, Set<Double> strikes) {
		logger.verbose("Additional parameters for reqId " + reqId +
				" exchange : " + exchange +
				" underlying contract id : " + underlyingConId +
				" trading class : " + tradingClass +
				" multiplier : " + multiplier);
		for(String i: expirations) {
			logger.verbose("Expiration " + i);
		}
		for(Double i: strikes) {
			logger.verbose("Strike " + i);
		}		
	}

	/**
	 * End of message for optional security parameter processing.
	 */
	@Override
	public void securityDefinitionOptionalParameterEnd(int reqId) {
		logger.verbose("End of optional security parameters for reques " + reqId);
	}

	/**
	 * Bit number to exchange + exchange abbreviation dictionary
	 */
	@Override
	public void smartComponents(int reqId, Map<Integer, Entry<String, Character>> theMap) {
		logger.verbose("Smart components for reqId" + reqId + " received");
		logger.verbose(theMap.toString());
	}

	/**
	 * Soft dollar tiers reporting implementation
	 */
	@Override
	public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
		logger.verbose("Soft dollar tiers for reqId " + reqId);
		for(SoftDollarTier i: tiers)
			logger.verbose(i.toString());		
	}

	/**
	 * Symbol samples' processing
	 */
	@Override
	public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {
		logger.verbose("Symbol samples for reqId " + reqId);
		for(ContractDescription i: contractDescriptions)
			logger.verbose(i.toString());	
	}

	/**
	 * Exchange for Physicals tick processing
	 */
	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, 
			int holdDays, String futureLastTradeDate, double dividendImpact,
			double dividendsToLastTradeDate ) {
		logger.verbose("EFP tick received. Ticker ID " + tickerId);
	}

	/**
	 * Generic tick processing
	 */
	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		logger.verbose("Tick Generic. Ticker Id:" + tickerId + ", Field: " + TickType.getField(tickType) + ", Value: " + value);	
	}

	/** 
	 * News tick processing
	 */
	@Override
	public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline, String extraData) {
		logger.log("Tick News. TickerId: " + tickerId + ", TimeStamp: " + timeStamp + 
				", ProviderCode: " + providerCode + ", ArticleId: " + articleId + 
				", Headline: " + headline + ", ExtraData: " + extraData + "\n");
		
	}

	/**
	 * Option greeks message processing
	 */
    @Override
    public void tickOptionComputation(int tickerId, int field,
            double impliedVol, double delta, double optPrice,
            double pvDividend, double gamma, double vega, double theta,
            double undPrice) {
        logger.verbose("TickOptionComputation. TickerId: " + tickerId + ", field: " + field + 
        		", ImpliedVolatility: " + impliedVol + ", Delta: " + delta +
                ", OptionPrice: " + optPrice + ", pvDividend: " + pvDividend +
                ", Gamma: " + gamma + ", Vega: " + vega + ", Theta: " + theta +
                ", UnderlyingPrice: " + undPrice);
    }

	/**
	 * Tick price processing
	 */
	@Override
	public void tickPrice(int tickerId, int field, double price, TickAttr attribs) {
		logger.verbose("Tick price. Ticker Id:" + tickerId + ", Field: " + field + 
				", Price: " + price + ", CanAutoExecute: " +  attribs.canAutoExecute() +
                ", pastLimit: " + attribs.pastLimit() + ", pre-open: " + attribs.preOpen());	
	}

	/**
	 * Exchange component mapping info
	 */
	@Override
	public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
		logger.log("Tick req params. Ticker Id:" + tickerId + ", Min tick: " + minTick + 
				", bbo exchange: " + bboExchange + ", Snapshot permissions: " + snapshotPermissions);
	}

	/**
	 * Tick size processing
	 */
	@Override
	public void tickSize(int tickerId, int field, int size) {
		logger.verbose("Tick Size. Ticker Id:" + tickerId + ", Field: " + field + ", Size: " + size);		
	}

	/**
	 * Tick snapshot processing end
	 */
	@Override
	public void tickSnapshotEnd(int tickerId) {
		logger.log("End of tick snapshot for ticker id: " + tickerId);
	}

	/**
	 * Tick string processing
	 */
	@Override
	public void tickString(int tickerId, int tickType, String value) {
		logger.verbose("Tick string. Ticker Id:" + tickerId + ", Type: " + tickType + ", Value: " + value);
	}

	/**
	 * Time update
	 */
	@Override
	public void updateAccountTime(String timestamp) {
		logger.log("UpdateAccountTime. Time: " + timestamp+"\n");
	}

	/**
	 * Account value update
	 */
	@Override
	public void updateAccountValue(String key, String value, String currency, String accountName) {
		logger.log("UpdateAccountValue. Key: " + key + ", Value: " + value + ", Currency: " + currency + ", AccountName: " + accountName);		
	}

	/**
	 * Update market depth handling
	 */
	@Override
    public void updateMktDepth(int tickerId, int position, int operation,
            int side, double price, int size) {
        logger.verbose("UpdateMarketDepth. " + tickerId + " - Position: " + position + 
        		", Operation: " + operation + ", Side: " + side +
        		", Price: " + price + ", Size: " + size + "");
    }

	/**
	 * Market L2 depth handling
	 */
    @Override
    public void updateMktDepthL2(int tickerId, int position,
            String marketMaker, int operation, int side, double price, int size) {
        logger.verbose("UpdateMarketDepthL2. " + tickerId + " - Position: " + position + 
        		", Operation: " + operation + ", Side: " + side + 
        		", Price: " + price + ", Size: " + size + "");
    }

    /**
     * News bulletin update handling
     */
    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message,
            String origExchange) {
       logger.verbose("News Bulletins. " + msgId + " - Type: " + msgType + 
    		   ", Message: " + message + ", Exchange of Origin: " + origExchange);
    }

	/**
	 * Portfolio update
	 */
	@Override
	public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue, double averageCost, 
			double unrealizedPNL, double realizedPNL, String accountName) {
		logger.log("UpdatePortfolio. "+ contract.symbol() + ", " + contract.getSecType() + " @ "+contract.exchange()
                + ": Position: " + position+", MarketPrice: " + marketPrice+", MarketValue: " + marketValue+", AverageCost: " + averageCost
                + ", UnrealizedPNL: " + unrealizedPNL + ", RealizedPNL: " + realizedPNL+", AccountName: " + accountName);
	}

	/**
	 * Verify and auth completion message
	 */
	@Override
	public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
		logger.verbose("Verify and auth completed : " + isSuccessful + " error text : " + errorText);	
	}

	/**
	 * Verify and Authenticate message API implementation
	 */
	@Override
	public void verifyAndAuthMessageAPI(String apiData, String xyzResponse) {
		logger.verbose("Verify and auth message API data :" + apiData + " response : " + xyzResponse);
		
	}

	/**
	 * Verify completed implementation
	 */
	@Override
	public void verifyCompleted(boolean isSuccessful, String errorText) {
		logger.verbose("Verify completed " + isSuccessful + "  error text " + errorText);	
	}

	/**
	 * Verify message API implementation
	 * @param apiData API data string
	 */
	@Override
	public void verifyMessageAPI(String apiData) {
		logger.verbose(apiData);
	}

	/**
	 * @return the validId
	 */
	public int getValidId() {
		return validId;
	}

	/**
	 * @return the client
	 */
	public EClientSocket getClient() {
		return client;
	}

}
