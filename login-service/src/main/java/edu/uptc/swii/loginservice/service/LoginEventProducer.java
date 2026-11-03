package edu.uptc.swii.loginservice.service;

import edu.uptc.swii.loginservice.model.Login;
import edu.uptc.swii.loginservice.utils.JsonUtils;
import edu.uptc.swii.loginservice.utils.KafkaEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoginEventProducer {

    // CAMBIO: un único tópico para Login
    private static final String TOPIC = "login_events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendAddLoginEvent(Login l) {
        sendEvent("ADD", JsonUtils.toJson(l));
    }

    public void sendEditLoginEvent(Login l) {
        sendEvent("EDIT", JsonUtils.toJson(l));
    }

    private void sendEvent(String operation, String payload) {
        KafkaEvent event = new KafkaEvent(operation, payload);
        kafkaTemplate.send(TOPIC, JsonUtils.toJson(event));
    }
}