package vn.uteexpress.config;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

				// Cho phép các request forward tới JSP
				.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

				// =========================
				// PUBLIC
				// =========================
				.requestMatchers("/", "/login").permitAll()

				// =========================
				// ADMIN
				// =========================
				.requestMatchers("/admin/**").hasRole("ADMIN")

				// =========================
				// MANAGER
				// =========================
				.requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")

				// =========================
				// VENDOR
				// =========================
				.requestMatchers("/vendor/**").hasAnyRole("VENDOR", "ADMIN")

				// =========================
				// SHIPPER
				// =========================
				.requestMatchers("/shipper/**").hasAnyRole("SHIPPER", "ADMIN")

				// =========================
				// CÁC URL KHÁC
				// =========================
				.anyRequest().authenticated())

				// =========================
				// LOGIN
				// =========================
				.formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login").defaultSuccessUrl("/", true)
						.failureUrl("/login?error=true").permitAll())

				// =========================
				// LOGOUT
				// =========================
				.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout=true").permitAll())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}