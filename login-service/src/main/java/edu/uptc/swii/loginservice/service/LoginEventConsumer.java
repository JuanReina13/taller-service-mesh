package edu.uptc.swii.loginservice.service;

import edu.uptc.swii.loginservice.model.Login;
import edu.uptc.swii.loginservice.utils.JsonUtils;
import edu.uptc.swii.loginservice.utils.KafkaEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class LoginEventConsumer {

    @Autowired
    private LoginService loginService;

    // CAMBIO: un único listener para todos los eventos de Login
    @KafkaListener(topics = "login_events", groupId = "login_group")
    public void handleLoginEvent(String message) {
        KafkaEvent event = JsonUtils.fromJson(message, KafkaEvent.class);
        String operation = event.getOperation();
        String payload = event.getPayload();

        switch (operation) {
            case "ADD":
                loginService.save(JsonUtils.fromJson(payload, Login.class));
                System.out.println("Login agregado.");
                break;

            case "EDIT":
                loginService.save(JsonUtils.fromJson(payload, Login.class));
                System.out.println("Login editado.");
                break;

            default:
                System.out.println("Operación no soportada en login_events: " + operation);
        }
    }
}