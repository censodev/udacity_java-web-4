package com.example.demo;

import com.example.demo.model.persistence.User;
import io.github.censodev.jauthlibcore.TokenProvider;
import io.github.censodev.jauthlibspringweb.SpringWebAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableJpaRepositories("com.example.demo.model.persistence.repositories")
@EntityScan("com.example.demo.model.persistence")
@SpringBootApplication
public class SareetaApplication {
	@Value("${auth.secret}")
	private String authSecret;

	public static void main(String[] args) {
		SpringApplication.run(SareetaApplication.class, args);
	}

	@Bean
	public TokenProvider tokenProvider() {
		return TokenProvider.builder()
				.expireInMillisecond(3_600_000)
				.refreshTokenExpireInMillisecond(86_400_000)
				.secret(authSecret)
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		SpringWebAuthFilter<User> filter = new SpringWebAuthFilter<>(tokenProvider(), User.class);
		return http
				.csrf()
				.disable()
				.cors()
				.and()
				.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> auth
						.antMatchers("/api/user/login", "/api/user/create").permitAll()
						.anyRequest().authenticated()
				)
				.httpBasic(withDefaults())
				.build();
	}
}
