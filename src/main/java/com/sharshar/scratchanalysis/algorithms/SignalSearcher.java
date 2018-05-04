package com.sharshar.scratchanalysis.algorithms;

import com.sharshar.scratchanalysis.beans.PriceData;
import com.sharshar.scratchanalysis.repository.PriceDataES;
import com.sharshar.scratchanalysis.utils.ScratchConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.sharshar.scratchanalysis.algorithms.AnalysisUtils.splitDates;

/**
 * Algorithm to search the data for micro (or macro) trends in data. This detects whether a price increases or decreases
 * significantly  over a period of time
 *
 * Created by lsharshar on 3/28/2018.
 */
@Service
public class SignalSearcher {
	private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

	@Autowired
	private static ApplicationContext context;

	@Autowired
	private static PriceDataES priceDataEs;

	@Value("${tickers}")
	private String tickers;

	@Value("${startDate}")
	private String startDate;

	@Value("${endDate}")
	private String endDate;

	@Value("${exchange}")
	private short exchange;

	@Value("${up}")
	private double up;

	@Value("${down}")
	private double down;

	@Value("${interval}")
	private long interval;

	public class SignalIdentifier {
		Date startDate;
		Date endDate;
		String ticker;
		short exchange;
		double multiplier;

		public Date getStartDate() {
			return startDate;
		}

		public Date getEndDate() {
			return endDate;
		}

		public double getMultiplier() {
			return multiplier;
		}

		public SignalIdentifier(Date startDate, Date endDate, String ticker, short exchange, double multiplier) {
			this.startDate = startDate;
			this.endDate = endDate;
			this.ticker = ticker;
			this.exchange = exchange;
			this.multiplier = multiplier;
		}
	}

	/**
	 * Determine if there are signals within a range of data. A signal is considered anyplace where price
	 * data fluctuates more than a minMultiplier within that interval time. For example, if the minMultiplier
	 * is 2, it looks to see if the min price in that range and max price in that range are greater than or equal
	 * to a multiple of 2. If the min price is 25, it will test to see if the max price is >= 50
	 *
	 * @param listToSearch - list to search for signals
	 * @return the list of signals within the data
	 */

	public List<SignalIdentifier> getSignals(List<PriceData> listToSearch, double maxMultiplier, double minMultiplier) {
		// Create a new list with the data list so that if we remove any, it doesn't mess with class data.
		List<SignalIdentifier> identifiers = new ArrayList<>();
		PriceData maxPrice = Collections.max(listToSearch, Comparator.comparing(PriceData::getPrice));
		PriceData minPrice = Collections.min(listToSearch, Comparator.comparing(PriceData::getPrice));
		PriceData maxDate = Collections.max(listToSearch, Comparator.comparing(PriceData::getUpdateTime));
		PriceData minDate = Collections.min(listToSearch, Comparator.comparing(PriceData::getUpdateTime));

		// Find the value of the multiplier
		double multiplier = 0;
		// If the price increased over time, the multiplier is over the min price
		if (minPrice.getUpdateTime().getTime() <= maxPrice.getUpdateTime().getTime()) {
			multiplier = (maxPrice.getPrice() - minPrice.getPrice())/minPrice.getPrice();
			if (multiplier < maxMultiplier) {
				// We didn't find anything that might apply along the entire data set, we're done
				return identifiers;
			}
		}
		// If the price decreased over time, the multiplier is over the max price and negative
		if (minPrice.getUpdateTime().getTime() > maxPrice.getUpdateTime().getTime()) {
			multiplier = (maxPrice.getPrice() - minPrice.getPrice())/maxPrice.getPrice();
			multiplier = multiplier * -1;
			// Since the value is negative, a greater decrease means a lower number
			if (multiplier > minMultiplier) {
				return identifiers;
			}
		}

		SignalIdentifier signal = new SignalIdentifier(minDate.getUpdateTime(), maxDate.getUpdateTime(),
				minDate.getTicker(), minDate.getExchange(), multiplier);
		identifiers.add(signal);
		return identifiers;
	}


	public Map<String, List<SignalIdentifier>> getAllSignalIdentifiers(PriceDataES pdes) throws ParseException {
		Map<String, List<SignalIdentifier>> identifiers = new HashMap<>();
		List<String> ts = Arrays.asList(tickers.split(","));
		for (String ticker : ts) {
			identifiers.put(ticker, getSignalIdentifiers(pdes, ticker));
		}
		return identifiers;
	}

	public List<SignalIdentifier> getSignalIdentifiers(PriceDataES dataES, String ticker) throws ParseException {
		Date sDate = sdf.parse(startDate);
		Date eDate = sdf.parse(endDate);
		List<SignalIdentifier> sigIds = new ArrayList<>();
		try {
			// Split over 4 hours
			List<AnalysisUtils.DateRange> dateRanges = splitDates(sDate, eDate, 1000 * 60 * 60 * 4);
			for (AnalysisUtils.DateRange dateRange : dateRanges) {
				List<PriceData> pd = dataES.findByTimeRange(ticker, dateRange.getStartDate(), dateRange.getEndDate(), exchange);
				List<List<PriceData>> splitList = AnalysisUtils.splitUpData(pd, interval);
				for (List<PriceData> subList : splitList) {
					sigIds.addAll(getSignals(subList, up, down));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sigIds;
	}

	public void printItOut(Map<String, List<SignalIdentifier>> identifiers) throws ParseException {
		if (identifiers == null) {
			System.out.println("No data");
			return;
		}
		Date sDate = sdf.parse(startDate);
		Date eDate = sdf.parse(endDate);
		System.out.println("Signals in " + ScratchConstants.EXCHANGES[exchange] + " from: " + sDate + " to " + eDate);
		Set<String> ts = identifiers.keySet();
		for (String ticker : ts) {
			List<SignalIdentifier> ids = identifiers.get(ticker);
			if (!ids.isEmpty()) {
				System.out.println("\n" + ticker + ":");
				ids.stream().forEach(id -> System.out.println(id.getMultiplier() + ", time: " + id.getStartDate() + " - " + id.getEndDate()));
			}
		}

	}
	public void printItOut(ApplicationContext context) throws ParseException {
		Date sDate = sdf.parse(startDate);
		Date eDate = sdf.parse(endDate);
		PriceDataES pdes = context.getBean(PriceDataES.class);
		System.out.println("Signals in " + ScratchConstants.EXCHANGES[exchange] + " from: " + sDate + " to " + eDate);
		List<String> ts = Arrays.asList(tickers.split(","));
		for (String ticker : ts) {
			boolean printTicker = true;
			try {
				// Split over 4 hours
				List<AnalysisUtils.DateRange> dateRanges = splitDates(sDate, eDate, 1000 * 60 * 60 * 4);
				for (AnalysisUtils.DateRange dateRange : dateRanges) {
					List<PriceData> pd = pdes.findByTimeRange(ticker, dateRange.getStartDate(), dateRange.getEndDate(), exchange);
					List<List<PriceData>> splitList = AnalysisUtils.splitUpData(pd, interval);
					for (List<PriceData> subList : splitList) {
						List<SignalSearcher.SignalIdentifier> results = getSignals(subList, up, down);
						if (results != null && !results.isEmpty()) {
							if (printTicker) {
								System.out.println();
								System.out.println(ticker + ":");
								printTicker = false;
							}
							for (SignalSearcher.SignalIdentifier id : results) {
								System.out.println(id.getMultiplier() + ", time: " + id.getStartDate() + " - " + id.getEndDate());
							}
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
