package vn.uteexpress.service;

import vn.uteexpress.entity.Favorite;
import vn.uteexpress.entity.Product;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.FavoriteRepository;
import vn.uteexpress.repository.ProductRepository;
import vn.uteexpress.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {

	private final FavoriteRepository favoriteRepository;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;

	public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository,
			ProductRepository productRepository) {

		this.favoriteRepository = favoriteRepository;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
	}

	// =========================
	// ADD FAVORITE
	// =========================

	@Transactional
	public Favorite addFavorite(Long userId, Long productId) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

		if (!product.isActive()) {
			throw new RuntimeException("Sản phẩm đã ngừng kinh doanh");
		}

		if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {

			throw new RuntimeException("Sản phẩm đã có trong danh sách yêu thích");
		}

		Favorite favorite = new Favorite();

		favorite.setUser(user);
		favorite.setProduct(product);

		return favoriteRepository.save(favorite);
	}

	// =========================
	// REMOVE FAVORITE
	// =========================

	@Transactional
	public void removeFavorite(Long userId, Long productId) {

		Favorite favorite = favoriteRepository.findByUserIdAndProductId(userId, productId)
				.orElseThrow(() -> new RuntimeException("Sản phẩm chưa có trong danh sách yêu thích"));

		favoriteRepository.delete(favorite);
	}

	// =========================
	// GET FAVORITES
	// =========================

	public List<Favorite> getFavorites(Long userId) {

		if (!userRepository.existsById(userId)) {
			throw new RuntimeException("Không tìm thấy người dùng");
		}

		return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
	}

	// =========================
	// CHECK FAVORITE
	// =========================

	public boolean isFavorite(Long userId, Long productId) {

		return favoriteRepository.existsByUserIdAndProductId(userId, productId);
	}

	// =========================
	// TOGGLE FAVORITE
	// =========================

	@Transactional
	public boolean toggleFavorite(Long userId, Long productId) {

		if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {

			favoriteRepository.deleteByUserIdAndProductId(userId, productId);

			return false;
		}

		addFavorite(userId, productId);

		return true;
	}
}