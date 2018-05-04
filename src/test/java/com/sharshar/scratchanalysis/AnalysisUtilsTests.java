package com.sharshar.scratchanalysis;

import com.sharshar.scratchanalysis.algorithms.AnalysisUtils;
import com.sharshar.scratchanalysis.beans.PriceData;
import com.sharshar.scratchanalysis.utils.ScratchConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the util class for analysis
 *
 * Created by lsharshar on 5/3/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AnalysisUtilsTests {
	@Test
	public void testGetPercentageChange() {
		assertEquals(0.2, AnalysisUtils.getPercentageChange(100, 120), 0.00001);
		assertEquals(-0.2, AnalysisUtils.getPercentageChange(100, 80), 0.00001);
		assertEquals(0, AnalysisUtils.getPercentageChange(100, 100), 0.00001);
		assertEquals(0, AnalysisUtils.getPercentageChange(0, 100), 0.00001);
	}

	@Test
	public void sortListAndMean() {
		// Create a bunch of times so we can see if they sort correctly
		int interval = 60000;
		Date first = new Date();
		Date second = new Date(first.getTime() + interval);
		Date third = new Date(second.getTime() + interval);
		Date fourth = new Date(third.getTime() + interval);

		PriceData pd1 = new PriceData()
			.setExchange(ScratchConstants.BINANCE).setPrice(1.0).setTicker("ETHBTC").setUpdateTime(third);
		PriceData pd2 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(2.0).setTicker("ETHBTC").setUpdateTime(second);
		PriceData pd3 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(3.0).setTicker("ETHBTC").setUpdateTime(first);
		PriceData pd4 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(4.0).setTicker("ETHBTC").setUpdateTime(fourth);

		List<PriceData> list = Arrays.asList(new PriceData[] {pd1, pd2, pd3, pd4});

		// sorting should result in p3, p2, p1, p4
		List<PriceData> newList = AnalysisUtils.sortList(list);
		assertEquals(4, newList.size());
		assertEquals(newList.get(0).getPrice(), 3.0, 0.00001);
		assertEquals(newList.get(1).getPrice(), 2.0, 0.00001);
		assertEquals(newList.get(2).getPrice(), 1.0, 0.00001);
		assertEquals(newList.get(3).getPrice(), 4.0, 0.00001);

		double mean = AnalysisUtils.getMean(list);
		assertEquals(mean, 2.5, 0.00001);
	}

	@Test
	public void testSplitUpData() {
		int interval = 60000;
		Date first = new Date();
		Date second = new Date(first.getTime() + interval/2);
		Date third = new Date(second.getTime() + interval/4);
		Date fourth = new Date(third.getTime() + interval);
		Date fifth = new Date(fourth.getTime() + interval/2);
		Date sixth = new Date(fifth.getTime() + interval/10);
		Date seven = new Date(fifth.getTime() + interval + interval/2);

		PriceData pd1 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(1.0).setTicker("ETHBTC").setUpdateTime(third);
		PriceData pd2 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(2.0).setTicker("ETHBTC").setUpdateTime(second);
		PriceData pd3 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(3.0).setTicker("ETHBTC").setUpdateTime(first);
		PriceData pd4 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(4.0).setTicker("ETHBTC").setUpdateTime(fifth);
		PriceData pd5 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(5.0).setTicker("ETHBTC").setUpdateTime(sixth);
		PriceData pd6 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(6.0).setTicker("ETHBTC").setUpdateTime(fourth);
		PriceData pd7 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(7.0).setTicker("ETHBTC").setUpdateTime(seven);

		List<PriceData> list = Arrays.asList(new PriceData[] {pd1, pd2, pd3, pd4, pd5, pd6, pd7});
		List<List<PriceData>> splitList = AnalysisUtils.splitUpData(list, interval);
		assertEquals(3, splitList.size());

		List<PriceData> l1 = splitList.get(0);
		assertEquals(3, l1.size());
		assertEquals(l1.get(0).getPrice(), 3.0, 0.00001);
		assertEquals(l1.get(1).getPrice(), 2.0, 0.00001);
		assertEquals(l1.get(2).getPrice(), 1.0, 0.00001);

		List<PriceData> l2 = splitList.get(1);
		assertEquals(3, l2.size());
		assertEquals(l2.get(0).getPrice(), 6.0, 0.00001);
		assertEquals(l2.get(1).getPrice(), 4.0, 0.00001);
		assertEquals(l2.get(2).getPrice(), 5.0, 0.00001);

		List<PriceData> l3 = splitList.get(2);
		assertEquals(1, l3.size());
		assertEquals(l3.get(0).getPrice(), 7.0, 0.00001);
	}

	@Test
	public void testGetDataInInterval() {
		int interval = 60000;
		Date first = new Date();
		Date second = new Date(first.getTime() + interval/2);
		Date third = new Date(second.getTime() + interval/4);
		Date fourth = new Date(third.getTime() + interval);

		PriceData pd1 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(1.0).setTicker("ETHBTC").setUpdateTime(third);
		PriceData pd2 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(2.0).setTicker("ETHBTC").setUpdateTime(second);
		PriceData pd3 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(3.0).setTicker("ETHBTC").setUpdateTime(fourth);
		PriceData pd4 = new PriceData()
				.setExchange(ScratchConstants.BINANCE).setPrice(4.0).setTicker("ETHBTC").setUpdateTime(first);

		List<PriceData> list = Arrays.asList(new PriceData[] {pd1, pd2, pd3, pd4});
		List<PriceData> l1 = AnalysisUtils.getDataWithinInterval(list, interval);

		assertEquals(3, l1.size());
		assertEquals(l1.get(0).getPrice(), 4.0, 0.00001);
		assertEquals(l1.get(1).getPrice(), 2.0, 0.00001);
		assertEquals(l1.get(2).getPrice(), 1.0, 0.00001);
	}

			/*
	public static List<AnalysisUtils.DateRange> splitDates(Date startDate, Date endDate, long maxTime) {
*/
}
