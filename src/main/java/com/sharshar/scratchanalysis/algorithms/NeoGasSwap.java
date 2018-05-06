package com.sharshar.scratchanalysis.algorithms;

import com.sharshar.scratchanalysis.beans.PriceData;
import com.sharshar.scratchanalysis.repository.PriceDataES;
import com.sharshar.scratchanalysis.utils.ScratchException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Algorithm to test if swapping between NEO and BTC and GAS gives better profits
 *
 * Created by lsharshar on 4/29/2018.
 */
@Service
public class NeoGasSwap {
	Logger logger = LogManager.getLogger();

	public class TradeAction {
		Date tradeDate;
		double gasPrice;
		double neoPrice;

		// True if the trade is to buy, false if it is to sell
		boolean buy;

		public TradeAction(Date tradeDate, double gasPrice, double neoPrice, boolean buy) {
			this.tradeDate = tradeDate;
			this.gasPrice = gasPrice;
			this.neoPrice = neoPrice;
			this.buy = buy;
		}

		public double getGasPrice() {
			return this.gasPrice;
		}

		public double getNeoPrice() {
			return this.neoPrice;
		}

		public boolean isBuy() {
			return buy;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.buy ? "BUY  : " : "SELL : ")
					.append(tradeDate)
					.append(" ")
					.append(gasPrice);
			return sb.toString();
		}
	}

	public class TradeSummary {
		double initialInvestment;
		double initialNeoPrice;
		double finalNeoPrice;
		double initialGasPrice;
		double finalGasPrice;
		double initialBtcPrice;
		double finalBtcPrice;
		Date initialTime;
		Date finalTime;
		List<TradeAction> trades;

		public double getInitialInvestment() {
			return initialInvestment;
		}

		public TradeSummary setInitialInvestment(double initialInvestment) {
			this.initialInvestment = initialInvestment;
			return this;
		}

		public double getInitialNeoPrice() {
			return initialNeoPrice;
		}

		public TradeSummary setInitialNeoPrice(double initialNeoPrice) {
			this.initialNeoPrice = initialNeoPrice;
			return this;
		}

		public double getFinalNeoPrice() {
			return finalNeoPrice;
		}

		public TradeSummary setFinalNeoPrice(double finalNeoPrice) {
			this.finalNeoPrice = finalNeoPrice;
			return this;
		}

		public double getInitialGasPrice() {
			return initialGasPrice;
		}

		public TradeSummary setInitialGasPrice(double initialGasPrice) {
			this.initialGasPrice = initialGasPrice;
			return this;
		}

		public double getFinalGasPrice() {
			return finalGasPrice;
		}

		public TradeSummary setFinalGasPrice(double finalGasPrice) {
			this.finalGasPrice = finalGasPrice;
			return this;
		}

		public double getGasIncreasePercentage() {
			return AnalysisUtils.getPercentageChange(initialGasPrice, finalGasPrice);
		}

		public double getNeoIncreasePercentage() {
			return AnalysisUtils.getPercentageChange(initialNeoPrice, finalNeoPrice);
		}

		public double getBtcIncreasePercentage() {
			return AnalysisUtils.getPercentageChange(initialBtcPrice, finalBtcPrice);
		}

		public List<TradeAction> getTrades() {
			return trades;
		}

		public TradeSummary setTrades(List<TradeAction> trades) {
			this.trades = trades;
			return this;
		}

		public double getInitialBtcPrice() {
			return initialBtcPrice;
		}

		public TradeSummary setInitialBtcPrice(double initialBtcPrice) {
			this.initialBtcPrice = initialBtcPrice;
			return this;
		}

		public double getFinalBtcPrice() {
			return finalBtcPrice;
		}

		public TradeSummary setFinalBtcPrice(double finalBtcPrice) {
			this.finalBtcPrice = finalBtcPrice;
			return this;
		}

		public Date getInitialTime() {
			return initialTime;
		}

		public TradeSummary setInitialTime(Date initialTime) {
			this.initialTime = initialTime;
			return this;
		}

		public Date getFinalTime() {
			return finalTime;
		}

		public TradeSummary setFinalTime(Date finalTime) {
			this.finalTime = finalTime;
			return this;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			String format = "%.5f";
			String format2 = "%.2f";
			builder
					.append("Date Range: ").append(startDate).append(" - ").append(finalTime).append("\n")
					.append("Initial Investment (BTC): ").append(initialInvestment).append("\n")
					.append("NEO Price: ").append(String.format(format, initialNeoPrice))
					.append(" - ").append(String.format(format, finalNeoPrice))
					.append(" (").append(String.format(format, (finalNeoPrice - initialNeoPrice)))
					.append(" - ").append(String.format(format2, getNeoIncreasePercentage() * 100))
					.append("%)\n")
					.append("GAS Price: ").append(String.format(format, initialGasPrice))
					.append(" - ").append(String.format(format, finalGasPrice))
					.append(" (").append(String.format(format, (finalGasPrice - initialGasPrice)))
					.append(" - ").append(String.format(format2, getGasIncreasePercentage() * 100))
					.append("%)\n")
					.append("BTC Price: ").append(String.format(format, initialBtcPrice))
					.append(" - ").append(String.format(format, finalBtcPrice))
					.append(" (").append(String.format(format, (finalBtcPrice - initialBtcPrice)))
					.append(" - ").append(String.format(format2, getBtcIncreasePercentage() * 100))
					.append("%)\n");
			if (trades != null) {
				builder.append("\nTrades: \n").append(trades.toString());
			}
			return builder.toString();
		}

	}

	private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

	@Autowired
	private PriceDataES priceDataEs;

	@Value("${startDate}")
	private String startDate;

	@Value("${endDate}")
	private String endDate;

	@Value("${exchange}")
	private short exchange;

	@Value("${interval}")
	private long interval;

	@Value("${initialInvestment}")
	private double initialInvestment;

	@Value("${lowGasRatio}")
	private double lowGasRatio;

	@Value("${highGasRatio}")
	private double highGasRatio;

	private List<TradeAction> tradeActions;


	public TradeSummary getProfitUsingGasToBtc(PriceDataES dataES) throws ParseException {
		int numberOfItemsToAverage = 5;
		TradeSummary summary = new TradeSummary().setInitialInvestment(initialInvestment);
		tradeActions = new ArrayList<>();
		Date sDate = sdf.parse(startDate);
		Date eDate = sdf.parse(endDate);
		try {
			// Assume we haven't bought anything yet
			boolean bought = false;
			// Save the last data that had price data in it so we can use it to specify the final prices, dates
			List<PriceData> lastNeoPd = null;
			List<PriceData> lastGasPd = null;
			AnalysisUtils.DateRange finalDateFound = null;

			// Split over 4 hours and iterate
			List<AnalysisUtils.DateRange> dateRanges = AnalysisUtils.splitDates(sDate, eDate, 1000 * 60 * 60 * 4);
			for (AnalysisUtils.DateRange dateRange : dateRanges) {
				// Load the data
				List<PriceData> neoPd = dataES.findByTimeRange("NEOBTC", dateRange.getStartDate(), dateRange.getEndDate(), exchange);
				List<PriceData> gasPd = dataES.findByTimeRange("GASBTC", dateRange.getStartDate(), dateRange.getEndDate(), exchange);
				// If this is the first item with data, store it as the first data
				if (summary.getInitialTime() == null && !neoPd.isEmpty()) {
					// We only need BTC prices for the start and the end so do it here
					List<PriceData> btcPd = dataES.findByTimeRange("BTCUSDT", dateRange.getStartDate(), dateRange.getEndDate(), exchange);
					// Set all the initial price/time values
					summary.setInitialTime(AnalysisUtils.getFirst(neoPd).getUpdateTime());
					summary.setInitialGasPrice(AnalysisUtils.getMean(AnalysisUtils.getFirstX(gasPd, numberOfItemsToAverage)));
					summary.setInitialNeoPrice(AnalysisUtils.getMean(AnalysisUtils.getFirstX(neoPd, numberOfItemsToAverage)));
					summary.setInitialBtcPrice(AnalysisUtils.getMean(AnalysisUtils.getFirstX(btcPd, numberOfItemsToAverage)));
				}
				// Set it to the last date range - eventually it will be true when we drop out of the for loop
				if (!neoPd.isEmpty()) {
					finalDateFound = dateRange;
					lastNeoPd = neoPd;
					lastGasPd = gasPd;
				}
				// Determine the ratio and then buy or sell (you can only buy if you've sold, you can only sell if you've bought)
				if (!gasPd.isEmpty()) {
					bought = getTradeActions(neoPd, gasPd, bought, lowGasRatio, highGasRatio, tradeActions);
				}
			}
			// We're done with iterating over the date ranges, get the BTC data from the last, non-empty date range
			if (finalDateFound != null) {
				List<PriceData> btcPd = dataES.findByTimeRange("BTCUSDT", finalDateFound.getStartDate(), finalDateFound.getEndDate(), exchange);
				// Set all the "final" data
				summary.setFinalBtcPrice(AnalysisUtils.getMean(AnalysisUtils.getLastX(btcPd, numberOfItemsToAverage)))
						.setFinalNeoPrice(AnalysisUtils.getMean(AnalysisUtils.getLastX(lastNeoPd, numberOfItemsToAverage)))
						.setFinalGasPrice(AnalysisUtils.getMean(AnalysisUtils.getLastX(lastGasPd, numberOfItemsToAverage)))
						.setTrades(tradeActions)
						.setFinalTime(AnalysisUtils.getLast(lastNeoPd).getUpdateTime());
			}
			return summary;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Calculate the amount of profit we can set with our buy/sell strategy if we convert to and from NEO and GAS
	 *
	 * @param firstNeoPrice - the first NEOBTC price
	 * @param lastNeoPrice - the last NEOBTC price
	 * @param initialInvestment - the initial investment in BTC
	 * @param actionList - the list of buy/sell combinations
	 * @return the profits (positive or negative depending on gain or loss)
	 */
	public static double calculateProfitsNeo(double firstNeoPrice, double lastNeoPrice,
					 double initialInvestment, List<TradeAction> actionList) {
		List<TradeAction> actions = new ArrayList<>();
		actions.addAll(actionList);
		try {
			if (!canFixList(actions)) {
				return 0;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}

		// first buy neo
		double amountInNeo = initialInvestment/firstNeoPrice;
		double amountInGas = 0;

		// Iterate through the remaining transactions, swapping between NEO and GAS
		for (TradeAction action : actions) {
			if (action.buy) {
				// buy GAS, sell NEO
				amountInGas += (action.getNeoPrice() / action.getGasPrice()) * amountInNeo;
				amountInNeo = 0;
			}
			if (!action.buy) {
				// sell GAS, buy NEO
				amountInNeo = (action.gasPrice / action.neoPrice) * amountInGas;
				amountInGas = 0;
			}
		}
		// finally, return to btc - use last price and subtract it from our initial investment
		return (lastNeoPrice * amountInNeo) - initialInvestment;
	}

	/**
	 * Calculate the profits you would get by trading GAS back and forth from GAS
	 *
	 * @return the profits (positive or negative depending on gain or loss)
	 */
	public static double calculateProfitsBtc(double initialInvestment, List<TradeAction> actionList) {
		List<TradeAction> actions = new ArrayList<>(actionList);
		try {
			if (!canFixList(actions)) {
				return 0;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}

		// We start with an investment
		double amountInBitcoin = initialInvestment;
		double amountInGas = 0;
		// Process all trades
		for (TradeAction action : actions) {
			// buy GAS with BTC
			if (action.buy) {
				// Assume we use all our available BTC, whether it is more or less than our initial investment
				// We could change this to have a maximum if we didn't want to risk profits
				amountInGas += amountInBitcoin / action.gasPrice;
				amountInBitcoin = 0;
			}
			// sell GAS for BTC
			if (!action.buy) {
				amountInBitcoin += action.gasPrice * amountInGas;
				amountInGas = 0;
			}
		}
		// The profit is how much above or below our initial investment
		return (amountInBitcoin - initialInvestment);
	}

	/**
	 * Find all the transactions that would occur given the low ration and high ratio that would trigger buys or sells
	 *
	 * @param neoList - the list of NEO price data
	 * @param gasList - the list of GAS price data
	 * @param bought - If we have last done a buy (we can't do a sell unless we've done a buy and vice versa)
	 * @param lowRatio - The ratio to buy GAS
	 * @param highRatio - the ration to sell GAS
	 * @return - if we are currently in a buy state
	 */
	private boolean getTradeActions(List<PriceData> neoList, List<PriceData> gasList, boolean bought,
									double lowRatio, double highRatio, List<TradeAction> actions) {
		// If any of the lists are empty, return our current state without change
		if (neoList.isEmpty() || gasList.isEmpty() || neoList.size() != gasList.size()) {
			return bought;
		}
		// Sort the data
		List<PriceData> splitNeoList = AnalysisUtils.sortList(neoList);
		List<PriceData> splitGasList = AnalysisUtils.sortList(gasList);

		// For each data point, compare the ratio
		for (int i = 0; i < splitNeoList.size(); i++) {
			PriceData neo = neoList.get(i);
			PriceData gas = splitGasList.get(i);
			double ratio = gas.getPrice() / neo.getPrice();
			// We are currently bought in and we're above our high ratio, sell GAS
			if (bought && ratio > highRatio) {
				// sell it
				actions.add(new TradeAction(gas.getUpdateTime(), gas.getPrice(), neo.getPrice(), false));
				bought = false;
			}
			// We are currently NOT bought in and we're below our low ratio, buy GAS
			if (!bought && ratio < lowRatio) {
				actions.add(new TradeAction(gas.getUpdateTime(), gas.getPrice(), neo.getPrice(), true));
				bought = true;
			}
		}
		return bought;
	}

	/**
	 * Attempt to fix the list if it's weird and if we can't, return false
	 *
	 * @param actions - the list that will be edited if needed or returned as invalid
	 * @return if the list has been successfully fixed
	 */
	public static boolean canFixList(List<TradeAction> actions) throws ScratchException {
		// We don't have enough data - we need at least one buy and one sell
		if (actions == null || actions.size() < 2) {
			throw new ScratchException("There are less than 2 actions");
		}
		// We ended on a buy, ignore the last item
		if (actions.size() % 2 == 1 && actions.get(0).isBuy()) {
			actions.remove(actions.size() - 1);
		}
		// For some reason, we started on a sell. This should never happen, but if
		// it does, remove the 1st one.
		if (actions.size() % 2 == 1 && !actions.get(0).isBuy()) {
			actions.remove(0);
		}
		// Weird, we started on a sell, but still have multiples of 2, so maybe end on a buy?
		if (actions.size() % 2 == 0 && !actions.get(0).isBuy()) {
			actions.remove(actions.size() - 1);
			actions.remove(0);
		}

		// At this point, all items should be an alternating sequence of BUY-SELL-BUY-SELL-....
		boolean oldBuy = false;
		for (TradeAction action : actions) {
			boolean newBuy = action.isBuy();
			if (oldBuy == newBuy) {
				throw new ScratchException("Invalid list - not ordered alternating BUY/SELL");
			}
			oldBuy = newBuy;
		}
		return true;
	}
}
