package com.example.demo;

import com.example.demo.model.persistence.User;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.LoginRequest;
import com.example.demo.model.requests.ModifyCartRequest;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SareetaApplicationTests {
    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void fullFlow() throws IOException, InterruptedException {
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
        var token = loginRes.headers().firstValue(HttpHeaders.AUTHORIZATION).orElse(null);
        assertNotNull(token);

        var pushCartRes = pushCart("admin", 1, 1, null);
        assertEquals(401, pushCartRes.statusCode());
        pushCartRes = pushCart("admin1", 1, 1, token);
        assertEquals(404, pushCartRes.statusCode());
        pushCartRes = pushCart("admin", 3, 1, token);
        assertEquals(404, pushCartRes.statusCode());
        pushCartRes = pushCart("admin", 1, 1, token);
        assertEquals(200, pushCartRes.statusCode());

        var popCartRes = popCart("admin", 1, 1, null);
        assertEquals(401, popCartRes.statusCode());
        popCartRes = popCart("admin1", 1, 1, token);
        assertEquals(404, popCartRes.statusCode());
        popCartRes = popCart("admin", 3, 1, token);
        assertEquals(404, popCartRes.statusCode());
        popCartRes = popCart("admin", 1, 1, token);
        assertEquals(200, popCartRes.statusCode());

        var orderRes = order("admin", null);
        assertEquals(401, orderRes.statusCode());
        orderRes = order("admin1", token);
        assertEquals(404, orderRes.statusCode());
        orderRes = order("admin", token);
        assertEquals(200, orderRes.statusCode());

        var orderHistoryRes = orderHistory("admin", null);
        assertEquals(401, orderHistoryRes.statusCode());
        orderHistoryRes = order("admin1", token);
        assertEquals(404, orderHistoryRes.statusCode());
        orderHistoryRes = order("admin", token);
        assertEquals(200, orderHistoryRes.statusCode());
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

    private HttpResponse<String> pushCart(String username, long itemId, int qty, String token) throws IOException, InterruptedException {
        var body = json(ModifyCartRequest.builder()
                .username(username)
                .itemId(itemId)
                .quantity(qty)
                .build());
        var req = HttpRequest.newBuilder(uri("/api/cart/addToCart"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> popCart(String username, long itemId, int qty, String token) throws IOException, InterruptedException {
        var body = json(ModifyCartRequest.builder()
                .username(username)
                .itemId(itemId)
                .quantity(qty)
                .build());
        var req = HttpRequest.newBuilder(uri("/api/cart/removeFromCart"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> order(String usn, String token) throws IOException, InterruptedException {
        var req = HttpRequest.newBuilder(uri("/api/order/submit/" + usn))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> orderHistory(String usn, String token) throws IOException, InterruptedException {
        var req = HttpRequest.newBuilder(uri("/api/order/history/" + usn))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
