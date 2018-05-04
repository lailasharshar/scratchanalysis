package com.sharshar.scratchanalysis.repository;

import com.sharshar.scratchanalysis.beans.PriceData;
import com.sharshar.scratchanalysis.configuration.EsConfig;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lsharshar on 3/24/2018.
 */
@Service
public class PriceDataES {

	@Autowired
	private PriceDataRepository priceDataRepository;

	@Autowired
	private EsConfig esConfig;

	public List<PriceData> findByTimeRange(String ticker, Date startDate, Date endDate, int exchange) throws Exception{
		DateTime dt1 = new DateTime(startDate);
		DateTime dt2 = new DateTime(endDate);
		Page<PriceData> data = priceDataRepository.findByUpdateTimeBetweenAndExchangeAndTicker(
				dt1, dt2, exchange, ticker, PageRequest.of(0, 10000));
		return data.getContent();
	}
}
