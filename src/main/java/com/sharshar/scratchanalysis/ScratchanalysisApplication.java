package com.sharshar.scratchanalysis;

import com.sharshar.scratchanalysis.algorithms.AnalysisUtils;
import com.sharshar.scratchanalysis.algorithms.CoinSwap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class ScratchanalysisApplication {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(ScratchanalysisApplication.class, args);
		//SignalSearcher s = context.getBean(SignalSearcher.class);
		//PriceDataES pdes = context.getBean(PriceDataES.class);
		//Map<String, List<SignalSearcher.SignalIdentifier>> identifiers = s.getAllSignalIdentifiers(pdes);
		//Map<String, List<SignalSearcher.SignalIdentifier>> identifiers = new HashMap<>();
		//identifiers.put("NEBLBNB", s.getSignalIdentifiers(pdes, "NEBLBNB"));
		//s.printItOut(identifiers);

		/*
		try (BufferedWriter s = Files.newBufferedWriter(Paths.get("c:/tmp/dataresults.txt"))) {
			List<String> allTickers = Arrays.asList("ZRXBTC", "ZILBTC", "ZECBTC", "XZCBTC", "XVGBTC",
				"XRPBTC", "XMRBTC", "XLMBTC", "XEMBTC", "WTCBTC", "WPRBTC", "WINGSBTC", "WAVESBTC", "WANBTC",
				"WABIBTC", "VIBEBTC", "VIBBTC", "VIABTC", "VENBTC", "TRXBTC", "TRIGBTC", "TNTBTC", "TNBBTC",
				"SYSBTC", "SUBBTC", "STRATBTC", "STORMBTC", "STORJBTC", "STEEMBTC", "SNTBTC", "SNMBTC",
				"SNGLSBTC", "SALTBTC", "RPXBTC", "RLCBTC", "REQBTC", "RDNBTC", "RCNBTC", "QTUMBTC", "QSPBTC",
				"QLCBTC", "PPTBTC", "POWRBTC", "POEBTC", "POABTC", "PIVXBTC", "OSTBTC", "ONTBTC", "OMGBTC",
				"OAXBTC", "NULSBTC", "NEOBTC", "NEBLBTC", "NCASHBTC", "NAVBTC", "NANOBTC", "MTLBTC",
				"MTHBTC", "MODBTC", "MDABTC", "MCOBTC", "MANABTC", "LUNBTC", "LTCBTC", "LSKBTC", "LRCBTC",
				"LINKBTC", "LENDBTC", "KNCBTC", "KMDBTC", "IOSTBTC", "INSBTC", "ICXBTC", "ICNBTC", "HSRBTC",
				"GXSBTC", "GVTBTC", "GTOBTC", "GRSBTC", "GNTBTC", "GASBTC", "FUNBTC", "FUELBTC", "EVXBTC",
				"ETHBTC", "ETCBTC", "EOSBTC", "ENJBTC", "ENGBTC", "ELFBTC", "EDOBTC", "DNTBTC", "DLTBTC",
				"DGDBTC", "DASHBTC", "CNDBTC", "CMTBTC", "CHATBTC", "CDTBTC", "BTSBTC", "BTGBTC", "BRDBTC",
				"BQXBTC", "BNTBTC", "BNBBTC", "BLZBTC", "BCPTBTC", "BCDBTC", "BCCBTC", "BATBTC", "ASTBTC",
				"ARNBTC", "ARKBTC", "APPCBTC", "AMBBTC", "AIONBTC", "AEBTC", "ADXBTC", "ADABTC");

			for (String coin1 : allTickers) {
				for (String coin2 : allTickers) {
					if (coin1.equalsIgnoreCase(coin2)) {
						continue;
					}
					double stdDevToUse = 0.8;
					s.write("Std Dev to Use: " + stdDevToUse + "\n");
					s.write("-------------------------------------------------------------\n");
					double initialInvestment = 1;
					CoinSwap swap = context.getBean(CoinSwap.class);
					CoinSwap.TradeSummary results = swap.getProfitUsingCoin1ToBtc(initialInvestment, coin1, coin2, stdDevToUse);
					s.write(results + "\n");
					double profits = swap.calculateProfitsBtc(initialInvestment, results.getTrades());
					s.write("Profit (Between " + coin1 + " and BTC): " + profits + "\n");
					double amountInvested1 = initialInvestment * results.getInitialBtcPrice();
					double amountReceived1 = (profits + initialInvestment) * results.getFinalBtcPrice();
					double percentageChange = AnalysisUtils.getPercentageChange(amountInvested1, amountReceived1);
					s.write("Invested: $" + amountInvested1 + " - Payed Out: $"
							+ amountReceived1 + " (" + percentageChange + ")\n\n");

					double profits2 = swap.calculateProfitsCoin2(results.getInitialCoin2Price(),
							results.getFinalCoin2Price(), initialInvestment, results.getTrades());
					s.write("Profit (Between " + coin1 + " and " + coin2 + "): " + profits2 + "\n");
					double amountInvested2 = initialInvestment * results.getInitialBtcPrice();
					double amountReceived2 = (profits2 + initialInvestment) * results.getFinalBtcPrice();
					double percentageChange2 = AnalysisUtils.getPercentageChange(amountInvested2, amountReceived2);
					s.write("Invested: $" + amountInvested2 + " - Payed Out: $"
							+ amountReceived2 + " (" + percentageChange2 + ")\n\n");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		*/
		String coin1 = "USDTUSD";
		String coin2 = "TUSDUSD";
		//double stdDevToUse = 2.5;
		//System.out.println("Std Dev to Use: " + stdDevToUse + "\n");
		System.out.println("-------------------------------------------------------------\n");
		double initialInvestment = 1000;
		CoinSwap swap = context.getBean(CoinSwap.class);
		// CoinSwap.TradeSummary results = swap.getProfitUsingCoin1ToBtc(initialInvestment, coin1, coin2, stdDevToUse);
		 CoinSwap.TradeSummary results = swap.getProfitAbsolute(initialInvestment, coin1, 1.0, 1.015);
		// CoinSwap.TradeSummary results = swap.getProfitAbsolute(initialInvestment, coin2, 0.9971, 1.009);
		double profits2 = swap.calculateProfitsCoin2(results.getInitialCoin1Price(),
				results.getFinalCoin1Price(), initialInvestment, results.getTrades());
		double amountInvested2 = initialInvestment;
		double amountReceived2 = (profits2 + initialInvestment);
		System.out.println("Number of trades: " + results.getTrades().size());
		System.out.println(results.getTrades());
		double percentageChange2 = AnalysisUtils.getPercentageChange(amountInvested2, amountReceived2);
		System.out.println("Invested: $" + amountInvested2 + " - Payed Out: $"
				+ amountReceived2 + " (" + percentageChange2 + ")\n\n");

	}
}
