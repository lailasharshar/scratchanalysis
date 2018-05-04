package com.sharshar.scratchanalysis.service;

import com.sharshar.scratchanalysis.algorithms.SignalSearcher;
import com.sharshar.scratchanalysis.beans.PriceData;
import com.sharshar.scratchanalysis.repository.PriceDataES;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to return data that can be analyzed. This is data from the database
 *
 * Created by lsharshar on 3/16/2018.
 */
@Service
public class DataAnalysis {

	@Autowired
	SignalSearcher signalSearcher;

	public class PriceDataLight {
		private String ticker;
		private double price;
		private Date updateDate;

		private PriceDataLight(String ticker, double price, Date updateDate) {
			this.ticker = ticker;
			this.price = price;
			this.updateDate = updateDate;
		}

		public String getTicker() {
			return ticker;
		}

		public double getPrice() {
			return price;
		}

		public Date getUpdateDate() {
			return updateDate;
		}

	}

	@Resource
	private PriceDataES priceDataEs;

	private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

	public List<PriceDataLight> getPriceData(String ticker, Date startDate, Date endDate, short exchange) throws Exception {
		if (startDate == null || endDate == null) {
			return new ArrayList<>();
		}
		List<PriceData> data = priceDataEs.findByTimeRange(ticker, startDate, endDate, exchange);
		if (data != null) {
			data.stream().sorted(Comparator.comparing(PriceData::getUpdateTime)).collect(Collectors.toList());
			return data.stream().map(d -> new PriceDataLight(d.getTicker(), d.getPrice(), d.getUpdateTime()))
					.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
}
