package edu.uptc.swii.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

// Llamada HTTP a customer-service. Dentro del clúster pasa por los sidecars de Envoy (Istio).
@Component
public class CustomerClient {

    private final RestClient restClient;

    public CustomerClient(@Value("${services.customer.url}") String customerUrl) {
        this.restClient = RestClient.create(customerUrl);
    }

    public boolean exists(String document) {
        try {
            restClient.get()
                    .uri("/customers/findcustomerbyid/{document}", document)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}
