package com.example.demo;

import com.example.demo.model.persistence.User;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SareetaApplicationTests {
    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void createUserThenLogin() throws IOException, InterruptedException {
        var createUserRes = createUser("admin", "admin", "admin1");
        assertEquals(400, createUserRes.statusCode());

        createUserRes = createUser("admin", "admin", "admin");
        assertEquals(200, createUserRes.statusCode());
        var createUserResBody = objectMapper.readValue(createUserRes.body(), User.class);
        assertEquals("admin", createUserResBody.getUsername());
        assertNotNull(createUserResBody.getPassword());

        var loginRes = login("admin", "admin1");
        assertEquals(401, loginRes.statusCode());

        loginRes = login("admin", "admin");
        assertEquals(200, loginRes.statusCode());
        assertTrue(loginRes.headers().firstValue(HttpHeaders.AUTHORIZATION).isPresent());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    @SneakyThrows
    private String json(Object data) {
        return objectMapper.writeValueAsString(data);
    }

    private HttpResponse<String> createUser(String usn, String pwd, String confPwd) throws IOException, InterruptedException {
        var body = CreateUserRequest.builder()
                .username(usn)
                .password(pwd)
                .confPassword(confPwd)
                .build();
        var req = HttpRequest.newBuilder(uri("/api/user/create"))
                .POST(HttpRequest.BodyPublishers.ofString(json(body)))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<Void> login(String usn, String pwd) throws IOException, InterruptedException {
        var body = json(LoginRequest.builder()
                .username(usn)
                .password(pwd)
                .build());
        var req = HttpRequest.newBuilder(uri("/api/user/login"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return http.send(req, HttpResponse.BodyHandlers.discarding());
    }
}
