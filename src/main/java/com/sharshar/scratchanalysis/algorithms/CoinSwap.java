package com.sharshar.scratchanalysis.algorithms;

import com.sharshar.scratchanalysis.beans.PriceData;
import com.sharshar.scratchanalysis.service.PriceDataPuller;
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
 * Algorithm to test if swapping between coin 2 and BTC and coin 1 gives better profits
 *
 * Created by lsharshar on 4/29/2018.
 */
@Service
public class CoinSwap {
	private static Logger logger = LogManager.getLogger();

	public class DataRange {
		double lowValue;
		double highValue;
		double mean;
		double stdDev;

		public double getLowValue() {
			return lowValue;
		}

		public DataRange setLowValue(double lowValue) {
			this.lowValue = lowValue;
			return this;
		}

		public double getHighValue() {
			return highValue;
		}

		public DataRange setHighValue(double highValue) {
			this.highValue = highValue;
			return this;
		}

		public double getMean() {
			return mean;
		}

		public DataRange setMean(double mean) {
			this.mean = mean;
			return this;
		}

		public double getStdDev() {
			return stdDev;
		}

		public DataRange setStdDev(double stdDev) {
			this.stdDev = stdDev;
			return this;
		}

		public DataRange() {
		}

		public DataRange(List<Double> pd) {
			if (pd == null || pd.isEmpty()) {
				return;
			}
			this.lowValue = pd.stream().mapToDouble(c -> c).min().orElse(0.0);
			this.highValue = pd.stream().mapToDouble(c -> c).max().orElse(0.0);
			this.mean = pd.stream().mapToDouble(c -> c).summaryStatistics().getAverage();
			double totalSquared = pd.stream().mapToDouble(c -> Math.pow(Math.abs(c - this.mean), 2)).sum();
			double meanOfSquared = totalSquared/(pd.size() - 1);
			this.stdDev = Math.sqrt(meanOfSquared);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Range: ").append(lowValue).append("-").append(highValue).append("\n")
					.append("Mean: ").append(mean).append("\n")
					.append("StdDev: ").append(stdDev).append("\n");
			return startDate.toString();
		}
	}

	public class TradeAction {
		Date tradeDate;
		double coin1Price;
		double coin2Price;

		// True if the trade is to buy, false if it is to sell
		boolean buy;

		public TradeAction(Date tradeDate, double coin1Price, double coin2Price, boolean buy) {
			this.tradeDate = tradeDate;
			this.coin1Price = coin1Price;
			this.coin2Price = coin2Price;
			this.buy = buy;
		}

		public double getCoin1Price() {
			return this.coin1Price;
		}

		public double getCoin2Price() {
			return this.coin2Price;
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
					.append(coin1Price);
			return sb.toString();
		}
	}

	public class TradeSummary {
		String coin1;
		String coin2;
		double initialInvestment;
		double initialCoin2Price;
		double finalCoin2Price;
		double initialCoin1Price;
		double finalCoin1Price;
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

		public double getInitialCoin2Price() {
			return initialCoin2Price;
		}

		public TradeSummary setInitialCoin2Price(double initialCoin2Price) {
			this.initialCoin2Price = initialCoin2Price;
			return this;
		}

		public double getFinalCoin2Price() {
			return finalCoin2Price;
		}

		public TradeSummary setFinalCoin2Price(double finalCoin2Price) {
			this.finalCoin2Price = finalCoin2Price;
			return this;
		}

		public double getInitialCoin1Price() {
			return initialCoin1Price;
		}

		public TradeSummary setInitialCoin1Price(double initialCoin1Price) {
			this.initialCoin1Price = initialCoin1Price;
			return this;
		}

		public double getFinalCoin1Price() {
			return finalCoin1Price;
		}

		public TradeSummary setFinalCoin1Price(double finalCoin1Price) {
			this.finalCoin1Price = finalCoin1Price;
			return this;
		}

		public double getCoin1IncreasePercentage() {
			return AnalysisUtils.getPercentageChange(initialCoin1Price, finalCoin1Price);
		}

		public double getCoin2IncreasePercentage() {
			return AnalysisUtils.getPercentageChange(initialCoin2Price, finalCoin2Price);
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

		public String getCoin1() {
			return coin1;
		}

		public TradeSummary setCoin1(String coin1) {
			this.coin1 = coin1;
			return this;
		}

		public String getCoin2() {
			return coin2;
		}

		public TradeSummary setCoin2(String coin2) {
			this.coin2 = coin2;
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
					.append(coin2).append(" Price: ").append(String.format(format, initialCoin2Price))
					.append(" - ").append(String.format(format, finalCoin2Price))
					.append(" (").append(String.format(format, (finalCoin2Price - initialCoin2Price)))
					.append(" - ").append(String.format(format2, getCoin2IncreasePercentage() * 100))
					.append("%)\n")
					.append(coin1).append(" Price: ").append(String.format(format, initialCoin1Price))
					.append(" - ").append(String.format(format, finalCoin1Price))
					.append(" (").append(String.format(format, (finalCoin1Price - initialCoin1Price)))
					.append(" - ").append(String.format(format2, getCoin1IncreasePercentage() * 100))
					.append("%)\n")
					.append("BTC Price: ").append(String.format(format, initialBtcPrice))
					.append(" - ").append(String.format(format, finalBtcPrice))
					.append(" (").append(String.format(format, (finalBtcPrice - initialBtcPrice)))
					.append(" - ").append(String.format(format2, getBtcIncreasePercentage() * 100))
					.append("%)\n");
			if (trades != null) {
				builder.append("No. Trades: ").append(trades.size());
			}
			return builder.toString();
		}

	}

	private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

	@Autowired
	private PriceDataPuller priceDataPuller;

	@Value("${startDate}")
	private String startDate;

	@Value("${sampleSize}")
	private int sampleSize;

	@Value("${endDate}")
	private String endDate;

	@Value("${exchange}")
	private short exchange;

	@Value("${interval}")
	private long interval;

	@Value("${lowCoin1Ratio}")
	private double lowCoin1Ratio;

	@Value("${highCoin1Ratio}")
	private double highCoin1Ratio;

	private List<TradeAction> tradeActions;


	public TradeSummary getProfitUsingCoin1ToBtc(double initialInvestment, String coin1, String coin2, double movingAverageStdDevToUse) throws ParseException {
		int numberOfItemsToAverage = 5;
		TradeSummary summary = new TradeSummary()
				.setInitialInvestment(initialInvestment).setCoin1(coin1).setCoin2(coin2);
		tradeActions = new ArrayList<>();
		Date sDate = sdf.parse(startDate);
		Date eDate = sdf.parse(endDate);
		try {
			//CoinSwap.DataRange lastYearDeviance = overAllTime();

			// Assume we haven't bought anything yet
			boolean bought = false;
			// Save the last data that had price data in it so we can use it to specify the final prices, dates
			List<PriceData> lastCoin2Pd = null;
			List<PriceData> lastCoin1Pd = null;
			AnalysisUtils.DateRange finalDateFound = null;

			// Split over 4 hours and iterate
			List<AnalysisUtils.DateRange> dateRanges = AnalysisUtils.splitDates(sDate, eDate, interval);
			for (AnalysisUtils.DateRange dateRange : dateRanges) {
				// Load the data
				List<PriceData> coin2Pd = priceDataPuller.getPriceData(coin2, exchange, dateRange.getStartDate(), dateRange.getEndDate());
				List<PriceData> coin1Pd = priceDataPuller.getPriceData(coin1, exchange, dateRange.getStartDate(), dateRange.getEndDate());
				// If this is the first item with data, store it as the first data
				if (summary.getInitialTime() == null && !coin2Pd.isEmpty()) {
					// We only need BTC prices for the start and the end so do it here
					List<PriceData> btcPd = priceDataPuller.getPriceData("BTCUSDT", exchange, dateRange.getStartDate(), dateRange.getEndDate());
					// Set all the initial price/time values
					summary.setInitialTime(AnalysisUtils.getFirst(coin2Pd).getUpdateTime());
					summary.setInitialCoin1Price(AnalysisUtils.getMean(AnalysisUtils.getFirstX(coin1Pd, numberOfItemsToAverage)));
					summary.setInitialCoin2Price(AnalysisUtils.getMean(AnalysisUtils.getFirstX(coin2Pd, numberOfItemsToAverage)));
					summary.setInitialBtcPrice(AnalysisUtils.getMean(AnalysisUtils.getFirstX(btcPd, numberOfItemsToAverage)));
				}
				// Set it to the last date range - eventually it will be true when we drop out of the for loop
				if (!coin2Pd.isEmpty()) {
					finalDateFound = dateRange;
					lastCoin2Pd = coin2Pd;
					lastCoin1Pd = coin1Pd;
				}
				// Determine the ratio and then buy or sell (you can only buy if you've sold, you can only sell if you've bought)
				if (!coin1Pd.isEmpty()) {
					bought = getTradeActions(
							coin2Pd, coin1Pd, bought, lowCoin1Ratio, highCoin1Ratio, tradeActions, movingAverageStdDevToUse);
				}
			}
			// We're done with iterating over the date ranges, get the BTC data from the last, non-empty date range
			if (finalDateFound != null) {
				List<PriceData> btcPd = priceDataPuller.getPriceData("BTCUSDT", exchange, finalDateFound.getStartDate(), finalDateFound.getEndDate());
				// Set all the "final" data
				summary.setFinalBtcPrice(AnalysisUtils.getMean(AnalysisUtils.getLastX(btcPd, numberOfItemsToAverage)))
						.setFinalCoin2Price(AnalysisUtils.getMean(AnalysisUtils.getLastX(lastCoin2Pd, numberOfItemsToAverage)))
						.setFinalCoin1Price(AnalysisUtils.getMean(AnalysisUtils.getLastX(lastCoin1Pd, numberOfItemsToAverage)))
						.setTrades(tradeActions)
						.setFinalTime(AnalysisUtils.getLast(lastCoin2Pd).getUpdateTime());
			}
			return summary;
		} catch (Exception ex) {
			logger.error("Unable to get profit using coin1 to BTC", ex);
			return null;
		}
	}


	public TradeSummary getProfitAbsolute(double initialInvestment, String coin1, double minAmount, double maxAmount) throws ParseException {
		int numberOfItemsToAverage = 5;
		TradeSummary summary = new TradeSummary().setInitialInvestment(initialInvestment).setCoin1(coin1);
		tradeActions = new ArrayList<>();
		Date sDate = sdf.parse(startDate);
		Date eDate = sdf.parse(endDate);
		try {
			// Assume we haven't bought anything yet
			boolean bought = false;
			// Save the last data that had price data in it so we can use it to specify the final prices, dates
			List<PriceData> lastCoin1Pd = null;
			AnalysisUtils.DateRange finalDateFound = null;

			// Split over 4 hours and iterate
			List<AnalysisUtils.DateRange> dateRanges = AnalysisUtils.splitDates(sDate, eDate, interval);
			for (AnalysisUtils.DateRange dateRange : dateRanges) {
				// Load the data
				List<PriceData> coin1Pd = priceDataPuller.getPriceData(coin1, exchange, dateRange.getStartDate(), dateRange.getEndDate());
				// If this is the first item with data, store it as the first data
				if (summary.getInitialTime() == null && !coin1Pd.isEmpty()) {
					// We only need BTC prices for the start and the end so do it here
					// Set all the initial price/time values
					summary.setInitialTime(AnalysisUtils.getFirst(coin1Pd).getUpdateTime());
					summary.setInitialCoin1Price(AnalysisUtils.getMean(AnalysisUtils.getFirstX(coin1Pd, numberOfItemsToAverage)));
				}
				// Set it to the last date range - eventually it will be true when we drop out of the for loop
				if (!coin1Pd.isEmpty()) {
					finalDateFound = dateRange;
					lastCoin1Pd = coin1Pd;
				}
				// Determine the ratio and then buy or sell (you can only buy if you've sold, you can only sell if you've bought)
				if (!coin1Pd.isEmpty()) {
					bought = getTradeActionsAbsolute(coin1Pd, bought, minAmount, maxAmount, tradeActions);
				}
			}
			// We're done with iterating over the date ranges, get the BTC data from the last, non-empty date range
			if (finalDateFound != null) {
				// Set all the "final" data
				summary .setFinalCoin2Price(1)
						.setFinalCoin1Price(AnalysisUtils.getMean(AnalysisUtils.getLastX(lastCoin1Pd, numberOfItemsToAverage)))
						.setTrades(tradeActions)
						.setFinalTime(AnalysisUtils.getLast(lastCoin1Pd).getUpdateTime());
			}
			return summary;
		} catch (Exception ex) {
			logger.error("Unable to get absolute profit", ex);
			return null;
		}
	}


	/**
	 * Calculate the amount of profit we can set with our buy/sell strategy if we convert to and from Coin 2 and Coin 1
	 *
	 * @param firstCoin2Price - the first Coin 2 price
	 * @param lastCoin2Price - the last Coin 2 price
	 * @param initialInvestment - the initial investment in BTC
	 * @param actionList - the list of buy/sell combinations
	 * @return the profits (positive or negative depending on gain or loss)
	 */
	public static double calculateProfitsCoin2(double firstCoin2Price, double lastCoin2Price,
					 double initialInvestment, List<TradeAction> actionList) {
		List<TradeAction> actions = new ArrayList<>();
		actions.addAll(actionList);
		try {
			fixList(actions);
		} catch (Exception ex) {
			logger.error("Unable to get absolute profit", ex);
			return 0;
		}

		// first buy coin2
		double amountInCoin2 = initialInvestment/firstCoin2Price;
		double amountInCoin1 = 0;

		// Iterate through the remaining transactions, swapping between Coin 2 and Coin 1
		for (TradeAction action : actions) {
			if (action.buy) {
				// buy Coin 1, sell Coin 2
				amountInCoin1 += (action.getCoin2Price() / action.getCoin1Price()) * amountInCoin2;
				// calculate transaction fee - for binance, it's .5% of the transacted amount
				amountInCoin1 = amountInCoin1 - (amountInCoin1 * 0.005);
				amountInCoin2 = 0;
			}
			if (!action.buy) {
				// sell Coin 1, buy Coin 2
				amountInCoin2 = (action.coin1Price / action.coin2Price) * amountInCoin1;
				amountInCoin2 = amountInCoin2 - (amountInCoin2 * 0.005);
				amountInCoin1 = 0;
			}
		}
		// finally, return to btc - use last price and subtract it from our initial investment
		return (lastCoin2Price * amountInCoin2) - initialInvestment;
	}

	/**
	 * Calculate the profits you would get by trading coin 1 back and forth from coin 1
	 *
	 * @return the profits (positive or negative depending on gain or loss)
	 */
	public static double calculateProfitsBtc(double initialInvestment, List<TradeAction> actionList) {
		List<TradeAction> actions = new ArrayList<>(actionList);
		try {
			fixList(actions);
		} catch (Exception ex) {
			logger.error("Can't fix action list", ex);
			return 0;
		}

		// We start with an investment
		double amountInBitcoin = initialInvestment;
		double amountInCoin1 = 0;
		// Process all trades
		for (TradeAction action : actions) {
			// buy coin 1 with BTC
			if (action.buy) {
				// Assume we use all our available BTC, whether it is more or less than our initial investment
				// We could change this to have a maximum if we didn't want to risk profits
				amountInCoin1 += amountInBitcoin / action.coin1Price;
				amountInBitcoin = 0;
			}
			// sell coin 1 for BTC
			if (!action.buy) {
				amountInBitcoin += action.coin1Price * amountInCoin1;
				amountInCoin1 = 0;
			}
		}
		// The profit is how much above or below our initial investment
		return (amountInBitcoin - initialInvestment);
	}

	/**
	 * Find all the transactions that would occur given the low ration and high ratio that would trigger buys or sells
	 *
	 * @param coin2List - the list of Coin 2 price data
	 * @param coin1List - the list of coin 1 price data
	 * @param bought - If we have last done a buy (we can't do a sell unless we've done a buy and vice versa)
	 * @param lowRatio - The ratio to buy coin 1
	 * @param highRatio - the ration to sell coin 1
	 * @return - if we are currently in a buy state
	 */
	private boolean getTradeActions(List<PriceData> coin2List, List<PriceData> coin1List, boolean bought,
										 double lowRatio, double highRatio, List<TradeAction> actions, double movingTransactionStdDevMultiple) {
		// If any of the lists are empty, return our current state without change
		if (coin2List.isEmpty() || coin1List.isEmpty() || coin2List.size() != coin1List.size()) {
			return bought;
		}
		// Sort the data
		List<PriceData> splitCoin2List = AnalysisUtils.sortList(coin2List);
		List<PriceData> splitCoin1List = AnalysisUtils.sortList(coin1List);

		double highRatioToUse = highRatio;
		double lowRatioToUse = lowRatio;

		// For each data point, compare the ratio
		for (int i = 0; i < splitCoin2List.size(); i++) {
			PriceData coin2 = coin2List.get(i);
			PriceData coin1 = splitCoin1List.get(i);
			if (movingTransactionStdDevMultiple > 0) {
				List<Double> samplePrices = getPriceSample(splitCoin2List, splitCoin1List, i, sampleSize);
				if (samplePrices != null && samplePrices.size() == sampleSize) {
					DataRange dr = new DataRange(samplePrices);
					highRatioToUse = dr.getMean() + (movingTransactionStdDevMultiple * dr.getStdDev());
					lowRatioToUse = dr.getMean() - (movingTransactionStdDevMultiple * dr.getStdDev());
					if (highRatioToUse > 1.0) {
						highRatioToUse = 1;
					}
					if (lowRatioToUse < 0.0) {
						lowRatioToUse = 0;
					}
				}
			}
			double ratio = coin1.getPrice() / coin2.getPrice();
			// We are currently bought in and we're above our high ratio, sell coin 1
			if (bought && ratio > highRatioToUse && highRatioToUse > 0 /*&& shouldSell(actions, ratio)*/) {
				// sell it
				actions.add(new TradeAction(coin1.getUpdateTime(), coin1.getPrice(), coin2.getPrice(), false));
				bought = false;
			}

			// We are currently NOT bought in and we're below our low ratio, buy coin 1
			if (!bought && ratio < lowRatioToUse && lowRatio > 0) {
				actions.add(new TradeAction(coin1.getUpdateTime(), coin1.getPrice(), coin2.getPrice(), true));
				bought = true;
			}
		}
		return bought;
	}

	private boolean getTradeActionsAbsolute(List<PriceData> coin1List, boolean bought,
									double lowAmount, double highAmount, List<TradeAction> actions) {
		// If any of the lists are empty, return our current state without change
		if (coin1List.isEmpty()) {
			return bought;
		}
		// Sort the data
		List<PriceData> splitCoin1List = AnalysisUtils.sortList(coin1List);

		// For each data point, compare the ratio
		for (PriceData coin1 : splitCoin1List) {
			double value = coin1.getPrice();
			// We are currently bought in and we're above our high ratio, sell coin 1
			if (bought && value > highAmount) {
				// sell it
				actions.add(new TradeAction(coin1.getUpdateTime(), coin1.getPrice(), 1.0, false));
				bought = false;
			}

			// We are currently NOT bought in and we're below our low ratio, buy coin 1
			if (!bought && value < lowAmount) {
				actions.add(new TradeAction(coin1.getUpdateTime(), coin1.getPrice(), 1, true));
				bought = true;
			}
		}
		return bought;
	}

	public boolean shouldSell(List<TradeAction> tradeActions, double ratio) {
		if (tradeActions == null || tradeActions.isEmpty()) {
			return false;
		}
		TradeAction lastAction = tradeActions.get(tradeActions.size() - 1);
		double lastRatio = lastAction.getCoin1Price()/lastAction.getCoin2Price();
		return !(!lastAction.isBuy() || lastRatio > ratio);
	}

	public List<Double> getPriceSample(List<PriceData> splitCoin2List, List<PriceData> splitCoin1List, int i, int sampleSize) {
		int startLocation = i + 1 - sampleSize;
		ArrayList<Double> ratios = new ArrayList<>();
		if (splitCoin1List == null ||
				splitCoin2List == null ||
				splitCoin1List.size() < sampleSize ||
				splitCoin2List.size() < sampleSize ||
				splitCoin1List.size() != splitCoin2List.size() ||
				startLocation < 0) {
			return ratios;
		}

		for (int x = startLocation; x <= i; x++) {
			double coin2Price = splitCoin2List.get(x).getPrice();
			double coin1Price = splitCoin1List.get(x).getPrice();
			ratios.add(coin1Price/coin2Price);
		}
		return ratios;
	}

	/**
	 * Attempt to fix the list if it's weird and if we can't, return false
	 *
	 * @param actions - the list that will be edited if needed or returned as invalid
	 */
	public static void fixList(List<TradeAction> actions) throws ScratchException {
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
	}

	public DataRange overAllTime(String coin1, String coin2) throws Exception {
		Date eDate = new Date();
		long oneYear = (long) 1000 * 60 * 60 * 24 * 365;
		Date sDate = new Date(eDate.getTime() - oneYear);
		List<Double> ratios = new ArrayList<>();
		List<AnalysisUtils.DateRange> dateRanges = AnalysisUtils.splitDates(sDate, eDate, interval);
		for (AnalysisUtils.DateRange dateRange : dateRanges) {
			List<PriceData> coin2Pd = AnalysisUtils.sortList(priceDataPuller.getPriceData(coin2, exchange, dateRange.getStartDate(), dateRange.getEndDate()));
			List<PriceData> coin1Pd = AnalysisUtils.sortList(priceDataPuller.getPriceData(coin1, exchange, dateRange.getStartDate(), dateRange.getEndDate()));
			if (coin2Pd.size() > 0 && coin1Pd.size() > 0 && coin2Pd.size() == coin1Pd.size()) {
				for (int i=0; i<coin2Pd.size(); i++) {
					ratios.add(coin1Pd.get(i).getPrice() / coin2Pd.get(i).getPrice());
				}
			}
		}
		return new DataRange(ratios);
	}
}
