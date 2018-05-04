package com.sharshar.scratchanalysis.algorithms;

import com.sharshar.scratchanalysis.beans.PriceData;
import com.sharshar.scratchanalysis.repository.PriceDataES;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
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
		public double initialInvestment;
		public double initialNeoPrice;
		public double finalNeoPrice;
		public double initialGasPrice;
		public double finalGasPrice;
		public double initialBtcPrice;
		public double finalBtcPrice;
		public Date initialTime;
		public Date finalTime;
		public List<TradeAction> trades;

		public double getInitialInvestment() {
			return initialInvestment;
		}

		public void setInitialInvestment(double initialInvestment) {
			this.initialInvestment = initialInvestment;
		}

		public double getInitialNeoPrice() {
			return initialNeoPrice;
		}

		public void setInitialNeoPrice(double initialNeoPrice) {
			this.initialNeoPrice = initialNeoPrice;
		}

		public double getFinalNeoPrice() {
			return finalNeoPrice;
		}

		public void setFinalNeoPrice(double finalNeoPrice) {
			this.finalNeoPrice = finalNeoPrice;
		}

		public double getInitialGasPrice() {
			return initialGasPrice;
		}

		public void setInitialGasPrice(double initialGasPrice) {
			this.initialGasPrice = initialGasPrice;
		}

		public double getFinalGasPrice() {
			return finalGasPrice;
		}

		public void setFinalGasPrice(double finalGasPrice) {
			this.finalGasPrice = finalGasPrice;
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

		public void setTrades(List<TradeAction> trades) {
			this.trades = trades;
		}

		public double getInitialBtcPrice() {
			return initialBtcPrice;
		}

		public void setInitialBtcPrice(double initialBtcPrice) {
			this.initialBtcPrice = initialBtcPrice;
		}

		public double getFinalBtcPrice() {
			return finalBtcPrice;
		}

		public void setFinalBtcPrice(double finalBtcPrice) {
			this.finalBtcPrice = finalBtcPrice;
		}

		public Date getInitialTime() {
			return initialTime;
		}

		public void setInitialTime(Date initialTime) {
			this.initialTime = initialTime;
		}

		public Date getFinalTime() {
			return finalTime;
		}

		public void setFinalTime(Date finalTime) {
			this.finalTime = finalTime;
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
		TradeSummary summary = new TradeSummary();
		summary.setInitialInvestment(initialInvestment);
		tradeActions = new ArrayList<>();
		Date sDate = sdf.parse(startDate);
		Date eDate = sdf.parse(endDate);
		try {
			// Split over 4 hours
			boolean bought = false;
			List<PriceData> firstNeoPriceData = null;
			List<PriceData> firstGasPriceData = null;
			List<PriceData> lastNeoPriceData = null;
			List<PriceData> lastGasPriceData = null;
			List<PriceData> firstBtcPriceData = null;
			List<PriceData> lasttBtcPriceData = null;
			List<AnalysisUtils.DateRange> dateRanges = AnalysisUtils.splitDates(sDate, eDate, 1000 * 60 * 60 * 4);
			boolean firstItem = true;
			for (AnalysisUtils.DateRange dateRange : dateRanges) {
				List<PriceData> neoPd = dataES.findByTimeRange("NEOBTC", dateRange.getStartDate(), dateRange.getEndDate(), exchange);
				List<PriceData> gasPd = dataES.findByTimeRange("GASBTC", dateRange.getStartDate(), dateRange.getEndDate(), exchange);
				List<PriceData> btcPd = dataES.findByTimeRange("BTCUSDT", dateRange.getStartDate(), dateRange.getEndDate(), exchange);
				List<List<PriceData>> splitNeoList = AnalysisUtils.splitUpData(neoPd, interval);
				// Set it to the last date range - eventually it will be true when we drop out of the for
				if (!neoPd.isEmpty()) {
					lastNeoPriceData = neoPd;
				}
				if (!gasPd.isEmpty()) {
					lastGasPriceData = gasPd;
				}
				// If we are not defined yet, this must be first
				if (firstNeoPriceData == null && !neoPd.isEmpty()) {
					firstNeoPriceData = neoPd;
				}
				if (firstGasPriceData == null && !gasPd.isEmpty()) {
					firstGasPriceData = gasPd;
				}
				// Determine the ratio and then buy or sell (you can only buy if you've sold, you can only sell if you've bought)
				List<List<PriceData>> splitGasList = AnalysisUtils.splitUpData(gasPd, interval);
				for (int i = 0; i < splitNeoList.size(); i++) {
					List<PriceData> neoList = splitNeoList.get(i);
					if (splitGasList.size() > i) {
						List<PriceData> gasList = splitGasList.get(i);
						bought = getTradeActions(neoList, gasList, bought, lowGasRatio, highGasRatio);
					}
				}
			}
			if (firstGasPriceData != null && lastGasPriceData != null && firstNeoPriceData != null && lastNeoPriceData != null &&
					!firstGasPriceData.isEmpty() && !lastGasPriceData.isEmpty() && !firstNeoPriceData.isEmpty() && !lastNeoPriceData.isEmpty()) {
				Date initialTime = firstGasPriceData.get(0).getUpdateTime();
				Date finalTime = lastGasPriceData.get(firstGasPriceData.size() - 1).getUpdateTime();
				summary.setInitialTime(initialTime);
				summary.setFinalTime(finalTime);
				summary.setInitialGasPrice(getInitialPrice(initialTime, "GASBTC", dataES));
				summary.setInitialNeoPrice(getInitialPrice(initialTime, "NEOBTC", dataES));
				summary.setInitialBtcPrice(getInitialPrice(initialTime, "BTCUSDT", dataES));
				summary.setTrades(tradeActions);
				summary.setFinalGasPrice(getFinalPrice(finalTime, "GASBTC", dataES));
				summary.setFinalNeoPrice(getFinalPrice(finalTime, "NEOBTC", dataES));
				summary.setFinalBtcPrice(getFinalPrice(finalTime, "BTCUSDT", dataES));
			}
			return summary;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public double calculateProfitsNeo(double lastNeoPrice, double lastGasPrice) {
		double amountInNeo = 0;
		double amountInGas = 0;
		if (tradeActions.size() < 2) {
			// We don't have enough data
			return 0;
		}
		if (tradeActions.size() % 2 == 1) {
			// We ended on a buy, ignore the last item
			tradeActions.remove(tradeActions.size() - 1);
		}

		// If the ratio is low, buy GAS, otherwise, buy NEO
		double ratio = tradeActions.get(0).getGasPrice() / tradeActions.get(0).getNeoPrice();
		double midValue = (highGasRatio + lowGasRatio) / 2;
		if (ratio > midValue) {
			amountInNeo = initialInvestment / (tradeActions.get(0).getNeoPrice());
			amountInGas = 0;
		} else {
			amountInGas = initialInvestment / (tradeActions.get(0).getGasPrice());
			amountInNeo = 0;
		}
		if (amountInGas > 0) {
			// Remove the first buy, we've already bought it
			tradeActions.remove(0);
		}

		double lastNeo = 0;
		for (TradeAction action : tradeActions) {
			if (action.buy) {
				// buy
				amountInGas += amountInNeo / action.gasPrice;
				amountInNeo = 0;
			}
			if (!action.buy) {
				// sell
				amountInNeo = action.neoPrice / action.gasPrice;
				amountInGas = 0;
			}
		}
		// finally, return to btc - use last price
		if (amountInGas > 0) {
			amountInNeo = (lastGasPrice/lastNeo) * amountInGas;
		}
		// Amount in bitcoin
		return (lastNeoPrice * amountInNeo) - initialInvestment;
	}

	public double calculateProfitsBtc() {
		if (tradeActions.size() < 2) {
			// We don't have enough data
			return 0;
		}
		if (tradeActions.size() % 2 == 1) {
			// We ended on a buy, ignore the last item
			tradeActions.remove(tradeActions.size() - 1);
		}
		double profits = 0;
		double amountInBitcoin = initialInvestment;
		double amountInGas = 0;
		for (TradeAction action : tradeActions) {
			logger.info(action.toString());
			if (action.buy) {
				// buy
				amountInBitcoin -= initialInvestment;
				amountInGas += initialInvestment / action.gasPrice;
			}
			if (!action.buy) {
				// sell
				amountInBitcoin += action.gasPrice * amountInGas;
				amountInGas = 0;
			}
		}
		return (amountInBitcoin - initialInvestment);
	}

	private boolean getTradeActions(List<PriceData> neoList, List<PriceData> gasList, boolean bought, double lowRatio, double highRatio) {
		if (neoList.isEmpty() || gasList.isEmpty() || neoList.size() != gasList.size()) {
			return bought;
		}
		List<PriceData> splitNeoList = AnalysisUtils.sortList(neoList);
		List<PriceData> splitGasList = AnalysisUtils.sortList(gasList);
		for (int i = 0; i < splitNeoList.size(); i++) {
			PriceData neo = neoList.get(i);
			PriceData gas = splitGasList.get(i);
			double ratio = gas.getPrice() / neo.getPrice();
			if (bought && ratio > highRatio) {
				// sell it
				tradeActions.add(new TradeAction(gas.getUpdateTime(), gas.getPrice(), neo.getPrice(), false));
				bought = false;
			}
			if (!bought && ratio < lowRatio) {
				tradeActions.add(new TradeAction(gas.getUpdateTime(), gas.getPrice(), neo.getPrice(), true));
				bought = true;
			}
		}
		return bought;
	}

	private double getInitialPrice(Date startDate, String ticker, PriceDataES dataES) throws Exception {
		// Take the end data as one minute later, take the mean of the 1st minute
		Date endDate = new Date(startDate.getTime() + (1000 * 60));
		List<PriceData> pd = dataES.findByTimeRange(ticker, startDate, endDate, exchange);
		return AnalysisUtils.getMean(pd);
	}

	private double getFinalPrice(Date endDate, String ticker, PriceDataES dataES) throws Exception {
		// Take the start data as one minute earlier, take the mean of the last minute
		Date startDate = new Date(endDate.getTime() - (1000 * 60));
		List<PriceData> pd = dataES.findByTimeRange(ticker, startDate, endDate, exchange);
		return AnalysisUtils.getMean(pd);
	}
}
