package com.sharshar.scratchanalysis;

import com.sharshar.scratchanalysis.algorithms.CoinSwap;
import com.sharshar.scratchanalysis.beans.PriceData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Tests the algorithms to do swaps between different currencies
 *
 * Created by lsharshar on 5/6/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class CoinSwapTests {
	@Autowired
	private CoinSwap swap;

	@Test
	public void testCalculateProfitsCoin2() {

		int interval = 1000 * 60 * 60; // 1 hour

		Date d1 = new Date(); double coin1Price1 = 1; double coin2Price1 = 3;
		CoinSwap.TradeAction act1 = swap.new TradeAction(d1, coin1Price1, coin2Price1, true);

		Date d2 = new Date(d1.getTime() + interval); double coin1Price2 = 2; double coin2Price2 = 3;
		CoinSwap.TradeAction act2 = swap.new TradeAction(d2, coin1Price2, coin2Price2, false);

		Date d3 = new Date(d2.getTime() + interval); double coin1Price3 = 1; double coin2Price3 = 3;
		CoinSwap.TradeAction act3 = swap.new TradeAction(d3, coin1Price3, coin2Price3, true);

		Date d4 = new Date(d3.getTime() + interval); double coin1Price4 = 3; double coin2Price4 = 4;
		CoinSwap.TradeAction act4 = swap.new TradeAction(d4, coin1Price4, coin2Price4, false);

		Date d5 = new Date(d4.getTime() + interval); double coin1Price5 = 1.9; double coin2Price5 = 5;
		CoinSwap.TradeAction act5 = swap.new TradeAction(d5, coin1Price5, coin2Price5, true);

		Date d6 = new Date(d5.getTime() + interval); double coin1Price6 = 2; double coin2Price6 = 3;
		CoinSwap.TradeAction act6 = swap.new TradeAction(d6, coin1Price6, coin2Price6, false);

		List<CoinSwap.TradeAction> actions = Arrays.asList(act1, act2, act3, act4, act5, act6);

		double profit = CoinSwap.calculateProfitsCoin2(coin2Price1,2, 300, actions);
		System.out.println("Coin 2 Profit: " + profit);
		double profit2 = CoinSwap.calculateProfitsBtc(300, actions);
		System.out.println("BTC Profit: " + profit2);

		List<CoinSwap.TradeAction> a1 = new ArrayList<>(actions);
		try {
			CoinSwap.fixList(a1);
			assertEquals(6, actions.size());
		} catch (Exception ex) {
			fail("Should be a valid list");
		}
		List<CoinSwap.TradeAction> a2 = new ArrayList<>(actions);
		a2.remove(0);
		try {
			CoinSwap.fixList(a2);
			assertEquals(4, a2.size());
		} catch (Exception ex) {
			fail("Should be a valid list");
		}
		List<CoinSwap.TradeAction> a3 = new ArrayList<>(actions);
		a3.remove(a3.size() - 1);
		try {
			CoinSwap.fixList(a3);
			assertEquals(4, a3.size());
		} catch (Exception ex) {
			fail("Should be a valid list");
		}

		List<CoinSwap.TradeAction> a4 = new ArrayList<>(actions);
		a4.remove(a4.size() - 1);
		a4.remove(0);
		try {
			CoinSwap.fixList(a4);
			assertEquals(2, a4.size());
		} catch (Exception ex) {
			fail("Should be a valid list");
		}

		try {
			CoinSwap.fixList(null);
			fail("Should be an invalid list");
		} catch (Exception ex) {
		}

		List<CoinSwap.TradeAction> badList = new ArrayList(Arrays.asList(act1, act3, act2, act4, act5, act6));
		try {
			CoinSwap.fixList(badList);
			fail("Should be an invalid list");
		} catch (Exception ex) {
		}
	}

	@Test
	public void testStandardDeviation() {
		int interval = 1000 * 60 * 60; // 1 hour

		List<Double> pd = Arrays.asList(9.0, 2.0, 5.0, 4.0, 12.0, 7.0 );

		CoinSwap.DataRange dataRange = swap.new DataRange(pd);
		assertEquals(2.0, dataRange.getLowValue(), 0.00001);
		assertEquals(12.0, dataRange.getHighValue(), 0.00001);
		assertEquals(6.5, dataRange.getMean(), 0.00001);
		assertEquals(3.619, dataRange.getStdDev(), 0.001);
	}

	@Test
	public void testGetPriceSample() {
		PriceData n1 = new PriceData().setPrice(1.0);
		PriceData n2 = new PriceData().setPrice(2.0);
		PriceData n3 = new PriceData().setPrice(3.0);
		PriceData n4 = new PriceData().setPrice(4.0);
		PriceData n5 = new PriceData().setPrice(5.0);
		PriceData g1 = new PriceData().setPrice(0.5);
		PriceData g2 = new PriceData().setPrice(1.5);
		PriceData g3 = new PriceData().setPrice(2.3);
		PriceData g4 = new PriceData().setPrice(3.0);
		PriceData g5 = new PriceData().setPrice(4.0);
		assertEquals(0, swap.getPriceSample(null, null, 3, 2).size());

		List<PriceData> coin2List = Arrays.asList(n1, n2, n3, n4, n5);
		List<PriceData> coin1List = Arrays.asList(g1, g2, g3, g4, g5);
		assertEquals(0, swap.getPriceSample(coin2List, coin1List, 3, 5).size());
		assertEquals(0, swap.getPriceSample(coin2List, coin1List, 0, 5).size());
		assertEquals(5, swap.getPriceSample(coin2List, coin1List, 4, 5).size());
		List<Double> results = swap.getPriceSample(coin2List, coin1List, 4, 3);
		assertEquals(results.size(), 3);
		assertEquals(results.get(0), 0.76666666666, 0.00001);
		assertEquals(results.get(1), 0.75, 0.00001);
		assertEquals(results.get(2), 0.80, 0.00001);
	}

	@Test
	public void canSell() {
		Date d1 = new Date(); double coin1Price1 = 1; double coin2Price1 = 3;
		CoinSwap.TradeAction act1 = swap.new TradeAction(
				d1, coin1Price1, coin2Price1, true);
		List<CoinSwap.TradeAction> actions = Arrays.asList(act1);
		assertTrue(swap.shouldSell(actions, 0.5));
		assertFalse(swap.shouldSell(actions, 0.25));
	}

	@Test
	public void getAllValues() throws Exception {
		CoinSwap.DataRange dr = swap.overAllTime("GASBTC", "NEOBTC");
		System.out.println(dr.toString());
	}
}
