package vn.uteexpress.dto;

public class StatisticsResponse {

	private long totalOrders;
	private long totalRevenue;
	private long totalCustomers;
	private long totalProducts;
	private long totalShippers;

	public StatisticsResponse() {
	}

	public StatisticsResponse(long totalOrders, long totalRevenue, long totalCustomers, long totalProducts,
			long totalShippers) {
		this.totalOrders = totalOrders;
		this.totalRevenue = totalRevenue;
		this.totalCustomers = totalCustomers;
		this.totalProducts = totalProducts;
		this.totalShippers = totalShippers;
	}

	public long getTotalOrders() {
		return totalOrders;
	}

	public void setTotalOrders(long totalOrders) {
		this.totalOrders = totalOrders;
	}

	public long getTotalRevenue() {
		return totalRevenue;
	}

	public long getTotalCustomers() {
		return totalCustomers;
	}

	public void setTotalCustomers(long totalCustomers) {
		this.totalCustomers = totalCustomers;
	}

	public long getTotalProducts() {
		return totalProducts;
	}

	public void setTotalProducts(long totalProducts) {
		this.totalProducts = totalProducts;
	}

	public long getTotalShippers() {
		return totalShippers;
	}

	public void setTotalShippers(long totalShippers) {
		this.totalShippers = totalShippers;
	}

	public void setTotalRevenue(long totalRevenue) {
		this.totalRevenue = totalRevenue;
	}
}
