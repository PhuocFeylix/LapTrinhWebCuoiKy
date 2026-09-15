package vn.uteexpress.service;

import org.springframework.stereotype.Service;

import vn.uteexpress.dto.StatisticsResponse;

@Service
public class StatisticsService {

	public StatisticsResponse getDashboardStats() {
		return new StatisticsResponse(0, 0L, 0L, 0L, 0L);
	}
}
