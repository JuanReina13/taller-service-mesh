package edu.uptc.swii.orderservice.service;

import edu.uptc.swii.orderservice.model.Order;
import edu.uptc.swii.orderservice.utils.JsonUtils;
import edu.uptc.swii.orderservice.utils.KafkaEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderEventConsumer {

    @Autowired
    private OrderService orderService;

    // CAMBIO: un único listener para todos los eventos de Order
    @KafkaListener(topics = "order_events", groupId = "order_group")
    public void handleOrderEvent(String message) {
        KafkaEvent event = JsonUtils.fromJson(message, KafkaEvent.class);
        String operation = event.getOperation();
        String payload = event.getPayload();

        switch (operation) {
            case "ADD":
                orderService.save(JsonUtils.fromJson(payload, Order.class));
                System.out.println("Order agregada.");
                break;

            case "EDIT":
                orderService.save(JsonUtils.fromJson(payload, Order.class));
                System.out.println("Order editada.");
                break;

            case "FIND_BY_ID":
                Order found = orderService.findById(payload);
                System.out.println("Order encontrada: " + found);
                break;

            case "FIND_ALL":
                List<Order> all = orderService.findAll();
                System.out.println("Total órdenes: " + all.size());
                break;

            default:
                System.out.println("Operación no soportada en order_events: " + operation);
        }
    }
}