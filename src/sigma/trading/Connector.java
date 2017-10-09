package sigma.trading;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
    		while (client.isConnected()) { 
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
		logger = new Logger();
		
		validId = -1;
		
		signal = new EJavaSignal();
		client = new EClientSocket(this, signal);
	}
	
	/**
	 * Default connection to TWS
	 * Uses localhost and port 4001
	 */
	public void twsConnect() {
		logger.log("Connecting to TWS API");
		client.eConnect("localhost", 4001, 1);
		
		while(! client.isConnected()) {}
		logger.log("Connection established");
		
		//Create a reader to consume messages from the TWS. The EReader will consume the incoming messages and put them in a queue
        reader = new EReader(client, signal);
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
		client.eDisconnect();
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
		logger.log("AccountSummaryEnd. Req Id: " + reqId + "\n");
		
	}

	@Override
	public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value, String currency) {
		logger.log("Account Update Multi. Request: " + reqId + ", Account: " + account + 
				", ModelCode: " + modelCode + ", Key: " + key + ", Value: " + value + ", Currency: " + currency + "\n");	
	}

	/**
	 * End of multi-account update processing
	 */
	@Override
	public void accountUpdateMultiEnd(int reqId) {
		logger.log("Account Update Multi End. Request: " + reqId + "\n");		
	}

	@Override
	public void bondContractDetails(int arg0, ContractDetails arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commissionReport(CommissionReport arg0) {
		// TODO Auto-generated method stub
		
	}

	/** 
	 * Connection acknowledgement
	 */
	@Override
	public void connectAck() {
		logger.log("Connection request received by TWS");
	}

	/** 
	 * Connection closure acknowledgement
	 */
	@Override
	public void connectionClosed() {
		logger.log("Connection closed by TWS");
		
	}

	@Override
	public void contractDetails(int arg0, ContractDetails arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contractDetailsEnd(int arg0) {
		// TODO Auto-generated method stub
		
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

	@Override
	public void deltaNeutralValidation(int arg0, DeltaNeutralContract arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void displayGroupList(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void displayGroupUpdated(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
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
		logger.error("Id: " + id + ", Code: " + errorCode + ", Msg: " + errorMsg + "\n");		
	}

	@Override
	public void execDetails(int arg0, Contract arg1, Execution arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execDetailsEnd(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void familyCodes(FamilyCode[] arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fundamentalData(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void headTimestamp(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void histogramData(int arg0, List<HistogramEntry> arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalData(int arg0, Bar arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalDataEnd(int arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalDataUpdate(int arg0, Bar arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalNews(int arg0, String arg1, String arg2, String arg3, String arg4) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalNewsEnd(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalTicks(int arg0, List<HistoricalTick> arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalTicksBidAsk(int arg0, List<HistoricalTickBidAsk> arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalTicksLast(int arg0, List<HistoricalTickLast> arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void managedAccounts(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void marketDataType(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void marketRule(int arg0, PriceIncrement[] arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mktDepthExchanges(DepthMktDataDescription[] arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newsArticle(int arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newsProviders(NewsProvider[] arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Update next valid ID
	 * 
	 * @param nextId containing next valid ID
	 */
	@Override
	public void nextValidId(int nextId) {
		this.validId = nextId;	
	}

	@Override
	public void openOrder(int arg0, Contract arg1, Order arg2, OrderState arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openOrderEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void orderStatus(int arg0, String arg1, double arg2, double arg3, double arg4, int arg5, int arg6,
			double arg7, int arg8, String arg9, double arg10) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pnl(int arg0, double arg1, double arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pnlSingle(int arg0, int arg1, double arg2, double arg3, double arg4) {
		// TODO Auto-generated method stub
		
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

	@Override
	public void realtimeBar(int arg0, long arg1, double arg2, double arg3, double arg4, double arg5, long arg6,
			double arg7, int arg8) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveFA(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rerouteMktDataReq(int arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rerouteMktDepthReq(int arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scannerData(int arg0, int arg1, ContractDetails arg2, String arg3, String arg4, String arg5,
			String arg6) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scannerDataEnd(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scannerParameters(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void securityDefinitionOptionalParameter(int arg0, String arg1, int arg2, String arg3, String arg4,
			Set<String> arg5, Set<Double> arg6) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void securityDefinitionOptionalParameterEnd(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void smartComponents(int arg0, Map<Integer, Entry<String, Character>> arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void softDollarTiers(int arg0, SoftDollarTier[] arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void symbolSamples(int arg0, ContractDescription[] arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickEFP(int arg0, int arg1, double arg2, String arg3, double arg4, int arg5, String arg6, double arg7,
			double arg8) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Generic tick processing
	 */
	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		logger.log("Tick Generic. Ticker Id:" + tickerId + ", Field: " + TickType.getField(tickType) + ", Value: " + value);	
	}

	@Override
	public void tickNews(int arg0, long arg1, String arg2, String arg3, String arg4, String arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickOptionComputation(int arg0, int arg1, double arg2, double arg3, double arg4, double arg5,
			double arg6, double arg7, double arg8, double arg9) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Tick price processing
	 */
	@Override
	public void tickPrice(int tickerId, int field, double price, TickAttr attribs) {
		logger.log("Tick Price. Ticker Id:" + tickerId + ", Field: " + field + 
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
		logger.log("Tick Size. Ticker Id:" + tickerId + ", Field: " + field + ", Size: " + size);		
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
		logger.log("Tick string. Ticker Id:" + tickerId + ", Type: " + tickType + ", Value: " + value);
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

	@Override
	public void updateMktDepth(int arg0, int arg1, int arg2, int arg3, double arg4, int arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMktDepthL2(int arg0, int arg1, String arg2, int arg3, int arg4, double arg5, int arg6) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateNewsBulletin(int arg0, int arg1, String arg2, String arg3) {
		// TODO Auto-generated method stub
		
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

	@Override
	public void verifyAndAuthCompleted(boolean arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verifyAndAuthMessageAPI(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verifyCompleted(boolean arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verifyMessageAPI(String arg0) {
		// TODO Auto-generated method stub
		
	}


}
