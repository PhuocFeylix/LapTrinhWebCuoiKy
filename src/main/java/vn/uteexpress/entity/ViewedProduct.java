package vn.uteexpress.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "viewed_products", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "product_id" }) })
public class ViewedProduct {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false)
	private LocalDateTime viewedAt = LocalDateTime.now();

	public ViewedProduct() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public LocalDateTime getViewedAt() {
		return viewedAt;
	}

	public void setViewedAt(LocalDateTime viewedAt) {
		this.viewedAt = viewedAt;
	}
}