package com.sharshar.scratchanalysis.service;

import com.sharshar.scratchanalysis.beans.PriceData;
import com.sharshar.scratchanalysis.beans.PriceDataSql;
import com.sharshar.scratchanalysis.repository.PriceDataES;
import com.sharshar.scratchanalysis.repository.PriceDataSQLRepository;
import com.sharshar.scratchanalysis.utils.ScratchConstants;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lsharshar on 5/14/2018.
 */
@Service
public class PriceDataPuller {
	@Resource
	private PriceDataES priceDataEs;

	@Resource
	private PriceDataSQLRepository priceDataSQLRepository;

	public List<PriceData> getPriceData(String ticker, short exchange, Date startDate, Date endDate) throws Exception {
		if (ScratchConstants.EXCHANGES_SEARCHES[exchange] == ScratchConstants.ELASTIC_SEARCH) {
			return priceDataEs.findByTimeRange(ticker, startDate, endDate, exchange);
		}
		if (ScratchConstants.EXCHANGES_SEARCHES[exchange] == ScratchConstants.SQL_SEARCH) {
			List<PriceDataSql> data = priceDataSQLRepository.findByTickerAndUpdateTimeGreaterThanAndUpdateTimeLessThan(ticker, startDate, endDate);
			return convertToPriceData(data, exchange);
		}
		return new ArrayList<>();
	}

	private static List<PriceData> convertToPriceData(List<PriceDataSql> data, short exchange) {
		if (data == null) {
			return new ArrayList<>();
		}
		return data.stream().map(
				d -> new PriceData()
						.setExchange(exchange).setPrice(d.getPrice()).setTicker(d.getTicker()).setUpdateTime(d.getUpdateTime()))
				.collect(Collectors.toList());
	}
}
