package com.example.demo.model.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.censodev.jauthlibcore.CanAuth;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;


@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements CanAuth {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty
	private long id;

	@Column(nullable = false, unique = true)
	@JsonProperty
	private String username;

	private String password;

	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cart_id", referencedColumnName = "id")
	@JsonIgnore
    private Cart cart;

	@Override
	public Object principle() {
		return username;
	}

	@Override
	public List<String> authorities() {
		return List.of("USER");
	}
}
