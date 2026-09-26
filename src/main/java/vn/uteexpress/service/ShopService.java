package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
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
	// =========================
	// GET SHOP BY VENDOR
	// =========================

	public Shop getShopByVendor(Long vendorId) {

		return shopRepository.findByVendorId(vendorId).orElseThrow(() -> new RuntimeException("Vendor chưa có shop"));
	}

	// =========================
	// REGISTER SHOP
	// =========================

	@Transactional
	public Shop registerShop(Long vendorId, String name, String description, String address, String phone,
			String logo) {

		User vendor = userRepository.findById(vendorId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy Vendor"));

		if (shopRepository.existsByVendorId(vendorId)) {
			throw new IllegalArgumentException("Vendor đã có shop");
		}

		validateShop(name, address, phone);

		Shop shop = new Shop();

		shop.setName(name.trim());

		shop.setDescription(description == null ? null : description.trim());

		shop.setAddress(address.trim());

		shop.setPhone(phone.trim());

		shop.setLogo(logo == null ? null : logo.trim());

		shop.setActive(true);

		shop.setVendor(vendor);

		return shopRepository.save(shop);
	}

	// =========================
	// UPDATE SHOP
	// =========================

	@Transactional
	public Shop updateShop(Long vendorId, Long shopId, String name, String description, String address, String phone,
			String logo) {

		Shop shop = shopRepository.findByIdAndVendorId(shopId, vendorId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy shop hoặc shop không thuộc Vendor"));

		validateShop(name, address, phone);

		shop.setName(name.trim());

		shop.setDescription(description == null ? null : description.trim());

		shop.setAddress(address.trim());

		shop.setPhone(phone.trim());

		if (logo != null) {
			shop.setLogo(logo.trim());
		}

		return shopRepository.save(shop);
	}

	// =========================
	// VALIDATE
	// =========================

	private void validateShop(String name, String address, String phone) {

		if (name == null || name.trim().isEmpty()) {

			throw new IllegalArgumentException("Tên shop không được để trống");
		}

		if (address == null || address.trim().isEmpty()) {

			throw new IllegalArgumentException("Địa chỉ shop không được để trống");
		}

		if (phone == null || phone.trim().isEmpty()) {

			throw new IllegalArgumentException("Số điện thoại shop không được để trống");
		}
	}
}