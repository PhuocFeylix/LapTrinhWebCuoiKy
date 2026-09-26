package vn.uteexpress.config;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import vn.uteexpress.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {

		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.authorizeHttpRequests(auth -> auth
				// =========================
				// PUBLIC SHIPMENT TRACKING
				// =========================

				.requestMatchers(HttpMethod.GET, "/api/shipments/tracking/**").permitAll()

				// =========================
				// SHIPMENT API
				// =========================

				.requestMatchers("/api/shipments/**").hasAnyRole("VENDOR", "SHIPPER", "MANAGER", "ADMIN")
				// =========================
				// JSP FORWARD / ERROR
				// =========================

				.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

				// =========================
				// PUBLIC WEB
				// =========================

				.requestMatchers("/", "/login").permitAll()

				// =========================
				// PUBLIC CATEGORY API
				// =========================

				.requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

				// =========================
				// CATEGORY MANAGEMENT
				// =========================

				.requestMatchers(HttpMethod.POST, "/api/categories/**").hasAnyRole("MANAGER", "ADMIN")

				.requestMatchers(HttpMethod.PUT, "/api/categories/**").hasAnyRole("MANAGER", "ADMIN")

				.requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasAnyRole("MANAGER", "ADMIN")

				// =========================
				// PUBLIC PRODUCT API
				// =========================

				.requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

				// =========================
				// PRODUCT MANAGEMENT
				// Ownership is checked
				// in ProductController
				// =========================

				.requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("VENDOR", "MANAGER", "ADMIN")

				.requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("VENDOR", "MANAGER", "ADMIN")

				.requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("VENDOR", "MANAGER", "ADMIN")

				// =========================
				// ADMIN WEB
				// =========================

				.requestMatchers("/admin/**").hasRole("ADMIN")

				// =========================
				// MANAGER WEB
				// =========================

				.requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")

				// =========================
				// VENDOR WEB
				// =========================

				.requestMatchers("/vendor/**").hasAnyRole("VENDOR", "ADMIN")

				// =========================
				// SHIPPER WEB
				// =========================

				.requestMatchers("/shipper/**").hasAnyRole("SHIPPER", "ADMIN")

				// =========================
				// SHIPPER API
				// =========================

				.requestMatchers("/api/shipper/**").hasAnyRole("SHIPPER", "ADMIN")

				// =========================
				// ADMIN / MANAGER SHIPMENT
				// =========================

				.requestMatchers("/api/admin/shipments/**").hasAnyRole("ADMIN", "MANAGER")

				// =========================
				// MANAGER SHOP ORDER
				// =========================

				.requestMatchers("/api/manager/shop-orders/**").hasAnyRole("MANAGER", "ADMIN")

				// =========================
				// VENDOR ORDER
				// =========================

				.requestMatchers("/api/vendor/orders/**").hasAnyRole("VENDOR", "MANAGER", "ADMIN")

				// =========================
				// VENDOR SHOP ORDER
				// =========================

				.requestMatchers("/api/vendor/shop-orders/**").hasAnyRole("VENDOR", "MANAGER", "ADMIN")

				// =========================
				// PROMOTION
				// =========================

				// Xem promotion công khai
				.requestMatchers(HttpMethod.GET, "/api/promotions/shop/**").permitAll()

				.requestMatchers(HttpMethod.GET, "/api/promotions/product/**").permitAll()

				.requestMatchers(HttpMethod.GET, "/api/promotions/{promotionId}").permitAll()

				.requestMatchers(HttpMethod.GET, "/api/promotions/{promotionId}/products").permitAll()

				// Tạo promotion
				.requestMatchers(HttpMethod.POST, "/api/promotions/shop/**").hasAnyRole("VENDOR", "MANAGER", "ADMIN")

				// Cập nhật promotion
				.requestMatchers(HttpMethod.PUT, "/api/promotions/**").hasAnyRole("VENDOR", "MANAGER", "ADMIN")

				// Xóa promotion
				.requestMatchers(HttpMethod.DELETE, "/api/promotions/**").hasAnyRole("VENDOR", "MANAGER", "ADMIN")

				// Add product vào promotion
				.requestMatchers(HttpMethod.POST, "/api/promotions/*/products/**")
				.hasAnyRole("VENDOR", "MANAGER", "ADMIN")

				// Remove product khỏi promotion
				.requestMatchers(HttpMethod.DELETE, "/api/promotions/*/products/**")
				.hasAnyRole("VENDOR", "MANAGER", "ADMIN")
				// =========================
				// COUPON
				// =========================

				// Xem coupon công khai
				.requestMatchers(HttpMethod.GET, "/api/coupons/code/**", "/api/coupons/active",
						"/api/coupons/calculate")
				.permitAll()

				// Xem coupon theo ID / Shop
				.requestMatchers(HttpMethod.GET, "/api/coupons/{couponId}", "/api/coupons/shop/**").permitAll()

				// Quản lý coupon
				.requestMatchers(HttpMethod.POST, "/api/coupons/shop/**").hasAnyRole("VENDOR", "MANAGER", "ADMIN")

				.requestMatchers(HttpMethod.PUT, "/api/coupons/**").hasAnyRole("VENDOR", "MANAGER", "ADMIN")

				.requestMatchers(HttpMethod.DELETE, "/api/coupons/**").hasAnyRole("VENDOR", "MANAGER", "ADMIN")
				// =========================
				// AUTHENTICATED DEFAULT
				// =========================
				// =========================
				// PAYMENT
				// =========================

				// Webhook được gọi từ Payment Provider
				// Việc xác thực chữ ký phải được xử lý tại
				// PaymentWebhookController / PaymentService.
				.requestMatchers(HttpMethod.POST, "/api/payments/webhook").permitAll()

				// Các API Payment còn lại yêu cầu đăng nhập
				.requestMatchers("/api/payments/**").authenticated().anyRequest().authenticated())

				// =========================
				// LOGIN
				// =========================

				.formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login").defaultSuccessUrl("/", true)
						.failureUrl("/login?error=true").permitAll())

				// =========================
				// LOGOUT
				// =========================

				.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout=true").permitAll())

				// =========================
				// JWT FILTER
				// =========================

				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}