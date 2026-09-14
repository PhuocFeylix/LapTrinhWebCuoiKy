package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.uteexpress.entity.Address;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.AddressRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class AddressService {

	private final AddressRepository addressRepository;
	private final UserRepository userRepository;

	public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
		this.addressRepository = addressRepository;
		this.userRepository = userRepository;
	}

	public List<Address> findByUserId(Long userId) {

		checkUserExists(userId);

		return addressRepository.findByUserId(userId);
	}

	public Address findByIdAndUser(Long id, Long userId) {

		return addressRepository.findByIdAndUserId(id, userId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
	}

	public Address create(Address address, Long userId) {

		validateAddress(address);

		User user = getUser(userId);

		address.setUser(user);

		/*
		 * Nếu đây là địa chỉ đầu tiên thì tự động đặt làm mặc định.
		 */
		List<Address> addresses = addressRepository.findByUserId(userId);

		if (addresses.isEmpty()) {
			address.setDefaultAddress(true);
		}

		/*
		 * Nếu người dùng chọn địa chỉ mới làm mặc định thì bỏ mặc định cũ.
		 */
		if (address.isDefaultAddress()) {
			removeDefaultAddress(userId);
		}

		return addressRepository.save(address);
	}

	public Address update(Long id, Address address, Long userId) {

		Address existing = findByIdAndUser(id, userId);

		validateAddress(address);

		existing.setReceiverName(address.getReceiverName().trim());

		existing.setPhone(address.getPhone().trim());

		existing.setAddress(address.getAddress().trim());

		existing.setWard(address.getWard());

		existing.setDistrict(address.getDistrict());

		existing.setCity(address.getCity());

		if (address.isDefaultAddress()) {
			removeDefaultAddress(userId);
		}

		existing.setDefaultAddress(address.isDefaultAddress());

		return addressRepository.save(existing);
	}

	public void delete(Long id, Long userId) {

		Address address = findByIdAndUser(id, userId);

		addressRepository.delete(address);
	}

	public Address setDefault(Long id, Long userId) {

		Address address = findByIdAndUser(id, userId);

		removeDefaultAddress(userId);

		address.setDefaultAddress(true);

		return addressRepository.save(address);
	}

	public Address getDefault(Long userId) {

		return addressRepository.findByUserIdAndDefaultAddressTrue(userId)
				.orElseThrow(() -> new RuntimeException("User chưa có địa chỉ mặc định"));
	}

	private User getUser(Long userId) {

		return userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));
	}

	private void checkUserExists(Long userId) {

		getUser(userId);
	}

	private void removeDefaultAddress(Long userId) {

		addressRepository.findByUserIdAndDefaultAddressTrue(userId).ifPresent(address -> {

			address.setDefaultAddress(false);

			addressRepository.save(address);
		});
	}

	private void validateAddress(Address address) {

		if (address.getReceiverName() == null || address.getReceiverName().trim().isEmpty()) {

			throw new IllegalArgumentException("Tên người nhận không được để trống");
		}

		if (address.getPhone() == null || address.getPhone().trim().isEmpty()) {

			throw new IllegalArgumentException("Số điện thoại không được để trống");
		}

		if (address.getAddress() == null || address.getAddress().trim().isEmpty()) {

			throw new IllegalArgumentException("Địa chỉ không được để trống");
		}
	}
}