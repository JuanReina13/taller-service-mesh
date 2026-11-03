package edu.uptc.swii.customerservice.service;

import edu.uptc.swii.customerservice.model.Customer;
import edu.uptc.swii.customerservice.utils.JsonUtils;
import edu.uptc.swii.customerservice.utils.KafkaEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomerEventProducer {

    // CAMBIO: un único tópico para todos los eventos de Customer
    private static final String TOPIC = "customer_events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendAddCustomerEvent(Customer customer) {
        sendEvent("ADD", JsonUtils.toJson(customer));
    }

    public void sendEditCustomerEvent(Customer customer) {
        sendEvent("EDIT", JsonUtils.toJson(customer));
    }

    public void sendFindByCustomerIDEvent(String document) {
        sendEvent("FIND_BY_ID", document);
    }

    public void sendFindAllCustomersEvent() {
        sendEvent("FIND_ALL", "");
    }

    private void sendEvent(String operation, String payload) {
        KafkaEvent event = new KafkaEvent(operation, payload);
        kafkaTemplate.send(TOPIC, JsonUtils.toJson(event));
    }
}