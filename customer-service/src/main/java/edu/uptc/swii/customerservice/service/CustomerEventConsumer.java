package edu.uptc.swii.customerservice.service;

import edu.uptc.swii.customerservice.client.LoginClient;
import edu.uptc.swii.customerservice.model.Customer;
import edu.uptc.swii.customerservice.utils.JsonUtils;
import edu.uptc.swii.customerservice.utils.KafkaEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class CustomerEventConsumer {

    @Autowired
    private CustomerService customerService;

    // Punto 3 (Sesión 14): el Login ahora vive en login-service, así que se crea por HTTP
    @Autowired
    private LoginClient loginClient;

    // Punto 4 (Sesión 14): un único listener para todos los eventos de Customer
    @KafkaListener(topics = "customer_events", groupId = "customer_group")
    public void handleCustomerEvent(String message) {
        KafkaEvent event = JsonUtils.fromJson(message, KafkaEvent.class);
        String operation = event.getOperation();
        String payload = event.getPayload();

        switch (operation) {
            case "ADD":
                Customer newCustomer = JsonUtils.fromJson(payload, Customer.class);
                customerService.save(newCustomer);
                try {
                    // contraseña por defecto = documento
                    loginClient.createLogin(newCustomer.getDocument(), newCustomer.getDocument());
                    System.out.println("Customer y Login creados para: " + newCustomer.getDocument());
                } catch (RestClientException e) {
                    System.out.println("Customer creado, pero login-service falló: " + e.getMessage());
                }
                break;

            case "EDIT":
                Customer editCustomer = JsonUtils.fromJson(payload, Customer.class);
                customerService.save(editCustomer);
                System.out.println("Customer editado: " + editCustomer.getDocument());
                break;

            case "FIND_BY_ID":
                Customer found = customerService.findById(payload);
                System.out.println("Cliente encontrado: " + found);
                break;

            case "FIND_ALL":
                List<Customer> all = customerService.findAll();
                System.out.println("Total clientes: " + all.size());
                break;

            default:
                System.out.println("Operación no soportada en customer_events: " + operation);
        }
    }
}
