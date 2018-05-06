package com.sharshar.scratchanalysis;

import com.sharshar.scratchanalysis.algorithms.NeoGasSwap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests the algorithms to do NEO/GAS/BTC swaps
 *
 * Created by lsharshar on 5/6/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NeoGasSwapTests {
	@Test
	public void testCalculateProfitsNeo() {
		NeoGasSwap swap = new NeoGasSwap();

		int interval = 1000 * 60 * 60; // 1 hour

		Date d1 = new Date(); double gasPrice1 = 1; double neoPrice1 = 3;
		NeoGasSwap.TradeAction act1 = swap.new TradeAction(d1, gasPrice1, neoPrice1, true);

		Date d2 = new Date(d1.getTime() + interval); double gasPrice2 = 2; double neoPrice2 = 3;
		NeoGasSwap.TradeAction act2 = swap.new TradeAction(d2, gasPrice2, neoPrice2, false);

		Date d3 = new Date(d2.getTime() + interval); double gasPrice3 = 1; double neoPrice3 = 3;
		NeoGasSwap.TradeAction act3 = swap.new TradeAction(d3, gasPrice3, neoPrice3, true);

		Date d4 = new Date(d3.getTime() + interval); double gasPrice4 = 3; double neoPrice4 = 4;
		NeoGasSwap.TradeAction act4 = swap.new TradeAction(d4, gasPrice4, neoPrice4, false);

		Date d5 = new Date(d4.getTime() + interval); double gasPrice5 = 1.9; double neoPrice5 = 5;
		NeoGasSwap.TradeAction act5 = swap.new TradeAction(d5, gasPrice5, neoPrice5, true);

		Date d6 = new Date(d5.getTime() + interval); double gasPrice6 = 2; double neoPrice6 = 3;
		NeoGasSwap.TradeAction act6 = swap.new TradeAction(d6, gasPrice6, neoPrice6, false);

		List<NeoGasSwap.TradeAction> actions = Arrays.asList(act1, act2, act3, act4, act5, act6);

		double profit = NeoGasSwap.calculateProfitsNeo(neoPrice1,2, 300, actions);
		System.out.println("NEO Profit: " + profit);
		double profit2 = NeoGasSwap.calculateProfitsBtc(300, actions);
		System.out.println("BTC Profit: " + profit2);

		List<NeoGasSwap.TradeAction> a1 = new ArrayList<>(actions);
		try {
			boolean result = NeoGasSwap.canFixList(a1);
			assertEquals(6, actions.size());
		} catch (Exception ex) {
			fail("Should be a valid list");
		}
		List<NeoGasSwap.TradeAction> a2 = new ArrayList<>(actions);
		a2.remove(0);
		try {
			boolean result = NeoGasSwap.canFixList(a2);
			assertEquals(4, a2.size());
		} catch (Exception ex) {
			fail("Should be a valid list");
		}
		List<NeoGasSwap.TradeAction> a3 = new ArrayList<>(actions);
		a3.remove(a3.size() - 1);
		try {
			boolean result = NeoGasSwap.canFixList(a3);
			assertEquals(4, a3.size());
		} catch (Exception ex) {
			fail("Should be a valid list");
		}

		List<NeoGasSwap.TradeAction> a4 = new ArrayList<>(actions);
		a4.remove(a4.size() - 1);
		a4.remove(0);
		try {
			boolean result = NeoGasSwap.canFixList(a4);
			assertEquals(2, a4.size());
		} catch (Exception ex) {
			fail("Should be a valid list");
		}

		try {
			boolean result = NeoGasSwap.canFixList(null);
			fail("Should be an invalid list");
		} catch (Exception ex) {
		}

		List<NeoGasSwap.TradeAction> badList = new ArrayList(Arrays.asList(act1, act3, act2, act4, act5, act6));
		try {
			boolean result = NeoGasSwap.canFixList(badList);
			fail("Should be an invalid list");
		} catch (Exception ex) {
		}
	}
}
