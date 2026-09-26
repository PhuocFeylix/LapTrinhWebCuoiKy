package vn.uteexpress.controller;

import vn.uteexpress.entity.Favorite;
import vn.uteexpress.service.FavoriteService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

	private final FavoriteService favoriteService;

	public FavoriteController(FavoriteService favoriteService) {

		this.favoriteService = favoriteService;
	}

	// =========================
	// ADD
	// =========================

	@PostMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Favorite> addFavorite(@PathVariable Long userId, @PathVariable Long productId) {

		return ResponseEntity.ok(favoriteService.addFavorite(userId, productId));
	}

	// =========================
	// REMOVE
	// =========================

	@DeleteMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Void> removeFavorite(@PathVariable Long userId, @PathVariable Long productId) {

		favoriteService.removeFavorite(userId, productId);

		return ResponseEntity.noContent().build();
	}

	// =========================
	// GET LIST
	// =========================

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Favorite>> getFavorites(@PathVariable Long userId) {

		return ResponseEntity.ok(favoriteService.getFavorites(userId));
	}

	// =========================
	// CHECK
	// =========================

	@GetMapping("/user/{userId}/product/{productId}")
	public ResponseEntity<Boolean> isFavorite(@PathVariable Long userId, @PathVariable Long productId) {

		return ResponseEntity.ok(favoriteService.isFavorite(userId, productId));
	}

	// =========================
	// TOGGLE
	// =========================

	@PostMapping("/user/{userId}/product/{productId}/toggle")
	public ResponseEntity<Boolean> toggleFavorite(@PathVariable Long userId, @PathVariable Long productId) {

		return ResponseEntity.ok(favoriteService.toggleFavorite(userId, productId));
	}
}