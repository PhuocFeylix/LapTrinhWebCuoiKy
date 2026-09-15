package vn.uteexpress.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
