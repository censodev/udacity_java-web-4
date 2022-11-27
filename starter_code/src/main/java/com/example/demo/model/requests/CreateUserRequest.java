package com.example.demo.model.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class CreateUserRequest {
	@JsonProperty
	@NotBlank
	private String username;
	@NotBlank
	private String password;
	@NotBlank
	private String confPassword;
}
