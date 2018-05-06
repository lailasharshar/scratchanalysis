package com.sharshar.scratchanalysis.algorithms;

import com.sharshar.scratchanalysis.beans.PriceData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Consolidated all the commonly used methods in one place
 *
 * Created by lsharshar on 4/29/2018.
 */
public class AnalysisUtils {
	/**
	 * A useful data range object
	 */
	public class DateRange {
		Date startDate;
		Date endDate;
		public Date getStartDate() {
			return startDate;
		}

		public Date getEndDate() {
			return endDate;
		}

		public DateRange(Date startDate, Date endDate) {
			this.startDate = startDate;
			this.endDate = endDate;
		}
	}

	/**
	 * Split up the data in time increments based on the time span. Used to put data into
	 * manageable buckets.
	 *
	 * @param priceData - the data to split
	 * @param timeSpan - the chunk of time for each data group
	 * @return the list of all the data broken up into chunks of time specified by timeSpan
	 */
	public static List<List<PriceData>> splitUpData(List<PriceData> priceData, long timeSpan) {
		// If the list is empty, return empty list
		List<List<PriceData>> splitData = new ArrayList<>();
		if (timeSpan == 0 || priceData == null || priceData.isEmpty()) {
			return splitData;
		}

		List<PriceData> priceDataCopy = new ArrayList<>();
		priceDataCopy.addAll(priceData);
		// Pull data from the list until there is no more data in the list
		while (!priceDataCopy.isEmpty()) {
			List<PriceData> inInterval = getDataWithinInterval(priceDataCopy, timeSpan);
			priceDataCopy.removeAll(inInterval);
			splitData.add(inInterval);
		}
		return splitData;
	}

	/**
	 * Given a list, retrieve data within an interval of the first item's date and the first item's
	 * date + interval
	 *
	 * @param list the list to parse
	 * @param interval the time interval in milliseconds
	 * @return the list within that interval
	 */
	public static List<PriceData> getDataWithinInterval(List<PriceData> list, long interval) {
		List<PriceData> dataInInterval = new ArrayList<>();
		if (list == null || list.isEmpty() || interval == 0) {
			return dataInInterval;
		}
		// Sort into time order - it usually doesn't come back from the DB that way
		List<PriceData> priceDataCopy = sortList(list);

		// Get everything between the 1st update time in the list to and up to that date plus the interval
		List<PriceData> resultVal =  list.stream()
				.filter(c -> c.getUpdateTime().getTime() < priceDataCopy.get(0).getUpdateTime().getTime() + interval)
				.collect(Collectors.toList());
		return sortList(resultVal);
	}

	/**
	 * Take a time interval and split them into date ranges with a maximum of maxTime size (the last one
	 * will probably be shorter)
	 *
	 * @param startDate - the start date
	 * @param endDate - the end data
	 * @param maxTime - the maximum span of one date range
	 * @return - the time splits
	 */
	public static List<AnalysisUtils.DateRange> splitDates(Date startDate, Date endDate, long maxTime) {
		List<AnalysisUtils.DateRange> dr = new ArrayList<>();
		if (startDate == null || endDate == null || endDate.getTime() <= startDate.getTime() || maxTime == 0) {
			return dr;
		}
		Date movingStartDate = new Date(startDate.getTime());
		while (movingStartDate.getTime() < endDate.getTime()) {
			Date movingEndDate = new Date(movingStartDate.getTime() + maxTime);
			// If we are at the end of the range, move the moving end to the end date
			if (movingEndDate.getTime() > endDate.getTime()) {
				movingEndDate.setTime(endDate.getTime());
			}
			dr.add(new AnalysisUtils().new DateRange(movingStartDate, movingEndDate));
			movingStartDate = new Date(movingEndDate.getTime());
		}
		return dr;
	}

	/**
	 * Given a list of price data, calculate the mean of the price. Used to calculate start prices and end prices so
	 * that one outlier does not define the entire data set
	 *
	 * @param data - the price data to average
	 * @return the average price
	 */
	public static double getMean(List<PriceData> data) {
		if (data == null || data.isEmpty()) {
			return 0;
		}
		double sub = data.stream().mapToDouble(PriceData::getPrice).sum();
		return sub/data.size();
	}

	/**
	 * Sort a price data list by update time
	 *
	 * @param pd - the price data
	 * @return the sorted price data
	 */
	public static List<PriceData> sortList(List<PriceData> pd) {
		return pd.stream().sorted(Comparator.comparing(PriceData::getUpdateTime)).collect(Collectors.toList());
	}

	/**
	 * Based on the start value and the end value, calculate the percentage (0-1) increase or
	 * decrease. Positive number indicates increases, negative numbers decreases.
	 *
	 * @param startVal - start value
	 * @param endVal - end value
	 * @return - the percent change
	 */
	public static double getPercentageChange(double startVal, double endVal) {
		if (startVal == 0 || endVal == 0) {
			return 0;
		}
		if (endVal >= startVal) {
			return (endVal - startVal)/startVal;
		}
		return -1 * ((startVal - endVal)/startVal);
	}

	/**
	 * Get the earliest price data in the list
	 *
	 * @param data - the data list
	 * @return the earliest price data
	 */
	public static PriceData getFirst(List<PriceData> data) {
		if (data == null || data.isEmpty()) {
			return null;
		}
		return data.stream().min(Comparator.comparingLong(c -> c.getUpdateTime().getTime())).orElse(null);
	}

	/**
	 * Get the latest price data in the list
	 *
	 * @param data - the data list
	 * @return the latest price data
	 */
	public static PriceData getLast(List<PriceData> data) {
		if (data == null || data.isEmpty()) {
			return null;
		}
		return data.stream().max(Comparator.comparingLong(c -> c.getUpdateTime().getTime())).orElse(null);
	}

	/**
	 * Get the earliest x number of price data records in the list
	 *
	 * @param list - the data list
	 * @param x - the number of items to pull
	 * @return the earliest price data list with x number of items, or the entire list if x is
	 * greater than the list size. The result is ordered by increasing date
	 */
	public static List<PriceData> getFirstX(List<PriceData> list, int x) {
		int xVal = x;
		List<PriceData> firstX = new ArrayList<>();
		if (list == null) {
			return firstX;
		}
		if (list.size() < x) {
			xVal = list.size();
		}
		List<PriceData> sortedList = sortList(list);
		for (int i=0; i<xVal; i++) {
			firstX.add(sortedList.get(i));
		}
		return firstX;
	}

	/**
	 * Get the latest x number of price data records in the list
	 *
	 * @param list - the data list
	 * @param x - the number of items to pull
	 * @return the latest price data list with x number of items, or the entire list if x is
	 * greater than the list size. The result is ordered by increasing date
	 */
	public static List<PriceData> getLastX(List<PriceData> list, int x) {
		int xVal = x;
		List<PriceData> lastX = new ArrayList<>();
		if (list == null) {
			return lastX;
		}
		if (list.size() < x) {
			xVal = list.size();
		}
		List<PriceData> sortedList = sortList(list);
		for (int i=(sortedList.size() - xVal); i<sortedList.size(); i++) {
			lastX.add(sortedList.get(i));
		}
		return lastX;
	}
}
