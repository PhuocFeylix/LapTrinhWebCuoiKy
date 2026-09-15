package vn.uteexpress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.uteexpress.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}

