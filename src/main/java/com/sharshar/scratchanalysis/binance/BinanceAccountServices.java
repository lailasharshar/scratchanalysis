package com.sharshar.scratchanalysis.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerPrice;
import com.sharshar.scratchanalysis.beans.PriceData;
import com.sharshar.scratchanalysis.utils.ScratchConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Used to access and clean up any data from the binance client
 *
 * Created by lsharshar on 5/27/2018.
 */
@Service
public class BinanceAccountServices {
	private Logger logger = LogManager.getLogger();

	@Autowired
	private BinanceApiRestClient binanceApiRestClient;

	public List<PriceData> getData() {
		List<TickerPrice> allPrices = binanceApiRestClient.getAllPrices();
		List<PriceData> priceData = new ArrayList<>();
		if (allPrices == null || allPrices.isEmpty()) {
			logger.error("Unable to load prices from Binance");
			return priceData;
		}
		Date now = new Date();
		for (TickerPrice tp : allPrices) {
			PriceData pd = new PriceData().setTicker(tp.getSymbol()).setExchange(ScratchConstants.BINANCE).setUpdateTime(now);
			try {
				double price = Double.parseDouble(tp.getPrice());
				pd.setPrice(price);
			} catch (Exception ex) {
				logger.error("Unable to parse value of : " + tp.getSymbol() + " - " + tp.getPrice());
				pd.setPrice(0.0);
			}
			priceData.add(pd);
		}
		return priceData;
	}
}
