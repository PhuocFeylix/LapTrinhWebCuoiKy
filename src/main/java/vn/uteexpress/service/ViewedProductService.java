package vn.uteexpress.service;

import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.User;
import vn.uteexpress.entity.ViewedProduct;
import vn.uteexpress.repository.ProductRepository;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.repository.ViewedProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ViewedProductService {

	private final ViewedProductRepository viewedProductRepository;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;

	public ViewedProductService(ViewedProductRepository viewedProductRepository, UserRepository userRepository,
			ProductRepository productRepository) {

		this.viewedProductRepository = viewedProductRepository;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
	}

	@Transactional
	public ViewedProduct recordView(Long userId, Long productId) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

		ViewedProduct viewedProduct = viewedProductRepository.findByUserIdAndProductId(userId, productId).orElse(null);

		if (viewedProduct == null) {

			viewedProduct = new ViewedProduct();

			viewedProduct.setUser(user);
			viewedProduct.setProduct(product);

		} else {

			viewedProduct.setViewedAt(LocalDateTime.now());
		}

		return viewedProductRepository.save(viewedProduct);
	}

	public List<ViewedProduct> getViewedProducts(Long userId) {

		if (!userRepository.existsById(userId)) {
			throw new RuntimeException("Không tìm thấy người dùng");
		}

		return viewedProductRepository.findByUserIdOrderByViewedAtDesc(userId);
	}

	@Transactional
	public void removeViewedProduct(Long userId, Long productId) {

		viewedProductRepository.deleteByUserIdAndProductId(userId, productId);
	}

	@Transactional
	public void clearHistory(Long userId) {

		if (!userRepository.existsById(userId)) {
			throw new RuntimeException("Không tìm thấy người dùng");
		}

		viewedProductRepository.deleteByUserId(userId);
	}
}