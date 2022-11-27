package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.LoginRequest;
import io.github.censodev.jauthlibcore.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CartRepository cartRepository;

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		return ResponseEntity.of(userRepository.findById(id));
	}

	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
	}

	@PostMapping("/create")
	public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        if (!createUserRequest.getPassword().equals(createUserRequest.getConfPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "confirmed password is not correct");
        }
		User user = new User();
		user.setUsername(createUserRequest.getUsername());
		user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
		Cart cart = new Cart();
		cartRepository.save(cart);
		user.setCart(cart);
		userRepository.save(user);
		return ResponseEntity.ok(user);
	}

	@PostMapping("login")
	public void login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
		var user = userRepository.findByUsername(loginRequest.getUsername());
		if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.generateAccessToken(user));
	}
}
