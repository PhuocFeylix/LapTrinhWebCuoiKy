package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.uteexpress.entity.Favorite;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.FavoriteService;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

	private final FavoriteService favoriteService;
	private final UserRepository userRepository;

	public FavoriteController(FavoriteService favoriteService, UserRepository userRepository) {

		this.favoriteService = favoriteService;
		this.userRepository = userRepository;
	}

	// =========================
	// ADD
	// =========================

	@PostMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Favorite> addFavorite(@PathVariable Long userId, @PathVariable Long productId,
			Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(favoriteService.addFavorite(userId, productId));
	}

	// =========================
	// REMOVE
	// =========================

	@DeleteMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Void> removeFavorite(@PathVariable Long userId, @PathVariable Long productId,
			Authentication authentication) {

		checkUserAccess(userId, authentication);

		favoriteService.removeFavorite(userId, productId);

		return ResponseEntity.noContent().build();
	}

	// =========================
	// GET LIST
	// =========================

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Favorite>> getFavorites(@PathVariable Long userId, Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(favoriteService.getFavorites(userId));
	}

	// =========================
	// CHECK
	// =========================

	@GetMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Boolean> isFavorite(@PathVariable Long userId, @PathVariable Long productId,
			Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(favoriteService.isFavorite(userId, productId));
	}

	// =========================
	// TOGGLE
	// =========================

	@PostMapping("/user/{userId}/product/{productId}/toggle")
	public ResponseEntity<Boolean> toggleFavorite(@PathVariable Long userId, @PathVariable Long productId,
			Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(favoriteService.toggleFavorite(userId, productId));
	}

	// =========================
	// CHECK USER ACCESS
	// =========================

	private void checkUserAccess(Long userId, Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		User currentUser = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

		boolean isAdmin = currentUser.getRole() != null && "ADMIN".equalsIgnoreCase(currentUser.getRole().getName());

		if (!isAdmin && !currentUser.getId().equals(userId)) {

			throw new IllegalArgumentException("Không được truy cập Favorite của người dùng khác");
		}
	}
}