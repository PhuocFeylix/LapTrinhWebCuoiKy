package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.uteexpress.entity.Shop;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.ShopRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class ShopService {

	private final ShopRepository shopRepository;
	private final UserRepository userRepository;

	public ShopService(ShopRepository shopRepository, UserRepository userRepository) {
		this.shopRepository = shopRepository;
		this.userRepository = userRepository;
	}

	public List<Shop> findAll() {
		return shopRepository.findAll();
	}

	public List<Shop> findActiveShops() {
		return shopRepository.findByActiveTrue();
	}

	public Shop findById(Long id) {

		return shopRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy shop với ID: " + id));
	}

	public Shop findByVendorId(Long vendorId) {

		return shopRepository.findByVendorId(vendorId).orElseThrow(() -> new RuntimeException("Vendor chưa có shop"));
	}

	public Shop create(Shop shop, Long vendorId) {

		if (shop.getName() == null || shop.getName().trim().isEmpty()) {

			throw new IllegalArgumentException("Tên shop không được để trống");
		}

		User vendor = userRepository.findById(vendorId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy vendor"));

		if (!"VENDOR".equalsIgnoreCase(vendor.getRole().getName())) {

			throw new IllegalArgumentException("User này không có role VENDOR");
		}

		if (shopRepository.existsByVendorId(vendorId)) {

			throw new IllegalArgumentException("Vendor đã có shop");
		}

		shop.setName(shop.getName().trim());
		shop.setVendor(vendor);
		shop.setActive(true);

		return shopRepository.save(shop);
	}

	public Shop update(Long id, Shop shop) {

		Shop existing = findById(id);

		if (shop.getName() == null || shop.getName().trim().isEmpty()) {

			throw new IllegalArgumentException("Tên shop không được để trống");
		}

		existing.setName(shop.getName().trim());
		existing.setDescription(shop.getDescription());
		existing.setAddress(shop.getAddress());
		existing.setPhone(shop.getPhone());
		existing.setLogo(shop.getLogo());
		existing.setActive(shop.isActive());

		return shopRepository.save(existing);
	}

	public void delete(Long id) {

		Shop shop = findById(id);

		shopRepository.delete(shop);
	}

	public List<Shop> search(String keyword) {

		if (keyword == null || keyword.trim().isEmpty()) {

			return findAll();
		}

		return shopRepository.findByNameContainingIgnoreCase(keyword.trim());
	}
}