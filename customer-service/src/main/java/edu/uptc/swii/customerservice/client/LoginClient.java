package edu.uptc.swii.customerservice.client;

import edu.uptc.swii.customerservice.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

// Llamada HTTP a login-service. Dentro del clúster pasa por los sidecars de Envoy (Istio).
@Component
public class LoginClient {

    private final RestClient restClient;

    public LoginClient(@Value("${services.login.url}") String loginUrl) {
        this.restClient = RestClient.create(loginUrl);
    }

    public void createLogin(String username, String password) {
        String body = JsonUtils.toJson(Map.of("username", username, "password", password));
        restClient.post()
                .uri("/logins/addlogin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
