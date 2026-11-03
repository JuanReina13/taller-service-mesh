package edu.uptc.swii.orderservice.service;

import edu.uptc.swii.orderservice.model.Order;
import edu.uptc.swii.orderservice.utils.JsonUtils;
import edu.uptc.swii.orderservice.utils.KafkaEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    // CAMBIO: un único tópico para Order
    private static final String TOPIC = "order_events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendAddOrderEvent(Order o) {
        sendEvent("ADD", JsonUtils.toJson(o));
    }

    public void sendEditOrderEvent(Order o) {
        sendEvent("EDIT", JsonUtils.toJson(o));
    }

    public void sendFindOrderByIdEvent(String id) {
        sendEvent("FIND_BY_ID", id);
    }

    public void sendFindAllOrdersEvent() {
        sendEvent("FIND_ALL", "");
    }

    private void sendEvent(String operation, String payload) {
        KafkaEvent event = new KafkaEvent(operation, payload);
        kafkaTemplate.send(TOPIC, JsonUtils.toJson(event));
    }
}