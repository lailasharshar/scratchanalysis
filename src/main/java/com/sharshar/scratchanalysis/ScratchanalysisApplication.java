package com.sharshar.scratchanalysis;

import com.sharshar.scratchanalysis.algorithms.NeoGasSwap;
import com.sharshar.scratchanalysis.algorithms.SignalSearcher;
import com.sharshar.scratchanalysis.repository.PriceDataES;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ScratchanalysisApplication {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(ScratchanalysisApplication.class, args);
		SignalSearcher s = context.getBean(SignalSearcher.class);
		PriceDataES pdes = context.getBean(PriceDataES.class);
		//Map<String, List<SignalSearcher.SignalIdentifier>> identifiers = s.getAllSignalIdentifiers(pdes);
		//Map<String, List<SignalSearcher.SignalIdentifier>> identifiers = new HashMap<>();
		//identifiers.put("NEBLBNB", s.getSignalIdentifiers(pdes, "NEBLBNB"));
		//s.printItOut(identifiers);

		NeoGasSwap swap = context.getBean(NeoGasSwap.class);
		NeoGasSwap.TradeSummary results = swap.getProfitUsingGasToBtc(pdes);
		System.out.println(results);
		double profits = swap.calculateProfitsBtc(2, results.getTrades());
		System.out.println("Profit (BTC Swap): " + profits);
		double profits2 = swap.calculateProfitsNeo(results.getInitialNeoPrice(),
				results.getFinalNeoPrice(), 2, results.getTrades());
		System.out.println("Profit (NEO swap): " + profits2);
	}
}
